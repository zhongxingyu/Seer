 package instructions;
 
 import static assemblernator.ErrorReporting.makeError;
 import simulanator.Deformatter;
 import simulanator.Deformatter.OpcodeBreakdown;
 import simulanator.Machine;
 import assemblernator.Instruction;
 import assemblernator.Module;
 
 /**
  * The CWSR instruction.
  * 
  * @author Generate.java
  * @date Apr 08, 2012; 08:26:19
  * @specRef IO2
  */
 public class USI_CWSR extends UIG_IO {
 	/**
 	 * The operation identifier of this instruction; while comments should not
 	 * be treated as an instruction, specification says they must be included in
 	 * the user report. Hence, we will simply give this class a semicolon as its
 	 * instruction ID.
 	 */
 	private static final String opId = "CWSR";
 
 	/** This instruction's identifying opcode. */
 	private static final int opCode = 0x2A; // 0b101010
 
 	/** The static instance for this instruction. */
 	static USI_CWSR staticInstance = new USI_CWSR(true);
 
 	/** @see assemblernator.Instruction#getNewLC(int, Module) */
 	@Override public int getNewLC(int lc, Module mod) {
 		return lc + 1;
 	}
 
 	/** @see assemblernator.Instruction#execute(int, Machine) */
 	@Override public void execute(int instruction, Machine machine) {
 		OpcodeBreakdown breakDown = Deformatter.breakDownSrcRange(instruction);
 		int addr = breakDown.getEffectiveSrcAddress(machine);
 		int nw = breakDown.numWords;
 		byte[] b = new byte[nw << 2];
 		for (int i = 0; i < nw; ++i) {
 			if(addr + i > 4095) {
				machine.hErr.reportError(makeError("runMemOOR"), machine.getLC(), -1);
 				break; //if address to read from is out of range stop reading.
 			}
 			int a = machine.getMemory(addr + i);
 			b[(i << 2) + 0] = ((byte) ((a & 0xFF000000) >>> 24));
 			b[(i << 2) + 1] = ((byte) ((a & 0x00FF0000) >>> 16));
 			b[(i << 2) + 2] = ((byte) ((a & 0x0000FF00) >>> 8));
 			b[(i << 2) + 3] = ((byte) ((a & 0x000000FF) >>> 0));
 		}
 		machine.output.putString(new String(b));
 	}
 
 	// =========================================================
 	// === Redundant code ======================================
 	// =========================================================
 	// === This code's the same in all instruction classes, ====
 	// === But Java lacks the mechanism to allow stuffing it ===
 	// === in super() where it belongs. ========================
 	// =========================================================
 
 	/**
 	 * @see Instruction
 	 * @return The static instance of this instruction.
 	 */
 	public static Instruction getInstance() {
 		return staticInstance;
 	}
 
 	/** @see assemblernator.Instruction#getOpId() */
 	@Override public String getOpId() {
 		return opId;
 	}
 
 	/** @see assemblernator.Instruction#getOpcode() */
 	@Override public int getOpcode() {
 		return opCode;
 	}
 
 	/** @see assemblernator.Instruction#getNewInstance() */
 	@Override public Instruction getNewInstance() {
 		return new USI_CWSR();
 	}
 
 	/**
 	 * Calls the Instance(String,int) constructor to track this instruction.
 	 * 
 	 * @param ignored
 	 *            Unused parameter; used to distinguish the constructor for the
 	 *            static instance.
 	 */
 	private USI_CWSR(boolean ignored) {
 		super(opId, opCode);
 	}
 
 	/** Default constructor; does nothing. */
 	private USI_CWSR() {}
 }
