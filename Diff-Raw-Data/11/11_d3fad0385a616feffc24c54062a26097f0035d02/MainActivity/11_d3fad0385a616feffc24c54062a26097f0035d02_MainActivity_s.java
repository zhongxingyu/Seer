 package com.davidykay.callrecorder;
 
import java.io.FileDescriptor;
 import java.io.IOException;
 
 import android.app.Activity;
 import android.media.MediaRecorder;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 public class MainActivity extends Activity {
       
  private FileDescriptor PATH_NAME;
   private MediaRecorder mRecorder; 
 
   ////////////////////////////////////////
   // Activity Lifecycle
   ////////////////////////////////////////
 
   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
 
     // Init Model
 
     mRecorder = new MediaRecorder();
     initRecorder();
 
    PATH_NAME = "this/is/my/path";
     
     // Init View
     setContentView(R.layout.main);
 
     final Button recordButton = (Button) findViewById(R.id.button_record);
     recordButton.setOnClickListener(new OnClickListener() {
       @Override
       public void onClick(View v) {
         startRecord();
       }
     });
     
     final Button playButton = (Button) findViewById(R.id.button_play);
     playButton.setOnClickListener(new OnClickListener() {
       @Override
       public void onClick(View v) {
         //startRecord();
       }
     });
     final Button pauseButton = (Button) findViewById(R.id.button_pause);
     pauseButton.setOnClickListener(new OnClickListener() {
       @Override
       public void onClick(View v) {
         //startRecord();
       }
     });
     final Button stopButton = (Button) findViewById(R.id.button_stop);
     stopButton.setOnClickListener(new OnClickListener() {
       @Override
       public void onClick(View v) {
         stopRecord();
       }
     });
   }
   
   ////////////////////////////////////////
   // Audio Recorder
   ////////////////////////////////////////
 
   @Override
   protected void onDestroy() {
     super.onDestroy();
 	  
     cleanupRecorder();
   }
 
   public boolean initRecorder() {
     mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
     mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
     mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
 
     mRecorder.setOutputFile(PATH_NAME);
     try {
       mRecorder.prepare();
       return true;
     } catch (IllegalStateException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     } catch (IOException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
     return false;
   }
 
   public void startRecord() {
     mRecorder.start();   // Recording is now started
   }
 
   public void stopRecord() {
     mRecorder.stop();
   }
 
   public void cleanupRecorder() {
     mRecorder.reset();   // You can reuse the object by going back to setAudioSource() step
     mRecorder.release(); // Now the object cannot be reused
   }
 
 }
