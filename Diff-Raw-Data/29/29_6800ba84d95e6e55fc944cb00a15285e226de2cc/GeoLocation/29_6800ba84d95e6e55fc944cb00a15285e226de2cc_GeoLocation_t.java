 package com.example.mobileindia;
 
 import java.io.InputStream;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import com.google.android.maps.GeoPoint;
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 
 public class GeoLocation implements LocationListener{
 
 	private Context thisContext=null;
 	private LocationManager manager=null;
 	private String bestProvider=null;
 	private Location thisLocation=null;
 	
 	public GeoLocation(Context theContext){
 		thisContext = theContext;
 
 		manager = (LocationManager) thisContext.getSystemService(Context.LOCATION_SERVICE);
 	    Criteria criteria = new Criteria();
 	    bestProvider = manager.getBestProvider(criteria, true);
 	    registerForLocationUpdates();
 	}
 	
 	private void registerForLocationUpdates(){
 	    manager.requestLocationUpdates(bestProvider, 15000, 1, (LocationListener) this);
 	}
 	
 	
 	public Location getBestCurrentLocation(){
 		Location myLocation=null;
 	    myLocation = manager.getLastKnownLocation(bestProvider);
 	    if (myLocation == null){
 	    	myLocation = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 	    }
 	    if (myLocation != null){
 	    	System.out.println("GeoLocation is >"+myLocation.toString()+"<");
 	    	thisLocation = myLocation;
 	    }
 		return thisLocation;
 	}
 	
 	public GeoPoint getGeoPointFromName(String locationName) {
 		GeoPoint tempGeoPoint=null;
         String cleanLocationName = locationName.replaceAll(" ","%20");
         
 		HttpGet httpGet = new HttpGet(
 				"http://maps.google.com/maps/api/geocode/json?address="
 				+ cleanLocationName
 				+ "&sensor=false");
 		HttpClient client = new DefaultHttpClient();
 		HttpResponse response;
 		StringBuilder stringBuilder = new StringBuilder();
 
 		try {
 			response = client.execute(httpGet);
 			HttpEntity entity = response.getEntity();
 			InputStream stream = entity.getContent();
 			int b;
 			while ((b = stream.read()) != -1) {
 				stringBuilder.append((char) b);
 			}
 
 
 			JSONObject jsonLocation = new JSONObject();
 			jsonLocation = new JSONObject(stringBuilder.toString());
 
 			tempGeoPoint = getGeoPointFromJSON(jsonLocation);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return tempGeoPoint;	
 	}
 
 	@SuppressLint("UseValueOf")
 	private static GeoPoint getGeoPointFromJSON(JSONObject jsonObject) {
 	    GeoPoint returnGeoPoint=null;
 		try {
 			Double longitude = new Double(0);
 			Double latitude = new Double(0);
 			longitude = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
 				.getJSONObject("geometry").getJSONObject("location")
 				.getDouble("lng");
 
 			latitude = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
 				.getJSONObject("geometry").getJSONObject("location")
 				.getDouble("lat");
 			returnGeoPoint = new GeoPoint((int) (latitude * 1E6), (int) (longitude * 1E6));
 
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
         return returnGeoPoint;
 	}
 
 	
 	@Override
 	public void onLocationChanged(Location location) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
