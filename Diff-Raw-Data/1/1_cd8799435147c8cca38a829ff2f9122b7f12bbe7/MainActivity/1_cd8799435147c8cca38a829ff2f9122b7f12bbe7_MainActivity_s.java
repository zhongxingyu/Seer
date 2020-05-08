 package com.msse.robot_cowboy;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.HashMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.hardware.Camera;
 import android.hardware.usb.UsbDevice;
 import android.hardware.usb.UsbManager;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.opengl.GLSurfaceView;
 import android.os.Bundle;
 import android.os.Looper;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.gson.Gson;
 import com.hoho.android.usbserial.driver.UsbSerialDriver;
 import com.hoho.android.usbserial.driver.UsbSerialProber;
 import com.hoho.android.usbserial.util.HexDump;
 import com.hoho.android.usbserial.util.SerialInputOutputManager;
 
 import edu.umn.robotcontrol.domain.RobotCommand;
 import edu.umn.robotcontrol.domain.RobotPosition;
 
 public class MainActivity extends Activity {
 
     private final String TAG = MainActivity.class.getSimpleName();
     private LocationManager locationManager;
     private UsbSerialDriver mSerialDevice;
     private UsbManager mUsbManager;
     private TextView mTitleTextView;
     private TextView mDumpTextView;
     private ScrollView mScrollView;
     private final ExecutorService mExecutor = Executors
             .newSingleThreadExecutor();
     private SerialInputOutputManager mSerialIoManager;
     private Timer commandPollingTimer = null;
     private Timer cameraPostingTimer = null;
     
     LocationListener onLocationChange=new LocationListener() {
         public void onLocationChanged(Location location) {
         	LocationPoster poster = new LocationPoster();
         	RobotPosition pos = PositionFacade.locToRobotPos(location);
     	    poster.execute(buildURL() + "/position", new Gson().toJson(pos));
         }
         
         public void onProviderDisabled(String provider) {
           // required for interface, not used
         }
         
         public void onProviderEnabled(String provider) {
           // required for interface, not used
         }
         
         public void onStatusChanged(String provider, int status,
                                       Bundle extras) {
           // required for interface, not used
         }
       };
       
 
     private final SerialInputOutputManager.Listener mListener = new SerialInputOutputManager.Listener() {
         @Override
         public void onRunError(Exception e) {
             Log.d(TAG, "Runner stopped.");
         }
 
         @Override
         public void onNewData(final byte[] data) {
             MainActivity.this.runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     MainActivity.this.updateReceivedData(data);
                 }
             });
         }
     };
 	private SurfaceView surfaceView;
 	private SurfaceHolder surfaceHolder;
 	
 	private String buildURL() {
 		String dataSource = PreferenceManager.getDefaultSharedPreferences(this).getString("pref_data_source", "");
 		return "http://" + dataSource + "/control";
 	}
 
     @Override
     protected void onPause() {
         super.onPause();
         stopIoManager();
         if (mSerialDevice != null) {
             try {
                 mSerialDevice.close();
             } catch (IOException e) {
                 // Ignore.
             }
             mSerialDevice = null;
         }
         stopAsyncTasks();
     }
     
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle item selection
 	    switch (item.getItemId()) {
 	        case R.id.action_settings:
 	        	Intent intent = new Intent(this, SettingsActivity.class);
 	    		startActivity(intent);
 	            return true;
 	        default:
 	            return super.onOptionsItemSelected(item);
 	    }
 	}
 
     protected void onResume() {
         super.onResume();
         HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
         mTitleTextView.setText("Listing " + deviceList.keySet().size() + " devices\n");
         for(String s : deviceList.keySet()){
             mTitleTextView.append("Key: " + s + " " + deviceList.get(s) + "\n");
         }
         
         mSerialDevice = UsbSerialProber.acquire(mUsbManager);
         Log.d(TAG, "Resumed, mSerialDevice=" + mSerialDevice);
         if (mSerialDevice == null) {
             mTitleTextView.append("No serial device.\n");
         } else {
             try {
                 mSerialDevice.open();
             } catch (IOException e) {
                 Log.e(TAG, "Error setting up device: " + e.getMessage(), e);
                 mTitleTextView.append("Error opening device: "
                         + e.getMessage());
                 try {
                     mSerialDevice.close();
                 } catch (IOException e2) {
                     // Ignore.
                 }
                 mSerialDevice = null;
                 return;
             }
             mTitleTextView.append("Serial device: " + mSerialDevice);
         }
         onDeviceStateChange();
         startAsyncTasks();
         surfaceView = (SurfaceView)findViewById(R.id.surfaceView1);
         surfaceHolder = surfaceView.getHolder();
     }
     private void stopIoManager() {
         if (mSerialIoManager != null) {
             Log.i(TAG, "Stopping io manager ..");
             mSerialIoManager.stop();
             mSerialIoManager = null;
         }
     }
 
     private void startIoManager() {
         if (mSerialDevice != null) {
             Log.i(TAG, "Starting io manager ..");
             mSerialIoManager = new SerialInputOutputManager(mSerialDevice,
                     mListener);
             mExecutor.submit(mSerialIoManager);
         }
     }
 
     private void onDeviceStateChange() {
         stopIoManager();
         startIoManager();
     }
 
     private void updateReceivedData(byte[] data) {
         final String message = "Read " + data.length + " bytes: \n"
                 + HexDump.dumpHexString(data) + "\n\n";
         mDumpTextView.append(message);
         mScrollView.smoothScrollTo(0, mDumpTextView.getBottom());
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
 		PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
         setContentView(R.layout.activity_main);
 
         mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
 
         mTitleTextView = (TextView) findViewById(R.id.logTitle);
         mDumpTextView = (TextView) findViewById(R.id.logText);
         mScrollView = (ScrollView) findViewById(R.id.logScroller);
 
         ImageButton forwardBtn = (ImageButton) findViewById(R.id.forward_btn);
         ImageButton leftBtn = (ImageButton) findViewById(R.id.left_btn);
         ImageButton stopBtn = (ImageButton) findViewById(R.id.stop_btn);
         ImageButton rightBtn = (ImageButton) findViewById(R.id.right_btn);
         ImageButton backwardBtn = (ImageButton) findViewById(R.id.backward_btn);
         Button pictureBtn = (Button) findViewById(R.id.photo_btn);
         Button snapBtn = (Button) findViewById(R.id.snap);
 
         forwardBtn.setOnClickListener(controlClickListener);
         leftBtn.setOnClickListener(controlClickListener);
         stopBtn.setOnClickListener(controlClickListener);
         rightBtn.setOnClickListener(controlClickListener);
         backwardBtn.setOnClickListener(controlClickListener);
         pictureBtn.setOnClickListener(cameraClickListener);
         snapBtn.setOnClickListener(snapListener);
         
         locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
         
 
     }
     
     private void startAsyncTasks() {
     	long period = Long.valueOf(PreferenceManager
 				.getDefaultSharedPreferences(this).getString(
 						"pref_update_period", "1000"));
     	long cameraFrequency = Long.valueOf(PreferenceManager
 				.getDefaultSharedPreferences(this).getString(
 						"pref_camera_period", "15000"));
     	
     	if (commandPollingTimer == null) {
     		commandPollingTimer = new Timer();
     		commandPollingTimer.scheduleAtFixedRate(new TimerTask() {
 				@Override
 				public void run() {
 					runOnUiThread(new Runnable(){
 						@Override
 						public void run() {
 							CommandPoller poller = new CommandPoller(MainActivity.this);
 							poller.execute(buildURL() +"/command");
 						}
 					});
 				}
 			}, 0, period);
     	}
     	
     	if (cameraPostingTimer == null) {
     		cameraPostingTimer = new Timer();
     		cameraPostingTimer.scheduleAtFixedRate(new TimerTask() {
 				@Override
 				public void run() {
 					runOnUiThread(new Runnable(){
 						@Override
 						public void run() {
 							CameraPoster cameraPoster = new CameraPoster(MainActivity.this);
 							cameraPoster.execute(buildURL() +"/photo");
 						}
 					});
 				}
 			}, 0, cameraFrequency);
     	}
     	
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
 											   period, 0, onLocationChange);
     }
     private void stopAsyncTasks() {
     	if (commandPollingTimer != null) {
     		Log.v(TAG, "Polling timer shutting down...");
     		commandPollingTimer.cancel();
     		commandPollingTimer = null;
     	}
     	if (cameraPostingTimer != null) {
     		Log.v(TAG, "Camera timer shutting down...");
     		cameraPostingTimer.cancel();
     		cameraPostingTimer = null;
     	}
     	locationManager.removeUpdates(onLocationChange);
     }
     
     private OnClickListener snapListener = new OnClickListener(){
     	@Override
     	public void onClick(View v){
     		camera.takePicture(null, null, new Camera.PictureCallback() {
 			@Override
 			public void onPictureTaken(byte[] data, Camera camera) {
 				Toast.makeText(getApplicationContext(),
 						"Took Picture, size " + data.length + " bytes",
 						Toast.LENGTH_LONG).show();
 				String FILENAME = "image_file";
 				try {
 					FileOutputStream fos = openFileOutput(FILENAME,
 							Context.MODE_PRIVATE);
 					fos.write(data);
 					fos.close();
 				} catch (Exception e) {
 					Toast.makeText(getApplicationContext(),
 							e.getMessage(), Toast.LENGTH_LONG).show();
 				}
 				camera.stopPreview();
 			}
     		});
 		}
     };
 	protected Camera camera;
     
     
     
 	private OnClickListener cameraClickListener = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			camera = Camera.open();
 			if (camera != null) {
 				try {
 					camera.setPreviewDisplay(surfaceHolder);
 				} catch (IOException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 				camera.startPreview();
 				
 			}
 		}
 	};
 
     private OnClickListener controlClickListener = new OnClickListener() {
 
         @Override
         public void onClick(View v) {
         	RobotCommand command = new RobotCommand();
         	command.setComponent(RobotCommand.MOVE_STOP);
         	command.setValue(100);
         	
             switch (v.getId()) {
             case R.id.forward_btn:
             	command.setComponent(RobotCommand.MOVE_FORWARD);
                 break;
             case R.id.left_btn:
             	command.setComponent(RobotCommand.MOVE_LEFT);
                 break;
             case R.id.stop_btn:
             	command.setComponent(RobotCommand.MOVE_STOP);
                 break;
             case R.id.right_btn:
             	command.setComponent(RobotCommand.MOVE_RIGHT);
                 break;
             case R.id.backward_btn:
             	command.setComponent(RobotCommand.MOVE_REVERSE);
                 break;
             }
             executeCommand(command);
         }
     };
     
     public void executeCommand(RobotCommand command) {
     	String serialCommand = "S";
     	if (command == null) {
     		return;
     	}
     	switch(command.getComponent()) {
     	case RobotCommand.MOVE_FORWARD:
     		serialCommand = "F";
     		break;
     	case RobotCommand.MOVE_LEFT:
     		serialCommand = "L";
     		break;
     	case RobotCommand.MOVE_REVERSE:
     		serialCommand = "B";
     		break;
     	case RobotCommand.MOVE_RIGHT:
     		serialCommand = "R";
     		break;
     	case RobotCommand.MOVE_STOP:
     		serialCommand = "S";
     		break;
     	}
     	
     	Log.v(TAG, "Sending serial command [" + serialCommand + "]");
     	
         try {
             if (mSerialDevice != null) {
                 mSerialDevice.write(serialCommand.getBytes(), 1000);
             }
             else {
             	mDumpTextView.append("\nNo connected Device..");
                 mScrollView.smoothScrollTo(0, mDumpTextView.getBottom());
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 }
