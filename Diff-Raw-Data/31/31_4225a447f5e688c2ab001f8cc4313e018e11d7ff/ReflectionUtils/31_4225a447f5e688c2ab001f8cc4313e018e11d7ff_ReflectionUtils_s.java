 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.tools;
 
 import java.lang.reflect.Field;
import java.lang.NoSuchFieldException;
 
 /** Convenience methods to deal with reflection.
  *
  * This class includes operations to obtain and set the value of private
  * fields. It looks for them up in the class hierarchy.
 * 
  * @deprecated use spring's DirectFieldAccessor instead.
  */
 @Deprecated
 public final class ReflectionUtils {
 
   /** A private constructor for this utility class.
    */
   private ReflectionUtils() {
   }
 
   /** Sets a value of a possible inaccesible field.
    *
    * @param object The object to set the attribute to. It cannot be null.
    *
    * @param attributeName The name of the attribute to change. The attribute
    * must exist in the object class. It cannot be null.
    *
    * @param value The new value for the attribute.
    */
   public static void setAttribute(final Object object, final String
       attributeName, final Object value) {
     try {
       Field field = getField(object.getClass(), attributeName);
       field.setAccessible(true);
       field.set(object, value);
     } catch (RuntimeException e) {
       throw e;
     } catch (Exception e) {
       throw new RuntimeException("Error setting field " + attributeName, e);
     }
   }
 
   /** Obtains the value of a possible inaccessible field.
    *
    * @param object The object to set the attribute to. It cannot be null.
    *
    * @param attributeName The name of the attribute to change. The attribute
    * must exist in the object class. It cannot be null.
    *
    * @return The value for the attribute.
    */
   public static Object getAttribute(final Object object, final String
       attributeName) {
     try {
       Field field = getField(object.getClass(), attributeName);
       field.setAccessible(true);
       return field.get(object);
     } catch (RuntimeException e) {
       throw e;
     } catch (Exception e) {
       throw new RuntimeException("Error setting field " + attributeName, e);
     }
   }
 
  /** Obtains a field from object, searching the full class hierarchy.
    *
   * @param object The object to set the attribute to. It cannot be null.
    *
    * @param attributeName The name of the attribute to search. It cannot be
    * null.
    */
   @SuppressWarnings("unchecked")
   private static Field getField(final Class<?> theClass, final String
       attributeName) throws NoSuchFieldException {
     try {
       Field field = theClass.getDeclaredField(attributeName);
       return field;
     } catch (NoSuchFieldException e) {
       Class superclass = theClass.getSuperclass();
       if (superclass != null) {
         return getField(superclass, attributeName);
       } else {
         throw e;
       }
     }
   }
 }
 
