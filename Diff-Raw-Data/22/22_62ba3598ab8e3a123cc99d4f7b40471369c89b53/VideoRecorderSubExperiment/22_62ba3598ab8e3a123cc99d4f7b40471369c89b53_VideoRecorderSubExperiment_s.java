 package ca.ilanguage.oprime.datacollection;
 
 import ca.ilanguage.oprime.R;
 import ca.ilanguage.oprime.content.OPrime;
 import ca.ilanguage.oprime.content.Participant;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.IntentFilter;
 import android.hardware.Camera;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.media.MediaRecorder;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.widget.Toast;
 import android.widget.VideoView;
 
 /**
  * Android video recorder with "no" preview (the preview is a 1x1 pixel which
  * simulates an unobtrusive recording led). Based on Pro Android 2 2010 (Hashimi
  * et al) source code in Listing 9-6.
  * 
  * Also demonstrates how to use the front-facing and back-facing cameras. A
  * calling Intent can pass an Extra to use the front facing camera if available.
  * 
  * Suitable use cases: A: eye gaze tracking library to let users use eyes as a
  * mouse to navigate a web page B: use tablet camera(s) to replace video camera
  * in lab experiments (psycholingusitics or other experiments)
  * 
  * Video is recording is controlled in two ways: 1. Video starts and stops with
  * the activity 2. Video starts and stops on any touch
  * 
  * To control recording in other ways see the try blocks of the onTouchEvent
  * 
  * To incorporate into project add these features and permissions to
  * manifest.xml:
  * 
  * <uses-feature android:name="android.hardware.camera"/> <uses-feature
  * android:name="android.hardware.camera.autofocus"/>
  * 
  * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
  * <uses-permission android:name="android.permission.CAMERA" /> <uses-permission
  * android:name="android.permission.RECORD_AUDIO" />
  * 
  * Tested Date: October 2 2011 with manifest.xml <uses-sdk
  * android:minSdkVersion="8" android:targetSdkVersion="11"/>
  * 
  * To call it, the following Extras are available:
  * 
  * Intent intent;
 			intent = new Intent(
 					"ca.ilanguage.oprime.intent.action.START_VIDEO_RECORDER");
 
 			intent.putExtra(OPrime.EXTRA_USE_FRONT_FACING_CAMERA, true);
 			intent.putExtra(OPrime.EXTRA_VIDEO_QUALITY, OPrime.DEFAULT_DEBUGGING_QUALITY); // will record low quality videos to save space and runtime memory
 			
 			startActivityForResult(intent, OPrime.EXPERIMENT_COMPLETED);
  */
 public class VideoRecorderSubExperiment extends Activity implements
 		SurfaceHolder.Callback {
 	
 	public static final String EXTRA_VIDEO_QUALITY = "videoQuality";
 	public static final String EXTRA_USE_FRONT_FACING_CAMERA = "usefrontcamera";
 	public static final int DEFAULT_DEBUGGING_QUALITY = 500000; //.5 megapixel
 	public static final int DEFAULT_HIGH_QUALITY = 3000000;// 3 megapixel,
 	
 	/*
 	 * Recording variables
 	 */
 	private static final String TAG = "VideoRecorderSubExperiment";
 	private Boolean mRecording = false;
 	private Boolean mUseFrontFacingCamera = false;
 	private VideoView mVideoView = null;
 	private MediaRecorder mVideoRecorder = null;
 	private Camera mCamera;
 	Context mContext;
 	private int mVideoQuality = DEFAULT_HIGH_QUALITY;
 
 	String mAudioResultsFile = "";
 	String mOutputDir = OPrime.OUTPUT_DIRECTORY;
 	
 	private VideoStatusReceiver videoStatusReceiver;
 
 	
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		
 		if (videoStatusReceiver == null){
 		      videoStatusReceiver = new VideoStatusReceiver();
 		    }
 			IntentFilter intentStopped = new IntentFilter(OPrime.INTENT_STOP_VIDEO_RECORDING);
 		    registerReceiver(videoStatusReceiver, intentStopped);
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		/*
 		 * Get extras from the Experiment Home screen and set up layout depending on extras
 		 */
 		setContentView(R.layout.video_recorder);
 		PackageManager pm = getPackageManager();
 		if(pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)){
			Toast.makeText(
					 getApplicationContext(),
					 "This Android has no camera" ,
					 Toast.LENGTH_LONG).show();
 					finish();
 		}else{
 			Toast.makeText(
 					 getApplicationContext(),
 					 "yes camera" ,
 					 Toast.LENGTH_LONG).show();
 		}
 		
 		
 		
 		mVideoView = (VideoView) this.findViewById(R.id.videoView);
 		mOutputDir = getIntent().getExtras().getString(OPrime.EXTRA_OUTPUT_DIR);
 		if(mOutputDir == null){
 			mOutputDir = OPrime.OUTPUT_DIRECTORY;
 		}
 		mVideoQuality = 		getIntent().getExtras().getInt(EXTRA_VIDEO_QUALITY, DEFAULT_HIGH_QUALITY); //default is high quality
 		mAudioResultsFile = 	getIntent().getExtras().getString(OPrime.EXTRA_RESULT_FILENAME);
 		mUseFrontFacingCamera = getIntent().getExtras().getBoolean(
 				EXTRA_USE_FRONT_FACING_CAMERA, true);
 		if (mUseFrontFacingCamera) {
 			// If caller wants to use front facing camera, then make sure the
 			// device has one...
 			// Hard coded to only open front facing camera on Xoom (model MZ604)
 			// For more universal solution try:
 			// http://stackoverflow.com/questions/2779002/how-to-open-front-camera-on-android-platform
 			String deviceModel = android.os.Build.MODEL;
 			if (deviceModel.contains("MZ604")) {
 				mUseFrontFacingCamera = true;
 			} else {
 				 Toast.makeText(
 				 getApplicationContext(),
 				 "The App isn't designed to use this Android's front facing camera.\n "
 				 +
 				 "The device model is : " + deviceModel,
 				 Toast.LENGTH_LONG).show();
 				mUseFrontFacingCamera = false;
 			}
 		}
 		final SurfaceHolder holder = mVideoView.getHolder();
 		holder.addCallback(this);
 		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 	}
 	public void surfaceCreated(SurfaceHolder holder) {
 		if (mRecording) {
 			return;
 		}
 		try {
 			beginRecording(holder);
 		} catch (Exception e) {
 			Log.e(TAG, e.toString());
 			beginRecordingAudio();
 		}
 	}
 
 	public void surfaceDestroyed(SurfaceHolder holder) {
 	}
 
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		Log.v(TAG, "Width x Height = " + width + "x" + height);
 	}
 	private void beginRecordingAudio(){
 		//TODO If the video fails, record audio.
 	}
 	private void stopRecording() throws Exception {
 		mRecording = false;
 		if (mVideoRecorder != null) {
 			mVideoRecorder.stop();
 			mVideoRecorder.release();
 			mVideoRecorder = null;
 			Toast.makeText(
 					 getApplicationContext(),
 					 "Saving video." ,
 					 Toast.LENGTH_LONG).show();
 		}
 		if (mCamera != null) {
 			mCamera.reconnect();
 			mCamera.stopPreview();
 			mCamera.release();
 			mCamera = null;
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 	}
 
 	/* 
 	 * Notes:
 	 * -Beware of security hazard of running code in this receiver.
 	 * In this case, only stopping and saving  the recording. 
 	 * -Receivers should be registered in the manifest, but this is an inner class so that it can access the member functions of this class so it 
 	 * doesn't need to be registered in the manifest.xml.
 	 * 
 	 * http://stackoverflow.com/questions/2463175/how-to-have-android-service-communicate-with-activity
 	 * http://thinkandroid.wordpress.com/2010/02/02/custom-intents-and-broadcasting-with-receivers/
 	 * 
 	 * could pass data in the Intent instead of updating database tables
 	 * 
 	 * @author cesine
 	 */
 	public class VideoStatusReceiver extends BroadcastReceiver {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if (intent.getAction().equals(OPrime.INTENT_STOP_VIDEO_RECORDING)) {
 				finish();
 			}
 		}
 	}
 	@Override
 	protected void onDestroy() {
 		try {
 			stopRecording();
 		} catch (Exception e) {
 			Log.e(TAG, e.toString());
 			//e.printStackTrace();
 		}
 		super.onDestroy();
 		if (videoStatusReceiver != null) {
 		      unregisterReceiver(videoStatusReceiver);
 		    }
 
 	}
 
 	/**
 	 * Uses the surface defined in video_recorder.xml Tested using 2.2 (HTC
 	 * Desire/Hero phone) -> Use all defaults works, records back facing camera
 	 * with AMR_NB audio 3.0 (Motorola Xoom tablet) -> Use all defaults doesn't
 	 * work, works with these specs, might work with others
 	 * 
 	 * @param holder
 	 *            The surfaceholder from the videoview of the layout
 	 * @throws Exception
 	 */
 	private void beginRecording(SurfaceHolder holder) throws Exception {
 		if (mVideoRecorder != null) {
 			mVideoRecorder.stop();
 			mVideoRecorder.release();
 			mVideoRecorder = null;
 		}
 		if (mCamera != null) {
 			mCamera.reconnect();
 			mCamera.stopPreview();
 			mCamera.release();
 			mCamera = null;
 		}
 
 		
 		try {
 			if (mUseFrontFacingCamera) {
 				// hard coded assuming 1 is the front facing camera
 				mCamera = Camera.open(1);
 			} else {
 				mCamera = Camera.open();
 			}
 
 			// Camera setup is based on the API Camera Preview demo
 			mCamera.setPreviewDisplay(holder);
 			Camera.Parameters parameters = mCamera.getParameters();
 			parameters.setPreviewSize(640, 480);
 			mCamera.setParameters(parameters);
 			mCamera.startPreview();
 			mCamera.unlock();
 
 			mVideoRecorder = new MediaRecorder();
 			mVideoRecorder.setCamera(mCamera);
 
 			// Media recorder setup is based on Listing 9-6, Hashimi et all 2010
 			// values based on best practices and good quality,
 			// tested via upload to YouTube and played in QuickTime on Mac Snow
 			// Leopard
 			mVideoRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
 			mVideoRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
 			mVideoRecorder
 					.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);// THREE_GPP
 																			// is
 																			// big-endian,
 																			// storing
 																			// and
 																			// transferring
 																			// the
 																			// most
 																			// significant
 																			// bytes
 																			// first.
 																			// MPEG_4
 																			// as
 																			// another
 																			// option
 			mVideoRecorder.setVideoSize(640, 480);// YouTube recommended size:
 													// 320x240,
 													// OpenGazer eye tracker:
 													// 640x480
 													// YouTube HD: 1280x720
 			mVideoRecorder.setVideoFrameRate(20); // might be auto-determined
 													// due to lighting
 			 mVideoRecorder.setVideoEncodingBitRate(mVideoQuality);// 3000000=3 megapixel,
 			// or the max of
 			// the camera
 			mVideoRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);// MPEG_4_SP
 																			// Simple
 																			// Profile
 																			// is
 																			// for
 																			// low
 																			// bit
 																			// rate
 																			// and
 																			// low
 																			// resolution
 																			// H264
 																			// is
 																			// MPEG-4
 																			// Part
 																			// 10
 																			// is
 																			// commonly
 																			// referred
 																			// to
 																			// as
 																			// H.264
 																			// or
 																			// AVC
 			int sdk = android.os.Build.VERSION.SDK_INT;
 			// Gingerbread and up can have wide band ie 16,000 hz recordings
 			// (Okay quality for human voice)
 			if (sdk >= 10) {
 				mVideoRecorder
 						.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
 				mVideoRecorder.setAudioSamplingRate(16000);
 			} else {
 				// Other devices only have narrow band, ie 8,000 hz
 				// (Same quality as a phone call, not really good quality for
 				// any purpose.
 				// For human voice 8,000 hz means /f/ and /th/ are
 				// indistinguishable)
 				mVideoRecorder
 						.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
 			}
 			mVideoRecorder.setMaxDuration(600000); // limite to 10min 600,000limit to 30 seconds 30,000
 			mVideoRecorder.setPreviewDisplay(holder.getSurface());
 			mVideoRecorder.setOutputFile(mAudioResultsFile);
 			mVideoRecorder.prepare();
 			mVideoRecorder.start();
 			mRecording = true;
 		} catch (Exception e) {
 			Log.e(TAG, e.toString());
 			
 		}
 	}
 }
