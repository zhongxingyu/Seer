 package instructions;
 
 import static assemblernator.ErrorReporting.makeError;
 import static assemblernator.InstructionFormatter.formatOther;
 import static simulanator.Deformatter.breakDownOther;
 import simulanator.Machine;
 import simulanator.Deformatter.OpcodeBreakdown;
 import assemblernator.AbstractInstruction;
 import assemblernator.ErrorReporting.ErrorHandler;
 import assemblernator.Instruction;
 import assemblernator.Module;
 import assemblernator.Module.Value;
 import assemblernator.Module.Value.BitLocation;
 import assemblernator.OperandChecker;
 
 /**
  * The PSH instruction.
  * 
  * @author Generate.java
  * @date Apr 08, 2012; 08:26:19
  * @specRef S0
  */
 public class USI_PSH extends AbstractInstruction {
 	/**
 	 * The operation identifier of this instruction; while comments should not
 	 * be treated as an instruction, specification says they must be included in
 	 * the user report. Hence, we will simply give this class a semicolon as its
 	 * instruction ID.
 	 */
 	private static final String opId = "PSH";
 
 	/** This instruction's identifying opcode. */
 	private static final int opCode = 0x30; // 0b110000
 
 	/** The static instance for this instruction. */
 	static USI_PSH staticInstance = new USI_PSH(true);
 
 	/** @see assemblernator.Instruction#getNewLC(int, Module) */
 	@Override public int getNewLC(int lc, Module mod) {
 		return lc+1;
 	}
 
 	/** @see assemblernator.Instruction#check(ErrorHandler, Module) */
 	@Override public boolean check(ErrorHandler hErr, Module module) {
 		boolean isValid = true;
 		Value value;
 		if(this.operands.size() > 1) {
 			hErr.reportError(makeError("extraOperandsIns", this.getOpId()), this.lineNum, -1);
 			isValid =  false;
 		} else if(this.operands.size() < 1) {
 			isValid = false;
 			hErr.reportError(makeError("tooFewOperands"), this.lineNum, -1);
 		} else if(this.hasOperand("FM")) {
 			value = module.evaluate(this.getOperand("FM"), true, BitLocation.Address, hErr, this, this.getOperandData("FM").valueStartPosition);
 			//range checking
 			isValid = OperandChecker.isValidMem(value.value);
 			if(!isValid) hErr.reportError(makeError("OORmemAddr", "FM", this.getOpId()), this.lineNum, -1);
 			this.getOperandData("FM").value = value;
 		} else if(this.hasOperand("FC")) {
 			//range checking
 			value = module.evaluate(this.getOperand("FC"), false, BitLocation.Literal, hErr, this, this.getOperandData("FC").valueStartPosition);
			isValid = OperandChecker.isValidConstant(value.value, ConstantRange.RANGE_ADDR);
 			if(!isValid) hErr.reportError(makeError("OORconstant", "FC", this.getOpId(),
					Integer.toString(ConstantRange.RANGE_ADDR.min), Integer.toString(ConstantRange.RANGE_ADDR.max)), this.lineNum, -1);
 			this.getOperandData("FC").value = value;
 		} else if(this.hasOperand("FL")){
 			//range checking
 			value = module.evaluate(this.getOperand("FL"), false, BitLocation.Literal,  hErr, this, this.getOperandData("FL").valueStartPosition);
			isValid = OperandChecker.isValidLiteral(value.value,ConstantRange.RANGE_ADDR);
 			if(!isValid) hErr.reportError(makeError("OORconstant", "FL", this.getOpId(),
					Integer.toString(ConstantRange.RANGE_ADDR.min), Integer.toString(ConstantRange.RANGE_ADDR.max)), this.lineNum, -1);
 			this.getOperandData("FL").value = value;
 		} else{
 			isValid = false;
 			if(this.hasOperand("FR")){
 				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "FR"), this.lineNum, -1);
 			}  else if(this.hasOperand("DM")){
 				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "DM"), this.lineNum, -1);				
 			}  else if(this.hasOperand("DR")){
 				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "DR"), this.lineNum, -1);				
 			} else if(this.hasOperand("DX")){
 				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "DX"), this.lineNum, -1);					
 			} else if(this.hasOperand("FS")){
 				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "FS"), this.lineNum, -1);				
 			} else if(this.hasOperand("LR")){
 				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "LR"), this.lineNum, -1);				
 			} else if(this.hasOperand("FM")){
				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "FM"), this.lineNum, -1);				
			} else if(this.hasOperand("EX")){
				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "EX"), this.lineNum, -1);				
 			} else if(this.hasOperand("NW")){
 				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "NW"), this.lineNum, -1);					
 			} else if(this.hasOperand("ST")){
 				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "ST"), this.lineNum, -1);					
 			}
 		}
 			return isValid;
 	}
 
 	/** @see assemblernator.Instruction#assemble() */
 	@Override public int[] assemble() {
 		return formatOther(this);
 	}
 
 	/** @see assemblernator.Instruction#execute(int, Machine) */
 	@Override public void execute(int instruction, Machine machine) {
 		
 		OpcodeBreakdown brkdwn = breakDownOther(machine.instruction);
 		int wordOrig = brkdwn.readFromSource(machine);
 		System.err.println(wordOrig);
 		machine.stack.push(wordOrig);
 		System.err.println(machine.stack.size());
 	
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
 		return new USI_PSH();
 	}
 
 	/**
 	 * Calls the Instance(String,int) constructor to track this instruction.
 	 * 
 	 * @param ignored
 	 *            Unused parameter; used to distinguish the constructor for the
 	 *            static instance.
 	 */
 	private USI_PSH(boolean ignored) {
 		super(opId, opCode);
 	}
 
 	/** Default constructor; does nothing. */
 	private USI_PSH() {}
 }
 
