 package adkTestProject.main;
 // usbmanager expose ADK as a File Descriptor
 import java.io.FileDescriptor;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import android.app.Activity;
 // hold ADK Intent, because it is not always connected
 import android.app.PendingIntent;
 import android.os.Bundle;
 import android.util.Log;
 // this is out UI Definition
 import android.widget.SeekBar;
 import android.widget.TextView;
 // this is precisely what usbmanager expose
 import android.os.ParcelFileDescriptor;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 // what we needed to read from ADK, 
 // use future because I only have a android 2.3.7 OS with me
 import com.android.future.usb.UsbManager;
 import com.android.future.usb.UsbAccessory;
 
 
 
 public class ADKTestProjectActivity extends Activity implements Runnable, SeekBar.OnSeekBarChangeListener {
 	// UsbManager to check if ADK is connected
 	private UsbManager mUsbManager;
 	// To read permission from ADK
 	private PendingIntent mPermissionIntent;
 	private boolean mPermissionRequestPending;
 	// This is the permission
 	private static final String ACTION_USB_PERMISSION = "com.google.android.ADKTestProject.action.USB_PERMISSION";
 	private static final String TAG = "ADKTestProject";
 	
 	SeekBar mRed;
 	SeekBar mBlue;
 	SeekBar mGreen;
 	TextView mRedText;
 	TextView mBlueText;
 	TextView mGreenText;
 	
 	int redValue = 255;
 	int blueValue = 255;
 	int greenValue = 255;
 	// This is where we read and write from ADK
 	FileInputStream mFileInputStream;
 	FileOutputStream mFileOutputStream;
 	ParcelFileDescriptor mFileDescriptor;	
 	// Accesory!!!
 	UsbAccessory mUsbAccessory;
 	// Receive intent from ADK so that the app will start
 	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver(){
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 			// permission ok?
 			if(ACTION_USB_PERMISSION.equals(action)){
 				synchronized (this) {
 					// get accessory definition 
 					UsbAccessory accessory = UsbManager.getAccessory(intent);
 				    // any extra permission?
 					if (intent.getBooleanExtra(
 							UsbManager.EXTRA_PERMISSION_GRANTED, false)){
 						// start accessory!!!!
 						openAccessory(accessory);
 					}
 					else {
 						// oops
 						Log.d(TAG,"Permission Denied For Accessory" 
 								+ accessory);
 					}
 					mPermissionRequestPending = false;
 				}
 			}
 			// it is not connected
 			else if(UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
 				// get accessory anyway
 				UsbAccessory accessory = UsbManager.getAccessory(intent);
 				// accessory is still not close cleanly
 				if (accessory != null && accessory.equals(mUsbAccessory)){
 					closeAccessory();
 				}
 			}
 			
 		}
 		
 	};
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         // get the USB Manager involved
         mUsbManager = UsbManager.getInstance(this);
         // setup permission
         mPermissionIntent = PendingIntent.getBroadcast(this, 0, 
         		new Intent(ACTION_USB_PERMISSION),0);
         // get the permission
         IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
         // register receiver
         registerReceiver(mUsbReceiver,filter);
         // device exist? Open and send value
         if(getLastNonConfigurationInstance() != null){
         	mUsbAccessory = (UsbAccessory) getLastNonConfigurationInstance();
         	openAccessory(mUsbAccessory);
         	sendCommand(redValue,blueValue,greenValue);
         }
         
         setContentView(R.layout.main);
         
         mRed = (SeekBar)findViewById(R.id.Red);
         mRed.setOnSeekBarChangeListener(this);
         mBlue = (SeekBar)findViewById(R.id.Blue);
         mBlue.setOnSeekBarChangeListener(this);
         mGreen = (SeekBar)findViewById(R.id.Green);
         mGreen.setOnSeekBarChangeListener(this);
         mRedText = (TextView)findViewById(R.id.RedText);
         mBlueText = (TextView)findViewById(R.id.BlueText);
         mGreenText = (TextView)findViewById(R.id.GreenText);
         mRedText.setText(getString(R.string.red_value_text) +
         		255);
         mBlueText.setText(getString(R.string.blue_value_text) +
         		255);
         mGreenText.setText(getString(R.string.green_value_text) +
         		255);
     }
     // retain the device, so that new activity can use it
     @Override
     public Object onRetainNonConfigurationInstance(){
     	if (mUsbAccessory != null){
     		return mUsbAccessory;
     	}
     	else{
     		return super.onRetainNonConfigurationInstance();
     	}
     }
     
     @Override
     public void onResume(){
     	super.onResume();
     	
     	Intent intent = getIntent();
     	// opps interface close, it is cool, do nothing
     	if (mFileInputStream != null && mFileOutputStream != null){
     		return ;
     	}
     	// get a list of accessory
     	UsbAccessory[] accessories = mUsbManager.getAccessoryList();
     	// if exist get the first one or give null
     	UsbAccessory accessory = (accessories == null ? null : accessories[0]);
    	// accessory exist
     	if (accessory != null){
     		// check permission
     		if (mUsbManager.hasPermission(accessory)){
     			// open and send value
     			openAccessory(accessory);
     			sendCommand(redValue,blueValue,greenValue);
     		}
     		else{
     			synchronized(this){
     				// request permission
     				if (!mPermissionRequestPending){
     					mUsbManager.requestPermission(accessory, 
     							mPermissionIntent);
     					mPermissionRequestPending = true;
     				}
     			}
     		}
     	}
     	else{
     		Log.d(TAG,"mAccessory is Null");
     	}
     }
     // just close it, it is cool
     @Override
     public void onPause(){
     	super.onPause();
     	closeAccessory();
     }
     // close everything
     @Override
     public void onDestroy(){
     	unregisterReceiver(mUsbReceiver);
     	super.onDestroy();
     }
     
 	public void onProgressChanged(SeekBar seekBar, int progress,
 			boolean fromUser) {
 		// TODO Auto-generated method stub
 		switch(seekBar.getId()){
 		case R.id.Red:
 			mRedText.setText(getString(R.string.red_value_text) + 
 					progress);
 			redValue = progress;
 			break;
 		case R.id.Blue:
 			mBlueText.setText(getString(R.string.blue_value_text) + 
 					progress);
 			blueValue = progress;
 			break;
 		case R.id.Green:
 			mGreenText.setText(getString(R.string.green_value_text) + 
 					progress);
 			greenValue = progress;
 			break;
 		}
 		// get value, and send it
 		sendCommand(redValue,blueValue,greenValue);
 	}
 	public void onStartTrackingTouch(SeekBar seekBar) {
 		// TODO Auto-generated method stub
 		
 	}
 	public void onStopTrackingTouch(SeekBar seekBar) {
 		// TODO Auto-generated method stub
 	}
 	
 	// this is mostly for reading
 	public void run() {
 		int ret = 0;
 		byte[] buffer = new byte[16384];
 		int i;
 		
 		while (ret >= 0){
 			try{
 				ret = mFileInputStream.read(buffer);
 			}
 			catch(IOException e) {
 				break;
 			}
 			i = 0;
 			while (i < ret){
 				int len = ret -i;
 				switch (buffer[i]){
 				default:
 					Log.d(TAG,"unknown msg: " + buffer[i]);
 					i = len;
 					break;
 				}
 			}
 		}
 		
 	}
 	// send command!!!!
 	public void sendCommand(int red, int green, int blue){
 		// we will only send 3 byte
 		byte[] buffer = new byte[3];
 		// make sure the value cannot be more than 255, 
 		// the LED can only receive that
 		if(red > 255){
 			red = 255;
 		}
 		if(green > 255){
 			green = 255;
 		}
 		if(blue > 255){
 			blue = 255;
 		}
 		// assign
 		buffer[0] = (byte)red;
 		buffer[1] = (byte)green;
 		buffer[2] = (byte)blue;
 		// make sure interface is there
 		if (mFileOutputStream != null && buffer[1] != -1){
 			try{
 				// write it
 				mFileOutputStream.write(buffer);
 			}
 			catch (IOException e){
 				Log.e(TAG,"Write Faile",e);
 			}
 		}
 	}
 	
 	private void openAccessory(UsbAccessory accessory){
 		// the interface is a file file descriptor
 		mFileDescriptor = mUsbManager.openAccessory(accessory);
 		if (mFileDescriptor != null) {
 			mUsbAccessory = accessory;
 			// get the file descriptor 
 			FileDescriptor fd = mFileDescriptor.getFileDescriptor();
 			// set one to read 
 			mFileInputStream = new FileInputStream(fd);
 			// set one to write
 			mFileOutputStream = new FileOutputStream(fd);
 			
 			Thread thread = new Thread(null,this,"ADKTestProject");
 			thread.start();
 			Log.d(TAG,"Accessory Opened");
 			
 		}
 		else {
 			Log.d(TAG,"Accessory Open Fail");
 		}
 	}
 	
 	private void closeAccessory(){
 		try{
 			if (mFileDescriptor != null){
 				mFileDescriptor.close();
 			}
 		} 
 		catch (IOException e) {
 			
 		}
 		finally {
 			mFileDescriptor = null;
 			mUsbAccessory = null;
 		}
 		
 	}
 }
