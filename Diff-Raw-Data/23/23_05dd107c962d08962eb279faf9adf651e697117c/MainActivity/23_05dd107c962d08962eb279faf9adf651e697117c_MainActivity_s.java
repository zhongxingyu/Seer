 package com.android.icecave.gui;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.ActivityInfo;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.RadioGroup;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 import com.android.icecave.R;
 
 public class MainActivity extends Activity
 {
 	final String PREFS_FILE_TAG = "prefsFile";
 	final int DEFAULT_LEVEL = 0;
 	SharedPreferences mShared;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 		setVolumeControlStream(AudioManager.STREAM_MUSIC);
 
 		mShared = getSharedPreferences(PREFS_FILE_TAG, 0);
 
 		Button optionsActivity = (Button) findViewById(R.id.options_button);
 		Button gameActivity = (Button) findViewById(R.id.game_button);
 		RadioGroup levelSelect = (RadioGroup) findViewById(R.id.levelSelect);
 
 		// Load level selection from prefs if exists
 		levelSelect.check(mShared.getInt(Consts.LEVEL_SELECT_TAG, DEFAULT_LEVEL));
 		
 		optionsActivity.setOnClickListener(new OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				startActivity(new Intent(v.getContext(), OptionsActivity.class));
 			}
 		});
 
 		gameActivity.setOnClickListener(new OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				Intent i = new Intent(v.getContext(), GameActivity.class);
 				
 				// Load selection from prefs if exists
 				i.putExtra(Consts.LEVEL_SELECT_TAG, mShared.getInt(Consts.LEVEL_SELECT_TAG, DEFAULT_LEVEL));
 				i.putExtra(Consts.PLAYER_SELECT_TAG, mShared.getString(Consts.PLAYER_SELECT_TAG, Consts.DEFAULT_PLAYER));
 				startActivity(i);
 			}
 		});
 
 		levelSelect.setOnCheckedChangeListener(new OnCheckedChangeListener()
 		{
 
 			@Override
 			public void onCheckedChanged(RadioGroup group, int checkedId)
 			{
 				// Save level to prefs
 				mShared.edit().putInt(Consts.LEVEL_SELECT_TAG, checkedId).commit();
 			}
 		});
 	}
 }
