 import java.util.*;
 
 /** * The purpose of this class is to practice with while loops and the Scanner class. Create the 
 following
  * three unrelated methods.
  * 
  */
 import java.util.Scanner;
 public class TestWhileLoops
 {
     public static void method1() {
         //create a new Scanner object that will read input from the keyboard
         Scanner myScanner = new Scanner(System.in);
         
         int input = myScanner.nextInt();
 		for (int i = 0; i < input; i++) {
 			System.out.println("out");
 		}
 	}    
     
 	public static void method2() {
         //create a new Scanner object that will read input from the keyboard
         Scanner myScanner = new Scanner(System.in);
         
         //Choose a random int between 1 and 15 and use a while loop as follows:
         //continue taking a guess from the user
         //until they either get it right or type ‐99 to give up.
         System.out.println("I have a number in mind, Dave. Can you guess?");
 		int number = (int)(Math.random);
 
        int guess = myScanner.nextInt();    
 
        while (guess != number) {
			method2();
 		}
     }    
    
          public static void method3()
     {
         //create a new Scanner object that will read input from the keyboard
         Scanner myScanner = new Scanner(System.in);
         
         //Create two arrays of 10 Strings: one called largeWords and
   //another one called shortWords.
         //Uise a while loop and the Scanner object to continue to read Strings from the user
         //as you read each String check its length and store it in largeWords if the length is 6 or 
         //more otherwise store the String in shortWords
         //keep track of where you are in each array and keep loop until BOTH arrays are filled
        //print arrays when done
         
 		ArrayList<String> short = new ArrayList<String>;
 		ArrayList<String> longer = new ArrayList<String>;
 
         System.out.println("Keep entering words until you are told to stop!");        
         while (x < 10) {
 			System.out.println("Enter a string: ");
 			str = myScanner.next();
 
 			if (str.length() > 6) {
 				longer.add(str);
 			} else {
 				short.add(str);
 			}
        }
             
             System.out.println("Thanks both lists are full.");
             System.out.println("Large List:");                       System.out.println("ShortList:");           
 }
