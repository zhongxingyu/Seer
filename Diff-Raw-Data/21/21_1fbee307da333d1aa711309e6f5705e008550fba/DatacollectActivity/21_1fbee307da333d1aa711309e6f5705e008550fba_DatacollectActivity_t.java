 package com.JS.musictranscribe;
 
 import java.util.HashMap;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.media.MediaRecorder.AudioSource;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 
 
 
 public class DatacollectActivity extends Activity {
 	
 	//tag
 	private static final String TAG = "DatacollectActivity";
 	
 	//for AudioCollector
 	private final int mSAMPLING_SPEED = 44100; //samples per second, 44100 default (guaranteed support on devices)
 	
 	private final int mEXTERNAL_BUFFER_TIME = 100; //desired milliseconds
 	private final int mEXTERNAL_BUFFER_SIZE = Helper.nextLowerPowerOf2((int)(mSAMPLING_SPEED*((float)mEXTERNAL_BUFFER_TIME/1000))); //find next lower power of two
 	private final int mACTUAL_EXTERNAL_BUFFER_TIME = mEXTERNAL_BUFFER_SIZE*1000/mSAMPLING_SPEED; //find actual time being measured based on above
 
 	
 	//UI 
 	private EditText mNumRecordingsEditText;
 	private Button mStartNRecordingsButton;
 	
 	private EditText mTimedRecordingEditText;
 	private Button mStartTimedRecordingButton;
 	
 	private Button mRecordingSummaryButton;
 	
 	private EditText mNoteNumEditText;
 	private Button mGetNoteDataButton;
 	
 	private TextView mStatusTextView;
 	
 	private HashMap<Integer, Double[]> mNoteSpectraMap;
 	private int mNumSamplesForThisMap;
 	
 	private Button mStartNewMapButton;
 
 	private EditText mNewNoteMapNameEditText;
 	private Button mSaveNewMapButton;
 	
 	private Button mListAllFilesButton;
 	
 	private MyListFragment mListFragment;
 	private SummaryFragment mSummaryFragment;
 	
 	private AudioCollector mAudioCollector;
 	private double[][] mSamples;
 	
 	//Dropbox
 	private boolean mIsDBLoggedIn;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_datacollect);
 		// Show the Up button in the action bar.
 		setupActionBar();
 		
 		
 		mNoteSpectraMap = new HashMap<Integer, Double[]>();
 		mNumSamplesForThisMap = -1;
 		
 		/*
 		 * [older] Button to record n samples as given by below EditText
 		 */
 		mNumRecordingsEditText = (EditText) findViewById(R.id.num_recordings_edittext);
 		mStartNRecordingsButton = (Button) findViewById(R.id.submit_n_recordings_button);
 		mStartNRecordingsButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				int num = Integer.parseInt(mNumRecordingsEditText.getText().toString());
 				disableInputs();
 				Log.i(TAG,"\tGoing to record: " + num + "times");
 				getNSamples(num);
 				mAudioCollector.writeSamplesToDropbox(mSamples, num+"recordings");
 				status("Recorded " + mSamples.length + " recordings and uploaded to Dropbox if logged in");
 				enableInputs();
 			}
 		});
 		
 		/*
 		 * [older] Button to record for n seconds as given in below EditText
 		 */
 		mTimedRecordingEditText = (EditText) findViewById(R.id.recording_time_edittext);
 		mStartTimedRecordingButton = (Button) findViewById(R.id.submit_time_recording_button);
 		mStartTimedRecordingButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				int msecs = Integer.parseInt(mTimedRecordingEditText.getText().toString());
 				disableInputs();
 				Log.i(TAG,"\tGoing to record for: " + mTimedRecordingEditText.getText().toString() + "milliseconds");
 				getSamplesFor(msecs*1000);
 				status("Recorded " + mSamples.length + " recordings in " +  mTimedRecordingEditText.getText().toString() +
 						" ms and uploaded to Dropbox if logged in");
 				mAudioCollector.writeSamplesToDropbox(mSamples, msecs+"msRecording");
 				enableInputs();
 				
 			}
 		});
 		
 		mRecordingSummaryButton = (Button) findViewById(R.id.recording_summary_button);
 		mRecordingSummaryButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				if (!mSummaryFragment.isVisible()) {
 					Log.i(TAG,"Starting summary fragment");
 					mSummaryFragment = new SummaryFragment();
 					FragmentManager fragmentManager = getFragmentManager();
 					Bundle bundle = new Bundle();
 					double[] joined = Helper.joinArrays(mSamples);
 					double[] averaged = Helper.averageArrays(mSamples);
 					bundle.putDoubleArray("fftVals", Helper.fft(averaged));
 					bundle.putDoubleArray("audioVals", joined);
 					bundle.putDouble("amplitude", Helper.sumArrayInAbs(averaged)/averaged.length);
 					bundle.putDouble("timeLength", ((double)joined.length)/Helper.SAMPLING_SPEED);
 					mSummaryFragment.setArguments(bundle);
 					FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
 					fragmentTransaction.add(R.id.empty_fragment, mSummaryFragment);
 					fragmentTransaction.addToBackStack(null);
 					fragmentTransaction.commit();
 				}
 				else {
 					double[] joined = Helper.joinArrays(mSamples);
 					double[] averaged = Helper.averageArrays(mSamples);
 					mSummaryFragment.setAmplitude(Helper.sumArrayInAbs(averaged)/averaged.length);
 					mSummaryFragment.setSampleLength(((double)joined.length)/Helper.SAMPLING_SPEED);
 					mSummaryFragment.setFftVals(Helper.fft(averaged));
 					mSummaryFragment.setAudioVals(joined);
 					mSummaryFragment.notifyDataChanged();
 				}
 			}
 		});
 		
 		
 		/*
 		 * Button to initialize a fresh HashMap
 		 */
 		mStartNewMapButton = (Button) findViewById(R.id.start_new_map_button);
 		mStartNewMapButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Log.i(TAG, "Referencing HashMap to new empty HashMap");
 				status("Initialized new map");
 				mNoteSpectraMap = new HashMap<Integer, Double[]>();
 				mNumSamplesForThisMap = -1;
 			}
 		});
 		
 		
 		/*
 		 * Button to save the current HashMap with the given name
 		 */
 		mNewNoteMapNameEditText = (EditText) findViewById(R.id.new_note_map_name_edittext);
 		mSaveNewMapButton = (Button) findViewById(R.id.save_new_map_button);
 		mSaveNewMapButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				
 				if (mNoteSpectraMap.keySet().size() == 0) {
 					Log.i(TAG,"Note map is empty, aborting");
 					status("Note map is empty! Aborting.");
 					return;
 				}
 				
 				String fileName;
 				fileName = mNewNoteMapNameEditText.getText().toString();
 				if (fileName.length() == 0) {
 					Log.e(TAG,"no name entered!");
 					status("Enter a name!");
 					return;
 				}
 				
 				try{
 					Helper.writeNewNoteSpectraFile(getApplicationContext(), fileName, mNoteSpectraMap);
 					status("Wrote data to new file " + fileName);
 				} catch (Exception e) {
 					status(e.getMessage());
 				}
 			}
 		});
 		
 		
 		mStatusTextView = (TextView) findViewById(R.id.status_textview);
 
 		/*
 		 * Button to record data for the note given in below EditText
 		 */
 		mNoteNumEditText = (EditText) findViewById(R.id.note_num_edittext);
 		mGetNoteDataButton = (Button) findViewById(R.id.get_note_data_button);
 		mGetNoteDataButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				new Thread() {
 					public void run() {
 						
 						int n;
 						try {
 							n = Integer.parseInt(mNoteNumEditText.getText().toString());
 						} catch (NumberFormatException e) {
 							Log.e(TAG,"invalid note number!");
 							runOnUiThread(new Runnable() {
 								@Override
 								public void run() {
 									status("Not a valid number");
 								}
 							});
 							return; //exit out!
 						}
 						
 						final int noteNum = n;
 						
 						if (noteNum < 1 || noteNum > 88) {	//use human counting, 1-88 allowed inclusive
 							Log.e(TAG,"noteNum is out of range");
 							runOnUiThread(new Runnable() {
 								@Override
 								public void run() {
 									status("Note must be in range 1-88 inclusive");
 								}
 							});
 							return; //exit out!
 						}
 						
 						
 						runOnUiThread(new Runnable() {
 							@Override
 							public void run() {
 								disableInputs();
 							}
 						});
 						
 						
 						Log.i(TAG, "Recording...");
 						
 						if (mNumSamplesForThisMap == -1) { //if this is the first recording for this map
 							getSamplesFor(3000000); //3 sec
 							mNumSamplesForThisMap = mSamples.length;
 							Log.i(TAG, "First time for new map, got " + mNumSamplesForThisMap + " samples");
 						}
 						else {
 							Log.i(TAG, "Getting another " + mNumSamplesForThisMap + " samples");
 							getNSamples(mNumSamplesForThisMap);
 						}
 						
 						Log.i(TAG,"Averaging samples &  doing FFT");
 						Double[] fft = mAudioCollector.fftIntoDoubleObjects(Helper.averageArrays(mSamples));
 						
 						Log.i(TAG,"Adding to hashmap");
 						if (mNoteSpectraMap.containsKey(Integer.valueOf(noteNum))) {
 							Log.i(TAG, "Overwriting previous hashmap entry for note " + noteNum);
 							mNoteSpectraMap.remove(Integer.valueOf(noteNum)); //remove to overwrite if exists
 						}
 						
 						mNoteSpectraMap.put(Integer.valueOf(noteNum), fft);	
 												
 						runOnUiThread(new Runnable() {
 							@Override
 							public void run() {
 								status("Finished recording and saving " + mNumSamplesForThisMap + " averaged samples in 3 seconds for note #" + noteNum);
 								enableInputs();
 							}
 						});		
 						
 					}
 				}.start();
 				
 			}
 		});
 		
 		
 		/*
 		 * Button to start list fragment for all files in the private storage
 		 */
 		mListAllFilesButton = (Button) findViewById(R.id.list_all_private_files_button);
 		mListAllFilesButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 
 				if (!mListFragment.isVisible()) {
 					Log.i(TAG, "starting list fragment...");
 					FragmentManager fragmentManager = getFragmentManager();
 					FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
 					fragmentTransaction.add(R.id.empty_fragment, mListFragment);
 					fragmentTransaction.addToBackStack(null);
 					fragmentTransaction.commit();
 					
 				}
 				
 			}
 		});
 		
 
 		//initialize new AudioCollector
 		mAudioCollector = new AudioCollector(AudioSource.MIC, Helper.SAMPLING_SPEED, 
 				true, true, Helper.EXTERNAL_BUFFER_SIZE, this);
 		
 		
 		//initialize List Fragment for possible later use
 		mListFragment = new MyListFragment();
 		mSummaryFragment = new SummaryFragment();
 
 		
 		mIsDBLoggedIn = getIntent().getBooleanExtra("DBLoggedIn", false);
 
 		mAudioCollector.setDBLoggedIn(mIsDBLoggedIn);
 		if(mAudioCollector.isDBLoggedIn()){ 
 			mAudioCollector.setDBDataUpload(true);
 		}
 	}
 
 	
 	/*
 	 * these two functions are here to channel all recordings in this class through two places
 	 * Allows us to update various things like summary fragment
 	 */
 	private void getSamplesFor(int microSec) {
 		mSamples = mAudioCollector.getSamplesFor(microSec);
		runOnUiThread(new Runnable() {
			public void run() {
				if (mSummaryFragment.isVisible()) {
					mRecordingSummaryButton.performClick(); //to update the summary fragment
				}
			}
		});
 	}
 	
 	private void getNSamples(int n) {
 		mSamples = mAudioCollector.getNSamples(n);
		runOnUiThread(new Runnable() {
			public void run() {
				if (mSummaryFragment.isVisible()) {
					mRecordingSummaryButton.performClick(); //to update the summary fragment
				}
			}
		});
 	}
 	
 	
 	private void disableInputs() {
 		mNumRecordingsEditText.setEnabled(false);
 		mStartNRecordingsButton.setEnabled(false);
 		
 		mTimedRecordingEditText.setEnabled(false);
 		mStartTimedRecordingButton.setEnabled(false);
 		
 		mNoteNumEditText.setEnabled(false);
 		mGetNoteDataButton.setEnabled(false);
 		
 		mStartNewMapButton.setEnabled(false);
 		mNewNoteMapNameEditText.setEnabled(false);
 		mSaveNewMapButton.setEnabled(false);
 				
 		
 	}
 	
 	private void enableInputs() {
 		mNumRecordingsEditText.setEnabled(true);
 		mStartNRecordingsButton.setEnabled(true);
 		
 		mTimedRecordingEditText.setEnabled(true);
 		mStartTimedRecordingButton.setEnabled(true);
 		
 		mNoteNumEditText.setEnabled(true);
 		mGetNoteDataButton.setEnabled(true);
 		
 		mStartNewMapButton.setEnabled(true);
 		mNewNoteMapNameEditText.setEnabled(true);
 		mSaveNewMapButton.setEnabled(true);
 		
 	}
 	
 	
 	private void status(String msg) {
 		mStatusTextView.setText(msg);
 	}
 	
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.datacollect, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 }
