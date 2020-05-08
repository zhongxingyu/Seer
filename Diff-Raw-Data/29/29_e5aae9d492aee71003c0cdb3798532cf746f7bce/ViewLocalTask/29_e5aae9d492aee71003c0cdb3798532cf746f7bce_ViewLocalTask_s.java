 package ca.ualberta.cmput301project;
 
 import java.util.ArrayList;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
public class ViewLocalTask extends ListActivity{
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_tasklist);
         
         refresh();
     }
     
     @Override
     public void onRestart() {
     	super.onRestart();
     	refresh();
     }
     @Override
     public void onResume() {
     	super.onResume();
     	refresh();
     }
     
     private void refresh() {
     	ArrayList<Task> tasks = LocalTaskManager.loadLocalTasks(this);
         setListAdapter(new ArrayAdapter<Task>(this,android.R.layout.simple_list_item_1,tasks));
     }
     
     /* (non-Javadoc)
      * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
      */
     /* (non-Javadoc)
      * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
      */
     public void onListItemClick(ListView parent,View v, int position,long id){
     	ArrayList<Task> tasks = LocalTaskManager.loadLocalTasks(this);
     	Task clickedTask = tasks.get(position);
     	
     	Intent intent = new Intent();
     	Bundle b = new Bundle();
 		b.putSerializable("task", clickedTask);
 		intent.putExtras(b);
 		intent.setClass(this, FulfillTask.class);
 		startActivity(intent);
     }
 }
