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
 package ca.ualberta.cs.c301f13t13.gui;
 
 import java.util.ArrayList;
 
 import android.app.ActionBar;
 import android.app.ActionBar.OnNavigationListener;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.GridView;
 import ca.ualberta.cmput301f13t13.storyhoard.R;
 import ca.ualberta.cs.c301f13t13.backend.SHController;
 import ca.ualberta.cs.c301f13t13.backend.Story;
 
 /**
  * Class which displays all stories in a grid, handles different view types.
  * 
  * @author alexanderwong
  * 
  */
 public class ViewBrowseStories extends Activity {
 
 	private GridView gridView;
 	private ArrayList<Story> gridArray = new ArrayList<Story>();
 	private StoriesViewAdapter customGridAdapter;
 	private SHController gc;
 	int viewType = SHController.CREATED;
 
 	/**
 	 * Create the View Browse Stories activity
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_view_browse_stories);
 
 		// Set up the action bar to show a dropdown list.
 		final ActionBar actionBar = getActionBar();
 		actionBar.setTitle("StoryHoard");
 		actionBar.setDisplayShowTitleEnabled(true);
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
 
 		// Setup the action bar items
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
 				actionBar.getThemedContext(),
 				android.R.layout.simple_list_item_1,
 				android.R.id.text1,
 				new String[] {
 						getString(R.string.title_viewBrowseStories_MyStories),
 						getString(R.string.title_viewBrowseStories_CachedStories),
 						getString(R.string.title_viewBrowseStories_PublishedStories), });
 
 		// Setup the action bar listener
 		actionBar.setListNavigationCallbacks(adapter,
 				new OnNavigationListener() {
 					@Override
 					public boolean onNavigationItemSelected(int itemPosition,
 							long itemId) {
 						if (itemPosition == 0) {
 							viewType = SHController.CREATED;
 						} else if (itemPosition == 1) {
 							viewType = SHController.CACHED;
 						} else if (itemPosition == 2) {
 							viewType = SHController.PUBLISHED;
 						}
 						refreshStories();
 						return true;
 					}
 				});
 
 		// Setup the grid view for the stories
 		gridView = (GridView) findViewById(R.id.gridStoriesView);
 		customGridAdapter = new StoriesViewAdapter(this, R.layout.browse_story_item,
 				gridArray);
 		gridView.setAdapter(customGridAdapter);
 
 		// Setup the grid view click listener
 		gridView.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				// Handle going to view story activity
 				Intent intent = new Intent(getBaseContext(),
 						ViewBrowseStory.class);
 				intent.putExtra("storyID", gridArray.get(arg2).getId());
 				startActivity(intent);
 			}
 		});
 
 	}
 
 	/**
 	 * Handle the creation of the View Browse Stories activity menu
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.view_browse_stories, menu);
 		return true;
 	}
 
 	/**
 	 * Handle the selection of the View Browse Stories activity menu items
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		Intent intent;
 		switch (item.getItemId()) {
 		case R.id.addNewStory:
 			intent = new Intent(this, EditStoryActivity.class);
 			// Pass it a boolean to indicate it is not editing
 			intent.putExtra("isEditing", false);
 			startActivity(intent);
 			return true;
 		case R.id.searchStories:
 			intent = new Intent(this, SearchActivity.class);
 			startActivity(intent);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		refreshStories();
 	}
 
 	/**
 	 * Called whenever the spinner is updated. Will story array based on
 	 * whatever the general controller returns.
 	 */
 	private void refreshStories() {
 		ArrayList<Story> newStories;
 		gridArray.clear();
		gc = GeneralController.getInstance(this);
 		newStories = gc.getAllStories(viewType);
 		if (newStories != null) {
 			gridArray.addAll(newStories);
 		}
 		customGridAdapter.notifyDataSetChanged();
 	}
 }
