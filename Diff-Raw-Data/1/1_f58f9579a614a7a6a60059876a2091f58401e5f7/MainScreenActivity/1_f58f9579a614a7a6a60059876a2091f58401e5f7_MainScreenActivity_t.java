 package com.github.AndroidGames.QuizGames;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 public class MainScreenActivity extends Activity implements OnClickListener {
 	
 	Button gameButton;
 	Button optionsButton;
 	
 	MediaPlayer mediaPlayer;
 	
 	private static final String TAG = "QuizGame";
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main_screen_activity);
 		Log.i(TAG, "Main activity created");
 		
 		gameButton = (Button) findViewById(R.id.game_button);
 		gameButton.setOnClickListener(this);
 		optionsButton = (Button) findViewById(R.id.options_button);
 		optionsButton.setOnClickListener(this);
 		
 		Log.d(TAG, "start mediaPlayer");
		mediaPlayer = new MediaPlayer();
 		mediaPlayer.setLooping(true);
         mediaPlayer = MediaPlayer.create(this, R.raw.beethoven);
         mediaPlayer.start();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main_screen, menu);
 		return true;
 	}
 
 	@Override
 	public void onClick(View v) {
 		Intent intent;
 		Log.i(TAG,"The " + v.getId() + " was clicked");
 		switch (v.getId()){
 		case R.id.game_button:
 			Log.i(TAG, "Game button was clicked. Creating intent");
 			intent = new Intent(this, ChooseGameActivity.class);
 			Log.i(TAG, "Starting ChooseGameActivity");
 			startActivity(intent);
 			break;
 		case R.id.options_button:
 			Log.i(TAG, "Options button was clicked. Creating intent");
 			intent = new Intent(this, OptionsActivity.class);
 			Log.i(TAG, "Starting OptionsActivity");
 			startActivity(intent);
 			break;
 		
 		}
 		
 	}
 
 }
