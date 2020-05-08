 package com.taskwhere.android.activity;
 
 import java.lang.reflect.GenericArrayType;
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.markupartist.android.widget.ActionBar;
 import com.markupartist.android.widget.ActionBar.Action;
 import com.markupartist.android.widget.ActionBar.IntentAction;
 import com.taskwhere.android.adapter.TaskListDbAdapter;
 import com.taskwhere.android.model.Task;
 
 public class TaskWhereActivity extends Activity {
     
 	private final static String TW = "TaskWhere";
 	private ArrayList<Task> taskList;
 	private ListView taskListView;
 	private static ActionBar actionBar;
	private TaskListDbAdapter dbAdapter;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	
     	requestWindowFeature(Window.FEATURE_NO_TITLE);
         super.onCreate(savedInstanceState);
         getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.main);
         setContentView(R.layout.main);
         taskList = new ArrayList<Task>();
         
         actionBar = (ActionBar) findViewById(R.id.actionbar);
         actionBar.setHomeAction(new IntentAction(this, createIntent(this),R.drawable.home));
         
         final Action infoAction = new IntentAction(this, InfoActivity.createIntent(this), R.drawable.info);
         actionBar.addAction(infoAction);
         final Action addAction = new IntentAction(this, AddTaskActivity.createIntent(this), R.drawable.add_item);
         actionBar.addAction(addAction);
         
         showSavedProfiles();
         
         taskListView = (ListView) findViewById(R.id.taskList);
         taskListView.setDivider(null);
         taskListView.setDividerHeight(10);
         taskListView.setAdapter(new TaskListAdapter(getApplicationContext(), R.layout.task_item, taskList));
     }
     
     public void showSavedProfiles(){
     	
    	dbAdapter = new TaskListDbAdapter(getApplicationContext());
     	dbAdapter.open();
     	
     	Cursor taskCursor = dbAdapter.getAllTasks();
     	startManagingCursor(taskCursor);
     	
     	Log.d(TW, "Cursor count : " + taskCursor.getCount());
     	
     	if(taskCursor.getCount() > 0){
     		
     		taskCursor.moveToFirst();
     		do{
     			Log.d(TW, "Task Text : " + taskCursor.getString(1));
     			Log.d(TW, "Task Loc : " + taskCursor.getString(2));
     			Log.d(TW, "Task Lat : " + taskCursor.getDouble(3));
     			Log.d(TW, "Task Long : " + taskCursor.getDouble(4));
     			Log.d(TW, "Task uniqueid : " + taskCursor.getInt(5));
     			
         		taskList.add(new Task(taskCursor.getString(1), taskCursor.getString(2),
         				taskCursor.getDouble(3), taskCursor.getDouble(4), taskCursor.getInt(5)));
     		}while(taskCursor.moveToNext());
     	}
    	taskCursor.close();
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	dbAdapter.close();
     }
     
     public static Intent createIntent(Context context) {
 		
     	Intent i = new Intent(context, TaskWhereActivity.class);
 		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         return i;
     }
     
     private class TaskListAdapter extends ArrayAdapter<Task>{
 
 		public TaskListAdapter(Context context, int textViewResourceId,
 				ArrayList<Task> objects) {
 			super(context, textViewResourceId, objects);
 			context = getContext();
 			taskList = objects;
 		}
     	
     	@Override
     	public View getView(int position, View convertView, ViewGroup parent) {
     		
     		View v = convertView;
 			
 			if(v == null){
 				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				v = inflater.inflate(R.layout.task_item, null);
 			}
 			
 			Task task = taskList.get(position);
 			
 			if(task != null){
 				
 				TextView taskText = (TextView) v.findViewById(R.id.taskText);
 				taskText.setText(task.getTaskText());
 				Log.d(TW, "Task Test : " + task.getTaskText());
 				TextView taskLocView = (TextView) v.findViewById(R.id.taskLoc);
 				taskLocView.setText(task.getTaskLoc());
 				Log.d(TW, "Task Test : " + task.getTaskLoc());
 			}
 			
 			return v;
     	}
     }
 }
