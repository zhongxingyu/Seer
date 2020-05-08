 package net.splashe.kineticsensors;
 
 import net.splashe.kineticsensors.SensorRecordingService.LocalBinder;
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 import android.view.Menu;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 public class MainActivity extends Activity {
 
 	public static final String TAG = "KINETIC";
 	public static final long UI_UPDATE_DELAY = (long)(0.25 * 1000); // in seconds
 	
 	RelativeLayout background;
 	TextView recordStatus;
 	TextView xAccelTextView, yAccelTextView, zAccelTextView;
 	TextView xGyroTextView, yGyroTextView, zGyroTextView;
 	ToggleButton recordToggle;
 	
 	ServiceConnection mConnection;
 	SensorRecordingService sensorRecordingService;
 	boolean isBoundToService;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		// Init UI Components
 		background = (RelativeLayout)findViewById(R.id.background);
 		recordStatus = (TextView)findViewById(R.id.recordStatus);
 		xAccelTextView = (TextView)findViewById(R.id.xAccelTextView);
 		yAccelTextView = (TextView)findViewById(R.id.yAccelTextView);
 		zAccelTextView = (TextView)findViewById(R.id.zAccelTextView);
 		xGyroTextView = (TextView)findViewById(R.id.xGyroTextView);
 		yGyroTextView = (TextView)findViewById(R.id.yGyroTextView);
 		zGyroTextView = (TextView)findViewById(R.id.zGyroTextView);
 		recordToggle = (ToggleButton)findViewById(R.id.recordToggle);
 		
 		// Set record toggle button listener
 		recordToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				// Change recording state according to the record button state
 				setRecording(isChecked);
 			}
 		});  // end listener
 		
 		isBoundToService = false; // Currently not bound to service
 		
 		// SensorRecordingService connection
 		mConnection = new ServiceConnection() {
 			
 			@Override
 			public void onServiceDisconnected(ComponentName name) {
 				isBoundToService = false;
 				sensorRecordingService = null;
 				Log.d(TAG, "Service Disconnected");
 			}
 			
 			@Override
 			public void onServiceConnected(ComponentName name, IBinder service) {
 				// Get local instance of service and all of its methods and attributes
 				isBoundToService = true;
 				LocalBinder mLocalBinder = (LocalBinder)service;
 				sensorRecordingService = mLocalBinder.getSensorRecordingServiceInstance();
 				
 				// Start UI updater thread (runnable inside runnable needed in order to achieve sleeping)
 				new Thread(new Runnable() 
 				{
 					@Override
 					public void run() 
 					{
 						while(isBoundToService)
 						{
 							MainActivity.this.runOnUiThread(new Runnable() 
 							{
 								@Override
 								public void run()
 								{
 									// UI update loop
 									// Update accelerometer values
 									xAccelTextView.setText("x: " + Float.toString(sensorRecordingService.accelData[0]));
 									yAccelTextView.setText("y: " + Float.toString(sensorRecordingService.accelData[1]));
 									zAccelTextView.setText("z: " + Float.toString(sensorRecordingService.accelData[2]));
 									
 									// Update gyro values
 									xGyroTextView.setText("x: " + Float.toString(sensorRecordingService.gyroData[0]));
 									yGyroTextView.setText("y: " + Float.toString(sensorRecordingService.gyroData[1]));
 									zGyroTextView.setText("z: " + Float.toString(sensorRecordingService.gyroData[2]));
 								}
 							}); // end run
 							try 
 							{
 								Thread.sleep(UI_UPDATE_DELAY); // Wait for certain amount of time
 							} catch (InterruptedException e) {
 								e.printStackTrace();
 							}
 						}
 					} // end run
 				}).start(); // end thread ----------------------------------------------------------
 				
 				
 				Log.d(TAG, "Service Connected");
 			}
 		};  // end service connection
 	}
 	
 	private void setRecording(boolean isRecording)
 	{
 		if(isRecording){
 			// Turn light on and tell user we are recording
 			recordStatus.setText("Recording...");
 
 			Log.d(TAG, "Started Recording"); // Debug
 			
 			// Bind to (and start) recording service
 			Intent serviceIntent = new Intent(SensorRecordingService.class.getName());
 			bindService(serviceIntent, mConnection, BIND_AUTO_CREATE); // BIND_AUTO_CREATE creates services as we bind
 			
 		} else {
 			// Turn light off and tell user we're waiting to start recording
 			recordStatus.setText("Record");
 			xAccelTextView.setText("x: -");
 			yAccelTextView.setText("y: -");
 			zAccelTextView.setText("z: -");
 			xGyroTextView.setText("x: -");
 			yGyroTextView.setText("y: -");
 			zGyroTextView.setText("z: -");
 			
 			Log.d(TAG, "Stoped Recording"); // Debug
 			
 			// Unbind from (and usually stop) recording service
 			safelyUnbind();
 		}
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		
 		setRecording(false);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		
 		setRecording(false);
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		
 		setRecording(false);
 	}
 	
 	
 	/* Safely unbind from sensor recording service */
 	private void safelyUnbind(){
 		if (isBoundToService) {
 			unbindService(mConnection);
 			isBoundToService = false;
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 }
