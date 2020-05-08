 package edu.tamu.csce470.mir;
 
 import java.io.File;
 import java.io.FileOutputStream;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.hardware.Camera;
 import android.hardware.Camera.PictureCallback;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.FrameLayout;
 
 public class ImageCaptureActivity extends Activity
 {
 	private Camera camera;
 	private PreviewImageCaptureView preview;
 	private ImageCaptureOverlayView overlay;
 	
 	private PictureCallback picture = new PictureCallback()
 	{		
 		@Override
 		public void onPictureTaken(byte[] data, Camera camera)
 		{
 			savePicture(data);
 		}
 	};
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		
 		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setContentView(R.layout.activity_image_capture);
 		
 		this.camera = getCameraInstance();
 		applyCameraSettings();
 		
 		CalibrationSettings settings = null;
 		if (getIntent().getExtras().containsKey("calibrationSettings"))
 		{
 			settings = (CalibrationSettings) getIntent().getExtras().getSerializable("calibrationSettings");
 		}
 		
 		this.preview = new PreviewImageCaptureView(this, this.camera);
 		FrameLayout previewLayout = (FrameLayout) findViewById(R.id.imageCapturePreview);
 		previewLayout.addView(this.preview);
 		
 		this.overlay = new ImageCaptureOverlayView(this, settings);
 		FrameLayout overlayLayout = (FrameLayout) findViewById(R.id.imageCaptureOverlay);
 		overlayLayout.addView(this.overlay);
 	}
 	
 	@Override
 	protected void onPause()
 	{
 		super.onPause();
 		
 		if (this.camera != null)
 		{
 			this.camera.release();
 			this.camera = null;
 		}
 	}
 	
 	@Override
 	protected void onResume()
 	{
 		super.onResume();
 		
 		if (this.camera == null)
 		{
 			this.camera = getCameraInstance();
 		}
 	}
 	
 	private Camera getCameraInstance()
 	{
 		Camera c = null;
 		try
 		{
 			c = Camera.open();
 		}
 		catch (Exception e)
 		{
 			Log.d("ImageCaptureActivity", "Failed to get a camera instance: " + e);
 		}
 		
 		return c;
 	}
 	
 	private void applyCameraSettings()
 	{
 		if (this.camera != null)
 		{
 			Camera.Parameters params = this.camera.getParameters();
 			params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
			Camera.Size size = params.getSupportedPictureSizes().get(0);
 			params.setPictureSize(size.width, size.height);
 			this.camera.setParameters(params);
 		}
 	}
 	
 	private void savePicture(byte[] data)
 	{
 		Intent intent = getIntent();
 		Uri pictureUri = (Uri) intent.getExtras().getParcelable(MediaStore.EXTRA_OUTPUT);
 		File pictureFile = new File(pictureUri.getPath());
 		
 		try
 		{
 			FileOutputStream fos = new FileOutputStream(pictureFile);
 			fos.write(data);
 			fos.close();
 		}
 		catch (Exception e)
 		{
 			Log.d("ImageCaptureActivity", "Could not save file: " + e.getMessage());
 		}
 		
 		setResult(RESULT_OK);
 		finish();
 	}
 	
 	public void onTakePicture(View view)
 	{
 		this.camera.takePicture(null, null, this.picture);
 	}
 }
