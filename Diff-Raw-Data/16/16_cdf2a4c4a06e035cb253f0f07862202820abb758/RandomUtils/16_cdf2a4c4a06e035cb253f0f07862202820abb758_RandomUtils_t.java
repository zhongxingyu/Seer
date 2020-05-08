 /*
 
    Copyright 2011 Monits
  
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 
 */
 
 
 
 /**
  * Random Utils.
  *
  * @copyright 2011 Monits
  * @license   Apache 2.0 License
  * @version   Release: 1.0.0
  * @link      http://www.monits.com/
  * @since     1.0.0
  */
 package com.monits.commons.utils;
 
 import java.util.Random;
 
 /**
  * Random Utils.
  *
  * @author    Maximiliano Luque <mluque@monits.com>
  * @copyright 2011 Monits
  * @license   Apache 2.0 License
  * @version   Release: 1.0.0
  * @link      http://www.monits.com/
  * @since     1.0.0
  */
 public class RandomUtils {
 
 	private static final char[] LATIN_ALPHABET = { 'a', 'b', 'c', 'd', 'e', 'f',
 		'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'ñ', 'o', 'p', 'q', 'r',
 		's', 't', 'u', 'v', 'w', 'x', 'y', 'z', };
 
 	private static final char[] ALPHANUMERIC = { '0', '1', '2', '3', '4', '5',
 		'6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i',
 		'j', 'k', 'l', 'm', 'n', 'ñ', 'o', 'p', 'q', 'r', 's', 't', 'u',
 		'v', 'w', 'x', 'y', 'z', };
 
 	private static final char[] HEXADECIMAL = { '0', '1', '2', '3', '4', '5',
 		'6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', };
 
 	private static final char[] NUMERIC = { '0', '1', '2', '3', '4', '5',
 		'6', '7', '8', '9', };
 
 	/**
 	 * Utility classes should not have a public or default constructor.
 	 */
 	private RandomUtils() {
		throw new AssertionError();
 	}
 
 	/**
 	 * Generates a random String with the number and type of characters requested.
 	 *
 	 * @param amount The number of characters in the string. If negative, it will be treated as zero.
 	 * @param typeChar The type of characters.
 	 *
 	 * @return The random String.
 	 */
 	public static String generateRandomString(final int amount, final char[] typeChar) {
 
 		final Random random = new Random();
 
 		final StringBuffer buffer = new StringBuffer();
 
 		for (int i = 0; i < amount ; i++) {
 			buffer.append(typeChar[random.nextInt(typeChar.length - 1)]);
 		}
 
 		return buffer.toString();
 	}
 
 	/**
 	 * Generates a random String with the number of characters requested in alphanumeric.
 	 * (this is just to keep the compatibility with older versions).
 	 *
 	 * @param amount The number of characters in the string. If negative, it will be treated as zero.
 	 *
 	 * @return A random string of length amount with lower case alphanumeric characters.
 	 * @deprecated Use {@link #generateRandomAlphanumericString(int)} instead.
 	 */
 	@Deprecated
 	public static String generateRandomString(final int amount) {
 		return generateRandomAlphanumericString(amount);
 	}
 
 	/**
 	 * Generates a random String with the number of characters requested in Latin alphabet. 
 	 *
 	 * @param amount The number of characters in the string. If negative, it will be treated as zero.
 	 *
 	 * @return A random string of length amount with lower case alphabetic characters. 
 	 */
 	public static String generateRandomLatinAlphabetString(final int amount) {
 
 		return generateRandomString(amount, LATIN_ALPHABET);
 	}
 
 	/**
 	 * Generates a random String with the number of characters requested in alphanumeric.
 	 *
 	 * @param amount The number of characters in the string. If negative, it will be treated as zero.
 	 *
 	 * @return A random string of length amount with lower case alphanumeric characters.
 	 */
 	public static String generateRandomAlphanumericString(final int amount) {
 
 		return generateRandomString(amount, ALPHANUMERIC);
 	}
 
 	/**
 	 * Generates a random String with the number of characters requested only with numbers.
 	 *
 	 * @param amount The number of characters in the string. If negative, it will be treated as zero.
 	 *
 	 * @return A random string of length amount with numeric characters. 
 	 */
 	public static String generateRandomNumericString(final int amount) {
 
 		return generateRandomString(amount, NUMERIC);
 	}
 
 	/**
 	 * Generates a random String with the number of characters requested in hexadecimal.
 	 *
 	 * @param amount The number of characters in the string. If negative, it will be treated as zero.
 	 *
 	 * @return A random string of length amount with lower case hexadecimal characters. .
 	 */
 	public static String generateRandomHexadecimalString(final int amount) {
 
 		return generateRandomString(amount, HEXADECIMAL);
 	}
 
 }
