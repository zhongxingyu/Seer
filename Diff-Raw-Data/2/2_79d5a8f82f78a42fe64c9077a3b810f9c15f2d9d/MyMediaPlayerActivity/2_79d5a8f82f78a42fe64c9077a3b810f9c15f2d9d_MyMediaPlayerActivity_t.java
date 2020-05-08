 package com.example.testapplication;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.res.AssetFileDescriptor;
 import android.content.res.AssetManager;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 
 import java.io.File;
 import java.io.FileDescriptor;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class MyMediaPlayerActivity extends Activity {
     WakeLock wakeLock;
     private static final String[] EXTENSIONS = {".mp3", ".mid", ".wav", ".ogg", ".mp4"}; //Playable Extensions
     List<String> trackNames; //Playable Track Titles
     List<String> trackArtworks; //Track artwork names
     AssetManager assets; //Assets (Compiled with APK)
     Music track; //currently loaded track
     Button btnPlay; //The play button will need to change from 'play' to 'pause', so we need an instance of it
    boolean isTuning; //is user currently jamming out, if so automatically start playing the next track
     int currentTrack; //index of current track selected
     int type; //0 for loading from assets, 1 for loading from SD card
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setVolumeControlStream(AudioManager.STREAM_MUSIC);
         PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
         wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "Lexiconda");
         setContentView(R.layout.musicplayer);
 
         initialize(0);
     }
 
     @Override
     public void onResume() {
         super.onResume();
         wakeLock.acquire();
     }
 
     @Override
     public void onPause() {
         super.onPause();
         wakeLock.release();
         if (track != null) {
             if (track.isPlaying()) {
                 track.pause();
                 isTuning = false;
                 btnPlay.setBackgroundResource(R.drawable.play);
             }
             if (isFinishing()) {
                 track.dispose();
                 finish();
             }
         } else {
             if (isFinishing()) {
                 finish();
             }
         }
     }
 
     private void initialize(int type) {
         btnPlay = (Button) findViewById(R.id.btnPlay);
         btnPlay.setBackgroundResource(R.drawable.play);
         trackNames = new ArrayList<String>();
         trackArtworks = new ArrayList<String>();
         assets = getAssets();
         currentTrack = 0;
         isTuning = false;
         this.type = type;
 
         addTracks(getTracks());
         loadTrack();
     }
 
     //Generate a String Array that represents all of the files found
     private String[] getTracks() {
         try {
             String[] temp = getAssets().list("");
             return temp;
         } catch (IOException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     //Adds the playable files to the trackNames List
     private void addTracks(String[] temp) {
         if (temp != null) {
             for (int i = 0; i < temp.length; i++) {
                 //Only accept files that have one of the extensions in the EXTENSIONS array
                 if (trackChecker(temp[i])) {
                     trackNames.add(temp[i]);
                     trackArtworks.add(temp[i].substring(0, temp[i].length() - 4));
                 }
             }
         }
     }
 
     //Checks to make sure that the track to be loaded has a correct extenson
     private boolean trackChecker(String trackToTest) {
         for (int j = 0; j < EXTENSIONS.length; j++) {
             if (trackToTest.contains(EXTENSIONS[j])) {
                 return true;
             }
         }
         return false;
     }
 
     //Loads the track by calling loadMusic
     private void loadTrack() {
         if (track != null) {
             track.dispose();
         }
         if (trackNames.size() > 0) {
             track = loadMusic(type);
         }
     }
 
     //loads a Music instance using either a built in asset or an external resource
     private Music loadMusic(int type) {
         switch (type) {
             case 0:
                 try {
                     AssetFileDescriptor assetDescriptor = assets.openFd(trackNames.get(currentTrack));
                     return new Music(assetDescriptor);
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
                 return null;
             case 1:
                 try {
                     FileInputStream fis = new FileInputStream(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), trackNames.get(currentTrack)));
                     FileDescriptor fileDescriptor = fis.getFD();
                     return new Music(fileDescriptor);
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
                 return null;
             default:
                 return null;
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         createMenu(menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case 0:
                 //Set Looping
                 synchronized (this) {
                     if (track.isLooping()) {
                         track.setLooping(false);
                     } else {
                         track.setLooping(true);
                     }
                 }
                 return true;
             case 1:
                 //Stop Music
                 synchronized (this) {
                     track.switchTracks();
                     btnPlay.setBackgroundResource(R.drawable.play);
                 }
                 return true;
             default:
                 return false;
         }
     }
 
     private void createMenu(Menu menu) {
         MenuItem miLooping = menu.add(0, 0, 0, "Looping");
         {
             miLooping.setIcon(R.drawable.looping);
         }
         MenuItem miStop = menu.add(0, 1, 1, "Stop");
         {
             miStop.setIcon(R.drawable.stop);
         }
     }
 
     public void click(View view) {
         int id = view.getId();
         switch (id) {
             case R.id.btnPlay:
                 synchronized (this) {
                     if (isTuning) {
                         isTuning = false;
                         btnPlay.setBackgroundResource(R.drawable.play);
                         track.pause();
                     } else {
                         isTuning = true;
                         btnPlay.setBackgroundResource(R.drawable.pause);
                         playTrack();
                     }
                 }
                 return;
             case R.id.btnPrevious:
                 setTrack(0);
                 loadTrack();
                 playTrack();
                 return;
             case R.id.btnNext:
                 setTrack(1);
                 loadTrack();
                 playTrack();
                 return;
             default:
                 return;
         }
     }
 
     private void setTrack(int direction) {
         if (direction == 0) {
             currentTrack--;
             if (currentTrack < 0) {
                 currentTrack = trackNames.size() - 1;
             }
         } else if (direction == 1) {
             currentTrack++;
             if (currentTrack > trackNames.size() - 1) {
                 currentTrack = 0;
             }
         }
     }
 
     //Plays the Track
     private void playTrack() {
         if (isTuning && track != null) {
             track.play();
         }
     }
 }
