 package com.nbos.phonebook;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.CursorJoiner;
 import android.database.MatrixCursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
 import android.provider.ContactsContract.CommonDataKinds.Phone;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.FilterQueryProvider;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.nbos.phonebook.database.IntCursorJoiner;
 import com.nbos.phonebook.database.tables.BookTable;
 import com.nbos.phonebook.util.ImageCursorAdapter;
 import com.nbos.phonebook.value.ContactRow;
 
 public class GroupActivity extends ListActivity {
 
 	String id, name, owner;
 	static String tag = "GroupActivity";
 	MatrixCursor m_cursor;
 	List<String> ids;
 	Cursor groupCursor;
 	ImageCursorAdapter adapter;
 	Cursor rawContactsCursor;
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.group);
 		// this.registerForContextMenu(getListView());
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			id = extras.getString("id");
 			name = extras.getString("name");
 			owner = extras.getString("owner");
 			Log.i(tag, "Owner is: " + owner);
 		}
 		queryGroup();
 		registerForContextMenu(getListView());
 		getListView().setTextFilterEnabled(true);
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		// if (owner != null) return;
 		Log.i(tag, "Create context menu");
 		super.onCreateContextMenu(menu, v, menuInfo);
 		// Get the info on which item was selected
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
 		m_cursor.moveToPosition(info.position);
 		String contactName = m_cursor.getString(m_cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)),
 			contactId = m_cursor.getString(m_cursor.getColumnIndex(ContactsContract.Contacts._ID));
 		
 		menu.setHeaderTitle("Menu: " + contactName);
 		int order = 0;
 		if(getPhoneNumber(contactId) != null)
 			menu.add(0, v.getId(), order++, "Call");
 		menu.add(0, v.getId(), order++, "Edit");
 		menu.add(0, v.getId(), order++, "Remove from group");
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		// Get the info on which item was selected
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
 				.getMenuInfo();
 
 		m_cursor.moveToPosition(info.position);
 		String contactId = m_cursor.getString(m_cursor
 				.getColumnIndex(ContactsContract.Contacts._ID)), 
 			name = m_cursor.getString(m_cursor
 						.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
 
 		Log.i(tag, "position is: " + info.position + ", contactId: "
 				+ contactId + ", name: " + name);
 		// Retrieve the item that was clicked on
 		// Object o = adapter.getItem(info.position);
 
 		if (item.getTitle() == "Remove from group") {
 			Log.i(tag, "Remove: " + item.getItemId());
 			removeFromGroup(contactId);
 		} else if (item.getTitle() == "Call") {
 			// call the guy
 			callFromGroup(contactId);
 		} else if (item.getTitle() == "Edit") {
 			showEdit(contactId);
 		} else {
 			return false;
 		}
 		return true;
 	}
 	int EDIT_CONTACT = 1;
 	private void showEdit(String contactId) {
 		// Intent i = new Intent(Intent.ACTION_EDIT);
 		Intent i = new Intent(GroupActivity.this, EditContactActivity.class);
 		i.setData(Uri.parse(ContactsContract.Contacts.CONTENT_URI + "/" + contactId));
 		startActivityForResult(i, EDIT_CONTACT);
 	}
 
 	private String getPhoneNumber(String contactId) {
 		Log.i(tag, "getPhoneNumber("+contactId+")");
 		Cursor phones = getContentResolver().query(
 				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
 				new String [] {Phone.NUMBER, Phone.TYPE},
 				ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, 
 				null, null);
 		Log.i(tag, "There are "+phones.getCount()+" phone numbers");		
 		if(phones.getCount()==0) return null;
 		phones.moveToFirst();
 		String phoneNumber = phones.getString(phones.getColumnIndexOrThrow(Phone.NUMBER));
 		Log.i(tag, "contactId:" + contactId + ", phone number is: " + phoneNumber);
 		phones.close();
 		return phoneNumber;
 	}
 	
 	private void callFromGroup(String contactId) {
 		String phoneNumber = getPhoneNumber(contactId);
 		if(phoneNumber == null) return;
 		Log.i(tag, "Calling contactId:" + contactId + ", phone number is: " + phoneNumber);
 		Intent callIntent = new Intent(Intent.ACTION_CALL);
 		callIntent.setData(Uri.parse("tel:" + phoneNumber));
 		startActivity(callIntent);
 	}
 
 	private void removeFromGroup(String contactId) {
 		String[] args = { id, contactId };
 		int b = getContentResolver()
 				.delete(ContactsContract.Data.CONTENT_URI,
 						ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID
 								+ " = ? "
 								+ " and "
 								+ ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID
 								+ " = ? ", args);
 
 		Toast.makeText(this, "Removed", Toast.LENGTH_SHORT).show();
 		Db.setGroupDirty(id, getContentResolver());
 		// notify registered observers that a row was updated
 		getContentResolver().notifyChange(ContactsContract.Data.CONTENT_URI,
 				null);
 		queryGroup();
 	}
 
 	private int numContacts() {
 		Cursor contactsCursor = Db.getContacts(this);// getContacts();
 		Log.i(tag, "There are " + contactsCursor.getCount() + " contacts");
 		int numContacts = 0;
 		Cursor bookCursor = Db.getBook(this, id);
 		Log.i(tag, "There are " + bookCursor.getCount()
 				+ " contacts sharing this group");
 		IntCursorJoiner joiner = new IntCursorJoiner(contactsCursor,
 				new String[] { ContactsContract.Contacts._ID }, bookCursor,
 				new String[] { BookTable.CONTACTID });
 
 		for (CursorJoiner.Result joinerResult : joiner) {
 			switch (joinerResult) {
 			case BOTH: // handle case where a row with the same key is in both
 						// cursors
 				numContacts++;
 				break;
 			}
 		}
 		Log.i(tag, "Sharing with " + numContacts + " contacts");
 		return numContacts;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		if (owner != null)
 			return false;
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.group_menu, menu);
 		Log.i(tag, "Menu Displayed");
 		return true;	
 
 	}
 
 	/*@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		m_cursor.moveToPosition(position);
 		String contactId = m_cursor.getString(m_cursor
 				.getColumnIndex(ContactsContract.Contacts._ID));
 		Log.i(tag, "position is: " + position + ", contactId: " + contactId
 				+ ", name: " + name);
 		callFromGroup(contactId);
 	}*/
 
 
 	private void queryGroup() {
 		if (owner == null) //
 			setTitle("Group: " + name + " (" + numContacts()
 					+ " contacts sharing with)");
 		else
 			setTitle("Group: " + name + " (" + owner + " is sharing)");
 		groupCursor = Db.getContactsInGroup(id, this.getContentResolver());
 		getContactsFromGroupCursor("");
 		String[] fields = new String[] { ContactsContract.Contacts.DISPLAY_NAME };
 		adapter = new ImageCursorAdapter(this, R.layout.contact_entry,
 				m_cursor, ids, fields, new int[] { R.id.contact_name });
 
 		adapter.setStringConversionColumn(m_cursor
 				.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
 
 		adapter.setFilterQueryProvider(new FilterQueryProvider() {
 
 			public Cursor runQuery(CharSequence constraint) {
 				String partialItemName = null;
 				if (constraint != null) {
 					partialItemName = constraint.toString();
 				}
 				getContactsFromGroupCursor(partialItemName);
 				adapter.setCursor(m_cursor);
 				adapter.setIds(ids);
 				return m_cursor;
 			}
 		});
 
 		getListView().setAdapter(adapter);
 
 		Log.i(tag, "There are " + m_cursor.getCount()
 				+ " contacts in this group");
 
 	}
 
 	private void getContactsFromGroupCursor(String search) {
 		ids = new ArrayList<String>();
 		Log.i(tag, "There are " + groupCursor.getCount()
 				+ " contacts in data for groupId: " + id);
 		Cursor contactsCursor = Db.getContacts(this, search);
 		Log.i(tag, "There are " + contactsCursor.getCount()
 				+ " contacts matching " + search);
 
 		IntCursorJoiner joiner = new IntCursorJoiner(contactsCursor,
 				new String[] { ContactsContract.Contacts._ID }, groupCursor,
 				new String[] { ContactsContract.Data.CONTACT_ID });
 		m_cursor = new MatrixCursor(new String[] {
 				ContactsContract.Contacts._ID,
 				ContactsContract.Contacts.DISPLAY_NAME }, 10);
 
 		List<ContactRow> rows = new ArrayList<ContactRow>();
 		for (CursorJoiner.Result joinerResult : joiner) {
 			switch (joinerResult) {
 			case BOTH: // handle case where a row with the same key is in both
 						// cursors
 				String contactId = contactsCursor.getString(contactsCursor
 						.getColumnIndex(ContactsContract.Contacts._ID));
				String rawContactId = groupCursor.getString(groupCursor
						.getColumnIndex(Data.RAW_CONTACT_ID));
				
 				String name = contactsCursor
 						.getString(contactsCursor
 								.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				Log.i(tag, "Contact id: " + contactId + ", rawContactId: "+rawContactId+", name: " + name);
 
 				
 				/*byte[] photo = null;
 
 				Uri photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(contactId));
 
 			    Bitmap photoBitmap;
 			    ContentResolver cr = getContentResolver();
 			    InputStream is = ContactsContract.Contacts.openContactPhotoInputStream(cr, photoUri);
 
 			    photoBitmap = BitmapFactory.decodeStream(is);
 				*/
 				/*Uri contactUri = ContentUris.withAppendedId(
 						Contacts.CONTENT_URI, Long.parseLong(contactId));
 				Uri photoUri = Uri.withAppendedPath(contactUri,
 						Contacts.Photo.CONTENT_DIRECTORY);
 				Cursor cursor = getContentResolver()
 						.query(photoUri,
 								new String[] { ContactsContract.CommonDataKinds.Photo.PHOTO },
 								null, null, null);
 				try {
 					if (cursor.moveToFirst()) {
 						photo = cursor.getBlob(0);
 						if (photo != null)
 							Log.i(tag, "name is: " + name + ", photo data is "
 									+ photo.length + " bytes");
 					}
 				} finally {
 					cursor.close();
 				}*/
 				if (name != null)
 					rows.add(new ContactRow(contactId, name));
 				break;
 			}
 		}
 		Collections.sort(rows);
 		Log.i(tag, "There are " + rows.size() + " contacts matching " + search);
 		for (ContactRow row : rows) {
 			m_cursor.addRow(new String[] { row.id, row.name });
 			Log.i(tag, "Adding row[" + row.id + "] = " + row.name);
 			ids.add(row.id);
 		}
 
 	}
 
 	ListView mGroupList;
 	boolean mShowInvisible = false;
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.delete_group:
 			showDeleteGroupDialog();
 			break;
 		case R.id.add_contacts:
 			Log.i(tag, "Option menu");
 			showAddContacts();
 			break;
 		case R.id.share_group:
 			showShareGroup();
 			break;
 
 		}
 		return true;
 		/*
 		 * case R.id.help: showHelp(); return true; default: return
 		 * super.onOptionsItemSelected(item);
 		 */
 	}
 
 	private void showShareGroup() {
 		Intent i = new Intent(GroupActivity.this, SharingWithActivity.class);
 		i.putExtra("id", id);
 		i.putExtra("name", name);
 		startActivityForResult(i, SHARE_GROUP);
 	}
 
 	static int ADD_CONTACTS = 1, SHARE_GROUP = 2;
 
 	private void showAddContacts() {
 		Log.i(tag, "Add Contact activity");
 		Intent i = new Intent(GroupActivity.this, AddContactsActivity.class);
 		i.putExtra("id", id);
 		i.putExtra("name", name);
 		startActivityForResult(i, ADD_CONTACTS);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		queryGroup();
 	}
 
 	private void showDeleteGroupDialog() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage("Are you sure you want to delete this group?")
 				.setCancelable(false)
 				.setPositiveButton("Yes",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								deleteGroup();
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
 
 	private void deleteGroup() {
 		System.out.println("delete group: " + id + ", " + name);
 		String[] args = { id };
 		try {
 			int b = getContentResolver().delete(
 					ContactsContract.Groups.CONTENT_URI, "_ID=?", args);
 
 			Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
 			// notify registered observers that a row was updated
 			getContentResolver().notifyChange(
 					ContactsContract.Groups.CONTENT_URI, null);
 
 		} catch (Exception e) {
 			Log.v(tag, e.getMessage(), e);
 			Toast.makeText(this, tag + " Delete Failed", Toast.LENGTH_LONG)
 					.show();
 		}
 		setResult(RESULT_OK, null);
 		finish();
 	}
 }
