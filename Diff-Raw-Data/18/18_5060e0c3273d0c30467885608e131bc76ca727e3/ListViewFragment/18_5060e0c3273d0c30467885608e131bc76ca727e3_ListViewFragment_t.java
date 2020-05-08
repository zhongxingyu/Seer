 package com.bixito;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 
 import org.xml.sax.SAXException;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.util.Log;
 import android.view.View;
 import android.widget.ListView;
 
 import com.bixito.station.BikeStation;
 import com.bixito.station.StationParser;
 
 public class ListViewFragment extends ListFragment {
 
 	static private StationParser stationParser;
 	static private ArrayList<BikeStation> stationList;
 	ProgressDialog dialog;
 	ShareStationList shareStationList;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Check if the savedInstanceState is null if so, initialize it
 		if (savedInstanceState == null) {
 			Log.d("DEBUG", "savedInstanceState was null in ListViewFrag");
 			setRetainInstance(true);
 
 			// Get the bixi data to parse
 			try {
 				stationParser = new StationParser(
 						getString(R.string.bike_station_data_url),
 						((MainActivity) getActivity()).getDeviceId());
 			} catch (MalformedURLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			Log.d("DEBUG", "list view frag was created ---");
 
 			loadStationList();
 
 		}
 
 	}
 
 	public boolean loadStationList() {
 		new stationListLoader().execute();
 		return true;
 	}
 
 	private class stationListLoader extends AsyncTask<String, Void, Void> {
 
 		protected void onPreExecute() {
 			dialog = ProgressDialog.show(getActivity(),
 					getString(R.string.loading),
 					getString(R.string.station_data_loading_dialog), true);
 		}
 
 		@Override
 		protected Void doInBackground(String... arg0) {
 			try {
 				stationList = stationParser.getStationList();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (SAXException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return null;
 		}
 
 		protected void onPostExecute(Void unused) {
 			dialog.dismiss();
 			ListViewAdapter adapter = new ListViewAdapter(getActivity(),
 					stationList);
 			setListAdapter(adapter);
 			
 			// Send the list over to the map fragment via the activity
 			shareStationList.shareList(stationList);
 
 			// TODO: Make use of List_view layout
 
 		}
 
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		BikeStation selectedStation = stationList.get(position);
 		
 		//share the selected bike station via the activity
 		shareStationList.shareSelectedStation(selectedStation);
 		
 		
 		//Intent i = new Intent(getActivity(), StationDetailActivity.class);
 		//i.putExtra("com.bixito.station.BikeStation", selectedStation);
 		//startActivity(i);
 
 	}
 
 	public interface ShareStationList {
 		public void shareList(ArrayList<BikeStation> stationList);
 		
 		public void shareSelectedStation(BikeStation selectedStation);
 	}
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		try {
 			shareStationList = (ShareStationList) activity;
 		} catch (ClassCastException e) {
 			throw new ClassCastException(activity.toString()
 					+ " must implement ShareStationList");
 		}
 	}
	
	public void onHiddenChanged(boolean isHidden){
		if(!isHidden){
			//If this fragment is no longer hidden,
			//tell the action bar in the activity to update
			
		}
	
	}
 
 }
