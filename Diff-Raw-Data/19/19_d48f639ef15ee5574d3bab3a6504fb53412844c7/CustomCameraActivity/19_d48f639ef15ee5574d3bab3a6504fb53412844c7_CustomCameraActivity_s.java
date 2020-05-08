 package mit.edu.stemplusplus;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 
 import mit.edu.stemplusplus.helper.MyHorizontalLayout;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.BitmapFactory.Options;
 import android.graphics.Matrix;
 import android.graphics.PixelFormat;
 import android.hardware.Camera;
 import android.hardware.Camera.PictureCallback;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ImageButton;
 import android.widget.ListView;
 
 /**
  * This class is to create a customized camera that let the user has more
  * options when taking picture
  * 
  * @author trannguyen
  * 
  */
 public class CustomCameraActivity extends StemPlusPlus implements
 		SurfaceHolder.Callback {
 
 	protected static final int MEDIA_TYPE_IMAGE = 1;
 	protected static final int REQUEST_CODE = 10;
 
 	Camera camera;
 
 	/** the view of the camera */
 	SurfaceView surfaceView = null;
 
 	/** camera holder */
 	SurfaceHolder surfaceHolder;
 
 	/** constant to check if the camera is in the preview mode */
 	boolean previewing = false;
 
 	/** inflater to create view from xml file */
 	LayoutInflater controlInflater = null;
 
 	Activity act = this;
 
 	/** layout to add the image horizontally */
 	MyHorizontalLayout myHorizontalLayout;
 
 	/** Array to hold the displayed image */
 	ArrayList<String> activityPicture = new ArrayList<String>();
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_custom_camera);
 		Log.d("load layout", "Ok");
 		// get the String of array image from gallery
 
 		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 
 		getWindow().setFormat(PixelFormat.UNKNOWN);
 
 		surfaceView = (SurfaceView) findViewById(R.id.camerapreview);
 
 		surfaceHolder = surfaceView.getHolder();
 
 		surfaceHolder.addCallback(this);
 
 		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 
 		controlInflater = LayoutInflater.from(getBaseContext());
 		View viewControl = controlInflater
 				.inflate(R.layout.custom_camera, null);
 
 		LayoutParams layoutParamsControl = new LayoutParams(
 				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
 		this.addContentView(viewControl, layoutParamsControl);
 
 		ImageButton galery = (ImageButton) findViewById(R.id.gallery_customized_camera);
 
 		Log.d("horizontal Layout", "ok");
 		myHorizontalLayout = (MyHorizontalLayout) findViewById(R.id.mygallery);
 
 		// if there is image already in the activity Picture;
 		myHorizontalLayout.setArrayList(activityPicture);
 
 		Intent intent = getIntent();
 		if (intent.getStringArrayListExtra(GALLERY_INTENT) != null) {
 			ArrayList<String> imagePath = intent
 					.getStringArrayListExtra(GALLERY_INTENT);
 			for (String i : imagePath) {
 				Log.d("image here", i);
 				myHorizontalLayout.add(i);
 			}
 		}
 		galery.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(v.getContext(),
 						CustomizedGallery.class);
 				intent.putStringArrayListExtra(IMAGE_INTENT, activityPicture);
 				startActivity(intent);
 
 			}
 		});
 
 		ImageButton shutter = (ImageButton) findViewById(R.id.shutter_customized_camera);
 
 		shutter.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				camera.takePicture(null, null, myPictureCallback_JPG);
 
 				Log.d("take picture after", "ok");
 			}
 		});
 
 		ImageButton done = (ImageButton) findViewById(R.id.done_customized_camera);
 
 		done.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (activityPicture.size() > 0) {
 					Intent i = new Intent(v.getContext(),
 							AllProjectDisplayActivity.class);
 
 					i.putStringArrayListExtra(IMAGE_INTENT, activityPicture);
 					startActivity(i);
 					finish();
 				}
 
 			}
 		});
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		if (previewing) {
 			camera.stopPreview();
 			previewing = false;
 		}
 
 		if (camera != null) {
 			try {
 				camera.setPreviewDisplay(surfaceHolder);
 				camera.startPreview();
 				previewing = true;
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		camera = Camera.open();
 		try {
 			Camera.Parameters parameters = camera.getParameters();
 			if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
 				// This is an undocumented although widely known feature
 				parameters.set("orientation", "portrait");
 				// For Android 2.2 and above
 				camera.setDisplayOrientation(90);
 
 				// Uncomment for Android 2.0 and above
 				// parameters.setRotation(90);
 			} else {
 				// This is an undocumented although widely known feature
 				parameters.set("orientation", "landscape");
 				// For Android 2.2 and above
 				camera.setDisplayOrientation(0);
 				// Uncomment for Android 2.0 and above
 				// parameters.setRotation(0);
 			}
 
 			camera.setParameters(parameters);
 			camera.setPreviewDisplay(holder);
 		} catch (IOException exception) {
 			camera.release();
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 		releaseCamera();
 	}
 
 	private void releaseCamera() {
 		if (camera != null) {
 			camera.stopPreview();
 			camera.release();
 			camera = null;
 			previewing = false;
 		}
 	}
 
 	private static Uri getOutputMediaFireUri(int type) {
 		return Uri.fromFile(getOutputMediaFile(type));
 	}
 
 	/**
 	 * Create a new file to store the image in the storage
 	 * 
 	 * @param type
 	 *            the type of the file (i.e IMAGE, VIDEO)
 	 * @return the File for storage
 	 */
 	private static File getOutputMediaFile(int type) {
 		File mediaStorageDir = new File(
 				Environment
 						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
 				"CustomizedCam");
 
 		if (!mediaStorageDir.exists()) {
 			if (!mediaStorageDir.mkdirs()) {
 				Log.d("CustomizedCam", "failed to create directory");
 				return null;
 			}
 		}
 
 		String timeStamp = new SimpleDateFormat("MM-dd-yyyy: HH:mm:ss")
 				.format(new Date());
 		File mediaFile;
 		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".jpeg");
			Log.d("create media file", mediaStorageDir.getPath()
					+ File.separator + "IMG_" + timeStamp + ".jpeg");
 		} else {
 			return null;
 		}
 
 		return mediaFile;
 	}
 
 	/**
 	 * Call back to take action when picture is taken
 	 */
 	private PictureCallback myPictureCallback_JPG = new PictureCallback() {
 		@Override
 		public void onPictureTaken(byte[] arg0, Camera arg1) {
 			Bitmap bMap;
 			int orientation;
 			BitmapFactory.Options options = new BitmapFactory.Options();
 			options.inSampleSize = 6;
 			options.inDither = false; // Disable Dithering mode
 			options.inPurgeable = true; // Tell to gc that whether it needs free
 										// memory, the Bitmap can be cleared
 			options.inInputShareable = true; // Which kind of reference will be
 												// used to recover the Bitmap
 												// data after being clear, when
 												// it will be used in the future
 			options.inTempStorage = new byte[32 * 1024];
 			options.inPreferredConfig = Bitmap.Config.RGB_565;
 			bMap = BitmapFactory.decodeByteArray(arg0, 0, arg0.length, options);
 
 			// others devices
 			if (bMap.getHeight() < bMap.getWidth()) {
 				orientation = 90;
 			} else {
 				orientation = 0;
 			}
 
 			Bitmap bMapRotate;
 			if (orientation != 0) {
 				Matrix matrix = new Matrix();
 				matrix.postRotate(orientation);
 				bMapRotate = Bitmap.createBitmap(bMap, 0, 0, bMap.getWidth(),
 						bMap.getHeight(), matrix, true);
 			} else
 				bMapRotate = Bitmap.createScaledBitmap(bMap, bMap.getWidth(),
 						bMap.getHeight(), true);
 
 			File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
 			if (pictureFile == null) {
 				Log.d("bad luck",
 						"Error creating media file, check storage permissions");
 				return;
 			}
 
 			FileOutputStream out;
 			try {
 				out = new FileOutputStream(pictureFile);
 				bMapRotate.compress(Bitmap.CompressFormat.JPEG, 90, out);
 				if (bMapRotate != null) {
 					bMapRotate.recycle();
 					bMapRotate = null;
 				}
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			myHorizontalLayout.add(pictureFile.getPath());
 			sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
 					Uri.parse("file://"
 							+ Environment.getExternalStorageDirectory())));
 			camera.startPreview();
 		}
 	};
 
 	/**
 	 * Call back to take action when picture is taken
 	 */
 	/*
 	 * private PictureCallback mPicture = new PictureCallback() {
 	 * 
 	 * @Override public void onPictureTaken(byte[] data, Camera arg1) { File
 	 * pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE); if (pictureFile ==
 	 * null) { Log.d("bad luck",
 	 * "Error creating media file, check storage permissions"); return; }
 	 * 
 	 * try {
 	 * 
 	 * Matrix mat = new Matrix(); mat.postRotate(90);
 	 * 
 	 * Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length); Bitmap
 	 * correctBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
 	 * bmp.getHeight(), mat, true);
 	 * 
 	 * 
 	 * Log.d("save file before", pictureFile.getPath()); // create output stream
 	 * to store picture to the file FileOutputStream fos = new
 	 * FileOutputStream(pictureFile); //BufferedOutputStream bos = new
 	 * BufferedOutputStream(fos); //
 	 * correctBmp.compress(Bitmap.CompressFormat.PNG, 50, bos); // bos.flush();
 	 * // bos.close(); fos.write(data); fos.close(); sendBroadcast(new
 	 * Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" +
 	 * Environment.getExternalStorageDirectory()))); Log.d("save file",
 	 * pictureFile.getPath());
 	 * 
 	 * // add the picture to the horizontal layout
 	 * myHorizontalLayout.add(pictureFile.getPath());
 	 * 
 	 * 
 	 * fos.close(); } catch (FileNotFoundException e) { Log.d("bad luck",
 	 * "File not found:" + e.getMessage()); } catch (IOException e) {
 	 * Log.d("bad luck", "Error accessing file: " + e.getMessage()); } try {//
 	 * sleep for 300s to wait for the picture to be done saving
 	 * Thread.sleep(300); } catch (InterruptedException e) { // TODO
 	 * Auto-generated catch block e.printStackTrace(); } // let the user
 	 * continue taking picture camera.setDisplayOrientation(90);
 	 * camera.startPreview(); }
 	 * 
 	 * };
 	 */
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		releaseCamera();
 	}
 
 	/**
 	 * Loading picture with a desired size
 	 * 
 	 * @param imgPath
 	 *            : directory of the picture
 	 * @param required_height
 	 *            : desired height
 	 * @param required_width
 	 *            : desired width
 	 * @return
 	 */
 	public Bitmap loadingBitmapEfficiently(String imgPath, int required_height,
 			int required_width) {
 		BitmapFactory.Options options = new BitmapFactory.Options();
 		// Find dimension of the picture
 		options.inJustDecodeBounds = true;
 		BitmapFactory.decodeFile(imgPath, options);
 		options.inSampleSize = Math.min(options.outHeight / required_height,
 				options.outWidth / required_width);
 		// Decode the image
 		options.inJustDecodeBounds = false;
 		return BitmapFactory.decodeFile(imgPath, options);
 	}
 
 	/**
 	 * Getting the picture from path string and resize the image to target width
 	 * and height efficiently
 	 * 
 	 * @param filePath
 	 * @param targetWidth
 	 * @param targetHeight
 	 * @return
 	 */
 	public static Bitmap resizeBitMapImage(String filePath, int targetWidth,
 			int targetHeight) {
 		Bitmap bitMapImage = null;
 		// First, get the dimensions of the image
 		Options options = new Options();
 		options.inJustDecodeBounds = true;
 		BitmapFactory.decodeFile(filePath, options);
 		double sampleSize = 0;
 		// Only scale if we need to
 		// (16384 buffer for img processing)
 		Boolean scaleByHeight = Math.abs(options.outHeight - targetHeight) >= Math
 				.abs(options.outWidth - targetWidth);
 
 		if (options.outHeight * options.outWidth * 2 >= 1638) {
 			// Load, scaling to smallest power of 2 that'll get it <= desired
 			// dimensions
 			sampleSize = scaleByHeight ? options.outHeight / targetHeight
 					: options.outWidth / targetWidth;
 			sampleSize = (int) Math.pow(2d,
 					Math.floor(Math.log(sampleSize) / Math.log(2d)));
 		}
 
 		// Do the actual decoding
 		options.inJustDecodeBounds = false;
 		options.inTempStorage = new byte[128];
 		while (true) {
 			try {
 				options.inSampleSize = (int) sampleSize;
 				bitMapImage = BitmapFactory.decodeFile(filePath, options);
 
 				break;
 			} catch (Exception ex) {
 				try {
 					sampleSize = sampleSize * 2;
 				} catch (Exception ex1) {
 
 				}
 			}
 		}
 
 		return bitMapImage;
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
 		super.onActivityResult(requestCode, resultCode, data);
 		try {
 			switch (requestCode) {
 			case REQUEST_CODE:
 				if (resultCode == Activity.RESULT_CANCELED)
 					break;
 				if (resultCode == Activity.RESULT_OK) {
 
 				}
 			}
 		} catch (Exception e) {
 			Log.e("Error Selecting Picture", e.getMessage());
 		}
 	}
 
 }
