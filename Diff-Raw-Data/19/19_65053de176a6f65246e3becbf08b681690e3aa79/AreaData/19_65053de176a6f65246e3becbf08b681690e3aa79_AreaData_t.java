 package jm.org.data.area;
 
 import static android.provider.BaseColumns._ID;
 import static jm.org.data.area.AreaConstants.API_LIST;
 import static jm.org.data.area.AreaConstants.BING_RESULT_DATA;
 import static jm.org.data.area.AreaConstants.BING_SEARCH;
 import static jm.org.data.area.AreaConstants.BING_SEARCH_DATA;
 import static jm.org.data.area.AreaConstants.COUNTRY_LIST;
 import static jm.org.data.area.AreaConstants.COUNTRY_SEARCH_DATA;
 import static jm.org.data.area.AreaConstants.FATAL_ERROR;
 import static jm.org.data.area.AreaConstants.IDS_PARAM_DATA;
 import static jm.org.data.area.AreaConstants.IDS_RESULT_DATA;
 import static jm.org.data.area.AreaConstants.IDS_SEARCH;
 import static jm.org.data.area.AreaConstants.IDS_SEARCH_DATA;
 import static jm.org.data.area.AreaConstants.INDICATOR_KEYWORDS;
 import static jm.org.data.area.AreaConstants.INDICATOR_LIST;
 import static jm.org.data.area.AreaConstants.PERIOD_LIST;
 import static jm.org.data.area.AreaConstants.RETURN_CNTRY_IDs;
 import static jm.org.data.area.AreaConstants.RETURN_COUNTRIES;
 import static jm.org.data.area.AreaConstants.RETURN_DATE;
 import static jm.org.data.area.AreaConstants.RETURN_IND_ID;
 import static jm.org.data.area.AreaConstants.RETURN_KEYWORDS;
 import static jm.org.data.area.AreaConstants.RETURN_STRING;
 import static jm.org.data.area.AreaConstants.RETURN_VALUE;
 import static jm.org.data.area.AreaConstants.RETURN_WB_IND_ID;
 import static jm.org.data.area.AreaConstants.SEARCH_API_NONE;
 import static jm.org.data.area.AreaConstants.SEARCH_API_SOME;
 import static jm.org.data.area.AreaConstants.SEARCH_DATA;
 import static jm.org.data.area.AreaConstants.SEARCH_FAIL;
 import static jm.org.data.area.AreaConstants.SEARCH_SUCCESS;
 import static jm.org.data.area.AreaConstants.WB_SEARCH_DATA;
 import static jm.org.data.area.AreaConstants.WORLD_SEARCH;
 import static jm.org.data.area.DBConstants.*;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Hashtable;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 
 public class AreaData {
 	
 	private static final String TAG = AreaData.class.getSimpleName();
 	//private int currentApiVersion = android.os.Build.VERSION.SDK_INT;
 	ContentValues values;
 	Context context;
 	AreaDB dbHelper;
 	JSONParse parser;
 	APIPull dataService;
 	
 	ArrayList<String> countries_to_get;
 	ArrayList<Integer> countryIDs;
 	String[] keyWords;
 
 	public AreaData(Context context){
 		this.context = context;
 		dbHelper = new AreaDB(context);
 		dataService = new APIPull();
 	}
 	
 	// Initialize and Update Tables at Startup
 	public void updateAPIs(){
 		values = new  ContentValues();
 		values.put(API_NAME, "World Bank");
 		values.put(API_DESC, "1: World Bank Data API. Retrieves Macro-Economic Data from the World Bank datasets");
 		values.put(BASE_URI, "http://api.worldbank.org/");
 		insert(API, values, 0);
 		
 		values = new  ContentValues();
 		values.put(API_NAME, "IDS");
 		values.put(API_DESC, "2: Institute of Development Studies Data API. Retrieves Academic Publications from Eldis and Bribge datasets");
 		values.put(BASE_URI, "http://api.ids.ac.uk/openapi/");
 		insert(API, values, 0);
 		
 		values = new  ContentValues();
 		values.put(API_NAME, "Bing");
 		values.put(API_DESC, "3: Retrieves online articles, web sites and publications and other electronic data using the Microsoft Bing internet search engine.");
 		values.put(BASE_URI, "http://api.bing.net/json.aspx?");
 		insert(API, values, 0);
 	}
 	
 	public void updatePeriod(){
 		Calendar calendar = Calendar.getInstance();
 		
 		//{PERIOD_ID, PERIOD_NAME, P_START_DATE, P_END_DATE	
 		values = new  ContentValues();
 		values.put(PERIOD_NAME, "15 Years");
 		values.put(P_START_DATE, "1990");
 		values.put(P_END_DATE, "" +calendar.get(Calendar.YEAR));
 		insert(PERIOD, values,0);
 		
 		values = new  ContentValues();
 		values.put(PERIOD_NAME, "30 Years");
 		values.put(P_START_DATE, "1970");
 		values.put(P_END_DATE, "" +calendar.get(Calendar.YEAR));
 		insert(PERIOD, values,0);
 		
 	}
 	
 	public void updateIndicators(){
 		// pull data and put in database
 		parser = new JSONParse(context);
 		// error right here
 		int numOfIndicators = parser.getWBTotal(dataService.HTTPRequest(0,
 				"http://api.worldbank.org/topic/1/Indicator?per_page=1&format=json"));
 		if(numOfIndicators == 0 ){
 			// error in parsing JSON data
 			Log.e(TAG, "Error In Parsing JSON data");
 		}else{
 			parser.parseIndicators(dataService.HTTPRequest(0,
 					"http://api.worldbank.org/topic/1/Indicator?per_page="+ numOfIndicators +"&format=json"));
 		}
 	}
 	
 	public void updateCountries(){
 		parser = new JSONParse(context);
 		int numOfCountries = parser.getWBTotal(dataService.HTTPRequest(0,
 				"http://api.worldbank.org/country?per_page=1&format=json"));
 		if(numOfCountries == 0 ){
 			// error in parsing JSON data
 			Log.e(TAG, "Error In Parsing JSON data");
 		}else{
 			parser.parseCountries(dataService.HTTPRequest(0,
 					"http://api.worldbank.org/country?per_page="+ numOfCountries +"&format=json"));
 		}
 	}
 	/**
 	* Insert record into specified table. Includes duplication check to prevent double entries
 	*
 	* @param tableName Name of table record to be inserted into
 	* @param tableRecord Name-value pairs
 	*/
 	synchronized public long insert(String tableName, ContentValues tableRecord, int update) {
 		SQLiteDatabase db = dbHelper.getWritableDatabase();
 		int tableCode = AreaApplication.getTableCode(tableName);
 		long recordid = 0;
 		String tableKey = "";
 		String tableKeyAdd = "";
 		Cursor cursor = null;
 
 		//Getting correct primary key for tables
 		switch (tableCode) {
 			case INDICATOR_LIST:
 				tableKey = WB_INDICATOR_ID;
 				break;
 			case COUNTRY_LIST:
 				tableKey = WB_COUNTRY_ID;
 				break;
 			case API_LIST:
 				tableKey = API_NAME;
 				break;
 			case PERIOD_LIST:
 				tableKey = PERIOD_NAME;
 				break;
 			case SEARCH_DATA:
 				tableKey = I_ID;
 				tableKeyAdd = AP_ID;
 				break;
 			case COUNTRY_SEARCH_DATA:
 				tableKey = C_ID;
 				tableKeyAdd = S_ID;
 				break;
 			case WB_SEARCH_DATA:
 				tableKey = SC_ID;
 				tableKeyAdd = IND_DATE;
 				break;
 			case IDS_SEARCH_DATA:
 				tableKey = I_ID;
 				break;
 			case IDS_PARAM_DATA:
 				tableKey = IDS_PARAMETER;
 				tableKeyAdd = IDS_PARAM_VALUE;
 				break;
 			case IDS_RESULT_DATA:
 				tableKey = IDS_DOC_ID;
 				break;
 			case BING_SEARCH_DATA:
 				tableKey = BING_QUERY;
 				break;
 			case BING_RESULT_DATA:
 				tableKey = BING_URL;
 				break;
 		}
 		try{
 			//Duplicate Check
 			if (tableCode == COUNTRY_SEARCH_DATA || tableCode == WB_SEARCH_DATA || tableCode == SEARCH_DATA || tableCode == IDS_PARAM_DATA) {
 				cursor = db.query(tableName, null, String.format("%s='%s' AND %s='%s'", tableKey, tableRecord.get(tableKey), tableKeyAdd, tableRecord.get(tableKeyAdd)), null, null, null, null);
 				Log.e(TAG,String.format("%s='%s' AND %s='%s'", tableKey, tableRecord.get(tableKey), tableKeyAdd, tableRecord.get(tableKeyAdd)));
 			} else { //Special condition for double primary key on crop table
 				//cursor = db.query(tableName, null, String.format("%s=%s AND %s=%s", tableKey, tableRecord.get(tableKey), tableKeyAdd, tableRecord.get(tableKeyAdd)), null, null, null, null);
 				cursor = db.query(tableName, null, tableKey + "='" + tableRecord.get(tableKey) + "'", null, null, null, null);
 			}
 			
 			if (cursor.getCount() > 0) {
 				if(update == 1){
 					try {
 						cursor.moveToFirst();
 						recordid = cursor.getInt(cursor.getColumnIndex(_ID));
 						db.update(tableName, tableRecord, "" + _ID  + " ='" + recordid + "'", null );
 						
 						if(recordid != 1){
 							Log.e(TAG,"Error Updating "+ tableName +" Record: " + cursor.getInt(cursor.getColumnIndex(_ID)));
 						}else{
 							Log.d(TAG, "Updating "+ tableName +" Record: " + cursor.getInt(cursor.getColumnIndex(_ID)));
 						}
 						
 					}
 					catch (RuntimeException e) {
 						Log.e(TAG,"Error Updating Record: "+ cursor.getCount()+ "--=>" +e.toString());
 					}
 				}else{
 					Log.d(TAG, String.format("Record already exists in table %s", tableName));
 				}
 				
 			} else {
 				try {
 					recordid = db.insertOrThrow(tableName, null, tableRecord);
 					Log.d(TAG, String.format("Inserting into table %s", tableName));
 				}
 				catch (RuntimeException e) {
 					Log.e(TAG,"Indicator Insertion Exception: "+e.toString());
 				}
 			}
 			cursor.close();
 			db.close();
 		}catch(Exception e){
 			Log.e(TAG,"Cursor Exception: "+e.toString());
 		}
 		
 		return recordid;
 	}
 	
 	public int delete(String table, String where_clause){
 		SQLiteDatabase db = dbHelper.getWritableDatabase();
 		int rows_affected;
 		//try{
 		rows_affected = db.delete(table, where_clause, null);
 		//}catch(Exception e){
 			
 		//}
 		
 		return rows_affected;
 		
 	}
 	/******************************
 	 * Application Search Functions
 	 ******************************/
 	
 	/**
 	 * Searches database for the data related to a particular country. Returns int code 
 	 * indicating status of the data and query
 	 * @param countryName
 	 * @return AreaConstants Search Code 
 	 */
 	public int getCountryInfo(String countryName) {
 		//Check if info in database
 			// return result if locally present
 			// otherwise send query to Service
 		return SEARCH_FAIL;
 	}
 	
 	/**
 	 * @param countryID
 	 * @return AreaConstants Search Code
 	 */
 	public int getCountryInfo(int countryID) {
 		
 		return SEARCH_FAIL;
 	}
 	
 	/**
 	 * Global Search function that returns reports and articles for a single search term 
 	 * @param searchPhrase
 	 * @return AreaConstants Search Code
 	 */
 	public int globalSearch(int API, String searchPhrase) {
 		Calendar today, searchDate;
 		today = Calendar.getInstance();
 		Cursor ids_result, bing_result, records_to_delete;
 		String ids_table = IDS_SEARCH_PARAMS, bing_table = BING_SEARCH_TABLE;
 		String params, bingParam, deleteParam, ids_param_str = "";
 		int num_to_delete, num_deleted;
 		
 		
 		// for BING search check if the searchPrase has been the subject of a previous search
 		
 		bingParam = "" + BING_QUERY + " ='" + searchPhrase + "'";
 		bing_result = dbHelper.rawQuery(bing_table, "*", bingParam);
 		
 		//if it was the subject then data exists in the database
 		if (bing_result.getCount() ==1 ){
 			bing_result.moveToFirst();
 			// check if the previous search is over a week old
 			searchDate = getDate(bing_result.getString(bing_result.getColumnIndex(QUERY_DATE)));
 			if ((today.get(Calendar.WEEK_OF_YEAR) - searchDate.get(Calendar.WEEK_OF_YEAR)) >= 7){
 				// if it is then fetch new results from the API, delete old results. 
 				// delete old results first
 				//get current search ID
 				deleteParam = "" + B_S_ID + " ='" + bing_result.getString(bing_result.getColumnIndex(BING_SEARCH_ID)) + "'";
 				// get number of results to delete - for error checking 
 				records_to_delete = dbHelper.rawQuery(BING_SEARCH_RESULTS, "*", deleteParam);
 				num_deleted = delete(BING_SEARCH_RESULTS, deleteParam);
 				num_to_delete = records_to_delete.getCount();
 				if (num_to_delete != num_deleted){
 					return FATAL_ERROR;
 				}
 				getBingArticles(searchPhrase);
 				
 				
 			}else{
 				return SEARCH_SUCCESS;
 			}
 		}else{
 			//if searchPhrase was not the subject of a previous search then fetch data from BING API
 			getBingArticles(searchPhrase);
 		}
 		
 		//separate individual keywords from searchPhrase
 		keyWords = searchPhrase.split(" ");
 		// Format to IDS parameters.
 		for(int n = 0; n < keyWords.length; n++){
 			if(n==0){
 				ids_param_str = ids_param_str + keyWords[n];
 			}else{
 				ids_param_str = ids_param_str + "%26" + keyWords[n];
 			}
 		}
 
 		// Check IDS_SEARCH_PARAMS table to see if parameters exist for any previous searches
 		params    = "" + IDS_PARAM_VALUE + " ='" + ids_param_str + "'";
 		ids_result 	= dbHelper.rawQuery(ids_table, "*", params);
 		// if results are found for this indicator then we assume that all relevant articles would be in the database
 		if (ids_result.getCount() > 0){
 			return SEARCH_SUCCESS;
 		}else{
 			getDocuments(0, keyWords);
 		}		
 		
 		return SEARCH_FAIL;
 	}
 	
 	/**
 	 * Search function utilized by application to get data related to contextual usage
 	 * @param dataSource Code for data source query to be run against (WB|IDS|Bing)
 	 * @param indicatorID	Indicator ID
 	 * @param country	Array of country ids
 	 * @return AreaConstants Search Code
 	 */
 	synchronized public int genericSearch(int dataSource, String indicatorID, String[] country) {
 		//format data for querying
 		Hashtable<String, Object> return_data = new Hashtable<String, Object>();
 		
 		Cursor wb_result, ids_result, bing_result, ind_result, country_result, country_IDresult;
 		int ind_id, search_id, country_id = -1, period, in_country_id;
 		String params, bingParam,  wb_country_id = "";
 		Calendar today, searchDate;
 		today = Calendar.getInstance();
 		Cursor  records_to_delete;
 		String  deleteParam, ids_param_str = "";
 		int num_to_delete, num_deleted;
 		
 		boolean has_country = false;
 		String wb_table = SEARCH, ids_table = IDS_SEARCH_TABLE, bing_table = BING_SEARCH_TABLE;
 		String indicatorStr;
 		countries_to_get = new ArrayList<String>();
 		countryIDs		 = new ArrayList<Integer>();
 		
 		// get indicator ID from db
 		ind_result = dbHelper.rawQuery(INDICATOR, "*" , "" + WB_INDICATOR_ID + " ='" + indicatorID + "'");
 		
 		if (ind_result.getCount() != 1){
 			
 			return_data.put(RETURN_VALUE, "" + FATAL_ERROR);
 			ind_result.close();
 			return FATAL_ERROR;
 		}else{
 			Log.e(TAG,"" + ind_result.getCount() + " ID: " + ind_result.getColumnIndexOrThrow(_ID));
 			ind_result.moveToFirst();
 			Log.e(TAG,"" + ind_result.getCount() + " ID: " + ind_result.getString( ind_result.getColumnIndexOrThrow(_ID)));
 			ind_id = Integer.parseInt(ind_result.getString(ind_result.getColumnIndexOrThrow(_ID)));
 			indicatorStr = ind_result.getString(ind_result.getColumnIndexOrThrow(INDICATOR_NAME));
 
 			int pos;
 							
 			// find position of first parenthesis or comma to extract relevant words.
 			pos = indicatorStr.indexOf(",");
 			if (pos < 0){
 				pos = indicatorStr.indexOf("(");
 			}
 			// remove section of string after the comma or within and after the parenthesis
 			indicatorStr = indicatorStr.substring(0, pos-1);
 		}
 		ind_result.close();
 		
 		// if user opts out of synchronized search, then search only indicator that is passed in
 		if (dataSource == WORLD_SEARCH){
 			params    = "" + I_ID + " ='" + ind_id + "'";
 			
 			// query search table for API-Indicator combination. 
 			wb_result 	= dbHelper.rawQuery(wb_table, "*", params);
 			
 			
 			//only one search result should be returned per indicator and api combination.
 			if (wb_result.getCount() == 1){
 				//check db information starting with country data. 
 				wb_result.moveToFirst();
 				// if data exist for combination, check if there exist data exist for all countries.
 				// get countries list related to search record
 				country_result = dbHelper.rawQuery(SEARCH_COUNTRY, "*", ""+ S_ID +" ='" + wb_result.getInt(wb_result.getColumnIndex(_ID)) +"'");
 				if (country_result.getCount() > 0){
 					// check to see if country params for the current search are already within the database.
 					// Loop through the list of countries currently being searched for, Checking each against the list returned for the current indicator
 					for (int n = 0; n < country.length; n++){
 						// get country ID from country table using the country name or WB_Country _ID passed in
 						country_IDresult = dbHelper.rawQuery(COUNTRY,"*", ""+ COUNTRY_NAME +" ='" + country[n] +"'");
 						
 						if (country_IDresult.getCount() == 1){
 							country_IDresult.moveToFirst();
 							wb_country_id	= country_IDresult.getString(country_IDresult.getColumnIndex(WB_COUNTRY_ID));
 							in_country_id = country_IDresult.getInt(country_IDresult.getColumnIndex(COUNTRY_ID));
 							//Check through list of countries returned for search record for countries passed in
 							country_result.moveToFirst();
 							whileloop:while(! country_result.isAfterLast()){
 								
 								country_id 		= country_result.getInt(country_result.getColumnIndex(C_ID));
 								period			= country_result.getInt(country_result.getColumnIndex(P_ID));
 								
 								if (country_id == in_country_id ){
 									has_country = true;
 									break whileloop;
 								}
 								
 								country_result.moveToNext();
 							}
 							// if current country record is not in the db the update COUNTRIES_TO_GET array with country	
 							if(has_country){
 								has_country = false;
 							}else{
 								countries_to_get.add(wb_country_id);
 								countryIDs.add(in_country_id);
 							}
 							
 							
 						}else{
 							Log.e(TAG,"Error in retrieving Country information: " + country_IDresult.getCount() + " rows returned");
 							return_data.put(RETURN_VALUE, "" + FATAL_ERROR);
 							country_result.close();
 							return FATAL_ERROR;
 						}
 						country_IDresult.close();
 					}// end for
 					
 				}else{
 					// if 0 rows were returned, return error. As Initial search would have returned at least 1 country info. 
 					Log.e(TAG,"Error in retrieving Country information: " + country_result.getCount() + " rows returned");
 					return_data.put(RETURN_VALUE, "" + FATAL_ERROR);
 					country_result.close();
 					return FATAL_ERROR;
 				}
 					
 				// if some countries are missing then update
 				country_result.close();
 				
 			}else{
 			// if combination does not exist the get data for all countries.
 				
 				for(int n = 0; n < country.length; n++){
 					
 					//get country id to add to list of countries to retrieve
 					country_IDresult = dbHelper.rawQuery(COUNTRY,"*", ""+ COUNTRY_NAME +" ='" + country[n] +"'");
 					if(country_IDresult.getCount() != 1){
 						Log.e(TAG,"Error in retrieving Country information: " + country_IDresult.getCount() + " rows returned");
 						return_data.put(RETURN_VALUE, "" + FATAL_ERROR);
 						country_IDresult.close();
 						return FATAL_ERROR;
 					}else{
 						country_IDresult.moveToFirst();
 						country_IDresult.getInt(country_IDresult.getColumnIndex(WB_COUNTRY_ID));
 						countries_to_get.add(country_IDresult.getString(country_IDresult.getColumnIndex(WB_COUNTRY_ID)));
 						countryIDs.add(country_IDresult.getInt(country_IDresult.getColumnIndex(COUNTRY_ID)));
 					}
 					country_IDresult.close();
 				}
 				//wb_result.close();
 				//getCountryIndicators(ind_id, indicatorID, countries_to_get, countryIDs, "date=1990:2012");
 				return_data.put(RETURN_VALUE		, SEARCH_API_NONE	);
 				return_data.put(RETURN_IND_ID		, ind_id			);
 				return_data.put(RETURN_WB_IND_ID	, indicatorID		);
 				return_data.put(RETURN_COUNTRIES	, countries_to_get	);
 				return_data.put(RETURN_CNTRY_IDs	, countryIDs		);
 				return_data.put(RETURN_DATE			, "date=1990:2012"	);
 				//return SEARCH_SUCCESS;
 				
 			}
 			wb_result.close();
 			if(!countries_to_get.isEmpty()){
 				return getCountryIndicators(ind_id, indicatorID, countries_to_get, countryIDs, "date=1990:2012");
 				
 			}else{
 				Log.e(TAG, "No Values to get :)");
 				return_data.put(RETURN_VALUE, "" + SEARCH_SUCCESS);
 				return SEARCH_SUCCESS;
 			}
 			
 			
 		}else if(dataSource == IDS_SEARCH){
 			params    = "" + I_ID + " ='" + ind_id + "'";
 			
 			// query search table for API-Indicator combination. 
 			ids_result 	= dbHelper.rawQuery(ids_table, "*", params);
 			
 			if (ids_result.getCount() ==1 ){
 				// if results found for this indicator then we assume that all the relevant data is in the database.
 				return_data.put(RETURN_VALUE		, SEARCH_SUCCESS	);
 				ids_result.close();
 				return SEARCH_SUCCESS;
 				
 			}else{
 				// if no results then go to the API and pull the related values for this indicator.
 				//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 				// get searchable keywords from the indicator name string 
 				// havent wrote it yet
 				// will use hashtable instead
 				
 				//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 				
 				ids_result.close();
 				// break up indicator string 
 				populateKeywords();
 				indicatorStr = INDICATOR_KEYWORDS.get(indicatorID);
 				keyWords = indicatorStr.split(" ");
 				return getDocuments(ind_id, keyWords);
 				
 				
 				/*if(keyWords.length <= 2 ){
 					// if 2 or less keywords the go ahead and search
 					getDocuments(ind_id, keyWords);
 				}else{
 					// else check for keywords to be removed before searching
 					// remove unnecessary keywords
 					// Perform a search of the IDS API
 					getDocuments(ind_id, keyWords);
 					
 				}*/
 				//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 				
 			}			
 			
 		}else if(dataSource == BING_SEARCH){
 			bingParam = "" + BING_QUERY + " ='" + indicatorStr + "'";
 			
 			// query search table for API-Indicator combination. 
 			bing_result = dbHelper.rawQuery(bing_table, "*", bingParam);
 			
 			if (bing_result.getCount() ==1 ){
 				// if the indicator data is found then the assumption is that relevant results are in the database
 				bing_result.moveToFirst();
 				// check if the previous search is over a week old
 				searchDate = getDate(bing_result.getString(bing_result.getColumnIndex(QUERY_DATE)));
 				if ((today.get(Calendar.WEEK_OF_YEAR) - searchDate.get(Calendar.WEEK_OF_YEAR)) >= 7){
 					// if it is then fetch new results from the API, delete old results. 
 					// delete old results first
 					//get current search ID
 					deleteParam = "" + B_S_ID + " ='" + bing_result.getString(bing_result.getColumnIndex(BING_SEARCH_ID)) + "'";
 					// get number of results to delete - for error checking 
 					records_to_delete = dbHelper.rawQuery(BING_SEARCH_RESULTS, "*", deleteParam);
 					num_deleted = delete(BING_SEARCH_RESULTS, deleteParam);
 					num_to_delete = records_to_delete.getCount();
 					if (num_to_delete != num_deleted){
 						
 						return_data.put(RETURN_VALUE		, FATAL_ERROR	);
 						bing_result.close();
 						return FATAL_ERROR;
 					}
 					getBingArticles(indicatorStr);
 					return_data.put(RETURN_VALUE		, SEARCH_API_NONE	);
 					return_data.put(RETURN_STRING		, indicatorStr		);
 					
 					
 				}else{
 					
 					return_data.put(RETURN_VALUE		, SEARCH_SUCCESS	);
 					bing_result.close();
 					return SEARCH_SUCCESS;
 				}
 			}else{
 				getBingArticles(indicatorStr);
 				return_data.put(RETURN_VALUE		, SEARCH_API_NONE	);
 				return_data.put(RETURN_STRING		, indicatorStr		);
 				bing_result.close();
 				return SEARCH_SUCCESS;
 			}
 							
 			bing_result.close();
 		}else{
 			//Search only for indicator passed in
 			
 		}
 		// if all APIs should be searched, then start with one passed in.
 		
 		
 		return_data.put(RETURN_VALUE	, SEARCH_FAIL	);
 		return  SEARCH_FAIL;
 	}
 
 	public Cursor getIndicatorList(){
 		Cursor result; 
 		
 		result = dbHelper.rawQuery(INDICATOR, "*", "");
 		
 		return result;
 	}
 	
 	public Cursor getCountryList(){
 		Cursor result; 
 		
 		result = dbHelper.rawQuery(COUNTRY, COUNTRY_NAME, "");
 		
 		return result;
 	}
 
 	public boolean inDatabase(String s_table, String params){
 		Cursor s_result; 
 		
 		// query search table for API-Indicator combination. 
 		s_result = dbHelper.rawQuery(s_table, "*", params);
 		
 		// if data exist for combination, check if there exist data exist for all countries. 
 		if (s_result.getCount() != 0){
 			// if some countries are missing then update
 			
 			 
 		}else{
 		// if combination does not exist the get data for all countries.
 		
 		}
 		return true;
 	}
 	
 	public Cursor getData(int dataSource, String indicatorID, String[] country){
 		
 		Cursor cursor, search_cursor, ind_result, wb_result, country_result, country_IDresult;
 		String table = "", indicatorStr, params = "";
 		int ind_id, in_country_id, country_id, period, search_country_id;
 		Integer[] search_country_array;
 		
 		
 		
 		ind_result = dbHelper.rawQuery(INDICATOR, "*" , "" + WB_INDICATOR_ID + " ='" + indicatorID + "'");
 		
 		if (ind_result.getCount() != 1){
 			Log.e(TAG, "Error retrieving Indicatror Information: indicator - " + indicatorID );
 			return null;
 		}else{
 			Log.e(TAG,"" + ind_result.getCount() + " ID: " + ind_result.getColumnIndexOrThrow(_ID));
 			ind_result.moveToFirst();
 			Log.e(TAG,"" + ind_result.getCount() + " ID: " + ind_result.getString( ind_result.getColumnIndexOrThrow(_ID)));
 			ind_id = Integer.parseInt(ind_result.getString(ind_result.getColumnIndexOrThrow(_ID)));
 			indicatorStr = ind_result.getString(ind_result.getColumnIndexOrThrow(INDICATOR_NAME));
 
 			int pos;
 							
 			// find position of first parenthesis or comma to extract relevant words.
 			pos = indicatorStr.indexOf(",");
 			if (pos < 0){
 				pos = indicatorStr.indexOf("(");
 			}
 			// remove section of string after the comma or within and after the parenthesis
 			indicatorStr = indicatorStr.substring(0, pos-1);
 		}
 		ind_result.close();
 		
 		switch (dataSource) {
 			case WORLD_SEARCH:				
 				table = WB_DATA;
 				
 				params    = "" + I_ID + " ='" + ind_id + "'";
 				// get search id Corresponding to search table
 				wb_result = rawQuery(SEARCH, "*", params);
 				// get corresponding search country results that relate to that indicator
 				//only one search result should be returned per indicator and api combination.
 				if (wb_result.getCount() == 1){
 					//check db information starting with country data. 
 					wb_result.moveToFirst();
 					// if data exist for combination, check if there exist data exist for all countries.
 					// get countries list related to search record
 					country_result = dbHelper.rawQuery(SEARCH_COUNTRY, "*", ""+ S_ID +" ='" + wb_result.getInt(wb_result.getColumnIndex(_ID)) +"'");
 					if (country_result.getCount() > 0){
 						// check to see if country params for the current search are already within the database.
 						// Loop through the list of countries currently being searched for, Checking each against the list returned for the current indicator
 						for (int n = 0; n < country.length; n++){
 							// get country ID from country table using the country name or WB_Country _ID passed in
 							country_IDresult = dbHelper.rawQuery(COUNTRY,"*", ""+ COUNTRY_NAME +" ='" + country[n] +"'");
 							
 							if (country_IDresult.getCount() == 1){
 								country_IDresult.moveToFirst();
 								//wb_country_id	= country_IDresult.getString(country_IDresult.getColumnIndex(WB_COUNTRY_ID));
 								in_country_id = country_IDresult.getInt(country_IDresult.getColumnIndex(COUNTRY_ID));
 								//Check through list of countries returned for search record for countries passed in
 								country_result.moveToFirst();
 								whileloop:while(! country_result.isAfterLast()){
 									
 									country_id 			= country_result.getInt(country_result.getColumnIndex(C_ID));
 									period				= country_result.getInt(country_result.getColumnIndex(P_ID));
 									search_country_id	= country_result.getInt(country_result.getColumnIndex(_ID ));
 									if (country_id == in_country_id ){
 										countryIDs.add(search_country_id);
 										break whileloop;
 									}
 									
 									country_result.moveToNext();
 									
 								}							
 								
 							}else{
 								Log.e(TAG,"Error in retrieving Country information: " + country_IDresult.getCount() + " rows returned");
 								
 								return null;
 							}
 							country_IDresult.close();
 						}// end for
 						
 					}else{
 						// if 0 rows were returned, return error. As Initial search would have returned at least 1 country info. 
 						Log.e(TAG,"Error in retrieving Country information: " + country_result.getCount() + " rows returned");
 						
 						return null;
 					}
 						
 					
 					country_result.close();
 					// update cursor to be returned with the country data
 					search_country_array = (Integer[])countryIDs.toArray(new Integer[countryIDs.size()]);
 					for(int a = 0; a < search_country_array.length; a++){
 						if(a == 0){
 							params =  "" + SC_ID + " = '" + search_country_array[a] + "'";
 						}else{
 							params = params + " and " + SC_ID + " = '" + search_country_array[a] + "'";
 						}
 					}
 					 
 				}else{
 					Log.e(TAG,"No Search Info: " + wb_result.getCount() + " rows returned");
 					
 					return null;
 				}
 				wb_result.close();
 				break;
 			case IDS_SEARCH:
 				
 				table = IDS_SEARCH_RESULTS;
 				 
 				params    = "" + I_ID + " ='" + ind_id + "'";
 				
 				// query search table for API-Indicator combination. 
 				search_cursor = rawQuery(IDS_SEARCH_TABLE, "*", params);
 				
 				if (search_cursor.getCount() ==1 ){
 					search_cursor.moveToFirst();
 					params = "" + IDS_S_ID + " ='" + search_cursor.getInt(search_cursor.getColumnIndex(IDS_SEARCH_ID))+ "'";
 					
 				}else{
 					Log.e(TAG,"No Search Info: " +search_cursor.getCount() + " rows returned");
 					
 					return null;
 									
 				}
 				search_cursor.close();
 				break;
 			case BING_SEARCH:
 				
 				table = BING_SEARCH_RESULTS;
 				params = "" + BING_QUERY + " ='" + indicatorStr + "'";
 				
 				// query search table for API-Indicator combination. 
 				search_cursor = dbHelper.rawQuery(BING_SEARCH_TABLE, "*", params);
 				
 				if (search_cursor.getCount() ==1 ){
 					// if the indicator data is found then the assumption is that relevant results are in the database
 					search_cursor.moveToFirst();
 					params = "" + B_S_ID + " ='" + search_cursor.getInt(search_cursor.getColumnIndex(BING_SEARCH_ID))+ "'";
 				}else{
 					Log.e(TAG,"No Search Info: " +search_cursor.getCount() + " rows returned");
 					
 					return null;				
 				}
 				search_cursor.close();
 				break;
 			
 		}
 		cursor = rawQuery(table, "*", params);
 		Log.e(TAG, String.format("Params: %s. Table: %s", params, table));
 		return cursor;
 	}
 	
 	public int getCountryIndicators(int indicator_id, String indicator, ArrayList<String> countries, ArrayList<Integer> countryIDList, String date){
 		parser = new JSONParse(context);
 		String queryStr = "http://api.worldbank.org/countries/";
 		String countryString = "";
 		String[] countryArray;
 		Integer[] countryIDArray;
 		Log.e(TAG, "Num of Countries:" + countryIDList.size());
 		countryArray = (String[]) countries.toArray(new String[countries.size()]);
 		countryIDArray = (Integer[])countryIDList.toArray(new Integer[countryIDList.size()]);
 		
 		
 		for(int n = 0; n < countryArray.length; n++){
 			// create country String for API call
 			if(n == 0){
 				countryString = countryArray[n];
 			}else{
 				countryString = countryString + ";" + countryArray[n];
 			}
 		}
 		
 		queryStr = queryStr + countryString + "/indicators/" + indicator + "?per_page=1&" + date + "&format=json";
 		
 		int numOfRecords = parser.getWBTotal(dataService.HTTPRequest(0,queryStr));
 		if(numOfRecords == 0 ){
 			// error in parsing JSON data
 			Log.e(TAG, "Error In Parsing JSON data:" + queryStr);
 			return SEARCH_FAIL;
 		}else{
 			
 			queryStr = "http://api.worldbank.org/countries/" + countryString + "/indicators/" + indicator + "?per_page="+ numOfRecords +"&" + date + "&format=json";
 			return parser.parseWBData(dataService.HTTPRequest(0,queryStr), indicator_id, countryIDArray, queryStr);
 			
 		}
 		
 		
 	}
 	
 	public int getDocuments(int indicator, String[] parameters){
 		parser = new JSONParse(context);
 		String querybase = "http://api.ids.ac.uk/openapi/";
 		int return_int;
 		String site = "eldis/", object = "documents/", parameter="q", num_results = "num_results=50";
		String extras = "extra_fields=description+author+publication_year+publisher+publication_date";
 		String queryStr;
 		String paramStr = "";
 		for(int n = 0; n < parameters.length; n++){
 			if(n==0){
 				paramStr = paramStr + parameters[n];
 			}else{
 				paramStr = paramStr + "%26" + parameters[n];
 			}
 		}
 		
 		queryStr = querybase + site + "search/" + object + "?" + parameter  + "=" + paramStr + "&" + extras + "&" +num_results;
 		//queryStr = "http://api.ids.ac.uk/openapi/eldis/search/documents/?q=Agriculture%26materials&num_results=50";
 		return_int =  parser.parseIDSData(dataService.HTTPRequest(1,queryStr), indicator, paramStr, queryStr);
 		if(return_int > 0){
 			return SEARCH_SUCCESS;
 		}else{
 			return SEARCH_FAIL;
 		}
 			
 	}
 	
 	public int getBingArticles(String param){
 		parser = new JSONParse(context);
 		String querybase = "http://api.bing.net/json.aspx?";
 		
 		String api_id = "Appid=", query = "query=", source="sources=web", num_results = "web.count=30";
 		String apiKey = "814D155520085B9C88670EA1B4DD2B6E082EEC9F";
 		String queryStr;
 		String paramStr = "";
 		String[] parameters = param.split(" ");
 		
 		for(int n = 0; n < parameters.length; n++){
 			if(n==0){
 				paramStr = paramStr + parameters[n];
 			}else{
 				paramStr = paramStr + "%20" + parameters[n];
 			}
 		}
 		
 		queryStr = querybase + api_id + apiKey + "&"+ query + paramStr + "&" + source + "&" + num_results;
 		
 		
 		return parser.parseBINGData(dataService.HTTPRequest(0,queryStr), param, queryStr);
 		
 	}
 
 	public String[] getCountry() {
 		SQLiteDatabase db = dbHelper.getReadableDatabase();
 		Cursor cursorCountry = db.query(COUNTRY, new String[] {COUNTRY_NAME}, null, null, COUNTRY_NAME, null, null);
 		String[] countryArray;
 		
 		if(cursorCountry.getCount() > 0) {
 			countryArray = new String[cursorCountry.getCount()];
 			int i = 0;
 			
 			while(cursorCountry.moveToNext()) {
 				countryArray[i] = cursorCountry.getString(cursorCountry.getColumnIndex(COUNTRY_NAME));
 				i++;
 			}
 		} else {
 			countryArray = new String[] {};
 		}
 		cursorCountry.close();
 		db.close();
 		
 		return countryArray;
 	}
 	
 	public int getIndicatorID(String indicator){
 		int id = -1;
 		Cursor ind_result;
 		
 		ind_result = dbHelper.rawQuery(INDICATOR, "*" , "" + WB_INDICATOR_ID + " ='" + indicator + "'");
 		
 		if (ind_result.getCount() != 1){
 			Log.e(TAG, "Error retrieving Indicatror Information: indicator - " + indicator );
 			return id;
 		}else{
 			Log.e(TAG,"" + ind_result.getCount() + " ID: " + ind_result.getColumnIndexOrThrow(_ID));
 			ind_result.moveToFirst();
 			Log.e(TAG,"" + ind_result.getCount() + " ID: " + ind_result.getString( ind_result.getColumnIndexOrThrow(_ID)));
 			
 			id = Integer.parseInt(ind_result.getString(ind_result.getColumnIndexOrThrow(_ID)));
 		}
 		ind_result.close();
 		return id;
 	}
 	
 	public String getIndicatorName(String indicator){
 		String indicatorStr = "";
 		Cursor ind_result;
 		
 		ind_result = dbHelper.rawQuery(INDICATOR, "*" , "" + WB_INDICATOR_ID + " ='" + indicator + "'");
 		
 		if (ind_result.getCount() != 1){
 			Log.e(TAG, "Error retrieving Indicatror Information: indicator - " + indicator );
 			return indicatorStr;
 		}else{
 			Log.e(TAG,"" + ind_result.getCount() + " ID: " + ind_result.getColumnIndexOrThrow(_ID));
 			ind_result.moveToFirst();
 			Log.e(TAG,"" + ind_result.getCount() + " ID: " + ind_result.getString( ind_result.getColumnIndexOrThrow(_ID)));
 			
 			indicatorStr = ind_result.getString(ind_result.getColumnIndexOrThrow(INDICATOR_NAME));
 		}
 		ind_result.close();
 		return indicatorStr;
 	}
 	
 	public double[][] getIndicatorList(int Indicator_id, String countryStr, int period){
 		double [][] values = null;
 		int search_country_id, country_id, search_id;
 		String params;
 		Cursor search, search_country, country, wb_data;
 		params = "" + I_ID + " ='" + Indicator_id + "'";
 		// get search id Corresponding to search table
 		search = rawQuery(SEARCH, "*", params);
 		// get 
 		if(search.getCount() == 1){
 			search.moveToFirst();
 			search_id = search.getInt(search.getColumnIndex(_ID));
 			country = dbHelper.rawQuery(COUNTRY,"*", ""+ COUNTRY_NAME +" ='" + countryStr +"'");
 			if (country.getCount() == 1){
 				country.moveToFirst();
 				country_id = country.getInt(country.getColumnIndex(_ID));
 				
 				search_country = dbHelper.rawQuery(SEARCH_COUNTRY, "*", ""+ S_ID +" ='" + search_id +"' and "
 						+ C_ID +" ='" + country_id +"'" );
 				if(search_country.getCount() == 1){
 					search_country.moveToFirst();
 					search_country_id = search_country.getInt(search_country.getColumnIndex(_ID));
 					Log.e(TAG,String.format("SC_ID %s - _ID %s ", search_country_id, search_country.getInt(search_country.getColumnIndex(_ID)) ));
 					wb_data = dbHelper.rawQuery(WB_DATA,"*", ""+ SC_ID +" ='" + search_country_id +"'" +" ORDER BY " + IND_DATE);
 					
 					if(wb_data.getCount() > 0){
 						
 						values = new double[2][wb_data.getCount()];
 						for(int n = 0; n < wb_data.getCount(); n++){
 							wb_data.moveToNext();
 							values[0][n] = (double) wb_data.getInt(wb_data.getColumnIndex(IND_DATE));
 							values[1][n] = wb_data.getDouble(wb_data.getColumnIndex(IND_VALUE));
 						}
 						
 						wb_data.close();
 						
 					}else{
 						wb_data.close();
 						values = new double[0][0];
 					}
 						
 				}else{
 					search_country.close();
 					values = new double[0][0];
 				}
 				search_country.close();
 			}else{
 				country.close();
 				values = new double[0][0];
 			}
 			country.close();
 		}else{
 			search.close();
 			values = new double[0][0];
 		}
 		search.close();
 		
 		
 		return values;
 	}
 	
 	public Cursor rawQuery(String tableName, String tableColumns, String queryParams) {
 		
 		Cursor cursor = null;
 		cursor = dbHelper.rawQuery(tableName, tableColumns, queryParams);
 		
 		return cursor;
 	}
 	
 	private Calendar getDate(String formattedDateStr){
 		Calendar calendar = null;
 		SimpleDateFormat format = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
 		
 		try{
 			Date date = (Date)format.parse(formattedDateStr);
 			calendar = Calendar.getInstance();
 			calendar.setTime(date);
 			
 		}catch (ParseException e){
 			Log.e(TAG,"Exception in parsing date string "+e.toString());
 		}
 		
 		
 		 return calendar;
 	}
 	
 	public void populateKeywords(){
 		INDICATOR_KEYWORDS.put("AG.AGR.TRAC.NO",	"agriculture machine"		);
 		INDICATOR_KEYWORDS.put("AG.CON.FERT.MT",	"fertilizer consumption"	);
 		INDICATOR_KEYWORDS.put("AG.CON.FERT.PT.ZS",	"fertilizer consumption"	);
		INDICATOR_KEYWORDS.put("AG.CON.FERT.ZS",	"fertilizer"				);
 		INDICATOR_KEYWORDS.put("AG.LND.AGRI.K2",	"agriculture"				);
 		INDICATOR_KEYWORDS.put("AG.LND.AGRI.ZS",	"agricultural land"			);
 		INDICATOR_KEYWORDS.put("AG.LND.ARBL.HA",	"arable land"				);
 		INDICATOR_KEYWORDS.put("AG.LND.ARBL.HA.PC",	"arable land"				);
 		INDICATOR_KEYWORDS.put("AG.LND.ARBL.ZS",	"land usage"				);
 		INDICATOR_KEYWORDS.put("AG.LND.CREL.HA",	"cereal production"			);
 		INDICATOR_KEYWORDS.put("AG.LND.CROP.ZS",	"cropland"					);
 		INDICATOR_KEYWORDS.put("AG.LND.FRST.K2",	"forrest area"				);
 		INDICATOR_KEYWORDS.put("AG.LND.FRST.ZS",	"forrest area"				);
 		INDICATOR_KEYWORDS.put("AG.LND.IRIG.AG.ZS",	"irrigated land"			);
 		INDICATOR_KEYWORDS.put("AG.LND.PRCP.MM",	"precipitation"				);
 		INDICATOR_KEYWORDS.put("AG.LND.TOTL.K2",	"land area"					);
 		INDICATOR_KEYWORDS.put("AG.LND.TRAC.ZS",	"tractors"					);
 		INDICATOR_KEYWORDS.put("AG.PRD.CROP.XD",	"crop production"			);
 		INDICATOR_KEYWORDS.put("AG.PRD.FOOD.XD",	"food production"			);
 		INDICATOR_KEYWORDS.put("AG.PRD.LVSK.XD",	"livestock production"		);
 		INDICATOR_KEYWORDS.put("AG.SRF.TOTL.K2",	"land area"					);
 		INDICATOR_KEYWORDS.put("AG.YLD.CREL.KG",	"cereal"					);
 		INDICATOR_KEYWORDS.put("EA.PRD.AGRI.KD",	"agriculture worker"		);
 		INDICATOR_KEYWORDS.put("EN.AGR.EMPL",		"agriculture employ"		);
 		INDICATOR_KEYWORDS.put("NV.AGR.TOTL.ZS",	"agriculture growth"		);
 		INDICATOR_KEYWORDS.put("SH.H2O.SAFE.RU.ZS",	"water source"				);
 		INDICATOR_KEYWORDS.put("SI.POV.RUGP",		"rural poverty"				);
 		INDICATOR_KEYWORDS.put("SI.POV.RUHC",		"poverty line"				);
 		INDICATOR_KEYWORDS.put("SL.AGR.EMPL.ZS",	"agriculture employment"	);
 		INDICATOR_KEYWORDS.put("SP.RUR.TOTL",		"rural population"			);
 		INDICATOR_KEYWORDS.put("SP.RUR.TOTL.ZG",	"population growth"			);
 		INDICATOR_KEYWORDS.put("SP.RUR.TOTL.ZS",	"rural population"			);
 		INDICATOR_KEYWORDS.put("TM.VAL.AGRI.ZS.UN",	"agriculture imports"		);
 		INDICATOR_KEYWORDS.put("TX.VAL.AGRI.ZS.UN",	"agriculture exports"		);
 		
 	}
 	
 	private class AreaDB extends SQLiteOpenHelper{
 		
		private static final int DATABASE_VERSION = 7;
 		private SQLiteDatabase db;
 		
 		
 		public AreaDB(Context ctx) {
 			super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 		
 		private static final String CREATE_TABLE_COUNTRY = "create table " + COUNTRY + " ( "
 				+ COUNTRY_ID 			+ " integer primary key autoincrement, "
 				+ WB_COUNTRY_ID 		+ " text not null, "
 				+ WB_COUNTRY_CODE		+ " text not null, "
 				+ COUNTRY_NAME 			+ " text not null, "
 				+ CAPITAL_CITY			+ " text not null, "
 				+ INCOME_LEVEL_ID 		+ " text not null, "
 				+ INCOME_LEVEL_NAME 	+ " text not null, "
 				+ POPULATION 			+ " integer, "
 				+ COUNTRY_REGION_ID 	+ " text not null, "
 				+ COUNTRY_REGION_NAME 	+ " text not null, "
 				+ GDP 					+ " integer, "
 				+ GNI_CAPITA 			+ " integer, "
 				+ POVERTY 				+ " integer, "
 				+ LIFE_EX 				+ " integer, "
 				+ LITERACY 				+ " integer)";
 		
 		
 		private static final String CREATE_TABLE_INDICATOR = "create table " + INDICATOR + " ( "
 				+ INDICATOR_ID 		+ " integer primary key autoincrement, "
 				+ WB_INDICATOR_ID 	+ " integer not null, "
 				+ INDICATOR_NAME 	+ " text not null, "
 				+ INDICATOR_DESC 	+ " text not null )";
 
 		private static final String CREATE_TABLE_SEARCH = "create table " + SEARCH + " ( "
 				+ SEARCH_ID 		+ " integer primary key autoincrement, "
 				+ I_ID				+ " integer not null, "
 				+ AP_ID 			+ " integer not null, "
 				+ SEARCH_CREATED 	+ " datetime not null, "
 				+ SEARCH_MODIFIED 	+ " datetime not null, "
 				+ SEARCH_URI 		+ " text not null )" ;
 		
 		private static final String CREATE_TABLE_IDS_SEARCH = "create table " + IDS_SEARCH_TABLE + " ( "
 				+ IDS_SEARCH_ID 		+ " integer primary key autoincrement, "
 				+ I_ID 					+ " integer not null, "
 				+ IDS_BASE_URL			+ " text not null,"
 				+ IDS_SITE				+ " text not null, "
 				+ IDS_OBJECT 			+ " text not null )" ;
 		
 		private static final String CREATE_TABLE_IDS_SEARCH_PARAMS = "create table " + IDS_SEARCH_PARAMS + " ( "
 				+ _ID 				+ " integer primary key autoincrement, "
 				+ IDS_S_ID			+ " integer not null,"
 				+ IDS_PARAMETER		+ " text not null, "
 				+ IDS_OPERAND		+ " text not null, "
 				+ IDS_PARAM_VALUE	+ " text not null, "
 				+ COMBINATION 			+ " text not null )" ;
 		
 		private static final String CREATE_TABLE_IDS_SEARCH_RESULTS = "create table " + IDS_SEARCH_RESULTS + " ( "
 				+ _ID 				+ " integer primary key autoincrement, "
 				+ IDS_S_ID			+ " integer not null,"
 				+ IDS_DOC_URL		+ " text not null, "
 				+ IDS_DOC_ID		+ " text not null, "
 				+ IDS_DOC_TYPE		+ " text not null, "
 				+ IDS_DOC_TITLE		+ " text not null, "
 				+ IDS_DOC_AUTH_STR	+ " text not null, "
 				+ IDS_DOC_PUB		+ " text not null, "
 				+ IDS_DOC_DATE		+ " text not null, "
 				+ IDS_DOC_DESC		+ " text not null, "
 				+ IDS_DOC_PATH 		+ " text not null )" ;
 		
 		
 		private static final String CREATE_TABLE_API = "create table " + API + " ( "
 				+ API_ID 			+ " integer primary key autoincrement, "
 				+ API_NAME 			+ " text not null, "
 				+ API_DESC 			+ " text not null, " 
 				+ BASE_URI 			+ " text not null )";
 		
 		private static final String CREATE_TABLE_BING_SEARCH = "create table " + BING_SEARCH_TABLE + " ( "
 				+ BING_SEARCH_ID	+ " integer primary key autoincrement, "
 				+ BING_QUERY		+ " text not null, "
 				+ QUERY_DATE		+ " datetime not null)";		
 		//public static final String[] FROM_BING_SEARCH_TABLE		= {BING_SEARCH_ID, BING_QUERY, QUERY_DATE };
 
 		private static final String CREATE_TABLE_BING_SEARCH_RESULTS = "create table " + BING_SEARCH_RESULTS + " ( "
 				+ _ID	 			+ " integer primary key autoincrement, "
 				+ B_S_ID			+ " integer not null,"
 				+ BING_TITLE 		+ " text not null, "
 				+ BING_DESC 		+ " text not null, " 
 				+ BING_URL			+ " text not null, "
 				+ BING_DISP_URL		+ " text not null, "
 				+ BING_DATE_TIME	+ " text not null )";
 		//public static final String[] FROM_BING_SEARCH_RESULTS	= {_ID, B_S_ID, BING_TITLE, BING_DESC, BING_URL, BING_DISP_URL, BING_DATE_TIME	};
 
 		private static final String CREATE_TABLE_WB_DATA = "create table " + WB_DATA + " ( "
 				+ WB_DATA_ID 	+ " integer primary key autoincrement, "
 				//+ I_ID 			+ " integer not null, "
 				//+ C_ID 			+ " integer not null, "
 				//+ S_ID 			+ " integer not null, "
 				+ SC_ID 		+ " integer not null, "
 				+ IND_VALUE 	+ " double not null, "
 				+ IND_DECIMAL 	+ " integer not null, "
 				+ IND_DATE 		+ " integer not null ) ";
 			
 		private static final String CREATE_TABLE_SEARCH_COUNTRY = "create table " + SEARCH_COUNTRY + " ( "
 				+ _ID 	+ " integer primary key autoincrement, "
 				+ S_ID 	+ " integer not null, "
 				+ C_ID 	+ " integer not null, "
 				+ P_ID 	+ " integer not null )";
 		
 		private static final String CREATE_TABLE_PERIOD = "create table " + PERIOD + " ( "
 				+ PERIOD_ID 	+ " integer primary key autoincrement, "
 				+ PERIOD_NAME 	+ " text not null, "
 				+ P_START_DATE 	+ " integer not null, "
 				+ P_END_DATE 	+ " integer not null )";
 
 		private static final String CREATE_TABLE_IDS_DATA = "create table " + IDS_DATA  + " ( "
 				+ DOCUMENT_ID 		+ " integer primary key autoincrement, "
 				+ S_ID				+ " integer not null, "
 				+ DOC_TITLE 		+ " text not null, "
 				+ LANGUAGE_NAME		+ " text not null, "
 				+ LICENCE_TYPE 		+ " text not null, "
 				+ PUBLICATION_DATE 	+ " datetime not null, "
 				+ PUBLISHER 		+ " text not null, "
 				+ PUBLISHER_COUNTRY + " text not null, "
 				+ JOURNAL_SITE 		+ " text not null, "
 				+ DOC_NAME 			+ " text not null, "
 				+ DATE_CREATED		+ " datetime not null, "
 				+ DATE_UPDATED 		+ " datetime not null, "
 				+ WEBSITE_URL 		+ " text not null )";
 		
 		private static final String CREATE_TABLE_IDS_AUTHOR = "create table " + IDS_AUTHOR + " ( "
 				+ AUTHOR_ID		 + " integer primary key autoincrement, "
 				+ D_ID			 + " integer not null, "
 				+ AUTHOR_NAME	 + " text not null )";
 		
 		private static final String CREATE_TABLE_IDS_DOC_THEME = "create table " + IDS_DOC_THEME + " ( "
 				+ _ID 		+ " integer primary key autoincrement, "
 				+ T_ID 		+ " integer not null, "
 				+ D_ID 		+ " integer not null )";
 		
 		private static final String CREATE_TABLE_IDS_THEME = "create table " + IDS_THEME + " ( "
 				+ THEME_ID 		+ " integer primary key autoincrement, "
 				+ THEME_NAME 	+ " text not null, "
 				+ IDS_THEME_ID 	+ " text not null, "
 				+ THEME_URL 	+ " text not null, "
 				+ THEME_LEVEL 	+ " integer not null)" ;
 		
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			try {
 				db.execSQL(CREATE_TABLE_COUNTRY															);
 				Log.d("AREA", "Create COUNTRY table: " + CREATE_TABLE_COUNTRY					);
 				
 				db.execSQL(CREATE_TABLE_INDICATOR														);
 				Log.d("AREA", "Create INDICATOR table: " + CREATE_TABLE_INDICATOR				);
 				
 				db.execSQL(CREATE_TABLE_SEARCH															);
 				Log.d("AREA", "Create SEARCH table: " + CREATE_TABLE_SEARCH						);
 				
 				db.execSQL(CREATE_TABLE_IDS_SEARCH														);
 				Log.d("AREA", "Create SEARCH table: " + CREATE_TABLE_IDS_SEARCH					);
 				
 				db.execSQL(CREATE_TABLE_IDS_SEARCH_PARAMS												);
 				Log.d("AREA", "Create SEARCH table: " + CREATE_TABLE_IDS_SEARCH_PARAMS			);
 				
 				db.execSQL(CREATE_TABLE_IDS_SEARCH_RESULTS										);
 				Log.d("AREA", "Create SEARCH table: " + CREATE_TABLE_IDS_SEARCH_RESULTS					);
 				
 				db.execSQL(CREATE_TABLE_API														);
 				Log.d("AREA", "Create API table: " + CREATE_TABLE_API									);
 				
 				db.execSQL(CREATE_TABLE_BING_SEARCH												);
 				Log.d("AREA", "Create API table: " + CREATE_TABLE_BING_SEARCH							);
 				
 				db.execSQL(CREATE_TABLE_BING_SEARCH_RESULTS										);
 				Log.d("AREA", "Create API table: " + CREATE_TABLE_BING_SEARCH_RESULTS					);
 				
 				db.execSQL(CREATE_TABLE_WB_DATA													);
 				Log.d("AREA", "Create WB_DATA table: " + CREATE_TABLE_WB_DATA							);
 				
 				db.execSQL(CREATE_TABLE_SEARCH_COUNTRY											);
 				Log.d("AREA", "Create SEARCH_COUNTRY table: " + CREATE_TABLE_SEARCH_COUNTRY				);
 
 				db.execSQL(CREATE_TABLE_PERIOD													);
 				Log.d("AREA", "Create PERIOD table: " + CREATE_TABLE_PERIOD								);
 				
 				db.execSQL(CREATE_TABLE_IDS_DATA												);
 				Log.d("AREA", "Create IDS_DATA table: " + CREATE_TABLE_IDS_DATA							);
 				
 				db.execSQL(CREATE_TABLE_IDS_AUTHOR												);
 				Log.d("AREA", "Create IDS_AUTHOR table: " + CREATE_TABLE_IDS_AUTHOR						);
 				
 				db.execSQL(CREATE_TABLE_IDS_DOC_THEME											);
 				Log.d("AREA", "Create IDS_DOC_THEME table: " + CREATE_TABLE_IDS_DOC_THEME				);
 				
 				db.execSQL(CREATE_TABLE_IDS_THEME												);
 				Log.d("AREA", "Create IDS_THEME table: " + CREATE_TABLE_IDS_THEME						);
 				
 				
 			} catch (RuntimeException e) {
 			Log.d("AREA", "Unable to create tables: ");
 			}
 		}
 		
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			
 			Log.w("AREA", "Upgrading database from version " + oldVersion + " to "
 					+ newVersion + ", which will destroy all old data");
 			try {
 				db.execSQL("DROP TABLE IF EXISTS " + COUNTRY				);
 				db.execSQL("DROP TABLE IF EXISTS " + INDICATOR				);
 				db.execSQL("DROP TABLE IF EXISTS " + SEARCH					);
 				db.execSQL("DROP TABLE IF EXISTS " + IDS_SEARCH_TABLE		);
 				db.execSQL("DROP TABLE IF EXISTS " + IDS_SEARCH_PARAMS		);
 				db.execSQL("DROP TABLE IF EXISTS " + IDS_SEARCH_RESULTS		);
 				db.execSQL("DROP TABLE IF EXISTS " + API					);
 				db.execSQL("DROP TABLE IF EXISTS " + BING_SEARCH_TABLE		);
 				db.execSQL("DROP TABLE IF EXISTS " + BING_SEARCH_RESULTS	);
 				db.execSQL("DROP TABLE IF EXISTS " + WB_DATA				);
 				db.execSQL("DROP TABLE IF EXISTS " + SEARCH_COUNTRY			);
 				db.execSQL("DROP TABLE IF EXISTS " + PERIOD					);
 				db.execSQL("DROP TABLE IF EXISTS " + IDS_DATA				);
 				db.execSQL("DROP TABLE IF EXISTS " + IDS_AUTHOR				);
 				db.execSQL("DROP TABLE IF EXISTS " + IDS_DOC_THEME			);
 				db.execSQL("DROP TABLE IF EXISTS " + IDS_THEME 				);
 			} catch (SQLException e) {
 				Log.d("AREA", "Upgrade step: " + "Unable to DROP TABLES");
 			}
 
 			onCreate(db);
 		}
 		
 		synchronized public Cursor rawQuery(String tableName, String tableColumns, String queryParams) {
 			db = this.getReadableDatabase();
 			Cursor cursor = null;
 			String query;
 			if (queryParams.equals("")){
 				query = "SELECT "+ tableColumns + " FROM " + tableName ;
 			}else{
 				query = "SELECT "+ tableColumns + " FROM " + tableName +" WHERE " + queryParams;
 			}
 			
 			try {
 				cursor = db.rawQuery(query, null);
 				Log.d("AREA", "Raw Query: " + query);
 				Log.d("AREA", "Raw Query Result: Returned " + cursor.getCount() + " record(s)");
 			} catch (SQLException e) {
 				Log.e("AREA", "Raw Query Exception: " + e.toString());
 			} catch (IllegalStateException ilEc){
 				db.close();
 				return rawQuery(tableName, tableColumns,queryParams);
 			}
 			db.close();
 			return cursor;
 		}
 		
 		
 	}
 }
