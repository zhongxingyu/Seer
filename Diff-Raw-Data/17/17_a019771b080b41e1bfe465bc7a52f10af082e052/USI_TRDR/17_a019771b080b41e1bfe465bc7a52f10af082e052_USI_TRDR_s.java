 package instructions;
 
 import static assemblernator.ErrorReporting.makeError;
 import assemblernator.ErrorReporting.ErrorHandler;
 import assemblernator.Instruction;
 import assemblernator.Module;
 import assemblernator.OperandChecker;
 
 /**
  * The TRDR instruction.
  * 
  * @author Generate.java
  * @date Apr 08, 2012; 08:26:19
  * @specRef JT4
  */
 public class USI_TRDR extends Instruction {
 	/**
 	 * The operation identifier of this instruction; while comments should not
 	 * be treated as an instruction, specification says they must be included in
 	 * the user report. Hence, we will simply give this class a semicolon as its
 	 * instruction ID.
 	 */
 	private static final String opId = "TRDR";
 
 	/** This instruction's identifying opcode. */
 	private static final int opCode = 0x00000024; // 0b10010000000000000000000000000000
 
 	/** The static instance for this instruction. */
 	static USI_TRDR staticInstance = new USI_TRDR(true);
 
 	/** @see assemblernator.Instruction#getNewLC(int, Module) */
 	@Override public int getNewLC(int lc, Module mod) {
 		return lc+1;
 	}
 	String dest = "";
 	String src = "";
 	/** @see assemblernator.Instruction#check(ErrorHandler) */
 	@Override public boolean check(ErrorHandler hErr) {
 		boolean isValid = true;
 		//not enough operand check
 		if(this.operands.size() < 2){
 			isValid=false;
 			hErr.reportError(makeError("instructionMissingOp", this.getOpId(), ""), this.lineNum, -1);
 			//checks combos for 2 operands
 		}else if(this.operands.size() == 2){
 			if(this.hasOperand("DM")){
 				dest="DM";
 				//range check
 				isValid = OperandChecker.isValidMem(this.getOperand("DM"));
 				if(!isValid) hErr.reportError(makeError("OORmemAddr", "DM", this.getOpId()), this.lineNum, -1);
 				if(this.hasOperand("FR")){
 					src="FR";
 					//range checking
 					isValid = OperandChecker.isValidReg(this.getOperand("FR"));
 					if(!isValid) hErr.reportError(makeError("OORidxReg", "FR", this.getOpId()), this.lineNum, -1);
 				}else if(this.hasOperand("FX")){
 					src="FX";
 					//range checking
 					isValid = OperandChecker.isValidIndex(this.getOperand("FX"));
 					if(!isValid) hErr.reportError(makeError("OORidxReg", "FX", this.getOpId()), this.lineNum, -1);
 				}else{
 					isValid=false;
 					hErr.reportError(makeError("instructionMissingOp", this.getOpId(), "FR or FX"), this.lineNum, -1);
 				}
 			}else{
 				isValid=false;
 				hErr.reportError(makeError("instructionMissingOp", this.getOpId(), "DM"), this.lineNum, -1);
 			}
 			//checks combos for 3 operands
 		}else if(this.operands.size() == 3){
 			if(this.hasOperand("DX") && this.hasOperand("DM")){
 				dest="DMDX";
 				//range check
 				isValid = OperandChecker.isValidIndex(this.getOperand("DX"));
 				if(!isValid) hErr.reportError(makeError("OORidxReg", "DX", this.getOpId()), this.lineNum, -1);
 				isValid = OperandChecker.isValidMem(this.getOperand("DM"));
 				if(!isValid) hErr.reportError(makeError("OORmemAddr", "DM", this.getOpId()), this.lineNum, -1);
 				if(this.hasOperand("FR")){
 					src="FR";
 					//range checking
 					isValid = OperandChecker.isValidReg(this.getOperand("FR"));
 					if(!isValid) hErr.reportError(makeError("OORidxReg", "FR", this.getOpId()), this.lineNum, -1);
 				}else if(this.hasOperand("FX")){
 					src="FX";
 					//range checking
 					isValid = OperandChecker.isValidIndex(this.getOperand("FX"));
 					if(!isValid) hErr.reportError(makeError("OORidxReg", "FX", this.getOpId()), this.lineNum, -1);
 				}else{
 					isValid=false;
 					hErr.reportError(makeError("instructionMissingOp", this.getOpId(), "FR or FX"), this.lineNum, -1);
 				}
 			}else{
 				isValid=false;
 				hErr.reportError(makeError("instructionMissingOp", this.getOpId(), "DX or DM"), this.lineNum, -1);
 			}
 			//to many operands
 		}else{
 			isValid =false;
 			hErr.reportError(makeError("extraOperandsIns", this.getOpId()), this.lineNum, -1);
 		}
		return false; // TODO: IMPLEMENT
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
 		return new USI_TRDR();
 	}
 
 	/**
 	 * Calls the Instance(String,int) constructor to track this instruction.
 	 * 
 	 * @param ignored
 	 *            Unused parameter; used to distinguish the constructor for the
 	 *            static instance.
 	 */
 	private USI_TRDR(boolean ignored) {
 		super(opId, opCode);
 	}
 
 	/** Default constructor; does nothing. */
 	private USI_TRDR() {}
 }
 
