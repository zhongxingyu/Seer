 package googleMapsDirections;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.codehaus.jackson.JsonNode;
 
 public class Directions {
 
     ArrayList<Location> route;
     final String apiServer = "http://maps.googleapis.com/maps/api/directions/json?";
 
     public Directions() {
 	route = new ArrayList<Location>();
     }
 
     public void addRoutePoint(Location loc) {
 	route.add(loc);
     }
 
     public long getTotalDistance() {
	String requestURL = String.format("%sorigin=%s&destination=%s&sensor=false&units=metric&waypoints=%s", apiServer, getOriginLocation(),
		getDestinationLocation(), getWaypointsLocations());
 
 	HttpRequest req = new HttpRequest(requestURL);
 	JsonNode result = req.getResult();
 
 	long distance = 0;
 
	if (result.findValue("routes") != null && result.findValue("routes").findValue("legs") != null) {
	    Iterator<JsonNode> it = result.findValue("routes").findValue("legs").getElements();
	    while (it.hasNext()) {
		JsonNode node = it.next();
		if (node.findValue("distance") != null && node.findValue("distance").findValue("value") != null) {
		    JsonNode distanceValue = node.findValue("distance").findValue("value");
		    distance += distanceValue.asLong();
		}
 	    }
 	}
 	return distance;
     }
 
     private String getWaypointsLocations() {
 	StringBuilder res = new StringBuilder();
 	for (Location loc : route.subList(1, route.size() - 1)) {
 	    res.append(loc.getLongLatString());
 	}
 	return res.toString();
     }
 
     private String getDestinationLocation() {
 	return route.get(route.size() - 1).getLongLatString();
     }
 
     private String getOriginLocation() {
 	return route.get(0).getLongLatString();
     }
 
 }
