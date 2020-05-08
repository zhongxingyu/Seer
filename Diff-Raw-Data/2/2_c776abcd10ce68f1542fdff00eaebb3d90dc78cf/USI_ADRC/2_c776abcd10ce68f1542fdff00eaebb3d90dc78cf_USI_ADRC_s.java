 package instructions;
 
 import static assemblernator.ErrorReporting.makeError;
 import static assemblernator.OperandChecker.isValidMem;
 import assemblernator.AbstractDirective;
 import assemblernator.ErrorReporting.ErrorHandler;
 import assemblernator.Instruction;
 import assemblernator.Module;
 import assemblernator.Module.Value;
 import assemblernator.Module.Value.BitLocation;
 
 /**
  * The ADRC instruction.
  * 
  * @author Generate.java
  * @date Apr 08, 2012; 08:26:19
  * @specRef D12
  */
 public class USI_ADRC extends AbstractDirective {
 	/**
 	 * The operation identifier of this instruction; while comments should not
 	 * be treated as an instruction, specification says they must be included in
 	 * the user report. Hence, we will simply give this class a semicolon as its
 	 * instruction ID.
 	 */
 	private static final String opId = "ADRC";
 
 	/** The static instance for this instruction. */
 	static USI_ADRC staticInstance = new USI_ADRC(true);
 
 	/** @see assemblernator.Instruction#getNewLC(int, Module) */
 	@Override public int getNewLC(int lc, Module mod) {
 		return lc+1;
 	}
 
 	/** @see assemblernator.Instruction#check(ErrorHandler, Module) */
 	@Override public boolean check(ErrorHandler hErr, Module module) {
 		boolean isValid = true;
 		Value val;
 		if(this.hasOperand("LR")) {
 			val = module.evaluate(this.getOperand("LR"), true, BitLocation.Address, hErr, this, this.getOperandData("LR").valueStartPosition);
 			this.getOperandData("LR").value = val;
 			isValid = isValidMem(val.value);
 			if(!isValid) hErr.reportError(makeError("OORmemAddr", "LR", this.getOpId()), this.lineNum, -1);
 		} else if(this.hasOperand("EX")) {
 			val = module.evaluate(this.getOperand("EX"), true, BitLocation.Address, hErr, this, this.getOperandData("EX").valueStartPosition);
 			this.getOperandData("EX").value = val;
 			isValid = isValidMem(val.value);
 			if(!isValid) hErr.reportError(makeError("OORmemAddr", "EX", this.getOpId()), this.lineNum, -1);
 		} else {
 			isValid = false;
 			hErr.reportError(makeError("directiveMissingOp2", this.getOpId(), "LR", "EX"), this.lineNum, -1);
 		}
		return false; 
 	}
 
 	/** @see assemblernator.Instruction#assemble() */
 	@Override public int[] assemble() {
 		int [] assembled = new int[1];
 		if(this.hasOperand("LR"))
 			assembled[0] = this.getOperandData("LR").value.value;
 		else if(this.hasOperand("EX"))
 			assembled[0] = this.getOperandData("EX").value.value;
 		else
 			assembled[0] = 0xDEADBEEF;
 		
 		return assembled;
 	}
 
 	/** @see assemblernator.Instruction#immediateCheck(assemblernator.ErrorReporting.ErrorHandler, Module) */
 	@Override public boolean immediateCheck(ErrorHandler hErr, Module module) {
 		boolean isValid = true;
 		
 		if(this.operands.size() > 1) {
 			isValid = false;
 			hErr.reportError(makeError("extraOperandsDir", this.getOpId()), this.lineNum, -1);
 		} else if(this.operands.size() < 1){
 			isValid = false;
 			hErr.reportError(makeError("tooFewOperandsDir", this.getOpId()), this.lineNum, -1);
 		} else if(this.hasOperand("LR")) {
 			Operand lr = getOperandData("LR");
 			lr.value = module.evaluate(lr.expression, true, BitLocation.Address, hErr, this, lr.valueStartPosition);
 		} else {
 			Operand ex = getOperandData("EX");
 			if (ex == null)
 				hErr.reportError(makeError("directiveMissingOp", opId, "EX"), lineNum, 0);
 			else
 				ex.value = module.evaluate(ex.expression, true, BitLocation.Address, hErr, this, ex.valueStartPosition);
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
 		return new USI_ADRC();
 	}
 
 	/**
 	 * Calls the Instance(String,int) constructor to track this instruction.
 	 * 
 	 * @param ignored
 	 *            Unused parameter; used to distinguish the constructor for the
 	 *            static instance.
 	 */
 	private USI_ADRC(boolean ignored) {
 		super(opId);
 	}
 
 	/** Default constructor; does nothing. */
 	private USI_ADRC() {}
 }
 
