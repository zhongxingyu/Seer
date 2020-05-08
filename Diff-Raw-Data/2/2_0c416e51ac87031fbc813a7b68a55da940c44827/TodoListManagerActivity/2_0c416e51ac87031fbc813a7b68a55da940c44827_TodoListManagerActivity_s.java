 package il.ac.huji.todolist;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import android.net.Uri;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class TodoListManagerActivity extends Activity {
 
 	private ArrayAdapter<Todo> adapter;
 	private List<Todo> todos;
 	private static final int contextDelete=0;
 	private static final int contextCall=1;
 	private static final int addNewItemRequestCall=42;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_todo_list_manager);
         
         todos = new ArrayList<Todo>();
        todos.add(new Todo("hello", new Date()));
        todos.add(new Todo("Call 123-12345", new Date()));
         ListView todoListView = 
         		(ListView)findViewById(R.id.lstTodoItems);
         adapter =   new CustomAdapter(this,	todos);
         todoListView.setAdapter(adapter);
         registerForContextMenu(todoListView);
 
     }
 
     public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {  
         super.onCreateContextMenu(menu, v, menuInfo);  
         getMenuInflater().inflate(R.menu.todo_list_context, menu);
         AdapterContextMenuInfo info =(AdapterContextMenuInfo)menuInfo;
         Todo task = todos.get((int)info.id);
         menu.setHeaderTitle(task.task);  
          if (task.task.startsWith("Call ")) {
         	 menu.getItem(1).setTitle(task.task);
          } else {
          	 menu.removeItem(R.id.menuItemCall);
          }
     }  
     @Override  
     public boolean onContextItemSelected(MenuItem item) { 
         AdapterContextMenuInfo info =(AdapterContextMenuInfo)item.getMenuInfo();
         Todo task = todos.get((int)info.id);
     	switch (item.getItemId()) {
     	case R.id.menuItemDelete:
     		adapter.remove(task);
     		break;
     	case R.id.menuItemCall:
     		Intent dial = new Intent(Intent.ACTION_DIAL,Uri.parse("tel:"+task.task.substring(5)));
     		startActivity(dial);
     		break;
     	default:
     		return false;
     	}
     	return true;  
     }  
   
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.todo_list_manager, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch (item.getItemId()) {
     	case R.id.menuItemAdd:
     		Intent intent = new Intent(this, AddNewTodoItemActivity.class);
     		startActivityForResult(intent, addNewItemRequestCall);
     		break;
     	}
     	return true;
     }
     
     @Override
 	protected void onActivityResult(int reqCode, int resCode, Intent data) {
 		  switch (reqCode) {
 		  case addNewItemRequestCall:
 			  if (resCode!=RESULT_OK || data==null) break;
 			  String task = data.getStringExtra("title");
 			  Date date = (Date) data.getSerializableExtra("dueDate");
 			  adapter.add(new Todo(task, date));
 			  break;
 		  }
 		}
 
 
 
     
 }
