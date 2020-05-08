 package edu.berkeley.cellscope.cscore.celltracker;
 
 import java.io.File;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 
 import org.opencv.android.BaseLoaderCallback;
 import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
 import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
 import org.opencv.android.LoaderCallbackInterface;
 import org.opencv.android.OpenCVLoader;
 import org.opencv.core.Mat;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 import edu.berkeley.cellscope.cscore.BluetoothSerialService;
 import edu.berkeley.cellscope.cscore.CameraActivity;
 import edu.berkeley.cellscope.cscore.DeviceListActivity;
 import edu.berkeley.cellscope.cscore.R;
 import edu.berkeley.cellscope.cscore.cameraui.CompoundTouchListener;
 import edu.berkeley.cellscope.cscore.cameraui.ManualExposure;
 import edu.berkeley.cellscope.cscore.cameraui.PannableStage;
 import edu.berkeley.cellscope.cscore.cameraui.PinchSelectActivity;
 import edu.berkeley.cellscope.cscore.cameraui.TouchControl;
 import edu.berkeley.cellscope.cscore.cameraui.TouchExposureControl;
 import edu.berkeley.cellscope.cscore.cameraui.TouchPanControl;
 import edu.berkeley.cellscope.cscore.cameraui.TouchZoomControl;
 import edu.berkeley.cellscope.cscore.cameraui.ZoomablePreview;
 
 public class OpenCVCameraActivity extends Activity implements CvCameraViewListener2, PannableStage, ZoomablePreview, ManualExposure {
 
 	private static final String TAG = "OpenCV_Camera";
 	
 	private OpenCVCameraView mOpenCvCameraView;
 	protected TextView zoomText;
 	protected TextView infoText;
 	protected ImageButton takePicture, toggleTimelapse, zoomIn, zoomOut;
 	protected Mat mRgba;
 	
 	private TouchControl touchPan, touchZoom, touchExposure;
 	
 	
 	long timeElapsed;
 	long currentTime;
 	boolean timelapseOn = false;
 	
 	public static File mediaStorageDir = CameraActivity.mediaStorageDir;
 	
 	//Bluetooth stuff
 
     protected MenuItem mMenuItemConnect, mMenuItemPinch;
     private static BluetoothSerialService mSerialService = null;
     private boolean mEnablingBT;
 
 	protected ScheduledExecutorService stepperThread;
 	protected StageStepper stepper;
     
     
     private static final long TIMELAPSE_INTERVAL = 5 * 1000; //milliseconds
 	
 	// Intent request codes
     private static final int REQUEST_CONNECT_DEVICE = 1;
     private static final int REQUEST_ENABLE_BT = 2;
     private static final int REQUEST_PINCH_CONTROL = 3;
 	
 
     // Name of the connected device
     private String mConnectedDeviceName = null;
     
 	private BluetoothAdapter mBluetoothAdapter = null;
 
     private boolean mLocalEcho = false;
     private boolean bluetoothEnabled = false;
     private boolean proceedWithConnection = true;
     TextView bluetoothNameLabel;
     
     // Message types sent from the BluetoothReadService Handler
     public static final int MESSAGE_STATE_CHANGE = 1;
     public static final int MESSAGE_READ = 2;
     public static final int MESSAGE_WRITE = 3;
     public static final int MESSAGE_DEVICE_NAME = 4;
     public static final int MESSAGE_TOAST = 5;	
 
     // Key names received from the BluetoothChatService Handler
     public static final String DEVICE_NAME = "device_name";
     public static final String TOAST = "toast";
 
 
     /**
      * Set to true to add debugging code and logging.
      */
     public static final boolean DEBUG = true;
 
     /**
      * Set to true to log each character received from the remote process to the
      * android log, which makes it easier to debug some kinds of problems with
      * emulating escape sequences and control codes.
      */
     public static final boolean LOG_CHARACTERS_FLAG = DEBUG && true;
 
     /**
      * Set to true to log unknown escape sequences.
      */
     public static final boolean LOG_UNKNOWN_ESCAPE_SEQUENCES = DEBUG && true;
 
     /**
      * The tag we use when logging, so that our messages can be distinguished
      * from other messages in the log. Public because it's used by several
      * classes.
      */
 	public static final String LOG_TAG = "CellScope";
 	
 	
 	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
         @Override
         public void onManagerConnected(int status) {
             switch (status) {
                 case LoaderCallbackInterface.SUCCESS:
                 {
                     Log.i(TAG, "OpenCV loaded successfully");
                     mOpenCvCameraView.enableView();
                     //mOpenCvCameraView.disableAutoFocus();
                     //mOpenCvCameraView.setOnTouchListener(OpenCVCameraActivity.this);
                 } break;
                 default:
                 {
                     super.onManagerConnected(status);
                 } break;
             }
         }
     };
     
     private final Handler mHandlerBT = new Handler() {
         @Override
         public void handleMessage(Message msg) {        	
             switch (msg.what) {
             case MESSAGE_STATE_CHANGE:
                 if(DEBUG) Log.i(LOG_TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                 switch (msg.arg1) {
                 case BluetoothSerialService.STATE_CONNECTED:
                     if(DEBUG) Log.i(LOG_TAG, "MESSAGE_STATE_CHANGE/STATE_CONNECTED");
                     bluetoothConnected();
                 	
                 	//Replace my input variable to the bluetooth device below
 //------           	mInputManager.showSoftInput(mEmulatorView, InputMethodManager.SHOW_IMPLICIT);
                 	
                     break;
                     
                 case BluetoothSerialService.STATE_CONNECTING:
                 	if(DEBUG) Log.i(LOG_TAG, "MESSAGE_STATE_CHANGE/STATE_CONNECTING");
                 	bluetoothNameLabel.setText(R.string.title_connecting);
                     break;
                     
                 case BluetoothSerialService.STATE_LISTEN:
                 	if(DEBUG) Log.i(LOG_TAG, "MESSAGE_STATE_CHANGE/STATE_LISTEN");
                 case BluetoothSerialService.STATE_NONE:
                 	if(DEBUG) Log.i(LOG_TAG, "MESSAGE_STATE_CHANGE/STATE_NONE");
                 	bluetoothDisconnected();
 
             		//I have to replace this line with whatever I am using as an input to the bluetooth device
 //-----                	mInputManager.hideSoftInputFromWindow(mEmulatorView.getWindowToken(), 0);
                 	bluetoothNameLabel.setText(R.string.title_not_connected);
             		bluetoothEnabled = false;
                 	if(DEBUG) Log.i(LOG_TAG, "MESSAGE_STATE_CHANGE/STATE_CONNECTED/CACA");
 
                     break;
                 }
                 break;
             case MESSAGE_WRITE:
             	if(DEBUG) Log.i(LOG_TAG, "MESSAGE_WRITE " + msg.arg1);
             	//if (mLocalEcho) {
             		//byte[] writeBuf = (byte[]) msg.obj;
             		//mEmulatorView.write(writeBuf, msg.arg1);
             	//}
                 
                 break;
                 
             case MESSAGE_READ:
             	if(DEBUG) Log.i(LOG_TAG, "MESSAGE_READ " + msg.arg1);
             	//byte[] readBuf = (byte[]) msg.obj;              
                 //mEmulatorView.write(readBuf, msg.arg1);
                 
                 break;
                 
             case MESSAGE_DEVICE_NAME:
             	if(DEBUG) Log.i(LOG_TAG, "MESSAGE_DEVICE_NAME: " + msg.arg1);
             	// save the connected device's name
                 mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                 Toast.makeText(getApplicationContext(), "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                 break;
             case MESSAGE_TOAST:
             	if(DEBUG) Log.i(LOG_TAG, "MESSAGE_TOAST: " + msg.arg1);
             	Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                                Toast.LENGTH_SHORT).show();
                 break;
             }
         }
     };
 
     protected void bluetoothConnected() {
     	if (mMenuItemConnect != null) {
     		mMenuItemConnect.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
     		mMenuItemConnect.setTitle(R.string.disconnect);
     	}
     	bluetoothNameLabel.setText(R.string.title_connected_to);
     	bluetoothNameLabel.append(mConnectedDeviceName);
 		bluetoothEnabled = true;
 		
 		stepperThread = Executors.newScheduledThreadPool(1);
 		//stepperThread.schedule(stepper, 0, TimeUnit.MILLISECONDS);
     }
     
     protected void bluetoothDisconnected() {
     	if (mMenuItemConnect != null) {
     		mMenuItemConnect.setIcon(android.R.drawable.ic_menu_search);
     		mMenuItemConnect.setTitle(R.string.connect);
     	}
     	
     	if (stepperThread != null) {
     		stepperThread.shutdownNow();
     		stepperThread = null;
     	}
     }
 
     public OpenCVCameraActivity() {
         Log.i(TAG, "Instantiated new " + this.getClass());
     }
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 		setContentView(R.layout.activity_opencv_camera);
         mOpenCvCameraView = (OpenCVCameraView) findViewById(R.id.opencv_camera_view);
         mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
         mOpenCvCameraView.setCvCameraViewListener(this);
         mOpenCvCameraView.setActivity(this);
 	    
         takePicture = (ImageButton)findViewById(R.id.opencv_takePhotoButton);
         toggleTimelapse = (ImageButton)findViewById(R.id.opencv_timelapse);
         zoomIn = (ImageButton)findViewById(R.id.opencv_zoomInButton);
         zoomOut = (ImageButton)findViewById(R.id.opencv_zoomOutButton);
         
         zoomText = (TextView)findViewById(R.id.opencv_zoomtext);
 	    zoomText.setText("100%");
 	    
 	    infoText = (TextView)findViewById(R.id.opencv_infotext);
 	    
 	    CompoundTouchListener compound = new CompoundTouchListener();
 	    touchPan = new TouchPanControl(this, this);
 	    touchZoom = new TouchZoomControl(this);
 	    touchExposure = new TouchExposureControl(this);
 	    touchPan.setEnabled(true);
 	    touchZoom.setEnabled(true);
 	    compound.addTouchListener(touchPan);
 	    compound.addTouchListener(touchZoom);
 	    compound.addTouchListener(touchExposure);
 	    mOpenCvCameraView.setOnTouchListener(compound);
 	    
 	    mSerialService = new BluetoothSerialService(this, mHandlerBT/*, mEmulatorView*/);
 
 
 		bluetoothNameLabel = (TextView) findViewById(R.id.opencv_bluetoothtext);
 
 		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 		//BluetoothAdapter myBluetoothAdapter = null; //This was to test to see what the noBluetoothAdapter() method did
 		if (mBluetoothAdapter == null){
 			mMenuItemConnect.setEnabled(false);
 		}
 		
 		byte[] stopCommand = new byte[]{stopMotor};
 		stepper = new StageStepper(stopCommand, stopCommand);
 		
 		
     }
 	
 
     @Override
     public void onStart() {
     	super.onStart();
 		mEnablingBT = false;
     }
 
     @Override
     public void onPause()
     {
         super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
     }
 
     @Override
     public void onResume()
     {
         super.onResume();
         OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5, this, mLoaderCallback);
        System.out.println("RESUMED");
     }
     
     @Override
     public void onDestroy() {
         super.onDestroy();
         if (mOpenCvCameraView != null)
             mOpenCvCameraView.disableView();
         if (mSerialService != null)
         	mSerialService.stop();
     }
 
 	public void onCameraViewStarted(int width, int height) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void onCameraViewStopped() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
 		mRgba = inputFrame.rgba();
 		initialFrame();
 		if (timelapseOn)
 			timelapse();
 		return mRgba;
 	}
 	
 	public void initialFrame() {
 		
 	}
 	
 	public boolean timelapse() {
 		if (currentTime != -1) {
 			long newTime = System.currentTimeMillis();
 			timeElapsed += newTime - currentTime;
 			if (timeElapsed > TIMELAPSE_INTERVAL) {
 				takePhoto();
 				timeElapsed -= TIMELAPSE_INTERVAL;
 			}
 			currentTime = newTime;
 		}
 		else
 			currentTime = System.currentTimeMillis();
 		return false;
 	}
 	
 	public void toggleTimelapse(View v) {
 		if (!timelapseOn) {
 			toggleTimelapse.setImageResource(R.drawable.stop);
 			timelapseOn = true;
 			timeElapsed = 0;
 			currentTime = -1;
 		}
 		else {
 			toggleTimelapse.setImageResource(R.drawable.record);
 			timelapseOn = false;
 		}
 	}
 	
 	public void takePhoto() {
 		File file = CameraActivity.getOutputMediaFile(CameraActivity.MEDIA_TYPE_IMAGE);
 		mOpenCvCameraView.takePicture(file);
 	}
 	
 	public void takePhoto(View view) {
 		takePhoto();
 	}
 	
 	public void zoomIn(View view) {
 		zoom(10);
 	}
 	public void zoomOut(View view) {
 		zoom(-10);
 	}
 	
 	public double getDiagonal() {
 		return CameraActivity.getScreenDiagonal(this);
 	}
 	
 	public double getMaxZoom() {
 		return mOpenCvCameraView.getMaxZoom();
 	}
 	
 	public void zoom(int amount) {
 		String str = mOpenCvCameraView.zoom(amount);
 		zoomText.setText(str);
 	}
 	
 	public int getMaxExposure() {
 		return mOpenCvCameraView.getMaxExposure();
 	}
 	
 	public int getMinExposure() {
 		return mOpenCvCameraView.getMinExposure();
 	}
 	
 	public void adjustExposure(int amount) {
 		String str = mOpenCvCameraView.adjustExposure(amount);
 		infoText.setText(getString(R.string.exposure_label) + str);
 	}
 	
 	public boolean panAvailable() {
 		return bluetoothEnabled;
 	}
 	
 	public void panStage(int newState) {
 		if (bluetoothEnabled) {
 			/*
 			if (panState == zUpMotor || panState == zDownMotor || panState == stopMotor) {
 				byte[] buffer = new byte[]{(byte)panState};
 				stepper.setFirst(buffer);
 				stepper.setSecond(buffer);
 				stepper.delayOne = stepper.delayTwo = 100;
 			}
 			else {
 				byte[] stop = new byte[]{(byte)stopMotor};
 				byte[] command = new byte[]{(byte)panState};
 				stepper.setFirst(stop);
 				stepper.setSecond(command);
 				stepper.delayOne = 200;
 				stepper.delayTwo = 5;
 			}
 			*/
 			byte[] buffer = new byte[1];
         	buffer[0] = (byte)newState;
         	mSerialService.write(buffer);
 		}
 	}
 	
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu_bluetooth, menu);
         mMenuItemConnect = menu.getItem(0);
         inflater.inflate(R.menu.menu_controls, menu);
         mMenuItemPinch = menu.getItem(1);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	int id = item.getItemId();
        	if (id == R.id.connect) {
         	connectBluetooth();
             return true;
         }
        	else if (id == R.id.menu_pinch) {
        		Intent intent = new Intent(this, PinchSelectActivity.class);
     		startActivityForResult(intent, REQUEST_PINCH_CONTROL);
        	}
         return false;
     }
     
     public void connectBluetooth() {
     	proceedWithConnection = true;
     	if (getConnectionState() == BluetoothSerialService.STATE_NONE) {
     		if (!mEnablingBT) { // If we are turning on the BT we cannot check if it's enable
     		    if ( (mBluetoothAdapter != null)  && (!mBluetoothAdapter.isEnabled()) ) {
             		proceedWithConnection = false;           	
                     AlertDialog.Builder builder = new AlertDialog.Builder(this);
                     builder.setMessage(R.string.alert_dialog_turn_on_bt)
                         .setIcon(android.R.drawable.ic_dialog_alert)
                         .setTitle(R.string.alert_dialog_warning_title)
                         .setCancelable( false )
                         .setPositiveButton(R.string.alert_dialog_yes, new DialogInterface.OnClickListener() {
                         	public void onClick(DialogInterface dialog, int id) {
                         		mEnablingBT = true;
                         		Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                         		startActivityForResult(enableIntent, REQUEST_ENABLE_BT);			
                         	}
                         })
                         .setNegativeButton(R.string.alert_dialog_no, new DialogInterface.OnClickListener() {
                         	public void onClick(DialogInterface dialog, int id) {
                         	}
                         });
                     AlertDialog alert = builder.create();
                     alert.show();
     		    }		
     		
     		    if (mSerialService != null) {
     		    	// Only if the state is STATE_NONE, do we know that we haven't started already
     		    	if (mSerialService.getState() == BluetoothSerialService.STATE_NONE) {
     		    		// Start the Bluetooth chat services
     		    		mSerialService.start();
     		    	}
     		    }
     		}
     		if (proceedWithConnection) {
         		// Launch the DeviceListActivity to see devices and do scan
         		Intent serverIntent = new Intent(this, DeviceListActivity.class);
         		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
     		}
     	}
     	else
         	if (getConnectionState() == BluetoothSerialService.STATE_CONNECTED) {
         		mSerialService.stop();
 	    		mSerialService.start();
         	}
     }
     
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         if(DEBUG) Log.d(LOG_TAG, "onActivityResult " + resultCode);
         if (requestCode == REQUEST_CONNECT_DEVICE) {
            //forceUpdateCamera = true;
             // When DeviceListActivity returns with a device to connect
             if (resultCode == Activity.RESULT_OK) {
                 // Get the device MAC address
                 String address = data.getExtras()
                                      .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                 // Get the BLuetoothDevice object
                 BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address); //I deleted "m." before the method getRemoteDevice()
                 // Attempt to connect to the device
                 mSerialService.connect(device);
             }
         }
         else if (requestCode == REQUEST_ENABLE_BT) {
             // When the request to enable Bluetooth returns
             if (resultCode != Activity.RESULT_OK) {
                 Log.d(LOG_TAG, "BT not enabled");
                //forceUpdateCamera = true;
                 mEnablingBT = false;
                 //finishDialogNoBluetooth();                
             }
             else {
             	Intent serverIntent = new Intent(this, DeviceListActivity.class);
             	startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
             }
         }
         
         else if (requestCode == REQUEST_PINCH_CONTROL) {
         	if (resultCode == PinchSelectActivity.SELECT_ZOOM) {
         		touchZoom.setEnabled(true);
         		touchExposure.setEnabled(false);
         		infoText.setText("");
         	}
         	else if (resultCode == PinchSelectActivity.SELECT_EXPOSURE) {
         		touchZoom.setEnabled(false);
         		touchExposure.setEnabled(true);
         	}
         }
     }
     
 	public int getConnectionState() {
 		return mSerialService.getState();
 	}
 	
 	class StageStepper implements Runnable {
 		volatile byte[] one, two;
 		boolean switcher = true;
 		int delayOne, delayTwo;
 		
 		StageStepper(byte[] a, byte[] b) {
 			one = a;
 			two = b;
 		}
 		
 		public synchronized void run() {
 			if (mSerialService != null) {
 				if (switcher) {
 					mSerialService.write(one);
 					//stepperThread.schedule(this, delayOne, TimeUnit.MILLISECONDS);
 				}
 				else {
 					mSerialService.write(two);
 					//stepperThread.schedule(this, delayTwo, TimeUnit.MILLISECONDS);
 				}
 				switcher = !switcher;
 			}
 		}
 		
 		public synchronized void setFirst(byte[] b) {
 			one = b;
 		}
 		
 		public synchronized void setSecond(byte[] b) {
 			two = b;
 		}
 	}
 }
