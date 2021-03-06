 package com.adaburrows.superpowers;
 
 import java.io.IOException;
 
 import android.util.Log;
 import android.app.Activity;
 import android.content.Context;
 import android.hardware.Camera;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.ViewGroup.LayoutParams;
 import android.media.AudioManager;
 import android.media.audiofx.Equalizer;
 import android.media.AudioFormat;
 import android.media.AudioRecord;
 import android.media.AudioTrack;
 import android.media.MediaPlayer;
 import android.media.MediaRecorder;
 import android.media.MediaRecorder.AudioSource;
 import android.media.audiofx.Visualizer;
 
 public class Superpowers extends Activity {
   private static final String TAG = "Superpowers";
 
   private CameraView mCameraView;
   Camera mCamera;
   private MagicOverlay mOverlay;
   AudioRecord mAudioRecord;
   AudioTrack mAudioTrack;
   Visualizer mAudioVisualizer;
   int mAudioSessionId;
   int mAudioSampleRate;
   int mAudioChannelInConfig;
   int mAudioChannelOutConfig;
   int mAudioEncodingFormat;
   int mAudioBufferSize;
 
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
 
     // Hide the window title.
     getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
     requestWindowFeature(Window.FEATURE_NO_TITLE);
   }
 
   @Override
   protected void onPause() {
     super.onPause();
     if (mCamera != null) {
       mCamera.stopPreview();
     }
     if (mAudioRecord != null) {
       mAudioRecord.stop();
     }
     if (mAudioTrack != null) {
       mAudioTrack.stop();
     }
   }
 
   @Override
   protected void onStop() {
     super.onStop();
     if (mCamera != null) {
       mCamera.release();
       mCamera = null;
     }
     if (mAudioRecord != null) {
       mAudioRecord.release();
       mAudioRecord = null;
     }
     if (mAudioTrack != null) {
       mAudioTrack.release();
       mAudioTrack = null;
     }
     if (mAudioVisualizer != null) {
       mAudioVisualizer.release();
     }
   }
 
   @Override
   protected void onStart() {
     super.onStart();
 
     mCamera = getCamera();
     setupCamera();
     setupAudio();
     setVolumeControlStream(AudioManager.STREAM_MUSIC);
 
     new Thread(new Runnable(){
       public void run(){
         mAudioRecord.startRecording();
         while (true) {
          int chunk_size = mAudioBufferSize / 2;
          short[] samples = new short[chunk_size];
          for (int i = 0; i < 2; i++) {
             int offset = i * chunk_size;
             mAudioRecord.read(samples, offset, chunk_size);
             mAudioTrack.write(samples, offset, chunk_size);
           }
         }
       }
    }).start();
 
     mAudioTrack.play();
     mCameraView = new CameraView(this, mCamera);
     mOverlay = new MagicOverlay(this);
     setContentView(mCameraView);
     addContentView(mOverlay, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
     //setContentView(R.layout.main);
   }
 
   public static Camera getCamera() {
     Camera c = null;
     try {
       // attempt to get a Camera instance
       c = Camera.open();
     }
     catch (Exception exception){
       // Camera is not available (in use or does not exist)
       Log.d(TAG, "Error fetching camera: " + exception.getMessage());
     }
     return c;
   }
 
   public void setupCamera() {
     if (mCamera != null) {
       Camera.Parameters parameters = mCamera.getParameters();
       /*
        * This needs to be changed. I'm using two obsolete API calls because it was easier.
        */
       parameters.setPreviewSize(640, 480); //obs
       parameters.setPreviewFrameRate(30); //obs
       parameters.setSceneMode(Camera.Parameters.SCENE_MODE_NIGHT);
       parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
       // Use above parameters
       mCamera.setParameters(parameters);
     }
   }
 
   public void setupAudio() {
     //HOWTO:  Set up audio buffer for reading.
     mAudioSampleRate = 44100;
     mAudioChannelInConfig = AudioFormat.CHANNEL_IN_MONO;
     mAudioChannelOutConfig = AudioFormat.CHANNEL_OUT_MONO;
     mAudioEncodingFormat = AudioFormat.ENCODING_PCM_16BIT;
     mAudioBufferSize = AudioRecord.getMinBufferSize(
       mAudioSampleRate,
       mAudioChannelInConfig,
       mAudioEncodingFormat
     );
     
     mAudioRecord = new AudioRecord(
       MediaRecorder.AudioSource.MIC,
       mAudioSampleRate,
       mAudioChannelInConfig,
       mAudioEncodingFormat,
       mAudioBufferSize
     );
     mAudioSessionId = mAudioRecord.getAudioSessionId();
     Log.d(TAG, "Audio session ID: " + mAudioSessionId);
 
     mAudioTrack = new AudioTrack(
       AudioManager.STREAM_MUSIC,
       mAudioSampleRate,
       mAudioChannelOutConfig,
       mAudioEncodingFormat,
       mAudioBufferSize,
       AudioTrack.MODE_STREAM,
       mAudioSessionId
     );
 
     mAudioVisualizer = null;
     try {
       mAudioVisualizer = new Visualizer(mAudioSessionId);
       mAudioVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
       mAudioVisualizer.setDataCaptureListener(
         new Visualizer.OnDataCaptureListener() {
 
           public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
           }
 
          public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
            mOverlay.updateData(bytes);
          }
 
         },
         Visualizer.getMaxCaptureRate() / 2, true, false
       );
       mAudioVisualizer.setEnabled(true);
     } catch (Exception exception) {
       Log.d(TAG, "Error creating Visualizer: " + exception.getMessage());
     }
   }
 
 }
