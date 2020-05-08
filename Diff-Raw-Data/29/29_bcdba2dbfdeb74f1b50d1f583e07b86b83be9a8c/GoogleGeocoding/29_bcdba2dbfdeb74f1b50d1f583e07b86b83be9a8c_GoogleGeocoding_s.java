 package uk.frequency.glance.server.business.remote;
 
 import java.util.List;
 
 import uk.frequency.glance.server.model.component.Position;
 
 import com.google.code.geocoder.Geocoder;
 import com.google.code.geocoder.GeocoderRequestBuilder;
 import com.google.code.geocoder.model.GeocodeResponse;
 import com.google.code.geocoder.model.GeocoderAddressComponent;
 import com.google.code.geocoder.model.GeocoderRequest;
 import com.google.code.geocoder.model.GeocoderResult;
 import com.google.code.geocoder.model.GeocoderStatus;
 import com.google.code.geocoder.model.LatLng;
 
 public class GoogleGeocoding {
 
 	static final String rootUrl = "http://maps.googleapis.com/maps/api/geocode/json";
 	
	static final String imageSize = EventDataFinder.imageSize;
 	static final String key = EventDataFinder.googleAPIsKey;
 	
 	public static String getAddress(Position pos){
 		Geocoder geo = new Geocoder(); //TODO create clientId? https://developers.google.com/maps/documentation/business/guide
 		LatLng latlng = new LatLng(""+pos.getLat(), ""+pos.getLng());
 		GeocoderRequest req = new GeocoderRequestBuilder().setLocation(latlng).getGeocoderRequest();
 		GeocodeResponse res = geo.geocode(req);
 		
 		GeocoderStatus status = res.getStatus();
 		if (!status.equals(GeocoderStatus.OK)){
 			System.err.println("geocoding api returned status: " + status);
 			return "Somewhere"; //TODO
 		}
 		
 		String address = res.getResults().get(0).getFormattedAddress();
 		return address;
 	}
 	
 	// TODO get from google places
 	public static String getLocationName(Position pos) {
 		Geocoder geo = new Geocoder(); // TODO create clientId? https://developers.google.com/maps/documentation/business/guide
 		LatLng latlng = new LatLng("" + pos.getLat(), "" + pos.getLng());
 		GeocoderRequest req = new GeocoderRequestBuilder().setLocation(latlng).getGeocoderRequest();
 		GeocodeResponse res = geo.geocode(req);
 
 		GeocoderStatus status = res.getStatus();
 		if (!status.equals(GeocoderStatus.OK)){
 			System.err.println("geocoding api returned status: " + status);
 			return "Somewhere"; //TODO
 		}
 
 		String[] preferedTypesInOrder = {"street_address", "route", "locality", "political", "administrative_area_level_2", "administrative_area_level_2", "country"};  
 		for(String preferedType :  preferedTypesInOrder){
 			for(GeocoderResult result : res.getResults()){
 				for(GeocoderAddressComponent comp : result.getAddressComponents()){
 					List<String> types = comp.getTypes();
 					if(types.contains(preferedType)){
 						return comp.getLongName();
 					}
 				}
 			}
 		}
 		
 		return res.getResults().get(0).getFormattedAddress();
 	}
 	
 	
 
 }
