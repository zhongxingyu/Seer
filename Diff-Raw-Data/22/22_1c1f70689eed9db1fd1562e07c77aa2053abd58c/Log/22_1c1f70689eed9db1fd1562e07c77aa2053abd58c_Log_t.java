 package com.deviceteam.kezdet;
 
 public class Log
 {
  private static int LEVEL = android.util.Log.INFO;
 
   public static void setLogLevel(int level)
   {
     LEVEL = level;
   }
 
   public static void verbose(String tag, String msg)
   {
     if (LEVEL<=android.util.Log.VERBOSE) {
       android.util.Log.v(tag, msg);
     }
   }
 
   public static void debug(String tag, String msg)
   {
     if (LEVEL<=android.util.Log.DEBUG) {
       android.util.Log.d( tag, msg );
     }
   }
 
   public static void info(String tag, String msg)
   {
     if (LEVEL<=android.util.Log.INFO) {
       android.util.Log.i( tag, msg );
     }
   }
 
   public static void warn(String tag, String msg)
   {
     if (LEVEL<=android.util.Log.WARN) {
       android.util.Log.w( tag, msg );
     }
   }
 
   public static void error(String tag, String msg)
   {
     if (LEVEL<=android.util.Log.ERROR) {
       android.util.Log.e(tag, msg);
     }
   }
 }
