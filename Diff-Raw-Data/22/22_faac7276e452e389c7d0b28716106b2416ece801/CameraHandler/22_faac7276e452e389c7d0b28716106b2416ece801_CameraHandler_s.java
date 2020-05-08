 package com.msse.robot_cowboy;
 
 import android.content.Context;
 import android.hardware.Camera;
 import android.hardware.Camera.PictureCallback;
import android.util.Log;
import android.widget.Toast;
 
 public class CameraHandler implements PictureCallback {
 	
 	private static final String TAG = CameraHandler.class.getSimpleName();

 	private final Context context;
 
 	public CameraHandler(Context context) {
 		this.context = context;
 	}
 
 	@Override
 	public void onPictureTaken(byte[] data, Camera camera) {
		try {
			Toast.makeText(context, "New Image captured",
					Toast.LENGTH_LONG).show();
			PhotoQueue.getInstance().add(data);
		} catch (Exception error) {
			Log.d(TAG, "File not saved: " + error.getMessage());
			Toast.makeText(context, "Image could not be saved.",
					Toast.LENGTH_LONG).show();
		}
 	}
 }
