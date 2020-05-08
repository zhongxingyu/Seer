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
 import android.os.Looper;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.content.SharedPreferences;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.ViewGroup;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.MotionEvent;
 import android.view.Display;
 import android.view.Surface;
 import android.view.View.OnTouchListener;
 import android.view.View.OnClickListener;
 import android.view.GestureDetector;
 import android.view.GestureDetector.SimpleOnGestureListener;
 import android.view.ViewConfiguration;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Typeface;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Matrix;
 import android.media.AudioRecord;
 import android.media.AudioFormat;
 import android.media.MediaRecorder;
 import android.util.Log;
 import android.util.TypedValue;
 import java.util.Timer;
 import java.util.TimerTask;
 
 
 public class FroyVisuals extends Activity implements OnClickListener
 {
     private final static String TAG = "FroyVisuals/FroyVisualsActivity";
     private final static String PREFS = "FroyVisualsPrefs";
     private static Settings mSettings;
     private NativeHelper mNativeHelper;
     private AudioRecord mAudio;
     private MediaRecorder mRecorder;
     private boolean mMicActive = false;
     private int PCM_SIZE = 1024;
     private static int RECORDER_SAMPLERATE = 44100;
     private static int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
     private static int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
     private boolean mDoMorph;
     private String mMorph;
     private String mInput;
     private String mActor;
 
     static private String mDisplayText = null;
 
     private static int SWIPE_MIN_DISTANCE = 120;
     private static int SWIPE_MAX_OFF_PATH = 250;
     private static int SWIPE_THRESHOLD_VELOCITY = 200;
     private GestureDetector gestureDetector;
     OnTouchListener gestureListener;
 
     private FroyVisualsView mView;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle state)
     {
         super.onCreate(state);
 
         mSettings = new Settings(this);
 
         this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
             WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
         mView = new FroyVisualsView(this);
 
         final ViewConfiguration vc = ViewConfiguration.get((Context)this);
 
         SWIPE_MIN_DISTANCE = vc.getScaledTouchSlop();
         SWIPE_THRESHOLD_VELOCITY = vc.getScaledMinimumFlingVelocity();
         SWIPE_MAX_OFF_PATH = vc.getScaledMaximumFlingVelocity();
 
         class MyGestureDetector extends SimpleOnGestureListener {
         @Override
         public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
             try {
                 if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                     return false;
                 if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && 
                     Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                     Log.w(TAG, "Left swipe...");
                     mNativeHelper.finalizeSwitch(1);
                     // Left swipe
                 }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && 
                     Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                     Log.w(TAG, "Right swipe...");
                     mNativeHelper.finalizeSwitch(0);
                     // Right swipe
                 }
             } catch (Exception e) {
                 Log.w(TAG, "Failure in onFling");
                 // nothing
             }
             return false;
         }
         }
         // Gesture detection
         gestureDetector = new GestureDetector(new MyGestureDetector());
         gestureListener = new View.OnTouchListener() {
             public boolean onTouch(View v, MotionEvent event) {
                 return gestureDetector.onTouchEvent(event);
             }
         };
         mView.setOnClickListener(FroyVisuals.this);
         mView.setOnTouchListener(gestureListener);
 
         setContentView(mView);
 
     }
 
     public void onClick(View v) {
 /*
         Filter f = (Filter) v.getTag();
         FilterFullscreenActivity.show(this, input, f);
 */
     }
 
     public void onResume()
     {
         super.onResume();
         SharedPreferences settings = getSharedPreferences(PREFS, 0);
 
         mDoMorph = settings.getBoolean("doMorph", true);
 
         mMorph = settings.getString("currentMorph", "alphablend");
         mInput = settings.getString("currentInput", "dummy");
         mActor = settings.getString("currentActor", "jakdaw");
 
         NativeHelper.morphSetCurrentByName(mMorph);
         NativeHelper.inputSetCurrentByName(mInput);
         NativeHelper.actorSetCurrentByName(mActor);
     }
 
     public void onStop()
     {
         super.onStop();
 
         SharedPreferences settings = getSharedPreferences(PREFS, 0);
         SharedPreferences.Editor editor = settings.edit();
 
         int morph = mNativeHelper.morphGetCurrent();
         int input = mNativeHelper.inputGetCurrent();
         int actor = mNativeHelper.actorGetCurrent();
 
         this.setMorph(mNativeHelper.morphGetName(morph));
         this.setInput(mNativeHelper.inputGetName(input));
         this.setActor(mNativeHelper.actorGetName(morph));
 
         editor.putString("currentMorph", mMorph);
         editor.putString("currentInput", mInput);
         editor.putString("currentActor", mActor);
 
         editor.putBoolean("doMorph", mDoMorph);
 
         //Commit edits
         editor.commit();
 
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
                 int index = mNativeHelper.cycleInput(1);
 
                 String input = mNativeHelper.inputGetName(index);
 
                 if(input == "mic")
                 {
                     if(!enableMic())
                         index = mNativeHelper.cycleInput(1);
                 } else {
                     mMicActive = false;
                 }
 
                 warn(mNativeHelper.inputGetLongName(index), true);
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
         System.loadLibrary("main");
     }
 
     /* Get the current morph plugin name */
     public String getMorph()
     {
         return mMorph;
     }
 
     /* Get the current input plugin name */
     public String getInput()
     {
         return mInput;
     }
 
     /* Get the current actor plugin name */
     public String getActor()
     {
         return mActor;
     }
 
     /* Set the current morph plugin name */
     public void setMorph(String morph)
     {
         mMorph = morph;
     }
 
     /* Set the current input plugin name */
     public void setInput(String input)
     {
         mInput = input;
     }
 
     /* Set the current actor plugin name */
     public void setActor(String actor)
     {
         mActor = actor;
     }
 
 
     /* Set whether to morph or not */
     public void setDoMorph(boolean doMorph)
     {
         mDoMorph = doMorph;
     }
 
     /* Get whether to morph or not */
     public boolean getDoMorph()
     {
         return mDoMorph;
     }
 
     /* Display a warning text: provide text, time in milliseconds, and priority */
     private long mLastRefresh = 0l;
     private boolean mWarn = false;
     public boolean warn(String text, int millis, boolean priority)
     {
         long now = System.currentTimeMillis();
 
         if(mWarn && (now - mLastRefresh) < millis && !priority) 
             return false;
 
         mDisplayText = text;
 
         mLastRefresh = now;
 
         mWarn = true;
 
         return true;
     }
 
     /* Display warning: provide text. */
     public boolean warn(String text)
     {
         return warn(text, 2000, false);
     }
 
     /* Display warning: provide text and priority */
     public boolean warn(String text, boolean priority)
     {
         return warn(text, 2000, priority);
     }
 
     public String getDisplayText()
     {
         return mDisplayText;
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
 
 class FroyVisualsView extends View {
     private final String TAG = "FroyVisuals/FroyVisualsView";
     private Bitmap mBitmap;
     private NativeHelper mNativeHelper;
     private FroyVisuals mActivity;
     private Stats mStats;
     private final int WIDTH = 256;
     private final int HEIGHT = 256;
     private Paint mPaint;
     private Matrix mMatrix;
     private Display mDisplay;
 
     public FroyVisualsView(Context context) {
         super(context);
 
        Log.e(TAG, "FroyVisualsVIew constructor");

         mActivity = (FroyVisuals)context;
 
         mPaint = new Paint();
         mPaint.setAntiAlias(true);
         mPaint.setTextSize(30);
         mPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.ITALIC));
         mPaint.setStyle(Paint.Style.STROKE);
         mPaint.setStrokeWidth(1);
         mPaint.setColor(Color.WHITE);
 
         mNativeHelper.initApp(WIDTH, HEIGHT, 0, 0);
 
         mStats = new Stats();
         mStats.statsInit();
 
         final int delay = 100;
         final int period = 300;
 
         final Timer timer = new Timer();
 
         TimerTask task = new TimerTask() {
             public void run() {
                 mActivity.warn(mStats.getText());
             }
         };
 
         timer.scheduleAtFixedRate(task, delay, period);
 
         mBitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.RGB_565);
 
         mDisplay = ((WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
 
 
     }
 
     @Override protected void onSizeChanged(int w, int h, int oldw, int oldh)
     {
         mMatrix = new Matrix();
         mMatrix.setScale(w/(float)WIDTH, h/(float)HEIGHT);
     }
 
     @Override protected void onDraw(Canvas canvas) 
     {
         mStats.startFrame();
 
         // Render frame to bitmap
         mNativeHelper.render(mBitmap);
 
         // Scale bitmap across canvas.
         canvas.drawBitmap(mBitmap, mMatrix, mPaint);
 
         // Do we have text to show?
         String text = mActivity.getDisplayText();
 
         if(text != null)
         {
             float canvasWidth = getWidth();
             float textWidth = mPaint.measureText(text);
             float startPositionX = (canvasWidth - textWidth / 2) / 2;
     
             canvas.drawText(text, startPositionX, getHeight()-50, mPaint);
         }
 
         invalidate();
 
         mStats.endFrame();
     }
 
 /*
     private int direction = -1;
     private float mX = 0.0f;
     private float mY = 0.0f;
     private int size = 0;
     @Override public boolean onTouchEvent (MotionEvent event) 
     {
         int action = event.getAction();
         float x = event.getX();
         float y = event.getY();
         switch(action)
         {
             case MotionEvent.ACTION_DOWN:
                 direction = -1;
                 size = 0;
             break;
             case MotionEvent.ACTION_UP:
                 if(direction >= 0) {
                     mNativeHelper.finalizeSwitch(direction);
                 }
             break;
             case MotionEvent.ACTION_MOVE:
                 mNativeHelper.mouseMotion(x, y);
                 size = size + 1;
                 if(size > 2)
                 {
                     if(mX < x)
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
         mX = x;
         mY = y;
         return true;    
     }
 */
 }
 
 
