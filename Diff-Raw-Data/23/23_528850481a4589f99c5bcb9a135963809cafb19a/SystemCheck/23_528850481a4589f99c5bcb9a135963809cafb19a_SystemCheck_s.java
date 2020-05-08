 package org.saseros.cleanstorms;
 
 import lejos.nxt.*;
 
 public class SystemCheck {
 	
 	private SensorPort tPort;
 	private SensorPort lsPort;
 	private SensorPort ussPort;
 	private float lowLevel;
 	
 	/**
 	 * Sets the ports to use and low battery level threshold.
 	 * 
 	 * @param tPort
 	 *            Port for the Touch sensor.
 	 * @param lsPort
 	 *            Port for the Light sensor.
 	 * @param ussPort
 	 *            Port for the Ultra sonic sensor.
 	 * @param lowLevel
 	 *            Low battery level threshold for battery checking.
 	 */
 	public SystemCheck(SensorPort ussPort, SensorPort tPort,
 			SensorPort lsPort, float lowLevel) {
 		this.tPort = tPort;
 		this.lsPort = lsPort;
 		this.ussPort = ussPort;
 		this.lowLevel = lowLevel;
 	}
 	
 	/**
 	 * Checks if motor is available and can move.
 	 * 
 	 * @param	m
 	 * @return	Returns true if the motor moves and is connected, 
 	 * 			and false if otherwise.
 	 */
 	private static boolean motorCheck(NXTRegulatedMotor m) {
 		boolean ret = true;
 		int pos = m.getTachoCount();
 		int speed = m.getSpeed();
 		
 		m.setSpeed(20);
 		m.rotate(20);
 		
 		try {
 			Thread.sleep(2);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		if (m.getTachoCount() == pos) ret = false;
 		
 		//Reset values
 		m.setSpeed(speed);
 		m.rotateTo(pos);
 		return ret;
 	}
 	
 	/**
 	 * Check if Touch sensor is connected.
 	 * 
 	 * @param	t
 	 * @return	Returns true if the Touchsensor is pressed.
 	 */
 	private static boolean checkSensor(TouchSensor t) {
 		return t.isPressed();
 	}
 	
 	/**
 	 * Check if Light sensor is connected.
 	 * 
 	 * @param	ls
 	 * @return	Returns true if the sensor is connected
  	 *			because it only has the value 0 
  	 *			if it is not connected.
 	 */
 	private static boolean checkSensor(LightSensor ls) {
 		return (ls.readNormalizedValue() != 0);
 	}
 	
 	/**
 	 * Check if Ultra Sonic sensor is connected.
 	 * 	
 	 * @param	us
 	 * @return	Returns true if the sensor is connected
  	 *			-5 means disconnected.
 	 */
 	public static boolean checkSensor(I2CSensor uss) {
 		return (uss.getData(0, null, 0) != -5);
 	}
 	
 	/**
 	 * Check if the battery voltage level is
 	 * above the low voltage value.
 	 * 
 	 * @param	lowLevel
 	 * @return	Returns true if the batterylevel 
 	 * 			is not below the given low value;
 	 */
 	public static boolean checkBatteryLevel(float lowLevel) {
 		return (Battery.getVoltage() > lowLevel);
 	}
 	
 	/**
 	 * Checks if all of the motors are connected 
 	 */
 	private static void enginesCheck() {
		if(!motorCheck(Motor.A))
			Alarm.createAlarmSoft("Motor A not working!");
		if(!motorCheck(Motor.B))
			Alarm.createAlarmSoft("Motor B not working!");
		if(!motorCheck(Motor.C))
			Alarm.createAlarmSoft("Motor C not working!");
 	}
 	
 	/**
 	 * Check if all sensors are connected
 	 */
 	private void sensorCheck() {
 		boolean ret = true;
 		while(ret) {
 			ret = false;
 			//defining and initializing all of the sensorinfo's
 			I2CSensor uss = new I2CSensor(ussPort);
 			TouchSensor t = new TouchSensor(tPort);
 			LightSensor ls = new LightSensor(lsPort);
 			
 			if(!checkSensor(uss)) {
 				Alarm.createAlarmSoft("UltraSonic sensor not Connected");
 				ret = true;
 			}
 			//give an message to the user, to preform some sensor tests
 			Alarm.showMessage("Please push the Touchsensor and hold" +
 					" hold that while you pressed the Orange button.");
 			
 			if(!checkSensor(t)) {
 				Alarm.createAlarmSoft("Touch Sensor not connected!");
 				ret = true;
 			}
 			if(!checkSensor(ls)) {
 				Alarm.createAlarmSoft("Light Sensor not connected!");
 				ret = true;
 			}
 			Alarm.showMessage("You can now release the Touch sensor" +
 					" and pres the orange button again.");
 		}
 	}
 	
 	/**
 	 * Preform all of the tests, first do a engine
 	 * check next the sensor check and check the
 	 * battery after that.
 	 */
 	public void preform() {
 		enginesCheck();
 		sensorCheck();
 		if(!checkBatteryLevel(lowLevel))
 			Alarm.createAlarmSoft("Batterylevel to low");
 	}
 }
