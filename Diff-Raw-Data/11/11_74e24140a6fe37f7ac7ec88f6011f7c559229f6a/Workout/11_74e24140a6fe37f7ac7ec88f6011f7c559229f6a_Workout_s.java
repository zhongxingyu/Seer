 /*
  * Copyright (C) 2009 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.example.myfirstapp;
 
 import java.util.ArrayList;
 
 import com.example.DBConnection.DBOperateDAO;
 import com.example.DBConnection.ProfileDTO;
 import java.sql.Time;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import com.example.DBConnection.DBOperateDAO;
 import com.example.DBConnection.ProfileDTO;
 import com.example.DBConnection.WorkoutDTO;
 
 import android.annotation.SuppressLint;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.ComponentName;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.Handler;
 import android.os.Message;
 import android.os.SystemClock;
 import android.provider.MediaStore;
 import android.speech.tts.TextToSpeech;
 import android.speech.tts.TextToSpeech.OnInitListener;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Chronometer;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * This is the main Activity that displays the current chat session.
  */
 public class Workout extends Activity implements OnInitListener {
 	
     private TextToSpeech tts;
     private static boolean voice_on = true;
     private static final int VOICE_OFF = 9;
     private static final int VOICE_ON = 8;
     private static final int secure_connect_scan = 7;
     private static final int SKIP_WARMUP = 6;
 
 	 // Debugging
     private static final String TAG = "Workout";
     private static final boolean D = true;
 
     
     //Timer
     private Chronometer mChronometer;
     long timeWhenClicked = 0;
     long startTime = 0;
     long endTime = 0;
     boolean isChronometerRunning = false;
     CountDownTimer countdown;
 
     //TextViews
     private TextView mWorkoutType;
     private TextView mHeartRate;
     private TextView mHeartRange;
     private TextView mAdjustedHeartRange;
     
 	private DBOperateDAO operatorDao;
 
 
 	public final static String WORKOUT_TYPE = "com.example.myfirstapp.MESSAGE";
 	
     // Message types sent from the BluetoothChatService Handler
     public static final int MESSAGE_STATE_CHANGE = 1;
     public static final int MESSAGE_READ = 2;
     public static final int MESSAGE_WRITE = 3;
     public static final int MESSAGE_DEVICE_NAME = 4;
     public static final int MESSAGE_TOAST = 5;
 
     // Key names received from the BluetoothChatService Handler
     public static final String DEVICE_NAME = "device_name";
     public static final String TOAST = "toast";
 
     // Intent request codes
     private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
     private static final int REQUEST_ENABLE_BT = 3;
 
 	private static final int MY_DATA_CHECK_CODE = 0;
 
     private String mConnectedDeviceName = null;
     private ArrayAdapter<String> mConversationArrayAdapter;
     private BluetoothAdapter mBluetoothAdapter = null;
     private BluetoothChatService mChatService = null;
     
     /**Variables Used For manipulating heart range**/
     private ArrayList<Integer> heartRates = new ArrayList<Integer>();
     private ArrayList<Integer> tempHeartRates = new ArrayList<Integer>();
     public static ArrayList<Integer> avgTempHeartRates = new ArrayList<Integer>();
     public static int heart_range_low;
     public static int heart_range_high;
     public static int current_range_low;
     public static int current_range_high;
     public static int consecutive_desired_lows;
     public static int consecutive_desired_highs;
     public static int consecutive_outrange_lows;
     public static int consecutive_outrange_highs;
     public static int consecutive_inrange_lows;
     public static int consecutive_inrange_highs;
     public static int consecutive_low_threshold;
     public static int consecutive_high_threshold;
     public static boolean isWarmup = true;
     
     private NotificationManager mNM;
     private static boolean service_is_running = false;
     public static String current_range_type;
 
     private ArrayList<ProfileDTO> profiles = new ArrayList<ProfileDTO>();
     WorkoutDTO workout = new WorkoutDTO();
 
     
     
 
 	@SuppressLint("NewApi")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		Log.i(TAG, "[ACTIVITY] onCreate");
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.workout);
 		
 		// Get local Bluetooth adapter
         mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 
         // If the adapter is null, then Bluetooth is not supported
         if (mBluetoothAdapter == null) {
             Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
             finish();
             return;
         }
                 
         Intent intent = getIntent();
         String workout_type = intent.getStringExtra(StartWorkout.WORKOUT_TYPE);
         final String heartRange_type = intent.getStringExtra(StartWorkout.HEART_RANGE_TYPE);
         
         mWorkoutType = (TextView) findViewById(R.id.workout_type);
         mWorkoutType.setText(workout_type);
         
         //create the DAO class object here
 		operatorDao = new DBOperateDAO(this);
 		//open Database connection
 		operatorDao.openDatabase();
 		
 		//Get the items from view & set their initial values (if they exist)
 		final ArrayList<ProfileDTO> profiles = operatorDao.getAllProfiles();
 
 		
 		if ((profiles.size() < 1)) {
 			Intent edit_profile_intent = new Intent(this, EditProfile.class);
 			startActivity(edit_profile_intent);
 			return;
 		} else {
 	        mHeartRange = (TextView) findViewById(R.id.heart_range_value);
 	        mAdjustedHeartRange = (TextView) findViewById(R.id.adjusted_heart_range_value);
 			if (heartRange_type.equals("Light Aerobic")){
 	        	heart_range_low = Util.getLightAerobicLowHeartRate(profiles.get(0).getPersonAge());
 		        heart_range_high = Util.getLightAerobicHighHeartRate(profiles.get(0).getPersonAge());
 		        current_range_type = "Light Aerobic";
 			} else if (heartRange_type.equals("Heavy Aerobic")) {
 				heart_range_low = Util.getHeavyAerobicLowHeartRate(profiles.get(0).getPersonAge());
 		        heart_range_high = Util.getHeavyAerobicHighHeartRate(profiles.get(0).getPersonAge());
 		        current_range_type = "Heavy Aerobic";
 			} else if (heartRange_type.equals("Light Weight-Management")) {
 				heart_range_low = Util.getLightWeightManageLowHeartRate(profiles.get(0).getPersonAge());
 		        heart_range_high = Util.getLightWeightManageHighHeartRate(profiles.get(0).getPersonAge());
 		        current_range_type = "Light Weight-Management";
 	        } else {
 	        	heart_range_low = Util.getHeavyWeightManageLowHeartRate(profiles.get(0).getPersonAge());
 		        heart_range_high = Util.getHeavyWeightManageHighHeartRate(profiles.get(0).getPersonAge());
 		        current_range_type = "Heavy Weight-Management";
 	        }
 
 	        current_range_low = heart_range_low;
 	        current_range_high = heart_range_high;
 	        
 	        mHeartRange.setText(heart_range_low + " - " + heart_range_high);
 	        mAdjustedHeartRange.setText(heart_range_low + " - " + heart_range_high);
 		}
         
 
 	}
 
 	@Override
     protected void onStart() {
         Log.i(TAG, "[ACTIVITY] onStart");
         super.onStart();
         
         profiles = operatorDao.getAllProfiles();
 		if ((profiles.size() < 1)) {
 			//Intent edit_profile_intent = new Intent(this, EditProfile.class);
 			//startActivity(edit_profile_intent);
 			return;
 		}
         
         // If BT is not on, request that it be enabled.
         if (!mBluetoothAdapter.isEnabled()) {
             Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
             startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
         // Otherwise, setup the chat session
         } else {
         	Log.i(TAG, "service_is_running = " + service_is_running);
             if (mChatService == null && service_is_running==false) setupChat();
         }
     }
 
     @Override
     protected void onResume() {
         Log.i(TAG, "[ACTIVITY] onResume");
         super.onResume();
         
         // Performing this check in onResume() covers the case in which BT was
         // not enabled during onStart(), so we were paused to enable it...
         // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
         
         if (mChatService != null) {
             // Only if the state is STATE_NONE, do we know that we haven't started already
             if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
               // Start the Bluetooth chat services
               mChatService.start();
             }
         }        
     }
     
     
     @Override
     protected void onPause() {
         Log.i(TAG, "[ACTIVITY] onPause");
         super.onPause();
     }
     
     @Override
     protected void onStop() {
         Log.i(TAG, "[ACTIVITY] onStop");
         super.onStop();
     }
 
     protected void onDestroy() {
         Log.i(TAG, "[ACTIVITY] onDestroy");
         operatorDao.closeDatabase();
         super.onDestroy();
         
         // Stop the Bluetooth chat services
         if (mChatService != null) mChatService.stop();
         
         //tts.shutdown();
 
     }
     
     protected void onRestart() {
         Log.i(TAG, "[ACTIVITY] onRestart");
         super.onRestart();
     }
     
     private void setupChat() {
         Log.d(TAG, "setupChat()");
 
         // Initialize the array adapter for the conversation thread
         mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
 
         // Initialize the BluetoothChatService to perform bluetooth connections
         mChatService = new BluetoothChatService(this, mHandler);
         
     	new StringBuffer("");
         Intent serverIntent = new Intent(this, DeviceListActivity.class);
         startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
     }
     
     private final void setStatus(int resId) {
         final ActionBar actionBar = getActionBar();
         actionBar.setSubtitle(resId);
     }
 
     private final void setStatus(CharSequence subTitle) {
         final ActionBar actionBar = getActionBar();
         actionBar.setSubtitle(subTitle);
     }
 
     // The Handler that gets information back from the BluetoothChatService
     private final Handler mHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             mHeartRate = (TextView) findViewById(R.id.heart_rate);
             mChronometer = (Chronometer) findViewById(R.id.chronometer);
 
             switch (msg.what) {
             case MESSAGE_STATE_CHANGE:
                 if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                 switch (msg.arg1) {
                 case BluetoothChatService.STATE_CONNECTED:
                     setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                     mConversationArrayAdapter.clear();
                     mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                     showNotification();
                     service_is_running = true;
                     break;
                 case BluetoothChatService.STATE_CONNECTING:
                     setStatus(R.string.title_connecting);
                     mHeartRate.setText("");
                     break;
                 case BluetoothChatService.STATE_LISTEN:
                 case BluetoothChatService.STATE_NONE:
                     setStatus(R.string.title_not_connected);       
                     mHeartRate.setText("Please connect heart monitor");
                     if (service_is_running) {
                     	mNM.cancel(R.string.app_name);
                     	service_is_running = false;
                     }
                     break;
                 }
                 break;
             case MESSAGE_READ:
             	byte[] readBuf = null;
             	readBuf = (byte[]) msg.obj;  
                 String Value = Util.byteToHex(readBuf[13]);
                 int heart_rate = Integer.parseInt(Value, 16);
                 
                 //Ignore non-sensible data
                 if (heart_rate < 50 || heart_rate > 220) {
                 	break;
                 }
                 if (heart_rate < current_range_low) {
                 	mHeartRate.setTextColor(Color.parseColor("#33B5E5"));
                 } else if (heart_rate > current_range_high) {
                 	mHeartRate.setTextColor(Color.parseColor("#FF4444"));
                 } else {
                 	mHeartRate.setTextColor(Color.parseColor("#99CC00"));
                 }
                 mHeartRate.setText(String.valueOf(heart_rate));
                 
                 if (isWarmup) {
                 	break;
                 }
                 
                 //Add the heart rate to an array to be used for calculating average heart rate
                 heartRates.add(heart_rate);
                 tempHeartRates.add(heart_rate);
                 
                 //Calculate average of last 15 heart rates
                 int avg = 0;
                 if (tempHeartRates.size() == 15) {
                 	avg = getAverage(tempHeartRates);
                     avgTempHeartRates.add(avg);
             		tempHeartRates.clear();
                 }
                 
                 //If the users heart rate is within our current range
                 if (avg >= current_range_low && avg <= current_range_high) {
                 	consecutive_outrange_lows = 0;
             		consecutive_outrange_highs = 0;
             		//If the current range is equivalent to the desired heart range
             		//PERFECT! this is what we want
                 	if (current_range_low == heart_range_low && current_range_high == heart_range_high) {
                 		consecutive_inrange_lows = 0;
                 		consecutive_inrange_highs = 0;
                 	} 
                 	//If the heart rate is within the current range, but still lower than the desired range
                 	//increase the consecutive desired lows and consecutive in range lows
                 	else if(avg < heart_range_low) {
                 		consecutive_desired_lows += 1;
                 		consecutive_desired_highs = 0;
                 		consecutive_inrange_lows += 1;
                 		consecutive_inrange_highs = consecutive_desired_highs = consecutive_outrange_highs = consecutive_outrange_lows = 0;
                 	}
                 	//If the heart rate is within the current range, but still higher than the desired range
                 	//increase the consecutive desired highs and consecutive in range highs
                 	else if (avg > heart_range_high) {
                 		consecutive_desired_highs += 1;
                 		consecutive_inrange_highs += 1;
                 		consecutive_inrange_lows = consecutive_desired_lows = consecutive_outrange_lows = consecutive_outrange_highs = 0;
                 	}
                 	//In this case the average was within the original desired range, so set custom range to be that
             		//GREAT! we've worked back to the desired range!
                 	else {
                 		current_range_low = heart_range_low;
                 		current_range_high = heart_range_high;
                 		consecutive_desired_lows = consecutive_desired_highs = 0;
                 		consecutive_inrange_lows = consecutive_inrange_highs = 0;
                 		consecutive_outrange_lows = consecutive_outrange_highs = 0;
                 	}
                 	//If the user is continually meeting the customized range, but the range is lower than the optimal range
                 	//In this case we will slowly increment the current range until it matches the desired range
                 	if (consecutive_inrange_lows == 2) {
                 		int difference = heart_range_low - current_range_low;
                 		if (difference > 5) {
                 			current_range_low += 5;
                 			current_range_high += 5;
                 		} else {
                 			current_range_low = heart_range_low;
                 			current_range_high = heart_range_high;
                 		}
                 		consecutive_inrange_highs = consecutive_inrange_lows = 0;
                 	//If the user is continually meeting the customized range, but the range is higher than the optimal range
                 	//In this case we will slowly decrement the current range until it matches the desired range
                 	} else if (consecutive_inrange_highs == 2) {
                 		int difference = current_range_high - heart_range_high;
                 		if (difference > 5) {
                 			current_range_high -= 5;
                 			current_range_low -= 5;
                 		} else {
                 			current_range_high = heart_range_high;
                 			current_range_low = heart_range_low;
                 		}
                 		consecutive_inrange_highs = consecutive_inrange_lows = 0;
                 	} 
                 } 
                 
                 
                 
                 
                 
                 
                 //If the user has heart range greater than the current range, but the current range is lower than desired range
                 //In this case, we will simply move the current_range higher because we want to get closer to the desired range
                 else if (avg > current_range_high && current_range_high < heart_range_high) {
                 	if (avg > heart_range_high) {
                 		current_range_low = heart_range_low;
                 		current_range_high = heart_range_high;
                 	} else {
 	                	current_range_high = avg + 10;
 	                	current_range_low = avg - 10;
                 	}
                 	consecutive_desired_lows += 1;
                 	consecutive_inrange_lows = consecutive_inrange_highs = 0;
                 	consecutive_outrange_lows = consecutive_outrange_highs = 0;
                 } 
                 //If the user has heart range lower than the current range, but the current range is higher than the desired range
                 //In this case, we will simply move the current range lower, because we want to get closer to the desired range
                 else if (avg < current_range_low && current_range_low > heart_range_low && avg != 0) {
                 	if (avg < heart_range_low) {
                 		current_range_low = heart_range_low;
                 		current_range_high = heart_range_high;
                 	} else {
                 		current_range_high = avg + 10;
                 		current_range_low = avg - 10;
                 	}
                 	consecutive_desired_highs += 1;
                 	consecutive_inrange_lows = consecutive_inrange_highs = 0;
                 	consecutive_outrange_lows = consecutive_outrange_highs = 0;
                 }
                 //The user has a heart range lower than the current range, and the current range is lower than the desired range OR
                 //The user has a heart range higher than the current range, and the current range is higher than the desired range
                 else {
                 	consecutive_low_threshold = consecutive_high_threshold = 8;
                 	//If the users heart rate is lower than the current range, and lower than the desired range
                 	//We will slowly decrement the current range dependent on how far away they are from the range
                 	if (avg < current_range_low && avg != 0) {
                 		consecutive_desired_lows += 1;
                 		consecutive_outrange_lows += 1;
                 		consecutive_desired_highs = consecutive_outrange_highs = consecutive_inrange_highs = consecutive_inrange_lows = 0;
                 		if (current_range_low - avg > current_range_low * .3) {
                 			consecutive_low_threshold = 1;
                 		} else if (current_range_low - avg > current_range_low * .2) {
                 			consecutive_low_threshold = 2;
                 		} else if (current_range_low - avg > current_range_low * .15) {
                 			consecutive_low_threshold = 4;
                 		} else if (current_range_low - avg > current_range_low * .1) {
                 			consecutive_low_threshold = 6;
                 		}
                 	} 
                 	//If the users heart rate is higher than the current range, and higher than the desired range
                 	//We will slowly increment the current range depended on how far they are away from the range
                 	else if (avg > current_range_high && avg!= 0){
                 		consecutive_desired_highs += 1;
                 		consecutive_outrange_highs += 1;
                 		consecutive_desired_lows = consecutive_outrange_lows = consecutive_inrange_lows = consecutive_inrange_highs = 0;
                 		if (avg - current_range_high > current_range_high * .3) {
                 			consecutive_high_threshold = 1;
                 		} else if (avg - current_range_high> current_range_high * .2) {
                 			consecutive_high_threshold = 2;
                 		} else if (avg - current_range_high > current_range_high * .15) {
                 			consecutive_high_threshold = 4;
                 		} else if (current_range_high - avg > current_range_high * .1) {
                 			consecutive_high_threshold = 6;
                 		}
                 	}
                 	//If the user is failing to meet our customized heart rate --> need to lower it more
                 	if (consecutive_outrange_lows >= consecutive_low_threshold) {
             			current_range_low -= 5;
             			current_range_high -= 5;
                 		consecutive_outrange_highs = consecutive_outrange_lows = 0;
                 	}
                 	//If the user is failing to bring heart rate down to customized heart rate --> need to raise range
                 	if (consecutive_outrange_highs >= consecutive_high_threshold) {
             			current_range_low += 5;
             			current_range_high += 5;
             			consecutive_outrange_highs = consecutive_outrange_lows = 0;
                 	}
                 }
                 
                 
                 //If the user is continually below the desired heart range (even if they are within the current range)
                 if (consecutive_desired_lows >= 8) {
                 	if (tts != null && voice_on) tts.speak("You need to raise your heart rate to optimize your workout", TextToSpeech.QUEUE_ADD, null);
                 	consecutive_desired_lows = 0;
                 }
                 //if the user is continually above the desired heart range (even if they are within the current range)
                 if (consecutive_desired_highs >= 8 ){
                 	if (tts != null && voice_on) tts.speak("You need to lower your heart rate to optimize your workout", TextToSpeech.QUEUE_ADD, null);
                 	consecutive_desired_highs = 0;
                 }
                                 
                 if (avg <= current_range_low && avg != 0 && tts != null && voice_on == true) {
             		tts.speak("Heart Rate Too Low", TextToSpeech.QUEUE_ADD, null);
                 }
                 if (avg >= current_range_high && tts != null && voice_on == true) {
             		tts.speak("Heart Rate Too High", TextToSpeech.QUEUE_ADD, null);
                 }
                 
                 
                 
     	        mAdjustedHeartRange = (TextView) findViewById(R.id.adjusted_heart_range_value);
     	        mAdjustedHeartRange.setText(current_range_low + " - " + current_range_high);
 
                 break;
             case MESSAGE_DEVICE_NAME:
                 // save the connected device's name
                 mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                 Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
 
                 //Start the clock
                 if (!isChronometerRunning) {
         			mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenClicked);
         			timeWhenClicked = mChronometer.getBase() - SystemClock.elapsedRealtime();
         			mChronometer.start();
         			startTime = System.currentTimeMillis(); 
         			isChronometerRunning = true;
         		}
                 
         		if (tts != null && voice_on == true) {
 	        		tts.speak("Warm up Started", TextToSpeech.QUEUE_ADD, null);
         		}
                 startWarmUp();
         		
         	    Intent checkIntent = new Intent();
         	    checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
         	    startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
         	    
                 break;
             case MESSAGE_TOAST:
                 Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),Toast.LENGTH_SHORT).show();
                 break;
             }
         }
     };
 
     
 
     public int getAverage(ArrayList<Integer> heartRates) {
     	int total = 0;
         for (int hr : heartRates) {
         	total = total + hr;
         }
         int avg = total/heartRates.size();
     	return avg;
     }
 
     
     
     
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         if(D) Log.d(TAG, "onActivityResult " + resultCode);
         switch (requestCode) {
         case REQUEST_CONNECT_DEVICE_SECURE:
             // When DeviceListActivity returns with a device to connect
             if (resultCode == Activity.RESULT_OK) {
                 connectDevice(data);
             }
             break;
         case REQUEST_ENABLE_BT:
             // When the request to enable Bluetooth returns
             if (resultCode == Activity.RESULT_OK) {
                 // Bluetooth is now enabled, so set up a chat session
                 setupChat();
             } else {
                 // User did not enable Bluetooth an error occurred
                 Log.d(TAG, "BT not enabled");
                 Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                 finish();
             } 
             break;
         case MY_DATA_CHECK_CODE: // For text to speech
         	Log.i(TAG, "my data check code");
             if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
             	Log.i(TAG, "check voice data pass");
                 // success, create the TTS instance
                 tts = new TextToSpeech(this, this);
             } 
             else {
                 // missing data, install it
             	Log.i(TAG, "not check voice data pass");
                 Intent installIntent = new Intent();
                 installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                 startActivity(installIntent);
             }
             break;
         }
     }
 
     private void connectDevice(Intent data) {
         // Get the device MAC address
         String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
         // Get the BluetoothDevice object
         BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
         // Attempt to connect to the device
         mChatService.connect(device, true);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.option_menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         Intent serverIntent = null;
         switch (item.getItemId()) {
         case secure_connect_scan:
             // Launch the DeviceListActivity to see devices and do scan
             serverIntent = new Intent(this, DeviceListActivity.class);
             startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
             return true;
         case VOICE_OFF:
         	voice_on = false;
             Toast.makeText(this, R.string.voice_turned_off, Toast.LENGTH_SHORT).show();
         	return true;
         case VOICE_ON:
         	voice_on = true;
             Toast.makeText(this, R.string.voice_turned_on, Toast.LENGTH_SHORT).show();
             return true;
         case SKIP_WARMUP:
         	isWarmup = false;
         	countdown.cancel();
             mAdjustedHeartRange = (TextView) findViewById(R.id.adjusted_heart_range_value);
             mAdjustedHeartRange.setText(heart_range_low + " - " + heart_range_high);
             if (tts != null && voice_on) tts.speak("Workout Started", TextToSpeech.QUEUE_ADD, null);
         	return true;
         }
         return false;
     }
     
     /* Creates the menu items */
     public boolean onPrepareOptionsMenu(Menu menu) {
         menu.clear();
         if (voice_on == true) {
 	        menu.add(0, VOICE_OFF, 0, R.string.turn_off_voice)
 	        .setIcon(android.R.drawable.ic_lock_power_off)
 	        .setShortcut('9', 'q');
         } else {
         	menu.add(0, VOICE_ON, 0, R.string.turn_on_voice)
         	.setIcon(android.R.drawable.ic_lock_power_off)
         	.setShortcut('9', 'q');
         }
         if (!service_is_running) {
             menu.add(0, secure_connect_scan, 0, R.string.secure_connect)
             .setIcon(android.R.drawable.ic_menu_search)
             .setShortcut('9', 'q');
         }
         if (isWarmup) {
         	menu.add(0, SKIP_WARMUP, 0, R.string.skip_warmup)
         	.setIcon(android.R.drawable.ic_lock_power_off)
         	.setShortcut('9', 'q');
         }
         return true;
     }
     
 	@Override
 	public void onInit(int status) {
 		Log.i(TAG, "onInit");
         if (status == TextToSpeech.SUCCESS) {
             //Toast.makeText(Workout.this, "Text-To-Speech engine is initialized", Toast.LENGTH_LONG).show();
             if (voice_on == true) {
             	tts.speak("Warm Up Started", TextToSpeech.QUEUE_ADD, null);
             }
         }
         else if (status == TextToSpeech.ERROR) {
             Toast.makeText(Workout.this, 
                     "Error occurred while initializing Text-To-Speech engine", Toast.LENGTH_LONG).show();
         }		
 	}
    public void playMusic(View view) {
 		@SuppressWarnings("deprecation")
 		Intent intent = new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
 		startActivity(intent);
 	}	
 	
     public void saveWorkout (View view) {
     	new AlertDialog.Builder(this)
     		.setIcon(android.R.drawable.ic_dialog_alert)
     		.setTitle("Save Workout")
     		.setMessage("Are you sure you want to save and end this workout?")
     		.setPositiveButton("Yes", new DialogInterface.OnClickListener()
      {
          @Override
          public void onClick(DialogInterface dialog, int which) {
         	//If the heart monitor was never attached --> can't save any information
          	if (heartRates.size() == 0) {
          		Toast.makeText(getApplicationContext(), "No workout data to save", Toast.LENGTH_LONG).show();
          		return;
          	}
          	endTime = System.currentTimeMillis();
             
             profiles = operatorDao.getAllProfiles();
             workout.setProfileId(profiles.get(0).getPersonId());
             workout.setWorkoutDate(Calendar.getInstance().getTime());
             workout.setWorkoutStart(new Time(startTime));
             workout.setWorkoutEnd(new Time(endTime));
             workout.setWorkoutType(mWorkoutType.getText().toString().trim());
             int max = heartRates.get(0);
             int min = heartRates.get(0);
 
             for ( int i = 1; i < heartRates.size(); i++) {
                 if ( heartRates.get(i) > max) {
                 	max = heartRates.get(i);
                 }
                 if (heartRates.get(i) < min) {
                 	min = heartRates.get(i);
                 }
             }
             workout.setHighHeartRate(max);
             workout.setLowHeartRate(min);
             
             // for just now these are set to 0.0
             
             int avgHeartRate = getAverage(heartRates);
             double weight = profiles.get(0).getWeight();
             int age = profiles.get(0).getPersonAge();
             long duration = endTime - startTime;
             String gender = profiles.get(0).getGender();
             Double calories = CalculateCalories(gender, avgHeartRate, weight, age, duration); 
             workout.setBurnedCalories(calories);
             //workout.setBurnedCalories(0.0);
             workout.setTimeWithinRange(0.0);
             workout.setAverageHeartRate(avgHeartRate);
             operatorDao.CreateWorkout(workout);
             Toast.makeText(getApplicationContext(), "Workout Data Saved Successfully", Toast.LENGTH_LONG).show(); 
      		finish();
      		
          }
      })
      .setNegativeButton("No", null)
      .show();
 	}
 	
 	public void discardWorkout (View view) {
 		new AlertDialog.Builder(this)
         	.setIcon(android.R.drawable.ic_dialog_alert)
         	.setTitle("Closing Activity")
         	.setMessage("Are you sure you want to discard and end this workout?")
         	.setPositiveButton("Yes", new DialogInterface.OnClickListener()
         	{
         		@Override
         		public void onClick(DialogInterface dialog, int which) {
         			finish();    
         		}
 
         	})
         	.setNegativeButton("No", null)
         	.show();		
 	}
 	
 	public void upgradeRange (View view) {
 		if (current_range_type.equals("Light Aerobic")) {
 			//Go to Heavy Aerobic
 			heart_range_low = Util.getHeavyAerobicLowHeartRate(profiles.get(0).getPersonAge());
 	        heart_range_high = Util.getHeavyAerobicHighHeartRate(profiles.get(0).getPersonAge());
 	        current_range_type = "Heavy Aerobic";
 		} else if (current_range_type.equals("Heavy Aerobic")) {
 			//Toast that you are already at maximum range
             Toast.makeText(getApplicationContext(), "Already at maximum desired range!", Toast.LENGTH_LONG).show(); 
 			return;
 		} else if (current_range_type.equals("Light Weight-Management")) {
 			// go to heavy weight-management
         	heart_range_low = Util.getHeavyWeightManageLowHeartRate(profiles.get(0).getPersonAge());
 	        heart_range_high = Util.getHeavyWeightManageHighHeartRate(profiles.get(0).getPersonAge());
 	        current_range_type = "Heavy Weight-Management";
 		} else if (current_range_type.equals("Heavy Weight-Management")) {
 			// go to light aerobic
         	heart_range_low = Util.getLightAerobicLowHeartRate(profiles.get(0).getPersonAge());
 	        heart_range_high = Util.getLightAerobicHighHeartRate(profiles.get(0).getPersonAge());
 	        current_range_type = "Light Aerobic";
 		} else {
             Toast.makeText(getApplicationContext(), "Error trying to upgrade range.", Toast.LENGTH_LONG).show(); 
 		}
 		//reset all consecutive counters
 		consecutive_desired_lows = consecutive_desired_highs = consecutive_inrange_lows = consecutive_inrange_highs = consecutive_desired_highs = consecutive_outrange_highs = consecutive_outrange_lows = 0;
        
 		//Change the display
 		mHeartRange = (TextView) findViewById(R.id.heart_range_value);
         mHeartRange.setText(heart_range_low + " - " + heart_range_high);
         
 		if (tts != null && voice_on) tts.speak("Desired range upgraded to " + current_range_type, TextToSpeech.QUEUE_ADD, null);
 
 		
 	}
 	
 	public void downgradeRange (View view) {
 		if (current_range_type.equals("Light Aerobic")) {
 			// go to heavy weight-management
         	heart_range_low = Util.getHeavyWeightManageLowHeartRate(profiles.get(0).getPersonAge());
 	        heart_range_high = Util.getHeavyWeightManageHighHeartRate(profiles.get(0).getPersonAge());
 	        current_range_type = "Heavy Weight-Management";
 		} else if (current_range_type.equals("Heavy Aerobic")) {
 			// go to light aerobic
         	heart_range_low = Util.getLightAerobicLowHeartRate(profiles.get(0).getPersonAge());
 	        heart_range_high = Util.getLightAerobicHighHeartRate(profiles.get(0).getPersonAge());
 	        current_range_type = "Light Aerobic";
 		} else if (current_range_type.equals("Light Weight-Management")) {
 			//Toast that you are already at minimum range
             Toast.makeText(getApplicationContext(), "Already at minimum desired range!", Toast.LENGTH_LONG).show(); 
         	return;
 		} else if (current_range_type.equals("Heavy Weight-Management")) {
 			// to to light weight-management
 			heart_range_low = Util.getLightWeightManageLowHeartRate(profiles.get(0).getPersonAge());
 	        heart_range_high = Util.getLightWeightManageHighHeartRate(profiles.get(0).getPersonAge());
 	        current_range_type = "Light Weight-Management";
 		}
 		// reset all consecutive counters
 		consecutive_desired_lows = consecutive_desired_highs = consecutive_inrange_lows = consecutive_inrange_highs = consecutive_desired_highs = consecutive_outrange_highs = consecutive_outrange_lows = 0;
 		
 		//change the display
         mHeartRange = (TextView) findViewById(R.id.heart_range_value);
         mHeartRange.setText(heart_range_low + " - " + heart_range_high);
         
 		if (tts != null && voice_on) tts.speak("Desired range downgraded to " + current_range_type, TextToSpeech.QUEUE_ADD, null);
 
 	}
 	
   	/**
      * Show a notification while this service is running.
      */
     @SuppressWarnings("deprecation")
 	private void showNotification() {
         CharSequence text = getText(R.string.app_name);
         Notification notification = new Notification(R.drawable.heart1, null, System.currentTimeMillis());
         notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
         Intent heartrateIntent = new Intent();
         heartrateIntent.setComponent(new ComponentName(this, Workout.class));
         heartrateIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         PendingIntent contentIntent = PendingIntent.getActivity(this, 0, heartrateIntent, 0);
         notification.setLatestEventInfo(this, text, getText(R.string.notification_subtitle), contentIntent);
 
         mNM.notify(R.string.app_name, notification);
     }
     
     @Override
     public void onBackPressed() {
         new AlertDialog.Builder(this)
             .setIcon(android.R.drawable.ic_dialog_alert)
             .setTitle("Closing Activity")
             .setMessage("Are you sure you want to close this activity?")
             .setPositiveButton("Yes", new DialogInterface.OnClickListener()
         {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 finish();    
             }
 
         })
         .setNegativeButton("No", null)
         .show();
     }
     
 
 	private Double CalculateCalories(String gender, int avgHeartRate, double weight, int age, long duration) {
 		if (gender == "m") {
 			Log.i("gender = ",  gender);
 			Log.i("avgHeartRate = " , "" + avgHeartRate);
 			Log.i("weight = ", "" + weight);
 			Log.i("age = " , "" + age);
 			Log.i("duration = " , "" + duration);
 			Log.i("hour duration = " , "" + duration/(60*1000*60));
 			return ((-55.0969 + (.6309 * avgHeartRate) + (0.1988 * (0.453592 * weight)) + (0.2017 * age)) / (4.184)) * 60 * (duration/1000*60*60);
 		} else {
 			Log.i("gender = ",  gender);
 			Log.i("avgHeartRate = " , "" + avgHeartRate);
 			Log.i("weight = ", "" + weight);
 			Log.i("age = " , "" + age);
 			Log.i("duration = " , "" + duration);
 			Log.i("hour duration = " , "" + duration/(60*1000*60));
 			return ((-20.4022 + (0.4472 * avgHeartRate) - (0.1263 * (0.453592 * weight)) + (0.074 * age))/ 4.184) * 60 * (duration/1000*60*60);
 		}
 	}
 	
 	private void startWarmUp() {
 		isWarmup = true;
         mAdjustedHeartRange = (TextView) findViewById(R.id.adjusted_heart_range_value);
 		countdown = new CountDownTimer(300000, 1000) {
 		     public void onTick(long millisUntilFinished) {
 		    	 int minutes = (int) (millisUntilFinished / (1000 * 60));
 		    	 int seconds = (int) ((millisUntilFinished/1000) - (60 * minutes));
 		    	 String adjustedSeconds = String.valueOf(seconds);
 		    	 if (seconds < 10) {
 		    		 adjustedSeconds = "0" + seconds;
 		    	 }
 		         mAdjustedHeartRange.setText("Warmup: " + minutes + ":" + adjustedSeconds);
 		     }
 
 		     public void onFinish() {
 		         mAdjustedHeartRange.setText(heart_range_low + " - " + heart_range_high);
              	 if (tts != null && voice_on) tts.speak("Workout Started", TextToSpeech.QUEUE_ADD, null);
 		         isWarmup = false;
 		     }
 		  }.start();
 	}
 
 }
