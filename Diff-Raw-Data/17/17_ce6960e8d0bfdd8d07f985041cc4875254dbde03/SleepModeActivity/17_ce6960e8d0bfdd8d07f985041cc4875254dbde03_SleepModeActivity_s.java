 package com.example.smartalarm;
 
 import java.io.IOException;
 
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.net.Uri;
 import android.os.Bundle;
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
 
 	private boolean awakened = false;
 	private MediaPlayer mp;
 	private Vibrator v;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);		
 		setContentView(R.layout.activity_sleep_mode);
 		
 		//debug textviews
 		TextView tvTone = (TextView) findViewById(R.id.testToneUri);
 		TextView tvVibrate = (TextView) findViewById(R.id.testVibrateOn);
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		tvTone.setText(prefs.getString("alarm_ringtone", "alarm_ringtone preference does not exist"));
 		tvVibrate.setText(String.valueOf(prefs.getBoolean("alarm_vibrate", false)));
 		TextView wake = (TextView) findViewById(R.id.testmediathreading);
 		wake.setText("sleep");
 
 		Intent intent = getIntent();
 		
 		//The start time and end time for the alarm window are passed into the activity on its creation
 		int windowStartHours 	= intent.getIntExtra(ConfirmSleepAlarmActivity.START_ALARM_HOURS, 6);
 		int windowStartMinutes 	= intent.getIntExtra(ConfirmSleepAlarmActivity.START_ALARM_MINUTES, 30);
 		int windowEndHours 		= intent.getIntExtra(ConfirmSleepAlarmActivity.FAILSAFE_ALARM_HOURS, 7);
 		int windowEndMinutes 	= intent.getIntExtra(ConfirmSleepAlarmActivity.FAILSAFE_ALARM_HOURS, 30);
 		
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
			mp.stop();
			mp.release();
 		}
 		if(v != null)
 		{
 			v.cancel();
 		}
 		
 		TextView wake = (TextView) findViewById(R.id.testmediathreading);
 		wake.setText("wake");
 		
 	}
 	public void testAlarmClicked(View view) {
 		
 		if(awakened)
 			return;
 		
 
 		
 		
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		String uri = prefs.getString("alarm_ringtone", "");
 				
 		//debug textviews
 		TextView tvTone = (TextView) findViewById(R.id.testToneUri);
 		TextView tvVibrate = (TextView) findViewById(R.id.testVibrateOn);
 		tvTone.setText(uri);
 		tvVibrate.setText(String.valueOf(prefs.getBoolean("alarm_vibrate", false)));
 		TextView wake = (TextView) findViewById(R.id.testmediathreading);
 		wake.setText("sleep");
 
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
