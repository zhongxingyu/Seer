 package net.geekzy.mobile.brewclock.activities;
 
 import net.geekzy.mobile.brewclock.R;
 import net.geekzy.mobile.brewclock.data.TeaData;
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.support.v4.widget.CursorAdapter;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity implements OnClickListener, OnItemSelectedListener {
 	private static final int INITIAL_PROFILE_POS = 0;
 
 	/** Properties **/
 	protected Button btnPlusTime;
 	protected Button btnMinusTime;
 	protected Button btnStartBrew;
 	protected TextView lblCountLabel;
 	protected TextView lblTimeLabel;
 	protected Spinner spnTea;
 
 	protected int brewTime = 3;
 	protected CountDownTimer timerCountDown;
 	protected int brewCount = 0;
 	protected boolean isBrewing = false;
 	protected boolean finishedBrew = false;
 	protected TeaData teaData;
 	protected SimpleCursorAdapter teaCursorAdapter;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		// Connect interface elements to properties
 		btnPlusTime = (Button) findViewById(R.id.brew_time_up);
 		btnMinusTime = (Button) findViewById(R.id.brew_time_down);
 		btnStartBrew = (Button) findViewById(R.id.brew_start);
 		lblCountLabel = (TextView) findViewById(R.id.brew_count_label);
 		lblTimeLabel = (TextView) findViewById(R.id.brew_time);
 		spnTea = (Spinner) findViewById(R.id.tea_spinner);
 
 		// Setup ClickListeners
 		btnPlusTime.setOnClickListener(this);
 		btnMinusTime.setOnClickListener(this);
 		btnStartBrew.setOnClickListener(this);
 		// Setup ItemSelectedListener
 		spnTea.setOnItemSelectedListener(this);
 
 		// initailize data
 		teaData = new TeaData(this);
 		// initialize spinner
 		// Add some default tea data! (Adjust to your preference :)
 		if (teaData.count() == 0) {
 			teaData.insert("Earl Grey", 3);
 			teaData.insert("Assam", 3);
 			teaData.insert("Jasmine Green", 1);
 			teaData.insert("Darjeeling", 2);
 		}
 		Cursor cursor = teaData.all(this);
 		teaCursorAdapter = new SimpleCursorAdapter(
 				this,
 				android.R.layout.simple_spinner_item,
 				cursor,
 				new String[] { TeaData.NAME },
 				new int[] { android.R.id.text1 },
 				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
 		spnTea.setAdapter(teaCursorAdapter);
 		teaCursorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 
 		// Set the initial brew values
 		setBrewCount(0);
 		setBrewTime(3);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		// update spinner
 		teaCursorAdapter.changeCursor(teaData.all(this));
 		teaCursorAdapter.notifyDataSetChanged();
 		spnTea.setSelection(INITIAL_PROFILE_POS);
 	}
 
	@Override
	public void onDestroy()	{
		super.onDestroy();
		// close data source
		teaData.close();
	}

 	/**
 	 * Set an absolute value for the number of minutes to brew.
 	 * has no effect if a brew is currently running.
 	 * @param minutes The number of minutes to brew.
 	 */
 	public void setBrewTime(int minutes) {
 		// skip set time while brewing
 		if (isBrewing) {
 			return;
 		}
 
 		// set brew time in minutes
 		brewTime = minutes;
 
 		// no zero brew time values; default to 1
 		if (brewTime < 1) {
 			brewTime = 1;
 		}
 
 		lblTimeLabel.setText(String.valueOf(brewTime) + "m");
 	}
 
 	/**
 	 * Set the number of brews that have been made,
 	 * and update the interface.
 	 * @param count the new number of the brews.
 	 */
 	public void setBrewCount(int count) {
 		brewCount = count;
 		lblCountLabel.setText(String.valueOf(brewCount));
 	}
 
 	/**
 	 * Start the brew timer
 	 */
 	public void startBrew() {
 		// create a new CountDownTimer to track the brew time
 		timerCountDown = new CountDownTimer(brewTime * 60 * 1000, 1000) {
 
 			@Override
 			public void onTick(long millisUntilFinished) {
 				// update timer label
 				lblTimeLabel.setText(String.valueOf(millisUntilFinished / 1000) + "s");
 			}
 
 			@Override
 			public void onFinish() {
 				// reset flag
 				isBrewing = false;
 				// add brewing counter
 				setBrewCount(++brewCount);
 				// mark finished
 				finishedBrew = true;
 
 				// update timer label
 				lblTimeLabel.setText(R.string.lbl_brew_up);
 				// set button label to more
 				btnStartBrew.setText(R.string.lbl_more);
 			}
 		};
 
 		// start the timer
 		timerCountDown.start();
 		// change button label
 		btnStartBrew.setText(R.string.lbl_stop);
 		// set flag
 		isBrewing = true;
 	}
 
 	/**
 	 * Stop the brew timer
 	 */
 	public void stopBrew() {
 		// cancel the timer
 		if (timerCountDown != null) {
 			timerCountDown.cancel();
 		}
 
 		// reset flag
 		isBrewing = false;
 		// reset label
 		btnStartBrew.setText(R.string.lbl_start);
 		// reset time
 		onItemSelected(spnTea, spnTea, 0, 0);
 	}
 
 	/**
 	 * Reset to initial state to do more brewing
 	 */
 	public void moreBrew() {
 		// reset brew time
 		setBrewTime(3);
 		// reset flag
 		finishedBrew = false;
 		// reset label
 		btnStartBrew.setText(R.string.lbl_start);
 	}
 
 	/**
 	 * Remove selected tea brew profile from spinner and db
 	 * @return true - successfully removed; false - cannot removed last profile
 	 */
 	public boolean removeTea() {
 		// warn cannot empty spinner
 		if (teaData.count() == 1) {
 			// TODO display warn
 			return false;
 		}
 		// get currently selected
 		Cursor cursor = (Cursor) spnTea.getSelectedItem();
 		String teaName = cursor.getString(1);
 
 		// delete it
 		teaData.delete(cursor.getInt(0));
 		// update spinner
 		teaCursorAdapter.changeCursor(teaData.all(this));
 		teaCursorAdapter.notifyDataSetChanged();
 
 		// toast it
 		Toast.makeText(this,
 				getString(R.string.suc_remove_tea, teaName),
 				Toast.LENGTH_LONG).show();
 
 		return true;
 	}
 
 	@Override
 	public void onClick(View v) {
 		// plus button clicked
 		if (v == btnPlusTime) {
 			setBrewTime(++brewTime);
 		}
 		// minus button clicked
 		else if (v == btnMinusTime) {
 			setBrewTime(--brewTime);
 		}
 		// start button clicked
 		else if (v == btnStartBrew) {
 			// is brewing the stop
 			if (isBrewing) {
 				stopBrew();
 			}
 			else if (finishedBrew) {
 				moreBrew();
 			}
 			// not brewing the start
 			else { startBrew(); }
 		}
 	}
 
 	@Override
 	public void onItemSelected(AdapterView<?> spinner, View view, int pos,
 			long id) {
 		if (spinner == spnTea) {
 			// update the brew time with the selected tea's brewtime
 			Cursor cursor = (Cursor) spinner.getSelectedItem();
 			// get the value from the third column of table teas
 			setBrewTime(cursor.getInt(2));
 		}
 	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> adapterView) {
 		// do nothing
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main, menu);
 
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.add_tea: // add tea brew profiles
 			Intent intent = new Intent(this, AddTeaActivity.class);
 			startActivity(intent);
 			return true;
 
 		case R.id.remove_tea: // remove a selected tea brew prifile
 			return removeTea();
 
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 }
