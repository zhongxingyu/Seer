 package makeit.phonemonitoring;
 
 import makeit.phonemonitoring.listener.BatteryChangeListener;
 import makeit.phonemonitoring.listener.GPSListener;
 import makeit.phonemonitoring.listener.StateListener;
 import android.app.IntentService;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.location.LocationManager;
 import android.support.v4.content.LocalBroadcastManager;
 import android.telephony.PhoneStateListener;
 import android.telephony.TelephonyManager;
 
 public class LastDataService extends IntentService {
 
     public static final String SERVICE_NAME = "PushDataService";
 
     public static final String DEVICE_ID_EXTRA = "makeit.phonemonitoring.deviceId";
     public static final String SERVER_HOST_EXTRA = "makeit.phonemonitoring.serverHost";
 
     // listeners
     private StateListener stateListener = new StateListener();
     private BatteryChangeListener batteryListener = new BatteryChangeListener();
     private GPSListener gpsListener = new GPSListener();
 
     public LastDataService() {
         super(SERVICE_NAME);
     }
 
     @Override
     protected void onHandleIntent(Intent intent) {
 
         // register listeners
         ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).listen(stateListener,
                 PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                         | PhoneStateListener.LISTEN_SERVICE_STATE | PhoneStateListener.LISTEN_CELL_LOCATION);
 
         this.registerReceiver(batteryListener, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
 
         LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsListener);
         }
 
         try {
             Thread.sleep(5000);
         } catch (InterruptedException e) {
            e.printStackTrace();
         }
 
         // the intent to notify the activity screen
         LastDataIntent lastDataIntent = new LastDataIntent();
 
         // retrieve data
         LastData data = new LastData();
 
         data.setRssi(stateListener.getRSSI());
         data.setOperator(stateListener.getOperator());
         data.setNetworkType(stateListener.getNetworkType());
         data.setRoaming(stateListener.isRoaming());
 
         data.setBatteryLevel(batteryListener.getBatteryLevel());
 
         data.setLatitude(gpsListener.getLatitude());
         data.setLongitude(gpsListener.getLongitude());
 
         lastDataIntent.setLastData(data);
 
        // push data to ther server
         String deviceId = intent.getStringExtra(DEVICE_ID_EXTRA);
         String serverHost = intent.getStringExtra(SERVER_HOST_EXTRA);
         try {
             new M3daPushDataService().pushData(deviceId, data, serverHost);
         } catch (Exception e) {
             e.printStackTrace();
 
             lastDataIntent.setError(e.getMessage());
         }
 
         // broadcast the last data event to refresh the activity screen
         LocalBroadcastManager.getInstance(LastDataService.this).sendBroadcast(lastDataIntent);
     }
 
     @Override
     public void onDestroy() {
 
         // unregister listeners
         ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).listen(stateListener,
                 PhoneStateListener.LISTEN_NONE);
 
         this.unregisterReceiver(batteryListener);
 
         ((LocationManager) getSystemService(Context.LOCATION_SERVICE)).removeUpdates(gpsListener);
 
     }
 
 }
