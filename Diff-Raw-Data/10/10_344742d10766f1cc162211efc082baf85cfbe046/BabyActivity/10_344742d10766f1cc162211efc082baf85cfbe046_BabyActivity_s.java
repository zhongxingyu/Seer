 package com.eggwall.BabyMusic;
 
 import android.app.Activity;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.View;
 
 public class BabyActivity extends Activity {
     private MediaPlayer mPlayer;
     private static final int PLAYING_NOTHING = 0;
     private static final int MUSIC = 1;
     private static final int WHITE_NOISE = 2;
     /** Set to MUSIC or WHITE_NOISE */
     private int mTypePlaying = 0;
     private final Handler mHandler = new Handler();
     private int mWidth;
     private int mHeight;
     // Change clock location every ten minutes
     private static final int DELAY = 10 * 60 * 1000;
     // Initial delay to go to a random position is much shorter
     private static final int INITIAL_DELAY = 500;
 
     // When this is 10, we are at total alpha decrement, and 0, we are at minimum.
     private int mAlphaDecrement = 1;
 
     private final Runnable mChangeClockLocation = new Runnable() {
         @Override
         public void run() {
             changeClockLocation();
             changeIconLocation();
             postClockChange(DELAY);
         }
     };
 
     /**
      * Moves the clock to some random location.
      */
     private final void changeClockLocation() {
         populateTopLevelDimen();
         final View v = findViewById(R.id.clock);
         final int x = (int)(Math.random()*(mWidth - v.getWidth()));
         final int y = (int)(Math.random()*(mHeight- v.getHeight()));
         v.animate().x(x).y(y);
     }
 
     private void populateTopLevelDimen() {
         if (mWidth == 0) {
             final View topLevel = findViewById(R.id.toplevel);
             mWidth = topLevel.getRight();
         }
         if (mHeight == 0) {
             final View topLevel = findViewById(R.id.toplevel);
             mHeight = topLevel.getBottom();
         }
     }
 
     /**
      * Moves the icons to some random location.
      */
     private final void changeIconLocation() {
         populateTopLevelDimen();
         setLowProfileMode();
         final View cloud = findViewById(R.id.cloud);
         final View note = findViewById(R.id.note);
         final double locationX = Math.random();
         // The top half of the screen is for the note.
         note.setY(0);
         // The bottom half of the screen is for white noise.
         cloud.setY(mHeight - cloud.getHeight());
         // The cloud and the note mirror each other on opposite sides to
         // increase visual separation.
        cloud.animate().x((int)(locationX * (mWidth - cloud.getWidth()))).alpha((float).35-(mAlphaDecrement/100));
        note.animate().x((int)((1-locationX) * (mWidth - note.getWidth()))).alpha((float) .50-(mAlphaDecrement/100));
        mAlphaDecrement++;
        if (mAlphaDecrement > 30) {
            mAlphaDecrement = 30;
         }
     }
 
     void resetAlphaDecrement() {
         mAlphaDecrement = 0;
         final View cloud = findViewById(R.id.cloud);
         final View note = findViewById(R.id.note);
         cloud.animate().alpha((float) .35);
         note.animate().alpha((float) .50);
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         // Go full screen.
         (getActionBar()).hide();
         postClockChange(INITIAL_DELAY);
         setLowProfileMode();
     }
 
     private void setLowProfileMode() {
         final View topLevel = findViewById(R.id.toplevel);
         // Hide the System status bar
         topLevel.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
     }
 
     private final void postClockChange(int delay) {
         mHandler.postDelayed(mChangeClockLocation, delay);
     }
 
     /**
      * Start white noise
      * @param unused the view that got this click event
      */
     public void startWhiteNoise(View unused) {
         startPlayingResource(R.raw.brownian_noise, WHITE_NOISE);
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         releasePlayer();
     }
 
     /**
      * Start white noise
      * @param unused the view that got this click event
      */
     public void startMusic(View unused) {
         startPlayingResource(R.raw.how_deep_is_the_ocean, MUSIC);
     }
 
     /**
      * Start playing the resource specified here.
      * @param id A resource like R.raw.music_file
      * @param type Either MUSIC, or WHITE_NOISE. Passing the same ID twice
      *             is a signal to stop playing music altogether.
      */
     private void startPlayingResource(int id, int type) {
         releasePlayer();
         // The user has touched the screen, show the icons a bit brighter.
         resetAlphaDecrement();
         // If the user hits the same button twice, just stop playing anything.
         if (mTypePlaying != type) {
             mTypePlaying = type;
             mPlayer = MediaPlayer.create(this, id);
             mPlayer.start();
         } else {
             mTypePlaying = PLAYING_NOTHING;
         }
     }
 
     private void releasePlayer() {
         if (mPlayer != null) {
             mPlayer.stop();
             mPlayer.release();
             mPlayer = null;
         }
     }
 }
