 package com.rosch.frontlight;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.content.Context;
 import android.hardware.Camera;
 import android.hardware.Camera.CameraInfo;
 import android.hardware.Camera.PictureCallback;
 import android.media.MediaScannerConnection;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.Settings.SettingNotFoundException;
 import android.provider.Settings.System;
 import android.view.Surface;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.FrameLayout;
 import android.widget.FrameLayout.LayoutParams;
 import android.widget.Toast;
 
 public class MainActivity extends Activity
 {
 	private boolean mBrightnessSaved = false;
 	private int mBrightnessMode = 0;
 	private int mBrightness = 0;
 
 	private Camera mCamera = null;
 
 	private PictureCallback mPictureCallback = new PictureCallback()
 	{
 		@Override
 		public void onPictureTaken(byte[] data, Camera camera)
 		{
 			File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
 
 			String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
 			File file = new File(path, timestamp + ".jpg");
 
 			try
 			{
 				path.mkdirs();
 
 				OutputStream output = new FileOutputStream(file);
 
 				output.write(data);
 				output.flush();
 				output.close();
 
 				MediaScannerConnection.scanFile(MainActivity.this, new String[] { file.toString() }, null, null);
 			}
 			catch (IOException exception)
 			{
 				Toast.makeText(MainActivity.this, "Unable to save picture!", Toast.LENGTH_SHORT).show();
 			}
 
			Toast.makeText(MainActivity.this, "Saved picture as: " + timestamp + ".jpg", Toast.LENGTH_SHORT).show();

 			camera.startPreview();
 		}
 	};
 
 	class PreviewSurfaceView extends SurfaceView implements SurfaceHolder.Callback
 	{
 		public PreviewSurfaceView(Context context)
 		{
 			super(context);
 
 			getHolder().addCallback(this);
 		}
 
 		@Override
 		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
 		{
 			if (holder.getSurface() == null)
 				return;
 
 			mCamera.stopPreview();
 
 			try
 			{
 				mCamera.setPreviewDisplay(holder);
 				mCamera.startPreview();
 			}
 			catch (IOException exception)
 			{
 
 			}
 		}
 
 		@Override
 		public void surfaceCreated(SurfaceHolder holder)
 		{
 			try
 			{
 				mCamera.setPreviewDisplay(holder);
 				mCamera.startPreview();
 			}
 			catch (IOException exception)
 			{
 
 			}
 		}
 
 		@Override
 		public void surfaceDestroyed(SurfaceHolder holder)
 		{
 
 		}
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.main_activity);
 	}
 
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 
 		try
 		{
 			mBrightnessMode = System.getInt(getContentResolver(), System.SCREEN_BRIGHTNESS_MODE);
 			mBrightness = System.getInt(getContentResolver(), System.SCREEN_BRIGHTNESS);
 
 			mBrightnessSaved = true;
 
 			System.putInt(getContentResolver(), System.SCREEN_BRIGHTNESS_MODE, System.SCREEN_BRIGHTNESS_MODE_MANUAL);
 			System.putInt(getContentResolver(), System.SCREEN_BRIGHTNESS, 255);
 		}
 		catch (SettingNotFoundException exception)
 		{
 			mBrightnessSaved = false;
 		}
 
 		int cameraId = -1;
 		Camera.CameraInfo cameraInfo = new CameraInfo();
 
 		for (int i = 0; i < Camera.getNumberOfCameras(); i++)
 		{
 			Camera.getCameraInfo(i, cameraInfo);
 
 			if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT)
 				cameraId = i;
 		}
 
 		if (cameraId < 0)
 			return;
 
 		try
 		{
 			mCamera = Camera.open(cameraId);
 		}
 		catch (Exception exception)
 		{
 			return;
 		}
 
 		int rotation = getWindowManager().getDefaultDisplay().getRotation() * 90;

 		Camera.Parameters cameraParameters = mCamera.getParameters();

		int rot = (cameraInfo.orientation + rotation + 360) % 360;
		cameraParameters.setRotation(rot);
 
 		mCamera.setParameters(cameraParameters);
 		mCamera.setDisplayOrientation((360 - ((cameraInfo.orientation + rotation) % 360)) % 360);
 
 		PreviewSurfaceView view = new PreviewSurfaceView(this);
 
 		try
 		{
 			mCamera.setPreviewDisplay(view.getHolder());
 		}
 		catch (IOException exception)
 		{
 			return;
 		}
 
 		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.camera_preview);
 		LayoutParams layoutParameters = (LayoutParams) frameLayout.getLayoutParams();
 
 		Camera.Size cameraSize = cameraParameters.getPreviewSize();
 		float scale = getResources().getDisplayMetrics().density;
 
 		layoutParameters.width = (int) (cameraSize.width * scale * 0.25 + 0.5f);
 		layoutParameters.height = (int) (cameraSize.height * scale * 0.25 + 0.5f);
 
 		if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180)
 		{
 			int t = layoutParameters.width;
 
 			layoutParameters.width = layoutParameters.height;
 			layoutParameters.height = t;
 		}
 
 		frameLayout.setLayoutParams(layoutParameters);
 		frameLayout.addView(view);
 
 		findViewById(R.id.button_capture).setOnClickListener(new OnClickListener()
 		{
 			@Override
 			public void onClick(View view)
 			{
 				mCamera.takePicture(null, null, mPictureCallback);
 			}
 		});
 	}
 
 	@Override
 	public void onPause()
 	{
 		super.onPause();
 
 		if (mCamera != null)
 		{
 			mCamera.stopPreview();
 			mCamera.release();
 
 			mCamera = null;
 		}
 
 		if (mBrightnessSaved == true)
 		{
 			System.putInt(getContentResolver(), System.SCREEN_BRIGHTNESS_MODE, mBrightnessMode);
 			System.putInt(getContentResolver(), System.SCREEN_BRIGHTNESS, mBrightness);
 		}
 	}
 }
