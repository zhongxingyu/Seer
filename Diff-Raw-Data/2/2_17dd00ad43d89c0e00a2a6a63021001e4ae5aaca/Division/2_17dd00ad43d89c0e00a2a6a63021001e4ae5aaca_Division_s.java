 package no.ntnu;
 
 public class Division {
 	public static int divide(int numberA, int numberB) {
 		int counter = 0;
		while(numberA > numberB) {
 			counter++;
 			numberA = Subtraction.subtract(numberA, numberB);
 		}
 		return counter;
 	}
 }
