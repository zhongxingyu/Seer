 package com.snot.whereareyou;
 
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.provider.ContactsContract.CommonDataKinds.Phone;
 import android.support.v4.app.Fragment;
 import android.telephony.SmsManager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.Toast;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 
 /**
  * GCM - http://developer.android.com/google/gcm/client.html
  *       https://code.google.com/p/gcm/
  *
  * TODO
  *  - Remove hardcoded strings
  *  - move api key in to main activity as static property
  */
 
 
 public class PickContactFragment extends Fragment {
 
 	private static final int PICK_CONTACT_REQUEST = 1;  // The request code
 
 	// Log tag
 	static final String TAG = "GCM Demo";
 
 	@Override
 	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
 	{
 		View view = inflater.inflate(R.layout.activity_main, container, false);
 		Button button = (Button)view.findViewById(R.id.pick_contact_button);
 		button.setOnClickListener(
 			new OnClickListener()
 			{
 				public void onClick(View v)
 				{
 					pickContact();
 				}
 			}
 		);
 		return view;
 	}
 
         /** Shows contact picker dialog
          */
         public void pickContact() {
             Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
             intent.setType(Phone.CONTENT_TYPE); // Show user only contacts w/ phone numbers
             startActivityForResult(intent, PICK_CONTACT_REQUEST);
         }
 
 	@Override
 	public void onActivityResult( int requestCode, int resultCode, Intent intent ) {
 		super.onActivityResult( requestCode, resultCode, intent );
 		if(resultCode == Activity.RESULT_OK) {
 			if(requestCode == PICK_CONTACT_REQUEST) {
 				handleContact(intent);
 			}
 		}
 	}
 
 	private void handleContact(Intent intent)
 	{
 		final String phoneNumber = getPhoneNumber(intent);
 		final Context context = getActivity();
 
 		String url = "";
                 try {
 			String regId = ((MainActivity)context).getRegistrationId(context);
 			String query = "?regId=" + URLEncoder.encode(regId, "UTF-8") + "&phoneNumber=" + URLEncoder.encode(phoneNumber, "UTF-8");
                         url = "https://whereareyoudroid.appspot.com/location" + query;
                 } catch (UnsupportedEncodingException e) {
                         // TODO Auto-generated catch block
                         e.printStackTrace();
                 }
 
		ShortenUrlTask task = new ShortenUrlTask() {
 			@Override
 			protected void onPostExecute(String result) {
 				String message = context.getString(R.string.sms_message) + result;
 				sendSMS(phoneNumber, message);
 				Toast.makeText(context, context.getString(R.string.sms_send_toast), Toast.LENGTH_SHORT).show();
 			}
 		};
 		task.execute(url);
 //		Toast.makeText(this, "SMS send to " + phoneNumber + ".\nAwaiting response...", Toast.LENGTH_SHORT).show();
         }
 
 // TODO: confirm that the text is send... no error if missing sim card
 // Also do something to show that the text has been delivered... toast and perhaps show something in hist.
 // perhaps use something from... http://stackoverflow.com/questions/7926506/toast-wont-show-up-on-sms-app
 // also... http://developer.android.com/reference/android/telephony/SmsManager.html#sendMultipartTextMessage(java.lang.String, java.lang.String, java.util.ArrayList<java.lang.String>, java.util.ArrayList<android.app.PendingIntent>, java.util.ArrayList<android.app.PendingIntent>)
         /** SMS sender with support for big messages
          * @param phoneNumber
          * @param message
          */
 	private void sendSMS(String phoneNumber, String message)
 	{
 		Log.v(TAG, String.format("sendSMS(%s, %s)", phoneNumber, message));
 		SmsManager sms = SmsManager.getDefault();
 		ArrayList<String> parts = sms.divideMessage(message);
 		// TODO: this used to work with null arguments as below... not anymore!!!
 		//sms.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
 		ArrayList<PendingIntent> sentList = new ArrayList<PendingIntent>();
 		ArrayList<PendingIntent> deliveredList = new ArrayList<PendingIntent>();
 		sms.sendMultipartTextMessage(phoneNumber, null, parts, sentList, deliveredList);
 	}
 
 	private String getPhoneNumber(Intent intent)
 	{
 		Uri contact = intent.getData();
 		String[] projection = {Phone.NUMBER};
 		Cursor cursor = getActivity().getContentResolver().query(contact, projection, null, null, null);
 		cursor.moveToFirst();
 		// Retrieve the phone number from the NUMBER column
 		int column = cursor.getColumnIndex(Phone.NUMBER);
 		String phoneNumber = null;
 		if(column != -1)
 		{
 			phoneNumber = cursor.getString(column);
 		}
 		cursor.close();
 		return phoneNumber;
 	}
 
 }
 
