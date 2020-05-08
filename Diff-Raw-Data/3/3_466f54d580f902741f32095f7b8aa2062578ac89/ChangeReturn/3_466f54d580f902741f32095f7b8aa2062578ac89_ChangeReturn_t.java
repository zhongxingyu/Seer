 //Written by David Hausner
 //9-10-2013
 
 import java.util.*;
 public class ChangeReturn {
 	public static void main(String[] arg) {
 		new ChangeReturn();
 	}
 	public ChangeReturn() {
 		//introduces the program, asks user for input
 		Scanner userScan = new Scanner(System.in);
 		System.out.println("Welcome to Change Return program!");
 		System.out.print("Enter change: ");
 		String input = userScan.next();
		userScan.close();
 		
 		//if the user includes a dollar sign, gets rid of it
 		//rather than treating it like invalid data in next step
 		input = input.replace("$", "");
 		
 		//error handling in case user input invalid data
 		double dChange;
 		try{
 			//tries to parse the string into a double
 			dChange = Double.parseDouble(input);
 		}
 		catch(NumberFormatException nfe){
 			//notifies the user that they input their data incorrectly
 			//and replaces their data with a valid number
 			System.out.println("You entered the change incorrectly.");
 			dChange = 0.00;
 		}
 		
 		//converts the double to an integer to ensure
 		//two decimal place formatting
 		int iChange = (int)(dChange * 100);
 		
 		//breaks down the integer into correct denomination
 		int num_dollars = iChange / 100;
 		iChange %=100;
 		int num_quarters = (iChange%100) / 25;
 		iChange -= 25 * num_quarters;
 		int num_dimes = (iChange%25) / 10;
 		iChange -= 10 * num_dimes;
 		int num_nickels = (iChange%10) / 5;
 		iChange -= 5 * num_nickels;
 		int num_pennies = iChange;
 		
 		//sets up output so that the singular form of the noun will be used
 		//if there is only 1 in any denomination, else use the plural form
 		String str_dollars, str_quarters, str_dimes, str_nickels, str_pennies;
 		if(num_dollars == 1)
 		{
 			str_dollars = " dollar, ";
 		}
 		else str_dollars = " dollars, ";
 		
 		if(num_quarters == 1)
 		{
 			str_quarters = " quarter, ";
 		}
 		else str_quarters = " quarters, ";
 		
 		if(num_dimes == 1)
 		{
 			str_dimes = " dime, ";
 		}
 		else str_dimes = " dimes, ";
 		
 		if(num_nickels == 1)
 		{
 			str_nickels = " nickel, and ";
 		}
 		else str_nickels = " nickels, and ";
 		
 		if(num_pennies == 1)
 		{
 			str_pennies = " penny.";
 		}
 		else str_pennies = " pennies.";
 		
 		//formats the output and outputs the data
 		System.out.format("Change is %d%s%d%s%d%s%d%s%d%s", num_dollars, str_dollars, num_quarters, 
 				str_quarters, num_dimes, str_dimes,	num_nickels, str_nickels, num_pennies, str_pennies); 
 	}
 }
