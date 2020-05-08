 package aup;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 
 /**
  *
  * @version 0.02
  *
  */
 public class IOUtils {
 
 	/**
 	 * This method lets you type in an floating point number of arbitrary size.
 	 * If an error occurs BigDecimal.ZERO is returned.
 	 *
 	 * @return BigInteger typed in
 	 */
 	public static BigDecimal readBigDecimal() {
 		BigDecimal readValue = BigDecimal.ZERO;
 		String inputString = readString();
 		try {
 			readValue = new BigDecimal(inputString);
 		} catch (NumberFormatException numFormEx) {
 			System.err.println("Number conversion error: "
 					+ numFormEx.getMessage());
 		}
 		return readValue;
 	}
 
 	/**
 	 * This method lets you type in an integral number bigger than
 	 * Long.MAX_VALUE or more negative than Long.MIN_VALUE and returns it. If an
 	 * error occurs BigInteger.ZERO is returned.
 	 *
 	 * @return BigInteger typed in
 	 */
 	public static BigInteger readBigInteger() {
 		BigInteger readValue = BigInteger.ZERO;
 		String inputString = readString();
 		try {
 			readValue = new BigInteger(inputString);
 		} catch (NumberFormatException numFormEx) {
 			System.err.println("number conversion error: "
 					+ numFormEx.getMessage());
 		}
 		return readValue;
 	}
 
 	/**
 	 * This method lets you type in a char and returns it. If an error occurs
 	 * Character.UNASSIGNED is returned.
 	 *
 	 * @return char value formerly read in
 	 */
 	public static char readChar() {
 		char readValue = Character.UNASSIGNED;
 		BufferedReader inputReader = null;
 		try {
 			inputReader = new BufferedReader(new InputStreamReader(System.in));
 			readValue = (char) inputReader.read();
 		} catch (IOException ioEx) {
 			System.err.println("IO error: " + ioEx.getMessage());
 		}
 		return readValue;
 	}
 
 	/**
 	 * This method lets you type in a floating point number and returns it. If
	 * an error occurs 0.0 is returned.
 	 *
 	 * @return double typed in
 	 */
 	public static double readDouble() {
 		double readValue = 0.0;
 		String inputString = readString();
 		try {
 			readValue = Double.parseDouble(inputString);
 		} catch (NumberFormatException numFormEx) {
 			System.err.println("number conversion error: "
 					+ numFormEx.getMessage());
 		}
 		return readValue;
 	}
 
 	/**
 	 * This method lets you type in a floating point number and returns it. If
 	 * an error occurs 0.0F is returned.
 	 *
 	 * @return float typed in
 	 */
 	public static float readFloat() {
 		float readValue = 0.0F;
 		String inputString = readString();
 		try {
 			readValue = Float.parseFloat(inputString);
 		} catch (NumberFormatException numFormEx) {
 			System.err.println("number conversion error: "
 					+ numFormEx.getLocalizedMessage());
 		}
 		return readValue;
 	}
 
 	/**
 	 * This method lets you type in an integral number and returns it. If an
 	 * error occurs 0 is returned.
 	 *
 	 * @return integer typed in
 	 */
 	public static int readInt() {
 		int readValue = 0;
 		String inputString = readString();
 		try {
 			readValue = Integer.parseInt(inputString);
 		} catch (NumberFormatException numFormEx) {
 			System.err.println("number conversion error: "
 					+ numFormEx.getLocalizedMessage());
 		}
 		return readValue;
 	}
 
 	/**
 	 * This method lets you type in an integral number and returns it. If an
 	 * error occurs 0 is returned.
 	 *
 	 * @return long typed in
 	 */
 	public static long readLong() {
 		long readValue = 0L;
 		String inputString = readString();
 		try {
 			readValue = Long.parseLong(inputString);
 		} catch (NumberFormatException numFormEx) {
 			System.err.println("number conversion error: "
 					+ numFormEx.getLocalizedMessage());
 		}
 		return readValue;
 	}
 
 	/**
 	 * This method lets you type in a string and returns it. If an error occurs
 	 * "" is returned.
 	 *
 	 * @return string typed in
 	 */
 	public static String readString() {
 		String readValue = "";
 		BufferedReader inputReader = null;
 		try {
 			inputReader = new BufferedReader(new InputStreamReader(System.in));
 			readValue = inputReader.readLine();
 		} catch (IOException ioEx) {
 			System.err.println("IO error: " + ioEx.getLocalizedMessage());
 		}
 		return readValue;
 	}
 
 } // END CLASS IOUtils
