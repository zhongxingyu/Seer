 package app.run;
 
 import app.boxmate.*;
 
 /**
  * Starts the application.
  *
  * @author Zachary Seguin
  * @version 1.0.0 (31/05/2012)
  * @since 1.0.0
  */
public class MainProgram
 {
 	/**
 	 * Prints the provided character, the provided number of times.
 	 *
 	 * @param character The character to print
 	 * @param count The number of times to print the character.
 	 *
 	 * @since 1.0.0
  	 */
 	public static void printChar(char character, int count)
 	{
 		for (int x = 0; x < count; x++)
 			System.out.print(character);
 
 		System.out.println();
 	}//End of printChar method
 
 	/**
 	 * The first method that is called in the application (by Java).
 	 *
 	 * @param args The command line arguments.
 	 *
 	 * @since 1.0.0
  	 */
 	public static void main (String [] args) throws Exception
 	{
 		printChar('*', 50);
 		System.out.println("\t Welcome to BoxMate");
 		printChar('*', 50);
 
 		Database db = new Database();
 
 		Show [] shows = db.loadShows();
 
 		System.out.println("Shows");
 		for (Show show : shows)
 			System.out.println(" + " + show.getName());
 	}//End of main method
 }//End of class
