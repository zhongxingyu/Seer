 /**
  * 
  */
 package com.ilikeblues.android.pathTracker.gps;
 
 import java.text.DecimalFormat;
 import java.util.List;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.Location;
 import android.util.Log;
 
 /**
  * @author ilikeblues
  *
  */
 public class LocationAddress extends Location {
 
 	private static Object sobj = new Object();
 	
 	private static final String EMPTY_STRING = "";
 	private String localizedAddress = EMPTY_STRING;
 
 	private Activity parent = null;
 	/**
 	 * @param provider
 	 */
 	public LocationAddress(String provider, Activity parent) {
 		super(provider);
 		
 		this.parent = parent;
 	}
 
 // TODO UCdetector: Remove unused code: 
 // 	/**
 // 	 * @param l
 // 	 */
 // 	public LocationAddress(Location l, Activity parent) {
 // 		super(l);
 // 		
 // 		this.parent = parent;
 // 	}
 
 	@Override
 	public void setLatitude(double latitude) {
 		super.setLatitude(latitude);
 		// getAddressAsString();
 	}
 	
 	@Override
 	public void setLongitude(double longitud) {
 		super.setLongitude(longitud);
 		// getAddressAsString();
 	}
 	
 	public void setLatLon(double latitude, double longitud) {
 		super.setLatitude(latitude);
 		super.setLongitude(longitud);
 		
 		// getAddressAsString();
 	}
 	
 	@Override
 	public void set(Location l) {
 		super.set(l);
 		// getAddressAsString();
 	}
 	
 	public void getAddressAsString() {
 		String locationText;
 		Geocoder gcd = new Geocoder(parent, Locale.getDefault());
 		
 		List<Address> addresses;
 		try {
 			// addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
 			double lat = getLatitude(); // - 3.6979293823242188;
 			double lon = getLongitude(); // + 40.41388815007209;
 			
 			addresses = gcd.getFromLocation(lat, lon, 10);
 			if (addresses.size() > 0) {
 				Address address = addresses.get(0);
 				
 				locationText = address.getAddressLine(0);;
 				for (int i = 1;i < address.getMaxAddressLineIndex();i++) {
 					locationText += ", ";
 					locationText += address.getAddressLine(i);
 				}
 			} else {
 				Log.e("LOCATION-ADDRESS", "Geocoder NO devuelve ninguna ADDRESS");
 				locationText = EMPTY_STRING;
 			}
 		} catch (Exception e) {
 			Log.e("LOCATION-ADDRESS", e.getLocalizedMessage());
 			Log.e("LOCATION-ADDRESS", "Geocoder NO devuelve ninguna ADDRESS");
 			locationText = EMPTY_STRING;
 		}        		
 		
 		synchronized(sobj) {
 			setLocalizedAddress(locationText);
 		}
 
 	}
 	
 	public String getArrangedAddress() {
 		String address;
 		synchronized(sobj) {
 			if (EMPTY_STRING.compareTo(localizedAddress) == 0) {
 					DecimalFormat lf = new DecimalFormat("000.00");
 					address = "lat: " + lf.format(getLatitude()) + "\nlon: " + lf.format(getLongitude());
 			} else {
 				address = localizedAddress;
 			}
 		}
 		
 		return address;
 	}
 
 	public String getLocalizedAddress() {
 		String address;
 		synchronized(sobj) {
 			address = localizedAddress;
 		}
 		
 		return address;
 	}
 
	private void setLocalizedAddress(String localizedAddress) {
 		this.localizedAddress = localizedAddress;
 		
 		Log.e("ADDRESS set TO:", this.localizedAddress);
 	}
 }
