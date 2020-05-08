 package com.appmunki.miragemobile.ar;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.Method;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Vector;
 import java.util.concurrent.locks.ReentrantLock;
 
 import org.opencv.android.Utils;
 import org.opencv.core.Core;
 import org.opencv.core.CvType;
 import org.opencv.core.Mat;
 import org.opencv.core.Point;
 import org.opencv.core.Scalar;
 import org.opencv.highgui.Highgui;
 import org.opencv.imgproc.Imgproc;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.pm.ActivityInfo;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.PixelFormat;
 import android.graphics.Typeface;
 import android.hardware.Camera;
 import android.hardware.Camera.Size;
 import android.opengl.GLSurfaceView;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.RelativeLayout.LayoutParams;
 import android.widget.TextView;
 
 import com.appmunki.miragemobile.R;
 import com.appmunki.miragemobile.utils.Util;
 import com.entity.KeyPoint;
 import com.entity.TargetImage;
 import com.entity.TargetImageResponse;
 import com.orm.androrm.DatabaseAdapter;
 import com.orm.androrm.Model;
 
 import flexjson.JSONDeserializer;
 
 /**
  * Purpose of this activity is to setup AR for a inheriting activity. Loading in
  * Markers Initializing Camera and overlay
  * 
  * @author radzell
  * 
  */
 public abstract class ARActivity extends Activity {
 
 	protected static final String TAG = "Aractivity";
 	static int countOnCreate = 0;
 
 	private CameraViewBase mCameraViewBase;
 	private RelativeLayout splashmain;
 	private CameraOverlayView mCameraOverlayView;
 	private boolean isDebugging = false;
 	private RelativeLayout main;
 	private GLSurfaceView mGLView;
 
 	private int mFrameWidth;
 	private int mFrameHeight;
 
 	private int mPictureWidth;
 	private int mPictureHeight;
 
 	private int counter = 0;
 	public ReentrantLock mPreviewBufferLock = new ReentrantLock();
 
 	// private Preview mPreview;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		setFullscreen();
 		disableScreenTurnOff();
 		setOrientation();
 		super.onCreate(savedInstanceState);
 		this.main = new RelativeLayout(this);
 
 	}
 
 	private void checkTestData() {
 		Log.d(TAG, "checkTestData");
 		// Load the splashscreen
 
 		loadSplashScreen();
 		new AsyncTask<Void, Void, Void>() {
 
 			@Override
 			protected Void doInBackground(Void... params) {
 				// Create the db
 				syncDB();
 				Matcher.load(true);
 				return null;
 			}
 
 			@Override
 			protected void onPostExecute(Void result) {
 				// Remove splash
 				splashmain.setVisibility(RelativeLayout.GONE);
 				// Set up Camera
 				setupGLSurfaceViewLayout();
 
 				// Start the camera
 				mCameraViewBase.openCamera();
 			};
 
 		}.execute();
 
 	}
 
 	
 
 	public void addPatternDebug(String name, InputStream res) {
 		Bitmap bitmap = Util.decodeSampledBitmapFromStream(res);
 		addPatternDebug(name, bitmap);
 	}
 	private void addPatternDebug(String name, Bitmap bitmap) {
 		Log.i(TAG,bitmap.getWidth()+"x"+bitmap.getHeight());
 	    Mat mat = new Mat (bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC4);
 		Utils.bitmapToMat(bitmap, mat);
 		Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
 
		Matcher.addPatternMat(mat.getNativeObjAddr());
 		File dir = new File(Environment.getExternalStorageDirectory()
 				.getAbsolutePath()+ File.separator+"miragemobile/");
 		dir.mkdirs();
 		Highgui.imwrite(Environment.getExternalStorageDirectory()
 				.getAbsolutePath() + File.separator+"miragemobile/"+name,mat);
 		bitmap.recycle();
 		mat.release();
 	}
 
 
 	public int matchDebug(Bitmap bitmap) {
 		int width = bitmap.getWidth();
 		int height = bitmap.getHeight();
 
 		Log.i("Match", "Scene size " + width + "x" + height);
 
 		// Result of the amount of found markers
 	    Mat mat = new Mat (bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC4);
 		Utils.bitmapToMat(bitmap, mat);
 		Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
 
 		int resultSize = Matcher.matchDebug(mat.getNativeObjAddr());
 
 		
 		 for(int i =0; i<Matcher.getNumpatternResults();i++){
 			org.opencv.core.Mat test = new org.opencv.core.Mat();
 			mat.copyTo(test);
 			float[] result =  Matcher.getHomography(i);
 			int index = 0;
 			for (int j = 0; j < 4; j++)
 			{
			    Log.i(TAG,"Point "+result[j]+","+result[j+1]);
 			}
 			 Core.line(test, new Point(result[0], result[1]), new Point(result[2],result[3]), new Scalar(0, 255, 0, 255), 10);
 			 Core.line(test, new Point(result[2], result[3]), new Point(result[4], result[5]), new Scalar(0, 255, 0, 255), 10); 
 			 Core.line(test, new Point(result[4], result[5]), new Point(result[6], result[7]), new Scalar(0, 255, 0, 255), 10); 
 			 Core.line(test, new Point(result[6], result[7]), new Point(result[0], result[1]), new Scalar(0, 255, 0, 255), 10);
 			 
 			 Core.circle(test, new Point(result[0], result[1]), 10, new Scalar(0,
 			 0, 255, 255), 5); Core.circle(test, new Point(result[2], result[3]),
 			 10, new Scalar(0, 0, 255, 255), 5); Core.circle(test, new
 			 Point(result[4], result[5]), 10, new Scalar(0, 0, 255, 255), 5);
 			 Core.circle(test, new Point(result[6], result[7]), 10, new Scalar(0,
 			 0, 255, 255), 5);
 			 String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
 
 			 if(!Highgui.imwrite(Environment.getExternalStorageDirectory()
 						.getAbsolutePath() + File.separator +"miragemobile/"+currentDateTimeString+".jpg", test)){
 
 				 Log.e(TAG, "Error imwrite");
 			 }
 		 }
 		 return resultSize;
 		
 	}
 
 
 
 	public void setupGLSurfaceViewLayout() {
 
 		final Preview preview = new Preview(this);
 
 		mGLView = new GLSurfaceView(this);
 		// mGLView.setEGLContextClientVersion(2);
 		mGLView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
 		mGLView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
 		mGLView.setZOrderOnTop(true);
 		setContentView(preview, new LayoutParams(LayoutParams.MATCH_PARENT,
 				LayoutParams.MATCH_PARENT));
 		mGLView.setRenderer(new TestRender());
 		addContentView(mGLView, new LayoutParams(LayoutParams.MATCH_PARENT,
 				LayoutParams.MATCH_PARENT));
 
 		// mCameraViewBase = new CameraViewBase(this, isDebugging);
 		// main.addView(mCameraViewBase);
 		// mCameraViewBase.setVisibility(SurfaceView.VISIBLE);
 		// // Add canvas overlay
 		// mCameraOverlayView = new CameraOverlayView(this);
 		// mCameraOverlayView.setVisibility(View.VISIBLE);
 		// mCameraViewBase.addMarkerFoundListener(mCameraOverlayView);
 
 		// main.addView(mCameraOverlayView);
 
 		// RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
 		// RelativeLayout.LayoutParams.MATCH_PARENT,
 		// RelativeLayout.LayoutParams.MATCH_PARENT);
 		// // Add in GLView
 		// mGLView = new GLSurfaceView(this);
 		// // mGLView.setEGLContextClientVersion(2);
 		// mGLView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
 		// mGLView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
 		// mGLView.setZOrderOnTop(true);
 		//
 		// mGLView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
 		// mGLView. setZOrderMediaOverlay(true);
 		// mGLView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
 
 		// mGLView.setRenderer(new TestRender());
 		// main.addView(mCameraOverlayView);
 		// main.addView(mGLView);
 		// setContentView(main);
 
 		
 		InputStream stream = Util.getStreamFromAsset(this, "posters/Movie Poster 1.jpg");
 		addPatternDebug("Movie Poster 1.jpg",stream);
 
 	}
 
 	public int getNumPatternResults() {
 		return Matcher.getNumpatternResults();
 	}
 
 	private void loadSplashScreen() {
 		splashmain = new RelativeLayout(this);
 		LinearLayout content = new LinearLayout(this);
 		content.setOrientation(LinearLayout.VERTICAL);
 
 		// Adding Logo
 		ImageView logoLayout = new ImageView(this);
 		logoLayout.setImageResource(R.drawable.miragelogo);
 		android.widget.LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
 				LinearLayout.LayoutParams.WRAP_CONTENT,
 				LinearLayout.LayoutParams.WRAP_CONTENT);
 
 		content.addView(logoLayout, lp1);
 
 		// Adding Text
 		TextView loadingText = new TextView(this);
 
 		loadingText.setText("Loading AR ...");
 		loadingText.setTypeface(null, Typeface.BOLD);
 		loadingText.setGravity(Gravity.CENTER);
 		lp1 = new LinearLayout.LayoutParams(
 				LinearLayout.LayoutParams.MATCH_PARENT,
 				LinearLayout.LayoutParams.WRAP_CONTENT);
 		content.addView(loadingText, lp1);
 
 		// Adding progress bar
 		ProgressBar progressBar = new ProgressBar(this, null,
 				android.R.attr.progressBarStyleLarge);
 
 		content.addView(progressBar);
 
 		LayoutParams lp = new RelativeLayout.LayoutParams(
 				RelativeLayout.LayoutParams.WRAP_CONTENT,
 				RelativeLayout.LayoutParams.WRAP_CONTENT);
 		lp.addRule(RelativeLayout.CENTER_IN_PARENT, content.getId());
 		splashmain.addView(content, lp);
 
 		setContentView(splashmain);
 	}
 
 	/**
 	 * Avoid that the screen get's turned off by the system.
 	 */
 	public void disableScreenTurnOff() {
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
 				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 	}
 
 	/**
 	 * Maximize the application.
 	 */
 	public void setFullscreen() {
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 	}
 
 	/**
 	 * Set's the orientation to landscape, as this is needed by AndAR.
 	 */
 	public void setOrientation() {
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 	}
 
 	@Override
 	protected void onPause() {
 		Log.i(TAG, "called onPause");
 		super.onPause();
 		if (mCameraViewBase != null)
 			mCameraViewBase.releaseCamera();
 	}
 
 	@Override
 	protected void onResume() {
 		Log.i(TAG, "called onResume");
 		super.onResume();
 		if (mCameraViewBase != null && !mCameraViewBase.openCamera()) {
 			AlertDialog ad = new AlertDialog.Builder(this).create();
 			ad.setCancelable(false); // This blocks the 'BACK' button
 			ad.setMessage("Fatal error: can't open camera!");
 			ad.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
 					new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							dialog.dismiss();
 							finish();
 						}
 					});
 			ad.show();
 		}
 	}
 
 	private void testJson() {
 		try {
 			InputStream in = getAssets().open("json.txt");
 			BufferedReader r = new BufferedReader(new InputStreamReader(in));
 			StringBuilder total = new StringBuilder();
 			String line;
 			while ((line = r.readLine()) != null) {
 				total.append(line);
 			}
 			String json = total.toString().split("Response")[1].trim();
 			parseJson(json);
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void parseJson(String json) {
 		System.out.print(json);
 		List<TargetImageResponse> items = new JSONDeserializer<List<TargetImageResponse>>()
 				.use("values", TargetImageResponse.class)
 				.use("values.dess", Mat.class).use("values.keys", Vector.class)
 				.use("values.keys.values", KeyPoint.class).deserialize(json);
 		for (TargetImageResponse item : items) {
 
 			new TargetImage(item).save(this);
 		}
 	}
 
 	/** Will set up the database definition **/
 	private void syncDB() {
 		DatabaseAdapter.setDatabaseName("miragedb");
 
 		List<Class<? extends Model>> models = new ArrayList<Class<? extends Model>>();
 		models.add(TargetImage.class);
 
 		DatabaseAdapter adapter = DatabaseAdapter.getInstance(this);
 		adapter.setModels(models);
 	}
 
 	static {
 		System.loadLibrary("opencv_java");
 		System.loadLibrary("MirageMobile");
 	}
 
 	protected void setDisplayOrientation(Camera camera, int angle) {
 		Method downPolymorphic;
 		try {
 			downPolymorphic = camera.getClass().getMethod(
 					"setDisplayOrientation", new Class[] { int.class });
 			if (downPolymorphic != null)
 				downPolymorphic.invoke(camera, new Object[] { angle });
 		} catch (Exception e1) {
 		}
 	}
 
 	class Preview extends SurfaceView implements SurfaceHolder.Callback,
 			Camera.PreviewCallback {
 
 		private static final String TAG = "Preview";
 		private SurfaceHolder mHolder;
 		public Camera mCamera;
 
 		private int preview_width;
 		private int preview_height;
 
 		public Preview(Context context) {
 			super(context);
 
 			mHolder = getHolder();
 			mHolder.addCallback(this);
 			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 			this.preview_width = 640;
 			this.preview_height = 480;
 
 		}
 
 		private void initCamera(SurfaceHolder holder)
 				throws InterruptedException {
 			if (mCamera == null) {
 				// The Surface has been created, acquire the camera and tell it
 				// where
 				// to draw.
 				int i = 0;
 				while (i++ < 5) {
 					try {
 						mCamera = Camera.open();
 						break;
 					} catch (RuntimeException e) {
 						Thread.sleep(200);
 					}
 				}
 				try {
 					mCamera.setPreviewDisplay(holder);
 				} catch (IOException exception) {
 					mCamera.release();
 					mCamera = null;
 
 				} catch (RuntimeException e) {
 					Log.e("camera", "stacktrace", e);
 				}
 			}
 		}
 
 		@Override
 		public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2,
 				int arg3) {
 			try {
 				initCamera(mHolder);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return;
 			}
 			// Now that the size is known, set up the camera parameters and
 			// begin
 			// the preview.
 
 			Camera.Parameters parameters = mCamera.getParameters();
 			List<Camera.Size> pvsizes = mCamera.getParameters()
 					.getSupportedPreviewSizes();
 			int best_width = 1000000;
 			int best_height = 1000000;
 			int bdist = 100000;
 			for (Size x : pvsizes) {
 				if (Math.abs(x.width - preview_width) < bdist) {
 					bdist = Math.abs(x.width - preview_width);
 					best_width = x.width;
 					best_height = x.height;
 				}
 			}
 			preview_width = best_width;
 			preview_height = best_height;
 
 			Log.d("NativePreviewer", "Determined compatible preview size is: ("
 					+ preview_width + "," + preview_height + ")");
 
 			Log.d("NativePreviewer", "Supported params: "
 					+ mCamera.getParameters().flatten());
 
 			// this is available in 8+
 			// parameters.setExposureCompensation(0);
 			if (parameters.getSupportedWhiteBalance().contains("auto")) {
 				parameters.setWhiteBalance("auto");
 			}
 			// if (parameters.getSupportedAntibanding().contains(
 			// Camera.Parameters.ANTIBANDING_OFF)) {
 			// parameters.setAntibanding(Camera.Parameters.ANTIBANDING_OFF);
 			// }
 
 			List<String> fmodes = mCamera.getParameters()
 					.getSupportedFocusModes();
 			// for(String x: fmodes){
 
 			// }
 
 			if (parameters.get("meter-mode") != null)
 				parameters.set("meter-mode", "meter-average");
 			int idx = fmodes.indexOf(Camera.Parameters.FOCUS_MODE_INFINITY);
 			if (idx != -1) {
 				parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
 			} else if (fmodes.indexOf(Camera.Parameters.FOCUS_MODE_FIXED) != -1) {
 				parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
 			}
 
 			// if (fmodes.indexOf(Camera.Parameters.FOCUS_MODE_AUTO) != -1) {
 			// hasAutoFocus = true;
 			// }
 
 			List<String> scenemodes = mCamera.getParameters()
 					.getSupportedSceneModes();
 			if (scenemodes != null)
 				if (scenemodes.indexOf(Camera.Parameters.SCENE_MODE_ACTION) != -1) {
 					parameters
 							.setSceneMode(Camera.Parameters.SCENE_MODE_ACTION);
 					Log.d("NativePreviewer", "set scenemode to action");
 				}
 
 			parameters.setPreviewSize(preview_width, preview_height);
 
 			mCamera.setParameters(parameters);
 
 			PixelFormat pixelinfo = new PixelFormat();
 			int pixelformat = mCamera.getParameters().getPreviewFormat();
 			PixelFormat.getPixelFormatInfo(pixelformat, pixelinfo);
 
 			Size preview_size = mCamera.getParameters().getPreviewSize();
 			preview_width = preview_size.width;
 			preview_height = preview_size.height;
 			// int bufSize = preview_width * preview_height
 			// * pixelinfo.bitsPerPixel;
 
 			mCamera.startPreview();
 
 		}
 
 		@Override
 		public void surfaceCreated(SurfaceHolder holder) {
 			mCamera = Camera.open();
 			Camera.Parameters p = mCamera.getParameters();
 			List<String> focusModes = p.getSupportedFocusModes();
 			if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
 				p.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
 			}
 
 			setDisplayOrientation(mCamera, 90);
 
 			// max resolution
 			mFrameWidth = 640;
 			mFrameHeight = 480;
 
 			mPictureWidth = 1600;
 			mPictureHeight = 1200;
 
 			p.setPreviewSize(mFrameWidth, mFrameHeight);
 			p.setPictureSize(mPictureWidth, mPictureHeight);
 
 			for (Size size : p.getSupportedPreviewSizes())
 				Log.e(TAG, "Preview size choosen: " + size.width + "x"
 						+ size.height);
 
 			for (Size size : p.getSupportedPictureSizes())
 				Log.e(TAG, "Picture size choosen: " + size.width + "x"
 						+ size.height);
 
 			Log.e(TAG, "Fps choosen: "
 					+ p.getSupportedPreviewFpsRange().get(0).length);
 			mCamera.setParameters(p);
 			mCamera.setPreviewCallback(previewCallback);
 
 			try {
 				mCamera.setPreviewDisplay(holder);
 				mCamera.startPreview();
 			} catch (IOException exception) {
 				mCamera.release();
 				mCamera = null;
 				Log.d(TAG, "Error setting preview display: ");
 				exception.printStackTrace();
 			}
 
 		}
 		private Date start;
         int fcount = 0;
 
 		Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
 
 
 			@Override
 			public void onPreviewFrame(byte[] data, Camera camera) {
 				if (start == null) {
                     start = new Date();
 				}
 				mPreviewBufferLock.lock();
 				try {
 					Log.i("Frame", "Size "
 							+ camera.getParameters().getPreviewSize().width
 							+ "x"
 							+ camera.getParameters().getPreviewSize().height);
 					Mat src = new Mat(
 							camera.getParameters().getPreviewSize().height,
 							camera.getParameters().getPreviewSize().width,
 							CvType.CV_8U, new Scalar(255));
 					src.put(0, 0, data);
 					Mat dst = new Mat(
 							camera.getParameters().getPreviewSize().height,
 							camera.getParameters().getPreviewSize().width,
 							CvType.CV_8U, new Scalar(255));
 
 					Core.transpose(src, dst);
 					Core.flip(dst, dst, 1);
 					
 					Matcher.matchDebugDiego(src.getNativeObjAddr());
 					Highgui.imwrite(Environment.getExternalStorageDirectory()
 							.getAbsolutePath() + File.separator+"miragemobile/"+fcount+".jpg",src);
 					// Matcher.matchDebug(camera.getParameters().getPreviewSize().height,
 					// camera.getParameters().getPreviewSize().width,
 					// data,baseDir);
 				} finally {
 					mPreviewBufferLock.unlock();
 					if (mCamera != null)
 						mCamera.addCallbackBuffer(data);
 				}
 				
 				fcount++;
                 if (fcount % 100 == 0) {
                         double ms = (new Date()).getTime() - start.getTime();
                         Log.i("NativePreviewer", "fps:" + fcount / (ms / 1000.0));
                         start = new Date();
                         fcount = 0;
                 }
 
 			}
 
 		};
 
 		@Override
 		public void surfaceDestroyed(SurfaceHolder arg0) {
 			if (mCamera != null) {
 				mCamera.stopPreview();
 				mCamera.release();
 				mCamera = null;
 			}
 
 		}
 
 		@Override
 		public void onPreviewFrame(byte[] arg0, Camera arg1) {
 			// TODO Auto-generated method stub
 		}
 
 	}
 
 }
