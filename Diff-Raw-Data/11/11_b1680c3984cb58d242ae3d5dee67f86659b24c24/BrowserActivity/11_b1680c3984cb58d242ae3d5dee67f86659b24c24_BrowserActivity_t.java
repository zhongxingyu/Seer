 /*
 	Adventure App - Allows you to create an Adventure Book, or Download
  	books from other authors.
     Copyright (C) Fall 2013 Team 5 CMPUT 301 University of Alberta
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.uofa.adventure_app.activity;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.UUID;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.GridView;
 import android.widget.SearchView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.uofa.adventure_app.R;
 import com.uofa.adventure_app.application.AdventureApplication;
 import com.uofa.adventure_app.controller.FragementParser;
 import com.uofa.adventure_app.controller.http.HttpObjectStory;
 import com.uofa.adventure_app.interfaces.AdventureActivity;
 import com.uofa.adventure_app.model.Fragement;
 import com.uofa.adventure_app.model.Story;
 import com.uofa.adventure_app.model.User;
 /**
  * This class takes in user input and processes it when the user
  * is in the Story Browsing screen.  It interacts with the Story
  * class in the model.
  * @author Kevin Lafond, Chris Pavlicek
  *
  */
 public class BrowserActivity extends AdventureActivity {
 
 	private StoryGridAdapter storyGridAdapter = null;
 	ArrayList<String> List;
 	View v;
 	TextView search;
 	String searchQuery = "";
 	SearchView searchView;
 	 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_browser);
 		v = this.findViewById(android.R.id.content);
 		
 		boolean firstrun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("firstrun", true);
 		/*
 		 * Ran only on the first time after install or if the user logs out of the app.
 		 */
 		if (firstrun){
 			Intent myIntent = new Intent(this, FirstRunOnlyActivity.class);
 			this.startActivity(myIntent);
 			// Save the state
 			getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putBoolean("firstrun", false).commit();
 		} else {
 			String username = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("username", null);
 			String uid = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("uid", null);
 			AdventureApplication.setUser(new User(username, UUID.fromString(uid)));
 		}
 		
 		loadAllStories();
 
 	}
 
 	public void loadAllStories() {
 		
 		AdventureApplication.getStoryController().loadStories();
 		if(this.isNetworkAvailable()) {
 			HttpObjectStory httpStory = new HttpObjectStory();
 			this.httpRequest(httpStory.fetchAll(), GET_ALL_METHOD);
 		} 
 		initGrid();
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main, menu);
 		
 		searchView = (SearchView) menu.findItem(R.id.search).getActionView();
 		final SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
 		    @Override
 		    public boolean onQueryTextChange(String newText) {
 		        // Do something
 		    	storyGridAdapter.filter(newText);
 		        return true;
 		    }
 
 		    @Override
 		    public boolean onQueryTextSubmit(String query) {
 		        // Do something
 		    	storyGridAdapter.filter(query);
 		        return true;
 		    }
 		};
 		searchView.setOnQueryTextListener(queryTextListener);
 		
 		return super.onCreateOptionsMenu(menu);
 	}
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle presses on the action bar items
 		switch (item.getItemId()) {
 		case R.id.new_story:
 			this.newStory();
 			break;
 		case R.id.refresh:
 				this.loadAllStories();
 			break;
 
 		case R.id.random:
 			Random rand = new Random();
 			int size = AdventureApplication.getStoryController().stories().size();
			int  n = 0;
			if(size > 1)
				n = rand.nextInt(size);
 			viewStory(v, AdventureApplication.getStoryController().stories().get(n));
 			break;
 		case R.id.help:
 			String helpText = new String();
 			helpText="Search for a specific story using search bar\n\n";
 			helpText=helpText+"Choose story, to read the first fragement of the story\n\n";
 			helpText=helpText+"Touch New Story to create new story from scratch\n\n";
 			helpText=helpText+"Touch Refresh to refresh stories\n\n";
 			Toast.makeText(this, helpText, Toast.LENGTH_LONG).show();
 			break;
 		case R.id.logout:
 			getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putBoolean("firstrun", true).commit();
 			Intent myIntent = new Intent(this, BrowserActivity.class);
 			this.startActivity(myIntent);
 			finish();
 			break;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	/**
 	 * called when the user clicks Create a new story.
 	 * Creates and calls the intent that calls the Edit Fragment screen.
 	 */
 	public void newStory() {
 		// Create a Story, add a blank fragement, and set it as the first
 		Story newStory = new Story();
 		newStory.setIsLocal(true);
 		newStory.setTitle("Your Story Title");
 		newStory.addUser(AdventureApplication.user());
 		Fragement newFragement = new Fragement();
 		newFragement.setTitle("Your First Fragement");
 		newFragement.setBody("Enter Body Text for Your First Fragement Here.");
 		newStory.addFragement(newFragement);
 		newStory.setStartFragement(newFragement);
 		
 		// Add it to our list
 		AdventureApplication.getStoryController().addStory(newStory);
 		
 		// Set as current Story
 		AdventureApplication.getStoryController().setCurrentStory(newStory);
 		
 		// Open editor
 		Intent myIntent = new Intent(this, EditStoryActivity.class);
 		this.startActivity(myIntent);
 		
 
 	}
 	/**
 	 * This creates and calls an intent to open the Activity that allows you to view the stories.
 	 * 
 	 * @param View v
 	 * @param Story s
 	 */
 	public void viewStory(View v, Story s) {
 
 		if(!s.isLocal()) {
 			fetchStory(s);
 		} else {
 			this.openStory(s);
 		}
 	}
 	/**
 	 * Gets a single story from the server.
 	 * @param Story s
 	 */
 	public void fetchStory(Story s) {
 		HttpObjectStory httpStory = new HttpObjectStory();
 		this.httpRequest(httpStory.fetch(s.id()), GET_METHOD);
 	}
 	/**
 	 * Opens the story passed into the method in the StoryActivity.
 	 * @param Story s
 	 */
 	public void openStory (Story s) {
 		// Set the Story as our current Story, Auto sets the current one we are on.
 		AdventureApplication.getStoryController().setCurrentStory(s);
 		
 		// Open the Story!
 		Intent myIntent = new Intent(this, StoryActivity.class);
 		this.startActivity(myIntent);
 	}
 	
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onResume()
 	 */
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		this.loadAllStories();
 	}
 
 	/**
 	 * Updates the view
 	 */
 	public void updateView(){
 		if(storyGridAdapter != null) {
 			storyGridAdapter.notifyDataSetChanged();
 		}
 	}
 
 
 	public void initGrid() {
 		//if(storyGridAdapter == null) {
 		GridView grid = (GridView) findViewById(R.id.gridView1);
         storyGridAdapter = new StoryGridAdapter(this, AdventureApplication.getStoryController().stories());
         grid.setAdapter(storyGridAdapter);
         grid.setOnItemClickListener(new 
                         GridView.OnItemClickListener() {
                 // @Override
                 public void onItemClick(AdapterView<?> a, View v, int i, long l) {                                        
                         viewStory(v, AdventureApplication.getStoryController().stories().get(i));
                 }
         });
 		//}  else {
 			//storyGridAdapter.notifyDataSetChanged();
 		//}
 	}
 	
 	/**
 	 * 
 	 * This is called every time the code checks json for an update.
 	 * 
 	 * @param String method
 	 * @param ArrayList<Story> result
 	 */
 	@Override
 	public void dataReturn(ArrayList<Story> result, String method) {
 		if(result.size() > 0) {
 		if(method.equals(GET_ALL_METHOD)) {
 			for(int i = 0; i < result.size(); i++ ) {
 					AdventureApplication.getStoryController().addStory(result.get(i));
 			}
 			initGrid();
 		}else if(method.equals(GET_METHOD)) {
 			//System.out.println("RESULT " + result);
 			Story currentStory = result.get(0);
 			if(currentStory != null) {
 				currentStory.setIsLocal(true);
 				//System.out.println(currentStory.getFragements().get(0).getTitle());
 				AdventureApplication.getStoryController().replaceStory(currentStory);
 				
 				for (Fragement f : currentStory.getFragements()) {
 					HttpObjectStory httpStory = new HttpObjectStory();
 					this.httpRequestFragement(httpStory.fetchFragement(f.uid()), GET_FRAGEMENT);
 				}
 				
 				// Updates all views to proper content
 				AdventureApplication.getActivityController().update();
 				
 				// Save the Stories Just in Case!
 				AdventureApplication.getStoryController().saveStories();
 				
 				// Open it!
 				openStory(currentStory);
 
 			}
 		} else if(method.equals(GET_FRAGEMENT)) {
 			Story currentStory = AdventureApplication.getStoryController().currentStory();
 			if(result != null && result.get(0)  != null) {
 				ArrayList<Fragement> fragList = result.get(0).getFragements();
 				for(Fragement f : fragList) {
 						System.out.println("f : " + f.uid().toString());
 						currentStory.replaceFragement(f);
 						
 						if(currentStory.startFragement().equals(f)) {
 							currentStory.setStartFragement(f);
 						}
 						if(AdventureApplication.getStoryController().currentFragement().equals(f)) {
 							AdventureApplication.getStoryController().setCurrentFragement(f);
 						}
 				}
 				
 			}
 			AdventureApplication.getStoryController().replaceStory(currentStory);
 			AdventureApplication.getActivityController().update();
 			
 		}
 		}
 	}
 	 protected void openLastFragement() {
 		 // Nothing Happens here.
 		 finish();
 	 }
 	 
 	protected void saveTextForView(View v, String text) {
 			
 	}
 
 }
 
