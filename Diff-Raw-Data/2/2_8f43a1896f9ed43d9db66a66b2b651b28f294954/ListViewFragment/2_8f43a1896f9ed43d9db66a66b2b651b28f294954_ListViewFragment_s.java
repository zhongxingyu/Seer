 package com.bixito;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 
 import org.xml.sax.SAXException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 import android.widget.SearchView;
 
 import com.actionbarsherlock.app.SherlockListFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.bixito.station.BikeStation;
 import com.bixito.station.StationParser;
 
 public class ListViewFragment extends SherlockListFragment implements SearchView.OnQueryTextListener {
 
 	static private StationParser stationParser;
 	static private ArrayList<BikeStation> stationList;
 	ProgressDialog dialog;
 	ListViewAdapter adapter;
 
 	ShareStationList shareStationList;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setHasOptionsMenu(true);
 
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
 	
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View view = inflater.inflate(R.layout.list_view, container, false);
 
         return view;
     }   
 
 	public boolean loadStationList() {
 		//Check to make sure internet connection is available
 		if(!isNetworkAvailable(getActivity())){
 			Log.d("DEBUG", "no network connection found.");
 			Builder b = new AlertDialog.Builder(getActivity());
 			b.setMessage(getString(R.string.error_no_network_connection_error));
 			b.setCancelable(false);
 			b.setPositiveButton(getString(R.string.error_no_network_connection_okay_button), 
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							getActivity().finish();
 						}
 					});
 			AlertDialog dialog = b.create();
 			dialog.show();
 			return false;
 		}
 		else{
 			new stationListLoader().execute();
 			return true;
 		}
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
 				Log.e("ERROR", e.getMessage());
 			} catch (SAXException e) {
 				// TODO Auto-generated catch block
 				Log.e("ERROR", e.getMessage());
 			} 
 			return null;
 		}
 
 		protected void onPostExecute(Void unused) {
 			dialog.dismiss();
 			try{
 				adapter = new ListViewAdapter(getActivity(), stationList);
 				setListAdapter(adapter);
 				
 				// Send the list over to the map fragment via the activity
 				shareStationList.shareList(stationList);
 
 			} catch (NullPointerException e){
 				Log.e("ERROR", "exception", e);
 
 				Builder b = new AlertDialog.Builder(getActivity());
 				b.setMessage(getString(R.string.error_loading_list_error));
 				b.setCancelable(false);
 				b.setPositiveButton(getString(R.string.error_loading_list_okay_button), 
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int which) {
 								getActivity().finish();
 							}
 						});
 				AlertDialog dialog = b.create();
 				dialog.show();
 			}
 			
 
 
 		}
 
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
		BikeStation selectedStation = stationList.get(position);
 		
 		//share the selected bike station via the activity
 		shareStationList.shareSelectedStation(selectedStation);
 
 	}
 
 	public interface ShareStationList {
 		public void shareList(ArrayList<BikeStation> stationList);
 		
 		public void shareSelectedStation(BikeStation selectedStation);
 		
 		public void selectMapTab();
 		
 		public void selectListTab();
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
 		if(isHidden){
 			//If this fragment has just been hidden, select the Map tab
 			shareStationList.selectMapTab();
 		}else{
 			//If this fragment has just been shown, select the List tab
 			shareStationList.selectListTab();
 			
 			//reset filter
 			adapter.getFilter().filter("");
 		}
 		
 	}
 	
 	public boolean isNetworkAvailable(Context context) {
 
 	    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
 	    
 	   
 	    if(cm.getActiveNetworkInfo() == null)
 	    	return false;
 	    if(cm.getActiveNetworkInfo().getState() == NetworkInfo.State.CONNECTED)
 	    	return true;
 	    else
 	    	return false;
 	    
 	}
 
 	@Override
 	public boolean onQueryTextChange(String query) {
 		adapter.getFilter().filter(query);
 		return false;
 	}
 
 	@Override
 	public boolean onQueryTextSubmit(String arg0) {
 		return false;
 	}
 	
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		inflater.inflate(R.menu.list_view_menu, menu);
 		
 		MenuItem searchItem = menu.findItem(R.id.action_search);
 		SearchView searchView = (SearchView) searchItem.getActionView();
 		
 		searchView.setOnQueryTextListener(this);
 		
 	}
 
 }
