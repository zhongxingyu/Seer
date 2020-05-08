 /**
  * 
  */
 package com.genymobile.sommeil;
 
 import java.io.FileDescriptor;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Typeface;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.ParcelFileDescriptor;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 import android.widget.SeekBar;
 import android.widget.TextView;
 
 import com.android.future.usb.UsbAccessory;
 import com.android.future.usb.UsbManager;
 
 /**
  * @author M-F.P
  *
  */
 public class StartSleepActivity extends Activity implements Runnable {
 private static final String TAG = "LightActivity";
     
     private static final String ACTION_USB_PERMISSION = "com.google.android.DemoKit.action.USB_PERMISSION";
 
     private UsbManager mUsbManager;
     private PendingIntent mPermissionIntent;
     private boolean mPermissionRequestPending;
 
     UsbAccessory mAccessory;
     ParcelFileDescriptor mFileDescriptor;
     FileInputStream mInputStream;
     FileOutputStream mOutputStream;
     Thread thread;
     
     SleepRecorder mSleepRecorder;
 
     private static final byte LIGHT_COMMAND = 'L';
 
     private static final int MESSAGE_LIGHT = 3;
 
     private static final int LIGHT_OFF = 0;
     private static final int LIGHT_MAX = 255;
 
     private static final byte LIGHT_ONE = 0;
     private static final byte LIGHT_TWO = 1;
 
     protected class LightMsg {
         private int light;
 
         public LightMsg(int light) {
             this.light = light;
         }
 
         public int getLight() {
             return light;
         }
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
                 finish();
             }
         }
     };
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         mSleepRecorder = new SleepRecorder(this, "TEST");
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
         
         Typeface font = Typeface.createFromAsset(getAssets(), "SueEllenFrancisco.ttf");
 		
         setContentView(R.layout.activity_sleeping);
         TextView title = (TextView) findViewById(R.id.sleepillow);
         TextView textAlarm = (TextView) findViewById(R.id.textAlarme);
         TextView currentTime = (TextView) findViewById(R.id.currentTime);
         currentTime.setTypeface(font);
         title.setTypeface(font);
         textAlarm.setTypeface(font);
         
         ImageButton dashBtn = (ImageButton) findViewById(R.id.dashBtn);
         dashBtn.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(StartSleepActivity.this, DashBoardActivity.class);
 				startActivity(intent);
 			}
 		});
         
 		enableControls(false);
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
 	public void onResume() {
 		super.onResume();
 
 		Intent intent = getIntent();
 		if (mInputStream != null && mOutputStream != null) {
 			return;
 		}
 
 		UsbAccessory[] accessories = mUsbManager.getAccessoryList();
 		UsbAccessory accessory = (accessories == null ? null : accessories[0]);
 		if (accessory != null) {
 			if (mUsbManager.hasPermission(accessory)) {
 				openAccessory(accessory);
 				openLightGradient();
 			} else {
 				synchronized (mUsbReceiver) {
 					if (!mPermissionRequestPending) {
 						mUsbManager.requestPermission(accessory,
 								mPermissionIntent);
 						mPermissionRequestPending = true;
 					}
 				}
 			}
 		} else {
 			Log.d(TAG, "mAccessory is null");
 		}
 	}
 	
 	private void openLightGradient() {
 		
 		new AsyncTask<Byte, Integer, Void>() {
 			
 			Byte mCommand;
 			
 			@Override
 			protected Void doInBackground(Byte... command) {
 				
 				mCommand = command[0];
 				
 				int progress;
 				for (progress=0;progress<=100;progress++)
 				{
 					try {
 						Thread.sleep(100);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 					publishProgress(progress);
 					progress++;				
 				}
 				return null;
 			}
 			
 			/* (non-Javadoc)
 			 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
 			 */
 			@Override
 			protected void onProgressUpdate(Integer... progress) {
 				// TODO Auto-generated method stub
 				sendCommand(mCommand, LIGHT_TWO, LIGHT_MAX * progress[0] / 100);
 				super.onProgressUpdate(progress);
 			}
 		}.execute(LIGHT_COMMAND);
 	}
 	
 	@Override
     public void onPause() {
         super.onPause();
         closeAccessory();
     }
 
     @Override
     public void onDestroy() {
         unregisterReceiver(mUsbReceiver);
         super.onDestroy();
     }
 
     private void openAccessory(UsbAccessory accessory) {
         mFileDescriptor = mUsbManager.openAccessory(accessory);
         if (mFileDescriptor != null) {
             mAccessory = accessory;
             FileDescriptor fd = mFileDescriptor.getFileDescriptor();
             mInputStream = new FileInputStream(fd);
             mOutputStream = new FileOutputStream(fd);
             thread = new Thread(null, this, "LightActivity");
             thread.start();
             Log.d(TAG, "accessory opened");
             enableControls(true);
         } else {
             Log.d(TAG, "accessory open fail");
         }
     }
 
     private void closeAccessory() {
         if (thread != null) {
             thread.interrupt();
             thread = null;
         }
         enableControls(false);
 
         try {
             if (mFileDescriptor != null) {
                 mFileDescriptor.close();
             }
         } catch (IOException e) {
         } finally {
             mFileDescriptor = null;
             mAccessory = null;
             mInputStream = null;
             mOutputStream = null;
         }
     }
 
     protected void enableControls(boolean enable) {
     }
 
     private int composeInt(byte hi, byte lo) {
         int val = (int) hi & 0xff;
         val *= 256;
         val += (int) lo & 0xff;
         return val;
     }
 
     private void manageMessage(byte[] buffer) {
 
         Log.d("LIGHT", "RECEIVED: " + buffer);
         for (int i = 0; i < buffer.length; ++i) {
            int realvalue = buffer[i] & 0xFF;
             Log.d("LIGHT", "rec " + realvalue);
             mSleepRecorder.addValue(realvalue);
         }
     }
 
     /*
      * R�ception des commandes (non-Javadoc)
      * @see java.lang.Runnable#run()
      */
     public void run() {
 
         int read = 0;
         int count = 0;
         int bufSize = 1024;
         byte[] buffer = new byte[bufSize];
         byte[] token;
 
         while (true) {
 
             try {
                 Log.d(TAG, "read " + read + "size " + buffer.length);
                 read = mInputStream.read(buffer, count, bufSize - count);
                 count += read;
 
                 for (int i = 0; i < count; i++) {
 
                     if (buffer[i] == 'M') {
                         Log.d(TAG, "I found M");
 
                         int length = buffer[++i];
                         if (length > 0) {
                             Log.d(TAG, "length " + length);
 
                             token = new byte[length];
                             for (int j = 0; i < count && j < length;) {
                                 token[j++] = buffer[++i];
                             }
                             manageMessage(token);
                         }
                     }
                 }
 
             } catch (IOException e) {
                 break;
             }
         }
     }
 
     Handler mHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             switch (msg.what) {
 
                 case MESSAGE_LIGHT:
                     LightMsg l = (LightMsg) msg.obj;
                     handleLightMessage(l);
                     break;
             }
         }
     };
 
     public void sendCommand(byte command, byte target, int value) {
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
 
     protected void handleLightMessage(LightMsg l) {
     }
 
     public void onStartTrackingTouch(SeekBar seekBar) {
     }
 
     public void onStopTrackingTouch(SeekBar seekBar) {
     }
 
     private void initRecordSleep(String name) {
 
     }
 
     private void recordSleep(int value) {
 
     }
 }
