 package com.barview.constants;
 
 public class BarviewConstants {
 	public static final String FAVORITES_URL_DEV = "http://new-host.home:8888/barview/index.php?/rest/favorites";
 	public static final String BARIMAGES_URL_DEV = "http://new-host.home:8888/barview/index.php?/rest/barimage";
	public static final String NEARBYBARS_URL_DEV = "http://new-host.home:8888/barview/index.php?/rest/nearbybars";
 	
 	// Favorite constants for the XML
 	public static final String FAVORITE_AGGREGATE	= "favorite";
 	public static final String FAVORITE_BAR_ID		= "bar_id";
 	public static final String FAVORITE_ADDRESS		= "address";
 	public static final String FAVORITE_NAME		= "name";
 	
 	// BarImage constants for the XML
 	public static final String BARIMAGE_AGGREGATE	= "barimage";
 	public static final String BARIMAGE_BAR_ID		= "bar_id";
 	public static final String BARIMAGE_IMAGE		= "image";
 	
	// Nearby bar constants for the XML
	public static final String NEARBY_BAR_AGGREGATE	= "bar";
	public static final String NEARBY_BAR_ID		= "bar_id";
	public static final String NEARBY_BAR_NAME		= "name";
	public static final String NEARBY_BAR_ADDRESS	= "address";
	public static final String NEARBY_BAR_LAT		= "lat";
	public static final String NEARBY_BAR_LNG		= "lng";
	
 	// Extras parameters
 	public static final String BAR_ID				= "bar_id";
 	public static final String BAR_NAME				= "bar_name";
	
	public static final int GEOPOINT_MULT			= 1000000;
 }
