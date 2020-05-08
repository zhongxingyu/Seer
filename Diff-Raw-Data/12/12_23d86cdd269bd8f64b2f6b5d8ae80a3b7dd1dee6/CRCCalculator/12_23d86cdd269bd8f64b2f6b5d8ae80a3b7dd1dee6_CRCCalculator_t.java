 /*
  * Brandon Forster
  * CIS 3360  Security in Computing Fall 2012
  * Programming Assignment 2
  * 7 November 2012
  */
 
 package crc;
 
 import java.util.*;
 import java.io.*;
 import java.math.*;
 
 
 public class CRCCalculator {
 
 	private static File userFile;
 	private final static String BINARY_POLYNOMIAL = "10000010110001001";
 
 	public static void main(String[] args) {
 
 		//set up the keyboard scanner
 		Scanner stdin = new Scanner(System.in);
 
 		String pathname;
 		//make the user input filenames until we find one we like
 		while (true)
 		{
 			System.out.println("Please enter path/ name for file.");
 			pathname= stdin.next();
 			File tempFile= new File(pathname);
 
 			//check to make sure that the file the user entered exists
 			if (tempFile.exists() == false)
 			{
 				System.out.println("Invalid pathname for file. Try again.");
 				continue;
 			}
 			else
 			{
 				//if it does, copy the object to the file of higher scope.
 				userFile= tempFile;
 
 				//verifyFile checks that the input is only hex characters.
 				//if it's not, it returns false and input loop begins again.
 				if (verifyFile(userFile) == true)
 					break;
 				else
 				{
 					System.out.println("Input is not in hexadecimal format.");
 					continue;
 				}
 			}
 		}
 
 		//an infinite loop that has program functionality in it.
 		//will run until user tells it to exit.
 		int userChoice= 0;
 		while (userChoice != 3){
 
 			//read in user input
 			System.out.println("-------------Menu-------------");
 			System.out.println("1: Calculate CRC");
 			System.out.println("2: Verify CRC");
 			System.out.println("3: Exit");
 
 			//make sure the user inputs an integer, like they're supposed to
 			try
 			{
 				userChoice = stdin.nextInt();
 			}
 			catch (InputMismatchException e)
 			{
 				//tell the user they dun goofed
 				System.out.println("Please enter a valid choice.\n");
 				//eat the input from the scanner so we don't get stuck
 				stdin.next();
 				continue;
 			}
 
 			//the user wants to calculate CRC, go do that
 			if (userChoice == 1)
 				calculateCRC();
 
 			//go verify CRC
 			else if (userChoice == 2)
 				verifyCRC();
 
 			//exit the program normally
 			else if (userChoice == 3)
 				System.exit(0);
 
 			//tell the user they entered something weird
 			else
 			{
 				System.out.println("Please enter a valid choice.\n");
 				continue;
 			}
 
 
 		}
 
 		//we're done, close the scanner
 		stdin.close();
 	}
 
 	public static void calculateCRC()
 	{
 		printInit();
 
 		System.out.println("We will append sixteen zeros at the end of the binary input.\n");
 		String inputString= hexToBinary(getInputAsString());
 		inputString = inputString + "0000000000000000";
 
 		System.out.println("The binary string answer at each XOR step of CRC calculation:");
 
 		printBinary(inputString);
 		String printString= inputString;
 
 		for (int i= 0; i< inputString.length(); i++)
 		{
 			if (printString.charAt(i)== '0')
 				continue;
 
 			if ((BINARY_POLYNOMIAL.length() + i + 1 ) > printString.length())
 			{
 				printString= printString.substring(0, i)
 						+ xor(printString.substring(i), BINARY_POLYNOMIAL);
 				printBinary(printString);
 				break;
 			}
 
 			//what we've already done + what got xor'd
 			printString= printString.substring(0, i)
 					+ xor(printString.substring(i,(BINARY_POLYNOMIAL.length() +i)), BINARY_POLYNOMIAL)
 					+ inputString.substring((BINARY_POLYNOMIAL.length()+ i));
 			printBinary(printString);
 		}
 
 		//double reverse the string to get the checksum out
 		printString = reverse(printString);
 		printString = printString.substring(0, 16);
 		printString = reverse(printString);
 
 		System.out.print("Thus, the CRC is: (bin) ");
 		printBinary(printString);
 		System.out.println("which equals "+binaryToHex(printString)+ " (hex)");
		String input = getInputAsString();
		System.out.println("Reading input file again: "+ input + binaryToHex(printString));
 
 		try{
 			BufferedWriter userFileWrite = new BufferedWriter(new FileWriter(userFile));
			userFileWrite.write(input + binaryToHex(printString));
 
 			System.out.println("Closing input file.");
 			userFileWrite.close();
 
 			//this should never run ever.
 		}	catch (IOException e) {
 			System.out.println("Something went wrong...");
 		}
 
 		return;
 	}
 
 	public static void verifyCRC()
 	{
 		//save the input to a string variable for manipulation
 		String input = getInputAsString();
 		
 		//print out the header
 		printInit();
 		
 		if (input.length() < 4)
 		{
 			System.out.println("Error: input too short. Terminating program.");
 			System.exit(1);
 		}
 		
 		//do a double reverse to isolate the CRC
 		String crc= reverse(input);
 		crc= crc.substring(0, 4);
 		crc= reverse(crc);
 		
 		//print out the CRC we isolated
 		System.out.print("The 16-bit CRC at the end of the file: (hex) "+ crc + "= ");
 		printBinary(hexToBinary(crc));
 		
 		//print the chart
 		System.out.println("The binary string answer at each XOR step of CRC verification:");
 		//since we print after a permute, do a print of the input to get step 0
 		printBinary(hexToBinary(input));
 		
 		//some variables that make our lives easier
 		String binaryInput = hexToBinary(input);
 		String printString = binaryInput;
 		
 		//build the chart
		for (int i= 0; i< binaryInput.length() +1; i++)
 		{
 			//don't work on substrings that start with 0
 			if (printString.charAt(i)== '0')
 				continue;
 
 			//if we're at the end and don't want to run into a StringOutOfBounds
			if ((BINARY_POLYNOMIAL.length() + i) > printString.length())
 			{
 				//string we've processed thus far + xor processed
 				printString= printString.substring(0, i)
 						+ xor(printString.substring(i), BINARY_POLYNOMIAL);
 				printBinary(printString);
 				
 				//aaaaand we're done!
 				break;
 			}
 
 			//string we've processed thus far + xor processed
 			//+ whatever is past that that we haven't gotten to yet in the string
 			printString= printString.substring(0, i)
 					+ xor(printString.substring(i,(BINARY_POLYNOMIAL.length() +i)), BINARY_POLYNOMIAL)
 					+ binaryInput.substring((BINARY_POLYNOMIAL.length()+ i));
 			printBinary(printString);
 		}
 		
 		boolean crcCheckPass = true;
 		
 		//if there is a 1 anywhere in the final string, the check did not pass.
 		for (int i= 0; i < printString.length(); i++)
 		{
 			if (printString.charAt(i) == '1')
 				crcCheckPass = false;
 		}
 		
 		//GET RESULTS.
 		System.out.print("\nDid the CRC check pass? (Yes or No): ");
 		if (crcCheckPass == true)
 			System.out.println("Yes");
 		else
 			System.out.println("No");
 	}
 
 
 
 	public static void printInit()
 	{
 		//print out the input file
 		System.out.println("The input file (hex): "+
 				getInputAsString());
 
 		//print out a binary representation of the input hex
 		System.out.println("The input file (bin): ");
 		printBinary(hexToBinary(getInputAsString()));
 		System.out.println("");
 
 		//print out polynomial used
 		System.out.print("The polynomial that was used "+
 				"(binary bit string): ");
 		printBinary(BINARY_POLYNOMIAL);
 		System.out.println("");
 	}
 
 	//return a string representation of input
 	public static String getInputAsString()
 	{
 		String inputString= "";
 		try {
 			Scanner scn = new Scanner(userFile);
 
 			//while the scanner can find strings in the input.
 			while (scn.hasNext())
 				inputString = inputString + scn.next();
 
 			scn.close();
 
 			//this should never run ever.
 		}	catch (FileNotFoundException e) {
 			System.out.println("Something went wrong...");
 		}
 
 		return inputString;
 	}
 
 	//make sure input is well formed.
 	public static boolean verifyFile(File input)
 	{
 		try {
 			Scanner hexScanner = new Scanner(input);
 
 			//changes the delimiter to the empty string so that .next()
 			//returns one character at a time.
 			hexScanner.useDelimiter("");
 
 			while (hexScanner.hasNext() == true)
 			{
 				//converts the read in string to a char, then checks if it is
 				//in valid ascii ranges
 				char check = hexScanner.next().toUpperCase().toCharArray()[0];
 				if (check < '0')
 				{
 					hexScanner.close();
 					return false;
 				}
 				else if (check > 'F')
 				{
 					hexScanner.close();
 					return false;
 				}
 				else if (check > '9' && check < 'A')
 				{
 					hexScanner.close();
 					return false;
 				}
 			}
 
 			hexScanner.close();
 
 			//if it passed above checks, it must be okay.
 			return true;
 
 			//this should never run ever.
 		}	catch (FileNotFoundException e) {
 			System.out.println("Something went wrong...");
 			return false;
 		}
 	}
 
 	//convert hexadecimal to binary using BigInt
 	public static String hexToBinary(String hexNumber)
 	{
 		BigInteger temp = new BigInteger(hexNumber, 16);
 		return temp.toString(2);
 	}
 
 	//print binary numbers using the defined rules
 	public static void printBinary(String binaryNumber)
 	{
 		//uses regexes that I looked up how to do online to perform
 		//required ops.
 		binaryNumber= binaryNumber.replaceAll(".{32}", "$0\n");
 
 		//does a double reverse to get proper formatting on bits
 		binaryNumber= reverse(binaryNumber);
 		binaryNumber= binaryNumber.replaceAll(".{4}", "$0 ");
 		binaryNumber= reverse(binaryNumber);
 
 		System.out.print(binaryNumber);
 
 		//some functionality that ensures there's always a newline
 		if (binaryNumber.length() < 32)
 			System.out.println("");
 	}
 
 	//reverses a string recursively
 	public static String reverse(String str) {
 		if (str.length() <= 1) { 
 			return str;
 		}
 		return reverse(str.substring(1, str.length())) + str.charAt(0);
 	}
 
 	//converts a binary string to hexadecimal
 	public static String binaryToHex (String binaryNumber)
 	{
 		BigInteger temp = new BigInteger(binaryNumber, 2);
 		return temp.toString(16).toUpperCase();
 	}
 
 	//performs an exclusive or on two binary strings
 	public static String xor (String one, String two)
 	{
 		//operate based on the smallest string
 		int minLength= Math.min(one.length(), two.length());
 		String output= "";
 		
 		//for each character in the string, do a bitwise XOR
 		for (int i= 0; i< minLength; i++)
 			output= output + (one.charAt(i) ^ two.charAt(i));
 
 		return output;
 	}
 }
