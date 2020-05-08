 package uk.co.wishf.worlddashclock;
 
 import java.util.Calendar;
 import java.util.Locale;
 import java.util.TimeZone;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 import com.google.android.apps.dashclock.api.DashClockExtension;
 import com.google.android.apps.dashclock.api.ExtensionData;
 
 public class WorldClockExtension extends DashClockExtension {
 	
 	static final String TZ_PREF = "timezone";
 	
 	Timer timer;
 	
 	String userTz;
 	
 	protected synchronized String getTz() {
 		return userTz;
 	}
 	
 	private synchronized void setTz(String tz) {
 		userTz = tz;
 	}
 	
 	@Override
 	protected void onInitialize(boolean isReconnect) {
 		this.setUpdateWhenScreenOn(true);
 		
 		if(timer == null) {
 			timer = new Timer("World Clock Dashclock Extension Updater");
 			Calendar now = Calendar.getInstance();
 			long dly = (1000 - now.get(Calendar.MILLISECOND)) + (1000 * (60 - now.get(Calendar.SECOND)));
 			timer.scheduleAtFixedRate(new ClockUpdater(this), dly, 60*1000);
 		}
 		
 		super.onInitialize(isReconnect);
 	}
 	
 	@Override
 	protected void onUpdateData(int reason) {
 		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
 		this.setTz(sp.getString(TZ_PREF, "Australia/Sydney"));
 		
		this.publishUpdate(generateExtensionData(this.getTz()));
 	}
 	
	private static ExtensionData generateExtensionData(String tzName) {
 		final TimeZone tz = TimeZone.getTimeZone(tzName);
 		final Calendar tzTime = Calendar.getInstance(tz);
 		
 		String[] nameComponents = tzName.replace('_', ' ').split("/");
 		String placeName = String.format("%s, %s", nameComponents[1], nameComponents[0]);
 		
 		StringBuilder status = new StringBuilder();
 		status.append(tzTime.get(Calendar.HOUR_OF_DAY));
 		status.append(':');
 		status.append(tzTime.get(Calendar.MINUTE));
 		
 		StringBuilder title = new StringBuilder();
 		title.append(tzTime.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()));
 		title.append(", ");
 		title.append(tzTime.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()));
 		title.append(' ');
 		title.append(tzTime.get(Calendar.DAY_OF_MONTH));
 		title.append(" at ");
 		title.append(status.toString());
 
 		
		return new ExtensionData()
				   .visible(true)
				   .icon(R.drawable.clock)
				   .status(status.toString())
				   .expandedTitle(title.toString())
				   .expandedBody(placeName);
 	}
 	
 	class ClockUpdater extends TimerTask {
 		
 		WorldClockExtension parent;
 		
 		public ClockUpdater(WorldClockExtension instance) {
 			parent = instance;
 		}
 		
 		@Override
 		public void run() {
			parent.publishUpdate(generateExtensionData(parent.getTz()));
 		}
 		
 	}
 
 }
