 package jp.shuri.android.pedometer;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import jp.shuri.android.pedometer.PedometerProvider.Contract;
 
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.Cursor;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.net.Uri;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.util.Log;
 
 public class MainService extends Service implements SensorEventListener {
 	
 	private final String TAG = "MainService";
 	public static final String ACTION = "jp.shuri.android.pedometer.MainService.countup";
 	private SensorManager sensorManager;
 	private Sensor accelerometer;
 	private int count;
 	private boolean isExist = false;
 	
 	private class ShutdownReceiver extends BroadcastReceiver {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			Log.d(TAG, "shutdown");
 			output2table(context);
 		}
 	}
 	
 	private class DateChangeReceiver extends BroadcastReceiver {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			Log.d(TAG, "DateChange");
 			output2table(context);
 			count = 0;
 		}
 		
 	}
 	
 	private ShutdownReceiver sReceiver = new ShutdownReceiver();
 	private DateChangeReceiver dReceiver = new DateChangeReceiver();
 	
 	private String getQueryString() {
 		final Calendar calendar = Calendar.getInstance();
 
 		final int year = calendar.get(Calendar.YEAR);
 		final int month = calendar.get(Calendar.MONTH);
 		final int day = calendar.get(Calendar.DAY_OF_MONTH);
 		
 		return "" + year + "-" + (month + 1) + "-" + day;
 	}
 	
 	private void output2table(Context context) {	
 		Log.d(TAG, "output2table");
 		String queryString = getQueryString();
 		Cursor mCursor =
             context.getContentResolver().query(Contract.Log.contentUri, null, Contract.Log.columns.get(2) + " = " + queryString, null, null);
 
		if (mCursor == null) {
 			Log.d(TAG, "no records");
 			ContentValues initialValues = new ContentValues();
 	        initialValues.put(Contract.Log.columns.get(2), queryString);
 	        initialValues.put(Contract.Log.columns.get(1), count);
 
 	        Uri tmp = context.getContentResolver().insert(Contract.Log.contentUri, initialValues);
 	        Log.d(TAG, "insert result : " + tmp);
 		} else {
 			Log.d(TAG, "record exist " + queryString);
 			ContentValues args = new ContentValues();
 	        args.put(Contract.Log.columns.get(2), queryString);
 	        args.put(Contract.Log.columns.get(1), count);
 
 	        int count = context.getContentResolver().update(Contract.Log.contentUri, args, Contract.Log.columns.get(2) + " = " + queryString, null);
 	        Log.d(TAG, "update count is " + count);
 		}
 
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		
 		sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
         
         List<Sensor> list;
         list=sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
         if (list.size()>0) accelerometer=list.get(0);
         
         if (accelerometer != null) {
             sensorManager.registerListener(this, accelerometer,SensorManager.SENSOR_DELAY_FASTEST);
         }
         
         //IntentFilter rebootIntentfilter = new IntentFilter("android.intent.action.REBOOT");
         //IntentFilter shutdownIntentfilter = new IntentFilter ("android.intent.action.ACTION_SHUTDOWN");
         //IntentFilter dateChangeIntentfilter = new IntentFilter("android.intent.action.DATE_CHANGED");
         //IntentFilter rebootIntentfilter = new IntentFilter(Intent.ACTION_REBOOT);
         IntentFilter shutdownIntentfilter = new IntentFilter (Intent.ACTION_SHUTDOWN);
         IntentFilter dateChangeIntentfilter = new IntentFilter(Intent.ACTION_DATE_CHANGED);
         
         //registerReceiver(sReceiver, rebootIntentfilter);
         registerReceiver(sReceiver, shutdownIntentfilter);
         registerReceiver(dReceiver, dateChangeIntentfilter);
 
         // 日付で fetch してあれば count の値を、なければ 0 を
 		String queryString = getQueryString();
 		Cursor mCursor =
             getApplicationContext().getContentResolver().query(Contract.Log.contentUri, null, Contract.Log.columns.get(2) + " = " + queryString, null, null);
		if (mCursor == null) {
 			Log.d(TAG, "query returns no row");
 			count = 0;
 		} else {
            count = mCursor.getInt(mCursor.getColumnIndexOrThrow(Contract.Log.columns.get(1)));
             Log.d(TAG, "count = " + count);
 		}
 		debugPrint();
 	}
 
 	private void debugPrint() {
 		Cursor c = getApplicationContext().getContentResolver().query(Contract.Log.contentUri, null, null, null, "_id DESC");
 		if (c == null) {
 			Log.d(TAG, "no records");
 			return;
 		}
 		int count = c.getCount();
 		c.moveToFirst();
 		for (int i = 0; i < count; i++) {
 			int id = c.getInt(0);
 			String date = c.getString(1);
 			int counter = c.getInt(2);
 			Log.d(TAG, "record no. " + i + " id : " + id + "Date : " + date + " count : " + counter);
 		}
 	}
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		Log.d(TAG, "onBind");
 		isExist = true;
 		return IMainServiceBinder;
 	}
 
 	@Override
 	public void onRebind(Intent intent) {
 		Log.d(TAG, "onRebind");
 		isExist = true;
 		super.onRebind(intent);
 
 	}
 
 	@Override
 	public boolean onUnbind(Intent intent) {
 		Log.d(TAG, "onUnbind");
 		isExist = false;
 		return true;
 	}
 
 	@Override
 	public void onStart(Intent intent, int startId) {
 		super.onStart(intent, startId);
 		
 		Log.d(TAG, "onStart");
 	}
 	
 	private IMainService.Stub IMainServiceBinder = new IMainService.Stub() {
 
 		@Override
 		public int getCount() throws RemoteException {
 			return count;
 		}
 	};
 
 	@Override
 	public void onAccuracyChanged(Sensor arg0, int arg1) {
 		
 	}
 
     private double d1, d2;
     private double k = 0.125;
     private boolean first = true;
     
     private double iirlpf(double in, boolean reset) {
             double t;
 
             if (!reset) {
                     t = d1;
                     d1 += (d2 - in) * k; 
                     d2 -= (t + d2 * 2.0) * k; /* Q = 0.5 */
                     return d2;
             }else{
                     d1 = -2.0 * in;
                     d2 = in;
                     return d2;
             }
     }
     
     private double lastValue;
     private long lastDate;
     private double topValue;
     private long topDate;
     private boolean measurement = false;
     
     private void output_logcat(double d) {
     	Log.d(TAG, " " + d + " ");
     }
     	
 	@Override
 	public void onSensorChanged(SensorEvent event) {
         double currentValue;
         if (event.sensor==accelerometer) {
         	double tmp = Math.sqrt(Math.pow((double)event.values[0], 2.0) + 
         			Math.pow((double)event.values[1], 2.0) + 
         			Math.pow((double)event.values[2], 2.0));
 
             currentValue = iirlpf(tmp, first);
             if (first) first = false;
             //output_logcat(currentValue);
             
             if (lastValue < currentValue) {
                 if (measurement) {
                     measurement = false;
                     if ((lastDate - topDate < 250) &&
                         (topValue - lastValue > 0.35)) {
                     	count++;
                     	if(isExist) {
                     		Intent i = new Intent(ACTION);
                     		i.putExtra("count", String.valueOf(count));
                     		sendBroadcast(i);
                     	}
                     }
                 }
             } else {
             	if (!measurement) {
             		topValue = lastValue;
             		topDate = lastDate;
             		measurement = true;
             	}
             }
             lastValue = currentValue;
             lastDate = new Date().getTime();
         }		
 	}
 
 }
