 package com.herrbert74.cvpresenter.activities;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.googlecode.androidannotations.annotations.AfterViews;
 import com.googlecode.androidannotations.annotations.EActivity;
 import com.googlecode.androidannotations.annotations.ViewById;
 import com.herrbert74.cvpresenter.CVApp;
 import com.herrbert74.cvpresenter.CVConstants;
 import com.herrbert74.cvpresenter.R;
 import com.herrbert74.cvpresenter.dao.SharedPreferencesHelper;
 
 @EActivity(R.layout.activity_main)
 public class MainCV extends SherlockFragmentActivity implements CVConstants, ActionBar.OnNavigationListener {
 
 	@ViewById
 	Button btn_fill;
 	@ViewById
 	EditText txt_fill;
 	@ViewById
 	Spinner sp_load;
 
 	transient SharedPreferencesHelper prefs;
 
 	private int mTheme;
 
	// Needed for don't trigger actionbar.onnavigationselected actions(restart activity)
 	private boolean isLoading = true;
 	// Needed for don't trigger load spinner(start cv activity)
 	private boolean isLoadingSpinner = true;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		prefs = new SharedPreferencesHelper();
 		prefs.restorePreferences();
 
		// If theme was changed, get it from intent. Otherwise, get it from sharedpreferences
 		mTheme = getIntent().getIntExtra("theme", prefs.theme);
 
 		switch (mTheme) {
 		case 0:
 			setTheme(R.style.Theme_Android);
 			break;
 		case 1:
 			setTheme(R.style.Theme_Squares);
 			break;
 		default:
 		}
 
 		super.onCreate(savedInstanceState);
 
 		Context context = getSupportActionBar().getThemedContext();
 		ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(context, R.array.themes, R.layout.sherlock_spinner_item);
 		list.setDropDownViewResource(R.layout.spinner_dropdown_item);
 
 		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
 
 		getSupportActionBar().setListNavigationCallbacks(list, this);
 		getSupportActionBar().setSelectedNavigationItem(mTheme);
 
 	}
 
 	@AfterViews
 	public void av() {
 		btn_fill.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 
 				String passcode = txt_fill.getText().toString();
 				if (passcode.length() > 0) {
 					Intent intent = new Intent(CVApp.getContext(), CVPages_.class);
 					intent.putExtra("theme", getSupportActionBar().getSelectedNavigationIndex());
 					intent.putExtra("passcode", Integer.parseInt(passcode));
 					startActivity(intent);
 					finish();
 				} else {
 					Toast.makeText(MainCV.this, getResources().getString(R.string.main_empty_code), Toast.LENGTH_SHORT).show();
 				}
 			}
 		});
 
 		final List<String> list = getCVIDList();
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
 		sp_load.setAdapter(adapter);
 		sp_load.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 				if (isLoadingSpinner) {
 					isLoadingSpinner = false;
 				} else if (arg2 > 0) {
 					// Fetch the cv from preferences instead of the web
 					Intent intent = new Intent(CVApp.getContext(), CVPages_.class);
 					intent.putExtra("theme", getSupportActionBar().getSelectedNavigationIndex());
					intent.putExtra("requested_cv_no", arg2 - 1);
 					startActivity(intent);
 					finish();
 				}
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 
 			}
 		});
 	}
 
 	private List<String> getCVIDList() {
 		SharedPreferencesHelper prefs = new SharedPreferencesHelper();
 		List<String> list = new ArrayList<String>();
 		int[] cviDs = prefs.getCVIDs();
 		String[] cvNames = prefs.getCVNames();
 		list.add(getResources().getString(R.string.main_choose_one));
 		for (int index = 0; index < cviDs.length; index++) {
 			list.add(Integer.toString(cviDs[index]) + " " + cvNames[index]);
 		}
 		return list;
 	}
 
 	@Override
 	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
 		if (isLoading) {
 			isLoading = false;
 		} else {
 			Intent intent = new Intent(this, MainCV_.class);
 			intent.putExtra("theme", itemPosition);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			startActivity(intent);
 		}
 		return false;
 	}
 }
