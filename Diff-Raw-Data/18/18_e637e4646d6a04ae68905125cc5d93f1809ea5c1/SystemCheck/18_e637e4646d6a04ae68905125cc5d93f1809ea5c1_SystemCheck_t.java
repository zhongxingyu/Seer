 package org.saseros.cleanstorms.test;
 
 import lejos.nxt.*;
 
 public class SystemCheck {
 	
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
 	 * Check if Touch sensor is connected
 	 * 
 	 * @param	t
 	 * @return	Returns true if the Touchsensor is pressed.
 	 */
 	private static boolean checkSensor(TouchSensor t) {
 		return t.isPressed();
 	}
 	
 	/**
 	 * Check if Light sensor is connected
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
 	 * Check if Ultra Sonic sensor is connected
 	 * 	
 	 * @param	us
 	 * @return	Returns true if the sensor is connected
  	 *			-5 means disconnected.
 	 */
 	private static boolean checkSensor(I2CSensor us) {
 		return (us.getData(0, null, 0) != -5);
 	}	
 	
 	/**
 	 * Checks if all of the motors are connected 
 	 */
 	public static void enginesCheck() {
 		if(!motorCheck(Motor.A))
 			Alarm.createAlarmHard("Motor A not working!");
 		if(!motorCheck(Motor.B))
 			Alarm.createAlarmHard("Motor B not working!");
 		if(!motorCheck(Motor.C))
 			Alarm.createAlarmHard("Motor C not working!");
 	}
 	
 	/**
 	 * Check if all sensors are connected
 	 */
 	public static void sensorCheck() {
 		//defining and initializing all of the sensorinfo's
 		I2CSensor us = new I2CSensor(SensorPort.S1);
 		TouchSensor t = new TouchSensor(SensorPort.S2);
 		LightSensor ls = new LightSensor(SensorPort.S3);
 		
		if(!checkSensor(us)) 
 			Alarm.createAlarmHard("UltraSonic sensor not Connected");
 		
 		//give an message to the user, to preform some sensor tests
		Alarm.showMessage("Please push the Touchsensor and hold" +
				" hold that while you pressed the Orange button.");
 		
		if(!checkSensor(t))
 			Alarm.createAlarmHard("Touch Sensor not connected!");
		if(!checkSensor(ls))
 			Alarm.createAlarmHard("Light Sensor not connected!");
 		
		Alarm.showMessage("You can now release the Touch sensor" +
				" and pres the orange button again.");
 	}
 	
 	/**
 	 * <b>Intented for testing purposes only</b>
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		enginesCheck();
 		sensorCheck();
 	}
 }
