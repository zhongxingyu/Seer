 package com.androidproductions.libs.sms;
 
 import android.app.PendingIntent;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.telephony.PhoneNumberUtils;
 import android.telephony.SmsManager;
 import android.util.Log;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 
 public class Transaction {
     private final Context mContext;
 

     public Transaction(Context context)
     {
         mContext = context;
     }
 
     public void sentMessage(Uri uri)
     {
         ContentValues cv = new ContentValues();
         cv.put("type",MessageType.SENT);
         mContext.getContentResolver().update(uri, cv, null, null);
     }
 
     public void recievedMessage(Uri uri)
     {
         ContentValues cv = new ContentValues();
         cv.put("seen",1);
         mContext.getContentResolver().update(uri,cv,null,null);
     }
 
     public void queueMessage(Uri uri)
     {
         ContentValues cv = new ContentValues();
         cv.put("type",MessageType.QUEUED);
         mContext.getContentResolver().update(uri,cv,null,null);
     }
 
     public void failedMessage(Uri uri)
     {
         ContentValues cv = new ContentValues();
         cv.put("type",MessageType.FAILED);
         mContext.getContentResolver().update(uri,cv,null,null);
     }
 
     public void sendMessage(SmsMessage message,Long threadId)
     {
         sendSmsMessage(message.getBody(), message.getAddresses(),message.getId(), threadId);
     }
 
     private void sendSmsMessage(String text, String[] addresses, Long messageId, Long threadId)
     {
         if (threadId == null) {
             threadId = getOrCreateThreadId(addresses);
         }
 
         for (int i = 0; i < addresses.length; i++) {
             sendSmsMessage(text,addresses[i],messageId,threadId);
         }
     }
 
     private void sendSmsMessage(String text, String address, Long messageId,Long threadId)
     {
         // save the message for each of the addresses
         Calendar cal = Calendar.getInstance();
         ContentValues values = new ContentValues();
         values.put("address", address);
         values.put("body", text);
         values.put("read", 1);
 
         // attempt to create correct thread id if one is not supplied
         if (threadId == null) {
             threadId = getOrCreateThreadId(address);
         }
 
         values.put("thread_id", threadId);
         Uri inserted;
         if (messageId != null)
         {
             mContext.getContentResolver().update(SmsUri.BASE_URI, values,"_id = ?", new String[] { String.valueOf(messageId)});
             inserted = ContentUris.withAppendedId(SmsUri.BASE_URI,messageId);
         }
         else
         {
             values.put("date", cal.getTimeInMillis());
             inserted = mContext.getContentResolver().insert(SmsUri.OUTBOX_URI, values);
         }
         Intent sendIntent = new Intent(Action.SENT);
         sendIntent.putExtra("SMSURI",inserted);
         Intent deliveredIntent = new Intent(Action.DELIVERED);
         sendIntent.putExtra("SMSURI",inserted);
         PendingIntent sentPI = PendingIntent.getBroadcast(mContext, 0, sendIntent,PendingIntent.FLAG_CANCEL_CURRENT);
         PendingIntent deliveredPI = PendingIntent.getBroadcast(mContext, 0, deliveredIntent, PendingIntent.FLAG_CANCEL_CURRENT);
 
         ArrayList<PendingIntent> sPI = new ArrayList<PendingIntent>();
         ArrayList<PendingIntent> dPI = new ArrayList<PendingIntent>();
 
         String body = text;
 
         SmsManager smsManager = SmsManager.getDefault();
 
         ArrayList<String> parts = smsManager.divideMessage(body);
 
         for (int i = 0; i < parts.size(); i++) {
             sPI.add(sentPI);
             dPI.add(deliveredPI);
         }
 
         try {
             smsManager.sendMultipartTextMessage(PhoneNumberUtils.stripSeparators(address), null, parts, sPI, dPI);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     public long getOrCreateThreadId(String recipient) {
         return getOrCreateThreadId(new String[] { recipient });
     }
 
     /**
      * Given the recipients list and subject of an unsaved message,
      * return its thread ID.  If the message starts a new thread,
      * allocate a new thread ID.  Otherwise, use the appropriate
      * existing thread ID.
      *
      * Find the thread ID of the same set of recipients (in
      * any order, without any additions). If one
      * is found, return it.  Otherwise, return a unique thread ID.
      */
     public long getOrCreateThreadId(String[] recipients) {
         Uri.Builder uriBuilder = SmsUri.THREAD_URI.buildUpon();
 
         for (String recipient : recipients) {
             uriBuilder.appendQueryParameter("recipient", recipient);
         }
 
         Uri uri = uriBuilder.build();
 
         Cursor cursor = mContext.getContentResolver().query(uri, new String[]{"_id"}, null, null, null);
         if (cursor != null) {
             try {
                 if (cursor.moveToFirst()) {
                     return cursor.getLong(0);
                 } else {
                     Log.e("SMS", "getOrCreateThreadId returned no rows!");
                 }
             } finally {
                 cursor.close();
             }
         }
 
         Log.e("SMS", "getOrCreateThreadId failed with uri " + uri.toString());
         throw new IllegalArgumentException("Unable to find or allocate a thread ID.");
     }
 }
