 package com.mridang.messstats;
 
 import java.util.Calendar;
 import java.util.Random;
 
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.database.Cursor;
 import android.net.Uri;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 import com.bugsense.trace.BugSenseHandler;
 import com.google.android.apps.dashclock.api.DashClockExtension;
 import com.google.android.apps.dashclock.api.ExtensionData;
 
 /*
  * This class is the main class that provides the widget
  */
 public class MessstatsWidget extends DashClockExtension {
 
 	/*
 	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onInitialize(boolean)
 	 */
 	@Override
 	protected void onInitialize(boolean isReconnect) {
 
 		super.onInitialize(isReconnect);
 
 		if (!isReconnect) {
 
			addWatchContentUris(new String[]{"content://sms/", "content://mms/"});
 
 		}
 
 	}
 
 	/*
 	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onCreate()
 	 */
 	public void onCreate() {
 
 		super.onCreate();
 		Log.d("MessstatsWidget", "Created");
 		BugSenseHandler.initAndStartSession(this, "bc2d4ec1");
 
 	}
 
 	/*
 	 * @see
 	 * com.google.android.apps.dashclock.api.DashClockExtension#onUpdateData
 	 * (int)
 	 */
 	@Override
	protected void onUpdateData(int intReason) {
 
 		Log.d("MessstatsWidget", "Calculating message statistics");
 		ExtensionData edtInformation = new ExtensionData();
		setUpdateWhenScreenOn(false);
 
 		try {
 
 			Log.d("MessstatsWidget", "Checking period that user has selected");
 			Calendar calCalendar = Calendar.getInstance();
 			calCalendar.set(Calendar.MINUTE, 0);
 			calCalendar.set(Calendar.HOUR, 0);
 			calCalendar.set(Calendar.SECOND, 0);
 			calCalendar.set(Calendar.MILLISECOND, 0);
 			calCalendar.set(Calendar.HOUR_OF_DAY, 0);
 
 			switch (Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("period", "4"))) {
 
 			case 0: //Day
 				Log.d("MessstatsWidget", "Fetch messages for the day");
 				calCalendar.set(Calendar.HOUR_OF_DAY, 0);
 				break;
 
 			case 1: //Week
 				Log.d("MessstatsWidget", "Fetch messages for the week");
 				calCalendar.set(Calendar.DAY_OF_WEEK, calCalendar.getFirstDayOfWeek());
 				break;
 
 			case 2: //Month
 				Log.d("MessstatsWidget", "Fetch messages for the month");
 				calCalendar.set(Calendar.DAY_OF_MONTH, 1);
 				break;
 
 			case 3: //Year
 				Log.d("MessstatsWidget", "Fetch messages for the year");
 				calCalendar.set(Calendar.DAY_OF_YEAR, 1);
 				break;
 
 			default:
 				Log.d("MessstatsWidget", "Fetch all messages");
 				calCalendar.clear(); 
 				break;
 
 			}
 
 			Log.d("MessstatsWidget", "Querying the database to get the messages since " + calCalendar.getTime());
 			String strClause = "date >= ?";
 			String[] strValues = {String.valueOf(calCalendar.getTimeInMillis())};
 
 			Log.d("MessstatsWidget", "Calculating SMS statistics");
 			Cursor curSmses = getContentResolver().query(Uri.parse("content://sms/"), null, strClause, strValues, null);
 
 			Integer intSentSms = 0;
 			Integer intRecdSms = 0;
 
 			while (curSmses != null && curSmses.moveToNext()) {
 
 				switch (curSmses.getInt(curSmses.getColumnIndex("type"))) {
 
 				case 1:
 					intRecdSms = intRecdSms + 1;
 					break;
 
 				case 2:
 					intSentSms = intSentSms + 1;
 					break;
 
 				}
 
 			}
 
 			Log.d("MessstatsWidget", "Send SMSes: " + intSentSms);
 			Log.d("MessstatsWidget", "Received SMSs: " + intRecdSms);
 			Log.d("MessstatsWidget", "Total SMSes: " + (intSentSms + intRecdSms));
 
 			Log.d("MessstatsWidget", "Calculating MMS statistics");
 			Cursor curMmses = getContentResolver().query(Uri.parse("content://mms/"), null, strClause, strValues, null);
 
 			Integer intSentMms = 0;
 			Integer intRecdMms = 0;
 
 			while (curMmses != null && curMmses.moveToNext()) {
 
 				switch (curMmses.getInt(curMmses.getColumnIndex("msg_box"))) {
 
 				case 2:
 					intRecdMms = intRecdMms + 1;
 					break;
 
 				case 1:
 					intSentMms = intSentMms + 1;
 					break;
 
 				}
 
 			}
 
 			Log.d("MessstatsWidget", "Send MMSes: " + intSentMms);
 			Log.d("MessstatsWidget", "Received MMSes: " + intRecdMms);
 			Log.d("MessstatsWidget", "Total MMSes: " + (intSentMms + intRecdMms));
 
 			edtInformation
 			.expandedBody((edtInformation.expandedBody() == null ? ""
 					: edtInformation.expandedBody() + "\n")
 					+ getResources().getQuantityString(R.plurals.sent,
 							intSentSms + intSentMms,
 							intSentSms + intSentMms));
 
 			edtInformation.status(String.format(getString(R.string.messages),
 					intSentSms + intRecdSms + intSentMms + intRecdMms));
 
 			edtInformation
 			.expandedBody((edtInformation.expandedBody() == null ? ""
 					: edtInformation.expandedBody() + "\n")
 					+ getResources().getQuantityString(R.plurals.received,
 							intRecdSms + intRecdMms,
 							intRecdSms + intRecdMms));
 
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
 				setUpdateWhenScreenOn(false);
 			}
 
 		} catch (Exception e) {
			edtInformation.visible(false);
 			Log.e("MessstatsWidget", "Encountered an error", e);
 			BugSenseHandler.sendException(e);
 		}	
 
 		edtInformation.icon(R.drawable.ic_dashclock);
 		publishUpdate(edtInformation);
 		Log.d("MessstatsWidget", "Done");
 
 	}
 
 	/*
 	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onDestroy()
 	 */
 	public void onDestroy() {
 
 		super.onDestroy();
 		Log.d("MessstatsWidget", "Destroyed");
 		BugSenseHandler.closeSession(this);
 
 	}
 
 }
