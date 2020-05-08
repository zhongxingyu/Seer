 package com.flexymind.labirynth.screens.game;
 
 import com.flexymind.labirynth.R;
 import com.flexymind.labirynth.screens.game.OpenGL.GLGameView;
 import com.flexymind.labirynth.screens.start.StartScreen;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.widget.FrameLayout;
 
 public class GameScreen extends Activity {
     /** Called when the activity is first created. */
 	
 	public static final String LEVELID = "id of level";
 	public static final String LEVELCHOOSEACTION = "levelchoose Action";
 	
 	private static final int NULLID = -666;
 	private static int gameID = NULLID;
 	private static Context context = null;
 	private GLGameView gameView;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         if (getIntent() == null){
         	finish();
         	return;
         }
         
         setContentView(R.layout.main);
         
         FrameLayout rlay = (FrameLayout)findViewById(R.id.gameLayout);
         
         gameView = (GLGameView)rlay.getChildAt(0);
         
         Bundle b = getIntent().getExtras();
         if (   LEVELCHOOSEACTION.equals(getIntent().getAction()) 
         	&& b != null 
         	&& b.containsKey(LEVELID)) {
         	gameID = b.getInt(LEVELID);
         }
         
         if (gameID != NULLID){
             gameView.setGameLevelID(gameID);
         }
        
        context = this.getApplicationContext();
     }
 	
 	@Override
     protected void onPause() {
 		gameView.onPause();
 		super.onPause();
 	}
 
 	
 	@Override
     protected void onResume() {
 		gameView.onResume();
 		super.onResume();
 	}
     
 	@Override
 	public void onBackPressed() {
 		super.onBackPressed();
 		gameView.onDestroy();
 	}
 	
     @Override
     public void onConfigurationChanged(Configuration newConfig) { 
     	super.onConfigurationChanged(newConfig);
     }
 
     public static void startNextLevel(){
     	if (gameID != NULLID){
     		gameID++;
     		if(StartScreen.startActivity != null){
     			StartScreen.startActivity.finishActivity(StartScreen.ID_GAMESCREEN);
     			if (context != null){
     				Intent intent = new Intent();
         			intent.setClass(context, GameScreen.class);
         			StartScreen.startActivity.startActivityForResult(intent, StartScreen.ID_GAMESCREEN);
     			}
     		}
     	}
     }
 }
