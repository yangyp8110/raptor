package com.ppdai.framework.raptor.spring.utils;

import com.ppdai.framework.raptor.annotation.RaptorInterface;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yinzuolong
 */
public class RaptorInterfaceUtils {

    private final static Map<String, Class<?>> METHOD_INTERFACE_CACHE = new ConcurrentHashMap<>();


    public static String getInterfaceName(Class<?> type, Method method) {
        Class<?> interfaceClass = getInterfaceClass(type, method);
        return interfaceClass == null ? null : interfaceClass.getName();
    }

    public static Class<?> getInterfaceClass(Class<?> type, Method method) {
        String methodSignature = getMethodSignature(method);
        String classMethodKey = type.getName() + "#" + methodSignature;
        Class<?> result = METHOD_INTERFACE_CACHE.get(classMethodKey);
        if (StringUtils.isEmpty(result)) {
            List<Class<?>> classList = findRaptorInterfaces(type);
            for (Class<?> interfaceClass : classList) {
                try {
                    Method interfaceMethod = interfaceClass.getMethod(method.getName(), method.getParameterTypes());
                    if (interfaceMethod != null) {
                        result = interfaceClass;
                        METHOD_INTERFACE_CACHE.put(classMethodKey, result);
                        return result;
                    }
                } catch (NoSuchMethodException ignored) {
                }
            }
        }
        return result;
    }

    public static List<Class<?>> findRaptorInterfaces(Class<?> clazz) {
        List<Class<?>> raptorInterfaces = new ArrayList<>();
        Set<Class<?>> interfaceClasses = ClassUtils.getAllInterfacesForClassAsSet(clazz);
        for (Class<?> interfaceClass : interfaceClasses) {
            Annotation annotation = AnnotationUtils.findAnnotation(interfaceClass, RaptorInterface.class);
            if (annotation != null) {
                raptorInterfaces.add(interfaceClass);
            }
        }
        return raptorInterfaces;
    }


    public static String getMethodSignature(Method method) {
        StringBuilder builder = new StringBuilder();
        builder.append(method.getName());
        builder.append("(");
        for (Class<?> parameterType : method.getParameterTypes()) {
            builder.append(parameterType.getName()).append(",");
        }
        if (builder.subSequence(builder.length() - 1, builder.length()).equals(",")) {
            builder.deleteCharAt(builder.length() - 1);
        }
        builder.append(")");
        return builder.toString();
    }
}
