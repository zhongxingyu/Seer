 package com.datakom;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.PixelFormat;
 import android.hardware.Camera;
 import android.hardware.Camera.PreviewCallback;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import com.datakom.POIObjects.HaggleConnector;
 
 class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
 	private static final String TAG = "Preview";
 
 	SurfaceHolder mHolder;
 	public Camera camera;
 
 	CameraPreview(Context context) {
 		super(context);
 
 		// Install a SurfaceHolder.Callback so we get notified when the
 		// underlying surface is created and destroyed.
 		mHolder = getHolder();
 		mHolder.addCallback(this);
 		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 	}
 
 	public void surfaceCreated(SurfaceHolder holder) {
 		// The Surface has been created, acquire the camera and tell it where
 		// to draw.
 		camera = Camera.open();
 		try {
 
 			camera.setPreviewDisplay(holder);
 //			camera.setPreviewCallback(new PreviewCallback() {
 //
 //				public void onPreviewFrame(byte[] data, Camera arg1) {
 //					FileOutputStream outStream = null;
 //					try {
 //						outStream = new FileOutputStream(String.format(
 //								HaggleConnector.STORAGE_PATH+"/%d.jpg", System.currentTimeMillis()));
 //						outStream.write(data);
 //						outStream.close();
 //						Log.d(TAG, "onPreviewFrame - wrote bytes: "
 //								+ data.length);
 //					} catch (FileNotFoundException e) {
 //						Log.e(getClass().getSimpleName(), e.getMessage());
 //					} catch (IOException e) {
 //						Log.e(getClass().getSimpleName(), e.getMessage());
 //					} finally {
 //					}
 //					CameraPreview.this.invalidate();
 //				}
 //			});
 		} catch (IOException e) {
 			Log.e(getClass().getSimpleName(), e.getMessage());
 		}
 	}
 
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		// Surface will be destroyed when we return, so stop the preview.
 		// Because the CameraDevice object is not a shared resource, it's very
 		// important to release it when the activity is paused.
 		camera.stopPreview();
		camera.release();
 		camera = null;
 	}
 
 	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
 		// Now that the size is known, set up the camera parameters and begin
 		// the preview.
 		Camera.Parameters parameters = camera.getParameters();
 		List<Camera.Size> previewSize;
 		previewSize = parameters.getSupportedPreviewSizes();
 		parameters.setPreviewSize(previewSize.get(3).width, previewSize.get(3).height);
 		camera.setParameters(parameters);
 		camera.startPreview();
 	}
 
 	@Override
 	public void draw(Canvas canvas) {
 		super.draw(canvas);
 		Paint p = new Paint(Color.RED);
 		Log.d(TAG, "draw");
 		canvas.drawText("PREVIEW", canvas.getWidth() / 2,
 				canvas.getHeight() / 2, p);
 	}
 }
