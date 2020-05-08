 /**
  * Android Music Settings by cyanogen (Steve Kondik)
  * 
  * Released under the Apache 2.0 license
  */
 package com.android.music;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceScreen;
 import android.preference.CheckBoxPreference;
 import android.preference.Preference.OnPreferenceClickListener;
 
 
 
 public class MusicSettingsActivity extends PreferenceActivity {
 
 	static final String KEY_SCREEN_ON_WHILE_PLUGGED_IN = "screen_on_while_plugged_in";
 	
 	static final String KEY_UNPAUSE_ON_HEADSET_PLUG = "unpause_on_headset_plug";
 	
 	static final String KEY_DOUBLETAP_TRACKBALL_SKIP = "doubletap_trackball_skip";
 
 	static final String KEY_NOW_PLAYING_FULLSCREEN = "now_playing_fullscreen";
 
 	static final String KEY_ENABLE_GESTURES = "enable_gestures";
 	
 	static final String KEY_INVERT_GESTURES = "invert_gestures";
 	
 	static final String KEY_HAPTIC_FEEDBACK = "haptic_feedback";
 	
 	static final String NEXT_LAYOUT = "next";
 	
 	static final String PREV_LAYOUT = "prevRestart";
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.xml.settings);
         
         invertGestures();
         
      // Get the custom preference
         PreferenceScreen screen = this.getPreferenceScreen();
         CheckBoxPreference invert = (CheckBoxPreference) screen.findPreference(KEY_INVERT_GESTURES);
         
         invert.setOnPreferenceClickListener(new OnPreferenceClickListener() {
             public boolean onPreferenceClick(Preference preference) {
             	invertGestures();
             	return true;
         	}
             });
 	}
 	
 	private void invertGestures(){
         PreferenceScreen screen = this.getPreferenceScreen();
         if (MusicUtils.getBooleanPref(this, MusicSettingsActivity.KEY_INVERT_GESTURES, false)) {
         	Preference next = (Preference) screen.findPreference(NEXT_LAYOUT);
         	next.setLayoutResource(R.layout.next_layout_inv);        
         	Preference prev = (Preference) screen.findPreference(PREV_LAYOUT);
         	prev.setLayoutResource(R.layout.prev_restart_layout_inv);
         } else {
             Preference next = (Preference) screen.findPreference(NEXT_LAYOUT);
             next.setLayoutResource(R.layout.next_layout);        
             Preference prev = (Preference) screen.findPreference(PREV_LAYOUT);
             prev.setLayoutResource(R.layout.prev_restart_layout);
         }
 	}
 }
