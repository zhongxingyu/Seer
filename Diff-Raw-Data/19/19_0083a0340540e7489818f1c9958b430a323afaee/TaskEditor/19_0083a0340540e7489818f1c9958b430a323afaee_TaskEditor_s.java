 package com.nullpointerengineering.android.pomodoro.activities;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.PixelFormat;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.EditText;
 import com.nullpointerengineering.android.pomodoro.R;
 import com.nullpointerengineering.android.pomodoro.widgets.NumberPicker;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Stratos
  * Date: 03/06/12
  * Time: 7:01 PM
  * An activity to create/edit tasks.
  */
 public class TaskEditor extends Activity {
 
     private EditText titleText;
     private NumberPicker priorityPicker;
     private NumberPicker estimationPicker;
     private long taskId;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.task_edit);
 
         titleText = (EditText) findViewById(R.id.title);
         priorityPicker = (NumberPicker) findViewById(R.id.priority);
         estimationPicker = (NumberPicker) findViewById(R.id.work_units);
 
         Button confirmButton = (Button) findViewById(R.id.confirm);
         confirmButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 saveState();
                 finish();
             }
         });
 
         priorityPicker.setRange(PRIORITY_MINIMUM, PRIORITY_MAXIMUM);
         estimationPicker.setRange(WORK_UNIT_MINIMUM, WORK_UNIT_MAXIMUM);
 
         populateFields();
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         saveState();
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         saveState();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         populateFields();
     }
 
     /* This little trick is to make the background play better with multiple screens */
     @Override
     public void onAttachedToWindow() {
         super.onAttachedToWindow();
         Window window = getWindow();
         window.setFormat(PixelFormat.RGB_565);          //Format and Dither flags for the background gradient.
         window.addFlags(WindowManager.LayoutParams.FLAG_DITHER);
     }
 
     private void populateFields() {
 /*
        taskId = this.getIntent().getExtras().getLong(TASKS_KEY_ID);
 
         if (taskId == 0) {
             priorityPicker.setCurrent(PRIORITY_MINIMUM);
             estimationPicker.setCurrent(WORK_UNIT_MINIMUM);
         } else {
             Task task = TaskDaoSqlite.createTaskDao(this).findTaskById(taskId);
 
             titleText.setText(task.getTitle());
             priorityPicker.setCurrent(task.getPriority());
             estimationPicker.setCurrent(task.getEstimate());
         }
 */
     }
 
     private void saveState() {
 /*
         String title = titleText.getText().toString();
         int priority = priorityPicker.getCurrent();
         int estimate = estimationPicker.getCurrent();
 
         taskId = TaskDaoSqlite.createTaskDao(this).storeTask( new TaskImpl(taskId, title, estimate, priority));
 
         Intent intent = this.getIntent();
         Bundle data = getIntent().getExtras();
 
        data.putLong(TASKS_KEY_ID, taskId);
         intent.putExtras(data);
 */
     }
     protected static String TASKS_KEY_ID      = "KEY ID";
     private static int PRIORITY_MINIMUM     = 1;
     private static int PRIORITY_MAXIMUM     = 5;
     private static int WORK_UNIT_MINIMUM    = 1;
     private static int WORK_UNIT_MAXIMUM    = 7;
 }
