 package com.example.smartalarm;
 
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.net.Uri;
 import android.os.Bundle;
import android.os.Handler;
 import android.os.Vibrator;
 import android.preference.PreferenceManager;
 import android.provider.Settings;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 
 public class SleepModeActivity extends Activity {
 
 	private boolean awakened = false; //no longer really necessary
 	private MediaPlayer mp;
 	private Vibrator v;
 	AccelThread daemon;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);		
 		setContentView(R.layout.activity_sleep_mode);
 		/*
 		//debug textviews
 		TextView tvTone = (TextView) findViewById(R.id.testToneUri);
 		TextView tvVibrate = (TextView) findViewById(R.id.testVibrateOn);
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		tvTone.setText(prefs.getString("alarm_ringtone", "alarm_ringtone preference does not exist"));
 		tvVibrate.setText(String.valueOf(prefs.getBoolean("alarm_vibrate", false)));
 		TextView wake = (TextView) findViewById(R.id.testmediathreading);
 		wake.setText("sleep");
 */
 		Intent intent = getIntent();
 		
 		//The start time and end time for the alarm window are passed into the activity on its creation
 		int windowStartHours 	= intent.getIntExtra(ConfirmSleepAlarmActivity.START_ALARM_HOURS, 6);
 		int windowStartMinutes 	= intent.getIntExtra(ConfirmSleepAlarmActivity.START_ALARM_MINUTES, 30);
 		int windowEndHours 		= intent.getIntExtra(ConfirmSleepAlarmActivity.FAILSAFE_ALARM_HOURS, 7);
 		int windowEndMinutes 	= intent.getIntExtra(ConfirmSleepAlarmActivity.FAILSAFE_ALARM_HOURS, 30);
 		
 		//got to use time since  January 1, 1970 00:00:00 UTC
 		//first find datetime corresponding to hour,minute pair entered in app
 		GregorianCalendar now = new GregorianCalendar();
 		
 		//make sure start of window is after current time
 		GregorianCalendar startCalendar = new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), 
 				now.get(Calendar.DAY_OF_MONTH), windowStartHours, windowStartMinutes );
 		while(now.after(startCalendar))
 		{
 			startCalendar.add(Calendar.HOUR_OF_DAY, 12);
 		}
 		
 		//make sure end of window is after start of window
 		GregorianCalendar endCalendar = new GregorianCalendar(startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH),
 				startCalendar.get(Calendar.DAY_OF_MONTH), windowEndHours, windowEndMinutes);
 		while(startCalendar.after(endCalendar))
 		{
 			endCalendar.add(Calendar.HOUR_OF_DAY, 12);
 		}
 		
		hdlr = new AlarmHandler(this);
 		
 		daemon = new AccelThread(this,
 				.2, //this number is entirely arbitrary
 				startCalendar.getTimeInMillis(), endCalendar.getTimeInMillis()); //the surrounding code needs to convert the wakeup times into long values as returned by System.currentTimeMillis() 
 		daemon.start();
 	}
 
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.sleep_mode, menu);
 		return true;
 	}
 	
 	public void awakeButtonClicked(View view) {
 		awakened = true;
 		if(mp != null)
 		{
 			
 			if(mp.isPlaying()){
 				try{
 					mp.stop();
 				}catch(IllegalStateException e){}
 			}
 			try{
 				mp.release();
 			}catch(IllegalStateException e){}
 			
 		}
 		if(v != null)
 		{
 			v.cancel();
 		}
 		/*
 		TextView wake = (TextView) findViewById(R.id.testmediathreading);
 		wake.setText("wake");
 */
 		Intent intent = new Intent(this, MainActivity.class);
 		startActivity(intent);
 
 	}
 	
 	public void testAlarmClicked(View view) {
 		triggerAlarm();
 	}
 	
 	public void triggerAlarm() {
		hdlr.sendEmptyMessage(0);
	}
	
	//this prevents ui thread issues
	private static class AlarmHandler extends Handler {
		private SleepModeActivity act = null;
		public void handleMessage(android.os.Message msg) {
			act.doAlarm();
		}
		public AlarmHandler(SleepModeActivity a) {
			act = a;
		}
	};
	private static AlarmHandler hdlr = null;
	
	private void doAlarm() {
 		if(awakened)
 			return;
 		
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		String uri = prefs.getString("alarm_ringtone", "");
 				/*
 		//debug textviews
 		TextView tvTone = (TextView) findViewById(R.id.testToneUri);
 		TextView tvVibrate = (TextView) findViewById(R.id.testVibrateOn);
 		tvTone.setText(uri);
 		tvVibrate.setText(String.valueOf(prefs.getBoolean("alarm_vibrate", false)));
 		TextView wake = (TextView) findViewById(R.id.testmediathreading);
 		wake.setText("sleep");*/
 
 		mp = MediaPlayer.create(this, Uri.parse(uri));
 		if(mp != null)
 		{
 			mp.setLooping(true);
 			mp.setOnCompletionListener(new OnCompletionListener() {
             	@Override
             	public void onCompletion(MediaPlayer mp) {
             		if(awakened){
             			mp.release();
             		}else{
             			mp.start();
             		}
             	}
         	});   
         	mp.start();
 		}
 		
 	
 		if(prefs.getBoolean("alarm_vibrate", false))
 		{
 			// Get instance of Vibrator from current Context
 			v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 			 
 			// Start immediately
 			// Vibrate for 200 milliseconds
 			// Sleep for 500 milliseconds
 			long[] pattern = { 0, 200, 500 };
 			 
 			// The "0" means to repeat the pattern starting at the beginning
 			v.vibrate(pattern, 0);
 		}
 	}
 }
