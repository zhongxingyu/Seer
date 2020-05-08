 package com.mridang.weeknum;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
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
 		BugSenseHandler.initAndStartSession(this, "ff803a7a");
 
 	}
 
 	/*
 	 * @see
 	 * com.google.android.apps.dashclock.api.DashClockExtension#onUpdateData
 	 * (int)
 	 */
 	@Override
 	protected void onUpdateData(int arg0) {
 
 		setUpdateWhenScreenOn(true);
 
 		Log.d("WeeknumWidget", "Calculating the current week number");
 		ExtensionData edtInformation = new ExtensionData();
 		edtInformation.visible(true);
 
 		try {
 
 			Calendar calCalendar = new GregorianCalendar();
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM");
 			
 			Calendar calWeek = Calendar.getInstance();
 			calWeek.clear();
 			calWeek.set(Calendar.WEEK_OF_YEAR, calCalendar.get(Calendar.WEEK_OF_YEAR));
 			calWeek.set(Calendar.YEAR, calCalendar.get(Calendar.YEAR));
 
 			String strStart = dateFormat.format(calWeek.getTime());
 			
 			calWeek.add(Calendar.DAY_OF_WEEK, 6);
 			String strEnd = dateFormat.format(calWeek.getTime());
 			
 			edtInformation.status(String.format(getString(R.string.status), calCalendar.get(Calendar.WEEK_OF_YEAR)));
 			edtInformation.expandedBody(String.format(getString(R.string.message), strStart, strEnd));
 
 		} catch (Exception e) {
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
