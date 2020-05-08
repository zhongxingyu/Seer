 /*
  * Copyright (C) 2009 The Android Open Source Project
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
 
 package com.qayto.mobile;
 
 
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.hardware.Camera;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnBufferingUpdateListener;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.media.MediaPlayer.OnVideoSizeChangedListener;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.Toast;
 
 
 public class MediaPlayer_Video extends Activity implements
         OnBufferingUpdateListener, OnCompletionListener,
         OnPreparedListener, OnVideoSizeChangedListener, SurfaceHolder.Callback {
 
     private static final String TAG = "MediaPlayerDemo";
     private int mVideoWidth;
     private int mVideoHeight;
     private MediaPlayer mMediaPlayer;
     private SurfaceView mPreview;
     private SurfaceHolder holder;
     private String path;
     private boolean mIsVideoSizeKnown = false;
     private boolean mIsVideoReadyToBePlayed = false;
     private Preview camera_view;
     private String question;
 
     /**
      * 
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle icicle) {
         super.onCreate(icicle);
         question = "";
         Bundle extras = getIntent().getExtras();
         if (extras != null) {
 			question = extras.getString("question");
 		}
         
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.mediaplayer_2);
        // camera_view = new Preview(this, (SurfaceView)findViewById(R.id.surface2));
 
         mPreview = (SurfaceView) findViewById(R.id.surface1);
         holder = mPreview.getHolder();
         holder.addCallback(this);
         holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 
         // Create our Preview view 
        camera_view = new Preview(this, (SurfaceView)findViewById(R.id.surface2));
 
        
        Button endsession = (Button) findViewById(R.id.button1);
        endsession.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            		Intent intent = new Intent();
 				intent.setClass(MediaPlayer_Video.this, RatingsActivity.class);
 				intent.putExtra("question", question);
 				startActivity(intent);
            }
        });
     }
 
     private void playVideo() {
         doCleanUp();
         try {
                 /*
                  * TODO: Set path variable to progressive streamable mp4 or
                  * 3gpp format URL. Http protocol should be used.
                  * Mediaplayer can only play "progressive streamable
                  * contents" which basically means: 1. the movie atom has to
                  * precede all the media data atoms. 2. The clip has to be
                  * reasonably interleaved.
                  * 
                  */
                 path = "http://robertwinkler.com/video_test1.mp4";
                 if (path == "") {
                     // Tell the user to provide a media file URL.
                     Toast
                             .makeText(
                                     MediaPlayer_Video.this,
                                     "Please edit MediaPlayerDemo_Video Activity,"
                                             + " and set the path variable to your media file URL.",
                                     Toast.LENGTH_LONG).show();
 
                 }
 
             
 
             // Create a new media player and set the listeners
             mMediaPlayer = new MediaPlayer();
             mMediaPlayer.setDataSource(path);
             mMediaPlayer.setDisplay(holder);
             mMediaPlayer.prepare();
             mMediaPlayer.setOnBufferingUpdateListener(this);
             mMediaPlayer.setOnCompletionListener(this);
             mMediaPlayer.setOnPreparedListener(this);
             mMediaPlayer.setOnVideoSizeChangedListener(this);
             mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
 
 
         } catch (Exception e) {
             Log.e(TAG, "error: " + e.getMessage(), e);
         }
     }
 
     public void onBufferingUpdate(MediaPlayer arg0, int percent) {
         Log.d(TAG, "onBufferingUpdate percent:" + percent);
 
     }
 
     public void onCompletion(MediaPlayer arg0) {
         Log.d(TAG, "onCompletion called");
     }
 
     public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
         Log.v(TAG, "onVideoSizeChanged called");
         if (width == 0 || height == 0) {
             Log.e(TAG, "invalid video width(" + width + ") or height(" + height + ")");
             return;
         }
         mIsVideoSizeKnown = true;
         mVideoWidth = width;
         mVideoHeight = height;
         if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
             startVideoPlayback();
         }
     }
 
     public void onPrepared(MediaPlayer mediaplayer) {
         Log.d(TAG, "onPrepared called");
         mIsVideoReadyToBePlayed = true;
         if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
             startVideoPlayback();
         }
     }
 
     public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k) {
         Log.d(TAG, "surfaceChanged called");
 //        //holder.setFixedSize(200, 200);
 //		 /*
 //		  *  Handle aspect ratio
 //		  */
 //		 int surfaceView_Width = mPreview.getWidth();
 //		 int surfaceView_Height = mPreview.getHeight();
 //
 //		 Log.d("tag1", ""+surfaceView_Width+" "+surfaceView_Height);
 //
 //		 float video_Width = mMediaPlayer.getVideoWidth();
 //		 float video_Height = mMediaPlayer.getVideoHeight();
 //
 //		 Log.d("tag2", ""+video_Width+" "+video_Height);
 //
 //		 float ratio_width = surfaceView_Width/video_Width;
 //		 float ratio_height = surfaceView_Height/video_Height;
 //		 float aspectratio = video_Width/video_Height;
 //
 //		 Log.d("tag3", ""+ratio_width+" "+ratio_height+" "+aspectratio);
 //		 LayoutParams layoutParams = mPreview.getLayoutParams();
 //		 
 //		 if (ratio_width > ratio_height){
 //			 layoutParams.width = (int) (surfaceView_Height * aspectratio);
 //			 layoutParams.height = surfaceView_Height;
 //			 Log.d("tag3.1", "ratio_width>ratio_height");
 //		 }else{
 //			 layoutParams.width = surfaceView_Width;
 //			 layoutParams.height = (int) (surfaceView_Width / aspectratio);
 //			 Log.d("tag3.2", "ratio_width<=ratio_height");
 //		 }
 //		 
 //		 Log.d("tag4", ""+layoutParams.width+" "+layoutParams.height);
 //
 //		 mPreview.setLayoutParams(layoutParams);
 
     }
 
     public void surfaceDestroyed(SurfaceHolder surfaceholder) {
         Log.d(TAG, "surfaceDestroyed called");
     }
 
 
     public void surfaceCreated(SurfaceHolder holder) {
         Log.d(TAG, "surfaceCreated called");
         playVideo();
 
 
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         releaseMediaPlayer();
         doCleanUp();
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         releaseMediaPlayer();
         doCleanUp();
     }
 
     private void releaseMediaPlayer() {
         if (mMediaPlayer != null) {
             mMediaPlayer.release();
             mMediaPlayer = null;
         }
     }
 
     private void doCleanUp() {
         mVideoWidth = 0;
         mVideoHeight = 0;
         mIsVideoReadyToBePlayed = false;
         mIsVideoSizeKnown = false;
     }
 
     private void startVideoPlayback() {
         Log.v(TAG, "startVideoPlayback");
         //holder.setFixedSize(200, 200);
 		 /*
 		  *  Handle aspect ratio
 		  */
 		 int surfaceView_Width = mPreview.getWidth();
 		 int surfaceView_Height = mPreview.getHeight();
 
 		 Log.d("tag1", ""+surfaceView_Width+" "+surfaceView_Height);
 
 		 float video_Width = mMediaPlayer.getVideoWidth();
 		 float video_Height = mMediaPlayer.getVideoHeight();
 
 		 Log.d("tag2", ""+video_Width+" "+video_Height);
 
 		 float ratio_width = surfaceView_Width/video_Width;
 		 float ratio_height = surfaceView_Height/video_Height;
 		 float aspectratio = video_Width/video_Height;
 
 		 Log.d("tag3", ""+ratio_width+" "+ratio_height+" "+aspectratio);
 		 LayoutParams layoutParams = mPreview.getLayoutParams();
 		 
 		 if (ratio_width > ratio_height){
 			 layoutParams.width = (int) (surfaceView_Height * aspectratio);
 			 layoutParams.height = surfaceView_Height;
 			 Log.d("tag3.1", "ratio_width>ratio_height");
 		 }else{
 			 layoutParams.width = surfaceView_Width;
 			 layoutParams.height = (int) (surfaceView_Width / aspectratio);
 			 Log.d("tag3.2", "ratio_width<=ratio_height");
 		 }
 		 
 		 Log.d("tag4", ""+layoutParams.width+" "+layoutParams.height);
 
 		 mPreview.setLayoutParams(layoutParams);
         mMediaPlayer.start();
     }
 }
 
 class Preview extends SurfaceView implements SurfaceHolder.Callback {
     SurfaceHolder mHolder;
     Camera mCamera;
     Boolean size_set;
     
     Preview(Context context, SurfaceView a_view) {
         super(context);
         
         size_set = false;
         // Install a SurfaceHolder.Callback so we get notified when the
         // underlying surface is created and destroyed.
         mHolder = a_view.getHolder();
         mHolder.addCallback(this);
         mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
     }
 
     public void surfaceCreated(SurfaceHolder holder) {
         // The Surface has been created, acquire the camera and tell it where
         // to draw.
         mCamera = Camera.open();
         try {
            mCamera.setPreviewDisplay(holder);
         } catch (IOException exception) {
             mCamera.release();
             mCamera = null;
             // TODO: add more exception handling logic here
         }
     }
 
     public void surfaceDestroyed(SurfaceHolder holder) {
         // Surface will be destroyed when we return, so stop the preview.
         // Because the CameraDevice object is not a shared resource, it's very
         // important to release it when the activity is paused.
         mCamera.stopPreview();
         mCamera.release();
         mCamera = null;
     }
     
     protected void setDisplayOrientation(Camera camera, int angle){
         Method downPolymorphic;
         try
         {
             downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", new Class[] { int.class });
             if (downPolymorphic != null)
                 downPolymorphic.invoke(camera, new Object[] { angle });
         }
         catch (Exception e1)
         {
         }
     }
 
     public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
         // Now that the size is known, set up the camera parameters and begin
         // the preview.
     //	if (!size_set) {
     		Configuration config = getResources().getConfiguration();
 	        Camera.Parameters parameters = mCamera.getParameters();
 	        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
 	        Camera.Size previewSize = previewSizes.get(2);
 	        parameters.setPreviewSize(previewSize.width, previewSize.height);
 	        Log.d("size_set", ""+w+" "+h);
 	        mCamera.stopPreview();
 	        mCamera.setParameters(parameters);
 	        if (config.orientation == Configuration.ORIENTATION_PORTRAIT)
 	        	setDisplayOrientation(mCamera, 90);
 	        else
 	        	setDisplayOrientation(mCamera, 180);
 	        mCamera.startPreview();
 	        size_set = true;
     	//}
     }
 
 }
