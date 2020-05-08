 package com.blushutter.camera;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager;
 import android.hardware.Camera;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 public class MainActivity extends Activity {
 
     // constants
     public static final String LOG_TAG = "com.blushutter.camera";
     public static final int NOT_SET = -1;
 
     private static final int REQUEST_CONNECT_DEVICE = 1;
     private static final int REQUEST_ENABLE_BT = 2;
     private static final String SELECTED_SOUND_ON = "SoundOn";
     private static final String SELECTED_CAMERA_ID_KEY = "mSelectedCameraId";
     private static final String SELECTED_PICTURE_SIZE = "SelectedPictureSizeIndex";
     private static final String SELECTED_BLUETOOTH_ID_KEY = "mSelectedBluetoothId";
     private static final String CAMERA_SIZE_DISPLAY_FORMAT = "%d x %d";
 
     // Message types sent from the BluetoothChatService Handler
     public static final int MESSAGE_STATE_CHANGE = 1;
     public static final int MESSAGE_READ = 2;
     public static final int MESSAGE_WRITE = 3;
     public static final int MESSAGE_DEVICE_NAME = 4;
     public static final int MESSAGE_TOAST = 5;
     // Key names received from the BluetoothCommandService Handler
     public static final String DEVICE_NAME = "device_name";
     public static final String TOAST = "toast";
 
 
     // private variables
     private final Context mContext = this;
     private static Toast mToastText;
     private Camera mSelectedCamera;
     private List<String> mBluetoothList = new ArrayList<String>();
     private int mSelectedCameraId = NOT_SET;
     private boolean mIsFocusing = false;
     private String[] mZoomLevels = null;
     private String mSelectedBluetoothId = null;
     private BluetoothAdapter mBluetoothAdapter = null;
     private BluetoothCommandService mCommandService = null;
     private Boolean mConnectionIsOpen = false;
     private ImageButton mImageButton;
     private ToggleButton mToggleButton;
 
     // public variables
     public Camera.Parameters CameraParameters = null;
     public Camera.Size SelectedPictureSize = null;
     public List<Camera.Size> SupportedPictureSizes = null;
     public int SelectedPictureSizeIndex = NOT_SET;
     public static Boolean SavePhotos = false;
     public static Boolean SoundOn = true;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         try {
 
             setVolumeControlStream(AudioManager.STREAM_SYSTEM);
 
             LoadPreferences();
 
             // create class-wide toast instance
             mToastText = Toast.makeText(this, "", Toast.LENGTH_SHORT);
 
 
             if (savedInstanceState == null) {
                 setContentView(R.layout.activity_main);
 
                 boolean hasCamera = checkCameras(this);
 
                 if (!hasCamera) {
                     showNoCameraDialog();
                 }
                 else {
                     setupButtons();
                     setupCamera();
                     setupBluetooth();
                 }
             }
 
             // pre-load sounds
             SoundManager.getSingleton().preload(this);
 
         }
         catch (Exception e) {
             Log.e(LOG_TAG, "Error in onCreate: " + e.getMessage());
         }
     }
 
     private void setupButtons() {
 
         // btnSound
         mToggleButton = (ToggleButton) findViewById(R.id.btnSound);
 
         mToggleButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 SoundOn = ((ToggleButton) v).isChecked();
                 //SoundManager.getSingleton().preload(v.getContext());
                 //SoundManager.getSingleton().SoundOn = soundOn;
                 if (SoundOn) {
                     displayText("Sound is On");
                 }
                 else {
                     displayText("Sound is Off");
                 }
             }
         });
 
         // btnSavePhoto
         mToggleButton = (ToggleButton) findViewById(R.id.btnSavePhoto);
 
         mToggleButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 SavePhotos = ((ToggleButton) v).isChecked();
                 if (SavePhotos) {
                     displayText("Save to Camera is On");
                 }
                 else {
                     displayText("Save to Camera is Off");
                 }
             }
         });
 
         // btnResolution
         mImageButton = (ImageButton) findViewById(R.id.btnResolution);
 
         mImageButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 btnResolutionClicked();
             }
         });
 
         // btnBluetoothDevices
         mImageButton = (ImageButton) findViewById(R.id.btnBluetoothDevices);
 
         mImageButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 showBluetoothDeviceSelection();
             }
         });
     }
 
     boolean checkCameras(Context context) {
 
         boolean hasCamera = false;
 
         try {
             PackageManager packageManager = context.getPackageManager();
             if (packageManager!=null)
                 hasCamera = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA);
         }
         catch (Exception e) {
             Log.e(LOG_TAG, "Error in checkCameras: " + e.getMessage());
         }
 
         return hasCamera;
     }
 
     private void setupCamera() {
 
         if (mSelectedCamera != null)
             return;
 
         try {
             mSelectedCameraId = CameraHelper.getFacingCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);
         }
         catch (Exception e) {
             Log.e(LOG_TAG, "Error in setupCamera: " + e.getMessage());
         }
     }
 
     private void setupBluetooth() {
 
         try {
             // Get local Bluetooth adapter
             mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 
             if (mBluetoothAdapter == null) {
                 displayText("Bluetooth is not available");
                 finish();
             }
             else {
 
                 if (!mBluetoothAdapter.isEnabled()) {
                     Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                     startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                 }
 
                 //ensureDiscoverable();
                 mBluetoothList.clear();
 
                 Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                 if (pairedDevices!=null) {
                     // If there are paired devices
                     if (pairedDevices.size() > 0) {
                         // Loop through paired devices
                         for (BluetoothDevice device : pairedDevices) {
                             // Add the name and address to an array adapter to show in a ListView
                             mBluetoothList.add(device.getName() + "\n" + device.getAddress());
                         }
                     }
                 }
 
                 if (mSelectedBluetoothId == null) {
                     showBluetoothDeviceSelection();
                 }
                 else {
 
                     if (mCommandService != null) {
                         connectToDevice();
                     }
                     else {
                         setupCommand();
                     }
 
                     displayText("Connecting to " + mSelectedBluetoothId + "...");
 
                 }
             }
         }
         catch (Exception e) {
             Log.e(LOG_TAG, "Error in setupBluetooth: " + e.getMessage());
         }
     }
 
     private void showBluetoothDeviceSelection() {
         CharSequence[] items = mBluetoothList.toArray(new CharSequence[mBluetoothList.size()]);
 
         AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
         builder.setTitle(mContext.getString(R.string.bluetoothDialog));
         builder.setSingleChoiceItems(items, -1,
                 new DialogInterface.OnClickListener() {
                     // indexSelected contains the index of item (of which checkbox checked)
                     @Override
                     public void onClick(DialogInterface dialog, int indexSelected) {
                         //widgets.get(indexSelected).setHidden(!isChecked);
                         mSelectedBluetoothId = mBluetoothList.get(indexSelected)
                                 .substring(mBluetoothList.get(indexSelected).length() - 17);
                     }
                 })
                 // Set the action buttons
                 .setPositiveButton(R.string.buttonOk, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int id) {
 
                         //Do nothing here. We override the onclick
                     }
                 });
 
         // create alert dialog
         final AlertDialog alertDialog = builder.create();
 
         alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
 
             @Override
             public void onShow(DialogInterface dialog) {
 
                 Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                 if (b!=null) {
                     b.setOnClickListener(new View.OnClickListener() {
 
                         @Override
                         public void onClick(View view) {
 
                             if (mSelectedBluetoothId == null)
                                 return;
 
                             setupCommand();
 
                             displayText("Connecting to " + mSelectedBluetoothId + "...");
 
                             //Dismiss once everything is OK.
                             alertDialog.dismiss();
                         }
                     });
                 }
             }
         });
 
         // show it
         alertDialog.show();
     }
 
     private void setupCommand() {
         try {
             // Initialize the BluetoothChatService to perform bluetooth connections
             mCommandService = new BluetoothCommandService(this, mHandler);
         }
         catch (Exception e) {
             Log.e(LOG_TAG, "Error in setupCommand: " + e.getMessage());
         }
 
         connectToDevice();
     }
 
     private void connectToDevice() {
         try {
             // Get the BLuetoothDevice object
             BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mSelectedBluetoothId);
             // Attempt to connect to the device
             mCommandService.connect(device);
         }
         catch (Exception e) {
             Log.e(LOG_TAG, "Error in connectToDevice: " + e.getMessage());
         }
     }
 
     // not using this method at all
     // but maybe in the future??
 //    private void ensureDiscoverable() {
 //        if (mBluetoothAdapter.getScanMode() !=
 //                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
 //            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
 //            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
 //            startActivity(discoverableIntent);
 //        }
 //    }
 
     private void takePicture() {
         try {
 
             if (mConnectionIsOpen) {
                 // play sound
                 if (SoundOn)
                     SoundManager.getSingleton().play(SoundManager.SOUND_SHUTTER);
 
                 // take picture
                 mSelectedCamera.takePicture(null, null, new HandlePictureStorage(this, mCommandService));
             }
             else {
                 AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
                 myAlertDialog.setTitle(getString(R.string.bluetoothDialogTitle));
                 myAlertDialog.setMessage(getString(R.string.bluetoothDialogMessage));
                 myAlertDialog.setPositiveButton(getString(R.string.buttonYes), new DialogInterface.OnClickListener() {
 
                     public void onClick(DialogInterface arg0, int arg1) {
                         setupBluetooth();
                     }});
 
                 myAlertDialog.setNegativeButton(getString(R.string.buttonNo), new DialogInterface.OnClickListener() {
 
                     public void onClick(DialogInterface arg0, int arg1) {
                         // nothing to do here
                     }});
 
                 myAlertDialog.show();
             }
         }
         catch (Exception e) {
             Log.e(LOG_TAG, "Error in takePicture: " + e.getMessage());
         }
     }
 
     private final Handler mHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
 
             System.out.println("In handler");
 
             try {
                 switch (msg.what) {
                     case MESSAGE_STATE_CHANGE:
                         switch (msg.arg1) {
                             case BluetoothCommandService.STATE_CONNECTED:
                                 mConnectionIsOpen = true;
                                 //mTitle.setText(R.string.title_connected_to);
                                 //mTitle.append(mConnectedDeviceName);
                                 System.out.println("State Connected");
 
                                 break;
                             case BluetoothCommandService.STATE_CONNECTING:
                                 //mTitle.setText(R.string.title_connecting);
                                 System.out.println("State Connecting");
 
                                 break;
                             case BluetoothCommandService.STATE_LISTEN:
                                 //TODO: will be implemented for client-server comm
                             case BluetoothCommandService.STATE_NONE:
                                 //mTitle.setText(R.string.title_not_connected);
                                 System.out.println("State None");
 
                                 break;
                         }
                         break;
                     case MESSAGE_DEVICE_NAME:
                         // save_off the connected device's name
                         String mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
 
                         displayText("Connected to " + mConnectedDeviceName + "...");
 
                         break;
                     case MESSAGE_TOAST:
 
                         String t = msg.getData().getString(TOAST);
 
                         if (t!=null && t.equals("Unable to connect device")) {
                             mConnectionIsOpen = false;
                             // show bluetooth device selector
                             showBluetoothDeviceSelection();
                         }
 
                         displayText(t);
 
                         break;
                 }
             }
             catch (Exception e) {
                 Log.e(LOG_TAG, "Error in handleMessage: " + e.getMessage());
             }
         }
     };
 
     @Override
     public void onDestroy() {
         super.onDestroy();
 
         try {
 
             if (mCommandService != null)
                 mCommandService.stop();
 
         }
         catch (Exception e) {
             Log.e(LOG_TAG, "Error in onDestroy: " + e.getMessage());
         }
     }
 
 //    @Override
 //    public boolean onCreateOptionsMenu(Menu menu) {
 //        // Inflate the menu; this adds items to the action bar if it is present.
 //        getMenuInflater().inflate(R.menu.main, menu);
 //        return true;
 //    }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         LoadPreferences();
 
         try {
             //setContentView(R.layout.activity_main);
             if (mSelectedCamera != null) {
                 mSelectedCamera = CameraHelper.releaseSelectedCamera(mSelectedCamera, this);
             }
 
             if (mSelectedCameraId == NOT_SET) {
                 mSelectedCameraId = CameraHelper.getFacingCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);
             }
 
             mSelectedCamera = CameraHelper.openSelectedCamera(mSelectedCameraId, this);
             mSelectedCamera.setParameters(CameraParameters);
 
             if (mCommandService != null) {
                 if (mCommandService.getState() == BluetoothCommandService.STATE_NONE) {
                     mCommandService.start();
                 }
             }
         }
         catch (Exception e) {
             Log.e(LOG_TAG, "Error in onResume: " + e.getMessage());
         }
 
 //        if (CameraParameters == null)
 //            CameraParameters = mSelectedCamera.getParameters();
 //
 //        _currentZoom = CameraParameters.getZoom();
 //        _maxZoom = CameraParameters.getMaxZoom();
 
     }
 
     @Override
     protected void onPause() {
         super.onPause();
 
         try {
 
             SavePreferences();
 
             mSelectedCamera = CameraHelper.releaseSelectedCamera(mSelectedCamera, this);
 
             if (mCommandService != null)
                 mCommandService.stop();
 
         }
         catch (Exception e) {
             Log.e(LOG_TAG, "Error in onPause: " + e.getMessage());
         }
 
 
 
     }
 
 //    @Override
 //    public boolean onOptionsItemSelected(MenuItem item) {
 //        boolean handled = true;
 //
 //        int id = item.getItemId();
 //        switch (id) {
 //            case R.id.action_resolution:
 //                resolutionMenuClicked();
 //                break;
 //            case R.id.action_sound:
 //                soundMenuClicked();
 //                break;
 //            default:
 //                handled = super.onOptionsItemSelected(item);
 //        }
 //
 //        return handled;
 //    }
 //
 //    private void soundMenuClicked() {
 //        SoundManager.getSingleton().preload(this);
 //        SoundManager.getSingleton().SoundOn = !SoundManager.getSingleton().SoundOn;
 //
 //        mImageButton = (ImageButton) findViewById(R.id.btnSound);
 //
 //        if (SoundManager.getSingleton().SoundOn) {
 //            mImageButton.setImageResource(R.drawable.sound_on);
 //        }
 //        else {
 //            mImageButton.setImageResource(R.drawable.sound_off);
 //        }
 //
 //    }
 
     private void btnResolutionClicked() {
 
         if (SupportedPictureSizes == null)
             return;
 
         // create an array of strings of the form 320x240
         final String[] pictureSizesAsString = new String[SupportedPictureSizes.size()];
         int index = 0;
 
         try {
             for (Camera.Size pictureSize: SupportedPictureSizes)
                 pictureSizesAsString[index++] = String.format(CAMERA_SIZE_DISPLAY_FORMAT, pictureSize.width, pictureSize.height);
 
             // show list in dialog
             AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
             builder.setTitle(getString(R.string.resolutionDialogTitle));
             builder.setSingleChoiceItems(pictureSizesAsString, SelectedPictureSizeIndex,
                     new DialogInterface.OnClickListener() {
                         // indexSelected contains the index of item (of which checkbox checked)
                         @Override
                         public void onClick(DialogInterface dialog, int indexSelected) {
 
                             SelectedPictureSizeIndex = indexSelected;
                             SelectedPictureSize = SupportedPictureSizes.get(indexSelected);
                         }
                     })
                     // Set the action buttons
                     .setPositiveButton(getString(R.string.buttonOk), new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int id) {
 
                             //Do nothing here. We override the onclick
 
                         }
                     });
 
             // create alert dialog
             final AlertDialog alertDialog = builder.create();
 
             alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
 
                 @Override
                 public void onShow(DialogInterface dialog) {
 
                     Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                     if (b != null) {
                         b.setOnClickListener(new View.OnClickListener() {
 
                             @Override
                             public void onClick(View view) {
 
                                 if (SelectedPictureSize != null) {
                                     if (CameraParameters != null) {
 
                                         CameraParameters.setPictureSize(SelectedPictureSize.width, SelectedPictureSize.height);
                                         mSelectedCamera.setParameters(CameraParameters);
 
                                         displayText("Resolution is set to " + pictureSizesAsString[SelectedPictureSizeIndex]);
 
                                     }
                                 }
 
                                 //Dismiss once everything is OK.
                                 alertDialog.dismiss();
                             }
                         });
                     }
                 }
             });
 
             // show it
             alertDialog.show();
         }
         catch (Exception e) {
             Log.e(LOG_TAG, "Error in settingsMenuClick: " + e.getMessage());
         }
     }
 
 //    private void resolutionMenuClicked() {
 //
 //        if (SupportedPictureSizes == null)
 //            return;
 //
 //        // create an array of strings of the form 320x240
 //        final String[] pictureSizesAsString = new String[SupportedPictureSizes.size()];
 //        int index = 0;
 //
 //        try {
 //            for (Camera.Size pictureSize: SupportedPictureSizes)
 //                pictureSizesAsString[index++] = String.format(CAMERA_SIZE_DISPLAY_FORMAT, pictureSize.width, pictureSize.height);
 //
 //            // show list in dialog
 //            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
 //            builder.setTitle(getString(R.string.resolutionDialogTitle));
 //            builder.setSingleChoiceItems(pictureSizesAsString, SelectedPictureSizeIndex,
 //                    new DialogInterface.OnClickListener() {
 //                        // indexSelected contains the index of item (of which checkbox checked)
 //                        @Override
 //                        public void onClick(DialogInterface dialog, int indexSelected) {
 //
 //                            SelectedPictureSizeIndex = indexSelected;
 //
 //                        }
 //                    })
 //                    // Set the action buttons
 //                    .setPositiveButton(getString(R.string.buttonOk), new DialogInterface.OnClickListener() {
 //                        @Override
 //                        public void onClick(DialogInterface dialog, int id) {
 //
 //                            //Do nothing here. We override the onclick
 //
 //                        }
 //                    });
 //
 //            // create alert dialog
 //            final AlertDialog alertDialog = builder.create();
 //
 //            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
 //
 //                @Override
 //                public void onShow(DialogInterface dialog) {
 //
 //                    Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
 //                    if (b != null) {
 //                        b.setOnClickListener(new View.OnClickListener() {
 //
 //                            @Override
 //                            public void onClick(View view) {
 //
 //                                if (SelectedPictureSize != null) {
 //                                    if (CameraParameters != null) {
 //                                        CameraParameters.setPictureSize(SelectedPictureSize.width, SelectedPictureSize.height);
 //                                        mSelectedCamera.setParameters(CameraParameters);
 //                                    }
 //
 //                                    displayText("Resolution is set to " + pictureSizesAsString[SelectedPictureSizeIndex]);
 //
 //                                }
 //
 //                                //Dismiss once everything is OK.
 //                                alertDialog.dismiss();
 //                            }
 //                        });
 //                    }
 //                }
 //            });
 //
 //            // show it
 //            alertDialog.show();
 //        }
 //        catch (Exception e) {
 //            Log.e(LOG_TAG, "Error in settingsMenuClick: " + e.getMessage());
 //        }
 //    }
 
     @Override
     public boolean onTouchEvent(MotionEvent event) {
         return super.onTouchEvent(event);
 //
 //        try {
 //            if (event.getAction() == MotionEvent.ACTION_UP) {
 //
 //                float x = event.getX();
 //                float y = event.getY();
 //                float touchMajor = event.getTouchMajor();
 //                float touchMinor = event.getTouchMinor();
 //
 //                Rect touchRect = new Rect((int)(x - touchMajor / 2), (int)(y - touchMinor / 2), (int)(x + touchMajor / 2), (int)(y + touchMinor / 2));
 //
 //                submitFocusAreaRect(touchRect);
 //
 //            }
 //        }
 //        catch (Exception e) {
 //            Log.e(LOG_TAG, "Error in onTouchEvent: " + e.getMessage());
 //        }
 //        return true;
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
 
         try {
             SavePreferences();
         }
         catch (Exception e) {
             Log.e(LOG_TAG, "Error in onSaveInstanceState: " + e.getMessage());
         }
 
     }
 
     @Override
     public boolean dispatchKeyEvent( KeyEvent event) {
         int action = event.getAction();
         int keyCode = event.getKeyCode();
         switch (keyCode) {
             case KeyEvent.KEYCODE_CAMERA:
                 if (action == KeyEvent.ACTION_UP) {
                     try {
 //                        if (CameraParameters == null) {
 //                            CameraParameters = mSelectedCamera.getParameters();
 //                        }
 
                         // camera orientation is locked to landscape
                         //int rotation = CameraHelper.getDisplayOrientationForCamera(this, mSelectedCameraId);
                         //CameraParameters.setRotation(rotation);
 
                         //mSelectedCamera.setParameters(CameraParameters);
 
                         //mSelectedCamera.autoFocus(null);
 
                         takePicture();
                     }
                     catch (Exception e) {
                         Log.e(LOG_TAG, "Error Taking Photo: " + e.getMessage());
 
                         displayText("Error Taking Photo");
 
                     }
                 }
                 return true;
             case KeyEvent.KEYCODE_FOCUS:
                 // this event is continuously called
                 // when the shutter button is press half way
                if (action == KeyEvent.ACTION_DOWN && event.getFlags() != 40) {
                     //call autofocus
                     if (!mIsFocusing) {
 
                         // play sound
 
                         mSelectedCamera.autoFocus(new Camera.AutoFocusCallback() {
                             @Override
                             public void onAutoFocus(boolean success, Camera camera) {
                                 if (success) {
                                     if (SoundOn)
                                         SoundManager.getSingleton().play(SoundManager.SOUND_FOCUS_END);
                                 } else {
                                     if (SoundOn)
                                         SoundManager.getSingleton().play(SoundManager.SOUND_FOCUS_FAIL);
                                 }
                             }
                         });
                     }
 
                     mIsFocusing = true;
                 }
                 else {
                     mSelectedCamera.cancelAutoFocus();
                     mIsFocusing = false;
                 }
                 return true;
             case KeyEvent.KEYCODE_ZOOM_IN:
 //                if (action == KeyEvent.ACTION_DOWN) {
 //
 //                    if (!_isZoomingIn) {
 //                        _isZoomingIn = true;
 //                        zoomIn();
 //                    }
 //                }
 //                else {
 //                    _isZoomingIn = false;
 //                }

                // I think scan code 545 means full press and 533 means half press
                // so...every time there is a full press there is also a half press
                if (action == KeyEvent.ACTION_UP && event.getScanCode() != 545) {
                     zoomIn();
                    return true;
                 }
                else {
                    return true;
                }

             case KeyEvent.KEYCODE_ZOOM_OUT:
 //                if (action == KeyEvent.ACTION_DOWN) {
 //
 //                    if (!_isZoomingOut) {
 //                        _isZoomingOut = true;
 //                        zoomOut();
 //                    }
 //
 //                }
 //                else {
 //                    //zoomIn();
 //                    _isZoomingOut = false;
 //                }

                // I think scan code 546 means full press and 534 means half press
                // so...every time there is a full press there is also a half press
                if (action == KeyEvent.ACTION_UP && event.getScanCode() != 546) {
                     zoomOut();
                    return true;
                }
                else {
                    return true;
                 }
             default:
                 return super.dispatchKeyEvent(event);
         }
     }
 
 
 
 //    @Override
 //    public void onZoomChange(int zoomValue, boolean stopped, Camera camera) {
 //
 //        if (stopped) {
 //            int currZoom = CameraParameters.getZoom();
 //            displayText("Zoom Level " + mZoomLevels[currZoom]);
 //        }
 //
 //
 //    }
 
     @Override
     public void onBackPressed() {
         SavePreferences();
         super.onBackPressed();
     }
 
     private void SavePreferences(){
         SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
         SharedPreferences.Editor editor = sharedPreferences.edit();
         editor.putString(SELECTED_BLUETOOTH_ID_KEY , mSelectedBluetoothId);
         editor.putInt(SELECTED_CAMERA_ID_KEY, mSelectedCameraId);
         editor.putInt(SELECTED_PICTURE_SIZE, SelectedPictureSizeIndex);
         editor.putBoolean(SELECTED_SOUND_ON, SoundOn);
         editor.commit();
     }
 
     private void LoadPreferences(){
 
         mZoomLevels = new String[] {
                 "1.0x",
                 "1.2x",
                 "1.5x",
                 "1.8x",
                 "2.2x",
                 "2.8x",
                 "3.4x",
                 "4.0x",
                 "5.0x",
                 "6.1x",
                 "7.5x",
                 "9.4x",
                 "11.4x",
                 "13.9x",
                 "17.9x",
                 "21.0x"};
 
         SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
         mSelectedBluetoothId = sharedPreferences.getString(SELECTED_BLUETOOTH_ID_KEY, null);
         mSelectedCameraId = sharedPreferences.getInt(SELECTED_CAMERA_ID_KEY, NOT_SET);
         SelectedPictureSizeIndex = sharedPreferences.getInt(SELECTED_PICTURE_SIZE, NOT_SET);
         SoundManager.getSingleton().preload(this);
         SoundOn = sharedPreferences.getBoolean(SELECTED_SOUND_ON, true);
     }
 
 //    private void submitFocusAreaRect(final Rect touchRect)
 //    {
 //        if (mSelectedCamera != null && CameraParameters != null) {
 //
 //            try {
 //                Camera.Parameters cameraParameters = mSelectedCamera.getParameters();
 //
 //                if (cameraParameters.getMaxNumFocusAreas() == 0)
 //                {
 //                    return;
 //                }
 //
 //                // Convert from View's width and height to +/- 1000
 //                Rect focusArea = new Rect();
 //
 //                CameraPreview cameraPreview = (CameraPreview) findViewById(R.id.cameraPreview);
 //                focusArea.set(touchRect.left * 2000 / cameraPreview.getWidth() - 1000,
 //                        touchRect.top * 2000 / cameraPreview.getHeight() - 1000,
 //                        touchRect.right * 2000 / cameraPreview.getWidth() - 1000,
 //                        touchRect.bottom * 2000 / cameraPreview.getHeight() - 1000);
 //
 //                // Submit focus area to camera
 //
 //                ArrayList<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
 //                focusAreas.add(new Camera.Area(focusArea, 1000));
 //
 //                cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
 //                cameraParameters.setFocusAreas(focusAreas);
 //                mSelectedCamera.setParameters(cameraParameters);
 //
 //                // play sound
 //                if (SoundManager.getSingleton().SoundOn)
 //                    SoundManager.getSingleton().play(SoundManager.SOUND_FOCUS_END);
 //
 //                // Start the autofocus operation
 //                mSelectedCamera.autoFocus(null);
 //            }
 //            catch (Exception e) {
 //                Log.e(LOG_TAG, "Error in submitFocusAreaRect: " + e.getMessage());
 //                //Toast.makeText(mContext, "Error Focusing: " + e.getMessage(), Toast.LENGTH_SHORT).show();
 //                // play sound
 //                if (SoundManager.getSingleton().SoundOn)
 //                    SoundManager.getSingleton().play(SoundManager.SOUND_FOCUS_FAIL);
 //
 //            }
 //        }
 //    }
 
     void zoomIn() {
         try {
 
             int currZoom = mSelectedCamera.getParameters().getZoom();
             int maxZoom = mSelectedCamera.getParameters().getMaxZoom();
 
             if (currZoom != maxZoom) {
 //
 //            Log.v(LOG_TAG, "Current Zoom: " + currZoom);
 //
                 currZoom++;
                 CameraParameters.setZoom(Math.min(currZoom, maxZoom));
                 mSelectedCamera.setParameters(CameraParameters);
 //
 //            currZoom = CameraParameters.getZoom();
 //
             }
 
             displayText("Zoom Level " + mZoomLevels[currZoom]);
 
         }
         catch (Exception e) {
             Log.e(LOG_TAG, "Error in zoomIn: " + e.getMessage());
         }
     }
 
     void zoomOut() {
         try {
 
 
             int currZoom = mSelectedCamera.getParameters().getZoom();
 
             if (CameraParameters.getZoom() != 0) {
 
 //
 //            Log.v(LOG_TAG, "Current Zoom: " + currZoom);
 //
                 currZoom--;
                 CameraParameters.setZoom(Math.max(currZoom, 0));
                 mSelectedCamera.setParameters(CameraParameters);
 
 
 //
 //            currZoom = CameraParameters.getZoom();
 //
             }
             displayText("Zoom Level " + mZoomLevels[currZoom]);
         }
         catch (Exception e) {
             Log.e(LOG_TAG, "Error in zoomOut: " + e.getMessage());
         }
     }
 
     // SmoothZoom is not available on Samsung Galaxy Camera
 //    void zoomTo(int value) {
 //        try {
 //            //if (_currentZoom != value) {
 //            mSelectedCamera.stopSmoothZoom();
 //            mSelectedCamera.startSmoothZoom(value);
 //            //}
 //        }
 //        catch (Exception e) {
 //            Log.e(LOG_TAG, "Error in zoomTo: " + e.getMessage());
 //        }
 //    }
 
     void showNoCameraDialog() {
         try {
             AlertDialog.Builder builder = new AlertDialog.Builder(this);
             builder.setTitle(getString(R.string.noCameraDialogTitle));
             builder.setMessage(
                     getString(R.string.noCameraDialogMessage)
             );
             builder.setPositiveButton(getString(R.string.noCameraDialogPositiveButton), null);
             AlertDialog dialog = builder.create();
             dialog.setCanceledOnTouchOutside(false);
             dialog.show();
         }
         catch (Exception e) {
             Log.e(LOG_TAG, "Error in showNoCameraDialog: " + e.getMessage());
         }
     }
 
     private void displayText(final String message) {
         //mToastText.cancel();
         mToastText.setText(message);
         mToastText.show();
     }
 
 }
