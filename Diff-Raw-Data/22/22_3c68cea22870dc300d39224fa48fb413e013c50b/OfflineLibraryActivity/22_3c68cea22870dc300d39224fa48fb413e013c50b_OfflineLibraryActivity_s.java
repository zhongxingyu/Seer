 /*
  * Copyright (C) <2013>  <Minhal Syed>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package c301.AdventureBook;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.SearchManager;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.SearchView;
 import android.widget.TextView;
 import android.widget.Toast;
 import c301.AdventureBook.Controllers.LibraryManager;
 import c301.AdventureBook.Controllers.StoryManager;
 import c301.AdventureBook.ElasticSearch.ESClient;
 import c301.AdventureBook.Models.Story;
 
 import com.example.adventurebook.R;
 
 /**
  * This is the offline library activity. This activity's main purpose is to show
  * all the local stories that are present in the phone's memory to the user. It
  * also provides the user with an interface to interact with the local stories.
  * Interactions include: viewing story, deleting story, editing story, adding
  * story, publishing story, and searching offline stories.
  * 
  * 
  * @author Minhal Syed - Main Creator
  * @author Justin Hoy - Minor Editor
  * @author Terence Yin Kiu Leung - Minor Editor
  */
 
 public class OfflineLibraryActivity extends Activity {
 
 	private static final int ACTIVITY_EDIT_STORY = 0;
 
 	ArrayList<Story> offlineStoryLibrary; // Stories array
 	ArrayAdapter<Story> adapter; // Adapter for the stories
 	LibraryManager lManagerInst; // Controller for the Library
 	StoryManager sManagerInst; // Controller for a story
 	
 	Story storyClicked;
 
 	/**
 	 * This function is called once, when the application loads this activity
 	 * for the first time.
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		offlineStoryLibrary = new ArrayList<Story>();
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.offline_library);
 
 		// Initiate and Load the Local Library Manager
 		lManagerInst = LibraryManager.getInstance();
 		lManagerInst.initContext(this);
 		
 		
 		sManagerInst = StoryManager.getInstance();
 		sManagerInst.initContext(this);
 		
 		// Populate the Display
 		populateListView();
 	}
 
 	/**
 	 * Create a search action in the action bar
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.library_options_menu, menu);
 
 		// Associate searchable configuration with the SearchView
 		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
 		final SearchView searchView = (SearchView) menu.findItem(R.id.search)
 				.getActionView();
 
 		final SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
 			/**
 			 * This function is called every time a user enters
 			 * anything on the search bar.
 			 * 
 			 */
 			@Override
 			public boolean onQueryTextChange(String newText) {
 				// Do something
 
 				//String keyword = searchView.getQuery().toString().toLowerCase();
 				//offlineStoryLibrary = fLoader.loadStoryFileWithKeyword(keyword);
 
 				//populateListView();
 				adapter.getFilter().filter(newText);
 				return true;
 			}
 
 			@Override
 			public boolean onQueryTextSubmit(String query) {
 				// Do something
 				return true;
 			}
 		};
 
 		searchView.setOnQueryTextListener(queryTextListener);
 
 		return true;
 	}
 
 	/**
 	 * Starts the create story activity to create a new story.
 	 */
 	public void launchNewStoryActivity(View v) {
 		Intent i = new Intent(this, CreateStoryActivity.class);
 		startActivity(i);
 	}
 
 	/**
 	 * Starts the online library activity to view the online library.
 	 */
 	public void launchOnlineLibraryActivity(View v) {
 		Intent i = new Intent(this, OnlineLibraryActivity.class);
 		startActivity(i);
 	}
 
 	/**
 	 * Populate the library's list view with all the available stories.
 	 */
 	private void populateListView() {
 		// Tutorial from : https://www.youtube.com/watch?v=4HkfDObzjXk
 
 		// Get the Library
 		offlineStoryLibrary = lManagerInst.getCurrentLibrary();
 				
 		final ListView offlineLV = (ListView) findViewById(R.id.offline_library_listView);
 		adapter = new CustomStoryAdapter(this, R.layout.library_row, offlineStoryLibrary);
 		offlineLV.setAdapter(adapter);
 		registerForContextMenu(offlineLV);
 
 		// tutorial used =
 		// http://stackoverflow.com/questions/9097723/adding-a-onclicklistener-to-listview-android
 
 		// When Clicked on the list item, we can return a story.
 		offlineLV.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 
 				// This is the story object that is returned when a list item is
 				// clicked.
 				Story story = (Story) offlineLV.getItemAtPosition(position);
 				Toast.makeText(getBaseContext(), story.getTitle(),
 						Toast.LENGTH_SHORT).show();
 				viewStory(story);
 			}
 		});
 
 	}
 
 	/**
 	 * Create long click menu for the ListView. When LongCliked, we can see
 	 * Publish Story, EditStory and DeleteStory functions.
 	 */
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		// tutorial from:
 		// http://stackoverflow.com/questions/2321332/detecting-which-selected-item-in-a-listview-spawned-the-contextmenu-android
 
 		super.onCreateContextMenu(menu, v, menuInfo);
 		menu.add("Publish Online");
 		menu.add("Edit Story");
 		menu.add("Delete Story");
 	}
 
 	/**
 	 * This function is a context menu listener. If the user presses publish,
 	 * delete, or edit story, this listener acts accordingly.
 	 */
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
 				.getMenuInfo();
 
 		// Get the Story that is clicked on the listView.
 		storyClicked = adapter.getItem(info.position);
 
 		if (item.getTitle() == "Publish Online") {
 			// Do Publish Story Function
 			publishStory(storyClicked);
 
 		} else if (item.getTitle() == "Edit Story") {
 
 			editStory(storyClicked);
 
 		} else if (item.getTitle() == "Delete Story") {
 			// Do Delete Story Function
 			deleteStory(storyClicked);
 
 		}
 		return true;
 	}
 
 	/**
 	 * This function starts the viewPage Activity.
 	 * That means that the user will be presented the 
 	 * first page of the story.
 	 * 
 	 * @param story
 	 *            - The Story that the user chose to view.
 	 */
 	private void viewStory(Story story) {
 		// Do Something
 		
 		sManagerInst.setCurrentStory(story);
 
 		Intent intent = new Intent(this, ViewStoryActivity.class);
 
 		startActivity(intent);
 		
 	}
 	/**
 	 * This function launches the editStory activity.
 	 * 
 	 * @param storyClicked
 	 */
 	public void editStory(Story storyClicked) {
 
 		sManagerInst.setCurrentStory(storyClicked);
 		
 		Intent i = new Intent(this, EditStoryActivity.class);
 		startActivityForResult(i, ACTIVITY_EDIT_STORY);
 	}
 
 	/**
 	 * This function publishes a story on the 
 	 * WebServer.
 	 * 
 	 * @param storyClicked
 	 */
 	public void publishStory(Story storyClicked) {
 		//Since we need to give the HTTP client some time
 		//to publish the story, we need to use AsyncTasks.
 		new publishStoryTask(storyClicked).execute();
 	}
 	/**
 	 * This class defines the publishStoryTask
 	 * 
 	 * @author Minhal
 	 *
 	 */
 	private class publishStoryTask extends AsyncTask<String, String, String> {
 		
 		Story story;
 		public publishStoryTask(Story storyClicked) {
 			this.story = storyClicked;
 		}
 		@Override
 		protected String doInBackground(String... arg0) {
 			ESClient client = new ESClient();
 			try {
 				client.insertStory(this.story);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return null;
 		}
 		protected void onPostExecute(String result) {
 			Toast.makeText(OfflineLibraryActivity.this,
 					"Published " + this.story.getTitle(), Toast.LENGTH_LONG)
 					.show();
 		}
 	}
 	
 	/**
 	 * This function deletes a Story from the local storage.
 	 * 
 	 * @param storyClicked
 	 */
 
 	public void deleteStory(Story storyClicked) {
 		lManagerInst.deleteStory(storyClicked);
 		populateListView();
 		Toast.makeText(this, "Deleted Story!", Toast.LENGTH_LONG).show();
 	}
 
 
 
 }
