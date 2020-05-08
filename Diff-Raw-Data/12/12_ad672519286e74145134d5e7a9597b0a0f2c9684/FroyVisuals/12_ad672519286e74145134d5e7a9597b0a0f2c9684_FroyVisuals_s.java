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
 import android.os.CountDownTimer;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.ViewGroup;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.MotionEvent;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Typeface;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.media.AudioRecord;
 import android.media.AudioFormat;
 import android.media.MediaRecorder;
 import android.util.Log;
 import android.util.TypedValue;
 import java.util.Timer;
 import java.util.TimerTask;
 
 
 public class FroyVisuals extends Activity
 {
     private final static String TAG = "FroyVisuals/FroyVisualsActivity";
     private static Settings mSettings;
     private NativeHelper mNativeHelper;
     private AudioRecord mAudio;
     private MediaRecorder mRecorder;
     private FroyVisualsView mView;
     private FroyVisualsRenderer mRenderer;
     private boolean mMicActive = false;
     private int PCM_SIZE = 1024;
     private static int RECORDER_SAMPLERATE = 44100;
     private static int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
     private static int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
 
     public String mTextDisplay = null;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
 
         mSettings = new Settings(this);
 
         this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
             WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
         mRenderer = new FroyVisualsRenderer(this);
 
         mView = new FroyVisualsView(this);
 
         mView.setRenderer(mRenderer);
 
         setContentView(mView);
 
     }
 
     @Override
     public void onPause()
     {
         super.onPause();
         mView.onPause();
     }
 
     @Override
     public void onResume()
     {
         super.onResume();
         mView.onResume();
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
 
 /*
             case R.id.about_plugins:
             {
                 startActivity(new Intent(this, AboutPluginsActivity.class));
                 return true;
             }
 */
             case R.id.close_app:
             {
                 mNativeHelper.visualsQuit();
                 return true;
             }
             case R.id.input_stub:
             {
/*
                 int index = mNativeHelper.cycleInput(1);
 
                 String input = mNativeHelper.inputGetName(index);
 
                 if(input == "mic")
                 {
                     if(!enableMic())
                        input = mNativeHelper.cycleInput(1);
                 } else {
                     mMicActive = false;
                 }
 
                 mTextDisplay = "Choosing input plugin: " + mNativeHelper.inputGetLongName(index);
 
                 new CountDownTimer(3000, 1000) {
                     public void onTick(long millisUntilFinished) {
                     }
     
                     public void onFinish() {
                         mTextDisplay = null;
                     }
                 }.start();
*/
 
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
 
     private boolean enableMic()
     {
         mAudio = findAudioRecord();
         if(mAudio != null)
         {
             mNativeHelper.resizePCM(PCM_SIZE, RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
             new Thread(new Runnable() {
                 public void run() {
                     mMicActive = true;
                     mAudio.startRecording();
                     while(mMicActive)
                     {
                         short[] data = new short[PCM_SIZE];
                         mAudio.read(data, 0, PCM_SIZE);
                         mNativeHelper.uploadAudio(data);
                     }
                     mAudio.stop();
                 }
             }).start();
             return true;
         }
         return false;
     }
 
     private static int[] mSampleRates = new int[] { 8000, 11025, 22050, 44100 };
     public AudioRecord findAudioRecord() {
         for (int rate : mSampleRates) {
             for (short audioFormat : new short[] { AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT }) {
                 for (short channelConfig : new short[] { AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO }) {
                     try {
                         Log.d(TAG, "Attempting rate " + rate + "Hz, bits: " + audioFormat + ", channel: "
                                 + channelConfig);
                         int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);
     
                         if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                             // check if we can instantiate and have a success
                             AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, rate, channelConfig, audioFormat, bufferSize);
     
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
                         Log.e(TAG, rate + " Exception, keep trying.",e);
                     }
                 }
             }
         }
         return null;
     }
 
 
 }
 
 
