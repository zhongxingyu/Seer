 package com.mridang.weeknum;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.Random;
 
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.pm.ResolveInfo;
 import android.net.Uri;
 import android.util.Log;
 
 import com.bugsense.trace.BugSenseHandler;
 import com.google.android.apps.dashclock.api.DashClockExtension;
 import com.google.android.apps.dashclock.api.ExtensionData;
 
 /*
  * This class is the main class that provides the widget
  */
 public class WeeknumWidget extends DashClockExtension {
 
 	/*
 	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onCreate()
 	 */
 	public void onCreate() {
 
 		super.onCreate();
 		Log.d("WeeknumWidget", "Created");
 		BugSenseHandler.initAndStartSession(this, getString(R.string.bugsense));
 
 	}
 
 	/*
 	 * @see
 	 * com.google.android.apps.dashclock.api.DashClockExtension#onUpdateData
 	 * (int)
 	 */
 	@Override
 	protected void onUpdateData(int arg0) {
 
 		Log.d("WeeknumWidget", "Calculating the current week number");
 		ExtensionData edtInformation = new ExtensionData();
 		setUpdateWhenScreenOn(false);
 
 		try {
 
 			Calendar calCalendar = new GregorianCalendar();
 			SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM");
 
 			Calendar calWeek = Calendar.getInstance();
 			calWeek.clear();
 			calWeek.set(Calendar.WEEK_OF_YEAR, calCalendar.get(Calendar.WEEK_OF_YEAR));
 			calWeek.set(Calendar.YEAR, calCalendar.get(Calendar.YEAR));
 
 			String strStart = dateFormat.format(calWeek.getTime());
 
 			calWeek.add(Calendar.DAY_OF_WEEK, 6);
 			String strEnd = dateFormat.format(calWeek.getTime());
 
 			edtInformation.expandedTitle(String.format(getString(R.string.status), calCalendar.get(Calendar.WEEK_OF_YEAR)));
 			edtInformation.status(Integer.toString(calCalendar.get(Calendar.WEEK_OF_YEAR)));
 			edtInformation.expandedBody(String.format(getString(R.string.message), strStart, strEnd));
			edtInformation.visible(true);
 
 			if (new Random().nextInt(5) == 0) {
 
 				PackageManager mgrPackages = getApplicationContext().getPackageManager();
 
 				try {
 
 					mgrPackages.getPackageInfo("com.mridang.donate", PackageManager.GET_META_DATA);
 
 				} catch (NameNotFoundException e) {
 
 					Integer intExtensions = 0;
 				    Intent ittFilter = new Intent("com.google.android.apps.dashclock.Extension");
 				    String strPackage;
 
 				    for (ResolveInfo info : mgrPackages.queryIntentServices(ittFilter, 0)) {
 
 				    	strPackage = info.serviceInfo.applicationInfo.packageName;
 						intExtensions = intExtensions + (strPackage.startsWith("com.mridang.") ? 1 : 0); 
 
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
 			edtInformation.visible(false);
 			Log.e("WeeknumWidget", "Encountered an error", e);
 			BugSenseHandler.sendException(e);
 		}
 
 		edtInformation.icon(R.drawable.ic_dashclock);
 		publishUpdate(edtInformation);
 		Log.d("WeeknumWidget", "Done");
 
 	}
 
 	/*
 	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onDestroy()
 	 */
 	public void onDestroy() {
 
 		super.onDestroy();
 		Log.d("WeeknumWidget", "Destroyed");
 		BugSenseHandler.closeSession(this);
 
 	}
 
 }
