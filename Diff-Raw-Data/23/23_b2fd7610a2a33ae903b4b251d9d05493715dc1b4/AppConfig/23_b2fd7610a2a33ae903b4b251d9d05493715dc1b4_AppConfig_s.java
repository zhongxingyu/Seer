 package org.routy.model;
 
 /**
  * Global properties used all over the app.  Centralized to make propagating  
  * updated data easy.
  * 
  */
 public final class AppConfig {
 	
 	public static final boolean DEBUG = true;
 
 	/** The minimum accuracy required to achieve a "good" location fix in meters when searching for the user. */
 	public static final double USER_LOCATION_ACCURACY_THRESHOLD_M = 50;
 	
 	/** The minimum accuracy required to achieve a "good" location fix in meters when searching for the device at startup. */
 	public static final double DEVICE_LOCATION_ACCURACY_THRESHOLD_M = 100;
 	
 	/** The amount of time before the last known location provided by Android is deemed "stale" */
 	public static final double LOCATION_EXPIRE_TIME_MS = 0;//300000;
 	
 	/** The amount of time Routy should spend searching for the user's location in milliseconds */
	public static final long LOCATION_FETCH_TIMEOUT_MS = 10000;
 	
 	public static final long CALCULATE_ROUTE_TIMEOUT_MS = 10000;
 	
 	/** The amount of time Routy should spend getting results from the Google Places API */
 	public static final long G_PLACES_TIMEOUT_MS = 10000;
 	
 	public static final long REVERSE_GEOCODE_TIMEOUT_MS = 10000;
 	
 	/** The base URL for accessing the Google Distance Matrix Web API */
 	public static final String G_DISTANCE_MATRIX_URL = "https://maps.googleapis.com/maps/api/distancematrix/json?";
 	
 	/** The base URL for accessing the <a href="https://developers.google.com/maps/documentation/geocoding/">Google Geocoding Web API</a> */
 	public static final String G_GEOCODING_API_URL = "https://maps.googleapis.com/maps/api/geocode/xml?";
 	
 	/** The base URL for accessing the <a href="">Google Places Web API</a> */
 	public static final String G_PLACES_API_URL = "https://maps.googleapis.com/maps/api/place/textsearch/xml?";
 	
 	/** The base URL for accessing the <a href="">Google Directions Web API</a> */
 	public static final String G_DIRECTIONS_API_URL = "https://maps.googleapis.com/maps/api/directions/xml?";
 
 	public static final int G_PLACES_MAX_RESULTS = 5;
 	
 	public static final int G_PLACES_SEARCH_RADIUS_M = 5000;
 	
 	public static final int NUM_MAX_DESTINATIONS = 5;
 	
 	public static final long SPLASH_SCREEN_DELAY_MS = 1250;
 
 	public static final String G_API_KEY = "AIzaSyAsBYyw8a9WUt60WvRLf2ibSe8UfRSnV9g";
 	
 	
 }
