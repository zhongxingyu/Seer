 package omalleyland.my.simple.tools;
 
 import android.app.PendingIntent;
 import android.app.Service;
 import android.appwidget.AppWidgetManager;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.os.IBinder;
 import android.widget.RemoteViews;
 
 public class UpdateService extends Service {
 
 	public static final String PREFS_NAME = "MyPrefsFile";
 	public static final String ALARM_ENABLED = "ALARM_ENABLED";
 	public static final String ALARM_HOUR = "ALARM_HOUR";
 	public static final String ALARM_MINUTE = "ALARM_MIN";
 	public static final int UPDATE = 1;
 
 	boolean AlarmEnabled;
     int AlarmHour;
 	int AlarmMin;
 	String HourStr;
 	String MinuteStr;
 	String AMPMStr;
 	String WidgetString;
 
 	
 //	@Override
 //	public void onReceive(Context context, Intent intent) {
 //		Log.e("SimpleAlarmClock", "onReceive...." + intent.getAction());
 //		if (REFRESH.equals(intent.getAction())) {
 //			Log.e("SimpleAlarmClock", "onReceive - starting service....");
 //	        Intent i = new Intent(context, UpdateService.class);
 //			context.startService(i);
 //		}
 //		else if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(intent.getAction())) {
 //			Log.e("SimpleAlarmClock", "onReceive - starting service....");
 //	        Intent i = new Intent(context, UpdateService.class);
 //			context.startService(i);
 //		}
 //		else {
 //			Log.e("SimpleAlarmClock", "onReceive - super.onReceive....");
 //			super.onReceive(context, intent);
 //		}
 //	}
 	
 	@Override
 	public void onCreate() {
 		super.onCreate();
 	}
 	
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 	}
 	
 	@Override
 	public void onStart(Intent intent, int startId) {
         super.onStart(intent, startId);		
         
         RemoteViews remoteView = UpdateWidgetComponents(this);
         pushUpdate(remoteView);
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		// TODO Auto-generated method stub
 		
 		return null;
 	}
 	
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		RemoteViews remoteView = UpdateWidgetComponents(this);
 		pushUpdate(remoteView);
 	}
 	
 	public void pushUpdate(RemoteViews updateViews) {
 
 		ComponentName thisWidget = new ComponentName(this.getPackageName(), NextAlarm.class.getName());
 		AppWidgetManager manager = AppWidgetManager.getInstance(this);
 		manager.updateAppWidget(thisWidget, UpdateWidgetComponents(this));	
 	}
 	
 	private RemoteViews UpdateWidgetComponents(Context context) {      
 		//Fetch Saved settings (Alarm Hour, Minute, Enable) and assign values accordingly
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		AlarmEnabled = settings.getBoolean(ALARM_ENABLED, false);
 		AlarmHour = settings.getInt(ALARM_HOUR, 12);
 		AlarmMin = settings.getInt(ALARM_MINUTE, 0);
 		if (AlarmEnabled) {
 			if (AlarmHour > 12) {
 				HourStr = Integer.toString(AlarmHour - 12);
 			} else {
 				HourStr = Integer.toString(AlarmHour);
 			}
 			MinuteStr = Integer.toString(AlarmMin);
 			if (AlarmMin < 10) {
 				MinuteStr = "0" + MinuteStr;
 			}
 			
 			if (AlarmHour < 12) {
 				AMPMStr = "AM";
 			}
 			else {
 				AMPMStr = "PM";
 			}
 			WidgetString = HourStr + ":" + MinuteStr + " " + AMPMStr;
 		}
 		else {
 			WidgetString = "DISABLED";
 		}	  	
 
 		//Create Pending Intent to attach to OpenButton Click
 		Intent configIntent = new Intent(context, ConfigureAlarm.class);
 		configIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		configIntent.setAction(NextAlarm.ACTION_WIDGET_CONFIGURE);    		
 		PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
 
 		RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.nextalarmwidget);
 		updateViews.setTextViewText(R.id.AlarmTimeTextView, WidgetString);
 		updateViews.setOnClickPendingIntent(R.id.linearLayout2, configPendingIntent);
 		
 		return updateViews;
 	}
 }
