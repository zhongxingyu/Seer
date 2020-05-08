 package com.catalyst.android.birdapp.camera;
 
 import java.io.IOException;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.hardware.Camera;
 import android.hardware.Camera.Parameters;
 import android.util.Log;
 import android.view.Display;
 import android.view.Surface;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.WindowManager;
 
 public class CameraPreview extends SurfaceView implements
                 SurfaceHolder.Callback {
         static final int NONE = 0;
         static final int DRAG = 1;
         static final int ZOOM = 2;
         int mode = NONE;
 
         float oldDist = 1f;
         private boolean isPreviewRunning;
         private SurfaceHolder mSurfaceHolder;
         private Camera mCamera;
         private Context context;
 
         // Constructor that obtains context and camera
         public CameraPreview(Context context, Camera camera) {
                 super(context);
                 this.context = context;
                 this.mCamera = camera;
                 this.mSurfaceHolder = this.getHolder();
                 this.mSurfaceHolder.addCallback(this);
 
         }
 
         /**
          * creates surface, starts the camera preview
          */
         @Override
         public void surfaceCreated(SurfaceHolder surfaceHolder) {
 
                 try {
                         mCamera.setPreviewDisplay(surfaceHolder);
                         mCamera.startPreview();
 
                 } catch (IOException e) {
 
                 }
         }
 
         /**
          * stops capturing the preview and resets the camera then disconnects and
          * releases the camera object.
          */
         @Override
         public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                mCamera.stopPreview();
                mCamera.release();
         }
 
         /**
          * on surface change this stops the preview if it's running and then checks
          * if the screen has been rotated then calls the previewCamera method to
          * restart the preview.
          * 
          */
         @Override
         public void surfaceChanged(SurfaceHolder surfaceHolder, int format,
                         int width, int height) {
 
                 if (isPreviewRunning) {
                         mCamera.stopPreview();
 
                 }
 
                 Parameters parameters = mCamera.getParameters();
 
                 Display display = ((WindowManager) context
                                 .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
 
                 if (display.getRotation() == Surface.ROTATION_0) {
                         parameters.setPreviewSize(parameters.getPreviewSize().width,
                                         parameters.getPreviewSize().height);
                         mCamera.setDisplayOrientation(90);
                 }
 
                 if (display.getRotation() == Surface.ROTATION_90) {
                         parameters.setPreviewSize(parameters.getPreviewSize().width,
                                         parameters.getPreviewSize().height);
 
                 }
 
                 if (display.getRotation() == Surface.ROTATION_180) {
                         parameters.setPreviewSize(parameters.getPreviewSize().height,
                                         parameters.getPreviewSize().width);
 
                 }
 
                 if (display.getRotation() == Surface.ROTATION_270) {
                         parameters.setPreviewSize(parameters.getPreviewSize().width,
                                         parameters.getPreviewSize().height);
                         mCamera.setDisplayOrientation(90);
 
                 }
 
                 mCamera.setParameters(parameters);
 
                 previewCamera();
         }
 
         /**
          * sets the camera preview display and then starts the preview.
          */
         public void previewCamera() {
 
                 try {
                         mCamera.setPreviewDisplay(mSurfaceHolder);
                         mCamera.startPreview();
                         isPreviewRunning = true;
                 } catch (Exception e) {
                         e.printStackTrace();
                 }
         }
 
 }
