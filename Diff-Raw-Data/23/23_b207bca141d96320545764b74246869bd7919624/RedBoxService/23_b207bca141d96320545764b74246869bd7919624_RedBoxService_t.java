 package org.clc.android.app.redbox;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.provider.CallLog;
 import android.telephony.PhoneStateListener;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.android.internal.telephony.ITelephony;
 
 public class RedBoxService extends Service {
     private static final String TAG = "RedBox_service";
 
     private ArrayList<String> mBlockedNumbers = null;
 
     private void readBlockedNumbersFromPrefs(SharedPreferences prefs) {
         mBlockedNumbers.clear();
         Map<String, ?> datas = prefs.getAll();
         for (Entry<String, ?> entry : datas.entrySet()) {
             if ((Boolean) entry.getValue()) {
                 mBlockedNumbers.add(entry.getKey());
             }
         }
     }
 
     private OnSharedPreferenceChangeListener mPreferencesChangeListener = new OnSharedPreferenceChangeListener() {
         @Override
         public void onSharedPreferenceChanged(
                 SharedPreferences sharedPreferences, String key) {
             readBlockedNumbersFromPrefs(sharedPreferences);
         }
     };
 
     private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
         @Override
         public void onCallStateChanged(int state, String incomingNumber) {
             if (state == TelephonyManager.CALL_STATE_RINGING) {
                 for (String blockedNumber : mBlockedNumbers) {
                     if (incomingNumber.matches(blockedNumber)) {
                         ban(incomingNumber);
                     }
                 }
             }
         }
     };
 
     @Override
     public void onCreate() {
         super.onCreate();
         mBlockedNumbers = new ArrayList<String>();
 
         SharedPreferences prefs = getSharedPreferences(
                 RedBoxActivity.DATA_PATH, MODE_PRIVATE);
         prefs
                 .registerOnSharedPreferenceChangeListener(mPreferencesChangeListener);
         readBlockedNumbersFromPrefs(prefs);
 
         startPhoneStateMonitoring();
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     private void startPhoneStateMonitoring() {
         TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
         telephonyManager.listen(mPhoneStateListener,
                 PhoneStateListener.LISTEN_CALL_STATE);
     }
 
     private void stopPhoneStateMonitoring() {
         TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
         telephonyManager.listen(mPhoneStateListener,
                 PhoneStateListener.LISTEN_NONE);
     }
 
     private void endCall() {
         TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
 
         try {
             Class c = Class.forName(telephonyManager.getClass().getName());
             Method m = c.getDeclaredMethod("getITelephony");
             m.setAccessible(true);
 
             ITelephony phoneInterface = (ITelephony) m.invoke(telephonyManager);
             phoneInterface.endCall();
         } catch (Throwable e) {
             Log.e(TAG, "Fail to end call!!!", e);
 
         }
     }
 
     private static final String[] CALL_PROJECTION = { CallLog.Calls._ID,
             CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.DURATION };
 
     private static final int DEL_CALL_LOG_MESSAGE = 1;
     private static final long CALL_LOG_CREATION_WAIT_TIME = 2000;
     private static final long RECENT_CALL_LOG_JUDGE_CRITERIA = 4000;
     private static final int CALL_LOG_CREATION_WAIT_TRY_LIMIT = 3;
 
     private void deleteCallLog(String number) {
         Message msg = new Message();
         msg.what = DEL_CALL_LOG_MESSAGE;
         msg.arg1 = 0;
         msg.obj = number;
         new Handler() {
             @Override
             public void handleMessage(Message msg) {
                 String number = (String) msg.obj;
                Uri callLogUri = CallLog.Calls.CONTENT_URI;
                String querySelection = CallLog.Calls.NUMBER + "=" + number;
                if (Build.MODEL.equals("SHW-M250S") || Build.MODEL.equals("SHW-M250K")) {
                    callLogUri = Uri.parse("content://logs/call");
                    querySelection = null;
                    
                }
                 Cursor cursor = getContentResolver().query(
                        callLogUri, CALL_PROJECTION,
                        querySelection, null,
                         CallLog.Calls.DEFAULT_SORT_ORDER);
                 if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                     int id = cursor.getInt(cursor
                             .getColumnIndex(CallLog.Calls._ID));
                     long date = cursor.getLong(cursor
                            .getColumnIndex(CallLog.Calls.DATE));                    
                     // If not recently created log, don't delete.
                     if (System.currentTimeMillis() - date > RECENT_CALL_LOG_JUDGE_CRITERIA) {
                         if (msg.arg1++ < CALL_LOG_CREATION_WAIT_TRY_LIMIT) {
                             sendMessageDelayed(msg, CALL_LOG_CREATION_WAIT_TIME);
                         }
                     } else {
                        getContentResolver().delete(callLogUri,
                                 CallLog.Calls._ID + "=" + Integer.toString(id),
                                 null);
                     }
                 }
                 cursor.close();
 
             }
         }.sendMessageDelayed(msg, CALL_LOG_CREATION_WAIT_TIME);
 
     }
 
     private void ban(String incomingNumber) {
         endCall();
         deleteCallLog(incomingNumber);
         Toast.makeText(getBaseContext(), "Block!!!", Toast.LENGTH_SHORT).show();
 
     }
 
 }
