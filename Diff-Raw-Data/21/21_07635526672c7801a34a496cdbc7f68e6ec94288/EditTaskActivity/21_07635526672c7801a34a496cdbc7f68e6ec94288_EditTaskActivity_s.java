 package com.linqcan.mytodo;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.MenuItem;
 import android.widget.EditText;
 
 import com.linqcan.mytodo.AddTaskFragment.addTaskListener;
 import com.linqcan.mytodo.AddTaskFragment.editTaskListener;
 
 public class EditTaskActivity extends Activity implements editTaskListener, addTaskListener {
 	
 	private static void putLogMessage(String msg){
 		MainActivity.putLogMessage("Linqcan::EditTaskActivity", msg);
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);		
 		putLogMessage("Call orientation validation");
 		MainActivity.validateOrientation(getResources());
 		if(MainActivity.isDualPane()){
 			if(savedInstanceState != null && savedInstanceState.getBoolean("mytodo") == true){
 				MainActivity.setActivityInstanceStateBundle(savedInstanceState);
 			}
 			finish();
 			return;
 		}
 		setContentView(R.layout.add_task_activity);
 		getActionBar().setDisplayHomeAsUpEnabled(false);
 	}
 	
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		EditText fieldTitle = (EditText) findViewById(R.id.add_task_title);
 		EditText fieldDesc =  (EditText) findViewById(R.id.add_task_description);
 		outState.putString("title", fieldTitle.getText().toString());
 		outState.putString("description", fieldDesc.getText().toString());
 		outState.putBoolean("mytodo", true);
 	}
 	
 	public void onUpdateTask(Task task) {
 		putLogMessage("onUpdateTask");
 		Intent data = new Intent();
 		data.putExtra("task",task);
 		setResult(0,data);
 		finish();		
 	}
 	
 	public void onCancel() {
 		finish();		
 	}
 	
 	public void onSaveTask(Task task) {
 		putLogMessage("This method should not have ben called!");		
 	}

 }
