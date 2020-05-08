 package com.bixito;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Filter;
 import android.widget.TextView;
 
 import com.bixito.station.BikeStation;
 
 public class ListViewAdapter extends BaseAdapter {
 
 	private Context context;
 	private ArrayList<BikeStation> bikeStationsToDisplay;
 	private ArrayList<BikeStation> bikeStationsOriginal;
 	
 	static final Long updateFreq = 1000L; //Every 1000 ms
 	static final Float updateDist = 10F; //10 Meters
 	
 	Location userNetworkLocation = null, userGpsLocation = null;
 	
 	
 	public ListViewAdapter(Context context, ArrayList<BikeStation> bStations){
 		//save the current activity context		
 		this.context = context;
 		
 		//variable to store the user's location
 		Location userLocation = null;
 
 		//get user's location
 		getUserLocation();
 		
 		//check which location provider is available, prioritize network provider first
 		if (userNetworkLocation != null)
 			userLocation = userNetworkLocation;
 		else if (userGpsLocation != null)
 			userLocation = userGpsLocation;
 		
 		//double check if the user location was available, if not - sort list alphabetically
 		if (userLocation != null){
 			//run the calculateDistanceFromUser() function for each BikeStation, using the user's current location
 			for (BikeStation b: bStations){
 				b.calculateDistanceFromUser(userLocation.getLatitude(), userLocation.getLongitude());
 			}
 			
 			//sort the BikeStation's by distance
 			java.util.Collections.sort(bStations, new MyDistanceComparator());
 			
 			
 			// ** will probably be used in the near future **
 			//sortStationsByDistance();
 		}
 		else{
 			//if no userLocation available, sort the BikeStation's by name
 			java.util.Collections.sort(bStations, new MyNameComparator());
 			
 			// ** will probably be used in the near future **
 			//sortStationsByName();
 		}
 		
 		
 		this.bikeStationsOriginal = (ArrayList<BikeStation>) bStations.clone();
 		this.bikeStationsToDisplay = bStations;
 	}
 	
 	
 	/**
 	 * Comparator for sorting BikeStation objects by distance
 	 */
 	private class MyDistanceComparator implements Comparator<BikeStation> {
 	    @Override
 	    public int compare(BikeStation o1, BikeStation o2) {
 	    	if (o1.getAbsoluteDistanceFromUser() > o2.getAbsoluteDistanceFromUser()) return 1;
 	    	if (o1.getAbsoluteDistanceFromUser() < o2.getAbsoluteDistanceFromUser()) return -1;
 	    	return 0;
 	    }
 	}
 	
 	/**
 	 * Comparator for sorting BikeStation objects alphabetically
 	 */
 	private class MyNameComparator implements Comparator<BikeStation> {
 	    @Override
 	    public int compare(BikeStation o1, BikeStation o2) {
 	    	if (o1.getAbsoluteDistanceFromUser() > o2.getAbsoluteDistanceFromUser()) return 1;
 	    	if (o1.getAbsoluteDistanceFromUser() < o2.getAbsoluteDistanceFromUser()) return -1;
 	    	return 0;
 	    }
 	}
 	
 	/**
 	 * Retrieves the user's location - used when sorting by distance
 	 */
 	private void getUserLocation(){
 		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
 		
 		if(locationManager != null){
 			boolean gpsIsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
 			boolean networkLocationIsEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
 			
 			
 			if(networkLocationIsEnabled){
 				//Locate user based on their network (less accurate)
 	            userNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 			}
 			else if(gpsIsEnabled){
 				//Use gps to locate the user
 				userGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 			}
 			else{
 				//TODO Show dialog telling user to enable location services
 				Log.d("DEBUG", "Location services not enabled.");
 			}
 		}
 		else{
 			//TODO Show dialog if something goes terribly wrong here with Location Manager
 			Log.d("DEBUG", "Something went wrong with location manager.");
 		}
 	}
 	
 	
 	/* methods to be implemented when custom ordering is available
 	public void sortStationsByDistance(){
 		//if no userLocation available, sort the BikeStation's by name
 		java.util.Collections.sort(bStations, new MyNameComparator());
 		
 		this.bikeStationsOriginal = (ArrayList<BikeStation>) bStations.clone();
 		this.bikeStationsToDisplay = bStations;
 	}
 	
 	public void sortStationsByName(){
 		//if no userLocation available, sort the BikeStation's by name
 		java.util.Collections.sort(bStations, new MyNameComparator());
 
 		this.bikeStationsOriginal = (ArrayList<BikeStation>) bStations.clone();
 		this.bikeStationsToDisplay = bStations;
 	}
 	*/
 	
 	
 	/**
 	 * Returns the number of bike stations.
 	 */
 	public int getCount() {
 		return bikeStationsToDisplay.size();
 	}
 
 	public Object getItem(int position) {
 		return bikeStationsToDisplay.get(position);
 	}
 
 	public long getItemId(int position) {
 		return position;
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
 		BikeStation station = bikeStationsToDisplay.get(position);
 		
 		//If the view for the item hasn't been created yet, create it
 		if(convertView == null){
 			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			convertView = inflater.inflate(R.layout.list_item, null);
 		}
 
 		//Display data
 		TextView text1 = (TextView) convertView.findViewById(R.id.text1);
 		text1.setText(station.getStationName());
 		
 		TextView text2 = (TextView) convertView.findViewById(R.id.text2);
 		text2.setText("Bikes: " + Integer.toString(station.getNbBikes()) + 
 				" Empty: " + Integer.toString(station.getNbEmptyDocks()) );
 		
 		
 		return convertView;
 		
 	}
 
 	
 	public Filter getFilter(){
 		return new Filter(){
 
 			@Override
 			protected FilterResults performFiltering(CharSequence searchString) {
 				FilterResults results = new FilterResults();
 				ArrayList<BikeStation> stationResults = new ArrayList<BikeStation>();
 				
 				if(searchString == null){
 					results.values = bikeStationsToDisplay;
 				}
 				else{
 					for(BikeStation s : bikeStationsOriginal){
						if(s.getStationName().toLowerCase().contains(searchString.toString().toLowerCase()))
 							stationResults.add(s);
 					}
 					results.values = stationResults;
 					return results;
 				}
 				
 				return null;
 			}
 
 			@Override
 			protected void publishResults(CharSequence searchString, FilterResults results) {
 				bikeStationsToDisplay = (ArrayList<BikeStation>) results.values;
 				notifyDataSetChanged();
 				
 			}
 			
 		};
 	}
 	
 }
