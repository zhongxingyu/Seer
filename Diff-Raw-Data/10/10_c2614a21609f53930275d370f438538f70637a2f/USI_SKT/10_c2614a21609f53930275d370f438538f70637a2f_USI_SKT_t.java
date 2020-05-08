 package instructions;
 
 import static assemblernator.ErrorReporting.makeError;
 import static assemblernator.InstructionFormatter.formatOther;
import static assemblernator.Module.Value.BitLocation.Other;
 import assemblernator.AbstractInstruction;
 import assemblernator.ErrorReporting.ErrorHandler;
 import assemblernator.Instruction;
 import assemblernator.Module;
 import assemblernator.Module.Value;
 import assemblernator.OperandChecker;
 
 /**
  * The SKT instruction.
  * 
  * @author Generate.java
  * @date Apr 08, 2012; 08:26:19
  * @specRef JT7
  */
 public class USI_SKT extends AbstractInstruction {
 	/**
 	 * The operation identifier of this instruction; while comments should not
 	 * be treated as an instruction, specification says they must be included in
 	 * the user report. Hence, we will simply give this class a semicolon as its
 	 * instruction ID.
 	 */
 	private static final String opId = "SKT";
 
 	/** This instruction's identifying opcode. */
 	private static final int opCode = 0x00000027; // 0b10011100000000000000000000000000
 
 	/** The static instance for this instruction. */
 	static USI_SKT staticInstance = new USI_SKT(true);
 
 	/** @see assemblernator.Instruction#getNewLC(int, Module) */
 	@Override public int getNewLC(int lc, Module mod) {
 		return lc+1;
 	}
 	/**
 	 * The type of operand specifying the destination for this operation.
 	 */
 	String dest= "";
 	/** @see assemblernator.Instruction#check(ErrorHandler, Module) */
 	@Override public boolean check(ErrorHandler hErr, Module module) {
 		boolean isValid = true;
 		//less than 1 operand error
 				if(this.operands.size() < 1){
 					isValid=false;
 					hErr.reportError(makeError("instructionMissingOp", this.getOpId(), ""), this.lineNum, -1);
 					//checks for DR
 				}else if (this.operands.size() == 1){
 					if(this.hasOperand("DR")){
 						dest = "DR";
 						//range check
 						Operand o = getOperandData("DR");
						Value constantSize = module.evaluate(o.expression, false, Other, hErr, this,
 								o.valueStartPosition);
 						this.getOperandData("DR").value = constantSize;
 						isValid = OperandChecker.isValidReg(constantSize.value);
 						if(!isValid) hErr.reportError(makeError("OORarithReg", "DR", this.getOpId()), this.lineNum, -1);
 					}else{
 						isValid=false;
 						hErr.reportError(makeError("instructionMissingOp", this.getOpId(), "DR"), this.lineNum, -1);
 					}
 					//more than 1 operand error
 				}else{
 					isValid =false;
 					hErr.reportError(makeError("extraOperandsIns", this.getOpId()), this.lineNum, -1);
 				}
 		return isValid; // TODO: IMPLEMENT
 	}
 
 	/** @see assemblernator.Instruction#assemble() */
 	@Override public int[] assemble() {
 		return formatOther(this);
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
 		return new USI_SKT();
 	}
 
 	/**
 	 * Calls the Instance(String,int) constructor to track this instruction.
 	 * 
 	 * @param ignored
 	 *            Unused parameter; used to distinguish the constructor for the
 	 *            static instance.
 	 */
 	private USI_SKT(boolean ignored) {
 		super(opId, opCode);
 	}
 
 	/** Default constructor; does nothing. */
 	private USI_SKT() {}
 }
 
