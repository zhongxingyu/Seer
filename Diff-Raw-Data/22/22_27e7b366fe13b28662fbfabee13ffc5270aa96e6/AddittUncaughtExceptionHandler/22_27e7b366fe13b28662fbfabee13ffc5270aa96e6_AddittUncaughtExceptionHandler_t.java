 package com.additt.maple_android;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.lang.Thread.UncaughtExceptionHandler;
 import java.util.Calendar;
 import java.util.Locale;
 import java.util.TimeZone;
 
import android.content.Intent;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.Build;
 
 import com.loopj.android.http.AsyncHttpResponseHandler;
 import com.loopj.android.http.RequestParams;
 
 /**
  * This class handles uncaught exceptions. It creates and error report to send
  * to the server and then restarts the application gracefully (hopefully...)
  * 
  * @author Eli
  * 
  */
 public class AddittUncaughtExceptionHandler implements UncaughtExceptionHandler {
 	private final String SERVER_PATH = "log_entries";
 	private Thread.UncaughtExceptionHandler mDefaultUEH;
 	private MapleApplication mApp = null;
 
 	/**
 	 * This initializes an uncaught exception handler for Additt. It should be
 	 * used with Thread.setDefaultUncaughtExceptionHandler() to catch uncaught
 	 * exceptions in the activity.
 	 * 
 	 * @param app
 	 */
 	public AddittUncaughtExceptionHandler(MapleApplication app) {
 
 		mDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
 		mApp = app;
 	}
 
 	@Override
 	public void uncaughtException(Thread thread, Throwable ex) {
 		ex.printStackTrace();
 
 		// log the exception with the server
 		// get the version of Additt in use
 		String versionName = "";
 		try {
 			versionName = mApp.getPackageManager().getPackageInfo(
 					mApp.getPackageName(), 0).versionName;
 		} catch (NameNotFoundException e) {
 			versionName = "Version Name not available";
 		}
 
 		// get the stacktrace in string form
 		StringWriter sw = new StringWriter();
 		PrintWriter pw = new PrintWriter(sw);
 		ex.printStackTrace(pw);
 		String stackTrace = sw.toString();
 
 		// If the exception was thrown in a background thread inside
 		// AsyncTask, then the actual exception can be found with getCause
 		stackTrace += "--------- Cause ---------\n\n";
 		Throwable cause = ex.getCause();
 		if (cause != null) {
 			stackTrace += cause.toString() + "\n\n";
 			StackTraceElement[] arr = cause.getStackTrace();
 			for (int i = 0; i < arr.length; i++) {
 				stackTrace += "    " + arr[i].toString() + "\n";
 			}
 		}
 		stackTrace += "-------------------------------\n\n";
 
 		// log whatever details we want to report to the server
 		RequestParams params = new RequestParams();
 		params.put("additt_version", versionName);
 		params.put("android_build", "" + Build.VERSION.SDK_INT);
 		// put time in PST
 		String time = Calendar
 				.getInstance(TimeZone.getTimeZone("America/Los_Angeles"),
 						Locale.US).getTime().toString();
 		params.put("time", time);
 		params.put("stack_trace", stackTrace);
		
		// get ad creation log if one has been started
		AdCreationManager manager =  mApp.getAdCreationManager();
		if(manager != null){
			params.put("ad_creation_log", manager.getLog());
		} else {
			params.put("ad_creation_log", "Ad creation not started");
		}
 
 		// post report details to the server. We don't need
 		// to register a response callback because we don't care
 		// about the response
 		MapleHttpClient.post(SERVER_PATH, params,
 				new AsyncHttpResponseHandler());
 
 		System.exit(0);
 	}
 
 }
