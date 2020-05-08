 /*
  * 
  *  Coded by: 	Cody Averett
  * Professor: 	S. Sever
  * 
  * Program: 	Palindrome tester
  * 				Program 4.30 
  * 
  * Variables:	userNumber, holds the users input
  * 				done, boolean for if the app is done or not
  * 				input, scanner object for data entry
  * 			
  */
 
 import java.util.Scanner;
 
 public class main 
 {	
 
 	public static void main(String[] args) 
 	{
 		int userNumber;
 		boolean done = false;
 		Scanner input = new Scanner(System.in);
 		
 		while(!done) //loop until the user enters a correct number
 		{
 			System.out.printf("Enter a positive 5 digit number for the Palindrome test \n");
 			userNumber = input.nextInt(); //gets number from the user
 			
			if ( isPalindrome(userNumber) && isNumCorrectFormat(userNumber) ) //if number is a palindrome do this
 			{
 				System.out.printf("User number %d is a palindrome \n", userNumber); 
 				done = true; //user entered a correct number, nothing else to see here, move along		
 			} else {
				System.out.printf("Sorry, %d is not a palindrome \n", userNumber);
 			}
 		}
 		
 	}
 	
 	/*
 	 * Checks if a number is in the correct format
 	 * number must have a length of exactly five
 	 * 
 	 * Variables: 	numLength, holds length of the userNumber
 	 */
 	private static boolean isNumCorrectFormat(int userNumber) 
 	{
		int numLength = String.valueOf(userNumber).trim().length(); //cast to a string and get the length
 		
		if (numLength < 5 || numLength > 5) //logic
			return false;
		else { 
 			System.out.printf("The number you entered is not correctly formatted \n" +
							  "Must have 5 digits exactly \n");
			return true;
 		}
 	}
 
 	/*
 	 * Checks if number is a palindrome
 	 * If the users number equals the reverse of the 
 	 * number then it is a palindrome
 	 */
 	public static boolean isPalindrome(int number) 
 	{
 		return number == reverse(number);
 	}
 		
 	/*
 	 * Reverses a number completely
 	 * ex 12345 will equal 54321
 	 */
 	public static int reverse(int userNumber)
 	{
 		int result = 0;		
 		while (userNumber != 0) {
 		int remainder = userNumber % 10; //gets the smallest digit 
 		
 		//System.out.printf("%d \t\t %d \n", remainder, userNumber); //status test
 
 		result = result * 10 + remainder; //builds the reversed number
 
 		userNumber /= 10; //removes the last digit
 		}
 
 		return result;
 	}
 
 }
