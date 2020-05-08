 package instructions;
 
 import simulanator.Deformatter;
 import simulanator.Machine;
 import simulanator.Deformatter.OpcodeBreakdown;
 import assemblernator.Instruction;
 import assemblernator.Module;
 
 /**
  * The IRKB instruction.
  * 
  * @author Generate.java
  * @date Apr 08, 2012; 08:26:19
  * @specRef IO1
  */
 public class USI_IRKB extends UIG_IO {
 	/**
 	 * The operation identifier of this instruction; while comments should not
 	 * be treated as an instruction, specification says they must be included in
 	 * the user report. Hence, we will simply give this class a semicolon as its
 	 * instruction ID.
 	 */
 	private static final String opId = "IRKB";
 
 	/** This instruction's identifying opcode. */
 	private static final int opCode = 0x29; // 0b101001
 
 	/** The static instance for this instruction. */
 	static USI_IRKB staticInstance = new USI_IRKB(true);
 
 	/** @see assemblernator.Instruction#getNewLC(int, Module) */
 	@Override public int getNewLC(int lc, Module mod) {
 		return lc+1;
 	}
 
 	/** @see assemblernator.Instruction#execute(int, Machine) */
 	@Override public void execute(int instruction, Machine machine) {
 		OpcodeBreakdown breakDown = Deformatter.breakDownSrcRange(instruction);
		int addr = breakDown.getEffectiveDestAddress(machine);
 		int nw = breakDown.numWords;
 		int word;
 		for (int i = 0; i < nw; ++i) {
 			word = Integer.parseInt(machine.input.getString());
			machine.setMemory(addr+ i, word);
 			breakDown.putToDest(word, machine);
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
 		return new USI_IRKB();
 	}
 
 	/**
 	 * Calls the Instance(String,int) constructor to track this instruction.
 	 * 
 	 * @param ignored
 	 *            Unused parameter; used to distinguish the constructor for the
 	 *            static instance.
 	 */
 	private USI_IRKB(boolean ignored) {
 		super(opId, opCode);
 	}
 
 	/** Default constructor; does nothing. */
 	private USI_IRKB() {}
 }
 
