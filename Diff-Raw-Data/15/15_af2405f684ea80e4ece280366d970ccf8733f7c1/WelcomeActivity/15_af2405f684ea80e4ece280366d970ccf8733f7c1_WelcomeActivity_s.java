 package com.nbos.phonebook;
 
 import java.util.List;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.provider.ContactsContract.Contacts;
 import android.provider.ContactsContract.Data;
 import android.provider.ContactsContract.Groups;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.CheckBox;
 import android.widget.FilterQueryProvider;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.Toast;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 
 import com.nbos.phonebook.contentprovider.Provider;
 import com.nbos.phonebook.sync.Constants;
 import com.nbos.phonebook.sync.authenticator.AuthenticatorActivity;
 import com.nbos.phonebook.sync.client.BookPermission;
 import com.nbos.phonebook.sync.platform.PhonebookSyncAdapterColumns;
 import com.nbos.phonebook.sync.platform.UpdateContacts;
 import com.nbos.phonebook.sync.syncadapter.SyncAdapter;
 import com.nbos.phonebook.util.WelcomeActivityCursorAdapter;
 import com.nbos.phonebook.value.Contact;
 import com.nbos.phonebook.value.Group;
 
 public class WelcomeActivity extends ListActivity {
 	static String tag = "Phonebook";
 	List<Contact> m_contacts = null;
 	List<Contact> m_phoneContacts = null;
 	ProgressDialog m_ProgressDialog = null;
 	int layout = R.layout.group_entry;
 	Db db;
 	WelcomeActivityCursorAdapter adapter ;
 	/** Called when the activity is first created. */
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_LEFT_ICON);
 		setContentView(R.layout.main);
 		setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.icon);
 		testContentProvider();
 		db = new Db(getApplicationContext());
 		populateGroups(layout);
 		listView=this.getListView();
 		getListView().setTextFilterEnabled(true);
 		String phoneNumber = getPhoneNumber();
 		Log.i(tag, "phone number: " + phoneNumber);
 		
 		if (!hasAccount()) {
 			final Intent intent = new Intent(getApplicationContext(),
 					AuthenticatorActivity.class);
 			intent.putExtra(AuthenticatorActivity.PARAM_AUTHTOKEN_TYPE,
 					Constants.ACCOUNT_TYPE);
 			startActivity(intent);
 		}
 		registerForContextMenu(getListView());
 		// startActivity(new Intent(Settings.ACTION_SYNC_SETTINGS));
 		//Test.getContactLinks(getApplicationContext());
 		//Test.getStoredContactLinks(getApplicationContext());
 		 //Test.getContacts(getApplicationContext());
 		// Test.getDataPicsCursor(getApplicationContext());
 		// Test.joinContacts(getApplicationContext());
 		// Test.deleteContactLinkTable(getApplicationContext());
 		//Test.updateServerId(getApplicationContext());
 	}
 
 	
 	public boolean onClick(View v) {
 		switch (v.getId()) 
 		{ 
 			case R.id.delete_group: 
 			showDeleteGroupDialog();
 			break;
 		} 
 		return true;
 	}
 
 	@Override 
 	public boolean onCreateOptionsMenu(Menu menu) { 
 		MenuInflater inflater = getMenuInflater(); 
 		inflater.inflate(R.menu.group_list_menu, menu); 
 		return true;
 	}
 	
 	
 	@Override
 	public void onAttachedToWindow() {
 		super.onAttachedToWindow();
 		openOptionsMenu();
 	}
 
 	  
 	@Override 
 	public boolean onOptionsItemSelected(MenuItem item) { 
 		// Handle item selection 
 		switch (item.getItemId()) { 
 			case R.id.add_group: 
 				Intent i = new Intent(WelcomeActivity.this, AddGroupActivity.class);
 		 		startActivityForResult(i, ADD_GROUP); 
 		 	break;
 			case R.id.sync:
 			try {
 				doSync();
 			} catch (Exception e) {
 				e.printStackTrace();
 			} 
 		 	break;
 		 	
 			case R.id.delete_group:
 				LinearLayout mainLayout=(LinearLayout)findViewById(R.id.mainlinearLayout);
 				LinearLayout childLayout=(LinearLayout)mainLayout.findViewById(R.id.deleteLayout);
 				childLayout.setVisibility(1);
 				populateGroups(R.layout.delete_group_entry);
 			break;
			
		    case R.id.select_sharing:
			    Cursor contactCursor=Db.getContacts(this);
			    Toast.makeText(getApplicationContext(), "contactsCount: "+contactCursor.getCount(), Toast.LENGTH_LONG).show();
			    Log.i(tag,"count: "+contactCursor.getCount());
		    break;
 		} 
 		return true;
 	}
 	
 	private void doSync() {
 		SyncAdapter syncAdapter = new SyncAdapter(getApplicationContext(), true);
 		Account account = Db.getAccount(getApplicationContext());
 		Log.i(tag, "Account is: "+account);//.name+", "+account.type);
 		if(account != null)
 		{
 			syncAdapter.onPerformSync(account, null, null, null, null);
 			populateGroups(layout);
 		}
 		else
 			Toast.makeText(getApplicationContext(), "You have not added a phonebook account", Toast.LENGTH_LONG)
 				.show();
 	}
 
 	static int ADD_GROUP = 1, SHOW_GROUP = 2, DELETE_GROUP=3;
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 	                                ContextMenuInfo menuInfo) 
 	{
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
 		m_cursor.moveToPosition(info.position);
 		String name = m_cursor.getString(m_cursor.getColumnIndex(ContactsContract.Groups.TITLE));
 		String owner = m_cursor.getString(m_cursor.getColumnIndex(Groups.SYNC1));
 		super.onCreateContextMenu(menu, v, menuInfo);
 		menu.setHeaderTitle("Group: "+name);
 		int order = 0;
 		if(owner == null && !name.equals("My phonebook"))
 		{
 			menu.add(0, v.getId(), order++, "Rename");
 		}
 	}
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		// Get the info on which item was selected
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
 				.getMenuInfo();
 		m_cursor.moveToPosition(info.position);
 		String groupId = m_cursor.getString(m_cursor.getColumnIndex(Groups._ID)), 
 		groupName = m_cursor.getString(m_cursor.getColumnIndex(Groups.TITLE)), 
 		groupOwner = m_cursor.getString(m_cursor.getColumnIndex(Groups.SYNC1)),
 		groupPermission = m_cursor.getString(m_cursor.getColumnIndex(Groups.SYNC2)),
 		accountType = m_cursor.getString(m_cursor.getColumnIndex(Groups.ACCOUNT_TYPE));
 
 	    Log.i(tag, "Group id: " + groupId + ", name: " + groupName);
 	    if(item.getTitle().equals("Showcontacts"))
 	    {
 		    	  Intent i = new Intent(WelcomeActivity.this, GroupActivity.class);
 		 	      i.putExtra("id", groupId);
 		 	      i.putExtra("name", groupName);
 		 	      i.putExtra("layout", R.layout.contact_entry);
 		 	      if(accountType.equals(Constants.ACCOUNT_TYPE))
 		 	        {
 		 		        i.putExtra("owner", groupOwner);
 		 		        i.putExtra("permission", groupPermission);
 		 	        }
 	 	
 	 	startActivityForResult(i, SHOW_GROUP);
 	    }
 	    if(item.getTitle().equals("Rename"))
 	    {
 	    		Intent r = new Intent(WelcomeActivity.this, AddGroupActivity.class);
 	    		r.putExtra("id", groupId);
 	    		r.putExtra("name", groupName);
 	    		r.putExtra("position", info.position);
 	    		startActivityForResult(r, ADD_GROUP);
 	    }
 	   
 		return true;
 	}
 
 	/*public boolean onClickAddGroup(View v) {
 
 		// Handle item selection
 		switch (v.getId()) {
 		case R.id.add_group_new:
 			Intent i = new Intent(WelcomeActivity.this, AddGroupActivity.class);
 			startActivityForResult(i, ADD_GROUP);
 			break;
 		}
 
 		return true;
 
 	}*/
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		//if ((requestCode == ADD_GROUP && resultCode == RESULT_OK)
 		//|| (requestCode == SHOW_GROUP && resultCode == RESULT_OK) 
 		//||(requestCode == DELETE_GROUP && resultCode == RESULT_OK))
 			refreshGroups();
 		    populateGroups(layout);
 	}
 
 	private void refreshGroups() {
 		System.out.println("Refresh the groups");
 		m_cursor.requery();
 	}
 
 	List<Group> m_groups;
 
 	private void testContentProvider() {
 		Uri URI = Uri.parse("content://" + Provider.AUTHORITY + "/"
 				+ Provider.BookContent.CONTENT_PATH);
 		Cursor c = getContentResolver().query(URI, null, null, null, null);
 		Log.i(tag, "There are " + c.getCount() + " books");
 	}
 
 	Cursor m_cursor;
 
 	private void populateGroups(int layout) {
 		ContentResolver cr = getContentResolver();
 		m_cursor = cr.query(ContactsContract.Groups.CONTENT_SUMMARY_URI, null,
 				ContactsContract.Groups.DELETED + "=0", null,
 				ContactsContract.Groups.TITLE);
 
 		String[] fields = new String[] { ContactsContract.Groups.TITLE,
 				ContactsContract.Groups.SUMMARY_COUNT };
 		Cursor sharedBooksCursor = Db.getBooks(cr);
 		Cursor rawContactsCursor = db.getRawContactsCursor(false);
 		adapter = new WelcomeActivityCursorAdapter(
 				this, layout, m_cursor, sharedBooksCursor, rawContactsCursor,
 				fields, new int[] { R.id.groupName, R.id.groupCount });
 
 		adapter.setStringConversionColumn(m_cursor
 				.getColumnIndexOrThrow(ContactsContract.Groups.TITLE));
 
 		adapter.setFilterQueryProvider(new FilterQueryProvider() {
 
 			public Cursor runQuery(CharSequence constraint) {
 				String partialItemName = null;
 				if (constraint != null) {
 					partialItemName = constraint.toString();
 				}
 				m_cursor = getContentResolver().query(
 						ContactsContract.Groups.CONTENT_SUMMARY_URI,
 						null,
 						// "DISPLAY_NAME = '" + NAME + "'",
 						ContactsContract.Groups.DELETED + "=0" + " and "
 								+ ContactsContract.Groups.TITLE + " like '%"
 								+ partialItemName + "%'", null, null);
 
 				Log.i(tag, "partial name is" + partialItemName);
 
 				return m_cursor;
 			}
 		});
 
 		getListView().setAdapter(adapter);
 
 	}
 
 	ListView mGroupList;
 
 	String m_exception = null;
 
 
 	private String getPhoneNumber() {
 		return ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE))
 				.getLine1Number();
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		m_cursor.moveToPosition(position);
 		String groupId = m_cursor.getString(m_cursor.getColumnIndex(Groups._ID)), 
 			groupName = m_cursor.getString(m_cursor.getColumnIndex(Groups.TITLE)), 
 			groupOwner = m_cursor.getString(m_cursor.getColumnIndex(Groups.SYNC1)),
 			groupPermission = m_cursor.getString(m_cursor.getColumnIndex(Groups.SYNC2)),
 			accountType = m_cursor.getString(m_cursor.getColumnIndex(Groups.ACCOUNT_TYPE));
 
 		Log.i(tag, "Group id: " + groupId + ", name: " + groupName);
 		Intent i = new Intent(WelcomeActivity.this, GroupActivity.class);
 		i.putExtra("id", groupId);
 		i.putExtra("name", groupName);
 		i.putExtra("layout", R.layout.contact_entry);
 		if(accountType.equals(Constants.ACCOUNT_TYPE))
 		{
 			i.putExtra("owner", groupOwner);
 			i.putExtra("permission", groupPermission);
 		}
 		
 		startActivityForResult(i, SHOW_GROUP);
 	}
 
 	boolean hasAccount() {
 		Account[] accounts = AccountManager.get(getApplicationContext())
 				.getAccounts();
 		Log.i(tag, "There are " + accounts.length + " accounts");
 		for (Account account : accounts)
 			if (account.type.equals(Constants.ACCOUNT_TYPE))
 			{
 				setTitle(" Phonebook: "+account.name);
 				return true;
 			}
 		return false;
 	}
 	
 	private void showDeleteGroupDialog() {
 		int delete_group_count=0;
 		for(int i=0;i<listView.getChildCount();i++)
 		{
 			View childView = listView.getChildAt(i);
     		CheckBox check =(CheckBox)childView.findViewById(R.id.check);
     		
     		if(check.isChecked())
     			delete_group_count++;
 		}
 		Log.i(tag,"selected group count"+delete_group_count);
 		if(delete_group_count>0)
 		{
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage("Are you sure you want to delete selected group(s)?")
 					.setCancelable(false)
 					.setPositiveButton("Yes",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog, int id) {
 									deleteGroups();
 								}
 							})
 					.setNegativeButton("No", new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							dialog.cancel();
 						}
 					});
 			AlertDialog alert = builder.create();
 			alert.show();
 		}
 		else
 			deleteGroups();
 	}
 
 	
 	ListView listView;
 	private void deleteGroups() {
 		int numDeleted = 0;
 		LinearLayout mainLayout=(LinearLayout)findViewById(R.id.mainlinearLayout);
 		LinearLayout childLayout=(LinearLayout)mainLayout.findViewById(R.id.deleteLayout);
 		
 		List<Boolean> checkedItems = adapter.getCheckedItems();
 		ContentResolver cr =getContentResolver();
 		for(int i=0;i<listView.getCount();i++) 
 		{
     		if(!checkedItems.get(i)) continue; 
 			Log.i(tag, i+" is checked");
 			m_cursor.moveToPosition(i);
 			String groupId = m_cursor.getString(m_cursor
 					.getColumnIndex(Groups._ID)), groupName = m_cursor
 					.getString(m_cursor
 							.getColumnIndex(ContactsContract.Groups.TITLE));
 			
 			System.out.println("delete group: " + groupId + ", " + groupName);
 			String[] args = { groupId };
 			
 			int b = cr.delete(
 					Groups.CONTENT_URI, "_ID=?", args);
 			numDeleted++;
 			Db.setGroupDirty(args[0], cr);
 			// notify registered observers that a row was updated
     	}
 		
 		Toast.makeText(this, "Deleted "+numDeleted+" group(s)", Toast.LENGTH_LONG).show();
 		childLayout.setVisibility(-1);
 		populateGroups(layout);
     }
 	
 	
 }
 
