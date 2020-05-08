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
 	}
 	
 	private static void printNums() {
 		int i;
 		for (i = 0; i < 10; i++) {
 			RConsole.println("The value of i is " + String.valueOf(i));
 		}
 	}
 }
