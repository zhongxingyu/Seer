 package com.android.icecave.gui;
 
 import android.app.Activity;
 import android.content.pm.ActivityInfo;
 import android.media.AudioManager;
 import android.os.Bundle;
 
 public class OptionsActivity extends Activity
 {
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 		setVolumeControlStream(AudioManager.STREAM_MUSIC);
 	}
 }
