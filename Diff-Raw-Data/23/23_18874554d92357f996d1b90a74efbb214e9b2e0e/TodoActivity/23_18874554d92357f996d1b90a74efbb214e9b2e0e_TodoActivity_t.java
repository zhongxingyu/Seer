 package com.manning.gia.todo.activity;
 
 import android.app.ListActivity;
 import android.app.LoaderManager;
 import android.content.CursorLoader;
 import android.content.Intent;
 import android.content.Loader;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
 
 import com.manning.gia.todo.R;
 import com.manning.gia.todo.model.Item;
 import com.manning.gia.todo.repository.ItemRepository;
 
 /**
  * Main Todo Activity.
  * 	Loads the data for listing.
  * 	Handles events from listing.
  * @author jkeam
  *
  */
 public class TodoActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
 	private static final int DELETE_ID = Menu.FIRST + 1;
 	private SimpleCursorAdapter adapter;
 
 	//Cursor Loader Methods
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.todo_list);
 		this.getListView().setDividerHeight(2);
 
 		setupAdapter();
 		registerForContextMenu(getListView());
 	}
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
 		return new CursorLoader(this, ItemRepository.getContentUri(), ItemRepository.getAllColumns(), null, null, null);
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
 		adapter.swapCursor(cursor);
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> loader) {
 		adapter.swapCursor(null);
 	}
 
 	//Context Menu Methods
 	@Override
 	public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenuInfo contextMenuInfo) {
 		super.onCreateContextMenu(contextMenu, view, contextMenuInfo);
 		contextMenu.add(Menu.NONE, DELETE_ID, Menu.NONE, R.string.delete_item_text);
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case DELETE_ID:
 			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 			ItemRepository.delete(getContentResolver(), info.id);
 			setupAdapter();
 			return true;
 		}
 		return super.onContextItemSelected(item);
 	}
 
 	//Override Single Click
 	@Override
 	protected void onListItemClick(ListView listView, View view, int position, long id) {
 		super.onListItemClick(listView, view, position, id);
 		Intent i = new Intent(this, TodoDetailActivity.class);
 		i.putExtra(ItemRepository.getContentItemType(), ItemRepository.getUri(id));
 		startActivity(i);
 	}
 
 	//Custom Event Handler Methods
 	public void onClickAdd(View view) {
 		SimpleCursorAdapter adapter = (SimpleCursorAdapter) getListAdapter();
 		switch (view.getId()) {
 		case R.id.add:
 			EditText editText = (EditText) findViewById(R.id.newText);
 			Item item = new Item(editText.getText().toString());
			String errorMessage = item.validate();
			if (errorMessage != null) {
				showValidation(errorMessage);
			} else {
				ItemRepository.save(getContentResolver(), item);
				editText.setText("");
			}
 			break;
 		}
 
 		adapter.notifyDataSetChanged();
 	}
 
 	//Helper Methods
 	protected void setupAdapter() {
 		getLoaderManager().initLoader(0, null, this);
 		adapter = new SimpleCursorAdapter(this, R.layout.todo_row, null, new String[]{ItemRepository.getNameColumn()}, new int[]{R.id.itemText}, 0);
 		setListAdapter(adapter);
 	}
	
	private void showValidation(String errorMessage) {
		Toast.makeText(TodoActivity.this, errorMessage, Toast.LENGTH_LONG).show();
	}
 }
