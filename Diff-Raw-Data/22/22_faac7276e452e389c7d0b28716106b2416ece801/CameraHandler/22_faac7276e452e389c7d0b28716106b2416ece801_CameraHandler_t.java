 package com.msse.robot_cowboy;
 
 import android.content.Context;
 import android.hardware.Camera;
 import android.hardware.Camera.PictureCallback;
 
 public class CameraHandler implements PictureCallback {
 	
 	private static final String TAG = CameraHandler.class.getSimpleName();
 	private final Context context;
 
 	public CameraHandler(Context context) {
 		this.context = context;
 	}
 
 	@Override
 	public void onPictureTaken(byte[] data, Camera camera) {
		PhotoQueue.getInstance().add(data);
 	}
 }
