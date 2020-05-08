 package com.example.testapplication;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 
 
 public class MainActivity extends Activity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
     }
 
     /**
      * Called when the user clicks the Music Player button
      */
     public void startMusicPlayer(View view) {
         Intent intent = new Intent(this, MyMediaPlayerActivity.class);
         startActivity(intent);
     }
 
     /**
      * Called when the user clicks the About button
      */
     public void startAbout(View view) {
         Intent intent = new Intent(this, About.class);
         startActivity(intent);
     }
 
     /**
      * Called when the user clicks the Camera button
      */
     public void startCamera(View view) {
         Intent intent = new Intent(this, PhotoIntentActivity.class);
         startActivity(intent);
     }
 
     /**
      * Called when the user clicks the Website button
      */
     public void startWebsite(View view) {
         Intent intent = new Intent(this, WebActivity.class);
         startActivity(intent);
     }
 
     /**
      * Called when the user clicks the Contacts button
      */
     public void startContacts(View view) {
         Intent intent = new Intent(this, ContactListActivity.class);
         startActivity(intent);
     }
 
     /**
      * Called when the user clicks the Video Player button
      */
     public void startVideoplayer(View view) {
         Intent intent = new Intent(this, VideoplayerActivity.class);
         startActivity(intent);
     }
 
     /**
     * Called when the user clicks the Video Player button
      */
     public void startProcSpeed(View view) {
         Intent intent = new Intent(this, ProcessingSpeedActivity.class);
         startActivity(intent);
     }
 
 }
