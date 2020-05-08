 package com.starlon.starvisuals;
 
 import android.app.ActivityManager;
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.ParcelFileDescriptor;
 import android.content.Context;
 import android.content.ContentUris;
 import android.content.res.Configuration;
 import android.content.SharedPreferences;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.BroadcastReceiver;
 import android.content.ContentResolver;
 import android.content.pm.ConfigurationInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.MotionEvent;
 import android.view.View.OnTouchListener;
 import android.view.View.OnClickListener;
 import android.view.GestureDetector;
 import android.view.GestureDetector.SimpleOnGestureListener;
 import android.view.ViewConfiguration;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.media.MediaRecorder;
 import android.media.AudioRecord;
 import android.media.AudioFormat;
 import android.util.Log;
 import android.net.Uri;
 import android.database.Cursor;
 import android.provider.MediaStore;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map.Entry;
 import java.io.FileOutputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.io.FileDescriptor;
 import java.io.OutputStreamWriter;
 import java.io.IOException;
 import java.lang.Process;
 
 import com.openglesbook.particlesystem.ParticleSystemRenderer;
 
 import org.libvisual.android.VisualObject;
import com.starlon.libscriptable.UtilsEvaluator;
 
 public class StarVisuals extends Activity implements OnClickListener, OnSharedPreferenceChangeListener
 {
     private final static String TAG = "StarVisuals/StarVisualsActivity";
     private final static String PREFS = "StarVisualsPrefs";
     private final static int WIDTH = 128;
     private final static int HEIGHT = 128;
     private final static int ARTWIDTH = 100;
     private final static int ARTHEIGHT = 100;
     private AudioRecord mAudio = null;
     private boolean mMicActive = false;
     private int PCM_SIZE = 1024;
     private static int RECORDER_SAMPLERATE = 44100;
     private static int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
     private static int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
 
     private final String MORPH = "checkers";
     private final String INPUT = "dummy";
     private final String ACTOR = "lv_analyzer";
     private final boolean DOBEAT = false;
     private final boolean DOSWAP = true;
     private final boolean DOMORPH = true;
     private final int MORPHSTEPS = 8;
     private final int MAXFPS = 40;
     private final boolean SHOWFPS = true;
     private final boolean SHOWART = true;
     private final boolean SHOWTEXT = true;
     private final boolean ISACTIVE = false;
     private final int MINBEAT = 0;
     private final boolean STUCKBEAT = false;
     private final int BEATHOLD = 10;
     private final String DISPLAYTEXT = "Please wait...";
     private final boolean USEGL = true;
 
     public String mMorph = MORPH;
     public String mInput = INPUT;
     public String mActor = ACTOR;
     public boolean mDoBeat = DOBEAT;
     public boolean mDoSwap = DOSWAP;
     public boolean mDoMorph = DOMORPH;
     public int mMorphSteps = MORPHSTEPS;
     public int mMaxFPS = MAXFPS;
     public boolean mShowFPS = SHOWFPS;
     public boolean mShowArt = SHOWART;
     public boolean mShowText = SHOWTEXT;
     public boolean mIsActive = ISACTIVE;
     public boolean mUseGL = USEGL;
     public int mWidth = WIDTH;
     public int mHeight = HEIGHT;
     public String mDisplayText = DISPLAYTEXT;
     public short mMicData[] = null;
 
     public boolean mStuckBeat = STUCKBEAT;
     public int mMinBeat = MINBEAT;
     public int mBeatHold = BEATHOLD;
 
     private String mSongAction = null;
     public String mSongCommand = null;
     public String mSongArtist = null;
     public String mSongAlbum = null;
     public String mSongTrack = null;
     public long mSongChanged = 0l;
     public Bitmap mAlbumArt = null;
     public IntentFilter mIntentFilter = null;
     public boolean mHasRoot = false;
     private Thread mAudioThread = null;
     public HashMap<String, Bitmap> mAlbumMap = new HashMap<String, Bitmap>();
     private SharedPreferences mPrefs;
     private SharedPreferences.Editor mEditor;
     private StarVisualsRenderer mRendererGLVis;
     private ParticleSystemRenderer mRendererGLPS;
 
     private static int SWIPE_MIN_DISTANCE = 120;
     private static int SWIPE_MAX_OFF_PATH = 250;
     private static int SWIPE_THRESHOLD_VELOCITY = 200;
     private GestureDetector mGestureDetector = null;
     OnTouchListener mGestureListener;
 
     private StarVisualsView mView = null;
     private StarVisualsViewGL mViewGL = null;
 
     private Stats mStats = new Stats();
     private VisualObject mVisualObject;
    private UtilsEvaluator mEvaluator;
 
     public VisualObject getVisualObject()
     {
         return mVisualObject;
     }
 
     private void makeFile(String file, int id)
     {
         InputStream inputStream = getResources().openRawResource(id);
      
         FileOutputStream outputStream = null;
         try {
             outputStream = new FileOutputStream(file);
         } catch (FileNotFoundException e) {
             e.printStackTrace();
             return;
         }
         
      
         int i;
         try {
             i = inputStream.read();
             while (i != -1)
             {
                 outputStream.write(i);
                 i = inputStream.read();
             }
             inputStream.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     public void keepScreenOn(boolean truth)
     {
         mView.setKeepScreenOn(truth);
         mViewGL.setKeepScreenOn(truth);
     }
 
     private boolean detectGL20()
     {
         ActivityManager am =
             (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
         ConfigurationInfo info = am.getDeviceConfigurationInfo();
         return (info.reqGlEsVersion >= 0x20000 && mUseGL);
     }
 
     public void getPrefs()
     {
         mActor = mPrefs.getString("prefs_actor_selection", ACTOR);
         mInput = mPrefs.getString("prefs_input_selection", INPUT);
         mMorph = mPrefs.getString("prefs_morph_selection", MORPH);
         mDoMorph = mPrefs.getBoolean("prefs_morph_enabled", DOMORPH);
         mDoBeat = mPrefs.getBoolean("prefs_do_beat", DOBEAT);
         mDoSwap = mPrefs.getBoolean("prefs_do_swap", DOSWAP);
         mDoMorph = mPrefs.getBoolean("prefs_do_morph", DOMORPH);
         mMorphSteps = mPrefs.getInt("prefs_morph_steps", MORPHSTEPS);
         mMaxFPS = mPrefs.getInt("prefs_max_fps", MAXFPS);
         mShowFPS = mPrefs.getBoolean("prefs_show_fps", SHOWFPS);
         mShowArt = mPrefs.getBoolean("prefs_show_art", SHOWART);
         mShowText = mPrefs.getBoolean("prefs_show_text", SHOWTEXT);
         mIsActive = mPrefs.getBoolean("prefs_is_active", ISACTIVE);
         mUseGL = mPrefs.getBoolean("prefs_use_gl", USEGL);
     }
 
     public void setPrefs()
     {
         mEditor.putString("prefs_actor_selection", mActor);
         mEditor.putString("prefs_input_selection", mInput);
         mEditor.putString("prefs_morph_selection", mMorph);
         mEditor.putBoolean("prefs_do_beat", mDoBeat);
         mEditor.putBoolean("prefs_do_swap", mDoSwap);
         mEditor.putBoolean("prefs_do_morph", mDoMorph);
         mEditor.putBoolean("prefs_do_beat", mDoBeat);
         mEditor.putInt("prefs_morph_steps", mMorphSteps);
         mEditor.putInt("prefs_max_fps", mMaxFPS);
         mEditor.putBoolean("prefs_show_fps", mShowFPS);
         mEditor.putBoolean("prefs_show_art", mShowArt);
         mEditor.putBoolean("prefs_show_text", mShowText);
         mEditor.putBoolean("prefs_is_active", mIsActive);
         mEditor.putBoolean("prefs_use_gl", mUseGL);
 
         mEditor.commit();
     }
 
     public void resetPrefs()
     {
         mEditor.putString("prefs_actor_selection", ACTOR);
         mEditor.putString("prefs_input_selection", INPUT);
         mEditor.putString("prefs_morph_selection", MORPH);
         mEditor.putBoolean("prefs_do_beat", DOBEAT);
         mEditor.putBoolean("prefs_do_swap", DOSWAP);
         mEditor.putBoolean("prefs_do_morph", DOMORPH);
         mEditor.putBoolean("prefs_do_beat", DOBEAT);
         mEditor.putInt("prefs_morph_steps", MORPHSTEPS);
         mEditor.putInt("prefs_max_fps", MAXFPS);
         mEditor.putBoolean("prefs_show_fps", SHOWFPS);
         mEditor.putBoolean("prefs_show_art", SHOWART);
         mEditor.putBoolean("prefs_show_text", SHOWTEXT);
         mEditor.putBoolean("prefs_is_active", ISACTIVE);
         mEditor.putBoolean("prefs_use_gl", USEGL);
 
         mEditor.commit();
     }
 
     /** Called when the activity is first created. */
     @Override
     protected void onCreate(Bundle state)
     {
         super.onCreate(state);
         makeFile("/data/data/com.starlon.starvisuals/libstub.lua", R.raw.libstub);
         makeFile("/data/data/com.starlon.starvisuals/pluginmath.lua", R.raw.pluginmath);
 
         this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
             WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 
        mEvaluator = new UtilsEvaluator();

         mPrefs = getSharedPreferences(PREFS, 0);
         mPrefs.registerOnSharedPreferenceChangeListener(this);
 
         mEditor = mPrefs.edit();
 
 
         final ViewConfiguration vc = ViewConfiguration.get((Context)this);
 
         SWIPE_MIN_DISTANCE = vc.getScaledTouchSlop();
         SWIPE_THRESHOLD_VELOCITY = vc.getScaledMinimumFlingVelocity();
         SWIPE_MAX_OFF_PATH = vc.getScaledMaximumFlingVelocity();
 
         class MyGestureDetector extends SimpleOnGestureListener {
             @Override
             public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                 synchronized(mView.mSynch)
                 {
                     int actor = -1;
                     try {
                         if(velocityX > velocityY)
                         {
                             if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                                 return false;
                             if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && 
                                     Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                                 // Left swipe
                                 Log.w(TAG, "Left swipe...");
                                 NativeHelper.finalizeSwitch(-1);
                                 actor = NativeHelper.actorGetCurrent();
                             }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && 
                                     Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                                 // Right swipe
                                 Log.w(TAG, "Right swipe...");
                                 NativeHelper.finalizeSwitch(1);
                                 actor = NativeHelper.actorGetCurrent();
                             }
                         } else {
                             if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_PATH)
                                 return false;
                             if(actor == -1 && e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE &&
                                 Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                                 // Up swipe
                                 Log.w(TAG, "Up swipe...");
                                 NativeHelper.finalizeSwitch( -2 );
                                 actor = NativeHelper.actorGetCurrent();
                             } else if (actor == -1 && e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE &&
                                 Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                                 // Down swipe
 
                                 Log.w(TAG, "Down swipe...");
                                 NativeHelper.finalizeSwitch( 2 );
                                 actor = NativeHelper.actorGetCurrent();
                             }
                         }
                         if(actor >= 0)
                         {
                             mActor = NativeHelper.actorGetName(actor);
                             mEditor.putString("prefs_actor_selection", mActor);
                             mEditor.commit();
                         }
                     } catch (Exception e) {
                         Log.w(TAG, "Failure in onFling: " + e.toString());
                         // nothing
                     }
                 } //mView
                 return false;
             }
         }
 
         // Gesture detection
         mGestureDetector = new GestureDetector(new MyGestureDetector());
         mGestureListener = new View.OnTouchListener() {
             public boolean onTouch(View v, MotionEvent event) {
                 return mGestureDetector.onTouchEvent(event);
             }
         };
 
         mIntentFilter = new IntentFilter();
         mIntentFilter.addAction("com.android.music.metachanged");
         mIntentFilter.addAction("com.android.music.playstatechanged");
         mIntentFilter.addAction("com.android.music.playbackcomplete");
         mIntentFilter.addAction("com.android.music.queuechanged");
 
         registerReceiver(mReceiver, mIntentFilter);
 
     }
 
     public void onSharedPreferenceChanged(SharedPreferences prefs, String key) 
     {
 
         if(key.equals("prefs_actor_selection"))
         {
             mActor = mPrefs.getString(key, ACTOR);
             NativeHelper.actorSetCurrentByName(mActor, true);
 
         }
         else if(key.equals("prefs_input_selection"))
         {
             mInput = mPrefs.getString(key, INPUT);
             NativeHelper.inputSetCurrentByName(mInput, true);
             enableMic(mInput);
         } 
         else if(key.equals("prefs_morph_selection"))
         {
             mMorph = mPrefs.getString(key, MORPH);
             NativeHelper.morphSetCurrentByName(mInput, true);
         } 
         else if(key.equals("prefs_do_beat"))
         {
             mDoBeat = mPrefs.getBoolean(key, DOBEAT);
         }        
         else if(key.equals("prefs_do_morph"))
         {
             mDoMorph = mPrefs.getBoolean(key, DOMORPH);
             NativeHelper.setMorphStyle(mDoMorph);
         }        
         else if(key.equals("prefs_morph_steps"))
         {
             mMorphSteps = mPrefs.getInt(key, MORPHSTEPS);
             NativeHelper.setMorphSteps(mMorphSteps);
         }        
         else if(key.equals("prefs_max_fps"))
         {
             mMaxFPS = mPrefs.getInt(key, MAXFPS);
         }        
         else if(key.equals("prefs_show_fps"))
         {
             mShowFPS = mPrefs.getBoolean(key, SHOWFPS);
         }        
         else if(key.equals("prefs_show_art"))
         {
             mShowArt = mPrefs.getBoolean(key, SHOWART);
         }        
         else if(key.equals("prefs_show_text"))
         {
             mShowText = mPrefs.getBoolean(key, SHOWTEXT);
         }        
         else if(key.equals("prefs_is_active"))
         {
             mIsActive = mPrefs.getBoolean(key, ISACTIVE);
         }        
         else if(key.equals("prefs_display_text"))
         {
             mDisplayText = mPrefs.getString(key, DISPLAYTEXT);
         }        
     }
 
     public void onClick(View v)
     {
 
     }
 
     public void setUseGL(boolean truth)
     {
             if(truth)
             {
                 mView.stopThread();
                 setContentView(mViewGL);
                 //mViewGL.startThread();
             }
             else
             {
                 //mViewGL.stopThread();
                 setContentView(mView);
                 mView.startThread();
             }
             mUseGL = truth;
     }
 
     public void setPlugins(boolean now)
     {
         mActor = mPrefs.getString("prefs_actor_selection", ACTOR);
         NativeHelper.actorSetCurrentByName(mActor, now);
         mInput = mPrefs.getString("prefs_input_selection", INPUT);
         NativeHelper.inputSetCurrentByName(mInput, now);
         mMorph = mPrefs.getString("prefs_morph_selection", MORPH);
         NativeHelper.morphSetCurrentByName(mInput, now);
         mDoMorph = mPrefs.getBoolean("prefs_morph_enabled", DOMORPH);
         NativeHelper.setMorphStyle(mDoMorph);
         enableMic(mInput);
         NativeHelper.finalizeSwitch(0);
     }
 
     public BroadcastReceiver mReceiver = new BroadcastReceiver() {
  
         @Override
         public void onReceive(Context context, Intent intent)
         {
             // intent.getAction() returns one of the following:
             // com.android.music.metachanged - new track has started
             // com.android.music.playstatechanged - playback queue has changed
             // com.android.music.playbackcomplete - playback has stopped, last file played
             // com.android.music.queuechanged - play-state has changed (pause/resume)
             String action = intent.getAction();
 
 
             if(action.equals("com.android.music.metachanged"))
             {
                 mSongCommand = intent.getStringExtra("command");
                 long id = intent.getLongExtra("id", -1);
                 mSongArtist = intent.getStringExtra("artist");
                 mSongAlbum = intent.getStringExtra("album");
                 mSongTrack = intent.getStringExtra("track");
                 mSongChanged = System.currentTimeMillis();
                 mAlbumArt = mAlbumMap.get(mSongAlbum);
                 NativeHelper.newSong();
                 warn("(" + mSongTrack + ")", 5000, true);
             }
             else if(action.equals("com.android.music.playbackcomplete"))
             {
                 mSongCommand = null;
                 mSongArtist = null;
                 mSongAlbum = null;
                 mSongTrack = null;
                 mSongChanged = 0l;
                 mAlbumArt = null;
                 NativeHelper.newSong();
                 warn("Ended playback...", true);
             }
         }
     };
 
     // This series of on<Action>() methods a flow chart are outlined here:
     // http://developer.android.com/reference/android/app/Activity.html
 
     // User returns to activity
     @Override
     public void onResume() 
     {
 
         super.onResume();
 
         if(detectGL20())
             mViewGL.onResume();
 
     }
 
     // follows onCreate() and onResume()
     @Override
     protected void onStart() 
     {   
         super.onStart();
 
         //mVisualObject = new VisualObject(mWidth, mHeight, mActor, mInput, mMorph);
 
         mRendererGLVis = new StarVisualsRenderer(this);
 
         mViewGL = new StarVisualsViewGL(this);
 
         mViewGL.setRenderer(mRendererGLVis);
 
         mView = new StarVisualsView(this);
 
         keepScreenOn(true);
 
         if(detectGL20())
         {
             mViewGL.setOnClickListener(StarVisuals.this);
             mViewGL.setOnTouchListener(mGestureListener);
         }
         else
         {
             mView.setOnClickListener(StarVisuals.this);
             mView.setOnTouchListener(mGestureListener);
         }
 
         enableMic(mInput);
 
         getAlbumArt();
 
         setUseGL(mUseGL);
 
         registerReceiver(mReceiver, mIntentFilter);
 
     }
 
     // another activity comes to foreground
     @Override
     protected void onPause() 
     {
         super.onPause();
 
         if(detectGL20())
             mViewGL.onPause();
 
         releaseAlbumArt();
 
         mEditor.putString("prefs_actor_selection", mActor);
         mEditor.putString("prefs_input_selection", mInput);
         mEditor.putString("prefs_morph_selection", mMorph);
 
         mEditor.commit();
 
     }
 
     // user navigates back to the activity. onRestart() -> onStart() -> onResume()
     @Override
     protected void onRestart() 
     {
         super.onRestart();
     }
 
     // This activity is no longer visible
     @Override
     protected void onStop()
     {
         super.onStop();
 
         disableMic();
 
         unregisterReceiver(mReceiver);
 
 
     }
 
     // Last method before shut down. Clean up LibVisual from here.
     @Override 
     protected void onDestroy()
     {
         super.onDestroy();
     }
 
     // Create options menu.
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.starvisuals, menu);
         return true;
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig)
     {
         super.onConfigurationChanged(newConfig);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         switch(item.getItemId())
         {
             case R.id.menu_about:
             {
                 startActivity(new Intent(this, AboutActivity.class));
                 return true;
             }
 
             case R.id.menu_about_plugins:
             {
                 startActivity(new Intent(this, AboutPluginsActivity.class));
                 return true;
             }
 
             case R.id.menu_close_app:
             {
                 synchronized(mView.mSynch)
                 {
                     NativeHelper.visualsQuit();
                     this.finish();
                 }
                 return true;
             }
             case R.id.menu_edit_settings:
             {
                 startActivity(new Intent(this, EditSettingsActivity.class));
                 return true;
             }
 /*
             case R.id.menu_use_gl:
             {
                 setUseGL(!mUseGL);
                 return true;
             }
 */
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
         System.loadLibrary("gnustl_shared");
         System.loadLibrary("visual");
         System.loadLibrary("common");
         System.loadLibrary("lvclient");
        System.loadLibrary("luascript");
        System.loadLibrary("scriptable");
         System.loadLibrary("main");
     }
 
     // Check for root privelege. This causes issues on rigid's phone. Find out why.
     public boolean checkRoot()
     {
         try {
             
             Process exec = Runtime.getRuntime().exec(new String[]{"su"});
     
             final OutputStreamWriter out = new OutputStreamWriter(exec.getOutputStream());
             out.write("\nexit\n");
             out.flush();
             Log.i(TAG, "Superuser detected...");
             return true; 
 
         } catch (IOException e)
         {
             e.printStackTrace();
         }
         Log.i(TAG, "Root not detected...");
         return false;
     }
 
     public String _S(int id)
     {
         return getResources().getText(id).toString();
     }
     public int _I(int id)
     {
         return getResources().getInteger(id);
     }
     public boolean _B(int id)
     {
         String bool = getResources().getString(id).toString();
         if(bool.equals("true"))
             return true;
         return false;
     }
 
 /// GETTERS
 
     /* Get the current morph plugin name */
     public String getMorph()
     {
         mMorph = mPrefs.getString("prefs_morph_selection", MORPH);
         return mMorph;
     }
 
     /* Get the current input plugin name */
     public String getInput()
     {
         mInput = mPrefs.getString("prefs_input_selection", INPUT);
         return mInput;
     }
 
     /* Get the current actor plugin name */
     public String getActor()
     {
         mActor = mPrefs.getString("prefs_actor_selection", ACTOR);
         return mActor;
     }
 
 
     double do_beat_then = 0.0;
     /* Get whether we process beats or not. */
     public boolean getDoBeat()
     {
         double do_beat_now;
 
         do_beat_now = mStats.nowMil();
         if(do_beat_now - do_beat_then > 1000)
         {
             mDoBeat = mPrefs.getBoolean("prefs_do_beat", _B(R.string.prefs_defaults_do_beat));
             do_beat_then = do_beat_now;
         }
         return mDoBeat;
     }
 
     /* Get whether we do endian swaps. */
     double swap_then = 0.0;
     public boolean getDoSwap()
     {
         double swap_now;
 
         swap_now = mStats.nowMil();
         if(swap_now - swap_then > 1000)
         {
             mDoSwap = mPrefs.getBoolean("prefs_do_swap", _B(R.string.prefs_defaults_do_swap));
             swap_then = swap_now;
         }
         return mDoSwap;
     }
 
     double morph_then = 0.0;
     /* Get whether we do morphs. */
     public boolean getDoMorph()
     {
         double morph_now;
 
         morph_now = mStats.nowMil();
         if(morph_now - morph_then > 1000)
         {
             mDoMorph = mPrefs.getBoolean("prefs_do_morph", _B(R.string.prefs_defaults_do_morph));
             morph_then = morph_now;
         }
         return mDoMorph;
 
     }
 
     double morph_steps_then = 0.0;
     /* How many steps we take to morph. */
     public int getMorphSteps()
     {
         double morph_steps_now;
 
         morph_steps_now = mStats.nowMil();
         if(morph_steps_now - morph_steps_then > 1000)
         {
             mMorphSteps = mPrefs.getInt("prefs_morph_steps", _I(R.integer.prefs_defaults_morph_steps));;
             morph_steps_then = morph_steps_now;
         }
         return mMorphSteps;
     }
         
     double max_fps_then = 0.0;
     /* Get Max FPS setting. */
     public int getMaxFPS()
     {
         double max_fps_now;
 
         max_fps_now = mStats.nowMil();
         if(max_fps_now - max_fps_then > 1000)
         {
             mMaxFPS = mPrefs.getInt("prefs_max_fps", _I(R.integer.prefs_defaults_max_fps));
             max_fps_then = max_fps_now;
         }
         return mMaxFPS;
     }
 
     double show_fps_then = 0.0;
     /* Get whether to show fps or not. */
     public boolean getShowFPS()
     {
         double show_fps_now;
 
         show_fps_now = mStats.nowMil();
         if(show_fps_now - show_fps_then > 1000)
         {
             mShowFPS = mPrefs.getBoolean("prefs_show_fps", _B(R.string.prefs_defaults_show_fps));
             show_fps_then = show_fps_now;
         }
         return mShowFPS;
     }
 
     double show_art_then = 0.0;
     /* Get whether to show art or not. */
     public boolean getShowArt()
     {
         double show_art_now;
 
         show_art_now = mStats.nowMil();
         if(show_art_now - show_art_then > 1000)
         {
             mShowArt = mPrefs.getBoolean("prefs_show_art", _B(R.string.prefs_defaults_show_art));
             show_art_then = show_art_now;
         }
         return mShowArt;
     }
 
     double show_text_then = 0.0;
     /* Whether to show text or not. */
     public boolean getShowText()
     {
         double show_text_now;
 
         show_text_now = mStats.nowMil();
         if(show_text_now - show_text_then > 1000)
         {
             mShowText = mPrefs.getBoolean("prefs_show_text", _B(R.string.prefs_defaults_show_text));
             show_text_then = show_text_now;
         }
         return mShowText;
     }
 
     double is_active_then = 0.0;
     /* Whether the app is active or not. */
     public boolean getIsActive()
     {
         return mIsActive;
     }
 
     /* Get the text that should show. */
     public String getDisplayText()
     {
         return mDisplayText;
     }
 
     double stuck_beat_then = 0.0;
     public boolean getStuckBeat()
     {
         double stuck_beat_now;
 
         stuck_beat_now = mStats.nowMil();
         if(stuck_beat_now - stuck_beat_then > 1000)
         {
             mStuckBeat = mPrefs.getBoolean("prefs_stuck_beat", _B(R.string.prefs_defaults_stuck_beat));
             stuck_beat_then = stuck_beat_now;
         }
         return mStuckBeat;
  
     }
 
     double min_beat_then = 0.0;
     public int getMinBeat()
     {
         double min_beat_now;
 
         min_beat_now = mStats.nowMil();
         if(min_beat_now - min_beat_then > 1000)
         {
             mMinBeat = mPrefs.getInt("prefs_min_beat", _I(R.integer.prefs_defaults_min_beat));
             min_beat_then = min_beat_now;
         }
         return mMinBeat;
  
     }
 
     double beat_hold_then = 0.0;
     /* Get the value in milliseconds that we'll hold a beat strike.*/
     public int getBeatHold()
     {
         double beat_hold_now;
 
         beat_hold_now = mStats.nowMil();
         if(beat_hold_now - beat_hold_then > 1000)
         {
             mBeatHold = mPrefs.getInt("prefs_min_beat", _I(R.integer.prefs_defaults_min_beat));
             beat_hold_then = beat_hold_now;
         }
         return mBeatHold;
  
     }
 
     /* Get the stored mic data. This is read-only. */
     public short[] getMicData()
     {
         return mMicData;
     }
 
 
 /// SETTERS
 
     /* Set the current morph plugin name */
     public void setMorph(String morph)
     {
         mMorph = morph;
         mEditor.putString("prefs_morph_selection", mMorph);
     }
 
     /* Set the current input plugin name */
     public void setInput(String input)
     {
         mInput = input;
         mEditor.putString("prefs_input_selection", mInput);
     }
 
     /* Set the current actor plugin name */
     public void setActor(String actor)
     {
         mActor = actor;
         mEditor.putString("prefs_actor_selection", mActor);
     }
 
     /* Set whether to morph or not */
     public void setDoSwap(boolean doSwap)
     {
         mDoSwap = doSwap;
         mEditor.putBoolean("prefs_do_swap", mDoSwap);
     }
 
     /* Set whether to morph or not */
     public void setDoMorph(boolean doMorph)
     {
         mDoMorph = doMorph;
         mEditor.putBoolean("prefs_do_morph", mDoMorph);
     }
 
     /* Set the VisBin's morphing steps. */
     public void setMorphSteps(int steps)
     {
         mMorphSteps = steps;
         mEditor.putInt("prefs_morph_steps", mMorphSteps);
     }
     public void setMaxFPS(int maxfps)
     {
         mMaxFPS = maxfps;
         mEditor.putInt("prefs_max_fps", mMaxFPS);
     }
     public void setShowFPS(boolean showfps)
     {
         mShowFPS = showfps;
         mEditor.putBoolean("prefs_show_fps", mShowFPS);
     }
     public void setShowArt(boolean showart)
     {
         mShowArt = showart;
         mEditor.putBoolean("prefs_show_art", mShowArt);
     }
     public void setShowText(boolean showtext)
     {
         mShowText = showtext;
         mEditor.putBoolean("prefs_show_text", mShowText);
     }
     public void setIsActive(boolean isactive)
     {
         mIsActive = isactive;
         mEditor.putBoolean("prefs_is_active", mIsActive);
     }
     public void setDisplayText(String displaytext)
     {
         mDisplayText = displaytext;
         mEditor.putString("prefs_display_text", mDisplayText);
     }
     public void setStuckBeat(boolean stuckbeat)
     {
         mStuckBeat = stuckbeat;
         mEditor.putBoolean("prefs_stuck_beat", mStuckBeat);
     }
     public void setMinBeat(int minbeat)
     {
         mMinBeat = minbeat;
         mEditor.putInt("prefs_min_beat", mMinBeat);
     }
 
     public void setBeatHold(int beathold)
     {
         mBeatHold = beathold;
         mEditor.putInt("prefs_beat_hold", mBeatHold);
     }
 
     /* Display a warning text: provide text, time in milliseconds, and priority */
     private double mLastRefresh = 0.0;
     private double mLastDelay = 0;
     public boolean warn(String text, int millis, boolean priority)
     {
         double now = mStats.nowMil();
 
         if((now - mLastRefresh) < mLastDelay && !priority) 
             return false;
 
         mDisplayText = text;
 
         mLastRefresh = now;
 
         mLastDelay = millis;
 
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
 
 
     public final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
 
     private void releaseAlbumArt()
     {
         for(Entry<String, Bitmap> entry : mAlbumMap.entrySet())
         {
             entry.getValue().recycle();
         }
         mAlbumMap.clear();
         mAlbumArt = null;
     }
 
     private void getAlbumArt()
     {
         ContentResolver contentResolver = this.getContentResolver();
 
         List<Long> result = new ArrayList<Long>();
         List<String> map = new ArrayList<String>();
         Cursor cursor = contentResolver.query(MediaStore.Audio.Media.getContentUri("external"), 
             new String[]{MediaStore.Audio.Media.ALBUM_ID}, null, null, null);
         Cursor albumCursor = contentResolver.query(MediaStore.Audio.Media.getContentUri("external"), 
             new String[]{MediaStore.Audio.Media.ALBUM}, null, null, null);
     
         if (cursor.moveToFirst() && albumCursor.moveToFirst())
         {
             do{
                 long albumId = cursor.getLong(0);
                 if (!result.contains(albumId))
                 {
                     String album = albumCursor.getString(0);
                     result.add(albumId);
                     Bitmap bm = getAlbumArt(albumId);
                     if(bm != null && album != null)
                         mAlbumMap.put(album, bm);
                 }
             } while (cursor.moveToNext() && albumCursor.moveToNext());
         }
     }
 
     /* http://stackoverflow.com/questions/6591087/most-robust-way-to-fetch-album-art-in-android*/
     public Bitmap getAlbumArt(long album_id) 
     {
         if(album_id == -1) 
             return null;
 
         Bitmap bm = null;
         try 
         {
             Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
 
             ParcelFileDescriptor pfd = ((Context)this).getContentResolver()
                 .openFileDescriptor(uri, "r");
 
             if (pfd != null) 
             {
                 FileDescriptor fd = pfd.getFileDescriptor();
                 bm = BitmapFactory.decodeFileDescriptor(fd);
             }
         } catch (Exception e) {
             // Do nothing
         }
         Bitmap scaled = null;
         if(bm != null)
         {
             scaled = Bitmap.createScaledBitmap(bm, ARTWIDTH, ARTHEIGHT, false);
             bm.recycle();
         }
         return scaled;
     }
 
     private void disableMic()
     {
         if(mAudio != null && mAudioThread != null)
         {
             mMicActive = false;
             mAudioThread.interrupt();
             try {
                 mAudioThread.join();
             } catch (InterruptedException e) {
                 // Do nothing
             }
             if(mAudio.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)
             {
                 mAudio.stop();
             }
         }
     }
 
     private boolean enableMic(String input)
     {
 
         if(input.equals("mic") == false)
         {
             disableMic();
             return false;
         }
 
         if(mAudio == null)
             mAudio = findAudioRecord();
 
         if(mAudio != null && mView != null)
         {
             if(mAudio.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)
                 mAudio.stop();
 
             mMicData = new short[PCM_SIZE * 2];
 
             synchronized(mView.mSynch)
             {
                 NativeHelper.resizePCM(PCM_SIZE, RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
                 mAudioThread = new Thread(new Runnable() 
                 {
                     public void run() {
                         mMicActive = true;
                         mAudio.startRecording();
                         while(mMicActive)
                         {
                             mAudio.read(mMicData, 0, PCM_SIZE);
                         }
                         mAudio.stop();
                     }
                 });
                 mAudioThread.start();
             }
             return true;
         }
         return false;
     }
 
     // Detect parameters from highest to lowest values. 
     private static int[] mSampleRates = new int[] { 48000, 44100, 22050, 11025, 8000 };
     public AudioRecord findAudioRecord() {
         for (int rate : mSampleRates) {
             for (short audioFormat : new short[] { AudioFormat.ENCODING_PCM_16BIT, AudioFormat.ENCODING_PCM_8BIT}) {
                 for (short channelConfig : new short[] { AudioFormat.CHANNEL_IN_STEREO, AudioFormat.CHANNEL_IN_MONO}) {
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
                                 Log.d(TAG, "Opened mic: " + rate + "Hz, bits: " + audioFormat + ", channel: " + channelConfig + ", buffersize:" + PCM_SIZE);
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
 
 
