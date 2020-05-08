 package com.r0adkll.deadskunk.utils;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.net.Uri;
 import android.os.Build;
 import android.util.Log;
 import android.webkit.MimeTypeMap;
 
 import java.net.URLConnection;
 import java.sql.Date;
 import java.util.Calendar;
 import java.util.IllegalFormatException;
 import java.util.Locale;
 import java.util.Random;
 import java.util.concurrent.TimeUnit;
 
 /**
  * This is the standard utils file containing common util functions
  * throughout my applications.
  * 
  * @author drew.heavner
  *
  */
 public class Utils {
 	
 	/**
 	 * Constants
 	 */
 	
 	/*
 	 * The 'VERBOSE' flag, this switches all log statements
 	 * that run throught this classes log shortcut functions
 	 */
 	public static final boolean VERBOSE = true;
 	
 	/*
 	 * This is the 'DEBUG' flag that signifies whether this application 
 	 * is in development mode, or production mode
 	 */
 	public static boolean DEBUG = true;
 	
 	/*
 	 * This is hte 'FREE_VERSION' flag that signifies whether this build
 	 * is the Full Price Version, or the Free Version 
 	 */
 	public static boolean FREE_VERSION = false;
 	
 	/**
 	 * Variables
 	 */
 	
 	// Random generator for use in the application
 	private static Random random = new Random();
 	
 	/* 
 	 * Float Format Constants 
 	 */
 	public static final String ONE_DIGIT = "%.1f";
 	public static final String TWO_DIGIT = "%.2f";
 	public static final String THREE_DIGIT = "%.3f";
 		
 	/**
 	 * Turn on debugging
 	 * @param value
 	 */
 	public static void setDebug(boolean value){
 		DEBUG = value;
 	}
 	
 	/**
 	 * Get the Random Number Generator
 	 * @return
 	 */
 	public static Random getRandom(){ return random; }
 	
 	/**
 	 * Format a time in milliseconds to 'hh:mm:ss' format
 	 * @param millis		the milliseconds time
 	 * @return				the time in 'hh:mm:ss' format
 	 */
 	public static String formatTimeText(int millis){
 		// Format the milliseconds into the form - 'hh:mm:ss'
 		return String.format(Locale.US, "%02d:%02d:%02d", 
 		    TimeUnit.MILLISECONDS.toHours(millis),
 		    TimeUnit.MILLISECONDS.toMinutes(millis) - 
 		    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
 		    TimeUnit.MILLISECONDS.toSeconds(millis) - 
 		    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
 	}
 	
 	/**
 	 * Format a time in milliseconds to 'mm:ss' format
 	 * @param millis		the milliseconds time
 	 * @return				the time in 'mm:ss' format
 	 */
 	public static String formatTimeTextMS(long millis){
 		// Format the milliseconds into the form - 'hh:mm:ss'
 		return String.format(Locale.US, "%02d:%02d",
 		    TimeUnit.MILLISECONDS.toMinutes(millis) - 
 		    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
 		    TimeUnit.MILLISECONDS.toSeconds(millis) - 
 		    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
 	}
 	
 	/**
 	 * Determine if the device is a tablet or not
 	 * @param ctx
 	 * @return
 	 */
 	public static boolean isTabletDevice(Context ctx) {
 		return (ctx.getResources().getConfiguration().screenLayout
 	            & Configuration.SCREENLAYOUT_SIZE_MASK)
 	            >= Configuration.SCREENLAYOUT_SIZE_LARGE;
 	}
 	
 	/**
 	 * Get the current Epoch Time
 	 * @return		current time in epoch
 	 */
 	public static long getEpochTime(){
 		return System.currentTimeMillis()/1000;
 	}
 	
 	/**
 	 * Convert Epoch time to a Date object
 	 * @param epoch
 	 * @return
 	 */
 	public static Date getDateFromEpoch(long epoch){
 		return new Date(epoch*1000);
 	}
 	
 	/**
 	 * Get the Device's GMT Offset
 	 * @return	the gmt offset in hours
 	 */
 	public static int getGMTOffset(){
 		Calendar now = Calendar.getInstance();
 		return (now.get(Calendar.ZONE_OFFSET) + now.get(Calendar.DST_OFFSET))  / 3600000;
 	}
 	
 	/**
 	 * Check to see if this device is running
 	 * Jelly Bean or not
 	 * @return
 	 */
 	public static boolean isJellyBean(){
 		return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN);
 	}
 	
 	/**
 	 * Check to see if this device is running
 	 * ICS or greater
 	 * @return
 	 */
 	public static boolean isICS(){
 		return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH);
 	}
 	
 	/**
 	 * Get the MIME type of a file
 	 * @param url
 	 * @return
 	 */
 	public static String getMimeType(String url)
 	{
 	    String type = null;
 	    String extension = MimeTypeMap.getFileExtensionFromUrl(url);
 	    if (extension != null) {
 	        MimeTypeMap mime = MimeTypeMap.getSingleton();
 	        type = mime.getMimeTypeFromExtension(extension);
 	    }
 	    return type;
 	}
 	
 	/**
 	 * Parse a file's mime type from the file extension
 	 * 
 	 * @param ext
 	 * @return
 	 */
 	public static String parseMimeType(String filename){
 		return URLConnection.guessContentTypeFromName(filename);
 	}
 	
 	/**
 	 * Condense a file size in bytes into a more proper form
 	 * of kilobytes, megabytes, gigabytes
 	 * @param bytes		the size in bytes
 	 * @return			the condensed string
 	 */
 	public static String condenseFileSize(long bytes){
 		
 		// Kilobyte Check
 		long kilo = bytes / 1024L;
 		long mega = kilo / 1024L;
 		long giga = mega / 1024L;
 		
 		// Determine which value to send back
 		if(giga > 1) 
 			return giga + " Gb";
 		else if(mega > 1)
 			return mega + " Mb";
 		else if(kilo > 1)
 			return kilo + " Kb";
 		else 
 			return bytes + " bytes";
 				
 	}
 	
 	/**
 	 * Condense a file size in bytes into a more proper form
 	 * of kilobytes, megabytes, gigabytes
 	 * @param bytes		the size in bytes
 	 * @return			the condensed string
 	 */
 	public static String condenseFileSize(long bytes, String precision) throws IllegalFormatException{
 		
 		// Kilobyte Check
 		float kilo = bytes / 1024f;
 		float mega = kilo / 1024f;
 		float giga = mega / 1024f;
 		
 		// Determine which value to send back
 		if(giga > 1) 
 			return String.format(precision + " GB", giga);
 		else if(mega > 1)
 			return String.format(precision + " MB", mega);
 		else if(kilo > 1)
 			return String.format(precision + " KB", kilo);
 		else 
 			return bytes + " b";
 				
 	}
 	
 	/**
 	 * Start the email with pre-filled information
 	 */
 	public static void sendSupportEmail(Context ctx, String subject){
 		
 		// Create Email Intent
 		Intent email = new Intent(Intent.ACTION_SEND);
 		email.setType("plain/text");
 		email.putExtra(Intent.EXTRA_EMAIL, new String[] {"support@52apps.com"});
 		email.putExtra(Intent.EXTRA_SUBJECT, subject);
 		
 		// Start Intent
 		ctx.startActivity(Intent.createChooser(email, "Send Support Email"));		
 	}
 	
 	/**
 	 * Launch the Play Store details of this app
 	 * @param ctx
 	 */
 	public static void openAppDetails(Context ctx){
 		Intent i = new Intent(Intent.ACTION_VIEW);
 		i.setData(Uri.parse("market://details?id=" + ctx.getPackageName()));
 		ctx.startActivity(i);
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	/**
 	 * Easy method for Logging 'DEBUG' messages
 	 * @param msg		the message
 	 * @param tag		the identifing tag
 	 */
 	public static void log(String tag, String msg){
 		log(Log.DEBUG, tag, msg);
 	}
 	
 	/**
 	 * Easy method for Logging 'INFO' messages
 	 * @param tag		the message
 	 * @param msg		the identifing tag
 	 */
 	public static void logi(String tag, String msg){
 		log(Log.INFO, tag, msg);
 	}
 	
 	/**
 	 * Easy method for Logging 'ERROR' messages
 	 * @param tag		the message
 	 * @param msg		the identifing tag
 	 */
 	public static void loge(String tag, String msg){
 		log(Log.ERROR, tag, msg);
 	}
 	
 	/**
 	 * Easy method for Logging 'WARN' messages
 	 * @param tag		the message
 	 * @param msg		the identifing tag
 	 */
 	public static void logw(String tag, String msg){
 		log(Log.WARN, tag, msg);
 	}
 
     /**
      * Easy method for Loggin 'WTF' messages
      * @param tag
      * @param msg
      */
     public static void logwtf(String tag, String msg){
         if(VERBOSE && DEBUG)
             Log.wtf(tag, msg);
     }
 	
 	/**
 	 * Easy Wrapper to printing to android Log
 	 * @param priority		the message priority
 	 * @param msg			the message
 	 * @param tag			the identifing tag
 	 */
 	public static void log(int priority, String tag, String msg){
 		if(VERBOSE && DEBUG)
 			Log.println(priority, tag, msg);
 	}
 
 }
