 package com.ladinc.showrss.activitys;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.ladinc.showrss.YourShows;
 import com.ladinc.showrss.domain.Show;
 import com.ladinc.showrss.utilities.LoadingDialog;
 import com.ladinc.showrss.utilities.Utilities;
 import com.ladinc.showrss.R;
 
 public class ShowConfigActivity extends Activity implements OnItemSelectedListener, OnClickListener {
 	private static final String TAG = "ShowConfigActivity";
 
 	String showName = "";
 
 	Button saveSettingsButton;
 	Button deleteShowButton;
 	TextView showNameTextView;
 	CheckBox showRepackCheckbox;
 	Spinner showQualitySpinner;
 
 	boolean inHD = false;
 	boolean inSD = false;
 
 	LoadingDialog loadingDialog;
 	
 	Show thisShow;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.showconfig);
 
 		Bundle bundle = getIntent().getExtras();
 		showName = bundle.get("showName").toString();
 
 		this.setupViews();
 		this.setupListeners();
 
 		if (Utilities.isOnline(this)) {
 			new getSettings().execute(showName);
 		} else {
 			displayToast(getString(R.string.no_internet_connection));
 		}
 
 	}
 
 	private void setupViews() {
 		showNameTextView = (TextView) findViewById(R.id.showNameTop);
 		saveSettingsButton = (Button) findViewById(R.id.saveSettingsButton);
 		deleteShowButton = (Button) findViewById(R.id.deleteShowButton);
 		showQualitySpinner = (Spinner) findViewById(R.id.spinner1);
 		showRepackCheckbox = (CheckBox) findViewById(R.id.checkBox1);
 
 		showNameTextView.setText(showName);
 		loadingDialog = new LoadingDialog(this, "");
 
 	}
 
 	private void setupListeners() {
 
 		saveSettingsButton.setOnClickListener(this);
 		deleteShowButton.setOnClickListener(this);
 		showQualitySpinner.setOnItemSelectedListener(this);
 
 	}
 
 	public void switchActivity(@SuppressWarnings("rawtypes") Class className) {
 		Intent myIntent = new Intent(this, className);
 		startActivity(myIntent);
 	}
 
 	private void saveSettings() {
 		if (inHD || inSD) {
 			try {
 				YourShows.setShowSettings(showName, inSD, inHD, showRepackCheckbox.isChecked());
 			} catch (Exception e) {
 				e.printStackTrace();
 				switchActivity(LoginActivity.class);
 			}
 		} else {
 			Toast.makeText(this, "You need to select a quality value.", Toast.LENGTH_SHORT).show();
 		}
 
 		// finish();
 	}
 
 	@Override
 	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
 		String selected = parent.getItemAtPosition(pos).toString();
 		if (selected.equals("HD")) {
 			inSD = false;
 			inHD = true;
 		} else if (selected.equals("SD")) {
 			inSD = true;
 			inHD = false;
 		} else if (selected.equals("Both")) {
 			inSD = true;
 			inHD = true;
 		}
 	}
 
 	/**
 	 * If nothing is selected, we have to reset everything back
 	 */
 	@Override
 	public void onNothingSelected(AdapterView<?> parent) {
 		inHD = false;
 		inSD = false;
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (Utilities.isOnline(this)) {
 			switch (v.getId()) {
 			case R.id.saveSettingsButton:
 				new updateShowSettings().execute(showName);
 				break;
 			case R.id.deleteShowButton:
 				new deleteShow().execute(showName);
 				break;
 			}
 		} else {
 			displayToast(getString(R.string.no_internet_connection));
 		}
 
 	}
 
 	private void displayToast(String message) {
 		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
 	}
 
 	private void populateSetitngs() 
 	{
 		Show show = this.thisShow;
 		boolean repack = false;
 
 		if (show.getHasProper() == 1)
 			repack = true;
 
 		showQualitySpinner.setSelection(show.getHasHd());
 
 		showRepackCheckbox.setChecked(repack);
 
 	}
 	
 	public Show getShowSettings(String showName) throws Exception
 	{
 		this.thisShow = new Show( YourShows.getShowSettings(showName));
 		return this.thisShow;
 	}
 
 	// Subclass for deleting shows
 	class deleteShow extends AsyncTask<String, Integer, String> {
 		@Override
 		protected void onPreExecute() {
 			loadingDialog.setMessage("Deleting...");
 			loadingDialog.showLoadingDialog();
 		}
 
 		@Override
 		protected String doInBackground(String... selectedShow) {
 			try {
 				YourShows.deleteShow(selectedShow[0]);
 				return selectedShow[0];
 			} catch (Exception e) {
 				e.printStackTrace();
 				return "earraid";
 			}
 		}
 
 		@Override
 		protected void onPostExecute(String selectedShow) {
 
 			if (selectedShow == "earraid") {
 				loadingDialog.hideLoadingDialog();
 				switchActivity(LoginActivity.class);
 			} else {
 				Log.d(TAG, "Successfully deleted " + selectedShow);
 
 				loadingDialog.hideLoadingDialog();
 
 				displayToast("Deleted " + selectedShow);
 
 				onBackPressed();
 			}
 
 		}
 	}
 
 	class updateShowSettings extends AsyncTask<String, Integer, String> {
 		@Override
 		protected void onPreExecute() {
 			loadingDialog.setMessage("Updating Settings...");
 			loadingDialog.showLoadingDialog();
 		}
 
 		@Override
 		protected String doInBackground(String... selectedShow) {
 			try {
 				saveSettings();
 				return selectedShow[0];
 			} catch (Exception e) {
 				e.printStackTrace();
 				return "earraid";
 			}
 		}
 
 		@Override
 		protected void onPostExecute(String selectedShow) {
 
 			if (selectedShow == "earraid") {
 				loadingDialog.hideLoadingDialog();
 				switchActivity(LoginActivity.class);
 			} else {
 				Log.d(TAG, "Successfully deleted " + selectedShow);
 
 				loadingDialog.hideLoadingDialog();
 
 				displayToast("Updated Settings for " + selectedShow);
 
 				onBackPressed();
 			}
 
 		}
 	}
 
 	// Subclass for pulling down show settings
 	class getSettings extends AsyncTask<String, Integer, Show> {
 		@Override
 		protected void onPreExecute() {
 			loadingDialog.setMessage("Loading Show's Settings...");
 			loadingDialog.showLoadingDialog();
 		}
 
 		@Override
 		protected Show doInBackground(String... selectedShow) {
 			try {
 <<<<<<< HEAD
 				Show show = getShowSettings(selectedShow[0]);
 				return show;
 =======
 				thisShow = getShowSettings(selectedShow[0]);
 				return thisShow;
 >>>>>>> 073dd85aabd26d4759fbe79465fd9e2b75a33147
 			} catch (Exception e) {
 				e.printStackTrace();
 				// I know we don't like returning null, but not sure what else
 				// to do here!
 				return null;
 			}
 		}
 
 		@Override
 		protected void onPostExecute(Show show) {
 
 			if (show == null) {
 				loadingDialog.hideLoadingDialog();
 				switchActivity(LoginActivity.class);
 			} else {
 				Log.d(TAG, "Got settings for " + show.getShowName());
 
 <<<<<<< HEAD
 				
 				thisShow = show;
 				
 				populateSetitngs();
 				
 				loadingDialog.hideLoadingDialog();
 =======
 				loadingDialog.hideLoadingDialog();
 
 				populateSetitngs();
 >>>>>>> 073dd85aabd26d4759fbe79465fd9e2b75a33147
 			}
 
 		}
 
 	}
 
 }
