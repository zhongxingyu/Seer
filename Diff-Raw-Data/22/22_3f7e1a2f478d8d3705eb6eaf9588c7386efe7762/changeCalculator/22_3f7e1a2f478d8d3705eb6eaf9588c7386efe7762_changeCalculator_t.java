//import java.math.*;
 import java.lang.Math;
 import javax.swing.*;
 import java.text.DecimalFormat;
 
 public class changeCalculator {
 //class-wide variables
 static String change;
 static double decimalChange;
 static int quarters = 0;
 static double doubleQuarters = 0;
 static double finalDoubleQuarters = 0;
 static int dimes = 0;
 static double doubleDimes = 0;
 static double finalDoubleDimes = 0;
 static int nickels = 0;
 static double doubleNickels = 0;
 static double finalDoubleNickels = 0;
 static int pennies = 0;
 static double doublePennies = 0;
 static double finalDoublePennies = 0;
 static double Convereter;
 static double firstDecimalChange=0;
 
 //Creating a function rounding the Decimal into String format
 public static String formatDecimal(double number)
 {
 	double epsilon = 0.004;
 	if(Math.abs(Math.round(number)-number)<epsilon)
 	{
 		return String.format("%10.0f", number);	
 	}else
 	{
 		return String.format("%10.2f", number);
 	}
 }
 
 public static void main(String args[]){
 //inputting the user input into a string, checking for empty string
 change = JOptionPane.showInputDialog("How much money do you have?");

//checks to see if the input is a number.
boolean check = true;
while(check){
	try
	{
	Double.parseDouble(change);
	check = false;
	}
	catch(NumberFormatException nfe){
		change = JOptionPane.showInputDialog(null,"Please enter a change amount: ");
	}	
}


/*if (change == null)
 		System.exit(0);
 	if (change == "")
 		{
 		JOptionPane.showMessageDialog(null,"Please enter a change amount.");
 		System.exit(0);
 		}
	*/
 //formatting the inputed string into a double (0.00 formatting)
 firstDecimalChange = Double.parseDouble(change);
 DecimalFormat secondDecimalChange = new DecimalFormat("0.00");
 decimalChange = new Double(secondDecimalChange.format(firstDecimalChange)).doubleValue();
 if(decimalChange <= 0.00)
 	{
 	JOptionPane.showMessageDialog(null,"Can't give change for less than $0.00");	
 	System.exit(0);	
 	}
 //If even amount of coins, divide evenly.
 if(decimalChange % .25 == 0){
 	doubleQuarters = (decimalChange/0.25);
 	quarters = (int)(doubleQuarters);
 	JOptionPane.showMessageDialog(null, "You have:\n " +
 			"Quarters: "+formatDecimal(quarters)+"\n " +
 			"Dimes: "+formatDecimal(dimes)+"\n " +
 			"Nickels: "+formatDecimal(nickels)+"\n" +
 			"Pennies: "+formatDecimal(pennies)+"\n");
 	System.exit(0);
 	}
 else if(decimalChange % .10 == 0){
 	doubleDimes = (decimalChange/0.10);
 	dimes = (int)(doubleDimes);
 	JOptionPane.showMessageDialog(null, "You have:\n " +
 			"Quarters: "+formatDecimal(quarters)+"\n " +
 			"Dimes: "+formatDecimal(dimes)+"\n " +
 			"Nickels: "+formatDecimal(nickels)+"\n" +
 			"Pennies: "+formatDecimal(pennies)+"\n");
 	System.exit(0);
 	}
 else if(decimalChange % .05 == 0){
 	doubleNickels = (decimalChange/.05);
 	nickels = (int)(doubleNickels);
 	JOptionPane.showMessageDialog(null, "You have:\n " +
 			"Quarters: "+formatDecimal(quarters)+"\n " +
 			"Dimes: "+formatDecimal(dimes)+"\n " +
 			"Nickels: "+formatDecimal(nickels)+"\n" +
 			"Pennies: "+formatDecimal(pennies)+"\n");
 	System.exit(0);
 	}
 else if(decimalChange % .01 == 0){
 	doublePennies = (decimalChange/.01);
 	pennies = (int)(doublePennies);
 	JOptionPane.showMessageDialog(null, "You have:\n " +
 			"Quarters: "+formatDecimal(quarters)+"\n " +
 			"Dimes: "+formatDecimal(dimes)+"\n " +
 			"Nickels: "+formatDecimal(nickels)+"\n" +
 			"Pennies: "+formatDecimal(pennies)+"\n");
 	System.exit(0);
 	}
 
 //Starting with Quarters/Dimes
 /*
  *If uneven amount of coins, 
  *take remainder of coins, 
  *divide amongst the smaller coins, 
  *subtract added amount of previous coins from total,
  *repeat until pennies.
  *
  *Print amount at each end of reduction,
  *Round each amount up into the various ints.
 */
 if(decimalChange % 0.25 >= 0){
 	doubleQuarters = (decimalChange/0.25);
 	finalDoubleQuarters = (double) Math.round(doubleQuarters*100.0)/100.0;
 	quarters = (int)(finalDoubleQuarters);
 	if((decimalChange-((quarters*25)*0.01)) % 0.10 >= 1){
 		doubleDimes = ((decimalChange-((quarters*25)*0.01))/0.10);
 		finalDoubleDimes = (double) Math.round(doubleDimes*100.0)/100.0;
 		dimes = (int)(finalDoubleDimes);
 		JOptionPane.showMessageDialog(null, "keep going");
 		if(decimalChange-((((quarters*25)+(dimes*10))*0.01)) % 0.05 >= 1){
 			doubleNickels = ((decimalChange-(((quarters*25)+(dimes*10))*0.01))/0.05);
 			finalDoubleNickels = (double) Math.round(doubleNickels*100.0)/100.0;
 			nickels = (int)(finalDoubleNickels);
 			JOptionPane.showMessageDialog(null, "one more");
 			if((decimalChange-(((quarters*25)+(dimes*10)+(nickels*5))*0.01)) > 0){
 				doublePennies = ((decimalChange-(((quarters*25)+(dimes*10)+(nickels*5))*0.01)));
 				finalDoublePennies = (double) Math.round(doublePennies*100.0)/100.0;
 				pennies = (int)(finalDoublePennies);
 				JOptionPane.showMessageDialog(null, "made it to the end. something happened.");
 				JOptionPane.showMessageDialog(null, "You have:\n " +
 						"Quarters: "+formatDecimal(quarters)+"\n " +
 						"Dimes: "+formatDecimal(dimes)+"\n " +
 						"Nickels: "+formatDecimal(nickels)+"\n" +
 						"Pennies: "+formatDecimal(pennies)+"\n");
 				System.exit(0);
 			}
 			JOptionPane.showMessageDialog(null, "You have:\n " +
 					"Quarters: "+formatDecimal(quarters)+"\n " +
 					"Dimes: "+formatDecimal(dimes)+"\n " +
 					"Nickels: "+formatDecimal(nickels)+"\n" +
 					"Pennies: "+formatDecimal(pennies)+"\n");
 			System.exit(0);
 		}
 		JOptionPane.showMessageDialog(null, "You have:\n " +
 				"Quarters: "+formatDecimal(quarters)+"\n " +
 				"Dimes: "+formatDecimal(dimes)+"\n " +
 				"Nickels: "+formatDecimal(nickels)+"\n" +
 				"Pennies: "+formatDecimal(pennies)+"\n");
 		System.exit(0);
 	}
 	//Starting with Nickels
 		else if (decimalChange-(((quarters*25)*0.01)) % 0.05 >= 1){
 			doubleNickels = ((decimalChange-((quarters*25)*0.01))/0.05);
 			finalDoubleNickels = (double) Math.round(doubleNickels*100.0)/100.0;
 			nickels = (int)(finalDoubleNickels);
 			if((decimalChange-(((quarters*25)+(nickels*5))*0.01)) >= 0){
 				doublePennies = ((decimalChange-(((quarters*25)+(nickels*5))*0.01)))*100.0;
 				finalDoublePennies = (double) Math.round(doublePennies*100.0);
 				pennies = (int)(doublePennies);
 				//JOptionPane.showMessageDialog(null, "hello world");
 				JOptionPane.showMessageDialog(null, "You have:\n " +
 						"Quarters: "+formatDecimal(quarters)+"\n " +
 						"Dimes: "+formatDecimal(dimes)+"\n " +
 						"Nickels: "+formatDecimal(nickels)+"\n" +
 						"Pennies: "+formatDecimal(pennies)+"\n");
 				System.exit(0);
 				}
 			JOptionPane.showMessageDialog(null, "You have:\n " +
 					"Quarters: "+formatDecimal(quarters)+"\n " +
 					"Dimes: "+formatDecimal(dimes)+"\n " +
 					"Nickels: "+formatDecimal(nickels)+"\n" +
 					"Pennies: "+formatDecimal(pennies)+"\n");
 			System.exit(0);
 			}
 	//Starting with Pennies
 		else if((decimalChange-((quarters*25)*0.01)) >= 0){
 			doublePennies = ((decimalChange-((quarters*25)*0.01)))*100.0;
 			//finalDoublePennies = (double) Math.round(doublePennies*100.0);
 			pennies = (int)(doublePennies);
 			//JOptionPane.showMessageDialog(null, "hello world");
 			JOptionPane.showMessageDialog(null, "You have:\n " +
 					"Quarters: "+formatDecimal(quarters)+"\n " +
 					"Dimes: "+formatDecimal(dimes)+"\n " +
 					"Nickels: "+formatDecimal(nickels)+"\n" +
 					"Pennies: "+formatDecimal(pennies)+"\n");
 			System.exit(0);
 		}
 }
 	}
 }
