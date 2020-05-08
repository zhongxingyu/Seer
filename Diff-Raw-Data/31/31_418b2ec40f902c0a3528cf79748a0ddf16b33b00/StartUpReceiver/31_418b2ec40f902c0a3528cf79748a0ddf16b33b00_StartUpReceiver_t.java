 package omalleyland.my.simple.tools;
 
 import android.widget.*;
 import android.util.Log;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.app.PendingIntent;
 import android.app.AlarmManager;
 import android.appwidget.AppWidgetManager;
 import java.util.Calendar;
 import android.content.SharedPreferences;
 import android.content.BroadcastReceiver;
 
 public class StartUpReceiver extends BroadcastReceiver {
 	
 	public static final String PREFS_NAME = "MyPrefsFile";
 	public static final String ALARM_ENABLED = "ALARM_ENABLED";
 	public static final String ALARM_HOUR = "ALARM_HOUR";
 	public static final String ALARM_MINUTE = "ALARM_MIN";
 	public static final String SNOOZE_MINUTE = "SNOOZE_MIN";
 	
 	private Context context;
 
 	@Override
     public void onReceive(Context context, Intent intent) { 
 	    int AlarmHour = 0;
 		int AlarmMin = 0;
 		boolean AlarmEnabled = false;
 		
 		Log.d("MySimpleAlarmClock", "Boot Up Received");
 		
 		this.context = context;
 		
 		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
 		AlarmEnabled = settings.getBoolean(ALARM_ENABLED, false);
 		AlarmHour = settings.getInt(ALARM_HOUR, 12);
 		AlarmMin = settings.getInt(ALARM_MINUTE, 0);
 		
 		setAlarm(AlarmEnabled, AlarmHour, AlarmMin);
 
         try {
             UpdateWidget(AlarmEnabled, AlarmHour, AlarmMin);
         }
         catch (Exception e) {
         }
 	}
     
     public void setAlarm(boolean AlarmEnabled, int AlarmHour, int AlarmMin) {  
         if (AlarmEnabled) {   
             
         	//Build Intent/Pending Intent for setting the alarm
     		Intent AlarmIntent = new Intent(context, AlarmReceiver.class);
         	AlarmManager AlmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
         	PendingIntent Sender = PendingIntent.getBroadcast(context, 0, AlarmIntent, 0);    	
 
         	//Build Calendar objects for setting alarm
     		Calendar curCalendar = Calendar.getInstance();
     		Calendar alarmCalendar = Calendar.getInstance();
     		
     		//Initialize Seconds and Milliseconds to 0 for both calendars
     		curCalendar.set(Calendar.SECOND, 0);
     		curCalendar.set(Calendar.MILLISECOND, 0);    		
     		alarmCalendar.set(Calendar.SECOND, 0);
     		alarmCalendar.set(Calendar.MILLISECOND, 0);
 
     		//Update alarmCalendar with Alarm Hour and Minute Settings
     		alarmCalendar.set(Calendar.HOUR_OF_DAY, AlarmHour);
     		alarmCalendar.set(Calendar.MINUTE, AlarmMin);
 
     		//If Alarm Time is now or in the past, set it for tomorrow 24 hours in advance from time selected
     		if (alarmCalendar.getTimeInMillis() <= curCalendar.getTimeInMillis()) {
     			alarmCalendar.add(Calendar.HOUR, 24);
     		}
     		//Set the alarm
     		AlmMgr.set(AlarmManager.RTC_WAKEUP, alarmCalendar.getTimeInMillis(), Sender);
     		
     		//Build the Strings for displaying the alarm time through Toast
     		String CalendarHourStr;
     		if (AlarmHour > 12) {
     			CalendarHourStr = Integer.toString(AlarmHour - 12);
     		} else {
     			CalendarHourStr = Integer.toString(AlarmHour);
     		}
     		String CalendarMinStr = Integer.toString(AlarmMin);
     		if (AlarmMin < 10) {
     			CalendarMinStr = "0" + CalendarMinStr;
     		}
     		
     		String strAmPM;
     		if (AlarmHour < 12) {
     			strAmPM = "AM";
     		}
     		else {
     			strAmPM = "PM";
     		}
     		Log.d("MySimpleAlarmClock", "Alarm Set For " + Integer.toString(alarmCalendar.get(Calendar.MONTH) + 1) + "/" + Integer.toString(alarmCalendar.get(Calendar.DAY_OF_MONTH)) + "/" + Integer.toString(alarmCalendar.get(Calendar.YEAR)) + " " + CalendarHourStr + ":" + CalendarMinStr + " " + strAmPM);
            // Toast.makeText(this, "Alarm Set For " + Integer.toString(alarmCalendar.get(Calendar.MONTH) + 1) + "/" + Integer.toString(alarmCalendar.get(Calendar.DAY_OF_MONTH)) + "/" + Integer.toString(alarmCalendar.get(Calendar.YEAR)) + " " + CalendarHourStr + ":" + CalendarMinStr + " " + strAmPM, Toast.LENGTH_LONG).show();    	
 	
         }
         else {        	
         	//Build Intent/Pending Intent for canceling the alarm
     		Intent AlarmIntent = new Intent(context, AlarmReceiver.class);
         	AlarmManager AlmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
         	PendingIntent Sender = PendingIntent.getBroadcast(context, 0, AlarmIntent, 0);  
         	AlmMgr.cancel(Sender);
         	
         	//Display Alarm Disabled Message
             //Toast.makeText(ConfigureAlarm.this, "Alarm Disabled", Toast.LENGTH_LONG).show();
         }
     }
 	
 	private void UpdateWidget(boolean AlarmEnabled, int AlarmHour, int AlarmMin) {
 
 		String HourStr;
 		String MinuteStr;
 		String AMPMStr;
 		String WidgetString;
 	    
 		//Fetch Saved settings (Alarm Hour, Minute, Enable) and assign values accordingly
 	    if (AlarmEnabled) {
 			if (AlarmHour > 12) {
 				HourStr = Integer.toString(AlarmHour - 12);
 			} else {
 				if (AlarmHour > 9){
 					HourStr = Integer.toString(AlarmHour);
 				}else{
 					HourStr = "0" + Integer.toString(AlarmHour);
 				}
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
 	    
 		RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.nextalarmwidget);
 		
 		// set the text of component TextView with id 'message'
 		updateViews.setTextViewText(R.id.AlarmTimeTextView, WidgetString);
 		
 		// Push update for this widget to the home screen
 		ComponentName thisWidget = new ComponentName(context.getPackageName(), NextAlarm.class.getName());
 		AppWidgetManager manager = AppWidgetManager.getInstance(context);
 		manager.updateAppWidget(thisWidget, updateViews);
 	}
 
 }
