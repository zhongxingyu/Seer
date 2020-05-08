
 package ioProcessor;
 /**
  * The main process for the EE260 debugger
  * @author boka
  */
 public class MainProcessor {
 
 	/**
 	 * @param args to run the program
 	 */
 	public static void main(String[] args) {
 		// Process arguments
 		if (!args[0].isEmpty()) {
 			if (args[0].endsWith(".s")) {
 				System.out.println("Running with input file: " + args[0]);
				
				// TODO iomain:  Pass Ejay's code the file here through constructor
			} 
 			else if (args[0].equals("-h")) {
 				System.out.println("Help menu:");
 				System.out.println("");
 			}
		} 
 		else {
 			System.out.println("Use command -h for help.");
 		}
 	}
 }
