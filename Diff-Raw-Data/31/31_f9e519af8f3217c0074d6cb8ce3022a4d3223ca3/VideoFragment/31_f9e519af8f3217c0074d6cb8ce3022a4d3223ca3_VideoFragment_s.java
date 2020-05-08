 package com.freespeech;
 
 import android.hardware.Camera;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Toast;
 
 public class VideoFragment extends Fragment {
 
 	private SurfaceView preview;
 	private SurfaceHolder previewHolder;
 	private Camera camera = null;
 	private boolean cameraConfigured = false;
 	private boolean inPreview = false;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		View view = inflater.inflate(R.layout.video, container, false);
 
 		preview = (SurfaceView) view.findViewById(R.id.surfaceView1);
 		previewHolder = preview.getHolder();
 		previewHolder.addCallback(surfaceCallback);
 
 		return view;
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 
		camera = Camera.open();
 		startPreview();
 	}
 
 	@Override
 	public void onPause() {
 		if (inPreview) {
 			camera.stopPreview();
 		}
 
 		camera.release();
 		camera = null;
 		inPreview = false;
 
 		super.onPause();
 	}
 
 	private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
 
 		@Override
 		public void surfaceDestroyed(SurfaceHolder holder) {
 			// no-op
 		}
 
 		@Override
 		public void surfaceCreated(SurfaceHolder holder) {
 			// no-op -- wait until surfaceChanged()
 			// TODO understand why this has to be NO-OP
 
 		}
 
 		@Override
 		public void surfaceChanged(SurfaceHolder holder, int format, int width,
 				int height) {
 			// TODO understand why these methods have to be called in this
 			// callback method
 			initPreview(width, height);
 			startPreview();
 		}
 	};
 
 	private void initPreview(int width, int height) {
 		if (camera != null && previewHolder.getSurface() != null) {
 			try {
 				camera.setPreviewDisplay(previewHolder);
 			} catch (Throwable t) {
 				Log.e("PreviewDemo-surfaceCallback",
 						"Exception in setPreviewDisplay()", t);
 				Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG)
 						.show();
 			}
 
 			if (!cameraConfigured) {
 				Camera.Parameters parameters = camera.getParameters();
 				Camera.Size size = getBestPreviewSize(width, height, parameters);
 
 				if (size != null) {
 					parameters.setPreviewSize(size.width, size.height);
 					camera.setParameters(parameters);
 					cameraConfigured = true;
 				}
 			}
 		}
 	}
 
 	private void startPreview() {
 		if (cameraConfigured && camera != null) {
 			camera.startPreview();
 			inPreview = true;
 		}
 	}
 
 	private Camera.Size getBestPreviewSize(int width, int height,
 			Camera.Parameters parameters) {
 		Camera.Size result = null;
 
 		for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
 			if (size.width <= width && size.height <= height) {
 				if (result == null) {
 					result = size;
 				} else {
 					int resultArea = result.width * result.height;
 					int newArea = size.width * size.height;
 
 					if (newArea > resultArea) {
 						result = size;
 					}
 				}
 			}
 		}
 
 		return (result);
 	}
 }
