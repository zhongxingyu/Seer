 package instructions;
 
 import assemblernator.ErrorReporting.ErrorHandler;
import static assemblernator.ErrorReporting.makeError;
 import assemblernator.Instruction;
 import assemblernator.Module;
import assemblernator.OperandChecker;
 
 /**
  * The KICKO instruction.
  * 
  * @author Generate.java
  * @date Apr 08, 2012; 08:26:19
  * @specRef D1
  */
 public class USI_KICKO extends Instruction {
 	/**
 	 * The operation identifier of this instruction; while comments should not
 	 * be treated as an instruction, specification says they must be included in
 	 * the user report. Hence, we will simply give this class a semicolon as its
 	 * instruction ID.
 	 */
 	private static final String opId = "KICKO";
 
 	/** This instruction's identifying opcode. */
 	private static final int opCode = 0xFFFFFFFF; // This instruction doesn't have an opcode.
 
 	/** The static instance for this instruction. */
 	static USI_KICKO staticInstance = new USI_KICKO(true);
 
 	/** @see assemblernator.Instruction#getNewLC(int, Module) */
 	@Override public int getNewLC(int lc, Module mod) {
 		int ownLC = mod.evaluate(getOperand("FC"));
 		this.lc = ownLC;
 		return ownLC;
 	}
 
 	/** @see assemblernator.Instruction#check(ErrorHandler) */
 	@Override public boolean check(ErrorHandler hErr) {
		boolean isValid = true;
		
		if (this.lineNum != 0) { //KICKO's should only be found at beginning of source.
			isValid = false;
			hErr.reportError(makeError("KICKOlineNum"), this.lineNum, -1);
		} else if (!this.hasOperand("FC")) { //KICKO must have FC operand.
			isValid = false;
			hErr.reportError(makeError("directiveMissingOp", this.getOpId(), "FC"), this.lineNum, -1);
		} else if (this.operands.size() > 1) { //KICKO can only have FC operand.
			isValid = false;
			hErr.reportError(makeError("extraOperandsDir", this.getOpId()), this.lineNum, -1);
		} else if (!OperandChecker.isValidMem(this.getOperand("FC"))){
			isValid = false;
			hErr.reportError(makeError("OORmemAddr", "FC", this.getOpId()), this.lineNum, -1);
		}

		return isValid;
 	}
 
 	/** @see assemblernator.Instruction#assemble() */
 	@Override public int[] assemble() {
 		return null; // TODO: IMPLEMENT
 	}
 
 	/** @see assemblernator.Instruction#execute(int) */
 	@Override public void execute(int instruction) {
 		// TODO: IMPLEMENT
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
 		return new USI_KICKO();
 	}
 
 	/**
 	 * Calls the Instance(String,int) constructor to track this instruction.
 	 * 
 	 * @param ignored
 	 *            Unused parameter; used to distinguish the constructor for the
 	 *            static instance.
 	 */
 	private USI_KICKO(boolean ignored) {
 		super(opId, opCode);
 		usage = Usage.PROGNAME;
 	}
 
 	/** Default constructor; does nothing. */
 	private USI_KICKO() {}
 }
 
