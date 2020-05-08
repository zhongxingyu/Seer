 package org.worldsproject.alarmclock;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class AlwaysAlarmActivity extends Activity 
 {
 	public static ArrayList<Alarm> alarms = new ArrayList<Alarm>();
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		Intent intent = getIntent();
 		String mode = intent.getStringExtra("mode");
 		
 		LinearLayout root = (LinearLayout)findViewById(R.id.alarms);
 		root.removeAllViews();
 		for(Alarm v:alarms)
 		{
 			root.addView(v.generateView(this));
 		}
 
 		if(mode != null)
 		{
 			if(mode.equals("new_alarm"))
 			{
 				Alarm alarm = new Alarm(intent.getIntExtra("hour", 0),
 						intent.getIntExtra("minute", 0),
 						intent.getIntExtra("steps", 0),
 						intent.getBooleanExtra("monday", false),
 						intent.getBooleanExtra("tuesday", false),
 						intent.getBooleanExtra("wednesday", false),
 						intent.getBooleanExtra("thursday", false),
 						intent.getBooleanExtra("friday", false),
 						intent.getBooleanExtra("saturday", false),
 						intent.getBooleanExtra("sunday", false));
 				
 				root.addView(alarm.generateView(this));
 				alarms.add(alarm);
 				
 				Calendar cal = alarm.nextAlarmEvent();
 				
				if(cal == null)
				{
					Toast.makeText(getBaseContext(), "Alarm exists in past.\nNo alarm added", Toast.LENGTH_LONG).show();
					return;
				}
				Calendar cur = Calendar.getInstance();
				cur.setTimeInMillis(System.currentTimeMillis());
				long diff = cal.getTimeInMillis() - cur.getTimeInMillis();
				Toast.makeText(getBaseContext(), "" + (diff/1000), Toast.LENGTH_LONG).show();
				
 		    	Intent alarmIntent = new Intent(AlwaysAlarmActivity.this, AlarmReceiver.class);
 		    	alarmIntent.putExtra("steps", intent.getIntExtra("steps", 0));
 		    	PendingIntent sender = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
 		    	
 		    	AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
 		    	am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
 			}
 		}
 	}
 
 	public void addAlarm(View v)
 	{
 		Intent myIntent = new Intent(AlwaysAlarmActivity.this, AddAlarmActivity.class);
 		AlwaysAlarmActivity.this.startActivity(myIntent);
 	}
 
 	public void showPreferences(View v)
 	{
 		Intent myIntent = new Intent(AlwaysAlarmActivity.this, AlarmPreferences.class);
 		AlwaysAlarmActivity.this.startActivity(myIntent);
 	}
 	
 	private String formatMinute(int second)
 	{
 		if(second < 10)
 			return "0" + second;
 		else
 			return "" + second;
 	}
 }
