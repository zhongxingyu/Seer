 package org.startsmall.openalarm;
 
 import android.content.Intent;
 import android.content.Context;
 import android.content.ContentValues;
 import android.content.ContentResolver;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.EditTextPreference;
 import android.preference.PreferenceCategory;
 import android.preference.Preference;
import android.telephony.gsm.SmsManager;
 import android.text.method.DialerKeyListener;
 import android.text.TextUtils;
 import android.util.Log;
 import java.util.*;
 
 public class TextHandler extends AbsHandler {
     private static final String TAG = "TextHandler";
     private static final String EXTRA_KEY_PHONE_NUMBER = "phone_number";
     private static final String EXTRA_KEY_SUBJECT = "subject";
     private static final String EXTRA_KEY_BODY = "body";
 
     @Override
     public void onReceive(Context context, Intent intent) {
         Log.v(TAG, "===> TextHandler.onReceive()");
 
         final String extra = intent.getStringExtra(AlarmColumns.EXTRA);
         putBundleIntoIntent(intent, getBundleFromExtra(extra));
 
         String phoneNumber =
             intent.getStringExtra(EXTRA_KEY_PHONE_NUMBER);
         String body =
             intent.getStringExtra(EXTRA_KEY_BODY);
 
         if (!TextUtils.isEmpty(phoneNumber) &&
             !TextUtils.isEmpty(body)) {
 
             SmsManager sm = SmsManager.getDefault();
             sm.sendTextMessage(
                 phoneNumber,
                 null,
                 body,
                 null, null);
 
             String subject = intent.getStringExtra(EXTRA_KEY_SUBJECT);
             addMessageToUri(context.getContentResolver(),
                             Uri.parse("content://sms/sent"),
                             phoneNumber,
                             body, subject,
                             System.currentTimeMillis(),
                             true,
                             false,
                             getOrCreateThreadId(context, phoneNumber));
         }
 
         // Reshedule this alarm.
         final int alarmId = intent.getIntExtra(AlarmColumns._ID, -1);
         Alarms.dismissAlarm(context, alarmId);
     }
 
     @Override
     public void addMyPreferences(final Context context,
                                  final PreferenceCategory category,
                                  final String extra) {
         Preference.OnPreferenceChangeListener prefChangeListener =
             new Preference.OnPreferenceChangeListener() {
                 @Override
                 public boolean onPreferenceChange(Preference p,
                                                   Object newValue) {
                     p.setSummary((String)newValue);
                     return true;
                 }
             };
 
         // Phone number to which text is sent
         EditTextPreference phonePref = new EditTextPreference(context);
         phonePref.setKey(EXTRA_KEY_PHONE_NUMBER);
         phonePref.setPersistent(true);
         phonePref.setTitle(R.string.phone_number_title);
         phonePref.getEditText().setKeyListener(DialerKeyListener.getInstance());
         phonePref.setDialogTitle(R.string.phone_number_dialog_title);
         phonePref.setOnPreferenceChangeListener(prefChangeListener);
         category.addPreference(phonePref);
 
         // SMS subject
         EditTextPreference subjectPref = new EditTextPreference(context);
         subjectPref.setKey(EXTRA_KEY_SUBJECT);
         subjectPref.setPersistent(true);
         subjectPref.setTitle(R.string.text_handler_subject_title);
         subjectPref.setOnPreferenceChangeListener(prefChangeListener);
         subjectPref.setDialogTitle(R.string.text_handler_subject_dialog_title);
         category.addPreference(subjectPref);
 
         // SMS body
         EditTextPreference bodyPref = new EditTextPreference(context);
         bodyPref.setKey(EXTRA_KEY_BODY);
         bodyPref.setPersistent(true);
         bodyPref.setTitle(R.string.text_handler_body_title);
         bodyPref.setOnPreferenceChangeListener(prefChangeListener);
         bodyPref.setDialogTitle(R.string.text_handler_body_dialog_title);
         category.addPreference(bodyPref);
 
         // Get settings from extra.
         if (TextUtils.isEmpty(extra)) {
             phonePref.setText("");
             phonePref.setSummary("");
             subjectPref.setText("");
             subjectPref.setSummary("");
             bodyPref.setText("");
             bodyPref.setSummary("");
         } else {
             Bundle result = getBundleFromExtra(extra);
 
             String phoneNumber = result.getString(EXTRA_KEY_PHONE_NUMBER);
             if(!TextUtils.isEmpty(phoneNumber)) {
                 phonePref.setText(phoneNumber);
                 phonePref.setSummary(phoneNumber);
             }
 
             String subjectString = result.getString(EXTRA_KEY_SUBJECT);
             if(!TextUtils.isEmpty(subjectString)) {
                 subjectPref.setText(subjectString);
                 subjectPref.setSummary(subjectString);
             }
 
             String bodyString = result.getString(EXTRA_KEY_BODY);
             if(!TextUtils.isEmpty(bodyString)) {
                 bodyPref.setText(bodyString);
                 bodyPref.setSummary(bodyString);
             }
         }
     }
 
     @Override
     protected void putBundleIntoIntent(Intent intent, Bundle bundle) {
         final String phoneNumber = bundle.getString(EXTRA_KEY_PHONE_NUMBER);
         if (!TextUtils.isEmpty(phoneNumber)) {
             intent.putExtra(EXTRA_KEY_PHONE_NUMBER, phoneNumber);
         }
 
         final String subject = bundle.getString(EXTRA_KEY_SUBJECT);
         if (!TextUtils.isEmpty(subject)) {
             intent.putExtra(EXTRA_KEY_SUBJECT, subject);
         }
 
         final String body = bundle.getString(EXTRA_KEY_BODY);
         if (!TextUtils.isEmpty(body)) {
             intent.putExtra(EXTRA_KEY_BODY, body);
         }
     }
 
     @Override
     protected Bundle getBundleFromExtra(String extra) {
         Bundle result = new Bundle();
         if (!TextUtils.isEmpty(extra)) {
             String[] values = TextUtils.split(extra, SEPARATOR);
             for (String value : values) {
                 if (TextUtils.isEmpty(value) ||
                     !value.matches("(\\w+)=.*")) {
                     continue;
                 }
 
                 String[] elems = value.split("=");
                 if (elems[0].equals(EXTRA_KEY_PHONE_NUMBER)) {
                     if(elems.length == 2 && !TextUtils.isEmpty(elems[1])) {
                         result.putString(EXTRA_KEY_PHONE_NUMBER, elems[1]);
                     }
                 } else if (elems[0].equals(EXTRA_KEY_SUBJECT)) {
                     if(elems.length == 2 && !TextUtils.isEmpty(elems[1])) {
                         result.putString(EXTRA_KEY_SUBJECT, elems[1]);
                     }
                 } else if (elems[0].equals(EXTRA_KEY_BODY)) {
                     if(elems.length == 2 && !TextUtils.isEmpty(elems[1])) {
                         result.putString(EXTRA_KEY_BODY, elems[1]);
                     }
                 }
             }
         }
         return result;
     }
 
     private static final String THREAD_ID = "thread_id";
     private static final String ADDRESS = "address";
     private static final String DATE = "date";
     private static final String READ = "read";
     private final String SUBJECT = "subject";
     private final String BODY = "body";
     private final String STATUS = "status";
     private final int STATUS_PENDING = 64;
 
     private Uri addMessageToUri(ContentResolver resolver,
                                 Uri uri,
                                 String address, String body, String subject,
                                 Long date, boolean read, boolean deliveryReport, long threadId) {
         ContentValues values = new ContentValues(7);
 
         values.put(ADDRESS, address);
         if (date != null) {
             values.put(DATE, date);
         }
         values.put(READ, read ? Integer.valueOf(1) : Integer.valueOf(0));
         values.put(SUBJECT, subject);
         values.put(BODY, body);
         if (deliveryReport) {
             values.put(STATUS, STATUS_PENDING);
         }
         if (threadId != 1L) {
             values.put(THREAD_ID, threadId);
         }
         return resolver.insert(uri, values);
     }
 
     private static final Uri THREAD_ID_CONTENT_URI = Uri.parse(
         "content://mms-sms/threadID");
 
     private static long getOrCreateThreadId(Context context, String recipient) {
         // Cursor cursor =
         //     context.getContentResolver().query(
         //         THREAD_ID_CONTENT_URI,
         //         new String[]{AlarmColumns._ID},
         //         "recipient=" + recipient, null, null);
         // try {
         //     if (cursor != null && cursor.moveToFirst()) {
         //         return cursor.getLong(0);
         //     } else {
         //         Log.e(TAG, "getOrCreateThreadId returned no rows!");
         //     }
         // } finally {
         //     cursor.close();
         // }
 
         // throw new IllegalArgumentException("Unable to find or allocate a thread ID.");
 
 
         Uri.Builder uriBuilder = THREAD_ID_CONTENT_URI.buildUpon();
         // for (String recipient : recipients) {
             // if (Mms.isEmailAddress(recipient)) {
             //         recipient = Mms.extractAddrSpec(recipient);
             //     }
 
         uriBuilder.appendQueryParameter("recipient", recipient);
         // }
 
         Uri uri = uriBuilder.build();
         Cursor cursor =
             context.getContentResolver().query(
                 uri,
                 new String[]{"_id"},
                 null, null, null);
         if (cursor != null) {
             try {
                 if (cursor.moveToFirst()) {
                     return cursor.getLong(0);
                 } else {
                     Log.e(TAG, "getOrCreateThreadId returned no rows!");
                 }
             } finally {
                 cursor.close();
             }
         }
 
         Log.e(TAG, "getOrCreateThreadId failed with uri " + uri.toString());
         throw new IllegalArgumentException("Unable to find or allocate a thread ID.");
     }
 }
