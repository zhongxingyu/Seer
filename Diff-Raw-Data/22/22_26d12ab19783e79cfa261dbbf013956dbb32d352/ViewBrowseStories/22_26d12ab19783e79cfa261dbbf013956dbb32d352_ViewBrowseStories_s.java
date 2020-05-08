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
 import java.util.UUID;
 
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
 import ca.ualberta.cmput301f13t13.storyhoard.backend.HolderApplication;
 import ca.ualberta.cmput301f13t13.storyhoard.backend.ObjectType;
 import ca.ualberta.cmput301f13t13.storyhoard.backend.SHController;
 import ca.ualberta.cmput301f13t13.storyhoard.backend.Story;
 
 /**
  * Class which displays all stories in a grid, handles different view types.
  * 
  * @author alexanderwong
  * 
  */
 public class ViewBrowseStories extends Activity {
 	HolderApplication app;
 	private GridView gridView;
 	private ArrayList<Story> gridArray = new ArrayList<Story>();
 	private AdapterStories customGridAdapter;
 	private SHController gc;
 	private ObjectType viewType = ObjectType.CREATED_STORY;
 	
 	/**
 	 * Create the View Browse Stories activity
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_view_browse_stories);
 		setGridView();
 	}
 
 	private void setActionBar() {
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
 							viewType = ObjectType.CREATED_STORY;
 						} else if (itemPosition == 1) {
 							viewType = ObjectType.CACHED_STORY;
 						} else if (itemPosition == 2) {
 							viewType = ObjectType.PUBLISHED_STORY;
 						}
 						refreshStories();
 						return true;
 					}
 				});
 	}
 
 	private void setGridView() {
 		// Setup the grid view for the stories
 		gridView = (GridView) findViewById(R.id.gridStoriesView);
 		customGridAdapter = new AdapterStories(this,
 				R.layout.browse_story_item, gridArray);
 		gridView.setAdapter(customGridAdapter);
 
 		// Setup the grid view click listener
 		gridView.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				Story story = gridArray.get(arg2);
 				// Handle caching the story if it's a published story, 
 				// currently breaks downloaded stories
 				if (viewType == ObjectType.PUBLISHED_STORY) {
 					UUID newStoryId = gc.cacheStory(story);
 					story = gc.getStory(newStoryId, ObjectType.CACHED_STORY);
 				}
 				
 				// Handle going to view story activity
 				Intent intent = new Intent(getBaseContext(), ViewStory.class);
				
 				app.setStory(story);
				app.setStoryType(ObjectType.CACHED_STORY);
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
 			app.setFirstStory(true);
 			app.setEditing(false);
 			startActivity(intent);
 			return true;
 		case R.id.searchStories:
 			intent = new Intent(this, SearchActivity.class);
 			startActivity(intent);
 			return true;
 		case R.id.lucky:
 			Story story = gc.getRandomStory();
 			UUID newId = gc.cacheStory(story);
 			story = gc.getStory(newId, ObjectType.CACHED_STORY);
 			app.setStory(story);
 			app.setStoryType(ObjectType.CACHED_STORY);
 			intent = new Intent(getBaseContext(), ViewStory.class);
 			startActivity(intent);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		app = (HolderApplication) this.getApplication();
 		setActionBar();
 		refreshStories();
 	}
 
 	/**
 	 * Called whenever the spinner is updated. Will story array based on
 	 * whatever the general controller returns.
 	 */
 	private void refreshStories() {
 		ArrayList<Story> newStories;
 		gridArray.clear();
 		gc = SHController.getInstance(this);
 		newStories = gc.getAllStories(viewType);
 		if (newStories != null) {
 			gridArray.addAll(newStories);
 		}
 		customGridAdapter.notifyDataSetChanged();
 	}
 }
