 package com.paad.todolist;
 
 import android.app.FragmentManager;
 import android.os.Bundle;
 import android.app.Activity;
import android.view.KeyEvent;
 import android.view.Menu;
import android.view.View;
 import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
 
 
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



        /*
        // Bind the Array Adapter to the List View
        myListView.setAdapter(aa);

        // Listen for D-pad or Enter key to be pressed and clear field
        myEditText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event){
               if (event.getAction() == KeyEvent.ACTION_DOWN)
                   if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER) ||
                           (keyCode == KeyEvent.KEYCODE_ENTER)){
                       // Enter the text the user inputed
                       todoItems.add(0, myEditText.getText().toString());
                       // Update the ListView
                       aa.notifyDataSetChanged();
                       // Clear the EditText box for new input
                       myEditText.setText("");
                       return true;
                   }
                return false;
            }
        });
        */
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
