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
 
 package story.book;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.ViewSwitcher;
 
 /**
  * Activity that allows the user to view the information of their story and then
  * view the story. When the user is in the Online Stories, this activity will allow them
  * to download the story that they are viewing.
  * 
  * @author Nancy Pham-Nguyen
  * @author Vina Nguyen
  * 
  */
 
 public class StoryInfoActivity extends Activity  {
 
 	TextView author;
 	TextView date;
 	TextView genre;
 	TextView synopsisText;
 	
 	StoryInfoController storyInfoController;
 	OnlineStoryController onlineController;
 	
 	StoryInfo storyInfo;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_edit_story_information);
 
 		storyInfoController = new StoryInfoController();
 		onlineController = new OnlineStoryController();
 		
 		author = (TextView) findViewById(R.id.label_author);
 		date = (TextView) findViewById(R.id.enter_date);
 		genre = (TextView) findViewById(R.id.label_genre);
 		synopsisText = (TextView) findViewById(R.id.label_synopsis);
 
 		storyInfo = storyInfoController.getStoryInfo();
 		
 		displayStoryInfo();
 		
 		final Intent intent = new Intent(this, StoryFragmentReadActivity.class);
 		Button viewButton = (Button) findViewById(R.id.view);
 		viewButton.setVisibility(View.VISIBLE);
 		viewButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				startActivity(intent);
 			}
 		});
 		
 		Boolean showDownload = getIntent().hasExtra("calledByOnline");
 		if (showDownload) {
 			Button downloadButton = (Button) findViewById(R.id.download);
 			final Intent intent2 = new Intent(this, StoryFragmentReadActivity.class);
 			downloadButton.setVisibility(View.VISIBLE);
 			downloadButton.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					onlineController.saveStory();
 					startActivity(intent2);
 				}
 			});
 		}
		//Switch to label view instead of edit view
 		ViewSwitcher switcher = (ViewSwitcher) findViewById(R.id.author_switch);
 		switcher.showNext();
 		switcher = (ViewSwitcher) findViewById(R.id.genre_switch);
 		switcher.showNext();
 		switcher = (ViewSwitcher) findViewById(R.id.synopsis_switch);
 		switcher.showNext();
 	}
 
	/**
	 * Display story information for current story.
	 */
 	private void displayStoryInfo() {
 		
 		setTitle(storyInfo.getTitle());
 		
 		author.setText(storyInfo.getAuthor());
 		date.setText(storyInfo.getPublishDateString());
 		genre.setText(storyInfo.getGenre());
 		synopsisText.setText(storyInfo.getSynopsis());
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
