 package instructions;
 
 import static assemblernator.ErrorReporting.makeError;
 import simulanator.Deformatter;
 import simulanator.Machine;
 import simulanator.Deformatter.OpcodeBreakdown;
 import assemblernator.Instruction;
 import assemblernator.Module;
 
 /**
  * The IWSR instruction.
  * 
  * @author Generate.java
  * @date Apr 08, 2012; 08:26:19
  * @specRef IO0
  */
 public class USI_IWSR extends UIG_IO {
 	/**
 	 * The operation identifier of this instruction; while comments should not
 	 * be treated as an instruction, specification says they must be included in
 	 * the user report. Hence, we will simply give this class a semicolon as its
 	 * instruction ID.
 	 */
 	private static final String opId = "IWSR";
 
 	/** This instruction's identifying opcode. */
 	private static final int opCode = 0x28; // 0b101000
 
 	/** The static instance for this instruction. */
 	static USI_IWSR staticInstance = new USI_IWSR(true);
 
 	/** @see assemblernator.Instruction#getNewLC(int, Module) */
 	@Override 
 	public int getNewLC(int lc, Module mod) {
 		return lc+1;
 	}
 
 	/** @see assemblernator.Instruction#execute(int, Machine) */
 	@Override 
 	public void execute(int instruction, Machine machine) {
 		OpcodeBreakdown breakDown = Deformatter.breakDownSrcRange(instruction);
 		int nw = breakDown.numWords;
 		String outContent;
 		for (int i = 0; i < nw; ++i) {
 			if(breakDown.literal) {
 				int val = breakDown.source;
 				val <<= 16; 
 				val >>= 16; //convert 16 bit source to 32 bit.
 				outContent = Integer.toString(val);
 			} else {
 				int addr = breakDown.getEffectiveSrcAddress(machine);
 				if(addr + i > 4095) {
					machine.hErr.reportError(makeError("runMemOOR", Integer.toString(addr + i)), machine.getLC(), -1);
 					return; //if address to read from is out of range stop reading.
 				}
 				outContent = Integer.toString(machine.getMemory(addr + i));
 			}
 			machine.output.putString(outContent);
 		}
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
 	@Override 
 	public String getOpId() {
 		return opId;
 	}
 
 	/** @see assemblernator.Instruction#getOpcode() */
 	@Override 
 	public int getOpcode() {
 		return opCode;
 	}
 
 	/** @see assemblernator.Instruction#getNewInstance() */
 	@Override 
 	public Instruction getNewInstance() {
 		return new USI_IWSR();
 	}
 
 	/**
 	 * Calls the Instance(String,int) constructor to track this instruction.
 	 * 
 	 * @param ignored
 	 *            Unused parameter; used to distinguish the constructor for the
 	 *            static instance.
 	 */
 	private USI_IWSR(boolean ignored) {
 		super(opId, opCode);
 	}
 
 	/** Default constructor; does nothing. */
 	private USI_IWSR() {}
 }
 
