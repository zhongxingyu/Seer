 package com.secondhand.debug;
 
 import android.util.Log;
 
//@SuppressWarnings("PMD")
 public final class MyDebug{ 
 	// ===========================================================
 	// Constants
 	// ===========================================================
 
 	// ===========================================================
 	// Fields
 	// ===========================================================
 
 	private static String sDebugTag = "Twirl";
 	private static DebugLevel sDebugLevel = DebugLevel.VERBOSE;
 
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 
 	private MyDebug() {}
 	
 	// ===========================================================
 	// Getter & Setter
 	// ===========================================================
 
 	public static String getDebugTag() {
 		return MyDebug.sDebugTag;
 	}
 	
 	public static void setDebugTag(final String pDebugTag) {
 		MyDebug.sDebugTag = pDebugTag;
 	}
 
 	public static DebugLevel getDebugLevel() {
 		return MyDebug.sDebugLevel;
 	}
 	
 
 	public static void setDebugLevel(final DebugLevel pDebugLevel) {
 		if(pDebugLevel == null) {
 			throw new IllegalArgumentException("pDebugLevel must not be null!");
 		}
 		MyDebug.sDebugLevel = pDebugLevel;
 	}
 
 	// ===========================================================
 	// Methods for/from SuperClass/Interfaces
 	// ===========================================================
 
 	// ===========================================================
 	// Methods
 	// ===========================================================
 
 	public static void v(final String pMessage) {
 		MyDebug.v(pMessage, null);
 	}
 
 	public static void v(final String pMessage, final Throwable pThrowable) {
 		if(sDebugLevel.isSameOrLessThan(DebugLevel.VERBOSE)) {
 			Log.v(sDebugTag, pMessage, pThrowable);
 		}
 	}
 
 	public static void d(final String pMessage) {
 		MyDebug.d(pMessage, null);
 	}
 
 	public static void d(final String pMessage, final Throwable pThrowable) {
 		if(sDebugLevel.isSameOrLessThan(DebugLevel.DEBUG)) {
 			Log.d(sDebugTag, pMessage, pThrowable);
 		}
 	}
 
 	public static void i(final String pMessage) {
 		MyDebug.i(pMessage, null);
 	}
 
 	public static void i(final String pMessage, final Throwable pThrowable) {
 		if(sDebugLevel.isSameOrLessThan(DebugLevel.INFO)) {
 			Log.i(sDebugTag, pMessage, pThrowable);
 		}
 	}
 
 	public static void w(final String pMessage) {
 		MyDebug.w(pMessage, null);
 	}
 
 	public static void w(final Throwable pThrowable) {
 		MyDebug.w("", pThrowable);
 	}
 
 	public static void w(final String pMessage, final Throwable pThrowable) {
 		if(sDebugLevel.isSameOrLessThan(DebugLevel.WARNING)) {
 			if(pThrowable == null) {
 				Log.w(sDebugTag, pMessage, new Exception());
 			} else {
 				Log.w(sDebugTag, pMessage, pThrowable);
 			}
 		}
 	}
 
 	public static void e(final String pMessage) {
 		MyDebug.e(pMessage, null);
 	}
 
 	public static void e(final Throwable pThrowable) {
 		MyDebug.e(sDebugTag, pThrowable);
 	}
 
 	public static void e(final String pMessage, final Throwable pThrowable) {
 		if(sDebugLevel.isSameOrLessThan(DebugLevel.ERROR)) {
 			if(pThrowable == null) {
 				Log.e(sDebugTag, pMessage, new Exception());
 			} else {
 				Log.e(sDebugTag, pMessage, pThrowable);
 			}
 		}
 	}
 
 	// ===========================================================
 	// Inner and Anonymous Classes
 	// ===========================================================
 
 	public static enum DebugLevel implements Comparable<DebugLevel> {
 		NONE,
 		ERROR,
 		WARNING,
 		INFO,
 		DEBUG,
 		VERBOSE;
 
 		public static final DebugLevel ALL = DebugLevel.VERBOSE;
 
 		private boolean isSameOrLessThan(final DebugLevel pDebugLevel) {
 			return this.compareTo(pDebugLevel) >= 0;
 		}
 	}
 }
