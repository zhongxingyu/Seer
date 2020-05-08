 package edu.ncsu.csc216.solitaire.ui;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.IllegalFormatException;
 import java.util.Scanner;
 import java.util.zip.DataFormatException;
 
 import edu.ncsu.csc216.solitaire.model.Deck;
 import edu.ncsu.csc216.solitaire.model.Message;
 
 
 /**
  * The userInterface
  * @author Andrew Kofink, William Blazer
  */
 public class UI {
 
 	/**
 	 * @param args The input parameters to the program
 	 */
 	public static void main(String[] args) {
 		UserInterface();
 	}
 	
 	/**
 	 * Interfaces with the user and accepts the input file
 	 */
 	public static void UserInterface() {
 		try {
 			Scanner console = new Scanner(System.in);
 			System.out.println("Please enter a filename to use:");
 			String filename = console.nextLine();
 			File f = new File(filename);
 			Scanner in = new Scanner(f);
 			
 			//Look for proper file format
 			String messageString = "";
 			while (in.hasNextLine()) {
 				messageString += in.nextLine();
 			}
 			
 			//Match this regular expression, or the file format is wrong
 			if (!messageString.matches("\\A(\\d+[ ]){27}\\d+\\z")) {
 				throw new DataFormatException();
 			}
			in = new Scanner(f);
 			//Gather file contents into an array of ints
 			int[] messageInts = new int[28];
 			for (int i = 0; i < messageInts.length; i++) {
 				messageInts[i] = in.nextInt();
 			}
 			Deck deck = new Deck(messageInts);
 		} catch (FileNotFoundException e) {
 			System.out.println("File not found.");
 			UserInterface();
 		} catch (DataFormatException dfe) {
 			System.out.println("Invalid file.");
 			UserInterface();
 		}
 	}
 
 }
