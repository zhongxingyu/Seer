 package com.google.corrigan.owen.wordformed;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.widget.TextView;
 import android.graphics.Typeface;
 public class WordFormed extends Activity implements OnClickListener
 {
 	public static Typeface tf;
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) 
 	{
     	super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setContentView(R.layout.main);
 		TileGenerator tiles = new TileGenerator(5);
 		Dictionary dict = new Dictionary(this);
         Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf");
         TextView tv = (TextView) findViewById(R.id.CustomFontText);
         //TODO: Add this, but at the moment it crashes the program
         tv.setTypeface(tf);
         setVolumeControlStream(AudioManager.STREAM_MUSIC);
         //Set up click listeners for all the buttons
         View newGameButton = findViewById(R.id.new_game_button);
         newGameButton.setOnClickListener(this);
 		View multiplayerButton = findViewById(R.id.multiplayer_button);
 		multiplayerButton.setOnClickListener(this);
 		View how_to_play_button = findViewById(R.id.how_to_play_button);
 		how_to_play_button.setOnClickListener(this);
 		View highScoresButton = findViewById(R.id.high_scores_button);
 		highScoresButton.setOnClickListener(this);
     }
 
 	public void onClick(View v)
 	{
 		switch(v.getId())
 		{
 			case R.id.new_game_button:
 				startActivity(new Intent(this, SinglePlayerGame.class));
 				break;
 			case R.id.multiplayer_button:
 				startActivity(new Intent(this, MultiplayerGame.class));
 				break;
 			case R.id.how_to_play_button:
 				startActivity(new Intent(this, HowToPlay.class));
 				break;
 			case R.id.high_scores_button:
				startActivity(new Intent(this, HiScore.class));
 				break;
 		}
 		
 	}
 }
