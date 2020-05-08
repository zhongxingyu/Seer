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
 import android.os.AsyncTask;
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
 import ca.ualberta.cmput301f13t13.storyhoard.dataClasses.Story;
 import ca.ualberta.cmput301f13t13.storyhoard.helpGuides.InfoActivity;
 import ca.ualberta.cmput301f13t13.storyhoard.local.StoryManager;
 import ca.ualberta.cmput301f13t13.storyhoard.serverClasses.ServerManager;
 
 /**
  * Search Activity
  * 
  * Allows user to enter keywords of the title of 
  * a published, downloaded or personal story they 
  * wish to find and read. Results of their search 
  * will be displayed in the search results activity. 
  * 
  * @author Kim Wu
  */
 
 public class SearchActivity extends Activity {
 	private Button searchButton;
 	private EditText titleInput;
 	private Spinner spinner;
 	private StoryManager storyMan;
 	private ServerManager serverMan;
 	private LifecycleData lifedata;
 
 	private enum Type {
 		AUTHOR, CACHED, PUBLISHED
 	};
 
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
 		serverMan = ServerManager.getInstance();
 		storyMan = StoryManager.getInstance(this);
 		searchButton = (Button) findViewById(R.id.searchButton);
 		titleInput = (EditText) findViewById(R.id.story_name);
 		spinner = (Spinner) findViewById(R.id.search_spinner);
 		onSpinnerClick();
 		setSearchListener();
 	}
 
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
 		case R.id.info:
 			getHelp();
 		default:
 			return super.onOptionsItemSelected(item);
 		}
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
 					new SearchKeywords().execute(title);
 				} else {
 					// Invalid Input types
 					alertDialog();
 				}
 			}
 		});
 	}
 
 	private class SearchKeywords extends AsyncTask<String, Void, Void> {
 		@Override
 		protected synchronized Void doInBackground(String... params) {
 			// search for a story
 			search((String) params[0]);
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 			super.onPostExecute(result);
 			finish();
 		}
 	}
 
 	private void search(String title) {
 		ArrayList<Story> stories = new ArrayList<Story>();
 		Intent intent = new Intent(getBaseContext(),
 				SearchResultsActivity.class);
 
 		if (viewType == Type.AUTHOR) {
 			stories = storyMan.searchAuthorStories(title);
 			intent.putExtra("isPublished", false);
 		} else if (viewType == Type.CACHED) {
 			stories = storyMan.searchCachedStories(title);
 			intent.putExtra("isPublished", false);
 		} else {
 			stories = serverMan.searchByKeywords(title);
 			intent.putExtra("isPublished", true);
 		}
 		lifedata.setSearchResults(stories);
 		startActivity(intent);
 	}
 
 	private void alertDialog() {
 		AlertDialog.Builder alert = new AlertDialog.Builder(SearchActivity.this);
 		alert.setTitle("Whoopsies!").setMessage("Story title is empty/invalid")
 				.setCancelable(false)
 				// cannot dismiss this dialog
 				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
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
 
 	/**
 	 * Displays help guide for ViewBrowseStories
 	 */
 	private void getHelp() {
 		Intent intent = new Intent(this, InfoActivity.class);
		String helpInfo = "Type a story you want to search "
 				+ "where it says Story title.\n\n"
				+ "Select from the dropdown which type of story "
 				+ "you would like to search for.\n\n"
				+ "Click the search stories button ";
 		intent.putExtra("theHelp", helpInfo);
 		startActivity(intent);
 	}
 }
