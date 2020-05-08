 package onRobot;
 
 import java.io.IOException;
 
 //Although these are fixed constants we don't use an enum because we need to send integer values via bluetooth.
 public interface CMD {
 	/**
 	 * Go forward for a certain distance.
 	 */
 	public static final int TRAVEL=0;
 	public void travel(int distance);
 	/**
 	 * Turn the whole robot a certain angle
 	 */
 	public static final int TURN=1;
 	public void turn(int angle);
 
 	/**
 	 * Turn the ultrasonic sensor a certain angle
 	 * So if the sensor is at 90  degrees and the second argument of the bluetooth-command is 90, the sensor will turn to 180 degrees.
 	 */
 	public static final int TURNSENSOR=2;
 	public void turnSensor(int angle);
 	
 	/**
 	 * Turn the ultrasonic sensor to a certain angle
 	 * So if the sensor is at 90  degrees and the second argument of the bluetooth-command is 90, the sensor will not turn at all.
 	 */
 	public static final int TURNSENSORTO=3;
 	public void turnSensorTo(int angle);
 
 	
 	/**
 	 * Stop immediately and return pose
 	 */
 	public static final int STOP=4;
 	public int[] stop();
 	/**
 	 * just give a status-update with position and rotation and whether the robot is moving or not.
 	 */
 	public static final int GETPOSE=5;
 	public int[] getPose();
 	/**
 	 * just give a status-update with sensor values: light-value, UltrasonicValue, touchValue, and the angle of the ultrasonic sensor.
 	 */
 	public static final int GETSENSORVALUES=6;
 	public int[] getSensorValues();
 	/**
 	 * just give a the battery level.
 	 */
 	public static final int BATTERY=7;
 	public int getBattery();
 	
 	/**
 	 * calibrate the lightsensor for the highest value
 	 */
 	public static final int CALIBRATELSHIGH=8;
 	public void calibrateLightSensorHigh();
 	
 	/**
 	 * calibrate the lightsensor for the lowest value
 	 */
 	public static final int CALIBRATELSLOW=9;
 	public void calibrateLightSensorLow();
 
 	/**
 	 * Make the robot start turning to the left(1) or right(-1).
 	 */
 	public static final int KEEPTURNING=10;
 	public void keepTurning(boolean left);
 	/**
 	 * keep going forward(1) or backward(-1)
 	 */
 	public static final int KEEPTRAVELLING=11;
 	public void keepTraveling(boolean forward);
 	/**
 	 * ends the bluetooth connection TODO
 	 */
 	public static final int DISCONNECT = 12;
 	public void disconnect();
 	
 	public static final int SCANBARCODE = 13;
	public void scanBarcode() throws IOException;
 	
 	public static final int DRIVESLOW = 14;
 	public void driveSlow();
 	
 	public static final int DRIVEFAST = 15;
 	public void driveFast();
 	
 	public static final int PLAYTUNE = 16;
 	public void playTune();
 	
 	public static final int WAIT5 = 17;
 	public void wait5();
 	
 	public static final int STRAIGHTEN = 18;
 	public void straighten();
 	
 	public static final int SETTRAVELSPEED = 19;
 	public void setTravelSpeed(int speed);
 	
 	public static final int SETTURNSPEED = 20;
 	public void setTurnSpeed(int speed);
 
 }
