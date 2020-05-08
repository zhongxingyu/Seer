 package com.snot.smswakeup;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 
 import android.view.MenuItem;
 import android.content.Intent;
 
 import android.widget.TextView;
 import android.widget.Toast;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 
 import android.media.Ringtone;
 import android.media.RingtoneManager;
 
 import android.util.Log;
 import android.net.Uri;
 
 
 public class MainActivity extends Activity {
 
 	public static final String TAG = "MainActivity";
 	public static final String INTENT_SILENCE = "1";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 
 		if(SoundAlarm.getInstance().isPlaying())
 		{
 			SoundAlarm.getInstance().stop();
 			Toast.makeText(this, getString(R.string.silence_toast), Toast.LENGTH_SHORT).show();
 		}
 
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String wakeUpCommand = prefs.getString("wakeup_cmd", "WAKEUP");
 		String info = getString(R.string.info);
 		TextView tv = (TextView)findViewById(R.id.info);
 		tv.setText(String.format(info, wakeUpCommand));
 	}
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		switch(item.getItemId())
 		{
 		case R.id.action_settings:
 			Intent settings = new Intent(this, Preferences.class);
 			startActivity(settings);
 			return true;
 		case R.id.action_blacklist:
 			Intent blacklist = new Intent(this, BlacklistActivity.class);
 			startActivity(blacklist);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 }
 
