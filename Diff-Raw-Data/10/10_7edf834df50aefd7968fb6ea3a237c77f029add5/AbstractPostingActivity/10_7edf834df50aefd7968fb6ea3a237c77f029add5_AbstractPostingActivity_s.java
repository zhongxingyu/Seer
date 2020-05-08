 /**
  * Main UI for the application. Displays a list of questions.
  * 
  * @author Cody Andrews, Phillip Leland, Justin Robb 05/01/2013
  * 
  */
 
 package com.huskysoft.interviewannihilator.ui;
 
 import java.io.File;
 import java.util.LinkedList;
 import java.util.List;
import java.util.Locale;

 import com.google.android.gms.common.AccountPicker;
 import com.huskysoft.interviewannihilator.R;
 import com.huskysoft.interviewannihilator.model.Category;
 import com.huskysoft.interviewannihilator.model.Difficulty;
 import com.huskysoft.interviewannihilator.model.Question;
 import com.huskysoft.interviewannihilator.model.RandomQuestionCollection;
 import com.huskysoft.interviewannihilator.runtime.FetchRandomQuestionsTask;
 import com.huskysoft.interviewannihilator.runtime.InitializeUserInfoTask;
 import com.huskysoft.interviewannihilator.service.QuestionService;
 import com.huskysoft.interviewannihilator.util.UIConstants;
 import com.huskysoft.interviewannihilator.util.Utility;
 import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
 import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivity;
 
 import android.accounts.AccountManager;
