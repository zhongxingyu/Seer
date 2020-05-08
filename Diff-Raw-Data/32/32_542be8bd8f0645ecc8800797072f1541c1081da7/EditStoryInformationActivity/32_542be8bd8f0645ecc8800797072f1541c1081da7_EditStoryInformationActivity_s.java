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
 
 import story.book.view.R;
 import story.book.controller.LocalStoryController;
 import story.book.controller.StoryInfoController;
 import story.book.model.StoryInfo;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 /**
  * Activity that allows the user to edit the information of their story or enter
  * information for their newly created story.
  * 
  * @author Nancy Pham-Nguyen
  * @author Vina Nguyen
  * 
  */
 
 public class EditStoryInformationActivity extends Activity  {
 
 	
 	public EditText editTitle;
 	public TextView date;
 	public EditText editAuthor;
 	public EditText editGenre;
 	public EditText editSynopsis;
 
 	public ArrayList<StoryInfo> storyList;
 
 	StoryInfoController storyInfoController = new StoryInfoController();
 	LocalStoryController localController = new LocalStoryController();
 	
 	StoryInfo storyInfo;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_edit_story_information);
 
 		storyInfo = storyInfoController.getStoryInfo();
 		
 		findViewById(R.id.title).setVisibility(View.VISIBLE);
 		editTitle = (EditText) findViewById(R.id.enter_title);
 		editTitle.setVisibility(View.VISIBLE);
 		editAuthor = (EditText) findViewById(R.id.enter_author);
 		date = (TextView) findViewById(R.id.enter_date);
 		editGenre = (EditText) findViewById(R.id.enter_genre);
 		editSynopsis = (EditText) findViewById(R.id.enter_synopsis);
 
 		displayStoryInfo();

		
 		
 		Button doneButton = (Button) findViewById(R.id.done);
 		doneButton.setVisibility(View.VISIBLE);
 		final Intent intent = new Intent(this, StoryFragmentListActivity.class);
 		doneButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// if the title field is empty then an AlertDialog will appear
 				if (editTitle.getText().toString().equals("")) {
 					alertDialog();
 				} else {
 					setStoryInfo();
 					// Navigate to the list of Story Fragments
 					startActivity(intent);
 				}
 			}
 		});
 	}
 
 	/**
 	 * Display the information that the the user has entered
 	 */
 	private void displayStoryInfo() {
 		setTitle(storyInfo.getTitle());
 		
 		editTitle.setText(storyInfo.getTitle());
 		editAuthor.setText(storyInfo.getAuthor());
 		date.setText(storyInfo.getPublishDateString());
 		editGenre.setText(storyInfo.getGenre());
 		editSynopsis.setText(storyInfo.getSynopsis());
 
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		
 		if (editTitle.getText().toString().equals("") == false) {
 			setStoryInfo();
 		} 
 	}
 
 	/**
 	 * Method that sets the information of the story entered by the user
 	 */
 	private void setStoryInfo() {
 		storyInfo.setTitle(editTitle.getText().toString());
 		storyInfo.setAuthor(editAuthor.getText().toString());
 		storyInfo.setGenre(editGenre.getText().toString());
 		storyInfo.setSynopsis(editSynopsis.getText().toString());
 		
 		storyInfoController.saveStory();
 	}
 
 	/**
 	 * AlertDialog http://developer.android.com/guide/topics/ui/dialogs.html
 	 * Method shows an alertDialog when the user selects the "Done" button but
 	 * the required text field has no text input
 	 */
 
 	public void alertDialog() {
 		// Instantiate an AlertDialog.Builder with its constructor
 		AlertDialog.Builder builder = new AlertDialog.Builder(
 				EditStoryInformationActivity.this);
 		// Chain together various setter methods to set the dialog
 		// characteristics
 		builder.setMessage(R.string.dialog_message).setTitle(
 				R.string.dialog_title);
 
 		// Add buttons to AlertDialog
 		builder.setPositiveButton(R.string.ok,
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						// User clicked OK button
 					}
 				});
 
 		// Create the AlertDialog
 		AlertDialog dialog = builder.create();
 		dialog.show();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.standard_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.title_activity_dashboard:
 			Intent intent = new Intent(this, Dashboard.class);
 			startActivity(intent);
 			finish();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 }
