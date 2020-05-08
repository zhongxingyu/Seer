 package com.webshrub.moonwalker.androidapp;
 
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.telephony.SmsMessage;
 
 import java.text.ParseException;
 import java.util.HashMap;
 import java.util.Map;
 
 import static com.webshrub.moonwalker.androidapp.DNDManagerConstants.SIMPLE_DATE_FORMAT;
 import static com.webshrub.moonwalker.androidapp.DNDManagerConstants.SIMPLE_DATE_TIME_FORMAT;
 
 public class DNDManagerUtil {
 
     public static String stripText(String input) {
         if (input.length() < 115) {
             return input;
         }
         return input.substring(0, 115);
     }
 
     public static String getMessageText(Context context, String number, String dateTime, String shortDescription) {
         try {
             String messageText = DNDManagerHtmlHelper.getMessageFormat(context).replaceAll("~~number~~", number);
             messageText = messageText.replaceAll("~~date~~", SIMPLE_DATE_FORMAT.format(SIMPLE_DATE_TIME_FORMAT.parse(dateTime)));
             messageText = messageText.replaceAll("~~datetime~~", dateTime);
             messageText = messageText.replaceAll("~~text~~", shortDescription);
             return messageText;
         } catch (ParseException e) {
             e.printStackTrace();
             return "";
         }
     }
 
     public static String getIncomingNumberFromSms(Intent intent) {
         Bundle bundle = intent.getExtras();
         if (bundle != null) {
             Object[] pdus = (Object[]) bundle.get("pdus");
             SmsMessage[] messages = new SmsMessage[pdus.length];
             for (int i = 0; i < pdus.length; i++) {
                 messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
             }
             return messages[0].getOriginatingAddress();
         }
         return "";
     }
 
     public static String getContactName(Context context, String number) {
         String returnName = "";
         try {
             String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup.NUMBER};
             String selection = ContactsContract.PhoneLookup.NUMBER + "=?";
             String[] selectionArgs = new String[]{number};
             String sortOrder = null;
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
            Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    returnName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return returnName;
     }
 }
