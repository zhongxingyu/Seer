 package com.example.samplesqlsync;
 
 
 import java.util.concurrent.Semaphore;
 
 
 import com.example.samplesqlsync.tasks.BenchmarkTask;
 import com.example.samplesqlsync.tasks.TestTask;
 import com.nexusray.sqlsync.settings.SyncSettings;
 import com.nexusray.sqlsync.sync.SyncExecutor;
 import android.os.Bundle;
 import android.os.Handler;
 import android.app.Activity;
 import android.content.ContentResolver;
 import android.util.Log;
 import android.view.View;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 /**
  * Test functionality of master-master synchronization library
  * and measures its speed after execution of certain operations
  * on custom database
  *
  */
 public class SampleActivity extends Activity {
 	
 	public SampleSyncObserver viewObs = null;
 	private String logTag = "sampleApp"; 
 	public long timeStart = 0;
 	
 	// URL must be same as library uses otherwise it won't work correctly
 	public final static String WEBSERVICE_URL = "http://10.0.0.32:9090";
 	//public final static String WEBSERVICE_URL = "http://nexusray.com/sqlsync/webservice";
 	public final static String SCRIPT_BENCH_URL = WEBSERVICE_URL + "/sample/SampleBench.php";
 	TextView resultField = null; 
 	ContentResolver cr = null;
 	
 	
 	
 	
 	public static Semaphore benchPhase = new Semaphore(0);
 	public static Semaphore testPhase = new Semaphore(0); 
 	
 	private int operationInProgress = 0;
 	
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.i(logTag, "Starting activity ..");
         setContentView(R.layout.sample_activity);
         
         // Creates one account, it will be overridden each run
         SyncExecutor.createAccount("samplename", "samplepass", getApplicationContext());
         SyncExecutor.enableSync(getApplicationContext());
         resultField = (TextView) findViewById(R.id.result); 
         cr = getContentResolver();
         
         viewObs = new SampleSyncObserver(new Handler());
               
         
        
         /*
         ContentValues values = new ContentValues();
         values.put("name", "lampa");
         values.put("count", 15);
         values.put("price", 11122);
        
         //cr.delete(Uri.parse("content://com.example.samplesqlsync/products"), "_syncID=310301", null);
         //cr.insert(Uri.parse("content://com.example.samplesqlsync/products"), values);
         //cr.update(Uri.parse("content://com.example.samplesqlsync/products"), values, "_syncID=80800", null);
         */
         
     }
     
     /**
      * Unregisters ContentObserver on pause
      */
     @Override
     public void onPause() {
     	super.onPause();    
     	
     	if(viewObs != null) {
     		cr.unregisterContentObserver(viewObs);
     	}    	
     }
     
     /**
      * Registers ContentObserver on unpause
      */
     @Override
     public void onResume() {
     	super.onResume();    
     	
     	if (viewObs != null) {
     		cr.registerContentObserver(SyncSettings.AUTHORITY_URI, true, viewObs);
     	}    	
     }
 
     /**
      * Displays another line of text
      */
     public void displayText(String text) {
     	resultField.append(text);
     	ScrollView scroll = (ScrollView) findViewById(R.id.scroll); 
     	scroll.fullScroll(View.FOCUS_DOWN);
     }
     
     /**
      * Clears all text from the screen
      */
     private void clearText() {
     	resultField.setText("");
     }
 
 
 	/**
 	 * Enables both buttons
 	 */
 	public void resetOperationInProgress() {
 		this.operationInProgress = 0;
 	}
 	
 	
 
 	/**
      * Method for benchmark button
      * @param view ignored
      */
     public void benchmarkSync(View view) {
     	
     	if (operationInProgress != 0) return;
     	operationInProgress = 1;
     	
     	clearText();
     	displayText("Starting benchmark...\n");
     	displayText("Please don't interact with your device till it's done, it might affect results\n\n");
     	
     	benchPhase.drainPermits();
     	new BenchmarkTask(this, cr).execute();
 
     }    
     
     
     
     
     
     /**
      * Method for test button
      * @param view ignored
      */
     public void testConflicts(View view) {
     	if (operationInProgress != 0) return;
     	operationInProgress = 2;
     	
     	clearText();
     	displayText("Testing synchronization\nThis operation might take a minute...\n");
     	
    	testPhase.drainPermits();
     	new TestTask(this, cr).execute();
     }
 
     
 
 }
 
