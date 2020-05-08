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
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.text.method.KeyListener;
 import android.util.Log;
 import android.view.*;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.widget.*;
 import android.widget.AdapterView.OnItemClickListener;
 import tasktracker.controller.DatabaseAdapter;
 import tasktracker.model.Preferences;
 //import tasktracker.model.ReadFromURL;
 //import tasktracker.model.WebDBManager;
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
 	// public List<Task> webTaskList;
 	// public List<Task> oldWebTaskList;
 	// private List<String> tasks;
 	private String[] tasks = new String[0];
 	private String _user;
 	// private PreferencesManager preferences;
 	private String[] _keywords = new String[0];
 	private EditText filterText;
 	// private WebDBManager webManager;
 	private DatabaseAdapter _dbHelper;
 	private Cursor _cursor;
 	private SimpleCursorAdapter adapter;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		// webManager = new WebDBManager();
 		// oldWebTaskList = new ArrayList<Task>();
 		// webTaskList = new ArrayList<Task>();
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_task_list_view);
 
 		_user = Preferences.getUsername(this);
 		if (_user == Preferences.INVALID_ACCOUNT) {
 			// User has likely never signed in, force login screen
 			Intent intent = new Intent(getApplicationContext(), Login.class);
 			startActivity(intent);
 		}
 		_dbHelper = new DatabaseAdapter(this);
 
 		Log.d("TaskListView", "On Create");
 
 		setupToolbarButtons();
 		setupTaskList();
 		setupDebugFeatures();
 	}
 	
 	void createDemoTasks(){
 
 		RequestCreateTask request;
 		Task task;
 
 		task = new Task("pennycandy", "Aww! I found a tiny shoe!", "Is it yours? Let me know!");
 		request = new RequestCreateTask(getBaseContext(), task);
 		
 		task = new Task("bobby7", "Lost: cuddly teddy bear", "He protects me from the scary monsters!");
 		request = new RequestCreateTask(getBaseContext(), task);
 
 		task = new Task("go4brny", "Wait for dairy", "What is uuuupppp!");
 		request = new RequestCreateTask(getBaseContext(), task);
 
 		task = new Task("felinehothothot", "Google \'Cats\'", "Just do it.");
 		request = new RequestCreateTask(getBaseContext(), task);
 		
 		task = new Task("hun-grie", "Find a kitty cat-themed lunchbox", "Meeow!");
 		request = new RequestCreateTask(getBaseContext(), task);
 
 		task = new Task("2H1P", "Photograph Your Lunches", "I want to know what you guys eat.");
 		request = new RequestCreateTask(getBaseContext(), task);
 		
 		task = new Task("lay-z", "Someone do my homework plz.", "99 problems and I won't do one.");
 		request = new RequestCreateTask(getBaseContext(), task);
 		
 		task = new Task("litricher", "Need book suggestions", "Send me some suggestions of books to read.");
 		request = new RequestCreateTask(getBaseContext(), task);
 		
 		task = new Task("qwerty", "Buy groceries", "Broke my leg and I can't walk to the market.  Take pictures of food you're going to buy me.  Thanks");
 		request = new RequestCreateTask(getBaseContext(), task);
 	}
 
 	protected void onStart() {
 		super.onStart();
 		// Now call an asynchronous method to populate the Database with items
 		// from Crowdsourcer
 		RequestDownloadTasksSummaries downloader = new RequestDownloadTasksSummaries(
 				getBaseContext());
 
 //		createDemoTasks();
 		_dbHelper.open();
 		fillData(new String[0]);
 	}
 
 	protected void onStop() {
 		super.onStop();
 		_dbHelper.close();
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
 	}
 	
 
 	private void setupToolbarButtons() {
 		filterText = (EditText) findViewById(R.id.search_box);
 		//filterText.addTextChangedListener(filterTextWatcher);
 		Button buttonMyTasks = (Button) findViewById(R.id.buttonMyTasks);
 		Button buttonCreate = (Button) findViewById(R.id.buttonCreateTask);
 		Button buttonNotifications = (Button) findViewById(R.id.buttonNotifications);
 //		buttonMyTasks.setEnabled(false);
 		
 		buttonMyTasks.setOnClickListener(new OnClickListener(){
 
 			public void onClick(View v) {
 				onStart();
 			}
 			
 		});
 
 		buttonCreate.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				Intent intent = new Intent(getApplicationContext(),
 						CreateTaskView.class);
 				startActivity(intent);
 			}
 		});
 
 //		buttonNotifications.setOnClickListener(new View.OnClickListener() {
 //
 //			public void onClick(View v) {
 //				Intent intent = new Intent(getApplicationContext(),
 //						NotificationListView.class);
 //				startActivity(intent);
 //			}
 //		});
 		
 		Button buttonSearch = (Button) findViewById(R.id.search_tasks);
 		
 		buttonSearch.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				
 				fillData(_keywords);
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
 		// case R.id.change_name:
 		// changeName();
 		// return true;
 		// case R.id.help:
 		// showHelp();
 		// return true;
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
 
 	private void setupTaskList() {
 		// Assign ListView and its on item click listener.
 		taskListView = (ListView) findViewById(R.id.taskList);
 		taskListView.setOnItemClickListener(new OnItemClickListener() {
 
 			public void onItemClick(AdapterView<?> a, View v, int i, long id) {
 				Intent intent = new Intent(getApplicationContext(),
 						TaskView.class);
				TextView taskID = (TextView) ((LinearLayout) v)
 						.findViewById(R.id.id);
 				System.out.println("Bug (Task ID passed)= "
 						+ taskID.getText().toString());
 				intent.putExtra("TASK_ID", taskID.getText().toString());
 				startActivity(intent);
 			}
 		});
 
 		filterText = (EditText) findViewById(R.id.search_box);
 		Log.d("TaskListView",
 				"null filtertext = " + Boolean.toString(filterText == null));
 
 		filterText.setOnKeyListener(new OnKeyListener(){
 
 			public boolean onKey(View view, int keyCode, KeyEvent event) {
 		        // If the event is a key-down event on the "enter" button
 		        if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
 		            (keyCode == KeyEvent.KEYCODE_ENTER)) {
 		          // Perform action on key press
 		          fillData(_keywords);
 		          filterText.requestFocus();
 		          return true;
 		        }
 		        return false;
 			}
 			
 		});
 		
 		filterText.addTextChangedListener(new TextWatcher() {
 
 			public void afterTextChanged(Editable s) {
 
 				_keywords = filterText.getText().toString()
 						.split("(\\s+)?,(\\s+)?");
 				
 				if (_keywords[0].matches("")) {
 					_keywords = new String[0];
 					fillData(_keywords);
 				}
 				//Log.d("TaskListView", filterText.getText().toString());
 
 				//fillData(keywords);
 			}
 
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 				// Do nothing.
 			}
 
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 				// Do nothing.
 			}
 
 		});
 	}
 
 	private void fillData(String[] filterWords) {
 		
 		_cursor = _dbHelper.fetchTasksAvailableToUser(_user, filterWords);
 		startManagingCursor(_cursor);
 
 		String[] from = new String[] { DatabaseAdapter.ID,
 				DatabaseAdapter.TASK, DatabaseAdapter.USER,
 				DatabaseAdapter.DATE, DatabaseAdapter.COUNT,
 				DatabaseAdapter.DOWNLOADED, DatabaseAdapter.TEXT };
 		int[] to = new int[] { R.id.id, R.id.item_title, R.id.item_text,
 				R.id.item_date_bottom, R.id.item_vote_count, R.id.downloaded,
 				R.id.description };
 
 		adapter = new SimpleCursorAdapter(this, R.layout.list_item, _cursor,
 				from, to);
 		// adapter.getFilter().filter('p');
 		// adapter.setStringConversionColumn(_cursor.getColumnIndex(DatabaseAdapter.TASK));
 		// adapter.setFilterQueryProvider(new FilterQueryProvider() {
 		//
 		// public Cursor runQuery(CharSequence constraint) {
 		// String partialItemName = null;
 		// if (constraint != null) {
 		// partialItemName = constraint.toString();
 		// }
 		// _dbHelper.
 		// return groceryDb.suggestItemCompletions(partialItemName);
 		// }
 		// });
 
 		taskListView.setAdapter(adapter);
 
 		stopManagingCursor(_cursor);
 
 
 	}
 
 	// private void filterTasks(String filterText){
 	// taskListView.setFilterText("photo");
 	// }
 
 //	private TextWatcher filterTextWatcher = new TextWatcher() {
 //
 //		public void afterTextChanged(Editable s) {
 //		}
 //
 //		public void beforeTextChanged(CharSequence s, int start, int count,
 //				int after) {
 //		}
 //
 //		public void onTextChanged(CharSequence s, int start, int before,
 //				int count) {
 //			//adapter.getFilter().filter(s);
 //		}
 //
 //	};
 }
