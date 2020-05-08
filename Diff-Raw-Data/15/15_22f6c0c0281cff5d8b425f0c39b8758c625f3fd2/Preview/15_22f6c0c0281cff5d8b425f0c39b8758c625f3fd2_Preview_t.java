 package com.ivl.cviewer;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.Config;
 import android.graphics.ImageFormat;
 import android.hardware.Camera;
 import android.hardware.Camera.PreviewCallback;
 import android.os.Environment;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 import com.ivl.network.ServerConnection;
 
 
 class Preview extends SurfaceView implements SurfaceHolder.Callback {
 	private static String TAG = "Preview";
     SurfaceHolder holder_;
     Camera camera_;
     ServerConnection server_;
 	private static int count_ = 0;
 	private boolean sendData_;
     
     Preview(Context context, ServerConnection server) {
         super(context);
         // Install a SurfaceHolder.Callback so we get notified when the
         // underlying surface is created and destroyed.
         holder_ = getHolder();
         holder_.addCallback(this);
         holder_.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
         setFocusable(true);
         setFocusableInTouchMode(true);
         setClickable(true);
 
         if (server == null) {
         	Log.e(TAG, "Passed bad server socket to " + TAG);
         }
         server_ = server;
         sendData_ = false;
     }
 
     public void sendData() {
     	sendData_ = true;
     }
     
     public void stopData() {
     	sendData_ = false;
     }
     
     public boolean isSending() {
     	return sendData_;
     }
     
     public void surfaceCreated(SurfaceHolder holder) {
         // The Surface has been created, acquire the camera and tell it where
         // to draw.
         camera_ = Camera.open();
         try {
            camera_.setPreviewDisplay(holder);
         } catch (IOException exception) {
             camera_.release();
             camera_ = null;
             // TODO: add more exception handling logic here
         }
     }
 
     // Surface will be destroyed when we return, so stop the preview.
     // Because the CameraDevice object is not a shared resource, it's very
     // important to release it when the activity is paused.
     public void surfaceDestroyed(SurfaceHolder holder) {
     	camera_.setPreviewCallback(null);
         camera_.stopPreview();
         camera_.release();
         camera_ = null;
     }
     
     static public void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {
         final int frameSize = width * height;
 
         for (int j = 0, yp = 0; j < height; j++) {
             int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
             for (int i = 0; i < width; i++, yp++) {
                 int y = (0xff & ((int) yuv420sp[yp])) - 16;
                 if (y < 0) y = 0;
                 if ((i & 1) == 0) {
                     v = (0xff & yuv420sp[uvp++]) - 128;
                     u = (0xff & yuv420sp[uvp++]) - 128;
                 }
                 int y1192 = 1192 * y;
                 int r = (y1192 + 1634 * v);
                 int g = (y1192 - 833 * v - 400 * u);
                 int b = (y1192 + 2066 * u);
 
                 if (r < 0) r = 0; else if (r > 262143) r = 262143;
                 if (g < 0) g = 0; else if (g > 262143) g = 262143;
                 if (b < 0) b = 0; else if (b > 262143) b = 262143;
 
                 rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
             }
         }
     }
 
 
     protected byte[] NV21ToScaledJPEG(byte[] bitmap, int newWidth, int newHeight) {
     	Log.d(TAG, "num yuv bytes: " + bitmap.length);
 		Log.d(TAG, "storing: " + Environment.getExternalStorageDirectory().getAbsolutePath()
 				 + "\nstate: " + Environment.getExternalStorageState());
 		File root = Environment.getExternalStorageDirectory();
 		Camera.Parameters parameters = camera_.getParameters();
 		byte[] jpegBytes = null;
 
 		int w = parameters.getPreviewSize().width;
 		Log.d(TAG, "parameter width " + w);
         int h = parameters.getPreviewSize().height;
         Log.d(TAG, "parameter height " + h);
 		int rgb[] = new int[w*h];
 		decodeYUV420SP(rgb, bitmap, w, h);
 		Bitmap bm = Bitmap.createBitmap(rgb, w, h, Config.ARGB_8888);
 		if (bm != null) {
 			Log.d(TAG, "success creating bm " + bm); 
 			Bitmap scaledBm = Bitmap.createScaledBitmap(bm, newWidth, newHeight, false);
 			  ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		        if (scaledBm.compress(Bitmap.CompressFormat.JPEG, 90, byteStream)) {
 		        	Log.d(TAG, "Success compressing");
 		        	jpegBytes = byteStream.toByteArray();
 		            Log.d(TAG, "num jpeg bytes: " + jpegBytes.length);
 		            
 		            /*
 		            // For debugging yo
 		     		FileOutputStream fileStream;
 					try {
 						fileStream = new FileOutputStream(root + "/" + "image" + count_ + ".jpg");
 						fileStream.write(jpegBytes);
 						fileStream.flush();
 						fileStream.close();
 					} catch (FileNotFoundException e) {
 						Log.d(TAG, "file not found" + e.getMessage());
 						e.printStackTrace();
 					} catch (IOException e) {
 						Log.d(TAG, "io exception " + e.getMessage());
 						e.printStackTrace();
 					}
 					*/
 		        }
 			
 		}
 		
 		return jpegBytes;
     }
     
     // TODO: should use preview frame rate?
     public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
         Camera.Parameters parameters = camera_.getParameters();
 //        parameters.setPreviewFrameRate(15);
 //        parameters.setSceneMode(Camera.Parameters.SCENE_MODE_NIGHT);
         parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
         camera_.setParameters(parameters);
     	
     	if (camera_ == null) {
     		Log.e(TAG, "No camera for preview!");
     		return;
     	}
     	
     	camera_.setPreviewCallback(new PreviewCallback() {
     		
     		// Send preview frames to server
 			@Override
 			public void onPreviewFrame(byte[] data, Camera camera) {
 				
 				++count_;
 				
 				// no point in sending goodies
 				if (!sendData_ || server_ == null) return;
 				
 				// Don't want to overload the server
 				if (count_ % 15 != 0) return;
 
 				Log.d(TAG, "on preview frame " + count_);
 				if (camera_ == null) {
 					Log.e(TAG, "no camera for preview frame!");
 					return;
 				}
 				
 				Camera.Parameters parameters = camera_.getParameters();
 				int imageFormat = parameters.getPreviewFormat();
                 if (imageFormat == ImageFormat.NV21) {
    				byte[] jpegBytes = NV21ToScaledJPEG(data, 320, 240);
                 	server_.sendPreviewFrame(jpegBytes);
                 } else if (imageFormat == ImageFormat.JPEG || imageFormat == ImageFormat.RGB_565) {
                 	Log.e(TAG, "TODO: image format JPEG or rgb");
                 }
 			} 
 			
     	});
     	
         camera_.startPreview();
     }
  
 }
