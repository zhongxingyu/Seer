 package com.canefaitrien.spacetrader;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.canefaitrien.spacetrader.dao.DaoMaster;
 import com.canefaitrien.spacetrader.dao.DaoMaster.DevOpenHelper;
 import com.canefaitrien.spacetrader.utils.AbstractActivity;
 
 public class MainMenuActivity extends AbstractActivity {
 
 	private static final String TAG = "MainMenu";
 	private Button button;
 	Typeface font;
 	MediaPlayer track; // background music
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_mainmenu);
 		addSound();
 		addListenerNewGameButton();
 		addListenerLoadGameButton();
 		addListenerDebugButton();
 
 		TextView txt = (TextView) findViewById(R.id.txtview_app_name);
 		font = Typeface.createFromAsset(getAssets(), "fonts/Street Corner.ttf");
 		txt.setTypeface(font);
 
 		DevOpenHelper helper = new DaoMaster.DevOpenHelper(this,
 				"spacetrader-db", null);
 		SpaceTrader.db = helper.getWritableDatabase();
 		SpaceTrader.daoMaster = new DaoMaster(SpaceTrader.db);
 		SpaceTrader.daoSession = SpaceTrader.daoMaster.newSession();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	// starts a new game, goes to Configuration Activity
 	private void addListenerNewGameButton() {
 		final Context context = this;
 
 		button = (Button) findViewById(R.id.btn_newgame);
 		button.setTypeface(Typeface.createFromAsset(getAssets(),
 				"fonts/Street Corner.ttf"));
 
 		button.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				track.release();
 
 				Intent intent = new Intent(context, ConfigurationActivity.class);
 				startActivity(intent);
 
 			}
 		});
 	}
 
 	// opens the database to view saved games
 	private void addListenerLoadGameButton() {
 		button = (Button) findViewById(R.id.btn_loadgame);
 		button.setTypeface(Typeface.createFromAsset(getAssets(),
 				"fonts/Street Corner.ttf"));
 		button.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				track.release();

 				Intent intent = new Intent(MainMenuActivity.this,
 						LoadGameActivity.class);
 				startActivity(intent);
 			}
 		});
 	}
 
 	// Edit this to go to a specific class for Debugging
 	private void addListenerDebugButton() {
 
 		button = (Button) findViewById(R.id.btn_debugmode);
 		button.setTypeface(Typeface.createFromAsset(getAssets(),
 				"fonts/Street Corner.ttf"));
 
 		button.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				// track.release();
 
 				Intent intent = new Intent(MainMenuActivity.this,
 						GalaxyMapActivity.class);
 				startActivity(intent);
 			}
 		});
 	}
 
 	// Plays sound
 	private void addSound() {
 		track = MediaPlayer.create(MainMenuActivity.this, R.raw.silbruch);
 		track.start();
 	}
 
 	protected void onStart() {
 		super.onStart();
 		Log.d(TAG, "onStart called.");
 	}
 
 	protected void onPause() {
 		super.onPause();
 		track.release();
 		Log.d(TAG, "onPause called.");
 	}
 
 	protected void onResume() {
 		super.onResume();
 		Log.d(TAG, "onResume called.");
 	}
 
 	protected void onStop() {
 		super.onStop();
 		Log.d(TAG, "onStop called.");
 	}
 
 	protected void onRestart() {
 		super.onRestart();
 		Log.d(TAG, "onRestart called.");
 	}
 
 	protected void onDestroy() {
 		super.onDestroy();
 	}
 
 }
