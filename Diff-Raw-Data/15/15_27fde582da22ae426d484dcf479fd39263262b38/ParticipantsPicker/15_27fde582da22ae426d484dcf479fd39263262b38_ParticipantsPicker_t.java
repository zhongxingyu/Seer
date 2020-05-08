 package com.app.settleexpenses;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 
 import java.util.ArrayList;
 
 public class ParticipantsPicker extends Activity {
 
     private static final int PICK_CONTACT = 0;
     private final ArrayList<String> contacts = new ArrayList<String>();
 	private final ArrayList<String> contactIds = new ArrayList<String>();
 	private final Activity currentActivity = this;
 	private long eventId = -1;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
         setContentView(R.layout.participants_picker);
         setTitle("Pick Participants");
         
         eventId = getIntent().getLongExtra(DbAdapter.EVENT_ID, -1);
 
         Button addParticipantButton = (Button) findViewById(R.id.add_participant);
         Button continueButton = (Button) findViewById(R.id.continueButton);
         addParticipantButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
 				startActivityForResult(intent, PICK_CONTACT);
 			}
 		});
         
         continueButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				Intent addExpensesIntent = new Intent(currentActivity, AddExpenses.class);
 				addExpensesIntent.putExtra(DbAdapter.PARTICIPANT_IDS, contactIds);
 				addExpensesIntent.putExtra(DbAdapter.EVENT_ID, eventId);
 				startActivityForResult(addExpensesIntent, 1);
 			}
 		});
     }
 	
 	@Override
 	public void onActivityResult(int reqCode, int resultCode, Intent data) {
 	  super.onActivityResult(reqCode, resultCode, data);
 
 	  switch (reqCode) {
 	    case (PICK_CONTACT) :
 	      if (resultCode == Activity.RESULT_OK) {
 	        Uri contactData = data.getData();
 	        Cursor c =  managedQuery(contactData, null, null, null, null);
 	        if (c.moveToFirst()) {
 	          String name = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
 	          String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
 	          ListView v = (ListView) findViewById(R.id.list);
 
 	          contacts.add(name);
 	          contactIds.add(id);
	          v.setAdapter(new ArrayAdapter<String>(this,R.layout.participant , contacts));
 	        }
 	      }
 	      break;
 	  }
 	}
 }
