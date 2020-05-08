 package com.rc.ui;
 
 import static com.rc.base.Output.printError;
 
 import java.io.InputStream;
 import java.util.HashMap;
 
 import org.opencv.android.BaseLoaderCallback;
 import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
 import org.opencv.android.JavaCameraView;
 import org.opencv.android.LoaderCallbackInterface;
 import org.opencv.android.OpenCVLoader;
 import org.opencv.android.Utils;
 import org.opencv.core.Core;
 import org.opencv.core.Mat;
 import org.opencv.core.MatOfDMatch;
 import org.opencv.core.MatOfKeyPoint;
 import org.opencv.core.Point;
 import org.opencv.core.Scalar;
 import org.opencv.features2d.DMatch;
 import org.opencv.features2d.DescriptorExtractor;
 import org.opencv.features2d.DescriptorMatcher;
 import org.opencv.features2d.FeatureDetector;
 import org.opencv.features2d.KeyPoint;
 import org.opencv.imgproc.Imgproc;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.Window;
 import android.view.WindowManager;
 
 import com.rc.MainExecutor;
 import com.rc.base.Executor;
 import com.rc.util.Consts;
 import com.rc.util.IntentFor;
 import com.rc.util.Preferences;
 
 public class VideoDetectorActivity extends Activity implements CvCameraViewListener {
 	
 	public static final String TAG = VideoDetectorActivity.class
 			.getSimpleName();
 	
 	private int mSceneCenterX;
 	private int mSceneCenterY;
 	
     private static final Scalar INFO_COLOR = new Scalar(0, 255, 0, 255);
 	private Point mTextPoint = new Point(0, 25);
 	private Point mTextPoint2 = new Point(0, 45);
 	private Point mTextPoint3 = new Point(0, 65);
     
 	private JavaCameraView mCameraView;
 	
 	private Mat mRgba;
 	private Mat mGray;
 	
 	private Mat mMarker;
 	private MatOfKeyPoint mMarkerKeyPoints;
 	private Mat mMarkerDescriptors;
 	
 	private MatOfKeyPoint mSceneKeyPoints;
 	private Mat mSceneDescriptors;
 	
 	private MatOfDMatch mMatchesMat;
 	
 	private FeatureDetector mDetector;
 	private DescriptorExtractor mExtractor;
 	private DescriptorMatcher mMatcher;
 	
 	private int mGoodMatchesCount;
 	
     private Point mLftTop = new Point(0, 0);
     private Point mRhtBtm = new Point(0, 0); 
 	
 	private long mCounter;
 	private long mSkipCount;
 	private double mDistance;
 	
 	private short mDetectTracker = 1;
 	private int mDetectRate;
 	private int mDetectDistance;
 	
 	
 	public static boolean isRunning;
 	
 	private Executor mMainExec;
 	private Preferences mPrefs;
 	
 	private HashMap<String, DetectTypes> mNameToTypes = new HashMap<String, DetectTypes>();
 	{
 		mNameToTypes.put("o", new DetectTypes(FeatureDetector.ORB,
 				DescriptorExtractor.ORB, DescriptorMatcher.BRUTEFORCE_HAMMING));
 		mNameToTypes.put("b",
 				new DetectTypes(FeatureDetector.BRISK, DescriptorExtractor.BRISK,
 						DescriptorMatcher.BRUTEFORCE_HAMMING));
 		mNameToTypes.put("f", new DetectTypes(FeatureDetector.FAST,
 				DescriptorExtractor.BRIEF, DescriptorMatcher.BRUTEFORCE));
 	}
 	
 	class DetectTypes {
 		
 		public final int detectorType;
 		public final int extractorType;
 		public final int matcherType;
 		
 		public DetectTypes(int detectorType, int extractorType, int matcherType) {
 			this.detectorType = detectorType;
 			this.extractorType = extractorType;
 			this.matcherType = matcherType;
 		}
 	}
 	
 	private IntentFilter mStopFilter = new IntentFilter();
 	private BroadcastReceiver mStopBroadcastReceiver = new BroadcastReceiver() {
 		
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			
 			VideoDetectorActivity.this.finish();
 		}
 	};
 	
 	private LoaderCallbackInterface mLoaderCallback = new BaseLoaderCallback(this) {
 		
 		@Override
 		public void onManagerConnected(int status) {
 			
 			if (status == LoaderCallbackInterface.SUCCESS) {
 				
 				DetectTypes types = mNameToTypes.get(mPrefs.detectMethod());
 				mDetector = FeatureDetector.create(types.detectorType);
 				mExtractor = DescriptorExtractor.create(types.extractorType);
 				mMatcher = DescriptorMatcher.create(types.matcherType);
 				
 				mCameraView.enableView();
 			} else {
 				super.onManagerConnected(status);
 			}
 		};
 	};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.activity_video_detector);
 		
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | 
         	    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | 
         	    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | 
         	    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
         	    WindowManager.LayoutParams.FLAG_FULLSCREEN | 
         	    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | 
         	    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | 
         	    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
         
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 		
		mMainExec = MainExecutor.getInstance(getApplicationContext());
 		mPrefs = Preferences.getInstance(getApplicationContext());
 		mSkipCount = mPrefs.skipCount();
 		mDetectRate = mPrefs.detectRate();
 		mDetectDistance = mPrefs.detectDistance();
 		
 		int sceneWidth = "l".equals(mPrefs.frameSize()) ? 800 : 
 				"m".equals(mPrefs.frameSize()) ? 640 : 320;
 		int sceneHeight = "l".equals(mPrefs.frameSize()) ? 600 : 
 				"m".equals(mPrefs.frameSize()) ? 480 : 240;
 		mSceneCenterX = sceneWidth / 2;
 		mSceneCenterY = sceneHeight / 2;
 		
 		mCameraView = (JavaCameraView) findViewById(R.id.camera_view);
 		mCameraView.setCvCameraViewListener(this);
 		mCameraView.setMaxFrameSize(sceneWidth, sceneHeight);
 		
         mStopFilter.addAction(Consts.STOP_VIDEO_DETECTOR_ACTION);
 	}
 	
 	@Override
 	protected void onResume() {
 		
 		super.onResume();
 		
 		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
 		registerReceiver(mStopBroadcastReceiver, mStopFilter);
 		isRunning = true;
 	}
 	
 	@Override
 	protected void onPause() {
 		
 		if (mCameraView != null)
 			mCameraView.disableView();
 		
 		unregisterReceiver(mStopBroadcastReceiver);
 		isRunning = false;
 		
 		super.onPause();
 	}
 	
 	@Override
 	protected void onDestroy() {
 		
 		super.onDestroy();
 		mCameraView.disableView();
 	}
 	
 	@Override
 	public Mat onCameraFrame(Mat sceneMat) {
 		
         sceneMat.copyTo(mRgba);
         Imgproc.cvtColor(sceneMat, mGray, Imgproc.COLOR_RGBA2GRAY);
         
         long detectTime = 0; 
         if (mCounter++ % mSkipCount == 0) { // Skip frames
         	
         	long start = System.currentTimeMillis();
 			mDistance = detect(mMarker);
 			detectTime = System.currentTimeMillis() - start;
 			
 			trackFound(isFound());
         }
         
         String text = detectTime + "ms" 
         		+ " dist: " + (Double.isInfinite(mDistance) ? "~" : (int)mDistance) 
         		+ " matches: " + mGoodMatchesCount;
         Core.putText(mRgba, text, mTextPoint, 2, 0.6, INFO_COLOR);
         
         if (isFound() && isDetected()) {
             Core.rectangle(mRgba, mLftTop, mRhtBtm, INFO_COLOR, 3);
             executeDetected();
         }
 		
 		return mRgba;
 	}
 	
 	private double detect(Mat sample) {
 		
 		try {
 
 			// Find key points
 			mDetector.detect(sample, mMarkerKeyPoints);
 			mDetector.detect(mGray, mSceneKeyPoints);
 			
 			// Calculate descriptors
 			mExtractor.compute(sample, mMarkerKeyPoints, mMarkerDescriptors);
 			mExtractor.compute(mGray, mSceneKeyPoints, mSceneDescriptors);
 			
 			// Match descriptors' vectors
 			mMatcher.match(mMarkerDescriptors, mSceneDescriptors, mMatchesMat);
 			
 		} catch (Exception e) {
 			Log.e(TAG, e.getLocalizedMessage(), e);
 	        Core.putText(mRgba, "error", mTextPoint2, 2, 0.6, INFO_COLOR);
 			printError(e);
 			return 0;
 		}
 		
 		// Find out absolute MIN distance between all sample descriptors
 		float minDistance = Float.MAX_VALUE;
 		DMatch[] matches = mMatchesMat.toArray();
 		for (DMatch match : matches) {
 			minDistance = Math.min(minDistance, match.distance);
 		}
 		
 		mGoodMatchesCount = 0;
 		
 		double	ltX = Double.MAX_VALUE,
 				ltY = Double.MAX_VALUE,
 				rbX = Double.MIN_VALUE,
 				rbY = Double.MIN_VALUE;
 		
 		KeyPoint[] sceneKeyPoints = mSceneKeyPoints.toArray();
 		
 		Point point;
 		float distanceLimit = minDistance * 3;
 		for (DMatch match : matches) {
 			
 			if (match.distance < distanceLimit) {
 				
 				point = sceneKeyPoints[match.trainIdx].pt;
 				
 				ltX = Math.min(ltX, point.x);
 				ltY = Math.min(ltY, point.y);
 				
 				rbX = Math.max(rbX, point.x);
 				rbY = Math.max(rbY, point.y);
 				
 				mGoodMatchesCount++;
 			}
 		}
 		
 		if (mGoodMatchesCount == 0)
 			return 0;
 		
 		mLftTop = new Point(ltX, ltY);
 		mRhtBtm = new Point(rbX, rbY);
 		
 		return Math.sqrt(
 				Math.pow(ltX - rbX, 2) + 
 				Math.pow(ltY - rbY, 2));
 	}	
 	
 	private boolean isFound() {
 		
 		return !Double.isInfinite(mDistance) && mDistance > 1
 				&& mDistance < mDetectDistance;
 	}
 	
 	private void trackFound(boolean isFound) {
 		mDetectTracker <<= 1;
 		mDetectTracker |= isFound ? 1 : 0;
 		
 		if (mDetectTracker == 0)
 			mDetectTracker = 1;
 	}
 	
 	private boolean isDetected() {
 		
 		int detectedCount = -1;
 		short bit = 1;
 		for (int i = 0; i < 16; i++) {
 			
 			if ((bit & mDetectTracker) == bit)
 				detectedCount++;
 			
 			bit <<= 1;
 		}
 		
 		return detectedCount >= mDetectRate;
 	}
 	
 	private void executeDetected() {
 		
 		// Marker center
 		int oX = (int) ((mRhtBtm.x - mLftTop.x) / 2 + mLftTop.x);
 		int oY = (int) ((mRhtBtm.y - mLftTop.y) / 2 + mLftTop.y);
 		
 		int offsetX = mSceneCenterX - oX;
 		int offsetY = mSceneCenterY - oY;
 		
         String text = String.format("offset: x=%d, y=%d", offsetX, offsetY);
         Core.putText(mRgba, text, mTextPoint3, 2, 0.6, INFO_COLOR);
         Core.putText(mRgba, "marker detected!", mTextPoint2, 2, 0.6, INFO_COLOR);
 		
 		String commandText = String.format("md %d,%d", offsetX, offsetY);
 		mMainExec.execute(commandText);
 	}
 
 	@Override
 	public void onCameraViewStarted(int width, int height) {
 		mRgba = new Mat();
 		mGray = new Mat();
 		
 		// Load sample images
 		mMarker = createSample(R.raw.finish);
 		
 		mMarkerKeyPoints = new MatOfKeyPoint();
 		mMarkerDescriptors = new Mat();
 		
 		mSceneKeyPoints = new MatOfKeyPoint();
 		mSceneDescriptors = new Mat();
 		
 		mMatchesMat = new MatOfDMatch();
 	}
 
 	private Mat createSample(int resId) {
 		InputStream input = getResources().openRawResource(resId); 
 		Bitmap bitmap = BitmapFactory.decodeStream(input);
 		
 		Mat mat = new Mat();
 		Utils.bitmapToMat(bitmap, mat);
 		Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2GRAY);
 		
 		return mat;
 	}
 
 	@Override
 	public void onCameraViewStopped() {
 		mRgba.release();
 		mGray.release();
 		
 		mMarker.release();
 		
 		mMarkerKeyPoints.release();
 		mMarkerDescriptors.release();
 		
 		mSceneKeyPoints.release();
 		mSceneDescriptors.release();
 		
 		mMatchesMat.release();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_video_detector, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		
 		if (item.getItemId() == R.id.menu_configurator) {
 			startActivity(IntentFor.mainActivity(this));
 		}
 		
 		return true;
 	}
 }
