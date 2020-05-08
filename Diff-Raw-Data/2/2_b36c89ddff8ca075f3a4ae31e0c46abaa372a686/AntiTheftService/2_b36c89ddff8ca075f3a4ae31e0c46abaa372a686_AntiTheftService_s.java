 package ch.ethz.inf.vs.android.siwehrli.antitheft;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.media.MediaPlayer;
 import android.os.IBinder;
 import android.telephony.SmsManager;
 import android.util.FloatMath;
 import android.util.Log;
 
 public class AntiTheftService extends Service {
 	public static final int NOTIFICATION_ACTIVATED_ID = 1;
 
 	private boolean activate = MainActivity.ACTIVATE_DEFAULT;
 	private int sensitivity = MainActivity.SENSITIVITY_DEFAULT;
 	private int timeout = MainActivity.TIMEOUT_DEFAULT; // in seconds!
 	private boolean inform = MainActivity.INFORM_DEFAULT;
 	private String phoneNumber = MainActivity.PHONE_NUMBER_DEFAULT;
 
 	private SensorEventListener listener;
 	private SensorManager sensorManager;
 	private Sensor sensor;
 
 	private int unsigCounter = 0;
	private static final int UNSIG_COUNTER_THRESHHOLD = 20;
 	private static final double CHANGE_100_PERCENT = 4; // defined by
 														// examination of
 														// average sensor
 														// values
 	private static final long ACCIDENTIAL_MOVEMENT_MAX_TIME = 5000; // arbitrarily
 																	// defined
 																	// by
 																	// exercise
 																	// sheet
 	private long lastUnsignificantSensorChange = 0;
 	private long lastCheckpoint = 0;
 	private long activationTime;
 
 	private boolean timeoutStarted = false;
 	private boolean alarmStarted = false;
 	private long timeoutStartTime;
 
 	private final class MySensorEventListener implements SensorEventListener {
 		private float lv1 = 0, lv2 = 0, lv3 = 0;
 
 		public void onAccuracyChanged(Sensor sensor, int accuracy) {
 
 		}
 
 		public void onSensorChanged(SensorEvent event) {
 
 			long now = System.currentTimeMillis();
 
 			if (Math.abs(activationTime - now) > 2000) {
 				// calculate the norm of the relative change of 3 component
 				// sensor data
 				float v1 = event.values[0];
 				float v2 = event.values[1];
 				float v3 = event.values[2];
 
 				float change = (v1 - lv1) * (v1 - lv1) + (v2 - lv2)
 						* (v2 - lv2) + (v3 - lv3) * (v3 - lv3);
 				change = FloatMath.sqrt(change);
 				boolean sig = true;
 
 				// check if change is not significant (dependent on
 				// sensitivity
 				// settings)
 				if (change < calculateNormThreshhold(sensitivity)) { // change
 																		// is
 																		// not
 																		// significant
 					lastUnsignificantSensorChange = now;
 					sig = false;
 
 					if (unsigCounter < UNSIG_COUNTER_THRESHHOLD) {
 						unsigCounter++;
 					} else {
 						lastCheckpoint = lastUnsignificantSensorChange;
 						unsigCounter = 0;
 					}
 				} else {
 					unsigCounter = 0;
 				}
 
 				// check if movement is more then an accidential movement
 				// (more
 				// than 5 seconds movement)
 				if (Math.abs(lastCheckpoint - now) > ACCIDENTIAL_MOVEMENT_MAX_TIME) {
 					startTimeout(now);
 
 				}
 
 				checkTimeout(now);
 
 				lv1 = v1;
 				lv2 = v2;
 				lv3 = v3;
 
 				Log.d("AntiTheftService", change + "   Significant: " + sig);
 
 			} else {
 				lastCheckpoint = now;
 				lastUnsignificantSensorChange = now;
 			}
 		}
 	}
 
 	@Override
 	public void onCreate() {
 		listener = new MySensorEventListener();
 
 		// initialize sensor
 		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
 		sensor = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
 
 		// start sensor
 		sensorManager.registerListener(listener, sensor,
 				SensorManager.SENSOR_DELAY_NORMAL);
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		this.activate = intent.getBooleanExtra(
 				"ch.ethz.inf.vs.android.siwehrli.antitheft.activate",
 				MainActivity.ACTIVATE_DEFAULT);
 		if (activate) {
 			// save time
 			this.activationTime = System.currentTimeMillis();
 
 			// show notification
 			NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 
 			int icon = R.drawable.notification_icon;
 			CharSequence tickerText = getResources().getString(
 					R.string.ticker_text);
 			long when = System.currentTimeMillis();
 			Notification notification = new Notification(icon, tickerText, when);
 			notification.flags |= Notification.FLAG_NO_CLEAR
 					| Notification.FLAG_ONGOING_EVENT;
 
 			Context context = getApplicationContext();
 			CharSequence contentTitle = getResources().getString(
 					R.string.ticker_text);
 			CharSequence contentText = getResources().getString(
 					R.string.notification_comment);
 			Intent notificationIntent = new Intent(this, MainActivity.class);
 			notificationIntent.putExtra("activate", false);
 			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
 
 			PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
 					notificationIntent, 0);
 
 			notification.setLatestEventInfo(context, contentTitle, contentText,
 					contentIntent);
 
 			mNotificationManager
 					.notify(NOTIFICATION_ACTIVATED_ID, notification);
 
 			// read configuration options out of intent
 			this.sensitivity = intent.getIntExtra(
 					"ch.ethz.inf.vs.android.siwehrli.antitheft.sensitivity",
 					MainActivity.SENSITIVITY_DEFAULT);
 			this.timeout = intent.getIntExtra(
 					"ch.ethz.inf.vs.android.siwehrli.antitheft.timeout",
 					MainActivity.TIMEOUT_DEFAULT);
 			this.inform = intent.getBooleanExtra(
 					"ch.ethz.inf.vs.android.siwehrli.antitheft.inform",
 					MainActivity.INFORM_DEFAULT);
 			// attention: phoneNumber is null if String no found
 			this.phoneNumber = intent
 					.getStringExtra("ch.ethz.inf.vs.android.siwehrli.antitheft.phone_number");
 		}
 
 		// If we get killed, after returning from here, restart
 		return START_STICKY;
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		// We don't provide binding, so return null
 		return null;
 	}
 
 	@Override
 	public void onDestroy() {
 		sensorManager.unregisterListener(listener);
 
 		if (alarmPlayer != null)
 			alarmPlayer.stop();
 	}
 
 	private void startTimeout(long now) {
 		if (!this.timeoutStarted) {
 			MediaPlayer mp = MediaPlayer.create(this, R.raw.timeout);
 			mp.setVolume(1.0f, 1.0f);
 			mp.start();
 
 			this.timeoutStarted = true;
 			this.timeoutStartTime = now;
 
 			Log.d("AntiTheftService", "TIMEOUT STARTED");
 		}
 	}
 
 	MediaPlayer alarmPlayer;
 
 	private void startAlarm() {
 		if (!this.alarmStarted) {
 			alarmStarted = true;
 			alarmPlayer = MediaPlayer.create(this, R.raw.alarm);
 			alarmPlayer.setVolume(1.0f, 1.0f);
 			alarmPlayer.setLooping(true);
 			alarmPlayer.start();
 
 			// if required by settings, send sms to friend
 			if (this.inform) {
 				SmsManager sm = SmsManager.getDefault();
 				// here is where the destination of the text should go
 				sm.sendTextMessage(phoneNumber, null,
 						getResources().getString(R.string.inform_message),
 						null, null);
 			}
 
 			Log.d("AntiTheftService", "ALARM STARTED");
 		}
 	}
 
 	private void checkTimeout(long now) {
 		if (this.timeoutStarted
 				&& Math.abs(now - this.timeoutStartTime) > this.timeout * 1000) {
 			this.startAlarm();
 		}
 	}
 
 	public static float calculateNormThreshhold(int sensitivity) {
 		return (float) ((float) (100 - sensitivity) / 100f * CHANGE_100_PERCENT);
 	}
 }
