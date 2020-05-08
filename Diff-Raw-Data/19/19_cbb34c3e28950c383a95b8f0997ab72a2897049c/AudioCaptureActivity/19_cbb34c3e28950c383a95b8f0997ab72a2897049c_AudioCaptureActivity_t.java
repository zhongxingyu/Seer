 package ca.ualberta.cs.completemytask.activities;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.media.MediaRecorder;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 import ca.ualberta.cs.completemytask.R;
 
 public class AudioCaptureActivity extends CustomActivity {
 	protected final String TAG = "AudioCaptureActivity";
 	
 	MediaRecorder recorder = new MediaRecorder();
 	byte[] returnAudio;
 	String captureUserName;
 	String captureAudioName;
 	File tempAudioFile;
 	private EditText audioNameTextView;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_audio_capture);
         
     }
 
     public void onStart(){
     	super.onStart();
     	
     	audioNameTextView = (EditText) findViewById(R.id.audioNameView);
     	
     	boolean exists = (new File(android.os.Environment.getExternalStorageDirectory() + "/Record/")).exists();
     	if (!exists) {
     	    new File(android.os.Environment.getExternalStorageDirectory() + "/Record/").mkdirs();
     	}
     	
         recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
         recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
         recorder.setOutputFile(android.os.Environment.getExternalStorageDirectory()+"/Record/TaskAudio.3gp");
         recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
         
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_audio_capture, menu);
         return true;
     }
     
     //Set some sort of cap on the size/duration?
     public void recordStart(View view) throws IOException {
     	try {
     		recorder.prepare();
     		//System.out.println("Recorder prepared");
     		recorder.start(); 
 			//System.out.println("Recorder started");
 
     	} catch (IllegalStateException e) {
 			// TODO Auto-generated catch block
 			System.out.println("Failed to start properly");
 			e.printStackTrace();
 		}
     	
     }
     
     /**
      * turn this into a 
      * @param view
      */
     public void recordStop(View view) {
     	try {
 			//System.out.println("About to stop...");
 			recorder.stop();
 			//System.out.println("It worked After stop");
			recorder.reset(); 
			
 			recorder.release();
 			//System.out.println("Recorder released");
 			
 			//Set our audio file
 			// Its already in a Try/Catch!
 			tempAudioFile = new File(android.os.Environment.getExternalStorageDirectory()+"/Record/TaskAudio.3gp");
 			
 		} catch (IllegalStateException e) {
 			// TODO Auto-generated catch block
 			System.out.println("Recorder failed to stop");
 			e.printStackTrace();
 		}
     }
     
     //////////////////////////
 	public byte[] getByteFromFile(File audioFile) {
 		/*
 		 * This functions converts audio file to a string which can be
 		 * JSONified.
 		 */
 		byte []buffer = new byte[(int) audioFile.length()];
 		InputStream ios = null;
 
 		try {
 			ios = new FileInputStream(audioFile);
 			if (ios.read(buffer) == -1) {
 				throw new IOException("EOF reached while trying to read the whole file");
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			Log.w(TAG, e.getStackTrace().toString());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			Log.w(TAG, e.getStackTrace().toString());
 		} finally {
 			try {
 				if (ios != null)
 					ios.close();
 			} catch (IOException e) {
 				Log.w(TAG, e.getStackTrace().toString());
 			}
 		}
 
 		//String encodedString = Base64.encodeToString(buffer, Base64.URL_SAFE);
 
 		//Log.v(TAG, encodedString);
 
 		//return encodedString;
 		return buffer;
 	}
 	//////////////////////
     
     public void playAudio(View view){
     	//If the file exists
     	//	Play audio file with media player
     	//else
     	//	"Record an audio clip first
     }
     
     public void recordReset(View view){
     	if (tempAudioFile != null){
     	//	delete the file
     	//  Reset
     		recorder.reset(); 
     	}
     	else {
 //    		put the MediaRecorder back into prepare(check android dev)
     		recorder.reset(); 
     	}
     		
     	
     
     }
     
     public void submitAudio(View view){
     	
     	//Convert the File to byte[]
     	returnAudio = getByteFromFile(tempAudioFile);
     	captureAudioName = audioNameTextView.getText().toString();
     	
 		//CALL THIS before exiting the AudioCaptureActivity
 		Intent resultData = new Intent();
 		//resultData.putExtra("name", captureUserName);
 		resultData.putExtra("title", captureAudioName);
 		resultData.putExtra("audioData", returnAudio);
 		setResult(Activity.RESULT_OK, resultData);
 		finish();
     }
     
 	/**
 	 * Closes the task
 	 * @param view
 	 */
 	public void close(View view) {
 		//delete the outstanding file?
 		//File file = new File(selectedFilePath);
 //		boolean deleted = file.delete();
 //		file.getAbsolutePath(returnAudio);
 //		boolean deleted = file.delete();
 		
 		//Exit mediarecorder if it is running.
 		
 		Intent resultData = new Intent();
 		setResult(Activity.RESULT_CANCELED, resultData);
		//this.finish();
		finish();
 	}
     
 }
 
