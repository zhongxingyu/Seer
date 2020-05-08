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
 import android.view.View;
 import android.view.MotionEvent;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.media.AudioRecord;
 import android.media.AudioFormat;
 import android.media.MediaRecorder;
 import android.util.Log;
 
 public class FroyVisuals extends Activity
 {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(new FroyVisualsView(this));
     }
 
     /* load our native library */
     static {
         System.loadLibrary("froyvisuals");
     }
 }
 
 class FroyVisualsView extends View {
     private Bitmap mBitmap;
     private AudioRecord mAudio;
     private int mH, mW;
     private boolean isAvailable;
     private int PCM_SIZE;
     private static int RECORDER_SAMPLERATE = 44100;
     private static int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
     private static int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
     private static final String APP_TAG = "FroyVisuals";
 
     private static native boolean renderFroyVisuals(Bitmap  bitmap);
     private static native void resizePCM(int size, int rate, int channels, int encoding);
     private static native void uploadAudio(short[] data);
     private static native void initApp();
     private static native void switchActor(int direction);
     private static native void mouseMotion(float x, float y);
     private static native void mouseButton(int button, float x, float y);
     private static native void screenResize(int w, int h);
 
 /*
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
 */
     
     //AudioRecord recorder = findAudioRecord();
     public FroyVisualsView(Context context) {
         super(context);
 
         mW = -1;
         mH = -1;
         isAvailable = false;
 
         initApp();
 /*
     mAudio = findAudioRecord();
         if(mAudio != null)
     {
         resizePCM(PCM_SIZE, RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
         isAvailable = true;
     }
 */
     }
 
     @Override protected void onDraw(Canvas canvas) {
     if( mW != getWidth() || mH != getHeight())
     {
         mW = getWidth();
         mH = getHeight();
         mBitmap = Bitmap.createBitmap(mW, mH, Bitmap.Config.RGB_565);
     }
 /*
     mAudio.startRecording();
     short[] data = new short[PCM_SIZE];
         mAudio.read(data, 0, PCM_SIZE);
     mAudio.stop();
         uploadAudio(data);
 */
         if(!renderFroyVisuals(mBitmap)) return;
 
         canvas.drawBitmap(mBitmap, 0, 0, null);
         // force a redraw
         invalidate();
     }
 
     @Override public boolean onTouchEvent (MotionEvent event) {
         int action = event.getAction();
         float x = event.getX();
         float y = event.getY();
         switch(action)
         {
             case MotionEvent.ACTION_DOWN:
                 switchActor(0);
                 mouseButton(1, x, y);
             break;
             case MotionEvent.ACTION_MOVE:
                 mouseMotion(x, y);
             break;
         }
         return true;    
     }
 }
