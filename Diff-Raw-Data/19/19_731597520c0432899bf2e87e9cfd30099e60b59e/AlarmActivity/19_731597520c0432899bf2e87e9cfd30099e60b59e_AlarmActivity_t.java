 /**
  *  Copyright 2012 Kari Wilhelm
  * 
  *  This file is part of French Press Timer.
  *  
  *  French Press Timer is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *  
  *  French Press Timer is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  
  *  You should have received a copy of the GNU General Public License
  *  along with French Press Timer.  If not, see <http://www.gnu.org/licenses/>.
  *  
  */ 
 
 package com.kari.frenchpress;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.View;
 import android.view.WindowManager;
 
 
 /**
  *
  * This activity sounds the alarm and displays
  * a control to allow the user to kill it. 
  * 
  * If this activity is paused the alarm will automatically
  * be killed.
  * 
  * @author kari
  *
  */
 public class AlarmActivity extends Activity {
 
 	private MediaPlayer mediaPlayer;
 
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.alarm);
 		
 		// make this Activity display over the lock screen
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
			WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
	        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON|
	        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
 	}
 	
 	
	
	
	@Override
	protected void onResume() {
		super.onResume();

		startTheNoise();
	}



 	public void startTheNoise() {
 		
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
 		
 		String noise = preferences.getString("noise", RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString());
 		
 		Uri alertUri = Uri.parse(noise);
 		
 		// Start the annoying sound
 
 		try {
 			mediaPlayer = new MediaPlayer();
 			mediaPlayer.setDataSource(this, alertUri);
 			
 			final AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
 			
 			if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
 				mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
 				mediaPlayer.setLooping(true);
 				mediaPlayer.prepare();
 				mediaPlayer.start();
 			}
 		} catch (Exception ex) {
 			
 			// If playing the alarm doesn't work there isn't much we can do about it
 			
 			Log.e("AlarmActivty", "Error playing alarm " + ex.toString());
 		}
 		
 	}
 
 	
 	public void stopTheNoise(View button) {
 		endThisNoiseAlready();
 	}
 	
 	
 	
 	private void endThisNoiseAlready() {
 		mediaPlayer.stop();
 		
 		WakeLockManager.release();
 		
 		// return to origial activity
 		this.finish();		
 	}
 	
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		
 		mediaPlayer.stop();			
 	}
 	
 	
 }
