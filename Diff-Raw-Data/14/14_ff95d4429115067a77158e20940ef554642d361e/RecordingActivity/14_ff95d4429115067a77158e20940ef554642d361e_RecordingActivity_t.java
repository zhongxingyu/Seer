 package com.weipai;
 
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.res.Resources;
 import android.hardware.Camera;
 import android.media.CamcorderProfile;
 import android.media.MediaRecorder;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.provider.Settings;
 import android.util.Log;
 import android.view.*;
 import android.widget.Button;
 import android.widget.Toast;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 public class RecordingActivity extends Activity implements SurfaceHolder.Callback,
         MediaRecorder.OnErrorListener,
         MediaRecorder.OnInfoListener {
 
     private MediaRecorder recorder;
     private Camera camera;
     private SurfaceView surfaceView;
     private android.view.SurfaceHolder holder;
 
     private boolean recording = false;
 
     private boolean mStartPreviewFail = false;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.recording);
 
         surfaceView = (SurfaceView) findViewById(R.id.camaraPreview);
         final Button recordControlButton = (Button) findViewById(R.id.recordControlButton);
 
         surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
         surfaceView.getHolder().addCallback(this);
 
         recordControlButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 controlRecoring();
                 if (recording) {
                     recordControlButton.setText("Stop");
 
                 } else {
                     recordControlButton.setText("Start Recording");
                     gotoDetailActivity();
                 }
             }
         });
 
 //        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
     }
 
     private void gotoDetailActivity(){
         startActivity(new Intent(this, ShareActivity.class));
 
     }
 
     private void setCameraParameters() {
         CamcorderProfile mProfile = CamcorderProfile.get(
                 CamcorderProfile.QUALITY_LOW);
 
         Camera.Parameters mParameters = camera.getParameters();
 
         mParameters.setPreviewSize(mProfile.videoFrameWidth, mProfile.videoFrameHeight);
         mParameters.setPreviewFrameRate(mProfile.videoFrameRate);
 
         camera.setParameters(mParameters);
         // Keep preview size up to date.
         mParameters = camera.getParameters();
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event) {
 
         switch (event.getAction()) {
             case MotionEvent.ACTION_UP:
 //                        controlRecoring();
                 break;
         }
 
         return true;
     }
 
     private void controlRecoring() {
         if (!recording) {
 
             try {
                 initRecorder();
                 recorder.prepare();
                 recorder.start();
             } catch (Exception e) {
                 releaseCamera();
                 releaseRecorder();
                 Log.e(this.getClass().getName(), "Failed to start recording", e);
             }
 
 
         } else {
             recorder.stop();
             releaseRecorder();
             camera.lock();
         }
         recording = !recording;
     }
 
     private void releaseCamera() {
         if (camera != null) {
             camera.lock();
             camera.release();
             camera = null;
         }
     }
 
     private void releaseRecorder() {
         if (recorder != null) {
             recorder.reset();
             recorder.setOnErrorListener(null);
             recorder.release();
             recorder = null;
         }
     }
 
     private boolean mPreviewing = false;
 
     private void closeCamera() {
         if (camera == null) {
             return;
         }
         // If we don't lock the camera, release() will fail.
         camera.lock();
         camera.release();
         camera = null;
         mPreviewing = false;
     }
 
     private void initCamera() throws IOException {
         Resources ress = getResources();
         if (camera == null) {
             try {
                 camera = Camera.open();
             } catch (Exception e) {
                 Util.showFatalErrorAndFinish(this,
 
                 ress.getString(R.string.camera_error_title),
                 ress.getString(R.string.cannot_connect_camera));
             }
         }
 
         if (mPreviewing == true) {
             camera.stopPreview();
             mPreviewing = false;
         }
         camera.setDisplayOrientation(90);
         setCameraParameters();
         setPreviewDisplay(holder);
 
 
         try {
             camera.startPreview();
             mPreviewing = true;
         } catch (Throwable ex) {
             closeCamera();
             throw new RuntimeException("startPreview failed", ex);
         }
     }
 
     private void setPreviewDisplay(SurfaceHolder holder) {
         try {
             camera.setPreviewDisplay(holder);
         } catch (Throwable ex) {
             closeCamera();
             throw new RuntimeException("setPreviewDisplay failed", ex);
         }
     }
 
     private String createName(long dateTaken) {
         Date date = new Date(dateTaken);
         SimpleDateFormat dateFormat = new SimpleDateFormat(
                 getString(R.string.video_file_name_format));
 
         return dateFormat.format(date);
     }
 
     private void initRecorder() throws IOException {
 
         recorder = new MediaRecorder();
         camera.unlock();
         recorder.setCamera(camera);
 //        recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
 //        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
         recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
         recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
         recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
 //        recorder.setVideoSize(176, 144);
 //        recorder.setVideoFrameRate(15);
 //        recorder.setMaxDuration(180000);
 //        recorder.setVideoEncodingBitRate(136);
 
 //        recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));
 
        File path = Environment.getExternalStorageDirectory();
 
         File file = new File(path, "weipai"+".mp4");
 
         recorder.setOutputFile(file.getPath());
         recorder.setPreviewDisplay(holder.getSurface());
         recorder.setMaxFileSize(2000000);
         recorder.setOnErrorListener(this);
         recorder.setOnInfoListener(this);
        //for 2.3
        //recorder.setOrientationHint(90);
 
         /*ContentValues values = new ContentValues(3);
        values.put(MediaStore.MediaColumns.TITLE, "Test Android Video");
        values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "video/3gpp");*/
     }
 
     // from MediaRecorder.OnErrorListener
     public void onError(MediaRecorder mr, int what, int extra) {
         if (what == MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN) {
             // We may have run out of space on the sdcard.
             stopVideoRecording();
         }
     }
 
     public void onInfo(MediaRecorder mr, int what, int extra) {
         if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
             if (recording) stopVideoRecording();
         } else if (what
                 == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
             if (recording) stopVideoRecording();
 
             // Show the toast.
             Toast.makeText(this, R.string.video_reach_size_limit,
                     Toast.LENGTH_LONG).show();
         }
     }
 
     private String TAG = this.getClass().getName();
     private void stopVideoRecording() {
 
         Log.v(TAG, "stopVideoRecording");
         if (recording) {
             boolean needToRegisterRecording = false;
             recorder.setOnErrorListener(null);
             recorder.setOnInfoListener(null);
             try {
                 recorder.stop();
                 needToRegisterRecording = true;
             } catch (RuntimeException e) {
                 Log.e(TAG, "stop fail: " + e.getMessage());
             }
             recording = false;
         }
         releaseRecorder();
     }
 
 
     private void releaseResources() {
         if (camera != null) {
             try {
                 camera.stopPreview();
                 camera.reconnect();
             } catch (IOException e) {
                 Log.e(this.getClass().getName(), e.getMessage(), e);
             }
             camera.release();
             camera = null;
         }
 
         releaseRecorder();
 
     }
 
     // Implement the methods of SurfaceHolder.Callback interface
 
     // SurfaceCreated : This method gets called when surface is created.
     // In this, initialize all parameters of MediaRecorder object.
     //The output file will be stored in SD Card.
 
     public void surfaceCreated(SurfaceHolder holder) {
         try {
             mStartPreviewFail = false;
             initCamera();
         } catch (Exception e) {
             Log.e(this.getClass().getName(), e.getMessage(), e);
             if ("eng".equals(Build.TYPE)) {
                 throw new RuntimeException(e);
             }
             mStartPreviewFail = true;
         }
     }
 
 
     public void surfaceDestroyed(SurfaceHolder holder) {
         releaseResources();
 
     }
 
     public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
         // Make sure we have a surface in the holder before proceeding.
         if (holder.getSurface() == null) {
             return;
         }
 
         this.holder = holder;
 
 
         // The camera will be null if it is fail to connect to the
         // camera hardware. In this case we will show a dialog and then
         // finish the activity, so it's OK to ignore it.
         if (camera == null) return;
 
         // Set preview display if the surface is being created. Preview was
         // already started.
         if (holder.isCreating()) {
             setPreviewDisplay(holder);
         } else {
 
         }
 
 
     }
 
 
 }
