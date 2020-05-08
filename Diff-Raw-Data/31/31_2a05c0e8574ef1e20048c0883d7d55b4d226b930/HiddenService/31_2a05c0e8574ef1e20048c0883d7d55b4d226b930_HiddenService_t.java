 package edu.wsn.phoneusage.main;
 
 import java.util.ArrayList;
 
 import edu.wsn.phoneusage.db.DataProvider;
 import edu.wsn.phoneusage.db.DataProvider_battery;
 import edu.wsn.phoneusage.db.DataProvider_bluetooth;
 import edu.wsn.phoneusage.db.DataProvider_threeg;
 import edu.wsn.phoneusage.db.DataProvider_wifi;
 import edu.wsn.phoneusage.device.BatteryIntentService;
 
 import android.annotation.SuppressLint;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.util.Log;
 
 /**
  * main background handler
  * 
  * @author Lothar Rubusch
  */
 public class HiddenService extends Service {
 	private static final String TAG = "HiddenService";
 
 	public HiddenService(){
 		super();
 		if( null == MainActivity.DB_list ){
 			MainActivity.DB_list = new ArrayList< DataProvider >();
 
 			DataProvider dp = new DataProvider_battery( MainActivity.context, null );
			MainActivity.DB_list.add( dp );
 			dp = new DataProvider_wifi( MainActivity.context, dp.db() );
 			MainActivity.DB_list.add( dp );
 			dp = new DataProvider_threeg( MainActivity.context, dp.db() );
 			MainActivity.DB_list.add( dp );
 			dp = new DataProvider_bluetooth( MainActivity.context, dp.db() );
 			MainActivity.DB_list.add( dp );
 /*
 // TODO refac: create DB by order in components
 
 			for( int idx = 0; idx < ...components.size(); ++idx){
 				
 			}
 //*/
 		}
 	}
 
 	// battery data
 	private Intent intent_battery;
 // TODO messenger - remove or use for debugging / plotting?
 	private Handler handler_battery = new Handler(){
 		@Override
 		public void handleMessage( Message msg ){
 			//String result = msg.getData().getString("result_battery");
 			super.handleMessage(msg);
 		}
 	};
 
 	// wifi, threeg data
 	private NetDeviceObserver netDeviceObserver;
 	private Thread netDeviceThread;
 
 	protected BroadcastReceiver receiver = new BroadcastReceiver(){
 		// in case take phone states into account, this may be extended (telephony states, etc) here
 		@Override
 		public void onReceive( Context context, Intent intent ){
 			String msg = "";
 			if( intent.getAction().equals(Intent.ACTION_AIRPLANE_MODE_CHANGED) ){
 				Bundle extras = intent.getExtras();
 				msg = String.valueOf( (boolean) extras.getBoolean("state") );
 				Log.i( SystemInfo.TIG, TAG + "::onReceive() ACTION_AIRPLANE_MODE_CHANGED " + msg);
 
 			}else if( intent.getAction().equals(Intent.ACTION_BATTERY_LOW) ){
 				Log.i( SystemInfo.TIG, TAG + "::onReceive() ACTION_BATTERY_LOW" );
 
 			}else if( intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED) ){
 				Log.i( SystemInfo.TIG, TAG + "::onReceive() ACTION_BATTERY_CHANGED" );
 				msg = "battery change "
 						+ intent.getIntExtra( "plugged", -1 ) + " "
 						+ intent.getIntExtra( "level", -1 ) + "/"
 						+ intent.getIntExtra( "scale", -1 ) + " "
 						+ intent.getIntExtra( "voltage", -1 ) + " "
 						+ intent.getIntExtra( "temperature", -1 ) + " ";
 				Log.i( SystemInfo.TIG, TAG + "::onReceive() " + msg );
 
 			}else if( intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED) 
 					|| intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED) ){
 				Log.i( SystemInfo.TIG, TAG + "::onReceive() ACTION_PACKAGE_REPLACED or ACTION_PACKAGE_REMOVED" );
 			}
 		}
 	};
 
 	/*
 	 * activity states
 	 */
 
 	@Override
 	public int onStartCommand( Intent intent, int flags, int startId ){
 //		Log.d(SystemInfo.TIG, TIG + "::onStartCommand()");
 
 		// timed start of battery IntentService
 		intent_battery = new Intent( this, BatteryIntentService.class );
 		intent_battery.putExtra("handler_battery", new Messenger(this.handler_battery) );
 		this.startService( intent_battery );
 
 		// wifi, 3g
 		netDeviceObserver = new NetDeviceObserver( this );
 		netDeviceThread = new Thread( netDeviceObserver );
 		netDeviceThread.start();
 
 		return Service.START_STICKY;
 	}
 
 
 	@Override
 	public void onDestroy(){
 //		Log.d( SystemInfo.TIG, TIG + "::onDestroy()");
 		if( null != netDeviceThread ){
 			netDeviceThread.interrupt();
 			while( netDeviceThread.isAlive() ){
 				try{
 					netDeviceThread.join();
 				}catch( InterruptedException e ){
 					Log.w( SystemInfo.TIG, TAG + "   " + "InterruptedException caught");
 				}
 			}
 		}
 		unregisterReceiver( receiver );
 		super.onDestroy();
 	}
 
 	/*
 	 * binder
 	 */
 
 	private IBinder binder = new HiddenServiceBinder();
 
 	public class HiddenServiceBinder extends Binder{
 		HiddenServiceBinder getService(){
 			return HiddenServiceBinder.this;
 		}
 	}
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 //		Log.d(SystemInfo.TIG, TIG + "::onBind()");
 		return binder;
 	}
 };
