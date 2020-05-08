 package com.seawolfsanctuary.keepingtracks;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import org.holoeverywhere.app.AlertDialog;
 import org.holoeverywhere.app.ProgressDialog;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.TabActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.CheckBox;
 import android.widget.DatePicker;
 import android.widget.ScrollView;
 import android.widget.TabHost;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 import com.seawolfsanctuary.keepingtracks.database.Journey;
 import com.seawolfsanctuary.keepingtracks.foursquare.CheckinActivity;
 
 public class AddActivity extends TabActivity {
 
 	Bundle template;
 
 	TabHost mTabHost;
 
 	TextView txt_FromSearch;
 	ArrayAdapter<String> ada_fromSearchAdapter;
 	DatePicker dp_FromDate;
 	TimePicker tp_FromTime;
 	AutoCompleteTextView actv_FromSearch;
 
 	ScrollView scrl_Detail;
 	TextView txt_DetailClass;
 	CheckBox chk_DetailClass;
 	TextView txt_DetailHeadcode;
 	CheckBox chk_DetailHeadcode;
 	CheckBox chk_DetailUseForStats;
 
 	TextView txt_ToSearch;
 	ArrayAdapter<String> ada_toSearchAdapter;
 	DatePicker dp_ToDate;
 	TimePicker tp_ToTime;
 	AutoCompleteTextView actv_ToSearch;
 
 	TextView txt_summary_from_stn_data;
 	TextView txt_summary_from_datetime_data;
 	TextView txt_summary_to_stn_data;
 	TextView txt_summary_to_datetime_data;
 	TableRow trow_summary_class;
 	TextView txt_summary_class_data;
 	TableRow trow_summary_headcode;
 	TextView txt_summary_headcode_data;
 	CheckBox chk_Checkin;
 
 	SharedPreferences settings;
 
 	private ProgressDialog dialog;
 
 	boolean isLocationEnabledNetwork = false;
 	boolean isLocationEnabledGPS = false;
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.context_menu_add, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.save:
 			boolean success = writeEntry(this.getCurrentFocus());
 			if (success == true) {
 				Intent intent = new Intent(this, ListSavedActivity.class);
 				AddActivity.this.finish();
 				startActivity(intent);
 			}
 			return success;
 		case R.id.cancel:
 			AddActivity.this.finish();
 			return true;
 		default:
 			return true;
 		}
 	}
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.add_activity);
 		settings = getSharedPreferences(UserPrefsActivity.APP_PREFS,
 				MODE_PRIVATE);
 
 		isLocationEnabledNetwork = Helpers
 				.isLocationEnabledNetwork(getApplicationContext());
 		isLocationEnabledGPS = Helpers
 				.isLocationEnabledGPS(getApplicationContext());
 
 		template = getIntent().getExtras();
 		Helpers.loadCurrentJourney(template, AddActivity.this);
 
 		mTabHost = getTabHost();
 		mTabHost.addTab(mTabHost.newTabSpec("tc_From")
 				.setIndicator(getString(R.string.add_tab_from))
 				.setContent(R.id.tc_From));
 		mTabHost.addTab(mTabHost.newTabSpec("tc_Detail")
 				.setIndicator(getString(R.string.add_tab_detail))
 				.setContent(R.id.tc_Detail));
 		mTabHost.addTab(mTabHost.newTabSpec("tc_To")
 				.setIndicator(getString(R.string.add_tab_to))
 				.setContent(R.id.tc_To));
 		mTabHost.addTab(mTabHost.newTabSpec("tc_Summary")
 				.setIndicator(getString(R.string.add_tab_summary))
 				.setContent(R.id.tc_Summary));
 
 		if (settings.getBoolean("AdvancedJourneys", false) == false) {
 			mTabHost.getTabWidget().getChildAt(1).setVisibility(View.GONE);
 		}
 
 		mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
 			@Override
 			public void onTabChanged(String tabID) {
 				try {
 					template.isEmpty();
 				} catch (NullPointerException e) {
 					template = new Bundle();
 				}
 
 				template = Helpers.saveCurrentJourney(template,
 						AddActivity.this);
 
 				if (tabID == "tc_From") {
 					if (template.containsKey("from_stn")) {
 						actv_FromSearch = (AutoCompleteTextView) findViewById(R.id.actv_FromSearch);
 						actv_FromSearch.setText(template.getString("from_stn"));
 					}
 					if (template.containsKey("from_year")) {
 						dp_FromDate = (DatePicker) findViewById(R.id.dp_FromDate);
 						dp_FromDate.init(template.getInt("from_date_year"),
 								template.getInt("from_date_month"),
 								template.getInt("from_date_day"), null);
 					}
 					if (template.containsKey("from_time_hour")) {
 						tp_FromTime = (TimePicker) findViewById(R.id.tp_FromTime);
 						tp_FromTime.setCurrentHour(template
 								.getInt("from_time_hour"));
 						tp_FromTime.setCurrentMinute(template
 								.getInt("from_time_minute"));
 					}
 				}
 
 				if (tabID == "tc_Detail") {
 					chk_DetailClass = (CheckBox) findViewById(R.id.chk_DetailClass);
 					txt_DetailClass = (TextView) findViewById(R.id.txt_DetailClass);
 					chk_DetailHeadcode = (CheckBox) findViewById(R.id.chk_DetailHeadcode);
 					txt_DetailHeadcode = (TextView) findViewById(R.id.txt_DetailHeadcode);
 					chk_DetailUseForStats = (CheckBox) findViewById(R.id.chk_DetailUseForStats);
 
 					if (template.containsKey("detail_class_checked")) {
 						chk_DetailClass.setChecked(template
 								.getBoolean("detail_class_checked"));
 						txt_DetailClass.setEnabled(template
 								.getBoolean("detail_class_checked"));
 					}
 
 					if (template.containsKey("detail_class")) {
 						txt_DetailClass.setText(template
 								.getCharSequence("detail_class"));
 					}
 
 					if (template.containsKey("detail_headcode_checked")) {
 						chk_DetailHeadcode.setChecked(template
 								.getBoolean("detail_headcode_checked"));
 						txt_DetailHeadcode.setEnabled(template
 								.getBoolean("detail_headcode_checked"));
 					}
 
 					if (template.containsKey("detail_headcode")) {
 						txt_DetailHeadcode.setText(template
 								.getCharSequence("detail_headcode"));
 					}
 
 					if (template.containsKey("detail_use_for_stats")) {
 						chk_DetailUseForStats.setChecked(template
 								.getBoolean("detail_use_for_stats"));
 					}
 				}
 
 				if (tabID == "tc_To") {
 					if (template.containsKey("to_stn")) {
 						actv_ToSearch = (AutoCompleteTextView) findViewById(R.id.actv_ToSearch);
 						actv_ToSearch.setText(template.getString("to_stn"));
 					}
 					if (template.containsKey("to_date_year")) {
 						dp_ToDate = (DatePicker) findViewById(R.id.dp_ToDate);
 						dp_ToDate.init(template.getInt("to_date_year"),
 								template.getInt("to_date_month"),
 								template.getInt("to_date_day"), null);
 					}
 					if (template.containsKey("to_time_hour")) {
 						tp_ToTime = (TimePicker) findViewById(R.id.tp_ToTime);
 						tp_ToTime.setCurrentHour(template
 								.getInt("to_time_hour"));
 						tp_ToTime.setCurrentMinute(template
 								.getInt("to_time_minute"));
 					}
 				}
 
 				if (tabID == "tc_Summary") {
 					updateSummary();
 					chk_Checkin = (CheckBox) findViewById(R.id.chk_Checkin);
 					actv_FromSearch = (AutoCompleteTextView) findViewById(R.id.actv_FromSearch);
 					actv_ToSearch = (AutoCompleteTextView) findViewById(R.id.actv_ToSearch);
 
 					boolean foursquareSetup = (Helpers.readAccessToken()
 							.length() > 0);
 					boolean locationAvailable = (isLocationEnabledNetwork || isLocationEnabledGPS);
 
 					if (foursquareSetup && locationAvailable) {
 						if (actv_FromSearch.getText().length() > 0
 								|| actv_ToSearch.getText().length() > 0) {
 							chk_Checkin.setChecked(!template
 									.containsKey("editing"));
 						}
 					} else {
 						chk_Checkin.setEnabled(false);
 						chk_Checkin.setChecked(false);
 					}
 				}
 			}
 		});
 
 		mTabHost.setCurrentTab(0);
 
 		// Link array of completions
 		String[] completions = read_csv("stations.lst");
 		ada_fromSearchAdapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_dropdown_item_1line, completions);
 		ada_toSearchAdapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_dropdown_item_1line, completions);
 
 		actv_FromSearch = (AutoCompleteTextView) findViewById(R.id.actv_FromSearch);
 		actv_ToSearch = (AutoCompleteTextView) findViewById(R.id.actv_ToSearch);
 		OnItemClickListener cl_FromToClickListener = new AdapterView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				updateSummary();
 				Helpers.hideKeyboard(view);
 			}
 		};
 
 		actv_FromSearch.setAdapter(ada_fromSearchAdapter);
 		actv_FromSearch.setThreshold(2);
 		actv_FromSearch.setOnItemClickListener(cl_FromToClickListener);
 
 		actv_ToSearch.setAdapter(ada_toSearchAdapter);
 		actv_ToSearch.setThreshold(2);
 		actv_ToSearch.setOnItemClickListener(cl_FromToClickListener);
 
 		try {
 			if (template.containsKey("editing")) {
 				getActionBar().setTitle(R.string.edit_saved);
 			}
 			if (template.containsKey("copying")) {
 				getActionBar().setTitle(R.string.copy_saved);
 			}
 		} catch (NullPointerException e) {
 			// meh
 		}
 		MenuActivity.hideLoader();
 	}
 
 	private String[] read_csv(String filename) {
 		String[] array = {};
 
 		try {
 			InputStream input;
 			input = getAssets().open(filename);
 			int size = input.available();
 			byte[] buffer = new byte[size];
 
 			input.read(buffer);
 			input.close();
 			array = new String(buffer).split("\n");
 
 			if (array.length < 1) {
 				Toast.makeText(getApplicationContext(),
 						getText(R.string.add_new_empty_stations),
 						Toast.LENGTH_LONG).show();
 			}
 
 		} catch (Exception e) {
 			actv_FromSearch = (AutoCompleteTextView) findViewById(R.id.actv_FromSearch);
 			actv_FromSearch.setError(getText(R.string.add_new_empty_stations));
 			actv_FromSearch.setEnabled(false);
 
 			actv_ToSearch = (AutoCompleteTextView) findViewById(R.id.actv_ToSearch);
 			actv_ToSearch.setError(getText(R.string.add_new_empty_stations));
 			actv_ToSearch.setEnabled(false);
 		}
 
 		return array;
 	}
 
 	private void updateSummary() {
 		actv_FromSearch = (AutoCompleteTextView) findViewById(R.id.actv_FromSearch);
 		dp_FromDate = (DatePicker) findViewById(R.id.dp_FromDate);
 		tp_FromTime = (TimePicker) findViewById(R.id.tp_FromTime);
 
 		chk_DetailClass = (CheckBox) findViewById(R.id.chk_DetailClass);
 		txt_DetailClass = (TextView) findViewById(R.id.txt_DetailClass);
 		chk_DetailHeadcode = (CheckBox) findViewById(R.id.chk_DetailHeadcode);
 		txt_DetailHeadcode = (TextView) findViewById(R.id.txt_DetailHeadcode);
 
 		actv_ToSearch = (AutoCompleteTextView) findViewById(R.id.actv_ToSearch);
 		dp_ToDate = (DatePicker) findViewById(R.id.dp_ToDate);
 		tp_ToTime = (TimePicker) findViewById(R.id.tp_ToTime);
 
 		txt_summary_from_stn_data = (TextView) findViewById(R.id.txt_summary_from_stn_data);
 		txt_summary_from_datetime_data = (TextView) findViewById(R.id.txt_summary_from_datetime_data);
 		txt_summary_to_stn_data = (TextView) findViewById(R.id.txt_summary_to_stn_data);
 		txt_summary_to_datetime_data = (TextView) findViewById(R.id.txt_summary_to_datetime_data);
 		trow_summary_class = (TableRow) findViewById(R.id.trow_summary_class);
 		txt_summary_class_data = (TextView) findViewById(R.id.txt_summary_class_data);
 		trow_summary_headcode = (TableRow) findViewById(R.id.trow_summary_headcode);
 		txt_summary_headcode_data = (TextView) findViewById(R.id.txt_summary_headcode_data);
 
 		txt_summary_from_stn_data.setText(Helpers.trimCodeFromStation(
 				actv_FromSearch.getText().toString(), getApplicationContext()));
 
 		txt_summary_from_datetime_data.setText(getString(
 				R.string.add_summary_datetime,
 				Helpers.leftPad("" + dp_FromDate.getYear(), 4),
 				Helpers.leftPad("" + (dp_FromDate.getMonth() + 1), 2),
 				Helpers.leftPad("" + dp_FromDate.getDayOfMonth(), 2),
 				Helpers.leftPad("" + tp_FromTime.getCurrentHour(), 2),
 				Helpers.leftPad("" + tp_FromTime.getCurrentMinute(), 2)));
 
 		txt_summary_to_stn_data.setText(Helpers.trimCodeFromStation(
 				actv_ToSearch.getText().toString(), getApplicationContext()));
 
 		txt_summary_to_datetime_data.setText(getString(
 				R.string.add_summary_datetime,
 				Helpers.leftPad("" + dp_ToDate.getYear(), 4),
 				Helpers.leftPad("" + (dp_ToDate.getMonth() + 1), 2),
 				Helpers.leftPad("" + dp_ToDate.getDayOfMonth(), 2),
 				Helpers.leftPad("" + tp_ToTime.getCurrentHour(), 2),
 				Helpers.leftPad("" + tp_ToTime.getCurrentMinute(), 2)));
 
 		if (settings.getBoolean("AdvancedJourneys", false) == true) {
 			if (chk_DetailClass.isChecked() == true) {
 				trow_summary_class.setVisibility(View.VISIBLE);
 				txt_summary_class_data.setText(txt_DetailClass.getText()
 						.toString());
 			} else {
 				txt_summary_class_data.setText("");
 				trow_summary_class.setVisibility(View.GONE);
 			}
 
 			if (chk_DetailHeadcode.isChecked() == true) {
 				trow_summary_headcode.setVisibility(View.VISIBLE);
 				txt_summary_headcode_data.setText(txt_DetailHeadcode.getText()
 						.toString());
 			} else {
 				txt_summary_headcode_data.setText("");
 				trow_summary_headcode.setVisibility(View.GONE);
 			}
 		} else {
 			trow_summary_headcode.setVisibility(View.GONE);
 			trow_summary_class.setVisibility(View.GONE);
 		}
 	}
 
 	public void onClassCheckboxClicked(View view) {
 		CheckBox chk_DetailClass = (CheckBox) findViewById(R.id.chk_DetailClass);
 		TextView txt_DetailClass = (TextView) findViewById(R.id.txt_DetailClass);
 		txt_DetailClass.setEnabled(((CheckBox) chk_DetailClass).isChecked());
 
 		Helpers.hideKeyboard(view);
 	}
 
 	public void onHeadcodeCheckboxClicked(View view) {
 		CheckBox chk_DetailHeadcode = (CheckBox) findViewById(R.id.chk_DetailHeadcode);
 		boolean checked = chk_DetailHeadcode.isChecked();
 		onHeadcodeCheckboxClicked(view, checked);
 	}
 
 	public void onHeadcodeCheckboxClicked(View view, boolean checked) {
 		CheckBox chk_DetailHeadcode = (CheckBox) findViewById(R.id.chk_DetailHeadcode);
 		TextView txt_DetailHeadcode = (TextView) findViewById(R.id.txt_DetailHeadcode);
 		chk_DetailHeadcode.setChecked(checked);
 		txt_DetailHeadcode.setEnabled(checked);
 
 		Helpers.hideKeyboard(view);
 	}
 
 	public boolean writeEntry(View view) {
 		boolean success = false;
 		actv_FromSearch = (AutoCompleteTextView) findViewById(R.id.actv_FromSearch);
 		dp_FromDate = (DatePicker) findViewById(R.id.dp_FromDate);
 		tp_FromTime = (TimePicker) findViewById(R.id.tp_FromTime);
 
 		chk_DetailClass = (CheckBox) findViewById(R.id.chk_DetailClass);
 		txt_DetailClass = (TextView) findViewById(R.id.txt_DetailClass);
 		chk_DetailHeadcode = (CheckBox) findViewById(R.id.chk_DetailHeadcode);
 		txt_DetailHeadcode = (TextView) findViewById(R.id.txt_DetailHeadcode);
 		chk_DetailUseForStats = (CheckBox) findViewById(R.id.chk_DetailUseForStats);
 
 		actv_ToSearch = (AutoCompleteTextView) findViewById(R.id.actv_ToSearch);
 		dp_ToDate = (DatePicker) findViewById(R.id.dp_ToDate);
 		tp_ToTime = (TimePicker) findViewById(R.id.tp_ToTime);
 
 		String detailClass = "";
 		if (chk_DetailClass.isChecked() == true) {
 			detailClass = txt_DetailClass.getText().toString();
 		}
 
 		String detailHeadcode = "";
 		if (chk_DetailHeadcode.isChecked() == true) {
 			detailHeadcode = txt_DetailHeadcode.getText().toString();
 		}
 
 		if (template.containsKey("editing")) {
 
 			// Editing an existing journey
 			Journey db_journeys = new Journey(this);
 			db_journeys.open();
 			boolean updated = db_journeys.updateJourney(template.getInt("id"),
 					actv_FromSearch.getText().toString(),
 					dp_FromDate.getYear(), (dp_FromDate.getMonth() + 1),
 					dp_FromDate.getDayOfMonth(), tp_FromTime.getCurrentHour(),
 					tp_FromTime.getCurrentMinute(), actv_ToSearch.getText()
 							.toString(), dp_ToDate.getYear(), (dp_ToDate
 							.getMonth() + 1), dp_ToDate.getDayOfMonth(),
 					tp_ToTime.getCurrentHour(), tp_ToTime.getCurrentMinute(),
 					detailClass, detailHeadcode, chk_DetailUseForStats
 							.isChecked());
 			db_journeys.close();
 
 			if (updated == true) {
 				success = true;
 				Toast.makeText(getApplicationContext(),
 						getString(R.string.edit_saved_edited),
 						Toast.LENGTH_SHORT).show();
 			} else {
 				Toast.makeText(getApplicationContext(),
 						getString(R.string.edit_saved_error),
 						Toast.LENGTH_SHORT).show();
 			}
 		} else {
 			// Adding a journey, either new or copied
 			Journey db_journeys = new Journey(this);
 			db_journeys.open();
 			long id;
 			id = db_journeys.insertJourney(
 					actv_FromSearch.getText().toString(),
 					dp_FromDate.getYear(), (dp_FromDate.getMonth() + 1),
 					dp_FromDate.getDayOfMonth(), tp_FromTime.getCurrentHour(),
 					tp_FromTime.getCurrentMinute(), actv_ToSearch.getText()
 							.toString(), dp_ToDate.getYear(), (dp_ToDate
 							.getMonth() + 1), dp_ToDate.getDayOfMonth(),
 					tp_ToTime.getCurrentHour(), tp_ToTime.getCurrentMinute(),
 					detailClass, detailHeadcode, chk_DetailUseForStats
 							.isChecked());
 			db_journeys.close();
 
 			if (id != -1) {
 				success = true;
 				Toast.makeText(getApplicationContext(),
 						getString(R.string.add_new_added), Toast.LENGTH_SHORT)
 						.show();
 			} else {
 				Toast.makeText(getApplicationContext(),
 						getString(R.string.add_new_error), Toast.LENGTH_SHORT)
 						.show();
 			}
 		}
 
 		if (success == true) {
 			chk_Checkin = (CheckBox) findViewById(R.id.chk_Checkin);
 			if (chk_Checkin.isChecked()) {
 				Bundle details = new Bundle();
 				details.putString("from_stn", Helpers.trimCodeFromStation(
 						actv_FromSearch.getText().toString(),
 						getApplicationContext()));
 				details.putString("to_stn", Helpers.trimCodeFromStation(
 						actv_ToSearch.getText().toString(),
 						getApplicationContext()));
 				details.putString("detail_class", detailClass);
 				details.putString("detail_headcode", detailHeadcode);
 
 				AddActivity.this.finish();
 				Intent intent = new Intent(this, ListSavedActivity.class);
 				startActivity(intent);
 
 				startFoursquareCheckin(details);
 			} else {
 				AddActivity.this.finish();
 				Intent intent = new Intent(this, ListSavedActivity.class);
 				startActivity(intent);
 			}
 		}
 
 		return success;
 	}
 
 	private void startFoursquareCheckin(Bundle journey) {
 		Intent intent = new Intent(this, CheckinActivity.class);
 		intent.putExtras(journey);
 		startActivity(intent);
 		AddActivity.this.finish();
 	}
 
 	@SuppressWarnings("unchecked")
 	public void startHeadcodeSelection(View view) {
 		actv_FromSearch = (AutoCompleteTextView) findViewById(R.id.actv_FromSearch);
 		actv_ToSearch = (AutoCompleteTextView) findViewById(R.id.actv_ToSearch);
 		dp_FromDate = (DatePicker) findViewById(R.id.dp_FromDate);
 		tp_FromTime = (TimePicker) findViewById(R.id.tp_FromTime);
 		chk_DetailHeadcode = (CheckBox) findViewById(R.id.chk_DetailHeadcode);
 
 		String from = actv_FromSearch.getText().toString();
 		String to = actv_ToSearch.getText().toString();
 		String month = "";
 		int m = -1;
 
 		if (from.length() > 2) {
 			from = from.substring(0, 3);
 		}
 
 		if (to.length() > 2) {
 			to = to.substring(0, 3);
 		}
 
 		m = dp_FromDate.getMonth();
 		if (Integer.toString(m).length() > 0) {
 			month = Integer.toString(m + 1);
 		}
 
 		if (from.length() < 3) {
 			Toast.makeText(getApplicationContext(),
 					getString(R.string.add_new_headcode_error_from_blank),
 					Toast.LENGTH_LONG).show();
 			mTabHost.setCurrentTab(0);
 		} else {
 			HashMap<String, String> journeyDetails = new HashMap<String, String>();
 			journeyDetails.put("from", from);
 			journeyDetails.put("to", to);
 			journeyDetails.put("hour", "" + tp_FromTime.getCurrentHour());
 			journeyDetails.put("min", "" + tp_FromTime.getCurrentMinute());
 			journeyDetails.put("year", "" + dp_FromDate.getYear());
 			journeyDetails.put("month", month);
 			journeyDetails.put("day", "" + dp_FromDate.getDayOfMonth());
 
 			dialog = ProgressDialog
 					.show(AddActivity.this,
 							getString(R.string.add_new_headcode_depboard_progress_title),
 							getString(R.string.add_new_headcode_depboard_progress_text),
 							true);
 			dialog.setCancelable(true);
 
 			new DownloadJourneysTask().execute(journeyDetails);
 			onHeadcodeCheckboxClicked(view, true);
 		}
 
 	}
 
 	private class DownloadJourneysTask
 			extends
 			AsyncTask<HashMap<String, String>, Void, Collection<Map<String, Object>>> {
 
 		protected Collection<Map<String, Object>> doInBackground(
 				HashMap<String, String>... journeysDetails) {
 			Collection<Map<String, Object>> schedules;
 
 			HashMap<String, String> journeyDetails = journeysDetails[0];
 			JSONObject schedulesJson = fetchTimetable(journeyDetails
 					.get("from").toUpperCase(), journeyDetails.get("year"),
 					journeyDetails.get("month"), journeyDetails.get("day"),
 					journeyDetails.get("hour"), "00", "60");
 
 			schedules = parseSchedules(schedulesJson);
 			System.out.println("Fetched " + schedules.size()
 					+ " schedules from this station.");
 
 			for (Map<String, Object> schedule : schedules) {
 				@SuppressWarnings("unchecked")
 				Map<String, String> origin = (Map<String, String>) schedule
 						.get("origin");
 				@SuppressWarnings("unchecked")
 				Map<String, String> destination = (Map<String, String>) schedule
 						.get("destination");
 				System.out.println("Service " + schedule.get("uid") + " is "
 						+ schedule.get("headcode") + ": " + origin.get("name")
 						+ " to " + destination.get("name") + " at platform "
 						+ schedule.get("platform") + " by "
 						+ schedule.get("tocCode"));
 			}
 
 			return schedules;
 		}
 
 		private JSONObject fetchTimetable(String crsCode, String year,
 				String month, String date, String hour, String minute,
 				String duration) {
 
 			JSONObject json = new JSONObject();
 			String rawJson = Helpers
 					.fetchData("http://api.traintimes.im/locations.json?location="
 							+ crsCode
 							+ "&date="
 							+ Helpers.leftPad(year, 4)
 							+ "-"
 							+ Helpers.leftPad(month, 2)
 							+ "-"
 							+ Helpers.leftPad(date, 2)
 							+ "&startTime="
 							+ hour
 							+ "" + minute + "&period=" + duration);
 			try {
 				json = new JSONObject(rawJson);
 			} catch (JSONException e) {
 				// boo hiss boo
 			}
 
 			return json;
 		}
 
 		private Collection<Map<String, Object>> parseSchedules(
 				JSONObject schedulesJson) {
 			Collection<Map<String, Object>> services = new HashSet<Map<String, Object>>();
 			try {
 				JSONArray servicesJson = schedulesJson.getJSONArray("services");
 				services = parseServices(servicesJson, "dep");
 			} catch (JSONException e) {
 				System.err.println("Unable to parse JSON (schedules): "
 						+ e.getMessage());
 			}
 			return services;
 		}
 
 		private List<Map<String, Object>> parseServices(JSONArray servicesJson,
 				String flagArrDep) {
 			List<Map<String, Object>> services = new ArrayList<Map<String, Object>>();
 			for (int i = 0; i < servicesJson.length(); i++) {
 				try {
 					JSONObject service = servicesJson.getJSONObject(i);
 					Map<String, Object> s = parseService(service);
 					if (s.get("train") == "true") {
 						if ((flagArrDep == "arr" && s.get("arrivalAt") != "null")
 								|| (flagArrDep == "dep" && s.get("departureAt") != "null")) {
 							services.add(s);
 						} else {
 							System.out.println("Schedule " + s.get("uid")
 									+ " (" + s.get("headcode")
 									+ ") is not scheduled to arrive/depart.");
 						}
 					} else {
 						System.out.println("Schedule " + s.get("uid") + " ("
 								+ s.get("headcode") + ") is not a train.");
 					}
 				} catch (JSONException e) {
 					System.err.println("Unable to parse JSON (services): "
 							+ e.getMessage());
 				}
 			}
 			return services;
 		}
 
 		private Map<String, Object> parseService(JSONObject serviceJson) {
 			Map<String, Object> service = new HashMap<String, Object>();
 			try {
 				service.put("uid", serviceJson.getString("uid"));
 				service.put("train", serviceJson.getString("train"));
 				service.put("headcode", serviceJson.getString("trainIdentity"));
 				service.put("tocCode", serviceJson.getString("operatorCode"));
 				service.put("platform", serviceJson.getString("platform"));
 				service.put("arrivalAt", serviceJson.getString("arrival_time"));
 				service.put("departureAt",
 						serviceJson.getString("departure_time"));
 
 				Map<String, String> origin = parseServiceOrigin(serviceJson
 						.getJSONObject("origin"));
 				service.put("origin", origin);
 				Map<String, String> destination = parseServiceDestination(serviceJson
 						.getJSONObject("destination"));
 				service.put("destination", destination);
 				System.out.println("Service " + service.get("uid") + " is "
 						+ service.get("headcode") + ": " + origin.get("name")
 						+ " to " + destination.get("name") + " at platform "
 						+ service.get("platform") + " by "
 						+ service.get("tocCode"));
 			} catch (JSONException e) {
 				System.err.println("Unable to parse JSON (service): "
 						+ e.getMessage());
 			}
 			return service;
 		}
 
 		private Map<String, String> parseServiceOrigin(JSONObject origin) {
 			Map<String, String> location = new HashMap<String, String>();
 			try {
 				location.put("crs", origin.getString("crs"));
 				location.put("name", origin.getString("description"));
 				location.put("time", origin.getString("departure_time"));
 			} catch (JSONException e) {
 				System.err.println("Unable to parse JSON (destination): "
 						+ e.getMessage());
 			}
 			return location;
 		}
 
 		private Map<String, String> parseServiceDestination(
 				JSONObject destination) {
 			Map<String, String> location = new HashMap<String, String>();
 			try {
 				location.put("crs", destination.getString("crs"));
 				location.put("name", destination.getString("description"));
 				location.put("time", destination.getString("arrival_time"));
 			} catch (JSONException e) {
 				System.err.println("Unable to parse JSON (destination): "
 						+ e.getMessage());
 			}
 			return location;
 		}
 
 		@SuppressWarnings("unchecked")
 		protected void onPostExecute(
 				final Collection<Map<String, Object>> schedules) {
 			dialog.dismiss();
 
 			AlertDialog.Builder builder = new AlertDialog.Builder(
 					AddActivity.this);
 
 			builder.setTitle(getString(R.string.add_new_headcode_depboard_results_title));
 
 			final String[] scheduleLabels = new String[schedules.size()];
 			int i = 0;
 			for (Map<String, Object> schedule : schedules) {
 				Map<String, String> origin = (Map<String, String>) schedule
 						.get("origin");
 				Map<String, String> destination = (Map<String, String>) schedule
 						.get("destination");
 
 				scheduleLabels[i] = "" + schedule.get("headcode") + ": "
 						+ schedule.get("departureAt") + " to "
						+ destination.get("name") + " (platform "
						+ schedule.get("platform") + ")";
 				i++;
 			}
 
 			builder.setSingleChoiceItems(scheduleLabels, -1, null);
 			AlertDialog alert = builder.create();
 			alert.show();
 		}
 	}
 
 	private class DownloadJourneyDetailTask extends
 			AsyncTask<String[], Void, ArrayList<ArrayList<String>>> {
 
 		protected ArrayList<ArrayList<String>> doInBackground(
 				String[]... journeyDetails) {
 			ArrayList<ArrayList<String>> formattedStations = new ArrayList<ArrayList<String>>();
 
 			ArrayList<String> result = new ArrayList<String>();
 			String dataError = getString(R.string.add_new_headcode_error_default_schedule);
 			result.add("ERROR");
 			result.add(dataError);
 			formattedStations.add(result);
 
 			String[] journeyDetail = journeyDetails[0];
 			String journeyId = journeyDetail[0];
 			String fromYear = journeyDetail[1];
 			String fromMonth = journeyDetail[2];
 			String fromDay = journeyDetail[3];
 			String fromTime = journeyDetail[4];
 			boolean passedStationInSchedule = false;
 
 			try {
 				URL url = new URL("http://trains.im/schedule/" + journeyId
 						+ "/" + fromYear + "/" + fromMonth + "/" + fromDay);
 				System.out.println("Fetching journey detail from: "
 						+ url.toString());
 
 				StringBuilder builder = new StringBuilder();
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(url.openStream(), "UTF-8"));
 
 				for (String line; (line = reader.readLine()) != null;) {
 					builder.append(line.trim());
 				}
 
 				String tableStart = "<table class=\"table table-striped\">";
 				String tableEnd = "</table>";
 				if (builder.indexOf(tableStart) > 0) {
 					String tablePart = builder.substring(builder
 							.indexOf(tableStart) + tableStart.length());
 					// System.out.println(tablePart);
 					String table = tablePart.substring(0,
 							tablePart.indexOf(tableEnd));
 
 					String bodyStart = "<tbody>";
 					String bodyEnd = "</tbody>";
 					if (table.indexOf(bodyStart) > 0) {
 						String bodyPart = table.substring(table
 								.indexOf(bodyStart) + bodyStart.length());
 						String body = bodyPart.substring(0,
 								bodyPart.indexOf(bodyEnd));
 
 						String rowStart = "<tr";
 						String rowEnd = "</tr>";
 						ArrayList<String> rows = new ArrayList<String>();
 
 						String[] rawRows = body.split(Pattern.quote(rowStart));
 						for (int r = 1; r < rawRows.length; r++) {
 							String row = rawRows[r];
 							rows.add(row);
 							System.out.println("Added row " + r + ": " + row);
 						}
 
 						ArrayList<ArrayList<String>> stations = new ArrayList<ArrayList<String>>();
 
 						for (int r = 0; r < rows.size(); r++) {
 							String row = rows.get(r);
 
 							// Split into array of cellS
 							String cellStart = "<td";
 							String cellEnd = "</";
 
 							ArrayList<String> cells = new ArrayList<String>();
 							String[] rawCells = row.split(Pattern
 									.quote(cellStart));
 							for (int i = 0; i < rawCells.length; i++) {
 								cells.add(rawCells[i]);
 							}
 							cells.remove(0);
 
 							ArrayList<String> station = new ArrayList<String>();
 
 							// get journey ID from headcode a[@href]
 							String link = cells.get(0);
 							System.out.println("Link: " + link);
 
 							int linkPos = link.indexOf("href") + 6;
 							int linkSepPos = linkPos - 1;
 							String sep = "" + link.charAt(linkSepPos);
 
 							int secondSepPos = link.indexOf(sep, linkPos);
 							String linkHref = link.substring(linkPos,
 									secondSepPos - 1);
 
 							System.out.println("Link HREF:" + linkHref);
 							String[] journeyParts = linkHref.split("/");
 							String year = journeyParts[3];
 							String month = journeyParts[4];
 							String day = journeyParts[5];
 
 							// Get contents of all cells and remove
 							// any more HTML tags from inside
 							for (int c = 0; c < cells.size(); c++) {
 								String cellPart = cells.get(c);
 								String cell = cellPart.substring(
 										cellPart.indexOf(">") + 1,
 										cellPart.indexOf(cellEnd));
 								cells.set(c, android.text.Html.fromHtml(cell)
 										.toString());
 							}
 
 							// Pick out elements
 							String stnName = cells.get(0);
 							String stnCode = stnName
 									.substring(stnName.length() - 3);
 							stnName = stnName
 									.substring(0, stnName.length() - 5);
 
 							String platform = cells.get(1);
 							String arrTime = cells.get(2);
 							String depTime = cells.get(3);
 
 							if (platform == " ") {
 								platform = ""
 										+ getText(R.string.add_new_headcode_results_no_platform);
 							}
 
 							System.out.println("Station: " + stnName);
 							System.out.println("Code: " + stnCode);
 							System.out.println("Platform: " + platform);
 							System.out.println("Arrival: " + arrTime);
 							System.out.println("Departure: " + depTime);
 							System.out.println("Year: " + year);
 							System.out.println("Month: " + month);
 							System.out.println("Day: " + day);
 
 							String time = arrTime;
 							if (!time.matches("[0-9]{4}")) {
 								time = "" + depTime;
 							}
 
 							station.add(stnName);
 							station.add(stnCode);
 							station.add(time);
 							station.add(year);
 							station.add(month);
 							station.add(day);
 
 							System.out.println("Station: " + station);
 							boolean laterToday = (Integer.parseInt(fromTime) < Integer
 									.parseInt(time)
 									&& fromYear.equals(year)
 									&& fromMonth.equals(month) && fromDay
 									.equals(day));
 							boolean tommorrow = (Integer.parseInt(year) > Integer
 									.parseInt(fromYear)
 									|| Integer.parseInt(month) > Integer
 											.parseInt(fromMonth) || Integer
 									.parseInt(day) > Integer.parseInt(fromDay));
 							if (laterToday || tommorrow) {
 								passedStationInSchedule = true;
 							}
 
 							if (passedStationInSchedule) {
 								result.clear();
 								result.add("SUCCESS");
 
 								if (formattedStations.size() == 0) {
 									formattedStations.add(result);
 									formattedStations.add(station);
 								} else {
 									formattedStations.set(0, result);
 									formattedStations.add(station);
 								}
 							}
 						}
 					}
 				}
 
 				try {
 					reader.close();
 				} catch (IOException e) {
 					System.err.println(e.getMessage());
 					System.err.println(e.getStackTrace());
 				}
 
 				if (formattedStations.get(0).get(0).equals("ERROR")) {
 					formattedStations.clear();
 					result.add("ERROR");
 					result.add(getString(R.string.add_new_headcode_error_last_station));
 					formattedStations.set(0, result);
 				}
 
 			} catch (UnsupportedEncodingException e) {
 				System.err.println(e.getMessage());
 				System.err.println(e.getStackTrace());
 				result.add("ERROR");
 				result.add(getString(R.string.add_new_headcode_error_invalid));
 				formattedStations.set(0, result);
 			} catch (IOException e) {
 				System.err.println(e.getMessage());
 				System.err.println(e.getStackTrace());
 				result.add("ERROR");
 				result.add(getString(R.string.add_new_headcode_error_io));
 				formattedStations.set(0, result);
 			}
 
 			return formattedStations;
 		}
 
 		protected void onPostExecute(
 				final ArrayList<ArrayList<String>> resultList) {
 			dialog.dismiss();
 			if (resultList.get(0).get(0) == "SUCCESS") {
 				resultList.remove(0);
 				txt_DetailHeadcode = (TextView) findViewById(R.id.txt_DetailHeadcode);
 				actv_ToSearch = (AutoCompleteTextView) findViewById(R.id.actv_ToSearch);
 				dp_ToDate = (DatePicker) findViewById(R.id.dp_ToDate);
 				tp_ToTime = (TimePicker) findViewById(R.id.tp_ToTime);
 
 				String[] presentedResults = new String[resultList.size()];
 				AlertDialog.Builder builder = new AlertDialog.Builder(
 						AddActivity.this);
 				builder.setTitle(getString(R.string.add_new_headcode_schedule_results_title));
 
 				for (int i = 0; i < resultList.size(); i++) {
 					ArrayList<String> result = resultList.get(i);
 					System.out.println("Result #" + i + ": " + result);
 					presentedResults[i] = result.get(2) + ": " + result.get(0);
 				}
 
 				builder.setSingleChoiceItems(presentedResults, -1,
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface d, int i) {
 								ArrayList<String> selection = resultList.get(i);
 								System.out.println("Selected #" + i + ": "
 										+ selection);
 								String destinationCode = selection.get(1);
 								String destination = "" + selection.get(1)
 										+ " " + selection.get(0);
 
 								String[] completions = read_csv("stations.lst");
 								for (String stationName : completions) {
 									String completionCode = Helpers
 											.trimNameFromStation(stationName,
 													getApplicationContext());
 									if (completionCode.equals(destinationCode)) {
 										destination = stationName;
 										break;
 									}
 								}
 
 								actv_ToSearch.setText(destination);
 
 								String time = selection.get(2);
 								if (time.matches("[0-9]{4}")) {
 									int hrs = Integer.parseInt(time.substring(
 											0, 2));
 									int min = Integer.parseInt(time.substring(
 											2, 4));
 
 									tp_ToTime.setCurrentHour(hrs);
 									tp_ToTime.setCurrentMinute(min);
 								}
 
 								int year = Integer.parseInt(selection.get(3));
 								int month = Integer.parseInt(selection.get(4));
 								int day = Integer.parseInt(selection.get(5));
 								dp_ToDate.updateDate(year, month - 1, day);
 
 								updateSummary();
 								d.dismiss();
 							}
 						});
 				AlertDialog alert = builder.create();
 				alert.show();
 			} else { // resultList.get(0).get(0) == "ERROR"
 				Toast.makeText(getApplicationContext(),
 						resultList.get(0).get(1), Toast.LENGTH_LONG).show();
 			}
 		}
 	}
 }
