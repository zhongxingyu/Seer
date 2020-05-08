 /**
  * Author: Andy Pacher
 * Date: May 4th, 2011
  * Assignment: AddCoins.java
  */
 
 import java.util.Scanner;
 import java.text.NumberFormat;
 
 public class AddCoins {
 	public static void main(String[] args) {
 		int numQuarter, numDime, numNickel, numPenny;
 		System.out.println("Enter your total coins");
 		numQuarter =AddCoins.getInput("Quarters: ");
 		numDime =	AddCoins.getInput("Dimes: ");
 		numNickel =	AddCoins.getInput("Nickels: ");
 		numPenny =	AddCoins.getInput("Pennies: ");
 		
 		System.out.print("T3h total is: " +getDollarAmount(numQuarter, numDime, numNickel, numPenny));
 	}
 
 	public static int getInput(String prompt) {
 		int userNum;
 		Scanner jeff = new Scanner(System.in);
 		System.out.print(prompt);
 		do {
 			
 			while (! jeff.hasNextInt()) {
 				jeff.next();
 				System.out.println("I SAID NUMBER");
 			}
 			userNum = jeff.nextInt();
 			if (userNum < 0)
 				System.out.println("\t want to tell me how you have a negative number of coins? try again: ");
 		}while(userNum < 0);
 
 		return userNum;
 	}
 
 	public static String  getDollarAmount(int quarter, int dime, int nickel, int penny){
 		double dollars;
 		String answer;
 		NumberFormat nf= NumberFormat.getCurrencyInstance();
 		
 		dollars = (.25 * quarter) + (.10 * dime) + (.05 * nickel) + (.01 * penny);
 		answer = nf.format(dollars);
 		
 		return answer;
 	}
 }
