 package com.avona.games.towerdefence;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 
 public final class Util {
 	public static void log(String s) {
 		System.out.println(s);
 	}
 
 	/**
 	 * Find the smallest power of two >= the input value. (Doesn't work for
 	 * negative numbers.)
 	 */
 	public static int roundUpPower2(int x) {
 		x = x - 1;
 		x = x | (x >> 1);
 		x = x | (x >> 2);
 		x = x | (x >> 4);
 		x = x | (x >> 8);
 		x = x | (x >> 16);
 		return x + 1;
 	}
 
	public static String Exception2String(Exception e) {
 		StringWriter sw = new StringWriter();
 		e.printStackTrace(new PrintWriter(sw));
 		return sw.toString();
 	}
 }
