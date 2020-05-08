 package com.eyad.slidepuzzle;
 
 import java.util.Arrays;
 import java.util.Collections;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.MenuItem;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.GridView;
 import android.widget.Toast;
 import android.support.v4.app.NavUtils;
 import android.annotation.TargetApi;
 import android.os.Build;
 import android.view.*;
 
 //TODO: Pop up if game is over / play again
 public class PlayGameActivity extends Activity {
 	static private String[] sliderContent;
 	static ArrayAdapter<String> adapter;
 	private OnItemClickListener mMessageClickHandlerClickListener = new OnItemClickListener() {
 		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 			updateSliderContent(parent, position);
 		}
 	};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_play_game);
 		
 		if (sliderContent == null) {
 			populateSliderContent();
 		}
 		shuffleSliderContent();
 
 		// Populate the gridview with the adapter's data and add the on click listener
 		GridView gridview = (GridView) findViewById(R.id.gridview); 
 		gridview.setAdapter(adapter);
 		gridview.setOnItemClickListener(mMessageClickHandlerClickListener);
 		
 		// Show the Up button in the action bar.
 		setupActionBar();
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
 	
 	private void populateSliderContent() {
 		// Populate sliderContent with values 1..8 and an empty slot at the end
 		sliderContent = new String[9];
 		for (Integer i = 1; i < sliderContent.length; ++i) {
 			sliderContent[i-1] = i.toString();
 		}
 		sliderContent[sliderContent.length - 1] = "";
 		
 		// Attach the slider content and the layout to the adapter
 		adapter = new ArrayAdapter<String>(this,
 		R.layout.slider_content_layout, sliderContent);
 	}
 	
 	private void shuffleSliderContent(){
 		// Possible shuffle could produce number in order
 		// To fix, we re-shuffle until out of order
 		do {
			System.out.println("shuffling");
 			Collections.shuffle(Arrays.asList(sliderContent));
 			fixEmptySpot();
 		} while (isInCorrectOrder());
 	}
 	
 	private boolean isInCorrectOrder() {
 		for (int i = 0; i < sliderContent.length - 2; ++i) {
 			if (sliderContent[i] == "" ||
 					sliderContent[i].compareTo(sliderContent[i+1]) > 0) {
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	private void swap(int lhs, int rhs) {
 		String temp = sliderContent[lhs];
 		sliderContent[lhs] = sliderContent[rhs];
 		sliderContent[rhs] = temp;
 	}
 	
 	private void fixEmptySpot() {
 		for (int i = 0; i < sliderContent.length; ++i) {
 			if (sliderContent[i]== "") {
 				swap(i, sliderContent.length-1);
 				break;
 			}
 		}
 	}
 	
 	private void updateSliderContent(AdapterView<?> parent, int position) {
 		GridView gridView = (GridView) parent; 
 		int numCols = gridView.getNumColumns();
 		int col = position % numCols;
 		
 		//First check adjacent rows
 		if (position - numCols >= 0 && sliderContent[position - numCols] == "") {
 			swap(position, position - numCols);
 		}
 		else if (position + numCols < sliderContent.length && sliderContent[position + numCols] == "") {
 			swap(position, position + numCols);
 		}
 		//Now check adjacent columns
 		else if (col > 0 && sliderContent[position - 1] == "") {
 			swap(position, position - 1);
 		}
 		else if (col < 2 && sliderContent[position + 1] == "") {
 			swap(position, position + 1);
 		}
 		else {
 			Toast toast = Toast.makeText(this, "Invalid move!", Toast.LENGTH_SHORT);
 			toast.setGravity(Gravity.CENTER, 0, 0);
 			toast.show();
 		}
 		adapter.notifyDataSetChanged();
 		
 		if (isInCorrectOrder()) {
 			Toast toast = Toast.makeText(this, "Congratulations you won!", Toast.LENGTH_LONG);
 			toast.setGravity(Gravity.CENTER, 0, 0);
 			toast.show();
 		}
 	}
 }
