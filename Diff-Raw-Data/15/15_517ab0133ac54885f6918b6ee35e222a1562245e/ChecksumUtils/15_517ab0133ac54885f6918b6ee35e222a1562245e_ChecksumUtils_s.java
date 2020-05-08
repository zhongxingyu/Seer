 package hx.bankcheck.accountvalidator.utils;
 
 import hx.bankcheck.accountvalidator.exceptions.IllegalAccountNumber;
 
 public class ChecksumUtils {
 
 	/**
 	 * Calculates the crossfoot of a number
 	 * 
 	 * @param number
 	 * @return
 	 */
 	public static int qs(int number) {
 		int qs = 0;
 
 		while (number != 0) {
 			qs = qs + (number % 10);
 			number = number / 10;
 		}
 
 		return qs;
 	}
 
 	/**
 	 * Fills up the account number by adding '0' to the left until size is
 	 * reached.
 	 * 
	 * @author Sascha D�mer (sdo@lmis.de) - LM Internet Services AG
 	 * @version 1.0
 	 * 
 	 * @param sizeOfAccountNumber
 	 *            new size of the account number
 	 * @param orignalAccountNumber
 	 *            account number to be filled
 	 * @return array containing the new account number
 	 */
 	public static int[] getFilledAcountNumber(int sizeOfAccountNumber,
 			int[] orignalAccountNumber) {
 		if (sizeOfAccountNumber > orignalAccountNumber.length) {
 			int[] filledAccountNumber = new int[sizeOfAccountNumber];
 			int offset = sizeOfAccountNumber - orignalAccountNumber.length;
 			for (int i = 0; i < orignalAccountNumber.length; i++) {
 				filledAccountNumber[i + offset] = orignalAccountNumber[i];
 			}
 			return filledAccountNumber;
 		} else
 			return orignalAccountNumber;
 	}
 
 	/**
 	 * Parses the given account number as long value for comparing it.
 	 * 
 	 * @author Tobias Mayer (bankcheck@tobiasm.de)
 	 * 
 	 * @param acccountNumber
 	 *            account number to parse
 	 * @return long value of the given account number
 	 */
 	public static long parseLong(int[] accountNumber) {
 		long l = 0l;
 		for(int i=0; i<accountNumber.length; i++) {
 			l *= 10;
 			l += accountNumber[i];
 		}
 		return l;
 	}
 
 	/**
 	 * Parses the given account number as int[].
 	 * 
	 * @author Sascha D�mer (sdo@lmis.de) - LM Internet Services AG
 	 * @version 1.0
 	 * 
 	 * @param accountNumberAsLong
 	 *            account number to parse
 	 * @return int[] containing the account number
 	 * @throws IllegalAccountNumber thrown when accountNumber has too much digits
 	 */
 	public static int[] parseAccountNumber(long accountNumberAsLong) throws IllegalAccountNumber {
 		int[] result= new int[10];
 		int pos = 0;
 		do {
 			int intValue = (int) (accountNumberAsLong % 10);
 			result[9-pos] =  intValue;
 			accountNumberAsLong /= 10;
 			pos++;
 		} while (accountNumberAsLong!=0 && pos<10);
 		
 		if (accountNumberAsLong > 0)
 			throw new IllegalAccountNumber("Accountnumber has more than 10 digits");
 		
 		return result;
 	}
 	
 }
