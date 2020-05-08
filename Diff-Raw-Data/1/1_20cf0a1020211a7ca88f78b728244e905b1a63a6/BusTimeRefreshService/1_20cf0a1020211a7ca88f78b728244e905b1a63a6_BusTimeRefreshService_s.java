 package uk.co.mentalspace.android.bustimes;
 
 import java.util.Collections;
 import java.io.Serializable;
 import java.util.List;
 
 import uk.co.mentalspace.android.bustimes.utils.BusTimeComparator;
 
 import android.content.Intent;
 import android.util.Log;
 
 public class BusTimeRefreshService extends WakefulIntentService {
 	private static final String LOGNAME = "BusTimeRefreshService";
 	
 	public static final String ACTION_REFRESH_BUS_TIMES = "uk.co.mentalspace.bustimes.REFRESH";
 	public static final String ACTION_LATEST_BUS_TIMES = "uk.co.mentalspace.bustimes.LATEST_BUS_TIMES";
 	public static final String ACTION_INVALID_REFRESH_REQUEST = "uk.co.mentalspace.bustimes.INVALID_REQUEST";
 	public static final String ACTION_REFRESH_FAILED = "uk.co.mentalspace.bustimes.REFRESH_FAILED";
 	
 	public static final String EXTRA_SOURCE_ID = "sourceId";
 	public static final String EXTRA_LOCATION_ID = "locationId";
 	public static final String EXTRA_BUS_TIMES = "busTimes";
 	public static final String EXTRA_MESSAGE = "message";
 	
 	public BusTimeRefreshService() {
 		super("BusTimeRefreshService");
 	}
 	
 	@Override
 	public void onStart(Intent intent, int startId) {
 		super.onStart(intent, startId);
 	}
 	
 	@Override
 	public void processIntent(Intent arg0) {
 		String action = arg0.getAction();
 		if (Preferences.ENABLE_LOGGING) Log.d(LOGNAME, "Handling action: "+action);
 		
 		if (ACTION_REFRESH_BUS_TIMES.equals(action)) {
 			String sourceId = arg0.getStringExtra(EXTRA_SOURCE_ID);
 			if (null == sourceId || "".equals(sourceId.trim())) {
 				if (Preferences.ENABLE_LOGGING) Log.e(LOGNAME, "Invalid source id for refresh bus times. aborting.");
 				Intent intent = getIntent(ACTION_INVALID_REFRESH_REQUEST, sourceId, -1, null, "Missing location source.");
 				this.sendBroadcast(intent);
 				return;
 			}
 			
 			Source src = SourceManager.getSourceBySourceId(getApplicationContext(), sourceId);
 			if (null == src) {
 				if (Preferences.ENABLE_LOGGING) Log.e(LOGNAME, "No matching source for source id ["+sourceId+"]. aborting.");
 				Intent intent = getIntent(ACTION_INVALID_REFRESH_REQUEST, sourceId, -1, null, "Invalid location source.");
 				this.sendBroadcast(intent);
 				return;
 			}
 			
 			long locationId = arg0.getLongExtra(EXTRA_LOCATION_ID, -1);
 			if (-1 == locationId) {
 				if (Preferences.ENABLE_LOGGING) Log.e(LOGNAME, "No Location ID specified. aborting.");
 				Intent intent = getIntent(ACTION_INVALID_REFRESH_REQUEST, sourceId, locationId, null, "Invalid location id.");
 				this.sendBroadcast(intent);
 				return;
 			}
 			
 			Location loc = LocationManager.getLocationById(this.getApplicationContext(), locationId);
 			if (null == loc) {
 				if (Preferences.ENABLE_LOGGING) Log.e(LOGNAME, "No match location for location id ["+locationId+"]. aborting.");
 				Intent intent = getIntent(ACTION_INVALID_REFRESH_REQUEST, sourceId, locationId, null, "Cannot find location.");
 				this.sendBroadcast(intent);
 				return;
 			}
 			
 			try {
 				BusTimeRefreshTask btrt = src.getBusTimesTask();			
 				List<BusTime> busTimes = btrt.getBusTimes(loc);
 				
 				BusTimeComparator btc = new BusTimeComparator();
 				Collections.sort(busTimes, btc);
 	
 				Intent intent = getIntent(ACTION_LATEST_BUS_TIMES, sourceId, locationId, (Serializable)busTimes, null);
 	
 				int busTimesSize = (null == busTimes) ? -1 : busTimes.size();
 				if (Preferences.ENABLE_LOGGING) Log.d(LOGNAME, "Sending ["+busTimesSize+"] bus times back.");
 				this.sendBroadcast(intent);
 			} catch (Exception e) {
 				Intent intent = getIntent(ACTION_REFRESH_FAILED, sourceId, locationId, null, "Refresh failed: "+e.toString());
 				this.sendBroadcast(intent);
 			}
 		}
 		
 	}
 	
 	protected Intent getIntent(String action, String sourceId, long locationId, Serializable data, String msg) {
 		Intent intent = new Intent();
 		intent.setAction(action);
 		intent.putExtra(EXTRA_SOURCE_ID, sourceId);
 		intent.putExtra(EXTRA_LOCATION_ID, locationId);
 		if (null != data) intent.putExtra(EXTRA_BUS_TIMES, data);
 		if (null != msg) intent.putExtra(EXTRA_MESSAGE, msg);
 		
 		return intent;
 	}
 
 }
