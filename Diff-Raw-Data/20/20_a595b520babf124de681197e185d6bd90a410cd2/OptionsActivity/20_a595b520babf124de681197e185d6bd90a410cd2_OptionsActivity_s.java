 package com.android.icecave.gui;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.content.pm.ActivityInfo;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.Spinner;
 import com.android.icecave.R;
 import com.android.icecave.general.Consts;
 import com.android.icecave.general.PlayerThemes;
 import com.android.icecave.general.TileThemes;
 
 public class OptionsActivity extends Activity
 {	
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.activity_options);
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 		setVolumeControlStream(AudioManager.STREAM_MUSIC);
 		
 		// Hide the Status Bar
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 		WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		
 		Spinner backgroundThemeSelect = (Spinner) findViewById(R.id.selectBackgroundTheme);
 		Spinner playerThemeSelect = (Spinner) findViewById(R.id.selectPlayerTheme);
 		CheckBox muteMusic = (CheckBox) findViewById(R.id.muteMusic);
 		
 		final PlayerThemes playerThemes = new PlayerThemes();
 		final TileThemes tileThemes = new TileThemes();
 		final SharedPreferences shared = getSharedPreferences(Consts.PREFS_FILE_TAG, 0);
 		
 		ArrayAdapter<String> tileAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
 		tileAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		tileAdapter.addAll(playerThemes.getThemeNames());
 		backgroundThemeSelect.setAdapter(tileAdapter);
 		
 		ArrayAdapter<String> playerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
 		playerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		playerAdapter.addAll(tileThemes.getThemeNames());
 		playerThemeSelect.setAdapter(playerAdapter);
 		
 		backgroundThemeSelect.setOnItemSelectedListener(new OnItemSelectedListener()
 		{
 			@Override
 			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
 			{
 				// Save selected tile theme
 				shared.edit().putInt(Consts.THEME_SELECT_TAG, tileThemes.getThemeId(pos)).commit();
 			}	
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0)
 			{
 			}
 		});
 		
 		playerThemeSelect.setOnItemSelectedListener(new OnItemSelectedListener()
 		{
 			@Override
 			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
 			{
 				// Save selected player theme
 				shared.edit().putInt(Consts.PLAYER_SELECT_TAG, playerThemes.getThemeId(pos)).commit();
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0)
 			{
 			}
 		});
 		
 		muteMusic.setOnCheckedChangeListener(new OnCheckedChangeListener()
 		{
 			
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
 			{
 				// TODO Complete
 			}
 		});
 	}
 }
