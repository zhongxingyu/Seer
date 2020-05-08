 package com.mobvcasting.localreport2012;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.res.Configuration;
 import android.hardware.Camera;
 import android.hardware.Camera.Size;
 import android.media.CamcorderProfile;
 import android.media.MediaRecorder;
 import android.net.ConnectivityManager;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.Environment;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class VideoCapture extends Activity implements OnClickListener, SurfaceHolder.Callback {
 
 	public static final String LOGTAG = "VIDEOCAPTURE";
 
 	public static final int HIGH_QUALITY = 1;
 	public static final int LOW_QUALITY = 0;
 	private int recordingQuality = LOW_QUALITY;
 	
 	public static final int TARGET_WIDTH = 720;
 	public static final int TARGET_HEIGHT = 480;
 	public static final int LOW_TARGET_BITRATE = 700000;
 	public static final int HIGH_TARGET_BITRATE = 1500000;
 	public static final int TARGET_FRAMERATE = 30;
 	
 	int videoWidth = 0;
 	int videoHeight = 0;
 	int videoFramerate = 0;
 	int videoBitrate = LOW_TARGET_BITRATE;
 	int videoEncoder = MediaRecorder.VideoEncoder.H264;
 	int videoSource = MediaRecorder.VideoSource.DEFAULT;
 	
 	private MediaRecorder recorder;
 	private SurfaceHolder holder;
 	//private CamcorderProfile camcorderProfile;
 	private Camera camera;	
 	private TextView countdownText;
 	private Button startButton;
 	private SurfaceView cameraView;
 	
 	private static long RECORD_TIME = 20000;
     private static long ONE_SECOND = 1000;
     
 	boolean recording = false;
 	boolean usecamera = true;
 	boolean previewRunning = false;
 	
 	String filePath = "";
 
     CountDownTimer countDownTimer;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 
 		//camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
		//camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
 		
 	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 	    if (cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()) {
 	    	//camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
 	    	recordingQuality = HIGH_QUALITY;
         	Toast.makeText(this, "WiFi Enabled, High Quality Recordng", Toast.LENGTH_LONG).show();
 	    } else if (cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isAvailable()) {
 	    	recordingQuality = LOW_QUALITY;
 	    	Toast.makeText(this, "WiFi Not Enabled, Low Quality Recordng", Toast.LENGTH_LONG).show();
 	    } else {
 	    	Toast.makeText(this, "No Network Connection Available, Can Not Record Video", Toast.LENGTH_LONG).show();
 	    	finish();
 	    }
 
 		setContentView(R.layout.activity_video_capture);
 
 		cameraView = (SurfaceView) findViewById(R.id.CameraView);
 		holder = cameraView.getHolder();
 		holder.addCallback(this);
 		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 
 		cameraView.setClickable(true);
 		cameraView.setOnClickListener(this);
 		
 		startButton = (Button) this.findViewById(R.id.StartButton);
 		startButton.setOnClickListener(this);
 		
 		countdownText = (TextView) this.findViewById(R.id.CountDownTimer);
 		countDownTimer = new CountDownTimer(RECORD_TIME, ONE_SECOND) {
 
 			public void onTick(long millisUntilFinished) {
 		    	 countdownText.setText("Time remaining: " + millisUntilFinished / ONE_SECOND);
 		     }
 
 		     public void onFinish() {
 		    	 stopRecording();
 		    	 countdownText.setText("Recording Complete");
 		     }
 		};
 	}
 	
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
       super.onConfigurationChanged(newConfig);
     }
 
 	private void prepareRecorder() {
         recorder = new MediaRecorder();
 		recorder.setPreviewDisplay(holder.getSurface());
 		
 		if (usecamera) {
 			camera.unlock();
 			recorder.setCamera(camera);
 		}
 		
 		recorder.setVideoSource(videoSource);
 		recorder.setMaxDuration((int)(RECORD_TIME + ONE_SECOND));
 		//recorder.setMaxFileSize(5000000); // Approximately 5 megabytes
 
		recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
 		
 		//Looping through settings in SurfaceChanged
 		System.out.println("Size: " + videoWidth + " " + videoHeight);
 		if (videoWidth > 0) {
 			recorder.setVideoSize(videoWidth, videoHeight);
 		} else {
 			recorder.setVideoSize(TARGET_WIDTH, TARGET_HEIGHT);
 		}
 
 		//Looping through settings in SurfaceChanged
 		System.out.println("Framerate: " + videoFramerate);
 		if (videoFramerate > 0) {
 			recorder.setVideoFrameRate(videoFramerate);
 		} else {
 			recorder.setVideoFrameRate(TARGET_FRAMERATE);
 		}
 		
 		recorder.setVideoEncoder(videoEncoder);
 
 		if (recordingQuality == HIGH_QUALITY) {
 			videoBitrate = HIGH_TARGET_BITRATE;
 		} else {
 			videoBitrate = LOW_TARGET_BITRATE;
 		}
 		recorder.setVideoEncodingBitRate(videoBitrate);
 		
 		try {
 			File newFile = File.createTempFile("localreport", ".mp4", Environment.getExternalStorageDirectory());
 			filePath = newFile.getAbsolutePath();
 			recorder.setOutputFile(filePath);
 
 			recorder.prepare();			
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 			finish();
 		} catch (IOException e) {
 			e.printStackTrace();
 			finish();
 		}
 	}
 
 	public void stopRecording() {
 		if (recording) {
 			recorder.stop();
 			/*
 			if (usecamera) {
 				try {
 					camera.reconnect();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			*/			
 			recorder.release();
 			recording = false;
 			Log.v(LOGTAG, "Recording Stopped");
 			// Let's prepareRecorder so we can record again - NOPE
 			//prepareRecorder();
 			
 			// Upload it
 			Intent fileUpIntent = new Intent(this,FileUploader.class);
 			fileUpIntent.putExtra("filePath", filePath);
 			fileUpIntent.putExtra("audio_or_video", "video");
 			startActivity(fileUpIntent);
 			finish();
 		}
 	}
 	
 	public void onClick(View v) {
 		if (v == startButton || v == cameraView) {
 			if (!recording) {
 				recording = true;
 				recorder.start();
 				countDownTimer.start();		    
 				Log.v(LOGTAG, "Recording Started");
 				startButton.setEnabled(false);
 			}
 		}
 	}
 
 	public void surfaceCreated(SurfaceHolder holder) {
 		Log.v(LOGTAG, "surfaceCreated");
 		
 		if (usecamera) {
 			camera = Camera.open();
 			
 			try {
 				camera.setPreviewDisplay(holder);
 				camera.startPreview();
 				previewRunning = true;
 			}
 			catch (IOException e) {
 				Log.e(LOGTAG,e.getMessage());
 				e.printStackTrace();
 			}	
 		}		
 		
 	}
 
 
 	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 		Log.v(LOGTAG, "surfaceChanged");
 
 		if (!recording && usecamera) {
 			if (previewRunning){
 				camera.stopPreview();
 			}
 
 			try {
 				Camera.Parameters cParameters = camera.getParameters();
 				
 				List<Size> supportedSizes = cParameters.getSupportedPreviewSizes();
 				Iterator<Size> supportedSizesI = supportedSizes.iterator();
 				while (supportedSizesI.hasNext()) {
 					Size cSize = supportedSizesI.next();
					System.out.println("Supports: " + cSize);
					if (cSize.width <= TARGET_WIDTH && TARGET_WIDTH - cSize.width < TARGET_WIDTH - videoWidth) {
 						videoWidth = cSize.width;
 						videoHeight = cSize.height;
 						System.out.println("Using");
 					}
 				}
 			    if (videoWidth > 0) {
 			    	cParameters.setPreviewSize(videoWidth, videoHeight);
 					Log.v(LOGTAG,"Preview Size: " + videoWidth + " " + videoHeight);
 			    }
 				
 				List<Integer> supportedFramerates = cParameters.getSupportedPreviewFrameRates();
 				Iterator<Integer> supportedFrameratesI = supportedFramerates.iterator();
 				while (supportedFrameratesI.hasNext()) {
 					Integer cFramerate = supportedFrameratesI.next();
 					System.out.println("Supports: " + cFramerate);
 					if (cFramerate <= TARGET_FRAMERATE && TARGET_FRAMERATE - cFramerate < TARGET_FRAMERATE - videoFramerate) {
 						videoFramerate = cFramerate;
 						System.out.println("Using");
 					}
 				}
 				if (videoFramerate > 0) {
 			    	cParameters.setPreviewFrameRate(videoFramerate);
 			    	Log.v(LOGTAG,"Framerate: " + videoFramerate);
 				}
 
 			    camera.setParameters(cParameters);
 				
 				camera.setPreviewDisplay(holder);
 				camera.startPreview();
 				previewRunning = true;
 			}
 			catch (IOException e) {
 				Log.e(LOGTAG,e.getMessage());
 				e.printStackTrace();
 			}	
 			
 			prepareRecorder();	
 		}
 	}
 
 	
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		Log.v(LOGTAG, "surfaceDestroyed");
 		if (recording) {
 			recorder.stop();
 			recording = false;
 		}
 		recorder.release();
 		if (usecamera) {
 			previewRunning = false;
 			camera.lock();
 			camera.release();
 		}
 	}
 }
