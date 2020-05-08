 package com.vaggs.Utils;
 
 import java.io.Writer;
 import java.util.List;
 
 import com.google.appengine.labs.repackaged.org.json.JSONException;
 import com.google.appengine.labs.repackaged.org.json.JSONWriter;
 import com.vaggs.AirportDiagram.Airport;
 import com.vaggs.Route.Route;
 import com.vaggs.Route.Taxiway;
 import com.vaggs.Route.Waypoint;
 
 public class VAGGSJsonWriter extends JSONWriter {
 
 	public VAGGSJsonWriter(Writer writer) {
 	    super(writer);
     }
 	
 	public void writeError(String error) {
 		try {
 	        object();
 		    	key("error");
 		    	object();
 		    		key("description");
 		    		value(error);
 		    	endObject();
 	    	endObject();
 		} catch (JSONException e) {
 	        e.printStackTrace();
         }
 	}
 	
 	public void writeRoute(Route route) {
 		try {
 			object();
 			key("routeName");
 			value(route.getName());
 			key("pts");
 			array();
 				for (Waypoint point : route) {
 					writePoint(point);
 				}
 			endArray();				
 			endObject();
 		} catch (JSONException e) { e.printStackTrace(); }
 	}
 	
 	public void writeRoutes(List<Route> routes) {
		try{ 
			array();
			for(Route r : routes) {
				writeRoute(r);
			}
			endArray();
		} catch (JSONException e) { e.printStackTrace(); }
 	}
 	
 	private void writePoint(Waypoint pt) throws JSONException {
 		object();
 		key("Lat");
 		value(pt.getPoint().getLat());
 		key("Lng");
 		value(pt.getPoint().getLng());
 		key("Holdshort");
 		value(pt.isHoldShort());
 		key("Intersection");
 		value(pt.isIntersection());
 		endObject();
 	}
 	
 	public void writeChannelToken(String token) {
 		try {
 			object();
 				key("token");
 			    value(token);
 			endObject();
 		} catch (JSONException e) { e.printStackTrace(); }
 	}
 	
 	private void writeTaxiway(Taxiway taxiway) throws JSONException {
 		array();
 		for(Waypoint w : taxiway.getWaypoints()) {
 			writePoint(w);
 		}
 		endArray();
 	}
 	
 	public void writeAirportTaxiways(Airport airport) {
 		/*[
 			[starting points],
 			[taxiway 1],
 			[taxiway 2],
 			...
 		  ]
 		*/
 		try {
 			array();
 				array();
 					for(Waypoint w : airport.getRouteStartingPoints()) {
 						writePoint(w);
 					}
 				endArray();
 				for(Taxiway t : airport.getTaxiways()) {
 					writeTaxiway(t);
 				}
 			endArray();
 		} catch (JSONException e) { e.printStackTrace(); }
 	}
 
 }
