 package com.tonyjhuang.listpop;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
 import android.widget.Toast;
 
 public class StartActivity extends Activity {
 	private static final int ADD_ACTIVITY = 1;
 	private static final int PRESETS_ACTIVITY = ADD_ACTIVITY + 1;
 	private static final int DELETE_ID = Menu.FIRST;
 
 	private DbAdapter mDbA;
 	private ListView mListView;
 	private BaseAdapter adapter = null;
 	private Cursor c;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.start);
 
 		// Start SQLite database.
 		mDbA = new DbAdapter(this);
 		mDbA.open();
 
 		// Initialize cursor (result set of all rows from SQLite database).
 		c = mDbA.fetchAll();
 
 		// Register listview for context menu and ItemClickListener.
 		mListView = (ListView) findViewById(R.id.listselection);
 		hookUpItemClickListener();
 		registerForContextMenu(mListView);
 
 		// Populate listview with a SimpleCursorAdapter.
 		fillData();
 	}
 
 	private void hookUpItemClickListener() {
 		mListView.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> list, View view,
 					int position, long id) {
 				//Fetch row requested from database.
 				Cursor result = mDbA.fetchListItem(id);
 				
 				//Query cursor for codified String.
 				final int KEY_LIST_COLUMN_INDEX = result
 						.getColumnIndex(DbAdapter.LIST);
 				String pList = result.getString(KEY_LIST_COLUMN_INDEX);
 				
 				//Query cursor for name of list.
 				final int KEY_LIST_HEADER_COLUMN_INDEX = result
 						.getColumnIndex(DbAdapter.LIST_HEADER);
 				String pName = result.getString(KEY_LIST_HEADER_COLUMN_INDEX);
 				
 				//Start PopActivity with codified String.
 				Intent i = new Intent(StartActivity.this, PopActivity.class);
 				i.putExtra(DbAdapter.LIST_HEADER, pName);
 				i.putExtra(DbAdapter.LIST, pList);
 				startActivity(i);
 
 			}
 		});
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		menu.add(0, DELETE_ID, 0, R.string.menu_delete);
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case DELETE_ID:
 			// Delete row from database.
 			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
 					.getMenuInfo();
 			mDbA.deleteListItem(info.id);
 
 			// Repopulate the activity's listview.
 			fillData();
 			return true;
 		}
 		return super.onContextItemSelected(item);
 	}
 
 	// Initialize adapter with a SimpleCursorAdapter and populate listview.
 	@SuppressWarnings("deprecation")
 	private void fillData() {
 		updateCursor();
 		startManagingCursor(c);
 
 		String[] from = new String[] { DbAdapter.LIST_HEADER };
 		int[] to = new int[] { R.id.listitem };
 		adapter = new SimpleCursorAdapter(StartActivity.this,
 				R.layout.list_item, c, from, to);
 		mListView.setAdapter(adapter);
 	}
 
 	// If adapter is a CustomCursorAdapter, mutate to SimpleCursorAdapter.
 	// Otherwise, mutate to CustomCursorAdapter.
 	private void toggleEdit() {
 		if (mDbA.isEmpty()) {
 			Toast t = Toast.makeText(this, "Add some lists!", Toast.LENGTH_SHORT);
 			t.show();
 		} else {
 			if (adapter instanceof CustomCursorAdapter)
 				fillData();
 			else {
 				updateCursor();
 				adapter = new CustomCursorAdapter(StartActivity.this, c);
 				mListView.setAdapter(adapter);
 			}
 		}
 	}
 
 	// Update cursor result set.
 	private void updateCursor() {
 		c = mDbA.fetchAll();
 		c.moveToFirst();
 	}
 
 	// Called from CustomCursorAdapter. Deletes inputted row from database
 	// and refreshes the cursor & adapter.
 	public void deleteFromAdapter(long rowid) {
 		mDbA.deleteListItem(rowid);
 		updateCursor();
 		adapter = new CustomCursorAdapter(StartActivity.this, c);
 		mListView.setAdapter(adapter);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch (resultCode) {
 		case ADD_ACTIVITY:
 			// Fall through to next case.
 		case PRESETS_ACTIVITY:
 			// Get data from intent, and enter it as a new row in the database.
 			String newListHeader = data.getStringExtra("list_header");
 			ArrayList<String> newList = data.getStringArrayListExtra("list");
 			mDbA.enterList(newListHeader, newList);
 
 			// Then repopulate listview.
 			fillData();
 			break;
 		}
 	}
 
 	// If adapter is a CustomCursorAdapter, mutate to SimpleCusorAdapter.
 	// Otherwise, kill the activity.
 	@Override
 	public void onBackPressed() {
 		if (adapter instanceof CustomCursorAdapter) {
 			fillData();
 		} else {
 			finish();
 		}
 
 	}
 
 	// Make sure you close the database to liberate system resources!
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		mDbA.close();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.action_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_edit:
 			toggleEdit();
 			break;
 
 		case R.id.custom:
 			Intent i = new Intent(StartActivity.this, AddActivity.class);
 			startActivityForResult(i, ADD_ACTIVITY);
 			break;
 
 		case R.id.preset:
 			Intent j = new Intent(StartActivity.this, PresetActivity.class);
 			startActivityForResult(j, ADD_ACTIVITY);
 			break;
 		}
 
 		return true;
 	}
 
 }
