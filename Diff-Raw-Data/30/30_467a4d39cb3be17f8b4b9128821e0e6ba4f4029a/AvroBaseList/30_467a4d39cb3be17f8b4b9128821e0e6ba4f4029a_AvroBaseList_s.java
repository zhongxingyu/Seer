 /*
  * Copyright (C) 2007 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package interdroid.vdb.avro.view;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.avro.Schema;
 import org.apache.avro.Schema.Field;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import interdroid.vdb.Actions;
 import interdroid.vdb.avro.R;
 import interdroid.vdb.content.EntityUriMatcher;
 import interdroid.vdb.content.EntityUriMatcher.UriMatch;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.ComponentName;
 import android.content.ContentUris;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Toast;
 
 /**
  * Displays a list of avros. Will display avros from the {@link Uri}
  * provided in the intent if there is one, otherwise defaults to displaying the
  * contents of the {@link NotePadProvider}
  */
 public class AvroBaseList extends ListActivity {
 	private static final Logger logger = LoggerFactory.getLogger(AvroBaseList.class);
 
 	// Menu item ids
 	public static final int MENU_ITEM_DELETE = Menu.FIRST;
 	public static final int MENU_ITEM_INSERT = Menu.FIRST + 1;
 	public static final int MENU_ITEM_COMMIT = Menu.FIRST + 2;
 	public static final int MENU_ITEM_EDIT = Menu.FIRST + 3;
 
 	/**
 	 * The columns we are interested in from the database
 	 */
 	private String[] PROJECTION;
 
 	private Uri mBranchUri;
 	private boolean mReadOnly = true;
 	private Schema mSchema;
 
 	public AvroBaseList() {
 		logger.debug("Constructed AvroBaseList: " + this + ":");
 	}
 
 	protected AvroBaseList(Schema schema, Uri defaultUri) {
 		setup(schema, defaultUri);
 	}
 
 	protected AvroBaseList(Schema schema) {
 		this(schema, null);
 	}
 
 	private void setup(Schema schema, Uri defaultUri) {
 		if (schema.getType() != Schema.Type.RECORD) {
 			throw new RuntimeException("Invalid base type. Must be a record.");
 		}
 		mSchema = schema;
 
 		if (defaultUri == null) {
 			mBranchUri = Uri.parse("content://" + schema.getNamespace() + "/branches/master/"+ schema.getName());
 			logger.debug("Using default URI: {}", mBranchUri);
 		} else {
 			mBranchUri = defaultUri;
 		}
 
 		List<Field> fields = schema.getFields();
 		ArrayList<String> listFields = new ArrayList<String>();
 		// SimpleCursorAdapter requires the _id field be in the PROJECTION.
 		// Since this is synthetic we add it here.
 		listFields.add("_id");
 		for (Field field : fields) {
			if (field.getProp("ui.list") == null || !field.getProp("ui.list").equals(Boolean.FALSE)) {
 				switch (field.schema().getType()) {
 				// TODO: Should build projection which can handle more things.
 				case STRING:
 					logger.debug("List field includes: {}", field.name());
 					listFields.add(field.name());
 					break;
 				default:
 					logger.debug("List does not include: {}", field.name());
 				}
 			}
 		}
 		PROJECTION = listFields.toArray(new String[listFields.size()]);
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Start the GIT service in the background.
 		startService(new Intent("interdroid.vdb.GIT_SERVICE"));
 
 		setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);
 
 		// If no data was given in the intent (because we were started
 		// as a MAIN activity), then use our default content provider.
 		Intent intent = getIntent();
 		if (intent.getData() == null) {
 			intent.setData(mBranchUri);
 		} else {
 			// We need a uri and a schema in the intent extras then.
 			Uri defaultUri = intent.getData();
 			if (defaultUri == null) {
 				throw new IllegalArgumentException("A Uri is required.");
 			}
 			String schemaJson = intent.getStringExtra(AvroBaseEditor.SCHEMA);
 			if (schemaJson == null) {
 				throw new IllegalArgumentException("A Schema is required.");
 			}
 			Schema schema = Schema.parse(schemaJson);
 			logger.debug("Setting up: {} {}", defaultUri, schema);
 			setup(schema, defaultUri);
 		}
 
 		UriMatch match = EntityUriMatcher.getMatch(intent.getData());
 		if (!match.isCheckout()) {
 			Toast.makeText(this, "Invalid URI.", Toast.LENGTH_LONG).show();
 			finish();
 		}
 		// In case it's a branch/remote/repository, add the table
 		if (match.entityName == null) {
 			match.entityName = mSchema.getName();
 			getIntent().setData(match.buildUri());
 		}
 
 		// For write checkouts we hold the branch Uri for launching the commit activity
 		mReadOnly = match.isReadOnlyCheckout();
 		mBranchUri = match.getCheckoutUri();
 		if (mReadOnly) {
 			Toast.makeText(this, "Read only", Toast.LENGTH_LONG).show();
 		}
 
 		// Inform the list we provide context menus for items
 		getListView().setOnCreateContextMenuListener(this);
 
 		if (intent.getAction() == Intent.ACTION_PICK) {
 			logger.debug("In pick mode.");
 			// We are canceled if they back out without picking.
 			setResult(RESULT_CANCELED);
 		}
 
 		new InitTask().execute(getIntent());
 	}
 
 	private class InitTask extends AsyncTask<Object, Void, Void> {
 		private ProgressDialog d;
 
 		protected void onPreExecute() {
 			d = ProgressDialog.show(AvroBaseList.this, "Loading..", "Loading...", true, false);
 		}
 
 		protected void onPostExecute(Void v) {
 			d.dismiss();
 		}
 
 		@Override
 		protected Void doInBackground(Object... params) {
 
 			// Perform a managed query. The Activity will handle closing and requerying the cursor
 			// when needed.
 			final Cursor cursor = managedQuery(((Intent)params[0]).getData(), PROJECTION, null, null, mSchema.getProp("ui.default_sort"));
 
 
 			AvroBaseList.this.runOnUiThread(new Runnable() { public void run() {
 				// Used to map entries from the database to views
 				// TODO: Need a complex adapter that comes from the View Factory.
 				final SimpleCursorAdapter adapter = new SimpleCursorAdapter(AvroBaseList.this, R.layout.avro_list_item, cursor,
 						new String[] { PROJECTION[1] }, new int[] { android.R.id.text1 });
 				setListAdapter(adapter);}});
 			return null;
 		}
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 
 		if (!mReadOnly) {
 			menu.add(0, MENU_ITEM_INSERT, 0, "Insert " + mSchema.getName())
 			.setShortcut('3', 'a')
 			.setIcon(android.R.drawable.ic_menu_add);
 
 			menu.add(1, MENU_ITEM_COMMIT, 0, "Commit")
 			.setShortcut('9', 'c')
 			.setIcon(android.R.drawable.ic_menu_save);
 		}
 		// Generate any additional actions that can be performed on the
 		// overall list.  In a normal install, there are no additional
 		// actions found here, but this allows other applications to extend
 		// our menu with their own actions.
 		Intent intent = new Intent(null, getIntent().getData());
 		intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
 		menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
 				new ComponentName(this, AvroBaseList.class), null, intent, 0, null);
 
 		return true;
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		super.onPrepareOptionsMenu(menu);
 		final boolean haveItems = getListAdapter().getCount() > 0;
 
 		// If there are any notes in the list (which implies that one of
 		// them is selected), then we need to generate the actions that
 		// can be performed on the current selection.  This will be a combination
 		// of our own specific actions along with any extensions that can be
 		// found.
 		if (haveItems && !mReadOnly) {
 			// This is the selected item.
 			Uri uri = ContentUris.withAppendedId(getIntent().getData(), getSelectedItemId());
 
 			// Build menu...  always starts with the EDIT action...
 			Intent[] specifics = new Intent[1];
 			specifics[0] = new Intent(Intent.ACTION_EDIT, uri);
 			MenuItem[] items = new MenuItem[1];
 
 			// ... is followed by whatever other actions are available...
 			Intent intent = new Intent(null, uri);
 			intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
 			menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0, null, specifics, intent, 0,
 					items);
 
 			// Give a shortcut to the edit action.
 			if (items[0] != null) {
 				items[0].setShortcut('1', 'e');
 			}
 		} else {
 			menu.removeGroup(Menu.CATEGORY_ALTERNATIVE);
 		}
 
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case MENU_ITEM_INSERT:
 			// Launch activity to insert a new item
 			startActivity(new Intent(Intent.ACTION_INSERT, getIntent().getData()));
 			return true;
 		case MENU_ITEM_COMMIT:
 			startActivity(new Intent(Actions.ACTION_COMMIT, mBranchUri));
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
 		AdapterView.AdapterContextMenuInfo info;
 		try {
 			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
 		} catch (ClassCastException e) {
 			logger.error("bad menuInfo", e);
 			return;
 		}
 
 		Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
 		if (cursor == null) {
 			// For some reason the requested item isn't available, do nothing
 			logger.debug("Requested item not avaialble.");
 			return;
 		}
 
 		// Setup the menu header
 		menu.setHeaderTitle(cursor.getString(cursor.getColumnIndex(PROJECTION[1])));
 
 		if (!mReadOnly) {
 			// Add a menu item to delete the item
 			menu.add(0, MENU_ITEM_DELETE, 0, "Delete " + mSchema.getName());
 			menu.add(0, MENU_ITEM_EDIT, 1, "Edit " + mSchema.getName());
 		}
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterView.AdapterContextMenuInfo info;
 		try {
 			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
 		} catch (ClassCastException e) {
 			logger.error("bad menuInfo", e);
 			return false;
 		}
 
 		switch (item.getItemId()) {
 		case MENU_ITEM_DELETE: {
 			// Delete the note that the context menu is for
 			Uri noteUri = ContentUris.withAppendedId(getIntent().getData(), info.id);
 			getContentResolver().delete(noteUri, null, null);
 			return true;
 		}
 		case MENU_ITEM_EDIT: {
 			Uri noteUri = ContentUris.withAppendedId(getIntent().getData(), info.id);
 			startActivity(new Intent(Intent.ACTION_EDIT, noteUri));
 		}
 		}
 		return false;
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
 
 		String action = getIntent().getAction();
 		if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
 			// The caller is waiting for us to return a note selected by
 			// the user.  The have clicked on one, so return it now.
 			logger.debug("Setting Result URI: {}", getContentResolver().getType(uri));
 			setResult(RESULT_OK, new Intent().setData(uri));
 		} else {
 			// Launch activity to view/edit the currently selected item
 			logger.debug("Launching edit for: {}", getContentResolver().getType(uri));
 			startActivity(new Intent(Intent.ACTION_EDIT, uri));
 		}
 	}
 }
