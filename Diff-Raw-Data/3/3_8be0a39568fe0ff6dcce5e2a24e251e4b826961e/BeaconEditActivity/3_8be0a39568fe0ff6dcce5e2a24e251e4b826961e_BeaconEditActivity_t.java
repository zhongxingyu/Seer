 package com.teamblobby.studybeacon;
 
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import com.google.android.maps.*;
 import com.teamblobby.studybeacon.datastructures.*;
 import com.teamblobby.studybeacon.network.APIClient;
 import com.teamblobby.studybeacon.network.APIHandler;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.util.Patterns;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.*;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.text.util.*;
 
 
 public class BeaconEditActivity extends Activity implements APIHandler {
 
 	private static final int DEFAULT_WORKINGON_SPINNER_POSITION = 0;
 	// Here is the interface for intents to use
 	public static final String EXTRA_COURSE = "Course";
 	public static final String EXTRA_BEACON = "beacon";
 
 	public static final String ACTION_NEW  = "com.blobby.studybeacon.BeaconEditActivity.new";
 	public static final String ACTION_EDIT = "com.blobby.studybeacon.BeaconEditActivity.edit";
 	public static final String ACTION_VIEW = "com.blobby.studybeacon.BeaconEditActivity.view";
 	public static final String TAG = "SBBeaconEditActivity";
 
 	protected enum OperationMode {
 		MODE_NEW,
 		MODE_EDIT,
 		MODE_VIEW
 	}
 
 	protected OperationMode mode;
 
 	protected TextView beaconTitleTV;
 	protected Spinner courseSpinner;
 	protected TextView expiresTV;
 	protected Spinner expiresSpinner;
 	protected TextView expiresTimeTV;
 	protected Spinner workingOnSpinner;
 	protected TextView contact;
 	protected EditText phone;
 	protected EditText email;
 	protected EditText details;
 	protected Button beaconActionButton;
 	protected Button beaconSecondaryActionButton;
 
 	// This represents the beacon we are making.
 	protected BeaconInfo mBeacon;
 
 	private ArrayAdapter<CourseInfo> courseAdapter;
 	private ArrayAdapter<DurationSpinnerItem> expiresAdapter;
 	private ArrayAdapter<String> workingOnAdapter;
 	private UserLocator userLocator;
 	private ProgressDialog currentDialog;
 
 
 	private DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT);
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.beacon);
 
 		loadUIEls();
 
 		// This is the intent that started us
 		Intent startingIntent = getIntent();
 		String startingAction = startingIntent.getAction();
 
 		// Figure out what to do
 		if (startingAction.equals(ACTION_VIEW)) {
 			mode = OperationMode.MODE_VIEW;
 		} else if (startingAction.equals(ACTION_EDIT)) {
 			mode = OperationMode.MODE_EDIT;
 		} else { // By default, create a new beacon
 			mode = OperationMode.MODE_NEW;
 		}
 
 		switch (mode) {
 		case MODE_VIEW:
 			setUpForView(savedInstanceState,startingIntent);
 			break;
 		case MODE_EDIT:
 			setUpForEdit(savedInstanceState,startingIntent);
 			break;
 		case MODE_NEW:
 		default:
 			setUpForNew(savedInstanceState,startingIntent);
 			break;
 		}
 
 	}
 
 	private void loadUIEls() {
 		beaconTitleTV    = (TextView) findViewById(R.id.titleText);
 		courseSpinner    = (Spinner)  findViewById(R.id.courseSpinner);
 		expiresTV        = (TextView) findViewById(R.id.expiresTV);
 		expiresSpinner   = (Spinner)  findViewById(R.id.expiresSpinner);
 		expiresTimeTV    = (TextView) findViewById(R.id.expiresTimeTV);
 		workingOnSpinner = (Spinner)  findViewById(R.id.workingOnSpinner);
 		contact          = (TextView) findViewById(R.id.contactTV);
 		phone            = (EditText) findViewById(R.id.phone);
 		email            = (EditText) findViewById(R.id.email);
 		details          = (EditText) findViewById(R.id.detailsEdit);
 		beaconActionButton = (Button) findViewById(R.id.beaconActionButton);
 		beaconSecondaryActionButton = (Button) findViewById(R.id.beaconSecondaryActionButton);
 
 		// Set the spinners up
 		courseAdapter =
 				new ArrayAdapter<CourseInfo>(this,
 						android.R.layout.simple_spinner_item,
 						Global.getMyCourseInfos());
 
 		courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		courseSpinner.setAdapter(courseAdapter);
 
 		// set up the array for the expires spinner
 		List<DurationSpinnerItem> expiresList = new ArrayList<DurationSpinnerItem>();
 		String[] expireTimes = this.getResources().getStringArray(R.array.expiresTimes);
 		int[] expireMinutes = this.getResources().getIntArray(R.array.expiresMinutes);
 		
 		for ( int j=0; j<expireTimes.length; j++ ){
 			expiresList.add(new DurationSpinnerItem(expireTimes[j], expireMinutes[j]));
 		}
 		
 		expiresAdapter = new ArrayAdapter<DurationSpinnerItem>(
 											this,
 											android.R.layout.simple_spinner_item,
 											expiresList);
 		
 		expiresAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		expiresSpinner.setAdapter(expiresAdapter);
 
 		//workingOnAdapter = ArrayAdapter.createFromResource(
 		//		this, R.array.workingOnList, android.R.layout.simple_spinner_item);
 		List<String> workingOnList = new ArrayList<String>();
 		workingOnList.addAll(Arrays.asList(getResources().getStringArray(R.array.workingOnList)));
 		
 		workingOnAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, workingOnList);
 		workingOnAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		workingOnSpinner.setAdapter(workingOnAdapter);
 		workingOnSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 			public void onItemSelected(AdapterView<?> adapterView, View itemView,
 					int position, long id) {
 				int count = workingOnAdapter.getCount();
 				if ( position == count-1 ) { // last element
 					customWorkingOnAlert(count-1).show();
 				}
 			}
 			public void onNothingSelected(AdapterView<?> arg0) {}
 		});
 
 		expiresSpinner.setSelection(Global.res.getInteger(R.integer.expiresDefaultIndex));
 
 	}
 	
 	private Builder customWorkingOnAlert(final int index){
 		final EditText input = new EditText(this);
 		return new AlertDialog.Builder(this)
 					.setTitle(getResources().getString(R.string.workingOn))
 					.setView(input)
 					.setPositiveButton(getResources().getString(R.string.workingOnOK), new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							if ( input.getText().toString().equals("")){
 								workingOnSpinner.setSelection(DEFAULT_WORKINGON_SPINNER_POSITION);
 								return; // nothing to do!
 							}
 							String text = input.getText().toString();
 							addToWorkingOn(index, text);
 						}
 					})
 					.setNegativeButton(getResources().getString(R.string.workingOnCancel), new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							// return spinner to first element
 							workingOnSpinner.setSelection(DEFAULT_WORKINGON_SPINNER_POSITION);
 						}
 					});
 	}
 	
 	private class DurationSpinnerItem {
 		private int minutes;
 		private String displayString;
 		
 		@Override
 		public String toString() {
 			return this.getDisplayString();
 		}
 
 		public int getMinutes() {
 			return minutes;
 		}
 
 		public String getDisplayString() {
 			return displayString;
 		}
 
 		DurationSpinnerItem(String displayString,int minutes){
 			this.minutes=minutes;
 			this.displayString=displayString;
 		}
 		
 		
 	}
 
 	protected void setCourseSpinnerItem(String course) {
 		if (course != null) {
 			// Set the course spinner's selected element
 			int courseIndex = 0;
 			int count = courseAdapter.getCount();
 			for ( int j=0; j<count; j++){
 				if (courseAdapter.getItem(j).getName().equals(course)){
 					courseIndex = j;
 					break;
 				}
 			}
 			courseSpinner.setSelection(courseIndex);
 		}
 	}
 
 	protected int durationFromField() {
 		return ((DurationSpinnerItem) expiresSpinner.getSelectedItem()).getMinutes();
 	}
 
 	protected BeaconInfo beaconFromFields() {
 		String courseName = ((CourseInfo) courseSpinner.getSelectedItem()).getName();
 
 		GeoPoint loc = userLocator.getLocation(); // grab the user's location
 		Log.d(TAG,"loc: late6="+loc.getLatitudeE6()+" longe6="+loc.getLongitudeE6());
 
 		return new BeaconInfoSimple(-1, // don't have a BeaconId yet
 				courseName,
 				loc,
 				-1, // don't have a # of visitors yet
 				(String)workingOnSpinner.getSelectedItem(),
 				details.getText().toString(),
 				phone.getText().toString(),
 				email.getText().toString(),
 				new Date(),
 				new Date() // TODO put this in the future
 				);
 	}
 
 	private void setUpForNew(Bundle savedInstanceState, Intent startingIntent) {
 		// TODO -- Add logic if already at a beacon
 
 		// Set title text
 		beaconTitleTV.setText(R.string.newBeacon);
 
 		// If a course has been selected in the intent, try to set the spinner
 		setCourseSpinnerItem(startingIntent.getStringExtra(EXTRA_COURSE));
 
 		// Add a listener for the action button
 		beaconActionButton.setOnClickListener(new NewBeaconClickListener(this));
 		// Set the drawable on the action button
 		beaconActionButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.newbeaconicon, 0, 0, 0);
 
 		// start getting the user's location
 		userLocator = new UserLocator();
 		userLocator.startLocating();
 	}
 
 	protected final class NewBeaconClickListener implements OnClickListener {
 
 		protected BeaconEditActivity mActivity;
 
 		public NewBeaconClickListener(BeaconEditActivity sbBeaconEditActivity) {
 
 			mActivity = sbBeaconEditActivity;
 		}
 
 		public void onClick(View v) {
 			if ( !mActivity.userLocator.isReady() ) {
 				Toast.makeText(mActivity, R.string.stillLocating, Toast.LENGTH_SHORT).show(); //inform we are still locating.
 				Log.d(TAG, "canceled, still locating");
 				return;
 			}
 			currentDialog = ProgressDialog.show(mActivity, "", "Creating beacon...");
 			// needs working on from fields
 			APIClient.add(mActivity.beaconFromFields(), mActivity.durationFromField(), mActivity);
 		}
 	}
 
 	private void setUpForEdit(Bundle savedInstanceState, Intent startingIntent) {
 		// TODO Add logic if already at a beacon
 		// Set title text
 		beaconTitleTV.setText(R.string.editBeacon);
 
 		// Don't let the class be editable
 		courseSpinner.setEnabled(false);
 
 		beaconActionButton.setText(R.string.saveBeacon);
 		// Set the drawable on the action button
 		beaconActionButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.beacon_edit, 0, 0, 0);
 
 		beaconSecondaryActionButton.setVisibility(View.VISIBLE);
 		beaconSecondaryActionButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.beacon_leave, 0, 0, 0);
 		// The secondary button is the leave button
 		beaconSecondaryActionButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				if (mBeacon != null){
 					APIClient.leave(mBeacon.getBeaconId(), BeaconEditActivity.this);
 					currentDialog = ProgressDialog.show(BeaconEditActivity.this, "", "Leaving beacon...");
 				}else
 					Toast.makeText(BeaconEditActivity.this,
 							"Something went wrong -- I don't know which beacon you're viewing",
 							Toast.LENGTH_SHORT).show();
 			}
 		});
 
 		mBeacon = Global.getCurrentBeacon();
 		loadBeaconData();
 
 	}
 
 	private void setUpForView(Bundle savedInstanceState, Intent startingIntent) {
 		// Set title text
 		beaconTitleTV.setText(R.string.beaconDetails);
 
 		// Disable the elements' editability
 		Spinner spinners[] = {courseSpinner, expiresSpinner, workingOnSpinner};
 		for (Spinner s : spinners)
 			s.setEnabled(false);
 
 		// Change the "expires" text
 		expiresTV.setText(R.string.expiresAt);
 		expiresSpinner.setVisibility(View.GONE);
 		expiresTimeTV.setVisibility(View.VISIBLE);
 
 		EditText ets[] = {phone, email, details};
 		for (EditText e : ets) {
 			e.setFocusable(false);
 		}
 
 		// make the details have a different hint if nothing was given
 		details.setHint(R.string.detailHintView);
 		
 		mBeacon = startingIntent.getParcelableExtra(EXTRA_BEACON);
 		loadBeaconData();
 
 		beaconActionButton.setText(R.string.joinBeacon);
 		// Set the drawable on the action button
 		beaconActionButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.beacon_join, 0, 0, 0);
 
 		beaconActionButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				// TODO check user's location
 				if (mBeacon != null) {
 					APIClient.join(mBeacon.getBeaconId(), BeaconEditActivity.this);
 					currentDialog = ProgressDialog.show(BeaconEditActivity.this, "", "Joining beacon...");
 				} else
 					Toast.makeText(BeaconEditActivity.this,
 							"Something went wrong -- I don't know which beacon you're viewing",
 							Toast.LENGTH_SHORT).show();
 			}
 		});
 
 		if ( mBeacon == null )
 			return;
 
 		// don't show contact details if they weren't filled in
 		if ( mBeacon.getTelephone().equals("") )
 			phone.setVisibility(View.GONE);
 
 		if ( mBeacon.getEmail().equals("") )
 			email.setVisibility(View.GONE);
 
 		if ( mBeacon.getTelephone().equals("") && mBeacon.getEmail().equals("") )
 			contact.setVisibility(View.GONE);
 	}
 
 	private void loadBeaconData() {
 		// TODO What do we do if somebody did not call this properly?
 		
 
 		if (mBeacon == null) // FAILURE
 			return;
 
 		// Load the course name
 		setCourseSpinnerItem(mBeacon.getCourseName());
 		phone.setText(mBeacon.getTelephone());
 		email.setText(mBeacon.getEmail());
 		setWorkingOn(mBeacon.getWorkingOn());
 		details.setText(mBeacon.getDetails());
 		expiresTimeTV.setText(df.format(mBeacon.getExpires()));
 
 		if (this.mode == OperationMode.MODE_VIEW) {
 			Linkify.addLinks(phone, Patterns.PHONE, "sms:",
 					Linkify.sPhoneNumberMatchFilter,
 					Linkify.sPhoneNumberTransformFilter);
 			Linkify.addLinks(email, Linkify.EMAIL_ADDRESSES);
 		}
 	}
 
 	private void setWorkingOn(String workingOn) {
 		int position = workingOnAdapter.getPosition(workingOn);
 		if ( position == -1 ){ // -1 means it didn't find it
 			// add it to the spinner
 			position = workingOnAdapter.getCount()-1;
 			addToWorkingOn(position, workingOn);
 		}
 		workingOnSpinner.setSelection(position);
 	}
 	////////////////////////////////////////////////////////////////
 	// The following are for implementing SBAPIHandler
 
 
 	public Activity getActivity() {
 		return this;
 	}
 
 	public void onSuccess(APICode code, Object response) {
 		BeaconInfo beacon = null;
 		String messageText = null;
 		switch (code) {
 		case CODE_ADD:
 			beacon = (BeaconInfo) response;
 			messageText = new String("Beacon added successfully");
 			break;
 		case CODE_JOIN:
 			beacon = (BeaconInfo) response;
 			messageText = new String("Beacon joined successfully");
 			break;
 		case CODE_LEAVE:
 			messageText = new String("Beacon left successfully");
 			break;
 		case CODE_SYNC:
 			beacon = (BeaconInfo) response;
 			messageText = new String("Resynced with server successfully");
 			break;
 		default:
 			// TODO Shouldn't get here ... complain?
 		}
 		Toast.makeText(this, messageText, Toast.LENGTH_SHORT).show();
 		Global.setCurrentBeacon(beacon);
 		Global.updateBeaconRunningNotification();
 		currentDialog.dismiss();
 		// go back home EXCEPT on sync
 		if (code != APICode.CODE_SYNC) {
 			// TODO Set a result code? SBMapActivity will need to get new data.
 			this.finish();
 		}
 	}
 
 	public void onFailure(APICode code, Throwable e) {
 		String messageText = null;
 		switch (code) {
 		case CODE_ADD:
 			messageText = new String("Failed to add beacon");
 			Global.setCurrentBeacon(null);
 			Global.updateBeaconRunningNotification();
 			break;
 		case CODE_JOIN:
 			messageText = new String("Failed to join beacon");
 			Global.setCurrentBeacon(null);
 			Global.updateBeaconRunningNotification();
 			break;
 		case CODE_LEAVE:
 			messageText = new String("Failed to leave beacon -- trying to re-sync with server");
 			APIClient.sync(this);
 			break;
 		case CODE_SYNC:
 			messageText = new String("Failed to re-sync with server.");
 			// TODO What do we do?
 			break;
 		default:
 			// Shouldn't get here ... complain?
 		}
 		Toast.makeText(this, messageText, Toast.LENGTH_SHORT).show();
 		currentDialog.dismiss();
 		// For CODE_LEAVE, we have started a sync; now show a dialog must be after the above dismissal
		if (code == APICode.CODE_LEAVE)
			currentDialog = ProgressDialog.show(this, "", "Trying to re-sync with server...");
 	}
 
 	protected void addToWorkingOn(final int index, String text) {
 		workingOnAdapter.insert(text, index);
 		workingOnAdapter.notifyDataSetChanged();
 	}
 
 }
