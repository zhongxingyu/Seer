 package uk.co.epsilontechnologies.primer;
 
 import java.lang.reflect.Field;
 
 public class PrimerAnnotations {
 
     public static void initPrimers(final Object testClass) {
         final Class<?> clazz = testClass.getClass();
         for (final Field field : clazz.getDeclaredFields()) {
             if (field.isAnnotationPresent(Primer.class)) {
                final Primer primerAnnotation = field.getDeclaredAnnotation(Primer.class);
                 try {
                     field.setAccessible(true);
                     field.set(
                             testClass,
                             new Primable(
                                     primerAnnotation.contextPath(),
                                     primerAnnotation.port()));
                 } catch (final IllegalAccessException e) {
                     throw new RuntimeException("Unable to initialize annotated primers", e);
                 }
             }
         }
     }
 
 }
