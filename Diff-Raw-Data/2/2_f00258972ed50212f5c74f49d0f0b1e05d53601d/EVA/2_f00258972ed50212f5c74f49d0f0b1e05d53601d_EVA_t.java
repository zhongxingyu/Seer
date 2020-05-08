 package com.cliqdigital.supergsdk.utils;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import java.util.Random;
 
 import javax.crypto.Mac;
 import javax.crypto.spec.SecretKeySpec;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.annotation.SuppressLint;
 import android.app.ActivityManager;
 import android.app.ActivityManager.RunningTaskInfo;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.provider.Settings.Secure;
 import android.telephony.TelephonyManager;
 import com.cliqdigital.supergsdk.utils.Log;
 
 import com.cliqdigital.supergsdk.components.AppsFlyerHelper;
 import com.cliqdigital.supergsdk.R;
 import com.google.android.gcm.GCMRegistrar;
 
 
 @SuppressLint("SimpleDateFormat")
 public class EVA {
 
 	private static final String TAG = "EVA";
 
 	//	PUBLIC EVENT LOGGING FUNCTIONS
 
 	private String connection_type;
 	static final String SENDER_ID = "906501791685";
 	static final String DISPLAY_MESSAGE_ACTION =
 			"com.google.android.gcm.demo.app.DISPLAY_MESSAGE";
 	static AsyncTask<Void, Void, Void> mRegisterTask;
 	static final String EXTRA_MESSAGE = "message";
 
 	AppsFlyerHelper appflyerhelper = new AppsFlyerHelper();
 	//applicationGetFocus also handles appInstalled()
 	public void applicationGetFocus(final Context context){
 		final SharedPreferences settings = context.getSharedPreferences("SuperGSettings", Context.MODE_PRIVATE);
 		final String firstlaunch = settings.getString("firstlaunch", "");
 		detectInstalledApplications(context);
 		gatherLocation(context);
 		getConnection(context);
 		registerPush(context);
 		Runnable mMyRunnable = new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				if(firstlaunch == ""){
 					saveEvent(context, "{\"unique_id\":\""+ getGuid(context) +"\","+getInstalledApplications(context)+"}","app_installed","","","","","","","","","","");
 					settings.edit().putString("firstlaunch", "launched").commit();		
 				}else{
 					saveEvent(context, "{\"unique_id\":\""+ getGuid(context) +"\","+getInstalledApplications(context)+"}","application_get_focus","","","","","","","","","","");
 				}
 			}
 		};
 		Handler myHandler = new Handler();
 		myHandler.postDelayed(mMyRunnable, 6000);//Create event after 6 seconds
 	}
 
 
 	public void registerPush(Context context){
 
 		GCMRegistrar.checkDevice(context);
 		GCMRegistrar.checkManifest(context);
 
 		if (GCMRegistrar.isRegistered(context)) {
 			String regId = GCMRegistrar.getRegistrationId(context);
 			Log.i(TAG, "already registered as" + regId);
 			SharedPreferences settings = context.getSharedPreferences("SuperGSettings", Context.MODE_PRIVATE);							
 			settings.edit().putString("registration_id", regId).commit();	            
 		}
 
 		final String regId = GCMRegistrar.getRegistrationId(context);
 
 		if (regId.equals("")) {
 			try {
 				GCMRegistrar.register(context, SENDER_ID);
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			Log.i(TAG, "Registering... "+GCMRegistrar.getRegistrationId(context));
 
 		} else {
 			Log.i(TAG, "already registered as" + regId);
 		}
 	}
 
 	public void deviceInfoUpdate(final Context context){
 		saveEvent(context, "","device_info_update","","","","","","","","","","");
 	}
 
 	public void achievementEarned(Context context,String achievement_name){
 		saveEvent(context, "","achievement_earned","","","","","","","","","",achievement_name);
 	}
 
 	public void click(Context context,String clicked_item){
 		saveEvent(context, "","click",clicked_item,"","","","","","","","","");
 	}
 
 	public void finishLevel(Context context,String app_mode, String level, String score_type, String score_value){
 		saveEvent(context, "","finish_level","",app_mode,level,score_type,score_value,"","","","","");
 	}
 
 	public void purchase(Context context,String currency, String payment_method, String price, String purchased_item){
 		saveEvent(context, "","purchase","","","","","",currency,payment_method,price,purchased_item,"");
 	}
 
 	public void startLevel(Context context,String app_mode, String level, String score_type, String score_value){
 		saveEvent(context, "","start_level","",app_mode,level,score_type,score_value,"","","","","");
 	}
 
 	public void upSell(Context context,String currency, String payment_method){
 		saveEvent(context, "","up_sell","","","","","",currency,payment_method,"","","");
 	}
 
 	//HELPER FUNCTIONS
 	private void saveEvent(final Context context, //1
 			String bagged,//2
 			String name,//3
 			String clicked_item,//4
 			String app_mode,//5
 			String level,//6
 			String score_type,//7
 			String score_value,//8
 			String currency,//9
 			String payment_method,//10
 			String price,//11
 			String purchased_item,//12
 			String achievement_name//13
 			){
 
 		Log.i(TAG,"Event saved: "+name);
 
 		if(bagged == ""){bagged = "{}";}
 
 		AppsFlyerHelper appflyerhelper = new AppsFlyerHelper();
 
 		String body = "{\"name\":\"" + name
 				+ "\",\"guid\":\"" + getGuid(context)
 				+ "\",\"event_date\":\"" + getDate()
 				+ "\",\"attributes\":{\"device_class\":\"Mobile"
 				+ "\",\"sdk_version\":\""+ context.getString(R.string.versionName)
 				+ "\",\"installation_id\":\""+ getInstallationId(context)
 				+ "\",\"advertising_id\":\""+ getAdvertisingId()
 				+ "\",\"android_id\":\"" + getAndroidId(context)
 				+ "\",\"appsflyer_id\":\"" + appflyerhelper.getAppsflyerId(context)
 				+ "\",\"device_token\":\"" + getDeviceToken()
 				+ "\",\"registration_id\":\"" + getRegistrationId(context)
 				+ "\",\"profile_id\":\"" + getProfileId(context)
 				+ "\",\"sid\":\"" + getSid(context)
 				+ "\",\"tracking_id\":\"" + getTrackingId()
 				+ "\",\"vendor_id\":\"" + getVendorId()
 				+ "\",\"device_brand\":\"" + getBrand()
 				+ "\",\"device\":\"" + getDevice()
 				+ "\",\"os\":\"" + getOs()
 				+ "\",\"os_version\":\"" + getOsVersion()
 				+ "\",\"device_country\":\"" + getDeviceCountry(context)
 				+ "\",\"device_language\":\"" + getDeviceLanguage()
 				+ "\",\"longitude\":\"" + getLongitude(context)
 				+ "\",\"latitude\":\"" + getLatitude(context)
 				+ "\",\"clicked_item\":\"" + clicked_item
 				+ "\",\"app_mode\":\"" + app_mode
 				+ "\",\"level\":\"" + level
 				+ "\",\"score_type\":\"" + score_type
 				+ "\",\"score_value\":\"" + score_value
 				+ "\",\"currency\":\"" + currency
 				+ "\",\"payment_method\":\"" + payment_method
 				+ "\",\"price\":\"" + price
 				+ "\",\"purchased_item\":\"" + purchased_item 
 				+ "\",\"achievement_name\":\"" + achievement_name
 				+ "\",\"previous_session_time\":\"" + getPreviousSessionTime(context)
 				+ "\",\"connection\":\"" + getConnection(context)
 				+ "\",\"bagged\":" + bagged
 				+ "}}";
 		Log.i(TAG,body);
 		DatabaseHandler db = new DatabaseHandler(context );		
 		Event event = new Event(getGuid(context),getDate(),name,body);
 		db.addEvent(event);
 
 	}
 
 
 	public void infoUpdateCheck(final Context context){
 		SharedPreferences settings = context.getSharedPreferences("SuperGSettings", Context.MODE_PRIVATE);
 		String syncedDeviceSettings = settings.getString("syncedDeviceSettings","");	
 
 		String updatedDeviceSettings = "{"
 				+ "sdk_version\":\""+ context.getString(R.string.versionName)
 				+ "\",\"installation_id\":\""+ getInstallationId(context)
 				+ "\",\"advertising_id\":\""+ getAdvertisingId()
 				+ "\",\"android_id\":\"" + getAndroidId(context)
 				+ "\",\"appsflyer_id\":\"" + appflyerhelper.getAppsflyerId(context)
 				+ "\",\"device_token\":\"" + getDeviceToken()
 				+ "\",\"registration_id\":\"" + getRegistrationId(context)
 				+ "\",\"profile_id\":\"" + getProfileId(context)
 				+ "\",\"sid\":\"" + getSid(context)
 				+ "\",\"tracking_id\":\"" + getTrackingId()
 				+ "\",\"vendor_id\":\"" + getVendorId()
 				+ "\",\"device_brand\":\"" + getBrand()
 				+ "\",\"device\":\"" + getDevice()
 				+ "\",\"os\":\"" + getOs()
 				+ "\",\"os_version\":\"" + getOsVersion()
 				+ "\",\"device_country\":\"" + getDeviceCountry(context)
 				+ "\",\"device_language\":\"" + getDeviceLanguage()
 				+ "\",\"longitude\":\"" + getLongitude(context)
 				+ "\",\"latitude\":\"" + getLatitude(context)
 				+ "\"}";
 
 		if(!syncedDeviceSettings.equals(updatedDeviceSettings) && !syncedDeviceSettings.equals("")){Log.i(TAG,"synced: "+syncedDeviceSettings+" new: "+updatedDeviceSettings);
 		this.deviceInfoUpdate(context);
 		}
 
 		settings.edit().putString("syncedDeviceSettings", updatedDeviceSettings).commit();
 	}
 
 	public void gatherLocation(final Context context){			
 		new Thread()
 		{
 			@Override
 			public void run() {
 
 				LocationManager locationManager = (LocationManager) 
 						context.getSystemService(Context.LOCATION_SERVICE);
 				Criteria criteria = new Criteria();
 				String bestProvider = locationManager.getBestProvider(criteria, false);
 				Location location = locationManager.getLastKnownLocation(bestProvider);
 				LocationListener loc_listener = new LocationListener() {
 					public void onLocationChanged(Location l) {}
 					public void onProviderEnabled(String p) {}
 					public void onProviderDisabled(String p) {}
 					@Override
 					public void onStatusChanged(String provider, int status, Bundle extras) {
 						// TODO Auto-generated method stub
 					}
 				};
 
 				locationManager.requestLocationUpdates(bestProvider, 0, 0, loc_listener);
 
 				location = locationManager.getLastKnownLocation(bestProvider);
 				double lat;
 				double lon;
 
 				try {
 					lat = location.getLatitude();
 					lon = location.getLongitude();
 				} catch (NullPointerException e) {
 					//e.printStackTrace();
 					lat = 0;
 					lon = 0;
 				}	
 
 				SharedPreferences settings = context.getSharedPreferences("SuperGSettings", Context.MODE_PRIVATE);
 				settings.edit().putString("latitude", (String.valueOf(lat) == "0" ? "" : String.valueOf(lat))).commit();
 				settings.edit().putString("longitude", (String.valueOf(lon) == "0" ? "" : String.valueOf(lon))).commit();
 			}
 		};
 		//Log.i(TAG,"Location: "+lat+""+lon);
 	}
 
 	private String getLongitude(Context context){
 		SharedPreferences settings = context.getSharedPreferences("SuperGSettings", Context.MODE_PRIVATE);
 		String longitude = settings.getString("longitude","");		
 		return longitude;
 	}
 
 
 
 	private String getLatitude(Context context){
 		SharedPreferences settings = context.getSharedPreferences("SuperGSettings", Context.MODE_PRIVATE);
 		String latitude = settings.getString("latitude","");		
 		return latitude;
 	}
 
 	public void sessionTimer(final Context context){
 
 		Thread thread = new Thread()
 		{
 			public int timer;
 
 			@Override
 			public void run() {
 				timer = 0;		
 
 				try {
 					while(true) {
 						Thread.sleep(2000);
 						if(inFront(context)){
 							timer += 2;													
 							//Log.i(TAG,"timer: "+timer);
 						}else{
 							SharedPreferences settings = context.getSharedPreferences("SuperGSettings", Context.MODE_PRIVATE);							
 							settings.edit().putString("previous_session_time", String.valueOf(timer)).commit();	
 							timer = 0;
 						}
 
 					}
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		};
 
 		thread.start();	
 	}
 
 	private String getPreviousSessionTime(Context context){
 		SharedPreferences settings = context.getSharedPreferences("SuperGSettings", Context.MODE_PRIVATE);
 		String previousSessionTime = settings.getString("previous_session_time", "");
 
 		if(previousSessionTime == ""){previousSessionTime = "0";}
 
 		return previousSessionTime;
 	}
 
 	private String getDate(){ 
 		Date currentTime = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
 		return sdf.format(currentTime);
 	}
 
 	private String getShortDate(){
 		Date currentTime = new Date();
 		SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmssSS");
 		return sdf.format(currentTime);
 	}
 
 
 
 	private String hashString(String entity, String salt){
 		try {
 			Mac mac = Mac.getInstance("HmacSHA256");
 			mac.init(new SecretKeySpec(salt.getBytes(), "HmacSHA1"));
 			byte[] bs = mac.doFinal(entity.getBytes());
 			StringBuffer sb = new StringBuffer();
 			for (int i = 0; i < bs.length; i++){
 				sb.append(Integer.toString((bs[i] & 0xff) + 0x100, 16).substring(1));
 			}
 			return sb.toString();
 		} catch (InvalidKeyException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalStateException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return "";
 	}
 
 	public boolean inFront(Context context){
 		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
 		List<RunningTaskInfo> services = activityManager
 				.getRunningTasks(Integer.MAX_VALUE);
 
 
 		if (services.get(0).topActivity.getPackageName().toString()
 				.equalsIgnoreCase(context.getPackageName().toString())) {
 			return true;
 		}
 		else{
 			return false;
 		}
 	}
 	
 	private boolean isSyncing = false;
 
 	public void syncEvents(final Context context, final Boolean force){
 
 		Thread thread = new Thread()
 		{
 			@Override
 			public void run() {
 				try {
 
 					while(true) {
 						String eventString = "";
 						if(inFront(context) || force){
 							infoUpdateCheck(context);
 							//Log.i(TAG,"Sync events");
 							gatherLocation(context);
 							DatabaseHandler db = new DatabaseHandler(context );
 							List<Event> events = db.getAllEvents();
 							for (Event ev : events) {
 								//String log = "Guid: "+ev.getGuid()+" ,Date: " + ev.getDate() + " ,Name: " + ev.getName() + "Body" + ev.getBody();
 								//Log.d(TAG, log);
 								eventString = eventString + ev.getBody() + ",";
 							}
 							if(events.size() > 0){
 								if(isSyncing == false){
 									isSyncing = true;
 									eventString =eventString.substring(0, eventString.length() - 1);
 									String json = "{\"push_date\":\"" + getDate() + "\",\"events\":[" + eventString + "]}";
 
 									HttpClient httpclient = MySSLSocketFactory.getNewHttpClient();
 									HttpPost httppost = new HttpPost("http://events.superg.mobi/api/v1/events");
 									try {
 										List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
 										nameValuePairs.add(new BasicNameValuePair("", json));
 										httppost.setHeader("x-api-client", "51fa85fe3547a");
 										httppost.setHeader("x-api-hmac", hashString(json, "7ba080f5000692adf37374629c71d3"));
 										//							        httppost.setHeader("x-api-client", "516e9064dd2f3");
 										//							        httppost.setHeader("x-api-hmac", hashString(json, "f368aaa8d63104735fe3"));							        
 										httppost.setHeader("Content-type", "application/json");
 										httppost.setEntity(new StringEntity(json, "UTF8"));
 
 										HttpResponse response = httpclient.execute(httppost);
 										String body = entityToString( response.getEntity() );
 										Log.i(TAG,"response: "+body);
 
 										int status = response.getStatusLine().getStatusCode();
 										Log.i(TAG,"status: "+String.valueOf(status));
 										if(status == 201){
 											Log.i(TAG,"Succesful EVA sync response");
 											db.deleteAllEvents();
 										}else{
 											Log.i(TAG,"(Partially) unuccesful EVA sync response");
 											try {
 												JSONObject jObject = new JSONObject(body);
 												JSONArray jArray = jObject.getJSONArray("events");
 												for (int i=0; i < jArray.length(); i++)
 												{
 													try {
 														JSONObject oneObject = jArray.getJSONObject(i);
 
 														String eventStatus = oneObject.getString("status");
 														String eventGuid = oneObject.getString("uid");
 														//											        String eventMessage = oneObject.getString("message");
 														String eventException = oneObject.getString("exception");
 
 
 														Event dbEvent = new Event();
 														dbEvent.setGuid(eventGuid);
 														if(eventStatus == "201"){
 															db.deleteEvent(dbEvent);
 														}
 														else{
 															Log.i(TAG,eventException);
 														}
 													} catch (JSONException e) {
 														// Oops
 													}
 												}
 											} catch (JSONException e) {
 												// TODO Auto-generated catch block
 												e.printStackTrace();
 											}
 
 										}
 
 									} catch (ClientProtocolException e) {
 										e.printStackTrace();
 										// TODO Auto-generated catch block
 									} catch (IOException e) {
 										e.printStackTrace();
 										// TODO Auto-generated catch block
 									}								
 								}
 								isSyncing=false;
 							}
 						}
 						if(force == true){
 							break;
 						}
 						Thread.sleep(10000);
 					}
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		};
 
 		thread.start();	
 	}
 
 
 	private String getGuid(Context context){
 		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
 		return  hashIt(tm.getDeviceId()) + getShortDate();
 	}
 
 	private String hashIt(String entity){
 		try {
 			Mac mac = Mac.getInstance("HmacSHA256");
 			String salt = "7ba080f5000692adf37374629c71d3";
 			mac.init(new SecretKeySpec(salt.getBytes(), "HmacSHA1"));
 			byte[] bs = mac.doFinal(entity.getBytes());
 			StringBuffer sb = new StringBuffer();
 			for (int i = 0; i < 7; i++){
 				sb.append(Integer.toString((bs[i] & 0xff) + 0x100, 16).substring(1));
 			}
 			return sb.toString();
 		} catch (InvalidKeyException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalStateException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return "";
 	}
 
 	private String getInstallationId(Context context){
 		SharedPreferences settings = context.getSharedPreferences("SuperGSettings", Context.MODE_PRIVATE);
 		String installationId = settings.getString("installationId", "");
 		if(installationId == ""){
 			String allowedCharacters = "-ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
 			final Random random=new Random();
 			final StringBuilder sb=new StringBuilder();
 			for(int i=0;i<64;++i){
 				sb.append(allowedCharacters.charAt(random.nextInt(allowedCharacters.length())));
 			}
 			settings.edit().putString("installationId", sb.toString()).commit();
 			return sb.toString();
 		}else{
 			return installationId;
 		}
 	}
 
 	private String getAdvertisingId(){
 		return "";
 	}
 
 	private String getAndroidId(Context context){
 		return Secure.getString(context.getContentResolver(),Secure.ANDROID_ID); 
 	}
 
 	private String getDeviceToken(){
 		return "";
 	}
 
 	public String getRegistrationId(Context context){
 		SharedPreferences settings = context.getSharedPreferences("SuperGSettings", Context.MODE_PRIVATE);
 		String registration_id = settings.getString("registration_id","");		
 		return registration_id;
 	}
 
 	private String getProfileId(Context context){
 		SharedPreferences settings = context.getSharedPreferences("SuperGSettings", Context.MODE_PRIVATE);
 		String profile_id = settings.getString("superg_id","");		
 		return profile_id;
 	}
 
 	private String getSid(Context context){
 		SharedPreferences settings = context.getSharedPreferences("SuperGSettings", Context.MODE_PRIVATE);
 		String sid = settings.getString("sid","");		
 		return sid;
 	}
 
 	private String getTrackingId(){
 		return "";
 	}
 
 	private String getVendorId(){
 		return "";
 	}
 
 	private String getBrand(){
 		return Build.BRAND;
 	}
 
 	private String getDevice(){
 		return Build.DEVICE;
 	}
 
 	private String getOs(){
 		return "Android";
 	}
 
 	private String getOsVersion(){
 		return Build.VERSION.RELEASE;
 	}
 
 	private String getDeviceCountry(Context context){
 		return context.getResources().getConfiguration().locale.getCountry();
 	}
 
 	private String getDeviceLanguage(){
 		return Locale.getDefault().getLanguage();
 	}
 
 	private String getInstalledApplications(Context context){
 		SharedPreferences settings = context.getSharedPreferences("SuperGSettings", Context.MODE_PRIVATE);
 		String installedApplications = settings.getString("installedApplications", "");
 		//Log.i(TAG,installedApplications);
 		return installedApplications;
 	}
 
 	private void detectInstalledApplications(Context context) {
 		String json  = "";
 		List<?> packs = context.getPackageManager().getInstalledPackages(0);
 		for(int i=0;i<packs.size();i++) {
 			PackageInfo p = (PackageInfo) packs.get(i);
 			if ((p.versionName == null) || p.packageName.contains("android.") || p.packageName.contains("samsung.")) {//no sys packages
 
 				continue ;
 			}
 			String appname = p.applicationInfo.loadLabel(context.getPackageManager()).toString();
 			String pname = p.packageName;
 
 			json = json + "{\"" + pname + "\":\""+appname+"\"},";
 		}
 		try {
 			json =json.substring(0, json.length() - 1);//remove the last comma
 		} catch (Exception e) {}
 
 		json = "\"installed_applications_android\" : ["+json+"]";
 
 		SharedPreferences settings = context.getSharedPreferences("SuperGSettings", Context.MODE_PRIVATE);
 		settings.edit().putString("installedApplications", json).commit();		
 
 	}	
 
 	private String getConnection(Context context){
 		connection_type = "";
 		try {
 			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
 			NetworkInfo info = cm.getActiveNetworkInfo();
 			//Log.i(TAG, "connection type: "+String.valueOf( info.getType() ) );
 
 			if(info.getType() == 0){
 				//mobile
 				connection_type = "wwan";
 			}else if(info.getType() == 1){
 				connection_type = "wifi";
 				//wifi
 			}else{
 				connection_type = "unknown";
 			}
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return connection_type;
 	}
 
 	private static String entityToString(HttpEntity entity) {
 		InputStream is = null;
 		try {
 			is = entity.getContent();
 		} catch (IllegalStateException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
 		StringBuilder str = new StringBuilder();
 
 		String line = null;
 		try {
 			while ((line = bufferedReader.readLine()) != null) {
 				str.append(line + "\n");
 			}
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		} finally {
 			try {
 				is.close();
 			} catch (IOException e) {
 				//tough luck...
 			}
 		}
 		return str.toString();
 	}
 
 }
