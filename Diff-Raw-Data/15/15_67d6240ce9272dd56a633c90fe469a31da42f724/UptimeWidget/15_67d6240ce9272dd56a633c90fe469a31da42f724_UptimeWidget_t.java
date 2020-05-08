 package com.mridang.uptime;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import java.util.Random;
 
 import org.ocpsoft.prettytime.Duration;
 import org.ocpsoft.prettytime.PrettyTime;
 import org.ocpsoft.prettytime.units.JustNow;
 
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.net.Uri;
 import android.os.SystemClock;
 import android.util.Log;
 
 import com.bugsense.trace.BugSenseHandler;
 import com.google.android.apps.dashclock.api.DashClockExtension;
 import com.google.android.apps.dashclock.api.ExtensionData;
 
 /*
  * This class is the main class that provides the widget
  */
 public class UptimeWidget extends DashClockExtension {
 
 	/*
 	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onCreate()
 	 */
 	public void onCreate() {
 
 		super.onCreate();
 		Log.d("UptimeWidget", "Created");
 		BugSenseHandler.initAndStartSession(this, "727fcaa3");
 
 	}
 
 	/*
 	 * @see
 	 * com.google.android.apps.dashclock.api.DashClockExtension#onUpdateData
 	 * (int)
 	 */
 	@Override
 	protected void onUpdateData(int arg0) {
 
 		Log.d("UptimeWidget", "Calculating the phone's uptime");
 		ExtensionData edtInformation = new ExtensionData();
		setUpdateWhenScreenOn(true);
 
 		try {
 
 			PrettyTime pd = new PrettyTime(new Date(SystemClock.elapsedRealtime()));
 
 			List<Duration> lstDurations = pd.calculatePreciseDuration(new Date(0));			
 			for (Duration d : lstDurations) {

 				if (d.getUnit() instanceof JustNow) {

 					lstDurations.remove(d);

 				}

 			}
 
 			if (!lstDurations.isEmpty()) {
 
 				edtInformation.expandedBody(String.format(getString(R.string.message), pd.format(lstDurations)));
 
 				SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault());
 				Date datReboot = new Date(Calendar.getInstance().getTimeInMillis() - SystemClock.elapsedRealtime());
 				edtInformation.status(String.format(getString(R.string.status), sdf.format(datReboot)));
 
 				edtInformation.visible(true);
 
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
 				setUpdateWhenScreenOn(true);
 			}
 
 		} catch (Exception e) {
			edtInformation.visible(false);
 			Log.e("UptimeWidget", "Encountered an error", e);
 			BugSenseHandler.sendException(e);
 		}
 
 		edtInformation.icon(R.drawable.ic_dashclock);
 		publishUpdate(edtInformation);
 		Log.d("UptimeWidget", "Done");
 
 	}
 
 	/*
 	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onDestroy()
 	 */
 	public void onDestroy() {
 
 		super.onDestroy();
 		Log.d("UptimeWidget", "Destroyed");
 		BugSenseHandler.closeSession(this);
 
 	}
 
 }
