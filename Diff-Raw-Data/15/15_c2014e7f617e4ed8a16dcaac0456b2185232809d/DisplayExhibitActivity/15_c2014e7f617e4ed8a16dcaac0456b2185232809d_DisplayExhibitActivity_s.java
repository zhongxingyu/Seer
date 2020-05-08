 package ca.on.rom.romsearch;
 
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Locale;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Point;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.util.DisplayMetrics;
 import android.view.Display;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.GridView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class DisplayExhibitActivity extends FragmentActivity
 									implements InputNameDialogFragment.InputNameDialogListener{
 	
 	
 	public final static String EXTRA_MESSAGE2 ="com.example.R.MESSAGE";
 	
 	public static String exhibit;
 	public static Integer[] image_ids;
 	public static Integer[] image_thumbs;
 	public static String[] image_names;
 	public static int pos = 0;
 	
 	public static ExhibitData savedata;
 	
 	@SuppressLint("NewApi")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Intent intent = getIntent();
 		setContentView(R.layout.activity_display_exhibit);
 		
 		//get exhibit title
 		exhibit = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
 		TextView textview = (TextView) findViewById(R.id.exhibit_title);
 		textview.setText(exhibit);
 		//get image names and ids
 		image_names = GridData.getNames().get(exhibit);
 		image_ids = GridData.getImages().get(exhibit);
 		image_thumbs = GridData.getThumbs().get(exhibit);
 		//get savedata
 		String data = intent.getStringExtra(MainActivity.EXTRA_SAVEFILE);
 		savedata = new ExhibitData(data);
 		updateCompletionDisplay();
 		
 		//ensure Honeycomb or higher to use ActionBar APIs
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			//show the up button in the action bar
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 		
 		// Get screen resolution
 		int[] display = getThisDisplay();
 		int screenHeight = display[0];
 		int screenWidth = display[1];
 		//setup grids
 		GridView gridview = (GridView) findViewById(R.id.gridview); //for images
 		gridview.setAdapter(new ImageAdapter(this, image_thumbs, screenHeight, screenWidth));
 		GridView overlay = (GridView) findViewById(R.id.overlay); //for completion image
 		overlay.setAdapter(new ImageAdapter(this, savedata.getOverlay(), screenHeight, screenWidth));
 		//Listen for click on overlay
 		overlay.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 				//get clicked on image, and set the dialog to contain it
 				pos = position;
 				if (savedata.getArr()[pos]) {
 					//if item already completed display info
 					DialogFragment dialog = DisplayItemDialogFragment.newInstance(image_ids[pos]
 							, image_names[pos]);
 					FragmentManager fm = getSupportFragmentManager();
 					dialog.show(fm, "display_item");
 				} else {
 					//if item has not been completed, ask for input
 					DialogFragment dialog = InputNameDialogFragment.newInstance(image_ids[pos]);	
 					FragmentManager fm = getSupportFragmentManager();
 					dialog.show(fm, "input_name");
 				}
 			}
 		});
 		
 	}
	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.display_exhibit, menu);
 		return true;
 	}	
 	
 	@Override
 	public void onDialogPositiveClick(DialogFragment dialog, String input) {
 		String correct = image_names[pos];
 		if (TextMatch(input, correct)) {
 			Toast.makeText(DisplayExhibitActivity.this, "Correct! It's " + correct + "!", Toast.LENGTH_SHORT).show();
 			//Mark as correct in savedata
 			savedata.Progress(pos);
 			//save the data
 			SharedPreferences sharedPref = this.getSharedPreferences(exhibit, Context.MODE_PRIVATE);
 			SharedPreferences.Editor editor = sharedPref.edit();
 			editor.putString(exhibit, savedata.getRaw());
 			editor.commit();
 			//update grid display and text display
 			GridView overlay = (GridView) findViewById(R.id.overlay);
 			updateCompletionDisplay();
 			int[] display = getThisDisplay();
 			int screenHeight = display[0];
 			int screenWidth = display[1];
 			overlay.setAdapter(new ImageAdapter(this, savedata.getOverlay(), screenHeight, screenWidth));
 		} else {
 			Toast.makeText(DisplayExhibitActivity.this, "Sorry, try again", Toast.LENGTH_SHORT).show();
 		}
 	}
 	
 	@Override
 	public void onDialogNegativeClick(DialogFragment dialog) {
 		
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item){
 	    Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
 	    startActivityForResult(myIntent, 0);
 	    return true;
 	}
 	
 	public int[] getThisDisplay(){
 		Display display = getWindowManager().getDefaultDisplay();
 		Point size = new Point();
 		display.getSize(size);
 		int width = size.x;
 		int height = size.y;
 		int[] returnval = {height, width};
 		return returnval;
 	}
 	
 	public boolean TextMatch(String input, String master) {
 		//tokenize input and master
 		ArrayList<String> itokens = new ArrayList<String>(Arrays.asList(input.toLowerCase(Locale.getDefault()).split(" |\\-")));
 		ArrayList<String> mtokens = new ArrayList<String>(Arrays.asList(master.toLowerCase(Locale.getDefault()).split(" |\\-")));
 		//return false if input is longer than master (allowing for a 1 margin of error (e.g. rogue "a"))
 		if (itokens.size() > mtokens.size() + 1) {
 			return false;
 		}
 		//remove connector words (e.g. "a", "the", "of", etc.)
 		String[] connectors = {"a", "an", "as", "of", "the", "for"};
 		removeBanned(mtokens, connectors);
 		removeBanned(itokens, connectors);
 		//check to see if all key words are shared
 		for (int i = 0; i < mtokens.size(); i++) {
 			String item = mtokens.get(i);
 			if (itokens.contains(item)) {
 				itokens.remove(item);
 			} else {
 				return false;
 			}
 		}
 		if (itokens.size() > 0) {
 			return false;
 		}
 		return true;
 	}
 	
 	/*
 	 * Remove words from String[] banned from ArrayList<String> tokens
 	 */
 	public void removeBanned(ArrayList<String> tokens, String[] banned) {
 		for (int i = 0; i < tokens.size(); i++) {
 			String t = tokens.get(i);
 			//remove tokens if in banned
 			for (int j = 0; j < banned.length; j++) {
 				if (t.equals(banned[j])) {
 					tokens.remove(t);
 					break;
 				}
 			}
 		}
 	}
 	
 	public void updateCompletionDisplay() {
 		DecimalFormat df2 = new DecimalFormat("###.##");
 		String completion = Double.toString(Double.valueOf(df2.format(savedata.getCompletion() * 100)));
 		TextView tv = (TextView) findViewById(R.id.completion_display);
 		tv.setText(completion + "% Complete");
 	}
 	
 }
