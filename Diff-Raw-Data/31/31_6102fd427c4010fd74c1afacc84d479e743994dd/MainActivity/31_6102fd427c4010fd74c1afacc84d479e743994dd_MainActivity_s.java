 package com.mani.emergency;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract.CommonDataKinds.Phone;
 import android.provider.ContactsContract.Contacts;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.ToggleButton;
 
 public class MainActivity extends ListActivity {
 	
 	private static final int CONTACT_PICKER_RESULT = 1001;
 	//LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
     ArrayList<String> listItems=new ArrayList<String>();
 
     //DEFINING STRING ADAPTER WHICH WILL HANDLE DATA OF LISTVIEW
     ArrayAdapter<String> adapter;
 
     DatabaseHandler db = new DatabaseHandler(this);
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 	    List<Contact> contacts = db.getAllContacts();       
 	    for (Contact cn : contacts) {
 	    	listItems.add(cn.getName());
 	        String log = "Id: "+cn.getId()+" ,Name: " + cn.getName() + " ,Phone: " + cn.getPhoneNumber();
 	            // Writing Contacts to log
 	    Log.d("Tag", log);
 	    }
 		adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
 	    setListAdapter(adapter);
 		Button button = (Button) findViewById(R.id.add);
 		button.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
 				startActivityForResult(intent, CONTACT_PICKER_RESULT);
 			}
 		});
 		Button delete = (Button) findViewById(R.id.delete);
 		delete.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				listItems.removeAll(listItems);
 				adapter.clear();
 				db.deleteAllContacts();
 				adapter.notifyDataSetChanged();
 			}
 		});
		final Intent intent = new Intent(this, ShakerService.class);
 		ToggleButton service = (ToggleButton) findViewById(R.id.toggle);
 		if(db.getButton(1) != null){
 			service.setChecked(db.getButton(1));
 		}else{
 			db.setButton(false);
 			service.setChecked(db.getButton(1));
 		}
 		service.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				if(isChecked) {
					startService(intent);
 					db.updateButton(isChecked);
 				}else{
					stopService(intent);
 					db.updateButton(isChecked);
 				}
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if(resultCode == RESULT_OK){
 			switch(requestCode){
 			case CONTACT_PICKER_RESULT:
 				Cursor cursor = null;
 				String phoneNumber = "";
 				String name = "";
 				List<String> allNumbers = new ArrayList<String>();
 				List<String> allNames = new ArrayList<String>();
 				int phoneIdx = 0;
 				int nameId = 0;
 				Uri result = data.getData();
 				String id = result.getLastPathSegment();
 				cursor = getContentResolver().query(Phone.CONTENT_URI, null, Phone.CONTACT_ID + "=?", new String[] {id}, null);
 				phoneIdx = cursor.getColumnIndex(Phone.DATA);
 				nameId = cursor.getColumnIndex(Contacts.DISPLAY_NAME);
 				try {
 					if (cursor.moveToFirst()) {
 						while (cursor.isAfterLast() == false) {
 							phoneNumber = cursor.getString(phoneIdx);
 							name = cursor.getString(nameId);
 							allNumbers.add(phoneNumber);
 							allNames.add(name);
 							cursor.moveToNext();
 						}
 					} else {
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				} finally {
 					if (cursor != null) {
 						cursor.close();
 					}
 				}
 				final CharSequence[] items = allNumbers.toArray(new String[allNumbers.size()]);
 				final CharSequence[] names = allNames.toArray(new String[allNames.size()]);
 				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
 				builder.setTitle(getString(R.string.chooseNumber));
 				builder.setItems(items, new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog,	int item) {
 						String selectedNumber = items[item].toString();
 						String selectedName = names[item].toString();
 						selectedNumber = selectedNumber.replace("-", "");
 						db.addContact(new Contact(selectedName, selectedNumber));
 						adapter.add(selectedName);
 						 // Reading all contacts
 					    Log.e("Tag", "Reading all contacts.."); 
 					    List<Contact> contacts = db.getAllContacts();       
 					     
 					    for (Contact cn : contacts) {
 					        String log = "Id: "+cn.getId()+" ,Name: " + cn.getName() + " ,Phone: " + cn.getPhoneNumber();
 					            // Writing Contacts to log
 					    Log.d("Tag", log);
 					    }
 					}
 				});
 				AlertDialog alert = builder.create();
 				if (allNumbers.size() > 1) {
 					alert.show();
 				} else {
 					String selectedNumber = phoneNumber.toString();
 					String selectedName = name.toString();
 					selectedNumber = selectedNumber.replace("-", "");
 					db.addContact(new Contact(selectedName, selectedNumber));
 					adapter.add(selectedName);
 				}
 				break;
 			}
 		}
 		adapter.notifyDataSetChanged();
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 }
