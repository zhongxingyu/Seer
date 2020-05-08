 package com.CMPUT301F12T07.crowdsource;
 
 import android.R.string;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.widget.TextView;
 
 public class ViewTask extends Activity {
 	
 	
 	private Task currentTask;
 	//private DatabaseHandler db;
 
 	private TextView taskTitle;
 	private TextView startDate;
 	private TextView endDate;
 	private TextView taskContent;
 	private TextView taskDesc;
 	
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_view_task);
         
        Task curentTask = getIntent().getExtras().getParcelable(@strings/TaskObject);
         
         // Getting the task title field
         this.taskTitle = (TextView) findViewById(R.id.textViewTitle);
         // unknown implimentation of task getters
         String titleStr = this.currentTask.get_title();
         taskDesc.setText(titleStr);
         
         // Getting the start Date field
         this.startDate = (TextView) findViewById(R.id.textViewCreatedDate);
         // unknown implimentation of task getters
         int startDateValue = Integer.parseInt(this.currentTask.get_dateCreate());
         startDate.setText(Integer.toString(startDateValue));
         
         // Getting the end Date field
         this.endDate = (TextView) findViewById(R.id.textViewDueDate);
         // unknown implimentation of task getters
         int endDateValue = Integer.parseInt(this.currentTask.get_dateDue());
         endDate.setText(Integer.toString(endDateValue));
         
         // Getting the task content field
         // type? content?
         this.taskContent = (TextView) findViewById(R.id.textViewContent);
         // unknown implimentation of task getters
         String contentStr = this.currentTask.get_type();
         taskDesc.setText(contentStr);
         
         // Getting the task description field
         this.taskDesc = (TextView) findViewById(R.id.textViewDescription);
         // unknown implimentation of task getters
         String descStr = this.currentTask.get_description();
         taskDesc.setText(descStr);
         
         /*
         timeEdit.setOnFocusChangeListener(new OnFocusChangeListener() {
         	public void onFocusChange(View v, boolean hasFocus){
         		if (!hasFocus){
         			currentTask.setTime(Integer.parseInt(timeEdit.getText().toString()));
         			db.updateTask(currentTask);
         		}
         	}
         }); */
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_view_task, menu);
         return true;
     }
 }
