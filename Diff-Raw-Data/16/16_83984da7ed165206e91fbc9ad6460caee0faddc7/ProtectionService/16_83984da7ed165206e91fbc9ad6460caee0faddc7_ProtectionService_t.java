 package com.androidhuman.putmedown.service;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.hardware.Sensor;
 import android.hardware.SensorManager;
 import android.os.BatteryManager;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.preference.PreferenceManager;
 
 import com.androidhuman.putmedown.R;
 import com.androidhuman.putmedown.activity.NfcUnlockActivity;
 import com.androidhuman.putmedown.activity.PinUnlockActivity;
 import com.androidhuman.putmedown.util.Util;
 import com.androidhuman.putmedown.util.Util.SensorSupport;
 import com.androidhuman.putmedown.util.Util.SoundSupport;
 import com.androidhuman.putmedown.util.Util.SoundSupport.SoundType;
 
 public class ProtectionService extends Service{
 	
 	public static final String ACTION_ALARM_FIRED = "com.androidhuman.putmedown.intent.action.ALARM_FIRED";
 	private SoundSupport mSoundSupport;
 	private boolean isBatteryListenerRegistered = false;
 	
 	private AntiTheftListener mAntiTheftListener = new AntiTheftListener(){
 
 		@Override
 		public void onChargerStateChanged(boolean plugged) {
 			// If charger has plugged in, stop listening sensor values.
 			if(plugged){
 				mSensorSupport.stopTracking();
 				
 			}else{ // charger has detached. Enable sensor-based protection service.
 				mSensorSupport.stabilize();
 				
 			}
 		}
 		
 		@Override
 		public void onWarning() {
 			// TODO play warning sound
 			mSoundSupport.play(SoundType.WARNING);
 		}
 
 		@Override
 		public void onAlarm() {
 			mSensorSupport.setAlarmFired(true);
 			showNotification();
 			mSoundSupport.play(SoundType.ALARM);
 			sendBroadcast(new Intent(ACTION_ALARM_FIRED));
 		}
 
 		@Override
 		public void onDismiss() {
 			mSensorSupport.setAlarmFired(false);
 			dismissNotification();
 			mSoundSupport.play(SoundType.DISMISS);
 		}
 
 	};
 	
 	private IBinder mBinder = new IProtectionService.Stub() {
 		
 		@Override
 		public void enableService() throws RemoteException {
 			PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("enabled", true).commit();
 			//enableChargerTracking();
 			mSensorSupport.stabilize();
 			mSoundSupport.play(SoundType.ACTIVATE);
 		}
 		
 		@Override
 		public void disableService() throws RemoteException {
 			PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("enabled", false).commit();
 			//disableChargerTracking(true);
 			mSensorSupport.setAlarmFired(false);
 			dismissNotification();
 			
 			mSensorSupport.stopTracking();
 			mSoundSupport.play(SoundType.SHUTDOWN);
 			
 		}
 
 		@Override
 		public void fireWarning() throws RemoteException {
 			mAntiTheftListener.onWarning();
 			
 		}
 
 		@Override
 		public void fireAlarm() throws RemoteException {
 			mAntiTheftListener.onAlarm();
 		}
 
 		@Override
 		public void dismissAlarm() throws RemoteException {
 			mAntiTheftListener.onDismiss();
 		}
 
 		@Override
 		public void playSound(String soundName) throws RemoteException {
 			mSoundSupport.play(soundName);
 		}
 	};
 	
 	@SuppressWarnings("deprecation")
 	private void showNotification(){
 		Notification notification = new Notification();
 		String unlockMethod = PreferenceManager
 				.getDefaultSharedPreferences(getApplicationContext())
 				.getString("unlock_method", "pin");
		System.out.println("unlockmethod="+unlockMethod);
 		
 		PendingIntent intent = 
 				PendingIntent.getActivity(getApplicationContext(), 0, 
 						new Intent(this, unlockMethod.equals("pin") ? PinUnlockActivity.class : NfcUnlockActivity.class), 0);
 		notification.icon = R.drawable.ic_launcher;
 		notification.tickerText = getString(com.androidhuman.putmedown.R.string.anti_theft_alarm);
 		notification.when = System.currentTimeMillis();
 		notification.setLatestEventInfo(getApplicationContext(), getString(R.string.app_name), getString(R.string.enter_pin_or_tag_to_dismiss), intent);
 		
 		notification.flags |= Notification.FLAG_ONGOING_EVENT;
 		NotificationManager notifManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
 		notifManager.notify(0, notification);
 	}
 	
 	private void dismissNotification(){
 		NotificationManager notifManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
 		notifManager.cancel(0);
 	}
 	
 	private void enableChargerTracking(){
 		IntentFilter filter = new IntentFilter();
 		filter.addAction(Intent.ACTION_BATTERY_CHANGED);
 		registerReceiver(mBatteryReceiver, filter);
 		isBatteryListenerRegistered = true;
 	}
 	
 	private void disableChargerTracking(boolean disablingService){
 		if(isBatteryListenerRegistered){
 			//unregisterReceiver(mBatteryReceiver);
 		}
 		if(!disablingService && !mSensorSupport.isTracking()){
 			mSensorSupport.startTracking();
 		}
 	}
 	
 	private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver(){
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			int pluggedState = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
 			
 			// If device is not connected to charger
 			if(pluggedState==0){
 				Util.Charging.setPlugged(getApplicationContext(), false);
 				mAntiTheftListener.onChargerStateChanged(false);
 			}else{
 				Util.Charging.setPlugged(getApplicationContext(), true);
 				mAntiTheftListener.onChargerStateChanged(true);
 			}
 		}
 		
 	};
 	
 	@Override
 	public IBinder onBind(Intent intent) {
 		System.out.println("onBind()");
 		return mBinder;
 	}
 
 	private SensorSupport mSensorSupport;
 	//private Sensor mGyro;
 	
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		System.out.println("onCreate");
 		if(mSensorSupport==null){
 			mSensorSupport = new SensorSupport(this, mAntiTheftListener);
 		}
 		if(mSoundSupport==null){
 			mSoundSupport = new SoundSupport(this);
 			mSoundSupport.loadSounds();
 		}
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		System.out.println("dd");
 		return Service.START_NOT_STICKY;
 	}
 	
 	
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		System.out.println("destroy");
 	}
 
 
 	public interface AntiTheftListener{
 		
 		/**
 		 * Called when charger plugged state has changed.
 		 * @param plugged true when charger has attached to device, false otherwise
 		 */
 		public void onChargerStateChanged(boolean plugged);
 		
 		/**
 		 * Called when device's state is unstable but still insufficient to invoke alarm
 		 */
 		public void onWarning();
 		
 		/**
 		 * Called when device is about to stolen.
 		 */
 		public void onAlarm();
 		
 		/**
 		 * Called when user dismissed alarm.
 		 */
 		public void onDismiss();
 		
 	}
 
 }
