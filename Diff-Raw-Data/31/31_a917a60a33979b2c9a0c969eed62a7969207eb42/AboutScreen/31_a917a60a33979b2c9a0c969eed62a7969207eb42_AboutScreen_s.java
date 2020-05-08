 package com.game.pileon;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 
 public class AboutScreen extends Activity
 {

 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_about_screen);
 	}
 
 	public void backToMain(View view)
 	{
 		Intent intent = new Intent(AboutScreen.this, GameMenu.class);
 		startActivity(intent);
 	}
 
 	protected void onDestroy()
 	{
 		super.onDestroy();
 	}
 
 }
