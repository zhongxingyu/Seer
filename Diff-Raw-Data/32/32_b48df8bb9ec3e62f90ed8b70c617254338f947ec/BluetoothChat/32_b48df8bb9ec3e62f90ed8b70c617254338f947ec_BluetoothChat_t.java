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
 
 package com.example.android.BluetoothChat;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import android.annotation.SuppressLint;
 import android.annotation.TargetApi;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.hardware.Camera;
 import android.hardware.Camera.Parameters;
 import android.hardware.Camera.Size;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Toast;
 
 /**
  * This is the main Activity that displays the current chat session.
  */
 @SuppressLint("HandlerLeak")
 @TargetApi(11)
 public class BluetoothChat extends Activity {
     // Debugging
     private static final String TAG = "BluetoothChat";
     private static final boolean D = true;
 
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
     private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
     private static final int REQUEST_ENABLE_BT = 3;
 
     // Layout Views
     private Button mSendButton;
     private SurfaceView mPreviewSurfaceView;
     private SurfaceView mReceiveSurfaceView;
     
 	// Name of the connected device
     private String mConnectedDeviceName = null;
     // Local Bluetooth adapter
     private BluetoothAdapter mBluetoothAdapter = null;
     // Member object for the chat services
     private BluetoothChatService mChatService = null;
 
     
     // Camera
 	private Camera mCamera = null;
 	private SurfaceHolder.Callback mSurfaceholderCallBack;
 	private Bitmap mCameraImage;
 	
 	private final int IMAGE_WIDTH = 100;
 	private final int IMAGE_HEIGHT = 100;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         if(D) Log.e(TAG, "+++ ON CREATE +++");
 
         // Set up the window layout
         setContentView(R.layout.main);
 
         // Get local Bluetooth adapter
         mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 
         // If the adapter is null, then Bluetooth is not supported
         if (mBluetoothAdapter == null) {
             Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
             finish();
             return;
         }
     }
 
     @Override
     public void onStart() {
         super.onStart();
         if(D) Log.e(TAG, "++ ON START ++");
 
         // If BT is not on, request that it be enabled.
         // setupChat() will then be called during onActivityResult
         if (!mBluetoothAdapter.isEnabled()) {
             Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
             startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
         // Otherwise, setup the chat session
         } else {
             if (mChatService == null) setupChat();
         }
     }
 
     @Override
     public synchronized void onResume() {
         super.onResume();
         if(D) Log.e(TAG, "+ ON RESUME +");
 
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
 
     private void setupChat() {
         Log.d(TAG, "setupChat()");
 
         mPreviewSurfaceView = (SurfaceView) findViewById(R.id.previewSurfaceView);
         mReceiveSurfaceView = (SurfaceView) findViewById(R.id.receivedSurfaceView);
         
         try {
         	mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
         }
         catch (Exception e) {
         	mCamera = Camera.open(0);
         }
         
         
         mSurfaceholderCallBack = new SurfaceHolder.Callback() {
 			@Override
 			public void surfaceDestroyed(SurfaceHolder holder) {
 				// not used
 			}
 			
 			@Override
 			public void surfaceCreated(SurfaceHolder holder) {
 				try {
 					Parameters cameraParameters = mCamera.getParameters();
 					List<Camera.Size> sizes = cameraParameters.getSupportedPreviewSizes(); 
 					
 		    		mCamera.setParameters(cameraParameters);
 					mCamera.setPreviewDisplay(mPreviewSurfaceView.getHolder());
 					mCamera.setDisplayOrientation(90);
 					mCamera.startPreview();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 					
 					// Unable to get the camera initialized properly, won't be able to take pictures
 				}
 			}
 			
 			@Override
 			public void surfaceChanged(SurfaceHolder holder, int format, int width,	int height) {
 				// not used
 			}
 		};
 		
 		mPreviewSurfaceView.getHolder().addCallback(mSurfaceholderCallBack);
         
         // Initialize the send button with a listener that for click events
         mSendButton = (Button) findViewById(R.id.button_send);
         mSendButton.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {                           	
                    	
             	
             	            	
         		mCamera.takePicture(null, null, new Camera.PictureCallback() {
         			@Override
         			public void onPictureTaken(byte[] data, Camera camera) {
         				// decode the data obtained by the camera into a Bitmap
         				mCameraImage = BitmapFactory.decodeByteArray(data, 0, data.length);
         				mCameraImage = Bitmap.createScaledBitmap(mCameraImage, IMAGE_WIDTH, IMAGE_HEIGHT, false);
         				
         				// send the image
         				ByteArrayOutputStream baos = new ByteArrayOutputStream();
         				mCameraImage.compress(CompressFormat.JPEG, 100, baos);        				
         				
         				sendData(baos.toByteArray());
         				
         				// restart the preview
 						mCamera.startPreview();     				
         			}
         		});
             }
         });
 
         // Initialize the BluetoothChatService to perform bluetooth connections
         mChatService = new BluetoothChatService(this, mHandler);        
     }
 
     @Override
     public synchronized void onPause() {
         super.onPause();
         if(D) Log.e(TAG, "- ON PAUSE -");
     }
 
     @Override
     public void onStop() {
         super.onStop();
         if(D) Log.e(TAG, "-- ON STOP --");
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         // Stop the Bluetooth chat services
         if (mChatService != null) mChatService.stop();
         mCamera.release();
         if(D) Log.e(TAG, "--- ON DESTROY ---");
     }
 
     private void ensureDiscoverable() {
         if(D) Log.d(TAG, "ensure discoverable");
         if (mBluetoothAdapter.getScanMode() !=
             BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
             Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
             discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
             startActivity(discoverableIntent);
         }
     }
 
     private void sendData(byte[] data) {
         // Check that we're actually connected before trying anything
         if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
             Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
             return;
         }
 
         // Check that there's actually something to send
         if (data.length > 0) {
             // Get the message bytes and tell the BluetoothChatService to write
             mChatService.write(data);
             
             
         }
     }
 
     private final void setStatus(int resId) {
         final ActionBar actionBar = getActionBar();
         if (actionBar != null) {
         	actionBar.setSubtitle(resId);
         }
     }
 
     private final void setStatus(CharSequence subTitle) {
         final ActionBar actionBar = getActionBar();
         actionBar.setSubtitle(subTitle);
     }
 
     // The Handler that gets information back from the BluetoothChatService
 	private final Handler mHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             switch (msg.what) {
             case MESSAGE_STATE_CHANGE:
                 if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                 switch (msg.arg1) {
                 case BluetoothChatService.STATE_CONNECTED:
                     setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                     break;
                 case BluetoothChatService.STATE_CONNECTING:
                     setStatus(R.string.title_connecting);
                     break;
                 case BluetoothChatService.STATE_LISTEN:
                 case BluetoothChatService.STATE_NONE:
                     setStatus(R.string.title_not_connected);
                     break;
                 }
                 break;
             case MESSAGE_WRITE:           	
             	
                 break;
             case MESSAGE_READ:
                 byte[] readBuf = (byte[]) msg.obj;
                 // construct a string from the valid bytes in the buffer
                 Bitmap receivedImage = BitmapFactory.decodeByteArray(readBuf, 0, msg.arg1);
                 
                if (receivedImage != null)
                {
	                SurfaceHolder surfaceHolder = mReceiveSurfaceView.getHolder();
	                Canvas canvas = surfaceHolder.lockCanvas();
	                canvas.drawBitmap(receivedImage, 0.0f, 0.0f, new Paint());
	                surfaceHolder.unlockCanvasAndPost(canvas);
                }
                 
                 break;
             case MESSAGE_DEVICE_NAME:
                 // save the connected device's name
                 mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                 Toast.makeText(getApplicationContext(), "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                 break;
             case MESSAGE_TOAST:
                 Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                                Toast.LENGTH_SHORT).show();
                 break;
             }
         }
     };
 
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         if(D) Log.d(TAG, "onActivityResult " + resultCode);
         switch (requestCode) {
         case REQUEST_CONNECT_DEVICE_SECURE:
             // When DeviceListActivity returns with a device to connect
             if (resultCode == Activity.RESULT_OK) {
                 connectDevice(data, true);
             }
             break;
         case REQUEST_CONNECT_DEVICE_INSECURE:
             // When DeviceListActivity returns with a device to connect
             if (resultCode == Activity.RESULT_OK) {
                 connectDevice(data, false);
             }
             break;
         case REQUEST_ENABLE_BT:
             // When the request to enable Bluetooth returns
             if (resultCode == Activity.RESULT_OK) {
                 // Bluetooth is now enabled, so set up a chat session
                 setupChat();
             } else {
                 // User did not enable Bluetooth or an error occurred
                 Log.d(TAG, "BT not enabled");
                 Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                 finish();
             }
         }
     }
 
     private void connectDevice(Intent data, boolean secure) {
         // Get the device MAC address
         String address = data.getExtras()
             .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
         // Get the BluetoothDevice object
         BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
         // Attempt to connect to the device
         mChatService.connect(device, secure);
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
         case R.id.secure_connect_scan:
             // Launch the DeviceListActivity to see devices and do scan
             serverIntent = new Intent(this, DeviceListActivity.class);
             startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
             return true;
         case R.id.insecure_connect_scan:
             // Launch the DeviceListActivity to see devices and do scan
             serverIntent = new Intent(this, DeviceListActivity.class);
             startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
             return true;
         case R.id.discoverable:
             // Ensure this device is discoverable by others
             ensureDiscoverable();
             return true;
         }
         return false;
     }
 
 }
