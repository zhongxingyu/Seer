 /***
   Copyright (c) 2013 CommonsWare, LLC
   Portions Copyright (C) 2007 The Android Open Source Project
   
   Licensed under the Apache License, Version 2.0 (the "License"); you may
   not use this file except in compliance with the License. You may obtain
   a copy of the License at
     http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
  */
 
 package com.commonsware.cwac.camera;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.pm.ActivityInfo;
 import android.graphics.ImageFormat;
 import android.hardware.Camera;
 import android.media.MediaRecorder;
 import android.os.Build;
 import android.util.DisplayMetrics;
import android.util.Log;
 import android.view.OrientationEventListener;
 import android.view.Surface;
 import android.view.View;
 import android.view.ViewGroup;
 import java.io.IOException;
 
 public class CameraView extends ViewGroup implements
     Camera.PictureCallback {
   static final String TAG="CWAC-Camera";
   private PreviewStrategy previewStrategy;
   private Camera.Size previewSize;
   private Camera camera;
   private boolean inPreview=false;
   private CameraHost host=null;
   private OnOrientationChange onOrientationChange=null;
   private int displayOrientation=-1;
   private int outputOrientation=-1;
   private int cameraId=-1;
   private MediaRecorder recorder=null;
   private Camera.Parameters previewParams=null;
 
   public CameraView(Context context) {
     super(context);
 
     onOrientationChange=new OnOrientationChange(context);
   }
 
   public CameraHost getHost() {
     return(host);
   }
 
   // must call this after constructor, before onResume()
 
   public void setHost(CameraHost host) {
     this.host=host;
 
     if (host.getDeviceProfile().useTextureView()) {
       previewStrategy=new TexturePreviewStrategy(this);
     }
     else {
       previewStrategy=new SurfacePreviewStrategy(this);
     }
   }
 
   public void onResume() {
     addView(previewStrategy.getWidget());
 
     if (camera == null) {
       cameraId=getHost().getCameraId();
      Log.d(getClass().getSimpleName(), "opening camera");
       camera=Camera.open(cameraId);
 
       if (getActivity().getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
         onOrientationChange.enable();
       }

       setCameraDisplayOrientation(cameraId, camera);
     }
   }
 
   public void onPause() {
     previewDestroyed();
     removeView(previewStrategy.getWidget());
   }
 
   public int getDisplayOrientation() {
     return(displayOrientation);
   }
 
   public void lockToLandscape(boolean enable) {
     if (enable) {
       getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
       onOrientationChange.enable();
     }
     else {
       getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
       onOrientationChange.disable();
     }
 
     setCameraDisplayOrientation(cameraId, camera);
   }
 
   @Override
   public void onPictureTaken(byte[] data, Camera camera) {
     camera.setParameters(previewParams);
 
     new ImageCleanupTask(data, cameraId, getHost(),
                          getContext().getCacheDir()).start();
 
     camera.startPreview();
     inPreview=true;
   }
 
   public void takePicture() {
     if (inPreview) {
       previewParams=camera.getParameters();
 
       Camera.Parameters pictureParams=camera.getParameters();
       Camera.Size pictureSize=getHost().getPictureSize(pictureParams);
 
       pictureParams.setPictureSize(pictureSize.width,
                                    pictureSize.height);
       pictureParams.setPictureFormat(ImageFormat.JPEG);
       camera.setParameters(getHost().adjustPictureParameters(pictureParams));
 
       camera.takePicture(null, null, this);
       inPreview=false;
     }
   }
 
   public boolean isRecording() {
     return(recorder != null);
   }
 
   public void record() throws Exception {
     if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
       throw new UnsupportedOperationException(
                                               "Video recording supported only on API Level 11+");
     }
 
     camera.unlock();
 
     try {
       recorder=new MediaRecorder();
       recorder.setCamera(camera);
       getHost().configureRecorderAudio(cameraId, recorder);
       recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
       getHost().configureRecorderProfile(cameraId, recorder);
       getHost().configureRecorderOutput(cameraId, recorder);
       recorder.setOrientationHint(outputOrientation);
       previewStrategy.attach(recorder);
       recorder.prepare();
       recorder.start();
     }
     catch (IOException e) {
       recorder.release();
       recorder=null;
       throw e;
     }
   }
 
   public void stopRecording() throws IOException {
     if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
       throw new UnsupportedOperationException(
                                               "Video recording supported only on API Level 11+");
     }
 
     MediaRecorder tempRecorder=recorder;
 
     recorder=null;
     tempRecorder.stop();
     tempRecorder.release();
     camera.reconnect();
   }
 
   // based on CameraPreview.java from ApiDemos
 
   @Override
   protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
     final int width=
         resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
     final int height=
         resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
     setMeasuredDimension(width, height);
 
     if (previewSize == null && camera != null) {
       previewSize=
           getHost().getPreviewSize(getDisplayOrientation(), width,
                                    height, camera.getParameters());
     }
   }
 
   // based on CameraPreview.java from ApiDemos
 
   @Override
   protected void onLayout(boolean changed, int l, int t, int r, int b) {
     if (changed && getChildCount() > 0) {
       final View child=getChildAt(0);
       final int width=r - l;
       final int height=b - t;
       int previewWidth=width;
       int previewHeight=height;
 
       // handle orientation
 
       if (previewSize != null) {
         if (getDisplayOrientation() == 90
             || getDisplayOrientation() == 270) {
           previewWidth=previewSize.height;
           previewHeight=previewSize.width;
         }
         else {
           previewWidth=previewSize.width;
           previewHeight=previewSize.height;
         }
       }
 
       // Center the child SurfaceView within the parent.
       if (width * previewHeight > height * previewWidth) {
         final int scaledChildWidth=
             previewWidth * height / previewHeight;
         child.layout((width - scaledChildWidth) / 2, 0,
                      (width + scaledChildWidth) / 2, height);
       }
       else {
         final int scaledChildHeight=
             previewHeight * width / previewWidth;
         child.layout(0, (height - scaledChildHeight) / 2, width,
                      (height + scaledChildHeight) / 2);
       }
     }
   }
 
   void previewCreated() {
     try {
       previewStrategy.attach(camera);
     }
     catch (IOException e) {
       getHost().handleException(e);
     }
   }
 
   void previewDestroyed() {
     if (camera != null) {
       previewStopped();
      Log.d(getClass().getSimpleName(), "releasing camera");
       camera.release();
       camera=null;
     }
   }
 
   void previewReset(int width, int height) {
     previewStopped();
     initPreview(width, height);
   }
 
   private void previewStopped() {
     if (inPreview) {
      Log.d(getClass().getSimpleName(), "stopping preview");
       camera.stopPreview();
       inPreview=false;
     }
   }
 
   public void initPreview(int w, int h) {
    Log.d(getClass().getSimpleName(),
          String.format("initPreview() called, setting up %d x %d",
                        previewSize.width, previewSize.height));

     Camera.Parameters parameters=camera.getParameters();
 
     parameters.setPreviewSize(previewSize.width, previewSize.height);
 
     requestLayout();
 
     camera.setParameters(getHost().adjustPreviewParameters(parameters));
     camera.startPreview();
     inPreview=true;
   }
 
   // based on
   // http://developer.android.com/reference/android/hardware/Camera.html#setDisplayOrientation(int)
   // and http://stackoverflow.com/a/10383164/115145
 
   private void setCameraDisplayOrientation(int cameraId,
                                            android.hardware.Camera camera) {
     Camera.CameraInfo info=new Camera.CameraInfo();
     int rotation=
         getActivity().getWindowManager().getDefaultDisplay()
                      .getRotation();
     int degrees=0;
     DisplayMetrics dm=new DisplayMetrics();
 
     Camera.getCameraInfo(cameraId, info);
     getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
 
     // if the device's natural orientation is portrait...
     // if ((rotation == Surface.ROTATION_0 || rotation ==
     // Surface.ROTATION_180)
     // && height > width
     // || (rotation == Surface.ROTATION_90 || rotation ==
     // Surface.ROTATION_270)
     // && width > height) {
     switch (rotation) {
       case Surface.ROTATION_0:
         degrees=0;
         break;
       case Surface.ROTATION_90:
         degrees=90;
         break;
       case Surface.ROTATION_180:
         degrees=180;
         break;
       case Surface.ROTATION_270:
         degrees=270;
         break;
     }
     // }
     // if the device's natural orientation is landscape...
     // else {
     // switch (rotation) {
     // case Surface.ROTATION_0:
     // degrees=90;
     // break;
     // case Surface.ROTATION_90:
     // degrees=0;
     // break;
     // case Surface.ROTATION_180:
     // degrees=270;
     // break;
     // case Surface.ROTATION_270:
     // degrees=180;
     // break;
     // }
     // }
 
     if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
       displayOrientation=(info.orientation + degrees) % 360;
       displayOrientation=(360 - displayOrientation) % 360;
     }
     else {
       displayOrientation=(info.orientation - degrees + 360) % 360;
     }
 
     camera.stopPreview();
     inPreview=false;
     camera.setDisplayOrientation(displayOrientation);
     camera.startPreview();
     inPreview=true;
 
     if (getActivity().getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
       outputOrientation=
           getCameraPictureRotation(getActivity().getWindowManager()
                                                 .getDefaultDisplay()
                                                 .getOrientation());
     }
     else if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
       outputOrientation=(360 - displayOrientation) % 360;
     }
     else {
       outputOrientation=displayOrientation;
     }
 
     Camera.Parameters params=camera.getParameters();
 
     params.setRotation(outputOrientation);
     camera.setParameters(params);
   }
 
   // based on:
   // http://developer.android.com/reference/android/hardware/Camera.Parameters.html#setRotation(int)
 
   private int getCameraPictureRotation(int orientation) {
     Camera.CameraInfo info=new Camera.CameraInfo();
     Camera.getCameraInfo(cameraId, info);
     int rotation=0;
 
     orientation=(orientation + 45) / 90 * 90;
 
     if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
       rotation=(info.orientation - orientation + 360) % 360;
     }
     else { // back-facing camera
       rotation=(info.orientation + orientation) % 360;
     }
 
     return(rotation);
   }
 
   Activity getActivity() {
     return((Activity)getContext());
   }
 
   private class OnOrientationChange extends OrientationEventListener {
     public OnOrientationChange(Context context) {
       super(context);
       disable();
     }
 
     @Override
     public void onOrientationChanged(int orientation) {
       if (camera != null) {
         Camera.Parameters params=camera.getParameters();
 
         outputOrientation=getCameraPictureRotation(orientation);
         params.setRotation(outputOrientation);
         camera.setParameters(params);
       }
     }
   }
 }
