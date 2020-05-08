 package com.allsafe;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.provider.ContactsContract.CommonDataKinds;
 import android.provider.ContactsContract.CommonDataKinds.Phone;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.allsafe.db.ContactsDBManager;
 
 public class MainActivity extends Activity {
 
     private static final int ADD_CONTACT_RESULT = 1234;
 	
 	private List<Contact> contacts;
 	private ListView contactsList;
 	private String myPhoneNumber = null;
 	
 	private ContactsDBManager db;
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         contacts = new LinkedList<Contact>();
         db = new ContactsDBManager(this);
         
         loadContactsFromDB();
         
         setContentView(R.layout.activity_main);
         
         getLocalPhoneNumber();
         
         loadListView();
         
         bindAddButton();
        bindSelectButton();
     }
 
     private void getLocalPhoneNumber() {
     	TelephonyManager telephonyManager = (TelephonyManager) this
 				.getSystemService(Context.TELEPHONY_SERVICE);
     	myPhoneNumber = telephonyManager.getLine1Number();
     	
     	if(myPhoneNumber == null || myPhoneNumber.isEmpty()){
     		showError("can't find local phone");
     	}
 		
 	}
 
     private void showError(String string) {
     	Log.e("<<safe>>",string);
     	Toast waitToast = Toast.makeText(this, string, Toast.LENGTH_LONG);
     	waitToast.setGravity(Gravity.CENTER, 0, 0);
     	waitToast.show();
 	}
 
 	private void loadListView() {
     	contactsList = (ListView) findViewById(R.id.contactsList);
     	contactsList.setAdapter( new ContactsAdapter(this, contacts)); 
     
 		
 	}
 
 	private void bindAddButton() {
     	(findViewById(R.id.add)).setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				addContacts();
 			}
 			
 		});
 	}
 	
 	private void bindCheckButton() {
     	(findViewById(R.id.check)).setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				addContacts();
 			}
 			
 		});
 	}
     
     
     private void addContacts() {
     	Intent intent = new Intent(Intent.ACTION_PICK);
     	intent.setData(Uri.parse("content://contacts/people/" ));
     	startActivityForResult(intent,ADD_CONTACT_RESULT);
 		
 	}
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     	if(requestCode==ADD_CONTACT_RESULT && resultCode==RESULT_OK ){
     		Uri contactUri = data.getData();
     		addContactFromUri(contactUri);
     	}
     }
     
     private void addContactFromUri(Uri contactUri) {
     	 String[] projection = { ContactsContract.Contacts._ID, Phone.DISPLAY_NAME };
 		Cursor cursor = getContentResolver()
                  .query(contactUri, projection , null, null, null);
          cursor.moveToFirst();
 
          String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
          String name = cursor.getString(cursor.getColumnIndex(Phone.DISPLAY_NAME));
          
          
         String phone = getPhoneFromPhonesBook(id,name);
         if(phone!= null){
 	         Contact contact = new Contact(name,phone, contactUri);
 	         
 	         if(!contacts.contains(contact)){
 		         contacts.add(contact);
 		         loadListView();
 		         addContactToDB(contact);
 	         }
         }
         else{
         	Log.e("<<safe>>", "ERROR: no phone for user");
         }
          
          
 		
 	}
 
 
 
 	private String getPhoneFromPhonesBook(String id, String name) {
 		ContentResolver cr = getContentResolver();
         
 		Cursor cur = cr.query(
 		  CommonDataKinds.Phone.CONTENT_URI,
 		  null,
 		  CommonDataKinds.Phone.CONTACT_ID +" = ?",
 		  new String[]{id}, null);
 		   
 		if (cur.getCount() > 0) {
 		          while (cur.moveToNext()) {
 		                             String number =
 		                                      cur.getString(
 		                                                cur.getColumnIndex(
 		                                                          ContactsContract.CommonDataKinds.Phone.NUMBER));
                             //TODO: more than one phone
 		                   return number;
 		          }
 		}
 		
 		return null;
 
 	}
 
 	private void addContactToDB(Contact contact) {
 		db.insert(contact.getName(),contact.getPhone());
 	}
 
 	private void loadContactsFromDB() {
 		 Cursor cursor = db.getAll();
 	        
         while(cursor.moveToNext()){
         	contacts.add(new Contact(cursor.getString(1), cursor.getString(2), null));
         }
 	}
 
 //	private void addContactToList(Contact contact) {
 //		TextView text = new TextView(this);
 //		text.setText(contact.getName());
 //		
 //		ImageView image = new ImageView(this);
 //		image.setImageResource(R.drawable.ic_launcher);
 //		mainView.addView(text);
 //		mainView.addView(image);
 //		
 //		
 //	}
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 }
