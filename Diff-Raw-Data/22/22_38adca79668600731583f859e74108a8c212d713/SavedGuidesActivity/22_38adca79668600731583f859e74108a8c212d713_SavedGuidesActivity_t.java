 package com.catsharksoftware.easygameguides;
 
 import java.util.ArrayList;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.support.v4.app.NavUtils;
 import android.annotation.TargetApi;
 import android.content.Intent;
 import android.os.Build;
 import com.catsharksoftware.easygameguides.R;
 
 public class SavedGuidesActivity extends Activity {
 	
 	public final static String NAME = "com.catsharksoftware.easygameguides.NAME";
 	public final static String LOADING_FILE = "Loading file...";
 	private ArrayList<Button> buttons;
 	private AlgorithmContainer algorithm;
	private String accessedGuide = "";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_saved_guides);
 		// Show the Up button in the action bar.
 		setupActionBar();
 		
 		//Initialize button search list and AlgorithmContainer.
 		algorithm = new AlgorithmContainer();
 		buttons = new ArrayList<Button>();
 		loadLocalGuides();
 		
 	}
 	
	protected void onResume()
	{
		super.onResume();
		for(Button button : buttons)
		{
			String name = (String) button.getText();
			if(name.equals(LOADING_FILE))
			{
				button.setText(accessedGuide);
				break;
			}
		}
	}
	
 	private void loadLocalGuides() {
 		
 		LinearLayout layout =  (LinearLayout) findViewById(R.id.guide_list);
 		String listOfFiles[] = fileList();
 		
 		//TODO: Sort files before displaying
 		listOfFiles = algorithm.mergeSort(listOfFiles);	
 		
 		for(String file : listOfFiles)
 		{
 			Button b = new Button(this);
 			b.setText(file);
 			b.setOnClickListener(new View.OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					openGuide(v);
 					
 				}
 			});
 			layout.addView(b);
 			buttons.add(b);
 		}	
 	}
 	
 	public void openGuide(View view)
 	{
 		Button button = null;
 		if(view instanceof Button)
 		{
 			button = (Button) view;
 			
			accessedGuide = (String) button.getText();
 			button.setText(LOADING_FILE);
 			
 			//Begin the DisplayGuide Activity:
 			Intent intent = new Intent(this, DisplayGuideActivity.class);
		    intent.putExtra(NAME, accessedGuide);
 		    startActivity(intent);
 		}
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.saved_guides, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 }
