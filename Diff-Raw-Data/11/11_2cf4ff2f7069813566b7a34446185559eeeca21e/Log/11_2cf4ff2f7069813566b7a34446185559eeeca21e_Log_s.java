 package com.lostandfound;
 
 /**
  * Encapulates android.util.Log and save logs to second location
  *
  */
 public class Log {
	private static final String	GLOBAL_TAG = "IMMobile";
 	private	static final String	DELIMITER = "\t--\t";
 	
 	public static void v(String tag, String message) {
 		if(Common.DEBUG) android.util.Log.v(GLOBAL_TAG, tag+DELIMITER+message);
 	}
 	
 	public static void d(String tag, String message) {
 		if(Common.DEBUG) android.util.Log.d(GLOBAL_TAG, tag+DELIMITER+message);
 	}
 	
 	public static void i(String tag, String message) {
 		if(Common.DEBUG) android.util.Log.i(GLOBAL_TAG, tag+DELIMITER+message);
 	}
 	
 	public static void w(String tag, String message) {
 		if(Common.DEBUG) android.util.Log.w(GLOBAL_TAG, tag+DELIMITER+message);
 	}
 	
 	public static void e(String tag, String message) {
 		if(Common.DEBUG) android.util.Log.e(GLOBAL_TAG, tag+DELIMITER+message);
 	}
 	
 	public static void wtf(String tag, String message) {
 		if(Common.DEBUG) android.util.Log.wtf(GLOBAL_TAG, tag+DELIMITER+message);
 	}
 }
