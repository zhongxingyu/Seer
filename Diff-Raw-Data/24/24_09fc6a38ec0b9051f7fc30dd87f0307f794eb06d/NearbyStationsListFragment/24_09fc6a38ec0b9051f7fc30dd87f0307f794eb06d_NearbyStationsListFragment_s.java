 package com.papagiannis.tuberun;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Set;
 
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.ListFragment;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 
 import com.papagiannis.tuberun.binders.NearbyBinder;
 import com.papagiannis.tuberun.fetchers.Observer;
 import com.papagiannis.tuberun.fetchers.StationsTubeFetcher;
 
 public class NearbyStationsListFragment extends ListFragment implements
 		Observer {
 	StationsTubeFetcher fetcher;
 	boolean has_moved = false;
 	boolean has_moved_accurate = false;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		fetcher = new StationsTubeFetcher(getActivity());
 		fetcher.registerCallback(this);
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		fetcher.abort();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 	}
 
 	// private Dialog wait_dialog;
 	// @Override
 	// protected Dialog onCreateDialog(int id) {
 	// wait_dialog = ProgressDialog.show(this, "",
 	// "Fetching location. Please wait...", true);
 	// return wait_dialog;
 	// }
 
 	Location lastKnownLocation;
 
 	public void locationChanged(Location l) {
 		lastKnownLocation = l;
 		if (fetcher == null) {
 			fetcher = new StationsTubeFetcher(getActivity());
 			fetcher.registerCallback(this);
 		}
 		fetcher.setLocation(lastKnownLocation);
 		fetcher.update();
 	}
 
 	ArrayList<Station> stations_nearby;
 	ArrayList<BusStation> prev_result = new ArrayList<BusStation>();
 
 	/**
 	 * Called when the background thread has finished the calculation of nearby
 	 * stations
 	 **/
 	@Override
 	public void update() {
 		TextView accuracy = (TextView) getActivity().findViewById(
 				R.id.location_accuracy_textview);
 		if (accuracy!=null)
 		accuracy.setText("accuracy: " + (int) (lastKnownLocation.getAccuracy())
 				+ "m");
 
 		ArrayList<HashMap<String, Object>> to_display = new ArrayList<HashMap<String, Object>>();
 
 		stations_nearby = fetcher.getResult();
 		for (Station s : stations_nearby) {
 			HashMap<String, Object> m = new HashMap<String, Object>();
 			m.put("name", s.getName());
 			m.put("distance",
 					(int) s.getLocation().distanceTo(lastKnownLocation));
 			ArrayList<LineType> lines = StationDetails.FetchLinesForStation(s
 					.getName());
 			for (LineType lt : lines) {
 				String line = LinePresentation.getStringRespresentation(lt);
 				m.put(line, line);
 			}
 			Set<String> existing = m.keySet();
 			for (String line : LinePresentation.getLinesStringList()) {
 				if (!existing.contains(line))
 					m.put(line, "");
 			}
 			to_display.add(m);
 		}
 
 		SimpleAdapter adapter = new SimpleAdapter(getActivity(), to_display,
 				R.layout.nearby_status, new String[] { "name", "distance",
						"Barkerloo", "Central", "Circle", "District", "DLR",
						"Hammersmith", "Jubilee", "Metropolitan", "Northern",
						"Overground", "Piccadily", "Victoria", "Waterloo" },
 				new int[] { R.id.nearby_name, R.id.nearby_distance,
 						R.id.nearby_Bakerloo, R.id.nearby_Central,
 						R.id.nearby_Circle, R.id.nearby_District,
 						R.id.nearby_DLR, R.id.nearby_Hammersmith,
 						R.id.nearby_Jubilee, R.id.nearby_Metropolitan,
 						R.id.nearby_Northern, R.id.nearby_Overground,
 						R.id.nearby_Piccadily, R.id.nearby_Victoria,
 						R.id.nearby_Waterloo });
 		adapter.setViewBinder(new NearbyBinder(getActivity()));
 		setListAdapter(adapter);
 
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		try {
 			Station s = stations_nearby.get(position);
 			Intent i = new Intent(getActivity(), DirectionsMapActivity.class);
 			i.putExtra("station", s.toString());
 			i.putExtra("user_longtitude",
 					(int) (lastKnownLocation.getLongitude() * 1000000));
 			i.putExtra("user_latitude",
 					(int) (lastKnownLocation.getLatitude() * 1000000));
 			startActivity(i);
 		} catch (Exception e) {
 
 		}
 	}
 
 }
