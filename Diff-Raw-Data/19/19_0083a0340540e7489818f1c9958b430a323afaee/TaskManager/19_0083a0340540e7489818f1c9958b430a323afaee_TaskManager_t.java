 package com.nullpointerengineering.android.pomodoro.activities;
 
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.Toast;
 import com.google.inject.Inject;
 import com.nullpointerengineering.android.pomodoro.R;
import com.nullpointerengineering.android.pomodoro.data.DatabaseConstants;
import com.nullpointerengineering.android.pomodoro.data.DatabaseHelper;
 import com.nullpointerengineering.android.pomodoro.utilities.Eula;
 import roboguice.activity.RoboListActivity;
 
 public class TaskManager extends ListActivity {
 
    private DatabaseHelper dbHelper;

     @Override
     public void onCreate(Bundle savedInstanceState){
         super.onCreate(savedInstanceState);
         setContentView(R.layout.pomodoro);
         Eula eula = new Eula();
         eula.show(this);
         ListView list = getListView();
         registerForContextMenu(list);
         list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 Toast hello = Toast.makeText(view.getContext(), "hello", 30);
                 hello.show();
             }
         });
 
         //On clickers
         Button playButton = (Button) findViewById(R.id.PlayBtn);
         playButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Toast hello = Toast.makeText(view.getContext(), "hello", 30);
                 hello.show();
             }
         });
 
         Button addTaskButton = (Button) findViewById(R.id.AddTaskBtn);
         addTaskButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 editTaskActivity(0);
             }
         });
 
         Button preferencesButton = (Button) findViewById(R.id.PreferenceBtn);
         preferencesButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent i = new Intent(view.getContext(),PreferencesEditor.class);
                 startActivity(i);
             }
         });
 
         ( findViewById(R.id.StatisticsBtn)).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 //TODO implement
             }
         });
 
        dbHelper = new DatabaseHelper(this);
        dbHelper.onCreate(dbHelper.getWritableDatabase());
     }
 
     //Activity launchers
     private void editTaskActivity(long id) {
         Intent i = new Intent(this, TaskEditor.class);
         Bundle bundle = new Bundle();
         bundle.putLong(TaskEditor.TASKS_KEY_ID, id);
         i.putExtras(bundle);
         startActivityForResult(i, ACTIVITY_EDIT);
     }
 
     private static final int    ACTIVITY_CREATE     = 0;
     private static final int    ACTIVITY_EDIT       = 1;
     private static final int    EDIT_ID             = Menu.FIRST;
     private static final int    DELETE_ID           = Menu.FIRST + 1;
     private static final int    CANCEL_ID           = Menu.FIRST + 2;
 
 }
