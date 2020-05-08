 package com.saywhat.saywhat;
 
 import java.io.File;
 import java.io.FileDescriptor;
 import java.io.FileInputStream;
 import java.io.IOException;
 
 import android.app.Activity;
 import android.content.res.AssetFileDescriptor;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.MediaRecorder;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Button;
 
 public class RecordPlayback extends Activity {
     private static final String FILE_EXT_3GP = ".3gp";
     private static final String RECORD_FOLDER = "SayWhat";
     private MediaRecorder recorder = null;
     private MediaPlayer m = null;
     private int currentFormat = 0;
     private int output_formats[] = {MediaRecorder.OutputFormat.THREE_GPP};
     private String file_exts[] = {FILE_EXT_3GP};
 
     private boolean recording;
     private boolean alreadyPressedUp;
     private final int timeCounterLimit = 1000;
     private int button;
 
     private final Handler handler = new Handler();
     private final Runnable runnable = new Runnable() {
         public void run() {
             if (alreadyPressedUp) {
                 AppLog.logString("Abandoning recording Attempt.");
                 recording = false;
             } else {
                 AppLog.logString("Start recording");
                 recording = true;
 
 
                 AssetFileDescriptor descriptor = null;
                 try {
                     descriptor = getAssets().openFd("recording.mp3");
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
                 if (descriptor != null)
                 {
                     playRecordingSound(descriptor);
                 }
 
                //startRecording();
             }
         }
     };
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         recording = false;
         super.onCreate(savedInstanceState);
         setContentView(R.layout.record_playback);
         m = new MediaPlayer();
         Button button1 = (Button) findViewById(R.id.Button1);
         button1.setOnTouchListener(new View.OnTouchListener() {
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 button = 1;
                 HandleTouchEvent(event);
                 return false;
             }
         });
 
         Button button2 = (Button) findViewById(R.id.Button2);
         button2.setOnTouchListener(new View.OnTouchListener() {
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 button = 2;
                 HandleTouchEvent(event);
                 return false;
             }
         });
 
         Button button3 = (Button) findViewById(R.id.Button3);
         button3.setOnTouchListener(new View.OnTouchListener() {
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 button = 3;
                 HandleTouchEvent(event);
                 return false;
             }
         });
 
         Button button4 = (Button) findViewById(R.id.Button4);
         button4.setOnTouchListener(new View.OnTouchListener() {
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 button = 4;
                 HandleTouchEvent(event);
                 return false;
             }
         });
     }
 
     private void HandleTouchEvent(MotionEvent event) {
         switch (event.getAction()) {
             case MotionEvent.ACTION_DOWN:
                 AppLog.logString("Processing Action Down");
                 alreadyPressedUp = false;
                 handler.postDelayed(runnable, timeCounterLimit);
                 break;
             case MotionEvent.ACTION_UP:
                 AppLog.logString("Processing Action Up");
                 long downTime = event.getEventTime() - event.getDownTime();
                 AppLog.logString(String.valueOf(downTime));
                 if (downTime < timeCounterLimit) {
                     AppLog.logString("Setting AlreadyPressedUp To True");
                     alreadyPressedUp = true;
                 }
                 if (recording) {
                     recording = false;
                     AppLog.logString("stop recording");
                     stopRecording();
                 } else {
                     AppLog.logString("Playing Back recording");
                     playBackRecording();
                 }
                 break;
         }
     }
 
     private void playRecordingSound(AssetFileDescriptor descriptor) {
         try {
             if (m.isPlaying()) {
                 m.stop();
             }
             m.reset();
             m = new MediaPlayer();
             m.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
             descriptor.close();
 
             m.prepare();
             m.setVolume(1f, 1f);
             m.setLooping(false);
             m.start();
             m.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
 
                 @Override
                 public void onCompletion(MediaPlayer m) {
                     m.reset();
                    startRecording();
                 }
 
             });
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private void playBackRecording() {
         AppLog.logString("Playing Back " + getFilename());
         try {
             if (m.isPlaying()) {
                 m.stop();
             }
             m.reset();
             m = new MediaPlayer();
             File file = new File(getFilename());
             FileInputStream fis = new FileInputStream(file);
             FileDescriptor descriptor = fis.getFD();
             m.setDataSource(descriptor);
             m.setAudioStreamType(AudioManager.STREAM_MUSIC);
             m.prepare();
             m.setVolume(1f, 1f);
             m.setLooping(false);
             m.start();
             m.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
 
                 @Override
                 public void onCompletion(MediaPlayer m) {
                     m.reset();
                 }
 
             });
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.record_playback, menu);
         return true;
     }
 
     private String getFilename() {
         String filepath = Environment.getExternalStorageDirectory().getPath();
         File file = new File(filepath, RECORD_FOLDER);
 
         if (!file.exists()) {
             file.mkdirs();
         }
         String fileName = "Button" + String.valueOf(button) + file_exts[currentFormat];
         File audioFile = new File(file.getAbsolutePath(), fileName);
         if (!file.exists()) {
             try {
                 file.createNewFile();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
 
         String temp = file.getAbsolutePath() + "/Button" + String.valueOf(button) + file_exts[currentFormat];
 
         return (temp);
     }
 
     private void startRecording() {
         AppLog.logString("Recording " + getFilename());
         recorder = new MediaRecorder();
         recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
         recorder.setOutputFormat(output_formats[currentFormat]);
         recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
         recorder.setOutputFile(getFilename());
         recorder.setOnErrorListener(errorListener);
         recorder.setOnInfoListener(infoListener);
 
         try {
             recorder.prepare();
             recorder.start();
         } catch (IllegalStateException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
         @Override
         public void onError(MediaRecorder mr, int what, int extra) {
             AppLog.logString("Error: " + what + ", " + extra);
         }
     };
 
     private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
         @Override
         public void onInfo(MediaRecorder mr, int what, int extra) {
             AppLog.logString("Warning: " + what + ", " + extra);
         }
     };
 
     private void stopRecording() {
         if (null != recorder) {
             recorder.stop();
             recorder.reset();
             recorder.release();
 
             recorder = null;
         }
     }
 }
