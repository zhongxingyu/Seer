 package com.JS.musictranscribe;
 
 import com.dropbox.client2.DropboxAPI;
 import com.dropbox.client2.android.AndroidAuthSession;
 import com.dropbox.client2.session.AccessTokenPair;
 import com.dropbox.client2.session.AppKeyPair;
 import com.dropbox.client2.session.Session.AccessType;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.media.MediaRecorder.AudioSource;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 
 public class RecordActivity extends Activity {
 
 	private static final String TAG = "RecordActivity";
 	
 	private final int mSAMPLING_SPEED = 44100; //samples per second, 44100 default (guaranteed support on devices)
 	
 	private final int mMAX_NOTE_SECONDS = 50; 			// SECONDS
 	private final int mFULL_BUFFER_SIZE = mSAMPLING_SPEED*mMAX_NOTE_SECONDS;
 	private final float mRECORDER_BUFFER_SIZE_MULTIPLIER = 1; //*0.5 bec 16 bit
 
 	private final int mEXTERNAL_BUFFER_TIME = 100; //desired milliseconds
 	private final int mEXTERNAL_BUFFER_SIZE = Helper.nextLowerPowerOf2((int)(mSAMPLING_SPEED*((float)mEXTERNAL_BUFFER_TIME/1000))); //find next lower power of two
 	private final int mACTUAL_EXTERNAL_BUFFER_TIME = mEXTERNAL_BUFFER_SIZE*1000/mSAMPLING_SPEED; //find actual time being measured based on above
 	
 	private boolean mIsRecordingPaused = true;
 	private boolean mIsRecordingDone = false;
 	private boolean mIsFirstToggle = true;
 	private Button mRecordingPausePlayButton;	
 	private Button mFinishRecordingButton;
 	private Button mGraphButton;
 	private AudioAnalyzer mAudioAnalyzer;
 
 	//DEBUG
 		private Button dGraphEveryCycleToggleButton;
 		private boolean dGraphEveryCycle = false;
 		
 		private Button dDBDataUploadToggleButton;
 		private boolean dDBDataUploadEnabled = false;
 		
 		private boolean mIsDBLoggedIn;
 
 	    
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_record);
 				
 		
 		mRecordingPausePlayButton = (Button) findViewById(R.id.Record_PausePlay_Button);
 		mRecordingPausePlayButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				toggleRecording();
 			}
 		});
 
 		mGraphButton = (Button) findViewById(R.id.Make_Graph_Button);
 		mGraphButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				makeGraphs();
 			}
 		});
 
 		dGraphEveryCycleToggleButton = (Button) findViewById(R.id.dGraph_Every_Cycle_Button);
 		dGraphEveryCycleToggleButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				toggleGraphEveryCycle();
 			}
 		});
 		
 		
 		
		/*NEED TO DECIDE:
		 * Where to put the Dropbox Upload, here or in AudioAnalyzer or in Helper class
		 * Currently it is here but that is inconvenient since most of the processing happens in AudioAnalyzer
		 * I'm thinking best is in an external helper class for AudioAnalyzer
		 */
 		dDBDataUploadToggleButton = (Button) findViewById(R.id.dDropbox_Upload_Button);
 		dDBDataUploadToggleButton.setEnabled(false); //only enable if DB is accessible
 		dDBDataUploadToggleButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				toggleDBDataUpload();
 			}
 		});
 
 		
 		
 		mAudioAnalyzer = new AudioAnalyzer(AudioSource.MIC, mSAMPLING_SPEED, 
 				true, true, mEXTERNAL_BUFFER_SIZE, this); 
 				//isMono and is16Bit = true, this = context to pass in for graphing activity source
 
 		Log.i(TAG,"Desired Buffer Time: "+mEXTERNAL_BUFFER_TIME+", Actual buffer time:"+ mACTUAL_EXTERNAL_BUFFER_TIME +" \n\n");
 
 		
 		mIsDBLoggedIn = getIntent().getBooleanExtra("DBLoggedIn", false);
 		if (mIsDBLoggedIn) {
 			dDBDataUploadToggleButton.setEnabled(true);
 			mAudioAnalyzer.setDBLoggedIn(true);
 		}
 		else {
 			dDBDataUploadToggleButton.setEnabled(false);
 			mAudioAnalyzer.setDBLoggedIn(false);
 		}
 		
 	}
 	
 	// -----END onCreate
 
 	/*
 	 * This is here to catch returning back buttons
 	 * Checks to see if graphEveryCycle is enabled
 	 * if yes, then start recorder right away again!
 	 */
 	@Override
 	public void onResume() {
		Log.i(TAG,"Onresume called");
 		super.onResume();
 		if (mAudioAnalyzer.isGraphEveryCycle()) {
			Log.i(TAG,"Resuming Recording");
 			resumeRecording();
 		}
 	}
 	
 	
 	// -----END onResume
 	
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.record, menu);
 		return true;
 	}
 
 	
 	/*
 	 * Required to be overridden to get intent back from sub-activity
 	 */
 	@Override 
 	public void onActivityResult(int requestCode, int resultCode, Intent returnIntent) {     
 	  super.onActivityResult(requestCode, resultCode, returnIntent); 
 	  switch(requestCode) { 
 	    case (AudioAnalyzer.GRAPH_EVERY_CYCLE_IDNUM) : { 
 	      if (resultCode == Activity.RESULT_OK) { 
 		      dGraphEveryCycle = returnIntent.getBooleanExtra("disableGraphContinously", true);
 		      toggleGraphEveryCycle(); //updates based on dGraphEveryCycle so it's safe this way
 		      pauseRecording();
 	      } 
 	      break; 
     	} 
 	  } 
 	}
 
 	private void toggleRecording() {
 		if (mIsFirstToggle) { //this only runs the first time
 			mIsRecordingPaused = false;
 			mAudioAnalyzer.startRecording();
 			mIsFirstToggle = false;
 		}
 		else if (mIsRecordingPaused) {
 			mIsRecordingPaused = false;
 			mAudioAnalyzer.resumeRecording();
 		}
 		else if (!mIsRecordingPaused) {
 			mIsRecordingPaused = true;
 			mAudioAnalyzer.pauseRecording();
 		}
 		mRecordingPausePlayButton.setText("paused: " +( mIsRecordingPaused ? "true" : "false"));
 	}
 	
 	private void pauseRecording() {
 		mIsRecordingPaused = false;
 		toggleRecording();
 	}
 	
 	private void resumeRecording() {
 		mIsRecordingPaused = true;
 		toggleRecording();
 	}
 
 	
 	//this is separate because I suspect we will want to send them to editing and or postprocess all of it at this point
 	private void finishRecording() {
 		if (mIsRecordingDone) {
 		}
 		else if (!mIsRecordingDone) {
 			mIsRecordingDone = true;		
 			mAudioAnalyzer.finishRecording();
 		}
 	}
 
 	private void makeGraphs() {
 		if (!mIsRecordingPaused) {
 			toggleRecording();
 		}
 		while (!mAudioAnalyzer.isThreadingPaused()) { //wait until the threading is actually paused to ensure everything is updated
 			try {			
 				Thread.sleep(1);
 			} catch (InterruptedException e) {}
 		}
 		mAudioAnalyzer.makeGraphs(mAudioAnalyzer.getRawAudioDataDoubles(), mAudioAnalyzer.getIntervalFreqData());
 	}
 				
 
 	private void toggleGraphEveryCycle() {
 		if (dGraphEveryCycle) {
 			mGraphButton.setEnabled(true); 
 			mAudioAnalyzer.dGraphEveryCycle = false;
 			dGraphEveryCycle = false;
 		} 
 		else if (!dGraphEveryCycle) {
 			mGraphButton.setEnabled(false); //disable the makeGraph button if graphing every new measurement
 			mAudioAnalyzer.dGraphEveryCycle = true;
 			dGraphEveryCycle = true;
 		}
 		dGraphEveryCycleToggleButton.setText("ContGraph: "+ (dGraphEveryCycle ? "true" : "false"));
 	}
 		
 	private void toggleDBDataUpload() {
 		if (dDBDataUploadEnabled) {
 			setDBDataUpload(false);
 			dDBDataUploadToggleButton.setText("Enable DB");
 		}
 		else {
 			dDBDataUploadToggleButton.setText("Disable DB");
 			setDBDataUpload(true);
 		}
 	}
 
 	
 	private void setDBDataUpload(boolean uploadEnabled) {
 		dDBDataUploadEnabled = uploadEnabled;
 		mAudioAnalyzer.setDBDataUpload(uploadEnabled);
 	}
 	
 	private boolean isDBDataUploadEnabled() {
 		return dDBDataUploadEnabled;
 	}
 }
