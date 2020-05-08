 package com.i2r.androidremotecontroller.main;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.hardware.Camera;
 import android.os.Bundle;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Log;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.i2r.androidremotecontroller.R;
 import com.i2r.androidremotecontroller.exceptions.ServiceNotFoundException;
 import com.i2r.androidremotecontroller.sensors.SensorDurationHandler;
 
 /**
  * This class acts as a base starter for remote control of an android device.
  * This class starts the {@link RemoteControlMaster} for this application,
  * giving it the connection type chosen by the user in the
  * {@link ConnectionTypeSelectionActivity} to start receiving commands with.
  * This activity is started with an Intent that is sent from
  * {@link ConnectionTypeSelectionActivity}, and that intent will hold the string
  * connection type to start the master with.
  * 
  * @see {@link ConnectionTypeSelectionActivity#EXTRA_BLUETOOTH}
  * @see {@link ConnectionTypeSelectionActivity#EXTRA_WIFI}
  * @see {@link ConnectionTypeSelectionActivity#EXTRA_USB}
  */
 public class RemoteControlActivity extends Activity {
 
 	private static final String TAG = "RemoteControlActivity";
 	
 	private static final int PING_FREQUENCY = 30000;
 	
 	/**
 	 * Used for filtering {@link Intent}s in this application. This constant is
 	 * called whenever a command filter has finished a short term task.
 	 */
 	public static final String ACTION_TASK_COMPLETE = "i2r_action_sensor_completed_task";
 
 	/**
 	 * Used for filtering {@link Intent}s in this application. This constant is
 	 * called whenever a connection manager has established or lost a connection
 	 * to the controller.
 	 */
 	public static final String ACTION_CONNECTOR_RESPONDED = "i2r_action_connection_created";
 
 	/**
 	 * Used for filtering {@link Intent}s in this application. This constant is
 	 * called a connection manager has received byte data on an open connection
 	 * with the controller.
 	 */
 	public static final String ACTION_CONNECTION_READ = "i2r_action_connection_read";
 
 	public static final String EXTRA_TASK_ID = "i2r_extra_task_id";
 	public static final String EXTRA_COMMAND = "i2r_extra_command";
 	public static final String EXTRA_RESULT_DATA = "i2r_extra_result_data";
 	public static final String EXTRA_DATA_TYPE = "i2r_extra_data_type";
 	public static final String EXTRA_INFO_MESSAGE = "i2r_extra_info_message";
 
 	private BroadcastReceiver receiver;
 	private LocalBroadcastManager manager;
 	private RemoteControlMaster master;
 	private CommandFilter cFilter;
 	private TextView action;
 	private Camera camera;
 	private Pinger pinger;
 	private boolean started;
 
 	/**
 	 * Create a new {@link BluetoothConnectionManager} to handle bluetooth
 	 * connection operations, get access to a TextView to update the UI of the
 	 * application's progress, and create an ArrayList of {@link CommandPacket}s
 	 * which are parsed from byte arrays received in the
 	 * BluetoothConnectionManager. The CommandPAcket ArrayList will be used as a
 	 * command Queue to execute command packets in the order that they come.
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_remote_control);
 
	
 		// booleans to query about the state of this activity during the
 		// first steps of creation
 		this.started = false;
 		this.action = (TextView) findViewById(R.id.current_action_text_view);
 		this.manager = LocalBroadcastManager.getInstance(this);
 
 		// create a receiver for the BluetoothConnectionManager to send to
 		this.receiver = new BroadcastReceiver() {
 			public void onReceive(Context context, final Intent intent) {
 
 				// if the source of the intent was a remote connection reading
 				// more commands
 				if (intent.getAction().equals(ACTION_CONNECTION_READ)) {
 
 					Log.d(TAG, "update broadcast from connection recieved");
 					action.setText("pasring command");
 					pinger.reset();
 					master.updateByRemoteControl(intent.getStringExtra(EXTRA_COMMAND));
 
 					// if the source of the intent was a new connection
 				} else if (intent.getAction()
 						.equals(ACTION_CONNECTOR_RESPONDED)) {
 
 					Log.d(TAG, "initialization broadcast recieved");
 					String message = intent.getStringExtra(EXTRA_INFO_MESSAGE);
 					action.setText(message);
 					master.initializeConnection();
 
 					// if a sensor is done with a task, ask the master to
 					// start the next one
 				} else if (intent.getAction().equals(ACTION_TASK_COMPLETE)) {
 					String result = intent.getStringExtra(EXTRA_INFO_MESSAGE);
 					Log.d(TAG, result);
 					action.setText(result);
 					master.updateSensors();
 				}
 			}
 		};
 
 		// button to stop remote control
 		Button stop = (Button) findViewById(R.id.stop_remote_control);
 		stop.setOnClickListener(new OnClickListener() {
 			public void onClick(View view) {
 				Intent result = new Intent(
 					ConnectionTypeSelectionActivity
 					.ACTION_USER_TERMINATED_CONNECTION);
 				setResult(RESULT_OK, result);
 				stopMaster();
 			}
 		});
 
 	}
 	
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
 		String connectionType = getIntent()
 					.getStringExtra(
 					ConnectionTypeSelectionActivity
 					.EXTRA_CONNECTION_TYPE);
 			startMaster(connectionType);
 		
 	}
 	
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		stopMaster();
 	}
 	
 	
 
 	
 	/**
 	 * Begins main execution of this remote control application. This creates a
 	 * new RemoteControlMaster to control the phone remotely by taking in
 	 * commands across a connection, and it creates a new CommandFilter for
 	 * the Master's use.
 	 */
 	private void startMaster(String connectionType) {
 
		// add an IntentFilter to receive updates from bluetooth manager
 		IntentFilter filter = new IntentFilter();
 		filter.addAction(ACTION_TASK_COMPLETE);
 		filter.addAction(ACTION_CONNECTOR_RESPONDED);
 		filter.addAction(ACTION_CONNECTION_READ);
 		manager.registerReceiver(receiver, filter);
 
 		
 		// creates a new master to control remote command flow, and a new
		// responder
		// to mediate between the master and the device sensors
 		this.camera = Camera.open();
 		SurfaceView view = (SurfaceView) findViewById(R.id.preview);
 		this.cFilter = new CommandFilter(this, camera, view.getHolder());
 		
 		try {
			this.master = new RemoteControlMaster(cFilter, connectionType);
 			if (!started) {
 				started = true;
 				action.setText("listening for connection to remote device...");
 				Log.d(TAG, "remote control started");
				this.pinger = new Pinger(PING_FREQUENCY);
 				pinger.start();
 				master.start();
 			}
 
 			// goes back to main activity if connectionType parameter was not
			// valid or
			// no connection of the given type was found
 		} catch (ServiceNotFoundException e) {
 			
 			Toast.makeText(
 					this,
 					"connection service for remote control not found, shutting down app",
 					Toast.LENGTH_LONG).show();
 			
			if(master != null){
				master.stop();
				master = null;
			}
			
			
			finish();
 		}
 	}
 
 	
 	/**
 	 * Stops this application's progress, and kills this activity, reverting
 	 * back to the {@link ConnectionTypeSelectionActivity} that started this
 	 * activity. This is called whenever the UI "stop" button is pushed, or when
 	 * {@link #startMaster(String)} is called with an invalid or unavailable
 	 * connection type.
 	 */
 	private void stopMaster() {
 		// set the 'stop' button to stop remote control
 		started = false;
 		Log.d(TAG, "remote control stopped");
 
 		// stop remote control and free resources
 		if (manager != null) {
 			manager.unregisterReceiver(receiver);
 		}
 
 		if (master != null) {
 			master.stop();
 			master = null;
			System.gc();
 		}
 
 		action.setText("remote control stopped, finishing...");
 		finish();
 	}
 	
 
 	
 	
 	/**
 	 * Used to periodically test the connection for
 	 * validity, since there are some cases in
 	 * which the connection does not close properly.
 	 */
 	private class Pinger extends Thread {
 		
 		private SensorDurationHandler handler;
 		
 		public Pinger(int frequency){
 			this.handler = new SensorDurationHandler(frequency);
 			handler.start();
 		}
 		
 		
 		@Override
 		public void run(){
 			while(started){
 				if(handler.maxReached()){
 					boolean connected = ResponsePacket.sendPing(
 							master.getConnectionManager()
 							.getConnection());
 					
 					if(!connected){
 						master.initializeConnection();
 					}
 					
 					handler.start();
 					
 				} else {
 					try{
 						Thread.sleep(1000);
 					} catch (InterruptedException e){}
 				}
 			}
 		}
 		
 		
 		public void reset(){
 			handler.start();
 		}
 		
 	} // end of Pinger class
 	
 
 }// end of SelectorActivity class
