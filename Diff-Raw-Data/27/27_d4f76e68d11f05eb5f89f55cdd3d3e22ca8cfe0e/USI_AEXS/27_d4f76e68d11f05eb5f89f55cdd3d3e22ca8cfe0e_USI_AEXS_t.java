 package instructions;
 
 import static assemblernator.ErrorReporting.makeError;
 import assemblernator.AbstractDirective;
 import assemblernator.ErrorReporting.ErrorHandler;
 import assemblernator.IOFormat;
 import assemblernator.Instruction;
 import assemblernator.Module;
 import assemblernator.OperandChecker;
 
 /**
  * The AEXS instruction.
  * 
  * @author Generate.java
  * @date Apr 08, 2012; 08:26:19
  * @specRef D8
  */
 public class USI_AEXS extends AbstractDirective {
 	/**
 	 * The operation identifier of this instruction; while comments should not
 	 * be treated as an instruction, specification says they must be included in
 	 * the user report. Hence, we will simply give this class a semicolon as its
 	 * instruction ID.
 	 */
 	private static final String opId = "AEXS";
 
 	/** The static instance for this instruction. */
 	static USI_AEXS staticInstance = new USI_AEXS(true);
 
 	/** @see assemblernator.Instruction#getNewLC(int, Module) */
 	@Override public int getNewLC(int lc, Module mod) {
 		return lc;
 	}
 
 	/** @see assemblernator.Instruction#check(ErrorHandler, Module) */
 	@Override public boolean check(ErrorHandler hErr, Module module) {
 		return true;
 	}
 
 	/** @see assemblernator.Instruction#assemble() */
 	@Override public int[] assemble() {
 		return null; // TODO: IMPLEMENT
 	}
 
 	/** @see assemblernator.Instruction#immediateCheck(assemblernator.ErrorReporting.ErrorHandler, Module) */
 	@Override public boolean immediateCheck(ErrorHandler hErr, Module module) {
 		boolean isValid = true;
 		int value;
 		
 		if(this.operands.size() > 1) {
 			isValid = false;
 			hErr.reportError(makeError("extraOperandsDir", opId), lineNum, -1);
 		} else if (this.operands.size() < 1) {
 			isValid = false;
 			hErr.reportError(makeError("directiveMissingOp2", opId, "FC", "LR"), lineNum, -1);
 		} else if(this.hasOperand("LR")){
 			value = module.evaluate(this.getOperand("LR"), false, hErr, this, this.getOperandData("LR").keywordStartPosition); 
 			if(!IOFormat.isValidLabel(this.getOperand("LR", 0))){
 				hErr.reportError(makeError("OORlabel", "LR", this.getOpId()), this.lineNum, getOperandData("LR",0).valueStartPosition);
 				isValid = false;
 			}
 		} else if(this.hasOperand("FC")){
 			value = module.evaluate(this.getOperand("FC"), false, hErr, this, this.getOperandData("FC").keywordStartPosition); 
 			isValid = OperandChecker.isValidConstant(value, ConstantRange.RANGE_ADDR); //check if value of FC is valid.
 			if(!isValid) hErr.reportError(makeError("OORconstant", "FC", 
 					this.getOpId(), Integer.toString(ConstantRange.RANGE_ADDR.min), Integer.toString(ConstantRange.RANGE_ADDR.max)), this.lineNum, -1);
 		} else {
 			isValid = false;
			if(!isValid) hErr.reportError(makeError("directiveMissingOp", this.getOpId(), "FC' or `LR"), this.lineNum, -1);
 		}
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
 		return new USI_AEXS();
 	}
 
 	/**
 	 * Calls the Instance(String,int) constructor to track this instruction.
 	 * 
 	 * @param ignored
 	 *            Unused parameter; used to distinguish the constructor for the
 	 *            static instance.
 	 */
 	private USI_AEXS(boolean ignored) {
 		super(opId);
 	}
 
 	/** Default constructor; does nothing. */
 	private USI_AEXS() {}
 }
 
