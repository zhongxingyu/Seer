 package com.cellcity.citiguide.ar;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.hardware.Camera;
 import android.util.AttributeSet;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public class CustomCameraView extends SurfaceView {
 	Camera camera;
 	SurfaceHolder previewHolder;
 	Context context;
 
 	// Callback for the surfaceholder
 	SurfaceHolder.Callback surfaceHolderListener = new SurfaceHolder.Callback() {
 		public void surfaceCreated(SurfaceHolder holder) {
 			camera = Camera.open();
 
 			try {
 				camera.setPreviewDisplay(previewHolder);
 			} catch (Throwable t) {
 
 			}
 		}
 
 		public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int w, int h) {
 
 			Camera.Parameters parameters = camera.getParameters();
			parameters.setPreviewSize(w, h);
 			camera.setParameters(parameters);
 			camera.startPreview();
 		}
 
 		public void surfaceDestroyed(SurfaceHolder arg0) {
 			camera.release();
 			camera = null;
 		}
 	};
 
 	public CustomCameraView(Context ctx) {
 		super(ctx);
 		context = ctx;
 		previewHolder = this.getHolder();
 		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 		previewHolder.addCallback(surfaceHolderListener);
 		setBackgroundColor(Color.TRANSPARENT);
 	}
 
 	public CustomCameraView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 	}
 
 	public void closeCamera() {
 		if (camera != null)
 			camera.release();
 	}
 
 	public void dispatchDraw(Canvas c) {
 		super.dispatchDraw(c);
 	}
 }
