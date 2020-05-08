 package net.premereur.mvp.util.reflection;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
/**
 * Some reflection-related utilities.
 * 
 * @author gpremer
 * 
 */
 public final class ReflectionUtil {
 
    public static <T> T uncheckedNewInstance(final Class<T> clazz) {
         try {
             return clazz.newInstance();
         } catch (InstantiationException e) {
             throw new RuntimeException(e);
         } catch (IllegalAccessException e) {
             throw new RuntimeException(e);
         }
     }
 
     public static Iterable<Method> annotatedMethods(final Class<?> clazz, final Class<? extends Annotation> annot) {
         Method[] methods = clazz.getMethods();
         List<Method> annotatedMethods = new ArrayList<Method>(methods.length);
         for (Method m : methods) {
             if (m.getAnnotation(annot) != null) {
                 annotatedMethods.add(m);
             }
         }
         return annotatedMethods;
     }
 
     private ReflectionUtil() {
     }
}
