 package com.starlon.starvisuals;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.ParcelFileDescriptor;
 import android.preference.PreferenceManager;
 import android.content.Context;
 import android.content.ContentUris;
 import android.content.res.Configuration;
 import android.content.SharedPreferences;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.BroadcastReceiver;
 import android.content.ContentResolver;
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
 
 public class StarVisuals extends Activity implements OnClickListener
 {
     private final static String TAG = "StarVisuals/StarVisualsActivity";
     private final static String PREFS = "StarVisualsPrefs";
     private final static int ARTWIDTH = 100;
     private final static int ARTHEIGHT = 100;
     private static Settings mSettings;
     private AudioRecord mAudio = null;
     private boolean mMicActive = false;
     private int PCM_SIZE = 1024;
     private static int RECORDER_SAMPLERATE = 44100;
     private static int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
     private static int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
     public boolean mDoMorph = true;
     public String mMorph = null;
     public String mInput = null;
     public String mActor = null;
     private float mSongChanged = 0;
     private String mSongAction = null;
     public String mSongCommand = null;
     public String mSongArtist = null;
     public String mSongAlbum = null;
     public String mSongTrack = null;
     public Bitmap mAlbumArt = null;
     public IntentFilter mIntentFilter = null;
     public boolean mHasRoot = false;
     private Thread mAudioThread = null;
     public HashMap<String, Bitmap> mAlbumMap = new HashMap<String, Bitmap>();
 
     static private String mDisplayText = "Please wait...";
 
     private static int SWIPE_MIN_DISTANCE = 120;
     private static int SWIPE_MAX_OFF_PATH = 250;
     private static int SWIPE_THRESHOLD_VELOCITY = 200;
     private GestureDetector gestureDetector = null;
     OnTouchListener gestureListener;
 
     private StarVisualsView mView;
 
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
 
     /** Called when the activity is first created. */
     @Override
     protected void onCreate(Bundle state)
     {
         super.onCreate(state);
         makeFile("/data/data/com.starlon.starvisuals/libstub.lua", R.raw.libstub);
         makeFile("/data/data/com.starlon.starvisuals/pluginmath.lua", R.raw.pluginmath);
 
         mSettings = new Settings(this);
 
         this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
             WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
         mView = new StarVisualsView(this);
 
         // Don't dim screen
         mView.setKeepScreenOn(true);
 
         final ViewConfiguration vc = ViewConfiguration.get((Context)this);
 
         SWIPE_MIN_DISTANCE = vc.getScaledTouchSlop();
         SWIPE_THRESHOLD_VELOCITY = vc.getScaledMinimumFlingVelocity();
         SWIPE_MAX_OFF_PATH = vc.getScaledMaximumFlingVelocity();
 
         class MyGestureDetector extends SimpleOnGestureListener {
             @Override
             public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                 synchronized(mView.mSynch)
                 {
                     try {
                         if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                             return false;
                         if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && 
                             Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                             Log.w(TAG, "Left swipe...");
                             mView.switchScene(1);
                             // Left swipe
                         }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && 
                             Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                             Log.w(TAG, "Right swipe...");
                             mView.switchScene(0);
                             // Right swipe
                         }
                     } catch (Exception e) {
                         Log.w(TAG, "Failure in onFling");
                         // nothing
                     }
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
         mView.setOnClickListener(StarVisuals.this);
         mView.setOnTouchListener(gestureListener);
 
         setContentView(mView);
 
         //mHasRoot = checkRoot();
 
         mIntentFilter = new IntentFilter();
         mIntentFilter.addAction("com.android.music.metachanged");
         mIntentFilter.addAction("com.android.music.playstatechanged");
         mIntentFilter.addAction("com.android.music.playbackcomplete");
         mIntentFilter.addAction("com.android.music.queuechanged");
         mIntentFilter.addAction("com.starlon.starvisuals.PREFS_UPDATE");
 
         registerReceiver(mReceiver, mIntentFilter);
 
         updatePrefs();
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
                 mSongChanged = 0;
                 mAlbumArt = null;
                 NativeHelper.newSong();
                 warn("Ended playback...", true);
             }
             else if(action.equals("com.starlon.starvisuals.PREFS_UPDATE"))
             {
                 updatePrefs();
             }
         }
     };
 
     public void onClick(View v) {
 /*
         Filter f = (Filter) v.getTag();
         FilterFullscreenActivity.show(this, input, f);
 */
     }
 
     public void updatePrefs() 
     {
             SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences((Context)this);
     
             mDoMorph = settings.getBoolean("doMorph", true);
     
             NativeHelper.setMorphStyle(mDoMorph);
     
             mMorph = settings.getString("prefs_morph_selection", "alphablend");
             mInput = settings.getString("prefs_input_selection", "mic");
             mActor = settings.getString("prefs_actor_selection", "oinksie");
     
             NativeHelper.morphSetCurrentByName(mMorph);
             NativeHelper.inputSetCurrentByName(mInput);
             NativeHelper.actorSetCurrentByName(mActor);
     
             mView.initVisual(); // FIXME width x height. This method's overloaded: initVisual(w, h);
     }
 
     // This series of on<Action>() methods a flow chart are outlined here:
     // http://developer.android.com/reference/android/app/Activity.html
 
     // User returns to activity
     @Override
     public void onResume() 
     {
         super.onResume();
 
     }
 
     // follows onCreate() and onResume()
     @Override
     protected void onStart() 
     {   
         super.onStart();
 
         getAlbumArt();
 
         mView.startThread();
 
         enableMic("mic");
 
         registerReceiver(mReceiver, mIntentFilter);
     }
 
     // another activity comes to foreground
     @Override
     protected void onPause() 
     {
         super.onPause();
 
         SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences((Context)this);
         SharedPreferences.Editor editor = settings.edit();
 
         int morph = NativeHelper.morphGetCurrent();
         int input = NativeHelper.inputGetCurrent();
         int actor = NativeHelper.actorGetCurrent();
 
         this.setMorph(NativeHelper.morphGetName(morph));
         this.setInput(NativeHelper.inputGetName(input));
         this.setActor(NativeHelper.actorGetName(morph));
 
         editor.putString("prefs_morph_selection", mMorph);
         editor.putString("prefs_input_selection", mInput);
         editor.putString("prefs_actor_selection", mActor);
 
         editor.putBoolean("doMorph", mDoMorph);
 
         //Commit edits
         editor.commit();
 
         releaseAlbumArt();
 
 
         mView.stopThread();
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
 
             case R.id.close_app:
             {
                 synchronized(mView.mSynch)
                 {
                     NativeHelper.visualsQuit();
                     this.finish();
                 }
                 return true;
             }
 /*
             case R.id.input_stub:
             {
                 synchronized(mView.mSynch)
                 {
                     int index = NativeHelper.cycleInput(1);
     
                     String input = NativeHelper.inputGetName(index);
     
                     if(!enableMic(input)) 
                     {
                         index = NativeHelper.cycleInput(1);
                     }
     
                     warn(NativeHelper.inputGetLongName(index), true);
                 }
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
         System.loadLibrary("visual");
        System.loadLibrary("lv_common");
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
     private int mLastDelay = 0;
     public boolean warn(String text, int millis, boolean priority)
     {
         long now = System.currentTimeMillis();
 
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
 
     public String getDisplayText()
     {
         return mDisplayText;
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
         if(mAudio != null)
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
 
         if(mAudio != null)
         {
             if(mAudio.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)
                 mAudio.stop();
 
             mView.mMicData = new short[PCM_SIZE * 4];
 
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
                             mAudio.read(mView.mMicData, 0, PCM_SIZE);
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
                                 PCM_SIZE = bufferSize / 4;
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
 
 
