 /*
  * Copyright (C) <2013>  <Terence Yin Kiu Leung>
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
 
 import java.util.List;
 import java.util.Random;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.util.Base64;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import c301.AdventureBook.Controllers.StoryManager;
 import c301.AdventureBook.Models.Option;
 import c301.AdventureBook.Models.Page;
 import c301.AdventureBook.Models.RandomOption;
 import c301.AdventureBook.Models.Story;
 
 import com.example.adventurebook.R;
 
 public class ViewPageActivity extends Activity {
 
 	private static final int LOCALLIBRARY_ID = 0;
 	//private Story currentStory;
 	//private List<Page> pages;
 	private List<Option> options;
 	ListView optionsListView;
 	Button endStoryButton;
 	TextView optionDescription;
 	
 	public StoryManager sManager;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.view_page);
 		
 		sManager = StoryManager.getInstance();
 		sManager.initContext(this);
 		populatePage();
 		
 		// When option is clicked, go to the next page.
 		optionsListView.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View v, int position,
 					long arg3) {
 				Option chosenOption = (Option) optionsListView.getItemAtPosition(position);
 				Random rand = new Random();
 				while(chosenOption instanceof RandomOption){
 					int randomIndex = rand.nextInt(options.size());
 					chosenOption = (Option) options.get(randomIndex);
 				}				
 				sManager.setCurrentOption(chosenOption);
 				Page goToPage = chosenOption.getGoToPage();
 				sManager.setCurrentPage(goToPage);
 				Toast.makeText(ViewPageActivity.this, "You Picked The Option: " + chosenOption.getDescription(), Toast.LENGTH_LONG).show();
 				Intent intent = new Intent(getApplicationContext(), ViewPageActivity.class);
 				startActivity(intent);
 				
 				/*
 				 * Allow the user to go back? Implementing finish() will
 				 * inhibit the user from going back to the previous page
 				 */
 				finish();
 			}
 		});
 		
 		endStoryButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				returnHome();
				finish();
 			}
 		});
 		
 	}
 	
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.view_story, menu);
         menu.add(0, LOCALLIBRARY_ID, 0, R.string.menu_offline_library);
         return true;
     }
     
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
         switch(item.getItemId()) {
             case LOCALLIBRARY_ID:
                 returnHome();
                 return true;
         }
         return super.onMenuItemSelected(featureId, item);
     }
 	
     private void returnHome() {
         Intent i = new Intent(this, OfflineLibraryActivity.class);
         startActivity(i);
     }
     
 	
 	/**
 	 * Loads the page data including the title, description, and options that
 	 * are available on that page.
 	 */
 	private void populatePage() {
 		Page page = sManager.getPage();
 		options = page.getOptions();
 		
 		// Set the Image for the Page
 	     CoverFlow coverFlow = (CoverFlow) findViewById(R.id.coverFlow);
 
 	     coverFlow.setAdapter(new ImageAdapter(this, page));
 
 	     ImageAdapter coverImageAdapter =  new ImageAdapter(this, page);
 	     
 	     //coverImageAdapter.createReflectedImages();
 	     
 	     coverFlow.setAdapter(coverImageAdapter);
 	     
 	     coverFlow.setSpacing(25);
 	     coverFlow.setSelection(0, true);
 	     coverFlow.setAnimationDuration(1000);
 		
 		
 		// Set the page description
 		String pageDescription = page.getPageDescription();
 		TextView pageDescriptionTV = (TextView) findViewById(R.id.pageDescriptionTV);
 		pageDescriptionTV.setText(pageDescription);
 		
 		
 		// Set the page options
 
 			optionsListView = (ListView) findViewById(R.id.list_options);
 			ArrayAdapter<Option> optionAdapter = new ArrayAdapter<Option>(this, R.layout.list_row, options);
 			optionsListView.setAdapter(optionAdapter);
 			endStoryButton = (Button) findViewById(R.id.endStory);
 			optionDescription = (TextView) findViewById(R.id.option_des);
 		
 			if(options.size() != 0){
 				endStoryButton.setVisibility(View.GONE);
 				
 			}
 			else{
 				optionDescription.setText("The End");
 				optionsListView.setVisibility(View.GONE);
 			}
 			
 		// Set the No of Annotations added
 		
		TextView annotationsCount = (TextView) findViewById(R.id.annotationCount);
 		annotationsCount.setText(""+ page.getAnnotations().size());
 		
 	}
 	
 	public void launchAnnotationsActivity(View v){
 		Intent i  = new Intent(this, AnnotationActivity.class);
 		startActivity(i);
 	}
 	
 
 }
