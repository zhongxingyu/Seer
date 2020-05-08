 package com.utopia.lijiang.global;
 
 import com.utopia.lijiang.R;
 import com.utopia.lijiang.alarm.AlarmManager;
import com.utopia.lijiang.alarm.SimpleAlarm;
 import com.utopia.lijiang.location.LocationUtil;
 import com.utopia.lijiang.service.LocationService;
 import com.utopia.lijiang.util.NotificationUtil;
 
import android.content.Context;
import android.content.Intent;
import android.location.Location;

 public class AppLoadHelper {
 
 	private Context ctx = null;
 	
 	public AppLoadHelper(Context context){
 		ctx = context;
 	}
 	
 	public void load(){
 		configNotification();
 		getAlarmsFromDB();
 		getLastKnownLocation();	
 		startLocationService();
 	}
 	
 	/**Reload Alarm Information from SQLite */
  	private void getAlarmsFromDB(){
  		AlarmManager.getInstance().reset();
 		AlarmManager.getInstance().load4DB(ctx, SimpleAlarm.class);
  		Status.getCurrentStatus().addObserver(AlarmManager.getInstance());
  	}
  	
  	/**Configure Notification's setting*/
 	private void configNotification(){
 	    	NotificationUtil.setIcon(R.drawable.ic_launcher);
 	    	NotificationUtil.setTickerText(ctx.getString(R.string.app_name));
 	}
 	
 	/**Get last known location of device*/
 	private Location getLastKnownLocation(){
 	    	Location loc = LocationUtil.getBestLastKnowLocation(ctx);	
 	    	Status.getCurrentStatus().setLocation(loc);	
 	    	return loc;
 	}
 	
 	/**Start Location Service*/
 	private void startLocationService(){
 	    	Intent intent = new Intent(ctx,LocationService.class);
 	    	ctx.startService(intent);  
 	}
 }
