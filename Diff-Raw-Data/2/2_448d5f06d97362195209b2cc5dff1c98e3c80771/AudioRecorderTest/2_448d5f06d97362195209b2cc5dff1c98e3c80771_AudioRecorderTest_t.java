 // AudioRecorderTest.java
 // ----------------------
 
 // this is an artifact from our Alpha release, and this code will likely end up
 // somewhere else, particularly in whatever recording module we develop.
 
 /*
  * The application needs to have the permission to write to external storage
  * if the output file is written to the external storage, and also the
  * permission to record audio. These permissions must be set in the
  * application's AndroidManifest.xml file, with something like:
  *
  * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
  * <uses-permission android:name="android.permission.RECORD_AUDIO" />
  *
  */
 package com.teamluper.luper;
 
 import android.widget.LinearLayout;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.view.View;
 import android.content.Context;
 import android.util.Log;
 import android.media.MediaRecorder;
 import android.media.MediaPlayer;
//import com.teamluper.luper.Clip;
 
 import java.io.IOException;
 
 import com.actionbarsherlock.app.SherlockActivity;
 
 
 public class AudioRecorderTest extends SherlockActivity
 {
     private static final String LOG_TAG = "AudioRecorderTest";
     private static String mFileName = null;
 
     private RecordButton mRecordButton = null;
     private MediaRecorder mRecorder = null;
 
     private PlayButton   mPlayButton = null;
     private MediaPlayer   mPlayer = null;
 
     private void onRecord(boolean start) {
 
         if (start) {
             startRecording();
         } else {
             stopRecording();
         }
     }
 
     private void onPlay(boolean start) {
         if (start) {
             startPlaying();
         } else {
             stopPlaying();
         }
     }
 
     private void startPlaying() {
         mPlayer = new MediaPlayer();
         try {
             mPlayer.setDataSource(mFileName);
             mPlayer.prepare();
             mPlayer.start();
         } catch (IOException e) {
             Log.e(LOG_TAG, "prepare() failed1");
         }
     }
 
     private void stopPlaying() {
         mPlayer.release();
         mPlayer = null;
     }
 
     private void startRecording() {
     	//Sets the name of the file when you start recording as opposed to when you click "Audio Record Test" from the main screen
         mFileName = Environment.getExternalStorageDirectory()+"/LuperApp/Clips";
         mFileName += "/clip_" + System.currentTimeMillis() +".3gp";
         
         mRecorder = new MediaRecorder();
         System.out.println("here");
         mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
         System.out.println("and here");
         mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
         mRecorder.setOutputFile(mFileName);
         //Clip newClip = new Clip(mFileName); mfilname --> string, needs a file?
         mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
 
         try {
             mRecorder.prepare();
         } catch (IOException e) {
         	System.out.println(e.toString());
             Log.e(LOG_TAG, "prepare() failed2");
         }
 
         mRecorder.start();
     }
 
     private void stopRecording() {
         mRecorder.stop();
         mRecorder.release();
         mRecorder = null;
     }
 
     class RecordButton extends Button {
         boolean mStartRecording = true;
 
         OnClickListener clicker = new OnClickListener() {
             public void onClick(View v) {
                 onRecord(mStartRecording);
                 if (mStartRecording) {
                     setText("Stop recording");
                 } else {
                     setText("Start recording");
                 }
                 mStartRecording = !mStartRecording;
             }
         };
 
         public RecordButton(Context ctx) {
             super(ctx);
             setText("Start recording");
             setOnClickListener(clicker);
         }
     }
 
     class PlayButton extends Button {
         boolean mStartPlaying = true;
 
         OnClickListener clicker = new OnClickListener() {
             public void onClick(View v) {
                 onPlay(mStartPlaying);
                 if (mStartPlaying) {
                     setText("Stop playing");
                 } else {
                     setText("Start playing");
                 }
                 mStartPlaying = !mStartPlaying;
             }
         };
 
         public PlayButton(Context ctx) {
             super(ctx);
             setText("Start playing");
             setOnClickListener(clicker);
         }
     }
 
     public AudioRecorderTest() {
     	mFileName = null;
     	/*Tabbed this out because it is useless to do here as we want to create multiple clips when record is pressed, not overwrite them
     	 * 
          *mFileName = Environment.getExternalStorageDirectory()+"/LuperApp/Clips";
          *mFileName += "/clip_" + System.currentTimeMillis() +".3gp";
          * 
          */
     }
 
     @Override
     public void onCreate(Bundle icicle) {
         super.onCreate(icicle);
 
         // this LinearLayout is used in place of an XML file.
         // Android lets you do your layouts either programattically like this,
         // or with an XML file.
         LinearLayout ll = new LinearLayout(this);
         mRecordButton = new RecordButton(this);
         ll.addView(mRecordButton,
             new LinearLayout.LayoutParams(
                 ViewGroup.LayoutParams.WRAP_CONTENT,
                 ViewGroup.LayoutParams.WRAP_CONTENT,
                 0));
         mPlayButton = new PlayButton(this);
         ll.addView(mPlayButton,
             new LinearLayout.LayoutParams(
                 ViewGroup.LayoutParams.WRAP_CONTENT,
                 ViewGroup.LayoutParams.WRAP_CONTENT,
                 0));
         setContentView(ll);
     }
 
     @Override
     public void onPause() {
         super.onPause();
         if (mRecorder != null) {
             mRecorder.release();
             mRecorder = null;
         }
 
         if (mPlayer != null) {
             mPlayer.release();
             mPlayer = null;
         }
     }
 }
