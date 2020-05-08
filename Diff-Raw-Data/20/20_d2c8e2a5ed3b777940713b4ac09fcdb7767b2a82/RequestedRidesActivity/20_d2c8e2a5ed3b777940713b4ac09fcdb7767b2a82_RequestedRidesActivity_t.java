 package com.app.getconnected.activities;
 
 import java.util.ArrayList;
 
 import com.app.getconnected.R;
 
 import android.app.DialogFragment;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TableLayout;
 import android.widget.TableLayout.LayoutParams;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.util.getconnected.ActivityHelper.DatePickerFragment;
 
 public class RequestedRidesActivity extends BaseActivity {
 	TableLayout tl;
	ArrayList<ArrayList<View>> tableArray = new ArrayList<ArrayList<View>>();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_requestedrides);
 		initLayout(R.string.title_activity_requestrides, true, true, true,
 				false);
 
 		tableinit();
 		addTableRow("08:30", "12:00", "Groningen", "Berlijn", true);
 	}
 
 	/**
 	 * Initializes the table
 	 */
 	private void tableinit() {
 		tl = (TableLayout) findViewById(R.id.tableLayoutRequestedRides);
 
 		TextView departure_time = new TextView(this);
 		departure_time.setText(getResources()
 				.getString(R.string.departure_time));
 		TextView arrival_time = new TextView(this);
 		arrival_time.setText(getResources().getString(R.string.arrival_time));
 		TextView from = new TextView(this);
 		from.setText(getResources().getString(R.string.location_from));
 		TextView destination = new TextView(this);
 		destination.setText(getResources().getString(
 				R.string.location_destination));
 
 		TableRow rowHeader = new TableRow(this);
 
 		rowHeader.addView(departure_time);
 		rowHeader.addView(arrival_time);
 		rowHeader.addView(from);
 		rowHeader.addView(destination);
 
 		tl.addView(rowHeader, new TableLayout.LayoutParams(
 				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 		addTableRow("", "", "", "", false);
 		tl.setStretchAllColumns(true);
 	}
 
 	/**
 	 * Adds a table row to a given table
 	 * 
 	 * @param departurev
 	 * @param arrivalv
 	 * @param fromv
 	 * @param destinationv
 	 */
 	private void addTableRow(String departurev, String arrivalv, String fromv,
 			String destinationv, Boolean includeButton) {
 		TextView departure_time = new TextView(this);
 		departure_time.setText(departurev);
 		TextView arrival_time = new TextView(this);
 		arrival_time.setText(arrivalv);
 		TextView from = new TextView(this);
 		from.setText(fromv);
 		TextView destination = new TextView(this);
 		destination.setText(destinationv);
 
 		TableRow datarow = new TableRow(this);
 		
 		ArrayList<View> dataArray = new ArrayList<View>();
 		dataArray.add(departure_time);
 		dataArray.add(arrival_time);
 		dataArray.add(from);
 		dataArray.add(destination);
 		
 		
 		for(int i = 0; i < dataArray.size(); i++){
 			datarow.addView(dataArray.get(i));
 		}	
 		
 		if (includeButton) {
 			tableArray.add(dataArray);
 			Button button = new Button(this);
 			button.setId(tableArray.size()-1);
 			button.setText(getResources().getString(R.string.join_ride));
 			button.setOnClickListener(new View.OnClickListener() {
 			    public void onClick(View v) {
 			        join_ride(v);
 			      }
 			    });
 			datarow.addView(button);
 		}
 
 		tl.addView(datarow, new TableLayout.LayoutParams(
 				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 
 		tl.setStretchAllColumns(true);
 	}
 
 	protected void join_ride(View v) {
 		String text = (((TextView) tableArray.get(v.getId()).get(0)).getText()).toString();
		Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
 		toast.show();		
 	}
 
 	/**
 	 * Shows the date picker
 	 * 
 	 * @param v
 	 */
 	public void showDatePickerDialog(View v) {
 		DialogFragment newFragment = new DatePickerFragment();
 		newFragment.show(getFragmentManager(), "datePicker");
 
 		if (DatePickerFragment.year == 2014) {
 			tl.removeAllViewsInLayout();
 			tableinit();
 		}
 		if (DatePickerFragment.year == 2013) {
 			tl.removeAllViewsInLayout();
 			tableinit();
 		}
 		tl.invalidate();
 		tl.refreshDrawableState();
 	}
 }
