 /*
  * This file is part of VIUtils.
  *
  * Copyright © 2012-2013 Visual Illusions Entertainment
  *
  * VIUtils is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  *
  * VIUtils is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with VIUtils.
  * If not, see http://www.gnu.org/licenses/lgpl.html.
  */
 package net.visualillusionsent.utils;
 
 import static net.visualillusionsent.utils.Verify.notEmpty;
 import static net.visualillusionsent.utils.Verify.notNull;
 import static net.visualillusionsent.utils.Verify.notOutOfRange;
 import static net.visualillusionsent.utils.Verify.notOutOfRangeEqual;
 
 /**
  * Provides static methods to help with {@link String} manipulations
  *
  * @author Jason (darkdiplomat)
  * @version 1.2
  * @since 1.0.0
  */
 public final class StringUtils {
 
     private static final float classVersion = 1.2F;
 
     /** This class should never be constructed */
     private StringUtils() {
     }
 
     /**
      * Joins a {@link String} array into a single String
      *
      * @param args
      *         the {@link String} array to be combined
     * @param delimiter
      *         the {@link String} to put between each element of the array
      * @param startIndex
      *         the index to start combining
      *
      * @return the newly joined {@link String}
      *
      * @throws UtilityException
      *         <br>
      *         if startIndex is greater than or equal to args.length<br>
     *         or if startIndex is greater than or equal to endIndex<br>
     *         or if endIndex is greater than or equal to args.length<br>
      */
    public static final String joinString(String[] args, String delimiter, int startIndex) throws UtilityException {
        return joinString(args, delimiter, startIndex, args.length - 1);
     }
 
     /**
      * Joins a {@link String} array into a single String
      *
      * @param args
      *         the {@link String} array to be combined
      * @param delimiter
     *         the {@link String} to put between each element of the array<br/>
     *         if delimiter is null, it will be treated as the null character (\u0000)
      * @param startIndex
      *         the index to start combining
      * @param endIndex
      *         the index to end combining
      *
      * @return the newly joined {@link String}
      *
      * @throws UtilityException
      *         <br>
      *         if startIndex is greater than or equal to args.length<br>
      *         or if startIndex is greater than or equal to endIndex<br>
      *         or if endIndex is greater than or equal to args.length<br>
      */
     public static final String joinString(String[] args, String delimiter, int startIndex, int endIndex) throws UtilityException {
         notNull(args, "String[] args");
        //notNull(delimiter, "String delimiter"); Convert Null to the null char
         notEmpty(args, "String[] args");
        //notEmpty(delimiter, "String delimiter"); NO; space is a valid delimiter
         notOutOfRange(startIndex, args.length, "startIndex greater than args.length");
         notOutOfRange(startIndex, endIndex, "startIndex greater than endIndex");
         notOutOfRangeEqual(endIndex, args.length, "endIndex greater than or equal to args.length");
 
        if (delimiter == null) {
            delimiter = "\u0000";
        }

         StringBuilder sb = new StringBuilder();
         for (int index = startIndex; index <= endIndex; index++) {
             sb.append(args[index]);
             sb.append(delimiter);
         }
         String preRet = sb.toString();
         return preRet.substring(0, preRet.lastIndexOf(delimiter)); //Remove last spacer
     }
 
     /**
      * Pads to the right of a {@link String} with spaces ' '<br>
      * NOTE: Padding also takes into account existing characters so a padAmount less that the give String has no effect
      *
      * @param toPad
      *         the {@link String} to be padded
      * @param padAmount
      *         the amount to pad
      *
      * @return the padded string
      *
      * @throws UtilityException
      *         if toPad is null
      */
     public static final String padRight(String toPad, int padAmount) throws UtilityException {
         return padCharRight(toPad, padAmount, ' ');
     }
 
     /**
      * Pads to the right of a {@link String} with specified character<br>
      * NOTE: Padding also takes into account existing characters so a padAmount less that the give String has no effect
      *
      * @param toPad
      *         the {@link String} to be padded
      * @param padAmount
      *         the amount to pad
      * @param delimiter
      *         character to pad the String with
      *
      * @return the padded string
      *
      * @throws UtilityException
      *         if toPad is null
      */
     public static final String padCharRight(String toPad, int padAmount, char delimiter) throws UtilityException {
         notNull(toPad, "String toPad");
 
         StringBuffer padded = new StringBuffer(toPad);
         while ((padded.length() - toPad.length()) < padAmount) {
             padded.append(delimiter);
         }
         return padded.toString();
     }
 
     /**
      * Pads to the left of a {@link String} with spaces ' '<br>
      * NOTE: Padding also takes into account existing characters so a padAmount less that the give String has no effect
      *
      * @param toPad
      *         the {@link String} to be padded
      * @param padAmount
      *         the amount to pad
      *
      * @return the padded string
      *
      * @throws UtilityException
      */
     public static final String padLeft(String toPad, int padAmount) throws UtilityException {
         return padCharLeft(toPad, padAmount, ' ');
     }
 
     /**
      * Pads to the left of a {@link String} with specified character<br>
      * NOTE: Padding also takes into account existing characters so a padAmount less that the give String has no effect
      *
      * @param toPad
      *         the {@link String} to be padded
      * @param padAmount
      *         the amount to pad
      * @param delimiter
      *         character to pad the String with
      *
      * @return the padded string
      *
      * @throws UtilityException
      */
     public static final String padCharLeft(String toPad, int padAmount, char delimiter) throws UtilityException {
         notNull(toPad, "String toPad");
 
         StringBuffer padded = new StringBuffer(toPad);
         while ((padded.length() - toPad.length()) < padAmount) {
             padded.insert(0, delimiter);
         }
         return padded.toString();
     }
 
     /**
      * Centers a String in the specified Line Length
      *
      * @param toCenter
      *         the String to center
      * @param lineLength
      *         the length of the line to center on
      *
      * @return the padded String
      *
      * @throws UtilityException
      *         if toCenter is null
      */
     public static final String centerLine(String toCenter, int lineLength) throws UtilityException {
         notNull(toCenter, "String toCenter");
 
         return padCharLeft(toCenter, (int) (Math.floor(lineLength - toCenter.length()) / 2), ' ');
     }
 
     /**
      * Trims whitespace off each element in a {@link String} array
      *
      * @param toTrim
      *         the {@link String} array to clean
      *
      * @return whitespace cleaned {@link String} array
      *
      * @throws UtilityException
      *         if toTrim is null or empty
      */
     public static String[] trimElements(String[] toTrim) throws UtilityException {
         notNull(toTrim, "String[] toTrim");
         notEmpty(toTrim, "String[] toTrim");
 
         String[] toRet = toTrim.clone();
         for (int index = 0; index < toRet.length; index++) {
             toRet[index] = toTrim[index].trim();
         }
         return toRet;
     }
 
     /**
      * Splits a {@link String} at commas ',' and converts the elements to bytes
      *
      * @param str
      *         the string to be converted
      *
      * @return byte array of the string
      *
      * @throws UtilityException
      */
     public static byte[] stringToByteArray(String str) throws UtilityException {
         return stringToByteArray(str, ",");
     }
 
     /**
      * Splits a {@link String} at specified character(s) and converts the elements to bytes
      *
      * @param str
      *         the string to be converted
      * @param delimiter
      *         the character(s) to separate at
      *
      * @return byte array of the string
      *
      * @throws UtilityException
      *         if str is null or empty<br>
      *         or if delimiter is null or empty<br>
      *         or if an element is not a number
      */
     public static byte[] stringToByteArray(String str, String delimiter) throws UtilityException {
         notNull(str, "String str");
         notNull(delimiter, "String delimiter");
         notEmpty(str, "String str");
         notEmpty(delimiter, "String delimiter");
 
         return stringArrayToByteArray(str.split(delimiter));
     }
 
     /**
      * Converts a String Array into a byte array
      *
      * @param strings
      *         the string array to convert
      *
      * @return byte array
      *
      * @throws UtilityException
      */
     public static byte[] stringArrayToByteArray(String[] strings) throws UtilityException {
         notNull(strings, "String[] strArray");
         notEmpty(strings, "String[] strArray");
 
         byte[] toRet = new byte[strings.length];
         for (int index = 0; index < strings.length; index++) {
             try {
                 toRet[index] = Byte.parseByte(strings[index].trim());
             }
             catch (NumberFormatException nfe) {
                 throw new UtilityException("str.nan", strings[index]);
             }
         }
         return toRet;
     }
 
     /**
      * Joins a byte array into a single {@link String} separated by a comma
      *
      * @param bytes
      *         the byte array to be joined
      *
      * @return the joined byte array as a String
      *
      * @throws UtilityException
      *         if bytes is null or empty
      */
     public static String byteArrayToString(byte[] bytes) throws UtilityException {
         return byteArrayToString(bytes, ",");
     }
 
     /**
      * Joins a byte array into a single {@link String} separated by specified character(s)
      *
      * @param bytes
      *         the byte array to be joined
      * @param delimiter
      *         the character(s) to space the bytes apart with
      *
      * @return the joined byte array as a String
      *
      * @throws UtilityException
      *         if bytes is null or empty<br/>
      *         or if spacer is null
      */
     public static String byteArrayToString(byte[] bytes, String delimiter) throws UtilityException {
         return joinString(byteArrayToStringArray(bytes), delimiter, 0);
     }
 
     /**
      * Converts the elements of the byte array to Strings
      *
      * @param bytes
      *         the byte array to convert
      *
      * @return string array
      *
      * @throws UtilityException
      *         if bytes is null or empty
      */
     public static String[] byteArrayToStringArray(byte[] bytes) throws UtilityException {
         notNull(bytes, "byte[] bytes");
         notEmpty(bytes, "byte[] bytes");
 
         String[] arr = new String[bytes.length];
         for (int index = 0; index < bytes.length; index++) {
             arr[index] = String.valueOf(bytes[index]);
         }
         return arr;
     }
 
     /**
      * Splits a {@link String} at commas ',' and converts the elements to short
      *
      * @param str
      *         the string to be converted
      *
      * @return short array of the string
      *
      * @throws UtilityException
      *         if str is null or empty<br/>
      *         or if an element is not a number
      */
     public static short[] stringToShortArray(String str) throws UtilityException {
         return stringToShortArray(str, ",");
     }
 
     /**
      * Splits a {@link String} at specified character(s) and converts the elements to short
      *
      * @param str
      *         the string to be converted
      * @param delimiter
      *         the character(s) to split at
      *
      * @return short array of the string
      *
      * @throws UtilityException
      *         if str is null or empty<br/>
      *         or if delimiter is null or empty<br/>
      *         or if an element is not a number
      */
     public static short[] stringToShortArray(String str, String delimiter) throws UtilityException {
         notNull(str, "String str");
         notNull(delimiter, "String delimiter");
         notEmpty(str, "String str");
         notEmpty(delimiter, "String delimiter");
 
         return stringArrayToShortArray(str.split(delimiter));
     }
 
     /**
      * Converts a String Array into a short array
      *
      * @param strings
      *         the string array to convert
      *
      * @return short array
      *
      * @throws UtilityException
      *         if strings is null or empty
      */
     public static short[] stringArrayToShortArray(String[] strings) throws UtilityException {
         notNull(strings, "String[] strArray");
         notEmpty(strings, "String[] strArray");
 
         short[] toRet = new short[strings.length];
         for (int index = 0; index < strings.length; index++) {
             try {
                 toRet[index] = Short.parseShort(strings[index].trim());
             }
             catch (NumberFormatException nfe) {
                 throw new UtilityException("str.nan", strings[index]);
             }
         }
         return toRet;
     }
 
     /**
      * Joins a shot array into a single {@link String} separated by a comma
      *
      * @param shorts
      *         the short array to be joined
      *
      * @return the joined short array as a String
      *
      * @throws UtilityException
      *         if shorts is null or empty
      */
     public static String shortArrayToString(short[] shorts) throws UtilityException {
         return shortArrayToString(shorts, ",");
     }
 
     /**
      * Joins a short array into a single {@link String} separated by specified character(s)
      *
      * @param shorts
      *         the short array to be joined
      * @param delimiter
      *         the character(s) to space the shorts apart with
      *
      * @return the joined short array as a String
      *
      * @throws UtilityException
      *         if shorts is null or empty<br/>
      *         or if spacer is null
      */
     public static String shortArrayToString(short[] shorts, String delimiter) throws UtilityException {
         return joinString(shortArrayToStringArray(shorts), delimiter, 0);
     }
 
     /**
      * Converts the elements of the short array to Strings
      *
      * @param shorts
      *         the short array to be converted
      *
      * @return string array
      *
      * @throws UtilityException
      *         if shorts is null or empty
      */
     public static String[] shortArrayToStringArray(short[] shorts) throws UtilityException {
         notNull(shorts, "short[] shorts");
         notEmpty(shorts, "short[] shorts");
 
         String[] arr = new String[shorts.length];
         for (int index = 0; index < shorts.length; index++) {
             arr[index] = String.valueOf(shorts[index]);
         }
         return arr;
     }
 
     /**
      * Splits a {@link String} at commas ',' and converts the elements to Integers
      *
      * @param str
      *         the string to be converted
      *
      * @return int array of the string
      *
      * @throws UtilityException
      *         if str is null or empty<br/>
      *         if an element is not a number
      */
     public static int[] stringToIntArray(String str) throws UtilityException {
         return stringToIntArray(str, ",");
     }
 
     /**
      * Splits a {@link String} at specified character(s) and converts the elements to int
      *
      * @param str
      *         the string to be converted
      * @param delimiter
      *         the character(s) to split at
      *
      * @return int array of the string
      *
      * @throws UtilityException
      *         if str is null or empty<br>
      *         or if delimiter is null or empty<br>
      *         or if an element is not a number
      */
     public static int[] stringToIntArray(String str, String delimiter) throws UtilityException {
         notNull(str, "String str");
         notNull(delimiter, "String delimiter");
         notEmpty(str, "String str");
         notEmpty(delimiter, "String delimiter");
 
         return stringArrayToIntArray(str.split(delimiter));
     }
 
     /**
      * Converts a String Array into a int array
      *
      * @param strings
      *         the string array to convert
      *
      * @return int array
      *
      * @throws UtilityException
      *         if strings is null or empty
      */
     public static int[] stringArrayToIntArray(String[] strings) throws UtilityException {
         notNull(strings, "String[] strings");
         notEmpty(strings, "String[] strings");
 
         int[] toRet = new int[strings.length];
         for (int index = 0; index < strings.length; index++) {
             try {
                 toRet[index] = Integer.parseInt(strings[index].trim());
             }
             catch (NumberFormatException nfe) {
                 throw new UtilityException("str.nan", strings[index]);
             }
         }
         return toRet;
     }
 
     /**
      * Joins a int array into a single {@link String} separated by a comma
      *
      * @param integers
      *         the int array to be joined
      *
      * @return the joined int array as a String
      *
      * @throws UtilityException
      *         if integers is null or empty
      */
     public static String intArrayToString(int[] integers) throws UtilityException {
         return intArrayToString(integers, ",");
     }
 
     /**
      * Joins a int array into a single {@link String} separated by specified character(s)
      *
      * @param integers
      *         the int array to be joined
      * @param delimiter
      *         the character(s) to space the integers apart with
      *
      * @return the joined int array as a String
      *
      * @throws UtilityException
      *         if integers is null or empty<br>
      *         or if delimiter is null
      */
     public static String intArrayToString(int[] integers, String delimiter) throws UtilityException {
         return joinString(intArrayToStringArray(integers), delimiter, 0);
     }
 
     /**
      * Converts the elements of the int array to Strings
      *
      * @param integers
      *         the int array to convert
      *
      * @return string array
      *
      * @throws UtilityException
      */
     public static String[] intArrayToStringArray(int[] integers) throws UtilityException {
         notNull(integers, "int[] integers");
         notEmpty(integers, "int[] integers");
 
         String[] arr = new String[integers.length];
         for (int index = 0; index < integers.length; index++) {
             arr[index] = String.valueOf(integers[index]);
         }
         return arr;
     }
 
     /**
      * Splits a {@link String} at commas ',' and converts the elements to longs
      *
      * @param str
      *         the string to be converted
      *
      * @return long array of the string
      *
      * @throws UtilityException
      */
     public static long[] stringToLongArray(String str) throws UtilityException {
         return stringToLongArray(str, ",");
     }
 
     /**
      * Splits a {@link String} at specified character(s) and converts the elements to long
      *
      * @param str
      *         the string to be converted
      * @param delimiter
      *         the character(s) to split at
      *
      * @return int array of the string
      *
      * @throws UtilityException
      *         if str is null or empty<br>
      *         or if splitBy is null or empty<br>
      *         or if an element is not a number
      */
     public static long[] stringToLongArray(String str, String delimiter) throws UtilityException {
         notNull(str, "String str");
         notNull(delimiter, "String delimiter");
         notEmpty(str, "String str");
         notEmpty(delimiter, "String delimiter");
 
         return stringArrayToLongArray(str.split(delimiter));
     }
 
     /**
      * Converts a String Array into a long array
      *
      * @param strings
      *         the string array to convert
      *
      * @return long array
      *
      * @throws UtilityException
      */
     public static long[] stringArrayToLongArray(String[] strings) throws UtilityException {
         notNull(strings, "String[] strings");
         notEmpty(strings, "String[] strings");
 
         long[] toRet = new long[strings.length];
         for (int index = 0; index < strings.length; index++) {
             try {
                 toRet[index] = Long.parseLong(strings[index].trim());
             }
             catch (NumberFormatException nfe) {
                 throw new UtilityException("str.nan", strings[index]);
             }
         }
         return toRet;
     }
 
     /**
      * Joins a long array into a single {@link String} separated by a comma
      *
      * @param longs
      *         the long array to be joined
      *
      * @return the joined long array as a String
      *
      * @throws UtilityException
      *         if longs is null or empty<br>
      */
     public static String longArrayToString(long[] longs) throws UtilityException {
         return longArrayToString(longs, ",");
     }
 
     /**
      * Joins a long array into a single {@link String} separated by specified character(s)
      *
      * @param longs
      *         the long array to be joined
      * @param delimiter
      *         the character(s) to space the longs apart with
      *
      * @return the joined long array as a String
      *
      * @throws UtilityException
      *         if longs is null or empty<br>
      *         or if delimiter is null
      */
     public static String longArrayToString(long[] longs, String delimiter) throws UtilityException {
         return joinString(longArrayToStringArray(longs), delimiter, 0);
     }
 
     /**
      * Converts the elements of the long array to Strings
      *
      * @param longs
      *         the long array to convert
      *
      * @return string array
      *
      * @throws UtilityException
      */
     public static String[] longArrayToStringArray(long[] longs) throws UtilityException {
         notNull(longs, "long[] longs");
         notEmpty(longs, "long[] longs");
 
         String[] arr = new String[longs.length];
         for (int index = 0; index < longs.length; index++) {
             arr[index] = String.valueOf(longs[index]);
         }
         return arr;
     }
 
     /**
      * Splits a {@link String} at commas ',' and converts the elements to floats
      *
      * @param str
      *         the string to be converted
      *
      * @return float array of the string
      *
      * @throws UtilityException
      */
     public static float[] stringToFloatArray(String str) throws UtilityException {
         return stringToFloatArray(str, ",");
     }
 
     /**
      * Splits a {@link String} at specified character(s) and converts the elements to float
      *
      * @param str
      *         the string to be converted
      * @param delimiter
      *         the character(s) to split at
      *
      * @return float array of the string
      *
      * @throws UtilityException
      *         if str is null or empty<br>
      *         or if splitBy is null or empty<br>
      *         or if an element is not a number
      */
     public static float[] stringToFloatArray(String str, String delimiter) throws UtilityException {
         notNull(str, "String str");
         notNull(delimiter, "String delimiter");
         notEmpty(str, "String str");
         notNull(delimiter, "String delimiter");
 
         return stringArrayToFloatArray(str.split(delimiter));
     }
 
     /**
      * Converts a String Array into a float array
      *
      * @param strings
      *         the string array to convert
      *
      * @return float array
      *
      * @throws UtilityException
      */
     public static float[] stringArrayToFloatArray(String[] strings) throws UtilityException {
         notNull(strings, "Strings[] strings");
         notEmpty(strings, "String[] strings");
 
         float[] toRet = new float[strings.length];
         for (int index = 0; index < strings.length; index++) {
             try {
                 toRet[index] = Float.parseFloat(strings[index].trim());
             }
             catch (NumberFormatException nfe) {
                 throw new UtilityException("str.nan", strings[index]);
             }
         }
         return toRet;
     }
 
     /**
      * Joins a float array into a single {@link String} separated by a comma
      *
      * @param floats
      *         the float array to be joined
      *
      * @return the joined float array as a String
      *
      * @throws UtilityException
      *         if floats is null or empty<br>
      */
     public static String floatArrayToString(float[] floats) throws UtilityException {
         return floatArrayToString(floats, ",");
     }
 
     /**
      * Joins a float array into a single {@link String} separated by specified character(s)
      *
      * @param floats
      *         the float array to be joined
      * @param delimiter
      *         the character(s) to space the floats apart with
      *
      * @return the joined float array as a String
      *
      * @throws UtilityException
      *         if floats is null or empty<br>
      *         or if spacer is null
      */
     public static String floatArrayToString(float[] floats, String delimiter) throws UtilityException {
         return joinString(floatArrayToStringArray(floats), delimiter, 0);
     }
 
     /**
      * Converts the elements of the float array to Strings
      *
      * @param floats
      *         the float array to convert
      *
      * @return string array
      *
      * @throws UtilityException
      */
     public static String[] floatArrayToStringArray(float[] floats) throws UtilityException {
         notNull(floats, "float[] floats");
         notEmpty(floats, "float[] floats");
 
         String[] arr = new String[floats.length];
         for (int index = 0; index < floats.length; index++) {
             arr[index] = String.valueOf(floats[index]);
         }
         return arr;
     }
 
     /**
      * Splits a {@link String} at commas ',' and converts the elements to double
      *
      * @param str
      *         the string to be converted
      *
      * @return double array of the string
      *
      * @throws UtilityException
      *         if str is null or empty<br>
      *         or if an element is not a number
      */
     public static double[] stringToDoubleArray(String str) throws UtilityException {
         return stringToDoubleArray(str, ",");
     }
 
     /**
      * Splits a {@link String} at specified character(s) and converts the elements to double
      *
      * @param str
      *         the string to be converted
      * @param delimiter
      *         the character(s) to split at
      *
      * @return double array of the string
      *
      * @throws UtilityException
      *         if str is null or empty<br>
      *         or if delimiter is null or empty<br>
      *         or if an element is not a number
      */
     public static double[] stringToDoubleArray(String str, String delimiter) throws UtilityException {
         notNull(str, "String str");
         notNull(delimiter, "String delimiter");
         notEmpty(str, "String str");
         notEmpty(delimiter, "String delimiter");
 
         return stringArrayToDoubleArray(str.split(delimiter));
     }
 
     /**
      * Converts a String Array into a double array
      *
      * @param strings
      *         the string array to convert
      *
      * @return double array
      *
      * @throws UtilityException
      */
     public static double[] stringArrayToDoubleArray(String[] strings) throws UtilityException {
         notNull(strings, "String[] strings");
         notEmpty(strings, "String[] strings");
 
         double[] toRet = new double[strings.length];
         for (int index = 0; index < strings.length; index++) {
             try {
                 toRet[index] = Double.parseDouble(strings[index].trim());
             }
             catch (NumberFormatException nfe) {
                 throw new UtilityException("str.nan", strings[index]);
             }
         }
         return toRet;
     }
 
     /**
      * Joins a double array into a single {@link String} separated by a comma
      *
      * @param doubles
      *         the doubles array to be joined
      *
      * @return the joined double array as a String
      *
      * @throws UtilityException
      *         if doubles is null or empty
      */
     public static String doubleArrayToString(double[] doubles) throws UtilityException {
         return doubleArrayToString(doubles, ",");
     }
 
     /**
      * Joins a double array into a single {@link String} separated by specified character(s)
      *
      * @param doubles
      *         the double array to be joined
      * @param delimiter
      *         the character(s) to space the doubles apart with
      *
      * @return the joined double array as a String
      *
      * @throws UtilityException
      *         if doubles is null or empty<br>
      *         or if delimiter is null or empty
      */
     public static String doubleArrayToString(double[] doubles, String delimiter) throws UtilityException {
         return joinString(doubleArrayToStringArray(doubles), delimiter, 0);
     }
 
     /**
      * Converts the elements of the double array to Strings
      *
      * @param doubles
      *         the double array to convert
      *
      * @return string array
      *
      * @throws UtilityException
      */
     public static String[] doubleArrayToStringArray(double[] doubles) throws UtilityException {
         notNull(doubles, "double[] doubles");
         notEmpty(doubles, "double[] doubles");
 
         String[] arr = new String[doubles.length];
         for (int index = 0; index < doubles.length; index++) {
             arr[index] = String.valueOf(doubles[index]);
         }
         return arr;
     }
 
     /**
      * Splits a {@link String} at commas ',' and converts the elements to boolean
      *
      * @param str
      *         the string to be converted
      *
      * @return boolean array of the string
      *
      * @throws UtilityException
      *         if str is null or empty<br>
      *         or if an element is not a number
      */
     public static boolean[] stringToBooleanArray(String str) throws UtilityException {
         return stringToBooleanArray(str, ",");
     }
 
     /**
      * Splits a {@link String} at specified character(s) and converts the elements to boolean
      *
      * @param str
      *         the string to be converted
      * @param delimiter
      *         the character(s) to split at
      *
      * @return boolean array of the string
      *
      * @throws UtilityException
      *         if str is null or empty<br>
      *         or if delimiter is null or empty<br>
      *         or if an element is not a number
      */
     public static boolean[] stringToBooleanArray(String str, String delimiter) throws UtilityException {
         notNull(str, "String str");
         notNull(delimiter, "String delimiter");
         notEmpty(str, "String str");
         notEmpty(delimiter, "String delimiter");
 
         return stringArrayToBooleanArray(str.split(delimiter));
     }
 
     /**
      * Converts a String Array into a float array
      *
      * @param strings
      *         the string array to convert
      *
      * @return boolean array
      *
      * @throws UtilityException
      */
     public static boolean[] stringArrayToBooleanArray(String[] strings) throws UtilityException {
         notNull(strings, "String[] strings");
         notEmpty(strings, "String[] strings");
 
         boolean[] toRet = new boolean[strings.length];
         for (int index = 0; index < strings.length; index++) {
             toRet[index] = BooleanUtils.parseBoolean(strings[index].trim());
         }
         return toRet;
     }
 
     /**
      * Joins a boolean array into a single {@link String} separated by a comma
      *
      * @param booleans
      *         the boolean array to be joined
      *
      * @return the joined boolean array as a String
      *
      * @throws UtilityException
      *         if doubles is null or empty<br>
      */
     public static String booleanArrayToString(boolean[] booleans) throws UtilityException {
         return booleanArrayToString(booleans, ",");
     }
 
     /**
      * Joins a boolean array into a single {@link String} separated by specified character(s)
      *
      * @param booleans
      *         the boolean array to be joined
      * @param delimiter
      *         the character(s) to space the booleans apart with
      *
      * @return the joined double array as a String
      *
      * @throws UtilityException
      *         if doubles is null or empty<br>
      *         or if spacer is null
      */
     public static String booleanArrayToString(boolean[] booleans, String delimiter) throws UtilityException {
         return joinString(booleanArrayToStringArray(booleans), delimiter, 0);
     }
 
     /**
      * Converts the elements of the boolean array to Strings
      *
      * @param booleans
      *         the boolean array to convert
      *
      * @return string array
      *
      * @throws UtilityException
      */
     public static String[] booleanArrayToStringArray(boolean[] booleans) throws UtilityException {
         notNull(booleans, "boolean[] booleans");
         notEmpty(booleans, "boolean[] booleans");
 
         String[] arr = new String[booleans.length];
         for (int index = 0; index < booleans.length; index++) {
             arr[index] = String.valueOf(booleans[index]);
         }
         return arr;
     }
 
     /**
      * Gets this class's version number
      *
      * @return the class version
      */
     public static final float getClassVersion() {
         return classVersion;
     }
 }
