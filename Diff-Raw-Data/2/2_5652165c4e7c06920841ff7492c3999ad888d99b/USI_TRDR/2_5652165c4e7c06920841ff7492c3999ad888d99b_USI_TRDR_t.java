 package instructions;
 
 import static assemblernator.ErrorReporting.makeError;
 import assemblernator.AbstractInstruction;
 import assemblernator.ErrorReporting.ErrorHandler;
 import assemblernator.Instruction.Operand;
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
 public class USI_TRDR extends AbstractInstruction {
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
 	/**
 	 * The type of operand specifying the destination for this operation.
 	 */
 	String dest = "";
 	/**
 	 * The type of operand specifying the source for this operation.
 	 */
 	String src = "";
 	/** @see assemblernator.Instruction#check(ErrorHandler, Module) */
 	@Override public boolean check(ErrorHandler hErr, Module module) {
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
 				Operand o = getOperandData("DM");
 				int constantSize = module.evaluate(o.expression, true, hErr, this,
 						o.valueStartPosition);
 				this.getOperandData("DM").value = constantSize;
 				isValid = OperandChecker.isValidMem(constantSize);
 				if(!isValid) hErr.reportError(makeError("OORmemAddr", "DM", this.getOpId()), this.lineNum, -1);
 				if(this.hasOperand("FR")){
 					src="FR";
 					//range checking
 					Operand o1 = getOperandData("FR");
 					int constantSize1 = module.evaluate(o1.expression, false, hErr, this,
 							o1.valueStartPosition);
 					this.getOperandData("FR").value = constantSize1;
 					isValid = OperandChecker.isValidReg(constantSize1);
 					if(!isValid) hErr.reportError(makeError("OORarithReg", "FR", this.getOpId()), this.lineNum, -1);
 				}else if(this.hasOperand("FX")){
 					src="FX";
 					//range checking
 					Operand o1 = getOperandData("FX");
 					int constantSize1 = module.evaluate(o1.expression, false, hErr, this,
 							o1.valueStartPosition);
					this.getOperandData("FX").value = constantSize1;
 					isValid = OperandChecker.isValidIndex(constantSize1);
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
 				Operand o1 = getOperandData("DX");
 				int constantSize1 = module.evaluate(o1.expression, false, hErr, this,
 						o1.valueStartPosition);
 				this.getOperandData("DX").value = constantSize1;
 				isValid = OperandChecker.isValidIndex(constantSize1);
 				if(!isValid) hErr.reportError(makeError("OORidxReg", "DX", this.getOpId()), this.lineNum, -1);
 				Operand o2 = getOperandData("DM");
 				int constantSize2 = module.evaluate(o2.expression, true, hErr, this,
 						o2.valueStartPosition);
 				this.getOperandData("DM").value = constantSize2;
 				isValid = OperandChecker.isValidMem(constantSize2);
 				if(!isValid) hErr.reportError(makeError("OORmemAddr", "DM", this.getOpId()), this.lineNum, -1);
 				if(this.hasOperand("FR")){
 					src="FR";
 					//range checking
 					Operand o = getOperandData("FR");
 					int constantSize = module.evaluate(o.expression, false, hErr, this,
 							o.valueStartPosition);
 					this.getOperandData("FR").value = constantSize;
 					isValid = OperandChecker.isValidReg(constantSize);
 					if(!isValid) hErr.reportError(makeError("OORarithReg", "FR", this.getOpId()), this.lineNum, -1);
 				}else if(this.hasOperand("FX")){
 					src="FX";
 					//range checking
 					Operand o = getOperandData("FX");
 					int constantSize = module.evaluate(o.expression, false, hErr, this,
 							o.valueStartPosition);
 					this.getOperandData("FX").value = constantSize;
 					isValid = OperandChecker.isValidIndex(constantSize);
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
 		return isValid; // TODO: IMPLEMENT
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
 
