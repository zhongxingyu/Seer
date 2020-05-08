 package googleMapsDirections;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import models.Location;
 import models.Waypoint;
 
 import org.codehaus.jackson.JsonNode;
 
 import workers.StatisticsGenerator;
 
 public class Directions {
 
     private final ArrayList<Waypoint> route;
     private final String apiServer = "http://maps.googleapis.com/maps/api/directions/json?";
     private long duration = 0;
     private long distance = 0;
 
     public Directions() {
 	this.route = new ArrayList<Waypoint>();
     }
 
     public void addWaypoint(Waypoint waypoint) {
 	route.add(waypoint);
     }
 
     public void addWaypoint(Waypoint waypoint, int index) {
 	route.add(index, waypoint);
     }
 
     public long getTotalDirectionDistance() {
 	//TODO: If not yet retrieved, retrieve before returning
 	return distance;
     }
 
     //TODO: Refactor: make private (Keep a flag if retrieved)
     // Has concequences in Trip.java, in the save() method of the entity
     public void retrieveGoogleAPICalculations() {
 	String requestURL = String.format("%sorigin=%s&destination=%s&sensor=false&units=metric&waypoints=%s", apiServer, getOriginLocation(),
 		getDestinationLocation(), getWaypointsLocationsAsURLParameter());
 
 	HttpRequest req = new HttpRequest(requestURL);
 	JsonNode result = req.getResult();
 
 	if (result.findValue("routes") != null && result.findValue("routes").findValue("legs") != null) {
 	    Iterator<JsonNode> it = result.findValue("routes").findValue("legs").getElements();
 	    while (it.hasNext()) {
 		JsonNode node = it.next();
 
 		if (node.findValue("distance") != null && node.findValue("distance").findValue("value") != null) {
 		    JsonNode distanceValue = node.findValue("distance").findValue("value");
 		    distance += distanceValue.asLong();
 		}
 
 		if (node.findValue("duration") != null && node.findValue("duration").findValue("value") != null) {
 		    JsonNode durationValue = node.findValue("duration").findValue("value");
 		    duration += durationValue.asLong();
 		}
 	    }
 	}
     }
 
     public long getCalculatedTravelTimeInSeconds() {
 	//TODO: If not yet retrieved, retrieve before returning
 	return duration;
     }
 
     public long getApproximateTravelTimeInSeconds() {
 	long distance = getTotalLinearDistance();
 	return (long) (distance / StatisticsGenerator.getDistanceToTravelTimeRatio(distance));
     }
 
     public long getTotalLinearDistance() {
 	float distance = 0;
 	for (int i = 0; i < route.size() - 1; i++) {
 	    distance += distFrom(route.get(i), route.get(i + 1));
 	}
 	return (long) distance;
     }
 
     public long getApproximateRouteDistance() {
 	long distance = getTotalLinearDistance();
 	return (long) (distance * StatisticsGenerator.getCrowFlyDistanceOverhead(distance));
     }
 
     public boolean isValidForWaypointTimeConstraints() {
 	Waypoint destination, origin;
 	long windowMin = route.get(route.size() - 1).getMinimumArrivalTime();
 	long windowMax = route.get(route.size() - 1).getMaximumDepartureTime();
 	
 	for (int i = route.size() - 1; i > 0; i--) {
 	    destination = route.get(i);
 	    origin = route.get(i - 1);
 	    long tripCrowFlyDistance = (long)distFrom(origin, destination);
 	    long tripDuration = (long) (tripCrowFlyDistance * StatisticsGenerator.getCrowFlyDistanceOverhead(tripCrowFlyDistance) / StatisticsGenerator.getDistanceToTravelTimeRatio(tripCrowFlyDistance));
 	    if(windowMin - tripDuration < origin.getMaximumDepartureTime())
 	    {
 		windowMin = Math.max((windowMin - tripDuration), origin.getMinimumArrivalTime());
 		windowMax = Math.min((windowMax - tripDuration), origin.getMaximumDepartureTime());
 	    }
 	    else
 	    {
 		return false;
 	    }
 	}
 	return windowMax >= windowMin;
     }
 
     private float distFrom(Location origin, Location destination) {
 	return distFrom(origin.getLongitude(), origin.getLatitude(), destination.getLongitude(), destination.getLatitude());
     }
 
     private float distFrom(double lat1, double lng1, double lat2, double lng2) {
 	double earthRadius = 3958.75;
 	double dLat = Math.toRadians(lat2 - lat1);
 	double dLng = Math.toRadians(lng2 - lng1);
 	double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2)
 		* Math.sin(dLng / 2);
 	double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
 	double dist = earthRadius * c;
 
 	int meterConversion = 1609;
 
 	return (float) (dist * meterConversion);
     }
 
     private String getWaypointsLocationsAsURLParameter() {
 	StringBuilder res = new StringBuilder();
 	for (Location loc : route.subList(1, route.size() - 1)) {
 	    res.append(loc.getLongLatString());
 	    res.append("|");
 	}
 	return res.toString();
     }
 
     private String getDestinationLocation() {
 	return route.get(route.size() - 1).getLongLatString();
     }
 
     private String getOriginLocation() {
 	return route.get(0).getLongLatString();
     }
 
     public Location getDirectionsCenter() {
 	Location startPoint = new Location(route.get(0).getLatitude(), route.get(0).getLongitude());
 	Location endPoint = new Location(route.get(route.size() - 1).getLatitude(), route.get(route.size() - 1).getLongitude());
 	double centerLatitude = (startPoint.getLatitude() + endPoint.getLatitude()) / 2;
 	double centerLongitude = (startPoint.getLongitude() + endPoint.getLongitude()) / 2;
 	return new Location(centerLatitude, centerLongitude);
     }
 
     public Location getSouthEastBounds() {
 	return getBounds(2.35619449); // 135 degrees in radians
     }
 
     public Location getNorthWestBounds() {
 	return getBounds(5.49778714); // 315 degrees in radians
     }
 
     // This method returns a very crude calculation of a new Location
     // with a specific offset. It basically treats the earth as flat
     // but it should serve our purpose.
     private Location getBounds(double bearing) {
 	double distance = Math.sqrt(Math.pow(
 		distFrom(route.get(0).getLatitude(), route.get(0).getLongitude(), this.getDirectionsCenter().getLatitude(), this
 			.getDirectionsCenter().getLongitude()), 2) * 2) * 1.1;
 
 	double dx = distance * Math.cos(bearing);
 	double dy = distance * Math.sin(bearing);
 
	double delta_longitude = dx / (111320 * Math.cos(getDirectionsCenter().getLatitude()));
 	double delta_latitude = dy / 110540;
 
 	double latitude = getDirectionsCenter().getLatitude() + delta_latitude;
 	double longitude = getDirectionsCenter().getLongitude() + delta_longitude;
 
 	// double latitude = this.getDirectionsCenter().getLatitude() + distance
 	// * deltaLatitude;
 	// double longitude = this.getDirectionsCenter().getLongitude() +
 	// distance * deltaLongitude;
 	return new Location(longitude, latitude);
     }
 
 }
