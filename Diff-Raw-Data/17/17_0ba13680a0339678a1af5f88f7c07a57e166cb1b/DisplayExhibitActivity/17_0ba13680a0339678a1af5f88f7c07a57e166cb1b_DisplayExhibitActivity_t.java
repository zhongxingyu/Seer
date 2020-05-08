 package ca.on.rom.romsearch;
 
 
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
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.FrameLayout;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class DisplayExhibitActivity extends FragmentActivity
 									implements InputNameDialogFragment.InputNameDialogListener{
 	
 	private int initialPaddingBottom = -1;
 	private int initialVerticalSpacing = 0;
 	public final static String EXTRA_MESSAGE2 ="com.example.R.MESSAGE";
 	public final static String ACHIEVEMENT = "achievement";
 	public final static String EXHIBITS_COMPLETE = "exhibits_complete";
 	public final static String ITEMS_COMPLETE = "items_complete";
 	
 	public static String exhibit;
 	public static Integer[] image_ids;
 	public static Integer[] image_thumbs;
 	public static String[] image_names;
 	public static int pos = 0;
 	public String[] exhibitArray;
 	public DialogFragment dialog;
 	
 	public static ExhibitData savedata;
 	public static AchievementData achievementData;
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		if (dialog != null) {
 			dialog.dismiss();
 		}
 	}
 	
 	@SuppressLint("NewApi")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Intent intent = getIntent();
 		
 		// Following line is for testing when there is no bottom nav-bar.
		//getWindow().getDecorView()
        //.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
 		
 		setContentView(R.layout.activity_display_exhibit);
 		setCorrectSpacing(); // Corrects based on existence of nav-bar.
 		GridData.setupTables(getApplicationContext());
 		
 		//get exhibit title and display corresponding image
 		exhibit = intent.getStringExtra(ChooseExhibitActivity.EXTRA_MESSAGE);
 		exhibitArray = getResources().getStringArray(R.array.exhibit_array);
 		ImageView title = (ImageView) findViewById(R.id.exhibit_title);
 		title.setImageResource(GridData.getTitle(exhibit));
 		
 		//get image names and ids
 		image_names = GridData.getNames(exhibit);
 		image_ids = GridData.getImages(exhibit);
 		image_thumbs = GridData.getThumbs(exhibit);
 
 		//get exhibit savedata
 		SharedPreferences sharedPref = this.getSharedPreferences(exhibit, Context.MODE_PRIVATE);
 		String data = sharedPref.getString(exhibit, "000000000");
 		savedata = new ExhibitData(data);
 		updateCompletionDisplay();
 		
 		//get achievements
 		achievementData = new AchievementData();
 		achievementData.setupAchievements(getApplicationContext());
 		SharedPreferences countPref = this.getSharedPreferences(ACHIEVEMENT, Context.MODE_PRIVATE);
 		int item_total = countPref.getInt(ITEMS_COMPLETE, 0);
 		int exhibit_total = countPref.getInt(EXHIBITS_COMPLETE, 0);
 		achievementData.getNextAchievements(item_total, exhibit_total);
 		
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
 					dialog = DisplayItemDialogFragment.newInstance(image_ids[pos]
 							, image_names[pos]);
 					FragmentManager fm = getSupportFragmentManager();
 					dialog.show(fm, "display_item");
 				} else {
 					//if item has not been completed, ask for input
 					dialog = InputNameDialogFragment.newInstance(image_ids[pos]);	
 					FragmentManager fm = getSupportFragmentManager();
 					dialog.show(fm, "input_name");
 				}
 			}
 		});
 		
 		//if first time running show tutorial
 		SharedPreferences sharedPref2 = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
 		boolean firstrun = sharedPref2.getBoolean("firstrun_display", true);
 		if (firstrun) {
 			showHelp();
 			SharedPreferences.Editor editor = sharedPref2.edit();
 			editor.putBoolean("firstrun_display", false);
 			editor.commit();
 		}
 		
 	}
 	
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.display_exhibit, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle item selection
 	    switch (item.getItemId()) {
 	        case R.id.action_help:
 	        	showHelp();
 	            return true;
 	        case R.id.action_achievements:
 	        	startActivity(new Intent(this, AchievementListActivity.class));
 	        	return true;
 	        case R.id.action_about:
 	        	showAbout();
 	        	return true;
 	        default:
 	        	startActivity(new Intent(this, ChooseExhibitActivity.class));
 	        	return true;
 	    }
 	}
 	
 	@SuppressLint("NewApi")
 	public void setCorrectSpacing() {
 		DisplayMetrics metrics = new DisplayMetrics();
 		ImageView exhibitTitle = (ImageView) findViewById(R.id.exhibit_title);
 		GridView actualGrid = (GridView) findViewById(R.id.gridview);
 		GridView overlay = (GridView) findViewById(R.id.overlay);
		TextView completion = (TextView) findViewById(R.id.completion_display);
 		getWindowManager().getDefaultDisplay().getMetrics(metrics);
 		if (initialPaddingBottom == -1 && initialVerticalSpacing == 0){
 			initialVerticalSpacing = actualGrid.getVerticalSpacing();
 			initialPaddingBottom = exhibitTitle.getPaddingBottom();
 		}
		if (metrics.heightPixels == 1280){
 			// XHDPI device; no nav-bar
			exhibitTitle.setPadding(0, 0, 0, initialPaddingBottom + 26);
			completion.setPadding(0, initialPaddingBottom + 101, 0, 0);
			actualGrid.setVerticalSpacing(initialVerticalSpacing + 23);
			overlay.setVerticalSpacing(initialVerticalSpacing + 23);
 		}
 	}
 	
 	@Override
 	public void onDialogPositiveClick(DialogFragment dialog, String input) {
 		String correct = image_names[pos];
 		if (TextMatcher.TextMatch(input, correct)) {
 			correct = TextMatcher.format(correct);
 			Toast.makeText(DisplayExhibitActivity.this, "Correct! It's " + correct + "!", Toast.LENGTH_SHORT).show();
 			//Mark as correct in savedata
 			savedata.Progress(pos);
 			//save the data
 			SharedPreferences sharedPref = this.getSharedPreferences(exhibit, Context.MODE_PRIVATE);
 			SharedPreferences.Editor editor = sharedPref.edit();
 			editor.putString(exhibit, savedata.getRaw());
 			editor.commit();
 			
 			//update achievement counters
 			SharedPreferences countPref = this.getSharedPreferences(ACHIEVEMENT, Context.MODE_PRIVATE);
 			int item_total = countPref.getInt(ITEMS_COMPLETE, 0);
 			int exhibit_total = countPref.getInt(EXHIBITS_COMPLETE, 0);
 			SharedPreferences.Editor count_editor = countPref.edit();
 			count_editor.putInt(ITEMS_COMPLETE, item_total + 1);
 			if (savedata.getCompletion() >= 1) {
 				count_editor.putInt(EXHIBITS_COMPLETE, exhibit_total + 1);
 				count_editor.putBoolean(exhibit, true);
 				count_editor.commit();
 				//display achievement unlocked popup for completing THIS exhibit (exhibit-type achievement)
 				Achievement exhibit_achievement = achievementData.getExhibitAchievement(exhibit);
 				DialogFragment unlocked = AchievementDialogFragment.newInstance(exhibit_achievement, true);
 				FragmentManager fm = getSupportFragmentManager();
 				unlocked.show(fm, "achievement");
 			} else {
 				count_editor.commit();
 			}
 			//check if achievements unlocked
 			checkAchievements();
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
 	
 	@SuppressLint("NewApi")
 	public int[] getThisDisplay(){
 		Display display = getWindowManager().getDefaultDisplay();
 		Point size = new Point();
 		display.getSize(size);
 		int width = size.y;
 		int height = size.x;
 		int[] returnval = {height, width};
 		return returnval;
 	}
 	
 	
 	public void updateCompletionDisplay() {
 		String completion = Integer.toString((int) Math.round(Double.valueOf(savedata.getCompletion() * 100)));
 		TextView tv = (TextView) findViewById(R.id.completion_display);
 		tv.setText(completion + "% Complete");
 	}
 	
 	public void checkAchievements() {
 		Achievement nextItem = achievementData.getNextItem();
 		Achievement nextExhibitTotal = achievementData.getNextExhibitTotal();
 		//get current completion
 		SharedPreferences countPref = this.getSharedPreferences(ACHIEVEMENT, Context.MODE_PRIVATE);
 		int item_total = countPref.getInt(ITEMS_COMPLETE, 0);
 		int exhibit_total = countPref.getInt(EXHIBITS_COMPLETE, 0);
 		//check if achievement has been unlocked
 		if (nextItem != null && nextItem.checkCompletion(item_total)) {
 			DialogFragment dialog = AchievementDialogFragment.newInstance(nextItem, true);
 			//dialogs_pending.add(dialog);
 			FragmentManager fm = getSupportFragmentManager();
 			dialog.show(fm, "achievement");
 		}
 		if (nextExhibitTotal != null && nextExhibitTotal.checkCompletion(exhibit_total)) {
 			//display popup
 			DialogFragment dialog = AchievementDialogFragment.newInstance(nextExhibitTotal, true);
 			//dialogs_pending.add(dialog);
 			FragmentManager fm = getSupportFragmentManager();
 			dialog.show(fm, "achievement");
 		}
 		achievementData.getNextAchievements(item_total, exhibit_total);
 	}
 	
 	public void showHelp(){
 		dialog = DisplayTutorialDialogFragment.newInstance(R.drawable.tutorial_final);
 		FragmentManager fm = getSupportFragmentManager();
 		dialog.show(fm, "tutorial");
 	}
 	
 	public void showAbout() {
 		//show credits and app info
 		dialog = new DisplayAppInfoDialogFragment();
 		FragmentManager fm = getSupportFragmentManager();
 		dialog.show(fm, "app info");
 	}
 }
