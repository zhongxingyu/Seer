 package edu.cmu.eventtracker.actionhandler;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map.Entry;
 import java.util.UUID;
 
 import com.effectiveJava.GeoLocationService;
 import com.effectiveJava.Point;
 
 import edu.cmu.eventtracker.action.GetCloseByEvents;
 import edu.cmu.eventtracker.action.InsertLocationAction;
 import edu.cmu.eventtracker.action.LocationHeartbeatAction;
 import edu.cmu.eventtracker.dto.Event;
 import edu.cmu.eventtracker.dto.Location;
 import edu.cmu.eventtracker.dto.LocationHeartbeatResponse;
 import edu.cmu.eventtracker.dto.ShardResponse;
 
 public class LocationHeartbeatHandler
 		implements
 			ActionHandler<LocationHeartbeatAction, LocationHeartbeatResponse> {
 
 	public static final int MIN_COUNT = 4;
 	public static final int MAX_PERIOD = 60; // minutes
 	public static final double RADIUS = 0.5; // km
 
 	@Override
 	public LocationHeartbeatResponse performAction(
 			LocationHeartbeatAction action, ActionContext context) {
 		GeoServiceContext geoContext = (GeoServiceContext) context;
 		LocationHeartbeatResponse response = new LocationHeartbeatResponse();
 		Location location = action.getLocation();
 		location.setId(UUID.randomUUID().toString());
 		location.setTimestamp(new Date());
 		context.execute(new InsertLocationAction(location));
 
 		HashMap<String, Event> closeByEvents = lookupCloseByEvents(action
 				.getLocation().getLat(), action.getLocation().getLng(),
 				geoContext);
 		if (location.getEventId() != null) {
 			Event event = closeByEvents.get(location.getEventId());
 			if (event == null) {
 				throw new NullPointerException(
 						"Event with the given id was not found");
 			}
 			Point[] extremes = GeoLocationService.getExtremePointsFrom(
 					new Point(event.getLocation().getLat(), event.getLocation()
 							.getLng()), RADIUS);
 			if (!(extremes[0].getLatitude() <= location.getLat()
 					&& extremes[0].getLongitude() <= location.getLng()
 					&& location.getLat() <= extremes[1].getLatitude() && location
 					.getLng() <= extremes[1].getLongitude())) {
 				throw new IllegalStateException(
 						"You are too far away from the original event");
 			}
 		}
 		response.setEvents(new ArrayList<Event>(closeByEvents.values()));
 		response.setCanCreateEvent(canCreateNewEvents(closeByEvents));
 		return response;
 	}
 
 	public static boolean canCreateNewEvents(
 			HashMap<String, Event> closeByEvents) {
 		Event usersWithoutEvent = closeByEvents.get(null);
 		if (usersWithoutEvent != null
 				&& usersWithoutEvent.getParticipantCount() >= MIN_COUNT) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public static HashMap<String, Event> lookupCloseByEvents(double lat,
 			double lng, GeoServiceContext context) {
 		HashSet<ShardResponse> shards = new HashSet<ShardResponse>();
 		shards.add(context.getService().getLocationShard());
 		Point[] extremePoints = GeoLocationService.getExtremePointsFrom(
 				new Point(lat, lng), LocationHeartbeatHandler.RADIUS);
 		HashMap<String, Event> events = context
 				.execute(new GetCloseByEvents(extremePoints[0].getLatitude(),
 						extremePoints[0].getLongitude(), extremePoints[1]
 								.getLatitude(), extremePoints[1].getLongitude()));
 		lookupOtherServersCloseByEvents(extremePoints[0].getLatitude(),
				extremePoints[0].getLongitude(), context, shards, events,
				extremePoints);
 		lookupOtherServersCloseByEvents(extremePoints[1].getLatitude(),
				extremePoints[1].getLongitude(), context, shards, events,
				extremePoints);
 		lookupOtherServersCloseByEvents(extremePoints[0].getLatitude(),
				extremePoints[1].getLongitude(), context, shards, events,
				extremePoints);
 		lookupOtherServersCloseByEvents(extremePoints[1].getLatitude(),
				extremePoints[0].getLongitude(), context, shards, events,
				extremePoints);
 		return events;
 	}
 
 	private static void lookupOtherServersCloseByEvents(double lat, double lng,
 			GeoServiceContext context, HashSet<ShardResponse> shards,
			HashMap<String, Event> allEvents, Point[] extremePoints) {
 		ShardResponse locationShard = context.getService().getLocatorService()
 				.getLocationShard(lat, lng);
 		if (shards.contains(locationShard)) {
 			return;
 		}
 		shards.add(locationShard);
 		HashMap<String, Event> events = context
 				.getService()
 				.getLocatorService()
 				.getLocationShardServer(lat, lng)
 				.execute(
						new GetCloseByEvents(extremePoints[0].getLatitude(),
								extremePoints[0].getLongitude(),
								extremePoints[1].getLatitude(),
								extremePoints[1].getLongitude()));
 		for (Entry<String, Event> entry : events.entrySet()) {
 			Event main = allEvents.get(entry.getKey());
 			if (main == null) {
 				allEvents.put(entry.getKey(), entry.getValue());
 				System.out.println("found " + entry.getValue().getName()
 						+ " from " + locationShard.getMaster() + " as "
 						+ context.getService().getUrl());
 			} else {
 				main.setParticipantCount(main.getParticipantCount()
 						+ entry.getValue().getParticipantCount());
 			}
 		}
 	}
 
 }
