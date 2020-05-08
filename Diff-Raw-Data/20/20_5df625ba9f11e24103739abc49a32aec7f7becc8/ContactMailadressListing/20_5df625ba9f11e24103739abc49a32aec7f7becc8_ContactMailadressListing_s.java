 package net.hekatoncheir.quicksendto;
 
import java.util.HashMap;
 import java.util.HashSet;
import java.util.Map;
 import java.util.Set;
 import java.util.Vector;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.net.Uri;
 import android.provider.ContactsContract;
 import android.util.Log;
 
 public class ContactMailadressListing
 {
 	static public Vector<String> listup(Activity context)
 	{
 		Vector<String> v = new Vector<String>();
 		
 		Log.d("EditTemplate", "loadContact");
 		
 		Uri uri = ContactsContract.Contacts.CONTENT_URI;
 		String[] projection = new String[] {
 			ContactsContract.Contacts._ID,
 			ContactsContract.Contacts.DISPLAY_NAME
 		};
 		String[] selectionArgs = null;
 		String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
 
		Map<Integer, String> displayNameSet = new HashMap<Integer, String>();
 		Set<String> emailAddressSet = new HashSet<String>();
 		
		Cursor cursor = context.managedQuery(uri, projection, null, selectionArgs, sortOrder);
 		while (cursor.moveToNext()){
 			String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
 			if( displayName == null)displayName = "";
 			int id = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID));
			displayNameSet.put(new Integer(id), displayName);
 		}
 		cursor.close();
 
 		Cursor emails = context.getContentResolver().query( 
 			ContactsContract.CommonDataKinds.Email.CONTENT_URI, 
 			null, 
 			null, null, null);
 		
 		while (emails.moveToNext()){
 			int id = emails.getInt(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID));
 			String emailAddress = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
			String displayName = displayNameSet.get(new Integer(id));
 			
 			if( emailAddress != null && displayName != null && !emailAddressSet.contains(emailAddress)){
 				Log.d("EditTemplate", displayName + " " +emailAddress);
 				v.add(GetAddressString(displayName, emailAddress));
 				
 				emailAddressSet.add(emailAddress);
 			}
 		}
 		emails.close();
 		
 		return v;
 
 	}
     static String GetAddressString(String displayName, String emailAddress)
 	{
 		if( displayName == null || displayName == emailAddress)return emailAddress;
 		return displayName + " <" + emailAddress + ">";
 	}		
 }
