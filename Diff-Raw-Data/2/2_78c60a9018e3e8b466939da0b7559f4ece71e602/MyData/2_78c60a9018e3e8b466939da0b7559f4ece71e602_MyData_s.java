 package de.thiemonagel.myapp;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.location.LocationManager;
 import android.telephony.TelephonyManager;
 import android.util.FloatMath;
 import android.util.Log;
 
 // Singleton containing data to be displayed
 public class MyData {
 
 	private static MyData                            fInstance = null;
 	private int                                      fCatFilterMask;
 	private int                                      fCatFilterMaskApplied;  // last mask committed
 	private ArrayList<HashMap<String, String>>       fDataList;  // data currently to be displayed
 	private HashMap<String, HashMap<String, String>> fDataMap;   // cache of full information
 	private Context                                  fContext;   // required for location manager, among others
 	private String                                   fError;     // error message
 	private boolean                                  fkm;        // whether distances are to be displayed in km
 	private boolean                                  fLoaded;
 	private SharedPreferences                        fSettings;
 	
 	public static final String PREFS_FILE    = "config";
 	public static final String PREFS_CATMASK = "CategoryMask"; 
 
 	private MyData( Context c ) {
 		fDataList  = new ArrayList<HashMap<String, String>>();
         fDataMap   = new HashMap<String,HashMap<String, String>>();
 		fContext   = c;
         fError     = "";
         fLoaded    = false;
         
     	// derive preferred units from SIM card country
     	TelephonyManager tm = (TelephonyManager)fContext.getSystemService(Context.TELEPHONY_SERVICE);
     	String ISO = tm.getSimCountryIso().toLowerCase();
 	    Log.i( "MyApp", "SIM country ISO: " + ISO );
 	    
 	    // it seems that only USA and GB still use miles:
 	    // https://en.wikipedia.org/wiki/Imperial_units#Current_use_of_imperial_units
     	if (    ISO.equals("gb")
     		 || ISO.equals("io")   // British Indian Ocean Territory
     		 || ISO.equals("uk")   // bad iso code, checking it nevertheless, just in case...
     		 || ISO.equals("um")   // U.S. Minor Outlying Islands
     		 || ISO.equals("us") 
     		 || ISO.equals("vg")   // British Virgin Islands
     		 || ISO.equals("vi") ) // U.S. Virgin Islands
     		fkm = false;
     	else
     		fkm = true;
     	
     	// load from SharedPreferences
    	fSettings             = c.getSharedPreferences( PREFS_FILE, c.MODE_PRIVATE );
         fCatFilterMask        = fSettings.getInt( PREFS_CATMASK, -1 );
         fCatFilterMaskApplied = fCatFilterMask;
 	    Log.i( "MyApp", "Read CatFilterMask: " + fCatFilterMask );
 	}
 	
 	// provide application context!
 	public static void initInstance( Context c ) {
 		if ( fInstance == null )
 			fInstance = new MyData( c );
 	}
 
 	public static MyData getInstance() {
 		assert( fInstance != null );
 		return fInstance;
 	}
 	
 	public String getError() { return fError; }
 
 	// set category index to value val
 	public void setCatFilter( int index, boolean val ) {
 		if ( val )
 			fCatFilterMask |= (1<<index);
 		else
 			fCatFilterMask &= ~(1<<index);
 	}
 	
 	public boolean[] getCatFilterBool() {
 		String[] list = fContext.getResources().getStringArray(R.array.categories);
 		int len = list.length;
 		boolean[] ret = new boolean[len];
 		for ( int mask = fCatFilterMask, i = 0; mask != 0 && i < len; mask >>>= 1, i++ )
 			ret[i] = (mask&1)==1 ? true : false;
 		return ret;
 	}
 	
 	// commit to SharedPreferences
 	public void commitCatFilter() {
 		if ( fCatFilterMask == fCatFilterMaskApplied )
 			return;
 		
 		SharedPreferences.Editor editor = fSettings.edit();
 		editor.putInt( PREFS_CATMASK, fCatFilterMask );
 	    editor.commit();   // TODO: use apply() instead, requires API 9
 	    fCatFilterMaskApplied = fCatFilterMask;
 	}
 	
 	// return global map
 	public HashMap<String, HashMap<String, String>> getMap() {
 		return fDataMap;
 	}
 	
 	// return current display list (possibly filtered)
 	public ArrayList<HashMap<String, String>> getList() {
 		return fDataList;
 	}
 	
 	// recreate current display list (must be run after filters have been updated)
 	public void updateList() {
 		// empty list
 		fDataList.clear();
 		
 		// filter
 		for ( Map.Entry<String, HashMap<String, String>> entry : fDataMap.entrySet() ) {
 			String[] list = fContext.getResources().getStringArray(R.array.categories);
 			boolean valid = false;
 			for ( int mask = fCatFilterMask, i = 0; mask != 0 && i < list.length; mask >>>= 1, i++ ) {
 				if ( (mask & 1) == 0 ) continue;
 				if ( entry.getValue().get("categories").contains( list[i] ) ) {
 					valid = true;
 					break;
 				}
 			}
 			if ( valid )			
 				fDataList.add( entry.getValue() );
 		}
 		
 		// sort
 		Collections.sort( fDataList, new Comparator<HashMap<String, String>>() {
 		    public int compare(HashMap<String, String> a, HashMap<String, String> b) {
 		    	String adist = a.get("pdistance");
 		    	String bdist = b.get("pdistance");
 		    	if ( adist == null )
 		    		return 1;
 		    	if ( bdist == null )
 		    		return -1;
 		        if ( adist.compareTo(bdist) < 0 )
 		        	return -1;
 		        else
 		        	return 1;
 		    }
 		});
 	}
 	
 	// pull data from vegguide.org and decode JSON into fDataMap
     protected void Load() {
     	// IIRC this is to prevent re-loading after screen rotation
     	if ( fLoaded )
     		return;
     	
 		// find location
 		LocationManager lMan = (LocationManager) fContext.getSystemService(Context.LOCATION_SERVICE);
 		List<String> lproviders = lMan.getProviders( false );  // true = enabled only
 		Location best = null;
 	    Log.i( "MyApp", lproviders.size() + " location providers found." );
 	    for ( String prov : lproviders ) {
 	    	Location l = lMan.getLastKnownLocation(prov);
 	
 	    	String logstr = prov + ": ";
 	    	if ( l != null ) {
 	    		logstr += l.getLatitude();
 	    		logstr += ", " + l.getLongitude();
 	    		logstr += ", time: " + l.getTime();
 	    		if ( l.hasAccuracy() ) {
 	    			logstr += ", error: " + l.getAccuracy() + " m";
 	    		}
 	    	} else {
 	    		logstr += "[empty]";
 	    	}
 	    	Log.i( "MyApp", logstr );
 	    	if ( l == null ) {
 	    		continue;
 	    	}
 	    	
 	    	if ( best == null ) {
 	    		best = l;
 	    		continue;
 	    	}
 	    	
 	    	// if one reading doesn't have accuracy, the latest is preferred
 	    	if ( !best.hasAccuracy() || !l.hasAccuracy() ) {
 	    		if ( l.getTime() > best.getTime() ) {
 	    			best = l;
 	    		}
 				continue;
 	    	}
 	    	
 	    	long  btime = best.getTime();     // ms
 	    	long  ltime = l.getTime();        // ms
 	    	float bacc  = best.getAccuracy(); // m
 	    	float lacc  = l.getAccuracy();    // m
 	    	
 	    	// both have accuracy, l is more recent and more accurate
 	    	if ( ltime > btime && lacc < bacc ) {
 	    		best = l;
 	    		continue;
 	    	}
 	    	
 	    	long  tdist = ltime - btime;
 	    	float dist  = l.distanceTo( best );
 	    	// agreement in sigmas
 	    	float agr  = dist / FloatMath.sqrt( bacc*bacc + lacc*lacc );
 	    	
 	    	// use outdated but more precise measurement only
 	    	// when agreement isn't too bad and time difference isn't
 	    	// too large
 	    	float crit = 1e5f / tdist;
 	    	if ( crit < 3f ) { crit = 3f; }
 	    	if ( agr < crit ) {
 	    		if ( lacc < bacc ) {
 	    			best = l;
 	    		}
 	    	} else {
 	    		if ( ltime > btime ) {
 	    			best = l;
 	    		}
 	    	}
 	    }
 	    
 	    String url = "http://www.vegguide.org/search/by-lat-long/";
     	float locationAccuracy;
 	    if ( best == null ) {
 	    	fError = "Location could not be determined!";
 	    	return;
 	    	
 	    	// for testing
 	        //Log.i( "MyApp", "No location found." );
 	        //url += "0,0";
 	        //locationAccuracy = .75f;
 	    } else {
 			url += best.getLatitude() + "," + best.getLongitude();
 			locationAccuracy = best.getAccuracy() / ( fkm ? 1000f : 1609.344f ); 
 	    }
 	    
 	    int roundMultiplier;  // for km/miles
 	    int roundDigits;
 	    if ( locationAccuracy < .015f ) {
 	    	roundMultiplier = 1000;
 	    	roundDigits     = 3;
 	    } else if ( locationAccuracy < .15f ) {
 	    	roundMultiplier = 100;
 	    	roundDigits     = 2;
 	    } else if ( locationAccuracy < 1.5f ) {
 	    	roundMultiplier = 10;
 	    	roundDigits     = 1;
 	    } else {
 	    	roundMultiplier = 1;
 	    	roundDigits     = 0;
 	    }
 	    Log.i( "MyApp", "roundMultiplier: " + roundMultiplier );
 	    
 	    url += "?unit=km&distance=100&limit=50";
 	    
 	    Log.i( "MyApp", "Getting: " +url );
 	    HttpClient client = new DefaultHttpClient();
 	    HttpGet httpGet = new HttpGet( url );
 	    StringBuilder builder = new StringBuilder();
 	    try {
 	      HttpResponse response = client.execute(httpGet);
 	      StatusLine statusLine = response.getStatusLine();
 	      int statusCode = statusLine.getStatusCode();
 	      if (statusCode == 200) {
 	        HttpEntity entity = response.getEntity();
 	        InputStream content = entity.getContent();
 	        BufferedReader reader = new BufferedReader(new InputStreamReader(content));
 	        String line;
 	        while ((line = reader.readLine()) != null) {
 	            builder.append(line);
 	        }
 	      } else {
 	    	  fError = "Bad server status code: " + statusCode;
 	    	  return;
 	      }
 	    } catch (ClientProtocolException e) {
 	        e.printStackTrace();
 	        fError = "ClientProtocolException";
 	        return;
 	    } catch (IOException e) {
 	        e.printStackTrace();
 	        fError = "IOException";
 	        return;
 	    }
 	    
 	    Log.i( "MyApp", builder.toString() );
 	    
 	    Date now = new Date();
 	    try {
 	    	JSONObject json = new JSONObject(builder.toString());
 	        JSONArray entries = json.getJSONArray("entries");
 	        for ( int i = 0; i < entries.length(); i++ ) {
 	        	JSONObject entry = entries.getJSONObject(i);
 	//        	Log.i("MyApp", entry.getString("name") );
 	
 	        	HashMap<String, String> map = new HashMap<String, String>();
 	        	String keylist[] = {
 	        			"address1", "address2", "close_date", "city", "distance", 
 	        			"name", "neighborhood", "phone", "postal_code",
 	        			"price_range", "short_description", "uri",
 	        			"veg_level", "veg_level_description", "website",
 	        			"weighted_rating" }; 
 
 	        	// missing keys are set to empty strings
 	        	for ( String key : keylist ) {
 		        	String s = "";
 		        	try {
 		            	s = entry.getString(key);
 		            } catch (JSONException e) {};
 	            	map.put( key, s );
 	        	}
 
 	        	// skip closed entries
 	        	if ( !map.get("close_date").equals("") ) {
 	        		SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd");
 	        		try {
 		        		Date closed = ft.parse( map.get("close_date") );
 		        		if ( closed.before(now) )
 		        			continue;
 	        		} catch (ParseException e) { 
 		            	Log.e("MyApp", "closed_date parse error!");
 	        		}
 	        	}
 	        	
 	            // long description (use short description if long one is missing)
 	            String ldes;
 	            try {
 	            	ldes = entry.getJSONObject("long_description").getString("text/html");
 	            } catch (JSONException e) { ldes = map.get("short_description"); }
 	            map.put( "long_description", ldes );
 	            
 	            // all categories in one string
 	            try {
 		            String cats;
 		            JSONArray acats = entry.getJSONArray( "categories" );
 		            cats = "(";
 		            for ( int j = 0; j < acats.length(); j++ ) {
 		            	if ( cats != "(" ) {
 		            		cats += ", ";
 		            	}
 		            	cats += acats.getString(j);
 		            }
 		            cats += ")";
 		            map.put( "categories", cats );
 	            } catch (JSONException e) { map.put( "categories", "" ); }
 	            
 	            // cats and veg_level in one string
 	            try {
 		            String cats_vlevel;
 		            JSONArray acats = entry.getJSONArray( "categories" );
 		            cats_vlevel = "";
 		            for ( int j = 0; j < acats.length(); j++ ) {
 		            	if ( cats_vlevel != "" ) {
 		            		cats_vlevel += ", ";
 		            	}
 		            	cats_vlevel += acats.getString(j);
 		            }
 		            
 	            	cats_vlevel += " -- " + entry.getString( "veg_level_description" ).toLowerCase();		            	
 		            map.put( "cats_vlevel", cats_vlevel );
 	            } catch (JSONException e) { map.put( "cats_vlevel", "" ); }
 	
 	            // store results
 	            try {
 	            	String uri = entry.getString("uri");
 		            // add to cache
 		            fDataMap.put( uri, map );
 
 		            String sd = map.get("distance");
 		            float  fd = 1e10f;
 	            	try {
 	            		fd = Float.parseFloat(sd);
 	            	} catch (Throwable e) {};
 	            	map.put("pdistance", String.format("%10.3f", fd) );  // pricise distance in km, for sorting
 	            	if ( !fkm ) fd /= 1.609344;  // international yard and pound treaty (1959)
 	            	fd = Math.round(fd*roundMultiplier) / (float) roundMultiplier;
 	            	if ( fd < 1f )
 	            		if ( fkm )
 	            			sd = String.format( "%.0f m", fd*1000 );
 	            		else
 	            			sd = String.format( "%.0f yds", fd*1760 );
 	            	else
 	            		sd = String.format( "%."+roundDigits+"f %s", fd, ( fkm ? " km" : " miles" ) );
 		            map.put("distance", sd);
 		            
 	            } catch (JSONException e) {
 	            	Log.e("MyApp", "uri missing!");
 	            	fError = "URI missing!";
 	            	return;
 	            }
 	        }
 	    } catch (JSONException e) {
 	        e.printStackTrace();
 	        fError = "JSONException";
 	        return;
 	    }
 	    
 	    fLoaded = true;
     }
 }
