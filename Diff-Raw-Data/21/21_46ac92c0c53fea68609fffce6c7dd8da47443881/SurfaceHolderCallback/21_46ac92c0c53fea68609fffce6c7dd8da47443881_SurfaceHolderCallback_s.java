 package com.pyrobot.droid;
 
 import java.net.Socket;
 import java.util.List;
 
 import android.graphics.ImageFormat;
 import android.graphics.Rect;
 import android.graphics.YuvImage;
 import android.hardware.Camera;
 import android.hardware.Camera.Parameters;
 import android.util.Log;
 import android.view.SurfaceHolder;
 
 public class SurfaceHolderCallback implements SurfaceHolder.Callback{
 
 	protected static final String TAG = "SurfaceHolder";
 	public Camera mCamera;
 	public SurfaceHolder holder;
 	private int width = 352;
 	private int height = 288;
 	
 	public SurfaceHolderCallback(SurfaceHolder holder) {
 		this.holder = holder;
 	}
 	public void initCamera(){
 		mCamera = Camera.open();
 	    Parameters params = mCamera.getParameters();
 	    params.setPreviewSize(width, height);
 	    
 	    List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();
 	    for(int i=0; i < previewSizes.size();i++){
 	    	Log.i(TAG,previewSizes.get(i).width + " x " + previewSizes.get(i).height);
 	    }
 	    params.setPreviewFormat(ImageFormat.NV21);
 	    //params.setFlashMode(Parameters.FLASH_MODE_ON);
 	    mCamera.setParameters(params);
 
 		try {
 			mCamera.setPreviewCallback(mPreviewCallback);
 			mCamera.setPreviewDisplay(holder);
 		} catch (Exception e) {
 			Log.e("exception", e.toString());
 			return;
 		}
 		mCamera.startPreview();
 	}
 
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		initCamera();
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		// TODO Auto-generated method stub
		//mCamera.stopPreview();
		if(mCamera != null)
			mCamera.release();
 	}
 	
 	Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
 		private int COMPRESSION_RATE = 20;
 
 		@Override
 		public void onPreviewFrame(byte[] data, Camera camera) {
 			try{
 				YuvImage yuvi = new YuvImage(data, ImageFormat.NV21, width, height, null);
 				Rect rect = new Rect(0,0,width,height);
 
 				BOutputStream bos = new BOutputStream();
 				yuvi.compressToJpeg(rect, COMPRESSION_RATE , bos);
 				for (Socket client : RobotServer.clients ) {
 					bos.send_udp(client.getInetAddress().getHostName(), VideoDecodeThread.PORT);
 				}
 				//Log.i(TAG, "sent frame");
 			} catch (Exception e) {
 				Log.e(TAG, e.toString());
 			};
 		}
 	};
 }
