 package instructions;
 
 import simulanator.Deformatter;
 import simulanator.Machine;
 import simulanator.Deformatter.OpcodeBreakdown;
 import assemblernator.Instruction;
 import assemblernator.Module;
 
 /**
  * The CRKB instruction.
  * 
  * @author Generate.java
  * @date Apr 08, 2012; 08:26:19
  * @specRef IO3
  */
 public class USI_CRKB extends UIG_IO {
 	/**
 	 * The operation identifier of this instruction; while comments should not
 	 * be treated as an instruction, specification says they must be included in
 	 * the user report. Hence, we will simply give this class a semicolon as its
 	 * instruction ID.
 	 */
 	private static final String opId = "CRKB";
 
 	/** This instruction's identifying opcode. */
 	private static final int opCode = 0x2C; // 0b101100
 
 	/** The static instance for this instruction. */
 	static USI_CRKB staticInstance = new USI_CRKB(true);
 
 	/** @see assemblernator.Instruction#getNewLC(int, Module) */
 	@Override 
 	public int getNewLC(int lc, Module mod) {
 		return lc+1;
 	}
 
 	/** @see assemblernator.Instruction#execute(int, Machine) */
 	@Override 
 	public void execute(int instruction, Machine machine) {
 		OpcodeBreakdown breakDown = Deformatter.breakDownDestRange(instruction);
 		int nw = breakDown.numWords;
 		int word;
 		int addr = breakDown.getEffectiveDestAddress(machine);
 		String input;
 		for (int i = 0; i < nw; ++i) {
 			input = machine.input.getString();
			if(input.length() != 1) {
 				machine.hErr.reportError("runInputChar", machine.getLC(), -1);
 			}
 			word = input.charAt(0);
 			machine.setMemory(addr + i, word);
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
 	@Override public String getOpId() {
 		return opId;
 	}
 
 	/** @see assemblernator.Instruction#getOpcode() */
 	@Override public int getOpcode() {
 		return opCode;
 	}
 
 	/** @see assemblernator.Instruction#getNewInstance() */
 	@Override public Instruction getNewInstance() {
 		return new USI_CRKB();
 	}
 
 	/**
 	 * Calls the Instance(String,int) constructor to track this instruction.
 	 * 
 	 * @param ignored
 	 *            Unused parameter; used to distinguish the constructor for the
 	 *            static instance.
 	 */
 	private USI_CRKB(boolean ignored) {
 		super(opId, opCode);
 	}
 
 	/** Default constructor; does nothing. */
 	private USI_CRKB() {}
 }
 
