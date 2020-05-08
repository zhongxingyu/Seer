 package com.seawolfsanctuary.keepingtracks;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.regex.Pattern;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.app.TabActivity;
 import android.content.Context;
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
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 import com.seawolfsanctuary.keepingtracks.database.Journey;
 import com.seawolfsanctuary.keepingtracks.foursquare.CheckinActivity;
 
 public class AddActivity extends TabActivity {
 
 	Bundle template;
 
 	TextView txt_Title;
 
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
 
 	TextView txt_Summary;
 	CheckBox chk_Checkin;
 
 	SharedPreferences settings;
 
 	private ProgressDialog dialog;
 
 	boolean isLocationEnabledNetwork = false;
 	boolean isLocationEnabledGPS = false;
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.context_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.list:
 			Intent intent = new Intent(this, ListSavedActivity.class);
 			AddActivity.this.finish();
 			startActivity(intent);
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
 
 		txt_Title = (TextView) findViewById(R.id.txt_Title);
 
 		mTabHost = getTabHost();
 		mTabHost.addTab(mTabHost.newTabSpec("tc_From").setIndicator("From")
 				.setContent(R.id.tc_From));
 		mTabHost.addTab(mTabHost.newTabSpec("tc_Detail").setIndicator("Detail")
 				.setContent(R.id.tc_Detail));
 		mTabHost.addTab(mTabHost.newTabSpec("tc_To").setIndicator("To")
 				.setContent(R.id.tc_To));
 		mTabHost.addTab(mTabHost.newTabSpec("tc_Summary")
 				.setIndicator("Summary").setContent(R.id.tc_Summary));
 
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
 					updateText();
 					txt_Summary = (TextView) findViewById(R.id.txt_Summary);
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
 				updateText();
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
 				txt_Title.setText(R.string.edit_saved);
 			}
 			if (template.containsKey("copying")) {
 				txt_Title.setText(R.string.copy_saved);
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
 
 	private void updateText() {
 		actv_FromSearch = (AutoCompleteTextView) findViewById(R.id.actv_FromSearch);
 		dp_FromDate = (DatePicker) findViewById(R.id.dp_FromDate);
 		tp_FromTime = (TimePicker) findViewById(R.id.tp_FromTime);
 
 		txt_DetailClass = (TextView) findViewById(R.id.txt_DetailClass);
 		txt_DetailHeadcode = (TextView) findViewById(R.id.txt_DetailHeadcode);
 
 		actv_ToSearch = (AutoCompleteTextView) findViewById(R.id.actv_ToSearch);
 		dp_ToDate = (DatePicker) findViewById(R.id.dp_ToDate);
 		tp_ToTime = (TimePicker) findViewById(R.id.tp_ToTime);
 
 		txt_Summary = (TextView) findViewById(R.id.txt_Summary);
 		String summaryText = constructSummary();
 		txt_Summary.setText(summaryText);
 	}
 
 	private String constructSummary() {
 		String summary = "";
 
 		summary += getString(R.string.add_summary_from,
 				Helpers.trimCodeFromStation(actv_FromSearch.getText()
 						.toString(), getApplicationContext()));
 
 		summary += "\n";
 
 		summary += getString(R.string.add_summary_on,
 				Helpers.leftPad("" + dp_FromDate.getYear(), 4),
 				Helpers.leftPad("" + (dp_FromDate.getMonth() + 1), 2),
 				Helpers.leftPad("" + dp_FromDate.getDayOfMonth(), 2));
 
 		summary += "\n";
 
 		summary += getString(R.string.add_summary_at,
 				Helpers.leftPad("" + tp_FromTime.getCurrentHour(), 2),
 				Helpers.leftPad("" + tp_FromTime.getCurrentMinute(), 2));
 
 		summary += "\n";
 		summary += "\n";
 
 		summary += getString(R.string.add_summary_to,
 				Helpers.trimCodeFromStation(actv_ToSearch.getText().toString(),
 						getApplicationContext()));
 
 		summary += "\n";
 
 		summary += getString(R.string.add_summary_on,
 				Helpers.leftPad("" + dp_ToDate.getYear(), 4),
 				Helpers.leftPad("" + (dp_ToDate.getMonth() + 1), 2),
 				Helpers.leftPad("" + dp_ToDate.getDayOfMonth(), 2));
 
 		summary += "\n";
 
 		summary += getString(R.string.add_summary_at,
 				Helpers.leftPad("" + tp_ToTime.getCurrentHour(), 2),
 				Helpers.leftPad("" + tp_ToTime.getCurrentMinute(), 2));
 
 		if (settings.getBoolean("AdvancedJourneys", false) == true) {
 			summary += "\n";
 			summary += "\n";
 
 			summary += getString(R.string.add_summary_with, txt_DetailClass
 					.getText().toString());
 
 			summary += "\n";
 
 			summary += getString(R.string.add_summary_as, txt_DetailHeadcode
 					.getText().toString());
 		}
 
 		return summary;
 	}
 
 	public void onClassCheckboxClicked(View view) {
 		CheckBox chk_DetailClass = (CheckBox) findViewById(R.id.chk_DetailClass);
 		TextView txt_DetailClass = (TextView) findViewById(R.id.txt_DetailClass);
 		txt_DetailClass.setEnabled(((CheckBox) chk_DetailClass).isChecked());
 
 		Helpers.hideKeyboard(view);
 	}
 
 	public void onHeadcodeCheckboxClicked(View view) {
 		CheckBox chk_DetailHeadcode = (CheckBox) findViewById(R.id.chk_DetailHeadcode);
 		TextView txt_DetailHeadcode = (TextView) findViewById(R.id.txt_DetailHeadcode);
 		txt_DetailHeadcode.setEnabled(((CheckBox) chk_DetailHeadcode)
 				.isChecked());
 
 		Helpers.hideKeyboard(view);
 	}
 
 	public boolean writeEntry(View view) {
 		boolean success = false;
 		actv_FromSearch = (AutoCompleteTextView) findViewById(R.id.actv_FromSearch);
 		dp_FromDate = (DatePicker) findViewById(R.id.dp_FromDate);
 		tp_FromTime = (TimePicker) findViewById(R.id.tp_FromTime);
 
 		txt_DetailClass = (TextView) findViewById(R.id.txt_DetailClass);
 		txt_DetailHeadcode = (TextView) findViewById(R.id.txt_DetailHeadcode);
 		chk_DetailUseForStats = (CheckBox) findViewById(R.id.chk_DetailUseForStats);
 
 		actv_ToSearch = (AutoCompleteTextView) findViewById(R.id.actv_ToSearch);
 		dp_ToDate = (DatePicker) findViewById(R.id.dp_ToDate);
 		tp_ToTime = (TimePicker) findViewById(R.id.tp_ToTime);
 
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
 					txt_DetailClass.getText().toString(), txt_DetailHeadcode
 							.getText().toString(), chk_DetailUseForStats
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
 					txt_DetailClass.getText().toString(), txt_DetailHeadcode
 							.getText().toString(), chk_DetailUseForStats
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
 				details.putString("detail_class", txt_DetailClass.getText()
 						.toString());
 				details.putString("detail_headcode", txt_DetailHeadcode
 						.getText().toString());
 
 				AddActivity.this.finish();
 				Intent intent = new Intent(this, ListSavedActivity.class);
 				startActivity(intent);
 
 				foursquareCheckin(details);
 			} else {
 				AddActivity.this.finish();
 				Intent intent = new Intent(this, ListSavedActivity.class);
 				startActivity(intent);
 			}
 		}
 
 		return success;
 	}
 
 	public void startClassInfoActivity(View view) {
 		template = Helpers.saveCurrentJourney(template, AddActivity.this);
 		if (template == null) {
 			template = new Bundle();
 		}
 
 		Intent intent = new Intent(this, ClassInfoActivity.class);
 		intent.putExtras(template);
 		startActivity(intent);
 		// TODO: can we finish this if a class is selected from new activity,
 		// but keep it if 'Back' is pushed instead?
 		AddActivity.this.finish();
 	}
 
 	private void foursquareCheckin(Bundle journey) {
 		Intent intent = new Intent(this, CheckinActivity.class);
 		intent.putExtras(journey);
 		startActivity(intent);
 		AddActivity.this.finish();
 	}
 
 	public void startHeadcodeSelectionActivity(View view) {
 		actv_FromSearch = (AutoCompleteTextView) findViewById(R.id.actv_FromSearch);
 		actv_ToSearch = (AutoCompleteTextView) findViewById(R.id.actv_ToSearch);
 		dp_FromDate = (DatePicker) findViewById(R.id.dp_FromDate);
 		tp_FromTime = (TimePicker) findViewById(R.id.tp_FromTime);
 
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
 			String[] journeyDetails = { from, to,
 					"" + tp_FromTime.getCurrentHour(),
 					"" + tp_FromTime.getCurrentMinute(),
 					"" + dp_FromDate.getYear(), month,
 					"" + dp_FromDate.getDayOfMonth() };
 
 			dialog = ProgressDialog
 					.show(AddActivity.this,
 							getString(R.string.add_new_headcode_depboard_progress_title),
 							getString(R.string.add_new_headcode_depboard_progress_text),
 							true);
 			dialog.setCancelable(true);
 
 			new DownloadJourneysTask().execute(journeyDetails);
 		}
 
 	}
 
 	private class DownloadJourneysTask extends
 			AsyncTask<String[], Void, ArrayList<ArrayList<String>>> {
 
 		protected ArrayList<ArrayList<String>> doInBackground(
 				String[]... journeysDetails) {
 			ArrayList<ArrayList<String>> formattedJourneys = new ArrayList<ArrayList<String>>();
 
 			ArrayList<String> result = new ArrayList<String>();
 			String dataError = getString(R.string.add_new_headcode_error_default_depboard);
 
 			formattedJourneys.add(result);
 
 			String[] journeyDetails = journeysDetails[0];
 			String fromStation = journeyDetails[0];
 			String toStation = journeyDetails[1];
 			String hour = journeyDetails[2];
 			String minute = journeyDetails[3];
 			String year = journeyDetails[4];
 			String month = journeyDetails[5];
 			String day = journeyDetails[6];
 
 			Integer pageDurationHours = 2;
 
 			String section = Integer
 					.toString((Integer.parseInt(hour) / pageDurationHours));
 			if (section.indexOf(".") != -1) {
 				section = section.substring(0, section.indexOf("."));
 			}
 
 			try {
 				URL url = new URL("http://trains.im/locationdepartures/"
 						+ fromStation + "/" + year + "/" + month + "/" + day
 						+ "/" + section);
 				System.out.println("Fetching journeys from: " + url.toString());
 
 				StringBuilder builder = new StringBuilder();
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(url.openStream(), "UTF-8"));
 
 				for (String line; (line = reader.readLine()) != null;) {
 					builder.append(line.trim());
 				}
 
 				String tableStart = "<table class=\"table table-striped\">";
 				String tableEnd = "</table>";
 				if (builder.indexOf(tableStart) < 0) {
 					result.add("ERROR");
 					result.add(dataError);
 					formattedJourneys.set(0, result);
 				} else {
 					String tablePart = builder.substring(builder
 							.indexOf(tableStart) + tableStart.length());
 					// System.out.println(tablePart);
 					String table = tablePart.substring(0,
 							tablePart.indexOf(tableEnd));
 
 					String bodyStart = "<tbody>";
 					String bodyEnd = "</tbody>";
 					if (table.indexOf(bodyStart) < 0) {
 						result.add("ERROR");
 						result.add(dataError);
 						formattedJourneys.set(0, result);
 					} else {
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
 						}
 
 						ArrayList<ArrayList<String>> journeys = new ArrayList<ArrayList<String>>();
 
 						for (int r = 0; r < rows.size(); r++) {
 							String row = rows.get(r);
 
 							// Split into array of cells
 							String cellStart = "<td";
 							String cellEnd = "</";
 
 							ArrayList<String> cells = new ArrayList<String>();
 							String[] rawCells = row.split(Pattern
 									.quote(cellStart));
 							for (int i = 0; i < rawCells.length; i++) {
 								cells.add(rawCells[i]);
 							}
 							cells.remove(0);
 
 							ArrayList<String> journey = new ArrayList<String>();
 
 							// get journey ID from headcode a[@href]
 							String link = cells.get(0);
 							System.out.println("Link: " + link);
 							int linkPos = link.indexOf("href") + 6;
 							int linkSepPos = linkPos - 1;
 							String sep = "" + link.charAt(linkSepPos);
 
 							int secondSepPos = link.indexOf(sep, linkPos);
 							String linkHref = link.substring(linkPos,
 									secondSepPos - 1);
 
 							int idStart = linkHref.indexOf("/") + 10;
 							String journeyPart = linkHref.substring(idStart);
 							int idLength = journeyPart.indexOf("/");
 							String journeyId = journeyPart.substring(0,
 									idLength);
 
 							// Get cell contents and remove any more HTML tags
 							// from inside
 							for (int c = 0; c < cells.size(); c++) {
 								String cellPart = cells.get(c);
 								String cell = cellPart.substring(
 										cellPart.indexOf(">") + 1,
 										cellPart.indexOf(cellEnd));
 								cells.set(c, android.text.Html.fromHtml(cell)
 										.toString());
 							}
 
 							// System.out.println("Headcode: " + cells.get(0));
 							// System.out.println("Departure: " + cells.get(1));
 							// System.out.println("Destination: " +
 							// cells.get(2));
 							// System.out.println("Platform: " + cells.get(3));
 							// System.out.println("Operator: " + cells.get(4));
 
 							for (int i = 0; i < cells.size(); i++) {
 								journey.add(cells.get(i));
 							}
 
 							journey.add(journeyId);
 							journey.add(year);
 							journey.add(month);
 							journey.add(day);
 
 							result.clear();
 							result.add("SUCCESS");
 							result.add("" + journeys.size());
 							formattedJourneys.set(0, result);
 
 							formattedJourneys.add(journey);
 						}
 					}
 				}
 
 				try {
 					reader.close();
 				} catch (IOException e) {
 					System.err.println(e.getMessage());
 					System.err.println(e.getStackTrace());
 				}
 
 			} catch (UnsupportedEncodingException e) {
 				System.err.println(e.getMessage());
 				System.err.println(e.getStackTrace());
 				result.add("ERROR");
 				result.add(getString(R.string.add_new_headcode_error_invalid));
 				formattedJourneys.set(0, result);
 			} catch (IOException e) {
 				result.add("ERROR");
 				result.add(getString(R.string.add_new_headcode_error_io));
 				formattedJourneys.set(0, result);
 			}
 
 			return formattedJourneys;
 		}
 
 		protected void onPostExecute(
 				final ArrayList<ArrayList<String>> resultList) {
 			dialog.dismiss();
 
 			if (resultList.get(0).get(0) == "SUCCESS") {
 				resultList.remove(0);
 				txt_DetailHeadcode = (TextView) findViewById(R.id.txt_DetailHeadcode);
 				tp_FromTime = (TimePicker) findViewById(R.id.tp_FromTime);
 
 				String[] presentedResults = new String[resultList.size()];
 
 				for (int i = 0; i < resultList.size(); i++) {
 					ArrayList<String> result = resultList.get(i);
 					System.out.println("Result #" + i + ": " + result);
 
 					String platformInfo = result.get(3);
 					if (platformInfo.length() > 0) {
 						platformInfo = " ("
 								+ getString(R.string.add_new_headcode_results_platform)
 								+ " " + platformInfo + ")";
 					}
 
 					presentedResults[i] = result.get(0) + ": " + result.get(1)
 							+ " "
 							+ getString(R.string.add_new_headcode_results_to)
 							+ " " + result.get(2) + platformInfo;
 				}
 
 				AlertDialog.Builder builder = new AlertDialog.Builder(
 						AddActivity.this);
 				builder.setTitle(getString(R.string.add_new_headcode_depboard_results_title));
 				builder.setSingleChoiceItems(presentedResults, -1,
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface d, int i) {
 								ArrayList<String> selection = resultList.get(i);
 								SharedPreferences settings = getSharedPreferences(
 										UserPrefsActivity.APP_PREFS,
 										MODE_PRIVATE);
 
 								System.out.println("Using: " + selection.get(1));
 								int hours = Integer.parseInt(selection.get(1)
 										.substring(0, 2));
 								int minutes = Integer.parseInt(selection.get(1)
 										.substring(2, 4));
 
 								if (settings.getBoolean("CompleteFromStation",
 										true)) {
 									txt_DetailHeadcode.setText(selection.get(0));
 									tp_FromTime.setCurrentHour(hours);
 									tp_FromTime.setCurrentMinute(minutes);
 								}
 								updateText();
 								d.dismiss();
 
 								if (settings.getBoolean("CompleteToStation",
 										true)) {
 									System.out
 											.println("Starting DownloadJourneyDetailTask()");
 									String[] journeyDetails = new String[] {
 											selection.get(5), selection.get(6),
 											selection.get(7), selection.get(8), };
 									dialog = ProgressDialog
 											.show(AddActivity.this,
 													getString(R.string.add_new_headcode_schedule_progress_title),
 													getString(R.string.add_new_headcode_schedule_progress_text),
 													true);
 									dialog.setCancelable(true);
 
 									new DownloadJourneyDetailTask()
 											.execute(journeyDetails);
 								}
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
 
 	private class DownloadJourneyDetailTask extends
 			AsyncTask<String[], Void, ArrayList<ArrayList<String>>> {
 
 		protected ArrayList<ArrayList<String>> doInBackground(
 				String[]... journeyDetails) {
 			ArrayList<ArrayList<String>> formattedStations = new ArrayList<ArrayList<String>>();
 
 			ArrayList<String> result = new ArrayList<String>();
 			String dataError = getString(R.string.add_new_headcode_error_default_schedule);
 
 			String[] journeyDetail = journeyDetails[0];
 			String journeyID = journeyDetail[0];
 			String year = journeyDetail[1];
 			String month = journeyDetail[2];
 			String day = journeyDetail[3];
 
 			try {
 				URL url = new URL("http://trains.im/schedule/" + journeyID
 						+ "/" + year + "/" + month + "/" + day);
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
 				if (builder.indexOf(tableStart) < 0) {
 					result.add("ERROR");
 					result.add(dataError);
 					formattedStations.set(0, result);
 				} else {
 					String tablePart = builder.substring(builder
 							.indexOf(tableStart) + tableStart.length());
 					// System.out.println(tablePart);
 					String table = tablePart.substring(0,
 							tablePart.indexOf(tableEnd));
 
 					String bodyStart = "<tbody>";
 					String bodyEnd = "</tbody>";
 					if (table.indexOf(bodyStart) < 0) {
 						result.add("ERROR");
 						result.add(dataError);
 						formattedStations.set(0, result);
 					} else {
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
 
 							// Split into array of cells
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
 
 							// Get cell contents and remove any more HTML tags
 							// from inside
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
 							if (platform == " ") {
 								platform = ""
 										+ getText(R.string.add_new_headcode_results_no_platform);
 							}
 							// System.out.println("Station: " + stnName);
 							// System.out.println("Code: " + stnCode);
 							// System.out.println("Platform: " + cells.get(1));
 							// System.out.println("Arrival: " + cells.get(2));
 							// System.out.println("Departure: " + cells.get(3));
 
 							String line = stnName + "(" + stnCode + ")";
 							String time = cells.get(2);
 							if (!time.matches("[0-9]{4}")) {
 								time = "" + cells.get(3);
 							}
 
 							line += ": " + time;
 							station.add(stnName);
 							station.add(stnCode);
 							station.add(time);
 
 							result.clear();
 							result.add("SUCCESS");
 							result.add("" + stations.size());
 
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
 
 				try {
 					reader.close();
 				} catch (IOException e) {
 					System.err.println(e.getMessage());
 					System.err.println(e.getStackTrace());
 				}
 
 			} catch (UnsupportedEncodingException e) {
 				System.err.println(e.getMessage());
 				System.err.println(e.getStackTrace());
 				result.add("ERROR");
 				result.add(getString(R.string.add_new_headcode_error_invalid));
 				formattedStations.set(0, result);
 			} catch (IOException e) {
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
 								Context c = getApplicationContext();
 
 								ArrayList<String> selection = resultList.get(i);
								String destinationCode = selection.get(1);
 								String destination = "" + selection.get(1)
 										+ " " + selection.get(0);
 
 								String[] completions = read_csv("stations.lst");
 								for (String stationName : completions) {
									String completionCode = Helpers
											.trimNameFromStation(stationName, c);
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
 
 								updateText();
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
