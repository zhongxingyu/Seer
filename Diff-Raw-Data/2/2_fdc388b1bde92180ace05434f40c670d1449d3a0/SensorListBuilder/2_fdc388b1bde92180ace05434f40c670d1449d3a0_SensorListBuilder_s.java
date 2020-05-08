 package sensors;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import spang.mobile.R;
 
 import android.annotation.SuppressLint;
 import android.content.res.Resources;
 import android.hardware.Sensor;
 import android.hardware.SensorManager;
 
 /**
  * Builds a list of all the valid implementations of SpangSensors.
  * @author Gustav Alm Rosenblad, Joakim Johansson & Pontus Pall
  *
  */
 public class SensorListBuilder {
 	private final List<ISensor> sensorList = new ArrayList<ISensor>();
 	private final SensorManager manager;
 	@SuppressLint("UseSparseArrays")
 	private final Map<Integer, ISensor> sensorBindings = new HashMap<Integer, ISensor>();
 
 	/**
 	 * For each new sensor, a binding must be created in the sensorBindings map. 
 	 * @param resources 
 	 * @param context
 	 */
 	public SensorListBuilder(SensorManager manager, Resources resources) {
 		this.manager = manager;
 		
 	
 		if(this.manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!=null)
 			sensorBindings.put(Sensor.TYPE_ACCELEROMETER, new SpangSensor(manager, Sensor.TYPE_ACCELEROMETER, (byte) resources.getInteger(R.integer.Accelerometer)));
 		
 		if(this.manager.getDefaultSensor(Sensor.TYPE_LIGHT)!=null)
 			sensorBindings.put(Sensor.TYPE_LIGHT, new SpangSensor(manager, Sensor.TYPE_LIGHT, (byte) resources.getInteger(R.integer.Luminance)));
 		
 		if(this.manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)!=null)
 			sensorBindings.put(Sensor.TYPE_GYROSCOPE, new SpangSensor(manager, Sensor.TYPE_GYROSCOPE, (byte) resources.getInteger(R.integer.Gyroscope)));
 		
 		if(this.manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)!=null)
 			sensorBindings.put(Sensor.TYPE_MAGNETIC_FIELD, new SpangSensor(manager, Sensor.TYPE_MAGNETIC_FIELD, (byte) resources.getInteger(R.integer.MagneticField)));
 		
 		if(this.manager.getDefaultSensor(Sensor.TYPE_PROXIMITY)!=null)
 			sensorBindings.put(Sensor.TYPE_PROXIMITY, new SpangSensor(manager, Sensor.TYPE_PROXIMITY, (byte) resources.getInteger(R.integer.Proximity)));
 		
 		if(this.manager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY)!=null)
 			sensorBindings.put(Sensor.TYPE_RELATIVE_HUMIDITY, new SpangSensor(manager, Sensor.TYPE_RELATIVE_HUMIDITY, (byte) resources.getInteger(R.integer.Humidity)));
 		
 		if(this.manager.getDefaultSensor(Sensor.TYPE_PRESSURE)!=null)
 			sensorBindings.put(Sensor.TYPE_PRESSURE, new SpangSensor(manager, Sensor.TYPE_PRESSURE, (byte) resources.getInteger(R.integer.AirPressure)));
 		
 		if(this.manager.getDefaultSensor(Sensor.TYPE_GRAVITY)!=null)
 			sensorBindings.put(Sensor.TYPE_GRAVITY, new SpangSensor(manager, Sensor.TYPE_GRAVITY, (byte) resources.getInteger(R.integer.Gravity)));
 		
 		//Both Magnetic sensor and gravity sensor needed to calculate orientation
 		if(this.manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)!=null && this.manager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) 
			sensorBindings.put(Sensor.TYPE_GRAVITY, new OrientationSensor(manager, (byte) resources.getInteger(R.integer.Orientation)));
 
 	}
 
 	/**
 	 * Builds and returns a list of all SpangSensors on the current device.
 	 * @return a list of all SpangSensors on the current device. 
 	 */
 	public List<ISensor> build() {
 		List<Sensor> sensors = manager.getSensorList(Sensor.TYPE_ALL);
 
 		for (Sensor sensor : sensors) {
 			ISensor spangSensor = sensorBindings.get(sensor.getType());
 			if(spangSensor != null)
 				sensorList.add(spangSensor);
 		}
 		return sensorList;
 	}
 }
