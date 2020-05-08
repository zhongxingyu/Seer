 package com.bimbr.clisson.util;
 
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Utility functions to verify arguments.
  *
  * @author mmakowski
  * @since 1.0.0 
  */
 public final class Arguments {
     /**
      * Checks that argument is not null.
      * @param arg the argument whose value is to be checked
      * @param argName the name of the argument, for diagnostics
      * @return the value of {@code arg}
      * @throws IllegalArgumentException if {@code arg} is null
      * @since 1.0.0
      */
     public static <T> T nonNull(T arg, String argName) {
         if (arg == null) throw new IllegalArgumentException(argName + " is null");
         return arg;
     }
 
     /**
      * Checks that argument is a non-empty string.
      * @param arg the argument whose value is to be checked
      * @param argName the name of the argument, for diagnostics
      * @return the value of {@code arg}
      * @throws IllegalArgumentException if {@code arg} is an empty string
      * @since 1.0.0
      */
     public static String nonEmpty(String arg, String argName) {
         nonNull(arg, argName);
         if (arg.length() == 0) throw new IllegalArgumentException(argName + " is an empty string");
         return arg;
     }
     
     /**
      * Checks that argument is a non-empty set.
      * @param arg the argument whose value is to be checked
      * @param argName the name of the argument, for diagnostics
      * @return the value of {@code arg}
      * @throws IllegalArgumentException if {@code arg} is null or an empty set
      * @since 1.0.0
      */
     public static <E> Set<E> nonEmpty(Set<E> arg, String argName) {
         nonNull(arg, argName);
         if (arg.isEmpty()) throw new IllegalArgumentException(argName + " is an empty set");
         return arg;
     }
     
     /**
      * Checks that map does not contain null keys and null values.
      * @param arg the map whose keys and values are to be verified
      * @param argName the name of the argument, for diagnostics
      * @return hte value of {@code arg}
      * @throws IllegalArgumentException if {@code arg} is null or contains null keys or values
      * @since 1.0.0
      */
     public static <K, V> Map<K, V> mapWithNonNullKeysAndValues(Map<K, V> arg, String argName) {
         nonNull(arg, argName);
        if (arg.containsKey(null)) throw new IllegalArgumentException(argName + " contains null key: " + arg.toString());
         if (arg.containsValue(null)) throw new IllegalArgumentException(argName + " contains null value: " + arg.toString());
         return arg;
     }
     
     /**
      * Checks that set does not contain null values.
      * @param arg the set whose elements are to be verified
      * @param argName the name of the argument, for diagnostics
      * @return hte value of {@code arg}
      * @throws IllegalArgumentException if {@code arg} is null or contains null values
      * @since 1.0.0
      */
     public static <E> Set<E> setWithNonNullElements(Set<E> arg, String argName) {
         nonNull(arg, argName);
        if (arg.contains(null)) throw new IllegalArgumentException(argName + " contains null: " + arg.toString());
         return arg;
     }
     
     private Arguments() {}
 }
