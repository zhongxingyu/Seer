 package com.gradugation;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 
 public class BaseActivity extends Activity{
 	
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 
 	public void onPause() {
 		super.onPause();
		SongPlayer.stopSongDelayed();
 	}
 	
 	protected void onResume() {
     	super.onResume();
     	SharedPreferences settings = getSharedPreferences(SettingsActivity.SOUND_PREFERENCE, 0);
 		boolean isSoundOn = settings.getBoolean(SettingsActivity.SOUND_ON, true);
 		
 		if (isSoundOn) {
			SongPlayer.playSong();
 		}
     }
 }
