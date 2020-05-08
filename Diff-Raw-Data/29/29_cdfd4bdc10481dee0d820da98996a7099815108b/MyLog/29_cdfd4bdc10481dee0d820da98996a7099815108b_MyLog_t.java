 package mobi.monaca.framework.util;
 
 import mobi.monaca.framework.psedo.BuildConfig;
 import mobi.monaca.utils.log.LogItem;
 import mobi.monaca.utils.log.LogSource;
 import android.content.Context;
 import android.content.Intent;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Log;
 
 public class MyLog {
 	public static final String PREFIX = "[MyLog] ";
 	public static void i(String TAG, String msg) {
 		if (BuildConfig.DEBUG) {
 			Log.i(PREFIX + TAG, msg);
 		}
 	}
 
 	public static void v(String TAG, String msg) {
 //		Log.i(PREFIX + TAG, "debug flag = " + BuildConfig.DEBUG);
 		if (BuildConfig.DEBUG) {
 			Log.v(PREFIX + TAG, msg);
 		}
 	}
 
 	public static void w(String TAG, String msg) {
 		if (BuildConfig.DEBUG) {
 			Log.w(PREFIX + TAG, msg);
 		}
 	}
 
 	public static void d(String TAG, String msg) {
 		if (BuildConfig.DEBUG) {
 			Log.d(PREFIX + TAG, msg);
 		}
 	}
 
	public static void d(String TAG, String msg, Throwable e) {
		if (BuildConfig.DEBUG) {
			Log.d(PREFIX + TAG, msg, e);
		}
	}

 	public static void e(String TAG, String msg) {
 		if (BuildConfig.DEBUG) {
 			Log.e(PREFIX + TAG, msg);
 		}
 	}
 
 	// send debuglog to debbuger and server
 	public static void sendBloadcastDebugLog(Context context, LogItem logItem) {
 		MyLog.v("MyLog", "sendBloadcastDebugLog. message:" + logItem.getMessage());
 		Intent broadcastIntent = new Intent("log_message_action");
 		broadcastIntent.putExtra("message", logItem.getMessage());
 		broadcastIntent.putExtra("source", logItem.getSource().toString());
 		broadcastIntent.putExtra("url", logItem.getUrl());
 		broadcastIntent.putExtra("line", logItem.getLineNumber());
 		broadcastIntent.putExtra("logLevel", logItem.getLogLevel().toString());
 		LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(broadcastIntent);
 	}
 	
 //	public static void sendBloadcastDebugLog(Context context, String broadcastMessage, String logLevel, LogSource source, String url, int lineNumber) {
 //		MyLog.v("MyLog", "sendBloadcastDebugLog. message:" + broadcastMessage);
 //		Intent broadcastIntent = new Intent("log_message_action");
 //		broadcastIntent.putExtra("message", broadcastMessage);
 //		broadcastIntent.putExtra("source", source);
 //		broadcastIntent.putExtra("url", url);
 //		broadcastIntent.putExtra("line", lineNumber);
 //		broadcastIntent.putExtra("logLevel", logLevel);
 //		LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(broadcastIntent);
 //	}
 	
 	public static void sendBloadcastPongLog(Context context) {
 		MyLog.i("MyLog", "sendBloadcastPongLog");
 		Intent broadcastIntent = new Intent("log_message_action");
 		broadcastIntent.putExtra("pong", true);
 		LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(broadcastIntent);
 	}
 }
