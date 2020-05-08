 package org.flowutils;
 
 import java.util.Collection;
 import java.util.Map;
 
 /**
  * Utility functions for checking parameter validity.
  * They throw a descriptive IllegalArgumentException if they fail.
  * <p/>
  * You may wish to use static import of this class for more concise code.
  */
 public final class Check {
 
 
     /**
      * Checks that the specified condition is fulfilled.
      *
      * @param condition true if condition acceptable, false if an error should be thrown.
      * @param description description of the invariant, added to exception if invariant failed.
      * @throws IllegalArgumentException if the check fails.
      */
     public static void invariant(boolean condition, String description) {
         if (!condition) {
             fail(description);
         }
     }
 
 
     /**
      * Checks that the specified parameter is not infinite and not NaN (not a number).
      *
      * @param parameter     the parameter value to check
      * @param parameterName the name of the parameter (used in error messages)
      * @throws IllegalArgumentException if the check fails.
      */
     public static void normalNumber(double parameter, String parameterName) {
         if (Double.isInfinite(parameter)) {
             fail(parameterName, parameter, "be a normal, non-infinite number");
         }
         if (Double.isNaN(parameter)) {
             fail(parameterName, parameter, "be a normal number");
         }
     }
 
 
     /**
      * Checks that the specified parameter is positive and not infinite and not NaN (not a number).
      *
      * @param parameter     the parameter value to check
      * @param parameterName the name of the parameter (used in error messages)
      * @throws IllegalArgumentException if the check fails.
      */
     public static void positiveOrZero(double parameter, String parameterName) {
         normalNumber(parameter, parameterName);
 
         if (parameter < 0) {
             fail(parameterName, parameter, "be a normal positive number");
         }
     }
 
 
     /**
      * Checks that the specified parameter is positive, not zero, not infinite and not NaN (not a number).
      *
      * @param parameter     the parameter value to check
      * @param parameterName the name of the parameter (used in error messages)
      * @throws IllegalArgumentException if the check fails.
      */
     public static void positive(double parameter, String parameterName) {
         normalNumber(parameter, parameterName);
 
         if (parameter <= 0) {
             fail(parameterName, parameter, "be a normal positive non-zero number");
         }
     }
 
 
     /**
      * Checks that the specified parameter is negative and not infinite and not NaN (not a number).
      *
      * @param parameter     the parameter value to check
      * @param parameterName the name of the parameter (used in error messages)
      * @throws IllegalArgumentException if the check fails.
      */
     public static void negativeOrZero(double parameter, String parameterName) {
         normalNumber(parameter, parameterName);
 
         if (parameter > 0) {
             fail(parameterName, parameter, "be a normal negative or zero number");
         }
     }
 
 
     /**
      * Checks that the specified parameter is negative, not zero, not infinite and not NaN (not a number).
      *
      * @param parameter     the parameter value to check
      * @param parameterName the name of the parameter (used in error messages)
      * @throws IllegalArgumentException if the check fails.
      */
     public static void negative(double parameter, String parameterName) {
         normalNumber(parameter, parameterName);
 
         if (parameter >= 0) {
             fail(parameterName, parameter, "be a normal negative non-zero number");
         }
     }
 
 
     /**
      * Checks that the specified parameter is not zero.
      *
      * @param parameter     the parameter value to check
      * @param parameterName the name of the parameter (used in error messages)
      * @param epsilon +- range around zero that is regarded as zero.
      * @throws IllegalArgumentException if the check fails.
      */
     public static void notZero(double parameter, String parameterName, double epsilon) {
         normalNumber(parameter, parameterName);
 
         if (parameter >= -epsilon && parameter <= epsilon) {
             fail(parameterName, parameter, "be a normal non-zero number");
         }
     }
 
     /**
      * Checks that the specified parameter is positive and not zero.
      *
      * @param parameter     the parameter value to check
      * @param parameterName the name of the parameter (used in error messages)
      * @throws IllegalArgumentException if the check fails.
      */
     public static void positiveInt(int parameter, String parameterName) {
         if (parameter <= 0) {
             fail(parameterName, parameter, "be a positive non-zero number");
         }
     }
 
 
     /**
      * Checks that the specified parameter is positive or zero.
      *
      * @param parameter     the parameter value to check
      * @param parameterName the name of the parameter (used in error messages)
      * @throws IllegalArgumentException if the check fails.
      */
     public static void positiveOrZeroInt(int parameter, String parameterName) {
         if (parameter < 0) {
             fail(parameterName, parameter, "be a positive or zero number");
         }
     }
 
     /**
      * Checks that the specified parameter is negative and not zero.
      *
      * @param parameter     the parameter value to check
      * @param parameterName the name of the parameter (used in error messages)
      * @throws IllegalArgumentException if the check fails.
      */
     public static void negativeInt(int parameter, String parameterName) {
         if (parameter >= 0) {
             fail(parameterName, parameter, "be a negative non-zero number");
         }
     }
 
 
     /**
      * Checks that the specified parameter is negative or zero.
      *
      * @param parameter     the parameter value to check
      * @param parameterName the name of the parameter (used in error messages)
      * @throws IllegalArgumentException if the check fails.
      */
     public static void negativeOrZeroInt(int parameter, String parameterName) {
         if (parameter > 0) {
             fail(parameterName, parameter, "be a negative or zero number");
         }
     }
 
     /**
      * Checks that the specified parameter not zero.
      *
      * @param parameter     the parameter value to check
      * @param parameterName the name of the parameter (used in error messages)
      * @throws IllegalArgumentException if the check fails.
      */
     public static void notZeroInt(int parameter, String parameterName) {
         if (parameter == 0) {
             fail(parameterName, parameter, "be a non-zero number");
         }
     }
 
 
     /**
      * Checks that the specified parameter is not null.
      *
      * @param parameter     the parameter value to check
      * @param parameterName the name of the parameter (used in error messages)
      * @throws IllegalArgumentException if the check fails.
      */
     public static void notNull(final Object parameter, final String parameterName) {
         if (parameter == null) {
             fail(parameterName, parameter, "not be null");
         }
     }
 
     /**
      * Checks that the specified parameter is a valid Java style identifier (starts with letter or underscore,
      * contains letters, underscores and numbers).
      *
      * @param parameter     the parameter value to check
      * @param parameterName the name of the parameter (used in error messages)
      * @throws IllegalArgumentException if the check fails.
      */
     public static void identifier(final String parameter, final String parameterName) {
         if (parameter == null || !isIdentifier(parameter)) {
             fail(parameterName, parameter, "be a valid Java style identifier");
         }
     }
 
     /**
      * Checks that the specified parameter is not an empty string nor null.
      *
      * @param parameter     the parameter value to check
      * @param parameterName the name of the parameter (used in error messages)
      * @throws IllegalArgumentException if the check fails.
      */
     public static void nonEmptyString(final String parameter, final String parameterName) {
         if (parameter == null || parameter.trim().isEmpty()) {
             fail(parameterName, parameter, "be a non-empty string value");
         }
     }
 
     /**
      * Checks that the parameter is in the range 0..1 (inclusive).
      *
      * @param parameter     the parameter value to check
      * @param parameterName the name of the parameter (used in error messages)
      * @throws IllegalArgumentException if the check fails.
      */
     public static void inRangeZeroToOne(final double parameter, String parameterName) {
         normalNumber(parameter, parameterName);
 
         if (parameter < 0 || parameter > 1) {
             fail(parameterName, parameter, "be in the range 0 to 1 inclusive");
         }
     }
 
 
     /**
      * Checks that the parameter is in the specified range
      *
      * @param parameter     the parameter value to check
      * @param parameterName the name of the parameter (used in error messages)
      * @throws IllegalArgumentException if the check fails.
      */
     public static void inRange(final double parameter,
                                String parameterName,
                                double minimumValueInclusive,
                                double maximumValueExclusive) {
         normalNumber(parameter, parameterName);
 
         if (parameter < minimumValueInclusive || parameter >= maximumValueExclusive) {
             fail(parameterName,
                  parameter,
                  "be in the range " + minimumValueInclusive + " (inclusive) " +
                  "to " + maximumValueExclusive + " (exclusive)");
         }
     }
 
 
     /**
      * Checks that the parameter is in the specified range
      *
      * @param parameter     the parameter value to check
      * @param parameterName the name of the parameter (used in error messages)
      * @throws IllegalArgumentException if the check fails.
      */
     public static void inRangeInt(final int parameter,
                                   String parameterName,
                                   int minimumValueInclusive,
                                   int maximumValueExclusive) {
         if (parameter < minimumValueInclusive || parameter >= maximumValueExclusive) {
             fail(parameterName,
                  parameter,
                  "be in the range " + minimumValueInclusive + " (inclusive) " +
                  "to " + maximumValueExclusive + " (exclusive)");
         }
     }
 
 
     /**
      * Checks that the parameter is smaller than the specified value.
      *
      * @param parameter     the parameter value to check
      * @param parameterName the name of the parameter (used in error messages)
      * @throws IllegalArgumentException if the check fails.
      */
     public static void under(final int parameter,
                              String parameterName,
                             int maximumValueInclusive) {
        if (parameter >= maximumValueInclusive) {
            fail(parameterName, parameter, "be smaller than " + maximumValueInclusive);
         }
     }
 
     /**
      * Checks that the parameter is smaller or equal to the specified value.
      *
      * @param parameter     the parameter value to check
      * @param parameterName the name of the parameter (used in error messages)
      * @throws IllegalArgumentException if the check fails.
      */
     public static void underOrEqual(final int parameter,
                                       String parameterName,
                                       int maximumValueInclusive) {
         if (parameter > maximumValueInclusive) {
             fail(parameterName, parameter, "be smaller or equal to " + maximumValueInclusive);
         }
     }
 
     /**
      * Checks that the parameter is greater than the specified value.
      *
      * @param parameter     the parameter value to check
      * @param parameterName the name of the parameter (used in error messages)
      * @throws IllegalArgumentException if the check fails.
      */
     public static void over(final int parameter,
                               String parameterName,
                              int minimumValueInclusive) {
        if (parameter <= minimumValueInclusive) {
            fail(parameterName, parameter, "be larger than " + minimumValueInclusive);
         }
     }
 
     /**
      * Checks that the parameter is greater or equal to the specified value.
      *
      * @param parameter     the parameter value to check
      * @param parameterName the name of the parameter (used in error messages)
      * @throws IllegalArgumentException if the check fails.
      */
     public static void overOrEqual(final int parameter,
                                      String parameterName,
                                      int minimumValueInclusive) {
         if (parameter < minimumValueInclusive) {
             fail(parameterName, parameter, "be larger or equal to " + minimumValueInclusive);
         }
     }
 
 
     /**
      * Checks that the integer parameter is equal to the specified value.
      *
      * @param parameter     the parameter value to check
      * @param parameterName the name of the parameter (used in error messages)
      * @throws IllegalArgumentException if the check fails.
      */
     public static void equal(final int parameter,
                              String parameterName,
                              int requiredValue,
                              String otherName) {
         if (parameter != requiredValue) {
             fail(parameterName,
                  parameter,
                  "be equal to " + otherName + " which is " + requiredValue);
         }
     }
 
 
     /**
      * Checks that the parameter equals the specified value.
      * Uses the equals method.
      *
      * @param parameter     the parameter value to check
      * @param parameterName the name of the parameter (used in error messages)
      * @throws IllegalArgumentException if the check fails.
      */
     public static void equals(final Object parameter,
                              String parameterName,
                              Object requiredValue,
                              String otherName) {
         if ((parameter != null && !parameter.equals(requiredValue)) ||
             (parameter == null && requiredValue != null)) {
             fail(parameterName,
                  parameter,
                  "be equal to " + otherName + " which is '" + requiredValue + "'");
         }
     }
 
     /**
      * Checks that the parameter equals the specified value.
      * Uses reference equality ( == ).
      *
      * @param parameter     the parameter value to check
      * @param parameterName the name of the parameter (used in error messages)
      * @throws IllegalArgumentException if the check fails.
      */
     public static void equalRef(final Object parameter,
                                 String parameterName,
                                 Object requiredValue,
                                 String otherName) {
         if (parameter != requiredValue) {
             fail(parameterName,
                  parameter,
                  "be the same object as " + otherName + " which is '" + requiredValue + "'");
         }
     }
 
 
     /**
      * Checks that the specified element is contained in the specified collection.
      *
      * @param element element to check for.
      * @param collection collection to check in
      * @param collectionName  name to use for collection in any error message.
      * @throws IllegalArgumentException if the check fails.
      */
     public static void contained(final Object element,
                                  final Collection collection,
                                  final String collectionName) {
         if (!collection.contains(element)) {
             fail("The " + collectionName + " does not contain " + describeElementType(element));
         }
     }
 
     /**
      * Checks that the specified element is not contained in the specified collection.
      *
      * @param element element to check for.
      * @param collection collection to check in
      * @param collectionName  name to use for collection in any error message.
      * @throws IllegalArgumentException if the check fails.
      */
     public static void notContained(final Object element, final Collection collection, final String collectionName) {
         if (collection.contains(element)) {
             fail("The " + collectionName + " already contains " + describeElementType(element));
         }
     }
 
 
     /**
      * Checks that the specified key is contained in the specified map.
      *
      * @param key key to check for.
      * @param map map to check in
      * @param mapName name to use for map in any error message.
      * @throws IllegalArgumentException if the check fails.
      */
     public static void contained(final Object key, final Map map, final String mapName) {
         if (!map.containsKey(key)) {
             fail("The " + mapName + " does not contain the key '" + key + "'");
         }
     }
 
 
     /**
      * Checks that the specified key is not contained in the specified map.
      *
      * @param key key to check for.
      * @param map map to check in
      * @param mapName name to use for map in any error message.
      * @throws IllegalArgumentException if the check fails.
      */
     public static void notContained(final Object key, final Map map, final String mapName) {
         if (map.containsKey(key)) {
             fail("The " + mapName + " already contains the key '" + key + "'");
         }
     }
 
     /**
      * Checks that the specified collection is empty.
      *
      * @param collection collection to check.
      * @param collectionName name to use for collection in any error message.
      * @throws IllegalArgumentException if the check fails.
      */
     public static void empty(final Collection collection, final String collectionName) {
         if (!collection.isEmpty()) {
             fail("The " + collectionName + " was empty");
         }
     }
 
     /**
      * Checks that the specified collection is not empty.
      *
      * @param collection collection to check.
      * @param collectionName name to use for collection in any error message.
      * @throws IllegalArgumentException if the check fails.
      */
     public static void notEmpty(final Collection collection, final String collectionName) {
         if (collection.isEmpty()) {
             fail("The " + collectionName + " was empty");
         }
     }
 
 
     /**
      * Checks that the parameter is an instance of the specified type.
      *
      * @param parameter parameter to check
      * @param parameterName name to use for parameter
      * @param expectedParameterType type to check for.
      * @throws IllegalArgumentException if the check fails.
      */
     public static void instanceOf(final Object parameter,
                                   final String parameterName,
                                   final Class expectedParameterType) {
         notNull(parameter, parameterName);
 
         if (!expectedParameterType.isInstance(parameter)) {
             fail(parameterName,
                  "of type '" + parameter.getClass() + "'",
                  "be of type '" + expectedParameterType + "'");
         }
     }
 
     /**
      * Checks that the parameter is not an instance of the specified type.
      *
      * @param parameter parameter to check
      * @param parameterName name to use for parameter
      * @param expectedParameterType type to check for.
      * @throws IllegalArgumentException if the check fails.
      */
     public static void notInstanceOf(final Object parameter,
                                   final String parameterName,
                                   final Class expectedParameterType) {
         notNull(parameter, parameterName);
 
         if (expectedParameterType.isInstance(parameter)) {
             fail(parameterName,
                  "of type '" + parameter.getClass() + "'",
                  "be of type '" + expectedParameterType + "'");
         }
     }
 
 
     /**
      * Throws an IllegalArgumentException, using the specified parameters to compose the error message,
      * and including information on the method that it was thrown from.
      *
      * @throws IllegalArgumentException with message.
      */
     public static void fail(final String parameterName,
                              final Object parameter,
                              final String expectedCondition) {
         fail("The parameter '" + parameterName + "' " +
              "should " + expectedCondition + ", " +
              "but it was '" + parameter + "'.");
     }
 
 
     /**
      * Throws an IllegalArgumentException with the specified description, including information on the method that
      * it was thrown from.
      *
      * @throws IllegalArgumentException with message.
      */
     public static void fail(final String desc) {
         throw new IllegalArgumentException(desc + determineContext());
     }
 
     //======================================================================
     // Private Methods
 
     private Check() {
     }
 
     private static String describeElementType(final Object element) {
         final String elementDesc;
         if (element == null) {
             elementDesc = "a null element.";
         } else {
             elementDesc = "the " + element.getClass() + "  '" + element + "'.";
         }
 
         return elementDesc;
     }
 
     /**
      * @return true if s is a valid Java style identifier.
      */
     private static boolean isIdentifier(String s) {
         if (s.isEmpty()) return false;
         else {
             if (!Character.isJavaIdentifierStart(s.charAt(0))) return false;
             for (int i = 1; i < s.length(); i++) {
                 if (!Character.isJavaIdentifierPart(s.charAt(0))) return false;
             }
             return true;
         }
     }
 
 
     /**
      * @return method and class that this method was called from, excluding any calls from within this class.
      */
     private static String determineContext() {
         // Get call stack
         final StackTraceElement[] trace = Thread.currentThread().getStackTrace();
 
         // Iterate to first method not in Check class (= the originator of the failed check)
         final String checkName = Check.class.getName();
         for (int i = trace.length - 1 - 2; i > 0; i--) {
             // We start 2 steps back, as there is at least two calls inside the Check class before this method.
 
             if (!trace[i].getClassName().equals(checkName)) {
                 String methodName = trace[i].getMethodName();
                 String className = trace[i].getClassName();
                 return " in method " + methodName + " of class " + className;
             }
         }
 
         // Normally we wouldn't get here
         return "";
     }
 
 }
 
