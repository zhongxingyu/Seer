 package org.crazythink.TimeTrackerv1;
 
 import java.util.Calendar;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.Chronometer;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 
 public class TimeTracker extends ListActivity {
     private static final int ACTIVITY_PROJECT_CREATE=0;
     private static final int ACTIVITY_PROJECT_EDIT=1;
     private static final int ACTIVITY_TASK_CREATE=2;
     private static final int ACTIVITY_TASK_EDIT=3;
 
     private static final int PROJECT_INSERT_ID = Menu.FIRST;
     private static final int PROJECT_EDIT_ID = Menu.FIRST + 1;
     private static final int PROJECT_DELETE_ID = Menu.FIRST + 2;
     private static final int TASK_INSERT_ID = Menu.FIRST + 3;
     private static final int TASK_EDIT_ID = Menu.FIRST + 4;
     private static final int TASK_DELETE_ID = Menu.FIRST + 5;
 	
 	private DbAdapter mDbHelper;
     private Cursor mTasksCursor;
     private Cursor mProjectsCursor;
     private MergeProjectsTasksAdapter adapter;
     private long selectedRowId;
     
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mDbHelper = new DbAdapter(this);
 		mDbHelper.open();
         setContentView(R.layout.main);
         
         //fillInDb();
         fillData();
         registerForContextMenu(getListView());
     }
     
     private void fillData() {
     	//Setup cursors attached to database tables
         mProjectsCursor = mDbHelper.fetchAllProjectsWithChildCount();
         startManagingCursor(mProjectsCursor);
         mTasksCursor = mDbHelper.fetchAllTasksWithDuration();
         startManagingCursor(mTasksCursor);
         
         //Map databases fields to layout ids for CursorAdapter to fill in
         String[] projectFrom = new String[]{DbAdapter.KEY_NAME};
         int[] projectTo = new int[]{R.id.projectNameTextView};
         Section projects = new Section(mProjectsCursor, 
         		new setTagCursorAdapter(this, R.layout.project, mProjectsCursor, projectFrom, projectTo));
         
         String[] taskFrom = new String[]{DbAdapter.KEY_NAME, DbAdapter.KEY_DURATION};
         int[] taskTo = new int[]{R.id.taskNameTextView, R.id.taskTotalTimeTextView};
         Section tasks = new Section(mTasksCursor, 
         		new setTagCursorAdapter(this, R.layout.task, mTasksCursor, taskFrom, taskTo));
         
         //Send projects and tasks adapters to MergeProjectsTasksAdapter to bind together
         adapter=new MergeProjectsTasksAdapter(projects, tasks);
         
         //Attach adapter to list on main page
         setListAdapter(adapter);
     }
     
     //Close cursors and database connection on program destroy
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		
 		mProjectsCursor.close();
 		mTasksCursor.close();
 		mDbHelper.close();
 	}
     
 	//Handle click on Start/Stop Button within list
 	public void btnStartStopClickHandler(View v) {
 		//Button is passed in as v - get parent view needing change and sub-views
     	RelativeLayout vParentRow = (RelativeLayout)v.getParent();
     	Long taskId = (Long)vParentRow.getTag();
     	Button btn = (Button)v;
     	TextView timetext = (TextView)vParentRow.getChildAt(2);
     	Chronometer chrono = (Chronometer)vParentRow.getChildAt(3);
     	Log.d("DEBUG:","chrono id " + chrono + " timetext " + timetext);
     	
     	//Check to see if the tasks is already running
     	Cursor c = mDbHelper.fetchRunningTimeByTask(taskId);
     	if(c != null && c.getCount() == 1 ) {
     		//Task is currently running - so stop
     		long rowId = c.getLong(c.getColumnIndex(DbAdapter.KEY_ROWID));
     		long startTime = c.getLong(c.getColumnIndex(DbAdapter.KEY_START_TIME));
     		String description = c.getString(c.getColumnIndex(DbAdapter.KEY_DESCRIPTION));
     		long entryId = mDbHelper.createEntry(description, startTime, taskId);
     		
     		if(entryId > 0) {
     			Log.v("DEBUG:", "deleted running time entry for task id " + taskId);
     			mDbHelper.deleteRunningTime(rowId);
     		}
     		else
     			Log.e("ERROR:", "deleting running time entry for task id " + taskId);
     		
     		//Change button text to start
     		btn.setText(R.string.start);
     		//Change background to normal
     		vParentRow.setBackgroundResource(R.drawable.rows);
     		//Stop chronometer (stopwatch) and hide
     		chrono.stop();
     		chrono.setVisibility(View.GONE);
     		//Show totalTimeTextView
     		timetext.setVisibility(View.VISIBLE);
     		
     		//Requery adapter cursor
     		adapter.getTasksCursor().requery();
     		adapter.notifyDataSetChanged();
     	}
     	else {
     		//Task is not running - start it
     		long rtId = mDbHelper.createRunningTime(taskId);
     		if (rtId > 0)
     			Log.v("DEBUG:", "created running time entry for task id " + taskId);
     		else
     			Log.e("ERROR:", "creating running time entry for task id " + taskId);
     		
     		//Change button text to Stop and change background to running image
     		btn.setText(R.string.stop);
     		vParentRow.setBackgroundResource(R.drawable.rows_flat_color);
     		
     		//Hide totalTimeTextView and show chronometer while starting it
     		timetext.setVisibility(View.GONE);
     		chrono.setVisibility(View.VISIBLE);
     		chrono.setBase(SystemClock.elapsedRealtime());
     		chrono.start();
     	}
     	//Close cursor
     	c.close();
     }
 	
 	//Handle short click on list item
 	@Override
 	public void onListItemClick(ListView parent, View v, int position, long id) {
 		super.onListItemClick(parent, v, position, id);
 		int vId = v.getId();
 		Intent i;
 		
 		if (vId == R.id.projectNameTextView) {
 			//Project view clicked
 			Log.v("DEBUG: ", "edit project viewid " + id);
 		}
 		else if (vId == R.id.taskBody) {
 			//Task view clicked
 			Log.v("DEBUG: ", "task entries viewid " + id);
 			
 			Cursor c = mDbHelper.fetchRunningTimeByTask(id);
 	    	if(c != null && c.getCount() == 1 ) {
 	    		//Task is currently running - so open description dialog
 	    		selectedRowId = c.getLong(c.getColumnIndex(DbAdapter.KEY_ROWID));
 	    		String description = c.getString(c.getColumnIndex(DbAdapter.KEY_DESCRIPTION));
	    		c.close();
 	    		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 	    		alert.setTitle(R.string.description);
 	    		alert.setMessage(R.string.task_description_message);
 	    		//alert.setMessage("editing running entry id " + rowId);
 	    		final EditText input = new EditText(this);
 	    		input.setText(description);
 	    		alert.setView(input);
 	    		
 	    		alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 					
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						String value = input.getText().toString().trim();
 						Log.d("DEBUG","dialog " + dialog + " which " + which);
 						if(mDbHelper.updateRunningTimeDescription(selectedRowId, value)) {
 							Toast.makeText(getApplicationContext(), "Description '" + value + "' saved", Toast.LENGTH_SHORT).show();
 						}
 					}
 				});
 				
 				alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
 					
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.cancel();
 					}
 				});
 				
 				alert.show();
 				
 	    	}
 	    	else {
 	    		//Task is not running - so open entries list activity
 		        i = new Intent(this, Entries.class);
 		        i.putExtra(DbAdapter.KEY_TASK_ID, id);
 		        startActivity(i);
 	    	}
 		}
 		else
 			Log.e("ERROR: ", "unknown viewId " + vId);
 	}
 	
 	//fillData after return from activity that gives a result
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
         super.onActivityResult(requestCode, resultCode, intent);
         fillData();
     }
 	
     //Create menu for click of "menu" button
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         menu.add(0, PROJECT_INSERT_ID, 0, R.string.project_insert);
         return true;
     }
     
     //Handle menu selection click
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
         switch(item.getItemId()) {
             case PROJECT_INSERT_ID:
                 Intent i = new Intent(this, ProjectEdit.class);
                 startActivityForResult(i, ACTIVITY_PROJECT_CREATE);
                 return true;
         }
 
         return super.onMenuItemSelected(featureId, item);
     }
 
     //Create menu for long click
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v,
             ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         
         /*
          * Information is passed in the menuInfo variable
          * with: id, position, and targetView
          * Get what type of view clicked on with viewId
          * and the id for what was clicked on with rowId
          */
         AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
         long clickedViewId = Long.valueOf(info.targetView.getId());
         long rowId = info.id;
 
         if (clickedViewId == R.id.projectNameTextView) {
         	//Project was long-clicked
         	TextView clickedView = (TextView)info.targetView;
         	Log.v("DEBUG: ", "long click project id " + rowId);
         	Log.v("DEBUG: ", "long click viewtype " + clickedView);
         	menu.setHeaderTitle(clickedView.getText());
         	menu.add(0, PROJECT_EDIT_ID, 0, R.string.edit_project);
         	menu.add(0, PROJECT_DELETE_ID, 1, R.string.project_delete);
         	menu.add(0, TASK_INSERT_ID, 2, R.string.task_insert);
 		}
 		else if (clickedViewId == R.id.taskBody) {
 			//Task was long-clicked
 			ViewGroup clickedView = (ViewGroup)info.targetView;
 			TextView childView = (TextView)clickedView.getChildAt(1);
 			Log.v("DEBUG: ", "long click task id " + rowId);
 			menu.setHeaderTitle(childView.getText());
 			menu.add(0, TASK_EDIT_ID, 0, R.string.edit_task);
 			menu.add(0, TASK_DELETE_ID, 1, R.string.task_delete);
         }
 		else {
 			Log.e("ERROR: ", "unknown viewId " + clickedViewId);
 	        Log.v("DEBUG: ", "menuinfo row id " + rowId);
 	        Log.v("DEBUG: ", "menuinfo position " + info.position);
 		}
         
     }
 
     //Handle long-click menu selection
     @Override
     public boolean onContextItemSelected(MenuItem item) {
     	AdapterContextMenuInfo info;
     	Intent i;
     	switch(item.getItemId()) {
 			case PROJECT_DELETE_ID:
 				info = (AdapterContextMenuInfo) item.getMenuInfo();
 				Log.v("DEBUG:", "project delete on id: " + info.id);
 				mDbHelper.deleteProject(info.id);
 				fillData();
 				return true;
 			case TASK_DELETE_ID:
 				info = (AdapterContextMenuInfo) item.getMenuInfo();
 				Log.v("DEBUG:", "task delete on id: " + info.id);
 				mDbHelper.deleteTask(info.id);
 				fillData();
 				return true;
 			case TASK_INSERT_ID:
 				info = (AdapterContextMenuInfo) item.getMenuInfo();
 				i = new Intent(this, TaskEdit.class);
 				i.putExtra(DbAdapter.KEY_PROJECT_ID, info.id);
 				startActivityForResult(i, ACTIVITY_TASK_CREATE);
 				return true;
 			case TASK_EDIT_ID:
 				info = (AdapterContextMenuInfo) item.getMenuInfo();
 				i = new Intent(this, TaskEdit.class);
 				i.putExtra(DbAdapter.KEY_ROWID, info.id);
 				startActivityForResult(i, ACTIVITY_TASK_EDIT);
 				return true;
 			case PROJECT_EDIT_ID:
 				info = (AdapterContextMenuInfo) item.getMenuInfo();
 				i = new Intent(this, ProjectEdit.class);
 				i.putExtra(DbAdapter.KEY_ROWID, info.id);
 				startActivityForResult(i, ACTIVITY_PROJECT_EDIT);
 				return true;
         }
         return super.onContextItemSelected(item);
     }
 	
     
     //Extend SimpleCursorAdapter with custom bindView for tasks
 	class setTagCursorAdapter extends SimpleCursorAdapter {
 		setTagCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
 			super(context, layout, c, from, to);
 		}
 		
 		@Override
 		public void bindView(View v, Context context, Cursor cursor) {
 			super.bindView(v, context, cursor);
 			
 			long taskId = cursor.getLong(cursor.getColumnIndex(DbAdapter.KEY_ROWID));
 			v.setTag(taskId);
 			
 			int vId = v.getId();
 			if (vId == R.id.taskBody) {
 				//View passed in is task body (viewGroup)
 				Cursor runningTasks = mDbHelper.fetchRunningTimeByTask(taskId);
 				ViewGroup vg = (ViewGroup)v;
 				Button btn = (Button)vg.getChildAt(0);
 		    	TextView timeTextView = (TextView)vg.getChildAt(2);
 		    	Chronometer chrono = (Chronometer)vg.getChildAt(3);
 		    	//Log.d("DEBUG:","button: " + btn + "chrono id " + chrono + " timetext " + timetext);
 			
 				if(runningTasks != null && runningTasks.getCount() == 1 ) {
 					//This task is currently running
 					//Set it up with Stop button and chronometer
 		    		btn.setText(R.string.stop);
 		    		v.setBackgroundResource(R.drawable.rows_flat_color);
 		    		
 		    		timeTextView.setVisibility(View.GONE);
 		    		chrono.setVisibility(View.VISIBLE);
 		    		runningTasks.moveToFirst();
 		    		Long timediff = Calendar.getInstance().getTime().getTime() - runningTasks.getLong(runningTasks.getColumnIndex(DbAdapter.KEY_START_TIME));
 		    		chrono.setBase(SystemClock.elapsedRealtime() - timediff);
 		    		chrono.start();
 		    	}
 		    	else {
 		    		//This task is not running
 		    		//Set up with Start button and no chronometer, but with totalTime
 		    		btn.setText(R.string.start);
 		    		v.setBackgroundResource(R.drawable.rows);
 		    		chrono.setVisibility(View.GONE);
 		    		timeTextView.setVisibility(View.VISIBLE);
 		    	}
 				runningTasks.close();
 				
 				//Receive minutes as int - change to show hours and minutes
 				int totalTime = cursor.getInt(cursor.getColumnIndex(DbAdapter.KEY_DURATION));
 				int hours = totalTime / 60;
 				int minutes = totalTime % 60; 
 				String formattedTime;
 				if (hours > 0)
 					formattedTime = hours + "h " + minutes + "m";
 				else
 					formattedTime = minutes + "m";
 				timeTextView.setText(formattedTime);
 			}
 		}
 	}
     
     private void fillInDb() {
     	//Some initial data for the database
 		long p1 = mDbHelper.createProject("test project");
 		long p2 = mDbHelper.createProject("another project");
 		mDbHelper.createTask("task1", "test desc", p1);
 		mDbHelper.createTask("task2", "test desc", p1);
 		mDbHelper.createTask("task3", "test desc", p2);
 		mDbHelper.createTask("task4", "test desc", p2);
 		mDbHelper.createTask("task5", "test desc", p1);
 		mDbHelper.createTask("task6", "test desc", p1);
     }
     
     
 }
