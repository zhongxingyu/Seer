 package com.baixing.view;
 
 import java.io.IOException;
 import java.util.List;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.graphics.ImageFormat;
 import android.hardware.Camera;
 import android.hardware.Camera.Parameters;
 import android.hardware.Camera.Size;
 import android.os.Build;
 import android.os.Build.VERSION;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 /**
  * 
  * @author liuchong
  *
  */
 @SuppressLint("NewApi")
 public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
 	public static final String TAG	= "CameraPreview";
     private SurfaceHolder mHolder;
     private Camera mCamera;
     private int screenRotation;
 
     public CameraPreview(Context context) {
         super(context);
         // Install a SurfaceHolder.Callback so we get notified when the
         // underlying surface is created and destroyed.
         mHolder = getHolder();
         mHolder.addCallback(this);
         // deprecated setting, but required on Android versions prior to 3.0
         mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
     }
     
     public void setCamera(Camera camera) {
         mCamera = camera;
         if (mCamera != null) {
             this.requestLayout();
         }
     }
 
     public void surfaceCreated(SurfaceHolder holder) {
         // The Surface has been created, now tell the camera where to draw the preview.
 //        try {
 //            mCamera.setPreviewDisplay(holder);
 //            mCamera.startPreview();
 //        } catch (IOException e) {
 //            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
 //        }
     }
 
     public void surfaceDestroyed(SurfaceHolder holder) {
         // empty. Take care of releasing the Camera preview in your activity.
     }
 
     public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
         // If your preview can change or rotate, take care of those events here.
         // Make sure to stop the preview before resizing or reformatting it.
 
         if (mHolder.getSurface() == null){
           // preview surface does not exist
           return;
         }
 
         // stop preview before making changes
         try {
             mCamera.stopPreview();
         } catch (Exception e){
           // ignore: tried to stop a non-existent preview
         }
         
         // set preview size and make any resize, rotate or
         // reformatting changes here
         
         final boolean isNewSdk = VERSION.SDK_INT > 10;
         
         
         // start preview with new settings
         try {
             mCamera.setPreviewDisplay(mHolder);
             
 //            if (isNewSdk) { //Set orientation on ZTEV880 will cause preview not display well.
 //            	try {
 //            		mCamera.setDisplayOrientation(90);//By design, we use landscape mode. For SDK level <== 8, MUST use landscape mode; for SDK level >8 can set display rotation by 90 degree to using portrait mode.
 //            	}
 //            	catch (Throwable t) {
 //            		//Ignor this exception.
 //            	}
 //            }
             
             Parameters param = mCamera.getParameters();
             param.setPictureFormat(ImageFormat.JPEG); //Picture format should be set to JPEG. 
             initParam(param, isNewSdk);
 //            Size size = getOptimalPreviewSize(param.getSupportedPreviewSizes(), isNewSdk ? h : w, isNewSdk ? w : h);
             Size size = getOptimalPreviewSize(param.getSupportedPreviewSizes(), w, h);
             if (size != null) {
             	param.setPreviewSize(size.width, size.height);
             }
             mCamera.setParameters(param);
             
             mCamera.startPreview();
 
         } catch (Exception e){
             Log.d(TAG, "Error starting camera preview: " + e.getMessage());
         }
     }
     
     private void initParam(Parameters params, boolean isNewSdk) {
 		List<Integer> prevF = params.getSupportedPreviewFormats();
 		params.setPreviewFormat(prevF.get(0));
 
 		final int min = params.getMinExposureCompensation();
 		final int max = params.getMaxExposureCompensation();
 		if (min == 0 && max == 0) {
 			
 		} else {
 			int mid = (params.getMinExposureCompensation() + params.getMaxExposureCompensation() ) /2;
 			params.setExposureCompensation(mid);
 		}
 		
 		
     	if (isNewSdk) {
     		List<int[]>  r = params.getSupportedPreviewFpsRange();
     		params.setPreviewFpsRange(r.get(0)[0], r.get(0)[1]);
     	}
     	else {
 //    		List<String> fM = params.getSupportedFlashModes();
 //    		if (fM != null) {
 //    			params.setFlashMode(fM.get(0));
 //    		}
 //    		List<Integer> r = params.getSupportedPreviewFrameRates();
 //    		params.setPreviewFrameRate(r.get(0));
 //    		List<String> cEs = params.getSupportedColorEffects();
 //    		params.setColorEffect(cEs.get(0));
 //    		List<String> bs = params.getSupportedAntibanding();
 //    		if (bs != null) {
 //    			params.setAntibanding(bs.get(0));
 //    		}
 //    		List<String> wbs = params.getSupportedWhiteBalance();
 //    		if (wbs != null) {
 //    			params.setWhiteBalance(wbs.get(0));
 //    		}
     	}
     }
     
     
     private static Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
         final double ASPECT_TOLERANCE = 0.1;
         double targetRatio = (double) w / h;
         if (sizes == null) return null;
 
         Size optimalSize = null;
         double minDiff = Double.MAX_VALUE;
 
         int targetHeight = h;
 
         // Try to find an size match aspect ratio and size
         for (Size size : sizes) {
             double ratio = (double) size.width / size.height;
             if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
             if (Math.abs(size.height - targetHeight) < minDiff) {
                 optimalSize = size;
                 minDiff = Math.abs(size.height - targetHeight);
             }
         }
 
         // Cannot find the one match the aspect ratio, ignore the requirement
         if (optimalSize == null) {
             minDiff = Double.MAX_VALUE;
             for (Size size : sizes) {
                 if (Math.abs(size.height - targetHeight) < minDiff) {
                     optimalSize = size;
                     minDiff = Math.abs(size.height - targetHeight);
                 }
             }
         }
         return optimalSize;
     }
     
 }
