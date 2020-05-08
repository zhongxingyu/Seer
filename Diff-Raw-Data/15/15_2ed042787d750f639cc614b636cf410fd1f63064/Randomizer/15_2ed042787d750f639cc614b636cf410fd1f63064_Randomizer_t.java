 package com.huskycode.jpaquery.util;
 
 import org.apache.commons.lang.RandomStringUtils;
 
 import java.util.Random;
 
 /** */
 public class Randomizer {
     
     private static final Random any = new Random();
     
     public static String getString(int length) {
         return (length == 0) ? "" : RandomStringUtils.random(Math.abs(length));
     }
 
     public static String getString() {
        return getString(any.nextInt());
     }
 
     public static String getAsciiString(int length) {
         return (length == 0) ? "" : RandomStringUtils.randomAscii(Math.abs(length));
     }
 
     public static String getAsciiString() {
        return getAsciiString(any.nextInt());
     }
     
     public static int getInt() {
         return any.nextInt();
     }
 
     public static int getNonNegativeInt() {
         return Math.abs(any.nextInt());
     }
 
     public static long getLong() {
         return any.nextLong();
     }
 
     public static long getNonNegativeLong() {
         return Math.abs(any.nextLong());
     }
 
     public static double getDouble() {
         return any.nextDouble();
     }
 
     public static boolean getBoolean() {
         return any.nextDouble() > 0.5;
     }
 }
