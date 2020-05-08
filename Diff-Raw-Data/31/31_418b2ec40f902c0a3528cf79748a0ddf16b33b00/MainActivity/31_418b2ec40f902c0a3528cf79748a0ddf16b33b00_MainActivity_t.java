 package omalleyland.my.simple.tools;
 
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AnalogClock;
 import android.widget.CheckBox;
 import android.widget.RemoteViews;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 /** Main Class for setting the Alarm */
 public class MainActivity extends Activity {
 	
 	public static final String PREFS_NAME = "MyPrefsFile";
 	public static final String ALARM_ENABLED = "ALARM_ENABLED";
 	public static final String ALARM_HOUR = "ALARM_HOUR";
 	public static final String ALARM_MINUTE = "ALARM_MIN";
 	public static final String SNOOZE_MINUTE = "SNOOZE_MIN";
 	
     int AlarmHour;
 	int AlarmMin;
 	boolean AlarmEnabled;
 	CheckBox EnableCheckBox;
 	TimePicker AlarmTimePicker;
 	AnalogClock Clock;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         	
 		//Fetch Saved settings (Alarm Hour, Minute, Enable) and assign values accordingly
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		AlarmEnabled = settings.getBoolean(ALARM_ENABLED, false);
 		AlarmHour = settings.getInt(ALARM_HOUR, 12);
 		AlarmMin = settings.getInt(ALARM_MINUTE, 0);
         
     	//set Layout
         setContentView(R.layout.alarmclock);
         
         //Assign UI components to local variables
         Clock = (AnalogClock)findViewById(R.id.AnalogClock);
         EnableCheckBox = (CheckBox)findViewById(R.id.EnabledCheckBox);        
         AlarmTimePicker = (TimePicker)findViewById(R.id.AlarmTimePicker);        
         EnableCheckBox.setOnClickListener(new AlarmEnableListener());
         
         //Set the Time Picker's Hour, Minute, AM/PM
         AlarmTimePicker.setCurrentHour(AlarmHour);
         AlarmTimePicker.setCurrentMinute(AlarmMin);
 
         //Sets the property, doesn't cause OnClick Event to fire
         EnableCheckBox.setChecked(AlarmEnabled);
         if (AlarmEnabled) {
         	AlarmTimePicker.setEnabled(false);
         } else {
         	AlarmTimePicker.setEnabled(true);
         }
 
         try {
             UpdateWidget();
         }
         catch (Exception e) {
             Toast.makeText(MainActivity.this, "Exception:: " + e.getMessage(), Toast.LENGTH_LONG).show();
         }
     } 	
 
 	//Override this because it is always called before an Activity is Destroyed
 	//And I use it to Save my Settings
     @Override
     public void onPause() {
         AlarmEnabled = EnableCheckBox.isChecked();
         AlarmHour = AlarmTimePicker.getCurrentHour();
         AlarmMin = AlarmTimePicker.getCurrentMinute();
         
 		//Save settings Enabled, Alarm Hour, Alarm Minute
         SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
         SharedPreferences.Editor editor = settings.edit();
         editor.putBoolean(ALARM_ENABLED, AlarmEnabled);
         editor.putInt(ALARM_HOUR, AlarmHour);
         editor.putInt(ALARM_MINUTE, AlarmMin);
 
         //Commit the edits
         editor.commit();
 
         try {
             UpdateWidget();
         }
         catch (Exception e) {
             Toast.makeText(MainActivity.this, "Exception:: " + e.getMessage(), Toast.LENGTH_LONG).show();
         }
                 
         super.onPause();
     }
 
     //Override this to restore settings when Activity is restored
     @Override
     public void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 		
         //Fetch Saved settings (Alarm Hour, Minute, Enable) and assign values accordingly
         SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
         AlarmEnabled = settings.getBoolean(ALARM_ENABLED, false);
         AlarmHour = settings.getInt(ALARM_HOUR, 12);
         AlarmMin = settings.getInt(ALARM_MINUTE, 0);
         
         //Set the Time Picker's Hour, Minute, AM/PM
         AlarmTimePicker.setCurrentHour(AlarmHour);
         AlarmTimePicker.setCurrentMinute(AlarmMin);
 
         //Sets the property, doesn't cause OnClick Event to fire
         EnableCheckBox.setChecked(AlarmEnabled);
         if (AlarmEnabled) {
         	AlarmTimePicker.setEnabled(false);
         } else {
         	AlarmTimePicker.setEnabled(true);
         }
     }
 
 
     /** Called to set/cancel an alarm. */
     public void SetAlarm(boolean AlarmEnabled, int AlarmHour, int AlarmMin) {
         if (AlarmEnabled) {   
         	AlarmTimePicker.setEnabled(false);
             
         	//Build Intent/Pending Intent for setting the alarm
     		Intent AlarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
         	AlarmManager AlmMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
         	PendingIntent Sender = PendingIntent.getBroadcast(MainActivity.this, 0, AlarmIntent, 0);    	
 
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
             Toast.makeText(this, "Alarm Set For " + Integer.toString(alarmCalendar.get(Calendar.MONTH) + 1) + "/" + Integer.toString(alarmCalendar.get(Calendar.DAY_OF_MONTH)) + "/" + Integer.toString(alarmCalendar.get(Calendar.YEAR)) + " " + CalendarHourStr + ":" + CalendarMinStr + " " + strAmPM, Toast.LENGTH_LONG).show();    	
 	
         }
         else {
         	AlarmTimePicker.setEnabled(true);
         	
         	//Build Intent/Pending Intent for canceling the alarm
     		Intent AlarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
         	AlarmManager AlmMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
         	PendingIntent Sender = PendingIntent.getBroadcast(MainActivity.this, 0, AlarmIntent, 0);  
         	AlmMgr.cancel(Sender);
         	
         	//Display Alarm Disabled Message
             Toast.makeText(MainActivity.this, "Alarm Disabled", Toast.LENGTH_LONG).show();
         }
     } 
     
     /** Class for implementing the Enable's Check Box Click Event Listener */
     public class AlarmEnableListener implements CheckBox.OnClickListener {
     	public static final String PREFS_NAME = "MyPrefsFile";
     	public static final String ALARM_HOUR = "ALARM_HOUR";
     	public static final String ALARM_MINUTE = "ALARM_MIN";
     	
     	public void onClick(View v) {
     		//Read State of UI components and call SetAlarm routine
             AlarmEnabled = EnableCheckBox.isChecked();
             AlarmHour = AlarmTimePicker.getCurrentHour();
             AlarmMin = AlarmTimePicker.getCurrentMinute();
             
             //Call Set Alarm Routine
             SetAlarm(AlarmEnabled, AlarmHour, AlarmMin);
             Clock.requestFocus();
 
             try {
                 UpdateWidget();
             }
             catch (Exception e) {
                 Toast.makeText(MainActivity.this, "Exception:: " + e.getMessage(), Toast.LENGTH_LONG).show();
             }
     	}
     	
     }
 	
 	private void UpdateWidget() {
 
 		int AlarmHour;
 		int AlarmMin;
 		String HourStr;
 		String MinuteStr;
 		String AMPMStr;
 		String WidgetString;
 	    
 		//Fetch Saved settings (Alarm Hour, Minute, Enable) and assign values accordingly
 	    AlarmEnabled = EnableCheckBox.isChecked();
 	    AlarmHour = AlarmTimePicker.getCurrentHour();
 	    AlarmMin = AlarmTimePicker.getCurrentMinute();
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
 	    
 		RemoteViews updateViews = new RemoteViews(this.getPackageName(), R.layout.nextalarmwidget);
 		
 		// set the text of component TextView with id 'message'
 		updateViews.setTextViewText(R.id.AlarmTimeTextView, WidgetString);
 		
 		// Push update for this widget to the home screen
 		ComponentName thisWidget = new ComponentName(this.getPackageName(), NextAlarm.class.getName());
 		AppWidgetManager manager = AppWidgetManager.getInstance(this);
 		manager.updateAppWidget(thisWidget, updateViews);
 	}
     
     
 }
