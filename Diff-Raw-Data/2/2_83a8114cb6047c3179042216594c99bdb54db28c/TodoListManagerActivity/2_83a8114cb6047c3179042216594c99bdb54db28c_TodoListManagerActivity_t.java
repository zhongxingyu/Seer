 package il.ac.huji.todolist;
 
 import java.util.Date;
 import java.util.ArrayList;
 
 import android.net.Uri;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 
 public class TodoListManagerActivity extends Activity {
 	
 	private ArrayList<TodoItem> todos;
 	private ListView todolist;
 	private ArrayAdapter<TodoItem> todoAdapter;
 	
 	public final static int ADD_ACTIVITY_REQUEST_CODE = 1;
 	public final static String ADD_ITEM_RESULT_STRING="title";
 	public final static String ADD_ITEM_RESULT_DATE="dueDate";
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_todo_list_manager);
 		
 		todos = new ArrayList<TodoItem>();
 		
 		
 		todolist = (ListView)findViewById(R.id.lstTodoItems);
 		todoAdapter = new TodoListArrayAdapter(this,android.R.layout.simple_list_item_1,todos);
 		todolist.setAdapter(todoAdapter);
 		
 		registerForContextMenu((ListView)findViewById(R.id.lstTodoItems));
 	}
 
 
 	public void OpenAddItemActivity()
 	{
 		Intent intent = new Intent(this,AddNewTodoItemActivity.class);
 		startActivityForResult(intent,ADD_ACTIVITY_REQUEST_CODE);
 	}
 	
 	
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
 	{
 		switch(requestCode)
 		{
 		case ADD_ACTIVITY_REQUEST_CODE:
 			if (RESULT_OK == resultCode)
 			{
 				String AddItemTitle = data.getStringExtra(ADD_ITEM_RESULT_STRING);
 				
 				Date dueDate = (Date)data.getSerializableExtra(ADD_ITEM_RESULT_DATE);
 				
 				AddItemInTodoList(AddItemTitle,dueDate);
 			}
 			break;
 		}
 	}
 
 
 	public void AddItemInTodoList(String thestring, Date dueDate)
 	{
     	if (null == thestring || thestring == "" || thestring.length()==0)
     	{
     		return;
     	}	
     	todoAdapter.add(new TodoItem(thestring,dueDate));
     	
 	}
 	
 	public void DeleteSelectedTodoItem()
 	{
 		ListView todoList = (ListView)findViewById(R.id.lstTodoItems);
 		int selected = todoList.getSelectedItemPosition();
 		if (selected >=0 )
 		{
 			todos.remove(selected);				
 			todoAdapter.notifyDataSetChanged();
 		}
 		
 	}
 	
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.todo_list_manager, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) 
 	{
 		switch (item.getItemId())
 		{
 		case R.id.menuItemAdd:
 			OpenAddItemActivity();
 			break;
 		case R.id.menuItemOptionDelete:
 			DeleteSelectedTodoItem();
 			break;
 		default:
 			return super.onOptionsItemSelected(item);	
 		}
 		return true;
 	}
 
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 			AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
 		int selected = info.position;
 		switch (item.getItemId()) {
 		case R.id.menuItemDelete:
 			todos.remove(selected);				
 			todoAdapter.notifyDataSetChanged();
 			break;
 		case R.id.menuItemCall:
 			// gets the number, assuming 'call ' at the beginning
 			String number = todos.get(selected).title.substring(5);  
 			Intent dial = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+number));
 			startActivity(dial);
 
 			break;
 		default:
 			break;
 		}
 		return true;
 	}
 
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.context_todo_menu, menu);
 	    TodoItem current = todos.get(((AdapterContextMenuInfo)menuInfo).position);
 	    menu.setHeaderTitle(current.title);
	    if (current.title.matches("Call\\s.*"))
 	    {
 		    MenuItem callItem = menu.getItem(1);
 		    callItem.setVisible(true);
 		    callItem.setTitle(current.title);
 	    }
 	    else
 	    {
 	    	menu.removeItem(1);
 	    }
 	}
 	
 }
