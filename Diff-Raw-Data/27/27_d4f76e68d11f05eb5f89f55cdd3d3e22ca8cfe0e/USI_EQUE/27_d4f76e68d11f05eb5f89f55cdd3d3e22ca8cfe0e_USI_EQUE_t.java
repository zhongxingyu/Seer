 package instructions;
 
 import static assemblernator.ErrorReporting.makeError;
 import assemblernator.ErrorReporting.ErrorHandler;
 import assemblernator.Instruction;
 import assemblernator.Module;
 
 /**
  * The EQUE instruction.
  * 
  * @author Generate.java
  * @date Apr 08, 2012; 08:26:19
  * @specRef D4
  */
 public class USI_EQUE extends UIG_Equated {
 	/**
 	 * The operation identifier of this instruction; while comments should not
 	 * be treated as an instruction, specification says they must be included in
 	 * the user report. Hence, we will simply give this class a semicolon as its
 	 * instruction ID.
 	 */
 	private static final String opId = "EQUE";
 
 	/** The static instance for this instruction. */
 	static USI_EQUE staticInstance = new USI_EQUE(true);
 
 	/** @see assemblernator.Instruction#immediateCheck(ErrorHandler, Module) */
 	@Override public boolean immediateCheck(ErrorHandler hErr, Module module) {
		boolean isValid = true;
		if (label == null) {
			hErr.reportError(makeError("noRQlabel", opId), lineNum, 0);
			isValid = false;
		}
 		if (operands.size() > 1) {
 			hErr.reportError(makeError("extraOperandsDir", opId), lineNum, -1);
 			return false;
 		}
 		if (operands.size() < 1) {
 			hErr.reportError(makeError("directiveMissingOp", opId, "EX"), lineNum, -1);
 			return false;
 		}
 		if (!operands.get(0).operand.equals("EX")) {
 			hErr.reportError(makeError("operandDirWrong", opId, operands.get(0).operand), lineNum, -1);
 			hErr.reportError(makeError("directiveMissingOp", opId, "EX"), lineNum, -1);
 			return false;
 		}
 		value = module.evaluate(operands.get(0).expression, false, hErr, this, operands.get(0).valueStartPosition);
		return isValid;
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
 
 	/** @see assemblernator.Instruction#getNewInstance() */
 	@Override public Instruction getNewInstance() {
 		return new USI_EQUE();
 	}
 
 	/**
 	 * Calls the Instance(String,int) constructor to track this instruction.
 	 * 
 	 * @param ignored
 	 *            Unused parameter; used to distinguish the constructor for the
 	 *            static instance.
 	 */
 	private USI_EQUE(boolean ignored) {
 		super(opId);
 		usage = Usage.EQUATE;
 	}
 
 	/** Default constructor; does nothing. */
 	private USI_EQUE() {}
 }
