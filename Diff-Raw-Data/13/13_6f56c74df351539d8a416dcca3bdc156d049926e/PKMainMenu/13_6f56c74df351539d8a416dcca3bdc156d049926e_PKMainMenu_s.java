 package com.example.projectkim;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 
 public class PKMainMenu extends Activity
 {
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_project_kim);
 		
 		// Fire up background music.
 		PKEngine.musicThread = new Thread()
 		{
 			public void run()
 			{
 				Intent bgmusic = new Intent(getApplicationContext(), PKMusic.class);
 				startService(bgmusic);
 				PKEngine.context = getApplicationContext();
 			}
 		};
 		PKEngine.musicThread.start();
 		
 		final PKEngine engine = new PKEngine();
 		
 		// Set menu button options.
 		ImageButton start = (ImageButton)findViewById(R.id.btnStart);
 		ImageButton settings = (ImageButton)findViewById(R.id.btnSettings);
 		ImageButton exit = (ImageButton)findViewById(R.id.btnExit);
 		
 		start.getBackground().setAlpha(PKEngine.MENU_BUTTON_ALPHA);
 		start.setHapticFeedbackEnabled(PKEngine.HAPTIC_BUTTON_FEEDBACK);
 		
 		settings.getBackground().setAlpha(PKEngine.MENU_BUTTON_ALPHA);
 		settings.setHapticFeedbackEnabled(PKEngine.HAPTIC_BUTTON_FEEDBACK);
 		
 		exit.getBackground().setAlpha(PKEngine.MENU_BUTTON_ALPHA);
 		exit.setHapticFeedbackEnabled(PKEngine.HAPTIC_BUTTON_FEEDBACK);
 		
 		start.setOnClickListener(new OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				// TODO Start game.
 			}
 		});
 		
 		settings.setOnClickListener(new OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				// TODO Open settings window.
 			}
 		});
 		
 		exit.setOnClickListener(new OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				// Exit game.
 				boolean clean = false;
 				clean = engine.onExit(v);
 				if (clean)
 				{
 					int pid = android.os.Process.myPid();
 					android.os.Process.killProcess(pid);
 				}
 			}
 		});
 	}
 	
 	@Override
 	protected void onPause()
 	{
 		System.out.println("paused");
 		super.onPause();
 		PKEngine.player.pause();
 	}
 
 	@Override
 	protected void onResume()
 	{
 		super.onResume();
 		if (PKEngine.player != null) PKEngine.player.start();
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event)
 	{
 		if (keyCode == KeyEvent.KEYCODE_BACK)
 		{
 			findViewById(R.id.btnExit).performClick();
 		}
 		return false;
 	}
 }
