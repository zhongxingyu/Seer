 package org.opencv.samples.facedetect;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.opencv.android.BaseLoaderCallback;
 import org.opencv.android.CameraBridgeViewBase;
 import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
 import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
 import org.opencv.android.LoaderCallbackInterface;
 import org.opencv.android.OpenCVLoader;
 import org.opencv.core.Core;
 import org.opencv.core.Mat;
 import org.opencv.core.MatOfRect;
 import org.opencv.core.Rect;
 import org.opencv.core.Scalar;
 import org.opencv.highgui.Highgui;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class FdActivity extends Activity implements CvCameraViewListener2 {
 
 	private static final String TAG = "OCVSample::Activity";
 	private static final Scalar FACE_RECT_COLOR = new Scalar(255, 0, 0, 255);
 	public static final int JAVA_DETECTOR = 0;
 	public static final int NATIVE_DETECTOR = 1;
 
 	private Mat mRgba;
 	private Mat mGray;
 	private Mat face;
 	private File mCascadeFile;
 	private File sadPhoto, happyPhoto, dir;
 	static public DetectionBasedTracker mNativeDetector;
 
 	private float mRelativeFaceSize = 0.2f;
 	private int mAbsoluteFaceSize = 0;
 	private boolean mood, bDetect;
 	private Intent player;
	private MenuItem mExit;
 
 	private CameraBridgeViewBase mOpenCvCameraView;
 
 	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
 		@Override
 		public void onManagerConnected(int status) {
 			switch (status) {
 			case LoaderCallbackInterface.SUCCESS: {
 				Log.i(TAG, "OpenCV loaded successfully");
 
 				// Load native library after(!) OpenCV initialization
 				System.loadLibrary("detection_based_tracker");
 
 				try {
 					// load cascade file from application resources
 					InputStream is = getResources().openRawResource(
 							R.raw.haarcascade_mcs_mouth);
 					File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
 					mCascadeFile = new File(cascadeDir, "haarcascade_mcs_mouth.xml");
 					FileOutputStream os = new FileOutputStream(mCascadeFile);
 
 					byte[] buffer = new byte[4096];
 					int bytesRead;
 					while ((bytesRead = is.read(buffer)) != -1) {
 						os.write(buffer, 0, bytesRead);
 					}
 					is.close();
 					os.close();
 
 					mNativeDetector = new DetectionBasedTracker(
 							mCascadeFile.getAbsolutePath(), 0);
 
 					cascadeDir.delete();
 
 				} catch (IOException e) {
 					e.printStackTrace();
 					Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
 				}
 
 				mOpenCvCameraView.enableView();
 			}
 				break;
 			default: {
 				super.onManagerConnected(status);
 			}
 				break;
 			}
 		}
 	};
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
 				mLoaderCallback);
 
 		player = new Intent(FdActivity.this, PlayerViewDemoActivity.class);
 
 		Log.i(TAG, "called onCreate");
 		super.onCreate(savedInstanceState);
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 
 		setContentView(R.layout.face_detect_surface_view);
 
 		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
 		mOpenCvCameraView.setCvCameraViewListener(this);
 		mood = false;
 		final Button bSwitch = (Button) findViewById(R.id.bSwitch);
 		bSwitch.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (happyPhoto.exists() == false || sadPhoto.exists() == false) {
 					dir = new File(Environment.getExternalStorageDirectory()
 							.getAbsolutePath() + "/moodplayer");
 					dir.mkdirs();
 					if (face != null) {
 						if (mood == false) {
 							// incadreaza gura
 							Highgui.imwrite(Environment.getExternalStorageDirectory()
 									.getPath() + "/moodplayer/face_sad.jpg", face);
 							mood = true;
							TextView tvTop = (TextView) findViewById(R.id.topTextView);
 							tvTop.setText("Now please make a happy face");
 							bSwitch.setText("Take happy picture");
 						} else {
 							// incadreaza gura
 							Highgui.imwrite(Environment.getExternalStorageDirectory()
 									.getPath() + "/moodplayer/face_happy.jpg", face);
							TextView tvTop = (TextView) findViewById(R.id.topTextView);
 							tvTop.setText("Thanks, enjoy your music!");
 							bSwitch.setText("Go to player");
 							bDetect = false;
 						}
 					}
 				} else {
 					startActivity(player);
 					finish();
 				}
 			}
 		});
 
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		if (mOpenCvCameraView != null)
 			mOpenCvCameraView.disableView();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		bDetect = true;
 		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
 				mLoaderCallback);
 
 		happyPhoto = new File(Environment.getExternalStorageDirectory()
 				.getAbsolutePath() + "/moodplayer/face_happy.jpg");
 		sadPhoto = new File(Environment.getExternalStorageDirectory()
 				.getAbsolutePath() + "/moodplayer/face_sad.jpg");
 
 		if (happyPhoto.exists() && sadPhoto.exists()) {
 			startActivity(player);
 			finish();
 		}
 	}
 
 	public void onDestroy() {
 		super.onDestroy();
 		mOpenCvCameraView.disableView();
 	}
 
 	public void onCameraViewStarted(int width, int height) {
 		mGray = new Mat();
 		mRgba = new Mat();
 	}
 
 	public void onCameraViewStopped() {
 		mGray.release();
 		mRgba.release();
 	}
 
 	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
 
 		mRgba = inputFrame.rgba();
 		mGray = inputFrame.gray();
 
 		if (bDetect == true) {
 			if (mAbsoluteFaceSize == 0) {
 				int height = mGray.rows();
 				if (Math.round(height * mRelativeFaceSize) > 0) {
 					mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
 				}
 				mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
 			}
 
 			MatOfRect faces = new MatOfRect();
 
 			mNativeDetector.detect(mGray, faces);
 
 			Rect[] facesArray = faces.toArray();
 			if (facesArray.length != 0) {
 				Core.rectangle(mRgba, facesArray[0].tl(), facesArray[0].br(),
 						FACE_RECT_COLOR, 3);
 				face = mRgba.submat(facesArray[0]);
 			}
 		}
 
 		return mRgba;
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu) {
		mExit = menu.add("Quit application");
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
		if (item == mExit)
			finish();
 		return true;
 	}
 
 	public boolean onTouch(View v, MotionEvent event) {
 		return false;
 	}
 
 }
