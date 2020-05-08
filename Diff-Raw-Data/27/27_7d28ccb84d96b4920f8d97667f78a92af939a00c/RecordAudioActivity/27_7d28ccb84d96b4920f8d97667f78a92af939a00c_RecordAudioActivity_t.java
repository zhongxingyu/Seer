 package com.CMPUT301F12T07.crowdsource;
 
 import java.io.File;
 import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
 
 import com.CMPUT301F12T07.crowdsource.R;
 
 import android.media.MediaRecorder;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.app.Activity;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class RecordAudioActivity extends Activity {
 
 	private TextView min;
 	private TextView sec;
 	private Button start;
 	private Button stop;
 	private Button cancel;
 	
 	private int cMin;
 	private int cSec;
 	
 	final static int delay = 1000;
 	
     private String folder;
     private File audioFile;
 
     private MediaRecorder mRecorder = null;
     private boolean started = false;
     
     /**
      * Handler for timer polling.
      */
     final Handler handler = new Handler();
     
     /**
      * Updater for when the handler is called. Handler set to be called
      * every 1 second. This also updates the timer on the display.
      */
     final Runnable runnable = new Runnable() {
     	public void run() {
     		if (cSec == 60) {
     			cMin++;
     			cSec = 0;
     			
     			if (cMin < 10) min.setText("0"+cMin);
     			else min.setText(""+cMin);
     		}
     		cSec++;
     		
     		handler.postDelayed(this, 1000);
 
     		if (cSec < 10) sec.setText("0"+cSec);
     		else sec.setText(""+cSec);
     	}
     };
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_record_audio);
         
         setUpTimer();
         setUpStart();
         setUpStop();
         setUpCancel();
         
     }
     
     /**
      * Initializes buttons, and timer display. 
      */
     private void setUpStart() {
     	start = (Button) findViewById(R.id.start);
     	start.setOnClickListener(new OnClickListener() {
     		public void onClick(View v) {		
     			if (started == true) return;
     			runnable.run();
     			started = true;
     			
     			try {
     				setUpRecorder();
     				setUpPath();
     				recordAudio();
     			} catch (IOException e) {
     				Log.e("RecordAudioActivity", "start recording error");
     			}
 
     		}
     	});
     }
     
     /**
      * Initializes the cancel button. If the button is called when
      * the timer has not yet been called, it will just return RESULT_CANCELED.
      * However, if the timer has been called previously, then it will stop the 
      * timer first return RESULT_CANCELED.
      */
     private void setUpCancel() {
     	cancel = (Button) findViewById(R.id.cancel);
     	cancel.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				if (started == true)  {
 					mRecorder.stop();
 			    	mRecorder.release();
 			    	
 			    	handler.removeCallbacks(runnable);
 				}
 				
 	    		Toast.makeText(RecordAudioActivity.this, "Recording canceled", Toast.LENGTH_LONG).show();
 
 	        	Intent result = new Intent();
 	        	setResult(RESULT_CANCELED,result);
 	        	finish();
 			}
     	});
     }
     
     /**
      * Initializes the stop button. This stops the handler from calling
      * again 1 second later.
      */
     private void setUpStop() {
     	stop = (Button) findViewById(R.id.stop);
     	stop.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				handler.removeCallbacks(runnable);
 				stopRecording();
 			}
     	});
     }
     
     /**
      * Initializes the timer display.
      */
     private void setUpTimer() {
     	min = (TextView) findViewById(R.id.min);
     	sec = (TextView) findViewById(R.id.sec);
     	
     	cMin = 0;
     	cSec = 0;
     }
     
     /**
      * Initializes the MediaRecorder with configurations.
      */
     private void setUpRecorder() {
         mRecorder = new MediaRecorder();
         mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
         mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
         mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
     }
     
     /**
      * Initializes the save path for the audio file.
      * @throws IOException
      */
     private void setUpPath() throws IOException {
         folder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/crowdsource/audio";
         
         File folderF = new File(folder);
         if (!folderF.exists()) folderF.mkdir();
         //TODO: change name
		audioFile = File.createTempFile(getDateTime(), ".3gp", folderF);
 
     }
     
     /**
     * This returns the current date and time for photo name.
     * @return Current date and time.
     */
    private String getDateTime() {
    	final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("MST"), Locale.CANADA);
    	
    	return "" + cal.get(Calendar.YEAR) + (cal.get(Calendar.MONTH) + 1) + 
    			cal.get(Calendar.DAY_OF_MONTH) + "-" + cal.getTimeInMillis();
    }
    
    /**
      * recordAudio will get called when the recording starts
      * @throws IOException
      */
     private void recordAudio() throws IOException {
         mRecorder.setOutputFile(audioFile.getAbsolutePath());
         
         mRecorder.prepare();
         mRecorder.start();
     }
     
     /**
      * stopRecording will get called, when recording is canceled or finished. If the recording
      * is cancelled, it will return RESULT_CANCELED, otherwise RESULT_OK to the caller.
      */
     private void stopRecording() {
     	if (started == false) {
     		Toast.makeText(RecordAudioActivity.this, "Recording canceled", Toast.LENGTH_LONG).show();
 
         	Intent result = new Intent();
         	setResult(RESULT_CANCELED,result);
         	finish();
     		
     		return;
     	}
     	
     	mRecorder.stop();
     	mRecorder.release();
 
     	String data = Uri.fromFile(audioFile).toString();
 
     	Intent result = new Intent();
     	result.putExtra("Audio", data);
     	result.putExtra("result", "pass");
     	setResult(RESULT_OK,result);
     	finish();
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_record_audio, menu);
         return true;
     }
 }
