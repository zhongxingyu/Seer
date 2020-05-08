 package com.mridang.uptime;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import org.ocpsoft.prettytime.Duration;
 import org.ocpsoft.prettytime.PrettyTime;
 import org.ocpsoft.prettytime.units.JustNow;
 
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
 
 		setUpdateWhenScreenOn(true);
 
 		Log.d("UptimeWidget", "Calculating the phone's uptime");
 		ExtensionData edtInformation = new ExtensionData();
 		edtInformation.visible(false);
 
 		try {
 
 			PrettyTime pd = new PrettyTime(new Date(SystemClock.uptimeMillis()));
 
 			List<Duration> lstDurations = pd.calculatePreciseDuration(new Date(0));			
 			for (Duration d : lstDurations) {
 			    if (d.getUnit() instanceof JustNow) {
 			    	lstDurations.remove(d);
 			    }
 			}

			if (!lstDurations.isEmpty()) {

				edtInformation.expandedBody(String.format(getString(R.string.message), pd.format(lstDurations)));
 			
				SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault());
				Date datReboot = new Date(Calendar.getInstance().getTimeInMillis() - SystemClock.uptimeMillis());
				edtInformation.status(String.format(getString(R.string.status), sdf.format(datReboot)));
 
				edtInformation.visible(true);
				
			}
 			
 		} catch (Exception e) {
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
