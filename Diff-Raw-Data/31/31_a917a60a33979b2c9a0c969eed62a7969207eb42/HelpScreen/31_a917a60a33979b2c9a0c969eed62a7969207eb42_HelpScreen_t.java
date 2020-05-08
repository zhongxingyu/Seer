 package com.game.pileon;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 
 public class HelpScreen extends Activity
 {
	private boolean gameInProgress;
	
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_help_screen);
		
		Intent intent = getIntent();
		gameInProgress = intent.getBooleanExtra("com.game.pileon.GameInProgress", false);
 	}
 
 	public void backToMain(View view)
 	{
 		Intent intent = new Intent(HelpScreen.this, GameMenu.class);
		intent.putExtra("com.game.pileon.GameInProgress", gameInProgress);
 		startActivity(intent);
 	}
 
 	protected void onDestroy()
 	{
 		super.onDestroy();
 	}
 
 }
