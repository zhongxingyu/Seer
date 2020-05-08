 package com.freespeech;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.ViewGroup;
 
 public class VideoFragment extends Fragment {
 
 	private SurfaceView preview;
 	private SurfaceHolder previewHolder;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		View view = inflater.inflate(R.layout.video, container, false);
 
 		preview = (SurfaceView) view.findViewById(R.id.surfaceView1);
 		previewHolder = preview.getHolder();
 		previewHolder.addCallback(surfaceCallback);
 
 		return view;
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
			// initPreview(width, height);
			// startPreview();
 
 		}
 	};
 }
