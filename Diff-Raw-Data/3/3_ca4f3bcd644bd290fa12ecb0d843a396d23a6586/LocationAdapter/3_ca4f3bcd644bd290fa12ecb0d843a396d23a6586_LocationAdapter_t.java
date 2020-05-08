 /**
  * 
  */
 package com.faizvisram.trailblazing;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Locale;
 
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.Polyline;
 import com.google.android.gms.maps.model.PolylineOptions;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.graphics.Color;
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.Location;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 /**
  * Handles creating views for a list, as well as providing markers to the Google Map object for 
  * each location.
  * 
  * @author Faiz
  *
  */
 public class LocationAdapter extends ArrayAdapter<Location> {
 	private List<Location> mItems;
 	private List<Address> mAddresses;
 	private GoogleMap mMap;
 	private Polyline mPolyline;
 	
 	public LocationAdapter(Context context, int textViewResourceId, GoogleMap map) {
 		super(context, textViewResourceId);
 		mItems = new ArrayList<Location>();
 		mAddresses = new ArrayList<Address>();
 		mMap = map;
 	}
 
 	@Override
 	public int getCount() {
 		return mItems.size();
 	}
 
 	@Override
 	public void add(Location location) {
 		mItems.add(0, location);	// Add to the top of the stack so it's sorted by latest event first
 		LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
 		Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
 		String name = location.getLatitude() + ", " + location.getLongitude();
     	List<Address> address;
     	
     	try {
 			address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
 			if (address.size() > 0) {
 				mAddresses.add(0, address.get(0)); // we've requested only one address, so it 
 														// should only have one
 				name = address.get(0).getAddressLine(0);
 			}
 		} catch (IOException e) {
 			mAddresses.add(null); // add null so the address indicies still correspond to the locations
 			e.printStackTrace();
 		}
 		
     	mMap.addMarker(getMarker(location, name));
 		
 		if (mPolyline != null) {
 			List<LatLng> points = mPolyline.getPoints();
 			points.add(point);
 			mPolyline.setPoints(points);
 		} else if (mItems.size() > 1) {
 			Location l2 = mItems.get(1);
 			mPolyline = mMap.addPolyline(new PolylineOptions()
 					.add(point)
 					.add(new LatLng(l2.getLatitude(), l2.getLongitude()))
 					.color(Color.GREEN));
 		}
 		
 		notifyDataSetChanged();
 	}
 
 	/**
 	 * Add all Locations from the provided list to the adapter.
 	 * 
 	 * @param list	List of Locations to be added.
 	 */
 	public void addAll(List<Location> list) {
		if (list == null)
			return;
		
 		for (int i = 0; i < list.size(); i++) {
 			add(list.get(i));
 		}
 	}
 	
 	@Override
 	public Location getItem(int position) {
 		return mItems.get(position);
 	}
 	
 	@Override
 	public View getView(int position, View view, ViewGroup parent) {
 	    View v = view;
 
 	    if (v == null) {
 	        LayoutInflater li = LayoutInflater.from(getContext());
 	        v = li.inflate(R.layout.location_view, null);
 	    }
 
 	    Location location = mItems.get(position);
 
 	    if (location != null) {
 	        TextView timeText = (TextView) v.findViewById(R.id.timeText);
 	        TextView nameText = (TextView) v.findViewById(R.id.nameText);
 	        TextView detailText = (TextView) v.findViewById(R.id.detailText);
 
 	        if (timeText != null) {
 	        	timeText.setText(timestampToTime((long) location.getTime()));
 	        }
 	        
 	        if (nameText != null && detailText != null) {
 	        	String name = location.getLatitude() + ", " + location.getLongitude();
 	        	nameText.setText(name);
 	        	detailText.setText("");
 	        	    
         		Address address = mAddresses.get(position);
 
         		if (address != null) {
         			nameText.setText(address.getAddressLine(0));
         			detailText.setText(address.getAddressLine(1));
         		}
 	        }
 	    }
 
 	    return v;
 	}
 	
     @SuppressLint({ "SimpleDateFormat"})
 	public static String timestampToTime(long timestamp) {
     	Calendar today = Calendar.getInstance();
 
     	Calendar date = Calendar.getInstance();
     	date.setTimeInMillis(timestamp);	
     	
     	String text = null;
     	
     	if (today.get(Calendar.YEAR) == date.get(Calendar.YEAR) && 
     			today.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)) {	// today
     		text = "today at " + new SimpleDateFormat("h:mm a").format(date.getTime());
     	} else if (today.get(Calendar.YEAR) == date.get(Calendar.YEAR) && 
     			today.get(Calendar.DAY_OF_YEAR) -1 == date.get(Calendar.DAY_OF_YEAR)) {	// yesterday
     		text = "yesterday at " + new SimpleDateFormat("h:mm a").format(date.getTime());
     	} else if (today.get(Calendar.YEAR) == date.get(Calendar.YEAR) && 
     			today.get(Calendar.WEEK_OF_YEAR) == date.get(Calendar.WEEK_OF_YEAR)) {	// sometime this week
     		text = "on " + new SimpleDateFormat("h:mm a").format(date.getTime());
     	} else {	// not any time this week
         	text = "on " + new SimpleDateFormat("MMMM d at h:mm a").format(date.getTime());
     	}
     
     	return text.toLowerCase(Locale.getDefault());
     }
     
     private MarkerOptions getMarker(Location location, String name) {
     	MarkerOptions marker = new MarkerOptions()
 		    	.position(new LatLng(location.getLatitude(), location.getLongitude()))
 		    	.title(name)
 		    	.snippet(timestampToTime(location.getTime()));
     	
     	return marker;
     }
     
     /**
      * Get the total distance, in meters, between recorded Locations from today's date.
      * 
      * @return	The total approximate distance, in meters.
      */
     public double getTodaysDistance() {
     	double distance = 0;
     	
     	if (mItems.size() < 2)
     		return distance;
     	
     	for (int i = 0; i + 1 < mItems.size(); i++) {
     		Location l1 = mItems.get(i);
     		Location l2 = mItems.get(i + 1);
     		
     		if (isToday(l1.getTime()) && isToday(l2.getTime())) {
     			distance += l1.distanceTo(l2);
     		} else {
     			return distance;
     		}
     	}
     	
     	return distance;
     }
 
 	private boolean isToday(long time) {
 		Calendar today = Calendar.getInstance();
 
 		Calendar date = Calendar.getInstance();
     	date.setTimeInMillis(time);	
     	
 		return (today.get(Calendar.YEAR) == date.get(Calendar.YEAR) && 
     			today.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR));
 	}
     
 }
