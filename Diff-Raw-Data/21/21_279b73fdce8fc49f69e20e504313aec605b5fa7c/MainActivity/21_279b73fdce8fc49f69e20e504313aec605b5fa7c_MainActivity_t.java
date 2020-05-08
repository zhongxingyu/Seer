 package com.gradugation;
 
 import android.content.Intent;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 
 public class MainActivity extends BaseActivity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         SongPlayer.initializePlayer(this);
 
         setVolumeControlStream(AudioManager.STREAM_MUSIC);
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
     public void newGameButtonClick(View view) {
     	Intent intent = new Intent(this, NewGameActivity.class);
     	startActivity(intent);
     }
     
     public void settingsButtonClick(View view) {
     	Intent intent = new Intent(this, SettingsActivity.class);
     	startActivity(intent);
     	
     }
     
     public void minigamesButtonClick(View view){
     	Intent intent = new Intent(this, MinigamesActivity.class);
     	startActivity(intent);
    }
 
     public void continueGameButtonClick(View view) {
     	Intent intent = new Intent(this, ContinueActivity.class);
     	startActivity(intent);
     }
 
     public void howToPlayClick(View view) {
     	Intent intent = new Intent(this, HowToPlayActivity.class);
     	startActivity(intent);
     }
 
 }
