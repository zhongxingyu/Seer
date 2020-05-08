 package edu.wsn.phoneusage.device;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import edu.wsn.phoneusage.db.DataElement;
 import edu.wsn.phoneusage.main.MainActivity;
 import edu.wsn.phoneusage.main.SystemInfo;
 
 import android.app.IntentService;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.BatteryManager;
 import android.util.Log;
 
 /**
  * battery measurement here
  * 
  * @author Lothar Rubusch
  */
 public class BatteryIntentService extends IntentService{
 	private String TAG = "BatteryIntenService";
 
 	public BatteryIntentService(){
 		super( "BatteryIntentService" );
 //		Log.d( SystemInfo.TIG, TAG + "::BatteryIntentService()");
 	}
 
 	// at least for debugging commonunication
 //	private Messenger messenger;
 
 	// the battery Intent listener
 	private BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver(){
 		@Override
 		public void onReceive(Context context, Intent intent){
 //			Log.d(SystemInfo.TIG, TAG + "::BroadcastReceiver::onReceive()" );
 			try{
 				context.unregisterReceiver( this );
 			}catch(IllegalArgumentException e){
 				Log.w(SystemInfo.TIG, TAG + " IllegalArgumentException");
 				e.printStackTrace();
 			}
 
 			int charging = 0; // not charging
 			int status = intent.getIntExtra( BatteryManager.EXTRA_STATUS, -1);
 			boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
 			if( isCharging ){
 				// is plugged
 				int chargePlug = intent.getIntExtra( BatteryManager.EXTRA_PLUGGED, -1);
 				if(chargePlug == BatteryManager.BATTERY_PLUGGED_AC){
 					charging = 1; // AC plugged
 				}else if(chargePlug == BatteryManager.BATTERY_PLUGGED_USB){
 					charging = 2; // charging via USB
 				}
 			}
 
 			// calculate percentage
 			int rawLevel = intent.getIntExtra( BatteryManager.EXTRA_LEVEL, -1);
 			int scale = intent.getIntExtra( BatteryManager.EXTRA_SCALE, -1);
 			int level = -1;
 			if( (0 <= rawLevel) && (0 < scale) ){
 				level = (rawLevel * 100) / scale; 
 			}
 
 /*
 // TODO messenger?
 
 			Message msg = Message.obtain();
 			Bundle data = new Bundle();
 			data.putString("result", String.valueOf( level ));
 			msg.setData( data );
 
 			try{
 				messenger.send( msg );
 			}catch( RemoteException re ){
 				re.printStackTrace();
 			}
 //*/
 
 			// write level to the database
 			DataElement element = new DataElement(); // level, charging );
			Log.d(SystemInfo.TIG, TAG + " - XXX BatteryIntentService level = " + level);
 			element.setValue( "KEY_VALUE", String.valueOf(level));
 // TODO in case better use SystemInfo static constants for DB keys?
 			element.setValue("KEY_CHARGING", String.valueOf(charging));
 /*
 			if( null != MainActivity.DB ){
 				MainActivity.DB.data_battery_save( element );
 			}
 //*/
 
 			if( null != MainActivity.DB_list ){
 				for(int idx=0; idx< MainActivity.DB_list.size(); ++idx){
 // TODO refactor - improve this
 					if( MainActivity.DB_list.get(idx).tablename() == SystemInfo.DB_TABLENAME_BATTERY ){
 						MainActivity.DB_list.get(idx).data_save(element); // FIXME
 						break;
 					}
 				}
 			}
 		}
 	};
 //*/
 
 	private Timer timer = new Timer();
 
 	// on Intent receive, a Messenger and BroadcastReceiver will be generated
 	// to parse the intent's battery information
 	@Override
 	protected void onHandleIntent(Intent intent) {
 //		Log.d( SystemInfo.TIG, TAG + "::onHandleIntent()" );
 
 //		messenger = (Messenger) intent.getExtras().get( "handler_battery" );
 		timer.schedule( new TimerTask(){
 			@Override
 			public void run(){
 				// apply the generated filter to finde battery flags
 
 				// TODO isn't this re-initializing over and over the same task?
 				IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
 				registerReceiver( batteryLevelReceiver, batteryLevelFilter );
 /*
 // TODO messaging actually not needed, in case to refresh the plot!?
 				Message msg = Message.obtain();
 				Bundle data = new Bundle();
 				data.putString("result_battery", String.valueOf( level ));
 				msg.setData( data );
 				try{
 					messenger.send(msg);
 				}catch(RemoteException re){
 					re.printStackTrace();
 				}
 //*/
 			}
 		}, 1234, SystemInfo.PERIOD_BATTERY); // DELAY [ms], PERIOD [ms]
 	}
 };
