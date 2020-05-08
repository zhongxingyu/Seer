 package io.github.linkjvm.linefollow;
 
 
 /*
  * Very simple line following program.
  */
 
 import linkjvm.libkovan.AnalogSensor;
 import linkjvm.libkovan.Motor;
 import linkjvm.libkovan.Sensor;
 
 public class LineFollow {
 
 	public static final int RIGHT_MOTOR_PORT = 0;
 	public static final int LEFT_MOTOR_PORT = 1;
 	public static final int RIGHT_SENSOR_PORT = 0;
 	public static final int LEFT_SENSOR_PORT = 1;
 
 	public static void main(String[] args){
 		Motor leftMotor = new Motor(0);
 		Motor rightMotor = new Motor(1);
 		Sensor leftSensor = new AnalogSensor(0);
 		Sensor rightSensor = new AnalogSensor(1);
 		while(true){
 			if(leftSensor.getValue() < 600){
				leftMotor.moveAtVelocity(100);
				rightMotor.moveAtVelocity(0);
 			}
 			else if(rightSensor.getValue() < 600){
				leftMotor.moveAtVelocity(0);
				rightMotor.moveAtVelocity(100);
 			}
 			else{
				leftMotor.moveAtVelocity(100);
				rightMotor.moveAtVelocity(100);
 			}
 		}
 	}
 }
