 package com.webshrub.moonwalker.androidapp;
 
 import android.content.ContentResolver;
 import android.database.Cursor;
 import android.net.Uri;
 import android.provider.ContactsContract;
 import android.util.Log;
 import org.apache.cordova.api.Plugin;
 import org.apache.cordova.api.PluginResult;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 public class SMSReaderPlugin extends Plugin {
     private static final String DATE_FORMAT = "dd/MM/yy;kk:mm";
     private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT);
     private Map<String, String> contactMap = new HashMap<String, String>();
 
     @Override
     public PluginResult execute(String action, JSONArray data, String callbackId) {
         Log.d("SMSReadPlugin", "Plugin Called");
         PluginResult result = null;
         JSONObject messages = new JSONObject();
         if (action.equals("inbox")) {
             try {
                 messages = readSMS("inbox");
                 result = new PluginResult(PluginResult.Status.OK, messages);
             } catch (JSONException jsonEx) {
                 Log.d("SMSReadPlugin", "Got JSON Exception " + jsonEx.getMessage());
                 result = new PluginResult(PluginResult.Status.JSON_EXCEPTION);
             }
         } else if (action.equals("sent")) {
             try {
                 messages = readSMS("sent");
                 Log.d("SMSReadPlugin", "Returning " + messages.toString());
                 result = new PluginResult(PluginResult.Status.OK, messages);
             } catch (JSONException jsonEx) {
                 Log.d("SMSReadPlugin", "Got JSON Exception " + jsonEx.getMessage());
                 result = new PluginResult(PluginResult.Status.JSON_EXCEPTION);
             }
         } else if (action.equals("delete")) {
             try {
                 deleteSmsById(data.getString(0));
                 result = new PluginResult(PluginResult.Status.OK, messages);
             } catch (JSONException jsonEx) {
                 Log.d("SMSReadPlugin", "Got JSON Exception " + jsonEx.getMessage());
                 result = new PluginResult(PluginResult.Status.JSON_EXCEPTION);
             }
         } else {
             result = new PluginResult(PluginResult.Status.INVALID_ACTION);
             Log.d("SMSReadPlugin", "Invalid action : " + action + " passed");
         }
         return result;
     }
 
     //Read messages from inbox/or sent box.
     private JSONObject readSMS(String folder) throws JSONException {
         JSONObject data = new JSONObject();
         Uri uriSMSURI = Uri.parse("");
         if (folder.equals("inbox")) {
             uriSMSURI = Uri.parse("content://sms/inbox");
         } else if (folder.equals("sent")) {
             uriSMSURI = Uri.parse("content://sms/sent");
         }
 
         String[] projection = new String[]{"_id", "address", "date", "body"};
         // time 3 days back
         Long time = System.currentTimeMillis() - 259200000;
 
         String selection = "date >?";
         String[] selectionArgs = new String[]{time.toString()};
         String sortOrder = null;
         Cursor cur = getContentResolver().query(uriSMSURI, projection, selection, selectionArgs, sortOrder);
         JSONArray smsList = new JSONArray();
         data.put("messages", smsList);
         while (cur.moveToNext()) {
             String name = getContact(cur.getString(cur.getColumnIndex("address")));
            if (name.equals("")) {
                 JSONObject sms = new JSONObject();
                 sms.put("_id", cur.getString(cur.getColumnIndex("_id")));
                 sms.put("number", cur.getString(cur.getColumnIndex("address")));
                 sms.put("text", cur.getString(cur.getColumnIndex("body")));
                 sms.put("name", (name == null || name.equalsIgnoreCase("")) ? "Unknown" : name);
                 sms.put("date", SIMPLE_DATE_FORMAT.format(new Date(cur.getLong(cur.getColumnIndex("date")))));
                 smsList.put(sms);
             }
         }
         return data;
     }
 
     private String getContact(String number) {
         String returnName = "";
         try {
             String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup.NUMBER};
             String selection = ContactsContract.PhoneLookup.NUMBER + "=?";
             String[] selectionArgs = new String[]{number};
             String sortOrder = null;
             if (contactMap.get(number) != null) {
                 returnName = contactMap.get(number);
                 return returnName;
             } else {
                 Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
                 Cursor cs = this.ctx.getContext().getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
                 if (cs.getCount() > 0) {
                     cs.moveToFirst();
                     returnName = cs.getString(cs.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                     contactMap.put(number, returnName);
                 }
             }
             if (returnName.equals("")) {
                 contactMap.put(number, returnName);
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return returnName;
     }
 
     private ContentResolver getContentResolver() {
         return this.ctx.getContext().getContentResolver();
     }
 
     public void deleteSmsById(String id) {
         try {
             String queryString = "_id" + " = " + id;
             int n = ctx.getContext().getContentResolver().delete(Uri.parse("content://sms"), queryString, null);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
