 package com.jozeflang.android.germanirregularverbs.util;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Random;
 
 
 /** 
 * Library class
  * @author @author Jozef Lang (developer@jozeflang.com)
  */
 public class Utils {
 
 	private Utils() {
 		
 	}
 	
 	/**
 	 * Returns random number from random number generator. Generated number is between selected boundaries.
 	 * @param from
 	 * @param to
 	 * @return
 	 */
 	public static int getRandom(int from, int to) {
 		return getRandom(new Random(), from, to);
 	}
 	
 	/**
 	 * Returns random number from random number generator. Generated number is between selected boundaries.
 	 * @param rg
 	 * @param from
 	 * @param to
 	 * @return
 	 */
 	public static int getRandom(final Random rg, int from, int to) {
 		int randomInt = rg.nextInt(to-from+1);
 		return randomInt + from;
 	}
 	
 	/**
 	 * Converts long value to integer.<br />
 	 * An {@link IllegalArgumentException} is thrown when long value is outside of the integer minimum or maximum values.
 	 * @param lvalue
 	 * @return
 	 */
 	public static int longToInt(final long lvalue) {
 		if (lvalue <= Integer.MIN_VALUE || lvalue > Integer.MAX_VALUE) 
 			throw new IllegalArgumentException(lvalue + " cannot be casted to integer");
 		return (int) lvalue;
 	}
 	
 	/**
 	 * Reads a text file from {@link InputStream} using {@link BufferedReader#readLine()} method <br />
 	 * If an exception occurs, a so far read content is returned 
 	 * @param in
 	 * @return
 	 */
 	public static String readTextFile(InputStream in) {
 		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
 		StringBuffer sb = new StringBuffer();
 		String line;
 		try {
 			while ((line = reader.readLine()) != null) {
 				sb.append(line);
 			}
 		} catch (IOException e) {
 			return sb.toString();
 		}
 		return sb.toString();
 	}
 }
