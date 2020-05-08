 package com.example.final_exer;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 
 
 public class TwitterListActivity extends ListActivity {
 	
 	
 	public static String MPLAT = "MPLATITUD";			
 	public static String MPLON = "MPLONGITUD";			
 	public static String LAT_DEF = "40.639635";		
 	public static String LON_DEF = "22.944599";		
 	
 	ArrayList<HashMap<String,String>> Twits; 			
 	String[] from=new String[] {"Index","Name","Date","Message","Location"};		
 	int[] to=new int[]{R.id.index,R.id.name,R.id.date,R.id.message,R.id.location}; 
 	
 	String Url = null;					
 	String urlSearch = null;			
 	String query = null;				
 	String latitude = null;				
 	String longitude = null;			
 	String gpsLatitude = null;			
 	String gpsLongitude = null;			
 	String range = null;				
 	String units = null;				
 	String rpp = null;					
 	String mapLatitud = null;			
 	String mapLongitud = null;			
 	boolean locManual = true;			
 	ArrayList<String[]> list = new ArrayList<String[]>();
 	
 	private ProgressDialog dialog;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_twitter_list);
 			
 		Intent intent = getIntent();		
 		if (intent != null)  				
 		{
 			String data = intent.getStringExtra(TwitterMainActivity.DATA); 	
 			if (data != null)  				
 			{
 				query = data;				
 			}
 			
 			String myLatitude = intent.getStringExtra(TwitterMainActivity.LAT); 
 			if (myLatitude != null)  		
 			{
 				gpsLatitude = myLatitude;	
 			} else {
 				gpsLatitude = LAT_DEF;		
 			}
 			
 			String myLongitude = intent.getStringExtra(TwitterMainActivity.LON); 
 			if (myLongitude != null)  		
 			{
 				gpsLongitude = myLongitude;	
 			} else {
 				gpsLongitude = LON_DEF;		
 			}
 		}
 		
 		
 		dialog = new ProgressDialog(this);
 	    dialog.setTitle("Progress");
 	    dialog.setMessage("Downloading ...");
 	    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 	    dialog.setCancelable(true);
 		
 	    
 		new backTask().execute();
 	
 	}
 	
 	public String JSONFeed() {
 		
 		urlSearch = TwitterPrefs.getUrlSearch(getBaseContext());	
 		locManual = TwitterPrefs.getlocManual(getBaseContext());	
 		if (locManual) {
 			latitude = TwitterPrefs.getLatitude(getBaseContext());	
 			longitude = TwitterPrefs.getLongitude(getBaseContext());
 		} else {
 			latitude = gpsLatitude;							
 			longitude = gpsLongitude;						
 		}
 		
 		range = TwitterPrefs.getDistance(getBaseContext());		
 		units = TwitterPrefs.getUnities(getBaseContext());		
 		rpp = TwitterPrefs.getResultPerPage(getBaseContext());	
        
 		
 		Url = urlSearch + query + "&geocode=" + latitude + "," + longitude + "," + range + units + "&rpp=" + rpp;
 		
 		StringBuilder builder = new StringBuilder();
 		HttpClient client = new DefaultHttpClient();
 		HttpGet httpGet = new HttpGet(Url);
 
 		try {
 			HttpResponse response = client.execute(httpGet);
 			StatusLine statusLine = response.getStatusLine();
 			Log.d("JSONFeed", "StatusLine: " + statusLine.toString());
 
 			int statusCode = statusLine.getStatusCode();
 			Log.d("JSONFeed", "StatusCode: " + statusCode);
 
 			if (statusCode == 200) {
 				HttpEntity entity = response.getEntity();
 				InputStream content = entity.getContent();
 				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
 				String line;
 				while ((line = reader.readLine()) != null) {
 					builder.append(line);
 				}
 			} else {
 				Log.d("JSONFeed", "Failed to download file");
 			}
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		return builder.toString();
 
 	}
 	
 	private class backTask extends AsyncTask<Void, Float, String> {
 		
 		   @Override
 		   protected void onPreExecute() {
 		      super.onPreExecute();
 		      dialog.setProgress(0);
 		      dialog.setMax(100);
 		      dialog.show(); 			
 		   }
 		 
 		   @Override
 		   protected String doInBackground(Void... params) {
 			 
 		      String json = JSONFeed();
 		      return json;
 		   }
 		 
 		   @Override
 		   protected void onProgressUpdate(Float... values) {
 		      super.onProgressUpdate(values);
 		      int p = Math.round(values[0]);
 		      dialog.setProgress(p);
 		   }
 		 
 		   @Override
 		   protected void onPostExecute(String result) {
 			   super.onPostExecute(result);
 			   dialog.dismiss();
 			   String readJSON = result;
 			   boolean emptyList = false; 
 			   Log.d("Zoumpis",result);
 			   
 			  
 			   try {
 				   JSONObject jsonObject = new JSONObject(readJSON);
 				   JSONArray jsonArray = jsonObject.getJSONArray("results");
 				   
 				   if (jsonArray.length() == 0) {		
 					   
 					   TextView empty = (TextView) getListView().getEmptyView(); 
 					   empty.setText(R.string.no_data);
 					   emptyList = true;
 					   
 				   } else {
 
 					   for (int i = 0; i < jsonArray.length(); i++) {
 						   JSONObject jsObject = jsonArray.getJSONObject(i);
 						   String geo = jsObject.getString("geo");
 						   String tweetLoc = null;
 
 						   if (geo.equals("null")) {  	
 							   tweetLoc = jsObject.getString("location");
 						   } else {						
 							   String regexLat = "(\\-?\\d+(\\.\\d+)?),";
 							   Matcher matcherLat = Pattern.compile(regexLat).matcher(geo);
 							   if (matcherLat.find())
 							   {
 								   mapLatitud = matcherLat.group();
 								   mapLatitud = mapLatitud.substring(0,mapLatitud.length()-1);
 							   } else {
 								   mapLatitud = LAT_DEF;		
 							   }
 
 							   
 							   String regexLon = ",(\\-?\\d+(\\.\\d+)?)";
 							   Matcher matcherLon = Pattern.compile(regexLon).matcher(geo);
 							   if (matcherLon.find())
 							   {
 								   mapLongitud = matcherLon.group();
 								   mapLongitud = mapLongitud.substring(1,mapLongitud.length());
 							   }
 							   else {
 								   mapLongitud = LON_DEF; 
 							   }
 
 							   tweetLoc = "MPLAT:" + mapLatitud + ", MPLON:" + mapLongitud;
 
 						   }
 
 						   String[] twitInfo = {	String.valueOf(i+1), 
 								   jsObject.getString("from_user_name"), 
 								   jsObject.getString("created_at"),
 								   jsObject.getString("text"), 
 								   tweetLoc};
 						   list.add(twitInfo);
 
 					   }
 				   }
 			   } catch (Exception e) {
 				   Log.d("ParseJSON", "jsonArray catch");
 				   e.printStackTrace();
 			   } 
 
 			   
 			   if (emptyList) {
 
 			   } else {
 				   
 				   Twits = new ArrayList<HashMap<String, String>>();
 
 				   for(String[] element:list){
 					   HashMap<String,String> dataTwitter=new HashMap<String, String>();
   
 					   dataTwitter.put("Index", element[0]);
 					   dataTwitter.put("Name", element[1]);
 					   dataTwitter.put("Date", element[2]);
 					   dataTwitter.put("Message", element[3]);
 					   dataTwitter.put("Location", element[4]);
 
 					   Twits.add(dataTwitter);
 				   }
 				  
 				   SimpleAdapter ListadoAdapter=new SimpleAdapter(getBaseContext(), Twits, R.layout.app_row, from, to);
 				   setListAdapter(ListadoAdapter);
 			   }
 		   }
 
 		}
 
 		
 		@Override
 		protected void onListItemClick(ListView l, View v, int position, long id) {
 		   		
 			String cadenaTwit = String.valueOf(Twits.get(position));
 			String mpLatitude = null;		
 			String regexMpLat = "MPLAT:(\\-?\\d+(\\.\\d+)?),";
 	    	Matcher matcherMpLat = Pattern.compile(regexMpLat).matcher(cadenaTwit);
 	    	
 	        if (matcherMpLat.find())
 	        {
 	        	mpLatitude = matcherMpLat.group();
 	        	mpLatitude = mpLatitude.substring(6,mpLatitude.length()-1);
 	        } else {
 	        	mpLatitude = LAT_DEF;		
 	        }
 	        
 			String mpLongitude = null;
 			
 			String regexMpLon = "MPLON:(\\-?\\d+(\\.\\d+)?),";
 	    	Matcher matcherMpLon = Pattern.compile(regexMpLon).matcher(cadenaTwit);
 	        if (matcherMpLon.find())
 	        {
 	        	mpLongitude = matcherMpLon.group();
 	        	mpLongitude = mpLongitude.substring(6,mpLongitude.length()-1);
 	        } else {
 	        	mpLongitude = LON_DEF;		
 	        }
 	        
	        Intent intent = new Intent(getBaseContext(), TwitterMainActivity.class);
 			intent.putExtra(MPLAT, mpLatitude);
 			intent.putExtra(MPLON, mpLongitude);
 			startActivity(intent);
 		}
 		
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.twitter_list, menu);
 		return true;
 	}
 
 }
