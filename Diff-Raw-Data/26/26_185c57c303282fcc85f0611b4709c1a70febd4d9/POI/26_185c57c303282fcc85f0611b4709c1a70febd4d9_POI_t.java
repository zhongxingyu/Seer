 package arb.mportal.models;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.location.Location;
 import arb.mportal.util.IEach;
 import arb.mportal.views.DefaultPOIView;
 
 public class POI {
 	
 	private String name = "";
	private double longitude = 0;
	private double latitude = 0; 
 	private Location location = null;
 	private float distance = 0.0f;
 	private Map<String, String> tags = new HashMap<String, String>(); 
 	private DefaultPOIView view = null;
 	
 	private static List<POI> all = new ArrayList<POI>(); 
 	
 	public POI() {
 	}
 	
 	
 	public static List<POI> findAll() {
 		return all; 
 	}
 	
 	
 	public static void add(POI poi) {
 		all.add(poi); 
 	}
 	
 	
 	public static int size() {
 		return all.size(); 
 	}
 	
 	public static void clear() {
 		all.clear();  
 	}
 
 	public static POI get(int index) {
 		return all.get(index); 
 	}
 	
 	
 	public String toString() {
 		return name + " - " + latitude + " - " + longitude; 
 	}
 	
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
	public double getLongitude() {
 		return longitude;
 	}
	public void setLongitude(double longitude) {
 		this.longitude = longitude;
 	}
	public double getLatitude() {
 		return latitude;
 	}
	public void setLatitude(double latitude) {
 		this.latitude = latitude;
 	}
 	public DefaultPOIView getView() {
 		return view;
 	}
 	public void setView(DefaultPOIView view) {
 		this.view = view;
 	}
 	public void addTag(String key, String value) {
 		tags.put(key, value);  
 	}
 	public Map<String, String> getTags() {
 		return tags;  
 	}
 	public void setDistance(float distance) {
 		this.distance = distance;
 	}
 	public float getDistance() {
 		return distance; 
 	}
 	public Location getLocation()  {
 		Location l = new Location("");
 		l.setLatitude(getLatitude());
 		l.setLongitude(getLongitude()); 
 		return l; 
 	}
 	
 	public String getTagStreet() {
 		return getTags().get("addr:street");
 	}
 	public String getTagHousenumber() {
 		return getTags().get("addr:housenumber");
 	}
 	public String getTagPostcode() {
 		return getTags().get("addr:postcode"); 
 	}
 	public String getTagURL() {
 		return getTags().get("url:official");
 	}
 	public String getTagDescription() {
 		return getTags().get("description"); 
 	}
 	
 	
 	public String getDescription() {
 		StringBuffer buf = new StringBuffer();
 		int lines = 5; 
 		if(getTagStreet() != null) {
 			buf.append(getTagStreet());
 		}
 		if(getTagHousenumber() != null) {
 			buf.append(" " + getTagHousenumber());
 		}
 		if(buf.toString().equals("") == false) {
 			buf.append("\n");
 			lines--;
 		}
 		if(getTagPostcode() != null) { 
 			buf.append(getTagPostcode() + "\n");
 			lines--;
 		}
 		if(getTagURL() != null && !getTagURL().equals("null") && !getTagURL().equals("")) { 
 			buf.append(getTags().get("url:official") + "\n");
 			lines--;
 		}
 		if(getTagDescription() != null && !getTagDescription().equals("null") && !getTagDescription().equals("")) {
 			buf.append(getTags().get("description") + "\n");   
 		}  
 		return buf.toString();
 	}
 	
 	
 	public static void eachPoi(IEach each) {
     	int i = 1; 
     	for(POI p : all) { 
     		each.each(p, i); 
     		i++; 
     	}		
 	}
 
 
 	public static void hideAll() {
 		POI.eachPoi(new IEach() {
 			public void each(Object item, int index) {
 				POI poi = (POI)item;
 				poi.getView().closeLayer();
 			}
 		});
 	}
 	
 	public String getTitle() {
 		String title = name;
 		if(title.equals(""))
 			title = getTags().get("amenity");
 		if(title.length() >= 15) {
 			return title.substring(0, 13) + "...";
 		}
 		return title;
 		  
 	}
 
 	
 }
