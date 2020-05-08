 package edu.berkeley.cellscope.cscore.celltracker;
 
 import java.io.File;
 import java.io.FileOutputStream;
 
 import org.opencv.android.JavaCameraView;
 
 import edu.berkeley.cellscope.cscore.R;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.hardware.Camera;
 import android.hardware.Camera.PictureCallback;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.widget.Toast;
 
 public class OpenCVCameraView extends JavaCameraView {
 	private static final String TAG = "OpenCvCameraView";
 	private static final int COMPRESSION_QUALITY = 90; //0-100
 	
 	private OpenCVCameraActivity activity;
 	private int minExposure, maxExposure;
 
 	public OpenCVCameraView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 	
 	public void setActivity(OpenCVCameraActivity activity) {
 		this.activity = activity;
 	}
 	
 	public void disableAutoFocus() {
 		Camera.Parameters params = mCamera.getParameters();
 		if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_INFINITY))
 			params.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
 		params.setAutoWhiteBalanceLock(false);
 		params.setExposureCompensation(0);
 		minExposure = params.getMinExposureCompensation();
 		maxExposure = params.getMaxExposureCompensation();
 		mCamera.setParameters(params);
 	}
 	
 	@Override
 	public void enableView() {
 		super.enableView();
 		disableAutoFocus();
 	}
 
 	public void takePicture(final File fileName) {
         Log.i(TAG, "Taking picture");
         PictureCallback callback = new PictureCallback() {
 
             public void onPictureTaken(byte[] data, Camera camera) {
                 Log.i(TAG, "Saving a bitmap to file: " + fileName.getPath());
                 Bitmap picture = BitmapFactory.decodeByteArray(data, 0, data.length);
                 try {
                     FileOutputStream out = new FileOutputStream(fileName);
                     picture.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, out);
                     picture.recycle();
                     out.close();
                     toast("Picture saved as " + fileName.getName());
    //             	FileOutputStream fos = new FileOutputStream(fileName);
    // 	            fos.write(data);
    // 	            fos.close();
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
                 //mCamera.stopPreview();
                 mCamera.startPreview();
             }
         };
         Camera.Parameters params = mCamera.getParameters();
         Camera.Size size = params.getPreviewSize();
         params.setPictureSize(size.width, size.height);
         mCamera.setParameters(params);
         System.out.println("preview sizes");
         for (Camera.Size s: params.getSupportedPreviewSizes()) {
         	System.out.println(s.width + " " + s.height);
         }
         System.out.println("picture sizes");
         for (Camera.Size s: params.getSupportedPictureSizes()) {
         	System.out.println(s.width + " " + s.height);
         }
         mCamera.takePicture(null, null, callback);
     }
     
 	public String zoom(int step) {
 		Camera.Parameters parameters = mCamera.getParameters();
 		if (!parameters.isZoomSupported())
 			return "100%";
 		int zoom = parameters.getZoom() + step;
 		if (zoom > parameters.getMaxZoom())
 			zoom = parameters.getMaxZoom();
 		else if (zoom < 0)
 			zoom = 0;
 		parameters.setZoom(zoom);
 		mCamera.setParameters(parameters);
 		return parameters.getZoomRatios().get(zoom) + "%";
 	}
 	
 	public int getMaxZoom() {
 		Camera.Parameters parameters = mCamera.getParameters();
 		if (!parameters.isZoomSupported())
 			return 0;
 		return parameters.getMaxZoom();
 	}
 	
 	private void toast(String message) {
 		Context context = activity.getApplicationContext();
 		int duration = Toast.LENGTH_SHORT;
 		Toast toast = Toast.makeText(context, message, duration);
 		toast.show();
 	}
 	
 	public int getMaxExposure() {
 		Camera.Parameters parameters = mCamera.getParameters();
 		return parameters.getMaxExposureCompensation();
 	}
 	
 	public int getMinExposure() {
 		Camera.Parameters parameters = mCamera.getParameters();
 		return parameters.getMinExposureCompensation();
 	}
 	
 	public String adjustExposure(int amount) {
 		Camera.Parameters parameters = mCamera.getParameters();
 		int current = parameters.getExposureCompensation();
 		current += amount;
 		int min = parameters.getMinExposureCompensation();
 		int max = parameters.getMaxExposureCompensation();
 		if (current < min)
 			current = min;
 		else if (current > max)
 			current = max;
 		parameters.setExposureCompensation(current);
 		mCamera.setParameters(parameters);
 		double exposure = parameters.getExposureCompensationStep() * parameters.getExposureCompensation();
 		exposure = (int)(exposure * 100) / 100d; //limit to two decimal places
 		return "" + exposure;
 	}
 }
