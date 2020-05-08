 /*
 Copyright 2012 NIC Inc.
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
 package gov.colorado.ctt.webservices;
 
 import gov.colorado.ctt.model.Route;
 import gov.colorado.ctt.model.Stop;
 import java.math.BigDecimal;
 import java.math.MathContext;
 import java.math.RoundingMode;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.ejb.Stateless;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.PersistenceUnit;
 import javax.persistence.TypedQuery;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.QueryParam;
 
 import com.google.places.GooglePlaces;
 
 @Path("/")
 @Stateless
 @SuppressWarnings("rawtypes")
 public class CommunityTransitTracker {
 	
 	private static MathContext coordMathContext = new MathContext(9, RoundingMode.HALF_UP);
 	
 	@PersistenceUnit(unitName="CommunityTransitTrackerModel")
 	private EntityManagerFactory emf;
 	
 	@GET()
 	@Produces("application/json")
 	@Path("/update")
 	public List<Stop> update() {
 		List<Stop> stops = getStops();
 		List<Stop> missingStops = new ArrayList<Stop>();
 		for(Stop stop : stops) {
 			if(isPlaceMissing(stop)) {
 				missingStops.add(stop);
 			}
 		}
 		return missingStops;
 	}
 
 	@SuppressWarnings({ "unchecked" })
 	@POST()
 	@Produces("application/json")
 	@Consumes("application/json")
 	@Path("/places/missing")
 	public Boolean isPlaceMissing(Stop stop) {
 		Map placeSearchResult = placesSearch(
 				stop.getStopLat() + "," + stop.getStopLon(),
 				null,
 				"distance",
 				"false",
 				null,
 				null,
 				null,
 				"bus_station|train_station",
 				null
 				);
 		List<Map> results = (List<Map>)placeSearchResult.get("results");
 		Map result = (Map)results.get(0);
 		return !compareCoords(
 				getPlaceLat(result), 
 				getPlaceLng(result), 
 				stop.getStopLat(), 
 				stop.getStopLon()
 				);			
 	}
 	
 	@GET()
 	@Produces("application/json")
 	@Path("/places/search")
 	public Map placesSearch(
 			@QueryParam("location")String location,
 			@QueryParam("radius")Integer radius,
 			@QueryParam("rankby")String rankby,
 			@QueryParam("sensor")String sensor,
 			@QueryParam("keyword")String keyword,
 			@QueryParam("language")String language,
 			@QueryParam("name")String name,
 			@QueryParam("types")String types,
 			@QueryParam("pagetoken")String pagetoken) {
 		return GooglePlaces.getInstance().placeSearch(location, radius, rankby, sensor, keyword, language, name, types, pagetoken);
 	}
 	
 	@SuppressWarnings({ "unchecked" })
 	@GET()
 	@Produces("application/json")
 	@Consumes("application/json")
 	@Path("/places/add")
 	public Map placesAdd(
 			@QueryParam("sensor")String sensor,
 			@QueryParam("lat")BigDecimal lat,
 			@QueryParam("lng")BigDecimal lng,
 			@QueryParam("accuracy")BigDecimal accuracy,
 			@QueryParam("name")String name,
 			@QueryParam("type")String type,
 			@QueryParam("language")String language
 			) {
 		Map placeAddRequest = new HashMap();
 		Map location = new HashMap();
 		location.put("lat", lat);
 		location.put("lng", lng);
 		placeAddRequest.put("location", location);
 		placeAddRequest.put("accuracy", accuracy);
 		placeAddRequest.put("name", name);
 		placeAddRequest.put("types", new String[] {type});
 		placeAddRequest.put("language", language);
 		return GooglePlaces.getInstance().add(sensor, placeAddRequest);
 	}
 	
 	@GET()
 	@Produces("application/json")
 	@Path("/stops")
 	public List<Stop> getStops() {
 		TypedQuery<Stop> query = emf.createEntityManager()
 				.createQuery("select stop from Stop stop", Stop.class);
 		return query.getResultList();
 	}
 	
 	@GET()
 	@Produces("application/json")
 	@Path("/stops/at")
 	public List<Stop> getStopsAt(
 			@QueryParam("lat")BigDecimal lat,
 			@QueryParam("lng")BigDecimal lng
 			) {
 		TypedQuery<Stop> query = emf.createEntityManager()
 				.createQuery("select stop from Stop stop where stop.stopLat = :lat and stop.stopLon = :lng", Stop.class);
 		query.setParameter("lat", lat);
 		query.setParameter("lng", lng);
 		return query.getResultList();
 	}
 	
 	@POST()
 	@Produces("application/json")
 	@Consumes("application/json")
 	@Path("/routes/at")
 	public List<Route> getRoutesAt(Stop stop) {
		TypedQuery<Route> query = emf.createEntityManager()
				.createQuery(
						"select \n" +
								"\tdistinct(route) \n" +
						"from \n" +
							"\tRoute route, \n" +
							"\tStop stop, \n" +
							"\tStopTime stopTime, \n" +
							"\tTrip trip\n " +
						"where \n" +
							"\tstopTime.id.stopId = :stopId and \n" +
							"\tstopTime.id.tripId = trip.tripId and \n" +
							"\ttrip.routeId = route.routeId", Route.class);
 		query.setParameter("stopId", stop.getStopId());
 		return query.getResultList();
 	}
 	
 	
 	@SuppressWarnings({ "unchecked" })
 	@GET()
 	@Produces("application/json")
 	@Path("/stops/nearby")
 	public List<Stop> getNearbyStops(
 			@QueryParam("sensor")String sensor,
 			@QueryParam("lat")BigDecimal lat,
 			@QueryParam("lng")BigDecimal lng
 			) {
 		List<Stop> nearbyStops = new ArrayList<Stop>();
 		Map placesSearchResponse = placesSearch(
 				lat + "," + lng, 
 				null, 
 				"distance", 
 				sensor, 
 				null, 
 				null, 
 				null, 
 				"bus_station|train_station", 
 				null
 				);
 		List<Map> results = (List<Map>)placesSearchResponse.get("results");
 		for(Map result : results) {
 			for(Stop stop : getStopsAt(getPlaceLat(result), getPlaceLng(result))) {
 				if(compareCoords(getPlaceLat(result), getPlaceLng(result), stop.getStopLat(), stop.getStopLon())) {
 					nearbyStops.add(stop);
 				}
 			}
 		}
 		//TODO: Include attributions data in response
 		return nearbyStops;
 	}
 	
 	private BigDecimal getPlaceLat(Map result) {
 		return new BigDecimal((Double)((Map)((Map)result.get("geometry")).get("location")).get("lat"), coordMathContext);
 	}
 	
 	private BigDecimal getPlaceLng(Map result) {
 		return new BigDecimal((Double)((Map)((Map)result.get("geometry")).get("location")).get("lng"), coordMathContext);
 	}
 	
 	private Boolean compareCoords(
 			BigDecimal aLat, 
 			BigDecimal aLng,
 			BigDecimal bLat,
 			BigDecimal bLng
 			) {
 		return (aLat.round(coordMathContext).compareTo(bLat.round(coordMathContext)) == 0 && 
 				aLng.round(coordMathContext).compareTo(bLng.round(coordMathContext)) == 0);			
 	}
 	
 }
