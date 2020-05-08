 package com.sysiq.android.callmanager.service;
 
 import android.content.Context;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.telephony.PhoneStateListener;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import com.j256.ormlite.android.apptools.OrmLiteBaseService;
 import com.sysiq.android.callmanager.model.CMDbHelper;
 import com.sysiq.android.callmanager.model.entity.CMItem;
 import com.sysiq.android.callmanager.model.entity.CMList;
 
 import java.sql.SQLException;
 import java.util.LinkedList;
 import java.util.List;
 
 public final class CMService extends OrmLiteBaseService<CMDbHelper> {
     public static final String TAG = "com.sysiq.android.callmanager.service.CMService";
     protected final List<String> numbers = new LinkedList<String>();
     protected AudioManager audioManager;
     private CMList cmList = null;
     private PhoneListener phoneListener = new PhoneListener();
     private boolean register = false;
     private TelephonyService telephonyService;
     private IBinder mBinder = new ICMService.Stub() {
         @Override
         public void reloadData(long id) throws RemoteException {
             try {
                 cmList = getHelper().getCmListDao().queryForId(id);
                if (null == cmList) {
                    deactivateReceiver();
                    return;
                }
                 cmList.refresh();
                 if (cmList.getEnabled()) {
                     if (!register)
                         activateReceiver();
                     numbers.clear();
                     numbers.addAll(getNumbers(cmList));
                 } else {
                     deactivateReceiver();
                 }
             } catch (SQLException e) {
                 Log.e(TAG, "SQL", e);
             }
         }
     };
 
     protected boolean isBlock(String phone) {
         return CMList.ListType.WHITE.equals(cmList.getType()) ? !numbers.contains(phone) : numbers.contains(phone);
     }
 
     private List<String> getNumbers(CMList cmList) {
         List<String> retVal = new LinkedList<String>();
         for (CMItem item : cmList.getCmItems()) {
             retVal.add(item.getPhoneNumber().replace(" ", ""));
         }
         return retVal;
     }
 
     @Override
     public void onCreate() {
         super.onCreate();
         try {
             cmList = getHelper().getCmListDao().getActiveList();
         } catch (SQLException e) {
             Log.e(TAG, "SQL", e);
         }
         if (null != cmList) {
             activateReceiver();
             numbers.clear();
             numbers.addAll(getNumbers(cmList));
         }
         audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
         telephonyService = new TelephonyService((TelephonyManager) this.getSystemService(TELEPHONY_SERVICE));
     }
 
     private void activateReceiver() {
         TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
         telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
         register = true;
     }
 
     private void deactivateReceiver() {
         TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
         telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_NONE);
         register = false;
     }
 
     public IBinder onBind(Intent intent) {
         return mBinder;
     }
 
     private void blockCall() {
         try {
             telephonyService.getTelephony().endCall();
         } catch (RemoteException e) {
             e.printStackTrace();
         }
     }
 
     private void silenceCall() {
         try {
             telephonyService.getTelephony().silenceRinger();
         } catch (RemoteException e) {
             e.printStackTrace();
         }
     }
 
     public class PhoneListener extends PhoneStateListener {
         @Override
         public void onCallStateChanged(int state, String incomingNumber) {
             audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
             switch (state) {
                 case TelephonyManager.CALL_STATE_IDLE:
                     audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                     Log.d(TAG, "IDLE");
                     break;
                 case TelephonyManager.CALL_STATE_OFFHOOK:
                     Log.d(TAG, "OFFHOOK");
                     break;
                 case TelephonyManager.CALL_STATE_RINGING:
                     if (isBlock(incomingNumber.replace(" ", ""))) {
                         CMList.ListType listType = cmList.getType();
                         if (CMList.ListType.BLACK.equals(listType) ||
                                 CMList.ListType.WHITE.equals(listType)) {
                             blockCall();
                             Log.d(TAG, "BLOCK CALL");
                         } else if (CMList.ListType.SILENCE.equals(listType)) {
                             Log.d(TAG, "SILENCE_LIST");
                             silenceCall();
                         }
                     }
                     audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                     break;
             }
         }
     }
 }
