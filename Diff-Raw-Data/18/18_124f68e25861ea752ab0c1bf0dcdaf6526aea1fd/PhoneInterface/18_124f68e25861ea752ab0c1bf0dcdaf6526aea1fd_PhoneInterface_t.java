 package spam.me;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.provider.ContactsContract.PhoneLookup;
 import android.provider.ContactsContract.CommonDataKinds.Phone;
 import android.telephony.SmsManager;
 import android.widget.Toast;
 
 public class PhoneInterface extends Activity {
 	public PhoneInterface(){
 		
 	}
 	
 	public List<Person> getContactList(){
 		List<Person> contactList = null;
 		
 		int idFieldColumnIndex = 0;
 		int nameFieldColumnIndex = 0;
 		int numberFieldColumnIndex = 0;
 		String id = "";
 		String name = "";
 		String number = "";
 
 		Uri contactUri = ContactsContract.Contacts.CONTENT_URI;
 		String[] PROJECTION = new String[] {
 				ContactsContract.Contacts._ID,
 				ContactsContract.Contacts.DISPLAY_NAME,
 				ContactsContract.Contacts.HAS_PHONE_NUMBER,
 		};
 		String SELECTION = ContactsContract.Contacts.HAS_PHONE_NUMBER + "='1'";
 		Cursor contacts = managedQuery(contactUri, PROJECTION, SELECTION, null, null );
 		// Because cursors always start at -1 instead of 0 and entries start at 0
 		contacts.moveToFirst();
 		
 		Person aContact;
 		do {
 			aContact = null;
 			// Get the id of this contact
 			idFieldColumnIndex = contacts.getColumnIndex(ContactsContract.Contacts._ID);
 			if (idFieldColumnIndex > 0)
 			{
 				id = contacts.getString(idFieldColumnIndex);
 				System.out.println(id);
 			}
 
 			// Get the name of this contact
 			nameFieldColumnIndex = contacts.getColumnIndex(PhoneLookup.DISPLAY_NAME);
 			if (nameFieldColumnIndex > 0)
 			{
 				aContact.setName(contacts.getString(nameFieldColumnIndex));
 				System.out.println(name);
 			}
 
 			// Get the phone number of this specific contact via contact ID lookup
 			Cursor pCur = managedQuery(Phone.CONTENT_URI,null,Phone.CONTACT_ID +" = ?", new String[]{id}, null);
 			// Because cursors always start at -1 instead of 0 and entries start at 0
 			pCur.moveToFirst();
 			numberFieldColumnIndex = pCur.getColumnIndex(Phone.NUMBER);
 			if (numberFieldColumnIndex > 0)
 			{
 				aContact.setPhoneNum(pCur.getString(numberFieldColumnIndex));
 				System.out.println(number);
 			}
 			pCur.close();
 			
 			contactList.add(aContact);
 		} while(contacts.moveToNext());
 		contacts.close();
 		
 		return contactList;
 	}
 	public void sendSMS(String msg, String number){
 		/*
 		Bundle bundle = this.getIntent().getExtras(); 
 		String msg = bundle.getString("message");
 		String number = bundle.getString("phoneNumber");
 		Toast.makeText(getBaseContext(), 
 				"Msg is: " + msg, 
 				Toast.LENGTH_SHORT).show();
 		*/
		//PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, PhoneInterface.class), 0);
 		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(number, null, msg, null, null);
 	}
 	public void receiveSMS(){
 	}
 }
