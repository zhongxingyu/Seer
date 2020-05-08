 package com.android.todolist;
 
 import java.sql.Date;
 import java.util.ArrayList;
 
 import com.android.todolist.ToDoItem.ToDoPriority;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnKeyListener;
 import android.widget.AdapterView;
 import android.widget.EditText;
 import android.widget.ListView;
 
 public class ToDoListActivity extends Activity {
 	private static final String TEXT_ENTRY_KEY = "TEXT_ENTRY_KEY";
 	private static final String ADDING_ITEM_KEY = "ADDING_ITEM_KEY";
 	private static final String EDITING_ITEM_KEY = "EDITING_ITEM_KEY";
 	private static final String EDITING_ITEM_MEMBER = "EDITING_ITEM_MEMBER";
 	private static final String SELECTED_INDEX_KEY = "SELECTED_INDEX_KEY";
 	
 	static final private int ADD_NEW_TODO = Menu.FIRST;
 	static final private int EDIT_TODO = Menu.FIRST +1;
 	static final private int REMOVE_TODO = Menu.FIRST + 2;
 	
 	private ArrayList<ToDoItem> todoItems;
 	private ToDoItemAdapter aa;
 	private ListView myListView;
 	private EditText myEditText;
 	
 	ToDoListDBAdapter toDoDBAdapter;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         // Inflate view
         setContentView(R.layout.main);
         
         // Get references to UI widget
         myListView = (ListView)findViewById(R.id.MyListView);
         myEditText = (EditText)findViewById(R.id.MyEditText);
         
         // Create the array list of to do items
         todoItems = new ArrayList<ToDoItem>();
         // Create the array adapter to bind the array to the ListView
         int resID = R.layout.todolist_item;
         aa = new ToDoItemAdapter(this, resID, todoItems);
         // Bind the array adapter to the list view
         myListView.setAdapter(aa);
         
         myEditText.setOnKeyListener(new OnKeyListener() {
         	public boolean onKey(View v, int keyCode, KeyEvent event) {
         		if (event.getAction() == KeyEvent.ACTION_DOWN)
         			if (keyCode == KeyEvent.KEYCODE_ENTER) {
         				if (myEditText.getText().length() != 0) {
         					ToDoItem newItem = new ToDoItem(myEditText.getText().toString());
         					if (!editing) {
         						toDoDBAdapter.insertTask(newItem);
         					}
         					else {
         						toDoDBAdapter.updateTask(editingSqlId, newItem);
         					}
         					updateArray();
         					myEditText.setText(R.string.empty);
         					aa.notifyDataSetChanged();
         				}
         				cancelAdd();
         				return true;
         			}
         		return false;
         	}
         });
         
         registerForContextMenu(myListView);
         restoreUIState();
         
         toDoDBAdapter = new ToDoListDBAdapter(this);
         
         // Open or create the database
         toDoDBAdapter.open();
         
         populateTodoList();
     }
     
     Cursor toDoListCursor;
     
     private void populateTodoList() {
     	// Get all the to do list items from the database.
     	toDoListCursor = toDoDBAdapter.getAllToDoItemsCursor();
     	startManagingCursor(toDoListCursor);
     	// Update the array.
     	updateArray();
     }
     
     private void updateArray() {
     	toDoListCursor.requery();
     	todoItems.clear();
     	if (toDoListCursor.moveToFirst())
     		do {
     			long sqlid = toDoListCursor.getLong(toDoListCursor.getColumnIndex(ToDoListDBAdapter.KEY_ID));
     			String task = toDoListCursor.getString(toDoListCursor.getColumnIndex(ToDoListDBAdapter.KEY_TASK));
     			int priority = toDoListCursor.getInt(toDoListCursor.getColumnIndex(ToDoListDBAdapter.KEY_PRIORITY));
     		    long created = toDoListCursor.getLong(toDoListCursor.getColumnIndex(ToDoListDBAdapter.KEY_CREATION_DATE));
 
     		    if (!(editing && (editingSqlId == sqlid))) {
     		    	ToDoItem newItem = new ToDoItem(sqlid, task, ToDoPriority.NORMAL_PRIORITY, new Date(created));
     		    	todoItems.add(0, newItem);
     		    }
     	} while(toDoListCursor.moveToNext());
     	aa.notifyDataSetChanged();
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	super.onCreateOptionsMenu(menu);
     	
     	// Create and add new menu items
     	MenuItem itemAdd = menu.add(0, ADD_NEW_TODO, Menu.NONE, R.string.add_new);
     	MenuItem itemEdi = menu.add(0, EDIT_TODO, Menu.NONE, R.string.edit);
     	MenuItem itemRem = menu.add(0, REMOVE_TODO, Menu.NONE, R.string.remove);
     	
     	// Assign icons
     	itemAdd.setIcon(android.R.drawable.ic_menu_add);
     	itemEdi.setIcon(android.R.drawable.ic_menu_edit);
     	itemRem.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
     	
     	return true;
     }
     
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
     	super.onCreateContextMenu(menu, v, menuInfo);
     	
     	menu.setHeaderTitle("Selected To Do Item");
     	menu.add(0, EDIT_TODO, Menu.NONE, R.string.edit);
     	menu.add(0, REMOVE_TODO, Menu.NONE, R.string.remove);
     }
     
     private boolean addingNew = false;
     private boolean editing = false;
     private long editingSqlId = -1;
     
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
     	super.onPrepareOptionsMenu(menu);
     	
     	int idx = myListView.getSelectedItemPosition();
     	
     	String removeTitle = getString(addingNew ? R.string.cancel : R.string.remove);
     	
    	MenuItem editItem = menu.findItem(EDIT_TODO);
    	MenuItem removeItem = menu.findItem(REMOVE_TODO);
    	editItem.setTitle(R.string.edit);
    	removeItem.setTitle(removeTitle);
    	editItem.setVisible(addingNew == false && idx > -1);
    	removeItem.setVisible(addingNew || idx > -1);
     	
     	return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	super.onOptionsItemSelected(item);
     	
     	myListView.requestFocus();
     	
     	int index = myListView.getSelectedItemPosition();
     	
     	switch (item.getItemId()) {
     		case (ADD_NEW_TODO): {
     			if (editing) {
     				myEditText.setText("");
     				cancelAdd();
     			}
     			
     			addNewItem();
     			return true;
     		}
     		case (EDIT_TODO): {
     			editItem(index);
     			return true;
     		}
     		case (REMOVE_TODO): {
     			if (addingNew) {
     				cancelAdd();
     			}
     			else {
     				removeItem(index);
     			}
     			return true;
     		}
     	}
     	
     	return false;
     }
     
     @Override
     public boolean onContextItemSelected(MenuItem item) {
     	super.onContextItemSelected(item);
     	
     	boolean edit = false;
     	
     	switch (item.getItemId()) {
     		case (EDIT_TODO):
     			edit = true;
     		case (REMOVE_TODO): {
     			AdapterView.AdapterContextMenuInfo menuInfo;
     			menuInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
     			int index = menuInfo.position;
     			
     			if (edit) {
     				editItem(index);
     				return true;
     			}
     		
     			removeItem(index);
     			return true;
     		}
     	}
     	
     	return false;
     }
     
    private void cancelAdd() {
    	addingNew = false;
    	editing = false;
    	myEditText.setText(R.string.empty);
    	myEditText.setVisibility(View.GONE);
    	updateArray();
    }
    
     private void addNewItem() {
     	addingNew = true;
     	myEditText.setVisibility(View.VISIBLE);
     	myEditText.requestFocus();
     }
     
     private void editItem(int index) {
 		ToDoItem item = todoItems.get(index);
 		myEditText.setText(item.getTask());
 		editingSqlId = item.getId();
 		
 		todoItems.remove(index);
 		
     	if (addingNew)
     		cancelAdd();
     	
     	addNewItem();
     	editing = true;
     }
     
     private void removeItem(int index) {
     	
     	ToDoItem item = todoItems.get(index);
     	toDoDBAdapter.removeTask(item.getId());
     	updateArray();
     }
     
     @Override
     protected void onPause() {
     	super.onPause();
     	
     	// Get the activity preference object
     	SharedPreferences uiState = getPreferences(0);
     	// Get the preferences editor
     	SharedPreferences.Editor editor = uiState.edit();
     	
     	// Add the UI state preference values
     	editor.putString(TEXT_ENTRY_KEY, myEditText.getText().toString());
     	editor.putBoolean(ADDING_ITEM_KEY, addingNew);
     	editor.putBoolean(EDITING_ITEM_KEY, editing);
     	editor.putLong(EDITING_ITEM_MEMBER, editingSqlId);
     	// Commit the preferences
     	editor.commit();
     }
     
     private void restoreUIState() {
     	// Get the activity preferences object
     	SharedPreferences settings = getPreferences(Activity.MODE_PRIVATE);
     	
     	// Read the UI state values, specifying default values
     	String text = settings.getString(TEXT_ENTRY_KEY, "");
     	Boolean adding = settings.getBoolean(ADDING_ITEM_KEY, false);
     	Boolean edit = settings.getBoolean(EDITING_ITEM_KEY, false);
     	Long editSqlId = settings.getLong(EDITING_ITEM_MEMBER, -1);
     	
     	// Restore the UI to the previous state
     	if (adding) {
     		addNewItem();
     		myEditText.setText(text);
     	}
     	
     	if (edit) {
     		editing = true;
     		editingSqlId = editSqlId;
     	}
     }
     
     @Override
     public void onSaveInstanceState(Bundle savedInstanceState) {
     	savedInstanceState.putInt(SELECTED_INDEX_KEY, myListView.getSelectedItemPosition());
     	
     	super.onSaveInstanceState(savedInstanceState);
     }
     
     @Override
     public void onRestoreInstanceState(Bundle savedInstanceState) {
     	int pos = -1;
     	
     	if (savedInstanceState != null)
     		if (savedInstanceState.containsKey(SELECTED_INDEX_KEY))
     			pos = savedInstanceState.getInt(SELECTED_INDEX_KEY, -1);
     	
     	myListView.setSelection(pos);
     }
     
     @Override
     public void onDestroy() {
     	super.onDestroy();
     	
     	// Close the database
     	toDoDBAdapter.close();
     }
 }
