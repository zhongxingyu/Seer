 package net.adbenson.codekata.parser;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.adbenson.codekata.common.Config;
 import net.adbenson.codekata.model.AccountNumber;
 import net.adbenson.codekata.model.Digit;
 
 /**
  * DigitNumberParser class parses a set of lines as a single account number
  * @author Andrew
  *
  */
 public class AccountNumberParser {
 	
 	/**
 	 * The parser that will identify the actual digits of the nuber
 	 */
 	private DigitParser digitParser;
 
 	/**
 	 * Parses an account number model to a long number
 	 * @param account
 	 * @return 
 	 */
 	public AccountNumber parse(List<String> accountNumberLines) {
 		List<Digit> digits = new ArrayList<Digit>();
 		
 		long number = 0;
 		
 		for(int i=0; i<Config.DIGITS_PER_NUMBER; i++) {
 					
 			//Find the digit value
 			List<String> rows = separateDigits(accountNumberLines, i);
 			Digit digit = digitParser.parse(rows);
 			
 			//Left-shift it to find the actual value
 			//The value of the digit increases L-R but we read R-L, so invert the index
 			int power = Config.DIGITS_PER_NUMBER - i - 1;
 			number += digit.getValue() * Math.round(Math.pow(10, power));
 		}
 		
 		AccountNumber account = new AccountNumber(digits, number);
 		return account;
 	}
 	
 	public List<String> separateDigits(List<String> lines, int offset) {
 		offset *= Config.CHARACTERS_PER_DIGIT;
 
 		List<String> rows = new ArrayList<String>();
 		for (String line : lines) {
 			String row = line.substring(offset, offset
 					+ Config.CHARACTERS_PER_DIGIT);
 			rows.add(row);
 		}
 		
 		return rows;
 	}
 
 	public void setDigitParser(DigitParser digitParser) {
 		this.digitParser = digitParser;		
 	}
 }
