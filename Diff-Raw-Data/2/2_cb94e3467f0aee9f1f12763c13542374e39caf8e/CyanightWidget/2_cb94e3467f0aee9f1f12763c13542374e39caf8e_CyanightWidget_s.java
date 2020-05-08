 package com.mridang.cyanight.services;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.joda.time.DateTime;
 import org.joda.time.Days;
 
 import android.content.ComponentName;
 import android.content.Intent;
 import android.util.Log;
 
 import com.bugsense.trace.BugSenseHandler;
 import com.google.android.apps.dashclock.api.DashClockExtension;
 import com.google.android.apps.dashclock.api.ExtensionData;
 import com.mridang.cyanight.R;
 import com.mridang.cyanight.functions.HelperFunctions;
 
 /*
  * This class is the main class that provides the widget
  */
 public class CyanightWidget extends DashClockExtension {
 
 	/*
 	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onCreate()
 	 */
 	public void onCreate() {
 
 		super.onCreate();
 		Log.d("CyanightWidget", "Created");
 		BugSenseHandler.initAndStartSession(this, "1c73a4f8");
 
 	}
 
 	/*
 	 * @see
 	 * com.google.android.apps.dashclock.api.DashClockExtension#onUpdateData
 	 * (int)
 	 */
 	@Override
 	protected void onUpdateData(int arg0) {
 
 		setUpdateWhenScreenOn(true);
 
 		Log.d("CyanightWidget", "Checking nightly commits");
 		ExtensionData edtInformation = new ExtensionData();
 		edtInformation.visible(false);
 
 		try {
 
 			Log.d("CyanightWidget", "Checking if it is a nightly build");
 			if (HelperFunctions.getType().equalsIgnoreCase("NIGHTLY")) {
 
 				Log.d("CyanightWidget", "Calculating the age");
 				try {
 
 					Date datBuild = new SimpleDateFormat("yyyyMMdd").parse(HelperFunctions.getDate());
 					Date datToday = new Date();
 					Integer intDays = Days.daysBetween(new DateTime(datBuild), new DateTime(datToday)).getDays();
 
 					edtInformation.visible(true);
					edtInformation.status(String.format(getString(R.string.changes), intDays));
 					edtInformation.expandedBody(String.format(getString(R.string.current), HelperFunctions.getBuildString()));
 
 					ComponentName comp = new ComponentName("com.cyanogenmod.updater", "com.cyanogenmod.updater.UpdatesSettings");
 					Intent ittUpdater = new Intent().setComponent(comp);
 					edtInformation.clickIntent(ittUpdater);
 
 				} catch (Exception e) {
 					Log.e("CyanightWidget", "Unable to calculate age", e);
 				}
 
 			} else {
 				Log.d("CyanightWidget", "Not a nightly build");
 			}
 
 		} catch (Exception e) {
 			Log.e("CyanightWidget", "Encountered an error", e);
 			BugSenseHandler.sendException(e);
 		}
 
 		edtInformation.icon(R.drawable.ic_dashclock);
 		publishUpdate(edtInformation);
 		Log.d("CyanightWidget", "Done");
 
 	}
 
 	/*
 	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onDestroy()
 	 */
 	public void onDestroy() {
 
 		super.onDestroy();
 		Log.d("CyanightWidget", "Destroyed");
 		BugSenseHandler.closeSession(this);
 
 	}
 
 }
