 package com.bbaumgar.net;
 
 import java.io.InputStream;
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.View;
 import android.widget.TabHost.TabContentFactory;
 
 import com.google.gson.Gson;
 
 public class Utils {
 
 	public static final String server_url = "https://data-collection.firebaseIO.com/incoming.json";
 
 	public static String constructRestUrlForSendData() {
 		return server_url;
 	}
 	
 	public static String serializeSendData(double lat, double lon, int strength) {
 		Gson gson = new Gson();
 		String requestBody = gson.toJson(new Utils.LocationDataObject(lat, lon, strength));
 		return requestBody;
 	}
 	
 	public static class LocationDataObject {
 		public CoordinateObject location;
 		public int strength;
 		public LocationDataObject(double lat, double lon, int str) {
 			location = new CoordinateObject(lat, lon);
 			this.strength = str;
 		}
 		public class CoordinateObject {
 			double lat;
 			double lon;
 			public CoordinateObject(double lat, double lon) {
 				this.lat = lat;
 				this.lon = lon;
 			}
 		}
 	}
 	
 	public static String convertStreamToString(java.io.InputStream is) {
		if(is != null) {
		    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		    return s.hasNext() ? s.next() : "";
		}
		return "";
 	}
 }
