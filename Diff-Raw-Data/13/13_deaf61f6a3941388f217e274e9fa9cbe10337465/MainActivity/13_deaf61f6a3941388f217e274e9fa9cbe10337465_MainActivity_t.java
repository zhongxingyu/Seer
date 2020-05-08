 package com.corridor9design.abandonedandroid;
 
 import java.util.Calendar;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.app.Activity;
 import android.app.AlarmManager;
import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.util.Log;
 import android.view.Menu;
 import android.widget.CompoundButton;
 import android.widget.ToggleButton;
 
 public class MainActivity extends Activity {
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		// check button state to properly display
 		checkToggleButtonState();
 		
 		// main method to start service
 		abandonMe();
 	}
 	
 	@Override
 	protected void onStop() {
 	    super.onStop();  // Always call the superclass method first
 	}
 	
 	@Override
 	protected void onResume(){
 		super.onResume();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	
 	private void abandonMe(){
 		// declare & instantiate toggle button object
 		ToggleButton toggleButton = (ToggleButton) findViewById(R.id.toggleButton1);
 		
 		// give tobbleButton a listener to check for change in state
 		// this new listener will implement the necessary methods if state is changed
 		toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
 			
 			Intent intent = new Intent(MainActivity.this, AbandonedAlarm.class);
 			PendingIntent pending = PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
 			
 			AlarmManager amanager = (AlarmManager) getSystemService(ALARM_SERVICE);
 			Calendar firstAlarm = Calendar.getInstance();
 			
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				if (isChecked){
 					
 					// first alarm will activate 10 seconds after toggleButton
 					// change to let user know of activation. Alarm is activated.
 					firstAlarm.add(Calendar.SECOND, 1); 
 					Log.d("LonelyAndroid Activated",""+firstAlarm.getTime());
 					amanager.set(AlarmManager.RTC_WAKEUP, firstAlarm.getTimeInMillis(), pending);
 					
 					// set the toggleButton preference on so that it will
 					// properly be set on resuming
 					setPreferences("ToggleState", "on", MainActivity.this);
 					
 					// set isFirstRun for service use
 					setPreferences("isFirstRun", "yes", MainActivity.this);
 				} else {
 					// if toggleButton is off, cancel any alarm we've previously set
 					// even if there are none due to first-start
 					amanager.cancel(pending);
 					Log.d("LonelyAndroid Deactivated", ""+firstAlarm.getTime());
 					setPreferences("ToggleState", "off", MainActivity.this);
					
					// cancel all notifications
					NotificationManager nManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE); 
					nManager.cancelAll();
 					finish(); // exit the program if the button gets turned off
 				}
 			}
 		});
 	}
 	
 	// generic method to set the preferences
 	public static void setPreferences(String key, String value, Context context) {
 		SharedPreferences preferences = PreferenceManager
 			.getDefaultSharedPreferences(context);
 		SharedPreferences.Editor editor = preferences.edit();
 		editor.putString(key, value);
 		editor.commit();
 	}
 	
 	// generic method to get preferences as needed
 	public static String getPreferences(String key, Context context) {
 		SharedPreferences preferences = PreferenceManager
 			.getDefaultSharedPreferences(context);
 		return preferences.getString(key, "0");
 	}
 	
 	// check the toggleButton state; on or off as listed in the shared preferences
 	public void checkToggleButtonState(){
 		ToggleButton toggleButton = (ToggleButton) findViewById(R.id.toggleButton1);
 
 		if (getPreferences("ToggleState", MainActivity.this).equals("on")){
 			toggleButton.setChecked(true);
 		}
 		
 
 	}
 }
