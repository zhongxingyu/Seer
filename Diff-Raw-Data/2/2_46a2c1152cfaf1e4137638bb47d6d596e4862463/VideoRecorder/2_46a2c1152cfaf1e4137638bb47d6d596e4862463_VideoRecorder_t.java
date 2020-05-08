 package com.monllao.david.androidrestclient.camera;
 
 import java.io.File;
 import java.util.Iterator;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.hardware.Camera;
 import android.hardware.Camera.Size;
 import android.media.MediaRecorder;
 import android.os.Handler;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.View;
 import android.widget.FrameLayout;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.MediaController;
 
 import com.monllao.david.androidrestclient.AndroidRestClientActivity;
 import com.monllao.david.androidrestclient.R;
 import com.monllao.david.androidrestclient.User;
 import com.monllao.david.androidrestclient.VideoDataActivity;
 
 public class VideoRecorder {
 	
 	/**
 	 * The caller activity
 	 */
 	private Activity activity;
 	
 	/**
 	 * Application user
 	 */
 	private User user;
 	
 	private boolean shareButtonClicked = false;
 	private boolean isRecording = false;
 
 	private MediaRecorder mediaRecorder;
 	private Camera camera = null;
     private File outputFile;
     
     private FrameLayout frameLayout;
 	private CameraVideoPreview preview = null;
     private RecordingViewer recordingViewer = null;
     
 	ImageButton captureButton;
 	ImageButton shareButton;
 	final ImageView counterView;
 	
 	/**
 	 * To reduce the counter
 	 */
 	private Handler mHandler;
 	private int counterValue;
 	
 	/**
 	 * Video height
 	 */
 	private int height;
 	
 	/**
 	 * Video width
 	 */
 	private int width;
 	
 	
 	/**
 	 * Fills the main layout
 	 * @param activity
 	 */
 	public VideoRecorder(Activity activity) {
 
 		this.activity = activity;
     	frameLayout = (FrameLayout) activity.findViewById(R.id.screen);
     	
     	// Sets the counterView object with the view element
     	counterView = (ImageView) activity.findViewById(R.id.counter);
 
     	// To manage the available time
     	mHandler = new Handler();
     	
     	// Display the camera preview
     	setPreviewView();
         
     	// Buttons to share and capture
         this.addCaptureButton();
         this.addShareButton();
 	}
 		
 
     /**
      * Initialises the share button 
      * 
      * Hidden until a recording is available 
      */
     protected void addShareButton() {
     	
     	shareButton = (ImageButton) activity.findViewById(R.id.button_share);
 
         // Make it invisible until we have a recording
         shareButton.setVisibility(View.INVISIBLE);
         
     	shareButton.setOnClickListener(
     		new View.OnClickListener() {
 			
 				public void onClick(View v) {
 				    shareButtonClicked = true;
 
 				    // We need both share click and the user to be set before we send the video
 				    if (user != null) {
 				    	initVideoData();
 				    }
 				}
 			}
     	);
     }
     
     
     /**
      * Restarts the counter to 0
      */
     protected void restartCounter() {
     	
     	counterView.setVisibility(View.VISIBLE);
     	counterValue = AndroidRestClientActivity.VIDEO_SECS;
     	counterView.setImageResource(activity.getResources().getIdentifier("counter_" + counterValue, "drawable","com.monllao.david.androidrestclient"));
     	counterValue--;
     	
     	// second to second
     	mHandler.postDelayed(counterReducerTask, 1000);
     }
     
     
     /**
      * Task to be executed every second when recording to reduce to counter
      */
     private Runnable counterReducerTask = new Runnable() {
     	
         public void run() {
         	
         	if (counterValue >= 0) {
         		
 	        	counterView.setImageResource(activity.getResources().getIdentifier("counter_" + counterValue, "drawable","com.monllao.david.androidrestclient"));
 	        	counterValue--;
 	        	mHandler.postDelayed(this, 1000);
         	}
         }
     };
     
     /**
      * Sets the video user and redirects to VideoData if the shared button is pressed
      * @param user
      */
     public void setUser(User user) {
 	    
     	this.user = user;
 
 	    // We need both share click and the user to be set before we send the video
 	    if (shareButtonClicked == true) {
 	    	initVideoData();
 	    }
     }
     
     /**
      * Initialises the button to capture
      */
     protected void addCaptureButton() {
         
         // Capture button
         captureButton = (ImageButton) activity.findViewById(R.id.button_rec);
     	captureButton.setOnClickListener(
     		new View.OnClickListener() {
     			
     			public void onClick(View v) {
     				
     				// stop recording, release camera and show the video
     				if (isRecording) {
     					stopRecording();
     	                
     	            // start recording
     	            } else {
     	            	startRecording();
     	            }
 
     			}
     		}
     	);
     	
     }
 
     
     /**
      * Instantiates the media recorder and assigns the on max duration reached
      */
     protected void createMediaRecorder() {
 
     	mediaRecorder = new MediaRecorder();
     	mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
 			
 			public void onInfo(MediaRecorder mr, int what, int extra) {
 				
 				if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
 					stopRecording();
 				}
 			}
 		});
     	
     }
     
     
     /**
      * Releases the mediarecorder, the camera and sets the recording view
      */
     protected void stopRecording() {
 
         preview.mediaRecorder.stop();
         release();
         camera = null;
         
         isRecording = false;
         
         // Stop image changes to rec image
         captureButton.setImageResource(R.drawable.record);
         
         // Share button available 
         shareButton.setVisibility(View.VISIBLE);
         
         // Hide the counter and reset it
         counterView.setVisibility(View.INVISIBLE);
         mHandler.removeCallbacks(counterReducerTask);
         
         // Display the recorded video
         setViewerView();
     }
     
     
     /**
      * Starts the recording and fills the record layout if it's empty
      */
     protected void startRecording() {
 
     	// If it's not the first recording restore the preview video
     	if (outputFile != null) {
     		setPreviewView();
     	}
     	
     	// Create the mediaRecorder here to get the info event
     	createMediaRecorder();
 
     	// New path / filename for the video
     	outputFile = CameraStorageManager.getOutputMediaFile();
 		preview.readyToRec(mediaRecorder, outputFile);
 		
 		isRecording = true;
 		
 		// Rec image changes to stop image
 		captureButton.setImageResource(R.drawable.stop);
 		
 		// Share button no visible
 		shareButton.setVisibility(View.INVISIBLE);
 		
 		// Restart the counter to VIDEO_SECS
 		restartCounter();
     }
     
     
     /**
      * New activity to set up the video data
      */
     protected void initVideoData() {
 
         // Set up the video data while the video is being sent
         Intent intent = new Intent(activity, VideoDataActivity.class);
         intent.putExtra("outputPath", outputFile.getPath());
         intent.putExtra("user", user);
         
         activity.startActivityForResult(intent, AndroidRestClientActivity.ACTIVITY_VIDEODATA);
     }
     
     
     /**
      * Fills the surface view with the video preview
      */
     protected void setPreviewView() {
 
 		// Getting the camera
     	try {
 			camera = Camera.open();
     	} catch (Exception e) {
     		Log.e(AndroidRestClientActivity.APP_NAME, "Can\'t get Camera Instance");
     	}
 	    
     	calculateOptimalScreenSize();
 	    preview = new CameraVideoPreview(this.activity, camera, width, height);
 		
 		frameLayout.removeAllViews();
 		frameLayout.addView(preview);
 
     }
     
     
     /**
      * Fills the surface view with the player
      */
     protected void setViewerView() {
 
     	recordingViewer = new RecordingViewer(activity, this.width, this.height);
     	
     	frameLayout.removeAllViews();
     	frameLayout.addView(recordingViewer);
 
     	// Video controls
     	MediaController mediaController = new MediaController(activity);
     	
     	// Assign the video controls to the view and start the reproduction
     	recordingViewer.setVideoPath(outputFile.getPath());
     	recordingViewer.setMediaController(mediaController);
     	recordingViewer.requestFocus();
     	recordingViewer.start();
     }
 
 
     /**
      * The desirable size is mmaximum screen height with minimal width
      */
     protected void calculateOptimalScreenSize() {
 
 		DisplayMetrics metrics = new DisplayMetrics();
 		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
 		
 		int screenHeight = metrics.heightPixels;
 		int screenWidth = metrics.widthPixels;
 
 		List<Size> supportedSizes = camera.getParameters().getSupportedPreviewSizes();
 		Iterator<Size> it = supportedSizes.iterator();
 		while (it.hasNext()) {
 			Size size = it.next();
 
 			Log.v(AndroidRestClientActivity.APP_NAME, "Supports: " + size.width+ ":" + size.height);
 			
 			// Same height minimum width available, probably 4:3
 			if (screenHeight == size.height && screenWidth > size.width) {
 
 				Log.i(AndroidRestClientActivity.APP_NAME, "Selected size: " + size.width+ ":" + size.height);
 				
 				height = size.height;
 				width = size.width;
 				
 			// As an alternative for some devices
			} else if (screenHeight > size.height && screenWidth > size.width && height == 0 && width == 0) {
 				
 				height = size.height;
 				width = size.width;
 			}
 		}
 		
     }
     
 
     /**
      * Releases both media recorder and camera
      * 
      * Redirects the call to CameraVideoPreview which owns the mediaRecorder
      */
     public void release() {
     	preview.release();
     }
     
 }
