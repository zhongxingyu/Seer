 package com.example.projectkim;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
import android.view.Window;
 
 public class ProjectKimActivity extends Activity
 {
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		
 		// Display splash screen.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.splashscreen);
 		
 		// Start new game thread.
 		new Handler().postDelayed(new Thread()
 		{
 			@Override
 			public void run()
 			{
 				Intent mainMenu = new Intent(ProjectKimActivity.this, PKMainMenu.class);
 				ProjectKimActivity.this.startActivity(mainMenu);
 				ProjectKimActivity.this.finish();
 				overridePendingTransition(R.layout.fadein, R.layout.fadeout);
 			}
 		}, PKEngine.GAME_THREAD_DELAY);
 	}
 }
