 package edu.oregonstate.biomed.actigps;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.UUID;
 import java.util.concurrent.locks.ReentrantLock;
 
 import android.content.Intent;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.util.Log;
 
 public class SensorReceiver implements SensorEventListener, ActivitySensor
 {
 	private ActivityTrackerService parentService = null;
 	private SensorManager mSensorManager = null;
 	private Sensor mAccelerometer = null;
 	
 	private ReentrantLock dataLock = new ReentrantLock();
 	
 	private ArrayList<Float> accelx = new ArrayList<Float>();
 	private ArrayList<Float> accely = new ArrayList<Float>();
 	private ArrayList<Float> accelz = new ArrayList<Float>();
 	private ArrayList<Long> accelt = new ArrayList<Long>();
 	
 	private final UUID xUUID = UUID.fromString("2a26c468-bb61-4981-8ccb-24a40e85b41e");
 	private final UUID yUUID = UUID.fromString("1fc8a314-ca47-4376-8aa4-bf77237dd809");
 	private final UUID zUUID = UUID.fromString("b792a01e-94e7-4a14-bdff-a5d82a893802");
 	
 	private long timeoffset = 0;
 	private long timeoffset_start = 0;
 	
 	public SensorReceiver(ActivityTrackerService serv)
 	{
 		parentService = serv;
 		
 		mSensorManager = serv.sensors;
 		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
 		
 		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public String getDataString()
 	{
 		final int pid = parentService.getPatientId();
 		String data = "";
 
 		dataLock.lock(); /* acquire data lock: we don't want data changing while we are reading it! */
 		
 		/* copy data to arrays */
 		ArrayList<Float> x = (ArrayList<Float>) accelx.clone();
 		ArrayList<Float> y = (ArrayList<Float>) accely.clone();
 		ArrayList<Float> z = (ArrayList<Float>) accelz.clone();
 		ArrayList<Long> t = (ArrayList<Long>) accelt.clone();
 		
 		dataLock.unlock(); /* release data lock */
 		
 		Log.d(ActivityTrackerService.TAG, "Buiding Strings...");
 		if( accelt.size() > 0 )	
 		{
 			data = buildDataString(x, y, z, t, pid);
 		}
 		Log.d(ActivityTrackerService.TAG, "Done buidling strings...");
 		
 		
 		return data;
 	}
 
 	@Override
 	public void clearData()
 	{
 		dataLock.lock(); /* acquire data lock */
 		
 		accelx.clear();
 		accely.clear();
 		accelz.clear();
 		accelt.clear();
 		
 		dataLock.unlock(); /* release data lock */
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent event)
 	{
 		switch (event.sensor.getType()) {
 			case Sensor.TYPE_ACCELEROMETER:
 				dataLock.lock(); /* acquire data lock */
 				
 				accelx.add(event.values[0]);
 				accely.add(event.values[1]);
 				accelz.add(event.values[2]);
 				
 //				Log.i(ActivityTrackerService.TAG, "Accelerometer received data of (" + 
 //						event.values[0] + "," + event.values[1] + "," + event.values[2] + ")");
 				
 				if (timeoffset == 0) {
 					timeoffset = (new Date()).getTime();
 					timeoffset_start = event.timestamp;
 				}
 				
 				/* build the timestamp using the nanotime stamp from the sensor and the
 				   offset from the RTC, keep the result in microseconds */
 				accelt.add(timeoffset*1000+(event.timestamp - timeoffset_start)/1000);			
 				
 				Intent i = new Intent("PHONE_ACCEL_UPDATE");
 				Bundle b = new Bundle();
 				b.putString("x", Float.toString(event.values[0]));
 				b.putString("y", Float.toString(event.values[1]));
 				b.putString("z", Float.toString(event.values[2]));
 				b.putString("t", accelt.get(accelt.size() - 1).toString());
 				i.putExtras(b);
 				parentService.sendBroadcast(i);
 				
 				dataLock.unlock(); /* release data lock */
 				break;
 			case Sensor.TYPE_GYROSCOPE:
 				
 				break;
 			case Sensor.TYPE_MAGNETIC_FIELD:
 				
 				break;
 			}
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy)
 	{
 		/* We will ignore this for now... but post a log message about it */
 		Log.i(ActivityTrackerService.TAG, "Sensor accuracy change");
 	}
 
 	@Override
 	public void unregister()
 	{
 		mSensorManager.unregisterListener(this);
 	}
 	
 	/**
 	 * Create a string for the HTTP post based on data, uuid, pid, and timestamps
 	 * @param list raw position data
 	 * @param u uuid of sensor
 	 * @param pid patient ID
 	 * @param times array of timestamps matching data
 	 * @return formatted string
 	 */
 	private String buildDataString(ArrayList<Float> xvals, ArrayList<Float> yvals, ArrayList<Float> zvals, 
 								   ArrayList<Long> tvals, int pid) {		
 		
 		/* use of StringBuilder avoids garbage collection when appending to strings */
		StringBuilder strBldr = new StringBuilder();
 		float xval, yval, zval, tval;
 		
 		if((xvals.size() != yvals.size()) || (xvals.size() != zvals.size())) 
 		{
 			Log.e(ActivityTrackerService.TAG, "Critical Error: Number of points in accelerometer arrays do not match.");
 			return "";
 		}
 		
 		Log.i(ActivityTrackerService.TAG, "Data Points: " + xvals.size());
 		
 		for(int i = 0; i < xvals.size(); i++)
 		{
 			xval = xvals.get(i);
 			yval = yvals.get(i);
 			zval = zvals.get(i);
 			if( tvals.size() > i )
 			{
 				tval = tvals.get(i) / 1000;
				strBldr.append(tval + " " + pid + " " + xUUID + " " + xval + "\r\n");
				strBldr.append(tval + " " + pid + " " + yUUID + " " + yval + "\r\n");
				strBldr.append(tval + " " + pid + " " + zUUID + " " + zval + "\r\n");
 			}
 			else
 			{
 				Log.w(ActivityTrackerService.TAG, "Warning: more Accelerometer data points than timestamps. Disregarding data point.");
 			}
 		}
 		
		return strBldr.toString();
 	}
 
 }
