 package com.runninghusky.spacetracker;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnCreateContextMenuListener;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.AdapterView.OnItemClickListener;
 
 import com.markupartist.android.widget.ActionBar;
 import com.markupartist.android.widget.ActionBar.IntentAction;
 
 public class SpaceTrackerActivity extends Activity {
 	// private ProgressDialog pd;
 	private SimpleAdapter mFlight;
 	private List<FlightData> flightData = new ArrayList<FlightData>();
 	private DataHelper dh;
 	private Boolean blnHistoryExist;
 	private ListView lvl;
 	private long flightId;
 	private String flightName;
 	public Context ctx = this;
 	private ActionBar actionBar;
 	private SharedPreferences prefs;
 	private Boolean isMetric = false;
 
 	// for preferences...
 	String UnitPref;
 
 	protected static final int CONTEXT_RUN = 1;
 	// protected static final int CONTEXT_STOP = 6;
 	protected static final int CONTEXT_EDIT = 2;
 	protected static final int CONTEXT_OPEN = 3;
 	protected static final int CONTEXT_EXPORT_KML = 4;
 	protected static final int CONTEXT_DELETE = 5;
 	protected static final int CONTEXT_MAP = 7;
 	protected static final int CONTEXT_RESET = 8;
 
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 	}
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		SharedPreferences sp = getSharedPreferences("spacetracker", 0);
 		if (!sp.getBoolean("agreedToEula", false)) {
 			Intent i = new Intent(SpaceTrackerActivity.this, EulaActivity.class);
 			SpaceTrackerActivity.this.startActivity(i);
 			finish();
 		}
 
 		actionBar = (ActionBar) findViewById(R.id.actionbar);
 		actionBar.addAction(new IntentAction(this, createQuickAddIntent(this),
 				android.R.drawable.ic_menu_add));
 
 		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 		isMetric = (prefs.getString("unit", "english").equals("english")) ? false
 				: true;
 
 		setupStart();
 	}
 
 	private void setupStart() {
 
 		this.dh = new DataHelper(this);
 		blnHistoryExist = this.dh.flightsExist();
 		this.dh.close();
 
 		lvl = (ListView) findViewById(R.id.ListViewFlights);
 		List<Flight> flightList = new ArrayList<Flight>();
 		if (blnHistoryExist) {
 			this.dh = new DataHelper(this);
 			flightList = this.dh.selectFlightHistory();
 			this.dh.close();
 		} else {
 			lvl.setVisibility(View.INVISIBLE);
 			HlprUtil.toast("Click add to create a new flight...", this, false);
 		}
 
 		ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();
 		HashMap<String, String> map = new HashMap<String, String>();
 		if (blnHistoryExist) {
 			// Log.d("history", "exists");
 			for (Flight f : flightList) {
 				map.put("id", Long.toString(f.getId()));
 				map.put("name", f.getName());
 				map.put("update", "SMS Update Interval (hh:mm:ss):  "
 						+ HlprUtil.convertSecondsToTime(Double.valueOf(f
 								.getSmsDuration())));
 				map.put("picupdate", "Pic Update Interval (hh:mm:ss):  "
 						+ HlprUtil.convertSecondsToTime(Double.valueOf(f
 								.getPicDuration())));
 				map.put("smsnum", "SMS Number:  " + f.getSmsNumber(false));
 				map.put("sendpics", "Send pics:  "
 						+ String.valueOf(f.getTakePic()));
 				map.put("sendsms", "Send SMS:  "
 						+ String.valueOf(f.getSendSms()));
 				if (isMetric) {
					map.put("distance", "Total Distance:  "
							+ String.valueOf(HlprUtil.roundTwoDecimals(Double
									.valueOf(f.getDistance()) * 1.609344))
							+ " kms");
				} else {
 					map.put("distance", "Total Distance:  " + f.getDistance()
 							+ " miles");
 				}
 				mylist.add(map);
 				map = new HashMap<String, String>();
 			}
 		} else {
 			map.put("id", "0");
 			map.put("name", "");
 			map.put("update", "");
 			map.put("smsnum", "");
 			map.put("sendpics", "");
 			map.put("sendsms", "");
 			map.put("picupdate", "");
 			map.put("distance", "");
 			mylist.add(map);
 			map = new HashMap<String, String>();
 		}
 
 		mFlight = new SimpleAdapter(this, mylist, R.layout.row, new String[] {
 				"id", "name", "update", "smsnum", "sendpics", "sendsms",
 				"picupdate", "distance" }, new int[] { R.id.id,
 				R.id.TextViewFlight, R.id.TextViewUpdateInter,
 				R.id.TextViewSMS, R.id.TextViewSendPictures,
 				R.id.TextViewSendSMS, R.id.TextViewPicUpdateInter,
 				R.id.TextViewDistance });
 		lvl.setAdapter(mFlight);
 
 		/* Add Context-Menu listener to the ListView. */
 		lvl.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
 			// @Override
 			public void onCreateContextMenu(ContextMenu menu, View v,
 					ContextMenu.ContextMenuInfo menuInfo) {
 
 				menu.add(0, CONTEXT_RUN, 0, "Start Logging");
 				// menu.add(0, CONTEXT_STOP, 1, "Stop Logging");
 				menu.add(0, CONTEXT_MAP, 2, "Show On Map");
 				menu.add(0, CONTEXT_EDIT, 3, "Edit Flight Settings");
 				// menu.add(0, CONTEXT_OPEN, 4, "View History");
 				menu.add(0, CONTEXT_DELETE, 5, "Delete Flight");
 				menu.add(0, CONTEXT_EXPORT_KML, 6, "Export KML");
 				menu.add(0, CONTEXT_RESET, 7, "Reset Track");
 			}
 		});
 
 		lvl.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> a, View v, int position,
 					long id) {
 				v.showContextMenu();
 			}
 		});
 	}
 
 	public boolean onContextItemSelected(MenuItem item) {
 
 		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
 				.getMenuInfo();
 		Object o = lvl.getItemAtPosition(menuInfo.position);
 		HashMap fullObject = (HashMap) o;
 		flightId = Long.parseLong(fullObject.get("id").toString());
 		flightName = String.valueOf(fullObject.get("name"));
 		switch (item.getItemId()) {
 		case CONTEXT_RUN:
 			if (String.valueOf(fullObject.get("sendpics")).equalsIgnoreCase(
 					"Send pics:  false")) {
 				startLoggingService();
 			} else {
 				startLoggingActivity();
 			}
 			break;
 		// case CONTEXT_STOP:
 		// stopLoggingService();
 		// break;
 		case CONTEXT_EDIT:
 			sendSetupIntent(flightId, false);
 			break;
 		case CONTEXT_OPEN:
 			sendHistoryIntent(flightId, flightName);
 			break;
 		case CONTEXT_DELETE:
 			resetFlight("delete");
 			break;
 		case CONTEXT_EXPORT_KML:
 			getSomeDataForEmail();
 			sendEmailKML();
 			break;
 		case CONTEXT_MAP:
 			initMap();
 			break;
 		case CONTEXT_RESET:
 			resetFlight("reset");
 			break;
 		default:
 			return super.onContextItemSelected(item);
 		}
 
 		return true;
 	}
 
 	private void startLoggingService() {
 		Intent i = new Intent(this, FlightLogService.class);
 		i.putExtra("flightId", flightId);
 		startService(i);
 	}
 
 	private void startLoggingActivity() {
 		Intent myIntent = new Intent(SpaceTrackerActivity.this,
 				CameraLoggerActivity.class);
 		Bundle b = new Bundle();
 		b.putLong("flightId", flightId);
 		myIntent.putExtras(b);
 		SpaceTrackerActivity.this.startActivity(myIntent);
 		finish();
 	}
 
 	// private void stopLoggingService() {
 	// stopService(new Intent(this, FlightLogService.class));
 	// }
 
 	private void sendSetupIntent(Long l, Boolean isNew) {
 		Intent myIntent = new Intent(SpaceTrackerActivity.this,
 				FlightSetupActivity.class);
 		Bundle b = new Bundle();
 		b.putLong("flightId", l);
 		b.putBoolean("isNew", isNew);
 		myIntent.putExtras(b);
 		SpaceTrackerActivity.this.startActivity(myIntent);
 		finish();
 	}
 
 	private void sendHistoryIntent(Long l, String n) {
 		Intent myIntent = new Intent(SpaceTrackerActivity.this,
 				FlightHistoryActivity.class);
 		Bundle b = new Bundle();
 		b.putLong("flightId", l);
 		Log.d("n", n);
 		b.putString("title", n);
 		myIntent.putExtras(b);
 		SpaceTrackerActivity.this.startActivity(myIntent);
 		finish();
 	}
 
 	// private void initHistory() {
 	// Intent myIntent = new Intent(SpaceTrackerActivity.this,
 	// FlightHistoryActivity.class);
 	// Bundle b = new Bundle();
 	// b.putLong("flightId", flightId);
 	// b.putString("title", flightName);
 	// myIntent.putExtras(b);
 	// SpaceTrackerActivity.this.startActivity(myIntent);
 	//
 	// finish();
 	// }
 
 	private void initMap() {
 		Intent myIntent = new Intent(SpaceTrackerActivity.this,
 				FlightMapActivity.class);
 		Bundle b = new Bundle();
 		b.putLong("flightId", flightId);
 		b.putString("title", flightName);
 		myIntent.putExtras(b);
 		SpaceTrackerActivity.this.startActivity(myIntent);
 
 		finish();
 	}
 
 	private void resetFlight(String s) {
 		final String type = s;
 		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				switch (which) {
 				case DialogInterface.BUTTON_POSITIVE:
 					// Yes button clicked
 					if (type.equals("reset")) {
 						resetFlight();
 					} else {
 						deleteSchedule();
 					}
 					break;
 				case DialogInterface.BUTTON_NEGATIVE:
 					// No button clicked
 					break;
 				}
 			}
 		};
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage("Are you sure you want to delete this flight?")
 				.setPositiveButton("Of Course...", dialogClickListener)
 				.setNegativeButton("No Way!", dialogClickListener).show();
 
 	}
 
 	private void deleteSchedule() {
 		this.dh = new DataHelper(this);
 		dh.deleteSingleFlight(flightId);
 		this.dh.close();
 		setupStart();
 	}
 
 	private void resetFlight() {
 		this.dh = new DataHelper(this);
 		dh.resetFlight(flightId);
 		this.dh.close();
 		setupStart();
 	}
 
 	private void getSomeDataForEmail() {
 		this.dh = new DataHelper(this);
 		flightData = dh.selectFlightData(flightId);
 		this.dh.close();
 	}
 
 	private void sendEmailKML() {
 		// this.dh = new DataHelper(this);
 
 		try {
 			File root = Environment.getExternalStorageDirectory();
 			File gpxfile = new File(root, "spacetracker.kml");
 			FileWriter writer = new FileWriter(gpxfile);
 
 			writer
 					.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n<Document>\n<name>\nSpaceTracker</name>\n<Style id=\"yellowLineGreenPoly\">\n<LineStyle>\n<color>\n7f00ffff</color>\n<width>\n4</width>\n</LineStyle>\n</Style>\n<Placemark>\n<name>\nNo name</name>\n<description>\nTrack Created by SpaceTracker</description>\n<styleUrl>\n#yellowLineGreenPoly</styleUrl>\n<LineString>\n<altitudeMode>absolute</altitudeMode>\n<extrude>1</extrude>\n<tessellate>1</tessellate>\n<coordinates>\n");
 			for (FlightData fd : flightData) {
 				writer.append(fd.getLongitude() + "," + fd.getLatitude() + ","
 						+ fd.getElevation() + "\n");
 			}
 
 			writer
 					.append("</coordinates>\n</LineString>\n</Placemark>\n</Document>\n</kml>\n");
 
 			writer.flush();
 			writer.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		Intent sendIntent = new Intent(Intent.ACTION_SEND);
 		sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Space Tracker KML");
 		sendIntent.setType("text/htm");
 		sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"
 				+ Environment.getExternalStorageDirectory()
 				+ "/spacetracker.kml"));
 		sendIntent.putExtra(Intent.EXTRA_STREAM, Uri
 				.parse("file:///sdcard/spacetracker.kml"));
 		sendIntent.putExtra(Intent.EXTRA_TEXT, "Thanks!!");
 		startActivity(Intent.createChooser(sendIntent, "Email:"));
 
 	}
 
 	private static Intent createQuickAddIntent(Context context) {
 		Intent i = new Intent(context, FlightSetupActivity.class);
 		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		Bundle b = new Bundle();
 		b.putLong("flightId", 0);
 		b.putBoolean("isNew", true);
 		i.putExtras(b);
 		return i;
 	}
 
 	/* Creates the menu items */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.preferences:
 			Intent myIntent = new Intent(SpaceTrackerActivity.this,
 					Preferences.class);
 			SpaceTrackerActivity.this.startActivity(myIntent);
 			finish();
 			break;
 		}
 		return true;
 	}
 
 }
