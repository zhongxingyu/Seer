 package edu.rosehulman.server;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.osmdroid.util.GeoPoint;
 
 public class POI {
     private final int UID;
     private String name;
     private double latitude;
     private double longitude;
     private int POItype;
     private Map<String,String> attributes;
 
     public POI(JSONObject data) throws JSONException
     {
     	this.UID = data.getInt("UID"); data.remove("UID");
     	this.name = data.getString("name"); data.remove("name");
     	this.latitude = data.getDouble("latitude"); data.remove("name");
     	this.longitude = data.getDouble("longitude"); data.remove("longitude");
     	this.attributes = new HashMap<String,String>();
    	
         Iterator i = data.keys();
         while (i.hasNext()) {
             try {
                 String key = i.next().toString();
                 String value = data.getString(key);
                 this.attributes.put(key, value);
             } catch (JSONException e) {
                 e.printStackTrace();
             }
         }
    	}
 
 	public POI(int UID, String name, double latitude, double longitude, int POItype, Map<String,String> attributes)
     {
     	this.UID = UID;
     	this.name = name;
     	this.latitude = latitude;
     	this.longitude = longitude;
     	this.POItype = POItype;
     	this.attributes = new HashMap<String,String>();
     	this.attributes.putAll(attributes);
     }
 
 	public int getUID()
 	{
 		return this.UID;
 	}
 	
 	public GeoPoint getGeoPoint()
 	{
 		return new GeoPoint(this.latitude, this.longitude);
 	}
 		
     public String toString()
     {
     	String data = "POI:\n";
     	data += "\tname: " + this.name + "\n";
     	data += "\tUID: " + this.UID + "\n";
     	data += "\tlatitude: " + this.latitude + "\n";
     	data += "\tlongitude: " + this.longitude + "\n";
     	data += "\ttype: " + this.POItype + "\n";
     	for (String key : attributes.keySet())
     	{
     		data += "\tattributes(" + key + "): " + attributes.get(key) + "\n";
     	}
     	return data;
     }
 }
