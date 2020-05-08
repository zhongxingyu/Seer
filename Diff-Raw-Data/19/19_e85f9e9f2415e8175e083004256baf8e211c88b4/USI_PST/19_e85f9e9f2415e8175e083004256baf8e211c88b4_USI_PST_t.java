 package instructions;
 
 import static assemblernator.ErrorReporting.makeError;
 import assemblernator.AbstractInstruction;
 import assemblernator.ErrorReporting.ErrorHandler;
import assemblernator.Instruction.ConstantRange;
 import assemblernator.Instruction;
 import assemblernator.Module;
 import assemblernator.OperandChecker;
 
 /**
  * The PST instruction.
  * 
  * @author Ratul Khosla
  * @date Apr 08, 2012; 08:26:19
  * @specRef S2
  */
 public class USI_PST extends AbstractInstruction {
 	/**
 	 * The operation identifier of this instruction; while comments should not
 	 * be treated as an instruction, specification says they must be included in
 	 * the user report. Hence, we will simply give this class a semicolon as its
 	 * instruction ID.
 	 */
 	private static final String opId = "PST";
 
 	/** This instruction's identifying opcode. */
 	private static final int opCode = 0x00000032; // 0b11001000000000000000000000000000
 
 	/** The static instance for this instruction. */
 	static USI_PST staticInstance = new USI_PST(true);
 
 	/** @see assemblernator.Instruction#getNewLC(int, Module) */
 	@Override public int getNewLC(int lc, Module mod) {
 		return lc+1;
 	}
 
 
 	/** @see assemblernator.Instruction#check(ErrorHandler, Module) */
 	@Override public boolean check(ErrorHandler hErr, Module module) {
 	
 		boolean isValid = true;
		int value ;
		if(this.operands.size() < 1){
			isValid = false;
			hErr.reportError(makeError("tooFewOperands"), this.lineNum, -1);
		} else if(this.operands.size() > 1 ) {
 			hErr.reportError(makeError("extraOperandsIns", this.getOpId()), this.lineNum, -1);
 			isValid =  false;
 		} else if(this.hasOperand("DR")) {
 			//range checking
			value = module.evaluate(this.getOperand("DR"), false, hErr, this, this.getOperandData("DR").keywordStartPosition);
 			isValid = OperandChecker.isValidReg(value);
 			if(!isValid) hErr.reportError(makeError("OORidxReg", "DR", this.getOpId()), this.lineNum, -1);
 		}  else if(this.hasOperand("FM")) {
 			//range checking
 			value = module.evaluate(this.getOperand("FM"), true, hErr, this, this.getOperandData("FM").keywordStartPosition);
 			isValid = OperandChecker.isValidMem(value);
 			hErr.reportError(makeError("operandInsWrong", "FM", this.getOpId()), this.lineNum, -1);
 			if(this.hasOperand("FX")) {
 				//range checking - FX only possible if FM appears before it.
 				value = module.evaluate(this.getOperand("FX"), false, hErr, this, this.getOperandData("FX").keywordStartPosition);
 				isValid = OperandChecker.isValidIndex(value);
 				if(!isValid) hErr.reportError(makeError("OORidxReg", "FX", this.getOpId()), this.lineNum, -1);
 			}
 		}  else if(this.hasOperand("FC")) {
 			//range checking
 			value = module.evaluate(this.getOperand("FC"), false, hErr, this, this.getOperandData("FC").keywordStartPosition);
 			isValid = OperandChecker.isValidConstant(value,ConstantRange.RANGE_SHIFT);
			if(!isValid) hErr.reportError(makeError("OORconstant", "FC", this.getOpId(),
					Integer.toString(ConstantRange.RANGE_ADDR.min), Integer.toString(ConstantRange.RANGE_ADDR.max)), this.lineNum, -1);
 		}  else if(this.hasOperand("FL")){
 			//range checking
 			value = module.evaluate(this.getOperand("FL"), false, hErr, this, this.getOperandData("FL").keywordStartPosition);
 			isValid = OperandChecker.isValidMem(value);
			if(!isValid) hErr.reportError(makeError("OORconstant", "FL", this.getOpId(),
					Integer.toString(ConstantRange.RANGE_ADDR.min), Integer.toString(ConstantRange.RANGE_ADDR.max)), this.lineNum, -1);
 		} else{
 			isValid = false;
 			if(this.hasOperand("FR")){
 				hErr.reportError(makeError("operandInsWrong", "FR", this.getOpId()), this.lineNum, -1);
 			}  else if(this.hasOperand("DM")){
 				hErr.reportError(makeError("operandInsWrong", "DM", this.getOpId()), this.lineNum, -1);				
 			} else if(this.hasOperand("FS")){
 				hErr.reportError(makeError("operandInsWrong", "FS", this.getOpId()), this.lineNum, -1);				
 			} else if(this.hasOperand("LR")){
 				hErr.reportError(makeError("operandInsWrong", "LR", this.getOpId()), this.lineNum, -1);				
 			} else if(this.hasOperand("DX")){
 				hErr.reportError(makeError("operandInsWrong", "DX", this.getOpId()), this.lineNum, -1);				
 			} else if(this.hasOperand("EX")){
 				hErr.reportError(makeError("operandInsWrong", "EX", this.getOpId()), this.lineNum, -1);				
 			} else if(this.hasOperand("NW")){
 				hErr.reportError(makeError("operandInsWrong", "NW", this.getOpId()), this.lineNum, -1);				
 			} else if(this.hasOperand("ST")){
 				hErr.reportError(makeError("operandInsWrong", "ST", this.getOpId()), this.lineNum, -1);				
 			}
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
 		return new USI_PST();
 	}
 
 	/**
 	 * Calls the Instance(String,int) constructor to track this instruction.
 	 * 
 	 * @param ignored
 	 *            Unused parameter; used to distinguish the constructor for the
 	 *            static instance.
 	 */
 	private USI_PST(boolean ignored) {
 		super(opId, opCode);
 	}
 
 	/** Default constructor; does nothing. */
 	private USI_PST() {}
 
 
 }
 
