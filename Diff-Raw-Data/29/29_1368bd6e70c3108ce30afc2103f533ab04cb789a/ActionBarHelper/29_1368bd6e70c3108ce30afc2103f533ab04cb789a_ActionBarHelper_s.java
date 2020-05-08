 /*   Copyright 2012 Mario Bhmer
  *
  *   Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported (CC BY-NC-SA 3.0) 
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://creativecommons.org/licenses/by-nc-sa/3.0/
  */
 package com.blogspot.marioboehmer.thingibrowse;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.os.Build;
 import android.util.Log;
 
 /**
  * Handles the action item specific logic and creates corresponding Intents.
  * 
  * @author Mario Bhmer
  */
 public class ActionBarHelper {
 
 	private static final String TAG = ActionBarHelper.class.getSimpleName();
 	private static ActionBarHelper INSTANCE = null;
 
 	private ActionBarHelper() {
 
 	}
 
 	public static ActionBarHelper getInstance() {
 		if (INSTANCE == null) {
 			INSTANCE = new ActionBarHelper();
 		}
 		return INSTANCE;
 	}
 
 	public Intent getHomeIntent(Context context) {
 		Intent homeIntent = new Intent(context, ThingResultListActivity.class);
 		homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		return homeIntent;
 	}
 
 	public Intent getInfoIntent(Context context) {
 		Intent infoTextIntent = new Intent(context.getApplicationContext(),
 				InfoActivity.class);
 		return infoTextIntent;
 	}
 
 	public Intent getFeedbackIntent(Context context) {
 		Intent feedbackMailIntent = new Intent(Intent.ACTION_SEND);
 		feedbackMailIntent.setType("plain/text");
 		feedbackMailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { context
 				.getString(R.string.feedback_mail_address) });
 		feedbackMailIntent.putExtra(Intent.EXTRA_SUBJECT,
 				context.getString(R.string.feedback));
 		feedbackMailIntent.putExtra(Intent.EXTRA_TEXT, getSystemInfo(context));
 		return feedbackMailIntent;
 	}
 
 	private String getSystemInfo(Context context) {
 		StringBuilder builder = new StringBuilder();
 		builder.append("\n\n");
 		builder.append("---------- System Info ----------");
 		builder.append("\n");
 		builder.append("OS Version: ");
 		builder.append(Build.VERSION.RELEASE);
 		builder.append("\n");
 		builder.append("OS Api Level: ");
 		builder.append(Build.VERSION.SDK_INT);
 		builder.append("\n");
 		builder.append("Manufacturer: ");
 		builder.append(Build.MANUFACTURER);
 		builder.append("\n");
 		builder.append("Model: ");
 		builder.append(Build.MODEL);
 		builder.append("\n");
 		try {
 			PackageInfo packageInfo = context.getPackageManager()
 					.getPackageInfo(context.getPackageName(),
 							PackageManager.GET_ACTIVITIES);
 			builder.append("App VersionCode: ");
 			builder.append(packageInfo.versionCode);
 			builder.append("\n");
 			builder.append("App VersionName: ");
 			builder.append(packageInfo.versionName);
 			builder.append("\n");
 		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
 		}
 		builder.append("---------- System Info ----------");
 		builder.append("\n\n");
 		return builder.toString();
 	}
 
 }
