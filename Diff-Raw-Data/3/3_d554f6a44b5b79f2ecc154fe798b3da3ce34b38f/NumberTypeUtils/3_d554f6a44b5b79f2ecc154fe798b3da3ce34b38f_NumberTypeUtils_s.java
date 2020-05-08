 /**
  * 
  */
 package org.esa.beam.dataViewer3D.utils;
 
 /**
  * Utility class for easier handling of the generic numeric types.
  * 
  * @author Martin Pecka
  */
 public class NumberTypeUtils
 {
     /**
      * Multiply the given number by the given scalar.
      * 
      * @param scalar The scalar to multiply with.
      * @param number The number to be multiplied.
      * @return The multiple.
      */
     @SuppressWarnings("unchecked")
     public static <N extends Number> N multiply(double scalar, N number)
     {
         if (number instanceof Integer) {
             return (N) (Object) (((int) scalar) * number.intValue());
         } else if (number instanceof Float) {
             return (N) (Object) (((float) scalar) * number.floatValue());
         } else if (number instanceof Double) {
             return (N) (Object) (scalar * number.doubleValue());
         } else if (number instanceof Long) {
             return (N) (Object) (((long) scalar) * number.longValue());
         } else if (number instanceof Byte) {
             return (N) (Object) ((byte) (((byte) scalar) * number.byteValue()));
         } else if (number instanceof Short) {
             return (N) (Object) ((short) (((short) scalar) * number.shortValue()));
         } else {
             throw new IllegalArgumentException();
         }
     }
 
     /**
      * Add the two given numbers.
      * 
      * @param n1 First number to add.
      * @param n2 Second number to add.
      * @return The sum.
      */
     @SuppressWarnings("unchecked")
     public static <N extends Number> N add(N n1, N n2)
     {
         if (n1 instanceof Integer) {
             return (N) (Object) (n1.intValue() + n2.intValue());
         } else if (n1 instanceof Float) {
             return (N) (Object) (n1.floatValue() + n2.floatValue());
         } else if (n1 instanceof Double) {
             return (N) (Object) (n1.doubleValue() + n2.doubleValue());
         } else if (n1 instanceof Long) {
             return (N) (Object) (n1.longValue() + n2.longValue());
         } else if (n1 instanceof Byte) {
             return (N) (Object) ((byte) (n1.byteValue() + n2.byteValue()));
         } else if (n1 instanceof Short) {
             return (N) (Object) ((short) (n1.shortValue() + n2.shortValue()));
         } else {
             throw new IllegalArgumentException();
         }
     }
 
     /**
      * Subtract the two given numbers.
      * 
      * @param n1 The number to subtract from.
      * @param n2 The number to subtract.
      * @return The difference.
      */
     @SuppressWarnings("unchecked")
     public static <N extends Number> N sub(N n1, N n2)
     {
         if (n1 instanceof Integer) {
             return (N) (Object) (n1.intValue() - n2.intValue());
         } else if (n1 instanceof Float) {
             return (N) (Object) (n1.floatValue() - n2.floatValue());
         } else if (n1 instanceof Double) {
             return (N) (Object) (n1.doubleValue() - n2.doubleValue());
         } else if (n1 instanceof Long) {
             return (N) (Object) (n1.longValue() - n2.longValue());
         } else if (n1 instanceof Byte) {
             return (N) (Object) ((byte) (n1.byteValue() - n2.byteValue()));
         } else if (n1 instanceof Short) {
             return (N) (Object) ((short) (n1.shortValue() - n2.shortValue()));
         } else {
             throw new IllegalArgumentException();
         }
     }
 
     /**
      * Return the maximum of the given numbers.
      * 
      * @param numbers A list of numbers to compare.
      * @return The maximum.
      */
     public static Number max(Number... numbers)
     {
         Number max = null;
         for (Number n : numbers) {
             if (max == null || n.doubleValue() > max.doubleValue())
                 max = n;
         }
         return max;
     }
 
     /**
      * Return the minimum of the given numbers.
      * 
      * @param numbers A list of numbers to compare.
      * @return The minimum.
      */
     public static Number min(Number... numbers)
     {
         Number min = null;
         for (Number n : numbers) {
             if (min == null || n.doubleValue() < min.doubleValue())
                 min = n;
         }
         return min;
     }
 
     /**
      * Cast the given number to the same type as numberOfDesiredType has.
      * 
      * @param numberOfDesiredType A number with the type to cast to.
      * @param number The number to be cast.
      * @return The cast number.
      */
     @SuppressWarnings("unchecked")
     public static <N extends Number> N castToType(N numberOfDesiredType, Number number)
     {
         if (numberOfDesiredType instanceof Integer) {
             if (number.doubleValue() > Integer.MAX_VALUE)
                 return (N) (Object) Integer.MAX_VALUE;
             if (number.doubleValue() < Integer.MIN_VALUE)
                 return (N) (Object) Integer.MIN_VALUE;
             return (N) (Object) number.intValue();
         } else if (numberOfDesiredType instanceof Float) {
             if (number.doubleValue() > Float.MAX_VALUE)
                 return (N) (Object) Float.MAX_VALUE;
             if (number.doubleValue() < -Float.MAX_VALUE)
                 return (N) (Object) (-Float.MAX_VALUE);
             return (N) (Object) number.floatValue();
         } else if (numberOfDesiredType instanceof Double) {
             return (N) (Object) number.doubleValue();
         } else if (numberOfDesiredType instanceof Long) {
             if (number.doubleValue() > Long.MAX_VALUE)
                 return (N) (Object) Long.MAX_VALUE;
             if (number.doubleValue() < Long.MIN_VALUE)
                 return (N) (Object) Long.MIN_VALUE;
             return (N) (Object) number.longValue();
         } else if (numberOfDesiredType instanceof Byte) {
             if (number.doubleValue() > Byte.MAX_VALUE)
                 return (N) (Object) Byte.MAX_VALUE;
             if (number.doubleValue() < Byte.MIN_VALUE)
                 return (N) (Object) Byte.MIN_VALUE;
             return (N) (Object) number.byteValue();
         } else if (numberOfDesiredType instanceof Short) {
             if (number.doubleValue() > Short.MAX_VALUE)
                 return (N) (Object) Short.MAX_VALUE;
             if (number.doubleValue() < Short.MIN_VALUE)
                 return (N) (Object) Short.MIN_VALUE;
             return (N) (Object) number.shortValue();
         } else {
             throw new IllegalArgumentException();
         }
     }
 }
