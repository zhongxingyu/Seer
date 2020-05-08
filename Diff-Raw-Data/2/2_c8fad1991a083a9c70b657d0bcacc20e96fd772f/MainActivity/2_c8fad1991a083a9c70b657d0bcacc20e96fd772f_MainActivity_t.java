 /*
  * 
  */
 package com.example.java1week2_4;
 
 import com.example.json.JSON;
 import com.example.json.Movie;
 import com.example.lib.FormThings;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 
 /**
  * The Class MainActivity.
  */
 public class MainActivity extends Activity {
 
 	/** detailNames is a string array to contain the names for the radio button */
 	String[] detailNames = new String[Movie.values().length];
 
 	/** The movie details. Radio Group. */
 	RadioGroup movieDetails;
 
 	/** The results view. */
 	TextView resultsView;
 
 	int i = 0;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Define Layout
 		LinearLayout ll = new LinearLayout(this);
 		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
 				LayoutParams.MATCH_PARENT);
 		ll.setLayoutParams(lp);
 		ll.setOrientation(LinearLayout.VERTICAL);
 
 		// Build JSON
 		JSON.buildJSON();
 
 		// Title
 		TextView titleText = new TextView(this);
		titleText.setText(getResources().getString(R.string.app_title));
 		titleText.setGravity(Gravity.CENTER);
 		titleText.setTextSize(30);
 		ll.addView(titleText);
 
 		// Define Radio Button Text
 		//Loop through movie ENUM
 		for (Movie info : Movie.values()) {
 			//QUESTION: Could I have done a normal for loop here? I feel like it woudln't have worked.
 			//For each ENUM check detailNames array position, 
 			if (i < detailNames.length) {
 				// add string,
 				detailNames[i] = info.name().toString();
 			}
 			// then advance position.
 			i++;
 
 		}
 		
 		//Create Radio Group
 		movieDetails = FormThings.getOptions(this, detailNames);
 		ll.addView(movieDetails);
 
 		// Create info Button
 		LinearLayout entryBox = FormThings.singleEntryWithButton(this,
 				"Get Info");
 		Button infoButton = (Button) entryBox.findViewById(2);
 		infoButton.setGravity(Gravity.CENTER);
 		ll.addView(entryBox);
 		
 		// On click for infoButton
 		infoButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// Get info from JSON
 				int id = movieDetails.getCheckedRadioButtonId();
 				RadioButton rb = (RadioButton) findViewById(id);
 
 				String selected = rb.getText().toString();
 
 				resultsView.setText(JSON.readJSON(selected));
 
 			}
 		});
 
 		
 
 		// Create Results View
 		resultsView = new TextView(this);
 		resultsView.setLayoutParams(lp);
 		resultsView.setGravity(Gravity.CENTER);
 		resultsView.setText("Select to see movie details");
 		ll.addView(resultsView);
 
 		// Set Content View
 		setContentView(ll);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 }
