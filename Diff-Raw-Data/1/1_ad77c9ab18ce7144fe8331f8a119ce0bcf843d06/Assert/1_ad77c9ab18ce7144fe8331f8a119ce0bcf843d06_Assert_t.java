 package de.codescape.bitvunit.util;
 
 /**
  * Utility class with reusable assertions.
  *
  * @author Stefan Glase
  * @since 0.5
  */
 public final class Assert {
 
     private Assert() {
         throw new UnsupportedOperationException("Utility class should not be instantiated.");
     }
 
     /**
      * Verifies that the given object is not <code>null</code> and throws an <code>IllegalArgumentException</code> with
      * a default message otherwise.
      *
      * @param object object that should be checked
      */
     public static void notNull(Object object) {
         notNull("Parameter may not be null.", object);
     }
 
     /**
      * Verifies that the given list of objects does not contain a single element that is <code>null</code> and throws an
      * <code>IllegalArgumentException</code> with a default message otherwise.
      *
      * @param objects objects that should be checked
      */
     public static void notNull(Object... objects) {
        notNull((Object) objects);
         for (Object object : objects) {
             notNull(object);
         }
     }
 
     /**
      * Verifies that the given object is not <code>null</code> and throws an <code>IllegalArgumentException</code> with
      * the given message otherwise.
      *
      * @param message message that should be included in case of an error
      * @param object  object that should be checked
      */
     public static void notNull(String message, Object object) {
         if (object == null) {
             throw new IllegalArgumentException(message);
         }
     }
 
     /**
      * Verifies that the given string is neither <code>null</code> nor empty and throws an
      * <code>IllegalArgumentException</code> with a default message otherwise.
      *
      * @param string string that should be checked
      */
     public static void notEmpty(String string) {
         notEmpty("Parameter may not be null or empty.", string);
     }
 
     /**
      * Verifies that the given string is neither <code>null</code> nor empty and throws an
      * <code>IllegalArgumentException</code> with the given message otherwise.
      *
      * @param message message that should be included in case of an error
      * @param string  string that should be checked
      */
     public static void notEmpty(String message, String string) {
         if (string == null || string.trim().isEmpty()) {
             throw new IllegalArgumentException(message);
         }
     }
 
 }
