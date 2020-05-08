 package com.InAndOut;
 
 import java.util.List;
 
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.parse.ParseUser;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.ListView;
 import android.widget.Toast;
 
 
 public class HomeScreen extends Activity implements LocationListener{
   private ListView listView1;
   ParseUser currentUser;
   ParseObject house;
 	String TAG = this.getClass().getCanonicalName();
 	static Location firstlocation = null;
 	static Integer geoswitch = 0;
 	
 	SimpleCursorAdapter mAdapter;
 	
 	LocationManager locationManager;
 
 	
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     //setContentView(R.layout.create_home);
     currentUser = ParseUser.getCurrentUser();
 		try {
 			house = currentUser.getParseObject("House").fetchIfNeeded();
 	    if (havewifi(this,house.getString("wifiname")) == false){
 	    	connectToHomeWifi(this, house);
 	    	Log.d(TAG, "wifi is not connected");
 	    } else {
 	    	geolocate();
 	    	Log.d(TAG, "wifi is already connected");
 	    }
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		if (ishome()){
 			Log.d(TAG,"is going home");
 			setishome(true);
 	    locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 	    String locationProvider = LocationManager.NETWORK_PROVIDER;
 	    // Or, use GPS location data:
 	    // String locationProvider = LocationManager.GPS_PROVIDER;
 
 	    locationManager.requestLocationUpdates(locationProvider, 200, 0, this);
 		} else {
 			setishome(false);
   	}
 
 	  setContentView(R.layout.home_view);
 
 
     ParseQuery<ParseUser> alluser = ParseUser.getQuery();
     house = (ParseObject) currentUser.get("House");
     alluser.whereEqualTo("House", house);
     List<ParseUser> filteruser = null;
 		try {
 			filteruser = alluser.find();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		listView1 = (ListView)findViewById(R.id.listView1);
        
     ParseUser[] array = filteruser.toArray(new ParseUser[filteruser.size()]);
     
     MySimpleArrayAdapter adapter = new MySimpleArrayAdapter(this, array); 
     listView1.setAdapter(adapter);
     
   }
   
   public void connectToHomeWifi(Context context, ParseObject house){
   	String networkSSID = house.getString("wifiname");
   	String networkPass = house.getString("wifipassword");
   	String networkType = house.getString("wifitype");
 
   	WifiConfiguration conf = new WifiConfiguration();
   	conf.SSID = "\"" + networkSSID + "\"";
   	
   	if (networkType == "wep"){
 	  	conf.wepKeys[0] = "\"" + networkPass + "\""; 
 	  	conf.wepTxKeyIndex = 0;
 	  	conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
 	  	conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40); 
 	  	conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
   	} else if (networkType == "wpa") {
   		conf.hiddenSSID = false;
   		conf.status = WifiConfiguration.Status.ENABLED;     
   		conf.priority = 40;
 
   		conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
 
   		conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40); 
   		conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
   		conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
   		conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
 
   		conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
 
   		conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
   		conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
 
   		conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
   		conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
 
   		conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
 			conf.preSharedKey = "\""+ networkPass +"\"";
   	} else {
   		conf.status = WifiConfiguration.Status.ENABLED;     
   		conf.priority = 40;
 
   		conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
 
   		conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40); 
   		conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
   		conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
   		conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
 
   		conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
 
   		conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
   		conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
 
   		conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
   		conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
 
   		conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
   	}
   	
   	WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE); 
   	wifiManager.addNetwork(conf);
   	
 //  	List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
 //  	for( WifiConfiguration i : list ) {
 //  	    if(i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
 //  	         wifiManager.disconnect();
 //  	         wifiManager.enableNetwork(i.networkId, true);
 //  	         wifiManager.reconnect();               
 //
 //  	         break;
 //  	    }           
 //  	 }
   }
   
   public Boolean havewifi(Context context,String wifiname) {
 
     Boolean match = false;
     ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
     NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
     if (networkInfo.isConnected()) {
       final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
 
    // Get WiFi status MARAKANA
       WifiInfo info = wifiManager.getConnectionInfo();
       String textStatus = "";
       textStatus += "\n\nWiFi Status: " + info.toString();
 
       // List stored networks
       List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();
       for (WifiConfiguration config : configs) {
           textStatus+= "\n\n" + config.toString();
           Log.d(TAG, config.SSID);
           if (config.SSID == wifiname){
           	match = true;
           }
       }
       Log.v(TAG,"from marakana: \n"+textStatus);
     }
     return match;
   }
   
   public String getCurrentSsid(Context context) {
 
     String ssid = null;
     ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
     NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
     if (networkInfo.isConnected()) {
       final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
       final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
       if (connectionInfo != null && !(connectionInfo.getSSID().equals(""))) {
           //if (connectionInfo != null && !StringUtil.isBlank(connectionInfo.getSSID())) {
         ssid = connectionInfo.getSSID();
       }
     }
     return ssid ;
   }
   
   public Boolean ishome(){
   	Boolean result = false;
   	
   	ParseUser currentUser = ParseUser.getCurrentUser();
     ParseObject house = null;
     try {
 			house = currentUser.getParseObject("House").fetchIfNeeded();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     Log.d(TAG, "this is something: " + getCurrentSsid(this));
     if (getCurrentSsid(this).contentEquals(("\"" + house.getString("wifiname") + "\""))){
     	result = true;
     }
   	
   	return result;
   }
   
   public void setishome(Boolean home){
   	ParseUser currentUser = ParseUser.getCurrentUser();
   	Log.d(TAG,"home: " + home.toString() );
   	currentUser.put("ishome", home);
   	try {
 			currentUser.save();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
   	Log.d(TAG,"saved" );
   }
   
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.login_screen, menu);
 		return true;
 	}
 	
  	@Override
  	public boolean onOptionsItemSelected(MenuItem item) {
  	    // Handle item selection
  	    switch (item.getItemId()) {
  	    case R.id.action_settings:
  	    		refresh();
  	    		return true;
  	    case R.id.action_leave_home:
  	        leaveHome();
  	        return true;
  	    case R.id.action_logout:
  	        LogOut();
  	        return true;
  	    default:
  	        return super.onOptionsItemSelected(item);
  	    }
  	}
  	
  	public void refresh(){
  		
 		if (ishome()){
 			Log.d(TAG,"is going home");
 			setishome(true);
 		} else {
 			setishome(false);
   	}
 		
     ParseQuery<ParseUser> alluser = ParseUser.getQuery();
     house = (ParseObject) currentUser.get("House");
     alluser.whereEqualTo("House", house);
     List<ParseUser> filteruser = null;
 		try {
 			filteruser = alluser.find();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		listView1 = (ListView)findViewById(R.id.listView1);
        
     ParseUser[] array = filteruser.toArray(new ParseUser[filteruser.size()]);
     
     MySimpleArrayAdapter adapter = new MySimpleArrayAdapter(this, array); 
     listView1.setAdapter(adapter);
     
  	}
  	
  	public void leaveHome(){
  		ParseUser currentUser = ParseUser.getCurrentUser();
  		currentUser.remove("House");
  		currentUser.saveEventually();
  		Intent i = new Intent(this, enter.class);
   	i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
   	startActivity(i);
   	finish();
  	}
  	
  	public void LogOut(){
  		ParseUser.logOut();
  		Intent i = new Intent(this, LoginScreen.class);
   	i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
   	startActivity(i);
   	finish();
  	}
  	
  	
  	public void onDestroy(){
  		super.onDestroy();
  		locationManager.removeUpdates(this);
  	}
  	
  	public void geolocate(){
  		String locationProvider = LocationManager.NETWORK_PROVIDER;
  // Or, use GPS location data:
  // String locationProvider = LocationManager.GPS_PROVIDER;
 
  		locationManager.requestLocationUpdates(locationProvider, 0, 0, this);
  	}
  	public void makeUseOfNewLocation(Location location){
  		Log.d(TAG,"This is the location: " + location.toString());
  		Integer d = difference(location, firstlocation);
 		if (d > 1){
  	 		Toast t = Toast.makeText(this, "moved alot: " + String.valueOf(d), Toast.LENGTH_LONG);
  	 		t.show();
  	 		if (geoswitch == 0 || geoswitch == 2){
 	 	 		setishome(false);
 	 	 		
 		 	 	ParseQuery<ParseUser> alluser = ParseUser.getQuery();
 		    house = (ParseObject) currentUser.get("House");
 		    alluser.whereEqualTo("House", house);
 		    List<ParseUser> filteruser = null;
 				try {
 					filteruser = alluser.find();
 				} catch (ParseException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				listView1 = (ListView)findViewById(R.id.listView1);
 		       
 		    ParseUser[] array = filteruser.toArray(new ParseUser[filteruser.size()]);
 		    
 		    MySimpleArrayAdapter adapter = new MySimpleArrayAdapter(this, array); 
 		    listView1.setAdapter(adapter);
 		    geoswitch = 1;
  	 		} 
  		} else { 
  			Toast t = Toast.makeText(this, "this is how much you've moved: " + String.valueOf(d), Toast.LENGTH_SHORT);
  	 		t.show();
  	 	if (geoswitch == 0 || geoswitch == 1){
  	 		setishome(true);
  	 		
 	 	 	ParseQuery<ParseUser> alluser = ParseUser.getQuery();
 	    house = (ParseObject) currentUser.get("House");
 	    alluser.whereEqualTo("House", house);
 	    List<ParseUser> filteruser = null;
 			try {
 				filteruser = alluser.find();
 			} catch (ParseException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			listView1 = (ListView)findViewById(R.id.listView1);
 	       
 	    ParseUser[] array = filteruser.toArray(new ParseUser[filteruser.size()]);
 	    
 	    MySimpleArrayAdapter adapter = new MySimpleArrayAdapter(this, array); 
 	    listView1.setAdapter(adapter);
 	    geoswitch = 2;
 	 		} 
  		}
  	}
 
  	public Integer difference(Location newlocation, Location firstlocation){
  		Integer result = 0;
  		
  		result = (int) firstlocation.distanceTo(newlocation);
  		
  		return result;
  	}
 	@Override
 	public void onLocationChanged(Location location) {
 		if (firstlocation == null){
 			firstlocation = location;
 		}
 		 makeUseOfNewLocation(location);
 	}
 
 	@Override
 	public void onProviderDisabled(String arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onProviderEnabled(String arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
 		// TODO Auto-generated method stub
 		
 	}
 }
