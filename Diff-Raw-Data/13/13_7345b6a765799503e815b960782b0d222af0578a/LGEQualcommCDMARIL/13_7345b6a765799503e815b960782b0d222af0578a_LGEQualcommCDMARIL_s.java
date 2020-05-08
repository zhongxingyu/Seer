 /*
  * Copyright (C) 2012 The CyanogenMod Project
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
 
 package com.android.internal.telephony;
 
 import static com.android.internal.telephony.RILConstants.*;
 
 import android.content.Context;
 import android.os.AsyncResult;
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.os.Looper;
 import android.os.Message;
 import android.os.Parcel;
 import android.telephony.SmsMessage;
 import android.os.SystemProperties;
 import android.os.SystemClock;
 import android.text.TextUtils;
 import android.util.Log;
 import android.telephony.SignalStrength;
 
 import android.telephony.PhoneNumberUtils;
 import com.android.internal.telephony.RILConstants;
 import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;
 import com.android.internal.telephony.cdma.CdmaInformationRecords;
 import com.android.internal.telephony.cdma.CdmaInformationRecords.CdmaSignalInfoRec;
 import com.android.internal.telephony.cdma.SignalToneUtil;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 /**
  * As stands, the RIL supports calling get subscription source which can
  * handle both ruim and nv qc devices. The lteOnCdma property allows
  * for the devices to provision for multiple cdma dun types at run time. This
  * extension of shared merely fixes nuance issues arising from the differences
  * caused by embedded SIM and legacy CDMA.
  * {@hide}
  */
 public class LGEQualcommCDMARIL extends RIL implements
 CommandsInterface {
     protected HandlerThread mIccThread;
     protected IccHandler mIccHandler;
     private Object mSMSLock = new Object();
     private boolean mIsSendingSMS = false;
     public static final long SEND_SMS_TIMEOUT_IN_MS = 30000;
 
     private final int RIL_INT_RADIO_OFF = 0;
     private final int RIL_INT_RADIO_UNAVALIABLE = 1;
     private final int RIL_INT_RADIO_ON = 2;
 
     public LGEQualcommCDMARIL(Context context, int networkMode,
             int cdmaSubscription) {
         super(context, networkMode, cdmaSubscription);
     }
 
     @Override
     protected Object responseIccCardStatus(Parcel p) {
         IccCardApplicationStatus ca;
 
         IccCardStatus status = new IccCardStatus();
         status.setCardState(p.readInt());
         status.setUniversalPinState(p.readInt());
         status.mGsmUmtsSubscriptionAppIndex = p.readInt();
         status.mCdmaSubscriptionAppIndex = p.readInt();
         status.mImsSubscriptionAppIndex = p.readInt();
 
         int numApplications = p.readInt();
 
         // limit to maximum allowed applications
         if (numApplications > IccCardStatus.CARD_MAX_APPS) {
             numApplications = IccCardStatus.CARD_MAX_APPS;
         }
         status.mApplications = new IccCardApplicationStatus[numApplications];
 
         for (int i = 0; i < numApplications; i++) {
             ca = new IccCardApplicationStatus();
             ca.app_type = ca.AppTypeFromRILInt(p.readInt());
             ca.app_state = ca.AppStateFromRILInt(p.readInt());
             ca.perso_substate = ca.PersoSubstateFromRILInt(p.readInt());
             if ((ca.app_state == IccCardApplicationStatus.AppState.APPSTATE_SUBSCRIPTION_PERSO) &&
                 ((ca.perso_substate == IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_READY) ||
                  (ca.perso_substate == IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_UNKNOWN))) {
                     // ridiculous hack for network SIM unlock pin
                     ca.app_state = IccCardApplicationStatus.AppState.APPSTATE_UNKNOWN;
                     Log.d(LOG_TAG, "ca.app_state == AppState.APPSTATE_SUBSCRIPTION_PERSO");
                     Log.d(LOG_TAG, "ca.perso_substate == PersoSubState.PERSOSUBSTATE_READY");
                 }
             ca.aid = p.readString();
             ca.app_label = p.readString();
             ca.pin1_replaced = p.readInt();
             ca.pin1 = ca.PinStateFromRILInt(p.readInt());
             ca.pin2 = ca.PinStateFromRILInt(p.readInt());
             p.readInt(); // remaining_count_pin1 - pin1_num_retries
             p.readInt(); // remaining_count_puk1 - puk1_num_retries
             p.readInt(); // remaining_count_pin2 - pin2_num_retries
             p.readInt(); // remaining_count_puk2 - puk2_num_retries
             p.readInt(); // - perso_unblock_retries
             status.mApplications[i] = ca;
         }
         return status;
     }
 
     @Override
     public void
     sendCdmaSms(byte[] pdu, Message result) {
         // Do not send a new SMS until the response for the previous SMS has been received
         //   * for the error case where the response never comes back, time out after
         //     30 seconds and just try the next CDMA_SEND_SMS
         synchronized (mSMSLock) {
             long timeoutTime  = SystemClock.elapsedRealtime() + SEND_SMS_TIMEOUT_IN_MS;
             long waitTimeLeft = SEND_SMS_TIMEOUT_IN_MS;
             while (mIsSendingSMS && (waitTimeLeft > 0)) {
                 Log.d(LOG_TAG, "sendCdmaSms() waiting for response of previous CDMA_SEND_SMS");
                 try {
                     mSMSLock.wait(waitTimeLeft);
                 } catch (InterruptedException ex) {
                     // ignore the interrupt and rewait for the remainder
                 }
                 waitTimeLeft = timeoutTime - SystemClock.elapsedRealtime();
             }
             if (waitTimeLeft <= 0) {
                 Log.e(LOG_TAG, "sendCdmaSms() timed out waiting for response of previous CDMA_SEND_SMS");
             }
             mIsSendingSMS = true;
         }
 
         super.sendCdmaSms(pdu, result);
     }
 
     @Override
     protected Object responseSignalStrength(Parcel p) {
         int numInts = 12;
         int response[];
 
         // This is a mashup of algorithms used in
         // LGEQualcommCDMARIL.java
 
         // Get raw data
         response = new int[numInts];
         for (int i = 0; i < numInts; i++) {
             response[i] = p.readInt();
         }
         // Take just the least significant byte as the signal strength
         response[2] %= 256;
         response[4] %= 256;
 
         // RIL_LTE_SignalStrength
         if (response[7] == 99) {
             // If LTE is not enabled, clear LTE results
             // 7-11 must be -1 for GSM signal strength to be used (see
             // frameworks/base/telephony/java/android/telephony/SignalStrength.java)
             response[8] = -1;
             response[9] = -1;
             response[10] = -1;
             response[11] = -1;
         }
 
         return new SignalStrength(response[0], response[1], response[2], response[3], response[4], response[5], response[6], response[7], response[8], response[9], response[10], response[11], false);
 
     }
   
     @Override
     protected void
     processUnsolicited (Parcel p) {
         Object ret;
         int dataPosition = p.dataPosition(); // save off position within the Parcel
         int response = p.readInt();
 
         switch(response) {
             case RIL_UNSOL_RIL_CONNECTED: ret = responseInts(p); break;
             case 1035: ret = responseVoid(p); break; // RIL_UNSOL_VOICE_RADIO_TECH_CHANGED
             case 1036: ret = responseVoid(p); break; // RIL_UNSOL_RESPONSE_IMS_NETWORK_STATE_CHANGED
             case 1037: ret = responseVoid(p); break; // RIL_UNSOL_EXIT_EMERGENCY_CALLBACK_MODE
             case 1038: ret = responseVoid(p); break; // RIL_UNSOL_DATA_NETWORK_STATE_CHANGED
 
             default:
                 // Rewind the Parcel
                 p.setDataPosition(dataPosition);
 
                 // Forward responses that we are not overriding to the super class
                 super.processUnsolicited(p);
                 return;
         }
 
         switch(response) {
             case RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED:
                 int state = p.readInt();
                 setRadioStateFromRILInt(state);
                 break;
             case RIL_UNSOL_RIL_CONNECTED:
                 notifyRegistrantsRilConnectionChanged(((int[])ret)[0]);
                 break;
             case 1035:
             case 1036:
                 break;
             case 1037: // RIL_UNSOL_EXIT_EMERGENCY_CALLBACK_MODE
                 if (mExitEmergencyCallbackModeRegistrants != null) {
                     mExitEmergencyCallbackModeRegistrants.notifyRegistrants(
                                         new AsyncResult (null, null, null));
                 }
                 break;
             case 1038:
                 break;
         }
     }
 
    /**
     * Notify all registrants that the ril has connected or disconnected.
     *
     * @param rilVer is the version of the ril or -1 if disconnected.
     */
    private void notifyRegistrantsRilConnectionChanged(int rilVer) {
        mRilVersion = rilVer;
        if (mRilConnectedRegistrants != null) {
            mRilConnectedRegistrants.notifyRegistrants(
                                new AsyncResult (null, new Integer(rilVer), null));
        }
    }

     private void setRadioStateFromRILInt (int stateCode) {
         CommandsInterface.RadioState radioState;
         HandlerThread handlerThread;
         Looper looper;
         IccHandler iccHandler;
 
         switch (stateCode) {
             case RIL_INT_RADIO_OFF:
                 radioState = CommandsInterface.RadioState.RADIO_OFF;
                 if (mIccHandler != null) {
                     mIccThread = null;
                     mIccHandler = null;
                 }
                 break;
             case RIL_INT_RADIO_UNAVALIABLE:
                 radioState = CommandsInterface.RadioState.RADIO_UNAVAILABLE;
                 break;
             case RIL_INT_RADIO_ON:
                 if (mIccHandler == null) {
                     handlerThread = new HandlerThread("IccHandler");
                     mIccThread = handlerThread;
 
                     mIccThread.start();
 
                     looper = mIccThread.getLooper();
                     mIccHandler = new IccHandler(this,looper);
                     mIccHandler.run();
                 }
                 radioState = CommandsInterface.RadioState.RADIO_ON;
                 break;
             default:
                 throw new RuntimeException("Unrecognized RIL_RadioState: " + stateCode);
         }
 
         setRadioState (radioState);
     }
 
     class IccHandler extends Handler implements Runnable {
         private static final int EVENT_RADIO_ON = 1;
         private static final int EVENT_ICC_STATUS_CHANGED = 2;
         private static final int EVENT_GET_ICC_STATUS_DONE = 3;
         private static final int EVENT_RADIO_OFF_OR_UNAVAILABLE = 4;
 
         private RIL mRil;
         private boolean mRadioOn = false;
 
         public IccHandler (RIL ril, Looper looper) {
             super (looper);
             mRil = ril;
         }
 
         public void handleMessage (Message paramMessage) {
             switch (paramMessage.what) {
                 case EVENT_RADIO_ON:
                     mRadioOn = true;
                     sendMessage(obtainMessage(EVENT_ICC_STATUS_CHANGED));
                     break;
                 case EVENT_GET_ICC_STATUS_DONE:
                     AsyncResult asyncResult = (AsyncResult) paramMessage.obj;
                     if (asyncResult.exception != null) {
                         break;
                     }
                     IccCardStatus status = (IccCardStatus) asyncResult.result;
                     if (status.mApplications == null || status.mApplications.length == 0) {
                         if (!mRil.getRadioState().isOn()) {
                             break;
                         }
 
                         mRil.setRadioState(CommandsInterface.RadioState.RADIO_ON);
                     } else {
                         int appIndex = status.mCdmaSubscriptionAppIndex;
                         IccCardApplicationStatus application = status.mApplications[appIndex];
                         IccCardApplicationStatus.AppState app_state = application.app_state;
                         IccCardApplicationStatus.AppType app_type = application.app_type;
                         switch (app_state) {
                             case APPSTATE_PIN:
                             case APPSTATE_PUK:
                                 switch (app_type) {
                                     case APPTYPE_USIM:
                                     case APPTYPE_RUIM:
                                         mRil.setRadioState(CommandsInterface.RadioState.RADIO_ON);
                                         break;
                                     default:
                                         return;
                                 }
                                 break;
                             case APPSTATE_READY:
                                 switch (app_type) {
                                     case APPTYPE_USIM:
                                     case APPTYPE_RUIM:
                                         mRil.setRadioState(CommandsInterface.RadioState.RADIO_ON);
                                         break;
                                     default:
                                         return;
                                 }
                                 break;
                             default:
                                 return;
                         }
                     }
                     break;
                 case EVENT_ICC_STATUS_CHANGED:
                     if (mRadioOn) {
                         mRil.getIccCardStatus(obtainMessage(EVENT_GET_ICC_STATUS_DONE, paramMessage.obj));
                     }
                     break;
                 case EVENT_RADIO_OFF_OR_UNAVAILABLE:
                     mRadioOn = false;
                 default:
                     break;
             }
         }
 
         public void run () {
             mRil.registerForIccStatusChanged(this, EVENT_ICC_STATUS_CHANGED, null);
             Message msg = obtainMessage(EVENT_RADIO_ON);
             mRil.getIccCardStatus(msg);
         }
     }
 
 
     @Override
     protected Object
     responseSMS(Parcel p) {
         // Notify that sendSMS() can send the next SMS
         synchronized (mSMSLock) {
             mIsSendingSMS = false;
             mSMSLock.notify();
         }
 
         return super.responseSMS(p);
     }
 }
