 package di.kdd.buildmon;
 
 import di.kdd.buildmon.protocol.DistributedSystem;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.View;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	/* States */
 	
 	private boolean connected;
 	private boolean samplingRunning;
 	
 	private AccelerationsSQLiteHelper accelerationsDb;
 	private DistributedSystem distributedSystem;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		accelerationsDb = new AccelerationsSQLiteHelper(this.getApplicationContext());
 	}
 
 	@Override
 	protected void onDestroy() {
 		accelerationsDb.dumpAccelerationBuffers();
 	}
 	
 	public void showMessage(String message) {		
 		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
 	}
 	
 	/**
 	 * Handler of the startSampling button. 
 	 * Registers the listener of the Accelerometer
 	 * @param view
 	 */
 	
 	public void startSampling(View _) {		
 		if(!samplingRunning) {
 			startService(new Intent(this, AccelerometerListenerService.class));
 			samplingRunning = true;
 		}
 		else {
 			Toast.makeText(getApplicationContext(), "Sampling Service is already running!", Toast.LENGTH_LONG).show();
 		}
 	}
 
 	/**
 	 * Handler of the stopSampling button. 
 	 * Unregisters the listener of the Accelerometer
 	 * @param ignored
 	 */	
 	
 	public void stopSampling(View _)	{
 		if(samplingRunning) {
 			stopService(new Intent(this, AccelerometerListenerService.class));
 			samplingRunning = false;
 		}
 		else {
 			Toast.makeText(this, "Sampling Service is not running!", Toast.LENGTH_LONG).show();
 		}
 	}	
 	
 	/***
 	 * Handler for the connect button. 
 	 * @param ignored
 	 */
 	
 	public void connect(View _) {
 		if(!connected) {
				distributedSystem = new DistributedSystem(this);
 				distributedSystem.connect();				
 				connected = true;
 		}
 		else {			
 			Toast.makeText(this, "Already connected!", Toast.LENGTH_LONG).show();
 		}
 	}
 	
 	/***
 	 * Handler for the disconnect button. 
 	 * @param ignored
 	 */
 	
 	public void disconnect(View _) {
 		if(connected) {
			distributedSystem.disconnect();
 			connected = false;
 		}
 		else {			
 			Toast.makeText(this, "Not connected!", Toast.LENGTH_LONG).show();
 		}
 	}
 		
 	/***
 	 * Dumps the stored Accelerations into 3 files, one for each Axis
 	 * @param ignored
 	 * @throws Exception
 	 */
 	
	public void exportToFile(View _) throws Exception {
 		if(!samplingRunning) {
 			accelerationsDb.dumpToFile();
 		}
 		else {
 			Toast.makeText(this, "Stop Sampling!", Toast.LENGTH_LONG).show();
 		}
 	}	
 }
