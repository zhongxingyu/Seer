 package uk.frequency.glance.server.business.remote;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import uk.frequency.glance.server.model.component.Location;
 import uk.frequency.glance.server.model.component.Position;
 import static uk.frequency.glance.server.business.remote.GooglePlacesModel.*;
 import static uk.frequency.glance.server.business.remote.GooglePlacesModel.Results.Status.*;
 
 @SuppressWarnings("unused")
 public class GooglePlaces {
 
 	static final String rootUrl = "https://maps.googleapis.com/maps/api/place/";
 	
 	static final String key = EventDataFinder.googleAPIsKey;
 	
 	private RemoteAPIClient client = new RemoteAPIClient(rootUrl);
 	
 	private Position requestedPosition;
 	private List<Place> results;
 	
 	public GooglePlaces(Position position, int radius) {
 		this.requestedPosition = position;
 		requestPlaceSearch(position, radius);
 	}
 
 	private void requestPlaceSearch(Position pos, int radius){
 		String searchPath = "nearbysearch/json?" +
 				"location=" + pos.getLat() + "," + pos.getLng() +
 				"&radius=" + radius +
 				"&sensor=true" +
 				"&key=" + key;
 		PlaceSearch search = client.getJson(searchPath, PlaceSearch.class);
 
 		if(search.status == OK){
 			this.results = search.results;
 		}else{
 			throw new RuntimeException("GooglePlaces returned an error: " + search.status);
 		}
 	}
 	
 	private void requestPlaceDetails(int index){
 		String ref = results.get(index).reference;
 		String detailsPath = "details/json?" +
 				"reference=" + ref +
 				"&sensor=true" +
 				"&key=" + key;
 		PlaceDetails details = client.getJson(detailsPath, PlaceDetails.class);
 		
 		if(details.status == OK){
 			results.set(index, details.result);
 		}else{
 			throw new RuntimeException("GooglePlaces returned an error: " + details.status);
 		}
 	}
 	
 	private String buildImageUrl(String reference, int maxHeight){
 		String url = rootUrl + "photo?" +
 				"photoreference=" + reference +
 				"&sensor=true" +
 				"&maxheight=" + maxHeight +
 				"&key=" + key;
 		return url;
 	}
 	
 	public List<Position> getResultPositions(){
 		List<Position> positions = new ArrayList<Position>();
 		for(Place place : results){
 			positions.add(place.geometry.location);
 		}
 		return positions;
 	}
 	
 	public Location getLocation(int index){
 		Place place = getDetailedPlace(index);
 		Location location = new Location();
 		location.setName(place.name);
 		location.setAddress(place.vicinity);
 		location.setPosition(place.geometry.location);
 		return location;
 	}
 	
 	public String getImageUrl(int index, int maxHeight){
 		Place place = getDetailedPlace(index);
 		if(place.photos != null && !place.photos.isEmpty()){
 			String ref = place.photos.get(0).photo_reference;
 			return buildImageUrl(ref, maxHeight);
 		}else{
 			return null;
 		}
 	}
 	
 	private DetailedPlace getDetailedPlace(int index){
 		if(!(results.get(index) instanceof DetailedPlace)){
 			requestPlaceDetails(index);
 		}
 		return (DetailedPlace)results.get(index);
 	}
 	
 }
