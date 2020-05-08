 package com.t2.vas.activity;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.DatePickerDialog;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.util.SparseBooleanArray;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Adapter;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
 
 import com.t2.vas.Global;
 import com.t2.vas.R;
 import com.t2.vas.db.tables.Group;
 import com.t2.vas.db.tables.Note;
 import com.t2.vas.db.tables.Scale;
 import com.t2.vas.view.SeparatedListAdapter;
 
 public class ShareActivity extends ABSNavigationActivity implements OnClickListener, OnItemClickListener {
 	private static final String TAG = ShareActivity.class.getSimpleName();
 	
 	private Button shareButton;
 	private DatePickerDialog fromDatePicker;
 	private DatePickerDialog toDatePicker;
 
 	private long fromTime = 0;
 	private long toTime = 0;
 	
 	private SimpleDateFormat dateFormatter = new SimpleDateFormat(Global.SHARE_TIME_FORMAT);
 	private SimpleDateFormat exportFormatter = new SimpleDateFormat(Global.EXPORT_TIME_FORMAT);
 	private Button fromTimeButton;
 	private Button toTimeButton;
 	private SimpleCursorAdapter groupsAdapter;
 	private SimpleAdapter otherItemsAdapter;
 	private SeparatedListAdapter listAdapter;
 	private ListView list;
 	private ProgressDialog progressDialog;
 	private ArrayList<Uri> attachementUris = new ArrayList<Uri>();
 	private Handler startShareIntentHandler;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		// Build the share handler
 		startShareIntentHandler = new Handler() {
 			@Override
 			public void handleMessage(Message msg) {
 				Log.v(TAG, "Received message, starting intent.");
 				
 				// Hide the progress dialog.
 				progressDialog.dismiss();
 				
 				Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
 				shareIntent.setType("text/csv");
 				shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachementUris);
 				startActivity(Intent.createChooser(shareIntent, getString(R.string.share_data_title)));
 			}
 		};
 		
 		// Build the progress dialog.
 		progressDialog = new ProgressDialog(this);
 		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		progressDialog.setMessage(getString(R.string.share_preparing_data));
 		progressDialog.setCancelable(false);
 		
 		// Build the from date dialog.
 		Calendar cal = Calendar.getInstance();
 		fromDatePicker = new DatePickerDialog(
 				this,
 				new DatePickerDialog.OnDateSetListener() {
 					@Override
 					public void onDateSet(DatePicker view, int year, int monthOfYear,
 							int dayOfMonth) {
 						onFromDateSet(year, monthOfYear, dayOfMonth);
 					}
 				},
 				cal.get(Calendar.YEAR),
 				cal.get(Calendar.MONTH),
 				cal.get(Calendar.DAY_OF_MONTH)
 		);
 		
 		// Build the to date dialog.
 		toDatePicker = new DatePickerDialog(
 				this,
 				new DatePickerDialog.OnDateSetListener() {
 					@Override
 					public void onDateSet(DatePicker view, int year, int monthOfYear,
 							int dayOfMonth) {
 						onToDateSet(year, monthOfYear, dayOfMonth);
 					}
 				},
 				cal.get(Calendar.YEAR),
 				cal.get(Calendar.MONTH),
 				cal.get(Calendar.DAY_OF_MONTH)
 		);
 		
 		
 		// Set the content view
 		this.setContentView(R.layout.share_activity);
 		
 		// Setup the buttons.
 		fromTimeButton = (Button)this.findViewById(R.id.fromDate);
 		fromTimeButton.setOnClickListener(this);
 		
 		toTimeButton = (Button)this.findViewById(R.id.toDate);
 		toTimeButton.setOnClickListener(this);
 		
 		shareButton = (Button)this.findViewById(R.id.shareButton);
 		shareButton.setOnClickListener(this);
 		
 		// Setup group items.
 		Group g = new Group(dbAdapter);
 		Cursor cursor = g.getGroupsCursor();
 		groupsAdapter = new SimpleCursorAdapter(
 				this, 
 				android.R.layout.simple_list_item_multiple_choice, 
 				cursor, 
 				new String[] {
 						"title",
 				}, 
 				new int[] {
 						android.R.id.text1,
 				}
 		);
 		
 		// Setup the other clear items.
 		ArrayList<HashMap<String,Object>> otherDataItems = new ArrayList<HashMap<String,Object>>();
 		HashMap<String,Object> item = new HashMap<String,Object>();
 		item.put("id", "notes");
 		item.put("title", getString(R.string.clear_data_notes));
 		otherDataItems.add(item);
 		otherItemsAdapter = new SimpleAdapter(
 				this, 
 				otherDataItems, 
 				android.R.layout.simple_list_item_multiple_choice,
 				new String[] {
 						"title", 
 				},
 				new int[] {
 						android.R.id.text1,
 				}
 		);
 		
 		// init the list adapter
 		listAdapter = new SeparatedListAdapter(this);
 		listAdapter.addSection(getString(R.string.share_groups_header), groupsAdapter);
 		listAdapter.addSection(getString(R.string.share_other_header), otherItemsAdapter);
 		
 		list = (ListView)this.findViewById(R.id.list);
 		list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
 		list.setOnItemClickListener(this);
 		list.setAdapter(listAdapter);
 
 		
 		// Set form item default values
		onToDateSet(
 				cal.get(Calendar.YEAR),
 				cal.get(Calendar.MONTH),
				cal.get(Calendar.DAY_OF_MONTH)
 		);
		onFromDateSet(
 				cal.get(Calendar.YEAR),
 				cal.get(Calendar.MONTH),
				cal.getActualMinimum(Calendar.DAY_OF_MONTH)
 		);
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch(v.getId()) {
 			case R.id.fromDate:
 				fromDatePicker.show();
 				return;
 				
 			case R.id.toDate:
 				toDatePicker.show();
 				return;
 				
 			case R.id.shareButton:
 				shareData();
 				return;
 		}
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 		setShareButtonEnabled();
 	}
 	
 	private void onFromDateSet(int year, int monthOfYear, int dayOfMonth) {
 		Calendar cal = Calendar.getInstance();
 		cal.set(Calendar.YEAR, year);
 		cal.set(Calendar.MONTH, monthOfYear);
 		cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
 		cal.set(Calendar.HOUR_OF_DAY, 0);
 		cal.set(Calendar.MINUTE, 0);
 		cal.set(Calendar.SECOND, 0);
 		cal.set(Calendar.MILLISECOND, 0);
 		
 		fromDatePicker.updateDate(
 				year, 
 				monthOfYear, 
 				dayOfMonth
 		);
 		
 		fromTime = cal.getTimeInMillis();
 		fromTimeButton.setText(dateFormatter.format(new Date(fromTime)));
 		setShareButtonEnabled();
 	}
 	
 	private void onToDateSet(int year, int monthOfYear, int dayOfMonth) {
 		Calendar cal = Calendar.getInstance();
 		cal.set(Calendar.YEAR, year);
 		cal.set(Calendar.MONTH, monthOfYear);
 		cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
 		cal.set(Calendar.HOUR_OF_DAY, 0);
 		cal.set(Calendar.MINUTE, 0);
 		cal.set(Calendar.SECOND, 0);
 		cal.set(Calendar.MILLISECOND, 0);
 		
 		toDatePicker.updateDate(
 				year, 
 				monthOfYear, 
 				dayOfMonth
 		);
 		
 		toTime = cal.getTimeInMillis();
 		toTimeButton.setText(dateFormatter.format(new Date(toTime)));
 		setShareButtonEnabled();
 	}
 	
 	private void setShareButtonEnabled() {
 		shareButton.setEnabled(isFormDataValid());
		
		if(fromTime > toTime) {
			Toast.makeText(this, R.string.share_dates_offset_bad, Toast.LENGTH_LONG).show();
		}
 	}
 	
 	private boolean isFormDataValid() {
 		SparseBooleanArray checkedPositions = list.getCheckedItemPositions();
 		boolean isListItemChecked = false;
 		for(int i = 0; i < checkedPositions.size(); ++i) {
 			int position = checkedPositions.keyAt(i);
 			boolean isChecked = checkedPositions.get(position);
 			if(isChecked) {
 				isListItemChecked = true;
 				break;
 			}
 		}
 		
 		boolean isTimeCorrect = false;
 		if(fromTime < toTime) {
 			isTimeCorrect = true;
 		}
 		
 		return isListItemChecked && isTimeCorrect;
 	}
 	
 	private void shareData() {
 		final List<Long> groupIds = new ArrayList<Long>();
 		final List<String> otherIds = new ArrayList<String>();
 		
 		// Determine what to export.
 		SparseBooleanArray checkedPositions = list.getCheckedItemPositions();
 		for(int i = 0; i < checkedPositions.size(); ++i) {
 			int position = checkedPositions.keyAt(i);
 			boolean isChecked = checkedPositions.get(position);
 			if(!isChecked) {
 				continue;
 			}
 			
 			Adapter adapter = listAdapter.getAdapterForItem(position);
 			if(adapter == groupsAdapter) {
 				groupIds.add(listAdapter.getItemId(position));
 				
 			} else if(adapter == otherItemsAdapter) {
 				HashMap<String,Object> item = (HashMap<String, Object>) listAdapter.getItem(position);
 				otherIds.add(item.get("id").toString());
 			}
 		}
 		
 		// Start the progress dialog.
 		progressDialog.show();
 		attachementUris.clear();
 		
 		// Run the export on a separate thread.
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				File groupsFile = new File(Environment.getExternalStorageDirectory(), "t2mt-groups.csv");
 				File notesFile = new File(Environment.getExternalStorageDirectory(), "t2mt-notes.csv");
 				
 				Log.v(TAG, "Export thread started");
 				
 				// Export the groups.
 				if(groupIds.size() > 0) {
 					Log.v(TAG, "Exporting groups.");
 					exportGroupData(
 							groupsFile,
 							groupIds,
 							fromTime,
 							toTime
 					);
 					attachementUris.add(Uri.fromFile(groupsFile));
 				}
 				
 				// Export the notes.
 				if(otherIds.contains("notes")) {
 					Log.v(TAG, "Exporting notes.");
 					exportNotesData(
 							notesFile,
 							fromTime,
 							toTime
 					);
 					attachementUris.add(Uri.fromFile(notesFile));
 				}
 				
 				// Make sure the files are deleted when they are not needed.
 				groupsFile.deleteOnExit();
 				notesFile.deleteOnExit();
 				
 				Log.v(TAG, "Starting intent.");
 				// Start the share activity.
 				startShareIntentHandler.sendEmptyMessage(0);
 			}
 		}).start();
 	}
 	
 	private void exportGroupData(File file, List<Long> groupIds, long startTime, long endTime) {
 		if(file.exists()) {
 			file.delete();
 		}
 		file.deleteOnExit();
 		
 		try {
 			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
 			Group group;
 			for(int i = 0; i < groupIds.size(); ++i) {
 				group = new Group(dbAdapter);
 				group._id = groupIds.get(i);
 				group.load();
 				
 				// print the title
 				writer.write("\""+ group.title.replace("\"", "\"\"") +"\"");
 				writer.write("\n");
 				
 				Cursor cursor;
 				Scale scale;
 				ArrayList<Scale> scales = group.getScales();
 				for(int j = 0; j < scales.size(); ++j) {
 					scale = scales.get(j);
 					
 					// print the max/min scales
 					writer.write(",");
 					writer.write("\""+ scale.max_label.replace("\"", "\"\"") +"\"");
 					writer.write(",");
 					writer.write("\""+ scale.min_label.replace("\"", "\"\"") +"\"");
 					writer.write("\n");
 					
 					// print the values.
 					cursor = scale.getResults(startTime, endTime);
 					int timeIndex = cursor.getColumnIndex("timestamp");
 					int valueIndex = cursor.getColumnIndex("value");
 					while(cursor.moveToNext()) {
 						writer.write(",");
 						writer.write("\""+ exportFormatter.format(new Date(cursor.getLong(timeIndex))) +"\"");
 						writer.write(",");
 						writer.write("\""+ cursor.getInt(valueIndex) +"\"");
 						writer.write("\n");
 					}
 					cursor.close();
 				}
 			}
 			
 			writer.close();
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void exportNotesData(File file, long startTime, long endTime) {
 		if(file.exists()) {
 			file.delete();
 		}
 		file.deleteOnExit();
 		
 		try {
 			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
 			Note note = new Note(dbAdapter);
 			
 			Cursor cursor = note.getNotesCursor(startTime, endTime);
 			int timestampIndex = cursor.getColumnIndex("timestamp");
 			int noteIndex = cursor.getColumnIndex("note");
 			while(cursor.moveToNext()) {
 				writer.write("\""+ exportFormatter.format(new Date(cursor.getLong(timestampIndex))) +"\"");
 				writer.write(",");
 				writer.write("\""+ cursor.getString(noteIndex).replace("\"", "\"\"") +"\"");
 				writer.write("\n");
 			}
 			cursor.close();
 			
 			writer.close();
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
