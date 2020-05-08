 package assemblernator;
 
 import instructions.Comment;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
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
 	enum Usage {
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
 	 * Get the number of words this instruction will consume; useful for
 	 * incrementing the location counter. Instructions such as EQU will have a
 	 * word count of zero, while most instructions will have a word count of
 	 * one. The CHAR instruction will return the number of words required to
 	 * contain the given string literal.
 	 * 
 	 * @author Josh Ventura
 	 * @date Apr 5, 2012; 11:36:32 PM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @return The number of words this instruction requires.
 	 * @specRef N/A
 	 */
 	abstract public int getWordCount();
 
 	// =====================================================================
 	// == Members valid with new instances =================================
 	// =====================================================================
 
 	/** Any label that was given before this instruction. */
 	public String label;
 	/** The line counter index at which this instruction was read. */
 	public int lc;
 	/** A hash map of any instructions encountered. */
 	public HashMap<String, String> operands = new HashMap<String, String>();
 	/** The type of this instruction as one of the {@link Usage} constants. */
 	public Usage usage;
 
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
 	 * @throws Exception
 	 *             Throws a general exception when a syntax error occurs.
 	 * @throws IOException
 	 *             Throws an IOException with the message "RAL" when the
 	 *             instruction is not concluded in the given string. The caller
 	 *             should re-invoke this method with the original parameter
 	 *             suffixed with the next line in the file.
 	 * @specRef S2.1
 	 * @specRef S4.1
 	 */
 	public static final Instruction parse(String line) throws Exception,
 			IOException {
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
 			if (i != 0)// @formatter:off
 				throw new Exception("URBAN specification says labels must start at column 0 ("
 						+ label + " is " + (IOFormat.isAlpha(label) ? "not an" : "an invalid")
 						+ " instruction, starts at column" + i + ")"); // @formatter:on
 
 			// Skip the label we read earlier
 			i += label.length();
 
 			do { // Skip whitespace between label and instruction
 				if (++i > line.length())
 					throw new IOException("RAL1");
 			} while (Character.isWhitespace(line.charAt(i)));
 
 			instruction = IOFormat.readLabel(line, i); // Read instruction now
 			if (!Assembler.instructions.containsKey(instruction.toUpperCase())) {
 				if (IOFormat.isAlpha(instruction))
 					throw new Exception("`" + instruction
 							+ "' is not a known instruction");
 				throw new Exception("`" + instruction
 						+ "' is not a valid instruction");
 			}
 			i += instruction.length();
 		}
 
 		if (i >= line.length())
 			throw new IOException("RAL2"); // Request another line
 
 		// So, now we know a bit about our instruction.
 		Instruction res;
 		res = Assembler.instructions.get(instruction.toUpperCase())
 				.getNewInstance();
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
 				throw new Exception("Expected operand keyword before '"
 						+ line.charAt(i) + "'");
 
 			// Isolate the keyword
 			final int sp = i;
 			while (Character.isLetter(line.charAt(++i)));
 			String operand = line.substring(sp, i);
			if (!Assembler.keyWords.contains(operand))
 				throw new Exception("Unrecognized operand keyword `" + operand
 						+ "'");
 
 			if (line.charAt(i) != ':')
 				throw new Exception("Expected colon following `" + operand
 						+ "' keyword");
 
 			final int exsp = i + 1;
 			do { // Now we're reading in the value of this expression
 				if (++i >= line.length()) // If we overrun this line looking,
 					throw new IOException("RAL4"); // Request another line.
 
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
 						throw new IOException("RAL5");
 				}
 			} while (line.charAt(i) != ';' && line.charAt(i) != ',');
 
 			String exp = line.substring(exsp, i);
			res.operands.put(operand, exp);
 
 			//
 			if (line.charAt(i) == ',')//@formatter:off
 				do if (++i >= line.length()) throw new IOException("RAL6");
 			while (Character.isWhitespace(line.charAt(i))); //@formatter:on
 		}
 
 		return res;
 	}
 
 	/**
 	 * Check if the token is semantically correct. This means that it has the
 	 * correct number of operands and the correct kinds of operands.
 	 * 
 	 * @author Josh Ventura
 	 * @return Returns whether the instruction is semantically correct.
 	 * @date Apr 4, 2012; 01:40:29AM
 	 */
 	public abstract boolean check();
 
 	/**
 	 * Assemble this instruction to byte code after it has been checked.
 	 * 
 	 * @author Josh Ventura
 	 * @date Apr 4, 2012; 1:40:52 AM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @return Returns an array of getWordCount() integers representing this
 	 *         instruction's byte code.
 	 */
 	public abstract int[] assemble();
 
 	/**
 	 * @author Josh Ventura
 	 * @date Apr 4, 2012; 9:11:51 AM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param instruction
 	 *            The byte code of the instruction to be executed.
 	 */
 	public abstract void execute(int instruction);
 
 	/**
 	 * Constructs a string representation of the instruction, as close to the
 	 * original code as reasonably possible.
 	 * 
 	 * @author Josh Ventura
 	 * @date Apr 5, 2012; 9:37:34 PM
 	 * @modified Apr 7 2012; 12:39:44 AM: Corrected formatting for printing
 	 *           multiple operands (added a comma between them).
 	 * @tested Apr 7 2012; 12:38:06 AM: Tested with basic MOVD instructions.
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @return Returns a semantic match to the original instruction code string.
 	 * @specRef N/A
 	 */
 	@Override public String toString() {
 		String res = (label.length() > 0 ? label + "\t" : "") + getOpId();
 		boolean prent = false;
 		for (Entry<String, String> op : operands.entrySet()) {
 			res += (prent ? ",\t" : "\t") + op.getKey() + ":" + op.getValue();
 			prent = true;
 		}
 		return res + ';';
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
 		System.out.println("Registered `" + iname + "' instruction");
 		Assembler.instructions.put(iname, this);
 		Assembler.byteCodes.put(opcode, this);
 	}
 }
