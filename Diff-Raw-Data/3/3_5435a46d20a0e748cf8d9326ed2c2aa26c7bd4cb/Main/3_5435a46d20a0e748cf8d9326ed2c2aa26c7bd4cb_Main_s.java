 package tk.rebornlegendmc.primenumbers;
 
 import java.util.Scanner;
 
 /**
  * Prime number calculator
  * 
  * Resources used:
  * 'for' loops: http://docs.oracle.com/javase/tutorial/java/nutsandbolts/for.html
  * Methods: http://docs.oracle.com/javase/tutorial/java/javaOO/methods.html
  * Tried to learn about lists but failed: http://docs.oracle.com/javase/6/docs/api/java/util/List.html
  * 
  * @author Ludburghmdm
  */
 public class Main {
 	
     public static void main(String[] args) {
 	try {
 	    Scanner input = new Scanner(System.in); // Make a scanner
 	    while(true) {
 	    	System.out.print("Input a Number: "); // Ask for how much it should check
	    	int upTo = 0; //It has to be defined outside of the try block to be visible outside it.
 	    	try {
 	    		upTo = input.nextInt(); // Set the "upTo" variable
 	    		break;//Success, it didn't fail, break the loop
 	    	} catch(Exception e) {//In case it wasn't a number...
 	    		System.out.print("You didn't input a full number(integer)"); //Tell the user it's not a integer
 	    	}
 	    }
 	    input.close();
 	    int current = 2; // Current
 	    for (; current <= upTo; current++) { // the loop (had to consult the java docs for this)
 		if (isPrime(current)) { // if it's true, print (I might've used some array or list thing, but it seemed hard for me)
 		    System.out.println(current); // print
 		}
 	    }
 	} catch (Exception e) { // Catching errors if you entered a string instead of an integer
 	    System.out.print("Error: "); // print error
 	    e.printStackTrace(); // print stack trace (Eclipses' auto-correct function helped me discover this xD)
 	}
     }
     /**
      * Check if an integer is prime
      * @param n The integer to check
      * @returns true if the integer is a prime number, false if not.
      **/
     public static boolean isPrime(int n) { // method (again, had to consult java docs)
 	for (int divideBy = 2; divideBy < n; divideBy++) {
 	    if (n % divideBy == 0) { // if equal to zero, return false
 		return false;
 	    }
 	}
 	return true; // if not, return true
     }
 }
