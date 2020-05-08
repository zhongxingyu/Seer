 /**
  * Main UI for the application. Displays a list of questions.
  * 
  * @author Cody Andrews, Phillip Leland, Justin Robb 05/01/2013
  * 
  */
 
 package com.huskysoft.interviewannihilator.ui;
 
 import java.io.File;
 
 import com.huskysoft.interviewannihilator.R;
 import com.huskysoft.interviewannihilator.model.Category;
 import com.huskysoft.interviewannihilator.model.Difficulty;
 import com.huskysoft.interviewannihilator.runtime.InitializeUserTask;
 import com.huskysoft.interviewannihilator.util.UIConstants;
 import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
 import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivity;
 
 import android.annotation.SuppressLint;
 import android.app.Dialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public abstract class AbstractPostingActivity extends SlidingActivity{
 	
 	/** Do we have to initialize this user? **/
 	protected static boolean initializedUser = false;
 	
 	/** Do we have to initialize this user? **/
 	protected static boolean tryInitialize = true;
 	
 	/** Shared SlideMenuInfo object */
 	protected SlideMenuInfo slideMenuInfo;
 	
 	@SuppressLint("NewApi")
 	@Override
 	public synchronized void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);	
 
 		// Get info from transfer class
 		slideMenuInfo = SlideMenuInfo.getInstance();
 	}
 	
 	/////////////////////////sliding menu stuff/////////////////////////
 	/**
 	 * Set up the Slide menu
 	 */
 	public void buildSlideMenu(){
 		SlidingMenu menu = getSlidingMenu();
 		DisplayMetrics metrics = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(metrics);
 		int width = (int) ((double) metrics.widthPixels);
 		menu.setBehindOffset((int) 
 				(width * SlideMenuInfo.SLIDE_MENU_WIDTH));
 		
 		Spinner spinner = (Spinner) findViewById(R.id.diff_spinner);
 		ArrayAdapter<CharSequence> adapter = 
 				ArrayAdapter.createFromResource(this,
 				R.array.difficulty, 
 				android.R.layout.simple_spinner_item);
 		
 		// Specify the layout to use when the list of choices appears
 		adapter.setDropDownViewResource(
 				android.R.layout.simple_spinner_dropdown_item);
 		
 		// Apply the adapter to the spinner
 		spinner.setAdapter(adapter);
 		
 		
 		Spinner categorySpinner = 
 				(Spinner) findViewById(R.id.category_spinner);
 			ArrayAdapter<CharSequence> catAdapter = 
 					ArrayAdapter.createFromResource(this,
 					R.array.category, 
 					android.R.layout.simple_spinner_item);
 			
 			catAdapter.setDropDownViewResource(
 					android.R.layout.simple_spinner_dropdown_item);
 			
 			categorySpinner.setAdapter(catAdapter);
 			
 			
 		// Handle onClick of Slide-Menu button
 		Button button = (Button) findViewById(R.id.slide_menu_button);
 		button.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Spinner diffSpinner = (Spinner) findViewById(R.id.diff_spinner);
 				String diffStr = diffSpinner.getSelectedItem().toString();
 				
 				Spinner catSpinner = 
 						(Spinner) findViewById(R.id.category_spinner);
 				String categoryStr = 
 						catSpinner.getSelectedItem()
 						.toString().replaceAll("\\s", "");
 				toggle();
 				
 
				if (diffStr == null || diffStr.isEmpty() ||
 					diffStr.equals(UIConstants.ALL)) {
 					slideMenuInfo.setDiff(null);
 				} else {
 					slideMenuInfo.setDiff(
 							Difficulty.valueOf(diffStr.toUpperCase()));
 				}
 				
 				if (categoryStr == null || categoryStr.length() == 0 ||
 					categoryStr.equals(UIConstants.ALL)){
 					slideMenuInfo.setCat(null);
 				} else{
 					slideMenuInfo.setCat(
 							Category.valueOf(categoryStr.toUpperCase()));
 				}
 				
 			}
 		});
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.post_solution, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			toggle();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	/**
 	 * Called when the user clicks on button to post a question
 	 * 
 	 * @param v The TextView that holds the selected question. 
 	 */
 	public void postQuestion(View v){
 		if (initializedUser){
 			Intent intent = new Intent(this, PostQuestionActivity.class);
 			startActivity(intent);
 		} else {
 			// helpful message
 			onValidationIssue();
 		}
 	}
 	
 	/**
 	 * Displays a message explaining why a user can't post something
 	 */
 	public void onValidationIssue(){
 		final Dialog dialog = new Dialog(this);
 		dialog.setContentView(R.layout.alertdialogcustom);
 		// set the custom dialog components - text, buttons
 		TextView text = (TextView) dialog.findViewById(R.id.dialog_text_alert);
 		text.setText(getString(R.string.userInfoHelp_title));
 		Button dialogButton = (Button) 
 				dialog.findViewById(R.id.dialogButtonOK);
 		// if button is clicked, send the solution
 		dialogButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Toast.makeText(getApplicationContext(), 
 						R.string.toast_return, Toast.LENGTH_LONG).show();
 				dialog.dismiss();
 			}
 		});
 		dialog.show();
 	}
 	
 	//////////////////User Validation stuff//////////////////////////
 	
 	/**
 	 * Attempts to initialize the user's information on database
 	 * 
 	 */
 	public void initializeUserInfo(){
 		File dir = getFilesDir();
 		new InitializeUserTask(this, dir, "Anon@example.com").execute();
 	}
 	
 	/**
 	 * Lets the application know that user info is initialized and user can post
 	 * 
 	 */
 	public void userInfoSuccessFunction(){
 		initializedUser = true;
 	}
 	
 	/**
 	 * Explains to the user the concept of validation
 	 * and asks them if they want to retry
 	 */
 	public void onInitializeError(){
 		initializedUser = false;
 		final Dialog dialog = new Dialog(this);
 		dialog.setContentView(R.layout.retrydialogcustom);
 		// set the custom dialog components - text, buttons
 		TextView text = (TextView) dialog.findViewById(R.id.dialog_text);
 		text.setText(getString(R.string.userInfoError_title));
 		Button dialogButton = (Button) 
 				dialog.findViewById(R.id.button_retry);
 		dialogButton.setText(getString(R.string.userInfoHelp_retry));
 		// if button is clicked, send the solution
 		dialogButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Toast.makeText(getApplicationContext(), 
 						R.string.toast_retry, Toast.LENGTH_LONG).show();
 				initializeUserInfo();
 			}
 		});
 		dialogButton = (Button) dialog.findViewById(R.id.button_cancel);
 		dialogButton.setText(getString(R.string.userInfoHelp_cancel));
 		// if button is clicked, close the custom dialog
 		dialogButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Toast.makeText(getApplicationContext(), 
 						R.string.toast_return, Toast.LENGTH_LONG).show();
 				tryInitialize = false;
 				dialog.dismiss();
 			}
 		});
 		dialog.show();
 	}
 }
 
