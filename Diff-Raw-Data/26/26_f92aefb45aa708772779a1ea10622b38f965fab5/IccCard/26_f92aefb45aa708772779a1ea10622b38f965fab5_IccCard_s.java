 /*
  * Copyright (C) 2006 The Android Open Source Project
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
 
 import static android.Manifest.permission.READ_PHONE_STATE;
 import android.app.ActivityManagerNative;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.AsyncResult;
 import android.os.Handler;
 import android.os.Message;
 import android.os.PowerManager;
 import android.os.Registrant;
 import android.os.RegistrantList;
 import android.util.Log;
 import android.view.WindowManager;
 
 import com.android.internal.telephony.PhoneBase;
 import com.android.internal.telephony.CommandsInterface.RadioState;
 import com.android.internal.telephony.gsm.GSMPhone;
 import com.android.internal.telephony.gsm.SIMFileHandler;
 import com.android.internal.telephony.gsm.SIMRecords;
 import com.android.internal.telephony.sip.SipPhone;
 import com.android.internal.telephony.cat.CatService;
 import com.android.internal.telephony.cdma.CDMALTEPhone;
 import com.android.internal.telephony.cdma.CDMAPhone;
 import com.android.internal.telephony.cdma.CdmaLteUiccFileHandler;
 import com.android.internal.telephony.cdma.CdmaLteUiccRecords;
 import com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager;
 import com.android.internal.telephony.cdma.RuimFileHandler;
 import com.android.internal.telephony.cdma.RuimRecords;
 
 import com.android.internal.R;
 
 /**
  * {@hide}
  */
 public class IccCard {
     protected String mLogTag;
     protected boolean mDbg;
 
     protected IccCardStatus mIccCardStatus = null;
     protected IccCardConstants.State mState = null;
     private final Object mStateMonitor = new Object();
 
     protected boolean is3gpp = true;
     protected boolean isSubscriptionFromIccCard = true;
     protected CdmaSubscriptionSourceManager mCdmaSSM = null;
     protected PhoneBase mPhone;
     private IccRecords mIccRecords;
     private IccFileHandler mIccFileHandler;
     private CatService mCatService;
 
     private RegistrantList mAbsentRegistrants = new RegistrantList();
     private RegistrantList mPinLockedRegistrants = new RegistrantList();
     private RegistrantList mNetworkLockedRegistrants = new RegistrantList();
     protected RegistrantList mReadyRegistrants = new RegistrantList();
     protected RegistrantList mRuimReadyRegistrants = new RegistrantList();
 
     private boolean mDesiredPinLocked;
     private boolean mDesiredFdnEnabled;
     private boolean mIccPinLocked = true; // Default to locked
     private boolean mIccFdnEnabled = false; // Default to disabled.
                                             // Will be updated when SIM_READY.
 
     /* Parameter is3gpp's values to be passed to constructor */
     public final static boolean CARD_IS_3GPP = true;
     public final static boolean CARD_IS_NOT_3GPP = false;
 
     protected static final int EVENT_ICC_LOCKED = 1;
     private static final int EVENT_GET_ICC_STATUS_DONE = 2;
     protected static final int EVENT_RADIO_OFF_OR_NOT_AVAILABLE = 3;
     protected static final int EVENT_ICC_READY = 6;
     private static final int EVENT_QUERY_FACILITY_LOCK_DONE = 7;
     private static final int EVENT_CHANGE_FACILITY_LOCK_DONE = 8;
     private static final int EVENT_CHANGE_ICC_PASSWORD_DONE = 9;
     private static final int EVENT_QUERY_FACILITY_FDN_DONE = 10;
     private static final int EVENT_CHANGE_FACILITY_FDN_DONE = 11;
     private static final int EVENT_ICC_STATUS_CHANGED = 12;
     private static final int EVENT_CARD_REMOVED = 13;
     private static final int EVENT_CARD_ADDED = 14;
     protected static final int EVENT_CDMA_SUBSCRIPTION_SOURCE_CHANGED = 15;
     protected static final int EVENT_RADIO_ON = 16;
 
     public IccCardConstants.State getState() {
         if (mState == null) {
             switch(mPhone.mCM.getRadioState()) {
                 /* This switch block must not return anything in
                  * IccCardConstants.State.isLocked() or IccCardConstants.State.ABSENT.
                  * If it does, handleSimStatus() may break
                  */
                 case RADIO_OFF:
                 case RADIO_UNAVAILABLE:
                     return IccCardConstants.State.UNKNOWN;
                 default:
                     if (!is3gpp && !isSubscriptionFromIccCard) {
                         // CDMA can get subscription from NV. In that case,
                         // subscription is ready as soon as Radio is ON.
                         return IccCardConstants.State.READY;
                     }
             }
         } else {
             return mState;
         }
 
         return IccCardConstants.State.UNKNOWN;
     }
 
     public IccCard(PhoneBase phone, IccCardStatus ics, String logTag, boolean dbg) {
         mLogTag = logTag;
         mDbg = dbg;
         if (mDbg) log("Creating");
         update(phone, ics);
         mCdmaSSM = CdmaSubscriptionSourceManager.getInstance(mPhone.getContext(),
                 mPhone.mCM, mHandler, EVENT_CDMA_SUBSCRIPTION_SOURCE_CHANGED, null);
         mPhone.mCM.registerForOffOrNotAvailable(mHandler, EVENT_RADIO_OFF_OR_NOT_AVAILABLE, null);
         mPhone.mCM.registerForOn(mHandler, EVENT_RADIO_ON, null);
     }
 
     public void dispose() {
         if (mDbg) log("Disposing card type " + (is3gpp ? "3gpp" : "3gpp2"));
        mPhone.mCM.unregisterForIccStatusChanged(mHandler);
         mPhone.mCM.unregisterForOffOrNotAvailable(mHandler);
         mPhone.mCM.unregisterForOn(mHandler);
         mCatService.dispose();
         mCdmaSSM.dispose(mHandler);
         mIccRecords.dispose();
         mIccFileHandler.dispose();
     }
 
     public void update(PhoneBase phone, IccCardStatus ics) {
         if (phone != mPhone) {
             PhoneBase oldPhone = mPhone;
             mPhone = phone;
             log("Update");
             if (phone instanceof GSMPhone) {
                 is3gpp = true;
             } else if (phone instanceof CDMALTEPhone){
                 is3gpp = true;
             } else if (phone instanceof CDMAPhone){
                 is3gpp = false;
             } else if (phone instanceof SipPhone){
                 is3gpp = true;
             } else {
                 throw new RuntimeException("Update: Unhandled phone type. Critical error!" +
                         phone.getPhoneName());
             }
 
 
             if (phone.mCM.getLteOnCdmaMode() == PhoneConstants.LTE_ON_CDMA_TRUE
                     && phone instanceof CDMALTEPhone) {
                 mIccFileHandler = new CdmaLteUiccFileHandler(this, "", mPhone.mCM);
                 mIccRecords = new CdmaLteUiccRecords(this, mPhone.mContext, mPhone.mCM);
             } else {
                 // Correct aid will be set later (when GET_SIM_STATUS returns)
                 mIccFileHandler = is3gpp ? new SIMFileHandler(this, "", mPhone.mCM) :
                                            new RuimFileHandler(this, "", mPhone.mCM);
                 mIccRecords = is3gpp ? new SIMRecords(this, mPhone.mContext, mPhone.mCM) :
                                        new RuimRecords(this, mPhone.mContext, mPhone.mCM);
             }
             mCatService = CatService.getInstance(mPhone.mCM, mIccRecords, mPhone.mContext,
                     mIccFileHandler, this);
         }
         mHandler.sendMessage(mHandler.obtainMessage(EVENT_GET_ICC_STATUS_DONE, ics));
     }
 
     protected void finalize() {
         if (mDbg) log("[IccCard] Finalized card type " + (is3gpp ? "3gpp" : "3gpp2"));
     }
 
     public IccRecords getIccRecords() {
         return mIccRecords;
     }
 
     public IccFileHandler getIccFileHandler() {
         return mIccFileHandler;
     }
 
     /**
      * Notifies handler of any transition into IccCardConstants.State.ABSENT
      */
     public void registerForAbsent(Handler h, int what, Object obj) {
         Registrant r = new Registrant (h, what, obj);
 
         mAbsentRegistrants.add(r);
 
         if (getState() == IccCardConstants.State.ABSENT) {
             r.notifyRegistrant();
         }
     }
 
     public void unregisterForAbsent(Handler h) {
         mAbsentRegistrants.remove(h);
     }
 
     /**
      * Notifies handler of any transition into IccCardConstants.State.NETWORK_LOCKED
      */
     public void registerForNetworkLocked(Handler h, int what, Object obj) {
         Registrant r = new Registrant (h, what, obj);
 
         mNetworkLockedRegistrants.add(r);
 
         if (getState() == IccCardConstants.State.NETWORK_LOCKED) {
             r.notifyRegistrant();
         }
     }
 
     public void unregisterForNetworkLocked(Handler h) {
         mNetworkLockedRegistrants.remove(h);
     }
 
     /**
      * Notifies handler of any transition into IccCardConstants.State.isPinLocked()
      */
     public void registerForLocked(Handler h, int what, Object obj) {
         Registrant r = new Registrant (h, what, obj);
 
         mPinLockedRegistrants.add(r);
 
         if (getState().isPinLocked()) {
             r.notifyRegistrant();
         }
     }
 
     public void unregisterForLocked(Handler h) {
         mPinLockedRegistrants.remove(h);
     }
 
     public void registerForReady(Handler h, int what, Object obj) {
         Registrant r = new Registrant (h, what, obj);
 
         synchronized (mStateMonitor) {
             mReadyRegistrants.add(r);
 
             if (getState() == IccCardConstants.State.READY) {
                 r.notifyRegistrant(new AsyncResult(null, null, null));
             }
         }
     }
 
     public void unregisterForReady(Handler h) {
         synchronized (mStateMonitor) {
             mReadyRegistrants.remove(h);
         }
     }
 
     public IccCardConstants.State getRuimState() {
         if(mIccCardStatus != null) {
             return getAppState(mIccCardStatus.getCdmaSubscriptionAppIndex());
         } else {
             return IccCardConstants.State.UNKNOWN;
         }
     }
 
     public void registerForRuimReady(Handler h, int what, Object obj) {
         Registrant r = new Registrant (h, what, obj);
 
         synchronized (mStateMonitor) {
             mRuimReadyRegistrants.add(r);
 
             if (getState() == IccCardConstants.State.READY &&
                     getRuimState() == IccCardConstants.State.READY ) {
                 r.notifyRegistrant(new AsyncResult(null, null, null));
             }
         }
     }
 
     public void unregisterForRuimReady(Handler h) {
         synchronized (mStateMonitor) {
             mRuimReadyRegistrants.remove(h);
         }
     }
 
     /**
      * Supply the ICC PIN to the ICC
      *
      * When the operation is complete, onComplete will be sent to its
      * Handler.
      *
      * onComplete.obj will be an AsyncResult
      *
      * ((AsyncResult)onComplete.obj).exception == null on success
      * ((AsyncResult)onComplete.obj).exception != null on fail
      *
      * If the supplied PIN is incorrect:
      * ((AsyncResult)onComplete.obj).exception != null
      * && ((AsyncResult)onComplete.obj).exception
      *       instanceof com.android.internal.telephony.gsm.CommandException)
      * && ((CommandException)(((AsyncResult)onComplete.obj).exception))
      *          .getCommandError() == CommandException.Error.PASSWORD_INCORRECT
      *
      *
      */
 
     public void supplyPin (String pin, Message onComplete) {
         mPhone.mCM.supplyIccPin(pin, onComplete);
     }
 
     public void supplyPuk (String puk, String newPin, Message onComplete) {
         mPhone.mCM.supplyIccPuk(puk, newPin, onComplete);
     }
 
     public void supplyPin2 (String pin2, Message onComplete) {
         mPhone.mCM.supplyIccPin2(pin2, onComplete);
     }
 
     public void supplyPuk2 (String puk2, String newPin2, Message onComplete) {
         mPhone.mCM.supplyIccPuk2(puk2, newPin2, onComplete);
     }
 
     public void supplyNetworkDepersonalization (String pin, Message onComplete) {
         mPhone.mCM.supplyNetworkDepersonalization(pin, onComplete);
     }
 
     /**
      * Check whether ICC pin lock is enabled
      * This is a sync call which returns the cached pin enabled state
      *
      * @return true for ICC locked enabled
      *         false for ICC locked disabled
      */
     public boolean getIccLockEnabled() {
         return mIccPinLocked;
      }
 
     /**
      * Check whether ICC fdn (fixed dialing number) is enabled
      * This is a sync call which returns the cached pin enabled state
      *
      * @return true for ICC fdn enabled
      *         false for ICC fdn disabled
      */
      public boolean getIccFdnEnabled() {
         return mIccFdnEnabled;
      }
 
      /**
       * Set the ICC pin lock enabled or disabled
       * When the operation is complete, onComplete will be sent to its handler
       *
       * @param enabled "true" for locked "false" for unlocked.
       * @param password needed to change the ICC pin state, aka. Pin1
       * @param onComplete
       *        onComplete.obj will be an AsyncResult
       *        ((AsyncResult)onComplete.obj).exception == null on success
       *        ((AsyncResult)onComplete.obj).exception != null on fail
       */
      public void setIccLockEnabled (boolean enabled,
              String password, Message onComplete) {
          int serviceClassX;
          serviceClassX = CommandsInterface.SERVICE_CLASS_VOICE +
                  CommandsInterface.SERVICE_CLASS_DATA +
                  CommandsInterface.SERVICE_CLASS_FAX;
 
          mDesiredPinLocked = enabled;
 
          mPhone.mCM.setFacilityLock(CommandsInterface.CB_FACILITY_BA_SIM,
                  enabled, password, serviceClassX,
                  mHandler.obtainMessage(EVENT_CHANGE_FACILITY_LOCK_DONE, onComplete));
      }
 
      /**
       * Set the ICC fdn enabled or disabled
       * When the operation is complete, onComplete will be sent to its handler
       *
       * @param enabled "true" for locked "false" for unlocked.
       * @param password needed to change the ICC fdn enable, aka Pin2
       * @param onComplete
       *        onComplete.obj will be an AsyncResult
       *        ((AsyncResult)onComplete.obj).exception == null on success
       *        ((AsyncResult)onComplete.obj).exception != null on fail
       */
      public void setIccFdnEnabled (boolean enabled,
              String password, Message onComplete) {
          int serviceClassX;
          serviceClassX = CommandsInterface.SERVICE_CLASS_VOICE +
                  CommandsInterface.SERVICE_CLASS_DATA +
                  CommandsInterface.SERVICE_CLASS_FAX +
                  CommandsInterface.SERVICE_CLASS_SMS;
 
          mDesiredFdnEnabled = enabled;
 
          mPhone.mCM.setFacilityLock(CommandsInterface.CB_FACILITY_BA_FD,
                  enabled, password, serviceClassX,
                  mHandler.obtainMessage(EVENT_CHANGE_FACILITY_FDN_DONE, onComplete));
      }
 
      /**
       * Change the ICC password used in ICC pin lock
       * When the operation is complete, onComplete will be sent to its handler
       *
       * @param oldPassword is the old password
       * @param newPassword is the new password
       * @param onComplete
       *        onComplete.obj will be an AsyncResult
       *        ((AsyncResult)onComplete.obj).exception == null on success
       *        ((AsyncResult)onComplete.obj).exception != null on fail
       */
      public void changeIccLockPassword(String oldPassword, String newPassword,
              Message onComplete) {
          mPhone.mCM.changeIccPin(oldPassword, newPassword,
                  mHandler.obtainMessage(EVENT_CHANGE_ICC_PASSWORD_DONE, onComplete));
 
      }
 
      /**
       * Change the ICC password used in ICC fdn enable
       * When the operation is complete, onComplete will be sent to its handler
       *
       * @param oldPassword is the old password
       * @param newPassword is the new password
       * @param onComplete
       *        onComplete.obj will be an AsyncResult
       *        ((AsyncResult)onComplete.obj).exception == null on success
       *        ((AsyncResult)onComplete.obj).exception != null on fail
       */
      public void changeIccFdnPassword(String oldPassword, String newPassword,
              Message onComplete) {
          mPhone.mCM.changeIccPin2(oldPassword, newPassword,
                  mHandler.obtainMessage(EVENT_CHANGE_ICC_PASSWORD_DONE, onComplete));
 
      }
 
 
     /**
      * Returns service provider name stored in ICC card.
      * If there is no service provider name associated or the record is not
      * yet available, null will be returned <p>
      *
      * Please use this value when display Service Provider Name in idle mode <p>
      *
      * Usage of this provider name in the UI is a common carrier requirement.
      *
      * Also available via Android property "gsm.sim.operator.alpha"
      *
      * @return Service Provider Name stored in ICC card
      *         null if no service provider name associated or the record is not
      *         yet available
      *
      */
     public String getServiceProviderName () {
         return mIccRecords.getServiceProviderName();
     }
 
     protected void updateStateProperty() {
         mPhone.setSystemProperty(TelephonyProperties.PROPERTY_SIM_STATE, getState().toString());
     }
 
     private void getIccCardStatusDone(IccCardStatus ics) {
         handleIccCardStatus(ics);
     }
 
     private void handleIccCardStatus(IccCardStatus newCardStatus) {
         boolean transitionedIntoPinLocked;
         boolean transitionedIntoAbsent;
         boolean transitionedIntoNetworkLocked;
         boolean transitionedIntoPermBlocked;
         boolean isIccCardRemoved;
         boolean isIccCardAdded;
 
         IccCardConstants.State oldState, newState;
         IccCardConstants.State oldRuimState = getRuimState();
 
         oldState = mState;
         mIccCardStatus = newCardStatus;
         newState = getIccCardState();
 
         synchronized (mStateMonitor) {
             mState = newState;
             updateStateProperty();
             if (oldState != IccCardConstants.State.READY &&
                     newState == IccCardConstants.State.READY) {
                 mHandler.sendMessage(mHandler.obtainMessage(EVENT_ICC_READY));
                 mReadyRegistrants.notifyRegistrants();
             } else if (newState.isPinLocked()) {
                 mHandler.sendMessage(mHandler.obtainMessage(EVENT_ICC_LOCKED));
             }
             if (oldRuimState != IccCardConstants.State.READY &&
                     getRuimState() == IccCardConstants.State.READY) {
                 mRuimReadyRegistrants.notifyRegistrants();
             }
         }
 
         transitionedIntoPinLocked = (
                  (oldState != IccCardConstants.State.PIN_REQUIRED &&
                      newState == IccCardConstants.State.PIN_REQUIRED)
               || (oldState != IccCardConstants.State.PUK_REQUIRED &&
                       newState == IccCardConstants.State.PUK_REQUIRED));
         transitionedIntoAbsent = (oldState != IccCardConstants.State.ABSENT &&
                 newState == IccCardConstants.State.ABSENT);
         transitionedIntoNetworkLocked = (oldState != IccCardConstants.State.NETWORK_LOCKED
                 && newState == IccCardConstants.State.NETWORK_LOCKED);
         transitionedIntoPermBlocked = (oldState != IccCardConstants.State.PERM_DISABLED
                 && newState == IccCardConstants.State.PERM_DISABLED);
         isIccCardRemoved = (oldState != null && oldState.iccCardExist() &&
                     newState == IccCardConstants.State.ABSENT);
         isIccCardAdded = (oldState == IccCardConstants.State.ABSENT &&
                         newState != null && newState.iccCardExist());
 
         if (transitionedIntoPinLocked) {
             if (mDbg) log("Notify SIM pin or puk locked.");
             mPinLockedRegistrants.notifyRegistrants();
             broadcastIccStateChangedIntent(IccCardConstants.INTENT_VALUE_ICC_LOCKED,
                     (newState == IccCardConstants.State.PIN_REQUIRED) ?
                             IccCardConstants.INTENT_VALUE_LOCKED_ON_PIN :
                                 IccCardConstants.INTENT_VALUE_LOCKED_ON_PUK);
         } else if (transitionedIntoAbsent) {
             if (mDbg) log("Notify SIM missing.");
             mAbsentRegistrants.notifyRegistrants();
             broadcastIccStateChangedIntent(IccCardConstants.INTENT_VALUE_ICC_ABSENT, null);
         } else if (transitionedIntoNetworkLocked) {
             if (mDbg) log("Notify SIM network locked.");
             mNetworkLockedRegistrants.notifyRegistrants();
             broadcastIccStateChangedIntent(IccCardConstants.INTENT_VALUE_ICC_LOCKED,
                     IccCardConstants.INTENT_VALUE_LOCKED_NETWORK);
         } else if (transitionedIntoPermBlocked) {
             if (mDbg) log("Notify SIM permanently disabled.");
             broadcastIccStateChangedIntent(IccCardConstants.INTENT_VALUE_ICC_ABSENT,
                     IccCardConstants.INTENT_VALUE_ABSENT_ON_PERM_DISABLED);
         }
 
         if (isIccCardRemoved) {
             mHandler.sendMessage(mHandler.obtainMessage(EVENT_CARD_REMOVED, null));
         } else if (isIccCardAdded) {
             mHandler.sendMessage(mHandler.obtainMessage(EVENT_CARD_ADDED, null));
         }
 
         // Call onReady Record(s) on the IccCard becomes ready (not NV)
         if (oldState != IccCardConstants.State.READY && newState == IccCardConstants.State.READY &&
                 (is3gpp || isSubscriptionFromIccCard)) {
             mIccFileHandler.setAid(getAid());
             broadcastIccStateChangedIntent(IccCardConstants.INTENT_VALUE_ICC_READY, null);
             mIccRecords.onReady();
         }
     }
 
     private void onIccSwap(boolean isAdded) {
         // TODO: Here we assume the device can't handle SIM hot-swap
         //      and has to reboot. We may want to add a property,
         //      e.g. REBOOT_ON_SIM_SWAP, to indicate if modem support
         //      hot-swap.
         DialogInterface.OnClickListener listener = null;
 
 
         // TODO: SimRecords is not reset while SIM ABSENT (only reset while
         //       Radio_off_or_not_available). Have to reset in both both
         //       added or removed situation.
         listener = new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 if (which == DialogInterface.BUTTON_POSITIVE) {
                     if (mDbg) log("Reboot due to SIM swap");
                     PowerManager pm = (PowerManager) mPhone.getContext()
                     .getSystemService(Context.POWER_SERVICE);
                     pm.reboot("SIM is added.");
                 }
             }
 
         };
 
         Resources r = Resources.getSystem();
 
         String title = (isAdded) ? r.getString(R.string.sim_added_title) :
             r.getString(R.string.sim_removed_title);
         String message = (isAdded) ? r.getString(R.string.sim_added_message) :
             r.getString(R.string.sim_removed_message);
         String buttonTxt = r.getString(R.string.sim_restart_button);
 
         AlertDialog dialog = new AlertDialog.Builder(mPhone.getContext())
             .setTitle(title)
             .setMessage(message)
             .setPositiveButton(buttonTxt, listener)
             .create();
         dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
         dialog.show();
     }
 
     /**
      * Interperate EVENT_QUERY_FACILITY_LOCK_DONE
      * @param ar is asyncResult of Query_Facility_Locked
      */
     private void onQueryFdnEnabled(AsyncResult ar) {
         if(ar.exception != null) {
             if(mDbg) log("Error in querying facility lock:" + ar.exception);
             return;
         }
 
         int[] ints = (int[])ar.result;
         if(ints.length != 0) {
             mIccFdnEnabled = (0!=ints[0]);
             if(mDbg) log("Query facility lock : "  + mIccFdnEnabled);
         } else {
             Log.e(mLogTag, "[IccCard] Bogus facility lock response");
         }
     }
 
     /**
      * Interperate EVENT_QUERY_FACILITY_LOCK_DONE
      * @param ar is asyncResult of Query_Facility_Locked
      */
     private void onQueryFacilityLock(AsyncResult ar) {
         if(ar.exception != null) {
             if (mDbg) log("Error in querying facility lock:" + ar.exception);
             return;
         }
 
         int[] ints = (int[])ar.result;
         if(ints.length != 0) {
             mIccPinLocked = (0!=ints[0]);
             if(mDbg) log("Query facility lock : "  + mIccPinLocked);
         } else {
             Log.e(mLogTag, "[IccCard] Bogus facility lock response");
         }
     }
 
     public void broadcastIccStateChangedIntent(String value, String reason) {
         Intent intent = new Intent(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
         intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
         intent.putExtra(PhoneConstants.PHONE_NAME_KEY, mPhone.getPhoneName());
         intent.putExtra(IccCardConstants.INTENT_KEY_ICC_STATE, value);
         intent.putExtra(IccCardConstants.INTENT_KEY_LOCKED_REASON, reason);
         if(mDbg) log("Broadcasting intent ACTION_SIM_STATE_CHANGED " +  value
                 + " reason " + reason);
         ActivityManagerNative.broadcastStickyIntent(intent, READ_PHONE_STATE);
     }
 
     protected Handler mHandler = new Handler() {
         @Override
         public void handleMessage(Message msg){
             AsyncResult ar;
             int serviceClassX;
 
             serviceClassX = CommandsInterface.SERVICE_CLASS_VOICE +
                             CommandsInterface.SERVICE_CLASS_DATA +
                             CommandsInterface.SERVICE_CLASS_FAX;
 
             if (!mPhone.mIsTheCurrentActivePhone) {
                 Log.e(mLogTag, "Received message " + msg + "[" + msg.what
                         + "] while being destroyed. Ignoring.");
                 return;
             }
 
             switch (msg.what) {
                 case EVENT_RADIO_OFF_OR_NOT_AVAILABLE:
                     mState = null;
                     updateStateProperty();
                     broadcastIccStateChangedIntent(IccCardConstants.INTENT_VALUE_ICC_NOT_READY,
                             null);
                     break;
                 case EVENT_RADIO_ON:
                     if (!is3gpp) {
                         handleCdmaSubscriptionSource();
                     }
                     break;
                 case EVENT_CDMA_SUBSCRIPTION_SOURCE_CHANGED:
                     handleCdmaSubscriptionSource();
                     break;
                 case EVENT_ICC_READY:
                     if(isSubscriptionFromIccCard) {
                         mPhone.mCM.queryFacilityLock (
                                 CommandsInterface.CB_FACILITY_BA_SIM, "", serviceClassX,
                                 obtainMessage(EVENT_QUERY_FACILITY_LOCK_DONE));
                         mPhone.mCM.queryFacilityLock (
                                 CommandsInterface.CB_FACILITY_BA_FD, "", serviceClassX,
                                 obtainMessage(EVENT_QUERY_FACILITY_FDN_DONE));
                     }
                     break;
                 case EVENT_ICC_LOCKED:
                     mPhone.mCM.queryFacilityLock (
                              CommandsInterface.CB_FACILITY_BA_SIM, "", serviceClassX,
                              obtainMessage(EVENT_QUERY_FACILITY_LOCK_DONE));
                      break;
                 case EVENT_GET_ICC_STATUS_DONE:
                     IccCardStatus cs = (IccCardStatus)msg.obj;
 
                     getIccCardStatusDone(cs);
                     break;
                 case EVENT_QUERY_FACILITY_LOCK_DONE:
                     ar = (AsyncResult)msg.obj;
                     onQueryFacilityLock(ar);
                     break;
                 case EVENT_QUERY_FACILITY_FDN_DONE:
                     ar = (AsyncResult)msg.obj;
                     onQueryFdnEnabled(ar);
                     break;
                 case EVENT_CHANGE_FACILITY_LOCK_DONE:
                     ar = (AsyncResult)msg.obj;
                     if (ar.exception == null) {
                         mIccPinLocked = mDesiredPinLocked;
                         if (mDbg) log( "EVENT_CHANGE_FACILITY_LOCK_DONE: " +
                                 "mIccPinLocked= " + mIccPinLocked);
                     } else {
                         Log.e(mLogTag, "Error change facility lock with exception "
                             + ar.exception);
                     }
                     AsyncResult.forMessage(((Message)ar.userObj)).exception
                                                         = ar.exception;
                     ((Message)ar.userObj).sendToTarget();
                     break;
                 case EVENT_CHANGE_FACILITY_FDN_DONE:
                     ar = (AsyncResult)msg.obj;
 
                     if (ar.exception == null) {
                         mIccFdnEnabled = mDesiredFdnEnabled;
                         if (mDbg) log("EVENT_CHANGE_FACILITY_FDN_DONE: " +
                                 "mIccFdnEnabled=" + mIccFdnEnabled);
                     } else {
                         Log.e(mLogTag, "Error change facility fdn with exception "
                                 + ar.exception);
                     }
                     AsyncResult.forMessage(((Message)ar.userObj)).exception
                                                         = ar.exception;
                     ((Message)ar.userObj).sendToTarget();
                     break;
                 case EVENT_CHANGE_ICC_PASSWORD_DONE:
                     ar = (AsyncResult)msg.obj;
                     if(ar.exception != null) {
                         Log.e(mLogTag, "Error in change sim password with exception"
                             + ar.exception);
                     }
                     AsyncResult.forMessage(((Message)ar.userObj)).exception
                                                         = ar.exception;
                     ((Message)ar.userObj).sendToTarget();
                     break;
                 case EVENT_CARD_REMOVED:
                     onIccSwap(false);
                     break;
                 case EVENT_CARD_ADDED:
                     onIccSwap(true);
                     break;
                 default:
                     Log.e(mLogTag, "[IccCard] Unknown Event " + msg.what);
             }
         }
     };
 
     private void handleCdmaSubscriptionSource() {
         if(mCdmaSSM != null)  {
             int newSubscriptionSource = mCdmaSSM.getCdmaSubscriptionSource();
 
             Log.d(mLogTag, "Received Cdma subscription source: " + newSubscriptionSource);
 
             boolean isNewSubFromRuim =
                 (newSubscriptionSource == CdmaSubscriptionSourceManager.SUBSCRIPTION_FROM_RUIM);
 
             if (isNewSubFromRuim != isSubscriptionFromIccCard) {
                 isSubscriptionFromIccCard = isNewSubFromRuim;
                 // Parse the Stored IccCardStatus Message to set mState correctly.
                 handleIccCardStatus(mIccCardStatus);
             }
         }
     }
 
     public IccCardConstants.State getIccCardState() {
         if(!is3gpp && !isSubscriptionFromIccCard) {
             // CDMA can get subscription from NV. In that case,
             // subscription is ready as soon as Radio is ON.
             return IccCardConstants.State.READY;
         }
 
         if (mIccCardStatus == null) {
             Log.e(mLogTag, "[IccCard] IccCardStatus is null");
             return IccCardConstants.State.ABSENT;
         }
 
         // this is common for all radio technologies
         if (!mIccCardStatus.getCardState().isCardPresent()) {
             return IccCardConstants.State.ABSENT;
         }
 
         RadioState currentRadioState = mPhone.mCM.getRadioState();
         // check radio technology
         if( currentRadioState == RadioState.RADIO_OFF         ||
             currentRadioState == RadioState.RADIO_UNAVAILABLE) {
             return IccCardConstants.State.NOT_READY;
         }
 
         if( currentRadioState == RadioState.RADIO_ON ) {
             IccCardConstants.State csimState =
                 getAppState(mIccCardStatus.getCdmaSubscriptionAppIndex());
             IccCardConstants.State usimState =
                 getAppState(mIccCardStatus.getGsmUmtsSubscriptionAppIndex());
 
             if(mDbg) log("USIM=" + usimState + " CSIM=" + csimState);
 
             if (mPhone.getLteOnCdmaMode() == PhoneConstants.LTE_ON_CDMA_TRUE) {
                 // UICC card contains both USIM and CSIM
                 // Return consolidated status
                 return getConsolidatedState(csimState, usimState, csimState);
             }
 
             // check for CDMA radio technology
             if (!is3gpp) {
                 return csimState;
             }
             return usimState;
         }
 
         return IccCardConstants.State.ABSENT;
     }
 
     private IccCardConstants.State getAppState(int appIndex) {
         IccCardApplication app;
         if (appIndex >= 0 && appIndex < IccCardStatus.CARD_MAX_APPS) {
             app = mIccCardStatus.getApplication(appIndex);
         } else {
             Log.e(mLogTag, "[IccCard] Invalid Subscription Application index:" + appIndex);
             return IccCardConstants.State.ABSENT;
         }
 
         if (app == null) {
             Log.e(mLogTag, "[IccCard] Subscription Application in not present");
             return IccCardConstants.State.ABSENT;
         }
 
         // check if PIN required
         if (app.pin1.isPermBlocked()) {
             return IccCardConstants.State.PERM_DISABLED;
         }
         if (app.app_state.isPinRequired()) {
             return IccCardConstants.State.PIN_REQUIRED;
         }
         if (app.app_state.isPukRequired()) {
             return IccCardConstants.State.PUK_REQUIRED;
         }
         if (app.app_state.isSubscriptionPersoEnabled()) {
             return IccCardConstants.State.NETWORK_LOCKED;
         }
         if (app.app_state.isAppReady()) {
             return IccCardConstants.State.READY;
         }
         if (app.app_state.isAppNotReady()) {
             return IccCardConstants.State.NOT_READY;
         }
         return IccCardConstants.State.NOT_READY;
     }
 
     private IccCardConstants.State getConsolidatedState(IccCardConstants.State left,
             IccCardConstants.State right, IccCardConstants.State preferredState) {
         // Check if either is absent.
         if (right == IccCardConstants.State.ABSENT) return left;
         if (left == IccCardConstants.State.ABSENT) return right;
 
         // Only if both are ready, return ready
         if ((left == IccCardConstants.State.READY) && (right == IccCardConstants.State.READY)) {
             return IccCardConstants.State.READY;
         }
 
         // Case one is ready, but the other is not.
         if (((right == IccCardConstants.State.NOT_READY) &&
                 (left == IccCardConstants.State.READY)) ||
             ((left == IccCardConstants.State.NOT_READY) &&
                     (right == IccCardConstants.State.READY))) {
             return IccCardConstants.State.NOT_READY;
         }
 
         // At this point, the other state is assumed to be one of locked state
         if (right == IccCardConstants.State.NOT_READY) return left;
         if (left == IccCardConstants.State.NOT_READY) return right;
 
         // At this point, FW currently just assumes the status will be
         // consistent across the applications...
         return preferredState;
     }
 
     public boolean isApplicationOnIcc(IccCardApplication.AppType type) {
         if (mIccCardStatus == null) return false;
 
         for (int i = 0 ; i < mIccCardStatus.getNumApplications(); i++) {
             IccCardApplication app = mIccCardStatus.getApplication(i);
             if (app != null && app.app_type == type) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * @return true if a ICC card is present
      */
     public boolean hasIccCard() {
         if (mIccCardStatus == null) {
             return false;
         } else {
             // Returns ICC card status for both GSM and CDMA mode
             return mIccCardStatus.getCardState().isCardPresent();
         }
     }
 
     private void log(String msg) {
         Log.d(mLogTag, "[IccCard] " + msg);
     }
 
     private void loge(String msg) {
         Log.e(mLogTag, "[IccCard] " + msg);
     }
 
     protected int getCurrentApplicationIndex() {
         if (is3gpp) {
             return mIccCardStatus.getGsmUmtsSubscriptionAppIndex();
         } else {
             return mIccCardStatus.getCdmaSubscriptionAppIndex();
         }
     }
 
     public String getAid() {
         String aid = "";
         if (mIccCardStatus == null) {
             return aid;
         }
 
         int appIndex = getCurrentApplicationIndex();
 
         if (appIndex >= 0 && appIndex < IccCardStatus.CARD_MAX_APPS) {
             IccCardApplication app = mIccCardStatus.getApplication(appIndex);
             if (app != null) {
                 aid = app.aid;
             } else {
                 Log.e(mLogTag, "[IccCard] getAid: no current application index=" + appIndex);
             }
         } else {
             Log.e(mLogTag, "[IccCard] getAid: Invalid Subscription Application index=" + appIndex);
         }
 
         return aid;
     }
 }
