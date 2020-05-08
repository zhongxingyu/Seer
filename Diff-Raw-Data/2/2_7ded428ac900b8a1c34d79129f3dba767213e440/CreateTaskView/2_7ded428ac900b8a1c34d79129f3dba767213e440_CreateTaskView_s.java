 package tasktracker.view;
 
 /**
  * TaskTracker
  * 
  * Copyright 2012 Jeanine Bonot, Michael Dardis, Katherine Jasniewski,
  * Jason Morawski
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may 
  * not use this file except in compliance with the License. You may obtain
  * a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed
  * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR 
  * CONDITIONS OF ANY KIND, either express or implied. See the License for the 
  * specific language governing permissions and limitations under the License.
  */
 
 /**
  * TaskTracker
  * 
  * Copyright 2012 Jeanine Bonot, Michael Dardis, Katherine Jasniewski,
  * Jason Morawski
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may 
  * not use this file except in compliance with the License. You may obtain
  * a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed
  * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR 
  * CONDITIONS OF ANY KIND, either express or implied. See the License for the 
  * specific language governing permissions and limitations under the License.
  */
 
 import java.util.List;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 
 import android.view.Gravity;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.*;
 import tasktracker.controller.TaskController;
 import tasktracker.model.WebDBManager;
 import tasktracker.model.elements.*;
 
 /**
  * An activity that allows a user to create a task.
  * 
  * @author Jeanine Bonot
  * 
  */
 public class CreateTaskView extends Activity {
 
 	private static final TaskController TASK_MANAGER = new TaskController();
 
 	private EditText _name;
 	private EditText _description;
 	private EditText _otherMembers;
 	private CheckBox _text;
 	private CheckBox _photo;
 	private WebDBManager _webManager;
 
 	/** The current app user */
 	private String _user;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_create_task_view);
 
 		// TODO: Get creator information
 		_user = "Debugger";
 
 		// Initialize our webManager
 		_webManager = new WebDBManager();
 
 		// Assign EditText fields
 		_name = (EditText) findViewById(R.id.taskName);
 		_description = (EditText) findViewById(R.id.editDescription);
 		_otherMembers = (EditText) findViewById(R.id.otherMembers);
 		_text = (CheckBox) findViewById(R.id.checkBoxText);
 		_photo = (CheckBox) findViewById(R.id.checkBoxPhoto);
 
 		// Assign Buttons
 		Button saveButton = (Button) findViewById(R.id.saveButton);
 		Button buttonMyTasks = (Button) findViewById(R.id.buttonMyTasks);
 		Button buttonCreate = (Button) findViewById(R.id.buttonCreateTask);
 		Button buttonNotifications = (Button) findViewById(R.id.buttonNotifications);
 		buttonCreate.setEnabled(false);
 
 		buttonMyTasks.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				Intent intent = new Intent(getApplicationContext(),
 						TaskListView.class);
 				intent.putExtra("USER", _user);
 				startActivity(intent);
 			}
 		});
 
 		buttonNotifications.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				Intent intent = new Intent(getApplicationContext(),
 						NotificationListView.class);
 				intent.putExtra("USER", _user);
 				startActivity(intent);
 			}
 		});
 
 		// Assign listener to Save button
 		saveButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View view) {
 				
 				if (hasEmptyFields()) {
 					return;
 				}
 
 				Task task = createTask();
 				if (TaskController.writeFile(task)) {
 					showToast("Wrote to file");
 				} else {
 					showToast("Failed to write to file.");
 				}
 
 				// Only add to web database if Creator has added members
 				List<String> others = task.getOtherMembers();
				if (others.size() > 0 && others != null) {
 					_webManager.insertTask(task);
 				}
 
 				finish();
 			}
 
 		});
 	}
 
 	/**
 	 * Checks if any of the required fields has been left empty.
 	 * 
 	 * @return True if a required field has been left empty, false otherwise.
 	 */
 	private boolean hasEmptyFields() {
 		if (_name.getText().toString().matches("")) {
 			showToast("Your task must have a name");
 			return true;
 		}
 		
 		if (_description.getText().toString().matches("")) {
 			showToast("Your task must have a description");
 			return true;
 		}
 
 		return false;
 	}
 	
 
 	/**
 	 * Create a task based on the creator's input.
 	 * 
 	 * @return The task created with the creator's input.
 	 */
 	private Task createTask() {
 
 		// TODO: Find out how to quickly access user information
 		Task task = new Task(_user);
 
 		task.setDescription(_description.getText().toString());
 		task.setName(_name.getText().toString());
 		task.setPhotoRequirement(_photo.isChecked());
 		task.setTextRequirement(_text.isChecked());
 		task.setOtherMembers(_otherMembers.toString());
 
 		return task;
 	}
 
 	private void showToast(String message) {
 		Toast toast = Toast.makeText(getApplicationContext(), message,
 				Toast.LENGTH_LONG);
 		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
 		toast.show();
 	}
 
 }
