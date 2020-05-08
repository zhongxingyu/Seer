 package edu.mit.media.icp.client.sensors;
 
 import android.app.Activity;
 import android.content.Context;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import edu.mit.media.icp.client.State;
 
 public class OrientationListener implements SensorEventListener {
 	SensorManager sm;
 	public float kFilteringFactor = (float) 0.05;
 
 	public OrientationListener(Activity a) {
 
 		sm = (SensorManager) a.getSystemService(Context.SENSOR_SERVICE);
 		sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ORIENTATION),
 				SensorManager.SENSOR_DELAY_FASTEST);
 		sm.registerListener(this, sm
 				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
 				SensorManager.SENSOR_DELAY_FASTEST);
 	}
 
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void onSensorChanged(SensorEvent event) {
 		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
 			float direction = event.values[0];
 			State.getOrientation().setCompassDirection(direction);
 			float pitch = event.values[1];
 		}
 		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
 			float rollingZ, rollingX, inclination;
 			float[] vals = event.values;
 			rollingZ = rollingX = inclination = 0;
 			rollingZ = (float) ((vals[2] * kFilteringFactor) + (rollingZ * (1.0 - kFilteringFactor)));
 			rollingX = (float) ((vals[0] * kFilteringFactor) + (rollingX * (1.0 - kFilteringFactor)));
 
 			if (rollingZ != 0.0) {
 				inclination = (float) Math.atan(rollingX / rollingZ);
 			} else if (rollingX < 0) {
 				inclination = (float) (Math.PI / 2.0);
 			} else if (rollingX >= 0) {
 				inclination = (float) (3 * Math.PI / 2.0);
 			}
 			// convert to degress
 			inclination = (float) (inclination * 180f / Math.PI);
 
 			// flip!
 			if (inclination < 0)
 				inclination = inclination + 90;
 			else
 				inclination = inclination - 90;
 
 			State.getOrientation().setPitch(inclination);
 		}
 	}
 
 }
