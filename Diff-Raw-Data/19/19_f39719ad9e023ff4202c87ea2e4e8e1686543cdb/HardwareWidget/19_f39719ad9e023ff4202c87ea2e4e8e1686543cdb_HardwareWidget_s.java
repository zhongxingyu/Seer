 package com.mridang.hardware;
 
 import java.io.RandomAccessFile;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Random;
 
 import android.app.ActivityManager;
 import android.app.ActivityManager.MemoryInfo;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.net.Uri;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 import com.bugsense.trace.BugSenseHandler;
 import com.google.android.apps.dashclock.api.DashClockExtension;
 import com.google.android.apps.dashclock.api.ExtensionData;
 
 /*
  * This class is the main class that provides the widget
  */
 public class HardwareWidget extends DashClockExtension {
 
 	/* This is the instance of the thread that keeps track of connected clients */
 	private Thread thrPeriodicTicker;
 
 	/*
 	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onCreate()
 	 */
 	public void onCreate() {
 
 		super.onCreate();
 		Log.d("HardwareWidget", "Created");
 		BugSenseHandler.initAndStartSession(this, "667d3440");
 
 	}
 
 	/*
 	 * @see
 	 * com.google.android.apps.dashclock.api.DashClockExtension#onUpdateData
 	 * (int)
 	 */
 	@Override
	protected void onUpdateData(int arg0) {
 
 		Log.d("HardwareWidget", "Fetching processor and memory utilisation information");
 		final ExtensionData edtInformation = new ExtensionData();
		edtInformation.visible(true);
 
 		try {
 
 			Log.d("HardwareWidget", "Checking if the periodic ticker is enabled");
 			if (thrPeriodicTicker == null) {
 
 				Log.d("HardwareWidget", "Starting a new periodic ticker to check proceesor and memory utilization");
 				thrPeriodicTicker = new Thread() {
 
 					public void run () {
 
 						Integer intPreviousTotal = 0;
 						Integer intPreviousIdle = 0;
 
 						for (;;) {
 
 							try {
 
 								RandomAccessFile rafProcessor = new RandomAccessFile("/proc/stat", "r");
 
 								edtInformation.icon(R.drawable.ic_dashclock);
 								edtInformation.visible(true);
								
 								List<String> lstColumns = Arrays.asList(rafProcessor.readLine().split(" "));
 								//lstColumns.remove(0);
 
 								Integer intCurrentIdle = Integer.parseInt(lstColumns.get(5));
 								Integer intCurrentTotal = 0;
								
 								MemoryInfo memInformation = new MemoryInfo(); 
 								((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE)).getMemoryInfo(memInformation);
 
 								for (String strStatistic : lstColumns.subList(2, lstColumns.size())) {
 
 									intCurrentTotal = intCurrentTotal + Integer.parseInt(strStatistic);
 
 								}
 
 								Integer intDifferenceIdle = intCurrentIdle - intPreviousIdle;
 								Integer intDifferenceTotal = intCurrentTotal - intPreviousTotal;
 
 								intPreviousIdle = intCurrentIdle;
 								intPreviousTotal = intCurrentTotal;
								
 								edtInformation.expandedTitle(getString(R.string.processor, 100 * (intDifferenceTotal - intDifferenceIdle) / intDifferenceTotal));
 								edtInformation.expandedBody(getString(R.string.memory, memInformation.availMem / 1048576L, memInformation.totalMem / 1048576L));
 
 								publishUpdate(edtInformation);
 								rafProcessor.close();
 
 								Thread.sleep(Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("interval", "5")) * 1000);
 
 							} catch (InterruptedException e) {
 								Log.d("HardwareWidget", "Stopping the periodic checker.");
 								return;
 							} catch (Exception e) {
 								Log.e("HardwareWidget", "Encountered an error", e);
 								BugSenseHandler.sendException(e);
 							}
 
 						}
 
 					}
 
 				};
 
 				thrPeriodicTicker.start();
 
 			}
 
 			if (new Random().nextInt(5) == 0) {
 
 				PackageManager mgrPackages = getApplicationContext().getPackageManager();
 
 				try {
 
 					mgrPackages.getPackageInfo("com.mridang.donate", PackageManager.GET_META_DATA);
 
 				} catch (NameNotFoundException e) {
 
 					Integer intExtensions = 0;
 
 					for (PackageInfo pkgPackage : mgrPackages.getInstalledPackages(0)) {
 
 						intExtensions = intExtensions + (pkgPackage.applicationInfo.packageName.startsWith("com.mridang.") ? 1 : 0); 
 
 					}
 
 					if (intExtensions > 1) {
 
 						edtInformation.visible(true);
 						edtInformation.clickIntent(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("market://details?id=com.mridang.donate")));
 						edtInformation.expandedTitle("Please consider a one time purchase to unlock.");
 						edtInformation.expandedBody("Thank you for using " + intExtensions + " extensions of mine. Click this to make a one-time purchase or use just one extension to make this disappear.");
 						setUpdateWhenScreenOn(true);
 
 					}
 
 				}
 
 			} else {
				setUpdateWhenScreenOn(false);
 			}
 
 		} catch (Exception e) {
 			Log.e("HardwareWidget", "Encountered an error", e);
 			BugSenseHandler.sendException(e);
 		}
 
 		edtInformation.icon(R.drawable.ic_dashclock);
 		publishUpdate(edtInformation);
 		Log.d("HardwareWidget", "Done");
 
 	}
 
 	/*
 	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onDestroy()
 	 */
 	public void onDestroy() {
 
 		super.onDestroy();
 		Log.d("HardwareWidget", "Destroyed");
 		BugSenseHandler.closeSession(this);
 
 	}
 
 }
