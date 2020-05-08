 package assemblernator;
 
 import java.util.HashMap;
 
 /**
  * The Instruction class is both a descriptor class and a storage class. It
  * represents the main mechanisms for tokenizing, lexing, and assembling URBAN
  * code.
  * 
  * @author Josh Ventura
  * @date Apr 4, 2012 1:39:03 AM
  */
 public abstract class Instruction {
 	/**
 	 * @author Josh Ventura
 	 * @date Apr 4, 2012; 4:22:13 PM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @return Returns a pointer to the global instance of this instruction.
 	 * @specRef N/A
 	 */
 	public abstract Instruction getInstance();
 
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
 		LABEL
 	}
 
 	// =====================================================================
 	// == Members valid in the global instance, obtained with getInstance()
 	// =====================================================================
 
 	/** The identifier by which this tag is referenced */
 	String id;
 	/**
 	 * The byte code that identifies this instruction. It should be possible to
 	 * compare against this instruction by ANDing the complete instruction by
 	 * this opcode, and then comparing the result to this opcode.
 	 */
 	int opcode;
 
 	// =====================================================================
 	// == Members valid with new instances =================================
 	// =====================================================================
 
 	/** Any label that was given before this instruction. */
 	String label;
 	/** The line counter index at which this instruction was read. */
 	int lc;
 	/** A hash map of any instructions encountered. */
 	HashMap<String, String> operands;
	/** The type of this instruction as one of the {@link Usage} constants. */
	Usage usage;
 
 	/**
 	 * Parse a string containing the operands of an instruction, storing the
 	 * operands locally.
 	 * 
 	 * @author Josh Ventura
 	 * @date Apr 4, 2012; 1:40:21 AM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param line
 	 *            A String containing the line of code to be parsed for an
 	 *            Instruction.
 	 * @return A new Instruction as defined in the given line of URBAN-ASM code.
 	 */
 	public final Instruction parse(String line) {
 		// TODO: IMPLEMENT
 		return null;
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
	 * @return Returns an integer containing this instruction's byte code.
 	 */
 	public abstract int assemble();
 
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
 	 * Default constructor. The constructor is private so that the parse()
 	 * method must be used externally to obtain an Instruction.
 	 */
 	private Instruction() {
 	}
 }
