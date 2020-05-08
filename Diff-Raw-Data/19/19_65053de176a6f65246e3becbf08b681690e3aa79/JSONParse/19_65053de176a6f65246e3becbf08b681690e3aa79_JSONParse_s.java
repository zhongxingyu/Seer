 
 package jm.org.data.area;
 
 
 import static android.provider.BaseColumns._ID;
 import static jm.org.data.area.AreaConstants.BING_SEARCH_LIST;
 import static jm.org.data.area.AreaConstants.IDS_SEARCH_DOC_AUTH;
 import static jm.org.data.area.AreaConstants.IDS_SEARCH_LIST;
 import static jm.org.data.area.AreaConstants.SEARCH_FAIL;
 import static jm.org.data.area.AreaConstants.WB_COUNTRY_LIST;
 import static jm.org.data.area.AreaConstants.WB_DATA_LIST;
 import static jm.org.data.area.AreaConstants.WB_IND_LIST;
 import static jm.org.data.area.DBConstants.AP_ID;
 import static jm.org.data.area.DBConstants.BING_QUERY;
 import static jm.org.data.area.DBConstants.BING_SEARCH_RESULTS;
 import static jm.org.data.area.DBConstants.BING_SEARCH_TABLE;
 import static jm.org.data.area.DBConstants.B_S_ID;
 import static jm.org.data.area.DBConstants.COMBINATION;
 import static jm.org.data.area.DBConstants.COUNTRY;
 import static jm.org.data.area.DBConstants.C_ID;
 import static jm.org.data.area.DBConstants.FROM_BING_SEARCH_RESULTS;
 import static jm.org.data.area.DBConstants.FROM_COUNTRY;
 import static jm.org.data.area.DBConstants.FROM_IDS_SEARCH_RESULTS;
 import static jm.org.data.area.DBConstants.FROM_INDICATOR;
 import static jm.org.data.area.DBConstants.FROM_WB_DATA;
 import static jm.org.data.area.DBConstants.IDS_BASE_URL;
 import static jm.org.data.area.DBConstants.IDS_DOC_PATH;
 import static jm.org.data.area.DBConstants.IDS_OBJECT;
 import static jm.org.data.area.DBConstants.IDS_OPERAND;
 import static jm.org.data.area.DBConstants.IDS_PARAMETER;
 import static jm.org.data.area.DBConstants.IDS_PARAM_VALUE;
 import static jm.org.data.area.DBConstants.IDS_SEARCH_PARAMS;
 import static jm.org.data.area.DBConstants.IDS_SEARCH_RESULTS;
 import static jm.org.data.area.DBConstants.IDS_SEARCH_TABLE;
 import static jm.org.data.area.DBConstants.IDS_SITE;
 import static jm.org.data.area.DBConstants.IDS_S_ID;
 import static jm.org.data.area.DBConstants.INDICATOR;
 import static jm.org.data.area.DBConstants.I_ID;
 import static jm.org.data.area.DBConstants.P_ID;
 import static jm.org.data.area.DBConstants.QUERY_DATE;
 import static jm.org.data.area.DBConstants.SC_ID;
 import static jm.org.data.area.DBConstants.SEARCH;
 import static jm.org.data.area.DBConstants.SEARCH_COUNTRY;
 import static jm.org.data.area.DBConstants.SEARCH_CREATED;
 import static jm.org.data.area.DBConstants.SEARCH_MODIFIED;
 import static jm.org.data.area.DBConstants.SEARCH_URI;
 import static jm.org.data.area.DBConstants.S_ID;
 import static jm.org.data.area.DBConstants.WB_COUNTRY_CODE;
 import static jm.org.data.area.DBConstants.WB_DATA;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Hashtable;
 import java.util.regex.PatternSyntaxException;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.util.Log;
 
 public class JSONParse {
 	private static final String TAG = JSONParse.class.getSimpleName();
 	private AreaData areaData;
 	//private Context appContext;
 	private ContentValues apiRecord;
 	
 	
 	public JSONParse(Context context){
 		//appContext = context;
 		areaData = new AreaData(context);
 		apiRecord = new ContentValues();
 		
 	}
 	
 	public String parseWB(String jsonData){
 		//ArrayList indicators = new ArrayList(1);
 		//String jsonText = "";
 		StringBuilder jsonText = new StringBuilder();
 		jsonText.append("Number");
 		try {
 			//jsonText.append("Number");
 			JSONArray jsonArray = new JSONArray(jsonData);
 			//jsonText.append(": 1 test");
 			//Log.i(ParseJSON.class.getName(),
 				//	"Number of entries " + jsonArray.length());
 			
 			jsonText.append("Number of entries " + String.valueOf(jsonArray.length()));
 			
 			jsonText.append("\n-------------------------------------\n");
 			JSONObject jsonObject = jsonArray.getJSONObject(0);
 			jsonText.append("Total: " +jsonObject.getString("total"));
 			
 			int numofObjects = Integer.parseInt(jsonObject.getString("total"));
 			if (numofObjects > 0){
 				jsonArray = jsonArray.getJSONArray(1);
 			}
 			
 			for (int i = 0; i < 10; i++) {
 				JSONObject jsonInnerObject = jsonArray.getJSONObject(i);
 				
 				//jsonObject.
 				jsonText.append(jsonInnerObject.toString()); 
 				//jsonText.append("\n-------------------------------------\n");
 				//Log.i(ParseJSON.class.getName(), jsonObject.getString("text"));
 						
 				// get the text of tweet from feed from the twitter feed
 				//jsonText.append(jsonObject.getString("text"));
 				
 				// get the text id from
 				//jsonText.append(jsonObject.getString("name"));
 
 				jsonText.append("\n--------------------------------------\n\n");
 				JSONArray names = jsonInnerObject.names();
 				for(int x = 0; x < names.length(); x++){
 					
 					if(jsonInnerObject.optJSONObject(names.getString(x)) == null){
 						jsonText.append("\n" + names.getString(x) + ": " + jsonInnerObject.getString(names.getString(x)));
 					}else{
 						jsonText.append("\n" + names.getString(x) + ": ");
 						jsonText.append("\n\t" +jsonInnerObject.optJSONObject(names.getString(x)).toString());
 					}
 				}
 				jsonText.append("\n--------------------------------------\n");
 				jsonText.append("--------------------------------------\n\n");
 				
 			}					
 			
 		} catch (Exception e) {
 			//e.printStackTrace();
 			Log.d("some", e.toString());
 			jsonText.append("\n--------------------------------------\n\n");
 		}
 		
 		return jsonText.toString();
 	}
 	
 	public int parseBINGData(String jsonData, String params, String uri){
 		Hashtable<String, String> bing_data = new Hashtable<String, String>();
 		long search_id = 0;
 		JSONArray resultArray;
 		try{
 			JSONObject jsonObject 	= new JSONObject(jsonData);
 			JSONObject response		= jsonObject.getJSONObject("SearchResponse");
 			JSONObject results		= response.getJSONObject("Web");
 			
 			int numOfrecords  = Integer.parseInt(results.getString("Total"));
 			
 			if(numOfrecords > 0){
 				resultArray = results.getJSONArray("Results");
 				String date = timeStamp();
 				// create Search record if it doesn't exist;
 				apiRecord = new ContentValues();
 				apiRecord.put(BING_QUERY	, params	);
 				apiRecord.put(QUERY_DATE	, date		);
 				//public static final String[] FROM_BING_SEARCH_TABLE		= {BING_SEARCH_ID, BING_QUERY, QUERY_DATE };
 				search_id = areaData.insert(BING_SEARCH_TABLE, apiRecord, 1);
 				if(search_id <= 0){
 					Log.e(TAG, "Error inserting BING Search record: query -" + params +  "URI-" + uri);
 					return SEARCH_FAIL;
 				}
 				
 				
 			}else{
 				Log.e(TAG, "Error NO data retrieved from IDS API: URL-" + uri);
 				return SEARCH_FAIL;
 			}
 			if(numOfrecords > 30){
 				numOfrecords = 30;
 			}
 			// get Data returned from the IDS
 			// update the IDS_SEARCH_RESULT table with the documents information
 			for (int i = 0; i < numOfrecords; i++) {
 				apiRecord = new ContentValues();
 				bing_data = parseJSON( bing_data, resultArray.getJSONObject(i), "");
 				
 				// add the IDS Search ID first
 				apiRecord.put(B_S_ID, search_id);
 				
 				for (int a = 0; a < BING_SEARCH_LIST.length; a++){
 					apiRecord.put(FROM_BING_SEARCH_RESULTS[a+2], (String)bing_data.get(BING_SEARCH_LIST[a]));	
 					Log.d("Indicators", ""+ FROM_BING_SEARCH_RESULTS[a+2] + ":-> " + (String)bing_data.get(BING_SEARCH_LIST[a]));
 				}
 				
 				areaData.insert(BING_SEARCH_RESULTS, apiRecord, 0);
 				
 			}
 			
 		}catch (Exception e){
 			Log.e(TAG, e.toString());
 			return SEARCH_FAIL;
 		}
 		return (int) search_id;
 	}
 	public int parseIDSData(String jsonData, int indicator, String params, String uri){
 		Hashtable<String, String> ids_data = new Hashtable<String, String>();
 		long search_id = 0;
 		JSONArray resultArray;
 		try{
 			JSONObject jsonObject 	= new JSONObject(jsonData);
 			
 			JSONObject metadata		= jsonObject.getJSONObject("metadata");
 			int numReturned  = Integer.parseInt(metadata.getString("total_results"));
 			
 			if(numReturned > 0){
 				resultArray = jsonObject.getJSONArray("results");
 				String site = "eldis/", object = "document/", parameter="keyword", querybase = "http://api.ids.ac.uk/openapi/";
 				// create Search record if it doesn't exist;
 				apiRecord = new ContentValues();
 				apiRecord.put(I_ID			, indicator	);
 				apiRecord.put(IDS_BASE_URL	, querybase	);
 				apiRecord.put(IDS_SITE		, site		);
 				apiRecord.put(IDS_OBJECT	, object	);
 				//String[] FROM_IDS_SEARCH= {IDS_SEARCH_ID, I_ID, IDS_BASE_URL, IDS_SITE, IDS_OBJECT};
 				search_id = areaData.insert(IDS_SEARCH_TABLE, apiRecord, 1);
 				if(search_id > 0){
 					//Log.e(TAG, "Num of Parameters:" + params.length);
 					//for(int n = 0; n < params.length; n++){
 					//Log.e(TAG, "Parameter:" + params[n]);
 					apiRecord = new ContentValues();
 					apiRecord.put(IDS_S_ID 			, search_id	);
 					apiRecord.put(IDS_PARAMETER 	, parameter	);
 					apiRecord.put(IDS_OPERAND  		, "="		);
 					apiRecord.put(IDS_PARAM_VALUE	, params	);
 					apiRecord.put(COMBINATION  		, 0			);
 					//FROM_IDS_SEARCH_PARAMS		= {_ID, IDS_S_ID, IDS_PARAMETER, IDS_OPERAND, IDS_PARAM_VALUE, COMBINATION		};
 					areaData.insert(IDS_SEARCH_PARAMS, apiRecord, 1);
 					//FROM_IDS_SEARCH_PARAMS		= {_ID, IDS_S_ID, IDS_PARAMETER, IDS_OPERAND, IDS_PARAM_VALUE, COMBINATION		};
 					//}
 				}else{
 					Log.e(TAG, "Error inserting IDS Search record: Indicator-" + indicator +  "URI-" + uri);
 					return SEARCH_FAIL;
 				}
 				
 				
 			}else{
 				Log.e(TAG, "Error NO data retrieved from IDS API: URL-" + uri);
 				return SEARCH_FAIL;
 			}
 			
 			// get Data returned from the IDS
 			// update the IDS_SEARCH_RESULT table with the documents information
 			for (int i = 0; i < numReturned; i++) {
 				apiRecord = new ContentValues();
 				ids_data = parseJSON( ids_data, resultArray.getJSONObject(i), "");
 				
 				// add the IDS Search ID first
 				apiRecord.put(IDS_S_ID, search_id);
 				
				for (int a = 0; a < IDS_SEARCH_LIST.length; a++){
 					apiRecord.put(FROM_IDS_SEARCH_RESULTS[a+2], (String)ids_data.get(IDS_SEARCH_LIST[a]));	
 					Log.d("Indicators", ""+ FROM_IDS_SEARCH_RESULTS[a+2] + ":-> " + (String)ids_data.get(IDS_SEARCH_LIST[a]));
 				}
 				apiRecord.put(IDS_DOC_PATH, "");
 				areaData.insert(IDS_SEARCH_RESULTS, apiRecord, 0);
 				
 			}
 			
 		}catch (Exception e){
 			Log.e(TAG, e.toString());
 			return SEARCH_FAIL;
 		}
 		return (int) search_id;
 	}
 
 	public int parseWBData(String jsonData, int indicator, Integer[] countries, String uri){
 		
 		Hashtable<String, String> wb_data = new Hashtable<String, String>();
 		long search_id = 0, country_search_id = -1;
 		int search_country_id = 0;
 		try {
 			
 			JSONArray jsonArray = new JSONArray(jsonData);
 			JSONObject jsonObject = jsonArray.getJSONObject(0);
 			
 			int numReturned  = Integer.parseInt(jsonObject.getString("per_page"));
 			int numofObjects = Integer.parseInt(jsonObject.getString("total"));
 			
 			if (numofObjects > 0){
 				jsonArray = jsonArray.getJSONArray(1);
 				// create or update Search record
 				String date = timeStamp();
 				apiRecord = new ContentValues();
 				apiRecord.put(I_ID 				, indicator	);
 				apiRecord.put(AP_ID				, 1			);
 				apiRecord.put(SEARCH_CREATED	, date		);
 				apiRecord.put(SEARCH_MODIFIED	, date		);
 				apiRecord.put(SEARCH_URI		, uri		);
 				//FROM_SEARCH			= {SEARCH_ID, I_ID, AP_ID, SEARCH_CREATED, SEARCH_MODIFIED, SEARCH_URI};
 				search_id = areaData.insert(SEARCH, apiRecord, 1);
 				Log.e(TAG, "Search_id:" + search_id);
 				// create or update Search_Country record
 				if(search_id > 0){
 					Log.e(TAG, "Num of Countries:" + countries.length);
 					for(int n = 0; n < countries.length; n++){
 						Log.e(TAG, "Country:" + countries[n]);
 						apiRecord = new ContentValues();
 						apiRecord.put(S_ID 	, search_id		);
 						apiRecord.put(C_ID 	, countries[n]	);
 						apiRecord.put(P_ID  , 1				);
 						//FROM_SEARCH_COUNTRY	= {_ID, S_ID, C_ID, P_ID};
 						country_search_id = areaData.insert(SEARCH_COUNTRY, apiRecord, 1);
 					}
 				}else{
 					Log.e(TAG, "Error inserting Search record: Indicator-" + indicator + ", API_ID- 1, " + "URI-" + uri);
 					return SEARCH_FAIL;
 				}
 				
 				// move on to parse and update WB_DATA 
 			}else{
 				// no data returned from World bank API pull
 				Log.e(TAG, "Error NO data retrieved from WB API: URL-" + uri);
 				return SEARCH_FAIL;
 			}
 			// get Data returned from the world bank
 			// update the WB_DATA table with the indicator values
 			for (int i = 0; i < numReturned; i++) {
 				apiRecord = new ContentValues();
 				wb_data = parseJSON( wb_data, jsonArray.getJSONObject(i), "");
 				
 				
 				//THIS WAS THE EASIEST WAY TO DO IT AT THE TIME
 				//get Country ID from Country table based on the Country code
 				Cursor countryData		= areaData.rawQuery(COUNTRY,"*", "" + WB_COUNTRY_CODE + " = '"+ (String)wb_data.get("country: id")+ "'");
 				// Then get SearchCountry ID from corresponding table now that we have both S_ID and C_ID
 				countryData.moveToFirst();
 				Cursor SearchCountry	= areaData.rawQuery(SEARCH_COUNTRY,"*", "" + C_ID + " = '"
 								+ countryData.getInt(countryData.getColumnIndex(_ID)) + "' AND "  + S_ID + "= '"+ search_id +"'");
 				SearchCountry.moveToFirst();
 				search_country_id = SearchCountry.getInt(SearchCountry.getColumnIndex(_ID));
 				if(search_country_id < 0){
 					return SEARCH_FAIL;
 				}
 				apiRecord.put(SC_ID, search_country_id);
 				Log.d("Indicators", ""+ SC_ID + ":-> " + SearchCountry.getInt(SearchCountry.getColumnIndex(_ID)));
 				search_country_id = apiRecord.getAsInteger(SC_ID);
 				for (int a = 0; a < WB_DATA_LIST.length; a++){
 					apiRecord.put(FROM_WB_DATA[a+2], (String)wb_data.get(WB_DATA_LIST[a]));	
 					Log.d("Indicators", ""+FROM_WB_DATA[a+2] + ":-> " + (String)wb_data.get(WB_DATA_LIST[a]));
 				}
 				countryData.close();
 				SearchCountry.close();
 				areaData.insert(WB_DATA, apiRecord, 0);
 				
 			}
 			
 		} catch (Exception e) {
 			//e.printStackTrace();
 			
 			Log.e(TAG, "WB DATA" + e.toString());
 			return SEARCH_FAIL;
 		}		
 		return search_country_id;
 	}
 	
 	public int getWBTotal(String jsonData){
 
 		int numofObjects = 0;
 		try {
 			
 			JSONArray jsonArray = new JSONArray(jsonData);
 			JSONObject jsonObject = jsonArray.getJSONObject(0);
 			
 			numofObjects = Integer.parseInt(jsonObject.getString("total"));
 		} catch (Exception e) {
 			//e.printStackTrace();
 			Log.e(TAG, e.toString());
 		}	
 		return numofObjects;
 	}
 	
 	public String parseIndicators(String jsonData){
 		
 		Hashtable<String, String> indicator_data = new Hashtable<String, String>();
 		
 		StringBuilder jsonText = new StringBuilder();
 		try {
 			
 			JSONArray jsonArray = new JSONArray(jsonData);
 			JSONObject jsonObject = jsonArray.getJSONObject(0);
 			
 			int numReturned  = Integer.parseInt(jsonObject.getString("per_page"));
 			int numofObjects = Integer.parseInt(jsonObject.getString("total"));
 			
 			if (numofObjects > 0){
 				jsonArray = jsonArray.getJSONArray(1);
 			}else{
 				// no data returned from World bank API pull
 			}
 			// get Data returned from the world bank 
 			for (int i = 0; i < numReturned; i++) {
 				apiRecord = new ContentValues();
 				indicator_data = parseJSON(indicator_data, jsonArray.getJSONObject(i), "");
 				for (int a = 0; a < WB_IND_LIST.length; a++){
 					apiRecord.put(FROM_INDICATOR[a+1], (String)indicator_data.get(WB_IND_LIST[a]));	
 					Log.d("Indicators", ""+FROM_INDICATOR[a+1] + ":-> " + (String)indicator_data.get(WB_IND_LIST[a]));
 				}
 				Log.d(TAG, indicator_data.toString());
 				areaData.insert(INDICATOR, apiRecord, 0);
 			}					
 			
 		} catch (Exception e) {
 			//e.printStackTrace();
 			Log.e(TAG, e.toString());
 		}		
 		
 		return jsonText.toString();
 	}
 	
 	// implemented on the basis that parseIndicators is working
 	public int parseCountries(String jsonData){
 		
 		Hashtable<String, String> country_data = new Hashtable<String, String>();
 		
 		try {
 			
 			JSONArray jsonArray = new JSONArray(jsonData);
 			JSONObject jsonObject = jsonArray.getJSONObject(0);
 			
 			int numReturned  = Integer.parseInt(jsonObject.getString("per_page"));
 			int numofObjects = Integer.parseInt(jsonObject.getString("total"));
 			
 			if (numofObjects > 0){
 				jsonArray = jsonArray.getJSONArray(1);
 			}else{
 				// no data returned from World bank API pull
 				return 0;
 			}
 			// get Data returned from the world bank 
 			for (int i = 0; i < numReturned; i++) {
 				apiRecord = new ContentValues();
 				//test this 
 				country_data = parseJSON(country_data, jsonArray.getJSONObject(i), "");
 				
 				for (int a = 0; a < WB_COUNTRY_LIST.length; a++){
 					apiRecord.put(FROM_COUNTRY[a+1], (String)country_data.get(WB_COUNTRY_LIST[a]));	
 					
 					Log.d("Countries", ""+FROM_COUNTRY[a+1] + ":-> " + (String)country_data.get(WB_COUNTRY_LIST[a]));
 				}
 				Log.d(TAG, country_data.toString());
 				areaData.insert(COUNTRY, apiRecord, 0);
 			}					
 			
 		} catch (Exception e) {
 			//e.printStackTrace();
 			Log.e("Parsing", e.toString());
 			return -1;
 		}		
 		
 		return 0;
 	}
 
 	private Hashtable<String, String> parseJSON(Hashtable<String, String> data, JSONObject jsonInnerObject, String base){
 		String key, value;
 		if (! base.equals("")){
 			base = base + ": ";
 		}
 		try {
 			data.put("json", jsonInnerObject.toString());
 					
 			JSONArray names = jsonInnerObject.names();
 			
 			for(int x = 0; x < names.length(); x++){
 				
 				if(jsonInnerObject.optJSONObject(names.getString(x)) == null){
 					key = "" + base + ""+names.getString(x);
 					value = jsonInnerObject.getString(names.getString(x));
 					if (key.equals(IDS_SEARCH_DOC_AUTH)){
 						value = "";
 						JSONArray auths = jsonInnerObject.getJSONArray(key);
 						for (int a = 0; a < auths.length(); a++){
 							if(a == 0){
 								value = value + auths.getString(a);
 							}else{
 								value = value + ", " + auths.getString(a) ;
 							}
 							
 						}
 						
 						Log.e(TAG,"Values String "+ value );
 					}
 					data.put(key, value);
 					
 				}else{
 					//jsonText.append("\n" + names.getString(x) + ": ");
 					//jsonText.append("\n\t" +jsonInnerObject.optJSONObject(names.getString(x)).toString());
 					//data.put(names.getString(x) , jsonInnerObject.optJSONObject(names.getString(x)).toString());
 					data = parseJSON(data, jsonInnerObject.optJSONObject(names.getString(x)), names.getString(x));
 				}
 			}
 		}
 		catch (JSONException e){
 			Log.e(TAG,"Exception in parsing Indicators List "+e.toString());
 		}catch(PatternSyntaxException e){
 			Log.e(TAG,"Exception in parsing Indicators List "+e.toString());
 		}
 		return data;
 	}
 	
 	private String timeStamp(){
 		Calendar calendar = Calendar.getInstance();
 		SimpleDateFormat format = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
 		
 		return format.format(calendar.getTime());
 		
 		
 	}
 }
