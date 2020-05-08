 package fi.side.sensors;
 
 import java.util.ArrayList;
 
 public class Sensor {
 	private ArrayList<SensorListener> listeners;
 	
 	public Sensor() {
 		listeners = new ArrayList<SensorListener>();
 	}
 	
 	public void addListener(SensorListener listener) {
 		this.listeners.add(listener);
 	}
 
 	//For testing only
 	public void  updateSensor(String data){
 		update(data);
 	}
 	
 	private void update(String data){
		listeners.get(0).HandleSensorData(data);
 	}
 	
 }
