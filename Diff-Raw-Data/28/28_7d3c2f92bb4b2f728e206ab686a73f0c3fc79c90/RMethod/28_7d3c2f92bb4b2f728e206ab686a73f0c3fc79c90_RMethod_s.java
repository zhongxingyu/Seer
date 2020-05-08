 package org.n3r.core.lang;
 
 import static org.n3r.core.lang.RClass.loadClass;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.List;
 
 import org.apache.commons.beanutils.MethodUtils;
 import org.n3r.core.joor.Reflect;
 
 import com.google.common.base.Throwables;
 import com.google.common.collect.Lists;
 
 public class RMethod {
 
     public static void invokeQuietly(Method method, Object object) {
         try {
             method.invoke(object, new Object[] {});
         }
         catch (Exception e) {
             // ingore
         }
     }
 
     @SuppressWarnings("unchecked")
     public static <T> T invoke(Method method, Object object, Object... args) {
         try {
             Object target = object;
             if (target == null && !Modifier.isStatic(method.getModifiers())) target = Reflect
                     .on(method.getDeclaringClass()).create().get();
 
             return (T) method.invoke(target, args);
         }
         catch (Exception e) {
             throw Throwables.propagate(e);
         }
     }
 
     public static List<Method> getMethodsAnnotatedWith(Class<?> clazz, Class<? extends Annotation> class1) {
         List<Method> methods = Lists.newArrayList();
         for (Method m : clazz.getMethods())
             if (m.isAnnotationPresent(class1)) methods.add(m);
 
         return methods;
     }
 
     /**
      * 获得类方法的反射。
      * 
      * @param clazz 类
      * @param methodName 方法名称
      * @param parameterTypes 方法参数列表
      * @return 方法
      */
     public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
         return MethodUtils.getAccessibleMethod(clazz, methodName, parameterTypes);
     }
 
     /**
      * 获得类方法的反射。
      * 
      * @param className 类名称
      * @param methodName 方法名称
      * @param parameterTypes 方法参数列表
      * @return 方法
      */
     public static Method getMethod(String className, String methodName, Class<?>... parameterTypes) {
         return getMethod(loadClass(className), methodName, parameterTypes);
     }
 
     public static Method getExactMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
         Method method = getMethod(clazz, methodName, parameterTypes);
         if (method != null) return method;
         Method[] methods = clazz.getMethods();
         for (Method m : methods) {
             if (!Modifier.isPublic(m.getModifiers())) continue;
 
             if (m.getName().equals(methodName) && m.getParameterTypes().length == parameterTypes.length) return m;
         }
         throw new NullPointerException(String.format("Not Find %s's method %s.", clazz.getCanonicalName(), methodName));
     }
 
     public static Method getExactMethod(String className, String methodName, Class<?>... parameterTypes) {
         return getExactMethod(loadClass(className), methodName, parameterTypes);
     }
 
 }
