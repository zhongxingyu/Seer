 package com.appirio.mobile.aau.nativemap;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 public class RoutesParser {
 
 	private List<Route> routes;
 	private List<BusStop> stops;
 	//private Map<String, String> markerIconMap = new HashMap<String, String>();
 	
 	public RoutesParser(JSONArray busRoutes) throws AMException {
 		try {
 			routes = new ArrayList<Route>();
 			System.out.println(busRoutes);
 			
 			// Parse json into java structures
 			for(int i = 0; i < busRoutes.length(); i++) {
 				JSONObject jsonRoute = busRoutes.getJSONObject(i);
 				
 				routes.add(new Route(jsonRoute));
 			}
 
 			// Associate routes with stops and create stops list
 			HashMap<String, BusStop> stopMap = new HashMap<String, BusStop>();
			for(Route route : routes) {
 				for(BusStop stop : route.getBusStops()) {
 					BusStop tempStop = stopMap.get(stop.getAddress());
 					
 					if(tempStop != null) {
 						tempStop.getRoutes().add(route.getName());
 					} else {
 						stop.getRoutes().add(route.getName());
 						stopMap.put(stop.getAddress(), stop);
 					}
 				}
 			}
 			
 			for(Route route : routes) {
 				for(BusStop stop : route.getBusStops()) {
 					BusStop tempStop = stopMap.get(stop.getAddress());
 					
 					if(tempStop != null) {
 						stop.setRoutes(tempStop.getRoutes());
 					}
 				}
 			}
 			
 			stops = new ArrayList<BusStop>();
 			
 			for(BusStop stop : stopMap.values()) {
 				stops.add(stop);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			
 			throw new AMException(e); 
 		}
 	}
 
 	public List<BusStop> getStops() {
 		return stops;
 	}
 
 	public List<Route> getRoutes() {
 		return routes;
 	}
 
 	public List<Route> getRoutes(Set<String> routeNames) {
 		ArrayList<Route> result = new ArrayList<Route>();
 		
 		for(String routeName : routeNames) {
 			for(Route route : this.getRoutes()) {
 				if(routeName.equals(route.getName())) {
 					result.add(route);
 					break;
 				}
 			}
 		}
 		
 		return result;
 	}
 
 }
