 package com.evilcorp.dev.trafficstop;
 
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.net.TrafficStats;
 import android.os.Binder;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 
 public class TrafficConrolService extends Service {
 
 	public static final double BytesCount = 1048576;
 
 	Timer tC;
 	TimerTask ttControl;
 	NotificationCompat.Builder notif;
 
 	SharedPreferences sPref;
 
 	double currentTraff;
 	double startTraff;
 	double traff;
 	double mBytes;
 
 	boolean isLimit;
 	boolean isRound;
 
 	int tarif;
 
 	DB db;
 	Controls ctr;
 
 	@Override
 	public void onCreate() {
 		// TODO Auto-generated method stub
 		super.onCreate();
 		startTraff = TrafficStats.getMobileRxBytes()
 				+ TrafficStats.getMobileTxBytes();
 
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		// TODO Auto-generated method stub
 		initialize();
 		mBytes = intent.getDoubleExtra("mBytes", 10);
 		checkRec();
 		traff = TrafficStats.getMobileRxBytes()
 				+ TrafficStats.getMobileTxBytes() + currentTraff - startTraff;
 
 		if (!isLimit && currentTraff >= mBytes * BytesCount) {
 			ctr.changeState(false);
 			sendNotif(1, false, getResources().getString(R.string.app_name),
 					getResources().getString(R.string.notif_status), false);
 			isLimit = true;
 			sPref.edit().putBoolean("isLimit", true);
 		}
 		if (sPref.getBoolean("pref_foreground", true)) {
 			Intent intent1 = new Intent(this, TrafficControl.class);
 			intent.putExtra("status",
 					getResources().getString(R.string.notif_status));
 
 			PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent1,
 					0);
 			notif = new NotificationCompat.Builder(this)
 			.setSmallIcon(R.drawable.icon)
 			.setContentTitle(
 					getResources().getString(R.string.app_name))
 					.setContentText(
 							getResources().getString(R.string.traff_control))
 							.setContentIntent(pIntent);
 			startForeground(2, notif.build());
 		} else {
 			sendNotif(2, true, getResources().getString(R.string.app_name),
 					getResources().getString(R.string.traff_control), false);
 		}
 
 		if (sPref.getBoolean("pref_round", false)) {
 			isRound = true;
 			String s = sPref.getString("pref_tarif", "0 kB");
 			int space = s.indexOf(" ");
 			tarif = Integer.parseInt(s.substring(0, space));
 		} else {
 			isRound = false;
 			tarif = 0;
 		}
 
 		controlTraff();
 
 		if (sPref.getBoolean("pref_foreground", true)) {
 			return START_REDELIVER_INTENT;
 		} else {
 			return START_NOT_STICKY;
 		}
 	}
 
 	private void initialize() {
 		db = new DB(this);
 		db.open();
 		ctr = new Controls(this);
 
 		sPref = PreferenceManager.getDefaultSharedPreferences(this);
 		isLimit = sPref.getBoolean("isLimit", false);
 
 		tC = new Timer();
 	}
 
 	private void checkRec() {
 		Calendar c = Calendar.getInstance();
 		c.add(Calendar.DATE, 0);
 		if (db.getDateData(c.get(Calendar.DATE) + "-"
 				+ (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.YEAR)) == null) {
 			c.add(Calendar.DATE, -1);
 			HashMap<String, Double> hm1 = db.getDateData(c.get(Calendar.DATE)
 					+ "-" + (c.get(Calendar.MONTH) + 1) + "-"
 					+ c.get(Calendar.YEAR));
 			if (hm1 != null) {
 				c.add(Calendar.DATE, 1);
 				db.addRec(
 						c.get(Calendar.DATE) + "-"
 								+ (c.get(Calendar.MONTH) + 1) + "-"
 								+ c.get(Calendar.YEAR), hm1.get("end")
 								+ startTraff, hm1.get("end") + startTraff);
 			} else {
 				c.add(Calendar.DATE, 1);
 				db.addRec(
 						c.get(Calendar.DATE) + "-"
 								+ (c.get(Calendar.MONTH) + 1) + "-"
 								+ c.get(Calendar.YEAR), startTraff, startTraff);
 			}
 		}
 		HashMap<String, Double> hm = db.getDateData(c.get(Calendar.DATE) + "-"
 				+ (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.YEAR));
 		if (hm != null) {
 			currentTraff = hm.get("end") - hm.get("start");
 		} else {
 			currentTraff = 0;
 		}
 	}
 
 	@Override
 	public void onDestroy() {
 		// TODO Auto-generated method stub
 		super.onDestroy();
 		Log.d("myLogs", "onDestroy serve");
 		Calendar c = Calendar.getInstance();
 		c.add(Calendar.DATE, 0);
 		db.updateRec(c.get(Calendar.DATE) + "-" + (c.get(Calendar.MONTH) + 1)
 				+ "-" + c.get(Calendar.YEAR), traff);
 		db.close();
 		disableTimers();
 		sendNotif(2, false, getResources().getString(R.string.app_name),
 				getResources().getString(R.string.traff_control), true);
 
 	}
 
 	private void disableTimers() {
 		if (ttControl != null)
 			ttControl.cancel();
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		// TODO Auto-generated method stub
 		return new Binder();
 	}
 
 	private void controlTraff() {
 		newTaskControl();
 		tC.schedule(ttControl, 100, 1000);
 	}
 
 	private void newTaskControl() {
 		if(ttControl != null) {
 			ttControl.cancel();
 		}
 		ttControl = new TimerTask() {
 
 			@Override
 			public void run() {
 				// TODO Auto-generated method stub
 				Calendar c = Calendar.getInstance();
 				c.add(Calendar.DATE, 0);
 				traff = TrafficStats.getMobileRxBytes() 
 						+ TrafficStats.getMobileTxBytes() + currentTraff - startTraff;	
 				double allTraff = traff;
 				try {
 					if(db.dbIsOpen()) {
 						HashMap<String, Double> hm2 = db.getDateData(c.get(Calendar.DATE) 
 								+ "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.YEAR));			
 						if(hm2 != null) {
 							allTraff += hm2.get("end");
 						}
 					}
 				}
 				catch(NullPointerException exc) {
 					
 				}
 				Intent intent  = new Intent(TrafficControl.BROADCAST_ACTION)
 				.putExtra("allTraff", allTraff/BytesCount)
 				.putExtra("traff", traff/BytesCount)
 				.putExtra("mBytes", mBytes);
 				sendBroadcast(intent);
 				if(!ctr.isConnected()) {
 					if(sPref.getBoolean("Disconect", true) && tarif > 0) {
 						traff += (tarif - ((int)traff % tarif));
 						Editor ed = sPref.edit();
 						ed.putBoolean("Disconect", false);
 						ed.putBoolean("Connected", true);
 						ed.commit();
 					}
 				}
 				else if(ctr.isConnected()) {
 					if(sPref.getBoolean("Connected", true)) {
 						Editor ed = sPref.edit();
 						ed.putBoolean("Disconect", true);
 						ed.putBoolean("Connected", false);
 						ed.commit();
 					}
 				}
 				if(isRound) {
 					if(!isLimit && Math.round(traff) >= mBytes * BytesCount) {
 						ctr.changeState(false);
 						sendNotif(1, false, getResources().getString(R.string.app_name), 
 								getResources().getString(R.string.notif_status), false);
 						isLimit = true;
 						sPref.edit().putBoolean("isLimit", true);
 						db.updateRec(c.get(Calendar.DATE) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.YEAR), traff);
 					}
 				}
 				else {
 					if(!isLimit && traff >= mBytes * BytesCount) {
 						ctr.changeState(false);
 						sendNotif(1, false, getResources().getString(R.string.app_name), 
 								getResources().getString(R.string.notif_status), false);
 						isLimit = true;
 						sPref.edit().putBoolean("isLimit", true);
 						db.updateRec(c.get(Calendar.DATE) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.YEAR), traff);
 					}
 				}
 
 				changeRecs();
 			}
 		};
 	}
 
 	private void changeRecs() {
 		Calendar c = Calendar.getInstance();
 		c.add(Calendar.DATE, 0);
 
 		if (c.get(Calendar.HOUR_OF_DAY) == 23 && c.get(Calendar.MINUTE) == 59
 				&& c.get(Calendar.SECOND) == 0) {
 			db.updateRec(c.get(Calendar.DATE) + "-"
 					+ (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.YEAR),
 					traff);
 		}
 
 		if (c.get(Calendar.HOUR_OF_DAY) == 0 && c.get(Calendar.MINUTE) == 0
 				&& c.get(Calendar.SECOND) == 0) {
 			HashMap<String, Double> hm1 = db.getDateData(c.get(Calendar.DATE)
 					+ "-" + (c.get(Calendar.MONTH) + 1) + "-"
 					+ c.get(Calendar.YEAR));
 
 			if (hm1 != null) {
 				db.addRec(
 						c.get(Calendar.DATE) + "-"
 								+ (c.get(Calendar.MONTH) + 1) + "-"
 								+ c.get(Calendar.YEAR), hm1.get("end"),
 								hm1.get("end"));
 			}
 			startTraff = TrafficStats.getMobileRxBytes()
 					+ TrafficStats.getMobileTxBytes();
 			currentTraff = 0;
 			isLimit = false;
 			sPref.edit().putBoolean("isLimit", false);
 		}
 
 	}
 
 	public void sendNotif(int id, boolean ongoing, String title, String text,
 			Boolean cancel) {
 		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 
 		notif = new NotificationCompat.Builder(this)
 		.setSmallIcon(R.drawable.icon).setContentTitle(title)
 		.setContentText(text).setOngoing(ongoing);
 
 		if (cancel) {
 			nm.cancel(id);
 			notif.setOngoing(false);
 			return;
 		}
 
 		if (!ongoing) {
 			notif.setDefaults(Notification.DEFAULT_VIBRATE);
 		}
 
 		Intent intent = new Intent(this, TrafficControl.class);
 		intent.putExtra("status",
 				getResources().getString(R.string.notif_status));
 
 		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
 		notif.setContentIntent(pIntent);
 
 		nm.notify(id, notif.build());
 	}
 
 }
