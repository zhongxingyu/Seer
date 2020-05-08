 package assemblernator;
 
 import assemblernator.ErrorReporting.ErrorHandler;
 import assemblernator.Instruction.ConstantRange;
 
 /**
  * checks operands for correct values.
  * 
  * @author Noah
  * @date Apr 14, 2012; 4:54:44 PM
  */
 public class OperandChecker {
 	/**
 	 * Checks if value of index register valid.
 	 * @author Noah
 	 * @date Apr 14, 2012; 2:33:20 PM
 	 * @modified Apr 18, 2012; 12:30:21 AM removed try catch. -Noah
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param idx value of index register.
 	 * @return 1 <= reg <= 7.
 	 * @specRef N/A
 	 */
 	public static boolean isValidIndex(int idx) {
 		return (idx <= 7 && idx >= 1);
 	}
 
 	/**
 	 * Checks if expression of registers
 	 * 
 	 * @author ERIC
 	 * @date Apr 14, 2012; 6:33:20 PM
 	 * @modified Apr 18, 2012; 12:28:05 AM removed try catch. -Noah
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param reg
 	 *            value of register.
 	 * @return if reg > 7 || reg < 0 return false, else return true.
 	 * @specRef N/A
 	 */
 	public static boolean isValidReg(int reg) {
 		return (reg <= 7 && reg >= 0);
 	}
 
 
 	/**
 	 * Checks if value of operand NW, is valid.
 	 * @author Noah
 	 * @date Apr 14, 2012; 5:10:21 PM
 	 * @modified Apr 18, 2012; 12:34:55 AM removed try catch. -Noah
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param nw number of words.
 	 * @return 0 <= nw <= 15
 	 * @specRef N/A
 	 */
 	public static boolean isValidNumWords(int nw) {
		return (nw <= 15 && nw >= 0); //NW is 4 bits in instruction format.
 	}
 
 	/**
 	 * Checks whether a literal value falls within range.
 	 * @author Noah
 	 * @date Apr 14, 2012; 5:10:38 PM
 	 * @modified Apr 18, 2012; 12:08 AM: added implementation and new arg. -Noah
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param lit the literal value to check.
 	 * @param range range of valid constant values.
 	 * @return if lit falls within range, then return true, else return false.
 	 * @specRef N/A
 	 */
 	//literal for arithmetic
 	public static boolean isValidLiteral(int lit, ConstantRange range) {
 		return isValidConstant(lit, range);
 	}
 
 	/**
 	 * Checks whether a constant value falls within range.
 	 * @author Noah
 	 * @date Apr 14, 2012; 5:10:41 PM
 	 * @modified Apr 17, 2012; 10:44 PM changed to accomodate new ConstantRange enum. - Noah
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param cnst the constant value to check.
 	 * @param range range of valid constant values.
 	 * @return if cnst falls within range, then return true, else return false.
 	 * @specRef N/A
 	 */
 	public static boolean isValidConstant(int cnst, ConstantRange range) {
 		return (cnst <= range.max && cnst >= range.min);
 	}
 
 	/**
 	 * Checks whether a given address is a valid address.
 	 * @author Noah
 	 * @date Apr 14, 2012; 5:10:44 PM
 	 * @modified Apr 18, 2012; 12:34:55 AM removed try catch. -Noah
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param addr address to check for validity.
 	 * @return 0 <= addr <= 4095.
 	 * @specRef N/A
 	 */
 	public static boolean isValidMem(int addr) {
 		return (addr <= 4095 && addr >= 0);
 	}
 
 	/**
 	 * Checks that a given parameter comprises precisely one string.
 	 * 
 	 * @author Josh Ventura
 	 * @date Apr 14, 2012; 5:10:51 PM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param str
 	 *            The string parameter to check.
 	 * @return Whether this parameter is exactly one string.
 	 * @specRef N/A
 	 */
 	public static boolean isValidString(String str) {
 		str = str.trim();
 		if (str.length() < 1)
 			return false;
 		if (str.charAt(0) != '\'')
 			return false;
 		for (int p = 1; p < str.length(); ++p)
 			if (str.charAt(p) == '\\')
 				++p;
 			else if (str.charAt(p) == '\'') {
 				System.out.println("(" + p + " + 1) >= '" + str
 						+ "'.length() = " + (p + 1) + " >= " + str.length()
 						+ " = " + (p + 1 >= str.length()));
 				return p + 1 >= str.length();
 			}
 		return false;
 	}
 }
