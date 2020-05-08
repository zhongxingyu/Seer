 package com.huronasolutions.plugins.ContactsPlugin;
 
 import org.apache.cordova.CallbackContext;
 import org.apache.cordova.CordovaPlugin;
 import org.apache.cordova.PluginResult;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.provider.ContactsContract;
 
 public class ContactView extends CordovaPlugin {
 	private static final int PICK_CONTACT = 1;
 	private CallbackContext callback;
 
 	@Override
 	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
 		if (action.equals("getContact")) {
 
 			this.callback = callbackContext;
 			cordova.getThreadPool().execute(new Runnable() {
 
             			public void run() {
 					startContactActivity();
 					PluginResult mPlugin = new PluginResult(PluginResult.Status.NO_RESULT);
 					mPlugin.setKeepCallback(true);
 					callbackContext.sendPluginResult(mPlugin);
 
                 			//callbackContext.success(); // Thread-safe.
             		}
 			});
 			return true;
 		}
 		return false;
 	}
 
 	public void startContactActivity() {
 		Intent intent = new Intent(Intent.ACTION_PICK);
 		intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
 		this.cordova.startActivityForResult((CordovaPlugin) this, intent, PICK_CONTACT);
 
 	}
 
	/* (non-Javadoc)
	 * @see org.apache.cordova.CordovaPlugin#onActivityResult(int, int, android.content.Intent)
	 */
 	@Override
 	public void onActivityResult(int reqCode, int resultCode, Intent data) {
 		String name = null;
 		String number = null;
		
 		switch (reqCode) {
 		case (PICK_CONTACT):
 			if (resultCode == Activity.RESULT_OK) {
 				Uri contactData = data.getData();
 				Cursor c = this.cordova.getActivity().getContentResolver()
 						.query(contactData, null, null, null, null);
 				if (c.moveToFirst()) {
 					String ContactID = c.getString(c
 							.getColumnIndex(ContactsContract.Contacts._ID));
 					String hasPhone = c
 							.getString(c
 									.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
 
 					if (Integer.parseInt(hasPhone) == 1) {
 						Cursor phoneCursor = this.cordova
 								.getActivity()
 								.getContentResolver()
 								.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
 										null,
 										ContactsContract.CommonDataKinds.Phone.CONTACT_ID
 												+ "='" + ContactID + "'", null,
 										null);
 						while (phoneCursor.moveToNext()) {
 							number = phoneCursor
 									.getString(phoneCursor
 											.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
 						}
 					}
 					// get email address
					/*Cursor emailCur = this.cordova
 							.getActivity()
 							.getContentResolver()
 							.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
 									null,
 									ContactsContract.CommonDataKinds.Email.CONTACT_ID
 											+ "='" + ContactID + "'", null,
 									null);
 					while (emailCur.moveToNext()) {
 						// This would allow you get several email addresses
 						// if the email addresses were stored in an array
 						email = emailCur
 								.getString(emailCur
 										.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
 						// String emailType = emailCur.getString(
 						// emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
 					}
					emailCur.close();*/
 
 					name = c.getString(c
 							.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
 					JSONObject contactObject = new JSONObject();
 					try {
 						contactObject.put("name", name);
 						contactObject.put("phone", number);
//						contactObject.put("email", email);
 						contactObject.put("status", 1);
 					} catch (JSONException e) {
 						System.err.println("Exception: " + e.getMessage());
 						callback.error(e.getMessage());
 					}
 
 					callback.success(contactObject);
 				}
 			}
 			else{
 				JSONObject messageObject = new JSONObject();
 				try {
 					messageObject.put("status", 0);
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					System.err.println("Exception: " + e.getMessage());
 					callback.error(e.getMessage());
 				}
 			callback.success(messageObject);
 			}
 			break;
 		}
 	}
 
 
 	@Override
 	public void onDestroy() {
 		this.cordova.getActivity().stopService(new Intent(Intent.ACTION_PICK));
 		super.onDestroy();
 	}
 }
