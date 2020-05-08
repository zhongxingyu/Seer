 package com.teamblobby.studybeacon;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import com.google.android.maps.*;
 import com.google.zxing.integration.android.*;
 import com.teamblobby.studybeacon.datastructures.*;
 import com.teamblobby.studybeacon.network.*;
 import com.teamblobby.studybeacon.ui.*;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.*;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 public class SBMapActivity extends MapActivity 
 {
 
 	public final static int REQUESTCODE_RETURNED_FROM_MYCOURSES = 1;
 
 	public final static String TAG = "SBMapActivity";
 
 	private static final int REQUESTCODE_RETURNED_FROM_BEACON = 0;
 	
 	protected Spinner courseSpinner;
 	private ArrayAdapter<String> courseSpinnerAdapter;
 
 	protected Button myClassesButton;
 	protected ImageButton beaconButton;
 
 	private SBMapView mapView;
 
 	private BeaconItemizedOverlay beacItemizedOverlay;
 
 	private final static Drawable beaconD = Global.res.getDrawable(R.drawable.beacon);
 
 	private List<String> courses;
 
 	protected int filterLastSelected = 0;
 
 	private QRButton qrButton;
 	
 	protected Timer timer;
 	protected TimerTask timerTask;
 	// The timer interval is in milliseconds
 	public final static long timerInterval = 10*1000;
 	
 	private class MyActivityApiHandler extends ActivityAPIHandler {
 		@Override
 		public Activity getActivity() {
 			return SBMapActivity.this;
 		}
 		
 		@Override
 		protected void handleFailure(APIClient.APICode code, Throwable e) {
 			// TODO Complain about failure
 			switch (code) {
 			case CODE_QUERY:
 				Toast.makeText(SBMapActivity.this, "Failed to load beacons from server", Toast.LENGTH_SHORT).show();
 				break;
 			default:
 				// Shouldn't ever get here. Complain?
 			}
 		}
 		
 		@Override
 		protected void handleSuccess(APIClient.APICode code, Object result) {
 			switch (code) {
 			case CODE_QUERY:
 				@SuppressWarnings("unchecked")
 				ArrayList<BeaconInfo> beacons = (ArrayList<BeaconInfo>) result;
 
				BeaconInfo presentBeacon = Global.getCurrentBeacon();

 				for (BeaconInfo beacon : beacons) {
 					beacItemizedOverlay.addReplaceRemoveBeacon(beacon); // also takes care of balloon
					if (presentBeacon != null) {
 						if ((presentBeacon.getBeaconId() == beacon.getBeaconId())
 							&& ( ! presentBeacon.equals(beacon) )) {
 							// Update
							Log.d(TAG,"save updated beacon info");
 							if (beacon.getVisitors() > 0) {
 								Global.setCurrentBeacon(beacon);
 							} else {
 								Global.setCurrentBeacon(null);
 							}
 						}
 					}
 				}
				Log.d(TAG,"invalidate");
 				mapView.invalidate();
 				break;
 			default:
 				// Shouldn't ever get here. Complain?
 			}
 		}	
 	}
 	
 	private MyActivityApiHandler myAPIHandler = new MyActivityApiHandler();
 
 	///////////////////////////////////////////////////////
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.map);
 
 		// Find the UI elements
 		courseSpinner = (Spinner) findViewById(R.id.mapCourseSpinner);
 		
 		// set up an onClickListener handler for classesButton
 		myClassesButton = (Button) findViewById(R.id.myClassesButton);
 		myClassesButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				startMyCoursesActivity();
 			}
 		});
 
 		beaconButton = (ImageButton) findViewById(R.id.newBeaconButton);
 		qrButton = (QRButton) findViewById(R.id.qrbutton);
 		
 		this.loadCourses(savedInstanceState);
 
 		this.setUpMapView(savedInstanceState);
 
 		this.setUpBeacons(savedInstanceState);
 
 		this.setUpTimer();
 
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		Log.d(TAG,"onResume()");
 		mapView.resume();
 
 		doTutorial();
 
 		updateBeaconButton();
 		// TODO -- Do we want to do this?
 		startQuery();
 
 		startTimer();
 		qrButton.updateButton(Global.getCurrentBeacon());
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		Log.d(TAG,"onPause()");
 		mapView.pause();
 
 		stopTimer();
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		APIClient.cancel(this);
 	}
 
 	private void setUpTimer() {
 		timer = new Timer();
 	}
 
 	private void startTimer() {
 
 		if (timerTask != null) {
 			timerTask.cancel();
 		}
 
 		timerTask = new TimerTask() {
 
 			@Override
 			public void run() {
 				beacItemizedOverlay.cleanBeacons();
 				startQuery();
 			}
 		};
 
 		timer.schedule(timerTask, timerInterval, timerInterval);
 
 	}
 
 	private void stopTimer() {
 
 		timerTask.cancel();
 		timerTask = null;
 
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		// TODO Auto-generated method stub
 		super.onSaveInstanceState(outState);
 		// TODO Save the state of mBeacons
 		mapView.onSaveInstanceState(outState);
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onRestoreInstanceState(savedInstanceState);
 		// This doesn't seem to ever get called
 		mapView.onRestoreInstanceState(savedInstanceState);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
 		if (scanResult != null) {
 			qrButton.handleResult(scanResult);
 		}
 		
 		switch (requestCode) {
 		case REQUESTCODE_RETURNED_FROM_MYCOURSES:
 			if (resultCode == MyCoursesActivity.RESULT_COURSES_CHANGED) {
 				loadCourses(null);
 				beacItemizedOverlay.cleanUntrackedCourseOverlays(courses);
 				updateBeaconButton();
 				startQuery();
 			}
 			break;
 		
 		case REQUESTCODE_RETURNED_FROM_BEACON:
 			updateBeaconButton();
 			break;
 		}
 	}
 	
 	private void updateBeaconButton() {
 		if (Global.atBeacon()) {
 			Log.d(TAG, "We are at a beacon.");
 			beaconButton.setImageResource(R.drawable.beacon_edit);
 			beaconButton.setOnClickListener(new OnClickListener() {
 
 				public void onClick(View v) {
 					editBeaconClicked(v);
 				}
 			});
 			beaconButton.setEnabled(true);
 		}
 		else {
 			Log.d(TAG, "We are not at a beacon.");
 			if (courses.size() > 0) {
 				beaconButton.setImageResource(R.drawable.newbeaconicon);
 				beaconButton.setOnClickListener(new OnClickListener() {
 
 					public void onClick(View v) {
 						newBeaconClicked(v);
 					}
 				});
 				beaconButton.setEnabled(true);
 			} else {
 				beaconButton.setImageResource(R.drawable.newbeaconicon_gray);
 				beaconButton.setEnabled(false);
 			}
 		}
 
 	}
 
 	protected void setUpMapView(Bundle savedInstanceState) {
 		// TODO use savedInstanceState if possible
 		// add zoom
 		this.mapView = (SBMapView) findViewById(R.id.mapView);
 		mapView.setActivity(this);
 		mapView.setBuiltInZoomControls(true);
 
 		// Nic is so smart! -Leo
 		if (savedInstanceState == null) mapView.setDefaultMapPosition();
 
 	}
 
 	private void setUpBeacons(Bundle savedInstanceState) {
 		// TODO use savedInstanceState
 		// Don't fill beacItemOverlay unless you already have some beacons
 		// If we had some beacons from savedInstanceState, we would want to do that here
 		beacItemizedOverlay = new BeaconItemizedOverlay(beaconD, this, mapView);
 		beacItemizedOverlay.addReplaceRemoveBeacon(new BeaconInfoSimple(-1, "", new GeoPoint(0,0), 0, "", "", "", "", new Date(), new Date()));
 
 		// TODO ADD THIS OVERLAY
 		mapView.getOverlays().add(beacItemizedOverlay);
 
 		startQuery();
 	}
 
 	private void doTutorial() {
 		switch ( Global.getTutorialStep() ) {
 		case 0:
 			(new AlertDialog.Builder(this)).setMessage(R.string.welcomeMapMessage).setTitle("Welcome to StudyBeacon!")
 			.setCancelable(true).setPositiveButton(R.string.OK,
 					new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 				}
 			})
 			.setNegativeButton("Skip Tutorial", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					Global.skipTutorial();
 				}
 			}).show();
 			//increment
 			Global.incrementTutorialStep();
 			break;
 		case 3:
 
 			(new AlertDialog.Builder(SBMapActivity.this)).setMessage(R.string.createBeaconTutorial1).setTitle("Finding a StudyBeacon")
 			.setCancelable(false).setPositiveButton(R.string.OK,
 					new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 
 					(new AlertDialog.Builder(SBMapActivity.this)).setMessage(R.string.createBeaconTutorial2).setTitle("Joining a StudyBeacon")
 					.setCancelable(false).setPositiveButton(R.string.OK,
 							new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 						}
 					}).show();
 
 				}
 			}).show();
 			//increment
 			Global.incrementTutorialStep();
 		default:
 			break;
 		}
 	}
 
 	protected Boolean wantAllCourses() {
 		// TODO Reimplement this when the course spinner is fixed
 		String selected = (String)courseSpinner.getSelectedItem();
 		return selected.equals(Global.res.getString(R.string.allCourses));
 	}
 
 	public void startQuery() {
 		// find the view bounds
 		int deltaLat = mapView.getLatitudeSpan(), deltaLon = mapView.getLongitudeSpan();
 		GeoPoint ctr = mapView.getMapCenter();
 		int LatE6Min, LatE6Max, LonE6Min, LonE6Max;
 		LatE6Min = ctr.getLatitudeE6() - deltaLat/2;
 		LatE6Max = ctr.getLatitudeE6() + deltaLat/2;
 		LonE6Min = ctr.getLongitudeE6() - deltaLon/2;
 		LonE6Max = ctr.getLongitudeE6() + deltaLon/2;
 
 		// Try to populate the beacons asynchronously
 		// TODO don't use courses, but rather the selection from the spinner.
 		// If the selection is All, then use courses.
 		
 		List<String> queryCourses;
 		String selected = (String)courseSpinner.getSelectedItem();
 		if (selected.equals(Global.res.getString(R.string.allCourses))) {
 			queryCourses = courses;
 		} else {
 			queryCourses = new ArrayList<String>();
 			queryCourses.add(selected);
 		}
 		
 		APIClient.query(LatE6Min, LatE6Max, LonE6Min, LonE6Max, queryCourses, myAPIHandler,
 				this);
 	}
 
 	protected void loadCourses(Bundle savedInstanceState) {
 		// TODO use savedInstanceState if possible.
 		// Specifically, keep track of what the selection was in the spinner
 		// and automatically set that.
 
 		courses = Global.getCourses();
 
 		courseSpinnerAdapter =
 				new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
 						Global.getCourses());
 
 		courseSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 
 		courseSpinnerAdapter.insert(getString(R.string.allCourses),0);
 		courseSpinnerAdapter.add(getString(R.string.editCourses));
 
 		courseSpinner.setAdapter(courseSpinnerAdapter);
 		courseSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			public void onItemSelected(AdapterView<?> parentAdapterView, View view,
 					int position, long id) {
 				if ( position == parentAdapterView.getAdapter().getCount()-1 ) {
 					//go back to all
 					parentAdapterView.setSelection(filterLastSelected);
 					// this is the edit item, so spawn my courses activity.
 					startMyCoursesActivity();
 				}
 				filterLastSelected  = position;
 				if (position == 0)
 					beacItemizedOverlay.setCourseToDisplay(null);
 				else if (position != parentAdapterView.getAdapter().getCount() - 1)
 					beacItemizedOverlay.setCourseToDisplay((String)parentAdapterView.getAdapter().getItem(position));
 			}
 
 			public void onNothingSelected(AdapterView<?> arg0) {
 				// TODO Auto-generated method stub
 			}
 
 		});
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 
 		return false;
 	}
 
 	public void newBeaconClicked(View view) {
 		Intent intent = new Intent(this, BeaconEditActivity.class);
 		intent.setAction(BeaconEditActivity.ACTION_NEW);
 		// check what the spinner is set to
 		// TODO
 		String selected = (String)courseSpinner.getSelectedItem();
 		if (! selected.equals(Global.res.getString(R.string.allCourses))) {
 			intent.putExtra(BeaconEditActivity.EXTRA_COURSE, selected);
 		}
 		startActivityForResult(intent, REQUESTCODE_RETURNED_FROM_BEACON);
 	}
 	
 	public void editBeaconClicked(View view) {
 		Intent intent = new Intent(this, BeaconEditActivity.class);
 		intent.setAction(BeaconEditActivity.ACTION_EDIT);
 		//intent.putExtra(BeaconEditActivity.EXTRA_BEACON, Global.getCurrentBeacon());
 		startActivityForResult(intent, REQUESTCODE_RETURNED_FROM_BEACON);
 	}
 
 	protected void startMyCoursesActivity() {
 		Intent i = new Intent(SBMapActivity.this, MyCoursesActivity.class);
 		startActivityForResult(i, REQUESTCODE_RETURNED_FROM_MYCOURSES);
 	}
 
 }
