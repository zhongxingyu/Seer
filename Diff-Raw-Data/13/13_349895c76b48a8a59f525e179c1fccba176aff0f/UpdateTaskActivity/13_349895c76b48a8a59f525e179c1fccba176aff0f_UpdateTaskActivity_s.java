 package com.CMPUT301F12T07.crowdsource;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import com.CMPUT301F12T07.crowdsource.taskmodeldb.LocalDB;
 import com.CMPUT301F12T07.crowdsource.taskmodeldb.Task;
 
 public class UpdateTaskActivity extends Activity {
 
 	private Task currentTask;
 	private EditText taskTitle;
 	private TextView startDate;
 	private EditText endDate;
 	private EditText taskQuantity;
 	private EditText taskDesc;
 	private LocalDB db;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_update_task);
 //        getActionBar().setDisplayHomeAsUpEnabled(true);
 
         db = new LocalDB(this);
         this.currentTask = db.getTask(getIntent().getExtras().getLong("taskID"));
         db.close();
         
         // Getting the task title field
         this.taskTitle = (EditText) findViewById(R.id.textEditTitle);
         taskTitle.setText(currentTask.get_title());
 
         // Getting the start Date field
         this.startDate = (TextView) findViewById(R.id.textEditCreatedDate);
         startDate.setText(currentTask.get_dateCreate());
 
         // Getting the end Date field
         this.endDate = (EditText) findViewById(R.id.textEditDueDate);
         endDate.setText(currentTask.get_dateDue());
 
         // Getting the task quantity field
         this.taskQuantity = (EditText) findViewById(R.id.textEditQuantity);
         taskQuantity.setText(Integer.toString(currentTask.get_quantity()));
 
         // Getting the task description field
         this.taskDesc = (EditText) findViewById(R.id.textEditDescription);
 
         taskDesc.setText(currentTask.get_description());
         
         final Button Cancel = (Button) findViewById(R.id.buttonCancel);
         Cancel.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	finish();
             }
         });
         
         final CheckBox Public = (CheckBox) findViewById(R.id.checkboxPublic);
         if (currentTask.get_visibility() == 0)
         	Public.setChecked(true);
         
         Public.setOnClickListener(new View.OnClickListener()
         {
         	public void onClick(View v)
         	{
         		if (Public.isChecked())
         			currentTask.set_visibility(0);
         		else
         			currentTask.set_visibility(1);
         	}
         });
  
         
         Button Save = (Button) findViewById(R.id.buttonSave);
         Save.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	currentTask.set_title(taskTitle.getText().toString());
             	currentTask.set_dateCreate(startDate.getText().toString());
             	currentTask.set_dateDue(endDate.getText().toString());
             	currentTask.set_quantity(Integer.parseInt(taskQuantity.getText().toString()));
             	currentTask.set_description(taskDesc.getText().toString());
             	
             	db.updateTask(currentTask, "");
             	
             	finish();
             }
         });
     }

     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_update_task, menu);
         return true;
     }
 
     
 //    @Override
 //    public boolean onOptionsItemSelected(MenuItem item) {
 //        switch (item.getItemId()) {
 //            case android.R.id.home:
 //                NavUtils.navigateUpFromSameTask(this);
 //                return true;
 //        }
 //        return super.onOptionsItemSelected(item);
 //    }
 
 }
