 package com.android.cameraproject.camera;
 
 import java.io.IOException;
 
 import android.content.Context;
 import android.hardware.Camera;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 // This class is used in CameraActivity to show what the camera is looking at
 public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback
 {
 	private SurfaceHolder mHolder;
 	private Camera mCamera;
 	
 	@SuppressWarnings("deprecation")
 	public CameraPreview(Context context, Camera camera)
 	{
 		super(context);
 		mCamera = camera;
 		
 		mHolder = getHolder();
 		mHolder.addCallback(this);
 		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 		
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) 
 	{
 		try
 		{
 			mCamera.setPreviewDisplay(holder);
 			mCamera.startPreview();
 		}
 		catch(IOException e)
 		{
 			Log.d("CameraPreview", "ERROR SETTING CAMERA PREVIEW: " + e.getMessage());
 		}
 		
 	}
 	
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) 
 	{
 		if(mHolder.getSurface() == null)
 		{
 			return;
 		}
 		
 		try
 		{
 			mCamera.stopPreview();
 		}
 		catch(Exception e)
		{}
		
		// Here's some stuff to deal with that pesky rotating business
		
		/*
		 
		Context context = this.getContext();
		Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		
		if(display.getRotation() == Surface.ROTATION_0)
 		{
			mCamera.setDisplayOrientation(90);
 		}
 		
		*/
		
 		
 		// Start preview
 		
 		try
 		{
 			mCamera.setPreviewDisplay(mHolder);
 			mCamera.startPreview();
 		}
 		catch(Exception e)
 		{
 			Log.d("CameraPreview", "ERROR STARTING CAMERA PREVIEW: " + e.getMessage());
 		}	
 		
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder arg0) 
 	{
 		
 	}
 }
