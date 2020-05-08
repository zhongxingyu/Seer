 package com.lspr;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Service;
import android.content.Context;
 import android.content.Intent;
 import android.hardware.Camera;
 import android.hardware.Camera.PictureCallback;
import android.media.AudioManager;
 import android.os.Environment;
 import android.os.IBinder;
 import android.util.Log;
 import android.view.SurfaceView;
 import android.widget.FrameLayout;
 import android.widget.Toast;
 
 public class CameraService extends Service{
 	private static final String TAG = "MyService";
 	
 	/** A safe way to get an instance of the Camera object. */
 	public static Camera getCameraInstance() {
 		Camera c = null;
 		try {
 			// 1 as the parameter is for the front camera, 0 is for back camera
 			c = Camera.open(1); // attempt to get a Camera instance
 		} catch (Exception e) {
 			// Camera is not available (in use or does not exist)
 		}
 		return c; // returns null if camera is unavailable
 	}
 
 	private void releaseCamera() {
 		if (mCamera != null) {
 			mCamera.release(); // release the camera for other applications
 			mCamera = null;
 		}
 	}
 
 	/** Create a File for saving an image or video */
 	private static File getOutputMediaFile(int type) {
 		// To be safe, you should check that the SDCard is mounted
 		// using Environment.getExternalStorageState() before doing this.
 
 		File mediaStorageDir = new File(
 				Environment
 						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
 				"RecoveryPicturesNew");
 		// This location works best if you want the created images to be shared
 		// between applications and persist after your app has been uninstalled.
 
 		// Create the storage directory if it does not exist
 		if (!mediaStorageDir.exists()) {
 			if (!mediaStorageDir.mkdirs()) {
 				Log.d("MyCameraApp", "failed to create directory");
 				return null;
 			}
 		}
 
 		// Create a media file name
 		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
 				.format(new Date());
 		File mediaFile;
 		if (type == MEDIA_TYPE_IMAGE) {
 			mediaFile = new File(mediaStorageDir.getPath() + File.separator
 					+ "IMG_" + timeStamp + ".jpg");
 		} else if (type == MEDIA_TYPE_VIDEO) {
 			mediaFile = new File(mediaStorageDir.getPath() + File.separator
 					+ "VID_" + timeStamp + ".mp4");
 		} else {
 			return null;
 		}
 
 		return mediaFile;
 	}
 
 	public static final int MEDIA_TYPE_IMAGE = 1;
 	public static final int MEDIA_TYPE_VIDEO = 2;
 	private Camera mCamera;
 	// picture call back start
 	private PictureCallback mPicture = new PictureCallback() {
 
 		@Override
 		public void onPictureTaken(byte[] data, Camera camera) {
 
 			File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
 			if (pictureFile == null) {
 				// Log.d(TAG,
 				// "Error creating media file, check storage permissions: " +
 				// e.getMessage());
 				return;
 			}
 
 			try {
 				FileOutputStream fos = new FileOutputStream(pictureFile);
 				fos.write(data);
 				fos.close();
 			} catch (FileNotFoundException e) {
 				// Log.d(TAG, "File not found: " + e.getMessage());
 			} catch (IOException e) {
 				// Log.d(TAG, "Error accessing file: " + e.getMessage());
 			}
 		}
 	};
 	
 	
 	
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 	
 	@Override
 	public void onCreate() {
 		Toast.makeText(this, "My Service Created", Toast.LENGTH_LONG).show();
 		Log.d(TAG, "onCreate");
 		
 
 		
 		
 		
 	}
 
 	@Override
 	public void onDestroy() {
 		Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
 		Log.d(TAG, "onDestroy");
 		releaseCamera();
 	}
 	
 	@Override
 	public void onStart(Intent intent, int startid) {
 		Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();
 		Log.d(TAG, "onStart");
 		
 		// Create an instance of Camera
 		mCamera = getCameraInstance();
 
		SurfaceView view = new SurfaceView(this.getApplicationContext());
 		try {
 			mCamera.setPreviewDisplay(view.getHolder());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		mCamera.startPreview();
		//change ringer to silent to disable shutter sound
		AudioManager am = (AudioManager) this.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
	
		am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		//take picture 
 		mCamera.takePicture(null, null, mPicture);
		//return to normal sound settings
		
 
 	}
 }
