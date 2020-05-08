 package com.github.AndroidGames.QuizGames;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 
 public class OptionsActivity extends Activity implements OnClickListener,  OnSeekBarChangeListener{
 	
 	Button statsButton, helpButton, aboutButton;
 	SeekBar volumeSeekBar;
 	CheckBox soundCheckBox;
 	
 	AudioManager audioManager;
 
 	private static final String TAG = "QuizGame";
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.options_activity);
 		Log.i(TAG, "OptionsActivity created");
 
 		statsButton = (Button) findViewById(R.id.statsButton);
 		statsButton.setOnClickListener(this);
 		helpButton = (Button) findViewById(R.id.helpButton);
 		helpButton.setOnClickListener(this);
 		aboutButton = (Button) findViewById(R.id.aboutButton);
 		aboutButton.setOnClickListener(this);
 		
 		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
 		
 		int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
         int curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
         
 		volumeSeekBar = (SeekBar) findViewById(R.id.volumeSeekBar);
         volumeSeekBar.setMax(maxVolume);
         volumeSeekBar.setProgress(curVolume);         
         volumeSeekBar.setOnSeekBarChangeListener(this);
         
         soundCheckBox = (CheckBox) findViewById(R.id.soundCheckBox);
         soundCheckBox.setOnClickListener(this);
 	}
 	
 	@Override
 	public void onClick(View v) {
 		Intent intent;
 		Log.i(TAG,"The " + v.getId() + " was clicked");
 		switch (v.getId()){
 		case R.id.helpButton:
 			Log.i(TAG, "Help button was clicked. Creating intent");
 			intent = new Intent(this, RulesActivity.class);
 			Log.i(TAG, "Starting RulesActivity");
 			startActivity(intent);
 			break;
 		case R.id.soundCheckBox:
 			Log.i(TAG, "soundCheckBox was clicked.");
			audioManager.setStreamMute(AudioManager.STREAM_MUSIC, soundCheckBox.isChecked());
 			break;
 		case R.id.statsButton:
 			Log.i(TAG, "Stats button was clicked. Creating intent");
 			intent = new Intent(this, StatsActivity.class);
 			Log.i(TAG, "Starting StatsActivity");
 			startActivity(intent);
 			break;
 		case R.id.aboutButton:
 			Log.i(TAG, "About button was clicked. Creating intent");
 			intent = new Intent(this, AboutActivity.class);
 			Log.i(TAG, "Starting AboutActivity");
 			startActivity(intent);
 			break;
 		}
 	}
 
 	@Override
 	public void onProgressChanged(SeekBar seekBar, int progress,
 			boolean fromUser) {
 		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
 		
 	}
 
 	@Override
 	public void onStartTrackingTouch(SeekBar seekBar) {
 		
 	}
 
 	@Override
 	public void onStopTrackingTouch(SeekBar seekBar) {
 		
 	}
 
 }
