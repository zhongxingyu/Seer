 /* Xiao G. Wu
 * CS111A - Inclass exercise on user input
  * 09/26/2010
  */
 
 import java.util.Scanner;
 
 public class EchoShout
 {
     /** Main method 
      * @params args Optional command line argument to specify string to check for during loop
      */
 
     public static void main(String[] args)
     {
         Scanner keyboard = new Scanner(System.in); // Reading from stdin
         boolean keepGoing = true; // Flag used to determine when to break out of do-while loop
         boolean checkArg = false; // Flag used to determine if to assign command line argument to string to check against user input
         String checkString = ""; // Command line argument user input string
         
         if (args.length > 0) // If command line argument is specified 
         {
             checkArg = true;
             checkString = args[0];
         }
         
         do  // Using a do-while because we always want to ask the user to provide some input
         {
             System.out.print("Enter some input or leave blank to exit: ");
             String userInput = keyboard.nextLine();
 
             if (userInput.length() > 0) // If user input is provided 
             {
                 if (checkArg) // If checkArg flag is true
                 {
                     if (userInput.equals(checkString)) //Check to see if user input matches the command line argument
                         System.out.println("Input matches original argument!");
                 }
                 System.out.println(userInput.toUpperCase()); // Echo user input in all caps
             }
             else // No user input provided
             {
                 keepGoing = false; // Break out of loop
                 System.out.println("Done.");
             }
         } while (keepGoing);
     }
 }
