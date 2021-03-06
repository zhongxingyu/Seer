 /*
  * Copyright (C) 2012-2014 The CyanogenMod Project
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
 import android.media.AudioManager;
 import android.os.AsyncResult;
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.os.Looper;
 import android.os.Message;
 import android.os.Parcel;
 import android.telephony.SmsMessage;
 import android.os.SystemProperties;
 import android.os.SystemClock;
 import android.provider.Settings;
 import android.text.TextUtils;
 import android.telephony.Rlog;
 
 import android.telephony.SignalStrength;
 
 import android.telephony.PhoneNumberUtils;
 import com.android.internal.telephony.RILConstants;
 import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;
 import com.android.internal.telephony.cdma.CdmaInformationRecords;
 import com.android.internal.telephony.cdma.CdmaInformationRecords.CdmaSignalInfoRec;
 import com.android.internal.telephony.cdma.SignalToneUtil;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import com.android.internal.telephony.uicc.IccCardApplicationStatus;
 import com.android.internal.telephony.uicc.IccCardStatus;
 
 /**
  * Qualcomm RIL for the Samsung family.
  * Quad core Exynos4 with Qualcomm modem and later is supported
  * Snapdragon S3 and later is supported
  * This RIL is univerisal meaning it supports CDMA and GSM radio.
  * Handles most GSM and CDMA cases.
  * {@hide}
  */
 public class SamsungQualcommRIL extends RIL implements CommandsInterface {
 
     private AudioManager mAudioManager;
 
     private Object mSMSLock = new Object();
     private boolean mIsSendingSMS = false;
     private boolean isGSM = false;
     public static final long SEND_SMS_TIMEOUT_IN_MS = 30000;
     private boolean oldRilState = needsOldRilFeature("exynos4RadioState");
     private boolean googleEditionSS = needsOldRilFeature("googleEditionSS");
     private boolean driverCall = needsOldRilFeature("newDriverCall");
     private boolean driverCallU = needsOldRilFeature("newDriverCallU");
     private boolean dialCode = needsOldRilFeature("newDialCode");
     public SamsungQualcommRIL(Context context, int networkMode,
             int cdmaSubscription) {
         super(context, networkMode, cdmaSubscription);
         mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
     }
 
     @Override
     protected Object
     responseIccCardStatus(Parcel p) {
         IccCardApplicationStatus appStatus;
 
         IccCardStatus cardStatus = new IccCardStatus();
         cardStatus.setCardState(p.readInt());
         cardStatus.setUniversalPinState(p.readInt());
         cardStatus.mGsmUmtsSubscriptionAppIndex = p.readInt();
         cardStatus.mCdmaSubscriptionAppIndex = p.readInt();
         cardStatus.mImsSubscriptionAppIndex = p.readInt();
 
         int numApplications = p.readInt();
 
         // limit to maximum allowed applications
         if (numApplications > IccCardStatus.CARD_MAX_APPS) {
             numApplications = IccCardStatus.CARD_MAX_APPS;
         }
         cardStatus.mApplications = new IccCardApplicationStatus[numApplications];
 
         appStatus = new IccCardApplicationStatus();
         for (int i = 0 ; i < numApplications ; i++) {
             if (i!=0) {
                 appStatus = new IccCardApplicationStatus();
             }
             appStatus.app_type       = appStatus.AppTypeFromRILInt(p.readInt());
             appStatus.app_state      = appStatus.AppStateFromRILInt(p.readInt());
             appStatus.perso_substate = appStatus.PersoSubstateFromRILInt(p.readInt());
             appStatus.aid            = p.readString();
             appStatus.app_label      = p.readString();
             appStatus.pin1_replaced  = p.readInt();
             appStatus.pin1           = appStatus.PinStateFromRILInt(p.readInt());
             appStatus.pin2           = appStatus.PinStateFromRILInt(p.readInt());
             p.readInt(); // remaining_count_pin1 - pin1_num_retries
             p.readInt(); // remaining_count_puk1 - puk1_num_retries
             p.readInt(); // remaining_count_pin2 - pin2_num_retries
             p.readInt(); // remaining_count_puk2 - puk2_num_retries
             p.readInt(); // - perso_unblock_retries
             cardStatus.mApplications[i] = appStatus;
         }
         if (numApplications==1 && !isGSM && appStatus.app_type == appStatus.AppTypeFromRILInt(2)) { // usim
             cardStatus.mApplications = new IccCardApplicationStatus[numApplications+2];
             cardStatus.mGsmUmtsSubscriptionAppIndex = 0;
             cardStatus.mApplications[cardStatus.mGsmUmtsSubscriptionAppIndex]=appStatus;
             cardStatus.mCdmaSubscriptionAppIndex = 1;
             cardStatus.mImsSubscriptionAppIndex = 2;
             IccCardApplicationStatus appStatus2 = new IccCardApplicationStatus();
             appStatus2.app_type       = appStatus2.AppTypeFromRILInt(4); // csim state
             appStatus2.app_state      = appStatus.app_state;
             appStatus2.perso_substate = appStatus.perso_substate;
             appStatus2.aid            = appStatus.aid;
             appStatus2.app_label      = appStatus.app_label;
             appStatus2.pin1_replaced  = appStatus.pin1_replaced;
             appStatus2.pin1           = appStatus.pin1;
             appStatus2.pin2           = appStatus.pin2;
             cardStatus.mApplications[cardStatus.mCdmaSubscriptionAppIndex] = appStatus2;
             IccCardApplicationStatus appStatus3 = new IccCardApplicationStatus();
             appStatus3.app_type       = appStatus3.AppTypeFromRILInt(5); // ims state
             appStatus3.app_state      = appStatus.app_state;
             appStatus3.perso_substate = appStatus.perso_substate;
             appStatus3.aid            = appStatus.aid;
             appStatus3.app_label      = appStatus.app_label;
             appStatus3.pin1_replaced  = appStatus.pin1_replaced;
             appStatus3.pin1           = appStatus.pin1;
             appStatus3.pin2           = appStatus.pin2;
             cardStatus.mApplications[cardStatus.mImsSubscriptionAppIndex] = appStatus3;
         }
         return cardStatus;
     }
 
     @Override
     public void
     sendCdmaSms(byte[] pdu, Message result) {
         smsLock();
         super.sendCdmaSms(pdu, result);
     }
 
     @Override
     public void
         sendSMS (String smscPDU, String pdu, Message result) {
         smsLock();
         super.sendSMS(smscPDU, pdu, result);
     }
 
     private void smsLock(){
         // Do not send a new SMS until the response for the previous SMS has been received
         //   * for the error case where the response never comes back, time out after
         //     30 seconds and just try the next SEND_SMS
         synchronized (mSMSLock) {
             long timeoutTime  = SystemClock.elapsedRealtime() + SEND_SMS_TIMEOUT_IN_MS;
             long waitTimeLeft = SEND_SMS_TIMEOUT_IN_MS;
             while (mIsSendingSMS && (waitTimeLeft > 0)) {
                 Rlog.d(RILJ_LOG_TAG, "sendSMS() waiting for response of previous SEND_SMS");
                 try {
                     mSMSLock.wait(waitTimeLeft);
                 } catch (InterruptedException ex) {
                     // ignore the interrupt and rewait for the remainder
                 }
                 waitTimeLeft = timeoutTime - SystemClock.elapsedRealtime();
             }
             if (waitTimeLeft <= 0) {
                 Rlog.e(RILJ_LOG_TAG, "sendSms() timed out waiting for response of previous CDMA_SEND_SMS");
             }
             mIsSendingSMS = true;
         }
 
     }
 
     @Override
     protected Object responseSignalStrength(Parcel p) {
         int numInts = 12;
         int response[];
 
         // This is a mashup of algorithms used in
         // SamsungQualcommUiccRIL.java
 
         // Get raw data
         response = new int[numInts];
         for (int i = 0; i < numInts; i++) {
             response[i] = p.readInt();
         }
         //gsm
         response[0] &= 0xff; //gsmDbm
 
         //cdma
         // Take just the least significant byte as the signal strength
         response[2] %= 256;
         response[4] %= 256;
 
         // RIL_LTE_SignalStrength
         if (googleEditionSS && !isGSM){
             response[8] = response[2];
         }else if ((response[7] & 0xff) == 255 || response[7] == 99) {
             // If LTE is not enabled, clear LTE results
             // 7-11 must be -1 for GSM signal strength to be used (see
             // frameworks/base/telephony/java/android/telephony/SignalStrength.java)
             // make sure lte is disabled
             response[7] = 99;
             response[8] = SignalStrength.INVALID;
             response[9] = SignalStrength.INVALID;
             response[10] = SignalStrength.INVALID;
             response[11] = SignalStrength.INVALID;
         }else{ // lte is gsm on samsung/qualcomm cdma stack
             response[7] &= 0xff;
         }
 
         return new SignalStrength(response[0], response[1], response[2], response[3], response[4], response[5], response[6], response[7], response[8], response[9], response[10], response[11], (p.readInt() != 0));
 
     }
 
     @Override
     protected RadioState getRadioStateFromInt(int stateInt) {
         if(!oldRilState)
             super.getRadioStateFromInt(stateInt);
         RadioState state;
 
         /* RIL_RadioState ril.h */
         switch(stateInt) {
             case 0: state = RadioState.RADIO_OFF; break;
             case 1:
             case 2: state = RadioState.RADIO_UNAVAILABLE; break;
             case 4:
                 // When SIM is PIN-unlocked, RIL doesn't respond with RIL_UNSOL_RESPONSE_SIM_STATUS_CHANGED.
                 // We notify the system here.
                 Rlog.d(RILJ_LOG_TAG, "SIM is PIN-unlocked now");
                 if (mIccStatusChangedRegistrants != null) {
                     mIccStatusChangedRegistrants.notifyRegistrants();
                 }
             case 3:
             case 5:
             case 6:
             case 7:
             case 8:
             case 9:
             case 10:
             case 13: state = RadioState.RADIO_ON; break;
 
             default:
                 throw new RuntimeException(
                                            "Unrecognized RIL_RadioState: " + stateInt);
         }
         return state;
     }
 
     @Override
     public void setPhoneType(int phoneType){
         super.setPhoneType(phoneType);
         isGSM = (phoneType != RILConstants.CDMA_PHONE);
     }
 
     @Override
     protected Object
     responseCallList(Parcel p) {
         samsungDriverCall = driverCallU || (driverCall && !isGSM) || mRilVersion < 7 ? false : true;
         return super.responseCallList(p);
     }
 
     @Override
     protected void
     processUnsolicited (Parcel p) {
         Object ret;
         int dataPosition = p.dataPosition(); // save off position within the Parcel
         int response = p.readInt();
 
         switch(response) {
             case RIL_UNSOL_RIL_CONNECTED: // Fix for NV/RUIM setting on CDMA SIM devices
                 // skip getcdmascriptionsource as if qualcomm handles it in the ril binary
                 ret = responseInts(p);
                 setRadioPower(false, null);
                 setPreferredNetworkType(mPreferredNetworkType, null);
                 int cdmaSubscription = Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.CDMA_SUBSCRIPTION_MODE, -1);
                 if(cdmaSubscription != -1) {
                     setCdmaSubscriptionSource(mCdmaSubscription, null);
                 }
                 setCellInfoListRate(Integer.MAX_VALUE, null);
                 notifyRegistrantsRilConnectionChanged(((int[])ret)[0]);
                 break;
             case RIL_UNSOL_NITZ_TIME_RECEIVED:
                 handleNitzTimeReceived(p);
                 break;
             // SAMSUNG STATES
             case SamsungExynos4RIL.RIL_UNSOL_AM:
                 ret = responseString(p);
                 String amString = (String) ret;
                 Rlog.d(RILJ_LOG_TAG, "Executing AM: " + amString);
 
                 try {
                     Runtime.getRuntime().exec("am " + amString);
                 } catch (IOException e) {
                     e.printStackTrace();
                     Rlog.e(RILJ_LOG_TAG, "am " + amString + " could not be executed.");
                 }
                 break;
             case SamsungExynos4RIL.RIL_UNSOL_RESPONSE_HANDOVER:
                 ret = responseVoid(p);
                 break;
             case 1036:
                 ret = responseVoid(p);
                 break;
             case SamsungExynos4RIL.RIL_UNSOL_WB_AMR_STATE:
                 ret = responseInts(p);
                 setWbAmr(((int[])ret)[0]);
                 break;
             default:
                 // Rewind the Parcel
                 p.setDataPosition(dataPosition);
 
                 // Forward responses that we are not overriding to the super class
                 super.processUnsolicited(p);
                 return;
         }
 
     }
 
     @Override
     protected RILRequest
     processSolicited (Parcel p) {
         int serial, error;
         boolean found = false;
 
         serial = p.readInt();
         error = p.readInt();
 
         RILRequest rr;
 
         rr = findAndRemoveRequestFromList(serial);
 
         if (rr == null) {
             Rlog.w(RILJ_LOG_TAG, "Unexpected solicited response! sn: "
                             + serial + " error: " + error);
             return null;
         }
 
         Object ret = null;
 
         if (error == 0 || p.dataAvail() > 0) {
             // either command succeeds or command fails but with data payload
             try {switch (rr.mRequest) {
             /*
  cat libs/telephony/ril_commands.h \
  | egrep "^ *{RIL_" \
  | sed -re 's/\{([^,]+),[^,]+,([^}]+).+/case \1: ret = \2(p); break;/'
              */
             case RIL_REQUEST_GET_SIM_STATUS: ret =  responseIccCardStatus(p); break;
             case RIL_REQUEST_ENTER_SIM_PIN: ret =  responseInts(p); break;
             case RIL_REQUEST_ENTER_SIM_PUK: ret =  responseInts(p); break;
             case RIL_REQUEST_ENTER_SIM_PIN2: ret =  responseInts(p); break;
             case RIL_REQUEST_ENTER_SIM_PUK2: ret =  responseInts(p); break;
             case RIL_REQUEST_CHANGE_SIM_PIN: ret =  responseInts(p); break;
             case RIL_REQUEST_CHANGE_SIM_PIN2: ret =  responseInts(p); break;
             case RIL_REQUEST_ENTER_NETWORK_DEPERSONALIZATION: ret =  responseInts(p); break;
             case RIL_REQUEST_GET_CURRENT_CALLS: ret =  responseCallList(p); break;
             case RIL_REQUEST_DIAL: ret =  responseVoid(p); break;
             case RIL_REQUEST_GET_IMSI: ret =  responseString(p); break;
             case RIL_REQUEST_HANGUP: ret =  responseVoid(p); break;
             case RIL_REQUEST_HANGUP_WAITING_OR_BACKGROUND: ret =  responseVoid(p); break;
             case RIL_REQUEST_HANGUP_FOREGROUND_RESUME_BACKGROUND: {
                 if (mTestingEmergencyCall.getAndSet(false)) {
                     if (mEmergencyCallbackModeRegistrant != null) {
                         riljLog("testing emergency call, notify ECM Registrants");
                         mEmergencyCallbackModeRegistrant.notifyRegistrant();
                     }
                 }
                 ret =  responseVoid(p);
                 break;
             }
             case RIL_REQUEST_SWITCH_WAITING_OR_HOLDING_AND_ACTIVE: ret =  responseVoid(p); break;
             case RIL_REQUEST_CONFERENCE: ret =  responseVoid(p); break;
             case RIL_REQUEST_UDUB: ret =  responseVoid(p); break;
             case RIL_REQUEST_LAST_CALL_FAIL_CAUSE: ret =  responseInts(p); break;
             case RIL_REQUEST_SIGNAL_STRENGTH: ret =  responseSignalStrength(p); break;
                     //modification start
                 // prevent exceptions from happenimg because the null value is null or a hexadecimel. so convert if it is not null
             case RIL_REQUEST_VOICE_REGISTRATION_STATE: ret =  responseVoiceDataRegistrationState(p); break;
             case RIL_REQUEST_DATA_REGISTRATION_STATE: ret =  responseVoiceDataRegistrationState(p); break;
                 // this fixes bogus values the modem creates
                 // sometimes the  ril may print out
                 // (always on sprint)
                 // sprint: (empty,empty,31000)
                 // this problemaic on sprint, lte won't start, response is slow
                 //speeds up response time on eherpderpd/lte networks
             case RIL_REQUEST_OPERATOR: ret =  operatorCheck(p); break;
                     //end modification
             case RIL_REQUEST_RADIO_POWER: ret =  responseVoid(p); break;
             case RIL_REQUEST_DTMF: ret =  responseVoid(p); break;
             case RIL_REQUEST_SEND_SMS: ret =  responseSMS(p); break;
             case RIL_REQUEST_SEND_SMS_EXPECT_MORE: ret =  responseSMS(p); break;
             case RIL_REQUEST_SETUP_DATA_CALL: ret =  responseSetupDataCall(p); break;
             case RIL_REQUEST_SIM_IO: ret =  responseICC_IO(p); break;
             case RIL_REQUEST_SEND_USSD: ret =  responseVoid(p); break;
             case RIL_REQUEST_CANCEL_USSD: ret =  responseVoid(p); break;
             case RIL_REQUEST_GET_CLIR: ret =  responseInts(p); break;
             case RIL_REQUEST_SET_CLIR: ret =  responseVoid(p); break;
             case RIL_REQUEST_QUERY_CALL_FORWARD_STATUS: ret =  responseCallForward(p); break;
             case RIL_REQUEST_SET_CALL_FORWARD: ret =  responseVoid(p); break;
             case RIL_REQUEST_QUERY_CALL_WAITING: ret =  responseInts(p); break;
             case RIL_REQUEST_SET_CALL_WAITING: ret =  responseVoid(p); break;
             case RIL_REQUEST_SMS_ACKNOWLEDGE: ret =  responseVoid(p); break;
             case RIL_REQUEST_GET_IMEI: ret =  responseString(p); break;
             case RIL_REQUEST_GET_IMEISV: ret =  responseString(p); break;
             case RIL_REQUEST_ANSWER: ret =  responseVoid(p); break;
             case RIL_REQUEST_DEACTIVATE_DATA_CALL: ret =  responseVoid(p); break;
             case RIL_REQUEST_QUERY_FACILITY_LOCK: ret =  responseInts(p); break;
             case RIL_REQUEST_SET_FACILITY_LOCK: ret =  responseInts(p); break;
             case RIL_REQUEST_CHANGE_BARRING_PASSWORD: ret =  responseVoid(p); break;
             case RIL_REQUEST_QUERY_NETWORK_SELECTION_MODE: ret =  responseInts(p); break;
             case RIL_REQUEST_SET_NETWORK_SELECTION_AUTOMATIC: ret =  responseVoid(p); break;
             case RIL_REQUEST_SET_NETWORK_SELECTION_MANUAL: ret =  responseVoid(p); break;
             case RIL_REQUEST_QUERY_AVAILABLE_NETWORKS : ret =  responseOperatorInfos(p); break;
             case RIL_REQUEST_DTMF_START: ret =  responseVoid(p); break;
             case RIL_REQUEST_DTMF_STOP: ret =  responseVoid(p); break;
             case RIL_REQUEST_BASEBAND_VERSION: ret =  responseString(p); break;
             case RIL_REQUEST_SEPARATE_CONNECTION: ret =  responseVoid(p); break;
             case RIL_REQUEST_SET_MUTE: ret =  responseVoid(p); break;
             case RIL_REQUEST_GET_MUTE: ret =  responseInts(p); break;
             case RIL_REQUEST_QUERY_CLIP: ret =  responseInts(p); break;
             case RIL_REQUEST_LAST_DATA_CALL_FAIL_CAUSE: ret =  responseInts(p); break;
             case RIL_REQUEST_DATA_CALL_LIST: ret =  responseDataCallList(p); break;
             case RIL_REQUEST_RESET_RADIO: ret =  responseVoid(p); break;
             case RIL_REQUEST_OEM_HOOK_RAW: ret =  responseRaw(p); break;
             case RIL_REQUEST_OEM_HOOK_STRINGS: ret =  responseStrings(p); break;
             case RIL_REQUEST_SCREEN_STATE: ret =  responseVoid(p); break;
             case RIL_REQUEST_SET_SUPP_SVC_NOTIFICATION: ret =  responseVoid(p); break;
             case RIL_REQUEST_WRITE_SMS_TO_SIM: ret =  responseInts(p); break;
             case RIL_REQUEST_DELETE_SMS_ON_SIM: ret =  responseVoid(p); break;
             case RIL_REQUEST_SET_BAND_MODE: ret =  responseVoid(p); break;
             case RIL_REQUEST_QUERY_AVAILABLE_BAND_MODE: ret =  responseInts(p); break;
             case RIL_REQUEST_STK_GET_PROFILE: ret =  responseString(p); break;
             case RIL_REQUEST_STK_SET_PROFILE: ret =  responseVoid(p); break;
             case RIL_REQUEST_STK_SEND_ENVELOPE_COMMAND: ret =  responseString(p); break;
             case RIL_REQUEST_STK_SEND_TERMINAL_RESPONSE: ret =  responseVoid(p); break;
             case RIL_REQUEST_STK_HANDLE_CALL_SETUP_REQUESTED_FROM_SIM: ret =  responseInts(p); break;
             case RIL_REQUEST_EXPLICIT_CALL_TRANSFER: ret =  responseVoid(p); break;
             case RIL_REQUEST_SET_PREFERRED_NETWORK_TYPE: ret =  responseVoid(p); break;
             case RIL_REQUEST_GET_PREFERRED_NETWORK_TYPE: ret =  responseGetPreferredNetworkType(p); break;
             case RIL_REQUEST_GET_NEIGHBORING_CELL_IDS: ret = responseCellList(p); break;
             case RIL_REQUEST_SET_LOCATION_UPDATES: ret =  responseVoid(p); break;
             case RIL_REQUEST_CDMA_SET_SUBSCRIPTION_SOURCE: ret =  responseVoid(p); break;
             case RIL_REQUEST_CDMA_SET_ROAMING_PREFERENCE: ret =  responseVoid(p); break;
             case RIL_REQUEST_CDMA_QUERY_ROAMING_PREFERENCE: ret =  responseInts(p); break;
             case RIL_REQUEST_SET_TTY_MODE: ret =  responseVoid(p); break;
             case RIL_REQUEST_QUERY_TTY_MODE: ret =  responseInts(p); break;
             case RIL_REQUEST_CDMA_SET_PREFERRED_VOICE_PRIVACY_MODE: ret =  responseVoid(p); break;
             case RIL_REQUEST_CDMA_QUERY_PREFERRED_VOICE_PRIVACY_MODE: ret =  responseInts(p); break;
             case RIL_REQUEST_CDMA_FLASH: ret =  responseVoid(p); break;
             case RIL_REQUEST_CDMA_BURST_DTMF: ret =  responseVoid(p); break;
             case RIL_REQUEST_CDMA_SEND_SMS: ret =  responseSMS(p); break;
             case RIL_REQUEST_CDMA_SMS_ACKNOWLEDGE: ret =  responseVoid(p); break;
             case RIL_REQUEST_GSM_GET_BROADCAST_CONFIG: ret =  responseGmsBroadcastConfig(p); break;
             case RIL_REQUEST_GSM_SET_BROADCAST_CONFIG: ret =  responseVoid(p); break;
             case RIL_REQUEST_GSM_BROADCAST_ACTIVATION: ret =  responseVoid(p); break;
             case RIL_REQUEST_CDMA_GET_BROADCAST_CONFIG: ret =  responseCdmaBroadcastConfig(p); break;
             case RIL_REQUEST_CDMA_SET_BROADCAST_CONFIG: ret =  responseVoid(p); break;
             case RIL_REQUEST_CDMA_BROADCAST_ACTIVATION: ret =  responseVoid(p); break;
             case RIL_REQUEST_CDMA_VALIDATE_AND_WRITE_AKEY: ret =  responseVoid(p); break;
             case RIL_REQUEST_CDMA_SUBSCRIPTION: ret =  responseStrings(p); break;
             case RIL_REQUEST_CDMA_WRITE_SMS_TO_RUIM: ret =  responseInts(p); break;
             case RIL_REQUEST_CDMA_DELETE_SMS_ON_RUIM: ret =  responseVoid(p); break;
             case RIL_REQUEST_DEVICE_IDENTITY: ret =  responseStrings(p); break;
             case RIL_REQUEST_GET_SMSC_ADDRESS: ret = responseString(p); break;
             case RIL_REQUEST_SET_SMSC_ADDRESS: ret = responseVoid(p); break;
             case RIL_REQUEST_EXIT_EMERGENCY_CALLBACK_MODE: ret = responseVoid(p); break;
             case RIL_REQUEST_REPORT_SMS_MEMORY_STATUS: ret = responseVoid(p); break;
             case RIL_REQUEST_REPORT_STK_SERVICE_IS_RUNNING: ret = responseVoid(p); break;
             case RIL_REQUEST_CDMA_GET_SUBSCRIPTION_SOURCE: ret =  responseInts(p); break;
             case RIL_REQUEST_ISIM_AUTHENTICATION: ret =  responseString(p); break;
             case RIL_REQUEST_ACKNOWLEDGE_INCOMING_GSM_SMS_WITH_PDU: ret = responseVoid(p); break;
             case RIL_REQUEST_STK_SEND_ENVELOPE_WITH_STATUS: ret = responseICC_IO(p); break;
             case RIL_REQUEST_VOICE_RADIO_TECH: ret = responseInts(p); break;
             case RIL_REQUEST_GET_CELL_INFO_LIST: ret = responseCellInfoList(p); break;
             case RIL_REQUEST_SET_UNSOL_CELL_INFO_LIST_RATE: ret = responseVoid(p); break;
             case RIL_REQUEST_SET_INITIAL_ATTACH_APN: ret = responseVoid(p); break;
             case RIL_REQUEST_IMS_REGISTRATION_STATE: ret = responseInts(p); break;
             case RIL_REQUEST_IMS_SEND_SMS: ret =  responseSMS(p); break;
             default:
                 throw new RuntimeException("Unrecognized solicited response: " + rr.mRequest);
             //break;
             }} catch (Throwable tr) {
                 // Exceptions here usually mean invalid RIL responses
 
                 Rlog.w(RILJ_LOG_TAG, rr.serialString() + "< "
                         + requestToString(rr.mRequest)
                         + " exception, possible invalid RIL response", tr);
 
                 if (rr.mResult != null) {
                     AsyncResult.forMessage(rr.mResult, null, tr);
                     rr.mResult.sendToTarget();
                 }
                 return rr;
             }
         }
 
         // Here and below fake RIL_UNSOL_RESPONSE_SIM_STATUS_CHANGED, see b/7255789.
         // This is needed otherwise we don't automatically transition to the main lock
         // screen when the pin or puk is entered incorrectly.
         switch (rr.mRequest) {
             case RIL_REQUEST_ENTER_SIM_PUK:
             case RIL_REQUEST_ENTER_SIM_PUK2:
                 if (mIccStatusChangedRegistrants != null) {
                     if (RILJ_LOGD) {
                         riljLog("ON enter sim puk fakeSimStatusChanged: reg count="
                                 + mIccStatusChangedRegistrants.size());
                     }
                     mIccStatusChangedRegistrants.notifyRegistrants();
                 }
                 break;
         }
 
         if (error != 0) {
             switch (rr.mRequest) {
                 case RIL_REQUEST_ENTER_SIM_PIN:
                 case RIL_REQUEST_ENTER_SIM_PIN2:
                 case RIL_REQUEST_CHANGE_SIM_PIN:
                 case RIL_REQUEST_CHANGE_SIM_PIN2:
                 case RIL_REQUEST_SET_FACILITY_LOCK:
                     if (mIccStatusChangedRegistrants != null) {
                         if (RILJ_LOGD) {
                             riljLog("ON some errors fakeSimStatusChanged: reg count="
                                     + mIccStatusChangedRegistrants.size());
                         }
                         mIccStatusChangedRegistrants.notifyRegistrants();
                     }
                     break;
             }
 
             rr.onError(error, ret);
         } else {
 
             if (RILJ_LOGD) riljLog(rr.serialString() + "< " + requestToString(rr.mRequest)
                     + " " + retToString(rr.mRequest, ret));
 
             if (rr.mResult != null) {
                 AsyncResult.forMessage(rr.mResult, ret, null);
                 rr.mResult.sendToTarget();
             }
         }
         return rr;
     }
 
     // CDMA FIXES, this fixes  bogus values in nv/sim on d2/jf/t0 cdma family or bogus information from sim card
     private Object
     operatorCheck(Parcel p) {
         String response[] = (String[])responseStrings(p);
         for(int i=0; i<2; i++){
             if (response[i]!= null){
                 response[i] = Operators.operatorReplace(response[i]);
             }
         }
         return response;
     }
     // handle exceptions
     private Object
     responseVoiceDataRegistrationState(Parcel p) {
         String response[] = (String[])responseStrings(p);
         if (isGSM){
             return response;
         }
         if ( response.length>=10){
             for(int i=6; i<=9; i++){
                 if (response[i]== null){
                     response[i]=Integer.toString(Integer.MAX_VALUE);
                 } else {
                     try {
                         Integer.parseInt(response[i]);
                     } catch(NumberFormatException e) {
                         response[i]=Integer.toString(Integer.parseInt(response[i],16));
                     }
                 }
             }
         }
 
         return response;
     }
     // has no effect
     // for debugging purposes , just generate out anything from response
     public static String s(String a[]){
         StringBuffer result = new StringBuffer();
 
         for (int i = 0; i < a.length; i++) {
             result.append( a[i] );
             result.append(",");
         }
         return result.toString();
     }
     // end  of cdma fix
 
     /**
      * Set audio parameter "wb_amr" for HD-Voice (Wideband AMR).
      *
      * @param state: 0 = unsupported, 1 = supported.
      * REQUIRED FOR JF FAMILY THIS SETS THE INFORMATION
      * CRASHES WITHOUT THIS FUNCTION
      * part of the new csd binary
      */
     private void setWbAmr(int state) {
         if (state == 1) {
             Rlog.d(RILJ_LOG_TAG, "setWbAmr(): setting audio parameter - wb_amr=on");
             mAudioManager.setParameters("wide_voice_enable=true");
         }else if (state == 0) {
            Rlog.d(RILJ_LOG_TAG, "setWbAmr(): setting audio parameter - wb_amr=off");
             mAudioManager.setParameters("wide_voice_enable=false");
         }
     }
 
     // Workaround for Samsung CDMA "ring of death" bug:
     //
     // Symptom: As soon as the phone receives notice of an incoming call, an
     // audible "old fashioned ring" is emitted through the earpiece and
     // persists through the duration of the call, or until reboot if the call
     // isn't answered.
     //
     // Background: The CDMA telephony stack implements a number of "signal info
     // tones" that are locally generated by ToneGenerator and mixed into the
     // voice call path in response to radio RIL_UNSOL_CDMA_INFO_REC requests.
     // One of these tones, IS95_CONST_IR_SIG_IS54B_L, is requested by the
     // radio just prior to notice of an incoming call when the voice call
     // path is muted. CallNotifier is responsible for stopping all signal
     // tones (by "playing" the TONE_CDMA_SIGNAL_OFF tone) upon receipt of a
     // "new ringing connection", prior to unmuting the voice call path.
     //
     // Problem: CallNotifier's incoming call path is designed to minimize
     // latency to notify users of incoming calls ASAP. Thus,
     // SignalInfoTonePlayer requests are handled asynchronously by spawning a
     // one-shot thread for each. Unfortunately the ToneGenerator API does
     // not provide a mechanism to specify an ordering on requests, and thus,
     // unexpected thread interleaving may result in ToneGenerator processing
     // them in the opposite order that CallNotifier intended. In this case,
     // playing the "signal off" tone first, followed by playing the "old
     // fashioned ring" indefinitely.
     //
     // Solution: An API change to ToneGenerator is required to enable
     // SignalInfoTonePlayer to impose an ordering on requests (i.e., drop any
     // request that's older than the most recent observed). Such a change,
     // or another appropriate fix should be implemented in AOSP first.
     //
     // Workaround: Intercept RIL_UNSOL_CDMA_INFO_REC requests from the radio,
     // check for a signal info record matching IS95_CONST_IR_SIG_IS54B_L, and
     // drop it so it's never seen by CallNotifier. If other signal tones are
     // observed to cause this problem, they should be dropped here as well.
     @Override
     protected void notifyRegistrantsCdmaInfoRec(CdmaInformationRecords infoRec) {
         final int response = RIL_UNSOL_CDMA_INFO_REC;
 
         if (infoRec.record instanceof CdmaSignalInfoRec) {
             CdmaSignalInfoRec sir = (CdmaSignalInfoRec) infoRec.record;
             if (sir != null
                     && sir.isPresent
                     && sir.signalType == SignalToneUtil.IS95_CONST_IR_SIGNAL_IS54B
                     && sir.alertPitch == SignalToneUtil.IS95_CONST_IR_ALERT_MED
                     && sir.signal == SignalToneUtil.IS95_CONST_IR_SIG_IS54B_L) {
 
                 Rlog.d(RILJ_LOG_TAG, "Dropping \"" + responseToString(response) + " "
                         + retToString(response, sir)
                         + "\" to prevent \"ring of death\" bug.");
                 return;
             }
         }
 
         super.notifyRegistrantsCdmaInfoRec(infoRec);
     }
 
     private void
     handleNitzTimeReceived(Parcel p) {
         String nitz = (String)responseString(p);
         //if (RILJ_LOGD) unsljLogRet(RIL_UNSOL_NITZ_TIME_RECEIVED, nitz);
 
         // has bonus long containing milliseconds since boot that the NITZ
         // time was received
         long nitzReceiveTime = p.readLong();
 
         Object[] result = new Object[2];
 
         String fixedNitz = nitz;
         String[] nitzParts = nitz.split(",");
         if (nitzParts.length == 4) {
             // 0=date, 1=time+zone, 2=dst, 3=garbage that confuses GsmServiceStateTracker (so remove it)
             fixedNitz = nitzParts[0]+","+nitzParts[1]+","+nitzParts[2]+",";
         }
 
         result[0] = fixedNitz;
         result[1] = Long.valueOf(nitzReceiveTime);
 
         boolean ignoreNitz = SystemProperties.getBoolean(
                         TelephonyProperties.PROPERTY_IGNORE_NITZ, false);
 
         if (ignoreNitz) {
             if (RILJ_LOGD) riljLog("ignoring UNSOL_NITZ_TIME_RECEIVED");
         } else {
             if (mNITZTimeRegistrant != null) {
                 mNITZTimeRegistrant
                 .notifyRegistrant(new AsyncResult (null, result, null));
             } else {
                 // in case NITZ time registrant isnt registered yet
                 mLastNITZTimeInfo = result;
             }
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
 
     @Override
     public void
     dial(String address, int clirMode, UUSInfo uusInfo, Message result) {
         if(!dialCode){
             super.dial(address, clirMode, uusInfo, result);
             return;
         }
         RILRequest rr = RILRequest.obtain(RIL_REQUEST_DIAL, result);
 
         rr.mParcel.writeString(address);
         rr.mParcel.writeInt(clirMode);
         rr.mParcel.writeInt(0);
         rr.mParcel.writeInt(1);
         rr.mParcel.writeString("");
 
         if (uusInfo == null) {
             rr.mParcel.writeInt(0); // UUS information is absent
         } else {
             rr.mParcel.writeInt(1); // UUS information is present
             rr.mParcel.writeInt(uusInfo.getType());
             rr.mParcel.writeInt(uusInfo.getDcs());
             rr.mParcel.writeByteArray(uusInfo.getUserData());
         }
 
         if (RILJ_LOGD) riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
 
         send(rr);
     }
 
     //this method is used in the search network functionality.
     // in mobile network setting-> network operators
     @Override
     protected Object
     responseOperatorInfos(Parcel p) {
         String strings[] = (String [])responseStrings(p);
         ArrayList<OperatorInfo> ret;
 
         if (strings.length % mQANElements != 0) {
             throw new RuntimeException(
                                        "RIL_REQUEST_QUERY_AVAILABLE_NETWORKS: invalid response. Got "
                                        + strings.length + " strings, expected multiple of " + mQANElements);
         }
 
         ret = new ArrayList<OperatorInfo>(strings.length / mQANElements);
         Operators init = null;
         if (strings.length != 0) {
             init = new Operators();
         }
         for (int i = 0 ; i < strings.length ; i += mQANElements) {
             String temp = init.unOptimizedOperatorReplace(strings[i+0]);
             ret.add (
                      new OperatorInfo(
                                       temp, //operatorAlphaLong
                                       temp,//operatorAlphaShort
                                       strings[i+2],//operatorNumeric
                                       strings[i+3]));//state
         }
 
         return ret;
     }
 }
