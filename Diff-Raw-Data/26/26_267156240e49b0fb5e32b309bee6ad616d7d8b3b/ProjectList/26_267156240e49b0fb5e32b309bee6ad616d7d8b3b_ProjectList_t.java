 /*
  * Copyright 2011 Andrew Anderson
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * 	-Redistributions of source code must retain the above copyright notice,
  * 	 this list of conditions and the following disclaimer.
  * 
  * 	-Redistributions in binary form must reproduce the above copyright
  * 	 notice, this list of conditions and the following disclaimer in the
  * 	 documentation and/or other materials provided with the distribution.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.example.knitknit;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 
 public class ProjectList extends ListActivity {
 	private static final String TAG = "bunny-knitknit-ProjectList";
 	private static final int ACTIVITY_VIEW = 1;
 
 	private DatabaseHelper mDatabaseHelper;
 	private static final int MENU_ADD = Menu.FIRST;
 	private static final int MENU_DELETE = Menu.FIRST + 1;
 	private static final int MENU_RENAME = Menu.FIRST + 2;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.projectlist);
 
 		// Open database
 		mDatabaseHelper = new DatabaseHelper(this);
 		mDatabaseHelper.open();
 
 		// Fill the list with the projects from the database
 		fillList();
 
 		registerForContextMenu(getListView());
 	}
 
 	@Override
 	public void onWindowFocusChanged(boolean hasFocus) {
 		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
 			fillList();
		}
 	}
 
 	/* Context Menu ******************************************************/
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 		ContextMenuInfo menuInfo)
 	{
 		super.onCreateContextMenu(menu, v, menuInfo);
 		menu.add(0, MENU_DELETE, 0, R.string.projectlist_delete);
 		menu.add(0, MENU_RENAME, 0, R.string.projectlist_rename);
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		// Get the ID the list item (same as projectID)
 		AdapterContextMenuInfo info =
 			(AdapterContextMenuInfo) item.getMenuInfo();
 
 		switch(item.getItemId()) {
 		case MENU_DELETE:
 			deleteProject(info.id);
 			fillList();
 			return true;
 		case MENU_RENAME:
 			renameProject(info.id,
 				(String)
 				((TextView) info.targetView.findViewById(
 					R.id.projectlist_row_name)).getText());
 			return true;
 		}
 		return super.onContextItemSelected(item);
 	}
 
 	private void deleteProject(long projectID) {
 		// TODO: Add confirmation dialog
 		mDatabaseHelper.deleteProject(projectID);
 	}
 
 	private void renameProject(long projectID, String currentName) {
 		showNameDialog(projectID, currentName);
 	}
 
 	/* Options Menu ******************************************************/
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		menu.add(0, MENU_ADD, 0, R.string.projectlist_add);
 		return true;
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureID, MenuItem item) {
 		switch(item.getItemId()) {
 		case MENU_ADD:
 			createProject();
 			return true;
 		}
 
 		return super.onMenuItemSelected(featureID, item);
 	}
 
 	private void createProject() {
 		showNameDialog();
 	}
 
 	// Overloaded version for creating only
 	private void showNameDialog() {
 		showNameDialog(null, null);
 	}
 
 	// @projectID
 	//	-If null, we are creating a project
 	//	-If a number, we are renaming the project with this projectID
 	private void showNameDialog(final Long projectID, String currentName) {
 		// Instantiate a view of projectlist_namedialog.xml
 		LayoutInflater inflater = (LayoutInflater)
 			this.getSystemService(LAYOUT_INFLATER_SERVICE);
 		View view = inflater.inflate(R.layout.projectlist_namedialog,
 			null, false);
 
 		// Find the name EditText field
 		final EditText name = (EditText)
 			view.findViewById(R.id.projectlist_namedialog_name);
 
 		// If we are renaming, put the current name in the box
 		if (currentName != null) {
 			name.append(currentName);
 		}
 
 		DialogInterface.OnClickListener listener =
 			new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				if (projectID == null) {
 					mDatabaseHelper.insertProject(
 						name.getText().toString());
 					fillList();
 				} else {
 					mDatabaseHelper.renameProject(
 						projectID,
 						name.getText().toString());
 				}
 				return;
 			}
 			};
 
 		// Create an AlertDialog and set its attributes
 		AlertDialog.Builder nameDialog =
 			new AlertDialog.Builder(this);
 		nameDialog.setCancelable(true);
 		nameDialog.setTitle(
 			(projectID == null ?
 			R.string.projectlist_namedialog_title_create :
 			R.string.projectlist_namedialog_title_rename));
 		nameDialog.setPositiveButton(
 			(projectID == null ?
 				R.string.projectlist_namedialog_create :
 				R.string.projectlist_namedialog_rename),
 			listener);
 
 		// Fill the dialog with the view
 		nameDialog.setView(view);
 		nameDialog.create();
 
 		// Show the dialog
 		nameDialog.show();
 	}
 
 
 	/* List **************************************************************/
 	@Override
 	protected void onListItemClick(ListView l, View v, int position,
 		long id)
 	{
 		super.onListItemClick(l, v, position, id);
 
 		Intent i = new Intent(this, CountingLand.class);
 		i.putExtra(DatabaseHelper.PROJECT_KEY_ID, id);
 
 		Log.w(TAG, "ID stored: " + id);
 
 		startActivityForResult(i, ACTIVITY_VIEW);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode,
 		Intent intent)
 	{
 		super.onActivityResult(requestCode, resultCode, intent);
 		fillList();
 	}
 
 	private void fillList() {
 		// Get a cursor over a list of all projects
 		Cursor cursor = mDatabaseHelper.fetchProjects();
 		startManagingCursor(cursor);
 
 		// Create an array to specify the fields we want to display in
 		// the list
 		String[] from = new String[] {
 			DatabaseHelper.PROJECT_KEY_NAME};
 
 		// Create and an array of the fields we want to bind those
 		// fields to
 		int[] to = new int[] {
 			R.id.projectlist_row_name};
 
 		// Now create a simple cursor adapter and set it to display
 		SimpleCursorAdapter projects = 
 			new SimpleCursorAdapter(
 				this, R.layout.projectlist_row, cursor,
 				from, to);
 
 		setListAdapter(projects);
 	}
 }
