 package com.papagiannis.tuberun;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Address;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.SearchRecentSuggestions;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 
 import com.papagiannis.tuberun.binders.SelectLinesBinder;
 import com.papagiannis.tuberun.fetchers.Observer;
 import com.papagiannis.tuberun.fetchers.ReverseGeocodeFetcher;
 import com.papagiannis.tuberun.fetchers.StationsTubeFetcher;
 
 public class SelectLineActivity extends ListActivity implements
 		OnClickListener, LocationListener, Observer {
 	private static final int FAILED_DIALOG = 1;
 	private static final String VIEW = "android.Intent.action.VIEW";
 
 	protected Button backButton;
 	protected Button logoButton;
 	protected Button searchButton;
 	protected EditText searchEditText;
 	TextView locationTextview;
 	TextView locationAccuracyTextview;
 	LinearLayout locationLayout;
 	ProgressBar locationProgressbar;
 
 	private final ArrayList<HashMap<String, Object>> lines_list = new ArrayList<HashMap<String, Object>>();
 	ArrayList<Station> stationsList = new ArrayList<Station>();
 
 	private LocationManager locationManager;
 	ReverseGeocodeFetcher geocoder = new ReverseGeocodeFetcher(this, null);
 	Observer geolocationObserver = new Observer() {
 		@Override
 		public void update() {
 			displayLocation(geocoder.getResult());
 		}
 	};
 	private StationsTubeFetcher fetcher = new StationsTubeFetcher(this);
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.select_line);
 		fetcher.registerCallback(this);
 
 		locationTextview = (TextView) findViewById(R.id.location_textview);
 		locationAccuracyTextview = (TextView) findViewById(R.id.location_accuracy_textview);
 		locationProgressbar = (ProgressBar) findViewById(R.id.location_progressbar);
 		locationLayout = (LinearLayout) findViewById(R.id.location_layout);
 		
 		backButton = (Button) findViewById(R.id.back_button);
 		logoButton = (Button) findViewById(R.id.logo_button);
 		backButton.setOnClickListener(this);
 		logoButton.setOnClickListener(this);
 
 		searchButton = (Button) findViewById(R.id.search_button);
 		searchButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				onSearchRequested();
 			}
 		});
 
 		Intent intent = getIntent();
 		if (VIEW.equals(intent.getAction())) {
 			handleIntent(intent);
 			finish();
 			return;
 		}
 
 		Iterable<LineType> lines = LineType.allDepartures();
 		for (LineType lt : lines) {
 			HashMap<String, Object> m = new HashMap<String, Object>();
 			m.put("line_name", LinePresentation.getStringRespresentation(lt));
 			m.put("line_color", lt);
 			Integer image = -1;
 			if (lt.equals(LineType.BUSES)) {
 				image = R.drawable.buses_inverted;
 			}
 			m.put("line_image", image);
 			m.put("line_more", true);
 			lines_list.add(m);
 		}
 		
 		populate(new ArrayList<Station>());
 
 		locationManager = (LocationManager) this
 				.getSystemService(Context.LOCATION_SERVICE);
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		if (locationManager != null)
 			locationManager.removeUpdates(this);
 		fetcher.abort();
 		fetcher.deregisterCallback(this);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		if (locationManager != null) {
 			requestLocationUpdates();
 			fetcher.registerCallback(this);
 		}
 	}
 
 	// LocationListener Methods
 	private Location lastKnownLocation;
 
 	@Override
 	public void onLocationChanged(Location l) {
 		if (SelectBusStationActivity.isBetterLocation(l, lastKnownLocation)) {
 			lastKnownLocation = l;
 			fetcher.setLocation(l);
 			fetcher.update();
 			reverseGeocode(l);
 		}
 	}
 
 	private void requestLocationUpdates() {
 		try {
 			if (locationManager != null) {
 				locationManager.requestLocationUpdates(
 						LocationManager.NETWORK_PROVIDER, 10 * 1000, 35, this);
 				locationManager.requestLocationUpdates(
 						LocationManager.GPS_PROVIDER, 10 * 1000, 35, this);
 			}
 		} catch (Exception e) {
 			Log.w("LocationService", e);
 		}
 	}
 
 	@Override
 	public void update() {
 		stationsList = fetcher.getResult();
 		populate(stationsList);
 	}
 
 	private ArrayList<Station> nearbyPrevious=new ArrayList<Station>();
 	private void populate(ArrayList<Station> nearby) {
 		if (nearby.size()>0) hasNearby=true;
		if (nearbyPrevious.size()!=0 && nearbyPrevious.equals(nearby)) return;
 		ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
 
 		for (Station s : nearby) {
 			HashMap<String, Object> m = new HashMap<String, Object>();
 			m.put("line_name", s.getName());
 			m.put("line_color", null);
 			m.put("line_image", s.getIcon());
 			m.put("line_more", false);
 			list.add(m);
 		}
 
 		list.addAll(lines_list);
 
 		SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.line,
 				new String[] { "line_name", "line_color", "line_image", "line_more" },
 				new int[] { R.id.line_name, R.id.line_color, R.id.line_image, R.id.line_more });
 		adapter.setViewBinder(new SelectLinesBinder(this));
 		setListAdapter(adapter);
 	}
 
 	@Override
 	protected void onNewIntent(Intent intent) {
 		try {
 			setIntent(intent);
 			handleIntent(intent);
 		}
 		catch (Exception e) {
 			Log.w("SelectLineActivity",e);
 		}
 	}
 
 	@SuppressWarnings("deprecation")
 	private void handleIntent(Intent intent) {
 		if (VIEW.equals(intent.getAction())) {
 			//store the query as a future suggestion
 			String query = intent.getData().toString();
 			SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
 					this, StationsProvider.AUTHORITY, StationsProvider.MODE);
 			suggestions.saveRecentQuery(query, null);
 			
 			//and launch the new activity
 			Uri data = intent.getData();
 			char first=data.toString().charAt(0);
 			if (first>='0' && first <='9') {
 				String[] tokens=data.toString().split("_");
 				if (tokens.length<2) showDialog(FAILED_DIALOG);
 				else startBusDepartures(tokens[1], tokens[0]);
 			}
 			else {
 				String[] tokens=data.toString().split("_");
 				if (tokens.length<2) showDialog(FAILED_DIALOG);
 				Station s=new Station(tokens[0],tokens[1]);
 				startDepartures(s);
 			}
 		}
 	}
 	
 	private boolean hasNearby=false;
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		try {
 			if (!hasNearby || position>=6) {
 				if (hasNearby && position>=6) position -= 6;
 				// display a l2ist of stations
 				String line_name = (String) lines_list.get(position).get(
 						"line_name");
 				Intent i = null;
 				if (line_name.equals(LinePresentation
 						.getStringRespresentation(LineType.BUSES))) {
 					i = new Intent(this, SelectBusStationActivity.class);
 				} else {
 					i = new Intent(this, SelectStationActivity.class);
 					i.putExtra("line", line_name);
 					i.putExtra("type", "departures");
 				}
 				startActivity(i);
 			} else {
 				// jump to departures
 				Station s = stationsList.get(position);
 				startDepartures(s);
 			}
 
 		} catch (Exception e) {
 			Log.w("SelectLine", e);
 		}
 
 	}
 
 	private void startDepartures(Station s) {
 		Intent i = null;
 		i = new Intent(this, DeparturesActivity.class);
 		i.putExtra("type", "station");
 		i.putExtra("station", s);
 		startActivity(i);
 	}
 
 	private void startBusDepartures(String name, String code) {
 		Intent i = new Intent(this, BusDeparturesActivity.class);
 		i.putExtra("code", code);
 		i.putExtra("name", name);
 		startActivity(i);
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		Dialog result = null;
 		switch (id) {
 		case FAILED_DIALOG:
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setTitle("Internal Error")
 					.setMessage("Please try another stop/station.")
 					.setCancelable(true);
 			result = builder.create();
 			break;
 		}
 		return result;
 
 	}
 
 	@Override
 	public void onClick(View v) {
 		finish();
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 	}
 	
 	private void displayLocation(List<Address> result) {
 		if (result == null || result.size() < 1) {
 			locationTextview.setText("");
 			locationAccuracyTextview.setText(("accuracy="
 					+ lastKnownLocation.getAccuracy() + "m"));
 		} else {
 			String geoc_result = result.get(0).getAddressLine(0);
 			locationTextview.setText(geoc_result);
 			locationAccuracyTextview.setText("accuracy="
 					+ lastKnownLocation.getAccuracy() + "m");
 		}
 	}
 
 	private void reverseGeocode(Location l) {
 		geocoder.abort();
 		geocoder = new ReverseGeocodeFetcher(this, l);
 		geocoder.registerCallback(geolocationObserver).update();
 	}
 
 }
