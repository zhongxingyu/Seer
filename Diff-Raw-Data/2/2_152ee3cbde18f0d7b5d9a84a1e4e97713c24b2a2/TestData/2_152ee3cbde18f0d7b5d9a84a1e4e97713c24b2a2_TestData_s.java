 /*
  *  Copyright 2008 the original author or authors.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 package net.sf.ipsedixit;
 
 import net.sf.ipsedixit.core.NumberMetaData;
 import net.sf.ipsedixit.core.StringMetaData;
 import net.sf.ipsedixit.core.StringType;
 import net.sf.ipsedixit.core.RandomDataProvider;
 import net.sf.ipsedixit.core.impl.DefaultRandomDataProvider;
 
 /**
  * A utility class that provides methods useful for setting up random values in your tests (or production code, if you
  * really want).
  */
 public final class TestData {
 
     private static final RandomDataProvider RANDOM_DATA_PROVIDER = new DefaultRandomDataProvider();
    private static final String[] WHITESPACE = {"", " ", "\t", "\n", "\r", "\r\n", String.valueOf((char) 0x0)};
 
     private TestData() {
     }
 
     /**
      * @return a random alpha-numeric string of {@link net.sf.ipsedixit.core.StringMetaData#DEFAULT_SIZE} length.
      */
     public static String randomString() {
         return randomString(StringType.ALPHANUMERIC);
     }
 
     /**
      * @param stringType a {@link net.sf.ipsedixit.core.StringType} to specify what the String should contain.
      * @return a string of length {@link net.sf.ipsedixit.core.StringMetaData#DEFAULT_SIZE} containing characters
      *         identified by the stringType paramter.
      */
     public static String randomString(StringType stringType) {
         return randomString(stringType, StringMetaData.DEFAULT_SIZE);
     }
 
     /**
      * @param stringType a {@link net.sf.ipsedixit.core.StringType} to specify what the String should contain.
      * @param length how long the String should be.
      * @return a String, generated to the specified length and the specified type.
      */
     public static String randomString(StringType stringType, int length) {
         return RANDOM_DATA_PROVIDER.randomString(stringType, length);
     }
 
     /**
      * @return true or false.
      */
     public static boolean randomBoolean() {
         return RANDOM_DATA_PROVIDER.randomBoolean();
     }
 
     /**
      * Picks out a value from the enumeration and returns it.
      *
      * @param enumClass the type of enum.
      * @param <E> an Enum type.
      * @return one of the values from the enum.
      */
     public static <E extends Enum> E randomEnum(Class<E> enumClass) {
         return RANDOM_DATA_PROVIDER.randomEnumValue(enumClass);
     }
 
     /**
      * Picks out a random value from an array and returns it.
      *
      * @param array an array of values to pick from.
      * @param <T> any Object type.
      * @return a random value from the array.
      */
     public static <T> T randomArrayElement(T[] array) {
         return RANDOM_DATA_PROVIDER.randomArrayElement(array);
     }
 
     /**
      * @return an integer between {@link net.sf.ipsedixit.core.NumberMetaData#DEFAULT_MINIMUM_NUMBER} and {@link
      *         net.sf.ipsedixit.core.NumberMetaData#DEFAULT_MAXIMUM_NUMBER}.
      */
     public static int randomInt() {
         return randomInt(NumberMetaData.DEFAULT_MINIMUM_NUMBER, NumberMetaData.DEFAULT_MAXIMUM_NUMBER);
     }
 
     /**
      * @param minValue the minimum inclusive value.
      * @param maxValue the maximum inclusive value.
      * @return an integer between minValue and maxValue, inclusive.
      */
     private static int randomInt(int minValue, int maxValue) {
         return (int) randomLong(minValue, maxValue);
     }
 
     /**
      * @return a long between {@link net.sf.ipsedixit.core.NumberMetaData#DEFAULT_MINIMUM_NUMBER} and {@link
      *         net.sf.ipsedixit.core.NumberMetaData#DEFAULT_MAXIMUM_NUMBER}.
      */
     public static long randomLong() {
         return randomLong(NumberMetaData.DEFAULT_MINIMUM_NUMBER, NumberMetaData.DEFAULT_MAXIMUM_NUMBER);
     }
 
     /**
      * @param minValue the minimum inclusive value.
      * @param maxValue the maximum inclusive value.
      * @return a long between minValue and maxValue, inclusive.
      */
     private static long randomLong(long minValue, long maxValue) {
         return RANDOM_DATA_PROVIDER.randomLongInRange(minValue, maxValue);
     }
 
     /**
      * @return a double between {@link net.sf.ipsedixit.core.NumberMetaData#DEFAULT_MINIMUM_NUMBER} and {@link
      *         net.sf.ipsedixit.core.NumberMetaData#DEFAULT_MAXIMUM_NUMBER}.
      */
     public static double randomDouble() {
         return randomDouble(NumberMetaData.DEFAULT_MINIMUM_NUMBER, NumberMetaData.DEFAULT_MAXIMUM_NUMBER);
     }
 
     /**
      * @param minValue the minimum inclusive value.
      * @param maxValue the maximum inclusive value.
      * @return a double between minValue inclusive, and maxValue exclusive.
      */
     private static double randomDouble(double minValue, double maxValue) {
         return RANDOM_DATA_PROVIDER.randomDoubleInRange(minValue, maxValue);
     }
 
     /**
      * @return a float between {@link net.sf.ipsedixit.core.NumberMetaData#DEFAULT_MINIMUM_NUMBER} and {@link
      *         net.sf.ipsedixit.core.NumberMetaData#DEFAULT_MAXIMUM_NUMBER}.
      */
     public static float randomFloat() {
         return randomFloat(NumberMetaData.DEFAULT_MINIMUM_NUMBER, NumberMetaData.DEFAULT_MAXIMUM_NUMBER);
     }
 
     /**
      * @param minValue the minimum inclusive value.
      * @param maxValue the maximum inclusive value.
      * @return a float between minValue inclusive, and maxValue exclusive.
      */
     private static float randomFloat(float minValue, float maxValue) {
         return (float) randomDouble(minValue, maxValue);
     }
 
     /**
      * Return an arbitrary String containing only whitespace characters.
      * <p/>
      * Gets a whitespace character from a set of whitespace value containing the following:
      * <p/>
      * <ul>
      * <li>empty string</li>
      * <li>space</li>
      * <li>tab</li>
      * <li>\n</li>
      * <li>\r</li>
      * <li>\r\n</li>
      * <li>0x0</li>
      * </ul>
      *
      * @return a random whitespace character.
      */
     public static String whitespace() {
         return randomArrayElement(WHITESPACE);
     }
 }
