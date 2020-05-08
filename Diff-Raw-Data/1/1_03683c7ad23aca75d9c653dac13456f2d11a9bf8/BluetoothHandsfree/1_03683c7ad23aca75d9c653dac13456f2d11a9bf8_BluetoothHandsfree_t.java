 /*
  * Copyright (C) 2008 The Android Open Source Project
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
 
 package com.android.bluetooth.hfp;
 
 
 import android.bluetooth.AtCommandHandler;
 import android.bluetooth.AtCommandResult;
 import android.bluetooth.AtParser;
 import android.bluetooth.BluetoothA2dp;
 import android.bluetooth.BluetoothAssignedNumbers;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothHeadset;
 import android.bluetooth.BluetoothProfile;
 import android.bluetooth.BluetoothServerSocket;
 import android.bluetooth.BluetoothSocket;
 import android.bluetooth.HeadsetBase;
 import android.content.ActivityNotFoundException;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.media.AudioManager;
 import android.net.Uri;
 import android.os.AsyncResult;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.os.Looper;
 import android.os.Message;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.os.SystemProperties;
 import android.telephony.PhoneNumberUtils;
 import android.telephony.ServiceState;
 import android.util.Log;
 import android.bluetooth.IBluetoothHeadsetPhone;
 import android.content.ServiceConnection;
 import android.os.IBinder;
 import android.content.ComponentName;
 import java.io.IOException;
 import android.os.RemoteException;
 // handling is required
 import java.io.InputStream;
 import java.util.LinkedList;
 
 /**
  * Bluetooth headset manager for the Phone app.
  * @hide
  */
 public class BluetoothHandsfree {
     private static final String TAG = "Bluetooth HS/HF";
     private static final boolean DBG = false;
     private static final boolean VDBG = false;  // even more logging
 
     private static final int VERSION_1_5 = 105;
     private static final int VERSION_1_6 = 106;
     private static final String PROP_VERSION_KEY = "ro.bluetooth.hfp.ver";
     private static final String PROP_VERSION_1_6 = "1.6";
 
     private static final int mVersion;
 
     public static final int TYPE_UNKNOWN           = 0;
     public static final int TYPE_HEADSET           = 1;
     public static final int TYPE_HANDSFREE         = 2;
     public static final String BLUETOOTH = "Bluetooth";
 
     /** The singleton instance. */
     private static BluetoothHandsfree sInstance;
     public static final String SLC_ESTABLISHED = "android.bluetooth.headset.action.SLC_UP";
 
     private final Context mContext;
     private final BluetoothAdapter mAdapter;
     //private final CallManager mCM;
     private IBluetoothHeadsetPhone mPhoneProxy; //The interface to Phone APK
     private BluetoothA2dp mA2dp;
 
     private BluetoothDevice mA2dpDevice;
     private int mA2dpState;
     private boolean mPendingAudioState;
     private int mAudioState;
     private boolean mStopRing = false;
     private boolean mDialingOut = false; //For outgoign calls
     //These values indicate what to send
     private int prevCall = 0;
     private int prevCallHeld = 0;
     private int prevCallsetup = 0;
     private int prevCallState = HeadsetHalConstants.CALL_STATE_IDLE;
     private int prevNumActive = 0;
     private int prevNumHeld = 0;
     private HeadsetBase mHeadset;
     private BluetoothHeadset mBluetoothHeadset;
     private int mHeadsetType;   // TYPE_UNKNOWN when not connected
     private boolean mAudioPossible;
     private BluetoothSocket mConnectedSco;
 
     private IncomingScoAcceptThread mIncomingScoThread = null;
     private ScoSocketConnectThread mConnectScoThread = null;
     private SignalScoCloseThread mSignalScoCloseThread = null;
     private ScoSocketDisconnectThread mDisconnectScoThread = null;
 
     private AudioManager mAudioManager;
     private PowerManager mPowerManager;
 
     private boolean mPendingScoForA2dp;  // waiting for a2dp sink to suspend before establishing SCO
     private boolean mPendingScoForWbs;  // waiting for wbs codec selection before establishing SCO
     private boolean mExpectingBCS = false;  // true after AG sends +BCS:<codec id>
     private boolean mA2dpSuspended;
     private boolean mPendingA2dpResume = false;
     private boolean mUserWantsAudio;
     private WakeLock mStartCallWakeLock;  // held while waiting for the intent to start call
     private WakeLock mStartVoiceRecognitionWakeLock;  // held while waiting for voice recognition
 
     // AT command state
     private static final int GSM_MAX_CONNECTIONS = 6;  // Max connections allowed by GSM
     private static final int CDMA_MAX_CONNECTIONS = 2;  // Max connections allowed by CDMA
 
     private static final int MAX_IIENABLED = 7;
 
     private long mBgndEarliestConnectionTime = 0;
     private boolean mClip = false;  // Calling Line Information Presentation
     private boolean mIndicatorsEnabled = false;
     /** Individual Indicators Activator for HFP 1.6, 1 based array*/
     private boolean[] mIIEnabled = new boolean[MAX_IIENABLED + 1];
     private boolean mCmee = false;  // Extended Error reporting
     private long[] mClccTimestamps; // Timestamps associated with each clcc index
     private boolean[] mClccUsed;     // Is this clcc index in use
     private boolean mWaitingForCallStart;
     private boolean mWaitingForVoiceRecognition;
     private boolean mHfInitiatedVrDeactivation;
     // do not connect audio until service connection is established
     // for 3-way supported devices, this is after AT+CHLD
     // for non-3-way supported devices, this is after AT+CMER (see spec)
     private boolean mServiceConnectionEstablished;
 
     //private final BluetoothPhoneState mBluetoothPhoneState;  // for CIND and CIEV updates
     private final BluetoothAtPhonebook mPhonebook;
     private HeadsetPhoneState mPhoneState;
 
     private DebugThread mDebugThread;
     private int mScoGain = Integer.MIN_VALUE;
 
     private static Intent sVoiceCommandIntent;
 
     // Audio parameters
     private static final String HEADSET_NREC = "bt_headset_nrec";
     private static final String HEADSET_NAME = "bt_headset_name";
     private static final String HEADSET_VGS  = "bt_headset_vgs";
     private static final String HEADSET_SAMPLERATE = "bt_samplerate";
 
     private int mRemoteBrsf = 0;
     private int mLocalBrsf = 0;
 
     private int mLocalCodec = 0;
     private int mRemoteCodec = 0;
     private int mRemoteAvailableCodecs = 0;
 
     // CDMA specific flag used in context with BT devices having display capabilities
     // to show which Caller is active. This state might not be always true as in CDMA
     // networks if a caller drops off no update is provided to the Phone.
     // This flag is just used as a toggle to provide a update to the BT device to specify
     // which caller is active.
    // private boolean mCdmaIsSecondCallActive = false;
    // private boolean mCdmaCallsSwapped = false;
 
     /* Constants from Bluetooth Specification Hands-Free profile version 1.5 */
     private static final int BRSF_AG_THREE_WAY_CALLING = 1 << 0;
     private static final int BRSF_AG_EC_NR = 1 << 1;
     private static final int BRSF_AG_VOICE_RECOG = 1 << 2;
     private static final int BRSF_AG_IN_BAND_RING = 1 << 3;
     private static final int BRSF_AG_VOICE_TAG_NUMBE = 1 << 4;
     private static final int BRSF_AG_REJECT_CALL = 1 << 5;
     private static final int BRSF_AG_ENHANCED_CALL_STATUS = 1 <<  6;
     private static final int BRSF_AG_ENHANCED_CALL_CONTROL = 1 << 7;
     private static final int BRSF_AG_ENHANCED_ERR_RESULT_CODES = 1 << 8;
     private static final int BRSF_AG_CODEC_NEGOTIATION = 1 << 9;
 
     private static final int BRSF_HF_EC_NR = 1 << 0;
     private static final int BRSF_HF_CW_THREE_WAY_CALLING = 1 << 1;
     private static final int BRSF_HF_CLIP = 1 << 2;
     private static final int BRSF_HF_VOICE_REG_ACT = 1 << 3;
     private static final int BRSF_HF_REMOTE_VOL_CONTROL = 1 << 4;
     private static final int BRSF_HF_ENHANCED_CALL_STATUS = 1 <<  5;
     private static final int BRSF_HF_ENHANCED_CALL_CONTROL = 1 << 6;
     private static final int BRSF_HF_CODEC_NEGOTIATION = 1 << 7;
 
     private static final int CODEC_ID_CVSD = 1;
     private static final int CODEC_ID_MSBC = 2;
 
     private static final int CODEC_CVSD = 1 << 0;
     private static final int CODEC_MSBC = 1 << 1;
 
     private static final int CODEC_NEGOTIATION_SETUP_TIMEOUT_VALUE = 10000; // 10 seconds
 
     private static final String SCHEME_TEL = "tel";
 
     // VirtualCall - true if Virtual Call is active, false otherwise
     private boolean mVirtualCallStarted = false;
 
     // Voice Recognition - true if Voice Recognition is active, false otherwise
     private boolean mVoiceRecognitionStarted;
 
     // is CHLD=1 command active from the remote side, as the call status
     // updates are different here need specific hanlding.
     private boolean mIsChld1Command; //TODO
     private HandsfreeMessageHandler mHandler;
 
     static {
         if (PROP_VERSION_1_6.equals(SystemProperties.get(PROP_VERSION_KEY))) {
             mVersion = VERSION_1_6;
             Log.d(TAG, "Version 1.6");
         } else {
             mVersion = VERSION_1_5;
             Log.d(TAG, "Version 1.5");
         }
     }
 
     public static String typeToString(int type) {
         switch (type) {
         case TYPE_UNKNOWN:
             return "unknown";
         case TYPE_HEADSET:
             return "headset";
         case TYPE_HANDSFREE:
             return "handsfree";
         }
         return null;
     }
 
     /**
      * Initialize the BluetoothHandsfree instance.
      * This is only done once, at startup, from BluetoothHeadsetservice.
      */
     static BluetoothHandsfree init(BluetoothHeadsetService context) {
         sInstance = new BluetoothHandsfree(context);
         return sInstance;
     }
 
     /** Private constructor; @see init() */
     private BluetoothHandsfree(BluetoothHeadsetService context) {
         //mCM = cm;
         mContext = context;
         mAdapter = BluetoothAdapter.getDefaultAdapter();
         boolean bluetoothCapable = (mAdapter != null);
         mHeadset = null;
         mHeadsetType = TYPE_UNKNOWN; // nothing connected yet
         if (bluetoothCapable) {
             mAdapter.getProfileProxy(mContext, mProfileListener,
                                      BluetoothProfile.A2DP);
         }
         mA2dpState = BluetoothA2dp.STATE_DISCONNECTED;
         mA2dpDevice = null;
         mA2dpSuspended = false;
 
         mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
         mStartCallWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                                                        TAG + ":StartCall");
         mStartCallWakeLock.setReferenceCounted(false);
         mStartVoiceRecognitionWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                                                        TAG + ":VoiceRecognition");
         mStartVoiceRecognitionWakeLock.setReferenceCounted(false);
         //Binding the BluetoothPhoneService for Phone APP interface
         if (!context.bindService(new Intent(IBluetoothHeadsetPhone.class.getName()),
                                  mConnection, 0)) {
             Log.e(TAG, "Could not bind to Bluetooth Headset Phone Service");
         }
 
         mLocalBrsf = BRSF_AG_THREE_WAY_CALLING |
                      BRSF_AG_EC_NR |
                      BRSF_AG_REJECT_CALL |
                      BRSF_AG_ENHANCED_CALL_STATUS;
 
         if (sVoiceCommandIntent == null) {
             sVoiceCommandIntent = new Intent(Intent.ACTION_VOICE_COMMAND);
             sVoiceCommandIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         }
         if (mContext.getPackageManager().resolveActivity(sVoiceCommandIntent, 0) != null &&
                 BluetoothHeadset.isBluetoothVoiceDialingEnabled(mContext)) {
             mLocalBrsf |= BRSF_AG_VOICE_RECOG;
         }
 
         HandlerThread thread = new HandlerThread("BluetoothHandsfreeHandler");
         thread.start();
         Looper looper = thread.getLooper();
         mHandler = new HandsfreeMessageHandler(looper);
         //mBluetoothPhoneState = new BluetoothPhoneState();
         mUserWantsAudio = true;
         mVirtualCallStarted = false;
         mVoiceRecognitionStarted = false;
         mIsChld1Command = false;
         mPhonebook = new BluetoothAtPhonebook(mContext, this);
         mPhoneState = new HeadsetPhoneState(context, this);
         mPhoneState.listenForPhoneState(true);
         mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
 
         if (mVersion == VERSION_1_6) {
             if (DBG) Log.d(TAG, "BRSF_AG_CODEC_NEGOTIATION is enabled!");
             mLocalBrsf |= BRSF_AG_CODEC_NEGOTIATION;
         } else {
             if (DBG) Log.d(TAG, "BRSF_AG_CODEC_NEGOTIATION is disabled");
         }
 
         if (bluetoothCapable) {
             resetAtState();
         }
         IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
         filter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
         filter.addAction(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED);
         filter.addAction(BluetoothDevice.ACTION_CONNECTION_ACCESS_REPLY);
         mContext.registerReceiver(mStateReceiver, filter);
     }
 
     //It should be called when HeadsetService is teared dowm
     //to make sure we unbind to BluetoothPhoneService
     public void cleanup() {
         if (mPhoneProxy != null) {
             if (DBG) Log.d(TAG,"Unbinding service...");
             synchronized (mConnection) {
                 try {
                     mPhoneProxy = null;
                     mContext.unbindService(mConnection);
                 } catch (Exception re) {
                     Log.e(TAG,"Error unbinding from IBluetoothHeadsetPhone",re);
                 }
             }
         }
         mContext.unregisterReceiver(mStateReceiver);
         if (mPhoneState != null) {
             mPhoneState.listenForPhoneState(false);
             mPhoneState.cleanup();
         }
         mPhoneState = null;
         if (mPhonebook != null) {
            mPhonebook.resetAtState();
         }
         //mPhonebook = null;
         // mPhoneState.setClip(0);
         mIndicatorsEnabled = false;
         mServiceConnectionEstablished = false;
         mCmee = false;
         mRemoteBrsf = 0;
         prevNumActive = 0;
         prevNumHeld = 0;
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
 
     private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {
     @Override
         public void onReceive(Context context, Intent intent) {
             if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                 Message msg = mHandler.obtainMessage(BATTERY_CHANGED,
                 intent);
                 mHandler.sendMessage(msg);
             } else if (intent.getAction().equals(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)) {
                 int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE,
                                    BluetoothProfile.STATE_DISCONNECTED);
                 int oldState = intent.getIntExtra(BluetoothProfile.EXTRA_PREVIOUS_STATE,
                                      BluetoothProfile.STATE_DISCONNECTED);
                 BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                 // We are only concerned about Connected sinks to suspend and resume
                 // them. We can safely ignore SINK_STATE_CHANGE for other devices.
                 if (device == null || (mA2dpDevice != null &&
                     !device.equals(mA2dpDevice))) {
                     return;
                 }
                 updateA2dpState(device, state, oldState);
             } else if (intent.getAction().equals(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED)) {
                 int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE,
                                    BluetoothA2dp.STATE_NOT_PLAYING);
                 int oldState = intent.getIntExtra(BluetoothProfile.EXTRA_PREVIOUS_STATE,
                                      BluetoothA2dp.STATE_NOT_PLAYING);
                 BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                 if (device == null || (mA2dpDevice != null
                     && !device.equals(mA2dpDevice))) {
                     return;
                 }
                 if (state == BluetoothA2dp.STATE_NOT_PLAYING) {
                     state = (mA2dp != null) ? mA2dp.getConnectionState(device)
                                         : BluetoothProfile.STATE_DISCONNECTED;
                 }
                 if (oldState == BluetoothA2dp.STATE_NOT_PLAYING) {
                     oldState = BluetoothProfile.STATE_CONNECTED;
                 }
                 updateA2dpState(device, state, oldState);
             } else if (intent.getAction().equals(BluetoothDevice.ACTION_CONNECTION_ACCESS_REPLY)) {
                 mPhonebook.handleAccessPermissionResult(intent);
             }
         }
     };
 
     private void updateA2dpState(BluetoothDevice device, int state, int oldState ){
         synchronized (BluetoothHandsfree.this) {
             mA2dpState = state;
             if (state == BluetoothProfile.STATE_DISCONNECTED) {
                 mA2dpDevice = null;
             } else {
                 mA2dpDevice = device;
             }
             if (oldState == BluetoothA2dp.STATE_PLAYING &&
                 mA2dpState == BluetoothProfile.STATE_CONNECTED) {
                 if (mA2dpSuspended) {
                     if (mPendingScoForA2dp) {
                         mHandler.removeMessages(MESSAGE_CHECK_PENDING_SCO);
                         if (DBG) Log.d(TAG,"A2DP suspended, completing SCO");
                         if ((0x0 != (mRemoteBrsf & BRSF_HF_CODEC_NEGOTIATION)) &&
                             (0x0 != (mLocalBrsf & BRSF_AG_CODEC_NEGOTIATION))) {
                             if (0x0 != (mRemoteAvailableCodecs & CODEC_MSBC)) {
                                 if (DBG) Log.d(TAG, "+BCS:2");
                                 mLocalCodec = CODEC_MSBC;
                                 sendURC("+BCS:2");
                             } else {
                                 if (DBG) Log.d(TAG, "+BCS:1");
                                 mLocalCodec = CODEC_CVSD;
                                 sendURC("+BCS:1");
                             }
                             mPendingScoForWbs = true;
                             mExpectingBCS = true;
                             Message msg = mHandler.obtainMessage(CODEC_CONNECTION_SETUP_TIMEOUT);
                             mHandler.sendMessageDelayed(msg, CODEC_NEGOTIATION_SETUP_TIMEOUT_VALUE);
                         } else {
                             mAudioManager.setParameters(HEADSET_SAMPLERATE + "=8000");
                             connectScoThread(false); //Start SCO for CVSD
                         }
                         mPendingScoForA2dp = false;
                     } else if (mPendingA2dpResume) {
                         if (DBG) Log.d(TAG,"resuming A2DP stream after disconnecting SCO");
                         mA2dp.resumeSink(mA2dpDevice);
                         mA2dpSuspended = false;
                         mPendingA2dpResume = false;
                     }
                 }
             }
         }
     }
 
 
     /**
      * A thread that runs in the background waiting for a Sco Server Socket to
      * accept a connection. Even after a connection has been accepted, the Sco Server
      * continues to listen for new connections.
      */
     private class IncomingScoAcceptThread extends Thread{
         private final BluetoothServerSocket mIncomingServerSocket;
         private BluetoothSocket mIncomingSco;
         private boolean stopped = false;
 
         public IncomingScoAcceptThread() {
             BluetoothServerSocket serverSocket = null;
             try {
                 serverSocket = BluetoothAdapter.listenUsingScoOn();
             } catch (IOException e) {
                 Log.e(TAG, "Could not create BluetoothServerSocket");
                 stopped = true;
             }
             mIncomingServerSocket = serverSocket;
         }
 
         @Override
         public void run() {
             while (!stopped) {
                 try {
                     mIncomingSco = mIncomingServerSocket.accept();
                 } catch (IOException e) {
                     Log.e(TAG, "BluetoothServerSocket could not accept connection");
                 }
 
                 if (mIncomingSco != null) {
                     connectSco();
                 }
             }
         }
 
         private void connectSco() {
             synchronized (BluetoothHandsfree.this) {
                 if (!Thread.currentThread().interrupted() &&
                     isHeadsetConnected() && mConnectedSco == null) {
                     Log.i(TAG, "Routing audio for incoming SCO connection");
                     mConnectedSco = mIncomingSco;
                     mAudioManager.setBluetoothScoOn(true);
                     setAudioState(BluetoothHeadset.STATE_AUDIO_CONNECTED,
                         mHeadset.getRemoteDevice());
 
                     if (mSignalScoCloseThread == null) {
                         mSignalScoCloseThread = new SignalScoCloseThread();
                         mSignalScoCloseThread.setName("SignalScoCloseThread");
                         mSignalScoCloseThread.start();
                     }
                 } else {
                     Log.i(TAG, "Rejecting incoming SCO connection");
                     try {
                         mIncomingSco.close();
                     }catch (IOException e) {
                         Log.e(TAG, "Error when closing incoming Sco socket");
                     }
                     mIncomingSco = null;
                 }
             }
         }
 
         // must be called with BluetoothHandsfree locked
         void shutdown() {
             try {
                 mIncomingServerSocket.close();
             } catch (IOException e) {
                 Log.w(TAG, "Error when closing server socket");
             }
             stopped = true;
             interrupt();
         }
     }
 
     /**
      * A thread that runs in the background waiting for a Sco Socket to
      * connect.Once the socket is connected, this thread shall be
      * shutdown.
      */
     private class ScoSocketConnectThread extends Thread{
         private BluetoothSocket mOutgoingSco;
         private boolean mIsWbs;
 
         public ScoSocketConnectThread(BluetoothDevice device, boolean wbs) {
             try {
                 mIsWbs = wbs;
                 if (wbs) {
                     mOutgoingSco = device.createScoWbsSocket();
                 } else {
                     mOutgoingSco = device.createScoSocket();
                 }
             } catch (IOException e) {
                 Log.w(TAG, "Could not create BluetoothSocket");
                 failedScoConnect();
             }
         }
 
         @Override
         public void run() {
             try {
                 mOutgoingSco.connect();
             }catch (IOException connectException) {
                 Log.e(TAG, "BluetoothSocket could not connect");
                 mOutgoingSco = null;
                 failedScoConnect();
             }
 
             if (mOutgoingSco != null) {
                 connectSco();
             }
         }
 
         private void connectSco() {
             synchronized (BluetoothHandsfree.this) {
                 if (!Thread.currentThread().interrupted() &&
                     isHeadsetConnected() && mConnectedSco == null) {
                     if (VDBG) Log.d(TAG,"Routing audio for outgoing SCO conection");
                     mConnectedSco = mOutgoingSco;
                     mAudioManager.setBluetoothScoOn(true);
 
                     setAudioState(BluetoothHeadset.STATE_AUDIO_CONNECTED,
                       mHeadset.getRemoteDevice());
 
                     if (mSignalScoCloseThread == null) {
                         mSignalScoCloseThread = new SignalScoCloseThread();
                         mSignalScoCloseThread.setName("SignalScoCloseThread");
                         mSignalScoCloseThread.start();
                     }
                 } else {
                     if (VDBG) Log.d(TAG,"Rejecting new connected outgoing SCO socket");
                     try {
                         mOutgoingSco.close();
                     }catch (IOException e) {
                         Log.e(TAG, "Error when closing Sco socket");
                     }
                     mOutgoingSco = null;
                     failedScoConnect();
                 }
             }
         }
 
         private void failedScoConnect() {
             // Wait for couple of secs before sending AUDIO_STATE_DISCONNECTED,
             // since an incoming SCO connection can happen immediately with
             // certain headsets.
             Message msg = Message.obtain(mHandler, SCO_AUDIO_STATE);
             msg.obj = mHeadset.getRemoteDevice();
             mHandler.sendMessageDelayed(msg, 2000);
 
             // Sync with interrupt() statement of shutdown method
             // This prevents resetting of a valid mConnectScoThread.
             // If this thread has been interrupted, it has been shutdown and
             // mConnectScoThread is/will be reset by the outer class.
             // We do not want to do it here since mConnectScoThread could be
             // assigned with a new object.
             synchronized (ScoSocketConnectThread.this) {
                 if (!isInterrupted()) {
                     resetConnectScoThread();
                 }
             }
             if (mIsWbs) {
                 fallbackNb();
             }
         }
 
         // must be called with BluetoothHandsfree locked
         void shutdown() {
             disconnectScoThread();
 
             // sync with isInterrupted() check in failedScoConnect method
             // see explanation there
             synchronized (ScoSocketConnectThread.this) {
                 interrupt();
             }
         }
     }
 
 
      private void disconnectScoThread(){
          // Sync with setting mConnectScoThread to null to assure the validity of
          // the condition
          synchronized (ScoSocketDisconnectThread.class) {
              if (mConnectedSco == null) {
                  if (DBG) Log.d(TAG,"SCO audio is already disconnected");
                  return;
              }
 
              if (mDisconnectScoThread == null) {
                  BluetoothDevice device = mHeadset.getRemoteDevice();
 
                  mDisconnectScoThread = new ScoSocketDisconnectThread();
                  mDisconnectScoThread.setName("HandsfreeScoSocketDisconnectThread");
 
                  mDisconnectScoThread.start();
              }
          }
     }
 
 
     private class ScoSocketDisconnectThread extends Thread{
         @Override
         public void run() {
             Log.e(TAG, "Before Sco disconnect");
             if (mConnectedSco != null) {
                 try {
                     mConnectedSco.close();
                 } catch (IOException e) {
                     Log.e(TAG, "Error when closing Sco socket");
                 }
             }
             Log.e(TAG, "After Sco disconnect");
             closeConnectedSco();
         }
 
         private void closeConnectedSco() {
             if (mConnectedSco != null) {
                 BluetoothDevice device = null;
                 if (mHeadset != null) {
                     device = mHeadset.getRemoteDevice();
                 }
                 if (mAudioManager.isSpeakerphoneOn()) {
                     // User option might be speaker as sco disconnection
                     // is delayed setting back the speaker option.
                     mAudioManager.setBluetoothScoOn(false);
                     mAudioManager.setSpeakerphoneOn(true);
                 } else {
                     mAudioManager.setBluetoothScoOn(false);
                 }
                 synchronized(BluetoothHandsfree.this) {
                     mConnectedSco = null;
                     setAudioState(BluetoothHeadset.STATE_AUDIO_DISCONNECTED,
                                   device);
                 }
             }
             synchronized (ScoSocketDisconnectThread.class) {
                 mDisconnectScoThread = null;
             }
         }
     }
 
 
     /*
      * Signals when a Sco connection has been closed
      */
     private class SignalScoCloseThread extends Thread{
         private boolean stopped = false;
 
         @Override
         public void run() {
             while (!stopped) {
                 BluetoothSocket connectedSco = null;
                 synchronized (BluetoothHandsfree.this) {
                     connectedSco = mConnectedSco;
                 }
                 if (connectedSco != null) {
                     byte b[] = new byte[1];
                     InputStream inStream = null;
                     try {
                         inStream = connectedSco.getInputStream();
                     } catch (IOException e) {}
 
                     if (inStream != null) {
                         try {
                             // inStream.read is a blocking call that won't ever
                             // return anything, but will throw an exception if the
                             // connection is closed
                             int ret = inStream.read(b, 0, 1);
                         }catch (IOException connectException) {
                             // call a message to close this thread and turn off audio
                             // we can't call audioOff directly because then
                             // the thread would try to close itself
                             Message msg = Message.obtain(mHandler, SCO_CLOSED);
                             mHandler.sendMessage(msg);
                             break;
                         }
                     }
                 }
             }
         }
 
         // must be called with BluetoothHandsfree locked
         void shutdown() {
             stopped = true;
             disconnectScoThread();
             interrupt();
         }
     }
 
     private void connectScoThread(boolean wbs){
         // Sync with setting mConnectScoThread to null to assure the validity of
         // the condition
         synchronized (ScoSocketConnectThread.class) {
             if (mConnectedSco != null) {
                 if (DBG) Log.d(TAG,"SCO audio is already connected");
                 return;
             }
 
             if (mConnectScoThread == null) {
                 BluetoothDevice device = mHeadset.getRemoteDevice();
                 if (getAudioState(device) == BluetoothHeadset.STATE_AUDIO_DISCONNECTED) {
                     setAudioState(BluetoothHeadset.STATE_AUDIO_CONNECTING, device);
                 }
 
                 mConnectScoThread = new ScoSocketConnectThread(mHeadset.getRemoteDevice(), wbs);
                 mConnectScoThread.setName("HandsfreeScoSocketConnectThread");
 
                 mConnectScoThread.start();
             }
         }
     }
 
     private void resetConnectScoThread() {
         // Sync with if (mConnectScoThread == null) check
         synchronized (ScoSocketConnectThread.class) {
             mConnectScoThread = null;
         }
     }
 
     /* package */ synchronized void onBluetoothEnabled() {
         /* Bluez has a bug where it will always accept and then orphan
          * incoming SCO connections, regardless of whether we have a listening
          * SCO socket. So the best thing to do is always run a listening socket
          * while bluetooth is on so that at least we can disconnect it
          * immediately when we don't want it.
          */
 
         if (mIncomingScoThread == null) {
             mIncomingScoThread = new IncomingScoAcceptThread();
             mIncomingScoThread.setName("incomingScoAcceptThread");
             mIncomingScoThread.start();
         }
     }
 
     /* package */ synchronized void onBluetoothDisabled() {
         // Close off the SCO sockets
         audioOff();
 
         if (mIncomingScoThread != null) {
             mIncomingScoThread.shutdown();
             mIncomingScoThread = null;
         }
         if (mPhonebook != null) {
             mPhonebook.resetAtState();
         }
         mIndicatorsEnabled = false;
         mServiceConnectionEstablished = false;
         mCmee = false;
         mRemoteBrsf = 0;
         prevNumActive = 0;
         prevNumHeld = 0;
         prevCallState = HeadsetHalConstants.CALL_STATE_IDLE;
     }
 
     private boolean isHeadsetConnected() {
         if (mHeadset == null || mHeadsetType == TYPE_UNKNOWN) {
             return false;
         }
         return mHeadset.isConnected();
     }
 
     /* package */ synchronized void connectHeadset(HeadsetBase headset, int headsetType) {
         mHeadset = headset;
         mHeadsetType = headsetType;
         if (mHeadsetType == TYPE_HEADSET) {
             initializeHeadsetAtParser();
         } else {
             initializeHandsfreeAtParser();
         }
 
         // Headset vendor-specific commands
         registerAllVendorSpecificCommands();
 
         headset.startEventThread();
         configAudioParameters();
 
         if (inDebug()) {
             Log.d(TAG, "Start HFP debug thread");
             startDebug();
         }
 
        mVoiceRecognitionStarted = false;
         mRemoteCodec = 0;
         for (int i = 1; i <= MAX_IIENABLED; i ++) {
             // Individual Indicators are set to true by default
             Log.d(TAG,"Individual Indicators are set to true by default");
             mIIEnabled[i] = true;
         }
         if (mPhoneState.isInCallAudio()) {
             audioOn();
         } else if ( mPhoneState.getCallState() == HeadsetHalConstants.CALL_STATE_INCOMING) {
             // need to update HS with RING when single ringing call exist
             if((mPhoneState.getNumActiveCall() == 0) ||(mPhoneState.getNumHeldCall() == 0) )
                 ring();
         }
     }
 
     /* package */ synchronized void disconnectHeadset() {
         audioOff();
 
         // No need to check if isVirtualCallInProgress()
         // terminateScoUsingVirtualVoiceCall() does the check
         Log.d(TAG, "Disconnect Headset is called");
         terminateScoUsingVirtualVoiceCall();
 
         mHeadsetType = TYPE_UNKNOWN;
         stopDebug();
         resetAtState();
     }
 
     synchronized void resetAtState() {
         mPhoneState.setClip(0);
         mIndicatorsEnabled = false;
         mServiceConnectionEstablished = false;
         mCmee = false;
         mRemoteBrsf = 0;
         prevNumActive = 0;
         prevNumHeld = 0;
         mPhonebook.resetAtState();
     }
 
     /* package */ HeadsetBase getHeadset() {
         return mHeadset;
     }
 
     private void configAudioParameters() {
         String name = mHeadset.getRemoteDevice().getName();
         if (name == null) {
             name = "<unknown>";
         }
         mAudioManager.setParameters(HEADSET_NAME+"="+name+";"+HEADSET_NREC+"=on");
     }
 
     boolean isBluetoothVoiceDialingEnabled() {
        return ((mRemoteBrsf & BRSF_HF_VOICE_REG_ACT) != 0x0) ? true : false;
     }
 
     private static final int SCO_CLOSED = 3;
     private static final int CHECK_CALL_STARTED = 4;
     private static final int CHECK_VOICE_RECOGNITION_STARTED = 5;
     private static final int MESSAGE_CHECK_PENDING_SCO = 6;
     private static final int SCO_AUDIO_STATE = 7;
     private static final int SCO_CONNECTION_CHECK = 8;
     private static final int BATTERY_CHANGED = 9;
     private static final int SIGNAL_STRENGTH_CHANGED = 10;
     private static final int CODEC_CONNECTION_SETUP_COMPLETED = 11;
     private static final int CODEC_CONNECTION_SETUP_TIMEOUT = 12;
     private static final int RING = 13;
     private static final int CALL_STATE_CHANGED = 14; //It will process th call
 
     private final class HandsfreeMessageHandler extends Handler {
         private HandsfreeMessageHandler(Looper looper) {
             super(looper);
         }
 
         @Override
         public void handleMessage(Message msg) {
             Log.d(TAG, "HandleMessage" + msg.what);
             switch (msg.what) {
             case SCO_CLOSED:
                 synchronized (BluetoothHandsfree.this) {
                     // synchronized
                     // Make atomic against audioOn, userWantsAudioOn
                     // TODO finer lock to decouple from other call flow such as
                     //      mWaitingForCallStart change
 
                     audioOff();
                     // notify mBluetoothPhoneState that the SCO channel has closed
                     // TODO. What can be done here.. mBluetoothPhoneState.scoClosed();
                 }
                 break;
             case CHECK_CALL_STARTED:
                 synchronized (BluetoothHandsfree.this) {
                     // synchronized
                     // Protect test/change of mWaitingForCallStart
                     if (mWaitingForCallStart) {
                         mWaitingForCallStart = false;
                         Log.e(TAG, "Timeout waiting for call to start");
                         sendURC("ERROR");
                         if (mStartCallWakeLock.isHeld()) {
                             mStartCallWakeLock.release();
                         }
                     }
                 }
                 break;
             case CHECK_VOICE_RECOGNITION_STARTED:
                 synchronized (BluetoothHandsfree.this) {
                     // synchronized
                     // Protect test/change of mWaitingForVoiceRecognition
                     if (mWaitingForVoiceRecognition) {
                         mWaitingForVoiceRecognition = false;
                         Log.e(TAG, "Timeout waiting for voice recognition to start");
                         sendURC("ERROR");
                     }
                 }
                 break;
             case MESSAGE_CHECK_PENDING_SCO:
                 if (mPendingScoForA2dp) {
                     Log.w(TAG, "Timeout suspending A2DP for SCO (mA2dpState = " +
                            mA2dpState + "). Starting SCO anyway");
                     if ((0x0 != (mRemoteBrsf & BRSF_HF_CODEC_NEGOTIATION)) &&
                         (0x0 != (mLocalBrsf & BRSF_AG_CODEC_NEGOTIATION))) {
                         if (0x0 == mRemoteCodec) {
                             if (0x0 != (mRemoteAvailableCodecs & CODEC_MSBC)) {
                                 if (DBG) Log.d(TAG, "+BCS:2");
                                 mLocalCodec = CODEC_MSBC;
                                 sendURC("+BCS:2");
                             } else {
                                 if (DBG) Log.d(TAG, "+BCS:1");
                                 mLocalCodec = CODEC_CVSD;
                                 sendURC("+BCS:1");
                             }
                             mPendingScoForWbs = true;
                             mExpectingBCS = true;
                             Message codec_msg = mHandler.obtainMessage(
                                                          CODEC_CONNECTION_SETUP_TIMEOUT);
                             mHandler.sendMessageDelayed(codec_msg,
                                      CODEC_NEGOTIATION_SETUP_TIMEOUT_VALUE);
                         } else {
                             if (0x0 != (mRemoteAvailableCodecs & CODEC_MSBC)) {
                                 mAudioManager.setParameters(HEADSET_SAMPLERATE + "=16000");
                                 connectScoThread(true); //Start SCO for mSBC
                             } else {
                                 mAudioManager.setParameters(HEADSET_SAMPLERATE + "=8000");
                                 connectScoThread(false); //Start SCO for CVSD
                             }
                         }
                     } else {
                         connectScoThread(false); //CVSD
                         mAudioManager.setParameters(HEADSET_SAMPLERATE + "=8000");
                     }
                     mPendingScoForA2dp = false;
                 }
                 break;
             case CODEC_CONNECTION_SETUP_COMPLETED:
                 if (mPendingScoForWbs) {
                     try {
                           connectScoThread(mLocalCodec == CODEC_MSBC);
                           mPendingScoForWbs = false;
                     } catch (Exception e) {
                             fallbackNb();
                     }
                 }
                 break;
             case CODEC_CONNECTION_SETUP_TIMEOUT:
                 // fall back to NB
                 if (mPendingScoForWbs) {
                     Log.i(TAG, "Timeout codec connection setup, starting SCO anyway using NB");
                     mAudioManager.setParameters(HEADSET_SAMPLERATE + "=8000");
                     connectScoThread(false);
                     mPendingScoForWbs = false;
                 }
                 break;
             case SCO_AUDIO_STATE:
                 BluetoothDevice device = (BluetoothDevice) msg.obj;
                 if (getAudioState(device) == BluetoothHeadset.STATE_AUDIO_CONNECTING) {
                     setAudioState(BluetoothHeadset.STATE_AUDIO_DISCONNECTED, device);
                 }
                 break;
             case SCO_CONNECTION_CHECK:
                 break;
             case RING:
                 AtCommandResult result = ring();
                 if (result != null) {
                     sendURC(result.toString());
                 }
                 break;
             case CALL_STATE_CHANGED:
                 processCallStateChanged((HeadsetCallState)msg.obj);
                 break;
             case BATTERY_CHANGED:
                 updateBatteryState((Intent) msg.obj);
                 break;
             }
         }
     }
 
     void fallbackNb() {
         if ((0x0 != (mLocalBrsf & BRSF_AG_CODEC_NEGOTIATION)) &&
             (0x0 != (mRemoteBrsf & BRSF_HF_CODEC_NEGOTIATION)) &&
             mRemoteCodec == CODEC_MSBC) {
             // fallback to try NB while trying WBS
             mRemoteCodec = 0;
             mLocalCodec = CODEC_CVSD;
             if (DBG) Log.d(TAG,"SCO_FAILED, sending +BCS:1 to try NB");
             mExpectingBCS = true;
             sendURC("+BCS:1");
             Message msg = mHandler.obtainMessage(CODEC_CONNECTION_SETUP_TIMEOUT);
             mHandler.sendMessageDelayed(msg, CODEC_NEGOTIATION_SETUP_TIMEOUT_VALUE);
         }
     }
 
     private void setAudioState(int state, BluetoothDevice device) {
         if (VDBG) Log.d(TAG,"setAudioState(" + state + ")");
         if (mBluetoothHeadset == null) {
             mAdapter.getProfileProxy(mContext, mProfileListener, BluetoothProfile.HEADSET);
             mPendingAudioState = true;
             mAudioState = state;
             return;
         }
         mBluetoothHeadset.setAudioState(device, state);
     }
 
     private synchronized int getAudioState(BluetoothDevice device) {
         if (mBluetoothHeadset == null) return BluetoothHeadset.STATE_AUDIO_DISCONNECTED;
         return mBluetoothHeadset.getAudioState(device);
     }
 
     private BluetoothProfile.ServiceListener mProfileListener =
             new BluetoothProfile.ServiceListener() {
         public void onServiceConnected(int profile, BluetoothProfile proxy) {
             if (profile == BluetoothProfile.HEADSET) {
                 mBluetoothHeadset = (BluetoothHeadset) proxy;
                 synchronized(BluetoothHandsfree.this) {
                     if (mPendingAudioState) {
                         mBluetoothHeadset.setAudioState(mHeadset.getRemoteDevice(), mAudioState);
                         mPendingAudioState = false;
                     }
                 }
             } else if (profile == BluetoothProfile.A2DP) {
                 mA2dp = (BluetoothA2dp) proxy;
             }
         }
         public void onServiceDisconnected(int profile) {
             if (profile == BluetoothProfile.HEADSET) {
                 mBluetoothHeadset = null;
             } else if (profile == BluetoothProfile.A2DP) {
                 mA2dp = null;
             }
         }
     };
 
     private boolean sendUpdate() {
         return isHeadsetConnected() && mHeadsetType == TYPE_HANDSFREE
                && mIndicatorsEnabled && mServiceConnectionEstablished;
     }
 
     //The actual state machine handling call state changes
     private void processCallStateChanged(HeadsetCallState callState) {
         mPhoneState.setNumActiveCall(callState.mNumActive);
         mPhoneState.setNumHeldCall(callState.mNumHeld);
         mPhoneState.setCallState(callState.mCallState);
         mPhoneState.setPhoneNumberAndType(callState.mNumber, callState.mType);
         if (mWaitingForCallStart && callState.mCallState ==
                                  HeadsetHalConstants.CALL_STATE_DIALING) {
             sendURC("OK");
             //mHandler.removeMessage(START_CALL_TIMEOUT); //Will it work
             mWaitingForCallStart = false;
         }
         Log.d(TAG,"mNumActive: " + callState.mNumActive + " mNumHeld: " +
         callState.mNumHeld +" mCallState: " + callState.mCallState);
         Log.d(TAG,"mNumber: " + callState.mNumber + " mType: " + callState.mType);
         //Process the call indicators
         updateCallState(callState.mNumActive, callState.mNumHeld,
             callState.mCallState, callState.mNumber, callState.mType);
 
         if (mHeadsetType == TYPE_HEADSET){
             if(callState.mCallState == HeadsetHalConstants.CALL_STATE_INCOMING){
                 AtCommandResult result = new AtCommandResult(AtCommandResult.UNSOLICITED);
                 log("Ring for HSP..");
                 mStopRing = false; //Make sure we ring
                 result.addResult(ring());
                 sendURC(result.toString());
             } else if(callState.mCallState == HeadsetHalConstants.CALL_STATE_ALERTING){
                 log("Outgoing call, open SCO");
                 audioOn();
             } else if (callState.mCallState == HeadsetHalConstants.CALL_STATE_IDLE){
                 log("Idle call state");
                 if((callState.mNumActive > 0) || (callState.mNumHeld > 0)){
                     log("An active or held call, open audio, if not opened");
                     audioOn();
                 }
             }
         }
     }
 
     private void sendCallCiev(int value){
         if(sendUpdate()){
             sendURC("+CIEV: 2," + value);
         }
     }
 
     private void sendCallsetupCiev(int value){
         if(sendUpdate()){
             sendURC("+CIEV: 3," + value);
         }
     }
 
     private void sendCallHeldCiev(int value){
         if(sendUpdate()){
             sendURC("+CIEV: 4," + value);
         }
     }
 
     private void updateCallState(int numActive, int numHeld, int callState,
                                  String number, int type) {
 
         log("update call, send proper value");
         boolean callConnected = false;
         //TODO
         // call state changed
         if (callState != prevCallState) {
             log("Call State changed recieved");
             if (callState == HeadsetHalConstants.CALL_STATE_INCOMING){
                 log("Incoming call.It could be incoming or waiting");
                 AtCommandResult result = new AtCommandResult(AtCommandResult.UNSOLICITED);
                 if((numActive == 1) || (numHeld == 1)) {
                     log("AN call is already present, this is a waiting call");
                     if(number != null){
                         log("prepare the CCWA string here");
                         if ((mRemoteBrsf & BRSF_HF_CW_THREE_WAY_CALLING) != 0x0) {
                             log("CCWA");
                             result.addResponse("+CCWA: \"" + number + "\"," + type);
                             sendCallsetupCiev(1);
                         }
                     }
                 } else {
                     log("Fresh incoming call");
                     if ((mLocalBrsf & BRSF_AG_IN_BAND_RING) != 0x0) {
                         //mCallStartTime = System.currentTimeMillis();TODO
                         audioOn();
                     }
                     mStopRing = false; //Make sure we ring
                     sendCallsetupCiev(1);
                     result.addResult(ring()); //It will send RING and CLIP
                 }
                 sendURC(result.toString()); //Send the values
             } else if (callState == HeadsetHalConstants.CALL_STATE_DIALING){
                 log("Dialing a outgoing call");
                 sendCallsetupCiev(2);
             } else if (callState == HeadsetHalConstants.CALL_STATE_ALERTING){
                 log("Remote phone is being alerted Need to start the SCO if not yet started");
                 sendCallsetupCiev(3);
                 audioOn();
             } else if(callState == HeadsetHalConstants.CALL_STATE_IDLE) {
                 log("CALL_STATE_IDLE (active, held and idle)");
                 switch(prevCallState){
                     case  HeadsetHalConstants.CALL_STATE_INCOMING:
                         log("prev was an incoming call");
                         if((numActive == 1) && (prevNumActive == 0)){
                             log("Incoming call connected, connect audio, if not");
                             callConnected = true;
                             //SsendURC("+CIEV: 3,0");
                             audioOn();
                         } else if ((numHeld == 1) && (prevNumHeld == 0)){
                             log("The incoming call is made to hold");
                             if(numHeld == 1)
                                 sendCallsetupCiev(0);
                         } else{
                             log("Incoming call is rejected");
                             sendCallsetupCiev(0);
                         }
                         break;
                     case  HeadsetHalConstants.CALL_STATE_DIALING:
                         log("A dialing call..It should not come in regular call scenario ");
                         sendCallsetupCiev(0);
                         break;
                     case  HeadsetHalConstants.CALL_STATE_ALERTING:
                         log("Prev state was call alert, it may be accepted or rejected");
                         if((numActive == 1) && (prevNumActive == 0)){
                             log("Remote accepted the call");
                             //TODO..Check if audio is established or not
                             callConnected = true;
                             //sendURC("+CIEV: 3,0");
                         } else{
                             log("Call rejected by remote or disconnected");
                             sendCallsetupCiev(0);
                             //Send +CIEV:3,0
                             //Close SCO if open
                         }
                         break;
                 }
             }
         }
         if (numActive != prevNumActive) {
             log("Active call state changed");
             if((numHeld + prevNumHeld) == 0) {
                 sendCallCiev(numActive);
             }
             if (callConnected == true) {
                 sendCallsetupCiev(0);
                 /*THis will be true when an ougoing call is active and prev call is
                  still on hold. Transition happens when call is accepted.
                  Transition from Alert to active will make sure that held call
                  state is changed from 4,2 to 4,1 */
                 if ((numHeld + prevNumHeld) == 2)
                     sendCallHeldCiev(1);
             }
         }
         if (numHeld != prevNumHeld){
             log("Held state changed");
             if (numHeld == 0) {
                 sendCallHeldCiev(0);//callheld = 0;
                 if ((numActive + prevNumActive) == 0)
                     sendCallCiev(0); //An held call was disonnected
             }
             else {
                 if (numActive == 0)
                     sendCallHeldCiev(2);
                 else
                     sendCallHeldCiev(1);
             }
         }
         //CAll swap happened when no state change observed.
         if ((callState == prevCallState) && ((numActive == 1) && (numHeld == 1)) &&
             (numActive == prevNumActive) && (numHeld == prevNumHeld)){
             //Send +CIEV:4,1
             sendCallHeldCiev(1);
             log("Call swapped");
         }
         //Save the current values
         prevNumActive = numActive;
         prevCallState = callState;
         prevNumHeld = numHeld;
     }
 
     public void phoneStateChanged(HeadsetCallState newState){
         log("Phone State is changed, send update to headset");
         Message msg = mHandler.obtainMessage(CALL_STATE_CHANGED, newState);
         mHandler.sendMessage(msg);
     }
 
     public void roamChanged(boolean roam) {
         log("Roaming is changed, send update to headset");
         mPhoneState.setRoam(roam ? HeadsetHalConstants.SERVICE_TYPE_ROAMING :
                                    HeadsetHalConstants.SERVICE_TYPE_HOME);
     }
 
     public void clccResponse(int index, int direction, int status, int mode,
                              boolean mpty,String number, int type) {
         int mult = 0;
         if(mpty == true)
             mult = 1;
         else mult = 0;
 
         log("CLCC response, send update to headset");
         AtCommandResult result = new AtCommandResult(AtCommandResult.UNSOLICITED);
         if(index == 0){
             Log.d(TAG, "Send OK only");
             sendURC("OK");
             return;
         }
         String args = "+CLCC: " + index + "," + direction + "," +
                       status + ",0," + mult;
         if (number != null) {
             args += ",\"" + number + "\"," + type;
         }
         result.addResponse(args);
         sendURC(result.toString());
     }
 
     //Sends the device state changed states
     //We send service, roam and CREG reponse here.
     public synchronized void updateServiceState(int service) {
         AtCommandResult result = new AtCommandResult(AtCommandResult.UNSOLICITED);
         if (sendUpdate() && mIIEnabled[1]) {
             log("Service State Changed");
             result.addResponse("+CIEV: 1," + service);
         }
         sendURC(result.toString());
     }
 
     public synchronized void updateRoamState(int roam) {
         AtCommandResult result = new AtCommandResult(AtCommandResult.UNSOLICITED);
         log("Send the roam update to the device");
         if (sendUpdate() && mIIEnabled[6]) {
             log("Roam State Changed");
             result.addResponse("+CIEV: 6," + roam);
         }
         sendURC(result.toString());
     }
 
     public synchronized void updateCregState(String creg) {
         if (sendUpdate())
             sendURC(creg);
     }
 
     public synchronized void updateSignalState(int signal) {
         if (sendUpdate() && mIIEnabled[5]){
             log("Signal State Changed");
             sendURC("+CIEV: 5," + signal);
         }
     }
 
     private synchronized void updateBatteryState(Intent intent){
         int batteryLevel = intent.getIntExtra("level", -1);
         int scale = intent.getIntExtra("scale", -1);
         if (batteryLevel == -1 || scale == -1) {
             return;  // ignore
         }
         log("Battery State Changed, send update" + batteryLevel + "Scale: " + scale);
         batteryLevel = batteryLevel * 5 / scale;
         if(mPhoneState.getBatteryCharge() != batteryLevel){
             mPhoneState.setBatteryCharge(batteryLevel);
             if (sendUpdate() && mIIEnabled[7]) {
                 log("Battery State Changed");
                 sendURC("+CIEV: 7," + batteryLevel);
             }
         }
     }
 
     private AtCommandResult ring() {
         if (sendRingUpdate()) {
             AtCommandResult result = new AtCommandResult(AtCommandResult.UNSOLICITED);
             result.addResponse("RING");
             if (sendClipUpdate()) {
                 String number = mPhoneState.getNumber();
                 int type = mPhoneState.getPhoneType();
                 result.addResponse("+CLIP: \"" + number + "\"," + type);
             }
             Message msg = Message.obtain(mHandler, RING);
             mHandler.sendMessageDelayed(msg, 3000);
             return result;
         }
         return null;
     }
 
     private boolean sendClipUpdate() {
         if (isHeadsetConnected() && mHeadsetType == TYPE_HANDSFREE &&
             mServiceConnectionEstablished){
             if(mPhoneState.getClip())
             return true;
         }
         return false;
     }
 
     private boolean sendRingUpdate() {
         if (isHeadsetConnected() && !mStopRing &&
             mPhoneState.getCallState() == HeadsetHalConstants.CALL_STATE_INCOMING) {
             if (mHeadsetType == TYPE_HANDSFREE) {
                 return mServiceConnectionEstablished ? true : false;
             }
             return true;
         }
         return false;
     }
 
     private void stopRing() {
         mStopRing = true;
     }
 
     /*
      * Put the AT command, company ID, arguments, and device in an Intent and broadcast it.
      */
     private void broadcastVendorSpecificEventIntent(String command,
                                                     int companyId,
                                                     int commandType,
                                                     Object[] arguments,
                                                     BluetoothDevice device) {
         if (VDBG) log("broadcastVendorSpecificEventIntent(" + command + ")");
         Intent intent =
                 new Intent(BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT);
         intent.putExtra(BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD, command);
         intent.putExtra(BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD_TYPE,
                         commandType);
         // assert: all elements of args are Serializable
         intent.putExtra(BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_ARGS, arguments);
         intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
 
         intent.addCategory(BluetoothHeadset.VENDOR_SPECIFIC_HEADSET_EVENT_COMPANY_ID_CATEGORY
             + "." + Integer.toString(companyId));
 
         mContext.sendBroadcast(intent, android.Manifest.permission.BLUETOOTH);
     }
 
     /** Request to establish SCO (audio) connection to bluetooth
      * headset/handsfree, if one is connected. Does not block.
      * Returns false if the user has requested audio off, or if there
      * is some other immediate problem that will prevent BT audio.
      */
     /* package */ synchronized boolean audioOn() {
         if (VDBG) log("audioOn()");
         if (!isHeadsetConnected()) {
             if (DBG) log("audioOn(): headset is not connected!");
             return false;
         }
         if (mHeadsetType == TYPE_HANDSFREE && !mServiceConnectionEstablished) {
             if (DBG) log("audioOn(): service connection not yet established!");
             return false;
         }
 
         if (mConnectedSco != null) {
             if (DBG) log("audioOn(): audio is already connected");
             return true;
         }
 
         if (!mUserWantsAudio) {
             if (DBG) log("audioOn(): user requested no audio, ignoring");
             return false;
         }
 
         if (mPendingScoForA2dp) {
             if (DBG) log("audioOn(): SCO already pending for A2DP");
             return true;
         }
 
         if (mPendingScoForWbs) {
             if (DBG) log("audioOn(): SCO already pending for WBS");
             return true;
         }
 
         mA2dpSuspended = false;
         mPendingScoForA2dp = false;
         mPendingA2dpResume = false;
         if ( mA2dpState == BluetoothA2dp.STATE_PLAYING) {
             if (DBG) log("suspending A2DP stream for SCO");
             mA2dpSuspended = mA2dp.suspendSink(mA2dpDevice);
             if (mA2dpSuspended) {
                 mPendingScoForA2dp = true;
                 Message msg = mHandler.obtainMessage(MESSAGE_CHECK_PENDING_SCO);
                 mHandler.sendMessageDelayed(msg, 2000);
             } else {
                 Log.w(TAG, "Could not suspend A2DP stream for SCO, going ahead with SCO");
             }
         }
 
         if (!mPendingScoForA2dp) {
             mPendingScoForWbs = false;
             if ((0x0 != (mRemoteBrsf & BRSF_HF_CODEC_NEGOTIATION)) &&
                 (0x0 != (mLocalBrsf & BRSF_AG_CODEC_NEGOTIATION))) {
                 if (0x0 == mRemoteCodec) {
                     if (0x0 != (mRemoteAvailableCodecs & CODEC_MSBC)) {
                         if (DBG) Log.d(TAG, "+BCS:2");
                             mLocalCodec = CODEC_MSBC;
                             sendURC("+BCS:2");
                         } else {
                            if (DBG) Log.d(TAG, "+BCS:1");
                             mLocalCodec = CODEC_CVSD;
                             sendURC("+BCS:1");
                         }
                     mPendingScoForWbs = true;
                     mExpectingBCS = true;
                     Message msg = mHandler.obtainMessage(
                                            CODEC_CONNECTION_SETUP_TIMEOUT);
                     mHandler.sendMessageDelayed(msg,
                              CODEC_NEGOTIATION_SETUP_TIMEOUT_VALUE);
                 } else {
                     if (0x0 != (mRemoteAvailableCodecs & CODEC_MSBC)) {
                         mAudioManager.setParameters(HEADSET_SAMPLERATE + "=16000");
                         connectScoThread(true); //Start SCO for mSBC
                     }
                     else {
                         mAudioManager.setParameters(HEADSET_SAMPLERATE + "=8000");
                         connectScoThread(false); //Start SCO for CVSD
                     }
                 }
             } else {
                 mAudioManager.setParameters(HEADSET_SAMPLERATE + "=8000");
                 connectScoThread(false); //Start SCO for CVSD
             }
         }
 
         return true;
     }
 
     /** Request to disconnect SCO (audio) connection to bluetooth
      * headset/handsfree, if one is connected. Does not block.
      */
     /* package */ synchronized void audioOff() {
         if (VDBG) log("audioOff(): mPendingScoForA2dp: " + mPendingScoForA2dp +
                 ", mPendingScoForWbs: " + mPendingScoForWbs +
                 ", mConnectedSco: " + mConnectedSco +
                 ", mA2dpState: " + mA2dpState +
                 ", mA2dpSuspended: " + mA2dpSuspended);
 
         if (!mPendingScoForA2dp) {
             if (mA2dpSuspended) {
                 if (DBG) log("resuming A2DP stream after disconnecting SCO");
                 mA2dp.resumeSink(mA2dpDevice);
                 mA2dpSuspended = false;
             }
         } else { // already suspendSink is in progress, so wait for issuing resume
             mPendingA2dpResume = true;
         }
 
         mPendingScoForA2dp = false;
         mPendingScoForWbs = false;
 
         if (mSignalScoCloseThread != null) {
             mSignalScoCloseThread.shutdown();
             mSignalScoCloseThread = null;
         }
 
         // Sync with setting mConnectScoThread to null to assure the validity of
         // the condition
         synchronized (ScoSocketConnectThread.class) {
             if (mConnectScoThread != null) {
                 mConnectScoThread.shutdown();
                 resetConnectScoThread();
             }
         }
 
         disconnectScoThread();    // Should be closed already, but just in case
     }
      //TODO. Now we have a new API called isAudioOn which is
      // a part of BluetoothHeadset aidl. this will be called from there.
     /* package */ boolean isAudioOn() {
         synchronized(BluetoothHandsfree.this) {
             return (mConnectedSco != null);
         }
     }
 
     private boolean isA2dpMultiProfile() {
         return mA2dp != null && mHeadset != null && mA2dpDevice != null &&
                 mA2dpDevice.equals(mHeadset.getRemoteDevice());
     }
 
     private void sendURC(String urc) {
         if (isHeadsetConnected()) {
             mHeadset.sendURC(urc);
         }
     }
 
     /** helper to redial last dialled number */
     private AtCommandResult redial() {
         String number = mPhonebook.getLastDialledNumber();
         if (number == null) {
             // spec seems to suggest sending ERROR if we dont have a
             // number to redial
             if (VDBG) log("Bluetooth redial requested (+BLDN), but no previous " +
                   "outgoing calls found. Ignoring");
             return new AtCommandResult(AtCommandResult.ERROR);
         }
         log("The number to dial is:" +number);
         // Outgoing call initiated by the handsfree device
         // Send terminateScoUsingVirtualVoiceCall
         terminateScoUsingVirtualVoiceCall();
         Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                 Uri.fromParts(SCHEME_TEL, number, null));
         intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         // this extra is to check if the CALL_PRIVILEGED intent is
         // from Bluetooth, thereby, avoid prompting the user for subs
         // when Prompt is selected in MultiSim scenario.
         intent.putExtra(BLUETOOTH, "true");
         mContext.startActivity(intent);
 
         // We do not immediately respond OK, wait until we get a phone state
         // update. If we return OK now and the handsfree immeidately requests
         // our phone state it will say we are not in call yet which confuses
         // some devices
         expectCallStart();
         return new AtCommandResult(AtCommandResult.UNSOLICITED);  // send nothing
     }
 
     /*
      * Register a vendor-specific command.
      * @param commandName the name of the command.  For example, if the expected
      * incoming command is <code>AT+FOO=bar,baz</code>, the value of this should be
      * <code>"+FOO"</code>.
      * @param companyId the Bluetooth SIG Company Identifier
      * @param parser the AtParser on which to register the command
      */
     private void registerVendorSpecificCommand(String commandName,
                                                int companyId,
                                                AtParser parser) {
         parser.register(commandName,
                         new VendorSpecificCommandHandler(commandName, companyId));
     }
 
     /*
      * Register all vendor-specific commands here.
      */
     private void registerAllVendorSpecificCommands() {
         AtParser parser = mHeadset.getAtParser();
 
         // Plantronics-specific headset events go here
         registerVendorSpecificCommand("+XEVENT",
                                       BluetoothAssignedNumbers.PLANTRONICS,
                                       parser);
     }
 
     /**
      * Register AT Command handlers to implement the Headset profile
      */
     private void initializeHeadsetAtParser() {
         if (VDBG) log("Registering Headset AT commands");
         AtParser parser = mHeadset.getAtParser();
         // Headsets usually only have one button, which is meant to cause the
         // HS to send us AT+CKPD=200 or AT+CKPD.
 
         parser.register("+CKPD", new AtCommandHandler() {
             private AtCommandResult headsetButtonPress() {
                if (mPhoneState.getCallState() == HeadsetHalConstants.CALL_STATE_INCOMING) {
                    if (mPhoneProxy != null) {
                        try {
                            stopRing();
                            sendURC("OK");
                            mPhoneProxy.answerCall();
                            audioOn();
                        } catch (RemoteException e) {
                            Log.e(TAG, Log.getStackTraceString(new Throwable()));
                        }
                        return new AtCommandResult(AtCommandResult.UNSOLICITED);
                    } else {
                        Log.e(TAG, "Handsfree phone proxy null for answering call");
                    }
                } else if (mPhoneState.getNumActiveCall() > 0) {
                    if (!isAudioOn()){
                        audioOn();
                    }
                    else {
                        if (mHeadset.getDirection() == HeadsetBase.DIRECTION_INCOMING &&
                        (System.currentTimeMillis() - mHeadset.getConnectTimestamp()) < 5000) {
                            // Headset made a recent ACL connection to us - and
                            // made a mandatory AT+CKPD request to connect
                            // audio which races with our automatic audio
                            // setup.  ignore
                        } else if (mPhoneProxy != null) {
                            try {
                                audioOff();
                                mPhoneProxy.hangupCall();
                            } catch (RemoteException e) {
                                Log.e(TAG, Log.getStackTraceString(new Throwable()));
                            }
                        } else {
                            Log.e(TAG, "Handsfree phone proxy null for hangup call");
                        }
                    }
                    return new AtCommandResult(AtCommandResult.OK);
                } else {
                    return redial();
                }
                //No response yet
                return new AtCommandResult(AtCommandResult.UNSOLICITED);
             }
 
             @Override
             public AtCommandResult handleActionCommand() {
                 return headsetButtonPress();
             }
             @Override
             public AtCommandResult handleSetCommand(Object[] args) {
                 return headsetButtonPress();
             }
         });
         // Speaker Gain
         parser.register("+VGS", new AtCommandHandler() {
             @Override
             public AtCommandResult handleSetCommand(Object[] args) {
                 // AT+VGS=<gain>    in range [0,15]
                 if (args.length != 1 || !(args[0] instanceof Integer)) {
                     return new AtCommandResult(AtCommandResult.ERROR);
                 }
                 mScoGain = (Integer) args[0];
                 int flag =  mAudioManager.isBluetoothScoOn() ? AudioManager.FLAG_SHOW_UI:0;
 
                 mAudioManager.setStreamVolume(AudioManager.STREAM_BLUETOOTH_SCO, mScoGain, flag);
                 return new AtCommandResult(AtCommandResult.OK);
             }
         });
         parser.register("+VGM", new AtCommandHandler() {
             @Override
             public AtCommandResult handleSetCommand(Object[] args) {
                 // AT+VGM=<gain>    in range [0,15]
                 // Headset/Handsfree is reporting its current gain setting
                 return new AtCommandResult(AtCommandResult.OK);
             }
         });
     }
 
     /**
      * Register AT Command handlers to implement the Handsfree profile
      */
     private void initializeHandsfreeAtParser() {
         if (VDBG) log("Registering Handsfree AT commands");
         AtParser parser = mHeadset.getAtParser();
         //final Phone phone = mCM.getDefaultPhone();
 
         // Answer
         parser.register('A', new AtCommandHandler() {
             @Override
             public AtCommandResult handleBasicCommand(String args) {
                 sendURC("OK");
                 stopRing();
                 if (mPhoneProxy != null) {
                     log("Answer the call");
                     try {
                         mPhoneProxy.answerCall();
                     } catch (RemoteException e) {
                        Log.e(TAG, Log.getStackTraceString(new Throwable()));
                     }
                 } else {
                     Log.e(TAG, "Handsfree phone proxy null for hanging up call");
                 }
                 return new AtCommandResult(AtCommandResult.UNSOLICITED);
             }
         });
         parser.register('D', new AtCommandHandler() {
             @Override
             public AtCommandResult handleBasicCommand(String args) {
                 if (args.length() > 0) {
                     if (args.charAt(0) == '>') {
                         // Yuck - memory dialling requested.
                         // Just dial last number for now
                         if (args.startsWith(">9999")) {   // for PTS test
                             return new AtCommandResult(AtCommandResult.ERROR);
                         }
                         return redial();
                     } else {
                         // Send terminateScoUsingVirtualVoiceCall
                         terminateScoUsingVirtualVoiceCall();
                         // Remove trailing ';'
                         if (args.charAt(args.length() - 1) == ';') {
                             args = args.substring(0, args.length() - 1);
                         }
 
                         args = PhoneNumberUtils.convertPreDial(args);
 
                         Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                                 Uri.fromParts(SCHEME_TEL, args, null));
                         intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                         mContext.startActivity(intent);
 
                         expectCallStart();
                         return new AtCommandResult(AtCommandResult.UNSOLICITED);  // send nothing
                     }
                 }
                 return new AtCommandResult(AtCommandResult.ERROR);
             }
         });
 
         // Hang-up command
         parser.register("+CHUP", new AtCommandHandler() {
             @Override
             public AtCommandResult handleActionCommand() {
                 sendURC("OK");
                 if (isVirtualCallInProgress()) {
                     terminateScoUsingVirtualVoiceCall();
                 } else /*{
                     if (mCM.hasActiveFgCall()) {
                         PhoneUtils.hangupActiveCall(mCM.getActiveFgCall());
                     } else if (mCM.hasActiveRingingCall()) {
                         PhoneUtils.hangupRingingCall(mCM.getFirstActiveRingingCall());
                     } else if (mCM.hasActiveBgCall()) {
                         PhoneUtils.hangupHoldingCall(mCM.getFirstActiveBgCall());
                     }
                 }*/
                 {
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
                 return new AtCommandResult(AtCommandResult.UNSOLICITED);
             }
         });
 
         // Bluetooth Retrieve Supported Features command
         parser.register("+BRSF", new AtCommandHandler() {
             private AtCommandResult sendBRSF() {
                 return new AtCommandResult("+BRSF: " + mLocalBrsf);
             }
             @Override
             public AtCommandResult handleSetCommand(Object[] args) {
                 // AT+BRSF=<handsfree supported features bitmap>
                 // Handsfree is telling us which features it supports. We
                 // send the features we support
                 if (args.length == 1 && (args[0] instanceof Integer)) {
                     mRemoteBrsf = (Integer) args[0];
                     if ((mRemoteBrsf & BRSF_HF_REMOTE_VOL_CONTROL) == 0x0) {
                         Log.i(TAG, " remote volume control not supported ");
                         mAudioManager.setParameters(HEADSET_VGS+"=off");
                     } else {
                         mAudioManager.setParameters(HEADSET_VGS+"=on");
                     }
                 } else {
                     Log.w(TAG, "HF didn't sent BRSF assuming 0");
                 }
                 return sendBRSF();
             }
             @Override
             public AtCommandResult handleActionCommand() {
                 // This seems to be out of spec, but lets do the nice thing
                 return sendBRSF();
             }
             @Override
             public AtCommandResult handleReadCommand() {
                 // This seems to be out of spec, but lets do the nice thing
                 return sendBRSF();
             }
         });
 
         // Call waiting notification on/off
         parser.register("+CCWA", new AtCommandHandler() {
             @Override
             public AtCommandResult handleActionCommand() {
                 // Seems to be out of spec, but lets return nicely
                 return new AtCommandResult(AtCommandResult.OK);
             }
             @Override
             public AtCommandResult handleReadCommand() {
                 // Call waiting is always on
                 return new AtCommandResult("+CCWA: 1");
             }
             @Override
             public AtCommandResult handleSetCommand(Object[] args) {
                 // AT+CCWA=<n>
                 // Handsfree is trying to enable/disable call waiting. We
                 // cannot disable in the current implementation.
                 return new AtCommandResult(AtCommandResult.OK);
             }
             @Override
             public AtCommandResult handleTestCommand() {
                 // Request for range of supported CCWA paramters
                 return new AtCommandResult("+CCWA: (\"n\",(1))");
             }
         });
 
         // Mobile Equipment Event Reporting enable/disable command
         // Of the full 3GPP syntax paramters (mode, keyp, disp, ind, bfr) we
         // only support paramter ind (disable/enable evert reporting using
         // +CDEV)
         parser.register("+CMER", new AtCommandHandler() {
             @Override
             public AtCommandResult handleReadCommand() {
                 return new AtCommandResult(
                         "+CMER: 3,0,0," + (mIndicatorsEnabled ? "1" : "0"));
             }
             @Override
             public AtCommandResult handleSetCommand(Object[] args) {
                 if (args.length < 4) {
                     // This is a syntax error
                     return new AtCommandResult(AtCommandResult.ERROR);
                 } else if (args[0].equals(3) && args[1].equals(0) &&
                            args[2].equals(0)) {
                     boolean valid = false;
                     if (args[3].equals(0)) {
                         mIndicatorsEnabled = false;
                         valid = true;
                     } else if (args[3].equals(1)) {
                         mIndicatorsEnabled = true;
                         valid = true;
                     }
                     if (valid) {
                         if ((mRemoteBrsf & BRSF_HF_CW_THREE_WAY_CALLING) == 0x0) {
                             mServiceConnectionEstablished = true;
                             sendURC("OK");  // send immediately, then initiate audio
                             broadcastSlcEstablished();
                             log("HFP SLC is established");
                             return new AtCommandResult(AtCommandResult.UNSOLICITED);
                         } else {
                             return new AtCommandResult(AtCommandResult.OK);
                         }
                     }
                 }
                 return reportCmeError(BluetoothCmeError.OPERATION_NOT_SUPPORTED);
             }
             @Override
             public AtCommandResult handleTestCommand() {
                 return new AtCommandResult("+CMER: (3),(0),(0),(0-1)");
             }
         });
 
         // Mobile Equipment Error Reporting enable/disable
         parser.register("+CMEE", new AtCommandHandler() {
             @Override
             public AtCommandResult handleActionCommand() {
                 // out of spec, assume they want to enable
                 mCmee = true;
                 return new AtCommandResult(AtCommandResult.OK);
             }
             @Override
             public AtCommandResult handleReadCommand() {
                 return new AtCommandResult("+CMEE: " + (mCmee ? "1" : "0"));
             }
             @Override
             public AtCommandResult handleSetCommand(Object[] args) {
                 // AT+CMEE=<n>
                 if (args.length == 0) {
                     // <n> ommitted - default to 0
                     mCmee = false;
                     return new AtCommandResult(AtCommandResult.OK);
                 } else if (!(args[0] instanceof Integer)) {
                     // Syntax error
                     return new AtCommandResult(AtCommandResult.ERROR);
                 } else {
                     mCmee = ((Integer)args[0] == 1);
                     return new AtCommandResult(AtCommandResult.OK);
                 }
             }
             @Override
             public AtCommandResult handleTestCommand() {
                 // Probably not required but spec, but no harm done
                 return new AtCommandResult("+CMEE: (0-1)");
             }
         });
 
         // Bluetooth Last Dialled Number
         parser.register("+BLDN", new AtCommandHandler() {
             @Override
             public AtCommandResult handleActionCommand() {
                 return redial();
             }
         });
 
         // Indicator Update command
         parser.register("+CIND", new AtCommandHandler() {
             @Override
             public AtCommandResult handleReadCommand() {
                 AtCommandResult result = new AtCommandResult(AtCommandResult.OK);
                 String status = mPhoneState.toCindResult();
                 log("The result for CIND is" + status);
                 result.addResponse(status);
                 return result;
             }
             @Override
             public AtCommandResult handleTestCommand() {
                 AtCommandResult result = new AtCommandResult(AtCommandResult.OK);
                 String status = mPhoneState.getCindTestResult();
                 log("The result for test CIND is" + status);
                 result.addResponse(status);
                 return result;
             }
         });
 
         // Query Signal Quality (legacy)
         parser.register("+CSQ", new AtCommandHandler() {
            private synchronized AtCommandResult toCsqResult() {
                AtCommandResult result = new AtCommandResult(AtCommandResult.OK);
                String status = "+CSQ: " + mPhoneState.getRssi()+ ",99";
                result.addResponse(status);
                return result;
            }
             @Override
             public AtCommandResult handleActionCommand() {
                 return toCsqResult();
             }
         });
 
         // Query network registration state
         parser.register("+CREG", new AtCommandHandler() {
             @Override
             public AtCommandResult handleReadCommand() {
                 updateCregState(mPhoneState.toCregString());
                 return new AtCommandResult(AtCommandResult.UNSOLICITED);
             }
         });
 
         // Send DTMF. I don't know if we are also expected to play the DTMF tone
         // locally, right now we don't
         parser.register("+VTS", new AtCommandHandler() {
             @Override
             public AtCommandResult handleSetCommand(Object[] args) {
                 if (args.length >= 1) {
                     char c;
                     if (args[0] instanceof Integer) {
                         c = ((Integer) args[0]).toString().charAt(0);
                     } else {
                         c = ((String) args[0]).charAt(0);
                     }
                     if (isValidDtmf(c)) {
                          if (mPhoneProxy != null) {
                              try {
                                  Log.d(TAG, "Send DTMF" + c );
                                  mPhoneProxy.sendDtmf(c);
                                  return new AtCommandResult(AtCommandResult.OK);
                              } catch (RemoteException e) {
                                  Log.e(TAG, Log.getStackTraceString(new Throwable()));
                                  return new AtCommandResult(AtCommandResult.ERROR);
                              }
                         } else {
                             Log.e(TAG, "Handsfree phone proxy null for sending DTMF");
                             return new AtCommandResult(AtCommandResult.ERROR);
                         }
                     }
                 }
                 return new AtCommandResult(AtCommandResult.ERROR);
             }
             private boolean isValidDtmf(char c) {
                 switch (c) {
                 case '#':
                 case '*':
                     return true;
                 default:
                     if (Character.digit(c, 14) != -1) {
                         return true;  // 0-9 and A-D
                     }
                     return false;
                 }
             }
         });
 
         // List calls
         parser.register("+CLCC", new AtCommandHandler() {
             @Override
             public AtCommandResult handleActionCommand() {
             if (mPhoneProxy != null) {
                 Log.d(TAG,"AT+CLCC");
                 try {
                     if(isVirtualCallInProgress()) {
                         String phoneNumber = "";
                         int type = PhoneNumberUtils.TOA_Unknown;
                         try {
                             phoneNumber = mPhoneProxy.getSubscriberNumber();
                             type = PhoneNumberUtils.toaFromString(phoneNumber);
                         } catch (RemoteException ee) {
                             Log.e(TAG, "Unable to retrieve phone number"+"using IBluetoothHeadsetPhone proxy");
                             phoneNumber = "";
                         }
                         clccResponse(1, 0, 0, 0, false, phoneNumber, type);
                     }
                     else if (!mPhoneProxy.listCurrentCalls()) {
                         Log.d(TAG,"Phone proxy sent error");
                         clccResponse(0, 0, 0, 0, false, "", 0);
                     }
                 } catch (RemoteException e) {
                     Log.e(TAG, Log.getStackTraceString(new Throwable()));
                     clccResponse(0, 0, 0, 0, false, "", 0);
                 }
                 } else {
                    Log.e(TAG, "Handsfree phone proxy null for At+CLCC");
                    clccResponse(0, 0, 0, 0, false, "", 0);
                 }
                 return new AtCommandResult(AtCommandResult.UNSOLICITED);
             }
         });
 
         // Call Hold and Multiparty Handling command
         parser.register("+CHLD", new AtCommandHandler() {
             @Override
             public AtCommandResult handleSetCommand(Object[] args) {
             //TODO. Pass the arguments properly.
                 if (args.length >= 1) {
                     int chld = 0;
                     if (args[0].equals(0)) chld = 0;
                     else if (args[0].equals(1)) chld = 1;
                     else if (args[0].equals(2)) chld = 2;
                     else if (args[0].equals(3)) chld = 3;
                     else return new AtCommandResult(AtCommandResult.ERROR);
                     if (mPhoneProxy != null) {
                         try {
                             if (mPhoneProxy.processChld(chld)) {
                                 return new AtCommandResult(AtCommandResult.OK);
                             } else {
                                 return new AtCommandResult(AtCommandResult.ERROR);
                             }
                         } catch (RemoteException e) {
                             Log.e(TAG, Log.getStackTraceString(new Throwable()));
                             return new AtCommandResult(AtCommandResult.ERROR);
                         }
                     } else {
                         Log.e(TAG, "Handsfree phone proxy null for At+Chld");
                         return new AtCommandResult(AtCommandResult.ERROR);
                     }
                 }
                 return new AtCommandResult(AtCommandResult.OK);
             }
             @Override
             public AtCommandResult handleTestCommand() {
                 mServiceConnectionEstablished = true;
                 sendURC("+CHLD: (0,1,2,3)");
                 sendURC("OK");  // send reply first, then connect audio
                 broadcastSlcEstablished();
                 log("SLC is established after CHLD exchange");
                 // already replied
                 return new AtCommandResult(AtCommandResult.UNSOLICITED);
             }
         });
 
         // Get Network operator name
         parser.register("+COPS", new AtCommandHandler() {
             @Override
             public AtCommandResult handleReadCommand() {
                 //TODO. Added the code , but need to see if any issue
                 if (mPhoneProxy != null) {
                    try {
                        String operatorName = mPhoneProxy.getNetworkOperator();
                        if (operatorName != null) {
                            if (operatorName.length() > 16) {
                                operatorName = operatorName.substring(0, 16);
                            }
                            return new AtCommandResult("+COPS: 0,0,\"" + operatorName + "\"");
                        } else {
                            return new AtCommandResult("+COPS: 0");
                        }
                    } catch (RemoteException e) {
                        Log.e(TAG, Log.getStackTraceString(new Throwable()));
                        return new AtCommandResult("+COPS: 0");
                    }
                 } else {
                     Log.e(TAG, "Handsfree phone proxy null for At+COPS");
                     return new AtCommandResult("+COPS: 0");
                 }
             }
             @Override
             public AtCommandResult handleSetCommand(Object[] args) {
                 // Handsfree only supports AT+COPS=3,0
                 if (args.length != 2 || !(args[0] instanceof Integer)
                     || !(args[1] instanceof Integer)) {
                     // syntax error
                     return new AtCommandResult(AtCommandResult.ERROR);
                 } else if ((Integer)args[0] != 3 || (Integer)args[1] != 0) {
                     return reportCmeError(BluetoothCmeError.OPERATION_NOT_SUPPORTED);
                 } else {
                     return new AtCommandResult(AtCommandResult.OK);
                 }
             }
             @Override
             public AtCommandResult handleTestCommand() {
                 // Out of spec, but lets be friendly
                 return new AtCommandResult("+COPS: (3),(0)");
             }
         });
 
         // Mobile PIN
         // AT+CPIN is not in the handsfree spec (although it is in 3GPP)
         parser.register("+CPIN", new AtCommandHandler() {
             @Override
             public AtCommandResult handleReadCommand() {
                 return new AtCommandResult("+CPIN: READY");
             }
         });
 
         // Bluetooth Response and Hold
         // Only supported on PDC (Japan) and CDMA networks.
         parser.register("+BTRH", new AtCommandHandler() {
             @Override
             public AtCommandResult handleReadCommand() {
                 // Replying with just OK indicates no response and hold
                 // features in use now
                 return new AtCommandResult(AtCommandResult.OK);
             }
             @Override
             public AtCommandResult handleSetCommand(Object[] args) {
                 // Neeed PDC or CDMA
                 return new AtCommandResult(AtCommandResult.ERROR);
             }
         });
 
         // Request International Mobile Subscriber Identity (IMSI)
         // Not in bluetooth handset spec
         parser.register("+CIMI", new AtCommandHandler() {
             @Override
             public AtCommandResult handleActionCommand() {
                 return new AtCommandResult(AtCommandResult.ERROR);
             }
         });
 
         // Calling Line Identification Presentation
         parser.register("+CLIP", new AtCommandHandler() {
             @Override
             public AtCommandResult handleReadCommand() {
                 // Currently assumes the network is provisioned for CLIP
                 return new AtCommandResult("+CLIP: " + (mPhoneState.getClip() ? "1" : "0") + ",1");
             }
             @Override
             public AtCommandResult handleSetCommand(Object[] args) {
                 // AT+CLIP=<n>
                 if (args.length >= 1 && (args[0].equals(0) || args[0].equals(1))) {
                     mPhoneState.setClip((Integer)args[0]);
                     return new AtCommandResult(AtCommandResult.OK);
                 } else {
                     return new AtCommandResult(AtCommandResult.ERROR);
                 }
             }
             @Override
             public AtCommandResult handleTestCommand() {
                 return new AtCommandResult("+CLIP: (0-1)");
             }
         });
 
         // AT+CGSN - Returns the device IMEI number.
         parser.register("+CGSN", new AtCommandHandler() {
             @Override
             public AtCommandResult handleActionCommand() {
                 // Get the IMEI of the device.
                 // phone will not be NULL at this point.
                 //Not supported in MR1, return ERROR
                 return new AtCommandResult(AtCommandResult.ERROR);
             }
         });
 
         // AT+CGMM - Query Model Information
         parser.register("+CGMM", new AtCommandHandler() {
             @Override
             public AtCommandResult handleActionCommand() {
                 // Return the Model Information.
                 String model = SystemProperties.get("ro.product.model");
                 if (model != null) {
                     return new AtCommandResult("+CGMM: " + model);
                 } else {
                     return new AtCommandResult(AtCommandResult.ERROR);
                 }
             }
         });
 
         // AT+CGMI - Query Manufacturer Information
         parser.register("+CGMI", new AtCommandHandler() {
             @Override
             public AtCommandResult handleActionCommand() {
                 // Return the Model Information.
                 String manuf = SystemProperties.get("ro.product.manufacturer");
                 if (manuf != null) {
                     return new AtCommandResult("+CGMI: " + manuf);
                 } else {
                     return new AtCommandResult(AtCommandResult.ERROR);
                 }
             }
         });
 
         // Noise Reduction and Echo Cancellation control
         parser.register("+NREC", new AtCommandHandler() {
             @Override
             public AtCommandResult handleSetCommand(Object[] args) {
                 if (args.length == 1 && (args[0] instanceof Integer)) {
                     if (args[0].equals(0)) {
                         mAudioManager.setParameters(HEADSET_NREC+"=off");
                         return new AtCommandResult(AtCommandResult.OK);
                     } else if (args[0].equals(1)) {
                         mAudioManager.setParameters(HEADSET_NREC+"=on");
                         return new AtCommandResult(AtCommandResult.OK);
                     }
                 }
                 return new AtCommandResult(AtCommandResult.ERROR);
             }
         });
 
         // Voice recognition (dialing)
         parser.register("+BVRA", new AtCommandHandler() {
             @Override
             public AtCommandResult handleSetCommand(Object[] args) {
                 if (!BluetoothHeadset.isBluetoothVoiceDialingEnabled(mContext)) {
                     return new AtCommandResult(AtCommandResult.ERROR);
                 }
                 //TODO.. Need to add API to check if the call is active
                 if (args.length >= 1 && args[0].equals(1)) {
                     synchronized (BluetoothHandsfree.this) {
                         if (!isVoiceRecognitionInProgress() &&
                             !isCellularCallInProgress() &&
                             !isVirtualCallInProgress()) {
                             try {
                                 mContext.startActivity(sVoiceCommandIntent);
                             } catch (ActivityNotFoundException e) {
                                 return new AtCommandResult(AtCommandResult.ERROR);
                             }
                             expectVoiceRecognition();
                         } else {
                             return new AtCommandResult(AtCommandResult.ERROR);
                         }
                     }
                     return new AtCommandResult(AtCommandResult.UNSOLICITED);  // send nothing yet
                 } else if (args.length >= 1 && args[0].equals(0)) {
                     if (isVoiceRecognitionInProgress()) {
                         audioOff();
                     }
                     mHfInitiatedVrDeactivation = true;
                     return new AtCommandResult(AtCommandResult.OK);
                 }
                 return new AtCommandResult(AtCommandResult.ERROR);
             }
             @Override
             public AtCommandResult handleTestCommand() {
                 return new AtCommandResult("+BVRA: (0-1)");
             }
         });
 
         // Retrieve Subscriber Number
         parser.register("+CNUM", new AtCommandHandler() {
             @Override
             public AtCommandResult handleActionCommand() {
                 if (mPhoneProxy != null) {
                     try {
                         String number = mPhoneProxy.getSubscriberNumber();
                         if (number != null) {
                             return new AtCommandResult("+CNUM: ,\"" + number + "\"," +
                                 PhoneNumberUtils.toaFromString(number) + ",,4");
                         }
                     } catch (RemoteException e) {
                         Log.e(TAG, Log.getStackTraceString(new Throwable()));
                         return new AtCommandResult(AtCommandResult.ERROR);
                     }
                 } else {
                     Log.e(TAG, "Handsfree phone proxy null for At+CNUM");
                     return new AtCommandResult(AtCommandResult.ERROR);
                 }
                  return new AtCommandResult(AtCommandResult.OK);
             }
         });
 
         // Microphone Gain
         parser.register("+VGM", new AtCommandHandler() {
             @Override
             public AtCommandResult handleSetCommand(Object[] args) {
                 // AT+VGM=<gain>    in range [0,15]
                 // Headset/Handsfree is reporting its current gain setting
                 if (args.length != 1 || !(args[0] instanceof Integer)) {
                     return new AtCommandResult(AtCommandResult.ERROR);
                 }
                 if ((Integer) args[0] > 15) // Invalid VGM gain
                     return new AtCommandResult(AtCommandResult.ERROR);
                 return new AtCommandResult(AtCommandResult.OK);
             }
         });
 
         // Speaker Gain
         parser.register("+VGS", new AtCommandHandler() {
             @Override
             public AtCommandResult handleSetCommand(Object[] args) {
                 // AT+VGS=<gain>    in range [0,15]
                 if (args.length != 1 || !(args[0] instanceof Integer)) {
                     return new AtCommandResult(AtCommandResult.ERROR);
                 }
                 if ((Integer) args[0] > 15) // Invalid VGS gain
                     return new AtCommandResult(AtCommandResult.ERROR);
                 mScoGain = (Integer) args[0];
                 int flag =  mAudioManager.isBluetoothScoOn() ? AudioManager.FLAG_SHOW_UI:0;
 
                 mAudioManager.setStreamVolume(AudioManager.STREAM_BLUETOOTH_SCO, mScoGain, flag);
                 return new AtCommandResult(AtCommandResult.OK);
             }
         });
 
         // Phone activity status
         parser.register("+CPAS", new AtCommandHandler() {
             @Override
             public AtCommandResult handleActionCommand() {
             //TODO. Need to get how to handle this command as new command
                 int status = 0;
                 /*switch (mCM.getState()) {
                 case IDLE:
                     status = 0;
                     break;
                 case RINGING:
                     status = 3;
                     break;
                 case OFFHOOK:
                     status = 4;
                     break;
                 }*/
                 return new AtCommandResult("+CPAS: " + status);
             }
         });
 
         // End of AT commands for HFP 1.5
         if (mVersion < VERSION_1_6) {
             mPhonebook.register(parser);
             return;
         }
 
         // AT commands only for HFP 1.6 below this line
         // Codec Connection
         parser.register("+BCC", new AtCommandHandler() {
             @Override
             public AtCommandResult handleActionCommand() {
                 if (DBG) Log.d(TAG, "Receiving AT+BCC from HF, sending OK to HF");
                 sendURC("OK");
                 mRemoteCodec = 0x0;
                 if (0x0 != (mLocalBrsf & BRSF_AG_CODEC_NEGOTIATION)) {
                     mExpectingBCS = true;
                     if (0x0 != (mRemoteAvailableCodecs & CODEC_MSBC)) {
                         mLocalCodec = CODEC_MSBC;
                         if (DBG) Log.d(TAG, "Sending +BCS:2 to HF");
                         sendURC("+BCS:2");
                     } else {
                         mLocalCodec = CODEC_CVSD;
                         if (DBG) Log.d(TAG, "Sending +BCS:1 to HF");
                         sendURC("+BCS:1");
                     }
                     return new AtCommandResult(AtCommandResult.UNSOLICITED); //Send Nothing
                 } else {
                     Log.e(TAG, "ERROR no codec negotiation enabled AG");
                     return new AtCommandResult(AtCommandResult.ERROR);
                 }
             }
         });
 
         // Codec Selection
         parser.register("+BCS", new AtCommandHandler() {
             @Override
             public AtCommandResult handleSetCommand(Object[] args) {
                 // AT+BCS=<u> (u is a codec ID)
                 if (DBG) Log.d(TAG, "Receiving AT+BCS=<u> from HF");
                 mExpectingBCS = false;
                 mHandler.removeMessages(CODEC_CONNECTION_SETUP_TIMEOUT);
                 if (args.length == 1 && (args[0] instanceof Integer)) {
                     if (DBG) Log.d(TAG, "HF=>AG AT+BCS=" + (Integer)args[0]);
                     mRemoteCodec = (Integer)args[0];
                     if (mRemoteCodec == mLocalCodec) {
                         switch(mLocalCodec) {
                             case CODEC_MSBC:
                                 Log.i(TAG, "HEADSET_SAMPLERATE=16000");
                                 mAudioManager.setParameters(HEADSET_SAMPLERATE + "=16000");
                                 break;
                             case CODEC_CVSD:
                                 Log.i(TAG, "HEADSET_SAMPLERATE=8000");
                                 mAudioManager.setParameters(HEADSET_SAMPLERATE + "=8000");
                                 break;
                         }
                         if (DBG) Log.d(TAG, "Sending OK to HF");
                         mPendingScoForWbs = true;
                         Message msg = mHandler.obtainMessage(CODEC_CONNECTION_SETUP_COMPLETED);
                         mHandler.sendMessageDelayed(msg, 100);
                         return new AtCommandResult(AtCommandResult.OK);
                     } else {
                         // Error Handling
                         mRemoteCodec = 0x0;
                         return new AtCommandResult(AtCommandResult.ERROR);
                     }
                 } else {
                     Log.w(TAG, "HF sent incorrect codec ID, assuming CVSD");
                     Log.i(TAG, "HEADSET_SAMPLERATE=8000");
                     mAudioManager.setParameters(HEADSET_SAMPLERATE + "=8000");
                     mPendingScoForWbs = true;
                     Message msg = mHandler.obtainMessage(CODEC_CONNECTION_SETUP_COMPLETED);
                     mHandler.sendMessageDelayed(msg, 100);
                     return new AtCommandResult(AtCommandResult.OK);
                 }
             }
         });
 
         // Available Codecs
         parser.register("+BAC", new AtCommandHandler() {
             @Override
             public AtCommandResult handleSetCommand(Object[] args) {
                 // AT+BAC=[<u1>[,<u2>[,...[,<un>]]]] (u1,u2,...,un are codec IDs)
                 if (DBG) Log.d(TAG, "Receiving AT+BAC");
                 mRemoteAvailableCodecs = 0x0;
                 mRemoteCodec = 0x0;
                 for (int i = 0; i < args.length; i ++) {
                     if (DBG) Log.d(TAG, "args[" + i + "]=" + args[i]);
                     if (args[i] instanceof Integer) {
                         switch ((Integer)args[i]) {
                             case CODEC_ID_CVSD:
                                 if (DBG) Log.d(TAG, "HF supports CODEC_CVSD");
                                 mRemoteAvailableCodecs |= CODEC_CVSD;
                                 break;
                             case CODEC_ID_MSBC:
                                 if (DBG) Log.d(TAG, "HF supports CODEC_MSBC");
                                 mRemoteAvailableCodecs |= CODEC_MSBC;
                                 break;
                             default:
                                 Log.w(TAG, "Unknown codec ID from HF: " + (Integer)args[i]);
                                 return new AtCommandResult(AtCommandResult.ERROR);
                         }
                     } else {
                         Log.w(TAG, "Invalid Codec ID Format from HF: " + args[i]);
                         return new AtCommandResult(AtCommandResult.ERROR);
                     }
                 }
                 if (DBG) Log.d(TAG, "mRemoteAvailableCodecs = " + mRemoteAvailableCodecs);
                 sendURC("OK");
                 if (mExpectingBCS) {
                     if (DBG) Log.d(TAG, "expecting AT+BCS=<codec id>, sending +BCS:<codec id> again");
                     mRemoteCodec = 0x0;
                     if (0x0 != (mRemoteAvailableCodecs & CODEC_MSBC)) {
                         if (DBG) Log.d(TAG, "+BCS:2");
                         mLocalCodec = CODEC_MSBC;
                         sendURC("+BCS:2");
                     } else {
                         if (DBG) Log.d(TAG, "+BCS:1");
                         mLocalCodec = CODEC_CVSD;
                         sendURC("+BCS:1");
                     }
                 }
                 return new AtCommandResult(AtCommandResult.UNSOLICITED);
             }
         });
 
         // Bluetooth Indicators Activation
         parser.register("+BIA", new AtCommandHandler() {
             @Override
             public AtCommandResult handleSetCommand(Object[] args) {
                 // AT+BIA=[[<indrep 1>][,[<indrep 2>][,...[,[<indrep n>]]]]]]
                 // Although indrep 2(call), 3(callsetup), 4(callheld) could be updated here,
                 // they won't be disabled.
                 if (DBG) Log.d(TAG, "Receiving AT+BIA");
                 final int size = (args.length > MAX_IIENABLED) ? MAX_IIENABLED : args.length;
                 for (int ai = 0, ii = 1; ai < size; ai ++, ii ++) {
                     if (DBG) Log.d(TAG, "args[" + ai + "]=" + args[ai]);
                     if (args[ai] instanceof Integer) {
                         if ((Integer)args[ai] == 1) {
                             mIIEnabled[ii] = true;
                         } else if ((Integer)args[ai] == 0) {
                             mIIEnabled[ii] = false;
                         } else if ((Integer)args[ai] == -1) {
                             if (DBG) Log.d(TAG, "Receiving AT+BIA with comma argument, ignore");
                         } else
                             return new AtCommandResult(AtCommandResult.ERROR);
                     } else
                         return new AtCommandResult(AtCommandResult.ERROR);
                 }
                 return new AtCommandResult(AtCommandResult.OK);
             }
         });
         mPhonebook.register(parser);
     }
 
     public void sendScoGainUpdate(int gain) {
         if (mScoGain != gain && (mRemoteBrsf & BRSF_HF_REMOTE_VOL_CONTROL) != 0x0) {
             sendURC("+VGS:" + gain);
             mScoGain = gain;
         }
     }
 
     public AtCommandResult reportCmeError(int error) {
         if (mCmee) {
             AtCommandResult result = new AtCommandResult(AtCommandResult.UNSOLICITED);
             result.addResponse("+CME ERROR: " + error);
             return result;
         } else {
             return new AtCommandResult(AtCommandResult.ERROR);
         }
     }
 
     private static final int START_CALL_TIMEOUT = 10000;  // ms
 
     private synchronized void expectCallStart() {
         mWaitingForCallStart = true;
         Message msg = Message.obtain(mHandler, CHECK_CALL_STARTED);
         mHandler.sendMessageDelayed(msg, START_CALL_TIMEOUT);
         if (!mStartCallWakeLock.isHeld()) {
             mStartCallWakeLock.acquire(START_CALL_TIMEOUT);
         }
     }
 
 
     private static final int START_VOICE_RECOGNITION_TIMEOUT = 5000;  // ms
 
     private synchronized void expectVoiceRecognition() {
         mWaitingForVoiceRecognition = true;
         Message msg = Message.obtain(mHandler, CHECK_VOICE_RECOGNITION_STARTED);
         mHandler.sendMessageDelayed(msg, START_VOICE_RECOGNITION_TIMEOUT);
         if (!mStartVoiceRecognitionWakeLock.isHeld()) {
             mStartVoiceRecognitionWakeLock.acquire(START_VOICE_RECOGNITION_TIMEOUT);
         }
     }
 
     /* package */ boolean startVoiceRecognition() {
 
         if  ((isCellularCallInProgress()) ||
              (isVirtualCallInProgress()) ||
              mVoiceRecognitionStarted) {
             Log.e(TAG, "startVoiceRecognition: Call in progress");
             return false;
         }
 
         mVoiceRecognitionStarted = true;
 
         if (mWaitingForVoiceRecognition) {
             // HF initiated
             mWaitingForVoiceRecognition = false;
             sendURC("OK");
         } else {
             // AG initiated
             sendURC("+BVRA: 1");
         }
         boolean ret = audioOn();
         if (ret == false) {
             mVoiceRecognitionStarted = false;
         }
         if (mStartVoiceRecognitionWakeLock.isHeld()) {
             mStartVoiceRecognitionWakeLock.release();
         }
         return ret;
     }
 
     /* package */ boolean stopVoiceRecognition() {
 
         if (!isVoiceRecognitionInProgress()) {
             return false;
         }
         mVoiceRecognitionStarted = false;
         if (mHfInitiatedVrDeactivation == true) {
             mHfInitiatedVrDeactivation = false;
             //No need to send +BVRA and calling audioOff() as it is already done.
             return false;
         }
         sendURC("+BVRA: 0");
         audioOff();
         return true;
     }
 
     // Voice Recognition in Progress
     private boolean isVoiceRecognitionInProgress() {
         return (mVoiceRecognitionStarted || mWaitingForVoiceRecognition);
     }
 
     /*
      * This class broadcasts vendor-specific commands + arguments to interested receivers.
      */
     private class VendorSpecificCommandHandler extends AtCommandHandler {
 
         private String mCommandName;
 
         private int mCompanyId;
 
         private VendorSpecificCommandHandler(String commandName, int companyId) {
             mCommandName = commandName;
             mCompanyId = companyId;
         }
 
         @Override
         public AtCommandResult handleReadCommand() {
             return new AtCommandResult(AtCommandResult.ERROR);
         }
 
         @Override
         public AtCommandResult handleTestCommand() {
             return new AtCommandResult(AtCommandResult.ERROR);
         }
 
         @Override
         public AtCommandResult handleActionCommand() {
             return new AtCommandResult(AtCommandResult.ERROR);
         }
 
         @Override
         public AtCommandResult handleSetCommand(Object[] arguments) {
             broadcastVendorSpecificEventIntent(mCommandName,
                                                mCompanyId,
                                                BluetoothHeadset.AT_CMD_TYPE_SET,
                                                arguments,
                                                mHeadset.getRemoteDevice());
             return new AtCommandResult(AtCommandResult.OK);
         }
     }
 
     private void broadcastSlcEstablished() {
         processSlcConnected();
         Intent intent = new Intent(SLC_ESTABLISHED);
         intent.putExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_CONNECTED);
         intent.putExtra(BluetoothDevice.EXTRA_DEVICE, mHeadset.getRemoteDevice());
         mContext.sendBroadcast(intent, android.Manifest.permission.BLUETOOTH);
     }
 
     private void processSlcConnected() {
         //Check if we need to open audio
         if (mPhoneState.isInCallAudio()) {
             audioOn();
         } else if ( mPhoneState.getCallState() == HeadsetHalConstants.CALL_STATE_INCOMING) {
             // need to update HS with RING when single ringing call exist
             if((mPhoneState.getNumActiveCall() == 0) ||(mPhoneState.getNumHeldCall() == 0) )
                 ring();
         }
         if (mPhoneProxy != null) {
             try {
             // Additionally, no indicator updates should be sent prior to SLC setup
             //mPhoneState.listenForPhoneState(true);
             mPhoneProxy.queryPhoneState();
             } catch (RemoteException e) {
                 Log.e(TAG, Log.getStackTraceString(new Throwable()));
             }
         } else {
             Log.e(TAG, "Handsfree phone proxy null for query phone state");
         }
         //To avoid sending duplicate indicators
         prevNumActive = mPhoneState.getNumActiveCall();
         prevNumHeld= mPhoneState.getNumHeldCall();
         prevCallState = mPhoneState.getCallState();
     }
 
     private boolean inDebug() {
         if(DBG && SystemProperties.getBoolean(DebugThread.DEBUG_HANDSFREE, false))
             Log.d(TAG, "InDebug");
         else Log.d(TAG, "Not InDebug");
         return DBG && SystemProperties.getBoolean(DebugThread.DEBUG_HANDSFREE, false);
     }
 
     private boolean allowAudioAnytime() {
         Log.d(TAG, "allowAudioAnytime");
         if(inDebug() && SystemProperties.getBoolean(DebugThread.DEBUG_HANDSFREE_AUDIO_ANYTIME,false))
                 Log.d(TAG, "allowAudioAnytime");
         else Log.d(TAG, "Dont allow AudioAnytime");
 
         return inDebug() && SystemProperties.getBoolean(DebugThread.DEBUG_HANDSFREE_AUDIO_ANYTIME,
                 false);
     }
 
     private void startDebug() {
         if (DBG && mDebugThread == null) {
             mDebugThread = new DebugThread();
             mDebugThread.start();
         }
     }
 
     private void stopDebug() {
         if (mDebugThread != null) {
             mDebugThread.interrupt();
             mDebugThread = null;
         }
     }
 
     // VirtualCall SCO support
     //
 
     // Cellular call in progress
     //TODO. Add new class variable to handle call.
     private boolean isCellularCallInProgress() {
         if (mPhoneState.isInCall())
             return true;
         return false;
     }
 
     // Virtual Call in Progress
     private boolean isVirtualCallInProgress() {
         return mVirtualCallStarted;
     }
 
     void setVirtualCallInProgress(boolean state) {
         mVirtualCallStarted = state;
     }
 
     //NOTE: Currently the VirtualCall API does not allow the application to initiate a call
     // transfer. Call transfer may be initiated from the handsfree device and this is handled by
     // the VirtualCall API
     synchronized boolean initiateScoUsingVirtualVoiceCall() {
         if (DBG) log("initiateScoUsingVirtualVoiceCall: Received");
         // 1. Check if the SCO state is idle
         if (isCellularCallInProgress() || isVoiceRecognitionInProgress()) {
             Log.e(TAG, "initiateScoUsingVirtualVoiceCall: Call in progress");
             return false;
         }
 
         // 2. Perform outgoing call setup procedure
         //TODO add below check
         if (/*mBluetoothPhoneState.sendUpdate() && */!isVirtualCallInProgress()) {
             AtCommandResult result = new AtCommandResult(AtCommandResult.UNSOLICITED);
             // outgoing call
             result.addResponse("+CIEV: 3,2");
             result.addResponse("+CIEV: 2,1");
             result.addResponse("+CIEV: 3,0");
             sendURC(result.toString());
             if (DBG) Log.d(TAG, "initiateScoUsingVirtualVoiceCall: Sent Call-setup procedure");
         }
 
         mVirtualCallStarted = true;
 
         // 3. Open the Audio Connection
         if (audioOn() == false) {
             log("initiateScoUsingVirtualVoiceCall: audioON failed");
             terminateScoUsingVirtualVoiceCall();
             return false;
         }
 
         mAudioPossible = true;
 
         // Done
         if (DBG) log("initiateScoUsingVirtualVoiceCall: Done");
         return true;
     }
 
     synchronized boolean terminateScoUsingVirtualVoiceCall() {
         if (DBG) log("terminateScoUsingVirtualVoiceCall: Received");
 
         if (!isVirtualCallInProgress()) {
             return false;
         }
 
         // 1. Release audio connection
         audioOff();
 
         // 2. terminate call-setup
         //TODO add below check
         if (/*mBluetoothPhoneState.sendUpdate()*/ DBG) {
             AtCommandResult result = new AtCommandResult(AtCommandResult.UNSOLICITED);
             // outgoing call
             result.addResponse("+CIEV: 2,0");
             sendURC(result.toString());
             if (DBG) log("terminateScoUsingVirtualVoiceCall: Sent Call-setup procedure");
         }
         mVirtualCallStarted = false;
         mAudioPossible = false;
 
         // Done
         if (DBG) log("terminateScoUsingVirtualVoiceCall: Done");
         return true;
     }
 
 
 
 //TODO..What is the need of debug thread.. Do we need this
     /** Debug thread to read debug properties - runs when debug.bt.hfp is true
      *  at the time a bluetooth handsfree device is connected. Debug properties
      *  are polled and mock updates sent every 1 second */
     private class DebugThread extends Thread {
         /** Turns on/off handsfree profile debugging mode */
         static final String DEBUG_HANDSFREE = "debug.bt.hfp";
 
         /** Mock battery level change - use 0 to 5 */
         static final String DEBUG_HANDSFREE_BATTERY = "debug.bt.hfp.battery";
 
         /** Mock no cellular service when false */
         static final String DEBUG_HANDSFREE_SERVICE = "debug.bt.hfp.service";
 
         /** Mock cellular roaming when true */
         static final String DEBUG_HANDSFREE_ROAM = "debug.bt.hfp.roam";
 
         /** false to true transition will force an audio (SCO) connection to
          *  be established. true to false will force audio to be disconnected
          */
         static final String DEBUG_HANDSFREE_AUDIO = "debug.bt.hfp.audio";
 
         /** true allows incoming SCO connection out of call.
          */
         static final String DEBUG_HANDSFREE_AUDIO_ANYTIME = "debug.bt.hfp.audio_anytime";
 
         /** Mock signal strength change in ASU - use 0 to 31 */
         static final String DEBUG_HANDSFREE_SIGNAL = "debug.bt.hfp.signal";
 
         /** Debug AT+CLCC: print +CLCC result */
         static final String DEBUG_HANDSFREE_CLCC = "debug.bt.hfp.clcc";
 
         /** Debug AT+BSIR - Send In Band Ringtones Unsolicited AT command.
          * debug.bt.unsol.inband = 0 => AT+BSIR = 0 sent by the AG
          * debug.bt.unsol.inband = 1 => AT+BSIR = 0 sent by the AG
          * Other values are ignored.
          */
 
         static final String DEBUG_UNSOL_INBAND_RINGTONE = "debug.bt.unsol.inband";
 
         @Override
         public void run() {
             boolean oldService = true;
             boolean oldRoam = false;
             boolean oldAudio = false;
 
             while (!isInterrupted() && inDebug()) {
                 int batteryLevel = SystemProperties.getInt(DEBUG_HANDSFREE_BATTERY, -1);
                 if (batteryLevel >= 0 && batteryLevel <= 5) {
                     Intent intent = new Intent();
                     intent.putExtra("level", batteryLevel);
                     intent.putExtra("scale", 5);
                     updateBatteryState(intent);
                 }
 
                 boolean serviceStateChanged = false;
                 if (SystemProperties.getBoolean(DEBUG_HANDSFREE_SERVICE, true) != oldService) {
                     oldService = !oldService;
                     serviceStateChanged = true;
                 }
                 if (SystemProperties.getBoolean(DEBUG_HANDSFREE_ROAM, false) != oldRoam) {
                     oldRoam = !oldRoam;
                     serviceStateChanged = true;
                 }
                 if (serviceStateChanged) {
                     Bundle b = new Bundle();
                     b.putInt("state", oldService ? 0 : 1);
                     b.putBoolean("roaming", oldRoam);
                     mPhoneState.sendDeviceStateChanged(ServiceState.newFromBundle(b));
                 }
 
                 if (SystemProperties.getBoolean(DEBUG_HANDSFREE_AUDIO, false) != oldAudio) {
                     oldAudio = !oldAudio;
                     if (oldAudio) {
                         audioOn();
                     } else {
                         audioOff();
                     }
                 }
 
                 int signalLevel = SystemProperties.getInt(DEBUG_HANDSFREE_SIGNAL, -1);
                 if (signalLevel >= 0 && signalLevel <= 31) {
                     //TODO..Do we need debug thread
                     /*SignalStrength signalStrength = new SignalStrength(signalLevel, -1, -1, -1,
                     -1, -1, -1, true);
                     Intent intent = new Intent();
                     Bundle data = new Bundle();
                     signalStrength.fillInNotifierBundle(data);
                     intent.putExtras(data);*/
                     //TODO. What to do here
                     //mBluetoothPhoneState.updateSignalState(intent);
                 }
 
                 if (SystemProperties.getBoolean(DEBUG_HANDSFREE_CLCC, false)) {
                     log("gsmGetClccResult().toString()"); //TODO..
                 }
                 try {
                     sleep(1000);  // 1 second
                 } catch (InterruptedException e) {
                     break;
                 }
 
                 int inBandRing =
                     SystemProperties.getInt(DEBUG_UNSOL_INBAND_RINGTONE, -1);
                 if (inBandRing == 0 || inBandRing == 1) {
                     AtCommandResult result =
                         new AtCommandResult(AtCommandResult.UNSOLICITED);
                     result.addResponse("+BSIR: " + inBandRing);
                     sendURC(result.toString());
                 }
             }
         }
     }
 
     private static void log(String msg) {
         if(DBG) Log.d(TAG, msg);
     }
 }
