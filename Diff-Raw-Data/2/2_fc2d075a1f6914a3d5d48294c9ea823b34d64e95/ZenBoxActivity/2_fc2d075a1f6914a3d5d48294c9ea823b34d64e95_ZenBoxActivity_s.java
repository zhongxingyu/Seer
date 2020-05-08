 package com.zenbox;
 
 import org.opencv.android.BaseLoaderCallback;
 import org.opencv.android.CameraBridgeViewBase;
 import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
 import org.opencv.android.LoaderCallbackInterface;
 import org.opencv.android.OpenCVLoader;
 import org.opencv.core.Core;
 import org.opencv.core.CvType;
 import org.opencv.core.Mat;
 import org.opencv.core.MatOfPoint2f;
 import org.opencv.core.Size;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.SeekBar;
 import android.widget.Spinner;
 
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
 	
 	// The average flow vector
 	private int[] mFlowVector;
 	
 	private int mSampleCount;
 	
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
 					break;
 				}
 				default: {
 					super.onManagerConnected(status);
 					break;
 				}
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
 		
 		mSampleCount = getResources().getStringArray(R.array.sample_file_list).length;
 
 		Spinner sample_spinner = (Spinner) findViewById(R.id.sample_list_spinner);
 		sampleSpinnerListener(sample_spinner);
 		
 		Spinner synth_spinner = (Spinner) findViewById(R.id.synth_type_spinner);
		sampleSpinnerListener(synth_spinner);
 
 		SeekBar vol = (SeekBar) findViewById(R.id.volume);
 		volumeListener(vol);
 
 		mAudioMsgr = AudioMessenger.getInstance(this); // should start the sound up
 		mAudioMsgr.sendSetFileName(getResources().getStringArray(
 				R.array.sample_file_list)[0]);
 	}
 
 	/*
 	 * Edit this one in order to change the volume
 	 */
 	private void volumeListener(SeekBar vol) {
 		//vol.setBackgroundColor(Color.rgb(255, 245, 238));
 		vol.setMax(100);
 		vol.setProgress(80);
 		vol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
 
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {}
 
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress,
 					boolean fromUser) {
 				// change this to change the sound 
 				// get the value form the progress
 				mAudioMsgr.sendFloat("volume", progress / 100f);
 			}
 		});
 	}
 
 	/*
 	 * get the file and change the sound sample
 	 */
 	private void sampleSpinnerListener(Spinner spinner) {
 
 		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 
 			// Select file from spinner
 			@Override
 			public void onItemSelected(AdapterView<?> parent, View view,
 					int pos, long id) {
 				// If the selected item is the last one ("No Sample"), then turn
 				// off the granular synth.  Otherwise, set the sample and turn it on.
 				if (pos == mSampleCount) {
 					mAudioMsgr.sendFloat("gr_go", 0.0f);
 				} else {
 					mAudioMsgr.sendSetFileName(getResources().getStringArray(
 							R.array.sample_file_list)[pos]);
 					mAudioMsgr.sendFloat("gr_go", 1.0f);
 				}
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 			}
 		});
 	}
 	
 	/*
 	 * get the file and change the sound sample
 	 */
 	private void synthSpinnerListener(Spinner spinner) {
 
 		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 
 			// Select file from spinner
 			@Override
 			public void onItemSelected(AdapterView<?> parent, View view,
 					int pos, long id) {
 				Spinner sample_spinner = (Spinner) findViewById(R.id.sample_list_spinner);
 				
 				// The granular synth is chosen
 				if (pos == 0) {
 					
 					// If the selected sample isn't the "no sample", activate the granular synth
 					mAudioMsgr.setGranularIsActive(
 							sample_spinner.getSelectedItemPosition() != mSampleCount);
 					mAudioMsgr.setZoneIsActive(false);
 					sample_spinner.setEnabled(true);
 				} else {
 					mAudioMsgr.setGranularIsActive(false);
 					mAudioMsgr.setZoneIsActive(true);
 					sample_spinner.setEnabled(true);
 				}
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 			}
 		});
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
 		mPrevFeatures = new MatOfPoint2f();
 		mFeatures = new MatOfPoint2f();
 		mFlowVector = new int[2];
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
 		// Grab a frame and process it with the object detector.
 		inputFrame.copyTo(mRgba);
 
 		double[] val = Core.mean(inputFrame).val;
 
 		float grainstart = AudioMessenger.normalize((float)val[0], 1.0f, 0.0f, 255.0f);
 		float graindur = AudioMessenger.normalize((float)val[1], 2000.0f, 10.0f, 255.0f);
 		float grainpitch = AudioMessenger.normalize((float)val[2], 2.0f, 0.3f, 255.0f);
 
 		mAudioMsgr.sendFloat("grainstart_in", grainstart);
 		mAudioMsgr.sendFloat("graindur_in", graindur);
 		mAudioMsgr.sendFloat("grainpitch_in", grainpitch);
 		
 		// Only detect movement every few frames.
 		if (mFrames == 1) {
 			inputFrame.copyTo(mRgba);
 			mFrames = 0;
 			OpticalFlow(
 					mRgba.getNativeObjAddr(),
 					mPrevGray.getNativeObjAddr(),
 					mGray.getNativeObjAddr(),
 					mPrevFeatures.getNativeObjAddr(),
 					mFeatures.getNativeObjAddr(),
 					mFlowVector);
 			Log.e(TAG, "flow xy: (" + mFlowVector[0] + ", " + mFlowVector[1] + ")");
 			mAudioMsgr.sendFloat("x", mFlowVector[0] / 400.0f);
 			mAudioMsgr.sendFloat("y", mFlowVector[1] / 500.0f);
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
 		mAudioMsgr.cleanup();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		// this statement is to active the mLoaderCallBack so it can enable the
 		// camera
 		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
 				mLoaderCallback);
 		mAudioMsgr = AudioMessenger.getInstance(this);
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
 			long addrCurMatGray, long addrPrevFeat, long addrCurFeat, int[] flowVector);
 	
 	public native void DetectFeatures(long addrImg, long addrGrayImg, long addrFeatures, long addrFrame);
 }
