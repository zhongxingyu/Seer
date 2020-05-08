 /*
  * Copyright (C) 2007 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.tware.glrun;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.graphics.Color;
 import android.media.MediaPlayer;
 import android.opengl.GLSurfaceView;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.Display;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.ViewGroup.LayoutParams;
 import android.view.WindowManager;
 import android.widget.TextView;
 
 /**
  * Wrapper activity demonstrating the use of {@link GLSurfaceView}, a view
  * that uses OpenGL drawing into a dedicated surface.
  */
 public class GlRunin extends Activity {
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // Create our Preview view and set it as the content of our
         // Activity
         formatter = new SimpleDateFormat("HH:mm:ss");
         beginTime = formatter.format(new Date());
         
         mGLSurfaceView = new GLSurfaceView(this);
         mGLSurfaceView.setRenderer(new CubeRenderer(false));
         setContentView(mGLSurfaceView);
         
         this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
         
         timeView = new TextView(this);
         timeView.setText("Hello world");
         
         this.addContentView(timeView,new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
         task = new TimerTask(){
         	@Override
         	public void run()
         	{
        		if (hou >= RUNTIME)
        		{
         			uHandler.sendEmptyMessage(1);
        		}else
        			uHandler.sendEmptyMessage(0);
         		}
         };
 
         timer.schedule(task, 1000, 1000);
         
         uHandler = new Handler(){
         	public void handleMessage(Message msg)
         	{
         		switch (msg.what)
         		{
         			case 0:
         				currTime = formatter.format(new Date());
        				if (min>=59)
        				{
         					sec = 0;
         					min = 0;
         					hou ++;
         				}
         				if(sec>=59){
         					sec = 0;
         					min ++;
         				}
         				sec ++;
         				timeView.setText("   Begin  Time: " + beginTime 
         						+"\nCurrent  Time: " + currTime
         						+"\nElapsed Time: " + hou+":"+min+":"+sec);
             		break;
             		
         		case 1:
         			if (mGLSurfaceView!=null)	mGLSurfaceView.onPause();
         			if (mediaplayer!=null) 		mediaplayer.stop();    
         			Display display = getWindowManager().getDefaultDisplay();
         			timeView.setWidth(display.getWidth());
         			timeView.setHeight(display.getHeight());
         			timeView.setBackgroundColor(Color.GREEN);
         			timeView.setTextSize(138);
         			timeView.setTextColor(Color.WHITE);
         			timeView.setGravity(Gravity.CENTER);
         			timeView.setText("Pass");
        			if (timer != null)
        			{
         				timer.cancel();
         				timer = null;
         			}
         			break;
         			
         		case 2:
         			if (mGLSurfaceView!=null)	mGLSurfaceView.onPause();
         			if (mediaplayer!=null) 		mediaplayer.stop();
         			display = getWindowManager().getDefaultDisplay();
         			timeView.setWidth(display.getWidth());
         			timeView.setHeight(display.getHeight());
         			timeView.setBackgroundColor(Color.RED);
         			timeView.setTextSize(138);
         			timeView.setTextColor(Color.WHITE);
         			timeView.setGravity(Gravity.CENTER);
         			timeView.setText("Fail");
        			if (timer != null)
        			{
         				timer.cancel();
         				timer = null;
         			}
         			break;
         		}
         	}
         };
     }
 
     @Override
     protected void onResume() {
         // Ideally a game should implement onResume() and onPause()
         // to take appropriate action when the activity looses focus
         super.onResume();
         mGLSurfaceView.onResume();
         
        if (mediaplayer != null)
        {
         	mediaplayer.start();
         	Log.e("3D Music", "Playing...");
         }else{
         	mediaplayer = MediaPlayer.create(this, R.raw.neocore2);
         	mediaplayer.setLooping(true);
 			mediaplayer.start();
         	Log.e("3D Music", "Started...");			
         }
     }
 
     @Override
     protected void onPause() {
         // Ideally a game should implement onResume() and onPause()
         // to take appropriate action when the activity looses focus
         super.onPause();
         mGLSurfaceView.onPause();
         if (mediaplayer!=null){
         	mediaplayer.pause();
         	Log.e("3D Music", "Paused!");
         }
     }
     
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			return true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
     
     private GLSurfaceView mGLSurfaceView;
     private TimerTask task;
     private Timer timer = new Timer();
     private TextView timeView;
     private Handler uHandler;
     private int sec = 0;
     private int min = 0;
     private int hou = 0;
     private int RUNTIME = 2;
     private String beginTime;
     private String currTime;
     private SimpleDateFormat formatter;
     private MediaPlayer mediaplayer;
 }
