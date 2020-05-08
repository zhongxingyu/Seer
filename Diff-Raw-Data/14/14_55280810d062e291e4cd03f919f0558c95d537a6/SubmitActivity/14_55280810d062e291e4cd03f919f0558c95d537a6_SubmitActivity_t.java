 package edu.gatech.oad.rocket.findmythings;
 
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 import com.google.api.client.util.DateTime;
 import com.google.api.services.fmthings.model.DBItem;
 import edu.gatech.oad.rocket.findmythings.control.LoginManager;
 import edu.gatech.oad.rocket.findmythings.model.Category;
 import edu.gatech.oad.rocket.findmythings.model.Type;
 import edu.gatech.oad.rocket.findmythings.service.EndpointUtils;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 
 /**
  * CS 2340 - FindMyStuff Android App
  * activity that deals with submitting a new item
  *
  * @author TeamRocket
  * */
 public class SubmitActivity extends Activity {
 
 	public static final String EXTRA_TYPE = "submittingType";
 	public static final int SUBMIT_REQUEST = 8002;
 
 	//UI references
 	private EditText description;
 	private EditText location;
 	private EditText reward;
 	private EditText iName;
 
 	private View focusView;
 
 	//Hold strings from the UI
 	private String desc, loc, name;
 	private int rward;
 
 	/**
 	 * The list to submit this item to.
 	 */
 	private Type mType = Type.LOST;
 
 	/**
 	 * Category for this item, helper for {@link SubmitFragment}.
 	 */
 	private Category mCategory = Category.MISC;
 
 	/**
 	 * Keep track of the task to ensure we can cancel it if requested.
 	 */
 	private SubmitItemTask mSubmitTask = null;
 	
 	private View mStatusForm;
 	private View mStatusView;
 	private TextView mStatusMessageView;
 
 	/**
 	 * creates new window with correct layout
 	 * @param savedInstanceState
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_submit);
 
 		//References the layout in activity_submit
 		iName = (EditText) findViewById(R.id.search_name);
 		description = (EditText) findViewById(R.id.description);
 		location = (EditText) findViewById(R.id.locationtext);
 		reward = (EditText) findViewById(R.id.rewardtext);
 
 		if (getIntent() != null && getIntent().getExtras() != null) {
 			String typeString = getIntent().getExtras().getString(EXTRA_TYPE);
 			mType = typeString == null ? Type.LOST : Type.valueOf(typeString);
 		}
 
 		// Hide the Up button in the action bar.
 		setupActionBar();
 
 		SubmitFragment frag = (SubmitFragment) getFragmentManager().findFragmentById(R.id.submit_fragment);
 		frag.syncTypePref(mType);
 		frag.syncCatPref(mCategory);
 		
 		mStatusForm = findViewById(R.id.submit_form);
 		mStatusView = findViewById(R.id.submit_status);
 		mStatusMessageView = (TextView) findViewById(R.id.submit_status_message);
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}.
 	 */
 	private void setupActionBar() {
 		getActionBar().setDisplayHomeAsUpEnabled(false);
 	}
 
 	/**
 	 * creates the options menu 
 	 * @param menu
 	 * @return true when done
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.submit, menu);
 		return true;
 	}
 
 	/**
 	 * deals with action to do once a key is pressed down
 	 * @param keyCode - key pressed
 	 * @param event - event to do in case of pressed
 	 * @return boolean signifying whether we responded to the event
 	 */
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event)  {
 		//Tells Activity what to do when back key is pressed
 	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
 			setResult(RESULT_CANCELED);
 	    	super.onBackPressed();
 			return true;
 	    }
 	    return super.onKeyDown(keyCode, event);
 	}
 	
 	/**
 	 * Checks for errors
 	 * @return boolean false(no errors) or true(there are errors)
 	 */
 	public boolean checkforErrors() {
 		boolean cancel = false;
 		focusView = null;
 
 		desc = description.getText().toString();
 		name = iName.getText().toString();
 
 		//Check to see if name is empty
 		if (TextUtils.isEmpty(name.trim())) {
 			iName.setError(getString(R.string.error_field_required));
 			focusView = iName;
 			cancel = true;
 		}
 
 		//Check to see if description is empty
 		if (TextUtils.isEmpty(desc.trim())) {
 			description.setError(getString(R.string.error_field_required));
 			focusView = description;
 			cancel = true;
 		}
 		return cancel;
 
 	}
 	
 	private boolean attemptToSubmit() {
 		if (mSubmitTask != null) {
 			return false;
 		}
 		
 		if (!checkforErrors()) {
 			mStatusMessageView.setText(R.string.submit_progress_message);
 			showProgress(true);
 			
 			loc = location.getText().toString();
 			rward = reward.getText().length() == 0 ? 0:Integer.parseInt(reward.getText().toString());
 
 
 			DBItem newItem = new DBItem().setName(name).setReward(rward)
 					.setDate(new DateTime(new Date()))
 					.setSubmittingUser(LoginManager.getLoginManager().getCurrentEmail())
 					.setCategory(mCategory.toString()).setType(mType.toString())
 					.setSearchableContent("").setSearchTokens(new ArrayList<String>())
 					.setDescription(desc).setLocation(loc);
 			
 			// Checks for valid user name
 			mSubmitTask = new SubmitItemTask();
 			mSubmitTask.execute(newItem);
 			return true;
 		} else {
 			focusView.requestFocus();
 			return false;
 		}
 	}
 	
 	/**
 	 * deals with action when an options button is selected
 	 * @param item
 	 * @return boolean  
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.submit_ok:
 			return attemptToSubmit();
 		case android.R.id.home:
 		case R.id.submit_cancel:
 			setResult(RESULT_CANCELED);
 			finish();
 			return true;
 		}
 
 
 		return super.onOptionsItemSelected(item);
 	}
 
 	/**
 	 * Sets the item type for this submission, i.e., the list
 	 * the item will be put on.
 	 * @param type An Item Type enumerated value.
 	 */
 	public void setItemType(Type type) {
 		mType = type;
 	}
 
 	/**
 	 * Returns the list the item will be put on.
 	 * @return An Item Type enumerated value.
 	 */
 	public Type getItemType() {
 		return mType;
 	}
 
 	/**
 	 * Sets the item category for this submission, used for filtering
 	 * @param type An Item Category enumerated value.
 	 */
 	public void setItemCategory(Category type) {
 		mCategory = type;
 	}
 
 	/**
 	 * Returns the category for the item.
 	 * @return An Item Category enumerated value.
 	 */
 	public Category getItemCategory() {
 		return mCategory;
 	}
 	/**
 	 * Shows the progress UI and hides the form.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
 	private void showProgress(final boolean show) {
 		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
 		// for very easy animations. If available, use these APIs to fade-in
 		// the progress spinner.
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
 			int shortAnimTime = getResources().getInteger(
 					android.R.integer.config_shortAnimTime);
 
 			mStatusView.setVisibility(View.VISIBLE);
 			mStatusView.animate().setDuration(shortAnimTime)
 					.alpha(show ? 1 : 0)
 					.setListener(new AnimatorListenerAdapter() {
 						@Override
 						public void onAnimationEnd(Animator animation) {
 							mStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
 						}
 					});
 
 			mStatusForm.setVisibility(View.VISIBLE);
 			mStatusForm.animate().setDuration(shortAnimTime)
 					.alpha(show ? 0 : 1)
 					.setListener(new AnimatorListenerAdapter() {
 						@Override
 						public void onAnimationEnd(Animator animation) {
 							mStatusForm.setVisibility(show ? View.GONE : View.VISIBLE);
 						}
 					});
 		} else {
 			// The ViewPropertyAnimator APIs are not available, so simply show
 			// and hide the relevant UI components.
 			mStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
 			mStatusForm.setVisibility(show ? View.GONE : View.VISIBLE);
 		}
 	}
 
 	/**
 	 * Represents an asynchronous submission task used to upload an item.
 	 */
 	public class SubmitItemTask extends AsyncTask<DBItem, Void, DBItem> {
 
 		@Override
 		protected DBItem doInBackground(DBItem... param) {
 			try {
 				return EndpointUtils.getEndpoint().items().insert(param[0]).execute();
 			} catch (IOException e) {
 				return null;
 			}
 		}
 
 		/**
 		 * deals with action when submitted either sucessfully or not
 		 * @param output - response from the API method
 		 */
 		@Override
 		protected void onPostExecute(final DBItem output) {
 			Intent resultIntent = new Intent();
 			resultIntent.putExtra(MainActivity.EXTRA_LIST, SubmitActivity.this.getItemType().toString());
 			setResult(Activity.RESULT_OK, resultIntent);
			showProgress(false);
 			finish();
 			//control.addItem(temp);
 		}
 
 		/**
 		 * deals with action when task cancelled
 		 */
 		@Override
 		protected void onCancelled() {
 			mSubmitTask = null;
 			showProgress(false);
 		}
 	}
 	
 	/**
 	 * Called to pop the login window from the navigation stack
 	 */
 	@Override 
     public void finish() {
         super.finish();
         overridePendingTransition(R.anim.hold, R.anim.slide_down_modal);
     }
 
 }
