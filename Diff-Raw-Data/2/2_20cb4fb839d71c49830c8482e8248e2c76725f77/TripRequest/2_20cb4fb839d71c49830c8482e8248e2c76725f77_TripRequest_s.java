 package models;
 
 import googleMapsDirections.Directions;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.OneToMany;
 
 import play.Logger;
 
 @Entity
 public class TripRequest extends Trip {
 
     private static final boolean useGoogleAPIAfterApproximation = false;
     private static boolean forceMockMatchesIfNoneAvailable = false;
     private static final double tripOverhead = 1.1;
 
     @OneToMany
     private List<TripMatch> matches = new ArrayList<TripMatch>();
 
     /**
      *
      */
     private static final long serialVersionUID = -7829362116641187593L;
     public static Finder<Integer, TripRequest> find = new Finder<Integer, TripRequest>(Integer.class, TripRequest.class);
 
     public TripRequest() {
 	super();
 	matches = new ArrayList<TripMatch>();
     }
 
     public List<TripMatch> getMatches() {
 	calculateNewMatchingOffers();
 	if (matches.size() == 0 && forceMockMatchesIfNoneAvailable) {
 	    TripMatch mockMatch = new TripMatch();
 	    mockMatch.setState(TripMatchState.OPEN.ordinal());
 	    mockMatch.setTripRequest(this);
 	    mockMatch.setTripOffer(searchRandomTripOffer());
 	    mockMatch.save();
 	    matches.add(mockMatch);
 	}
 	return matches;
     }
 
     private TripOffer searchRandomTripOffer() {
 	int nrOfTripOffers = TripOffer.find.findRowCount();
 	TripOffer offer = null;
 	while (offer == null) {
 	    offer = TripOffer.find.byId((int) (Math.random() * nrOfTripOffers));
 	}
	return null;
     }
 
     private void calculateNewMatchingOffers() {
 
 	List<TripOffer> matchesInTimeWindow = TripOffer.find.where().le("start_time_min", getStartTimeMax()).ge("start_time_max", getStartTimeMin())
 		.ge("end_time_max", getEndTimeMin()).le("end_time_min", getEndTimeMax()).ge("number_of_seats", getNumberOfSeats()).findList();
 
 	Directions originalDirections, directionsIncludingRequest;
 
 	for (TripOffer matchingOffer : matchesInTimeWindow) {
 
 	    if (isNewOffer(matchingOffer)) {
 
 		Waypoint offerOrigin = new Waypoint(matchingOffer.getOriginLong(), matchingOffer.getOriginLat(), matchingOffer.getStartTimeMin(),
 			matchingOffer.getStartTimeMax());
 		Waypoint offerDestination = new Waypoint(matchingOffer.getDestinationLong(), matchingOffer.getDestinationLat(),
 			matchingOffer.getEndTimeMin(), matchingOffer.getEndTimeMax());
 		Waypoint requestOrigin = new Waypoint(getOriginLong(), getOriginLat(), getStartTimeMin(), getStartTimeMax());
 		Waypoint requestDestination = new Waypoint(getDestinationLong(), getDestinationLat(), getEndTimeMin(), getEndTimeMax());
 
 		originalDirections = new Directions();
 		originalDirections.addWaypoint(offerOrigin);
 		originalDirections.addWaypoint(offerDestination);
 
 		directionsIncludingRequest = new Directions();
 		directionsIncludingRequest.addWaypoint(offerOrigin);
 		directionsIncludingRequest.addWaypoint(requestOrigin);
 		directionsIncludingRequest.addWaypoint(requestDestination);
 		directionsIncludingRequest.addWaypoint(offerDestination);
 
 		if (this.isBetweenBounds(originalDirections.getNorthWestBounds(), originalDirections.getSouthEastBounds())
 			&& isPossibleMatchOnTravelDistance(matchingOffer, originalDirections, directionsIncludingRequest)) {
 		    // TODO: Add
 		    // isPossibleMatchOnTravelTime(directionsIncludingRequest)
 
 		    TripMatch match = new TripMatch();
 		    match.setTripOffer(matchingOffer);
 		    match.setTripRequest(this);
 		    match.setState(TripMatchState.OPEN.ordinal());
 		    match.save();
 		    matches.add(match);
 		}
 	    }
 	}
     }
 
     private boolean isNewOffer(TripOffer matchingOffer) {
 	for (TripMatch match : matches) {
 	    if (match.getTripOffer().getId() == matchingOffer.getId()) {
 		return false;
 	    }
 	}
 	return true;
     }
 
     @SuppressWarnings("unused")
     private boolean isPossibleMatchOnTravelTime(Directions directions) {
 	return directions.isValidForWaypointTimeConstraints();
     }
 
     private boolean isPossibleMatchOnTravelDistance(TripOffer tripOffer, Directions originalDirections, Directions newDirections) {
 
 	if (originalDirections.getApproximateRouteDistance() * tripOverhead >= newDirections.getApproximateRouteDistance()) {
 
 	    if (!useGoogleAPIAfterApproximation) {
 		return true;
 	    } else {
 		newDirections.retrieveGoogleAPICalculations();
 
 		long offerIncludingRequestDistance = newDirections.getTotalDirectionDistance();
 
 		if (!tripOffer.getMetaData().hasResultsFromAPI()) {
 		    originalDirections.retrieveGoogleAPICalculations();
 
 		    tripOffer.getMetaData().setCalculatedDuration(originalDirections.getCalculatedTravelTimeInSeconds());
 		    tripOffer.getMetaData().setDirectionsDistance(originalDirections.getTotalDirectionDistance());
 		    tripOffer.getMetaData().save();
 		    // Sleep is for not sending too many requests to the Google
 		    // API
 		    // per second
 		    try {
 			Thread.sleep(150);
 		    } catch (InterruptedException e) {
 			e.printStackTrace();
 		    }
 		}
 
 		// Sleep is for not sending too many requests to the Google API
 		// per second
 		try {
 		    Thread.sleep(150);
 		} catch (InterruptedException e) {
 		    e.printStackTrace();
 		}
 
 		long originalDistance = tripOffer.getMetaData().getDirectionsDistance();
 
 		if (originalDistance == 0 || offerIncludingRequestDistance == 0) {
 		    Logger.error("Request rejected by Google API");
 		} else {
 		    if ((originalDistance * tripOverhead) >= offerIncludingRequestDistance) {
 			return true;
 		    }
 		}
 	    }
 	}
 	return false;
     }
 
     private boolean isBetweenBounds(Location northWestBounds, Location southEastBounds) {
 	boolean inBoundaries;
 
 	inBoundaries = (northWestBounds.getLongitude() >= getOriginLong());
 	inBoundaries = inBoundaries && (northWestBounds.getLongitude() >= getDestinationLong());
 	inBoundaries = inBoundaries && (northWestBounds.getLatitude() <= getOriginLat());
 	inBoundaries = inBoundaries && (northWestBounds.getLatitude() <= getDestinationLat());
 	inBoundaries = inBoundaries && (southEastBounds.getLongitude() <= getOriginLong());
 	inBoundaries = inBoundaries && (southEastBounds.getLongitude() <= getDestinationLong());
 	inBoundaries = inBoundaries && (southEastBounds.getLatitude() >= getOriginLat());
 	inBoundaries = inBoundaries && (southEastBounds.getLatitude() >= getDestinationLat());
 
 	return inBoundaries;
     }
 
 }
