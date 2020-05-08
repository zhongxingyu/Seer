 package com.gamalinda.java.util;
 
 public class Log {
 
 	private static final String ERROR = "ERROR";
 	private static final String DEBUG = "DEBUG";
 	private static final String INFO = "INFO";
	private static final String LOG_FORMAT = "{0}: {1}: {2}";
 
 	public static void e(String tag, String message) {
 		System.out.println(String.format(LOG_FORMAT, ERROR, tag, message));
 	}
 
 	public static void d(String tag, String message) {
 		System.out.println(String.format(LOG_FORMAT, DEBUG, tag, message));
 	}
 
 	public static void i(String tag, String message) {
 		System.out.println(String.format(LOG_FORMAT, INFO, tag, message));
 	}
 }
