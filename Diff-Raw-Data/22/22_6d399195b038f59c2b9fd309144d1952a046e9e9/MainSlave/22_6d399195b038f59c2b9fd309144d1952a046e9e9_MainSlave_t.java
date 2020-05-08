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
 
 /**
  * Application running on the Slave-NXT.
  * @author David Wille
  * @author Sven Schuster
  */
 public class MainSlave {
 	static int counter = 0;
 	static ColorSensor counterSensor;
 	static DataInputStream in;
 	static DataOutputStream out;
 
 	private static void pushBit(int bit, int newBit, Motor motor) {
 		if (bit<newBit){
 			motor.rotate(Common.PUSH_ANGLE_SLAVE);
 			motor.rotate(Common.PUSH_ANGLE_SLAVE*(-1)+1);
 		}
 	}
 	
	public void run() {
 		Common.playTune("HAHA",200);
 		TouchSensor ts1 = new TouchSensor(SensorPort.S4);
 		// initialize speeds
 		Motor.B.setSpeed(Common.PUSH_SPEED);
 		Motor.C.setSpeed(Common.PUSH_SPEED);
 
 		// sensor listener for emergency stop
 		SensorPort.S4.addSensorPortListener(new SensorPortListener() {
 			public void stateChanged(SensorPort port, int oldValue, int newValue) {
 				if (oldValue > 500 && newValue < 500) {
 					Common.playTune("HAHA",200);
 					System.exit(0);
 				}
 			}
 		});
 
 		while (true) {
 			// setup connection
 			LCD.drawString("Waiting...", 0, 0);
 			NXTConnection connection = Bluetooth.waitForConnection();           
 			LCD.clearDisplay();
 			LCD.drawString("Connecting...", 0, 0);
 			in = connection.openDataInputStream();
 			out = connection.openDataOutputStream();           
 			LCD.clearDisplay();
 			LCD.drawString("Connected", 0, 0);
 			
 			//listen to commands
 			this.serve(); 
 			
 			//close connection
 			LCD.drawString("Disconnecting...", 0, 0);
 			connection.close();
 		}
 	}
 	
 	private void serve() {
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
					return;
 				case 't':
 					LCD.drawString("Pushing...", 0, 0);
 					Motor.B.rotate(Common.PUSH_ANGLE_SLAVE);
 					Motor.B.rotate(Common.PUSH_ANGLE_SLAVE*(-1)+1);
 					Motor.C.rotate(Common.PUSH_ANGLE_SLAVE);
 					Motor.C.rotate(Common.PUSH_ANGLE_SLAVE*(-1)+1);
 					LCD.clearDisplay();
 					break;
 				case 'w':
 					int bit1, bit0, nbit1, nbit0;
 					char newSymbol = 'n', currentSymbol = 'n';
 					try{
 						currentSymbol = in.readChar();
 						newSymbol = in.readChar();
 					}
 					catch(IOException e){}
 					switch(currentSymbol){
 						case '#': bit1 = 0; bit0 = 0; break;
 						case '0': bit1 = 0; bit0 = 1; break;
 						case '1': bit1 = 1; bit0 = 0; break;
 						case '2': bit1 = 1; bit0 = 1; break;
 						default: bit1 = -1; bit0 = -1; break;
 					}
 					switch(newSymbol){
 						case '#': nbit1 = 0; nbit0 = 0; break;
 						case '0': nbit1 = 0; nbit0 = 1; break;
 						case '1': nbit1 = 1; nbit0 = 0; break;
 						case '2': nbit1 = 1; nbit0 = 1; break;
 						default: nbit1 = -1; nbit0 = -1; break;
 					}
 					pushBit(bit1, nbit1, Motor.B);
 					pushBit(bit0, nbit0, Motor.C);
 					try {
 						out.writeChar('.');
 						out.flush();
 					}
 					catch (IOException e) {
 					}
 					break;
 			}
 		}
 	}
	
	
	/**
	 * Establishes connection to the controlling PC and waits for commands.
	 * @param args
	 */
	public static void main(String[] args) {
		MainMaster master = new MainMaster();
		master.run();
	}
 }
