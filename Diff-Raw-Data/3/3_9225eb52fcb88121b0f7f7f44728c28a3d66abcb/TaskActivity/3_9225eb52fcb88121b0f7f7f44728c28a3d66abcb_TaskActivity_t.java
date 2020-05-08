 /*
  * This file is part of TaskMan
  *
  * Copyright (C) 2012 Jed Barlow, Mark Galloway, Taylor Lloyd, Braeden Petruk
  *
  * TaskMan is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * TaskMan is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with TaskMan.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package ca.cmput301.team13.taskman;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.TextView;
 import ca.cmput301.team13.taskman.model.Requirement.contentType;
 import ca.cmput301.team13.taskman.model.Task;
 
 /**
  * This activity has two modes, one for viewing tasks, and one for
  * editing.
  */
 public class TaskActivity extends Activity implements OnClickListener {
 
     private Task task;
     private String mode;
     private RequirementListAdapter reqAdapter;
     private FulfillmentListAdapter fulAdapter;
 
     /**
      * Handles initialization of the activity.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Bundle extras = getIntent().getExtras();
         setMode(extras.getString("mode"));
 
         task = (Task) extras.getParcelable("task");
 
         //If we're in an editing mode, populate the editing controls and consider the appropriate layout
         if (getMode().equals("edit") || getMode().equals("create")) {
             setContentView(R.layout.activity_edit_task);
             setEditingFields();
             ((Button)findViewById(R.id.save_button)).setOnClickListener(this);
             ((Button)findViewById(R.id.cancel_button)).setOnClickListener(this);
             if(getMode().equals("edit")) {
                 ((Button)findViewById(R.id.delete_button)).setOnClickListener(this);
             } else {
                 findViewById(R.id.delete_button).setVisibility((View.INVISIBLE));
             }
         } else {
             setContentView(R.layout.activity_view_task);
             setViewingFields();
         }
         //Prevent loss of focus when selecting an EditText field
         ((ListView)findViewById(R.id.requirement_list)).setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
         //        ((LinearLayout)findViewById(R.id.basic_info_entry_panel)).setVisibility(View.GONE);
     }
 
     /**
      * Sets the listeners, adapters, and text fields for the Task Editing View
      */
     private void setEditingFields() {
         //Disconnect our Task from the Repository
         task.delaySaves(true);
         //Set the Title
         ((EditText)findViewById(R.id.entry_title)).setText(task.getTitle());
         //Set the Description
         ((EditText)findViewById(R.id.entry_description)).setText(task.getDescription());
 
         //Add onClickListeners
         ((Button)findViewById(R.id.save_button)).setOnClickListener(this);
         ((Button)findViewById(R.id.cancel_button)).setOnClickListener(this);
 
         ((ImageButton)findViewById(R.id.req_addTxt_btn)).setOnClickListener(this);
         ((ImageButton)findViewById(R.id.req_addImg_btn)).setOnClickListener(this);
         ((ImageButton)findViewById(R.id.req_addAud_btn)).setOnClickListener(this);
         ((ImageButton)findViewById(R.id.req_addVid_btn)).setOnClickListener(this);
         //TODO: Set the requirements
         reqAdapter = new RequirementListAdapter(task, "edit", this);
         //Disconnect our Requirements from the repository
         reqAdapter.delaySaves(true);
         ((ListView)findViewById(R.id.requirement_list)).setAdapter(reqAdapter);
     }
 
     /**
      * Sets the listeners, adapters, and text fields for the Task View
      */
     private void setViewingFields() {
         //Show title and description
         ((TextView)findViewById(R.id.task_name_text)).setText(task.getTitle());
         ((TextView)findViewById(R.id.task_desc_text)).setText(task.getDescription());
         
         //Hide edit button if not the creator
         ((Button)findViewById(R.id.task_edit_btn)).setVisibility(
                 task.getCreator().equals(TaskMan.getInstance().getUser()) ? View.VISIBLE : View.GONE);
         ((Button)findViewById(R.id.task_edit_btn)).setOnClickListener(this);
         
         //Setup the Requirements List
         reqAdapter = new RequirementListAdapter(task, mode, this);
         ((ListView)findViewById(R.id.requirement_list)).setAdapter(reqAdapter);
         fulAdapter = new FulfillmentListAdapter(task, this);
         ((ListView)findViewById(R.id.fulfillment_list)).setAdapter(fulAdapter);
     }
 
     /**
      * Constructs menu options.
      */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_task, menu);
         return true;
     }
 
     private String getMode() {
         return mode;
     }
 
     private void setMode(String mode) {
         this.mode = mode;
     }
 
     /**
      * Handles pause event.
      */
     @Override
     public void onPause() {
         super.onPause();
         if(getMode().equals("create")) {
             //Destroy the associated Task before returning
             TaskMan.getInstance().getRepository().removeTask(task);
             //This means that pressing Save should switch the mode to "edit", before leaving the Activity
         }
     }
     
     /**
      * Handles resume event.
      */
     @Override
     public void onResume() {
     	super.onResume();
     	if(getMode().equals("view")) {
     		task = TaskMan.getInstance().getRepository().getTaskUpdate(task);
     		setViewingFields();
     	}
     }
 
     /**
      * Saves the current Task
      */
     private void saveTask() {
         String taskTitle = ((EditText)findViewById(R.id.entry_title)).getText().toString();
         String taskDescription = ((EditText)findViewById(R.id.entry_description)).getText().toString();
         //TODO: Validation? Ensure each Task has a title and a single requirement, at least?
         //Update the parceled Task
         if(getMode().equals("edit") || getMode().equals("create")) {
             task.setTitle(taskTitle);
             task.setDescription(taskDescription);
             //Push all changes to the repository
             task.delaySaves(false);
             reqAdapter.delaySaves(false);
             //Make sure we don't delete the Task after all this
             setMode("edit");
         }
         super.finish();
     }
 
     /**
      * Return to the previous activity without saving
      */
     private void cancelTask() {
         super.finish();
     }
 
     /**
      * Delete the task and end the activity
      */
     private void deleteTask() {
         //Destroy the associated Task
         TaskMan.getInstance().getRepository().removeTask(task);
         Intent i = new Intent(this, RootActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         startActivity(i);
     }
     
     /**
      * Handles click events.
      */
     public void onClick(View source) {
         if(source.equals(findViewById(R.id.save_button))) {
             saveTask();
         } else if(source.equals(findViewById(R.id.cancel_button))) {
             cancelTask();
         } else if(source.equals(findViewById(R.id.delete_button))) {
             deleteTask();
         } else if  (source.getId() == R.id.req_addTxt_btn) {
             TaskMan.getInstance().getRepository().createRequirement(TaskMan.getInstance().getUser(), task, contentType.text);
             reqAdapter.update();
         } else if  (source.getId() == R.id.req_addImg_btn) {
             TaskMan.getInstance().getRepository().createRequirement(TaskMan.getInstance().getUser(), task, contentType.image);
             reqAdapter.update();
         } else if  (source.getId() == R.id.req_addAud_btn) {
             TaskMan.getInstance().getRepository().createRequirement(TaskMan.getInstance().getUser(), task, contentType.audio);
             reqAdapter.update();
         } else if  (source.getId() == R.id.req_addVid_btn) {
             TaskMan.getInstance().getRepository().createRequirement(TaskMan.getInstance().getUser(), task, contentType.video);
             reqAdapter.update();
         } else if  (source.getId() == R.id.task_edit_btn) {
             Bundle b = new Bundle();
             b.putParcelable("task", task);
             b.putString("mode", "edit");
             Intent i = new Intent(this, TaskActivity.class);
             i.putExtras(b);
             startActivity(i);
         } else {
             InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
             inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
         }
     }
 
 }
