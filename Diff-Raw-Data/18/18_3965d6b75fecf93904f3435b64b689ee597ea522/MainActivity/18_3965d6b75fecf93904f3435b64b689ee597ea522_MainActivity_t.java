 package com.CMPUT301F12T07.crowdsource;
 
 import android.R;
 import android.app.Activity;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;

 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 public class MainActivity extends Activity {
 
 	private ListView myList;
 	private List<Task> tasks;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         LocalDB db = new LocalDB(this);
         this.tasks = db.getAllTasks();
         
         myList = (ListView)findViewById(R.id.tasklist);
         myList.setAdapter(new TaskListAdapter(MainActivity.this, tasks));
         
         // Adds listener for when a Task is clicked in the ListView
         myList.setOnItemClickListener(new OnItemClickListener() {
         	public void onItemClick(AdapterView<?> parent, View view, int position, long id){
         		//Intent intent = new Intent(this, ViewTask.class);
         		//intent.putExtra(); ## TODO: Add Database Connection here
         		//startActivity(intent);
         	}
         });
     }
 
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     /* Function to call ViewTask
      * Intent intent = new intent(this, ViewTask.class);
      * intent.putExtra(@string/TaskObject, database.getTaskById(Id));  // database call needs to be fixed
      * startActivity(intent);
      */
 }
