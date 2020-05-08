 package com.papagiannis.tuberun;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
import android.widget.Toast;
 
 import com.papagiannis.tuberun.cyclehire.CycleHireStation;
 import com.papagiannis.tuberun.cyclehire.NearbyCyclesBinder;
 import com.papagiannis.tuberun.cyclehire.StationsCycleHireFetcher;
 import com.papagiannis.tuberun.fetchers.Observer;
 
 public class NearbyCycleStationsListFragment extends ListFragment implements
 		Observer {
 	StationsCycleHireFetcher fetcher;
 	boolean has_moved = false;
 	boolean has_moved_accurate = false;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 	
 	@Override
 	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View v=null;
 		try {
 			v=inflater.inflate(R.layout.cycle_hire_list, null);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			String s=e.toString();
 			s=s+s;
 		}
 		return v;
 	}	
 	
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		if (fetcher!=null) fetcher.abort();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 	}
 
 
 	Location lastKnownLocation;
 	public void locationChanged(Location l) {
 		lastKnownLocation = l;
 		if (fetcher == null) {
 			fetcher = new StationsCycleHireFetcher(getActivity());
 			fetcher.registerCallback(this);
 		}
 		fetcher.setLocation(lastKnownLocation);
 		fetcher.update();
 	}
 
 	ArrayList<CycleHireStation> stations_nearby;
 	ArrayList<CycleHireStation> prev_result = new ArrayList<CycleHireStation>();
 
 	/**
 	 * Called when the background thread has finished the calculation of nearby
 	 * stations
 	 **/
 	@Override
 	public void update() {
 		ArrayList<HashMap<String, Object>> to_display = new ArrayList<HashMap<String, Object>>();
 		stations_nearby = fetcher.getResult();
 		for (CycleHireStation s : stations_nearby) {
 			HashMap<String, Object> m = new HashMap<String, Object>();
 			m.put("name", s.getName());
 			m.put("distance",
 					(int) s.getLocation().distanceTo(lastKnownLocation));
 			m.put("nAvailableBikes", ""+ s.getnAvailableBikes());
 			m.put("nEmptyDocks", ""+ s.getnEmptyDocks());
 			m.put("nTotalDocks", ""+ s.getnTotalDocks());
 			to_display.add(m);
 		}
 
 		SimpleAdapter adapter = new SimpleAdapter(getActivity(), to_display,
 				R.layout.nearby_cycle_status, new String[] { "name", "distance",
 						"nAvailableBikes","nEmptyDocks","nTotalDocks"},
 				new int[] { R.id.nearby_name, R.id.nearby_distance,
 						R.id.available_bikes_textview,
 						R.id.empty_docks_textview,
 						R.id.total_bikes_textview});
 		adapter.setViewBinder(new NearbyCyclesBinder(getActivity()));
 		setListAdapter(adapter);
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		try {
 			Station s = stations_nearby.get(position);
 			Intent i = new Intent(getActivity(), DirectionsMapActivity.class);
 			i.putExtra("station", s.toString());
 			i.putExtra("type", "cyclehire");
 			i.putExtra("user_longtitude",
 					(int) (lastKnownLocation.getLongitude() * 1000000));
 			i.putExtra("user_latitude",
 					(int) (lastKnownLocation.getLatitude() * 1000000));
 			startActivity(i);
 		} catch (Exception e) {
 
 		}
 	}
	
//	public void showToast() {
//		String msg="Barclays Cycle Hire availability data is updated by TfL in 3min intervals";
//		Toast toast = Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG);
//		toast.show();
//	}
 
 		
 }
