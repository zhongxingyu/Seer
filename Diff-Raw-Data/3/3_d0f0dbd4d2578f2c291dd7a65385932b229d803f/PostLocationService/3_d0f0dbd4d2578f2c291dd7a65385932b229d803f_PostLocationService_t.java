 //xumengyi@baixing.com
 package com.baixing.util.post;
 
 import java.io.UnsupportedEncodingException;
 
 import android.os.Handler;
 import android.os.Message;
 import android.util.Pair;
 
 import com.baixing.android.api.WebUtils;
 import com.baixing.data.GlobalDataManager;
 import com.baixing.data.LocationManager;
 import com.baixing.entity.BXLocation;
 import com.baixing.util.Communication;
 import com.baixing.util.LocationService;
 import com.baixing.util.LocationService.BXRgcListener;
 
 public class PostLocationService implements BXRgcListener, LocationManager.onLocationFetchedListener {
 	private boolean gettingLocationFromBaidu = false;
 	private boolean inreverse = false;
 	private Handler handler;
 	
 	public PostLocationService(Handler handler){
 		this.handler = handler;
 	}
 	
 	public void start(){
 		inreverse = false;
 		GlobalDataManager.getInstance().getLocationManager().addLocationListener(this);
 	}
 	
 	public void stop(){
 		GlobalDataManager.getInstance().getLocationManager().removeLocationListener(this);
 	}
 	
 	public boolean retreiveLocation(String city, String addr){
 		this.gettingLocationFromBaidu = true;
 		return LocationService.getInstance().geocode(addr, city, this);
 	}
 	
 	static public Pair<Double, Double> retreiveCoorFromGoogle(String addr){
 		if(addr == null || addr.equals("")){
 			return new Pair<Double, Double>((double)0, (double)0);
 		}
 		String googleUrl = String.format("http://maps.google.com/maps/geo?q=%s&output=csv", addr);
 		try{
			String googleJsn = Communication.getDataByUrlGet(googleUrl);
//			String googleJsn = WebUtils.doGet(GlobalDataManager.getInstance().getApplicationContext(), googleUrl, null);//Communication.getDataByUrlGet(googleUrl);
 			String[] info = googleJsn.split(",");
 			if(info != null && info.length == 4){
 				return new Pair<Double, Double>(Double.parseDouble(info[2]), Double.parseDouble(info[3]));
 			}
 		}catch(UnsupportedEncodingException e){
 			e.printStackTrace();
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		return new Pair<Double, Double>((double)0, (double)0);
 	}
 	
 	@Override
 	public void onRgcUpdated(BXLocation location) {
 		// TODO Auto-generated method stub
 		if(!this.gettingLocationFromBaidu) return;
 		// TODO Auto-generated method stub
 		if(!inreverse && location != null && (location.subCityName == null || location.subCityName.equals(""))){
 			LocationService.getInstance().reverseGeocode(location.fLat, location.fLon, this);
 			inreverse = true;
 		}else{
 			Message msg = Message.obtain();
 			msg.what = PostCommonValues.MSG_GEOCODING_FETCHED;
 			msg.obj = location;
 			handler.sendMessage(msg);
 		}		
 		gettingLocationFromBaidu = false;
 	}
 
 	@Override
 	public void onLocationFetched(BXLocation location) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void onGeocodedLocationFetched(BXLocation location) {
 		// TODO Auto-generated method stub
 		if(location == null) return;
 		Message msg = Message.obtain();
 		msg.what = PostCommonValues.MSG_GPS_LOC_FETCHED;
 		msg.obj = location;
 		handler.sendMessage(msg);
 	}
 }
