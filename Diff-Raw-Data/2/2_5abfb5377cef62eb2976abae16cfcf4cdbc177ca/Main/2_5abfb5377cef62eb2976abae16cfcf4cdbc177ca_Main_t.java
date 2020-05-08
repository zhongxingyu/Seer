 package com.example.shooter.antistress;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.util.Iterator;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.BitmapFactory;
 import android.graphics.BitmapFactory.Options;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.PixelFormat;
 import android.hardware.Camera;
 import android.hardware.Camera.AutoFocusCallback;
 import android.hardware.Camera.CameraInfo;
 import android.hardware.Camera.Parameters;
 import android.hardware.Camera.PictureCallback;
 import android.hardware.Camera.Size;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.StatFs;
 import android.util.Log;
 import android.view.Display;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.ImageButton;
 import android.widget.Toast;
 
 public class Main extends Activity implements SurfaceHolder.Callback {
 
 	public static final int PHOTO_WIDTH = 480;
 	public static final int PHOTO_HEIGHT = 800;
 
 	private SurfaceView cameraView;
 	private GifView weaponView;
 	private SurfaceHolder cameraHolder;
 	private SurfaceHolder weaponHolder;
 	private Camera camera;
 	private boolean isFocusingNeeded = false;
 
 	private ImageButton throwButton;
 
 	private CameraViewStatusCodes cameraViewStatus = CameraViewStatusCodes.STARTING;
 
 	private int resId;
 
 	private enum CameraViewStatusCodes {
 		ERROR, WAITING, AUTOFOCUSING, DRAWING, DRAWING_ENDED, PAUSED, STARTING
 	};
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setContentView(R.layout.main);
 		
 		resId = getIntent().getIntExtra(getString(R.string.intent_extra_name),
 				-1);
 		cameraView = (SurfaceView) findViewById(R.id.cameraSurfaceView);
 		cameraHolder = cameraView.getHolder();
 		cameraHolder.addCallback(this);
 		cameraHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 		weaponView = ((GifView) findViewById(R.id.throwingObjectSurfaceView));
 		weaponView.setOnTouchListener(myOnTouchListener);
 		weaponView.setOnClickListener(null);
 		weaponHolder = weaponView.getHolder();
 		weaponHolder.addCallback(new SurfaceHolder.Callback() {
 			@Override
 			public void surfaceDestroyed(SurfaceHolder holder) {
 				Log.d("watch", "SurfaceDestroyed wv");
 				weaponView.release();
 			}
 
 			@Override
 			public void surfaceCreated(SurfaceHolder holder) {
 				Log.d("watch", "SurfaceCreated wv");
 				if (resId != -1)
 					weaponView.setGif(resId);
 				else
 					weaponView.setGif(R.id.tomatoImageButton);
 				Toast.makeText(getApplicationContext(), R.string.how_to, Toast.LENGTH_SHORT).show();
 			}
 
 			@Override
 			public void surfaceChanged(SurfaceHolder holder, int format,
 					int width, int height) {
 				Log.d("watch", "SurfaceChanged wv");
 			}
 		});
 		weaponHolder.setFormat(PixelFormat.TRANSLUCENT);
 		weaponView.setZOrderMediaOverlay(true);
 		throwButton = (ImageButton) findViewById(R.id.shootImageButton);
 		throwButton.setOnClickListener(myBtnOnClickListener);
 
 		Log.d("watch", "onCreate");
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		Log.d("watch", "onPause");
 		if (cameraViewStatus == CameraViewStatusCodes.WAITING) {
 			camera.stopPreview();
 			cameraViewStatus = CameraViewStatusCodes.PAUSED;
 		}
 		if (camera != null && cameraViewStatus != CameraViewStatusCodes.ERROR)
 			camera.release();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		Log.d("watch", "onResume");
 		camera = getCamera();
 		if (cameraViewStatus == CameraViewStatusCodes.PAUSED) {
 			setCameraParameters(camera);
 			camera.startPreview();
 			cameraViewStatus = CameraViewStatusCodes.WAITING;
 		}
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		Log.d("watch", "SurfaceDestroyed");
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		weaponView.footerHeight = findViewById(R.id.footerImageView).getHeight();
 		Log.d("watch", "SurfaceChanged");
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		throwButton.setEnabled(true);		
 		if (cameraViewStatus != CameraViewStatusCodes.ERROR) {
 			setCameraParameters(camera);
 			if (cameraViewStatus == CameraViewStatusCodes.STARTING) {
 				camera.startPreview();
 				cameraViewStatus = CameraViewStatusCodes.WAITING;
 			} else if (cameraViewStatus == CameraViewStatusCodes.DRAWING_ENDED) {
 				throwButton.performClick();
 			}
 		}
 		Log.d("watch", "SurfaceCreated");
 	}
 
 	OnClickListener myBtnOnClickListener = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			Log.d("watch", "onClick " + cameraViewStatus.toString());
 			switch (cameraViewStatus) {
 			case WAITING: {
 				cameraViewStatus = CameraViewStatusCodes.AUTOFOCUSING;
 				throwButton.setEnabled(false);
 				if (isFocusingNeeded) {
 					camera.autoFocus(myAutoFocusCallback);
 				} else {
 					Throw();
 				}
 			}
 				break;
 			case DRAWING_ENDED: {
 				cameraViewStatus = CameraViewStatusCodes.WAITING;
 				setCameraParameters(camera);
 				camera.startPreview();
 			}
 				break;
 			case ERROR: {
 				Toast.makeText(getApplicationContext(),
 						R.string.camera_error, Toast.LENGTH_LONG)
 						.show();
 			}
 				break;
 			case DRAWING: {
 
 			}
 				break;
 			default: {
 				Toast.makeText(getApplicationContext(),
 						"Have no idea what this is :(", Toast.LENGTH_LONG)
 						.show();
 			}
 				break;
 			}
 		}
 	};
 
 	OnTouchListener myOnTouchListener = new OnTouchListener() {
 		@Override
 		public boolean onTouch(View v, MotionEvent event) {
 			Log.d("Touching", "event action: " + event.getAction());
			if (event.getAction() == MotionEvent.ACTION_UP && cameraViewStatus == CameraViewStatusCodes.WAITING) {
 				weaponView.finalX = (int) event.getX();
 				weaponView.finalY = (int) event.getY();
 				throwButton.performClick();
 			}
 			return false;
 		}
 	};
 
 	AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback() {
 		@Override
 		public void onAutoFocus(boolean success, Camera camera) {
 			Throw();
 		}
 	};
 
 	PictureCallback myPictureCallback = new PictureCallback() {
 		@Override
 		public void onPictureTaken(byte[] data, Camera camera) {
 			Log.d("camera monitoring", "picture taken");
 			cameraViewStatus = CameraViewStatusCodes.DRAWING_ENDED;
 
 			BitmapFactory.Options opts = new Options();
 			opts.inJustDecodeBounds = true;
 			BitmapFactory.decodeByteArray(data, 0, data.length, opts);
 			opts.inSampleSize = calculateInSampleSize(opts, PHOTO_WIDTH,
 					PHOTO_HEIGHT);
 			opts.inJustDecodeBounds = false;
 			opts.inPurgeable = true;
 			Bitmap finalBitmap = BitmapFactory.decodeByteArray(data, 0,
 					data.length, opts);
 
 			int width = finalBitmap.getWidth();
 			int height = finalBitmap.getHeight();
 
 			if (width > height) {
 				Matrix matrix = new Matrix();
 				matrix.postRotate(90);
 				finalBitmap = Bitmap.createBitmap(finalBitmap, 0, 0, width,
 						height, matrix, true);
 			} else {
 				finalBitmap = Bitmap.createScaledBitmap(finalBitmap, width,
 						height - 1, true);
 			}
 			try {
 				Canvas finalCanvas = new Canvas(finalBitmap);
 				finalCanvas.setDensity(finalBitmap.getDensity());
 				weaponView.getFinalBitmap(finalCanvas);
 				finalCanvas.save();
 				finalCanvas = null;
 
 				String fileName = "";
 				StatFs stat = new StatFs(Environment
 						.getExternalStorageDirectory().getPath());
 				long bytesAvailable = (long) stat.getBlockSize()
 						* (long) stat.getAvailableBlocks();
 				if (bytesAvailable < finalBitmap.getRowBytes()
 						* finalBitmap.getHeight()) {
 					stat = new StatFs(Environment.getRootDirectory().getPath());
 					bytesAvailable = (long) stat.getBlockSize()
 							* (long) stat.getAvailableBlocks();
 					if (bytesAvailable < finalBitmap.getRowBytes()
 							* finalBitmap.getHeight()) {
 						throw new Exception("Not enough free memory");
 					} else {
 						fileName = getCacheDir().getPath() + "/image.jpg";
 					}
 				} else {
 					fileName = getExternalCacheDir().getPath() + "/image.jpg";
 				}
 				File tmpFile = new File(fileName);
 				FileOutputStream fos = new FileOutputStream(tmpFile);
 				finalBitmap.compress(CompressFormat.JPEG, 100, fos);
 				fos.flush();
 				fos.close();
 				finalBitmap.recycle();
 				finalBitmap = null;
 
 				Intent intent = new Intent();
 				intent.setClass(getApplicationContext(), Share.class);
 				intent.putExtra("finalImagePath", tmpFile.getPath());
 				startActivity(intent);
 			} catch (Exception e) {
 				Log.e("Exception", "Exception while saving", e);
 				if (finalBitmap != null) {
 					finalBitmap.recycle();
 				}
 				Toast.makeText(getApplicationContext(),
 						R.string.save_error, Toast.LENGTH_LONG)
 						.show();
 				throwButton.setEnabled(true);
 				throwButton.performClick();
 			}
 
 		}
 	};
 
 	public static int calculateInSampleSize(BitmapFactory.Options options,
 			int reqWidth, int reqHeight) {
 		final int height = options.outHeight;
 		final int width = options.outWidth;
 		int inSampleSize = 1;
 		if ((reqWidth < reqHeight && width > height)
 				|| (reqWidth > reqHeight && width < height)) {
 			int tmp;
 			tmp = reqWidth;
 			reqWidth = reqHeight;
 			reqHeight = tmp;
 		}
 
 		if (height > reqHeight || width > reqWidth) {
 			final int heightRatio = Math.round((float) height
 					/ (float) reqHeight);
 			final int widthRatio = Math.round((float) width / (float) reqWidth);
 			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
 		}
 
 		return inSampleSize;
 	}
 
 	private void Throw() {
 		cameraViewStatus = CameraViewStatusCodes.DRAWING;
 		camera.takePicture(null, null, null, myPictureCallback);
 		weaponView.play();
 	}
 
 	private Camera getCamera() {
 		Camera cam = null;
 		try {
 			cam = Camera.open();
 			if (cam == null)
 				throw new Exception("Don't have back facing camera");
 		} catch (Exception e) {
 			Log.e("Exception", "Exception while opening camera", e);
 			cameraViewStatus = CameraViewStatusCodes.ERROR;
 			Toast.makeText(
 					getApplicationContext(),
 					R.string.camera_open_error,
 					Toast.LENGTH_LONG).show();
 			finish();
 		}
 		return cam;
 	}
 
 	private void setCameraParameters(Camera cam) {
 		if (cam == null)
 			cam = getCamera();
 		int degrees = 90;
 		Camera.Parameters parameters = cam.getParameters();
 		Size optimalSize = getOptimalSize(
 				parameters.getSupportedPictureSizes(), PHOTO_WIDTH,
 				PHOTO_HEIGHT);
 		Log.d("camera monitoring", "Optimal Sizes for picture is "
 				+ optimalSize.width + "x" + optimalSize.height);
 		parameters.setPictureSize(optimalSize.width, optimalSize.height);
 		Display display = getWindowManager().getDefaultDisplay();
 		optimalSize = getOptimalSize(parameters.getSupportedPreviewSizes(),
 				display.getWidth(), display.getHeight());
 		Log.d("camera monitoring", "Optimal Sizes for preview is "
 				+ optimalSize.width + "x" + optimalSize.height);
 		parameters.setPreviewSize(optimalSize.width, optimalSize.height);
 		Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
 		int rotation = 0;
 		for (int id = 0; id < Camera.getNumberOfCameras(); id++) {
 			Camera.getCameraInfo(id, info);
 			if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
 				rotation = (info.orientation - degrees + 360) % 360;
 			}
 		}
 		parameters.setRotation(rotation);
 		parameters.set("orientation", "portrait");
 		List<String> focusModes = parameters.getSupportedFocusModes();
 		if (focusModes != null) {
 			String finalFocusMode = "";
 			if (focusModes.contains(Parameters.FOCUS_MODE_EDOF))
 				finalFocusMode = Parameters.FOCUS_MODE_EDOF;
 			else if (focusModes
 					.contains(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
 				finalFocusMode = Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
 			else if (focusModes.contains(Parameters.FOCUS_MODE_INFINITY))
 				finalFocusMode = Parameters.FOCUS_MODE_INFINITY;
 			else if (focusModes.contains(Parameters.FOCUS_MODE_AUTO)) {
 				finalFocusMode = Parameters.FOCUS_MODE_AUTO;
 				isFocusingNeeded = true;
 			}
 			if (finalFocusMode != "")
 				parameters.setFocusMode(finalFocusMode);
 		}
 		cam.setDisplayOrientation(degrees);
 		try {
 			cam.setParameters(parameters);
 			camera.setPreviewDisplay(cameraHolder);
 		} catch (Exception e) {
 			Log.e("Exception", "Exception while setting prev display", e);
 			cameraViewStatus = CameraViewStatusCodes.ERROR;
 			if (camera != null)
 				camera.release();
 		}
 	}
 
 	public static Size getOptimalSize(List<Size> sizes, int screenWidth,
 			int screenHeight) {
 		final double epsilon = 0.15;
 		double aspectRatio = ((double) screenWidth) / screenHeight;
 		Size optimalSize = null;
 		for (Iterator<Size> iterator = sizes.iterator(); iterator.hasNext();) {
 			Size currSize = iterator.next();
 			double curAspectRatio = ((double) currSize.width) / currSize.height;
 			if (Math.abs(aspectRatio - curAspectRatio) < epsilon) {
 				if (optimalSize != null) {
 					if (optimalSize.height < currSize.height
 							&& optimalSize.width < currSize.width) {
 						optimalSize = currSize;
 					}
 				} else {
 					optimalSize = currSize;
 				}
 			}
 		}
 		if (optimalSize == null) {
 			if (screenWidth < screenHeight)
 				optimalSize = getOptimalSize(sizes, screenHeight, screenWidth);
 			else
 				optimalSize = sizes.get(0);
 		}
 		return optimalSize;
 	}
 
 }
