 import linkjvm.libkovan.AnalogSensor;
 import linkjvm.libkovan.Controller;
 import linkjvm.libkovan.Servo;
 
 /*
  * Example for servos.
  * Turns the servo to one side on low sensor
  * values and to the other side on high values.
  */
 
 public class ServoExample {
 
 	public static void main(String[] agrs){
 		AnalogSensor sensor = new AnalogSensor(0);
 		Servo servo = new Servo(0);
 		servo.enable();
 		while(true){
 			System.out.println("Place sensor!");
			Controller.msleep(1000);
 			int currentVal = sensor.getValue();
 			System.out.println(currentVal);
 			if(currentVal > 600){
 				servo.setPosition(2000);
				Controller.msleep(500);
 			}
 			else{
 				servo.setPosition(0);
				Controller.msleep(500);
 			}
 			servo.setPosition(1000);
			Controller.msleep(500);
 		}
 	}
 }
