 package com.google.corrigan.owen.wordformed;
 
 import java.util.ArrayList;
 import java.util.Map;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Window;
 import android.view.WindowManager;
 
 public class SinglePlayerGame extends Activity
 {
 	private static final String TAG = SinglePlayerGame.class.getSimpleName();
 	static ArrayList<Word> wordList;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setVolumeControlStream(AudioManager.STREAM_MUSIC);
 		setContentView(new SinglePlayerGameView(this));
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);		
 
 		wordList = new ArrayList<Word>();
 		//game.requestFocus();
 	}
 	
 	@Override
 	protected void onDestroy()
 	{
 		Log.d(TAG, "Destroyed");
		startActivity(new Intent(this, GameOverActivity.class));
 		super.onDestroy();
 	}
 	
 	protected void onStop()
 	{
 		Log.d(TAG, "Stopped");
 		super.onStop();
 	}
 }
