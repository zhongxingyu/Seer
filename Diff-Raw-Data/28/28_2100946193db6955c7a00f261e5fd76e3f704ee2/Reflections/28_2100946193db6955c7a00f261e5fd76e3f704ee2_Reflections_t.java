 package net.sparkmuse.common;
 
 import com.google.common.base.Preconditions;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
 import java.util.List;
 import java.util.Arrays;
import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
import play.Play;
 
 /**
  * @author neteller
  * @created: Jan 16, 2011
  */
 public class Reflections {
 
   /**
    * Overlays any new properties on top of the existing object's properties.
    * This is useful if a POJO is bound for form submission in the controller but
    * only the properties the user has access to are submitted.  We need to ensure
    * that we copy these user submitted properties on top of existing data instead
    * of simply saving the new copy and potentially overwriting existing stuff.
    *
    * This method will read fields directly from the underlying class but will only
    * update those which have bean-style getters and setters defined.
    *
    * BE CAREFUL when attempting to overlay objects with primitives as they will
    * never be null and thus always be copied.
    *
    * @param existingObject
    * @param newObject
    * @param properties      list of properties to copy, if empty all non-null properties
    *                        will be copied
    * @return
    */
   public static <T> T overlay(T existingObject, T newObject, String... properties) {
     Preconditions.checkNotNull(existingObject);
     Preconditions.checkNotNull(newObject);
 
     final Iterable<Field> existingObjectFields = Iterables.filter(
         Arrays.asList(existingObject.getClass().getDeclaredFields()),
         new OverlayPropertyPredicate(properties)
     );
 
     for (Field field: existingObjectFields) {
       final Method setter = setterFor(field);
       if (null != setter) {
         try {
           final Method getter = getterFor(newObject.getClass(), field.getName());
           final Object newObjectProperty = getter.invoke(newObject);
           if (null != newObjectProperty) {
             setter.invoke(existingObject, newObjectProperty);
           }
         } catch (IllegalAccessException e) {
           //security issue in JVM
           throw new RuntimeException(e);
         } catch (InvocationTargetException e) {
           //problem invoking getter/setter
           throw new RuntimeException("Could not transfer values.  Please check that getter and setter" +
               " for the property " + field.getName() + " are analogous.", e);
         }
       }
     }
 
     return existingObject;
   }
 
   private static Method getterFor(Class clazz, String field) {
     try {
       final String getterName = "get" + StringUtils.capitalize(field);
       return clazz.getMethod(getterName, new Class[]{});
     } catch (NoSuchMethodException e) {
       try {
         final String getterName = "is" + StringUtils.capitalize(field);
         return clazz.getMethod(getterName, new Class[]{});
       } catch (NoSuchMethodException e1) {
         return null;
       }
     }
   }
 
   private static Method setterFor(Field field) {
     try {
       final String setterName = "set" + StringUtils.capitalize(field.getName());
       return field.getDeclaringClass().getMethod(setterName, new Class[]{field.getType()});
     } catch (NoSuchMethodException e) {
       return null;
     }
   }
 
   private static class OverlayPropertyPredicate implements Predicate<Field> {
 
     private List<String> properties;
 
     private OverlayPropertyPredicate(String[] properties) {
       this.properties = Arrays.asList(properties);
     }
 
     public boolean apply(Field field) {
       if (properties.size() == 0) return true;
       else return properties.contains(field.getName());
     }
   }
 
 }
