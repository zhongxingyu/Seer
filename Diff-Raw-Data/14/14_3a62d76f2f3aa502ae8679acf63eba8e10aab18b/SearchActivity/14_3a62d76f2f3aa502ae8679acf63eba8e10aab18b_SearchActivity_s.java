 /**
  * Copyright 2013 Alex Wong, Ashley Brown, Josh Tate, Kim Wu, Stephanie Gil
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package ca.ualberta.cmput301f13t13.storyhoard.gui;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import ca.ualberta.cmput301f13t13.storyhoard.R;
 import ca.ualberta.cmput301f13t13.storyhoard.backend.LifecycleData;
 import ca.ualberta.cmput301f13t13.storyhoard.backend.Story;
 import ca.ualberta.cmput301f13t13.storyhoard.controllers.LocalStoryController;
 import ca.ualberta.cmput301f13t13.storyhoard.controllers.ServerStoryController;
 
 /**
  * Search Activity
  * 
  * Allows user to search for a specific story
  * 
  * @author Kim Wu
  * 
  */
 
 public class SearchActivity extends Activity {
 	private Button searchButton;
 	private EditText titleInput;
 	private Spinner spinner;
 	private LocalStoryController localCon;
 	private ServerStoryController serverCon;
 	private LifecycleData lifedata;
 	private enum Type {AUTHOR, CACHED, PUBLISHED};
 	private Type viewType = Type.AUTHOR;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_search);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		lifedata = LifecycleData.getInstance();
 		serverCon = ServerStoryController.getInstance(this);
 		localCon = LocalStoryController.getInstance(this);
 		searchButton = (Button) findViewById(R.id.searchButton);
 		titleInput = (EditText) findViewById(R.id.story_name);
 		spinner = (Spinner) findViewById(R.id.search_spinner);
 		onSpinnerClick();
 		setSearchListener();
 	}
 
 	private void setSearchListener() {
 		searchButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				String title = titleInput.getText().toString();
 				title = title.trim();
 				title = title.replaceAll("[\n\r]", "");
 
 				if (validInput(title)) {
 					// Correct Input: will save data to database and refresh
 					// activity.
 					search(title);
 				} else {   
 					// Invalid Input types
 					alertDialog();
 				}
 			}
 		});		
 	}
 	
 	private void search(String title) {
 		ArrayList<Story> stories = new ArrayList<Story>();
 		if (viewType == Type.AUTHOR) {
 			stories = localCon.searchAuthorStories(title);
 		} else if (viewType == Type.CACHED) {
			stories = localCon.searchAuthorStories(title);
 		} else {
 			stories = serverCon.searchByTitle(title);
 		}
 		
 		lifedata.setStoryList(stories);
 		Intent intent = new Intent(getBaseContext(),
 				SearchResultsActivity.class);
 		finish();
 		startActivity(intent);		
 	}
 	
 	private void alertDialog() {
 		AlertDialog.Builder alert = new AlertDialog.Builder(
 				SearchActivity.this);
 		alert.setTitle("Whoopsies!")
 				.setMessage("Story title is empty/invalid")
 				.setCancelable(false)
 				// cannot dismiss this dialog
 				.setPositiveButton("Ok",
 						new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(
 									DialogInterface dialog,
 									int which) {
 								dialog.cancel();
 							}
 						}); // parenthesis mean an anonymous class
 		// Show alert dialog
 		AlertDialog show_alert = alert.create();
 		show_alert.show();		
 	}
 	
 	// When the spinner is clicked
 	private void onSpinnerClick() {
 		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 			@Override
 			public void onItemSelected(AdapterView<?> adapter, View v,
 					int position, long id) {
 				if (position == 0) {
 					viewType = Type.AUTHOR;
 				} else if (position == 1) {
 					viewType = Type.CACHED;
 				} else if (position == 2) {
 					viewType = Type.PUBLISHED;
 				}
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 				// TODO Auto-generated method stub
 			}
 		});
 	}
 
 	// Checks to see if story title is empty
 	private boolean validInput(String userInput) {
 		int length = userInput.length();
 		if (length == 0) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	// MENU INFORMATION
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.search, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle presses on the action bar items
 		switch (item.getItemId()) {
 		case R.id.browse_stories:
 			Intent browse = new Intent(this, ViewBrowseStories.class);
 			startActivity(browse);
 			return true;
 		case R.id.add_story:
 			Intent add = new Intent(this, EditStoryActivity.class);
 			add.putExtra("isEditing", false);
 			startActivity(add);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 }
