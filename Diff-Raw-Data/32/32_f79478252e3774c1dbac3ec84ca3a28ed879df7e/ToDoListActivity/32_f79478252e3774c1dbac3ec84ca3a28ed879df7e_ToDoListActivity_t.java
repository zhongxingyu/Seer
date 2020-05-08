 package com.paad.todolist;
 
 import android.app.FragmentManager;
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.widget.ArrayAdapter;
 
 
 import java.util.ArrayList;
 
 public class ToDoListActivity extends Activity implements NewItemFragment.OnNewItemAddedListener {
 
     private ArrayAdapter<String>aa;
     private ArrayList<String> todoItems;
 
 
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         // Get references to the Fragments
         FragmentManager fm = getFragmentManager();
         ToDoListFragment todoListFragment =
                 (ToDoListFragment)fm.findFragmentById(R.id.TodoListFragment);
 
         // Create the Array list of to do items
         todoItems = new ArrayList<String>();
 
         // Create the Array Adapter to bind the array to the List View
         aa = new ArrayAdapter<String>(this,
                                         android.R.layout.simple_list_item_1,
                                         todoItems);
 
         // Bind the array adapter ot the listview
         todoListFragment.setListAdapter(aa);
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.to_do_list, menu);
         return true;
     }
 
 
 
     public void onNewItemAdded(String newItem) {
         todoItems.add(newItem);
         aa.notifyDataSetChanged();
     }
 }
