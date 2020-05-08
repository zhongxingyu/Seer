 package com.base.echode;
 
 import java.util.Scanner;
 
 public class Echode {
 	static Scanner scan;
 	private static String in;
 	
 	/**
 	* @param args
 	*/
 	public static void main(String[] args) {
 		scan = new Scanner(System.in);
 		intro();
 	}
 	
	//welcome message
 	public static void intro() {
		System.out.println("Welcome to ECHODE version 0.2.3");
 		mainLoop();
 	}
 	
 	private static void mainLoop() {
 		while (true) {
 			System.out.print("->");
 			in = scan.nextLine();
 			parse(in);
 		}
 	}
 	
 	private static void parse(String in2) {
		/**
		 * Commented this out, in case needed.
		 * 
		if (in2.equalsIgnoreCase("about")) {
 			System.out.println("Echode version 0.2.2\nMade by Erik Konijn and Marks Polakovs");
 		} else {
 			if (in2.equalsIgnoreCase("kill")){
 				System.out.println("Echode shut down succesfully.");
 				System.exit(0);
 			}
 		else{
 				System.out.println("Not implemented yet.");
 		}
 		}
 		**/
 		switch(in2) {
 			case "about":
				System.out.println("Echode version 0.2.3\nMade by Erik Konijn and Marks Polakovs");
 				break;
 			case "kill":
 				System.out.println("Echode shut down succesfully.");
 				System.exit(0);
 				break;
 			case "help":
 				System.out.println("List of commands:\n" + 
 						"about ----------------------------------- Gives some info about ECHODE\n" +
 						"help ---------------------------------------------- Lists all commands\n" +
 						"kill ---------------------------------------- Quits the ECHODE console\n");
 				break;
 			default:
 				System.out.println("Not implemented yet.");
 				break;
 		}
 	}
 
 }
