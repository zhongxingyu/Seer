 package com.aciertoteam.util;
 
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 import java.beans.PropertyDescriptor;
 import java.lang.reflect.AccessibleObject;
 import java.lang.reflect.Array;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Currency;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 import org.apache.commons.lang3.ClassUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * @author ishestiporov
  */
 public final class ReflectionUtils {
 
     private static final Log LOG = LogFactory.getLog(ReflectionUtils.class);
 
    private static final Object[] PRIMITIVES = { "Test", 1l, 1, Locale.getDefault(), true, 'a', 1f, 1d,
             Currency.getInstance("EUR"), BigDecimal.ZERO };
 
     private static final Map<Class, Object> COLLECTION_MAP = new HashMap<Class, Object>();
     static {
         COLLECTION_MAP.put(Set.class, Collections.emptySet());
         COLLECTION_MAP.put(List.class, Collections.emptyList());
         COLLECTION_MAP.put(Map.class, Collections.emptyMap());
     }
 
     private ReflectionUtils() {
         // restrict instantiation
     }
 
     public static Object[] getMethodParameters(Constructor constructor) {
         return getMethodParameters(constructor, constructor.getParameterTypes());
     }
 
     private static Object[] getMethodParameters(AccessibleObject method, Class[] parameterTypes) {
         List<Object> objects = new ArrayList<Object>();
         makeAccessible(method);
         for (Class parameterClass : parameterTypes) {
             objects.add(getParamValue(parameterClass));
         }
         return objects.toArray(new Object[objects.size()]);
     }
 
     private static void makeAccessible(AccessibleObject method) {
         if (!method.isAccessible()) {
             method.setAccessible(true);
         }
     }
 
     private static Object getParamValue(Class parameterClass) {
         for (Object primitive : PRIMITIVES) {
             if (primitive.getClass().equals(ClassUtils.primitiveToWrapper(parameterClass))) {
                 return primitive;
             }
         }
         if (parameterClass.isArray()) {
             return Array.newInstance(parameterClass.getComponentType(), 0);
         }
         if (COLLECTION_MAP.containsKey(parameterClass)) {
             return COLLECTION_MAP.get(parameterClass);
         }
         if (parameterClass.isEnum()) {
             return parameterClass.getEnumConstants()[0];
         }
         return mockParam(parameterClass);
     }
 
     private static Object mockParam(Class parameterClass) {
         Object mock = mock(parameterClass);
         mockChildGetters(parameterClass, mock);
         return mock;
     }
 
     private static void mockChildGetters(Class parameterClass, Object mock) {
         for (Method method : parameterClass.getDeclaredMethods()) {
             try {
                 if (ReflectionUtils.isGetter(method) && isCustomObjectToBeMocked(method)) {
                     Object param = mock(method.getReturnType());
                     makeAccessible(method);
                     when(method.invoke(mock)).thenReturn(param);
                 }
             } catch (Exception e) {
                 throw new IllegalArgumentException(e.getMessage(), e);
             }
         }
     }
 
     private static boolean isCustomObjectToBeMocked(Method method) {
         return isReturningCustomClass(method) && !method.getReturnType().isEnum();
     }
 
     private static boolean isReturningCustomClass(Method method) {
         return method.getReturnType().getPackage() != null
                 && method.getReturnType().getPackage().getName().startsWith("com.acierto");
     }
 
     public static boolean hasField(Class clazz, PropertyDescriptor descriptor) {
         for (Field field : clazz.getDeclaredFields()) {
             if (!isStatic(field) && descriptor.getName().equalsIgnoreCase(field.getName())) {
                 return true;
             }
         }
         return false;
     }
 
     public static boolean isStatic(Field field) {
         return Modifier.isStatic(field.getModifiers());
     }
 
     public static Object getFieldValue(Object object, PropertyDescriptor descriptor) {
         try {
             Field field = object.getClass().getDeclaredField(descriptor.getName());
             makeAccessible(field);
             return field.get(object);
         } catch (Exception e) {
             throw new IllegalArgumentException(String.format("Error: %s; Method: %s; Reason: %s", object.getClass(),
                     descriptor.getName(), e.getMessage()), e);
         }
     }
 
     public static Object invokeSetter(Object object, PropertyDescriptor descriptor) {
         return invokeDescriptorMethod(object, descriptor.getWriteMethod());
     }
 
     public static Object invokeGetter(Object object, PropertyDescriptor descriptor) {
         return invokeDescriptorMethod(object, descriptor.getReadMethod());
     }
 
     public static Object callMethodOrField(PropertyDescriptor descriptor, boolean isWrite, Object victim) {
         Object returned = null;
         Method method = isWrite ? descriptor.getWriteMethod() : descriptor.getReadMethod();
         if (method != null) {
             returned = ReflectionUtils.invokeDescriptorMethod(victim, method);
         }
         if (returned == null && ReflectionUtils.hasField(victim.getClass(), descriptor)) {
             if (isWrite) {
                 returned = ReflectionUtils.setFieldValue(victim, descriptor);
             } else {
                 returned = ReflectionUtils.getFieldValue(victim, descriptor);
             }
         }
         return returned;
     }
 
     private static Object invokeDescriptorMethod(Object object, Method method) {
         if (method != null) {
             return invokeMethod(object, method);
         }
         return null;
     }
 
     private static Object invokeMethod(Object object, Method method) {
         try {
             Object[] methodParameters = getMethodParameters(method, method.getParameterTypes());
             makeAccessible(method);
             Object result = method.invoke(object, methodParameters);
             if (isSetter(method)) {
                 result = methodParameters[0];
             }
             return result;
         } catch (Exception e) {
             LOG.error(String.format("Class = %s, method = %s. Reason: %s", method.getDeclaringClass(),
                     method.getName(), e.getMessage()), e);
         }
         return null;
     }
 
     private static boolean isSetter(Method method) {
         return method.getReturnType().equals(void.class);
     }
 
     public static Object constructNewObject(Class clazz) {
         Constructor constructor = clazz.getDeclaredConstructors()[0];
         makeAccessible(constructor);
         return callConstructor(constructor);
     }
 
     public static Object callConstructor(Constructor constructor) {
         try {
             return constructor.newInstance(ReflectionUtils.getMethodParameters(constructor));
         } catch (Exception e) {
             throw new IllegalArgumentException("Cannot instantiate class: " + constructor.getDeclaringClass(), e);
         }
     }
 
     public static boolean isGetter(Method method) {
         return (method.getName().startsWith("get") || method.getName().startsWith("is"))
                 && method.getParameterTypes().length == 0;
     }
 
     public static Object setFieldValue(Object object, PropertyDescriptor descriptor) {
         try {
             Field field = object.getClass().getDeclaredField(descriptor.getName());
             makeAccessible(field);
             Object value = getParamValue(field.getType());
             field.set(object, value);
             return value;
         } catch (Exception e) {
             LOG.error(e.getMessage(), e);
         }
         return null;
     }
 }
