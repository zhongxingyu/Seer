 /*
  * Copyright (C) 2012 Google Inc.
  */
 
 /**
  * Bluetooth Handset StateMachine
  *                      (Disconnected)
  *                           |    ^
  *                   CONNECT |    | DISCONNECTED
  *                           V    |
  *                         (Pending)
  *                           |    ^
  *                 CONNECTED |    | CONNECT
  *                           V    |
  *                        (Connected)
  *                           |    ^
  *             CONNECT_AUDIO |    | DISCONNECT_AUDIO
  *                           V    |
  *                         (AudioOn)
  */
 package com.android.bluetooth.hfp;
 
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothHeadset;
 import android.bluetooth.BluetoothProfile;
 import android.bluetooth.BluetoothUuid;
 import android.bluetooth.IBluetooth;
 import android.bluetooth.IBluetoothHeadsetPhone;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.media.AudioManager;
 import android.net.Uri;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.ParcelUuid;
 import android.os.RemoteException;
 import android.os.ServiceManager;
 import android.telephony.PhoneNumberUtils;
 import android.util.Log;
 import com.android.bluetooth.Utils;
 import com.android.internal.util.IState;
 import com.android.internal.util.State;
 import com.android.internal.util.StateMachine;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 final class HeadsetStateMachine extends StateMachine {
     private static final String TAG = "HeadsetStateMachine";
     private static final boolean DBG = true;
 
     static final int CONNECT = 1;
     static final int DISCONNECT = 2;
     static final int CONNECT_AUDIO = 3;
     static final int DISCONNECT_AUDIO = 4;
     static final int VOICE_RECOGNITION_START = 5;
     static final int VOICE_RECOGNITION_STOP = 6;
 
     // message.obj is an intent AudioManager.VOLUME_CHANGED_ACTION
     // EXTRA_VOLUME_STREAM_TYPE is STREAM_BLUETOOTH_SCO
     static final int INTENT_SCO_VOLUME_CHANGED = 7;
     static final int SET_MIC_VOLUME = 8;
     static final int CALL_STATE_CHANGED = 9;
     static final int INTENT_BATTERY_CHANGED = 10;
     static final int DEVICE_STATE_CHANGED = 11;
     static final int ROAM_CHANGED = 12;
     static final int SEND_CCLC_RESPONSE = 13;
 
     private static final int STACK_EVENT = 101;
     private static final int DIALING_OUT_TIMEOUT = 102;
 
     private static final int CONNECT_TIMEOUT = 201;
 
     private static final int DIALING_OUT_TIMEOUT_VALUE = 10000;
 
     private static final ParcelUuid[] HEADSET_UUIDS = {
         BluetoothUuid.HSP,
         BluetoothUuid.Handsfree,
     };
 
     private Disconnected mDisconnected;
     private Pending mPending;
     private Connected mConnected;
     private AudioOn mAudioOn;
 
     private Context mContext;
     private boolean mVoiceRecognitionStarted = false;
     private boolean mDialingOut = false;
     private AudioManager mAudioManager;
     private AtPhonebook mPhonebook;
 
     private HeadsetPhoneState mPhoneState;
     private int mAudioState;
     private BluetoothAdapter mAdapter;
     private IBluetooth mAdapterService;
     private IBluetoothHeadsetPhone mPhoneProxy;
 
     // mCurrentDevice is the device connected before the state changes
     // mTargetDevice is the device to be connected
     // mIncomingDevice is the device connecting to us, valid only in Pending state
     //                when mIncomingDevice is not null, both mCurrentDevice
     //                  and mTargetDevice are null
     //                when either mCurrentDevice or mTargetDevice is not null,
     //                  mIncomingDevice is null
     // Stable states
     //   No connection, Disconnected state
     //                  both mCurrentDevice and mTargetDevice are null
     //   Connected, Connected state
     //              mCurrentDevice is not null, mTargetDevice is null
     // Interim states
     //   Connecting to a device, Pending
     //                           mCurrentDevice is null, mTargetDevice is not null
     //   Disconnecting device, Connecting to new device
     //     Pending
     //     Both mCurrentDevice and mTargetDevice are not null
     //   Disconnecting device Pending
     //                        mCurrentDevice is not null, mTargetDevice is null
     //   Incoming connections Pending
     //                        Both mCurrentDevice and mTargetDevice are null
     private BluetoothDevice mCurrentDevice = null;
     private BluetoothDevice mTargetDevice = null;
     private BluetoothDevice mIncomingDevice = null;
 
     static {
         classInitNative();
     }
 
     HeadsetStateMachine(Context context) {
         super(TAG);
 
         mContext = context;
         mVoiceRecognitionStarted = false;
         mDialingOut = false;
         mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
         mPhonebook = new AtPhonebook(mContext);
         mPhoneState = new HeadsetPhoneState(context, this);
         mAudioState = BluetoothHeadset.STATE_AUDIO_DISCONNECTED;
         mAdapter = BluetoothAdapter.getDefaultAdapter();
         mAdapterService = IBluetooth.Stub.asInterface(ServiceManager.getService("bluetooth"));
         if (!context.bindService(new Intent(IBluetoothHeadsetPhone.class.getName()),
                                  mConnection, 0)) {
             Log.e(TAG, "Could not bind to Bluetooth Headset Phone Service");
         }
 
         initializeNativeDataNative();
 
         mDisconnected = new Disconnected();
         mPending = new Pending();
         mConnected = new Connected();
         mAudioOn = new AudioOn();
 
         addState(mDisconnected);
         addState(mPending);
         addState(mConnected);
         addState(mAudioOn);
 
         setInitialState(mDisconnected);
     }
 
     private class Disconnected extends State {
         @Override
         public void enter() {
             log("Enter Disconnected: " + getCurrentMessage().what);
             mPhoneState.listenForPhoneState(false);
         }
 
         @Override
         public boolean processMessage(Message message) {
             log("Disconnected process message: " + message.what);
             if (DBG) {
                 if (mCurrentDevice != null || mTargetDevice != null || mIncomingDevice != null) {
                     log("ERROR: current, target, or mIncomingDevice not null in Disconnected");
                     return NOT_HANDLED;
                 }
             }
 
             boolean retValue = HANDLED;
             switch(message.what) {
                 case CONNECT:
                     BluetoothDevice device = (BluetoothDevice) message.obj;
                     broadcastConnectionState(device, BluetoothProfile.STATE_CONNECTING,
                                    BluetoothProfile.STATE_DISCONNECTED);
 
                     if (!connectHfpNative(getByteAddress(device)) ) {
                         broadcastConnectionState(device, BluetoothProfile.STATE_DISCONNECTED,
                                        BluetoothProfile.STATE_CONNECTING);
                         break;
                     }
 
                     synchronized (HeadsetStateMachine.this) {
                         mTargetDevice = device;
                         transitionTo(mPending);
                     }
                     // TODO(BT) remove CONNECT_TIMEOUT when the stack
                     //          sends back events consistently
                     sendMessageDelayed(CONNECT_TIMEOUT, 30000);
                     break;
                 case DISCONNECT:
                     // ignore
                     break;
                 case INTENT_BATTERY_CHANGED:
                     processIntentBatteryChanged((Intent) message.obj);
                     break;
                 case ROAM_CHANGED:
                     processRoamChanged((Boolean) message.obj);
                     break;
                 case CALL_STATE_CHANGED:
                     processCallState((HeadsetCallState) message.obj);
                     break;
                 case STACK_EVENT:
                     StackEvent event = (StackEvent) message.obj;
                     switch (event.type) {
                         case EVENT_TYPE_CONNECTION_STATE_CHANGED:
                             processConnectionEvent(event.valueInt, event.device);
                             break;
                         default:
                             Log.e(TAG, "Unexpected stack event: " + event.type);
                             break;
                     }
                     break;
                 default:
                     return NOT_HANDLED;
             }
             return retValue;
         }
 
         @Override
         public void exit() {
             log("Exit Disconnected: " + getCurrentMessage().what);
             mPhoneState.listenForPhoneState(true);
         }
 
         // in Disconnected state
         private void processConnectionEvent(int state, BluetoothDevice device) {
             switch (state) {
             case HeadsetHalConstants.CONNECTION_STATE_DISCONNECTED:
                 Log.w(TAG, "Ignore HF DISCONNECTED event, device: " + device);
                 break;
             case HeadsetHalConstants.CONNECTION_STATE_CONNECTING:
                 // TODO(BT) Assume it's incoming connection
                 //     Do we need to check priority and accept/reject accordingly?
                 broadcastConnectionState(device, BluetoothProfile.STATE_CONNECTING,
                                          BluetoothProfile.STATE_DISCONNECTED);
                 synchronized (HeadsetStateMachine.this) {
                     mIncomingDevice = device;
                     transitionTo(mPending);
                 }
                 break;
             case HeadsetHalConstants.CONNECTION_STATE_CONNECTED:
                 Log.w(TAG, "HFP Connected from Disconnected state");
                 broadcastConnectionState(device, BluetoothProfile.STATE_CONNECTED,
                                          BluetoothProfile.STATE_DISCONNECTED);
                 synchronized (HeadsetStateMachine.this) {
                     mCurrentDevice = device;
                     transitionTo(mConnected);
                 }
                 break;
             case HeadsetHalConstants.CONNECTION_STATE_DISCONNECTING:
                 Log.w(TAG, "Ignore HF DISCONNECTING event, device: " + device);
                 break;
             default:
                 Log.e(TAG, "Incorrect state: " + state);
                 break;
             }
         }
     }
 
     private class Pending extends State {
         @Override
         public void enter() {
             log("Enter Pending: " + getCurrentMessage().what);
         }
 
         @Override
         public boolean processMessage(Message message) {
             log("Pending process message: " + message.what);
 
             boolean retValue = HANDLED;
             switch(message.what) {
                 case CONNECT:
                     deferMessage(message);
                     break;
                 case CONNECT_TIMEOUT:
                     onConnectionStateChanged(HeadsetHalConstants.CONNECTION_STATE_DISCONNECTED,
                                              getByteAddress(mTargetDevice));
                     break;
                 case DISCONNECT:
                     BluetoothDevice device = (BluetoothDevice) message.obj;
                     if (mCurrentDevice != null && mTargetDevice != null &&
                         mTargetDevice.equals(device) ) {
                         // cancel connection to the mTargetDevice
                         broadcastConnectionState(device, BluetoothProfile.STATE_DISCONNECTED,
                                        BluetoothProfile.STATE_CONNECTING);
                         synchronized (HeadsetStateMachine.this) {
                             mTargetDevice = null;
                         }
                     } else {
                         deferMessage(message);
                     }
                     break;
                 case INTENT_BATTERY_CHANGED:
                     processIntentBatteryChanged((Intent) message.obj);
                     break;
                 case ROAM_CHANGED:
                     processRoamChanged((Boolean) message.obj);
                     break;
                 case CALL_STATE_CHANGED:
                     processCallState((HeadsetCallState) message.obj);
                     break;
                 case STACK_EVENT:
                     StackEvent event = (StackEvent) message.obj;
                     switch (event.type) {
                         case EVENT_TYPE_CONNECTION_STATE_CHANGED:
                             removeMessages(CONNECT_TIMEOUT);
                             processConnectionEvent(event.valueInt, event.device);
                             break;
                         default:
                             Log.e(TAG, "Unexpected event: " + event.type);
                             break;
                     }
                     break;
                 default:
                     return NOT_HANDLED;
             }
             return retValue;
         }
 
         // in Pending state
         private void processConnectionEvent(int state, BluetoothDevice device) {
             switch (state) {
                 case HeadsetHalConstants.CONNECTION_STATE_DISCONNECTED:
                     if ((mCurrentDevice != null) && mCurrentDevice.equals(device)) {
                         broadcastConnectionState(mCurrentDevice,
                                                  BluetoothProfile.STATE_DISCONNECTED,
                                                  BluetoothProfile.STATE_DISCONNECTING);
                         synchronized (HeadsetStateMachine.this) {
                             mCurrentDevice = null;
                         }
 
                         if (mTargetDevice != null) {
                             if (!connectHfpNative(getByteAddress(mTargetDevice))) {
                                 broadcastConnectionState(mTargetDevice,
                                                          BluetoothProfile.STATE_DISCONNECTED,
                                                          BluetoothProfile.STATE_CONNECTING);
                                 synchronized (HeadsetStateMachine.this) {
                                     mTargetDevice = null;
                                     transitionTo(mDisconnected);
                                 }
                             }
                         } else {
                             synchronized (HeadsetStateMachine.this) {
                                 mIncomingDevice = null;
                                 transitionTo(mDisconnected);
                             }
                         }
                     } else if (mTargetDevice != null && mTargetDevice.equals(device)) {
                         // outgoing connection failed
                         broadcastConnectionState(mTargetDevice, BluetoothProfile.STATE_DISCONNECTED,
                                                  BluetoothProfile.STATE_CONNECTING);
                         synchronized (HeadsetStateMachine.this) {
                             mTargetDevice = null;
                             transitionTo(mDisconnected);
                         }
                     } else if (mIncomingDevice != null && mIncomingDevice.equals(device)) {
                         broadcastConnectionState(mIncomingDevice,
                                                  BluetoothProfile.STATE_DISCONNECTED,
                                                  BluetoothProfile.STATE_CONNECTING);
                         synchronized (HeadsetStateMachine.this) {
                             mIncomingDevice = null;
                             transitionTo(mDisconnected);
                         }
                     } else {
                         Log.e(TAG, "Unknown device Disconnected: " + device);
                     }
                     break;
             case HeadsetHalConstants.CONNECTION_STATE_CONNECTED:
                 if ((mCurrentDevice != null) && mCurrentDevice.equals(device)) {
                     // disconnection failed
                     broadcastConnectionState(mCurrentDevice, BluetoothProfile.STATE_CONNECTED,
                                              BluetoothProfile.STATE_DISCONNECTING);
                     if (mTargetDevice != null) {
                         broadcastConnectionState(mTargetDevice, BluetoothProfile.STATE_DISCONNECTED,
                                                  BluetoothProfile.STATE_CONNECTING);
                     }
                     synchronized (HeadsetStateMachine.this) {
                         mTargetDevice = null;
                         transitionTo(mConnected);
                     }
                 } else if (mTargetDevice != null && mTargetDevice.equals(device)) {
                     broadcastConnectionState(mTargetDevice, BluetoothProfile.STATE_CONNECTED,
                                              BluetoothProfile.STATE_CONNECTING);
                     synchronized (HeadsetStateMachine.this) {
                         mCurrentDevice = mTargetDevice;
                         mTargetDevice = null;
                         transitionTo(mConnected);
                     }
                 } else if (mIncomingDevice != null && mIncomingDevice.equals(device)) {
                     broadcastConnectionState(mIncomingDevice, BluetoothProfile.STATE_CONNECTED,
                                              BluetoothProfile.STATE_CONNECTING);
                     synchronized (HeadsetStateMachine.this) {
                         mCurrentDevice = mIncomingDevice;
                         mIncomingDevice = null;
                         transitionTo(mConnected);
                     }
                 } else {
                     Log.e(TAG, "Unknown device Connected: " + device);
                     // something is wrong here, but sync our state with stack
                     broadcastConnectionState(device, BluetoothProfile.STATE_CONNECTED,
                                              BluetoothProfile.STATE_DISCONNECTED);
                     synchronized (HeadsetStateMachine.this) {
                         mCurrentDevice = device;
                         mTargetDevice = null;
                         mIncomingDevice = null;
                         transitionTo(mConnected);
                     }
                 }
                 break;
             case HeadsetHalConstants.CONNECTION_STATE_CONNECTING:
                 if ((mCurrentDevice != null) && mCurrentDevice.equals(device)) {
                     log("current device tries to connect back");
                     // TODO(BT) ignore or reject
                 } else if (mTargetDevice != null && mTargetDevice.equals(device)) {
                     // The stack is connecting to target device or
                     // there is an incoming connection from the target device at the same time
                     // we already broadcasted the intent, doing nothing here
                     if (DBG) {
                         log("Stack and target device are connecting");
                     }
                 }
                 else if (mIncomingDevice != null && mIncomingDevice.equals(device)) {
                     Log.e(TAG, "Another connecting event on the incoming device");
                 } else {
                     // We get an incoming connecting request while Pending
                     // TODO(BT) is stack handing this case? let's ignore it for now
                     log("Incoming connection while pending, ignore");
                 }
                 break;
             case HeadsetHalConstants.CONNECTION_STATE_DISCONNECTING:
                 if ((mCurrentDevice != null) && mCurrentDevice.equals(device)) {
                     // we already broadcasted the intent, doing nothing here
                     if (DBG) {
                         log("stack is disconnecting mCurrentDevice");
                     }
                 } else if (mTargetDevice != null && mTargetDevice.equals(device)) {
                     Log.e(TAG, "TargetDevice is getting disconnected");
                 } else if (mIncomingDevice != null && mIncomingDevice.equals(device)) {
                     Log.e(TAG, "IncomingDevice is getting disconnected");
                 } else {
                     Log.e(TAG, "Disconnecting unknow device: " + device);
                 }
                 break;
             default:
                 Log.e(TAG, "Incorrect state: " + state);
                 break;
             }
         }
 
     }
 
     private class Connected extends State {
         @Override
         public void enter() {
             log("Enter Connected: " + getCurrentMessage().what);
             if (isInCall()) {
                 sendMessage(CONNECT_AUDIO);
             }
         }
 
         @Override
         public boolean processMessage(Message message) {
             log("Connected process message: " + message.what);
             if (DBG) {
                 if (mCurrentDevice == null) {
                     log("ERROR: mCurrentDevice is null in Connected");
                     return NOT_HANDLED;
                 }
             }
 
             boolean retValue = HANDLED;
             switch(message.what) {
                 case CONNECT:
                 {
                     BluetoothDevice device = (BluetoothDevice) message.obj;
                     if (mCurrentDevice.equals(device)) {
                         break;
                     }
 
                     broadcastConnectionState(device, BluetoothProfile.STATE_CONNECTING,
                                    BluetoothProfile.STATE_DISCONNECTED);
                     if (!disconnectHfpNative(getByteAddress(mCurrentDevice))) {
                         broadcastConnectionState(device, BluetoothProfile.STATE_DISCONNECTED,
                                        BluetoothProfile.STATE_CONNECTING);
                         break;
                     }
 
                     synchronized (HeadsetStateMachine.this) {
                         mTargetDevice = device;
                         transitionTo(mPending);
                     }
                 }
                     break;
                 case DISCONNECT:
                 {
                     BluetoothDevice device = (BluetoothDevice) message.obj;
                     if (!mCurrentDevice.equals(device)) {
                         break;
                     }
                     broadcastConnectionState(device, BluetoothProfile.STATE_DISCONNECTING,
                                    BluetoothProfile.STATE_CONNECTED);
                     if (!disconnectHfpNative(getByteAddress(device))) {
                         broadcastConnectionState(device, BluetoothProfile.STATE_CONNECTED,
                                        BluetoothProfile.STATE_DISCONNECTED);
                         break;
                     }
                     transitionTo(mPending);
                 }
                     break;
                 case CONNECT_AUDIO:
                     // TODO(BT) when failure, broadcast audio connecting to disconnected intent
                     //          check if device matches mCurrentDevice
                     connectAudioNative(getByteAddress(mCurrentDevice));
                     break;
                 case VOICE_RECOGNITION_START:
                     // TODO(BT) connect audio and do voice recognition in AudioOn state
                     break;
                 case CALL_STATE_CHANGED:
                     processCallState((HeadsetCallState) message.obj);
                     break;
                 case INTENT_BATTERY_CHANGED:
                     processIntentBatteryChanged((Intent) message.obj);
                     break;
                 case ROAM_CHANGED:
                     processRoamChanged((Boolean) message.obj);
                     break;
                 case DEVICE_STATE_CHANGED:
                     processDeviceStateChanged((HeadsetDeviceState) message.obj);
                     break;
                 case SEND_CCLC_RESPONSE:
                     processSendClccResponse((HeadsetClccResponse) message.obj);
                     break;
                 case DIALING_OUT_TIMEOUT:
                     if (mDialingOut) {
                         mDialingOut= false;
                         atResponseCodeNative(HeadsetHalConstants.AT_RESPONSE_ERROR);
                     }
                     break;
                 case STACK_EVENT:
                     StackEvent event = (StackEvent) message.obj;
                     switch (event.type) {
                         case EVENT_TYPE_CONNECTION_STATE_CHANGED:
                             processConnectionEvent(event.valueInt, event.device);
                             break;
                         case EVENT_TYPE_AUDIO_STATE_CHANGED:
                             processAudioEvent(event.valueInt, event.device);
                             break;
                         case EVENT_TYPE_ANSWER_CALL:
                             // TODO(BT) could answer call happen on Connected state?
                             processAnswerCall();
                             break;
                         case EVENT_TYPE_HANGUP_CALL:
                             // TODO(BT) could hangup call happen on Connected state?
                             processHangupCall();
                             break;
                         case EVENT_TYPE_VOLUME_CHANGED:
                             processVolumeEvent(event.valueInt, event.valueInt2);
                             break;
                         case EVENT_TYPE_DIAL_CALL:
                             processDialCall(event.valueString);
                             break;
                         case EVENT_TYPE_SEND_DTMF:
                             processSendDtmf(event.valueInt);
                             break;
                         case EVENT_TYPE_AT_CHLD:
                             processAtChld(event.valueInt);
                             break;
                         case EVENT_TYPE_SUBSCRIBER_NUMBER_REQUEST:
                             processSubscriberNumberRequest();
                             break;
                         case EVENT_TYPE_AT_CIND:
                             processAtCind();
                             break;
                         case EVENT_TYPE_AT_COPS:
                             processAtCops();
                             break;
                         case EVENT_TYPE_AT_CLCC:
                             processAtClcc();
                             break;
                         case EVENT_TYPE_UNKNOWN_AT:
                             processUnknownAt(event.valueString);
                             break;
                         case EVENT_TYPE_KEY_PRESSED:
                             processKeyPressed();
                             break;
                         default:
                             Log.e(TAG, "Unknown stack event: " + event.type);
                             break;
                     }
                     break;
                 default:
                     return NOT_HANDLED;
             }
             return retValue;
         }
 
         // in Connected state
         private void processConnectionEvent(int state, BluetoothDevice device) {
             switch (state) {
                 case HeadsetHalConstants.CONNECTION_STATE_DISCONNECTED:
                     if (mCurrentDevice.equals(device)) {
                         broadcastConnectionState(mCurrentDevice, BluetoothProfile.STATE_DISCONNECTED,
                                                  BluetoothProfile.STATE_CONNECTED);
                         synchronized (HeadsetStateMachine.this) {
                             mCurrentDevice = null;
                             transitionTo(mDisconnected);
                         }
                     } else {
                         Log.e(TAG, "Disconnected from unknown device: " + device);
                     }
                     break;
               default:
                   Log.e(TAG, "Connection State Device: " + device + " bad state: " + state);
                   break;
             }
         }
 
         // in Connected state
         private void processAudioEvent(int state, BluetoothDevice device) {
             if (!mCurrentDevice.equals(device)) {
                 Log.e(TAG, "Audio changed on disconnected device: " + device);
                 return;
             }
 
             switch (state) {
                 case HeadsetHalConstants.AUDIO_STATE_CONNECTED:
                     // TODO(BT) should I save the state for next broadcast as the prevState?
                     mAudioState = BluetoothHeadset.STATE_AUDIO_CONNECTED;
                     mAudioManager.setBluetoothScoOn(true);
                     broadcastAudioState(device, BluetoothHeadset.STATE_AUDIO_CONNECTED,
                                         BluetoothHeadset.STATE_AUDIO_CONNECTING);
                     transitionTo(mAudioOn);
                     break;
                 case HeadsetHalConstants.AUDIO_STATE_CONNECTING:
                     mAudioState = BluetoothHeadset.STATE_AUDIO_CONNECTING;
                     broadcastAudioState(device, BluetoothHeadset.STATE_AUDIO_CONNECTING,
                                         BluetoothHeadset.STATE_AUDIO_DISCONNECTED);
                     break;
                     // TODO(BT) process other states
                 default:
                     Log.e(TAG, "Audio State Device: " + device + " bad state: " + state);
                     break;
             }
         }
     }
 
     private class AudioOn extends State {
         // Audio parameters
         private static final String HEADSET_NAME = "bt_headset_name";
         private static final String HEADSET_NREC = "bt_headset_nrec";
 
         @Override
         public void enter() {
             log("Enter Audio: " + getCurrentMessage().what);
             mAudioManager.setParameters(HEADSET_NAME + "=" + getCurrentDeviceName() + ";" +
                                         HEADSET_NREC + "=on");
         }
 
         @Override
         public boolean processMessage(Message message) {
             log("AudioOn process message: " + message.what);
             if (DBG) {
                 if (mCurrentDevice == null) {
                     log("ERROR: mCurrentDevice is null in AudioOn");
                     return NOT_HANDLED;
                 }
             }
 
             boolean retValue = HANDLED;
             switch(message.what) {
                 case DISCONNECT_AUDIO:
                     // TODO(BT) when failure broadcast a audio disconnecting to connected intent
                     //          check if device matches mCurrentDevice
                     disconnectAudioNative(getByteAddress(mCurrentDevice));
                     break;
                 case VOICE_RECOGNITION_START:
                     // TODO(BT) should we check if device matches mCurrentDevice?
                     startVoiceRecognitionNative();
                     break;
                 case VOICE_RECOGNITION_STOP:
                     stopVoiceRecognitionNative();
                     break;
                 case INTENT_SCO_VOLUME_CHANGED:
                     processIntentScoVolume((Intent) message.obj);
                     break;
                 case CALL_STATE_CHANGED:
                     processCallState((HeadsetCallState) message.obj);
                     break;
                 case INTENT_BATTERY_CHANGED:
                     processIntentBatteryChanged((Intent) message.obj);
                     break;
                 case ROAM_CHANGED:
                     processRoamChanged((Boolean) message.obj);
                     break;
                 case DEVICE_STATE_CHANGED:
                     processDeviceStateChanged((HeadsetDeviceState) message.obj);
                     break;
                 case SEND_CCLC_RESPONSE:
                     processSendClccResponse((HeadsetClccResponse) message.obj);
                     break;
                 case DIALING_OUT_TIMEOUT:
                     if (mDialingOut) {
                         mDialingOut= false;
                         atResponseCodeNative(HeadsetHalConstants.AT_RESPONSE_ERROR);
                     }
                     break;
                 case STACK_EVENT:
                     StackEvent event = (StackEvent) message.obj;
                     switch (event.type) {
                         case EVENT_TYPE_AUDIO_STATE_CHANGED:
                             processAudioEvent(event.valueInt, event.device);
                             break;
                         case EVENT_TYPE_VR_STATE_CHANGED:
                             processVrEvent(event.valueInt);
                             break;
                         case EVENT_TYPE_ANSWER_CALL:
                             processAnswerCall();
                             break;
                         case EVENT_TYPE_HANGUP_CALL:
                             processHangupCall();
                             break;
                         case EVENT_TYPE_VOLUME_CHANGED:
                             processVolumeEvent(event.valueInt, event.valueInt2);
                             break;
                         case EVENT_TYPE_DIAL_CALL:
                             processDialCall(event.valueString);
                             break;
                         case EVENT_TYPE_SEND_DTMF:
                             processSendDtmf(event.valueInt);
                             break;
                         case EVENT_TYPE_NOICE_REDUCTION:
                             processNoiceReductionEvent(event.valueInt);
                             break;
                         case EVENT_TYPE_AT_CHLD:
                             processAtChld(event.valueInt);
                             break;
                         case EVENT_TYPE_SUBSCRIBER_NUMBER_REQUEST:
                             processSubscriberNumberRequest();
                             break;
                         case EVENT_TYPE_AT_CIND:
                             processAtCind();
                             break;
                         case EVENT_TYPE_AT_COPS:
                             processAtCops();
                             break;
                         case EVENT_TYPE_AT_CLCC:
                             processAtClcc();
                             break;
                         case EVENT_TYPE_UNKNOWN_AT:
                             processUnknownAt(event.valueString);
                             break;
                         case EVENT_TYPE_KEY_PRESSED:
                             processKeyPressed();
                             break;
                         default:
                             Log.e(TAG, "Unknown stack event: " + event.type);
                             break;
                     }
                     break;
                 default:
                     return NOT_HANDLED;
             }
             return retValue;
         }
 
         // in AudioOn state
         private void processAudioEvent(int state, BluetoothDevice device) {
             if (!mCurrentDevice.equals(device)) {
                 Log.e(TAG, "Audio changed on disconnected device: " + device);
                 return;
             }
 
             switch (state) {
                 case HeadsetHalConstants.AUDIO_STATE_DISCONNECTED:
                     mAudioState = BluetoothHeadset.STATE_AUDIO_DISCONNECTED;
                     mAudioManager.setBluetoothScoOn(false);
                     broadcastAudioState(device, BluetoothHeadset.STATE_AUDIO_DISCONNECTED,
                                         BluetoothHeadset.STATE_AUDIO_CONNECTED);
                     transitionTo(mConnected);
                     break;
                 case HeadsetHalConstants.AUDIO_STATE_DISCONNECTING:
                     // TODO(BT) adding STATE_AUDIO_DISCONNECTING in BluetoothHeadset?
                     //broadcastAudioState(device, BluetoothHeadset.STATE_AUDIO_DISCONNECTING,
                     //                    BluetoothHeadset.STATE_AUDIO_CONNECTED);
                     break;
                 default:
                     Log.e(TAG, "Audio State Device: " + device + " bad state: " + state);
                     break;
             }
         }
 
         private void processVrEvent(int state) {
             if (state == HeadsetHalConstants.VR_STATE_STARTED) {
                 mVoiceRecognitionStarted = true;
                 // TODO(BT) should we send out Intent.ACTION_VOICE_COMMAND intent
                 //     and do expectVoiceRecognition, acquire wake lock etc
             } else if (state == HeadsetHalConstants.VR_STATE_STOPPED) {
                 mVoiceRecognitionStarted = false;
             } else {
                 Log.e(TAG, "Bad Voice Recognition state: " + state);
             }
         }
 
         // enable 1 enable noice reduction
         //        0 disable noice reduction
         private void processNoiceReductionEvent(int enable) {
             if (enable == 1) {
                 mAudioManager.setParameters(HEADSET_NREC + "=on");
             } else {
                 mAudioManager.setParameters(HEADSET_NREC + "off");
             }
         }
 
         private void processIntentScoVolume(Intent intent) {
             int volumeValue = intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_VALUE, 0);
             setVolumeNative(HeadsetHalConstants.VOLUME_TYPE_SPK, volumeValue);
         }
     }
 
     private ServiceConnection mConnection = new ServiceConnection() {
         public void onServiceConnected(ComponentName className, IBinder service) {
             if (DBG) Log.d(TAG, "Proxy object connected");
             mPhoneProxy = IBluetoothHeadsetPhone.Stub.asInterface(service);
         }
 
         public void onServiceDisconnected(ComponentName className) {
             if (DBG) Log.d(TAG, "Proxy object disconnected");
             mPhoneProxy = null;
         }
     };
 
     // HFP Connection state of the device could be changed by the state machine
     // in separate thread while this method is executing.
     int getConnectionState(BluetoothDevice device) {
         if (getCurrentState() == mDisconnected) {
             return BluetoothProfile.STATE_DISCONNECTED;
         }
 
         synchronized (this) {
             IState currentState = getCurrentState();
             if (currentState == mPending) {
                 if ((mTargetDevice != null) && mTargetDevice.equals(device)) {
                     return BluetoothProfile.STATE_CONNECTING;
                 }
                 if ((mCurrentDevice != null) && mCurrentDevice.equals(device)) {
                     return BluetoothProfile.STATE_DISCONNECTING;
                 }
                 if ((mIncomingDevice != null) && mIncomingDevice.equals(device)) {
                     return BluetoothProfile.STATE_CONNECTING; // incoming connection
                 }
                 return BluetoothProfile.STATE_DISCONNECTED;
             }
 
             if (currentState == mConnected || currentState == mAudioOn) {
                 if (mCurrentDevice.equals(device)) {
                     return BluetoothProfile.STATE_CONNECTED;
                 }
                 return BluetoothProfile.STATE_DISCONNECTED;
             } else {
                 Log.e(TAG, "Bad currentState: " + currentState);
                 return BluetoothProfile.STATE_DISCONNECTED;
             }
         }
     }
 
     List<BluetoothDevice> getConnectedDevices() {
         List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
         synchronized(this) {
             if (isConnected()) {
                 devices.add(mCurrentDevice);
             }
         }
         return devices;
     }
 
     boolean isAudioOn() {
         return (getCurrentState() == mAudioOn);
     }
 
     boolean isAudioConnected(BluetoothDevice device) {
         synchronized(this) {
             if (getCurrentState() == mAudioOn && mCurrentDevice.equals(device)) {
                 return true;
             }
         }
         return false;
     }
 
     int getAudioState(BluetoothDevice device) {
         synchronized(this) {
             if (mCurrentDevice == null || !mCurrentDevice.equals(device)) {
                 return BluetoothHeadset.STATE_AUDIO_DISCONNECTED;
             }
         }
         return mAudioState;
     }
 
     List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) {
         List<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
         Set<BluetoothDevice> bondedDevices = mAdapter.getBondedDevices();
         int connectionState;
         synchronized (this) {
             for (BluetoothDevice device : bondedDevices) {
                 ParcelUuid[] featureUuids = device.getUuids();
                 if (!BluetoothUuid.containsAnyUuid(featureUuids, HEADSET_UUIDS)) {
                     continue;
                 }
                 connectionState = getConnectionState(device);
                 for(int i = 0; i < states.length; i++) {
                     if (connectionState == states[i]) {
                         deviceList.add(device);
                     }
                 }
             }
         }
         return deviceList;
     }
 
     // This method does not check for error conditon (newState == prevState)
     private void broadcastConnectionState(BluetoothDevice device, int newState, int prevState) {
         Intent intent = new Intent(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
         intent.putExtra(BluetoothProfile.EXTRA_PREVIOUS_STATE, prevState);
         intent.putExtra(BluetoothProfile.EXTRA_STATE, newState);
         intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
         mContext.sendBroadcast(intent, HeadsetService.BLUETOOTH_PERM);
         if (DBG) log("Connection state " + device + ": " + prevState + "->" + newState);
         try {
             mAdapterService.sendConnectionStateChange(device, BluetoothProfile.HEADSET, newState,
                                                       prevState);
         } catch (RemoteException e) {
             Log.e(TAG, Log.getStackTraceString(new Throwable()));
         }
     }
 
     private void broadcastAudioState(BluetoothDevice device, int newState, int prevState) {
         Intent intent = new Intent(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);
         intent.putExtra(BluetoothProfile.EXTRA_PREVIOUS_STATE, prevState);
         intent.putExtra(BluetoothProfile.EXTRA_STATE, newState);
         intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
         mContext.sendBroadcast(intent, HeadsetService.BLUETOOTH_PERM);
         if (DBG) log("Audio state " + device + ": " + prevState + "->" + newState);
     }
 
     private void processAnswerCall() {
         if (mPhoneProxy != null) {
             try {
                 mPhoneProxy.answerCall();
             } catch (RemoteException e) {
                 Log.e(TAG, Log.getStackTraceString(new Throwable()));
             }
         } else {
             Log.e(TAG, "Handsfree phone proxy null for answering call");
         }
     }
 
     private void processHangupCall() {
         if (mPhoneProxy != null) {
             try {
                 mPhoneProxy.hangupCall();
             } catch (RemoteException e) {
                 Log.e(TAG, Log.getStackTraceString(new Throwable()));
             }
         } else {
             Log.e(TAG, "Handsfree phone proxy null for hanging up call");
         }
     }
 
     private void processDialCall(String number) {
         String dialNumber;
         if (number == null) {
             dialNumber = mPhonebook.getLastDialledNumber();
             if (dialNumber == null) {
                 if (DBG) log("processDialCall, last dial number null");
                 atResponseCodeNative(HeadsetHalConstants.AT_RESPONSE_ERROR);
                 return;
             }
         } else if (number.charAt(0) == '>') {
             // Yuck - memory dialling requested.
             // Just dial last number for now
             if (number.startsWith(">9999")) {   // for PTS test
                 atResponseCodeNative(HeadsetHalConstants.AT_RESPONSE_ERROR);
                 return;
             }
             dialNumber = mPhonebook.getLastDialledNumber();
         } else {
             dialNumber = PhoneNumberUtils.convertPreDial(number);
         }
         // TODO(BT) do we need to terminate virtual call first
         //          like call terminateScoUsingVirtualVoiceCall()?
         Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                                    Uri.fromParts(SCHEME_TEL, dialNumber, null));
         intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         mContext.startActivity(intent);
         // TODO(BT) continue send OK reults code after call starts
         //          hold wait lock, start a timer, set wait call flag
         //          Get call started indication from bluetooth phone
         mDialingOut = true;
         sendMessageDelayed(DIALING_OUT_TIMEOUT, DIALING_OUT_TIMEOUT_VALUE);
     }
 
     private void processVolumeEvent(int volumeType, int volume) {
         if (volumeType == HeadsetHalConstants.VOLUME_TYPE_SPK) {
             int flag = (getCurrentState() == mAudioOn) ? AudioManager.FLAG_SHOW_UI : 0;
             mAudioManager.setStreamVolume(AudioManager.STREAM_BLUETOOTH_SCO, volume, flag);
         } // TODO(BT) handle MIC volume change
     }
 
     private void processSendDtmf(int dtmf) {
         if (mPhoneProxy != null) {
             try {
                 mPhoneProxy.sendDtmf(dtmf);
             } catch (RemoteException e) {
                 Log.e(TAG, Log.getStackTraceString(new Throwable()));
             }
         } else {
             Log.e(TAG, "Handsfree phone proxy null for sending DTMF");
         }
     }
 
     private void processCallState(HeadsetCallState callState) {
         mPhoneState.setNumActiveCall(callState.mNumActive);
         mPhoneState.setNumHeldCall(callState.mNumHeld);
         mPhoneState.setCallState(callState.mCallState);
         if (mDialingOut && callState.mCallState == HeadsetHalConstants.CALL_STATE_DIALING) {
                 atResponseCodeNative(HeadsetHalConstants.AT_RESPONSE_OK);
                 removeMessages(DIALING_OUT_TIMEOUT);
                 mDialingOut = false;
         }
         log("mNumActive: " + callState.mNumActive + " mNumHeld: " + callState.mNumHeld +
             " mCallState: " + callState.mCallState);
         log("mNumber: " + callState.mNumber + " mType: " + callState.mType);
         phoneStateChangeNative(callState.mNumActive, callState.mNumHeld, callState.mCallState,
                                callState.mNumber, callState.mType);
     }
 
     private void processAtChld(int chld) {
         if (mPhoneProxy != null) {
             try {
                 if (mPhoneProxy.processChld(chld)) {
                     atResponseCodeNative(HeadsetHalConstants.AT_RESPONSE_OK);
                 } else {
                     atResponseCodeNative(HeadsetHalConstants.AT_RESPONSE_ERROR);
                 }
             } catch (RemoteException e) {
                 Log.e(TAG, Log.getStackTraceString(new Throwable()));
                 atResponseCodeNative(HeadsetHalConstants.AT_RESPONSE_ERROR);
             }
         } else {
             Log.e(TAG, "Handsfree phone proxy null for At+Chld");
             atResponseCodeNative(HeadsetHalConstants.AT_RESPONSE_ERROR);
         }
     }
 
     private void processSubscriberNumberRequest() {
         if (mPhoneProxy != null) {
             try {
                 String number = mPhoneProxy.getSubscriberNumber();
                 if (number != null) {
                     atResponseStringNative("+CNUM: ,\"" + number + "\"," +
                                            PhoneNumberUtils.toaFromString(number) + ",,4");
                 }
             } catch (RemoteException e) {
                 Log.e(TAG, Log.getStackTraceString(new Throwable()));
             }
         } else {
             Log.e(TAG, "Handsfree phone proxy null for At+CNUM");
         }
     }
 
     private void processAtCind() {
         cindResponseNative(mPhoneState.getService(), mPhoneState.getNumActiveCall(),
                            mPhoneState.getNumHeldCall(), mPhoneState.getCallState(),
                            mPhoneState.getSignal(), mPhoneState.getRoam(),
                            mPhoneState.getBatteryCharge());
     }
 
     private void processAtCops() {
         if (mPhoneProxy != null) {
             try {
                 String operatorName = mPhoneProxy.getNetworkOperator();
                 if (operatorName == null) {
                     operatorName = "";
                 } 
                 copsResponseNative(operatorName);
             } catch (RemoteException e) {
                 Log.e(TAG, Log.getStackTraceString(new Throwable()));
                 copsResponseNative("");
             }
         } else {
             Log.e(TAG, "Handsfree phone proxy null for At+COPS");
             copsResponseNative("");
         }
     }
 
     private void processAtClcc() {
         if (mPhoneProxy != null) {
             try {
                 if (!mPhoneProxy.listCurrentCalls()) {
                     clccResponseNative(0, 0, 0, 0, false, "", 0);
                 }
             } catch (RemoteException e) {
                 Log.e(TAG, Log.getStackTraceString(new Throwable()));
                 clccResponseNative(0, 0, 0, 0, false, "", 0);
             }
         } else {
             Log.e(TAG, "Handsfree phone proxy null for At+CLCC");
             clccResponseNative(0, 0, 0, 0, false, "", 0);
         }
     }
 
     private void processUnknownAt(String atString) {
         // TODO (BT)
         atResponseCodeNative(HeadsetHalConstants.AT_RESPONSE_ERROR);
     }
 
     private void processKeyPressed() {
         if (mPhoneState.getCallState() == HeadsetHalConstants.CALL_STATE_INCOMING) {
             if (mPhoneProxy != null) {
                 try {
                     mPhoneProxy.answerCall();
                 } catch (RemoteException e) {
                     Log.e(TAG, Log.getStackTraceString(new Throwable()));
                 }
             } else {
                 Log.e(TAG, "Handsfree phone proxy null for answering call");
             }
         } else if (mPhoneState.getNumActiveCall() > 0) {
             if (mPhoneProxy != null) {
                 try {
                     mPhoneProxy.answerCall();
                 } catch (RemoteException e) {
                     Log.e(TAG, Log.getStackTraceString(new Throwable()));
                 }
             } else {
                 Log.e(TAG, "Handsfree phone proxy null for hangup call");
             }
         } else {
             String dialNumber = mPhonebook.getLastDialledNumber();
             if (dialNumber == null) {
                 if (DBG) log("processKeyPressed, last dial number null");
                 return;
             }
             Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                                        Uri.fromParts(SCHEME_TEL, dialNumber, null));
             intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
             mContext.startActivity(intent);
         }
     }
 
     private void onConnectionStateChanged(int state, byte[] address) {
         StackEvent event = new StackEvent(EVENT_TYPE_CONNECTION_STATE_CHANGED);
         event.valueInt = state;
         event.device = getDevice(address);
         sendMessage(STACK_EVENT, event);
     }
 
     private void onAudioStateChanged(int state, byte[] address) {
         StackEvent event = new StackEvent(EVENT_TYPE_AUDIO_STATE_CHANGED);
         event.valueInt = state;
         event.device = getDevice(address);
         sendMessage(STACK_EVENT, event);
     }
 
     private void onVrStateChanged(int state) {
         StackEvent event = new StackEvent(EVENT_TYPE_VR_STATE_CHANGED);
         event.valueInt = state;
         sendMessage(STACK_EVENT, event);
     }
 
     private void onAnswerCall() {
         StackEvent event = new StackEvent(EVENT_TYPE_ANSWER_CALL);
         sendMessage(STACK_EVENT, event);
     }
 
     private void onHangupCall() {
         StackEvent event = new StackEvent(EVENT_TYPE_HANGUP_CALL);
         sendMessage(STACK_EVENT, event);
     }
 
     private void onVolumeChanged(int type, int volume) {
         StackEvent event = new StackEvent(EVENT_TYPE_VOLUME_CHANGED);
         event.valueInt = type;
         event.valueInt2 = volume;
         sendMessage(STACK_EVENT, event);
     }
 
     private void onDialCall(String number) {
         StackEvent event = new StackEvent(EVENT_TYPE_DIAL_CALL);
         event.valueString = number;
         sendMessage(STACK_EVENT, event);
     }
 
     private void onSendDtmf(int dtmf) {
         StackEvent event = new StackEvent(EVENT_TYPE_SEND_DTMF);
         event.valueInt = dtmf;
         sendMessage(STACK_EVENT, event);
     }
 
     private void onNoiceReductionEnable(boolean enable) {
         StackEvent event = new StackEvent(EVENT_TYPE_NOICE_REDUCTION);
         event.valueInt = enable ? 1 : 0;
         sendMessage(STACK_EVENT, event);
     }
 
     private void onAtChld(int chld) {
         StackEvent event = new StackEvent(EVENT_TYPE_AT_CHLD);
         event.valueInt = chld;
         sendMessage(STACK_EVENT, event);
     }
 
     private void onAtCnum() {
         StackEvent event = new StackEvent(EVENT_TYPE_SUBSCRIBER_NUMBER_REQUEST);
         sendMessage(STACK_EVENT, event);
     }
 
     private void onAtCind() {
         StackEvent event = new StackEvent(EVENT_TYPE_AT_CIND);
         sendMessage(STACK_EVENT, event);
     }
 
     private void onAtCops() {
         StackEvent event = new StackEvent(EVENT_TYPE_AT_COPS);
         sendMessage(STACK_EVENT, event);
     }
 
     private void onAtClcc() {
         StackEvent event = new StackEvent(EVENT_TYPE_AT_CLCC);
         sendMessage(STACK_EVENT, event);
     }
 
     private void onUnknownAt(String atString) {
         StackEvent event = new StackEvent(EVENT_TYPE_UNKNOWN_AT);
         event.valueString = atString;
         sendMessage(STACK_EVENT, event);
     }
 
     private void onKeyPressed() {
         StackEvent event = new StackEvent(EVENT_TYPE_KEY_PRESSED);
         sendMessage(STACK_EVENT, event);
     }
 
     private void processIntentBatteryChanged(Intent intent) {
         int batteryLevel = intent.getIntExtra("level", -1);
         int scale = intent.getIntExtra("scale", -1);
         if (batteryLevel == -1 || scale == -1 || scale == 0) {
             Log.e(TAG, "Bad Battery Changed intent: " + batteryLevel + "," + scale);
             return;
         }
         batteryLevel = batteryLevel * 5 / scale;
         mPhoneState.setBatteryCharge(batteryLevel);
     }
 
     private void processRoamChanged(boolean roam) {
         mPhoneState.setRoam(roam ? HeadsetHalConstants.SERVICE_TYPE_ROAMING :
                             HeadsetHalConstants.SERVICE_TYPE_HOME);
     }
 
     private void processDeviceStateChanged(HeadsetDeviceState deviceState) {
         notifyDeviceStatusNative(deviceState.mService, deviceState.mRoam, deviceState.mSignal,
                                  deviceState.mBatteryCharge);
     }
 
     private void processSendClccResponse(HeadsetClccResponse clcc) {
         clccResponseNative(clcc.mIndex, clcc.mDirection, clcc.mStatus, clcc.mMode, clcc.mMpty,
                            clcc.mNumber, clcc.mType);
     }
 
     private String getCurrentDeviceName() {
         String defaultName = "<unknown>";
         if (mCurrentDevice == null) {
             return defaultName;
         }
         String deviceName = mCurrentDevice.getName();
         if (deviceName == null) {
             return defaultName;
         }
         return deviceName;
     }
 
     private byte[] getByteAddress(BluetoothDevice device) {
         return Utils.getBytesFromAddress(device.getAddress());
     }
 
     private BluetoothDevice getDevice(byte[] address) {
         return mAdapter.getRemoteDevice(Utils.getAddressStringFromByte(address));
     }
 
     private boolean isInCall() {
         return mPhoneState.isInCall();
     }
 
     boolean isConnected() {
         IState currentState = getCurrentState();
         return (currentState == mConnected || currentState == mAudioOn);
     }
 
     private void log(String msg) {
         if (DBG) {
             Log.d(TAG, msg);
         }
     }
 
     private static final String SCHEME_TEL = "tel";
 
     // Event types for STACK_EVENT message
     final private static int EVENT_TYPE_NONE = 0;
     final private static int EVENT_TYPE_CONNECTION_STATE_CHANGED = 1;
     final private static int EVENT_TYPE_AUDIO_STATE_CHANGED = 2;
     final private static int EVENT_TYPE_VR_STATE_CHANGED = 3;
     final private static int EVENT_TYPE_ANSWER_CALL = 4;
     final private static int EVENT_TYPE_HANGUP_CALL = 5;
     final private static int EVENT_TYPE_VOLUME_CHANGED = 6;
     final private static int EVENT_TYPE_DIAL_CALL = 7;
     final private static int EVENT_TYPE_SEND_DTMF = 8;
     final private static int EVENT_TYPE_NOICE_REDUCTION = 9;
     final private static int EVENT_TYPE_AT_CHLD = 10;
     final private static int EVENT_TYPE_SUBSCRIBER_NUMBER_REQUEST = 11;
     final private static int EVENT_TYPE_AT_CIND = 12;
     final private static int EVENT_TYPE_AT_COPS = 13;
     final private static int EVENT_TYPE_AT_CLCC = 14;
     final private static int EVENT_TYPE_UNKNOWN_AT = 15;
     final private static int EVENT_TYPE_KEY_PRESSED = 16;
 
     private class StackEvent {
         int type = EVENT_TYPE_NONE;
         int valueInt = 0;
         int valueInt2 = 0;
         String valueString = null;
         BluetoothDevice device = null;
 
         private StackEvent(int type) {
             this.type = type;
         }
     }
 
     private native static void classInitNative();
     private native void initializeNativeDataNative();
     private native boolean connectHfpNative(byte[] address);
     private native boolean disconnectHfpNative(byte[] address);
     private native boolean connectAudioNative(byte[] address);
     private native boolean disconnectAudioNative(byte[] address);
     private native boolean startVoiceRecognitionNative();
     private native boolean stopVoiceRecognitionNative();
     private native boolean setVolumeNative(int volumeType, int volume);
     private native boolean cindResponseNative(int service, int numActive, int numHeld,
                                               int callState, int signal, int roam,
                                               int batteryCharge);
     private native boolean notifyDeviceStatusNative(int networkState, int serviceType, int signal,
                                                     int batteryCharge);
     private native boolean atResponseCodeNative(int responseCode);
     private native boolean clccResponseNative(int index, int dir, int status, int mode,
                                               boolean mpty, String number, int type);
     private native boolean copsResponseNative(String operatorName);
     private native boolean atResponseStringNative(String responseString);
     private native boolean phoneStateChangeNative(int numActive, int numHeld, int callState,
                                                   String number, int type);
 }
