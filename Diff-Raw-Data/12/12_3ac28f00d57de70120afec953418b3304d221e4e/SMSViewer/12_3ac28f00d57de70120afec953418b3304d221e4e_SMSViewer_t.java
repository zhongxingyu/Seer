 package com.nickivy.dashclocksmsviewer;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager;
 import android.net.Uri;
 
 import com.google.android.apps.dashclock.api.DashClockExtension;
 import com.google.android.apps.dashclock.api.ExtensionData;
 import com.nickivy.dashclocksmsviewer.R;
 
 import android.database.Cursor;
 import android.preference.PreferenceManager;
 import android.provider.ContactsContract;
 import android.text.TextUtils;
 import android.widget.Toast;
 
 /**
  * This code is based off of the base SMS extension that comes with DashClock.
  * Instead of displaying the number of unread conversations in the 'status'
  * section and the senders in the 'body' section, this shows the sender in the
  * 'status' section and the text of the most recent message in the 'body' section,
  * unless there is more than one unread conversation.
  */
 public class SMSViewer extends DashClockExtension {
 //    private static final String TAG = LogUtils.makeLogTag(SmsExtension.class);
 	
 	public static final String PREF_SWITCH = "pref_switch";
 	public static final String PREF_MULTI = "pref_multi";
 	public static final String PREF_HANGOUTS = "pref_hangouts";
 	
 	//Public variables for Panels 2, 3 to read from
 	
 	public static String panel2_title = ""; //title and contents refer to default where
 	public static String panel3_title = ""; //sender is on top and contents on bottom
 	
 	public static String panel2_contents = "";
 	public static String panel3_contents = "";
 	
 	public static String panel2_address = "";
 	public static String panel3_address = "";
 	
 	public static boolean panel2_visible = false;
 	public static boolean panel3_visible = false;
 	
 	public static int[] nummsg = new int[3];
 	
 	public static int unreadConversations = 0;
 	
 
     @Override
     protected void onInitialize(boolean isReconnect) {
         super.onInitialize(isReconnect);
         if (!isReconnect) {
             addWatchContentUris(new String[]{
                     TelephonyProviderConstants.MmsSms.CONTENT_URI.toString(),
             });
         }
         setUpdateWhenScreenOn(true);
     }
 
     @Override
     protected void onUpdateData(int reason) {
     	unreadConversations = 0;
         StringBuilder names = new StringBuilder();
         ArrayList<String> namelist = new ArrayList<String>();
         ArrayList<String> addrlist = new ArrayList<String>();
         ArrayList<Long> idlist = new ArrayList<Long>();
         SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
         boolean switcher = sp.getBoolean(PREF_SWITCH, false);
         boolean multi = sp.getBoolean(PREF_MULTI, false);
         boolean hangouts = sp.getBoolean(PREF_HANGOUTS, false);
         int status = 0;
         
         	Cursor cursor = tryOpenSimpleThreadsCursor();
         	if (cursor != null) {
                 while (cursor.moveToNext()) {
                     if (cursor.getInt(SimpleThreadsQuery.READ) == 0) {
                         long threadId = cursor.getLong(SimpleThreadsQuery._ID);
                         idlist.add(threadId);
 
                         // This is all redone (credit goes to Roman, from the base DashClock extension)
                         // because Samsung decided to be different. Oh well.
                         String recipientIdsStr = cursor.getString(SimpleThreadsQuery.RECIPIENT_IDS);
                         if (!TextUtils.isEmpty(recipientIdsStr)) {
                             String[] recipientIds = TextUtils.split(recipientIdsStr, " ");
                             for (String recipientId : recipientIds) {
                                 Cursor canonAddrCursor = tryOpenCanonicalAddressCursorById(
                                         Long.parseLong(recipientId));
                                 if (canonAddrCursor == null) {
                                     continue;
                                 }
                                 if (canonAddrCursor.moveToFirst()) {
                                     String address = canonAddrCursor.getString(
                                             CanonicalAddressQuery.ADDRESS);
                                     addrlist.add(address);
                                     
                                     String displayName = address;
                                     displayName = getDisplayNameForContact(address);
 
                                     if (names.length() > 0) {
                                         names.append(", ");
                                     }
                                     names.append(displayName);
                                     namelist.add(displayName);
                                 }
                                 canonAddrCursor.close();
                             }
                         }
                     }
                 }
         }
         
         unreadConversations = namelist.size();
 
         /*Intent for Panel 1
          * If multipanel enabled:
          *  - if 1-3 convos active lead to convo 1
          *  - if > 3 convos active lead to app
          * If disabled:
          *  - if 1 convo active lead to it
          *  - if more than that lead to messaging app
          */
         Intent clickIntent = null;
         if (unreadConversations > 0 && unreadConversations < 4 && ((!multi && unreadConversations == 1) || multi )) {
         	clickIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("smsto:" + addrlist.get(0)));
         } else {
         	if (hangouts){
         		PackageManager pm = getPackageManager();
             	try{
             		clickIntent = pm.getLaunchIntentForPackage("com.google.android.talk");
             		if (clickIntent == null)
             			throw new PackageManager.NameNotFoundException();
             		else
             			clickIntent.addCategory(Intent.CATEGORY_LAUNCHER);
             	}catch(PackageManager.NameNotFoundException e){
             		Toast.makeText(getApplicationContext(), getResources().getString(R.string.install_hangouts_plz), Toast.LENGTH_SHORT).show();
             	}
         	}else{
             clickIntent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN,
                     Intent.CATEGORY_APP_MESSAGING);
         	}
         }
         
         //Display number if more than one convo, text of latest message if just one
         String body = "";
         String title = "";
         if (!multi){
             if (unreadConversations > 1){
             	body = getResources().getQuantityString(
             		   R.plurals.sms_title_template, unreadConversations,
             		   unreadConversations);
             }
             if(unreadConversations == 1)
             	body = getMessageText(addrlist.get(0),0);
             title = names.toString();
             if (body == null)
             	body = getResources().getString(R.string.no_body_sub);
             status = unreadConversations;
         	panel2_visible = false;
         	panel3_visible = false;
         }else{
             if (unreadConversations > 3){
             	body = getResources().getQuantityString(
             		   R.plurals.sms_title_template, unreadConversations,
             		   unreadConversations);
             	title = names.toString();
             	status = unreadConversations;
             	panel2_visible = false;
             	panel3_visible = false;
             }
             if(unreadConversations > 0 && unreadConversations < 4){
             	title = namelist.get(0);
             	body = getMessageText(addrlist.get(0),0);
             	panel2_visible = false;
             	panel3_visible = false;
         		status = nummsg[0];
             	if(unreadConversations > 1 && multi){
             		panel2_title = namelist.get(1);
             		panel2_contents = getMessageText(addrlist.get(1),1);
             		panel2_address = addrlist.get(1);
             		panel2_visible = true;
             		if (unreadConversations > 2){
                 		panel3_title = namelist.get(2);
                 		panel3_contents = getMessageText(addrlist.get(2),2);
                 		panel3_address = addrlist.get(2);
             			panel3_visible = true;
             		}
             	}
             }
             if (body == null)
             	body = getResources().getString(R.string.no_body_sub);
         	
         }
 
         publishUpdate(new ExtensionData()
                 .visible(unreadConversations > 0)
                 .icon(R.drawable.ic_icon)
                 .status(Integer.toString(status))
                 .expandedTitle(switcher? body : title)
                 .expandedBody(switcher? title : body)
                 .clickIntent(clickIntent));
     	   
     }
         private String getDisplayNameForContact(String address) {
             String displayName = address;
                 Cursor contactCursor = tryOpenContactsCursorByAddress(address);
                 if (contactCursor != null) {
                     if (contactCursor.moveToFirst()) {
                         displayName = contactCursor.getString(ContactsQuery.DISPLAY_NAME);
                     }
                     contactCursor.close();
                 }
 //            }
 
             return displayName;
         }
     
     private Cursor tryOpenSimpleThreadsCursor() {
         try {
             return getContentResolver().query(
                     TelephonyProviderConstants.Threads.CONTENT_URI
                             .buildUpon()
                             .appendQueryParameter("simple", "true")
                             .build(),
                     SimpleThreadsQuery.PROJECTION,
                     null,
                     null,
                     null);
 
         } catch (Exception e) {
 //            LOGW(TAG, "Error accessing simple SMS threads cursor", e);
             return null;
         }
     }
     
     private Cursor tryOpenCanonicalAddressCursorById(long id) {
         try {
             return getContentResolver().query(
                     TelephonyProviderConstants.CanonicalAddresses.CONTENT_URI.buildUpon()
                             .build(),
                     CanonicalAddressQuery.PROJECTION,
                     TelephonyProviderConstants.CanonicalAddresses._ID + "=?",
                     new String[]{Long.toString(id)},
                     null);
 
         } catch (Exception e) {
 //            LOGE(TAG, "Error accessing canonical addresses cursor", e);
             return null;
         }
     }
 
     private Cursor tryOpenContactsCursorByAddress(String phoneNumber) {
         try {
             return getContentResolver().query(
                     ContactsContract.PhoneLookup.CONTENT_FILTER_URI.buildUpon()
                             .appendPath(Uri.encode(phoneNumber)).build(),
                     ContactsQuery.PROJECTION,
                     null,
                     null,
                     null);
         } catch (IllegalArgumentException e) {
             // Can be called by the content provider (from Google Play crash/ANR console)
             // java.lang.IllegalArgumentException: URI: content://com.android.contacts/phone_lookup/
 //            LogUtils.LOGW(TAG, "Error looking up contact name", e);
 //            Log.w(TAG, "Error looking up contact name", e);
             return null;
         }
     }
 
     private interface SimpleThreadsQuery {
         String[] PROJECTION = {
                 TelephonyProviderConstants.Threads._ID,
                 TelephonyProviderConstants.Threads.READ,
                 TelephonyProviderConstants.Threads.RECIPIENT_IDS,
         };
 
         int _ID = 0;
         int READ = 1;
         int RECIPIENT_IDS = 2;
     }
 
     private interface CanonicalAddressQuery {
         String[] PROJECTION = {
                 TelephonyProviderConstants.CanonicalAddresses._ID,
                 TelephonyProviderConstants.CanonicalAddresses.ADDRESS,
         };
 
         int _ID = 0;
         int ADDRESS = 1;
     }
 
     private interface ContactsQuery {
         String[] PROJECTION = {
                 ContactsContract.Contacts.DISPLAY_NAME,
         };
 
         int DISPLAY_NAME = 0;
     }
    
    private String procAddr(String in){
    	in = in.replaceAll("\\s","");
    	in = in.replaceAll("-","");
    	return in;
    }
 
     //For when multiple contacts exist and you want to get messages from only one
     public String getMessageText(String addr, int num){
    	addr = procAddr(addr);
     	Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), new String[] { "address","body", } , "read = 0", null, null);
     	if (cursor == null){
     		return null;
     	}
         if (!cursor.moveToFirst())
         {
             cursor.close();
             return null;
         }
         cursor.moveToLast();
         String body = "";
         int numproc = 0;
         nummsg[num] = 0;
         while(numproc != cursor.getCount()){
         	if(cursor.getString(cursor.getColumnIndexOrThrow("address")).equals(addr)){
         		if (body.equals(""))
         			body = body + cursor.getString(cursor.getColumnIndexOrThrow("body"));
         		else
         			body = body + "\n" + cursor.getString(cursor.getColumnIndexOrThrow("body"));
             	nummsg[num]++;
         	}
     		numproc++;
     		cursor.moveToPrevious();
         }
         cursor.close();
     	
        
     	return body;
     }
 }
