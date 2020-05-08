 // ServiceADKActivity.java
 // ---------------------------
 // RobotGrrl.com
 // November 29, 2011
 
 package com.scigames.registration;
 
 import java.io.FileDescriptor;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.android.future.usb.UsbAccessory;
 import com.android.future.usb.UsbManager;
 import com.scigames.registration.R;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.PendingIntent;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.res.Resources;
 import android.graphics.Typeface;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.ParcelFileDescriptor;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Registration2RfidMass_AdkServiceActivity extends Activity implements Runnable, SciGamesListener{
     /** Called when the activity is first created. */
     private boolean debug = false ; //if you don't have a board to attach and are just testing
     
 	private static final String TAG = "Registration2RfidMass_AdkServiceActivity";
 	
 	/*** service stuff ***/
 	private static final String ACTION_USB_PERMISSION = "com.google.android.DemoKit.action.USB_PERMISSION";
 	private UsbManager mUsbManager;
 	private PendingIntent mPermissionIntent;
 	private boolean mPermissionRequestPending;
 	UsbAccessory mAccessory;
 	ParcelFileDescriptor mFileDescriptor;
 	FileInputStream mInputStream;
 	FileOutputStream mOutputStream;
 	Thread mThread;
 	TextView fileDescText, inputStreamText, outputStreamText, accessoryText;
 	private Handler mHandler = new Handler();	
 	/*** end service stuff ***/
 	
     private String visitIdIn = "FNAME";
     private String studentIdIn = "LNAME";
     private String needsRfid = "maybe";
     private String firstNameIn = "test";
     private String lastNameIn = "test";
     private boolean haveRfid = false;
 	
     TextView braceletId;
     TextView thisMass;
     
     TextView greets;
     Button rfidContinueButton;
     Button massContinueButton;
     Button massCaptureButton;
     
     AlertDialog alertDialog;
     AlertDialog infoDialog; //only used for debug
 
     
     ProgressDialog progressBar;
     private int progressBarStatus = 0;
     private Handler progressBarHandler = new Handler();
     private int currProgress = 0;
     
     Typeface ExistenceLightOtf;
     Typeface Museo300Regular;
     Typeface Museo500Regular;
     Typeface Museo700Regular;
     
     SciGamesHttpPoster task = new SciGamesHttpPoster(Registration2RfidMass_AdkServiceActivity.this, "http://db.scigam.es/push/new_rfid.php");
     //SciGamesHttpPoster MassTask = new SciGamesHttpPoster(Registration2RfidMass_AdkServiceActivity.this, "http://db.scigam.es/push/update_mass.php");
     	
 	// ---------
 	// Lifecycle
 	// ---------
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
 		
         super.onCreate(savedInstanceState);
         Log.d(TAG,"onCreate");
         task.setOnResultsListener(this);
         
 		alertDialog = new AlertDialog.Builder(Registration2RfidMass_AdkServiceActivity.this).create();
 	    alertDialog.setTitle("No Registration System Attached ");
 	    alertDialog.setButton(RESULT_OK,"OK", new DialogInterface.OnClickListener() {
 	        public void onClick(DialogInterface dialog, int which) {
 	        	//Write your code here to execute after dialog closed
 		        Toast.makeText(getApplicationContext(), "quitting", Toast.LENGTH_SHORT).show();
 		        finish();
 		        System.exit(0);
 	//	        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
 	//	        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 	//	        intent.putExtra("EXIT", true);
 	//	        startActivity(intent);
 	        }
 	    });
 	    
 		infoDialog = new AlertDialog.Builder(Registration2RfidMass_AdkServiceActivity.this).create();
 		infoDialog.setTitle("debug info! ");
 		infoDialog.setButton(RESULT_OK,"OK", new DialogInterface.OnClickListener() {
 	        public void onClick(DialogInterface dialog, int which) {
 	        	Toast.makeText(getApplicationContext(), "ok!", Toast.LENGTH_SHORT).show();
 	        }
 	    });
         
         /******* service stuff ******/
         mUsbManager = UsbManager.getInstance(this);
 		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
 				ACTION_USB_PERMISSION), 0);
 		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
 		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
 		registerReceiver(mUsbReceiver, filter);
 
 		if (getLastNonConfigurationInstance() != null) {
 			mAccessory = (UsbAccessory) getLastNonConfigurationInstance();
 			openAccessory(mAccessory);
 		}
 		Log.e(TAG, "Hellohello!");	
 		startService(new Intent(this, ADKService.class));
 
 		/***** end service stuff *****/
         
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
         Intent i = getIntent();
         Log.d(TAG,"getIntent");
     	if(i.hasExtra("fName")) firstNameIn = i.getStringExtra("fName");
     	if(i.hasExtra("lName"))lastNameIn = i.getStringExtra("lName");
     	if(i.hasExtra("studentId"))studentIdIn = i.getStringExtra("studentId");
     	if(i.hasExtra("visitId"))visitIdIn = i.getStringExtra("visitId");
     	if(i.hasExtra("needsRfid"))needsRfid = i.getStringExtra("needsRfid");
     	Log.d(TAG,"...getStringExtra");
     	
 	    ExistenceLightOtf = Typeface.createFromAsset(getAssets(),"fonts/Existence-Light.ttf");
 	    Museo300Regular = Typeface.createFromAsset(getAssets(),"fonts/Museo300-Regular.otf");
 	    Museo500Regular = Typeface.createFromAsset(getAssets(),"fonts/Museo500-Regular.otf");
 	    Museo700Regular = Typeface.createFromAsset(getAssets(),"fonts/Museo700-Regular.otf");
 	    
     	Resources res = getResources();
     	setContentView(R.layout.registration2_rfid);
     	
 		braceletId = (TextView) findViewById(R.id.bracelet_id);
 		braceletId.setVisibility(View.INVISIBLE);
 		
 		rfidContinueButton = (Button) findViewById(R.id.rfid_continue_button);
 		rfidContinueButton.setOnClickListener(mRFIDContinueButtonListener);
 		rfidContinueButton.setVisibility(View.INVISIBLE);
   	
         greets = (TextView)findViewById(R.id.greeting);
         setTextViewFont(Museo700Regular, greets);
         greets.setText(String.format(res.getString(R.string.greeting), firstNameIn, lastNameIn));
         Log.d(TAG,"...Greetings");
         	    
     	if(needsRfid.equals("yes")){
     		Log.d(TAG,"needsRfid!");
     		haveRfid = false;
 
     	} else {
     		haveRfid = true;
     		setContentView(R.layout.registration3_mass);
 
     		massContinueButton = (Button) findViewById(R.id.mass_continue_button);
     		massContinueButton.setOnClickListener(mRFIDContinueButtonListener);
             massContinueButton.setVisibility(View.INVISIBLE);
             
             massCaptureButton = (Button) findViewById(R.id.capture_mass);
             massContinueButton.setOnClickListener(mCaptureMassButtonListener);
             massContinueButton.setVisibility(View.VISIBLE);
             
             thisMass = (TextView) findViewById(R.id.mass);
             thisMass.setVisibility(View.INVISIBLE);
     	}
 	    
         
         task.setOnResultsListener(this);
         if(debug){infoDialog.setTitle("VisitIdIn");
         infoDialog.setMessage(visitIdIn);
         infoDialog.show();}
         
         Log.d(TAG, "...end OnCreate");
     }
 
     @Override
 	public Object onRetainNonConfigurationInstance() {
 		if (mAccessory != null) {
 			return mAccessory;
 		} else {
 			return super.onRetainNonConfigurationInstance();
 		}
 	}
     
     @Override
     protected void onNewIntent(Intent intent){
     	Log.v(TAG, "onResume");
         Intent i = getIntent();
         Log.d(TAG,"getIntent");
         Log.d(TAG,"values in:");
         firstNameIn = intent.getExtras().getString("fName");
         Log.d(TAG,firstNameIn);
         lastNameIn = intent.getExtras().getString("lName");
         Log.d(TAG,lastNameIn);
         studentIdIn = intent.getExtras().getString("studentId");
         Log.d(TAG,studentIdIn);
         visitIdIn = intent.getExtras().getString("visitId");
         Log.d(TAG,visitIdIn);
         //visitIdIn = this.getIntent().getExtras().getString("visitId");
         needsRfid = this.getIntent().getExtras().getString("needsRfid");
         
         if(debug){infoDialog.setTitle("VisitIdIn from New Intent");
         infoDialog.setMessage(visitIdIn);
         infoDialog.show();}
     }
     
     @Override
 	public void onResume() {
     	
     	Resources res = getResources();
     		
     	if(needsRfid.equals("yes")){
     		Log.d(TAG,"needsRfid!");
     		haveRfid = false;
     		setContentView(R.layout.registration2_rfid);
     		rfidContinueButton = (Button) findViewById(R.id.rfid_continue_button);
     		rfidContinueButton.setOnClickListener(mRFIDContinueButtonListener);
     		rfidContinueButton.setVisibility(View.INVISIBLE);
     		
     		greets = (TextView)findViewById(R.id.greeting);
 //            Log.d(TAG,"...TextView greets find greeting");
             greets.setText(String.format(res.getString(R.string.greeting), firstNameIn, lastNameIn));
             setTextViewFont(Museo700Regular, greets);
             
             braceletId = (TextView)findViewById(R.id.bracelet_id);
             setTextViewFont(Museo700Regular, braceletId);
             braceletId.setVisibility(View.INVISIBLE);
 //            Log.d(TAG,"...Greetings");
 //            ((Button) findViewById(R.id.rfid_continue_button)).setOnClickListener(mRFIDContinueButtonListener);
 //            setTextViewFont(Museo300Regular, greets);   
     	} else {
     		haveRfid = true;
     		setContentView(R.layout.registration3_mass);
     		massContinueButton = (Button) findViewById(R.id.mass_continue_button);
     		massContinueButton.setOnClickListener(mRFIDContinueButtonListener);
             massContinueButton.setVisibility(View.INVISIBLE);
             
             massCaptureButton = (Button) findViewById(R.id.capture_mass);
             massCaptureButton.setOnClickListener(mCaptureMassButtonListener);
             massCaptureButton.setVisibility(View.VISIBLE);
             
             thisMass = (TextView) findViewById(R.id.mass);
             thisMass.setVisibility(View.INVISIBLE);
     	}
     	
 		super.onResume();
 		
 		try {
 			ADKService.self.stopUpdater();
 		} catch(Exception e) {
 			Log.d(TAG, "Stopping the updater failed");
 		}
 		
 		Intent intent = getIntent();
 		
 		if (mInputStream != null && mOutputStream != null) {
 			Log.v(TAG, "input and output stream weren't null!");
 			enableControls(true);
 			return;
 		}
 		
 		UsbAccessory[] accessories = mUsbManager.getAccessoryList();
 		
 		Log.v(TAG, "all the accessories: " + accessories);
 		
 		UsbAccessory accessory = (accessories == null ? null : accessories[0]);
 		if (accessory != null) {
 			if (mUsbManager.hasPermission(accessory)) {
 				Log.v(TAG, "mUsbManager does have permission");
 				openAccessory(accessory);
 			} else {
 				Log.v(TAG, "mUsbManager did not have permission");
 				synchronized (mUsbReceiver) {
 					if (!mPermissionRequestPending) {
 						mUsbManager.requestPermission(accessory,
 								mPermissionIntent);
 						mPermissionRequestPending = true;
 					}
 				}
 			}
 		} else if (!debug){
 			Log.d(TAG, "mAccessory is null");
 			Log.d(TAG, "NO ACCESSORY ATTACHED");
 			alertDialog.setMessage("Please Attach the Registration System to this Tablet and Login");
 			alertDialog.show();
 		
 		} else if (debug){
 			setBraceletId("testrfidstring"+String.valueOf(Math.random()*100));
 			Log.d(TAG,"...setBraceletId");
 			setTextViewFont(Museo300Regular, braceletId);
 			braceletId.setVisibility(View.VISIBLE);
 			rfidContinueButton.setVisibility(View.VISIBLE);
 			greets.setVisibility(View.INVISIBLE);
 			View thisView = findViewById(R.id.registration2_rfid_layout);
 			thisView.setBackgroundResource(R.drawable.bg_bracelet_allset);
 			
 		}
 		
 		// Let's update the textviews for easy debugging here...
 		updateTextViews();
 		
 	}
     
     @Override
 	public void onPause() {
     	Log.v(TAG, "onPause");
     	//closeAccessory();
     	try {
     		ADKService.self.startUpdater();
 		} catch(Exception e) {		
 		} 	
         Log.v(TAG, "done, now pause");
 		super.onPause();
 	}
 
 	@Override
 	public void onDestroy() {
 		Log.v(TAG, "onDestroy");
 		unregisterReceiver(mUsbReceiver);
 		super.onDestroy();
 	}
 	
 	 @Override
 	 protected void onStop() {
 	      super.onStop();
 	}
 
 	//@Override
 	public void run() {
 		int ret = 0;
 		byte[] buffer = new byte[16384];
 		int i;
 		int iValue = 0;
 		String thisBracelet = "";
 		int thisMassR = 0;
 		//while (ret >= 0) {
 		while(true){
 			try {
 				ret = mInputStream.read(buffer);
 				Log.d(TAG, "ret =" + String.valueOf(ret));
 			} catch (IOException e) {
 				break;
 			}
 			thisBracelet = "";
 			i = 0;
 			while (i < ret) {
 				int len = ret - i;
 				Log.v(TAG, "Read: " + buffer[i]);	
 				final int val = (int)buffer[i];
 				byte[] value = new byte [ret];
 				value[i] = (byte)buffer[i];
 				iValue = (int)buffer[i];
 				if(i == 0 && iValue == 111){
 					thisMassR = buffer[1] & 0xFF;
 				}
 				Log.d(TAG, "buffer: "+String.valueOf(i));
 				Log.d(TAG, Integer.toHexString(iValue));
 				thisBracelet = thisBracelet.concat(Integer.toHexString(iValue));
 				Log.d(TAG, "thisBracelet: "+thisBracelet);
 				
 				i++;
 			}	
 			final String fThisBracelet = thisBracelet;
 			final String fThisMass = String.valueOf(thisMassR);
 			mHandler.post(new Runnable() {
 				
 				public void run() {
 	            	// This gets executed on the UI thread so it can safely modify Views
 					if(!haveRfid){
 						setBraceletId(fThisBracelet);
 						Log.d(TAG,"...setBraceletId");
 						setTextViewFont(Museo300Regular, braceletId);
 						braceletId.setVisibility(View.VISIBLE);
 						rfidContinueButton.setVisibility(View.VISIBLE);
 						greets.setVisibility(View.INVISIBLE);
 						View thisView = findViewById(R.id.registration2_rfid_layout);
 						thisView.setBackgroundResource(R.drawable.bg_bracelet_allset);
 					} else {
 						if(Integer.parseInt(fThisMass) < 15) setMass(String.valueOf(Math.round(45+Math.random()*30))); //for debugging < 15kg means no scale attached.
 						else setMass(fThisMass);
 						View thisView = findViewById(R.id.registration3_mass_layout);
 						thisView.setBackgroundResource(R.drawable.bg_mass_done);
 						Log.d(TAG,"...setMass");
 					}
 				}
 			});
 //			switch (buffer[i]) {
 //				default:
 //					Log.d(TAG, "unknown msg: " + buffer[i]);
 //					i = len;
 //					break;
 //				}
 		}
 	}
 	public void setMass(String m){
 		Resources res = getResources();
 		greets.setVisibility(View.INVISIBLE);
 		thisMass = (TextView) findViewById(R.id.mass);
 		thisMass.setText(m);
        //thisMass.setVisibility(View.VISIBLE); 
		thisMass.setVisibility(View.INVISIBLE);/** keep invisible for actual use! **/
 		
 		massContinueButton = (Button) findViewById(R.id.mass_continue_button);
 		massContinueButton.setVisibility(View.VISIBLE);
 		
 		massCaptureButton = (Button) findViewById(R.id.capture_mass);
 		//massCaptureButton.setVisibility(View.INVISIBLE);
 
 	}
 		
 	public void setBraceletId(String bId){
 		Resources res = getResources();
 		greets.setVisibility(View.INVISIBLE);
 		braceletId = (TextView) findViewById(R.id.bracelet_id);
 		braceletId.setText(bId);
 	}
 
 		
     // ------------
     // ADK Handling
 	// ------------
 	
 	private void openAccessory(UsbAccessory accessory) {
 		
 		Log.e(TAG, "openAccessory: " + accessory);
 		Log.d(TAG, "this is mUsbManager: " + mUsbManager);
 		mFileDescriptor = mUsbManager.openAccessory(accessory);
 		
 		Log.d(TAG, "Tried to open");
 		
 		if (mFileDescriptor != null) {
 			mAccessory = accessory;
 			FileDescriptor fd = mFileDescriptor.getFileDescriptor();
 			mInputStream = new FileInputStream(fd);
 			mOutputStream = new FileOutputStream(fd);
 			mThread = new Thread(null, this, "DemoKit"); // meep
 			mThread.start(); // meep
 			Log.d(TAG, "accessory opened");
 			enableControls(true);
 		} else {
 			Log.d(TAG, "accessory open fail");
 			
 				Log.d(TAG, "NO ACCESSORY ATTACHED");
 				alertDialog.setMessage("Please Attach the Registration System to this Tablet and Login");
 				alertDialog.show();
 			
 			enableControls(false);
 		}
 	}
 	
 	private void closeAccessory() {
 
 		Log.e(TAG, "closing accessory");
 		
 		try {
 			if (mFileDescriptor != null) {
 				mFileDescriptor.close();
 			}
 		} catch (IOException e) {
 		} finally {
 			mFileDescriptor = null;
 			mAccessory = null;
 		}
 		
 		enableControls(false);
 		
 	}
 
 	public void sendCommand(byte command, byte target, int value) {
 		Log.e(TAG,"sendCommand hit");
 		byte[] buffer = new byte[3];
 		if (value > 255)
 			value = 255;
 
 		buffer[0] = command;
 		buffer[1] = target;
 		buffer[2] = (byte) value;
 		if (mOutputStream != null && buffer[1] != -1) {
 			try {
 				mOutputStream.write(buffer);
 			} catch (IOException e) {
 				Log.e(TAG, "write failed", e);
 			}
 		}
 	}
 	
 
     public void sendPress(char c) {
 		Log.d(TAG, "sendPress hit: ");
 		Log.d(TAG, String.valueOf(c));
     	byte[] buffer = new byte[2];
 		buffer[0] = (byte)'!';
 		buffer[1] = (byte)c;
 			
 		if (mOutputStream != null) {
 			try {
 				mOutputStream.write(buffer);
 			} catch (IOException e) {
 				Log.e(TAG, "write failed", e);
 			}
 		}
 		
 	}
     
 	public boolean adkConnected() {
     	//if(mInputStream != null && mOutputStream != null) return true;
     	if(mFileDescriptor != null) return true;
     	return false;
     }
 	
 	
 	// --------------
 	// User interface
 	// --------------
 	
 	private void enableControls(boolean b) {
 		((ServiceADKApplication) getApplication()).setInputStream(mInputStream);
 		((ServiceADKApplication) getApplication()).setOutputStream(mOutputStream);
 		((ServiceADKApplication) getApplication()).setFileDescriptor(mFileDescriptor);
 		((ServiceADKApplication) getApplication()).setUsbAccessory(mAccessory);
 		updateTextViews();
 		
 		if(!b) {
 			try {
 	    		ADKService.self.stopUpdater();
 			} catch(Exception e) {
 				
 			}
 		}
 		sendPress('A');
 	}
     
 //    @Override
 //	public void onClick(View v) {
 //		Log.v(TAG, "click!");
 //		
 //		if(v.getId() == R.id.button1) {
 //			Log.v(TAG, "Pressed Read");
 //			sendPress('a');
 //		}
 //		else if(v.getId() == R.id.button2) {
 //			Log.v(TAG, "pressed button2");
 //			sendPress('C');
 //			Intent i = new Intent(Registration2RfidMass_AdkServiceActivity.this, Registration3MassActivity.class);
 //			Registration2RfidMass_AdkServiceActivity.this.startActivity(i);
 //			
 //		}
 //		
 //	}
     
     private void updateTextViews() {
 
     	Log.v(TAG, "updated text views");
 //    	Resources res = getResources();
 //    	braceletId = (TextView) findViewById(R.id.bracelet_id);
 //    	braceletId.setText("");
 //    	thisMass = (TextView) findViewById(R.id.mass);
 //    	thisMass.setText("");
 //		if(mInputStream == null) {
 //			inputStreamText.setText("Input stream is NULL");
 //			Log.d(TAG, "Input stream is NULL");
 //		} else {
 //			inputStreamText.setText("Input stream is not null");
 //			Log.d(TAG, "Input stream is not null");
 //		}
 //
 //		if(mOutputStream == null) {
 //			outputStreamText.setText("Output stream is NULL");
 //			Log.d(TAG, "Output stream is NULL");
 //		} else {
 //			outputStreamText.setText("Output stream is not null");
 //			Log.d(TAG, "Output stream is not null");
 //		}
 //
 //		if(mAccessory == null) {
 //			accessoryText.setText("USB Accessory is NULL");
 //			Log.d(TAG, "USB Accessory is NULL");
 //		} else {
 //			accessoryText.setText("USB Accessory is not null");
 //			Log.d(TAG, "USB Accessory is not null");
 //		}
 //
 //		if(mFileDescriptor == null) {
 //			fileDescText.setText("File Descriptor is NULL");
 //			Log.d(TAG, "File Descriptor is NULL");
 //		} else {
 //			fileDescText.setText("File Descriptor is not null");
 //			Log.d(TAG, "File Descriptor is not null");
 //		}
     	
     }
 
 
 	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 			if (ACTION_USB_PERMISSION.equals(action)) {
 				synchronized (this) {
 					UsbAccessory accessory = UsbManager.getAccessory(intent);
 					if (intent.getBooleanExtra(
 							UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
 						openAccessory(accessory);
 					} else {
 						Log.d(TAG, "permission denied for accessory "
 								+ accessory);
 					}
 					mPermissionRequestPending = false;
 				}
 			} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
 				UsbAccessory accessory = UsbManager.getAccessory(intent);
 				if (accessory != null && accessory.equals(mAccessory)) {
 					closeAccessory();
 				}
 			}
 		}
 	};
 	
 	/********* server querying ***********/
 	public void onResultsSucceeded(String[] serverResponseStrings, JSONObject serverResponseJSON) throws JSONException {
 		if(debug){
 			infoDialog.setTitle("Received from Server:");
 			infoDialog.setMessage(serverResponseJSON.toString());
 		}
 		
 		if (haveRfid == false){ 
 			Log.d(TAG, "RFID POST SUCCEEDED: ");
 			for(int i=0; i<serverResponseStrings.length; i++){ //just print everything returned as a String[] for fun
 				Log.d(TAG, "["+i+"] "+serverResponseStrings[i]);
 			}
 			JSONObject thisStudent = serverResponseJSON.getJSONObject("student");
 			
 			Log.d(TAG, "this student: ");
 			Log.d(TAG, thisStudent.toString());			
 			Log.d(TAG,"...onResultsSucceeded");
 			
 //	   		Intent i = new Intent(Registration2RfidMass_AdkServiceActivity.this,Registration3MassActivity.class); //CORRECT
 //	   		//Intent i = new Intent(Registration2RFIDActivity.this,Adk_MassActivity.class); //CORRECT
 //			Log.d(TAG,"new Intent");
 //			i.putExtra("fName", firstNameIn);
 //			i.putExtra("lName", lastNameIn);
 //			i.putExtra("studentId",serverResponseStrings[0]);
 //			i.putExtra("visitId",serverResponseStrings[1]);
 //			//i.putExtra("pword",password.getText().toString());
 //			Log.d(TAG,"startActivity...");
 //			Registration2RfidMass_AdkServiceActivity.this.startActivity(i);
 //			Log.d(TAG,"...startActivity");
 			
     		setContentView(R.layout.registration3_mass);
     		massContinueButton = (Button) findViewById(R.id.mass_continue_button);
     		massContinueButton.setOnClickListener(mMassContinueButtonListener);
     		massContinueButton.setVisibility(View.INVISIBLE);
         	((Button) findViewById(R.id.mass_continue_button)).setOnClickListener(mMassContinueButtonListener);
             ((Button) findViewById(R.id.capture_mass)).setOnClickListener(mCaptureMassButtonListener);
     
 			// send some sort of message to Arduino about collecting mass!
             sendPress('B');
 			haveRfid = true;
 		}
 		else {
 			Log.d(TAG, "MASS POST SUCCEEDED: ");
 			for(int i=0; i<serverResponseStrings.length; i++){ //just print everything returned as a String[] for fun
 				Log.d(TAG, "["+i+"] "+serverResponseStrings[i]);
 			}
 			
 			JSONObject thisStudent;
 			thisStudent = serverResponseJSON.getJSONObject("student");
 			Log.d(TAG, "this student: ");
 			Log.d(TAG, thisStudent.toString());
 			Log.d(TAG,"...onResultsSucceeded");
 			
 			sendPress('D');
 			
 	   		Intent i = new Intent(Registration2RfidMass_AdkServiceActivity.this, Registration4PhotoActivity.class);
 			Log.d(TAG,"new Intent");
 			i.putExtra("fName", firstNameIn);
 			i.putExtra("lName", lastNameIn);
 			i.putExtra("studentId",serverResponseStrings[0]);
 			i.putExtra("visitId",serverResponseStrings[1]);
 			Log.d(TAG,"startActivity...");
 			Registration2RfidMass_AdkServiceActivity.this.startActivity(i);
 			Log.d(TAG,"...startActivity");
 			
 		}
 		
 	}
 	
 	public void failedQuery(String failureReason) {
 		Log.d(TAG, "LOGIN FAILED, REASON: " + failureReason);
 		infoDialog.setTitle("Bracelet Problem");
 		infoDialog.setMessage("Your bracelet is already being used by someone today! Try using a different one.");
 		infoDialog.show();
 		needsRfid = "yes";
 		haveRfid = false;
 		onResume();
 	}
 	
 	
 	/********* buttons and fonts **********/
 	
 	   OnClickListener mCaptureMassButtonListener = new OnClickListener() {
 	        public void onClick(View v) {
 	        	Log.d(TAG,"...mScanButtonListener onClick");
 	        	
 	        	//successfulScanBlink();
 	        	//adkAct.successfulScanBlink();
 	        	
 	        	// prepare for a progress bar dialog
 				progressBar = new ProgressDialog(v.getContext());
 				progressBar.setCancelable(true);
 				progressBar.setMessage("Measuring Your Mass ...");
 				progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 				progressBar.setProgress(0);
 				progressBar.setMax(100);
 				progressBar.show();
 				//reset progress bar status
 				progressBarStatus = 0;
 				//reset currProgress
 				currProgress = 0;
 				
 				new Thread (new Runnable() {
 					public void run() {
 						while (progressBarStatus < 100) {
 						  // process some tasks
 							progressBarStatus = measureMass();
 							// your computer is too fast, sleep 1 second
 							try {
 								Thread.sleep(10);
 							} catch (InterruptedException e) {
 								e.printStackTrace();
 							}
 							// Update the progress bar
 							progressBarHandler.post(new Runnable() {
 							
 								public void run() {
 									progressBar.setProgress(progressBarStatus);
 								}
 							});
 						}
 						// ok, time is up
 						if (progressBarStatus >= 100) {
 							Log.d(TAG, "...progressBar time's up");
 							// sleep 2 seconds, so that you can see the 100%
 							try {
 								Thread.sleep(100);
 							} catch (InterruptedException e) {
 								e.printStackTrace();
 							}
 							Message msg = new Message();
 					        //String textTochange = myMass;
 					        //msg.obj = textTochange;
 					        mHandler.sendMessage(msg);
 							// close the progress bar dialog
 							progressBar.dismiss();
 							Log.d(TAG, "...progressBar.dismiss()");
 							sendPress('C'); //TAKE MASS READING NOW
 	        			}
 
 					}
 
 				}).start(); 	
 			}
 	        
 			Handler mHandler = new Handler() {
 		        @Override
 		        public void handleMessage(Message msg){
 		        	String _mass = (String)msg.obj;
 		    		//check if RFID got read
 		    		//	Log.d(TAG, "check if rfID == 'searching'...");
 		    	   	if(_mass == "measuring"){
 		    	           //EditText braceletIdResp = (EditText)findViewById(R.id.bracelet_id);
 		    	            //braceletIdResp.setText("No Bracelet Found, please try again");
 		    	   		thisMass = (EditText) findViewById(R.id.mass);
 		    	   		Log.d(TAG, "...myMass == 'measuring'");
 		    	   		thisMass.setText("No Mass Captured, please try again");
 		    	    	Log.d(TAG, "...measuredMass.setText none found");
 		    	  	}
 		        
 		    	   	//call setText here
 		        }
 			};
 	    };      
 	  
 		// progress bar simulator... will hold ADK stuff...
 		public int measureMass() {
 			//myMass = "measuring";
 			if (currProgress <= 95) {
 				currProgress++;
 				return currProgress;
 				// wait for Arduino here
 			} else {
 				return 100;
 			}
 		}
 	
     OnClickListener mMassContinueButtonListener = new OnClickListener(){
     	public void onClick(View v) {
     		
  		    task.cancel(true);
 		    //create a new async task for every time you hit login (each can only run once ever)
 		   	task = new SciGamesHttpPoster(Registration2RfidMass_AdkServiceActivity.this,"http://db.scigam.es/push/update_mass.php");
 		    //set listener
 	        task.setOnResultsListener(Registration2RfidMass_AdkServiceActivity.this);
 	        		
 			//prepare key value pairs to send
 			String[] keyVals = {"student_id", studentIdIn, "visit_id", visitIdIn, "mass", thisMass.getText().toString()}; 
 			Log.d(TAG,"keyVals passed: ");
 			Log.d(TAG, "student_id"+ studentIdIn+ "visit_id"+ visitIdIn+ "mass"+ thisMass.getText().toString());
 			
 			//create AsyncTask, then execute
 			AsyncTask<String, Void, JSONObject> serverResponse = null;
 			serverResponse = task.execute(keyVals);
 			Log.d(TAG,"...task.execute(keyVals)");
 
     	}
     };
 	
     OnClickListener mRFIDContinueButtonListener = new OnClickListener(){ 	
   	   public void onClick(View v) {
   		   
   		  Log.d(TAG,"mRFIDContinueButtonListener.onClick");
  		    task.cancel(true);
  		    //create a new async task for every time you hit login (each can only run once ever)
  		   	task = new SciGamesHttpPoster(Registration2RfidMass_AdkServiceActivity.this,"http://db.scigam.es/push/new_rfid.php");
  		    //set listener
  	        task.setOnResultsListener(Registration2RfidMass_AdkServiceActivity.this);
  	        if(debug){infoDialog.setTitle("keyValuePairsSent:");
  	        infoDialog.setMessage("student_id"+":"+studentIdIn+","+"visit_id"+":"+visitIdIn+","+"rfid"+":"+braceletId.getText().toString());
  	        infoDialog.show();}
  	        //prepare key value pairs to send
  			String[] keyVals = {"student_id", studentIdIn, "visit_id", visitIdIn, "rfid", braceletId.getText().toString()}; 
  			Log.d(TAG,"keyVals passed: ");
  			Log.d(TAG, "student_id"+ studentIdIn+ "visit_id"+ visitIdIn+ "rfid"+ braceletId.getText().toString());
  			
  			//create AsyncTask, then execute
  			AsyncTask<String, Void, JSONObject> serverResponse = null;
  			serverResponse = task.execute(keyVals);
  			Log.d(TAG,"mLogInListener server response:");
  			Log.d(TAG,serverResponse.toString());
   	   }
      };
 
     public static void setTextViewFont(Typeface tf, TextView...params) {
         for (TextView tv : params) {
             tv.setTypeface(tf);
         }
     }
 
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onBackPressed() {
 		//do nothing
 		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
         
 	}
  
 }
