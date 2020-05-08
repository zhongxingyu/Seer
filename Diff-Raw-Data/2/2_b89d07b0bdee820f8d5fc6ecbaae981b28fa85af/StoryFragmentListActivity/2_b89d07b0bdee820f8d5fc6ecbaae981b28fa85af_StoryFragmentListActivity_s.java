 /* CMPUT301F13T06-Adventure Club: A choose-your-own-adventure story platform
  * Copyright (C) 2013 Alexander Cheung, Jessica Surya, Vina Nguyen, Anthony Ou,
  * Nancy Pham-Nguyen
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package story.book.view;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import story.book.view.R;
 import story.book.controller.StoryCreationController;
 import story.book.model.Story;
 import story.book.model.StoryFragment;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DialogFragment;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 
 /**
  * StoryFragmentListActivity displays all story fragments contained
  * in the story that is currently open. Selecting a story fragment
  * will allow user to:
  * 		1. Edit the story fragment
  * 		2. Set the story fragment as the starting fragment
  * 		3. Delete the story fragment from the story
  * 
  * @author Jessica Surya
  * @author Vina Nguyen
  */
 public class StoryFragmentListActivity extends Activity implements StoryView<Story>, RequestingActivity {
 	ActionBar actionBar;
 	ArrayList<StoryFragment> SFL;
 	ArrayAdapter<StoryFragment> adapter;
 	StoryCreationController SCC;
 
 	int pos;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.story_fragment_read_activity);
 
 		SCC = new StoryCreationController();
 		return;
 	}
 
 	@Override
 	public void onStart() {
		super.onPause();
 		SCC.getStory().addView(this);
 
 		updateFragmentList();
 
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		Story s = SCC.getStory();
 		SCC.saveStory();
 		s.deleteView(this);
 	}
 
 	@Override
 	public void update(Story model) {
 		updateFragmentList(); 
 	}
 
 	private void updateFragmentList() {
 		SFL = new ArrayList<StoryFragment>();
 
 		HashMap<Integer, StoryFragment> map = SCC.getFragments();
 		for (Integer key : map.keySet()){
 			SFL.add(map.get(key));
 		}
 
 		String title = SCC.getStory().getStoryInfo().getTitle();
 		actionBar = getActionBar();
 		actionBar.setTitle(title);
 
 		adapter = new ArrayAdapter<StoryFragment>(this, android.R.layout.simple_list_item_1,
 				SFL);
 
 		ListView listview = new ListView(this);
 
 		listview.setBackgroundColor(Color.WHITE);
 
 		listview.setAdapter(adapter);
 		setContentView(listview);
 
 		registerForContextMenu(listview);
 		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView parent, View v, int position, long id) {
 				pos = position;
 				Intent i = new Intent(parent.getContext(), StoryFragmentEditActivity.class);
 				i.putExtra("FID", SFL.get(pos).getFragmentID());
 				startActivity(i);
 			}
 
 		});
 	}
 
 	private void editFragment(int FID) {
 		Intent i = new Intent(this, StoryFragmentEditActivity.class);
 
 		i.putExtra("FID", FID);
 		startActivity(i);
 	}
 	/*
 	 * addFragment() adds a new fragment to the current story.
 	 */
 	private void addFragment() {
 		DialogFragment newFragment = new RequestTextDialog();
 		((RequestTextDialog)newFragment).setParent(this);
 		((RequestTextDialog)newFragment).setHeader(this.getString(R.string.add_fragment_title));
 		((RequestTextDialog)newFragment).setWarning(this.getString(R.string.bad_frag_title_msg));
 		newFragment.show(getFragmentManager(), "addFragment");
 	}
 
 	public void onUserSelectValue(String title) {
 		if (title != null) {
 			//Create fragment with this title
 			StoryFragment fragment = SCC.newFragment(title);
 
 			//Open fragment for editing
 			editFragment(fragment.getFragmentID());
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu items for use in the action bar
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.fragment_list_menu, menu);
 		inflater.inflate(R.menu.standard_menu, menu);
 		inflater.inflate(R.menu.search_bar, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.title_activity_dashboard:
 			Intent intent = new Intent(this, Dashboard.class);
 			startActivity(intent);
 			finish();
 			return true;
 
 		case R.id.add_fragment:
 			addFragment();
 			return true;
 		case R.id.publish:
 			if (checkInternetConnected()) {
 				SCC.publishStory();
 			} else {
 				SimpleWarningDialog.getWarningDialog(this.getString(R.string.no_internet), this);
 			}
 			return true;
 		case R.id.change_info:
 			Intent intent2 = new Intent(this, EditStoryInformationActivity.class);
 			startActivity(intent2);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		menu.setHeaderTitle("Select an Option:");
 		menu.add(0, v.getId(), 1, "Edit");  
 		menu.add(0, v.getId(), 2, "Set as Starting Story Fragment"); 
 		menu.add(0, v.getId(), 3, "Delete");
 		menu.add(0, v.getId(), 4, "Cancel");
 	}
 
 	@Override  
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 		pos = info.position;
 
 		switch (item.getOrder()) {
 		case 1:
 			// Edit story fragment
 			editFragment(SFL.get(pos).getFragmentID());
 			break;
 
 		case 2:
 			// Set as starting story fragment
 			SCC.setStartingFragment(SFL.get(pos).getFragmentID());
 			break;
 		case 3:
 			//Delete
 			int FID = SFL.get(pos).getFragmentID();
 			if (FID == SCC.getStartingFragment()) {
 				//fragmentDeleteDialog();
 				SimpleWarningDialog.getWarningDialog(this.getString(R.string.bad_frag_delete_msg), this);
 			} else {
 				SCC.deleteFragment(FID);
 			}
 			break;
 		case 4:
 			// Cancel options
 			return false;
 		}
 
 		return true; 
 
 	}
 
 	/**
 	 * http://stackoverflow.com/questions/4238921/android-detect-whether-there-is-an-internet-connection-available
 	 */
 	//TODO put this also when click onlinelibrary button
 	private Boolean checkInternetConnected() {
 		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
 		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
 	}
 
 }
