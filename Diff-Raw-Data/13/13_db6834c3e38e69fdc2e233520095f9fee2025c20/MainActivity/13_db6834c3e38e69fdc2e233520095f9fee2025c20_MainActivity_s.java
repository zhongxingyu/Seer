 package com.room;
 
 import com.room.media.MIntro;
 import com.room.media.MMusic;
 import com.room.puzzles.PExample;
 import com.room.render.RModelLoader;
 import com.room.render.RRenderActivity;
 import com.room.scene.SLayoutLoader;
 
 import android.os.*;
 import android.app.*;
 import android.content.Intent;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.*;
 import android.view.View.OnClickListener;
 
 public class MainActivity extends Activity implements OnClickListener
 {
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);        
         
         //store reference to first activity for any asset loaders
         Global.mainActivity = this;
         
         //determine the width and height here
 		DisplayMetrics displaymetrics = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
 		Global.SCREEN_WIDTH = displaymetrics.widthPixels;
 		Global.SCREEN_HEIGHT = displaymetrics.heightPixels;
 		
         //ALL loaders should initialise here
         SLayoutLoader.getInstance().init();		
         RModelLoader.getInstance().init();
         
         //TBD - Right now these two loaders are initialized in RRenderer,
         //      We should remove that dependency and init them here as well.
 		//RShaderLoader.getInstance().init();
 		//RTextureLoader.getInstance().init();
 
         MMusic.loadSEmusic(this);
 		MMusic.playBGmusic(this, R.raw.haunting);
 		        
         setContentView(R.layout.activity_main);
 
         View continueButton = findViewById(R.id.continue_button);
         continueButton.setOnClickListener(this);
         View newButton = findViewById(R.id.new_button);
         newButton.setOnClickListener(this);
         View optionsButton = findViewById(R.id.options_button);
         optionsButton.setOnClickListener(this);
         View creditsButton = findViewById(R.id.credits_button);
         creditsButton.setOnClickListener(this);
         View helpButton = findViewById(R.id.help_button);
         helpButton.setOnClickListener(this);
         View exitButton = findViewById(R.id.exit_button);
         exitButton.setOnClickListener(this);
         
         if(Global.DEBUG_SKIP_MENU)
         	startGame();
 		        
 	}
 
 	@Override
 	public void onClick(View v) {
 		  MMusic.playSound(this, R.raw.swords);
 	      switch (v.getId()) {
 	      case R.id.continue_button:
 	         startGame(); //TODO: Save some state
 	         break;
 	      case R.id.new_button:
 	         startGameWithIntro();
 	         break;
 	      case R.id.options_button:
 	    	 showOptions(); 
 	         break;
 	      case R.id.credits_button:
 	    	 showCredits();
 	    	 break;
 	      case R.id.help_button:
 	    	 break;
 	      case R.id.exit_button:
	         finish();
 	         break;
 	      }
 	}
 	
 	private void startGame()
 	{
 	      Intent intent = new Intent(this, RRenderActivity.class);
 	      startActivity(intent);
 	}
 	
 	private void showCredits() {
 		  Intent intent = new Intent(this, Credits.class);
 		  startActivity(intent);
 	}
 	
 	private void showOptions() {
 		  Intent intent = new Intent(this, Options.class);
 		  startActivity(intent);
 	}
 	
 	private void startGameWithIntro() {
 		  Intent intent = new Intent(this, MIntro.class);
 		  startActivity(intent);
 	}
 	
 	@Override
 	protected void onResume() {
 	      super.onResume();
           if ( Options.getBGMusic(this) == false ) {
         	  MMusic.stopBGmusic(this);
           }
           if ( ! MMusic.isPlaying(this) && Options.getBGMusic(this) == true )
           {
         	  Log.e("MainActivity","onResume() - Resume music");
       		  MMusic.playBGmusic(this, R.raw.haunting);
           }
           else if ( Global.RESUME_MUSIC == true )
           {
         	  MMusic.playBGmusic(this, R.raw.haunting);
         	  Global.RESUME_MUSIC = false;
           }
 	}
 }
