 package T07;
 
 import lejos.nxt.Button;
 import lejos.nxt.Motor;
 import lejos.nxt.comm.RConsole;
 
 public class DPM {
 	
 	public static void main(String[] args){
 		// This code now is to test the RConsole
 		RConsole.openBluetooth(20000);
 		RConsole.println("Connected!");
 		printNums();
 		
		while (Button.readButtons() != Button.ID_ESCAPE);
		
		// BILAL - Odometer test
		/*TwoWheeledRobot patBot = new TwoWheeledRobot(Motor.A, Motor.B, Motor.C);
		Odometer odo = new Odometer(patBot, true);
		LCDInfo lcd = new LCDInfo(odo);
		Navigation nav = new Navigation(odo);
		
		Motor.A.flt();
		Motor.B.flt();
		
		Button.waitForAnyPress();
		
		nav.turnTo(-45);
		
		Button.waitForAnyPress();*/
		
 	}
 	
 	private static void printNums() {
 		int i;
 		for (i = 0; i < 10; i++) {
 			RConsole.println("The value of i is " + String.valueOf(i));
 		}
 	}
 }
