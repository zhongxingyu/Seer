 /* --------------------------------------------------------------------------
  * @author Hauke Walden
  * @created 28.06.2011 
  * Copyright 2011 by Hauke Walden 
  * All rights reserved.
  * --------------------------------------------------------------------------
  */
 
 package de.mbaaba.calendar;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.HashMap;
 
 import com.google.gson.Gson;
 
 import de.mbaaba.directions.GoogleDirections;
 import de.mbaaba.directions.Legs;
 import de.mbaaba.directions.Routes;
 import de.mbaaba.util.Configurator;
 
 public class FixLocationFilter implements ICalendarFilter {
 
 	static final String DEFAULT_LOCATION = "default.location";
 
 	/**
 	 * A request cache.
 	 */
 	private static HashMap<String, String> cachedLocations = new HashMap<String, String>();
 
 	private static final int SANITY_LENGTH = 8;
 
 	private static final Number ONE_HUNDRED_KILOMETER = 100000;
 
 	private final Configurator configurator;
 
 	public FixLocationFilter(Configurator aConfigurator) {
 		configurator = aConfigurator;
 
 	}
 
 	public boolean passes(ICalendarEntry aParamCalendarEntry) throws Exception {
 
 		String defaultLocation = configurator.getProperty(DEFAULT_LOCATION, "");
 
 		String room = aParamCalendarEntry.getRoom();
 		if (room == null) {
 			room = "";
 		}
 		String location = aParamCalendarEntry.getLocation();
 		if (location == null) {
 			location = "";
 		}
 
 		if (room.isEmpty()) {
 			// if no room is set, it is probably set in the location instead.
 			if (!location.isEmpty()) {
 				aParamCalendarEntry.setRoom(location);
 
 			}
 		}
 
 		location = fixLocation(location, defaultLocation);
 
 		aParamCalendarEntry.setLocation(location);
 		return true;
 	}
 
 	private String fixLocation(final String originalLocation, String defaultLocation) throws MalformedURLException, IOException {
 		String result;
 		if (cachedLocations.containsKey(originalLocation)) {
 			result = cachedLocations.get(originalLocation);
 		} else {
 			result = defaultLocation;
 			if (originalLocation.length() >= SANITY_LENGTH) {
 				URL url = new URL("http://maps.google.de/maps/api/directions/json?origin=" + URLEncoder.encode(defaultLocation, "UTF-8") + "&destination=" + URLEncoder.encode(originalLocation, "")
 						+ "&sensor=true");
 
 				Gson gson = new Gson(); // Or use new GsonBuilder().create();
 
 				URLConnection conn = url.openConnection();
 				BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 				GoogleDirections directions = gson.fromJson(rd, GoogleDirections.class);
 
 				if ((directions.getStatus().equals("OK"))) {
 					Routes route0 = directions.getRoutes().get(0);
 					Legs leg0 = route0.getLegs().get(0);
 					if (leg0.getDistance().getValue().longValue() > ONE_HUNDRED_KILOMETER.longValue()) {
 						// result is probably broken!
 						result = defaultLocation;
 					} else {
 						result = leg0.getEnd_address();
 					}
 				}
 
 				// String s = rd.readLine();
 				// while (s != null) {
 				// s = s.trim();
 				// if (s.contains("<status>NOT_FOUND</status>")) {
 				// // invalid location!
 				// break;
 				// } else {
 				// if (s.startsWith(START_ADDRESS)) {
 				// result = s.substring(START_ADDRESS.length());
 				// result = result.substring(0, result.indexOf("<"));
 				// break;
 				// }
 				// }
 				// s = rd.readLine();
 				// }
 			}
 			cachedLocations.put(originalLocation, result);
 		}
 		return result;
 	}
 
 }
