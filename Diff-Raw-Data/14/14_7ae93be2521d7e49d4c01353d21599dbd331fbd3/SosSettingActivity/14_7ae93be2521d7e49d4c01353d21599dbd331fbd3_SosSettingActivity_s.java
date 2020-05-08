 package android.course.project.sos;
 
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.course.project.sos.domain.Contact;
 import android.course.project.sos.utility.DatabaseHelper;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.TextView;
 
public final class SosSettingActivity extends Activity {
 
	private TextView sosPhoneCallLabel;
 	private HashMap<String,Contact> selectedContact;
 	static final int PICK_CONTACT_REQUEST = 1;  // The request code
 	private DatabaseHelper dbHelper;
 	private boolean phoneCall = false;
 	private boolean phonesms = false;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.sos_setting);
         selectedContact = new HashMap<String,Contact>();
//        sosPhoneCallLabel = (TextView) findViewById(R.id.textTimer);
         
         dbHelper=new DatabaseHelper(this);
 
         List<Contact> values = dbHelper.getAllContacts();
 
 
//        ArrayAdapter<Contact> adapter = new ArrayAdapter<Contact>(this,android.R.layout.simple_list_item_1, values);
//		setListAdapter(adapter);				
         
         Button pickPhoneCallButton = (Button) findViewById(R.id.btnPickPhoneCallContact);       
         pickPhoneCallButton.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View view){
 //				phoneCall  =true;
 //				phonesms  =false;
 				Intent intent = new Intent(Intent.ACTION_PICK,ContactsContract.Contacts.CONTENT_URI);
                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
         		startActivityForResult(intent, PICK_CONTACT_REQUEST);
         		
 
         	}
         });
         
         Button pickSmsButton = (Button) findViewById(R.id.btnPickSMSContact);       
         pickSmsButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view){
 				phoneCall = false;
 				phonesms  = true;
         	}
         });
         
     }
 	
 	@Override
 	public void onActivityResult(int reqCode, int resultCode, Intent data) {
 		super.onActivityResult(reqCode, resultCode, data);
 //		ArrayAdapter<Contact> adapter = (ArrayAdapter<Contact>) getListAdapter();
 		switch (reqCode) {
 		case (PICK_CONTACT_REQUEST):
 			if (resultCode == Activity.RESULT_OK) {
 				Uri contactData = data.getData();
 		        String[] projection = new String[] {
 		                ContactsContract.Contacts._ID,
 		                ContactsContract.Contacts.DISPLAY_NAME,
 		        };
 		        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
 				Cursor c = getContentResolver().query(contactData, projection, null, null, sortOrder);
 				if (c.moveToFirst()) {
 					String phoneNumber  = "";
 					Contact selected = new Contact();
 					selected.setID(c.getString(c.getColumnIndexOrThrow(ContactsContract.Data._ID)));
 					selected.setName(c.getString(c.getColumnIndexOrThrow(ContactsContract.Data.DISPLAY_NAME)));
 		            if (Integer.parseInt(c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) 
 		            {
 		                Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ selected.getID(),null, null); 
 		                while (phones.moveToNext()) { 
 		                	phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
 		                    if (phoneNumber.length() > 0)
 		                    	break;
 		                } 
 		                phones.close(); 
 		            }
 		            selected.setPhoneNumber(phoneNumber);
 		            selected.setPhone(phoneCall);
 		            selected.setSms(phonesms);
 		            dbHelper.addContact(selected);
 					selectedContact.put(selected.getID(), selected);
 				}
 			}
 			
 			break;
 		}
 //		adapter.notifyDataSetChanged();
 	}
 	
 
 }
