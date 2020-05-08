 package com.mridang.processes;
 
 import java.util.Random;
 
 import android.app.ActivityManager;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.net.Uri;
import android.provider.Settings;
 import android.util.Log;
 
 import com.bugsense.trace.BugSenseHandler;
 import com.google.android.apps.dashclock.api.DashClockExtension;
 import com.google.android.apps.dashclock.api.ExtensionData;
 
 /*
  * This class is the main class that provides the widget
  */
 public class ProcessesWidget extends DashClockExtension {
 
 	/*
 	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onCreate()
 	 */
 	public void onCreate() {
 
 		super.onCreate();
 		Log.d("ProcessesWidget", "Created");
 		BugSenseHandler.initAndStartSession(this, "15988b02");
 
 	}
 
 	/*
 	 * @see
 	 * com.google.android.apps.dashclock.api.DashClockExtension#onUpdateData
 	 * (int)
 	 */
 	@Override
 	protected void onUpdateData(int arg0) {
 
 		Log.d("ProcessesWidget", "Getting the status of the processes");
 		ExtensionData edtInformation = new ExtensionData();
		setUpdateWhenScreenOn(true);
 
 		try {
 
 			ActivityManager mgrActivity = (ActivityManager) this.getSystemService( ACTIVITY_SERVICE );
 
			edtInformation.clickIntent(new Intent(Intent.ACTION_VIEW).setClassName("com.android.settings", "com.android.settings.RunningServices"));
			
 			Log.d("ProcessesWidget", "Calculating the number of recent activities");
 			Integer intActivities =  mgrActivity.getRecentTasks(Integer.MAX_VALUE, ActivityManager.RECENT_IGNORE_UNAVAILABLE).size() - 1;
 			Log.d("ProcessesWidget", intActivities + " recent activities");
 
 			edtInformation.status(Integer.toString(intActivities));
 			if (intActivities > 0) 
 				edtInformation.expandedTitle(getResources().getQuantityString(R.plurals.activity, intActivities, intActivities));
 			else
 				edtInformation.expandedTitle(getString(R.string.no_activities));
 
 			Log.d("ProcessesWidget", "Calculating the number of running applications");
 			Integer intApplications = mgrActivity.getRunningAppProcesses().size();
 			Log.d("ProcessesWidget", intApplications + " running applications");
 
 			if (intApplications > 0) 
 				edtInformation.expandedBody(getResources().getQuantityString(R.plurals.process, intApplications, intApplications));
 			else
 				edtInformation.expandedTitle(getString(R.string.no_processes));			
 
			edtInformation.visible(true);

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
 				setUpdateWhenScreenOn(true);
 			}
 
 		} catch (Exception e) {
			edtInformation.visible(false);
 			Log.e("ProcessesWidget", "Encountered an error", e);
 			BugSenseHandler.sendException(e);
 		}
 
 		edtInformation.icon(R.drawable.ic_dashclock);
 		publishUpdate(edtInformation);
 		Log.d("ProcessesWidget", "Done");
 
 	}
 
 	/*
 	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onDestroy()
 	 */
 	public void onDestroy() {
 
 		super.onDestroy();
 		Log.d("ProcessesWidget", "Destroyed");
 		BugSenseHandler.closeSession(this);
 
 	}
 
 }
