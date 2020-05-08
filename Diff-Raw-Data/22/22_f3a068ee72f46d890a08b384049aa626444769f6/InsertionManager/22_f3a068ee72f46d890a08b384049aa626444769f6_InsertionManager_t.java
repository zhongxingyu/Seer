 package org.sensapp.android.sensappdroid.api;
 
 import java.util.ArrayDeque;
 import java.util.Hashtable;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import org.sensapp.android.sensappdroid.contract.SensAppContract;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.util.Log;
 
 public class InsertionManager {
 
 	private final static int LOWEST_INTERVAL = 1;
 	
 	private static Hashtable<String, ArrayDeque<ContentValues>> buffers = new Hashtable<String, ArrayDeque<ContentValues>>();
	private static Hashtable<String, Boolean> empty = new Hashtable<String, Boolean>();
 	
 	private static final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();

 	public static void storeMeasure(final Context context, ContentValues values) {
		Log.w("DEBUG", "__Store__");
 		final String sensor = (String) values.get(SensAppContract.Measure.SENSOR);
 		if (!buffers.containsKey(sensor)) { 
 			buffers.put(sensor, new ArrayDeque<ContentValues>());
			empty.put(sensor, true);
 		}
 		buffers.get(sensor).add(values);
		if (empty.get(sensor)) {
			scheduleFlush(context, sensor);
			empty.put(sensor, false);
		}
 	}
 
 	private static void flushSensorMeasures(Context context, String name) {
		Log.e("DEBUG", "__Flush__");
 		ContentValues values = buffers.get(name).pollFirst(); 
 		while (values != null) {
 			context.getContentResolver().insert(SensAppContract.Measure.CONTENT_URI, values);
 			values = buffers.get(name).pollFirst();
			empty.put(name, true);
 		}
 	}
 	
 	private static void scheduleFlush(final Context context, final String sensor) {
 		worker.schedule(new Runnable() {
 			public void run() {
 				flushSensorMeasures(context, sensor);
 			}
 		}, LOWEST_INTERVAL, TimeUnit.SECONDS);
 	}
 }
