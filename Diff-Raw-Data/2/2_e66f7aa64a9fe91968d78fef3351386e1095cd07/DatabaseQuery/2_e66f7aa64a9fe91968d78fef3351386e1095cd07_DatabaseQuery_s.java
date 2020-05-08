 package me.mattsutter.conditionred.util;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 
 import static me.mattsutter.conditionred.util.DatabaseQueryHelper.*;
 
 
 /** Class that deals with all of the transactions with the radar
  * database.  This is made in an effort to simplify the code elsewhere.
  * 
  * @author Matt Sutter
  */
 public class DatabaseQuery{
 	
 	private static SQLiteDatabase radar_db;
 	private static DatabaseQueryHelper db_helper;
 	
 	private static boolean is_loaded = false;
 	
 	/**
 	 * Opens the database (if it's not already loaded).
 	 * @param context - Application {@link Context} in which to find the database.
 	 */
 	public static void open(Context context){
 		if (!is_loaded){
 			db_helper = new DatabaseQueryHelper(context);
 			is_loaded = loadDatabase();
 		}
 	}
 	
 	/**
 	 * Loads the database.
 	 * @return True if opened successfully, false otherwise.
 	 */
 	private synchronized static boolean loadDatabase(){
 		try{
 			radar_db = db_helper.getWritableDatabase();
 		}
 		catch (SQLiteException sqle){
 			radar_db = db_helper.getReadableDatabase();
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Closes the database (if it's not already closed).
 	 */
 	public static void close(){
 		if (is_loaded){
 			radar_db.close();
 			is_loaded = false;
 		}
 	}
 
 	/**	Searches the database for the url for a given product (distinguished by product code
 	 * 	and angle).  
 	 * @param product_type - The product you want.
 	 * @param angle - The angle of the product. Put zero here if there is no angle.
 	 * 
 	 * 	@return Returns the URL extension of for the product. Returns null if something went wrong.
 	 */
 
 	public static String getProductURL(int product_type, int angle){
 		String result;
 		final String[] from = {PROD_URL};
 		final String where = 	PROD_TYPE + "=\""
 						+ Integer.toString(product_type) + "\" and " 
 						+ PROD_ANGLE + "=\"" 
 						+ Integer.toString(angle) + "\" AND "
 						+ PROD_ENABLED + "=1";		
 
 		final Cursor returnedQuery = radar_db.query(true, PRODUCT_TABLE, from, where, null, null, null, null, null);
 
 		// Just to make sure there is actually a first row and that the cursor is there.
 		if (returnedQuery.moveToFirst())
 			result = returnedQuery.getString(0);
 		else
 			result = null;
 
 		returnedQuery.close();
 		return result;
 	}
 	
 	/**	Searches the database for the url for a given product (distinguished by product code
 	 * 	and angle).  
 	 * @param site_id - Four letter designator for the site (e.g. "KMKX").
 	 * 
 	 * 	@return Returns the URL extension of for the product. Returns null if something went wrong.
 	 */
 
 	public static String getSiteCity(String site_id){
 		String result;
 		final String query = "SELECT DISTINCT city FROM sites WHERE site_id=\"" + site_id + "\"";
 		
 		final Cursor returnedQuery = radar_db.rawQuery(query, null);
 			
 		// Just to make sure there is actually a first row and that the cursor is there.
 		if (returnedQuery.moveToFirst())
 			result = returnedQuery.getString(0);
 		else
 			result = null;
 		
 		returnedQuery.close();
 		return result;
 	}
 		
 	/**
 	 * Find the URL extension for a given product using the name of the product.
 	 * @param prod_name - Short name of the product.
 	 * @return The URL extension. Returns null if something went wrong.
 	 */
 	public static String getProductURL(String prod_name){
 		String result;
 		final String query = "SELECT DISTINCT " + PROD_URL 
 			+ " FROM products WHERE " + PROD_NAME 
 			+ "=\"" + prod_name + "\" AND "
 			+ PROD_ENABLED + "=1";	
 		
 		final Cursor returnedQuery = radar_db.rawQuery(query, null);
 			
 		// Just to make sure there is actually a first row and that the cursor is there.
 		if (returnedQuery.moveToFirst())
 			result = returnedQuery.getString(0);
 		else
 			result = null;
 		
 		returnedQuery.close();
 		return result;
 	}
 		
 	/** Searches the database for the URL of a radar site.
 	 * @param city - City name of the site.
 	 * @param state - Two letter acronym (e.g "AK").
 	 * @return Returns the URL for a given site. Returns null if something went wrong. 
 	 */
 	public static String getSiteURL(String city, String state){
 		String result;
 		final String[] from = {SITE_URL};
 		final String where = SITE_ID + "=\"" + DatabaseQuery.getSite(city, state) + "\"";	
 		
 		final Cursor returnedQuery = radar_db.query(true, SITE_TABLE, from, where, null, null, null, null, null);
 			
 		// Just to make sure there is actually a first row and that the cursor is there.
 		if (returnedQuery.moveToFirst())
 			result = returnedQuery.getString(0);
 		else
 			result = null;
 		
 		returnedQuery.close();
 		return result;
 	}
 
 	/** Searches the database for the URL of a radar site.
 	 * @param site - Four letter designator for the site (e.g. "KMKX").
 	 * @return Returns the URL for a given site. Returns null if something went wrong. 
 	 */
 	public static String getSiteURL(String site){
 		String result;
 		final String[] from = {SITE_URL};
 		final String where = SITE_ID + "=\"" + site + "\"";	
 	
 		final Cursor returnedQuery = radar_db.query(true, SITE_TABLE, from, where, null, null, null, null, null);
 			
 		// Just to make sure there is actually a first row and that the cursor is there.
 		if (returnedQuery.moveToFirst())
 			result = returnedQuery.getString(0);
 		else
 			result = null;
 		
 		returnedQuery.close();
 		return result;
 	}
 
 	/**	
 	 * Searches the database for all of the cities in a given state.
 	 * @param state - Two letter acronym (e.g "AK").
 	 * @return Returns a list of the city names for all of the sites 
 	 *  in a given state, in
 	 *  ascending order. Returns null if something went wrong.  
 	 */
 	public static Cursor getStateCities(String state){
 		final String query = "SELECT city, _id FROM sites WHERE state=\"" + state + "\" GROUP BY city ORDER BY city ASC";
 		
 		return radar_db.rawQuery(query, null);
 	}
 
 	/**
 	 * Generates a list of all of the states in the database.
 	 * @return Returns a {@link Cursor} containing the states.
 	 */
 	public static Cursor getStates(){
 		final String query = "SELECT state, _id FROM sites GROUP BY state ORDER BY state ASC";
 		
 		return radar_db.rawQuery(query, null);
 	}
 	
 	/**
 	 * Generates a list of all of the site in the database.
 	 * @return Returns a {@link Cursor} containing the sites.
 	 */
 	public static Cursor getSites(){
 		final String query = "SELECT site_id, _id FROM sites";
 		
 		return radar_db.rawQuery(query, null);
 	}
 	
 	/**
 	 * Generates a {@link Cursor} containing all of the sites in the database
 	 * with their respective latitude and longitude (both fixed-point integers; 
 	 * three fractional digits).
	 * @return {@link Cursor} with the rows: site_id (0), lat (1), long (2), 
 	 * and primary integer key (3).
 	 */
 	public static Cursor getSitesAndLatLng(){
 		final String query = "SELECT site_id, lat, long, _id FROM sites";
 		
 		return radar_db.rawQuery(query, null);
 	}
 	
 	/**
 	 * Selects all of the sites within certain latitude and longitude bounds. 
 	 * @param lat1 - Latitude lower bound
 	 * @param lat2 - Latitude upper bound
 	 * @param long1 - Longitude lower bound
 	 * @param long2 - Longitude upper bound
 	 * @return {@link Cursor} containing all of the sites within the bounds.
 	 */
 	public static Cursor getSitesBetween(int lat1, int lat2, int long1, int long2){
 		final String query = "SELECT site_id FROM sites WHERE lat BETWEEN " 
 			+ lat1 + " AND " + lat2 + " AND long BETWEEN " + long1 + " AND " + long2;
 		
 		return radar_db.rawQuery(query, null);
 	}
 
 	/**	
 	 * Searches the database for the site ID (four letter designator) for a given city name and, *sigh*,
 	 * 	state (damn you Wilmington!).
 	 * @param city - City name of the site.
 	 * @param state - Two letter acronym (e.g "AK").
 	 * @return Returns the site ID.  Returns null if something went wrong. 
 	 */
 	public static String getSite(String city, String state){
 		String result;
 		final String[] from = {SITE_ID};
 		final String where = 	SITE_STATE + "=\"" 
 						+ state + "\" AND " 
 						+ SITE_CITY + "=\"" 
 						+ city + "\"";
 		
 		final Cursor returnedQuery = radar_db.query(true, SITE_TABLE, from, where, null, null, null, null, null);
 			
 		// Just to make sure there is actually a first row and that the cursor is there.
 		if (returnedQuery.moveToFirst())
 			result = returnedQuery.getString(0);
 		else
 			result = null;
 		
 		returnedQuery.close();
 		return result;
 	}
 
 	/**
 	 * Find the latitude and longitude for a given site.
 	 * @param site - Site's four letter acronym. (e.g. KMKX).
 	 * @return Integer array.  [0] latitude; [1] longitude.  Returns Integer.MAX_VALUE is something went wrong.
 	 */
 	public static int[] getLatLong(String site){
 		final int[] result = new int[2];
 		final String query = "SELECT DISTINCT lat, long FROM sites WHERE site_id=\"" + site + "\"";
 		
 		final Cursor returnedQuery = radar_db.rawQuery(query, null);
 			
 		// Just to make sure there is actually a first row and that the cursor is there.
 		if (returnedQuery.moveToFirst()){
 			result[0] = returnedQuery.getInt(0);
 			result[1] = returnedQuery.getInt(1);
 		}
 		else{
 			result[0] = Integer.MAX_VALUE;
 			result[1] = Integer.MAX_VALUE;
 		}
 		returnedQuery.close();
 		return result;
 	}
 
 	/**
 	 * Generates a list of all the products in the database;
 	 * @return {@link Cursor} containing the list of products.
 	 */
 	public static Cursor getProductNames(){
 		final String query = "SELECT " + PROD_NAME 
 			+ ", _id FROM products WHERE " + PROD_ENABLED 
 			+ "=1 GROUP BY name ORDER BY name ASC"; 		// The argument for the FROM SQL command.
 			
 		return radar_db.rawQuery(query, null);
 	}
 		
 	/**
 	 * Finds a product's code given its complementary URL extension.
 	 * @param product_url - URL extension for the product you're looking for.
 	 * @return Product code.  Returns {@link Integer.MAX_VALUE} if something went wrong.
 	 */
 	public static int getProductCode(String product_url){
 		int result;
 		final String query = "SELECT DISTINCT " + PROD_TYPE 
 			+ ", _id FROM products WHERE " + PROD_URL 
 			+ "=\"" + product_url + "\" AND "
 			+ PROD_ENABLED + "=1"; 		// The argument for the FROM SQL command.
 			
 		final Cursor returnedQuery = radar_db.rawQuery(query, null);
 			
 		// Just to make sure there is actually a first row and that the cursor is there.
 		if (returnedQuery.moveToFirst())
 			result = returnedQuery.getInt(0);
 		else
 			result = Integer.MAX_VALUE;
 		
 		returnedQuery.close();
 		return result;
 	}
 		
 	/**
 	 * Find the elevation angle of the product.
 	 * @param product_name - Short name of the product.
 	 * @return Elevation angle.  Default is 5.
 	 */
 	public static int getProductAngle(String product_name){
 		int result;
 		final String query = "SELECT DISTINCT " + PROD_ANGLE 
 			+ ", _id FROM products WHERE " + PROD_NAME 
 			+ "=\"" + product_name + "\" AND "
 			+ PROD_ENABLED + "=1";
 		
 		final Cursor returnedQuery = radar_db.rawQuery(query, null);
 			
 		// Just to make sure there is actually a first row and that the cursor is there.
 		if (returnedQuery.moveToFirst())
 			result = returnedQuery.getInt(0);
 		else
 			result = 5;
 		
 		returnedQuery.close();
 		return result;
 	}
 	
 	/**
 	 * Finds the elevation angle for a product given its URL extension.
 	 * @param prod_url - URL extension for the product you're looking for.
 	 * @return Elevation angle.  Default is 5.
 	 */
 	public static int getProductAngleFromURL(String prod_url){
 		int result;
 		final String query = "SELECT DISTINCT angle, _id FROM products WHERE " 
 			+ PROD_URL + "=\"" + prod_url + "\" AND "
 			+ PROD_ENABLED + "=1";
 		
 		final Cursor returnedQuery = radar_db.rawQuery(query, null);
 			
 		// Just to make sure there is actually a first row and that the cursor is there.
 		if (returnedQuery.moveToFirst())
 			result = returnedQuery.getInt(0);
 		else
 			result = 5;
 		
 		returnedQuery.close();
 		return result;
 	}
 	
 	/**
 	 * Finds a the short name of a product given its URL extension.
 	 * @param prod_url - URL extension for the product you're looking for.
 	 * @return Short name of the product.
 	 */
 	public static String getProductNameFromURL(String prod_url){
 		final String query = "SELECT DISTINCT " + PROD_NAME 
 					+ ",_id FROM products WHERE " + PROD_URL 
 					+ "=\"" + prod_url + "\" AND "
 					+ PROD_ENABLED + "=1";
 		
 		final Cursor returned_query = radar_db.rawQuery(query, null);
 		
 		String result;
 		
 		if (returned_query.moveToFirst())
 			result = returned_query.getString(0);
 		else
 			result = "";
 		
 		returned_query.close();
 		return result;
 	}
 	
 	/**
 	 * Finds the product code and elevation angle for a product given its URL extension.
 	 * @param prod_url - URL extension for the product you're looking for.
 	 * @return {@link Integer} array: product code [0], elevation angle [1]. Returns 
 	 * {@link Integer.MAX_VALUE} if something went wrong.
 	 */
 	public static int[] getProductTypeAndAngle(String prod_url){
 		final int[] result = new int[2];
 		final String query = "SELECT DISTINCT " + PROD_TYPE + ", " 
 			+ PROD_ANGLE + ", _id FROM products WHERE " 
 			+ PROD_URL + "=\"" + prod_url + "\" AND "
 			+ PROD_ENABLED + "=1";
 		
 		final Cursor returnedQuery = radar_db.rawQuery(query, null);
 
 		// Just to make sure there is actually a first row and that the cursor is there.
 		if (returnedQuery.moveToFirst()){
 			result[0] = returnedQuery.getInt(0);
 			result[1] = returnedQuery.getInt(1);
 		}
 		else{
 			result[0] = Integer.MAX_VALUE;
 			result[1] = Integer.MAX_VALUE;
 		}		
 
 		returnedQuery.close();
 		return result;
 	}
 }
