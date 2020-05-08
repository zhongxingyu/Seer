 package com.emdoor.autotest;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Context;
 import android.hardware.Camera;
 import android.hardware.Camera.PreviewCallback;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.Surface;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.widget.Toast;
 
 public class CameraView extends SurfaceView implements SurfaceHolder.Callback,
 		Camera.PictureCallback {
 	private static final String TAG = "CameraView";
 	private SurfaceHolder holder;
 	private Camera camera;
 	private Handler handler;
 
 	public CameraView(Activity context, Handler handler) {// 캯
 		super(context);
 		this.handler = handler;
 		holder = getHolder();// Surface Holder
 		holder.addCallback(this);
 
 		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// ָPush Buffer
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {// Surface¼Ĵ
 		try {
 			camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
 			camera.setPreviewDisplay(holder);
 		} catch (Exception e) {
 		}
 	}
 
 	@SuppressLint("NewApi")
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {// Surfaceı¼Ĵ
 		//Commands.buffCameraPhoto=null;
 		if (camera != null) {
 			Camera.Parameters parameters = camera.getParameters();
 			Log.d(TAG, "surfaceChanged width=" + width + ",height=" + height);
 			// parameters.setPreviewSize(320, 240);
			parameters.setPictureSize(1600, 1200);
 			camera.setDisplayOrientation(Surface.ROTATION_0);
			camera.enableShutterSound(true);
 			camera.setParameters(parameters);// ò
 			camera.startPreview();// ʼԤ
 			takePicture();
 		} else {
 			Toast.makeText(getContext(), "camera open fail", Toast.LENGTH_SHORT)
 					.show();
 			if (handler != null) {
 				Message msg = new Message();
 				msg.what = Messages.MSG_PHOTO_TAKEN;
 				handler.sendMessage(msg);
 			}
 			((Activity) getContext()).finish();
 		}
 	}
 
 	public void takePicture() {
 		new Thread(){
 
 			@Override
 			public void run() {
 				try {
					Thread.sleep(1000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				try {
 					camera.takePicture(null, null, CameraView.this);
 				} catch (Exception e) {
 					if (handler != null) {
 						Message msg = new Message();
 						msg.what = Messages.MSG_PHOTO_TAKEN;
 						handler.sendMessage(msg);
 					}
 					((Activity) getContext()).finish();
 				}
 				
 			}
 			
 		}.start();
 		
 	}
 
 	@Override
 	public void onPictureTaken(byte[] data, Camera camera) {// ɺ󱣴Ƭ
 		Log.d(TAG, "onPictureTaken,data length=" + data.length);
 		//Commands.buffCameraPhoto = data;
 
 		if (handler != null) {
 			Message msg = new Message();
 			msg.obj=data;
 			msg.what = Messages.MSG_PHOTO_TAKEN;
 			handler.sendMessage(msg);
 		}
 		((Activity) getContext()).finish();
 
 		// camera.startPreview();
 
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 
 		if (camera != null) {
 			camera.setPreviewCallback(null);
 			camera.stopPreview();
 			camera.release();
 			camera = null;
 		}
 	}
 
 }
