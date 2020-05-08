 package com.davecoss.android.todo;
 
 import java.util.List;
 
 import org.json.JSONException;
 
 import com.davecoss.android.lib.Notifier;
 import com.davecoss.android.lib.utils;
 import com.davecoss.android.todo.ListDB;
 import android.os.Bundle;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.database.SQLException;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class TODO extends ListActivity {
 	public static final String IMPORT_FILENAME = "todo_list.json";
 	public static final String EXPORT_FILENAME = "todo_list_export.json";
 	public static final int SHOW_ITEM_EDITOR = 42;
 	public static final int RESULT_OK = 0;
 	ListDB dbconn;
 	Notifier notifier;
 	String last_removed;
 	String curr_category;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_todo);
         notifier = new Notifier(this.getApplicationContext());
         last_removed = null;
         curr_category = null;
         dbconn = create_db();
         
         // Use the SimpleCursorAdapter to show the
         // elements in a ListView
         rebuild_adapter();
         
         Button add_button = (Button) findViewById(R.id.add);
         add_button.setFocusableInTouchMode(true);
         add_button.requestFocus();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_todo, menu);
         return true;
     }
     
     private ArrayAdapter<TodoObject> rebuild_adapter()
     {
     	List<TodoObject> todolist = dbconn.getList(curr_category);
         
     	ArrayAdapter<TodoObject> adapter = new ArrayAdapter<TodoObject>(this,
             android.R.layout.simple_list_item_1 , todolist);
         setListAdapter(adapter);
         return adapter;
     }
     
     public ListDB create_db()
     {
     	ListDB newdbconn = new ListDB(this.getApplicationContext());
     	return newdbconn;
     }
     
     public void add_todo(String message)
     {
     	@SuppressWarnings("unchecked")
 		ArrayAdapter<TodoObject> adapter = (ArrayAdapter<TodoObject>) getListAdapter();
     	if(message.length() == 0)// ignore empty strings
     		return;
 		String category = null;
 		if(message.contains(":"))
 		{
 			int loc = message.indexOf(":");
 			category = message.substring(0, loc).trim();
 			message = message.substring(loc+1).trim();
 		}
     	try
     	{
     		TodoObject new_item = this.dbconn.add_message(message, category);
     		adapter.add(new_item);
     		touch_adapter(adapter);
     	}
     	catch(SQLException sqle)
     	{
    		Log.e("TODO","Could not add message.\n" + sqle.getMessage());
     	}
 		
     }
     
     public void onClick(View view)
     {
     	switch (view.getId())
     	{
     	case R.id.add:
     		EditText new_todo = (EditText) findViewById(R.id.edit_box);
         	String message = new_todo.getText().toString().trim();
         	add_todo(message);
         	new_todo.setText("");
     		break;
     	default:
     		break;
     	}
     }
     
     @SuppressWarnings("unchecked")
 	private void touch_adapter(ArrayAdapter<TodoObject> adapter)
     {
     	ArrayAdapter<TodoObject> _adapter = adapter;
     	if(_adapter == null)
     	{
 	    	_adapter = (ArrayAdapter<TodoObject>) getListAdapter();
     	}
     	_adapter.notifyDataSetChanged();
     }
     
     protected void onListItemClick (ListView l, View view, int position, long id)
     {
     	TodoObject todo = (TodoObject) this.getListAdapter().getItem(position);
     	
     	Intent item_editor_activity = new Intent(getBaseContext(), ListItemEditor.class);
     	try {
 			item_editor_activity.putExtra(ListItemEditor.TODO_MESSAGE, todo.toJSON().toString());
 			startActivityForResult(item_editor_activity,SHOW_ITEM_EDITOR);
 		} catch (JSONException jsone) {
 			notifier.log_exception("TODO", "Could not get todo item", jsone);
 		}
     	
     }
     
     @Override 
     public void onActivityResult(int requestCode, int resultCode, Intent data) 
     {     
       super.onActivityResult(requestCode, resultCode, data); 
       switch(requestCode) 
       {
       case SHOW_ITEM_EDITOR:
     	  if (resultCode == RESULT_OK) 
     	  {
     		  if(data == null || !data.hasExtra(ListItemEditor.STR_ID))
     			  break;
     		  int action = data.getIntExtra(ListItemEditor.STR_ID,ListItemEditor.ACTION_IGNORE);
     		  if(action == ListItemEditor.ACTION_DELETE)
     		  {
     			  String message = data.getStringExtra(ListItemEditor.TODO_MESSAGE);
     			  remove_item(message);
     		  }
 	      }
     	  break;
       }
     }
     
     private void remove_item(String message)
     {
     	try
     	{
     		if(curr_category != null && curr_category.length() != 0)
     			this.dbconn.remove_message(message, curr_category);
     		else if(message.contains(":"))
     		{
     			int loc = message.indexOf(":");
     			String msg = message.substring(loc+1);
     			String cat = message.substring(0,loc);
     			this.dbconn.remove_message(msg,cat);
     		}
     		else
     			this.dbconn.remove_message(message,null);
     		rebuild_adapter();
     		last_removed = message;
     		notifier.toast_message("Removed: " + message);
     	}
     	catch(SQLException sqle)
     	{
     		notifier.log_exception("TODO", "Failed to remove: " + message, sqle);
     		
     	}
 		
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
         case R.id.undo:
         	if(last_removed == null)
         		break;
         	add_todo(last_removed);
         	notifier.toast_message("Restored: " + last_removed);
         	last_removed = null;
         	break;
         case R.id.menu_export:
         	this.dbconn.export_json(EXPORT_FILENAME);
         	notifier.toast_message("Exported TODO List as " + EXPORT_FILENAME);
         	break;
         case R.id.menu_import:
         	this.dbconn.import_json(IMPORT_FILENAME);
         	rebuild_adapter();
         	notifier.toast_message("Imported TODO list items");
         	break;
         case R.id.menu_version:
         	String app_ver;
 			try {
 				app_ver = utils.get_app_version(this);
 	        } catch (NameNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				app_ver = "Error getting version";
 			}
 			notifier.toast_message(app_ver);
         	return true;
     	default:
     		return super.onOptionsItemSelected(item);
         }
         
         return true;
     }
 }
