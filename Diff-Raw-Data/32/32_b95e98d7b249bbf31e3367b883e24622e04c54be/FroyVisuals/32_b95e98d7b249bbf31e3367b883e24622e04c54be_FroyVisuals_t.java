 /*
  * Copyright (C) 2010 The Android Open Source Project
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
 package com.starlon.froyvisuals;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.ViewGroup;
 import android.view.View;
 import android.view.MotionEvent;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.media.AudioRecord;
 import android.media.AudioFormat;
 import android.media.MediaRecorder;
 import android.util.Log;
 import android.util.TypedValue;
 
 public class FroyVisuals extends Activity
 {
     private final static String TAG = "FroyVisuals/FroyVisualsActivity";
     private static Settings mSettings;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
 
         mSettings = new Settings(this);
 /*
         View v = new FroyVisualsView(this);
         v.setOnLongClickListener(new View.OnLongClickListener() {
             public boolean onLongClick(View v)
             {
                 switchActor(0);
                 return false;
             }
         });
 */
         setContentView(new FroyVisualsView(this));
     }
 
     public void onResume()
     {
         super.onResume();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.froyvisuals, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         switch(item.getItemId())
         {
             case R.id.about:
             {
                 startActivity(new Intent(this, AboutActivity.class));
                 return true;
             }
 
             case R.id.settings:
             {
                startActivity(new Intent(this, PreferencesActivity.class));
                 return true;
             }
             default:
             {
                 Log.w(TAG, "Unhandled menu-item. This is a bug!");
                 break;
             }
         }
         return false;
     }
     /* load our native library */
     static {
         System.loadLibrary("visual");
         //System.loadLibrary("common");
         //System.loadLibrary("actor_avs");
         System.loadLibrary("main");
     }
 }
 
 class FroyVisualsView extends View {
     private Bitmap mBitmap;
     private AudioRecord mAudio;
     private int mH, mW;
    private boolean mActive;
    private boolean mInit = false;
     private int PCM_SIZE;
     private static int RECORDER_SAMPLERATE = 44100;
     private static int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
     private static int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
     private static final String APP_TAG = "FroyVisuals";
 
     private static native void switchActor(int direction);
     private static native boolean render(Bitmap  bitmap);
     private static native void resizePCM(int size, int rate, int channels, int encoding);
     private static native void uploadAudio(short[] data);
     private static native void initApp(int w, int h);
     private static native void mouseMotion(float x, float y);
     private static native void mouseButton(int button, float x, float y);
     private static native void screenResize(int w, int h);
 
     private static int[] mSampleRates = new int[] { 8000, 11025, 22050, 44100 };
     public AudioRecord findAudioRecord() {
         for (int rate : mSampleRates) {
             for (short audioFormat : new short[] { AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT }) {
                 for (short channelConfig : new short[] { AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO }) {
                     try {
                         Log.d(APP_TAG, "Attempting rate " + rate + "Hz, bits: " + audioFormat + ", channel: "
                                 + channelConfig);
                         int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);
     
                         if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                             // check if we can instantiate and have a success
                             AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, rate, channelConfig, audioFormat, bufferSize);
     
                             if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
                             {
                                 PCM_SIZE = bufferSize;
                                 RECORDER_SAMPLERATE = rate;
                                 RECORDER_CHANNELS = channelConfig;
                                 RECORDER_AUDIO_ENCODING = audioFormat;
                                 return recorder;
                             }
                         }
                     } catch (Exception e) {
                         Log.e(APP_TAG, rate + " Exception, keep trying.",e);
                     }
                 }
             }
         }
         return null;
     }
     
     AudioRecord recorder = findAudioRecord();
     public FroyVisualsView(Context context) {
         super(context);
 
        if(mInit) return;
        mInit = true;

         mW = -1;
         mH = -1;
        mActive = true;
 
         initApp(getWidth(), getHeight());

         mAudio = findAudioRecord();
         if(mAudio != null)
         {
             resizePCM(PCM_SIZE, RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
 	        new Thread(new Runnable() {
 	            public void run() {
					mAudio.startRecording();
                    while(mActive)
                     {
 					    short[] data = new short[PCM_SIZE];
 					    mAudio.read(data, 0, PCM_SIZE);
 					    uploadAudio(data);
                     }
					mAudio.stop();
 	            }
 	        }).start();
         }
     }
 
     @Override protected void onDraw(Canvas canvas) 
     {
         if( mW != getWidth() || mH != getHeight())
         {
             mW = getWidth();
             mH = getHeight();
             mBitmap = Bitmap.createBitmap(mW, mH, Bitmap.Config.RGB_565);
             screenResize(mW, mH);
         }
 
 
         if(!render(mBitmap)) return;
 
         canvas.drawBitmap(mBitmap, 0, 0, null);
 
         invalidate();
     }
 
     private int direction = -1;
     @Override public boolean onTouchEvent (MotionEvent event) 
     {
         int action = event.getAction();
         float x = event.getX();
         float y = event.getY();
         switch(action)
         {
             case MotionEvent.ACTION_DOWN:
                 direction = -1;
             break;
             case MotionEvent.ACTION_UP:
                 if(direction >= 0) {
                     switchActor(direction);
                 }
             break;
             case MotionEvent.ACTION_MOVE:
                 mouseMotion(x, y);
                 int size = event.getHistorySize();
                 if(size > 1)
                 {
                     if(event.getHistoricalX(0, 0) < event.getHistoricalX(0, 1))
                     {
                         direction = 0;
                     }
                     else
                     {
                         direction = 1;
                     }
                 }
             break;
         }
         return true;    
     }
 }
