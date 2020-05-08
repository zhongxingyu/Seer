 package com.group7.dragonwars.ui;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.Button;
 
 public class GameActivity extends Activity {
     private static final String TAG = "GameActivity";
     private Integer orientation;
     private Boolean orientationChanged = false;
 
     @Override
     protected final void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // remove the title bar
         requestWindowFeature(Window.FEATURE_NO_TITLE);
 
         // remove the status bar
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                              WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
         Log.d(TAG, "in onCreate");
         setContentView(R.layout.activity_game);
         Log.v(null, "on inCreate");
 
         Button menuButton = (Button) this.findViewById(R.id.menuButton);
         GameView gameView = (GameView) this.findViewById(R.id.gameView);
         menuButton.setOnClickListener(gameView);
     }
 
 }
