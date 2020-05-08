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
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import ca.ualberta.cmput301f13t13.storyhoard.R;
 
 
 /**
  * 
  * ViewBrowseAuthorStories:
  * 
  * The ViewBrowseAuthorStories activity displays a scrolling list of all stories
  * created by the user. Information displayed includes the Title of the story
  * and the author(s) which wrote them.
  * 
  * Upon clicking a story, the user has the option to gain access of the
  * following: a) Settings: Update settings such as story information b) Edit:
  * Add/Edit chapters in the story c) Read: Read the story. Cancel will take the
  * user back to the ViewBrowseAuthorStories activity.
  * 
  * @author Kim Wu
  * 
  */
 
 public class ViewBrowseAuthorStories extends ListActivity {
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_browse_author_stories);
 
 		// Populates list with stories and displays author
 		String[] stories = new String[] { "Story1 \nby:asd", "Stor2\nby:mandy",
 				"Story3 \nby:Tom", "Story4 \nby:Dan", "Story5 \nby:Sue" };
 
 		//
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_list_item_1, stories) {
 			@Override
 			// Formats the listView
 			public View getView(int position, View convertView, ViewGroup parent) {
 				View view = super.getView(position, convertView, parent);
 				TextView textView = (TextView) view
 						.findViewById(android.R.id.text1);
 				textView.setTextColor(Color.parseColor("#fff190"));
 				return view;
 			}
 		};
 		setListAdapter(adapter);
 	}
 
 	// When user clicks a story from the list:
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		Dialog dialog = userAction(position, id);
 		dialog.show();
 	}
 
 	/**
 	 * userAction Choices: 3 possible actions can be taking from clicking a
 	 * story on the list. 1) Settings: Will take user to story settings 2) EDIT:
 	 * will allow user to edit/add chapters 3) Read : take users to read the
 	 * story
 	 * 
 	 */
 	private AlertDialog userAction(final int position, final long id) {
 		// Initialize the Alert Dialog
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		// Source of the data in the DIalog
 		CharSequence[] array = { "Settings", "Edit", "Read" };
 
 		// Set the dialog title
 		builder.setTitle("Story Action")
 				// Specify the list array, the items to be selected by default
 				.setSingleChoiceItems(array, 1,
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								// TODO Auto-generated method stub
 							}
 						})
 
 				// Set the action buttons
 				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int id) {
 						// add method
 
 					}
 				})
 				.setNegativeButton("Cancel",
 						new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog, int id) {
 							}
 						});
 		return builder.create();
 	}
 
 	// MENU ITEMS
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
		case R.id.addStoryIcon:
 			Intent addStory = new Intent(this, AddStoryActivity.class);
 			finish();
 			startActivity(addStory);
 			return true;
 		case R.id.publishIcon:
 			Intent viewPublished = new Intent(this, ViewBrowsePublishedStories.class);
 			finish();
 			startActivity(viewPublished);
 			return true;
 		case R.id.downloadIcon:
 			Intent viewDownloads = new Intent(this, ViewBrowseCachedStories.class);
 			finish();
 			startActivity(viewDownloads);
 			return true;
 		}
 		return false;
 	}
 }
