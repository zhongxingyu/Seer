 package com.seawolfsanctuary.tmt;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ExpandableListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.ExpandableListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.seawolfsanctuary.tmt.database.Journey;
 
 public class ListSavedActivity extends ExpandableListActivity {
 
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
 		case R.id.add_new:
 			Intent intent = new Intent(this, AddActivity.class);
 			ListSavedActivity.this.finish();
 			startActivity(intent);
 			return true;
 		case R.id.import_csv:
 			final Context context = this;
 			new AlertDialog.Builder(context)
 					.setTitle("Import Journeys")
 					.setMessage(
 							"Ensure a quoted CSV import file exists at '"
 									+ Helpers.exportDirectoryPath
 									+ "/routes.csv' before proceeding.")
 					.setPositiveButton("OK", new OnClickListener() {
 						public void onClick(DialogInterface arg0, int arg1) {
 							ProgressDialog progressDialog = new ProgressDialog(
 									context);
 							progressDialog
 									.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 							progressDialog.setTitle("Importing...");
 							progressDialog
 									.setMessage("Importing journeys from CSV file at '"
 											+ Helpers.exportDirectoryPath
 											+ "/routes.csv' ...");
 							progressDialog.setCancelable(false);
 							new ImportTask(progressDialog)
 									.execute(Helpers.exportDirectoryPath
 											+ "/routes.csv");
 						}
 					}).setNegativeButton("Cancel", new OnClickListener() {
 						public void onClick(DialogInterface arg0, int arg1) {
 							// ignore
 						}
 					}).show();
 			return true;
 		case R.id.export_csv:
 			ProgressDialog progressDialog = new ProgressDialog(this);
 			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 			progressDialog.setTitle("Exporting...");
 			progressDialog.setMessage("Exporting routes to CSV file at '"
 					+ Helpers.exportDirectoryPath + "/routes.csv' ...");
 			progressDialog.setCancelable(false);
 			new ExportTask(progressDialog).execute();
 			return true;
 		default:
 			return true;
 		}
 	}
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.list_saved_activity);
 		setListAdapter(new ListSavedAdapter());
 		registerForContextMenu(getExpandableListView());
 
 		ExpandableListView lv = getExpandableListView();
 		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
 			@Override
 			public boolean onItemLongClick(AdapterView<?> parentView,
 					View childView, final int positionInParentList,
 					final long positionWithinChildList) {
 
 				boolean isParentSelected = (positionWithinChildList >= 0);
 				if (isParentSelected) {
 					// by some fluke we have a parent
 					new AlertDialog.Builder(parentView.getContext())
 							.setTitle(R.string.list_saved_question_title)
 							.setMessage(R.string.list_saved_question_text)
 							.setPositiveButton("Edit", new OnClickListener() {
 								public void onClick(DialogInterface arg0,
 										int arg1) {
 									editEntry(ExpandableListView
 											.getPackedPositionGroup(positionWithinChildList));
 									// save will create ListSavedActivity
 								}
 							})
 							.setNeutralButton("Copy", new OnClickListener() {
 								public void onClick(DialogInterface arg0,
 										int arg1) {
 									copyEntry(ExpandableListView
 											.getPackedPositionGroup(positionWithinChildList));
 									// save will create ListSavedActivity
 								}
 							})
 							.setNegativeButton("Delete", new OnClickListener() {
 								public void onClick(DialogInterface arg0,
 										int arg1) {
 									deleteEntry(ExpandableListView
 											.getPackedPositionGroup(positionWithinChildList));
 
 									Intent intent = new Intent(
 											getApplicationContext(),
 											ListSavedActivity.class);
 									ListSavedActivity.this.finish();
 									startActivity(intent);
 								}
 							}).show();
 
 					return true;
 				}
 				return false;
 			}
 		});
 
 		checkForLegacy(this.getExpandableListView());
 	}
 
 	private class ListSavedAdapter extends BaseExpandableListAdapter {
 
 		ArrayList<String[]> data = loadSavedEntries(true);
 		ArrayList<String> names = new ArrayList<String>(getNames(data));
 
 		private String[] presentedNames = Helpers
 				.arrayListToArray(getNames(data));
 		private String[][] presentedData = Helpers
 				.multiArrayListToArray(getData(data));
 
 		private ArrayList<String> getNames(ArrayList<String[]> data) {
 			ArrayList<String> names = new ArrayList<String>();
 			for (int i = 0; i < data.size(); i++) {
 				String[] entry = data.get(i);
 				names.add(Helpers.leftPad(entry[1], 2) + "/"
 						+ Helpers.leftPad(entry[2], 2) + "/"
 						+ Helpers.leftPad(entry[3], 4) + ":\n"
 						+ Helpers.trimCodeFromStation(entry[0], getBaseContext()) + "\n"
 						+ Helpers.trimCodeFromStation(entry[6], getBaseContext()));
 			}
 			return names;
 		}
 
 		private ArrayList<ArrayList<String>> getData(ArrayList<String[]> entries) {
 			ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
 
 			for (int i = 0; i < entries.size(); i++) {
 				String[] entry = entries.get(i);
 				ArrayList<String> split = new ArrayList<String>();
 
 				split.add("From: " + Helpers.nameAndCodeFromStation(entry[0], getBaseContext())
 						+ "\n" + Helpers.leftPad(entry[1], 2) + "/"
 						+ Helpers.leftPad(entry[2], 2) + "/"
 						+ Helpers.leftPad(entry[3], 2) + " - "
 						+ Helpers.leftPad(entry[4], 2) + ":"
 						+ Helpers.leftPad(entry[5], 2));
 
 				split.add("To: " + Helpers.nameAndCodeFromStation(entry[6], getBaseContext())
 						+ "\n" + Helpers.leftPad(entry[7], 2) + "/"
 						+ Helpers.leftPad(entry[8], 2) + "/"
 						+ Helpers.leftPad(entry[9], 2) + " - "
 						+ Helpers.leftPad(entry[10], 2) + ":"
 						+ Helpers.leftPad(entry[11], 2));
 
 				if (entry[12].length() > 0) {
 					split.add("With: " + entry[12]);
 				}
 
 				if (entry[13].length() > 0) {
 					split.add("Journey: " + entry[13]);
 				}
 
 				data.add(split);
 			}
 
 			return data;
 		}
 
 		public Object getChild(int groupPosition, int childPosition) {
 			return presentedData[groupPosition][childPosition];
 		}
 
 		public long getChildId(int groupPosition, int childPosition) {
 			return childPosition;
 		}
 
 		public int getChildrenCount(int groupPosition) {
 			return presentedData[groupPosition].length;
 		}
 
 		public TextView getGenericView() {
 			// Layout parameters for the ExpandableListView
 			AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
 					ViewGroup.LayoutParams.FILL_PARENT, 64);
 
 			TextView textView = new TextView(ListSavedActivity.this);
 			textView.setLayoutParams(lp);
 			// Centre the text vertically
 			textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
 			// Set the text starting position
 			textView.setPadding(36, 0, 0, 0);
 			return textView;
 		}
 
 		public View getChildView(int groupPosition, int childPosition,
 				boolean isLastChild, View convertView, ViewGroup parent) {
 			TextView textView = getGenericView();
 			textView.setText(getChild(groupPosition, childPosition).toString());
 			return textView;
 		}
 
 		public Object getGroup(int groupPosition) {
 			return presentedNames[groupPosition];
 		}
 
 		public int getGroupCount() {
 			return presentedNames.length;
 		}
 
 		public long getGroupId(int groupPosition) {
 			return groupPosition;
 		}
 
 		public View getGroupView(int groupPosition, boolean isExpanded,
 				View convertView, ViewGroup parent) {
 			TextView textView = getGenericView();
 			textView.setText(getGroup(groupPosition).toString());
 			return textView;
 		}
 
 		public boolean isChildSelectable(int groupPosition, int childPosition) {
 			return true;
 		}
 
 		public boolean hasStableIds() {
 			return true;
 		}
 
 	}
 
 	public ArrayList<String[]> loadSavedEntries(boolean showToast) {
 
 		ArrayList<String[]> allJourneys = new ArrayList<String[]>();
 
 		Journey db_journeys = new Journey(this);
 		db_journeys.open();
 		Cursor c = db_journeys.getAllJourneysReverse();
 		if (c.moveToFirst()) {
 			do {
 				String[] entry = new String[14];
				// System.out.println("Reading row #" + c.getInt(0) + "...");
 				entry[0] = c.getString(1);
 				entry[1] = "" + c.getInt(2);
 				entry[2] = "" + c.getInt(3);
 				entry[3] = "" + c.getInt(4);
 				entry[4] = "" + c.getInt(5);
 				entry[5] = "" + c.getInt(6);
 				entry[6] = c.getString(7);
 				entry[7] = "" + c.getInt(8);
 				entry[8] = "" + c.getInt(9);
 				entry[9] = "" + c.getInt(10);
 				entry[10] = "" + c.getInt(11);
 				entry[11] = "" + c.getInt(12);
 				entry[12] = c.getString(13);
 				entry[13] = c.getString(14);
 				allJourneys.add(entry);
 			} while (c.moveToNext());
 		}
 		db_journeys.close();
 
 		if (showToast) {
 			String msg = "Loaded " + allJourneys.size() + " entr"
 					+ (allJourneys.size() == 1 ? "y" : "ies")
 					+ " from database.";
 			System.out.println(msg);
 			Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
 		}
 		return allJourneys;
 	}
 
 	public boolean editEntry(int position) {
 		boolean success = false;
 		Bundle entry = fetchJourneyToBundle(this, position, true);
 		entry.putBoolean("editing", true);
 
 		try {
 			System.out.println("Editing entry: " + entry.getInt("id"));
 			Intent intent = new Intent(this, AddActivity.class);
 			ListSavedActivity.this.finish();
 			intent.putExtras(entry);
 			startActivity(intent);
 
 		} catch (Exception e) {
 			Toast.makeText(getBaseContext(), "Error: " + e.getMessage(),
 					Toast.LENGTH_LONG).show();
 		}
 
 		return success;
 	}
 
 	public boolean copyEntry(int position) {
 		boolean success = false;
 		Bundle entry = fetchJourneyToBundle(this, position, false);
 		entry.putBoolean("copying", true);
 
 		try {
 			System.out.println("Copying entry from position " + position);
 			Intent intent = new Intent(this, AddActivity.class);
 			ListSavedActivity.this.finish();
 			intent.putExtras(entry);
 			startActivity(intent);
 
 		} catch (Exception e) {
 			Toast.makeText(getBaseContext(), "Error: " + e.getMessage(),
 					Toast.LENGTH_LONG).show();
 		}
 
 		return success;
 	}
 
 	private Bundle fetchJourneyToBundle(Activity activity, int position,
 			boolean includeId) {
 		Bundle entry = new Bundle();
 		int id = -1;
 
 		Journey db_journeys = new Journey(activity);
 		db_journeys.open();
 
 		// Fetch all journeys so we can find out the id to edit
 		Cursor c = db_journeys.getAllJourneysReverse();
 		if (c.moveToFirst()) {
 			c.moveToPosition(position);
 			id = c.getInt(0);
 			System.out.println("Fetching row #" + id + "...");
 			Cursor journey = db_journeys.getJourney(id);
 
 			if (includeId) {
 				entry.putInt("id", journey.getInt(0));
 			}
 
 			entry.putString("from_stn", journey.getString(1));
 			entry.putInt("from_date_day", journey.getInt(2));
 			entry.putInt("from_date_month", journey.getInt(3));
 			entry.putInt("from_date_year", journey.getInt(4));
 			entry.putInt("from_time_hour", journey.getInt(5));
 			entry.putInt("from_time_minute", journey.getInt(6));
 
 			entry.putString("to_stn", journey.getString(7));
 			entry.putInt("to_date_day", journey.getInt(8));
 			entry.putInt("to_date_month", journey.getInt(9));
 			entry.putInt("to_date_year", journey.getInt(10));
 			entry.putInt("to_time_hour", journey.getInt(11));
 			entry.putInt("to_time_minute", journey.getInt(12));
 
 			if (journey.getString(13).length() < 1) {
 				entry.putBoolean("detail_class_checked", false);
 			}
 			if (journey.getString(14).length() < 1) {
 				entry.putBoolean("detail_headcode_checked", false);
 			}
 
 			entry.putString("detail_class", journey.getString(13));
 			entry.putString("detail_headcode", journey.getString(14));
 		}
 		db_journeys.close();
 
 		return entry;
 	}
 
 	public boolean deleteEntry(int position) {
 		boolean success = false;
 		int id = -1;
 
 		try {
 			Journey db_journeys = new Journey(this);
 			db_journeys.open();
 
 			// Fetch all journeys so we can find out the id to delete
 			Cursor c = db_journeys.getAllJourneysReverse();
 			if (c.moveToFirst()) {
 				c.moveToPosition(position);
 				id = c.getInt(0);
 				System.out.println("Deleting row #" + id + "...");
 				success = db_journeys.deleteJourney(id);
 			}
 			db_journeys.close();
 			success = true;
 
 			Toast.makeText(getBaseContext(), "Entry deleted.",
 					Toast.LENGTH_SHORT).show();
 		} catch (Exception e) {
 			Toast.makeText(getBaseContext(), "Error: " + e.getMessage(),
 					Toast.LENGTH_LONG).show();
 		}
 
 		return success;
 	}
 
 	private void checkForLegacy(final View view) {
 		// do we have a CSV file on the SD card?
 		if (new File(Helpers.dataDirectoryPath + "/routes.csv").exists()) {
 			new AlertDialog.Builder(view.getContext())
 					.setTitle("Old Data Found")
 					.setMessage(
 							"The SD card has a file containing previous journeys."
 									+ " "
 									+ "Would you like to import it into the database?")
 					.setPositiveButton("Yes", new OnClickListener() {
 						public void onClick(DialogInterface arg0, int arg1) {
 							ProgressDialog progressDialog = new ProgressDialog(
 									view.getContext());
 							progressDialog
 									.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 							progressDialog.setTitle("Importing...");
 							progressDialog
 									.setMessage("Importing routes from CSV file...");
 							progressDialog.setCancelable(false);
 							new ImportTask(progressDialog)
 									.execute(Helpers.dataDirectoryPath
 											+ "/routes.csv");
 						}
 					}).setNegativeButton("No", new OnClickListener() {
 						public void onClick(DialogInterface arg0, int arg1) {
 							// ignore
 						}
 					}).show();
 		}
 	}
 
 	private class ImportTask extends
 			AsyncTask<String, Void, ArrayList<Boolean>> {
 		private ProgressDialog progressDialog;
 
 		public ImportTask(ProgressDialog dialogFromActivity) {
 			progressDialog = dialogFromActivity;
 		}
 
 		public void onPreExecute() {
 			progressDialog.show();
 		}
 
 		protected ArrayList<Boolean> doInBackground(String... args) {
 			return new Journey(progressDialog.getContext())
 					.importFromCSV(args[0]);
 		}
 
 		protected void onPostExecute(ArrayList<Boolean> statuses) {
 			progressDialog.dismiss();
 
 			int successes = 0;
 			int failures = 0;
 			for (Boolean status : statuses) {
 				if (status == true) {
 					successes += 1;
 				} else {
 					failures += 1;
 				}
 			}
 
 			Intent intent = ListSavedActivity.this.getIntent();
 			ListSavedActivity.this.finish();
 			startActivity(intent);
 
 			Toast.makeText(
 					getBaseContext(),
 					"Imported " + successes + " routes, " + failures
 							+ " failed.", Toast.LENGTH_LONG).show();
 		}
 	}
 
 	private class ExportTask extends AsyncTask<Void, Void, Boolean> {
 		private ProgressDialog progressDialog;
 
 		public ExportTask(ProgressDialog dialogFromActivity) {
 			progressDialog = dialogFromActivity;
 		}
 
 		public void onPreExecute() {
 			progressDialog.show();
 		}
 
 		protected Boolean doInBackground(Void... args) {
 			return new Journey(progressDialog.getContext()).exportToCSV();
 		}
 
 		protected void onPostExecute(Boolean status) {
 			progressDialog.dismiss();
 			String msg;
 			if (status == true) {
 				msg = "Journeys exported to " + Helpers.exportDirectoryPath
 						+ "/routes.csv";
 			} else {
 				msg = "Failed to export journeys. Check the SD card is available and writable.";
 			}
 
 			Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
 		}
 	}
 }
