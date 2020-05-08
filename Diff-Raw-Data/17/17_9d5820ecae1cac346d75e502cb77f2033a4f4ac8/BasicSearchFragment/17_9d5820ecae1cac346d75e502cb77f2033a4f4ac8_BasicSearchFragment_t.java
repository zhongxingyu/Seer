 package edu.grinnell.appdev.grinnelldirectory;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 /* A fragment off the Search Form Activity with a simple search interface */
 public class BasicSearchFragment extends SherlockFragment {
 	TextView firstNameText;
 	TextView lastNameText;
 	Button submitButton;
 	ImageView backgroundImage;
 
 	SearchFormActivity mActivity;
 	View mView;
 
 	// An intent for ProfileListActivity
 	Intent listIntent;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		mView = inflater.inflate(R.layout.fragment_simple_search, container,
 				false);
 		mActivity = (SearchFormActivity) getActivity();
 
 		setHasOptionsMenu(true);
 
		
 		return mView;
 	}
	
	public void onResume(){
	    super.onResume();
	    initializeViews(mActivity); // Initialize all of the variables.
	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.activity_search_form, menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.search:
 			sendQuery();
 			return true;
 		case R.id.reset:
 			clearFields();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	public void initializeViews(Context c) {
 		firstNameText = (TextView) mView.findViewById(R.id.first_text);
 		lastNameText = (TextView) mView.findViewById(R.id.last_text);
 		backgroundImage = (ImageView) mView.findViewById(R.id.background_image);
 
 		int backgroundNum = (int) (Math.random() * 5);
 		if (backgroundNum == 0) {
 			backgroundImage.setImageResource(R.drawable.jrc);
 		} else if (backgroundNum == 1)
 			backgroundImage.setImageResource(R.drawable.arh);
 		else if (backgroundNum == 2)
 			backgroundImage.setImageResource(R.drawable.main);
 		else if (backgroundNum == 3)
 			backgroundImage.setImageResource(R.drawable.gates);
 		else
 			backgroundImage.setImageResource(R.drawable.east);
 
 		OnEditorActionListener editTextListener = new OnEditorActionListener() {
 			@Override
 			public boolean onEditorAction(TextView v, int actionId,
 					KeyEvent event) {
 				boolean handled = false;
 				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
 					sendQuery();
 					handled = true;
 				}
 				return handled;
 			}
 		};
 
 		firstNameText.setOnEditorActionListener(editTextListener);
 		lastNameText.setOnEditorActionListener(editTextListener);
 	}
 
 	// send user search query
 	public void sendQuery() {
 		InputMethodManager imm = (InputMethodManager) mActivity
 				.getSystemService(Context.INPUT_METHOD_SERVICE);
 		// hide keyboard
 		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
 
 		String theURL = "https://itwebapps.grinnell.edu/classic/asp/campusdirectory/GCdefault.asp?transmit=true&blackboardref=true&LastName="
 				+ mActivity.cleanString(lastNameText.getText().toString())
 				+ "&LNameSearch=startswith&FirstName="
 				+ mActivity.cleanString(firstNameText.getText().toString());
 
 		new RequestTask(mActivity).execute(theURL);
 	}
 
 	public void clearFields() {
 		firstNameText.setText("");
 		lastNameText.setText("");
 	}
	
	@Override
	public void onPause(){
	    super.onPause();
	    if(backgroundImage != null)
		backgroundImage.setImageDrawable(null);
	}
	
 }
