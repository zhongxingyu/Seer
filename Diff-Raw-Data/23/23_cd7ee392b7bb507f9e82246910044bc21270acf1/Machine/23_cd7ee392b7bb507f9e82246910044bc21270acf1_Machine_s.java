 package simulanator;
 
 import static assemblernator.ErrorReporting.makeError;
 
 import java.util.Stack;
 
 import assemblernator.Assembler;
 import assemblernator.ErrorReporting.ErrorHandler;
 import assemblernator.IOFormat;
 import assemblernator.Instruction;
 
 /**
  * A class representing an entire machine state.
  * 
  * @author Josh Ventura
  * @date Apr 28, 2012; 1:09:45 AM
  */
 public class Machine {
 	/** The size of the memory of the URBAN machine. */
 	public static final int memorySizeInWords = 4096;
 
 	/** Our eight machine registers. */
 	private int[] registers;
 	/** Our seven index registers: Index register 0 is unused. */
 	private int[] indexRegisters;
 	/** Our entire memory; all 16 kibibytes of it. */
 	private int[] memory;
 	/** The current program counter. */
 	private int lc;
 	/** The current instruction. */
 	public int instruction;
 
 	/** Stack of integers. */
 	public Stack<Integer> stack;
 
 	/** Interface to handle input. */
 	public interface URBANInputStream {
 		/**
 		 * Prompt the user for a string, blocking until the user submits it.
 		 * 
 		 * @return A string given by the user.
 		 */
 		String getString();
 	}
 
 	/** Interface to handle input. */
 	public interface URBANOutputStream {
 		/**
 		 * Display a string to the user.
 		 * 
 		 * @param str
 		 *            The string to display.
 		 */
 		void putString(String str);
 	}
 
 	// ========================================================================
 	// === MACHINE STREAMS ====================================================
 	// ========================================================================
 
 	/** An error channel to which any access violations can be reported. */
 	ErrorHandler hErr;
 	/** Out input stream, through which the user will be prompted for input. */
 	URBANInputStream input;
 	/** An output stream to which messages can be printed. */
 	URBANOutputStream output;
 
 	// ========================================================================
 	// === MACHINE STATE ======================================================
 	// ========================================================================
 	/** True if the machine is still running. */
 	boolean running;
 
 	/**
 	 * @param err
 	 *            The error handler to which any problems are reported.
 	 * @param uis
 	 *            The URBAN input stream for this machine.
 	 * @param uos
 	 *            The URBAN output stream for this machine.
 	 */
 	public Machine(ErrorHandler err, URBANInputStream uis, URBANOutputStream uos) {
 		hErr = err;
 		input = uis;
 		output = uos;
 		registers = new int[8];
 		indexRegisters = new int[8];
 		memory = new int[memorySizeInWords];
 	}
 
 	/**
 	 * Fork constructor, for use in SYS_FORK.
 	 * 
 	 * @param forkFrom
 	 *            The machine to be forked.
 	 */
 	public Machine(Machine forkFrom) {
 		hErr = forkFrom.hErr;
 		input = forkFrom.input;
 		output = forkFrom.output;
 		registers = forkFrom.registers;
 		indexRegisters = forkFrom.indexRegisters;
 		memory = forkFrom.memory;
 	}
 
 	/**
 	 * Creates a string dump of this machine state, according to spec.
 	 * 
 	 * @author Josh Ventura
 	 * @date Apr 28, 2012; 1:23:42 AM
 	 * @modified UNMODIFIED
 	 * @tested Apr 28, 2012; 1:41:00 AM: Tested with empty memory.
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param level
 	 *            The detail level for this dump; a value of one dumps all
 	 *            registers, a value of two dumps the memory, and a value of
 	 *            three dumps both.
 	 * @return A String containing the dump of this machine state.
 	 * @specRef C1
 	 * @specRef DUMP Format
 	 */
 	public String dump(int level) {
 		String res = "";
 		if ((level & 1) != 0) {
 			res += "LC " + getLC() + "  WORD="
 					+ IOFormat.formatHexInteger(instruction, 8) + "\n";
 			for (int i = 0; i < 8; ++i)
 				res += "R" + i + "="
 						+ IOFormat.formatHexInteger(registers[i], 8)
 						+ (i % 4 == 3 ? "\n" : " ");
 			res += "\n";
 		}
 		if ((level & 2) != 0) {
 			for (int row = 0; row < memorySizeInWords; row += 8) {
 				res += IOFormat.formatHexInteger(row, 4) + ": ";
 				for (int col = row; col < row + 8 && col < memorySizeInWords; ++col)
 					res += " " + IOFormat.formatHexInteger(memory[col], 8);
 				res += "\n";
 			}
 		}
 		return res;
 	}
 
 	/** @see java.lang.Object#toString() */
 	@Override public String toString() {
 		return dump(3);
 	}
 
 	/**
 	 * @author Josh Ventura
 	 * @date May 18, 2012; 7:50:40 PM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param execStart
 	 *            The starting LC from which to run.
 	 */
 	public void runThread(int execStart) {
 		setLC(execStart);
 		while (running) {
			if (lc < 0 || lc > memory.length) {
 				hErr.reportError(
 						makeError("runLCwentOOR", Integer.toString(lc, 16)),
 						-1, -1);
 				running = false;
 				break;
 			}
 			instruction = memory[lc++];
 			Integer opcode = (instruction & 0xFC000000) >> 26;
 			Instruction ins = Assembler.byteCodes.get(opcode);
 			if (ins == null) {
 				hErr.reportError(
 						makeError("InvOpCode",
 								IOFormat.formatBinInteger(opcode, 6)), -1, -1);
 			}
 		}
 	}
 
 	/**
 	 * @param addr
 	 *            The address of the desired memory word.
 	 * @return The word at the given address.
 	 */
 	public int getMemory(int addr) {
 		return memory[addr];
 	}
 
 	/**
 	 * Standard setter; fires memory change.
 	 * 
 	 * @param addr
 	 *            The address of the desired memory word.
 	 * @param word
 	 *            The new word to place at that address.
 	 */
 	public void setMemory(int addr, int word) {
 		memory[addr] = word;
 	}
 
 	/** @return The current location counter. */
 	public int getLC() {
 		return lc;
 	}
 
 	/**
 	 * Standard setter; fires location counter change.
 	 * 
 	 * @param lc
 	 *            The new location counter.
 	 */
 	public void setLC(int lc) {
 		this.lc = lc;
 	}
 
 	/**
 	 * Standard setter; fires register change.
 	 * 
 	 * @param register
 	 *            The ID of the register to set, 0-7.
 	 * @param word
 	 *            The new value for the register.
 	 */
 	public void setRegister(int register, int word) {
 		registers[register] = word;
 	}
 
 	/**
 	 * Standard setter; fires register change.
 	 * 
 	 * @param register
 	 *            The ID of the index register to set, 1-7.
 	 * @param word
 	 *            The new value for the register.
 	 */
 	public void setIndexRegister(int register, int word) {
 		indexRegisters[register] = word;
 	}
 
 	/**
 	 * @param reg
 	 *            The register to get.
 	 * @return The value of the index register with the given ID.
 	 */
 	public int getIndexRegister(int reg) {
 		return indexRegisters[reg];
 	}
 
 	/**
 	 * @param reg
 	 *            The register to get.
 	 * @return The value of the index register with the given ID.
 	 */
 	public int getRegister(int reg) {
 		return registers[reg];
 	}
 }
