 /*
  * Waker project 2013
  * 
  * folks studio
  * 
  * by caifangmao8@gmail.com
  */
 package com.cfm.waker.service;
 
 import java.util.Calendar;
 
 import com.cfm.waker.WakerApplication;
 import com.cfm.waker.dao.WakerDatabaseHelper;
 import com.cfm.waker.entity.Alarm;
 import com.cfm.waker.log.WLog;
 import com.cfm.waker.ui.ShakeActivity;
 import com.cfm.waker.util.CursorableList;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.IBinder;
 
 public class WakerService extends Service {
 	
 	private Context context;
 	
 	private int alarmId;
 	
 	private AlarmManager alarmManager;
 	
 	private LocalBinder localBinder;
 	
 	public class LocalBinder extends Binder{
 		public WakerService getService(){
 			return WakerService.this;
 		}
 	}
 	
 	@Override
 	public void onCreate(){
 		super.onCreate();
 		WLog.print("SERVICE", "service started");
 		context = getBaseContext();
 		
 		alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
 		localBinder = new LocalBinder();
 	}
 	
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId){
 		alarmId = 0;
 		
 		Intent serviceIntent = new Intent(this, WakerService.class);
 		PendingIntent pendingIntent = PendingIntent.getService(context, 0, serviceIntent, alarmId);
 		alarmManager.set(AlarmManager.RTC_WAKEUP, getNextZero(), pendingIntent);
 		
 		setAlarms();
 		
 		flags = START_STICKY;
 		return super.onStartCommand(intent, flags, startId);
 	}
 	
 	private void setAlarms(){
		if(WakerDatabaseHelper.getInstance(context).getDBCount() > 0){
			CursorableList<Alarm> alarms = new CursorableList<Alarm>();
			alarms.addAll(WakerDatabaseHelper.getInstance(context).getAlarms(((WakerApplication) getApplication()).is24()));
			if(alarms.moveToFirst()){
				do{
					setAlarm(alarms.get());
				}while(alarms.moveToNext());
			}
 		}
 	}
 	
 	public void setAlarm(Alarm alarm){
 		WLog.print("SERVICE", "activity message sent:" + alarm.getHour() + ":" + alarm.getMinute());
 		long currentTime = System.currentTimeMillis();
 		long time = alarm.getCalendar().getTimeInMillis();
 		if(currentTime < time && alarm.isEnabled() && isDaySet(alarm)){
 			Intent alarmIntent = new Intent(this, ShakeActivity.class);
 			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, alarmIntent, ++alarmId);
 			alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
 		}
 	}
 	
 	private boolean isDaySet(Alarm alarm){
 		Calendar calendar = Calendar.getInstance();
 		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
 		
 		return alarm.isDaySet(dayOfWeek);
 	}
 
 	private long getNextZero(){
 		Calendar calendar = Calendar.getInstance();
 		calendar.set(Calendar.HOUR, 0);
 		calendar.set(Calendar.MINUTE, 0);
 		calendar.set(Calendar.SECOND, 0);
 		
 		return calendar.getTimeInMillis() + 86400000L;
 	}
 	
 	@Override
 	public IBinder onBind(Intent intent) {
 		return localBinder;
 	}
 
 	@Override
 	public void onDestroy(){
 		super.onDestroy();
 		Intent intent = new Intent(this, WakerService.class);
 		context.startService(intent);
 	}
 }
