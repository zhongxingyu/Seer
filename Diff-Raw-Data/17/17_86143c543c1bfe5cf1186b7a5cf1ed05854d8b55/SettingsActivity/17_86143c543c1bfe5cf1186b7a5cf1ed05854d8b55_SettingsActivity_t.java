 package org.zeu.controller;
 
 import org.zeu.controller.util.Persistency;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.View;
import android.view.Window;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 
 public class SettingsActivity extends Activity {
 
 	private Button done, gameFinder;
 	private EditText url, name, gameId;
 	private CheckBox showOnStartup;
 	private Persistency pref;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

 		setContentView(R.layout.settings);
 
 		pref = new Persistency(this);
 
 		url = (EditText) findViewById(R.id.editText1);
 		url.setText(pref.getUrl());
 
 		name = (EditText) findViewById(R.id.editText2);
 		name.setText(pref.getUsername());
 
 		gameId = (EditText) findViewById(R.id.editText3);
 		gameId.setText(pref.getGameId());
 		gameFinder = (Button) findViewById(R.id.game_finder_button);
 		gameFinder.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				// Not using Util.launchGameFinder because it crashes
 				// it says activity can not be started from outside application
 				// context
 				Intent settingsIntent = new Intent(getApplicationContext(),
 						GameFinderActivity.class);
 				startActivity(settingsIntent);
 			}
 		});
 
 		showOnStartup = (CheckBox) findViewById(R.id.checkBox1);
 		showOnStartup.setChecked(pref.isShowSettingsAtStartup());
 		showOnStartup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView,
 					boolean isChecked) {
 				pref.setSettingsAtStartup(isChecked);
 			}
 		});
 
 		done = (Button) findViewById(R.id.done_button);
 		done.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				save();
 				finish();
 			}
 		});
 	}
 
 	public void save() {
 		pref.setUsername(name.getText().toString());
 		pref.setUrl(url.getText().toString());
 		pref.setGameId(gameId.getText().toString());
 	}
 
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
 			save();
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		gameId.setText(pref.getGameId());
 	}
 }
