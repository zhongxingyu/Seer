 package com.zenbox;
 
 import org.opencv.android.BaseLoaderCallback;
 import org.opencv.android.CameraBridgeViewBase;
 import org.opencv.android.LoaderCallbackInterface;
 import org.opencv.android.OpenCVLoader;
 import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
 
 import org.opencv.core.CvType;
 import org.opencv.core.Mat;
 import org.opencv.core.MatOfPoint2f;
 import org.opencv.core.Size;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.util.Log;
 import android.view.Window;
 import android.view.WindowManager;
 
 public class ZenBoxActivity extends Activity implements CvCameraViewListener {
 	// tag for this class
 	private static final String TAG = "ZenBox::Activity";
 	
 	private static final Size IMAGE_SIZE = new Size(640, 480);
 	
 	// open CV camera
 	private CameraBridgeViewBase mOpenCvCameraView;
 	
 	private int mFrames = 0; // the number of frames passed.
 	
 	// Feature detector goodies.
 	private MatOfPoint2f mFeatures;
 	private MatOfPoint2f mPrevFeatures;
 	
 	// Pyramid matrices for optical flow detection.
 	private Mat mPrevPyr;
 	private Mat mCurPyr;
 	
 	// The audio manager member.
 	private AudioMessenger mAudioMsgr;
 	
 	// The main Rgba matrix.
 	private Mat mRgba;
 	private Mat mPrevRgba;
 	// An intermediate grayscale matrix meant for mRgba.
 	private Mat mGray;
 	private Mat mPrevGray;
 
 	// need this callback in order to enable the openCV camera
 	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
 		@Override
 		public void onManagerConnected(int status) {
 			switch (status) {
 			case LoaderCallbackInterface.SUCCESS: {
 				Log.i(TAG, "ZenBox loaded successfully");
 				System.loadLibrary("zen_box");
 				mOpenCvCameraView.enableView();
 				mOpenCvCameraView.setMaxFrameSize((int)IMAGE_SIZE.width, (int)IMAGE_SIZE.height);
 			}
 				break;
 			default: {
 				super.onManagerConnected(status);
 			}
 				break;
 			}
 		}
 	};
 
 	public ZenBoxActivity() {
 		Log.i(TAG, "Instantiated new " + this.getClass());
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Log.i(TAG, "called onCreate");
 		// turn off the title on the screen
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		// Window flag: as long as this window is visible to the user, keep the
 		// device's screen turned on and bright.
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 
 		setContentView(R.layout.activity_zen_box);
 
 		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.activity_zen_box_view);
 		mOpenCvCameraView.setCvCameraViewListener(this);
 		AudioMessenger.getInstance(this); // should start the sound up
 	}
 
 	@Override
 	public void onCameraViewStarted(int width, int height) {
 		// get the data from camera
 		// type: Array type. Use CV_8UC1,..., CV_64FC4 to create 1-4 channel
 		// matrices, or CV_8UC(n),..., CV_64FC(n) to create multi-channel (up to
 		// CV_MAX_CN channels) matrices.
 		mRgba = new Mat(height, width, CvType.CV_8UC4);
 		mPrevRgba = new Mat(height, width, CvType.CV_8UC4);
 		mGray = new Mat(height, width, CvType.CV_8UC1);
 		mPrevGray = new Mat(height, width, CvType.CV_8UC1);
 		mAudioMsgr = AudioMessenger.getInstance(ZenBoxActivity.this);
 	}
 
 	@Override
 	public void onCameraViewStopped() {
 		mRgba.release();
 		mPrevFeatures.release();
 		mPrevRgba.release();
 		mFeatures.release();
 		mGray.release();
 	}
 
 	/*
 	 * bring something onto the screen. So this is called every time
 	 * 
 	 * @see
 	 * org.opencv.android.CameraBridgeViewBase.CvCameraViewListener#onCameraFrame
 	 * (org.opencv.core.Mat)
 	 */
 	@Override
 	public Mat onCameraFrame(Mat inputFrame) {
 		// Only detect movement every few frames.
 		if (mFrames == 1) {
 			inputFrame.copyTo(mRgba);
 			mFrames = 0;
 			OpticalFlow(
 					mRgba.getNativeObjAddr(),
 					mPrevGray.getNativeObjAddr(),
 					mGray.getNativeObjAddr(),
 					mPrevFeatures.getNativeObjAddr(),
 					mFeatures.getNativeObjAddr());
 			return mRgba;
 		} else {
 			inputFrame.copyTo(mPrevRgba);
 			DetectFeatures(mPrevRgba.getNativeObjAddr(), 
 					mPrevGray.getNativeObjAddr(), 
 					mPrevFeatures.getNativeObjAddr(),
 					mPrevRgba.getNativeObjAddr());
 			++mFrames;
 			return mPrevRgba;
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		if (mOpenCvCameraView != null)
 			mOpenCvCameraView.disableView();
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		// this statement is to active the mLoaderCallBack so it can enable the
 		// camera
 		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
 				mLoaderCallback);
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		if (mOpenCvCameraView != null)
 			mOpenCvCameraView.disableView();
 		mAudioMsgr.cleanup();
 	}
 
 	
 	/////// native methods (documentation in zen_box.hpp ///////
 	public native void OpticalFlow(long addrCurMat, long addrPrevMatGray,
 			long addrCurMatGray, long addrPrevFeat, long addrCurFeat);
 	
 	public native void DetectFeatures(long addrImg, long addrGrayImg, long addrFeatures, long addrFrame);
 }
