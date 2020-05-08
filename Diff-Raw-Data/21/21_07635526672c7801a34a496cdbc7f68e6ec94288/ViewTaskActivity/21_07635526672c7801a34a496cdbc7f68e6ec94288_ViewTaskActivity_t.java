 package com.linqcan.mytodo;
 
 import com.linqcan.mytodo.ViewTaskFragment.viewTaskListener;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.util.Log;
import android.view.MenuItem;
 
 public class ViewTaskActivity extends Activity implements viewTaskListener{	
 
 	private long mTaskid= -1;
 	
 	private static void putLogMessage(String msg){
 		MainActivity.putLogMessage("Linqcan::ViewTaskActivity", msg);
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		putLogMessage("Call orientation validation");
 		MainActivity.validateOrientation(getResources());
 		
 		if(MainActivity.isDualPane()){
 			finish();
 			return;
 		}
 		
 		setContentView(R.layout.view_task_activity);
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 		
 		Bundle args = getIntent().getExtras();
 		if(args != null){
 			mTaskid = args.getLong("id");
 		}
 		else{
 			finish();
 		}
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);		
 		if(data != null){
 			if(requestCode == 0 && resultCode == 0){
 				putLogMessage("Received updated data from EditTask");
 				//Just pass the data on to the main activity
 				// and show the task list again, for now...
 				setResult(0, data);
 				finish();
 			}
 		}
 	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case android.R.id.home:
				putLogMessage("Up clicked!");
				Intent intent = new Intent(this, MainActivity.class);
				startActivity(intent);
				finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
 
 	public void deleteTask(long taskid) {
 		Intent data = new Intent();
 		data.putExtra("id", taskid);
 		setResult(1, data);
 		finish();
 	}
 
 	public void editTask(long taskid) {
 		Intent data = new Intent(getApplicationContext(),EditTaskActivity.class);
 		data.putExtra("id", taskid);
 		startActivityForResult(data, 0);		
 	}
 	
 	public long getTaskid(){
 		return this.mTaskid;
 	}
 
 }
