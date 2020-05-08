 /**
  * 
  */
 package edu.pc3.sensoract.vpds.tasklet;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Observable;
 import org.apache.log4j.Logger;
 
 import org.quartz.JobDetail;
 
 import edu.pc3.sensoract.vpds.api.request.WaveSegmentFormat;
 
 /**
  * @author samy
  * 
  */
 public class DeviceEvent extends Observable {
 	
 	//TODO: make the map thread safe for all the methods
 	// use java.util.concurrent.* classes
 
 	private static Map<String, ArrayList<DeviceEventListener>> mapListeners = 
 			new HashMap<String, ArrayList<DeviceEventListener>>();
 
 	public DeviceEvent() {
 	//	mapListeners = new HashMap<String, ArrayList<DeviceEventListener>>();
 	}
 
 	/**
 	 * 
 	 * @param ws
 	 */
 	public void notifyWaveSegmentArrived(WaveSegmentFormat ws) {
 
 		DeviceId deviceId = new DeviceId(ws.secretkey, ws.data.dname,
 				ws.data.sname, ws.data.sid);
 		
		Logger uploadLog = Logger.getLogger("DataUploadWavesegments");
 		uploadLog.info("notifyWaveSegmentArrived.. DeviceId "
 				+ deviceId.toString());
 
 		ArrayList<DeviceEventListener> listListener = mapListeners.get(deviceId
 				.toString());
 		if (null == listListener)
 			return;
 
 		uploadLog.info("notifyWaveSegmentArrived.. Listeners "
 				+ listListener.size() + "\n");
 
 		for (DeviceEventListener listener : listListener) {
 			listener.deviceDataReceived(ws);
 		}
 	}
 
 	/**
 	 * 
 	 * @param deviceId
 	 * @param newListener
 	 */
 	public void addDeviceEventListener(DeviceId deviceId,
 			DeviceEventListener newListener) {
 
 		System.out.println("addDeviceEventListener.. DeviceId "
 				+ deviceId.toString());
 		ArrayList<DeviceEventListener> listListener = mapListeners.get(deviceId
 				.toString());
 
 		if (null == listListener) {
 			listListener = new ArrayList<DeviceEventListener>();
 		}
 		listListener.add(newListener);
 		mapListeners.put(deviceId.toString(), listListener);
 
 		ArrayList<DeviceEventListener> listListener1 = mapListeners
 				.get(deviceId.toString());
 
 		System.out.println("addDeviceEventListener.. Listener count "
 				+ listListener1.size());
 
 	}
 
 	/**
 	 * 
 	 * @param deviceId
 	 * @param jobdetail
 	 */
 	public boolean removeDeviceEventListener(DeviceId deviceId,
 			JobDetail jobdetail) {
 
 		ArrayList<DeviceEventListener> listListener = mapListeners.get(deviceId
 				.toString());
 
 		boolean result = false;
 
 		if (null != listListener) {
 			
 			for (DeviceEventListener listener : listListener) {				
 						
 				if (listener.getJobDetail().equals(jobdetail)) {					
 					result = listListener.remove(listener);					
 					break;
 				}
 			}
 		}		
 		return result;
 	}
 
 }
