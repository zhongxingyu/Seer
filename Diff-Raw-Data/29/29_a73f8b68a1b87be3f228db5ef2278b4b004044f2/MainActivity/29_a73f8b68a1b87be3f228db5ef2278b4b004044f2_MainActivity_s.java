 package com.jacknife.dubdroid;
 
 import java.io.IOException;
 import android.app.Activity;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 import com.jacknife.Utils;
 
 public class MainActivity extends Activity {	
 	
 	private MediaPlayer sound01;
     private MediaPlayer sound02;
     private MediaPlayer sound03;
     private MediaPlayer sound04;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         //Init the media Players
         sound01 = MediaPlayer.create(this, R.raw.loop1);
         sound02 = MediaPlayer.create(this, R.raw.loop2);
         sound03 = MediaPlayer.create(this, R.raw.loop3);
         sound04 = MediaPlayer.create(this, R.raw.loop4);
         
         //Setup for Loop1 Button
         Button mButton1 = (Button) findViewById(R.id.loop1);
         mButton1.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				try {
 					sound01.prepare();
 				} catch (IllegalStateException e) {
 					System.err.println("Illegal state");
 					e.printStackTrace();
 				} catch (IOException e) {
 					System.err.println("IOException!");
 					e.printStackTrace();
 				}
 				sound01.start();				
 				
 			}
 		});
         
         //Setup for Loop2 Button
         Button mButton2 = (Button) findViewById(R.id.loop2);
         mButton2.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				try {
 					sound02.prepare();
 				} catch (IllegalStateException e) {
 					System.err.println("Illegal state");
 					e.printStackTrace();
 				} catch (IOException e) {
 					System.err.println("IOException!");
 					e.printStackTrace();
 				}
 				sound02.start();				
 				
 			}
 		});
         
         //Setup for Loop3 Button
         Button mButton3 = (Button) findViewById(R.id.loop3);
         mButton3.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				try {
 					sound03.prepare();
 				} catch (IllegalStateException e) {
 					System.err.println("Illegal state");
 					e.printStackTrace();
 				} catch (IOException e) {
 					System.err.println("IOException!");
 					e.printStackTrace();
 				}
 				sound03.start();				
 				
 			}
 		});
         
         //Setup for Loop4 Button
         Button mButton4 = (Button) findViewById(R.id.loop4);
         mButton4.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				try {
 					sound04.prepare();
 				} catch (IllegalStateException e) {
 					System.err.println("Illegal state");
 					e.printStackTrace();
 				} catch (IOException e) {
 					System.err.println("IOException!");
 					e.printStackTrace();
 				}
 				sound04.start();				
 				
 			}
 		});
         
         //Setup for Stop button
         Button mStop = (Button)findViewById(R.id.stopButton);
         mStop.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				//stop all the active sounds
 				if(sound01.isPlaying()){
 					sound01.stop();
 				}
 				if(sound02.isPlaying()){
 					sound02.stop();
 				}
 				if(sound03.isPlaying()){
 					sound03.stop();
 				}
 				if(sound04.isPlaying()){
 					sound04.stop();
 				}
 				Utils.makeToast("Stopping...", v.getContext());
 			}
 		});
     }
     
         @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
 }
