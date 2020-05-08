 package assemblernator;
 
 import static assemblernator.ErrorReporting.makeError;
 import instructions.Comment;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import assemblernator.ErrorReporting.ErrorHandler;
 import assemblernator.ErrorReporting.URBANSyntaxException;
 
 
 /**
  * The Instruction class is the do-all, see-all, have-all class for instruction
  * use and representation.
  * 
  * This class is both a descriptor class and a storage class. It represents the
  * main mechanisms for tokenizing, lexing, and assembling URBAN code.
  * 
  * The Instruction class is designed such that every module of the
  * Assemblernator can utilize it in some form or another.
  * 
  * These modules can interact with it in two ways, depending on what is
  * available:
  * 
  * First, they can interface with it using a static (or singleton) instance
  * obtained via getInstance(). These singleton instances have no context in a
  * particular piece of code; they are used for methods that would otherwise be
  * static if we could look up static instances. It is from these instances of
  * Instruction that methods such as getNewInstance() and execute() are most
  * likely to be called (though they are still valid for other instances, it is
  * the static instance which will be most handy).
  * 
  * Second, modules can interface with Instructions that were pulled from code.
  * These instances of Instruction are obtained using the static method
  * Instruction.parse(). It is for these instances of Instruction that the
  * check() and assemble() methods are valid.
  * 
  * @author Josh Ventura
  * @date Apr 4, 2012 1:39:03 AM
  */
 public abstract class Instruction {
 	/**
 	 * @author Josh Ventura
 	 * @date Apr 8, 2012; 1:51:01 AM
 	 * @modified Apr 17, 2012; 6:43:11 PM: added value field and toString() function. - Noah
 	 */
 	public static class Operand {
 		/** The operand keyword */
 		public String operand;
 		/** The expression given to the operand */
 		public String expression;
 		/** The value of the operand. */
 		public int value;
 		/**
 		 * The position in the line at which the keyword for this operand
 		 * started
 		 */
 		public int keywordStartPosition;
 		/**
 		 * The position in the line at which the value for this operand
 		 * started
 		 */
 		public int valueStartPosition;
 
 
 		/**
 		 * @param op
 		 *            The operand keyword.
 		 * @param exp
 		 *            The expression.
 		 * @param key_sp
 		 *            The index of the first character of this keyword's name in
 		 *            the line from which it was parsed.
 		 * @param val_sp
 		 *            The index of the first character of this keyword's value
 		 *            in the line from which it was parsed.
 		 */
 		public Operand(String op, String exp, int key_sp, int val_sp) {
 			operand = op;
 			expression = exp;
 			keywordStartPosition = key_sp;
 			valueStartPosition = val_sp;
 		}
 		
 		/**
 		 * @author Noah
 		 * @date Apr 17, 2012; 5:52:59 PM
 		 * @return The bit string of the value of this operand.
 		 * @specRef N/A
 		 */
 		@Override public String toString() {
 			return Integer.toBinaryString(value);
 		}
 	}
 
 	/**
 	 * @author Josh Ventura
 	 * @date Apr 5, 2012; 10:40:23 PM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @return A newly allocated instance of a particular child instruction.
 	 * @specRef N/A
 	 */
 	public abstract Instruction getNewInstance();
 
 	/**
 	 * An enumeration of constants that can be used as our usage kind.
 	 * 
 	 * @author Josh Ventura
 	 */
 	public enum Usage {
 		/** This item should not appear in the symbol table */
 		NONE,
 		/** Reference to external label */
 		EXTERNAL,
 		/** Preprocessor value */
 		EQUATE,
 		/** Entry point */
 		ENTRY,
 		/** A local label of some instruction */
 		LABEL,
 		/** The name of this module */
 		PROGNAME
 	}
 
 	/**
 	 * An enumeration of ranges for operand values of type FC.
 	 * @author Noah
 	 * @date Apr 17, 2012; 10:20:27 PM
 	 */
 	public enum ConstantRange {
 		/** DMP constant range.*/
 		RANGE_DMP(1,3),	//[1,3]
 		/** SHIFT constant range.*/
 		RANGE_SHIFT(0, 31), //[0, 31]
 		/** 13 bit 2's complement range.*/
 		RANGE_13_TC(-4095, 4095), //[-2^12, (2^12) - 1]
 		/** 16 bit 2's complement range.*/
 		RANGE_16_TC(-32768, 32767), //[-2^15, (2^15) - 1]
 		/** 12 bit address range.*/
 		RANGE_ADDR(0, 4095); //[0, 4095]
 		
 		/** max value of constant. */
 		public int max;
 		/** minimum value of constant. */
 		public int min;
 		
 		/**
 		 * constructs ConstantRange with a minimum value and maximum value.
 		 * @param min minimum value of constant operand.
 		 * @param max maximum value of constant operand.
 		 */
 		ConstantRange(int min, int max) {
 			this.min = min;
 			this.max = max;
 		}
 		
 	}
 	// =====================================================================
 	// == Members valid in the global instance, obtained with getInstance()
 	// =====================================================================
 
 	/**
 	 * Get the operation identifier used to refer to this instruction, such as
 	 * "MOVD", "IADD", or "NUM". Literally, get the name of this instruction.
 	 * 
 	 * @author Josh Ventura
 	 * @date Apr 5, 2012; 6:52:21 PM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @return The name of this instruction.
 	 * @specRef N/A
 	 */
 	abstract public String getOpId();
 
 	/**
 	 * The byte code that identifies this instruction to the machine. It should
 	 * be possible to compare against this instruction by ANDing the complete
 	 * instruction by this opcode, and then comparing the result to this opcode.
 	 * 
 	 * This is NOT a complete instruction opcode, just the segment that
 	 * identifies WHICH instruction is affiliated. To get the complete
 	 * instruction's opcode, use assemble().
 	 * 
 	 * @author Josh Ventura
 	 * @date Apr 5, 2012; 6:53:30 PM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @return The byte code identifying this instruction.
 	 * @specRef N/A
 	 */
 	abstract public int getOpcode();
 
 	/**
 	 * Get the location counter of the instruction following this instruction,
 	 * where the location counter at this instruction is provided as an
 	 * argument.
 	 * 
 	 * @author Josh Ventura
 	 * @date Apr 5, 2012; 11:36:32 PM
 	 * @modified Apr 8, 2012; 1:43:59 PM: Renamed method and
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param lc
 	 *            The original value of the location counter.
 	 * @param mod
 	 *            The Module which can be used to look up symbols if needed.
 	 * @return The value of the location counter for the next instruction.
 	 * @specRef N/A: See specification reference for individual Instance
 	 *          subclasses.
 	 */
 	abstract public int getNewLC(int lc, Module mod);
 
 	// =====================================================================
 	// == Members valid with new instances =================================
 	// =====================================================================
 
 	/** Any label that was given before this instruction. */
 	public String label;
 	/** The line counter index at which this instruction was read. */
 	public int lc;
 	/** A hash map of any instructions encountered. */
 	public ArrayList<Operand> operands = new ArrayList<Operand>();
 	/** The type of this instruction as one of the {@link Usage} constants. */
 	public Usage usage = Usage.NONE;
 	/** line number in source file. */
 	public int lineNum;
 	/** original source line. */
 	public String origSrcLine;
 
 	/**
 	 * Trivial utility method to check if an operand is used in this particular
 	 * instruction.
 	 * 
 	 * @author Josh Ventura
 	 * @param op
 	 *            The operand to check for.
 	 * @return True if this Instruction contains at least one instance of the
 	 *         given operand, false otherwise.
 	 * @date Apr 8, 2012; 1:35:52 AM
 	 */
 	public boolean hasOperand(String op) {
 		for (int i = 0; i < operands.size(); i++)
 			if (operands.get(i).operand.equals(op))
 				return true;
 		return false;
 	}
 
 	/**
 	 * Trivial utility method to get the number of times an operand is used
 	 * in this particular instruction.
 	 * 
 	 * @author Josh Ventura
 	 * @param op
 	 *            The operand to check for.
 	 * @return The number of times the given operand is given for this
 	 *         Instruction.
 	 * @date Apr 8, 2012; 1:35:52 AM
 	 */
 	public int countOperand(String op) {
 		int count = 0;
 		for (int i = 0; i < operands.size(); i++)
 			if (operands.get(i).operand.equals(op))
 				++count;
 		return count;
 	}
 
 	/**
 	 * Trivial utility method to get the expression of a given operand.
 	 * 
 	 * @author Josh Ventura
 	 * @param op
 	 *            The operand to check for.
 	 * @return The expression given to the operand.
 	 * @date Apr 8, 2012; 1:35:52 AM
 	 */
 	public String getOperand(String op) {
 		for (int i = 0; i < operands.size(); i++)
 			if (operands.get(i).operand.equals(op))
 				return operands.get(i).expression;
 		return null;
 	}
 
 	/**
 	 * Trivial utility method to get an Operand by its name.
 	 * 
 	 * @author Josh Ventura
 	 * @param op
 	 *            The operand to check for.
 	 * @return The complete operand.
 	 * @date Apr 15, 2012; 11:59:07 AM
 	 */
 	public Operand getOperandData(String op) {
 		for (int i = 0; i < operands.size(); i++)
 			if (operands.get(i).operand.equals(op))
 				return operands.get(i);
 		return null;
 	}
 
 	/**
 	 * Trivial utility method to get the expression of the Nth occurrence a
 	 * given operand.
 	 * 
 	 * @author Josh Ventura
 	 * @param op
 	 *            The operand to check for.
 	 * @param indx
 	 *            The number of matching operands to skip.
 	 * @return The expression given to the operand.
 	 * @date Apr 8, 2012; 1:35:52 AM
 	 */
 	public String getOperand(String op, int indx) {
 		for (int i = 0; i < operands.size(); i++)
 			if (operands.get(i).operand.equals(op))
 				if (--indx <= 0)
 					return operands.get(i).expression;
 		return null;
 	}
 
 	/**
 	 * Trivial utility method to get the Nth occurrence an Operand with a
 	 * given name.
 	 * 
 	 * @author Josh Ventura
 	 * @param op
 	 *            The operand to check for.
 	 * @param indx
 	 *            The number of matching operands to skip.
 	 * @return The complete Operand.
 	 * @date Apr 8, 2012; 1:35:52 AM
 	 */
 	public Operand getOperandData(String op, int indx) {
 		for (int i = 0; i < operands.size(); i++)
 			if (operands.get(i).operand.equals(op))
 				if (--indx <= 0)
 					return operands.get(i);
 		return null;
 	}
 
 	/**
 	 * @author Josh Ventura
 	 * @date Apr 13, 2012; 8:20:18 PM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param expectedOperands
 	 *            All expected operands.
 	 * @return True if our operands match the expected operands precisely
 	 *         (except for order), false otherwise.
 	 */
 	boolean matchOperands(String... expectedOperands) {
 		HashMap<String, Integer> expOps = new HashMap<String, Integer>();
 		// Populate our hash map with all expected operands, by the number
 		// of operands we expect to see.
 		for (String opr : expectedOperands) {
 			Integer ov = expOps.put(opr, new Integer(1));
 			if (ov != null)
 				expOps.put(opr, ov + 1);
 		}
 		// Remove operands from that map as they are matched in this.
 		for (int i = 0; i < operands.size(); i++) {
 			Integer remaining = expOps.get(operands.get(i).operand);
 			// If this operand was not in our map, we don't have a match.
 			if (remaining == null)
 				return false;
 
 			// Otherwise, remove it (or one of it).
 			if (remaining == 1)
 				expOps.remove(operands.get(i));
 			else
 				expOps.put(operands.get(i).operand, remaining - 1);
 		}
 
 		// If there is anything left in expOps, we have missing operands.
 		if (expOps.size() > 0)
 			return false;
 
 		// Seems we have a match.
 		return true;
 	}
 
 	/**
 	 * Parse a string containing the operands of an instruction, storing the
 	 * operands locally.
 	 * 
 	 * If the given code does not contain an instruction, null is returned.
 	 * 
 	 * If the instruction is not concluded on the given line, this method will
 	 * throw an IOException with the message "RAL". "RAL" is short for "Request
 	 * Another Line"; the caller should read an additional line from the input,
 	 * append it to the string that was passed to the instance of this method
 	 * that generated the "RAL" exception, and then pass the concatenated string
 	 * back to this method to obtain the complete Instruction. This can happen
 	 * any number of times in sequence.
 	 * 
 	 * @author Josh Ventura
 	 * @date Apr 4, 2012; 1:40:21 AM
 	 * @modified Apr 5, 2012: 10:02:26 PM: Wrote body.
 	 * 
 	 *           Apr 6, 2012; 11:04:12 AM: Modified behavior for incomplete
 	 *           lines to more gracefully account for non-operational lines.
 	 * 
 	 *           Apr 7, 2012; 12:32:17 AM: Completed first working draft; more
 	 *           instruction classes required to test properly.
 	 * 
 	 *           Apr 8, 2012; 12:11:32 AM: Added usage information to output
 	 *           Instructions.
 	 * 
 	 *           Apr 15, 2012; 10:29:03 AM: Changed to allow spaces before
 	 *           colons as well as after them. Fixes bug in which quotes in
 	 *           labels are parsed as strings when in operand values.
 	 * 
 	 *           Apr 15, 2012; 12:14:28 PM: Added tracking of operand locations
 	 *           relative to the line on which they are used. Refactored operand
 	 *           loop.
 	 * @tested Apr 7, 2012; 12:52:43 AM: Tested with basic MOVD codes, given
 	 *         various kinds of expressions. While more instructions are
 	 *         necessary to get a full idea of whether or not this code is
 	 *         working, for the time being, it seems to function as required.
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param line
 	 *            A String containing the line of code to be parsed for an
 	 *            Instruction.
 	 * @return A new Instruction as defined in the given line of URBAN-ASM code,
 	 *         or null if the line was syntactically correct but did not contain
 	 *         an instruction.
 	 * @throws IOException
 	 *             Throws an IOException with the message "RAL" when the
 	 *             instruction is not concluded in the given string. The caller
 	 *             should re-invoke this method with the original parameter
 	 *             suffixed with the next line in the file.
 	 * @throws URBANSyntaxException
 	 *             Thrown when a syntax error occurs.
 	 * @specRef S2.1
 	 * @specRef S4.1
 	 */
 	public static final Instruction parse(String line) throws IOException,
 			URBANSyntaxException {
 		int i = 0;
 
 		// Skip leading whitespace and check boundaries.
 		if (i >= line.length()) // If the line is empty,
 			return null; // Then there's no point asking for another.
 		while (Character.isWhitespace(line.charAt(i)))
 			if (++i >= line.length()) // If the line is empty except whitespace,
 				return null; // Then there's no point asking for another.
 
 		// Exit if comment line.
 		if (line.charAt(i) == ';') // If the line is just a comment,
 			return new Comment(line.substring(++i));
 
 		// From here on out, we assume there is a valid instruction on this line
 		// which may or may not expand to other lines.
 
 		// Read in label and instruction data.
 		String label = IOFormat.readLabel(line, i); // Read in a label
 		String instruction;
 
 		// Check if we are at an instruction or a label
 		if (Assembler.instructions.containsKey(label.toUpperCase())) {
 			i += label.length(); // Skip the label
 			instruction = label;
 			label = null;
 		}
 		else {
 			if (i != 0)
 				throw new URBANSyntaxException(makeError("labelNotCol0", label,
 						(IOFormat.isAlpha(label) ? "known" : "valid")), i);
 
 			// Skip the label we read earlier
 			i += label.length();
 
 			if (i >= line.length())
 				throw new IOException("RAL");
 
 			if (!Character.isWhitespace(line.charAt(i)))
 				throw new URBANSyntaxException(makeError("unexSymFollow", ""
 						+ line.charAt(i), "label"), i);
 
 			while (++i < line.length()
 					&& Character.isWhitespace(line.charAt(i)));
 
 			if (i >= line.length())
 				throw new IOException("RAL");
 
 			instruction = IOFormat.readLabel(line, i); // Read instruction now
 			if (!Assembler.instructions.containsKey(instruction.toUpperCase())) {
 				if (IOFormat.isAlpha(instruction))
 					throw new URBANSyntaxException(makeError("instrUnknown",
 							instruction), i);
 				throw new URBANSyntaxException(makeError("instrInvalid",
 						instruction), i);
 			}
 			i += instruction.length();
 		}
 
 		if (i >= line.length())
 			throw new IOException("RAL2"); // Request another line
 
 		// So, now we know a bit about our instruction.
 		Instruction res, resp;
 		resp = Assembler.instructions.get(instruction.toUpperCase());
 		res = resp.getNewInstance();
 		res.label = label;
 
 		// Skip whitespace between instruction and operands.
 		while (Character.isWhitespace(line.charAt(i))) {
 			if (++i >= line.length()) // If we overrun this line looking,
 				throw new IOException("RAL3"); // Request another line.
 		}
 
 		// Our method can now handle a number of operand instructions.
 		while (line.charAt(i) != ';') {
 			// All operand keywords contain only letters.
 			if (!Character.isLetter(line.charAt(i)))
 				throw new URBANSyntaxException(makeError("expectOpBefore", ""
 						+ line.charAt(i)), i);
 
 			// Isolate the keyword
 			final int key_sp = i;
 			while (Character.isLetter(line.charAt(++i)));
 			String operand = line.substring(key_sp, i);
 			if (!Assembler.keyWords.contains(operand.toUpperCase()))
 				throw new URBANSyntaxException(
 						makeError("notOperand", operand), i);
 
 			if (Character.isWhitespace(line.charAt(i)))
 				do
 					if (++i >= line.length())
 						throw new IOException("RAL4");
 				while (Character.isWhitespace(line.charAt(i)));
 
 			if (line.charAt(i) != ':')
 				throw new URBANSyntaxException(makeError("expectOpColon",
 						operand), i);
 
 			if (++i >= line.length())
 				throw new IOException("RAL5");
 			while (Character.isWhitespace(line.charAt(i)))
 				if (++i >= line.length())
 					throw new IOException("RAL6");
 
 
 			final int val_sp = i;
 			while (line.charAt(i) != ';' && line.charAt(i) != ',') // Search.
 			{
 				// Don't get bitten by oddly formatted labels.
 				if (Character.isLetter(line.charAt(i))) {
 					i += IOFormat.readLabel(line, i).length();
 					continue;
 				}
 
 				/* Don't get tripped up by string literals.
 				 * Specification says that the single quote is the delimiter.
 				 * This loop searches for a matching quote, while observing
 				 * escape sequences. When a backslash is encountered, the
 				 * character proceeding it is skipped. We can prove the validity
 				 * of this design by examining its three basic cases:
 				 * 
 				 * If the escape sequence is \', the quote is skipped, and the
 				 * behavior is correct.
 				 * 
 				 * If the escape sequence is \\, the second slash is skipped,
 				 * and cannot accidentally escape another character, so '\\' is
 				 * correct.
 				 * 
 				 * In any other case, \r \n \xFF \100, the proceeding character
 				 * is immaterial, and can safely be skipped. */
 				if (line.charAt(i) == '\'') {
 					while (++i < line.length() && line.charAt(i) != '\'')
 						if (line.charAt(i) == '\\')
 							++i;
 					// Make sure we didn't just run out of line
 					if (++i >= line.length())
 						throw new IOException("RAL8");
 					continue;
 				}
 
 				if (!Character.isWhitespace(line.charAt(i))
 						&& !Character.isDigit(line.charAt(i)))
 					switch (line.charAt(i)) // Figure out what we have
 					{
 					case '+':
 					case '-':
 					case '*':
 					case '/':
 						break;
 					default:
 						throw new URBANSyntaxException(makeError("unexpSymOp",
 								"" + line.charAt(i), operand), i);
 					}
 
 				// Whatever we're at isn't our problem.
 				if (++i >= line.length()) // If we overrun this line looking,
 					throw new IOException("RAL7"); // Request another line.
 			}
 
 			String exp = line.substring(val_sp, i);
 			res.operands.add(new Operand(operand.toUpperCase(), exp, key_sp,
 					val_sp));
 
 			// Check if we have more work to do.
 			if (line.charAt(i) == ',')
 				do
 					if (++i >= line.length())
 						throw new IOException("RAL9");
 				while (Character.isWhitespace(line.charAt(i)));
 		}
 
 		res.usage = resp.usage;
 		if (res.usage == Usage.NONE && res.label != null)
 			res.usage = Usage.LABEL;
 		return res;
 	}
 
 	/**
 	 * Check if the token is semantically correct. This means that it has the
 	 * correct number of operands and the correct kinds of operands.
 	 * Also, checks whether operand values are within range.
 	 * 
 	 * @author Josh Ventura
 	 * @modified Apr 14, 2012; 12:00 PM: Added error handler to parameters.
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards This method is abstract.
 	 * @testingStandards This method is abstract.
 	 * @param hErr
 	 *            An error handler which will receive any error or warning
 	 *            messages.
 	 * @param module TODO
 	 * @return Returns whether the instruction is semantically correct.
 	 * @date Apr 4, 2012; 01:40:29AM
 	 */
 	public abstract boolean check(ErrorHandler hErr, Module module);
 
 	/**
 	 * Checks for lexical correctness; called immediately after construction.
 	 * 
 	 * @author Josh Ventura
 	 * @modified Apr 14, 2012; 12:00 PM: Added error handler to parameters.
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards This method is abstract.
 	 * @testingStandards This method is abstract.
 	 * @param hErr
 	 *            An error handler which will receive any error or warning
 	 *            messages.
 	 * @param module TODO
 	 * @return Returns whether the instruction is semantically correct.
 	 * @date Apr 4, 2012; 01:40:29AM
 	 */
 	public abstract boolean immediateCheck(ErrorHandler hErr, Module module);
 	
 	/**
 	 * Used to check if this Instruction is actually a directive.
 	 * @author Josh Ventura
 	 * @date Apr 16, 2012; 8:24:58 PM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @return Returns whether this Instruction is actually a directive
 	 * @specRef N/A
 	 */
 	public abstract boolean isDirective();
 	
 	/**
 	 * Assemble this instruction to byte code after it has been checked.
 	 * 
 	 * @author Josh Ventura
 	 * @date Apr 4, 2012; 1:40:52 AM
 	 * @modified UNMODIFIED
 	 * @tested This method is abstract.
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards This method is abstract.
 	 * @testingStandards This method is abstract.
 	 * @return Returns an array of getWordCount() integers representing this
 	 *         instruction's byte code.
 	 */
 	public abstract int[] assemble();
 
 	/**
 	 * @author Josh Ventura
 	 * @date Apr 4, 2012; 9:11:51 AM
 	 * @modified UNMODIFIED
 	 * @tested This method is abstract.
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards This method is abstract.
 	 * @testingStandards This method is abstract.
 	 * @param instruction
 	 *            The byte code of the instruction to be executed.
 	 */
 	public abstract void execute(int instruction);
 
 	/**
 	 * Returns a string representation of Instruction:
 	 * origSourceLine + "Line number: " + instr.LineNum + " " + LC: " + lc + " " + "Label: " + label + ",\n"
 	 * 		+ "instruction/Directive: " + instr.getOpID() + " " + Binary Equivalent: " + binEquiv + "\n" 
 	 * 		+ "operand " + i + operandKeyWord + ":" + operandValue + "Binary Equivalent: " + operBinEquiv;
 	 * 			where if instr does not have a label, then label = "", 
 	 * 				else label = instr.label, and
 	 * 			if instr is a directive and thus has no opcode, then binEquiv = "------", 
 	 * 				else binEquiv = instr.opcode in binary format, and
 	 * 			i represents the ith operand of instr, and
 	 * 				operandKeyword = the key word for the ith operand;
 	 * 				operandValue = the value associated w the operand with operandKeyword keyword for the ith operand, and
 	 * 			origSourceLine = instr.origSrcLine,
 	 * 			operBinEquiv = string representation of Operand.
 	 * 			and lc = instr.lc displayed in hexadecimal w/ 4 bits.
 	 * @author Josh Ventura
 	 * @date Apr 5, 2012; 9:37:34 PM
 	 * @modified Apr 7 2012; 12:39:44 AM: Corrected formatting for printing
 	 *           multiple operands (added a comma between them). -Josh
 	 *           Apr 17, 2012; 6:39:41 PM: changed definition of toString to breakdown of Instruction. -Noah.
 	 * @tested Apr 7 2012; 12:38:06 AM: Tested with basic MOVD instructions.
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @return Returns a string representation of Instruction.
 	 * @specRef N/A
 	 */
 	@Override 
 	public String toString() {
 		String rep = "";
 		rep = rep + "original source line: " + this.origSrcLine + "\n";
 
 		// opcode.
 		String binEquiv = IOFormat.formatBinInteger(this.getOpcode(), 6);
 		String lc = IOFormat.formatHexInteger(this.lc, 4);
 		String label = this.label;
 
 		// instr is a directive and thus has no opcode.
 		if (this.getOpcode() == 0xFFFFFFFF) {
 			binEquiv = "------"; // so binary equivalent is non-existant.
 		}
 
 		// if the instruction has no label
 		if (this.label == null) {
 			label = ""; // no label to print.
 			// also, can't print "------" b/c label may be "------".
 		}
 
 
 		String instrBreak = "Line number: " + this.lineNum + " " + "LC: "
 				+ lc + " " + "Label: " + label + ",\n"
 				+ "instruction/Directive: " + this.getOpId() + " "
 				+ "Binary Equivalent: " + binEquiv + "\n";
 
 		Iterator<Operand> operandIt = operands.iterator();
 
 		int i = 1;
 		while (operandIt.hasNext()) {
 			Operand oprnd = operandIt.next();
 
 			instrBreak = instrBreak + "Operand " + i + ": " + oprnd.operand
 					+ ":" + oprnd.expression + "\tBinary Equivalent: " + oprnd.toString() + "\n";
 
 			i++;
 		}
 
 		instrBreak = instrBreak + "\n";
 
 		rep = rep + instrBreak;
 		
 		return rep;
 	}
 
 	/**
 	 * Default constructor. The constructor is protected so that the parse()
 	 * method must be used externally to obtain an Instruction.
 	 */
 	protected Instruction() {}
 
 	/**
 	 * Default constructor. The constructor is protected so that the parse()
 	 * method must be used externally to obtain an Instruction.
 	 * 
 	 * @param iname
 	 *            The name of this instruction as returned by getOpId().
 	 * @param opcode
 	 *            The byte code for this instruction as returned by getOpCode().
 	 */
 	protected Instruction(String iname, int opcode) {
 		Assembler.instructions.put(iname, this);
 		Assembler.byteCodes.put(opcode, this);
 	}
 	
 	// ------------------------------------------------------------------
 	// --- Error Handling -----------------------------------------------
 	// ------------------------------------------------------------------
 	/** Any errors reported through our handler  */
	ArrayList<String> errors;
 	
 	/**
 	 * @author Josh Ventura
 	 * @date Apr 18, 2012; 3:13:22 AM
 	 * @param hErr The error handler which will be invoked in chain.
 	 * @return A wrapper to the error handler which also logs errors to this.
 	 */
 	public final ErrorHandler getHErr(ErrorHandler hErr) {
 		return new WrapHErr(hErr);
 	}
 	
 	/**
 	 * Wrapper class to an error handler that copies errors into this.
 	 * @author Josh Ventura
 	 * @date Apr 18, 2012; 3:10:59 AM
 	 */
 	class WrapHErr implements ErrorHandler {
 		/** The error handler we are intercepting messages to. */
 		private final ErrorHandler wrapped;
 		/**
 		 * Adds error to our list and calls wrapped instance.
 		 * 
 		 * @see assemblernator.ErrorReporting.ErrorHandler#reportError(java.lang.String, int, int)
 		 */
 		@Override public void reportError(String err, int line, int pos) {
 			errors.add("ERROR: " + err);
 			wrapped.reportError(err, line, pos);
 		}
 
 		/**
 		 * Adds warning to our list and calls wrapped instance.
 		 * 
 		 * @see assemblernator.ErrorReporting.ErrorHandler#reportWarning(java.lang.String, int, int)
 		 */
 		@Override public void reportWarning(String warn, int line, int pos) {
 			errors.add("Warning: " + warn);
 			wrapped.reportWarning(warn, line, pos);
 		}
 		
 		/**
 		 * @param hErr The error handler to wrap reports to.
 		 */
 		WrapHErr(ErrorHandler hErr) {
 			wrapped = hErr;
 		}
 	}
 }
