 package edu.illinois.geosight.servercom;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.json.JSONException;
 
 import com.google.android.maps.GeoPoint;
 
 public class Sight {
 	protected long id;
 	protected long user_id;
 	
 	protected String name;
 	protected double radius;
 	protected Date created_at;
 	protected Date updated_at;
 	protected GeoPoint location;
 	
 	public Sight(long id) throws GeosightException {
 		
 		GeosightEntity sight = GeosightEntity.jsonFromGet( String.format("/sights/%d.json", id) );
 		
 		try {
 			populate( sight );
 		} catch (JSONException e) {
 			throw new GeosightException(e);
 		} catch (java.text.ParseException e) {
 			throw new GeosightException(e);
 		}
 	}
 	
 	protected Sight(GeosightEntity sight) throws GeosightException{
 		try {
 			populate(sight);
 		} catch (JSONException e) {
 			throw new GeosightException(e);
 		} catch (java.text.ParseException e) {
 			throw new GeosightException(e);
 		}
 	}
 	
 	public static List<Sight> getAllSights() throws GeosightException{
 		List<Sight> sights = new ArrayList<Sight>();
 		List<GeosightEntity> allSights = GeosightEntity.jsonArrayFromGet("/sights.json");
 
 			
 		for(int i=0;i<allSights.size();i++){
			try {
				sights.add( new Sight( allSights.get(i).getObject("sight") ) );
			} catch (JSONException e) {
				continue;
			}
 		}
 		
 		return sights;
 	}
 
 	public String toString(){
 		return name;
 	}
 	
 	private void populate(GeosightEntity sight) throws JSONException, java.text.ParseException{
 		id = sight.getLong("id");
 		user_id = sight.getLong("user_id");
 		name = sight.getString("name");
 		radius = sight.getDouble("radius");
 		created_at  = sight.getDate("created_at");
 		updated_at = sight.getDate("updated_at");
 		location = sight.getGeoPoint("latitude", "longitude");
 	}
 	
 	public String getName(){
 		return name;
 	}
 
 	public GeoPoint getLocation() {
 		return location;
 	}
 
 	public int getLatitudeE6() {
 		return location.getLatitudeE6();
 	}
 
 	public int getLongitudeE6() {
 		return location.getLongitudeE6();
 	}
 }