import android.annotation.SuppressLint;
 import android.app.Dialog;
 import android.content.Intent;
 import android.content.pm.ApplicationInfo;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.Adapter;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public abstract class AbstractPostingActivity extends SlidingActivity{
 
 	public static final String TAG = "AbstractPostingActivity";
 	/**
 	 * Unique request code for the AccountPicker intent in 
 	 * AbstractPostingActivity
 	 */
 	public static final int ACCT_PICKER_REQ_CODE = 6541328;
 
 	/** Indicates whether the user's private local data has been initialized **/
 	private static boolean userInfoLoaded = false;
 
 	public static boolean isUserInfoLoaded() {
 		return userInfoLoaded;
 	}
 
 	public static void setUserInfoLoaded(boolean isLoaded) {
 		AbstractPostingActivity.userInfoLoaded = isLoaded;
 	}
 
 	/** Shared SlideMenuInfo object */
 	protected SlideMenuInfo slideMenuInfo;
 
 	@Override
 	public synchronized void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Get info from transfer class
 		slideMenuInfo = SlideMenuInfo.getInstance();
 	}
 
 	/////////////////////////sliding menu stuff/////////////////////////
 
 	/**
 	 * Cycles through the current categories selected in the Slide
 	 * In Menu and places them in a list.
 	 * 
 	 * @return List of selected Categories
 	 */
 	public List<Category> getCurrentCategories(){
 		TableLayout table = (TableLayout) findViewById(R.id.slide_table);
 		List<Category> categories = new LinkedList<Category>();
 
 		for(int i = 1; i < table.getChildCount() - 1; i++){
 			TableRow row = (TableRow) table.getChildAt(i);
 			Spinner catSpinner = (Spinner) row.getChildAt(1);
 
 			int selection = 
 					catSpinner.getSelectedItemPosition();
 			
 			if (selection == 0){
 				categories.clear();
 			} else{
 				categories.add(
 						Category.values()[selection - 1]);
 
 			}
 
 
 
 		}
 		return categories;
 	}
 
 	/**
 	 * Builds a new Spinner object for a new Category.
 	 * Sets the selected value to the passed in parameter.
 	 * 
 	 * Called by MainActivity
 	 * 
 	 * @param selected
 	 * @return Spinner object.
 	 */
 	public Spinner newCategorySpinner(String selected){
 		Spinner newCategory = new Spinner(this);
 
 		ArrayAdapter<CharSequence> adapter = 
 				ArrayAdapter.createFromResource(this,
 						R.array.category, 
 						android.R.layout.simple_spinner_item);
 
 		// Specify the layout to use when the list of choices appears
 		adapter.setDropDownViewResource(
 				android.R.layout.simple_spinner_dropdown_item);
 
 		// Apply the adapter to the spinner
 		newCategory.setAdapter(adapter);
 
 		if(!selected.equals("")){
 			Adapter a = newCategory.getAdapter();
 			for (int x = 0; x < a.getCount(); x++){
 				String possible = a.getItem(x).toString().toUpperCase();
 				if (possible.equals(selected.toUpperCase())){
 					newCategory.setSelection(x);
 				}
 			}
 		}
 
 		return newCategory;
 	}
 
 	/**
 	 * Add another category spinner in the slide menu, exists to be called
 	 * by the "Add Category" button.
 	 */
 	public void addCategory(View v){
 		addCategory("");
 	}
 	
 	/**
 	 * Add another category spinner in the slide menu
 	 */
 	public void addCategory(String cat){
 		TableLayout table = (TableLayout) findViewById(R.id.slide_table);
 
 		if(table.getChildCount() < Category.values().length + 3){
 			TableRow row = new TableRow(this);
 
 			int pad = UIConstants.SLIDE_MENU_PADDING;
 			row.setPaddingRelative(pad, pad, pad, pad);
 
 			TextView categoryText = new TextView(this);
 
 			categoryText.setText("Category");
 			categoryText.setTextSize(UIConstants.SLIDE_MENU_TEXT_SIZE);
 
 			TableRow.LayoutParams params = new TableRow.LayoutParams(
 					TableRow.LayoutParams.MATCH_PARENT,
 					TableRow.LayoutParams.WRAP_CONTENT);
 
 			row.addView(categoryText, params);
 			
 			row.addView(newCategorySpinner(cat), params);
 			// Add new row before the buttons
 			table.addView(row, table.getChildCount() - 1);
 
 			// Set Remove button visible
 			Button removeButton = 
 					(Button) findViewById(R.id.remove_category_button);
 			removeButton.setVisibility(View.VISIBLE);
 		}
 	}
 
 	/**
 	 * OnClick handler for the remove category button of the
 	 * slide menu.
 	 * 
 	 * @param v
 	 */
 	public void removeCategory(View v){
 		TableLayout table = (TableLayout) findViewById(R.id.slide_table);
 		int baseRows = UIConstants.BASE_NUM_MENU_ROWS;
 
 		if(table.getChildCount() > baseRows){
 			table.removeViewAt(table.getChildCount() - (baseRows - 1));
 		}
 		if(table.getChildCount() == baseRows){
 			Button removeButton = 
 					(Button) findViewById(R.id.remove_category_button);
 			removeButton.setVisibility(View.GONE);
 		}
 	}
 
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
 
 				slideMenuInfo.setCat(getCurrentCategories());
 
 				Spinner diffSpinner = (Spinner) findViewById(R.id.diff_spinner);
 				int selected = diffSpinner.getSelectedItemPosition();
 				
 				if (selected == 0) {
 					slideMenuInfo.setDiff(null);
 				} else {
 					slideMenuInfo.setDiff(
 							Difficulty.values()[selected - 1]);
 				}
 
 				toggle();
 				Intent intent = new Intent(getApplicationContext(), 
 						MainActivity.class);
 				startActivity(intent);
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
 		boolean ret;
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			toggle();
 			ret = true;
 			break;
 
 		case R.id.random_question:
 			if(RandomQuestionCollection.getInstance().isEmpty()){
 				new FetchRandomQuestionsTask(this).execute();
 			} else {
 				Question rand = RandomQuestionCollection.
 						getInstance().getQuestion();
 				Intent intent = new Intent(this, QuestionActivity.class);
 				intent.putExtra(MainActivity.EXTRA_MESSAGE, rand);
 				startActivity(intent);
 			}
 			ret = false;
 			break;
 
 		default:
 			ret = super.onOptionsItemSelected(item);
 		}
 		return ret;
 	}
 
 	/**
 	 * Called when the user clicks on button to post a question
 	 * 
 	 * @param v The TextView that holds the selected question. 
 	 */
 	public void postQuestion(View v) {
 		if (userInfoLoaded) {
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
 	public void onValidationIssue() {
 		final Dialog dialog = new Dialog(this);
 		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
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
 	 * @return true on success
 	 */
 	public void initializeUserInfo() {
 		// try to load existing UserInfo
 		if (QuestionService.getInstance().loadUserInfo(getFilesDir())) {
 			userInfoSuccessFunction();
 			return;
 		}
 
 		// prompt the user to select an account
 		// skip if running a debug build (for compatibility reasons)
 		if ((getApplicationInfo().flags & 
 				ApplicationInfo.FLAG_DEBUGGABLE) == 0) {
 			String[] allowedTypes = {Utility.ACCOUNT_TYPE_GOOGLE};
 			Intent intent = AccountPicker.newChooseAccountIntent(null, null,
 					allowedTypes, false, null, null, null, null);
 			startActivityForResult(intent, ACCT_PICKER_REQ_CODE);
 		} else {
 			File dir = getFilesDir();
 			new InitializeUserInfoTask(
 					this, dir, Utility.DEBUG_USER_EMAIL).execute();
 		}
 
 		// (see onActivityResult)
 	}
 
 	/**
 	 * Lets the application know that user info is initialized and user can post
 	 * 
 	 */
 	public void userInfoSuccessFunction(){
 		setUserInfoLoaded(true);
 
 		String email = QuestionService.getInstance().getUserEmail();
 		String toastText = "Validated " + email;
 		Toast.makeText(getApplicationContext(), 
 				toastText, Toast.LENGTH_LONG).show();
 	}
 
 	/**
 	 * Explains to the user the concept of validation
 	 * and asks them if they want to retry
 	 */
 	public void onInitializeError(){
 		setUserInfoLoaded(false);
 		final Dialog dialog = new Dialog(this);
 		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
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
 				dialog.dismiss();
 			}
 		});
 		dialog.show();
 	}
 
 	@Override
 	protected void onActivityResult(final int requestCode, 
 			final int resultCode, final Intent data) {
 		if (requestCode == ACCT_PICKER_REQ_CODE && resultCode == RESULT_OK) {
 			// initialize UserInfo
 			String email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
 			File dir = getFilesDir();
 			new InitializeUserInfoTask(this, dir, email).execute();
 		}
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		writeUserInfo();
 	}
 
 	/**
 	 * Write the currently-loaded UserInfo object to disk. Returns true on
 	 * success, false on failure.
 	 * 
 	 * @return
 	 */
 	private static boolean writeUserInfo() {
 		if (isUserInfoLoaded()) {
 			try {
 				QuestionService.getInstance().writeUserInfo();
 				return true;
 			} catch (Exception e) {
 				Log.e(TAG, "Failed to write UserInfo :" + e.getMessage());
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 }
 
