 package com.madhackerdesigns.neverbelate.util;
 
 import android.util.Log;
 
 public class Logger {
 	
	private static final boolean LOGGING = false;
 	private static final String LOG_TAG = "NeverLate";
 	private static final int NOT_LOGGED = 0x0;
 
 	public static int d (String msg) {
 		if (LOGGING) { return Log.d(LOG_TAG, msg); }
 		else { return NOT_LOGGED; }
 	}
 	
 	public static int d (String tag, String msg) {
 		if (LOGGING) { return Log.d(tag, msg); }
 		else { return NOT_LOGGED; }
 	}
 	
 	public static int d (String tag, String msg, Throwable tr) {
 		if (LOGGING) { return Log.d(tag, msg, tr); }
 		else { return NOT_LOGGED; }
 	}
 	
 	public static int e (String tag, String msg) {
 		if (LOGGING) { return Log.e(tag, msg); }
 		else { return NOT_LOGGED; }
 	}
 	
 	public static int e (String tag, String msg, Throwable tr) {
 		if (LOGGING) { return Log.e(tag, msg, tr); }
 		else { return NOT_LOGGED; }
 	}
 	
 	public static String getStackTraceString (Throwable tr) {
 		return Log.getStackTraceString(tr);
 	}
 	
 	public static int i (String tag, String msg) {
 		if (LOGGING) { return Log.i(tag, msg); }
 		else { return NOT_LOGGED; }
 	}
 	
 	public static int i (String tag, String msg, Throwable tr) {
 		if (LOGGING) { return Log.i(tag, msg, tr); }
 		else { return NOT_LOGGED; }
 	}
 	
 	public static boolean isLoggable (String tag, int level) {
 		return Log.isLoggable(tag, level);
 	}
 	
 	public static int println (int priority, String tag, String msg) {
 		return Log.println(priority, tag, msg);
 	}
 	
 	public static int v (String tag, String msg) {
 		if (LOGGING) { return Log.v(tag, msg); }
 		else { return NOT_LOGGED; }
 	}
 	
 	public static int v (String tag, String msg, Throwable tr) {
 		if (LOGGING) { return Log.v(tag, msg, tr); }
 		else { return NOT_LOGGED; }
 	}
 	
 	public static int w (String tag, String msg) {
 		if (LOGGING) { return Log.w(tag, msg); }
 		else { return NOT_LOGGED; }
 	}
 	
 	public static int w (String tag, Throwable tr) {
 		if (LOGGING) { return Log.w(tag, tr); }
 		else { return NOT_LOGGED; }
 	}
 	
 	public static int w (String tag, String msg, Throwable tr) {
 		if (LOGGING) { return Log.w(tag, msg, tr); }
 		else { return NOT_LOGGED; }
 	}
 }
