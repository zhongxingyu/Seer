 import lejos.nxt.*;
 import lejos.nxt.addon.ColorSensor;
 import lejos.nxt.remote.*;
 import lejos.nxt.comm.*;
 import javax.bluetooth.*;;
 import java.io.*;
 
 /*
  * black = 0
  * brown = 0
  * blue = 2
  * dark green = 4
  * light green = 6
  * yellow = 7
  * red = 9
  * orange = 9
  * grey = 14
  * white = 14
  */
 
 public class MainMaster {
 	static int counter = 0;
 	static ColorSensor counterSensor;
 	static DataInputStream in;
 	static DataOutputStream out;
 
 	public static void main(String[] args) {
 		Common.playTune("HAHA",200);
 		String sensor1, sensor2, sensor3, counterString;
 		ColorSensor cs1 = new ColorSensor(SensorPort.S1);
 		ColorSensor cs2 = new ColorSensor(SensorPort.S2);
 		TouchSensor ts1 = new TouchSensor(SensorPort.S4);
 		// initialize speeds
 		Motor.A.setSpeed(Common.LINE_SPEED);
 		Motor.B.setSpeed(Common.PUSH_SPEED);
 		Motor.C.setSpeed(Common.PUSH_SPEED);
 		
 		Line line = new Line();
 		line.start();
 		//initialize counter
 		counterSensor = line.getCounterSensor();
 		
 		// sensor listener for emergency stop
 		SensorPort.S4.addSensorPortListener(new SensorPortListener() {
 			public void stateChanged(SensorPort port, int oldValue, int newValue) {
 				if (oldValue > 500 && newValue < 500) {
 					Common.playTune("HAHA",200);
 					System.exit(0);
 				}
 			}
 		});
 		
 		// setup connection
 		LCD.drawString("Waiting...", 0, 0);
 		NXTConnection connection = Bluetooth.waitForConnection();           
 		LCD.clearDisplay();
 		LCD.drawString("Connecting...", 0, 0);
 		in = connection.openDataInputStream();
 		out = connection.openDataOutputStream();           
 		LCD.clearDisplay();
 		LCD.drawString("Connected", 0, 0);
 		
 		char ch = ' ';
 
 		while (true) {
 			try {
 				ch = in.readChar();
 			}
 			catch (IOException e) {
 			}
 	           
 			LCD.clearDisplay();
 			switch (ch) {
 				case 'q':
 					connection.close();
 					System.exit(0);
 					break;
 				case 't':
 					LCD.drawString("Pushing...", 0, 0);
 					Motor.B.rotate(Common.PUSH_ANGLE_MASTER);
 					Motor.B.rotate(Common.PUSH_ANGLE_MASTER*(-1)+1);
 					Motor.C.rotate(Common.PUSH_ANGLE_MASTER);
 					Motor.C.rotate(Common.PUSH_ANGLE_MASTER*(-1)+1);
 					LCD.clearDisplay();
 					break;
 				case 'r':
 					boolean cs1Active, cs2Active;
 					if (cs1.getColorNumber() >= 5 && cs1.getColorNumber() <= 10) {
 						LCD.drawString("brick", 0, 0);
 						cs1Active = true;
 					}
 					else {
 						LCD.drawString("no brick", 0, 0);
 						cs1Active = false;
 						
 					}
 					if (cs2.getColorNumber() >= 5 && cs2.getColorNumber() <= 10) {
 						LCD.drawString("brick", 0, 1);
 						cs2Active = true;
 					}
 					else {
 						LCD.drawString("no brick", 0, 1);
 						cs2Active = false;
 					}
 					try {
 						if (!cs1Active && !cs2Active) {
 							out.writeChar('#');
 						}
 						else if (!cs1Active && cs2Active) {
 							out.writeChar('0');
 						}
 						else if (cs1Active && !cs2Active) {
 							out.writeChar('1');
 						}
 						else if (cs1Active && cs2Active) {
 							out.writeChar('2');
 						}
 						out.flush();
 					}
 					catch (IOException e) {
 					}
 					break;
				case 'l': 
 					if(line.driveLeft()) {
 						try {
 							out.writeChar('.');
 							out.flush();
 						}
 						catch (IOException e) {
 						}
 					}
 					else {
 						try {
 							out.writeChar('!');
 							out.flush();
 						}
 						catch (IOException e) {
 						}						
 					}
 					break;
				case 'r': 
 					if(line.driveRight()) {
 						try {
 							out.writeChar('.');
 							out.flush();
 						}
 						catch (IOException e) {
 						}
 					}
 					else {
 						try {
 							out.writeChar('!');
 							out.flush();
 						}
 						catch (IOException e) {
 						}
 					}
 					break;
 			}
 		}
 	}
 }
