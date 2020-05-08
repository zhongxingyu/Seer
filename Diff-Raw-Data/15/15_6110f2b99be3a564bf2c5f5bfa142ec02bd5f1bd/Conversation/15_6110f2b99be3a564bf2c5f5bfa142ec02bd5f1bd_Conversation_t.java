 package com.low_light_apps.low.light.texting;
 
 
 
 import java.text.DateFormat;
 
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.provider.ContactsContract;
 import android.app.Activity;
 import android.app.ListActivity;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.Cursor;
 import android.telephony.SmsManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ListAdapter;
 import android.widget.Toast;
 import android.support.v4.app.NavUtils;
 import android.support.v4.widget.SimpleCursorAdapter;
 
 
 public class Conversation extends ListActivity {
 	private String reply_address;
 	private EditText editMessage;
 	private ArrayList<String> addresses = new ArrayList<String>();
 	private ArrayList<String> messages =  new ArrayList<String>();
 	private ArrayList<String> type =  new ArrayList<String>();
 	private ArrayList<String> contacts =  new ArrayList<String>();
 	private Cursor sms_cur;
 	private Cursor mms_cur;
 	//private String person = null;
 	public final static String NEXT_MESSAGE = "com.low_light_apps.low.light.texting.NEXT_MESSAGE";
 	String main_message = null;
 	String contact_name = null;
 	ConversationArrayAdapter myAdapter;
 	MessageArrayAdapter myMessageAdapter;
 	private BroadcastReceiver mIntentReceiver;
 	private int curr_count = 0;
 	private int mms_cur_count = 0;
 	private Handler mHandler = new Handler();
 	private ArrayList<Message> all_messages = new ArrayList<Message>();
 	
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_conversation);
         Intent intent = getIntent();
         
         if (intent.getStringExtra(Conversation.NEXT_MESSAGE) != null){
         	main_message = intent.getStringExtra(NEXT_MESSAGE);
         	
         }
         
         else if (intent.getStringExtra(MainActivity.EXTRA_MESSAGE) != null){
         	main_message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
         }
         else {
         	main_message = intent.getStringExtra(ContactsActivity.THREAD_ID);
 //        	if(main_message == "0") {
 //        		//person = intent.getStringExtra(ContactsActivity.PERSON);
 //        	}
         }
         Log.v("main_message", main_message);
         
             Uri selectUri = Uri.parse("content://sms/");  //mms-sms doesn't work
             Uri mmsUri = Uri.parse("content://mms/");
 //      	String[] projection = new String[] {"_id"};
 //      	String[] thread_projection = new String [] {"thread_id"};
             String[] selectionArgs = new String [] {main_message};
 //      	new String[] { "_id", "thread_id", "address", "person", "date",
 //              "body", "type" }
             sms_cur = getContentResolver().query(selectUri, null, "thread_id = ?", selectionArgs, "Date");//works!!
             mms_cur = getContentResolver().query(mmsUri, null, "thread_id = ?", selectionArgs, "Date");//works!!
 
      
             curr_count = sms_cur.getCount();
             mms_cur_count = mms_cur.getCount();
            // getCursorColumns(mms_cur);
        // Log.v("sms_cur on create", String.valueOf(curr_count));
         if (sms_cur != null) {
           if (sms_cur.moveToFirst()) {
               do {
               //addresses.add(sms_cur.getString(sms_cur.getColumnIndex("address")));
               //addresses.add("placeholder");
               String dateVal = sms_cur.getString(sms_cur.getColumnIndex("date"));
                   Date date = new Date(Long.valueOf(dateVal));
                   //String myString = DateFormat.getDateInstance().format(date);
                   String sms_date = DateFormat.getDateTimeInstance().format(date);
                   //addresses.add(myString);
                   String sms_message = (sms_cur.getString(sms_cur.getColumnIndex("body")));
                   String sent_received = sms_cur.getString(sms_cur.getColumnIndex("type"));
                   //type.add(sent_received);
                   String contact;
               	if(sent_received.equals("1") ){
               		//contact_names.add("Sent by Somebody");
               		String number = (sms_cur.getString(sms_cur.getColumnIndex("address")));
               		String name = getContactName(this, number);
               		
                 	if(name.equals("Contact Not Found") ){
                 		//contacts.add(number);
                 		contact = number;
                 	}
                 	else {
                 		//contacts.add(name);
                 		contact = name;
                 	}
               	}
               	else {
               		//contacts.add("Me");
               		contact = "Me";
               	}
               	Message message = new Message(sms_date, contact, sms_message, sent_received, date);
               	all_messages.add(message);
               	
               	
               } while (sms_cur.moveToNext());
           }
           // end of sms_cur
 
           //this works but shows that a count of 0 in some cases.
           if (mms_cur != null) {
         	 // Toast.makeText(this, "This Conversation has MMS messages! " + String.valueOf(mms_cur_count), Toast.LENGTH_SHORT).show();
         	  if (mms_cur.moveToFirst()) {
         		  do {
         			   long timestamp = mms_cur.getLong(2) * 1000;
         			   Date date = new Date(timestamp);
                      // String dateVal = mms_cur.getString(mms_cur.getColumnIndex("date"));
                       //Date date = new Date(Long.valueOf(dateVal));
                       //String myString = DateFormat.getDateInstance().format(date);
                       String mms_date = DateFormat.getDateTimeInstance().format(date);
                       //addresses.add(myString); //nb:  Addresses equals "Date"
                       String mms_message = "MMS Message";
                      // messages.add("MMS Message");
 //                      contacts.add("TBD");
 //                      type.add("TBD");
                       String sent_received = mms_cur.getString(mms_cur.getColumnIndex("m_type"));
                       type.add(sent_received);
                       String contact;
                       	if(sent_received.equals("1") ){
                       		//contact_names.add("Sent by Somebody");
                       		String number = (mms_cur.getString(mms_cur.getColumnIndex("address")));
                       		String name = getContactName(this, number);
                         	if(name.equals("Contact Not Found") ){
                         	//	contacts.add(number);
                         		contact = number;
                         	}
                         	else {
                         		//contacts.add(name);
                         		contact = name;
                         	}
                       	}
                       	else {
                       		//contacts.add("Me");
                       		contact = "Me";
                       	}
                       	Message message = new Message(mms_date, contact, mms_message, sent_received, date);
                       	all_messages.add(message);
         		  
         		  } while (mms_cur.moveToNext());
         	  }
         	  
           }
           else {
         	//  Toast.makeText(this, "No MMS Messages in this conversation", Toast.LENGTH_SHORT).show();
 
           }
     	  Toast.makeText(this, "Number of all messages " + String.valueOf(all_messages.size()), Toast.LENGTH_SHORT).show();
     	  Collections.sort(all_messages, new Comparator<Message>() {
 
 				public int compare(Message lhs, Message rhs) {
 					// TODO Auto-generated method stub
 					//return lhs.message.compareToIgnoreCase(rhs.message);
 					return lhs.message_date.compareTo(rhs.message_date);
 				}
             
 
 
           });
          // myAdapter = new ConversationArrayAdapter(this, addresses, messages, type, contacts);
     	  myMessageAdapter = new MessageArrayAdapter(this, all_messages);
           setListAdapter(myMessageAdapter);
           
         if (sms_cur.getCount() == 0) {
         	
         	reply_address = intent.getStringExtra(ContactsActivity.REPLY_ADDRESS);
         	//Log.v("ReplyAddress", reply_address);
         }
         else {
         	sms_cur.moveToLast(); //lazily there can be many user - just get last one
         	reply_address = sms_cur.getString(sms_cur.getColumnIndex("address"));
         }
         
        }
         sms_cur.close();
     }
     @Override
     protected void onResume() {
     super.onResume();
 
     IntentFilter intentFilter = new IntentFilter("SmsMessage.intent.MAIN");
     mIntentReceiver = new BroadcastReceiver() {
     @Override
     public void onReceive(Context context, Intent intent) {
     String msg = intent.getStringExtra("get_msg");
     Log.v("get_msg", msg);
     //Process the sms format and extract body &amp; phoneNumber
 //    msg = msg.replace("\n", "");
 //    String body = msg.substring(msg.lastIndexOf(":")+1, msg.length());
 //    String pNumber = msg.substring(0,msg.lastIndexOf(":"));
 
     //Add it to the list or do whatever you wish to
    		mHandler.postDelayed(new Runnable() {
    			public void run() {
     		updateMessages();
    			//        doStuff();
    			}
    		}, 3000);
    	
 	}
     };
     this.registerReceiver(mIntentReceiver, intentFilter);
     }
     @Override
     protected void onPause() {
 
     super.onPause();
     this.unregisterReceiver(this.mIntentReceiver);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_conversation, menu);
         return true;
     }
     //for testing
     private void getCursorColumns(Cursor cursor){
         if (cursor != null) {
             int num = cursor.getColumnCount();
             for (int i = 0; i < num; ++i) {
                 String colname = cursor.getColumnName(i);
                 Log.v("Column_Name:  ", colname);
 
             }
         }
     }
 
     private Cursor getConversations(String person_id){
     	String str_id = String.valueOf(person_id);
         String[] selectionArgs = new String [] {person_id};
     	Cursor cursor = getContentResolver().query(Uri.parse("content://sms/"), null, "person = ?", selectionArgs, "Date");
         Log.v("LLT", "created cursor");
         startManagingCursor(cursor);
 //        cursor.moveToFirst();
         Log.v("getConversations", "StartedManagingCursor");
         Log.v("cursor count is ", String.valueOf(cursor.getCount()));
     	
 		return cursor;
     	
     }
     
     public void updateMessages(){
     	Log.v("update Messages", "Started");
     	myMessageAdapter.clear();
     	all_messages.clear();
     	Log.v("main_message", main_message);
         
         Uri selectUri = Uri.parse("content://sms/");  //mms-sms doesn't work
         Uri mmsUri = Uri.parse("content://mms/");
 //  	String[] projection = new String[] {"_id"};
 //  	String[] thread_projection = new String [] {"thread_id"};
         String[] selectionArgs = new String [] {main_message};
 //  	new String[] { "_id", "thread_id", "address", "person", "date",
 //          "body", "type" }
         sms_cur = getContentResolver().query(selectUri, null, "thread_id = ?", selectionArgs, "Date");//works!!
         mms_cur = getContentResolver().query(mmsUri, null, "thread_id = ?", selectionArgs, "Date");//works!!
 
  
         curr_count = sms_cur.getCount();
         mms_cur_count = mms_cur.getCount();
        // getCursorColumns(mms_cur);
    // Log.v("sms_cur on create", String.valueOf(curr_count));
     if (sms_cur != null) {
       if (sms_cur.moveToFirst()) {
           do {
           //addresses.add(sms_cur.getString(sms_cur.getColumnIndex("address")));
           //addresses.add("placeholder");
           String dateVal = sms_cur.getString(sms_cur.getColumnIndex("date"));
               Date date = new Date(Long.valueOf(dateVal));
               //String myString = DateFormat.getDateInstance().format(date);
               String sms_date = DateFormat.getDateTimeInstance().format(date);
               //addresses.add(myString);
               String sms_message = (sms_cur.getString(sms_cur.getColumnIndex("body")));
               String sent_received = sms_cur.getString(sms_cur.getColumnIndex("type"));
               //type.add(sent_received);
               String contact;
           	if(sent_received.equals("1") ){
           		//contact_names.add("Sent by Somebody");
           		String number = (sms_cur.getString(sms_cur.getColumnIndex("address")));
           		String name = getContactName(this, number);
           		
             	if(name.equals("Contact Not Found") ){
             		//contacts.add(number);
             		contact = number;
             	}
             	else {
             		//contacts.add(name);
             		contact = name;
             	}
           	}
           	else {
           		//contacts.add("Me");
           		contact = "Me";
           	}
           	Message message = new Message(sms_date, contact, sms_message, sent_received, date);
           	all_messages.add(message);
           	
           	
           } while (sms_cur.moveToNext());
       }
     }
     if (mms_cur != null) {
   	 // Toast.makeText(this, "This Conversation has MMS messages! " + String.valueOf(mms_cur_count), Toast.LENGTH_SHORT).show();
   	  if (mms_cur.moveToFirst()) {
   		  do {
   			   long timestamp = mms_cur.getLong(2) * 1000;
   			   Date date = new Date(timestamp);
                // String dateVal = mms_cur.getString(mms_cur.getColumnIndex("date"));
                 //Date date = new Date(Long.valueOf(dateVal));
                 //String myString = DateFormat.getDateInstance().format(date);
                 String mms_date = DateFormat.getDateTimeInstance().format(date);
                 //addresses.add(myString); //nb:  Addresses equals "Date"
                 String mms_message = "MMS Message";
                // messages.add("MMS Message");
 //                contacts.add("TBD");
 //                type.add("TBD");
                 String sent_received = mms_cur.getString(mms_cur.getColumnIndex("m_type"));
                 type.add(sent_received);
                 String contact;
                 	if(sent_received.equals("1") ){
                 		//contact_names.add("Sent by Somebody");
                 		String number = (mms_cur.getString(mms_cur.getColumnIndex("address")));
                 		String name = getContactName(this, number);
                   	if(name.equals("Contact Not Found") ){
                   	//	contacts.add(number);
                   		contact = number;
                   	}
                   	else {
                   		//contacts.add(name);
                   		contact = name;
                   	}
                 	}
                 	else {
                 		//contacts.add("Me");
                 		contact = "Me";
                 	}
                 	Message message = new Message(mms_date, contact, mms_message, sent_received, date);
                 	all_messages.add(message);
   		  
   		  } while (mms_cur.moveToNext());
   	  }
   	  
     }
     
     Collections.sort(all_messages, new Comparator<Message>() {
 
 		public int compare(Message lhs, Message rhs) {
 			// TODO Auto-generated method stub
 			//return lhs.message.compareToIgnoreCase(rhs.message);
 			return lhs.message_date.compareTo(rhs.message_date);
 		}
     
 
 
   });
     
     	myMessageAdapter = new MessageArrayAdapter(this, all_messages);
         setListAdapter(myMessageAdapter);
    }
   
     
    
     public void prepareSMS(View v){
     	editMessage = (EditText) findViewById(R.id.NewMessageContent);
 		final String messageText = editMessage.getText().toString();
 		if(messageText.length() == 0){
 			return;
 		}
 //		if(messageText.length() > 160){
 //			Toast.makeText(this, "Message Too Long", Toast.LENGTH_LONG).show();
 //			return;
 //		}
 		SmsManager smsMgr = SmsManager.getDefault();
 		ArrayList<String> messages = smsMgr.divideMessage(messageText);
 		
 		for (int i=0; i < messages.size(); i++){
 			sendSMS(messages.get(i));
 		}
 		
 		
     	
     }
     public void sendSMS(String messsageText) {
     	String _messageNumber = reply_address;
     	final String messageText = editMessage.getText().toString();
 		String sent = "SMS_SENT";
         PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
             new Intent(sent), 0);
         //---when the SMS has been sent---
         registerReceiver(new BroadcastReceiver(){
             @Override
             public void onReceive(Context arg0, Intent arg1) {
             	if(getResultCode() == Activity.RESULT_OK)
                 {
                   Toast.makeText(getBaseContext(), "SMS sent",
                                 Toast.LENGTH_SHORT).show();
                   editMessage.setHint(R.string.new_text_message_hint);
                   ContentValues values = new ContentValues();
                   values.put("address", reply_address);
                   values.put("body", messageText);
                   //values.put("person", person);
                   Log.v("Values", String.valueOf(values.size()));
                   Uri InsertUri = getContentResolver().insert(Uri.parse("content://sms/sent"), values);
                   Log.v("sms_cur after send message", String.valueOf(sms_cur.getCount()));
                   //String dateVal = sms_cur.getString(sms_cur.getColumnIndex("date"));
                   Date date = new Date();
                   //String myString = DateFormat.getDateInstance().format(date);
                   String myString = DateFormat.getDateTimeInstance().format(date);
//                  
//                    addresses.add(myString);
//                  	//addresses.add("placeholder");
//                  	messages.add(messageText);
//                  	type.add("2");
//                  	contacts.add("Me");
                	Message message = new Message(myString, "Me", messageText, "2", date);
                	all_messages.add(message);
                  	myMessageAdapter.notifyDataSetChanged(); //updates conversation with sent message!!!
                 }
                 else
                 {
                 	Toast.makeText(getBaseContext(), "SMS could not sent",
                             Toast.LENGTH_SHORT).show();
                 }
             	
             }
         }, new IntentFilter(sent));
         Log.v("message number", _messageNumber);
         SmsManager sms = SmsManager.getDefault();
         sms.sendTextMessage(_messageNumber, null, messageText, sentPI, null);
         editMessage.setText("");
         editMessage.setHint("Sending message...");
         
 	}
     public String getContactName(String id){
 		 String retval = "Contact not in DB";
 		 String[] selectionArgs = new String [] {id};
 		 Cursor mCursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, "_id = ?", selectionArgs, null);
 		 Log.v("getContactName", "started");
 		//getCursorColumns(mCursor);
 		 startManagingCursor(mCursor);
 		 Log.v("cursor", String.valueOf(mCursor.getCount()));
 		 if(mCursor.getCount() > 0){
 			 mCursor.moveToFirst();
 			 retval = mCursor.getString(mCursor.getColumnIndex("display_name"));
 //			 Toast.makeText(this, retval, Toast.LENGTH_SHORT).show();
 //			 if(retval == null){
 //				 retval = "no name to display";
 //			 }
 			 
 			 
 			 Log.v("disp_name", retval);
 		
 		 }
 		 return retval;
 	 }
     
     
 //    public void updateMessages(View v){
 //    	      
 //    	       myAdapter.clear();
 //    	   
 //    	     Uri selectUri = Uri.parse("content://sms/");  //mms-sms doesn't work
 //            String[] selectionArgs = new String [] {main_message};
 //            Log.v("MainMessage", main_message);
 //    	       Cursor update_cur = getContentResolver().query(selectUri, null, "thread_id = ?", selectionArgs, "Date");//works!!
 //    	        addresses.clear();
 //    	        messages.clear();
 //    	        type.clear();
 //    	        contacts.clear();
 //    
 //    	     Log.v("update_cur on create", String.valueOf(update_cur.getCount()));
 //    	     if (update_cur != null) {
 //    	       if (update_cur.moveToFirst()) {
 //    	           do {
 //    	           addresses.add(update_cur.getString(update_cur.getColumnIndex("address")));
 //               messages.add(update_cur.getString(update_cur.getColumnIndex("body")));
 //    	           type.add(update_cur.getString(update_cur.getColumnIndex("type")));
 //    	         //  Log.v("message_id:  ", message);  
 //    	           } while (update_cur.moveToNext());
 //    	       
 //    	
 //    	      
 //    	       myAdapter = new ConversationArrayAdapter(this, addresses, messages, type, contacts);
 //    	       setListAdapter(myAdapter);
 //           }
 //    	    }
 //    	   }
 //    
     private void doStuff() {
         Toast.makeText(this, "Delayed Toast!", Toast.LENGTH_SHORT).show();
     }
     
  
     
 //    private void ConversationIntent(){
 //    Intent intent = new Intent(Conversation.this, Conversation.class);
 //    Log.v("next_message", main_message);
 //    intent.putExtra(NEXT_MESSAGE, main_message);
 //    startActivity(intent);
 //    }
     
     private String getContactName(Context context, String number) {
 
 		 Log.v("ffnet", "Started uploadcontactphoto...");
 
 		 String name = null;
 		 String contactId = null;
 		 // define the columns I want the query to return
 		 String[] projection = new String[] {
 		         ContactsContract.PhoneLookup.DISPLAY_NAME,
 		         ContactsContract.PhoneLookup._ID};
 
 		 // encode the phone number and build the filter URI
 		 Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
 
 		 // query time
 		 Cursor cursor = context.getContentResolver().query(contactUri, projection, null, null, null);
 		 startManagingCursor(cursor);
 		 if (cursor.moveToFirst()) {
 
 		     // Get values from contacts database:
 		     contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup._ID));
 		     name =      cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
 
 		     // Get photo of contactId as input stream:
 		     Log.v("ffnet", "Started uploadcontactphoto: Contact Found @ " + number);            
 		     Log.v("ffnet", "Started uploadcontactphoto: Contact name  = " + name);
 		     Log.v("ffnet", "Started uploadcontactphoto: Contact id    = " + contactId);
 
 		 } else {
 
 		     Log.v("ffnet", "Started uploadcontactphoto: Contact Not Found @ " + number);
 		     name = "Contact Not Found";
 
 		 }
 		 return name;
 
 	 }
     
 }
