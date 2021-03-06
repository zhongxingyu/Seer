 package com.osmand;
 
 import java.util.List;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 
 import com.osmand.activities.RouteProvider.RouteService;
 import com.osmand.activities.search.SearchHistoryHelper;
 import com.osmand.map.ITileSource;
 import com.osmand.map.TileSourceManager;
 import com.osmand.map.TileSourceManager.TileSourceTemplate;
 import com.osmand.osm.LatLon;
 
 public class OsmandSettings {
 
 	public enum ApplicationMode {
 		/*
 		 * DEFAULT("Default"), CAR("Car"), BICYCLE("Bicycle"), PEDESTRIAN("Pedestrian");
 		 */
 
 		DEFAULT(R.string.app_mode_default), 
 		CAR(R.string.app_mode_car), 
 		BICYCLE(R.string.app_mode_bicycle), 
 		PEDESTRIAN(R.string.app_mode_pedestrian);
 
 		private final int key;
 		
 		ApplicationMode(int key) {
 			this.key = key;
 		}
 		public static String toHumanString(ApplicationMode m, Context ctx){
 			return ctx.getResources().getString(m.key);
 		}
 
 	}
 
 	// These settings are stored in SharedPreferences
 	public static final String SHARED_PREFERENCES_NAME = "com.osmand.settings"; //$NON-NLS-1$
 
 	public static final int CENTER_CONSTANT = 0;
 	public static final int BOTTOM_CONSTANT = 1;
 
 	// this value string is synchronized with settings_pref.xml preference name
 	public static final String USE_INTERNET_TO_DOWNLOAD_TILES = "use_internet_to_download_tiles"; //$NON-NLS-1$
 
 	public static boolean isUsingInternetToDownloadTiles(Context ctx) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		return prefs.getBoolean(USE_INTERNET_TO_DOWNLOAD_TILES, true);
 	}
 	
	// this value string is synchronized with settings_pref.xml preference name
	public static final String USE_INTERNET_TO_CALCULATE_ROUTE = "use_internet_to_calculate_route"; //$NON-NLS-1$

	public static boolean isUsingInternetToCalculateRoute(Context ctx) {
		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
		return prefs.getBoolean(USE_INTERNET_TO_CALCULATE_ROUTE, false);
	}
 
 	// this value string is synchronized with settings_pref.xml preference name
 	public static final String SHOW_POI_OVER_MAP = "show_poi_over_map"; //$NON-NLS-1$
 
 	public static boolean isShowingPoiOverMap(Context ctx) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		return prefs.getBoolean(SHOW_POI_OVER_MAP, false);
 	}
 	
 	public static boolean setShowPoiOverMap(Context ctx, boolean val) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		return prefs.edit().putBoolean(SHOW_POI_OVER_MAP, val).commit();
 	}
 
 	// this value string is synchronized with settings_pref.xml preference name
 	public static final String USER_NAME = "user_name"; //$NON-NLS-1$
 
 	public static String getUserName(Context ctx) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		return prefs.getString(USER_NAME, "NoName"); //$NON-NLS-1$
 	}
 
 	public static boolean setUserName(Context ctx, String name) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		return prefs.edit().putString(USER_NAME, name).commit();
 	}
 	
 	public static final String USER_PASSWORD = "user_password"; //$NON-NLS-1$
 	public static String getUserPassword(Context ctx){
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		return prefs.getString(USER_PASSWORD, ""); //$NON-NLS-1$
 	}
 	
 	public static boolean setUserPassword(Context ctx, String name){
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		return prefs.edit().putString(USER_PASSWORD, name).commit();
 	}
 	
 	// this value string is synchronized with settings_pref.xml preference name
 	public static final String APPLICATION_MODE = "application_mode"; //$NON-NLS-1$
 
 	public static ApplicationMode getApplicationMode(Context ctx) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		String s = prefs.getString(APPLICATION_MODE, ApplicationMode.DEFAULT.name());
 		try {
 			return ApplicationMode.valueOf(s);
 		} catch (IllegalArgumentException e) {
 			return ApplicationMode.DEFAULT;
 		}
 	}
 
 	public static boolean setApplicationMode(Context ctx, ApplicationMode p) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		return prefs.edit().putString(APPLICATION_MODE, p.name()).commit();
 	}
 	
 	// this value string is synchronized with settings_pref.xml preference name
 	public static final String ROUTER_SERVICE = "router_service"; //$NON-NLS-1$
 
 	public static RouteService getRouterService(Context ctx) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		int ord = prefs.getInt(ROUTER_SERVICE, RouteService.CLOUDMADE.ordinal());
 		if(ord < RouteService.values().length){
 			return RouteService.values()[ord];
 		} else {
 			return RouteService.CLOUDMADE;
 		}
 	}
 
 	public static boolean setRouterService(Context ctx, RouteService p) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		return prefs.edit().putInt(ROUTER_SERVICE, p.ordinal()).commit();
 	}	
 
 	// this value string is synchronized with settings_pref.xml preference name
 	public static final String SAVE_CURRENT_TRACK = "save_current_track"; //$NON-NLS-1$
 	public static final String RELOAD_INDEXES = "reload_indexes"; //$NON-NLS-1$
 	public static final String DOWNLOAD_INDEXES = "download_indexes"; //$NON-NLS-1$
 
 	// this value string is synchronized with settings_pref.xml preference name
 	public static final String SAVE_TRACK_TO_GPX = "save_track_to_gpx"; //$NON-NLS-1$
 
 	public static boolean isSavingTrackToGpx(Context ctx) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		return prefs.getBoolean(SAVE_TRACK_TO_GPX, false);
 	}
 
 	// this value string is synchronized with settings_pref.xml preference name
 	public static final String SAVE_TRACK_INTERVAL = "save_track_interval"; //$NON-NLS-1$
 
 	public static int getSavingTrackInterval(Context ctx) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		return prefs.getInt(SAVE_TRACK_INTERVAL, 5);
 	}
 
 	// this value string is synchronized with settings_pref.xml preference name
 	public static final String SHOW_OSM_BUGS = "show_osm_bugs"; //$NON-NLS-1$
 
 	public static boolean isShowingOsmBugs(Context ctx) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		return prefs.getBoolean(SHOW_OSM_BUGS, false);
 	}
 
 	// this value string is synchronized with settings_pref.xml preference name
 	public static final String SHOW_VIEW_ANGLE = "show_view_angle"; //$NON-NLS-1$
 
 	public static boolean isShowingViewAngle(Context ctx) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		return prefs.getBoolean(SHOW_VIEW_ANGLE, true);
 	}
 
 	// this value string is synchronized with settings_pref.xml preference name
 	public static final String AUTO_ZOOM_MAP = "auto_zoom_map"; //$NON-NLS-1$
 
 	public static boolean isAutoZoomEnabled(Context ctx) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		return prefs.getBoolean(AUTO_ZOOM_MAP, false);
 	}
 
 	// this value string is synchronized with settings_pref.xml preference name
 	public static final String ROTATE_MAP_TO_BEARING = "rotate_map_to_bearing"; //$NON-NLS-1$
 
 	public static boolean isRotateMapToBearing(Context ctx) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		return prefs.getBoolean(ROTATE_MAP_TO_BEARING, false);
 	}
 
 	// this value string is synchronized with settings_pref.xml preference name
 	public static final String POSITION_ON_MAP = "position_on_map"; //$NON-NLS-1$
 
 	public static int getPositionOnMap(Context ctx) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		return prefs.getInt(POSITION_ON_MAP, CENTER_CONSTANT);
 	}
 	
 	// this value string is synchronized with settings_pref.xml preference name
 	public static final String MAX_LEVEL_TO_DOWNLOAD_TILE = "max_level_download_tile"; //$NON-NLS-1$
 
 	public static int getMaximumLevelToDownloadTile(Context ctx) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		return prefs.getInt(MAX_LEVEL_TO_DOWNLOAD_TILE, 18);
 	}
 
 	// this value string is synchronized with settings_pref.xml preference name
 	public static final String MAP_VIEW_3D = "map_view_3d"; //$NON-NLS-1$
 
 	public static boolean isMapView3D(Context ctx) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		return prefs.getBoolean(MAP_VIEW_3D, false);
 	}
 
 	// this value string is synchronized with settings_pref.xml preference name
 	public static final String USE_ENGLISH_NAMES = "use_english_names"; //$NON-NLS-1$
 
 	public static boolean usingEnglishNames(Context ctx) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		return prefs.getBoolean(USE_ENGLISH_NAMES, false);
 	}
 
 	public static boolean setUseEnglishNames(Context ctx, boolean useEnglishNames) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		return prefs.edit().putBoolean(USE_ENGLISH_NAMES, useEnglishNames).commit();
 	}
 
 	// this value string is synchronized with settings_pref.xml preference name
 	public static final String MAP_TILE_SOURCES = "map_tile_sources"; //$NON-NLS-1$
 
 	public static ITileSource getMapTileSource(Context ctx) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		String tileName = prefs.getString(MAP_TILE_SOURCES, null);
 		if (tileName != null) {
 			List<TileSourceTemplate> list = TileSourceManager.getKnownSourceTemplates();
 			for (TileSourceTemplate l : list) {
 				if (l.getName().equals(tileName)) {
 					return l;
 				}
 			}
 		}
 		return TileSourceManager.getMapnikSource();
 	}
 
 	public static String getMapTileSourceName(Context ctx) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		String tileName = prefs.getString(MAP_TILE_SOURCES, null);
 		if (tileName != null) {
 			return tileName;
 		}
 		return TileSourceManager.getMapnikSource().getName();
 	}
 
 	// This value is a key for saving last known location shown on the map
 	public static final String LAST_KNOWN_MAP_LAT = "last_known_map_lat"; //$NON-NLS-1$
 	public static final String LAST_KNOWN_MAP_LON = "last_known_map_lon"; //$NON-NLS-1$
 	public static final String IS_MAP_SYNC_TO_GPS_LOCATION = "is_map_sync_to_gps_location"; //$NON-NLS-1$
 	public static final String LAST_KNOWN_MAP_ZOOM = "last_known_map_zoom"; //$NON-NLS-1$
 
 	public static LatLon getLastKnownMapLocation(Context ctx) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		float lat = prefs.getFloat(LAST_KNOWN_MAP_LAT, 0);
 		float lon = prefs.getFloat(LAST_KNOWN_MAP_LON, 0);
 		return new LatLon(lat, lon);
 	}
 
 	public static void setMapLocationToShow(Context ctx, double latitude, double longitude) {
 		setMapLocationToShow(ctx, latitude, longitude, null);
 	}
 	
 	public static void setMapLocationToShow(Context ctx, double latitude, double longitude, String historyDescription) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		Editor edit = prefs.edit();
 		edit.putFloat(LAST_KNOWN_MAP_LAT, (float) latitude);
 		edit.putFloat(LAST_KNOWN_MAP_LON, (float) longitude);
 		edit.putBoolean(IS_MAP_SYNC_TO_GPS_LOCATION, false);
 		edit.commit();
 		if(historyDescription != null){
 			SearchHistoryHelper.getInstance().addNewItemToHistory(latitude, longitude, historyDescription, ctx);
 		}
 	}
 
 	// Do not use that method if you want to show point on map. Use setMapLocationToShow
 	public static void setLastKnownMapLocation(Context ctx, double latitude, double longitude) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		Editor edit = prefs.edit();
 		edit.putFloat(LAST_KNOWN_MAP_LAT, (float) latitude);
 		edit.putFloat(LAST_KNOWN_MAP_LON, (float) longitude);
 		edit.commit();
 	}
 
 	public static boolean setSyncMapToGpsLocation(Context ctx, boolean value) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		return prefs.edit().putBoolean(IS_MAP_SYNC_TO_GPS_LOCATION, value).commit();
 	}
 
 	public static boolean isMapSyncToGpsLocation(Context ctx) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		return prefs.getBoolean(IS_MAP_SYNC_TO_GPS_LOCATION, true);
 	}
 
 	public static int getLastKnownMapZoom(Context ctx) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		return prefs.getInt(LAST_KNOWN_MAP_ZOOM, 3);
 	}
 
 	public static void setLastKnownMapZoom(Context ctx, int zoom) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		Editor edit = prefs.edit();
 		edit.putInt(LAST_KNOWN_MAP_ZOOM, zoom);
 		edit.commit();
 	}
 
 	public final static String POINT_NAVIGATE_LAT = "point_navigate_lat"; //$NON-NLS-1$
 	public final static String POINT_NAVIGATE_LON = "point_navigate_lon"; //$NON-NLS-1$
 
 	public static LatLon getPointToNavigate(Context ctx) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		float lat = prefs.getFloat(POINT_NAVIGATE_LAT, 0);
 		float lon = prefs.getFloat(POINT_NAVIGATE_LON, 0);
 		if (lat == 0 && lon == 0) {
 			return null;
 		}
 		return new LatLon(lat, lon);
 	}
 
 	public static boolean clearPointToNavigate(Context ctx) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		return prefs.edit().remove(POINT_NAVIGATE_LAT).remove(POINT_NAVIGATE_LON).commit();
 	}
 
 	public static boolean setPointToNavigate(Context ctx, double latitude, double longitude) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		return prefs.edit().putFloat(POINT_NAVIGATE_LAT, (float) latitude).putFloat(POINT_NAVIGATE_LON, (float) longitude).commit();
 	}
 
 	public static final String LAST_SEARCHED_REGION = "last_searched_region"; //$NON-NLS-1$
 	public static final String LAST_SEARCHED_CITY = "last_searched_city"; //$NON-NLS-1$
 	public static final String lAST_SEARCHED_POSTCODE= "last_searched_postcode"; //$NON-NLS-1$
 	public static final String LAST_SEARCHED_STREET = "last_searched_street"; //$NON-NLS-1$
 	public static final String LAST_SEARCHED_BUILDING = "last_searched_building"; //$NON-NLS-1$
 	public static final String LAST_SEARCHED_INTERSECTED_STREET = "last_searched_intersected_street"; //$NON-NLS-1$
 
 	public static String getLastSearchedRegion(Context ctx) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		return prefs.getString(LAST_SEARCHED_REGION, ""); //$NON-NLS-1$
 	}
 
 	public static boolean setLastSearchedRegion(Context ctx, String region) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		Editor edit = prefs.edit().putString(LAST_SEARCHED_REGION, region).putLong(LAST_SEARCHED_CITY, -1).putString(LAST_SEARCHED_STREET,
 				"").putString(LAST_SEARCHED_BUILDING, ""); //$NON-NLS-1$ //$NON-NLS-2$
 		if (prefs.contains(LAST_SEARCHED_INTERSECTED_STREET)) {
 			edit.putString(LAST_SEARCHED_INTERSECTED_STREET, ""); //$NON-NLS-1$
 		}
 		return edit.commit();
 	}
 	
 	public static String getLastSearchedPostcode(Context ctx){
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		return prefs.getString(lAST_SEARCHED_POSTCODE, null);	
 	}
 	
 	public static boolean setLastSearchedPostcode(Context ctx, String postcode){
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		Editor edit = prefs.edit().putLong(LAST_SEARCHED_CITY, -1).putString(LAST_SEARCHED_STREET, "").putString( //$NON-NLS-1$
 				LAST_SEARCHED_BUILDING, "").putString(lAST_SEARCHED_POSTCODE, postcode); //$NON-NLS-1$
 		if(prefs.contains(LAST_SEARCHED_INTERSECTED_STREET)){
 			edit.putString(LAST_SEARCHED_INTERSECTED_STREET, ""); //$NON-NLS-1$
 		}
 		return edit.commit();
 	}
 
 	public static Long getLastSearchedCity(Context ctx) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		return prefs.getLong(LAST_SEARCHED_CITY, -1);
 	}
 
 	public static boolean setLastSearchedCity(Context ctx, Long cityId) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		Editor edit = prefs.edit().putLong(LAST_SEARCHED_CITY, cityId).putString(LAST_SEARCHED_STREET, "").putString( //$NON-NLS-1$
 				LAST_SEARCHED_BUILDING, ""); //$NON-NLS-1$
 		edit.remove(lAST_SEARCHED_POSTCODE);
 		if(prefs.contains(LAST_SEARCHED_INTERSECTED_STREET)){
 			edit.putString(LAST_SEARCHED_INTERSECTED_STREET, ""); //$NON-NLS-1$
 		}
 		return edit.commit();
 	}
 
 	public static String getLastSearchedStreet(Context ctx) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		return prefs.getString(LAST_SEARCHED_STREET, ""); //$NON-NLS-1$
 	}
 
 	public static boolean setLastSearchedStreet(Context ctx, String street) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		Editor edit = prefs.edit().putString(LAST_SEARCHED_STREET, street).putString(LAST_SEARCHED_BUILDING, ""); //$NON-NLS-1$
 		if (prefs.contains(LAST_SEARCHED_INTERSECTED_STREET)) {
 			edit.putString(LAST_SEARCHED_INTERSECTED_STREET, ""); //$NON-NLS-1$
 		}
 		return edit.commit();
 	}
 
 	public static String getLastSearchedBuilding(Context ctx) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		return prefs.getString(LAST_SEARCHED_BUILDING, ""); //$NON-NLS-1$
 	}
 
 	public static boolean setLastSearchedBuilding(Context ctx, String building) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		return prefs.edit().putString(LAST_SEARCHED_BUILDING, building).remove(LAST_SEARCHED_INTERSECTED_STREET).commit();
 	}
 
 	public static String getLastSearchedIntersectedStreet(Context ctx) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		if (!prefs.contains(LAST_SEARCHED_INTERSECTED_STREET)) {
 			return null;
 		}
 		return prefs.getString(LAST_SEARCHED_INTERSECTED_STREET, ""); //$NON-NLS-1$
 	}
 
 	public static boolean setLastSearchedIntersectedStreet(Context ctx, String street) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		return prefs.edit().putString(LAST_SEARCHED_INTERSECTED_STREET, street).commit();
 	}
 
 	public static boolean removeLastSearchedIntersectedStreet(Context ctx) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		return prefs.edit().remove(LAST_SEARCHED_INTERSECTED_STREET).commit();
 	}
 
 	public static final String SELECTED_POI_FILTER_FOR_MAP = "selected_poi_filter_for_map"; //$NON-NLS-1$
 
 	public static boolean setPoiFilterForMap(Context ctx, String filterId) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		return prefs.edit().putString(SELECTED_POI_FILTER_FOR_MAP, filterId).commit();
 	}
 
 	public static PoiFilter getPoiFilterForMap(Context ctx) {
 		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);
 		String filterId = prefs.getString(SELECTED_POI_FILTER_FOR_MAP, null);
 		PoiFilter filter = PoiFiltersHelper.getFilterById(ctx, filterId);
 		if (filter != null) {
 			return filter;
 		}
 		return new PoiFilter(null);
 	}
 }
