 package net.jhorstmann.gein.introspection;
 
 import java.beans.BeanInfo;
 import java.beans.IntrospectionException;
 import java.beans.Introspector;
 import java.beans.PropertyDescriptor;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.WeakHashMap;
 
 public class Introspection {
 
     private static final WeakHashMap<Class, Map<String, PropertyDelegate>> cache = new WeakHashMap<Class, Map<String, PropertyDelegate>>();
 
     private Introspection() {
 
     }
 
     private static void introspectFieldsRec(Class beanClass, Map<String, PropertyDelegate> res) {
         while (beanClass != Object.class) {
             Field[] declaredFields = beanClass.getDeclaredFields();
             for (Field field : declaredFields) {
                 String name = field.getName();
                 if (!res.containsKey(name)) {
                     res.put(name, new FieldBasedProperty(field, name));
                 }
             }
             beanClass = beanClass.getSuperclass();
         }
     }
 
     public static Map<String, PropertyDelegate> introspectFields(Class beanClass) {
         Map<String, PropertyDelegate> res = cache.get(beanClass);
         if (res == null) {
             if (Map.class.isAssignableFrom(beanClass)) {
                 PropertyDelegate delegate = new MapBasedProperty(beanClass, Object.class, "");
                 res = Collections.singletonMap("", delegate);
             } else {
                 introspectFieldsRec(beanClass, res);
             }
             cache.put(beanClass, res);
         }
         return res;
     }
 
     public static Map<String, PropertyDelegate> introspectProperties(Class beanClass) throws IntrospectionException {
         Map<String, PropertyDelegate> res = cache.get(beanClass);
         if (res == null) {
             if (Map.class.isAssignableFrom(beanClass)) {
                 PropertyDelegate delegate = new MapBasedProperty(beanClass, Object.class, "");
                 res = Collections.singletonMap("", delegate);
             } else {
                 res = new HashMap<String, PropertyDelegate>();
                 BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
                 PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
                 for (PropertyDescriptor prop : propertyDescriptors) {
                     String name = prop.getName();
                     Method readMethod = prop.getReadMethod();
                     Method writeMethod = prop.getWriteMethod();
                     Class<?> declaringClass = readMethod.getDeclaringClass();
                     if (declaringClass != Object.class) {
                         PropertyDelegate delegate = new MethodBasedProperty(readMethod, writeMethod, name);
                         res.put(name, delegate);
                     }
                 }
                 res = Collections.unmodifiableMap(res);
             }
             cache.put(beanClass, res);
         }
         return res;
     }
 
     static Map<String, PropertyDelegate> introspect(Class beanClass, boolean accessFields) throws IntrospectionException {
         return accessFields ? introspectFields(beanClass) : introspectProperties(beanClass);
     }
 }
