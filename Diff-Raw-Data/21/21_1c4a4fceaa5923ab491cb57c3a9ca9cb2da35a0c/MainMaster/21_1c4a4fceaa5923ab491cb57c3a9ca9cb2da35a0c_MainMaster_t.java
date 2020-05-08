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
  * Application running on the Master-NXT.
  * @author David Wille
  * @author Sven Schuster
  */
 public class MainMaster {
 	static int counter = 0;
 	static DataInputStream in;
 	static DataOutputStream out;
 
 
 	ColorSensor cs1 = null;
 	ColorSensor cs2 = null;
 	TouchSensor ts1 = null;
 
 	Tape tape = null;
 
 	public void run() {
 		Common.playTune("HAHA",200);
 		String sensor1, sensor2, sensor3, counterString;
 		this.cs1 = new ColorSensor(SensorPort.S1);
 		this.cs2 = new ColorSensor(SensorPort.S2);
 		this.ts1 = new TouchSensor(SensorPort.S4);
 		// initialize speeds
 		Motor.A.setSpeed(Common.LINE_SPEED);
 		Motor.B.setSpeed(Common.PUSH_SPEED);
 		Motor.C.setSpeed(Common.PUSH_SPEED);
 
 		// sensor listener for emergency stop
 		SensorPort.S4.addSensorPortListener(new SensorPortListener() {
 			public void stateChanged(SensorPort port, int oldValue, int newValue) {
 				if (oldValue > 500 && newValue < 500)
 					System.exit(0);
 			}
 		});
 
 		while (true) {
 			LCD.drawString("Moving to begin...", 0, 0);
 			tape = new Tape(Common.TAPE_SIZE);
 			tape.start();
 			LCD.clearDisplay();
 			LCD.drawString("Clearing tape...", 0, 0);
 			if (tape.clearTape()) {
 				break;
 			}
 			else {
 				LCD.clearDisplay();
 				LCD.drawString("ATTENTION", 0, 0);
 				LCD.drawString("Clearing tape failed.", 0, 1);
 				LCD.drawString("After the sound,", 0, 2);
 				LCD.drawString("clearing starts again!", 0, 3);
 
 				tape.stop();
 				
 				Common.playTune("CXCXCXCXCXCXCXCX", 400);
 				for (int i = 0; i < 3; i++) {
 					try {
 						Thread.sleep(1000);
 					}
 					catch (InterruptedException e) {}
 					Common.playTune("E", 600);
 				}
 				LCD.clearDisplay();
 			}
 		}
 
 
 		while (true) {
 			// setup connection
 			Common.playTune("CEG", 400);
 			LCD.clearDisplay();
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
 			LCD.clearDisplay();
 			LCD.drawString("Disconnecting...", 0, 0);
 			tape.stop();
 			connection.close();
 
 			LCD.drawString("Moving to begin...", 0, 0);
 			tape = new Tape(Common.TAPE_SIZE); // laenge uebergeben
 			tape.start();
 		}
 	}
 
 	private void serve() {
 		char ch = ' ';
 
 		while (true) {
 			try {
 				ch = in.readChar();
 			}
 			catch (IOException e) {
 				break;
 			}
 
 			LCD.clearDisplay();
 			switch (ch) {
 			case 'q':
 				return; //end serving
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
 						Common.playTune("C",200); //testing
 					}
 					else if (!cs1Active && cs2Active) {
 						out.writeChar('0');
 						Common.playTune("E",200); //testing
 					}
 					else if (cs1Active && !cs2Active) {
 						out.writeChar('1');
 						Common.playTune("G",200); //testing
 					}
 					else if (cs1Active && cs2Active) {
 						out.writeChar('2');
 						Common.playTune("H",200); //testing
 					}
 					out.flush();
 				}
 				catch (IOException e) {
 				}
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
 				Common.pushBits(bit1>nbit1,bit0>nbit0);
 					
 				try {
 					out.writeChar('.');
 					out.flush();
 				}
 				catch (IOException e) {
 				}
 				break;
 			case 'L':
 				if(tape.moveLeft()) {
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
 			case 'R': 
 				if(tape.moveRight()) {
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
 
 	/**
 	 * Establishes connection to the controlling PC and waits for commands.
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		MainMaster master = new MainMaster();
 		master.run();
 	}
 }
