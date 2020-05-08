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
 
 import java.util.*;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.util.Log;
 import android.view.*;
 import android.widget.*;
 import android.widget.AdapterView.OnItemClickListener;
 import tasktracker.controller.DatabaseAdapter;
 import tasktracker.model.Preferences;
 import tasktracker.model.ReadFromURL;
 import tasktracker.model.WebDBManager;
 import tasktracker.model.elements.*;
 
 /**
  * An activity that displays a list of tasks that a user can view and fulfill.
  * 
  * @author Jeanine Bonot
  * 
  */
 public class TaskListView extends Activity {
 
 	private ListView taskListView;
 	private List<Task> taskList;
 	public List<Task> webTaskList;
 	public List<Task> oldWebTaskList;
 	// private List<String> tasks;
 	private String[] tasks = new String[0];
 	private String _user;
 	// private PreferencesManager preferences;
 
 	private WebDBManager webManager;
 	private DatabaseAdapter _dbHelper;
 	private Cursor _cursor;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		webManager = new WebDBManager();
 		oldWebTaskList = new ArrayList<Task>();
 		webTaskList = new ArrayList<Task>();
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_task_list_view);
 
 		_user = Preferences.getUsername(this);
 		if (_user == Preferences.INVALID_ACCOUNT) {
 			//User has likely never signed in, force login screen
 			Intent intent = new Intent(getApplicationContext(), Login.class);
 			startActivity(intent);
 		}
 		_dbHelper = new DatabaseAdapter(this);
 
 		Log.d("TaskListView", "On Create");
 
 		setupToolbarButtons();
 		setupTaskList();
 		setupDebugFeatures();
 	}
 
 	protected void onStart() {
 		super.onStart();
 		_dbHelper.open();
 		fillData();
 		// loadTasks();
 		// contactWebserver webRequest = new contactWebserver();
 		// webRequest.execute();
 	}
 
 	protected void onStop() {
 		super.onStop();
 		_dbHelper.close();
 		stopManagingCursor(_cursor);
 		_cursor.close();
 	}
 
 	private void setupDebugFeatures() {
 		Button clearSQL = (Button) findViewById(R.id.button_clearSQL);
 		clearSQL.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				System.out.println("Button click");
 				_dbHelper.resetDatabase();
 				onStart();
 			}
 
 		});
 		
 		Button nukeCrowdSourcer = (Button) findViewById(R.id.button_nukeCrowdSourcer);
 		nukeCrowdSourcer.setOnClickListener(new View.OnClickListener() {
 			
 			public void onClick(View v) {
 				WebDBManager db = new WebDBManager();
 				db.nukeAll();
 				
 			}
 		});
 	}
 
 	private void setupToolbarButtons() {
 
 		Button buttonMyTasks = (Button) findViewById(R.id.buttonMyTasks);
 		Button buttonCreate = (Button) findViewById(R.id.buttonCreateTask);
 		Button buttonNotifications = (Button) findViewById(R.id.buttonNotifications);
 		buttonMyTasks.setEnabled(false);
 
 		buttonCreate.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				Intent intent = new Intent(getApplicationContext(),
 						CreateTaskView.class);
 				startActivity(intent);
 			}
 		});
 
 		buttonNotifications.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				Intent intent = new Intent(getApplicationContext(),
 						NotificationListView.class);
 				startActivity(intent);
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.account_menu, menu);
 		
 		MenuItem account = menu.findItem(R.id.Account_menu);
 		account.setTitle(_user);
 		
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// An Action Menu or Help menu item was selected
 		// By Mike
 		switch (item.getItemId()) {
 		case R.id.change_name:
 			changeName();
 			return true;
 		case R.id.help:
 			showHelp();
 			return true;
 		case R.id.logout:
 			Intent intent = new Intent(getApplicationContext(), Login.class);
 			startActivity(intent);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private void changeName() {
 		// InputPopup popup = new InputPopup();
 		InputPopup.make("Change Username",
 				"Old username was " + Preferences.getUsername(getBaseContext())
 						+ ". Please, keep it clean.", this,
 				InputPopup.Type.username);
 	}
 
 	private void showHelp() {
 		// TODO Create a help menu
 		showToast("Show help clicked");
 	}
 
 	private void setupTaskList() {
 		// Assign ListView and its on item click listener.
 		taskListView = (ListView) findViewById(R.id.taskList);
 		taskListView.setOnItemClickListener(new OnItemClickListener() {
 
 			public void onItemClick(AdapterView<?> a, View v, int i, long id) {
 				Intent intent = new Intent(getApplicationContext(),
 						TaskView.class);
 				TextView taskID = (TextView) ((RelativeLayout) v)
 						.findViewById(R.id.id);
				System.out.println("Bug = "+taskID.getText().toString());
 				intent.putExtra("TASK_ID",
 						taskID.getText().toString());
 				startActivity(intent);
 			}
 		});
 	}
 
 	private void showToast(String message) {
 		Toast toast = Toast.makeText(getApplicationContext(), message,
 				Toast.LENGTH_LONG);
 		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
 		toast.show();
 	}
 
 	private void fillData() {
 		_cursor = _dbHelper.fetchTasksAvailableToUser(_user);
 		startManagingCursor(_cursor);
 /*
 		String[] from = new String[] { DatabaseAdapter.ID,
 				DatabaseAdapter.TASK, DatabaseAdapter.USER,
 				DatabaseAdapter.DATE, DatabaseAdapter.COUNT,
 				DatabaseAdapter.DOWNLOADED, DatabaseAdapter.TEXT};
 		*/
 		String[] from = new String[] { DatabaseAdapter.ID,
 				DatabaseAdapter.TASK, DatabaseAdapter.USER,
 				DatabaseAdapter.DATE, DatabaseAdapter.COUNT,
 				DatabaseAdapter.DOWNLOADED, DatabaseAdapter.TEXT};
 		//int[] to = new int[] { R.id.id, R.id.item_title, R.id.item_text,
 		//		R.id.item_date_bottom, R.id.item_vote_count, R.id.downloaded, R.id.description };
 		int[] to = new int[] { R.id.id, R.id.item_title, R.id.item_text,
 				R.id.item_date_bottom, R.id.item_vote_count, R.id.downloaded, R.id.description };
 
 		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
 				R.layout.list_item, _cursor, from, to);
 
 		taskListView.setAdapter(adapter);
 		
 		//Now call an asynchronous method to populate the Database with items from Crowdsourcer
 		RequestDownloadTasksSummaries downloader = new RequestDownloadTasksSummaries(getBaseContext());
 		
 	}
 
 	private void update() {
 		taskList.addAll(webTaskList);
 		ArrayAdapter<Task> adapter = new ArrayAdapter<Task>(this,
 				R.layout.list_item, taskList);
 		taskListView.setAdapter(adapter);
 		oldWebTaskList = new ArrayList<Task>();
 		for (Task t : webTaskList) {
 			oldWebTaskList.add(t);
 		}
 	}
 
 	private class contactWebserver extends AsyncTask<Void, Void, Void> {
 		@Override
 		protected Void doInBackground(Void... temp) {
 
 			webTaskList = new ArrayList<Task>();
 			System.out.println("testing");
 			String[][] results = webManager.listTasksAsArrays();
 			String id;
 			Task newTask;
 			for (int n = 0; n < results.length; n++) {
 				if (results[n].length > 1) {
 					System.out.println("index =" + n);
 					id = results[n][1];
 					newTask = webManager.getTask(id);
 					webTaskList.add(newTask);
 				}
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void unused) {
 			// update UI with my objects
 			// taskList.addAll(webTaskList);
 
 			update();
 		}
 
 	}
 }
