 /*
  * project	SeattleCoffeeLocator
  * 
  * package	com.j2w3.rbarnes.seattlecoffeelocator
  * 
  * @author	Ronaldo Barnes
  * 
  * date		Mar 20, 2013
  */
 package com.j2w3.rbarnes.seattlecoffeelocator;
 
 import java.io.BufferedInputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.rbarnes.other.LocationContentProvider;
 import com.rbarnes.other.LocationDB;
 
 import android.app.Service;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.IBinder;
 import android.util.Log;
 import android.widget.Toast;
 
 public class CoffeeService extends Service{
 
 	Context _context;
 	static Boolean _connected = false;
 	static String _connectionType = "Unavailable";
 	Toast _toast;
 	
 	
 	
 	 @Override
 	  public int onStartCommand(Intent intent, int flags, int startId) {
 		 _context = this;
 		 Boolean connected = getConnectionStatus(_context);
 		 Toast.makeText(this,"This service works :)", Toast.LENGTH_LONG).show();
 		 if (connected){
 				Log.i("SERVICE", "CONNECTED");
 			}else{
 				Log.i("SERVICE", "NOT CONNECTED");
 			}
 		 getLocations("Coffee", "98101");
 		 this.stopSelf();
 		 
 	    return Service.START_NOT_STICKY;
 	  }
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		Log.d("service", "Service started");
 		this.stopSelf();
 	}
     @Override
     public void onDestroy() {
         // TODO Auto-generated method stub
         super.onDestroy();
         Log.d("service", "Service destroyed");
     }
   
   	public static String getConnectionType(Context context){
   		netInfo(context);
   		return _connectionType;
   	}
   	
   	public static Boolean getConnectionStatus(Context context) {
   		netInfo(context);
   		return _connected;
   	}
   	
   	private static void netInfo(Context context){
   		ConnectivityManager cManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
   		NetworkInfo nInfo = cManager.getActiveNetworkInfo();
   		if(nInfo != null){
   			
   			if(nInfo.isConnected()){
   				_connectionType = nInfo.getTypeName();
   				_connected = true;
   				
   			}else{
   				_connected = false;
   			}
   			
   		}
   	}
   	
   	
   	public static String getUrlStringResponse(URL url){
   		String response = "";
   		
   		try{
   			URLConnection connection = url.openConnection();
   			BufferedInputStream bin = new BufferedInputStream(connection.getInputStream());
   			
   			byte[] contentBytes = new byte[1024];
   			int bytesRead = 0;
   			StringBuffer responseBuffer = new StringBuffer();
   			
   			while((bytesRead = bin.read(contentBytes)) != -1){
   				
   				response = new String(contentBytes,0,bytesRead);
   				
   				responseBuffer.append(response);
   				
   			}
   			return responseBuffer.toString();
   		}catch (Exception e){
   			Log.e("URL RESPONSE ERROR","getURLStringResponse");
   		}
   		
   		return response;
   	}
   	
   	private void getLocations(String dessert, String zipCode){
		String baseUrl = "http://local.yahooapis.com/LocalSearchService/V3/localSearch?appid=qJIjRlbV34GJZfg2AwqSWVV03eeg8SpTQKy5PZqSfjlRrItt5hS2n3PIysdPU_CCIQlCGXIGjoTDESp3l42Ueic3O1EaYXU-&query="+dessert+"&zip="+zipCode+"&results=20&output=json";
 		URL finalURL;
 		try{
 			finalURL = new URL(baseUrl);
 			LocationRequest lr = new LocationRequest();
 			lr.execute(finalURL);
 			Log.i("URL ", baseUrl);
 			
 		}catch(MalformedURLException e){
 			Log.e("BAD URL","MALFORMED URL");
 			finalURL = null;
 		}
 	}
 	
 	//get results
 	private class LocationRequest extends AsyncTask<URL, Void, String>{
 		@Override
 		protected String doInBackground(URL... urls){
 			String response = "";
 			
 			for(URL url: urls){
 				
 				response = getUrlStringResponse(url);
 			}
 			
 			return response;
 		}
 		
 		
 		@Override
 		protected void onPostExecute(String result){
 			
 			Log.i("URL RESPONSE", result);
 			try{
 				JSONObject json = new JSONObject(result);
 				JSONObject locationsObject = json.getJSONObject("ResultSet");
 				if(locationsObject.getString("totalResultsAvailable").compareTo("0")==0){
 					_toast = Toast.makeText(_context, "No Results" , Toast.LENGTH_SHORT);
 					_toast.show();
 				}else{
 					JSONArray locations = locationsObject.getJSONArray("Result");
 					
 					if(locations != null){
 						_toast = Toast.makeText(_context, "Saving File.", Toast.LENGTH_SHORT);
 						_toast.show();
						for(int i=0;i<3;i++){							
 							JSONObject location = locations.getJSONObject(i);
 						Log.i("Location ", location.toString());
 						ContentValues locationData = new ContentValues();
 						locationData.put(LocationDB.COL_TITLE, location.getString("Title"));
 						locationData.put(LocationDB.COL_ADDRESS, location.getString("Address"));
 						locationData.put(LocationDB.COL_CITY, location.getString("City"));
 						locationData.put(LocationDB.COL_STATE, location.getString("State"));
 						locationData.put(LocationDB.COL_PHONE, location.getString("Phone"));
 						locationData.put(LocationDB.COL_COORDS, location.getString("Latitude")+","+location.getString("Longitude"));
 						getContentResolver().insert(LocationContentProvider.CONTENT_URI,locationData);
 						
 						}
 
 					}else{
 						_toast = Toast.makeText(_context, "Something went wrong" , Toast.LENGTH_SHORT);
 						_toast.show();
 					}
 				}
 				
 			}catch(JSONException e){
 				Log.e("JSON", "JSON OBJECT EXCEPTION");
 			}
 			
 			
 		}
 	}
 }
