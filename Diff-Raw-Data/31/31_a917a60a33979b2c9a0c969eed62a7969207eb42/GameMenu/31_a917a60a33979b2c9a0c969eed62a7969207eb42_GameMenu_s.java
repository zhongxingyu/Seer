 package com.game.pileon;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 
 public class GameMenu extends Activity
 {
 	private boolean gameInProgress;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_game_menu);
 		
 		Button continueButton = (Button) findViewById(R.id.button4);
 		
 		Intent intent = getIntent();
 		gameInProgress = intent.getBooleanExtra("com.game.pileon.GameInProgress", false);
 		
 		if(gameInProgress){
 			continueButton.setVisibility(View.VISIBLE);
 		}
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		getMenuInflater().inflate(R.menu.activity_game_menu, menu);
 		return true;
 	}
 
 	public void about(View view)
 	{
 		Intent intent = new Intent(this, AboutScreen.class);
 		startActivity(intent);
 	}
 
 	public void help(View view)
 	{
 		Intent intent = new Intent(this, HelpScreen.class);
 		startActivity(intent);
 	}
 
 	public void launchGame(View view)
 	{
 		Intent intent = new Intent(this, MainGame.class);
 		startActivity(intent);
 	}
 	
 	public void continueGame(View view)
 	{
 		Intent intent = new Intent(this, MainGame.class);
 		intent.putExtra("com.game.pileon.GameInProgress", true);
 		startActivity(intent);
 	}
 
 	protected void onDestroy()
 	{
 		super.onDestroy();
 	}
 
 }
