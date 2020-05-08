 package net.lukegjpotter.app.addressbookdemo;
 
 /**
  *  AddressBookActivity.java
  * 
  *  @author Luke Potter
  *  @date: 21/Feb/2013
  * 
  *  This class displays a list view of contacts.
  */
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.CursorAdapter;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 
 public class AddressBookActivity extends ListActivity {
 
 	public static final String ROW_ID = "row_id"; // Extra key for Intent; passed between activities.
 	private ListView contactListView;             // The ListActivity's ListView; can interact with it programmatically.
 	private CursorAdapter contactAdapter;         // Adapter for ListView; populates the AddressBook's ListActivity.
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 		
 		contactListView = getListView(); // Get the in built ListView
 		contactListView.setOnItemClickListener(viewContactListner);
 		
 		// Map each contact's name in a TextView in the ListView layout
 		String[] from = new String[] {"name"};
 		int[] to = new int[] {R.id.contactTextView};
 		
 		@SuppressWarnings("deprecation")
 		CursorAdapter cursorAdapter = new SimpleCursorAdapter(AddressBookActivity.this, R.layout.view_contact_list_item, null, from, to);
 		setListAdapter(cursorAdapter); // Set the ContactView's adapter
 	}
 	
 	@Override
 	protected void onResume() {
 		
 		super.onResume();
 		
 		// Create a new GetContactsTask and execute it.
 		new GetContactsTask().execute((Object[]) null);
 	}
 	
 	@Override
 	protected void onStop() {
 
 		Cursor cursor = contactAdapter.getCursor();
 		
 		if (cursor != null) {
 			cursor.deactivate();
 		}
 		
 		contactAdapter.changeCursor(null);
 		
 		super.onStop();
 	}
 
 	/**
 	 * A nested class that extends ASyncTask that performs a database query outside the GUI thread.
 	 */
 	private class GetContactsTask extends AsyncTask<Object, Object, Cursor> {
 
 		DatabaseConnector dbConn = new DatabaseConnector(AddressBookActivity.this);
 		
 		// Perform the database access
 		@Override
 		protected Cursor doInBackground(Object... params) {
 			
 			dbConn.open();
 			
 			// Get a cursor containing all contacts.
 			return dbConn.getAllContacts();
 		}
 		
 		@Override
 		protected void onPostExecute(Cursor result) {
 			
			contactAdapter.changeCursor(result); // Set the adaptor's cursor.
			dbConn.close();
 		}
 	}
 	
 	/**
 	 * Create the Activity's menu from a menu resource XML file.
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		
 		super.onCreateOptionsMenu(menu);
 		
 		// Inflate the menu; this adds items to the action bar if it is present. <- Cool Story Brah!
 		getMenuInflater().inflate(R.menu.activity_address_book, menu);
 		
 		return true;
 	}
 	
 	/**
 	 * Handle choice from options menu.
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		
 		// Create a new intent to launch the AddEditContact Activity.
 		Intent addNewContact = new Intent(AddressBookActivity.this, AddEditContactActivity.class);
 		startActivity(addNewContact);
 		
 		return super.onOptionsItemSelected(item);
 	}
 
 	/**
 	 * An Event Listener that responds to the user touching a contact's name in the ListView.
 	 */
 	OnItemClickListener viewContactListner = new OnItemClickListener() {
 
 		@Override
 		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 			
 			// Create the Intent to launch the ViewContact Activity
 			Intent viewContact = new Intent(AddressBookActivity.this, ViewContactActivity.class);
 			
 			// Pass the selected contact's row ID as an extra with the Intent.
 			viewContact.putExtra(ROW_ID, arg3);
 			startActivity(viewContact);
 		}
 	};
 }
