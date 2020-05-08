 package no.ntnu.falldetection.controllers;
 
 import java.util.ArrayList;
 
 import no.ntnu.falldetection.models.AlarmEvent;
 import no.ntnu.falldetection.models.AlarmListener;
import no.ntnu.falldetection.models.ThresholdAlarm;
 import no.ntnu.falldetection.utils.SensorEvent;
 import no.ntnu.falldetection.utils.SensorListener;
 import no.ntnu.falldetection.utils.motea.Mote;
 import no.ntnu.falldetection.utils.motea.StatusInformationReport;
 import no.ntnu.falldetection.utils.motea.event.AccelerometerEvent;
 import no.ntnu.falldetection.utils.motea.event.AccelerometerListener;
 import no.ntnu.falldetection.utils.motea.event.GyroEvent;
 import no.ntnu.falldetection.utils.motea.event.GyroListener;
 import no.ntnu.falldetection.utils.motea.event.MoteDisconnectedEvent;
 import no.ntnu.falldetection.utils.motea.event.MoteDisconnectedListener;
 import no.ntnu.falldetection.utils.motea.event.StatusInformationListener;
 import android.bluetooth.BluetoothDevice;
 
 public class WiiMoteHandler implements AccelerometerListener<Mote>,
 		GyroListener, MoteDisconnectedListener<Mote>, 
 		StatusInformationListener, AlarmListener {
 	private Mote mote;
 	private GyroEvent newGyroEvent = null;
 	private AccelerometerEvent<Mote> newAccelEvent = null;
 	private ArrayList<SensorListener> listeners = new ArrayList<SensorListener>();
 	private boolean calibrated = false;
 	private int batteryLevel = -1;
 
 	public WiiMoteHandler(BluetoothDevice device) {
 		this.mote = new Mote(device);
 		mote.addAccelerometerListener(this);
 		mote.addMoteDisconnectedListener(this);
 		mote.addStatusInformationListener(this);
 		mote.addGyroListener(this);
 		
 		new Thread() {
 			public void run() {
 				while (true) {
 					try {
 						if(alarm){
 							mote.rumble(true);
 							Thread.sleep(100);
 							mote.rumble(false);
 						}else if(!alarm){
 							mote.rumble(false);
 						}
 						Thread.sleep(100l +(long)(DELAY*(1-severity)));
 					} catch (Exception e) {
 					}
 				}
 			}
 		}.start();	
 	}
 
 	public void addSensorListener(SensorListener listener) {
 		listeners.add(listener);
 	}
 
 	public void removeSensorListener(SensorListener listener) {
 		listeners.remove(listener);
 	}
 
 	@Override
 	public void gyroChanged(GyroEvent evt) {
 		fireSensorEvent(evt);
 	}
 
 	@Override
 	public void accelerometerChanged(AccelerometerEvent<Mote> evt) {
 		fireSensorEvent(evt);
 	}
 
 	@Override
 	public void moteDisconnected(MoteDisconnectedEvent<Mote> evt) {
 		// TODO notify activity
 	}
 
 	public void calibrateMotionPlus() {
 		calibrated = true;
 		mote.calibrateMotionPlus();
 	}
 
 	public void fireSensorEvent(Object evt) {
 		if (evt instanceof GyroEvent) {
 			newGyroEvent = (GyroEvent) evt;
 		} else if (evt instanceof AccelerometerEvent) {
 			newAccelEvent = (AccelerometerEvent<Mote>) evt;
 		}
 
 		if (newGyroEvent != null && newAccelEvent != null) {
 			SensorEvent orientationEvent = new SensorEvent(
 					newGyroEvent.getYaw(), newGyroEvent.getPitch(),
 					newGyroEvent.getRoll(), newAccelEvent.getX(),
 					newAccelEvent.getY(), newAccelEvent.getZ(), calibrated);
 			if (calibrated) {
 				calibrated = false;
 			}
 			for (SensorListener listener : listeners) {
 				listener.newSensorData(orientationEvent);
 			}
 
 			newAccelEvent = null;
 			newGyroEvent = null;
 		}
 	}
 
 	public void disconnect() {
 		if (mote != null) {
 			mote.disconnect();
 		}
 	}
 
 	public boolean isConnected() {
 		return mote != null;
 	}
 
 	@Override
 	public void statusInformationReceived(StatusInformationReport report) {
 		float battery = (float)(report.getBatteryLevel() & 0xff) / 0xc8;		
 		battery = battery/0.25f;
 		
 		int newBatteryLevel = Math.round(battery);
 		
 		if (batteryLevel != newBatteryLevel) {
 			batteryLevel = newBatteryLevel;
 			boolean[] leds = new boolean[4];
 			for (int i = 0; i < leds.length; i++) {
 				if (i < batteryLevel) {
 					leds[i] = true;
 				} else {
 					leds[i] = false;
 				}
 			}
 			mote.setLeds(leds);
 		}
 	}
 
 	private final long DELAY = 1000l;
 	private boolean alarm = false;
 	private float severity = 0;
 	@Override
 	public void alarmOn(AlarmEvent evt) {
 		alarm = true;
 		severity = evt.getSeverity();
 	}
 
 	@Override
 	public void alarmOff() {
 		alarm = false;
 	}
 }
