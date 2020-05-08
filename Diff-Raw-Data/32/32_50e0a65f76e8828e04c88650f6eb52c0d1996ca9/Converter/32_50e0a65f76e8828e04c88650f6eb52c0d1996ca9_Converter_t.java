 import java.io.UnsupportedEncodingException;
 import java.lang.Math;
 
 /**
  * OpConverter takes a line of source code in a number format, and converts <br />
  * it into the requested format, that is, binary, hexadecimal, or decimal.
  * 
  * @author Jeff Wolfe
  *
  */
 public class Converter implements ConverterInterface {
 
 	public Converter() {
 		//No fields, it's a utility class.
 	}
 	
 	@Override
 	public String binaryToHex(String binary) {
 		//Convert the binary string into a decimal value.		
 		long decimal = Long.parseLong(binary,2);
 		//Convert the decimal value into a hex value.
 		String hexOut = Long.toHexString(decimal);
 		return hexOut;
 	}
 
 	@Override
 	public String hexToBinary(String hex) {
 		int counter = 0;
 		int decimal = 0;
 		char digit;
 		
 		//Convert the entire string to uppercase for conversion purposes.
 		hex = hex.toUpperCase();
 		
 		//Convert each hex digit to decimal one by one and add them up.
 		while (hex.length() > counter)
 		{
 			//Get least significant digit.
 			digit = hex.charAt(hex.length()-1-counter);
 			//If the digit is 0 we need to do nothing.
 			if (digit == '0')
 			{
 			}
 			//Otherwise, we add it to our decimal variable.
 			else if (digit == '1')
 			{
 				decimal = (int) (decimal + (1 * (Math.pow(16,counter))));
 			}
 			else if (digit == '2')
 			{
 				decimal = (int) (decimal + (2 * (Math.pow(16,counter))));
 			}
 			else if (digit == '3')
 			{
 				decimal = (int) (decimal + (3 * (Math.pow(16,counter))));
 			}
 			else if (digit == '4')
 			{
 				decimal = (int) (decimal + (4 * (Math.pow(16,counter))));
 			}
 			else if (digit == '5')
 			{
 				decimal = (int) (decimal + (5 * (Math.pow(16,counter))));
 			}
 			else if (digit == '6')
 			{
 				decimal = (int) (decimal + (6 * (Math.pow(16,counter))));
 			}
 			else if (digit == '7')
 			{
 				decimal = (int) (decimal + (7 * (Math.pow(16,counter))));
 			}
 			else if (digit == '8')
 			{
 				decimal = (int) (decimal + (8 * (Math.pow(16,counter))));
 			}
 			else if (digit == '9')
 			{
 				decimal = (int) (decimal + (9 * (Math.pow(16,counter))));
 			}
 			else if (digit == 'A')
 			{
 				decimal = (int) (decimal + (10 * (Math.pow(16,counter))));
 			}
 			else if (digit == 'B')
 			{
 				decimal = (int) (decimal + (11 * (Math.pow(16,counter))));
 			}
 			else if (digit == 'C')
 			{
 				decimal = (int) (decimal + (12 * (Math.pow(16,counter))));
 			}
 			else if (digit == 'D')
 			{
 				decimal = (int) (decimal + (13 * (Math.pow(16,counter))));
 			}
 			else if (digit == 'E')
 			{
 				decimal = (int) (decimal + (14 * (Math.pow(16,counter))));
 			}
 			else
 			{
 				decimal = (int) (decimal + (15 * (Math.pow(16,counter))));
 			}
 			counter++;
 		}
 		//Return the binary string.
 		return Integer.toBinaryString(decimal);
 	}
 	
 	@Override
 	public String hexToDec(String hex) {
 		int counter = 0;
 		int decimal = 0;
 		char digit;
 		
 		//Convert the entire string to uppercase for conversion purposes.
 		hex = hex.toUpperCase();
 		
 		//Convert each hex digit to decimal one by one and add them up.
 		while (hex.length() > counter)
 		{
 			//Get least significant digit.
 			digit = hex.charAt(hex.length()-1-counter);
 			//If the digit is 0 we need to do nothing.
 			if (digit == '0')
 			{
 			}
 			//Otherwise, we add it to our decimal variable.
 			else if (digit == '1')
 			{
 				decimal = (int) (decimal + (1 * (Math.pow(16,counter))));
 			}
 			else if (digit == '2')
 			{
 				decimal = (int) (decimal + (2 * (Math.pow(16,counter))));
 			}
 			else if (digit == '3')
 			{
 				decimal = (int) (decimal + (3 * (Math.pow(16,counter))));
 			}
 			else if (digit == '4')
 			{
 				decimal = (int) (decimal + (4 * (Math.pow(16,counter))));
 			}
 			else if (digit == '5')
 			{
 				decimal = (int) (decimal + (5 * (Math.pow(16,counter))));
 			}
 			else if (digit == '6')
 			{
 				decimal = (int) (decimal + (6 * (Math.pow(16,counter))));
 			}
 			else if (digit == '7')
 			{
 				decimal = (int) (decimal + (7 * (Math.pow(16,counter))));
 			}
 			else if (digit == '8')
 			{
 				decimal = (int) (decimal + (8 * (Math.pow(16,counter))));
 			}
 			else if (digit == '9')
 			{
 				decimal = (int) (decimal + (9 * (Math.pow(16,counter))));
 			}
 			else if (digit == 'A')
 			{
 				decimal = (int) (decimal + (10 * (Math.pow(16,counter))));
 			}
 			else if (digit == 'B')
 			{
 				decimal = (int) (decimal + (11 * (Math.pow(16,counter))));
 			}
 			else if (digit == 'C')
 			{
 				decimal = (int) (decimal + (12 * (Math.pow(16,counter))));
 			}
 			else if (digit == 'D')
 			{
 				decimal = (int) (decimal + (13 * (Math.pow(16,counter))));
 			}
 			else if (digit == 'E')
 			{
 				decimal = (int) (decimal + (14 * (Math.pow(16,counter))));
 			}
 			else
 			{
 				decimal = (int) (decimal + (15 * (Math.pow(16,counter))));
 			}
 			counter++;
 		}
 		//Return the binary string.
		return Integer.toString(decimal);
 	}
 
 	@Override
 	public String decimalToHex(String decimal) {
 		
 		//Convert the string into an integer.
 		int dec = Integer.parseInt(decimal);
 		//Return the hex string.
 		return Integer.toHexString(dec);
 	}
 
 	@Override
 	public String binaryToDecimal(String binary) {
 		
 		// Convert the binary string into a decimal integer, then convert
 		// the decimal integer into a String and return.
 		//Convert the binary string into a decimal value.		
 		long decimal = Long.parseLong(binary,2);
 		return Long.toString(decimal);
 	}
 
 	@Override
 	public String decimalToBinary(String decimal) {
 		
 		//Convert the string into an integer.
 		int dec = Integer.parseInt(decimal);
 		//Return the binary string.
 		return Integer.toBinaryString(dec);
 	}
 	
 	public String asciiToBinary(String ascii) {
 		
 		//Create a new array of bytes, capable of storing 4 bytes,
 		//as that is the maximum number of ascii characters we will
 		//encounter at any one time, as the SAL560 contains 1-word
 		//operations and operands.
 		byte[] binary = new byte[4];
 		
 		//Create a counter variable for iterating through the byte array.
 		int counter = 0;
 		
 		//Create an integer intermediate value to convert into a binary string.
 		int rep;
 		
 		//Create a string for concatenation.
 		String currentBin = new String();
 		
 		//Create a string for holding the total binary value.
 		String totalBin = new String();
 		
 		
 		//Use a try catch for syntactical correctness.
 		try 
 		{
 			//Convert the ascii string passed in, into
 			//an array of bytes containing their binary
 			//representation.
 			binary = ascii.getBytes("US-ASCII");
 		} 
 		//"US-ASCII" is a supported encoding, so this will never
 		//throw an error, but is required for syntax measures.
 		catch (UnsupportedEncodingException e) 
 		{
 			//Again, since this will never throw an error, this
 			//is here for syntax purposes, but the stack trace
 			//would just print out a trace of where the error
 			//occurred and halt the program.
 			e.printStackTrace();
 		}
 		
 		//Iterate through the array and create the binary 
 		//representation
 		while (counter < binary.length)
 		{
 			//Get one ascii value from the byte array and 
 			//store it in an integer in base 10.
 			rep = binary[counter];
 			
 			//Turn it into a 8 digit binary string.
 			currentBin = Integer.toBinaryString(rep);
 			while (currentBin.length() < 8)
 			{
 				currentBin = "0" + currentBin;
 			}
 			
 			//Concatenate it with the total binary string.
 			totalBin = totalBin.concat(currentBin);
 			
 			counter++;
 			
 		}
 
 		//Return the final string
 		return totalBin;
 	}
 	
 	public String twosCompToInteger(String twos)
 	{
 		//Create a value to hold the final integer representation
 		String integerValue = new String();
 		
 		//Create an integer to hold the intermediate integer representation
 		int value = 0;
 		
 		//Check the most significant bit, if it is a one, treat it as a negative
 		if (twos.charAt(0) == '1')
 		{
 			int counter = 0;
 			String newTwos = new String();
 			//Flip the bits
 			while (counter < twos.length())
 			{
 				if (twos.charAt(counter) == '1')
 				{
 					newTwos = newTwos + "0";
 				}
 				else
 				{
 					newTwos = newTwos + "1";
 				}
 				counter++;
 			}
 			//Convert that flipped value into an integer to add to
 			value = binToDec(newTwos);
 			//Add one to it to complete the conversion
 			value = value + 1;
 			//Make it negative
 			value = value * -1;
 			
 		}
 		//Otherwise, treat it as a positive
 		else
 		{
 			//convert each digit into an integer (base 10) using a while loop and add it
 			//to a temporary int object, then convert to string.
 			value = binToDec(twos);
 		}
 		
 		integerValue = Integer.toString(value);
 		return integerValue;
 	}
 
 	/**
 	 * This private method is called for the intermediate step in some <br />
 	 * conversions of converting binary to decimal.
 	 * 
 	 * @param binary The binary number to be converted into decimal.
 	 * @return The converted decimal integer.
 	 */
 	private int binToDec(String binary) {
 		int decimal = 0;
 		int counter = 0;
 		int conversion = 0;
 		String binOne = new String();
 		
 		//Convert each binary digit until you reach the end of the binary number.
 		while (binary.length() > counter) 
 		{
 			//Turn one digit, starting with the least significant one, of the binary string into an integer.
 			binOne = binary.substring(binary.length()-(counter+1), binary.length()-counter);			
 			conversion = Integer.parseInt(binOne);
 			//Multiply that by 2 to the power of whatever position in the string
 			//we are in, starting at 0 and ending at binary.length - 1.
 			conversion = (int) (conversion * Math.pow(2, counter));
 			//Add the newly converted binary digit to the total decimal number.
 			decimal = decimal + conversion;
 			//Increment the counter.
 			counter++;
 		}
 		
 		return decimal;
 	}
 }
