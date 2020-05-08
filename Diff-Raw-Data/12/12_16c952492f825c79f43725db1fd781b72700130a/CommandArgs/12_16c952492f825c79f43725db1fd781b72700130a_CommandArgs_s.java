 /**
  * Read and print data entered via command
  * line.
  * Class Invariants:
  * TODO invariants list
  * 
  * @author Matt Scholes <mscholes3@gmail.com>
  * @version May 3, 2013
  *
  */
 package lab2;
 
 import java.util.Scanner;
 
 public class CommandArgs {
 
 	/**
 	 * Read and print name and age entered via
 	 * command line.
 	 * Precondition: None.
 	 * Postcondition: None.
 	 * @param 
 	 * @return 
 	 */
 	public static void main(String[] args) {
 		
 
 // Can't seem to get this to work because I need args to be readable for Scanner later. But if
 // I establish it as a string in the main setup above, I can't convert it to readable below...
 // I prolly don't understand the 'Readable' function, but I need to move on so I'll just skip
 // checking if there are 0 arguments provided.
//		
//		if (args.length == 0) {
//			System.out.println("Proper usage is: java lab2.CommandArgs firstname lastname age");
//			System.exit(0);
//		}
 		
 // Doesn't work...
 //		Readable rargs = args;		
 		
 		// Concatenates the array 'args' into a space separated string sargs
 		String sargs = "";
 		for (String arg : args) {
 			sargs = sargs + ' ' + arg;
 		}
 		
         Scanner input = new Scanner(sargs);
 
 		String nameFirst = input.next();
 		String nameLast  = input.next();
 		
 		while (input.hasNextInt()
         		|| input.hasNextDouble()
         		|| input.hasNextFloat()) {
 			System.out.print("!ERROR!\n  Enter age in word form!");
 			System.exit(0);
 		}
 		String age = input.nextLine();
 		
 		System.out.println("You Entered:\n" + nameLast + ", " + nameFirst + ", " + age);
 		
 		input.close();
 		
 	}
 
 }
