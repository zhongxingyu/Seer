 package instructions;
 
 import static assemblernator.ErrorReporting.makeError;
 import static assemblernator.InstructionFormatter.formatSrcMem;
 import static assemblernator.Module.Value.BitLocation.Address;
 import static assemblernator.Module.Value.BitLocation.Other;
 import static assemblernator.OperandChecker.isValidIndex;
 import static assemblernator.OperandChecker.isValidMem;
 import static assemblernator.OperandChecker.isValidNumWords;
 import static assemblernator.OperandChecker.isValidReg;
 import simulanator.Deformatter;
 import simulanator.Deformatter.OpcodeBreakdown;
 import simulanator.Machine;
 import assemblernator.AbstractInstruction;
 import assemblernator.ErrorReporting.ErrorHandler;
 import assemblernator.Instruction;
 import assemblernator.Module;
 import assemblernator.Module.Value;
 
 /**
  * The ISRG instruction.
  * 
  * @author Generate.java
  * @date Apr 08, 2012; 08:26:19
  * @specRef IA3
  */
 public class USI_ISRG extends AbstractInstruction {
 	/**
 	 * The operation identifier of this instruction; while comments should not
 	 * be treated as an instruction, specification says they must be included in
 	 * the user report. Hence, we will simply give this class a semicolon as its
 	 * instruction ID.
 	 */
 	private static final String opId = "ISRG";
 
 	/** This instruction's identifying opcode. */
 	private static final int opCode = 0x0B; // 0b001011
 
 	/** The static instance for this instruction. */
 	static USI_ISRG staticInstance = new USI_ISRG(true);
 
 	/** @see assemblernator.Instruction#getNewLC(int, Module) */
 	@Override public int getNewLC(int lc, Module mod) {
 		return lc+1;
 	}
 
 	/** @see assemblernator.Instruction#check(ErrorHandler, Module) */
 	@Override public boolean check(ErrorHandler hErr, Module module) {
 		boolean isValid = true;
 		Value value;
 		
 		//check for operand combos.
 		if(this.hasOperand("NW")) {
 			value = module.evaluate(this.getOperand("NW"), false, Other, hErr, this, this.getOperandData("NW").valueStartPosition);
 			isValid = isValidNumWords(value.value);
 			if(!isValid) hErr.reportError(makeError("OORnw", this.getOpId()), this.lineNum, -1);
 			this.getOperandData("NW").value = value;
 			
 			if(this.operands.size() == 3 && isValid) {
 				if(this.hasOperand("DR") || this.hasOperand("DX")) {
 					if(this.hasOperand("DR")) {
 						value = module.evaluate(this.getOperand("DR"), false, Other, hErr, this, this.getOperandData("DR").valueStartPosition);
 						isValid = isValidReg(value.value);
 						if(!isValid) hErr.reportError(makeError("OORarithReg", "DR", this.getOpId()), this.lineNum, -1);
 						this.getOperandData("DR").value = value;
 					} else if(this.hasOperand("DX")) {
 						value = module.evaluate(this.getOperand("DX"), false, Other,  hErr, this, this.getOperandData("DX").valueStartPosition);
 						isValid = isValidIndex(value.value);
 						if(!isValid) hErr.reportError(makeError("OORidxReg", "DX", this.getOpId()), this.lineNum, -1);
 						this.getOperandData("DX").value = value;
 					}
 					
 					if(isValid) {
 						if(this.hasOperand("FM")) {
 							value = module.evaluate(this.getOperand("FM"), true, Address, hErr, this, this.getOperandData("FM").valueStartPosition);
 							isValid = isValidMem(value.value);
 							if(!isValid) hErr.reportError(makeError("OORmemAddr", "FM", this.getOpId()), this.lineNum, -1);
 							this.getOperandData("FM").value = value;
 						} else {
 							isValid = false;
 							hErr.reportError(makeError("instructionMissingOp", this.getOpId(), "FM"), this.lineNum, -1);
 						}
 					}
 				} else {
 					isValid = false;
 					hErr.reportError(makeError("instructionMissingOp2", this.getOpId(), "DR", "DX"), this.lineNum, -1);
 				}
 			} else {
 				isValid = false;
 				if(this.operands.size() > 3) {
 					hErr.reportError(makeError("extraOperandsIns", this.getOpId()), this.lineNum, -1);
 				} else {
 					hErr.reportError(makeError("tooFewOperandsIns", this.getOpId()), this.lineNum, -1);
 				}
 			}
 		} else {
 			isValid = false;
 			hErr.reportError(makeError("instructionMissingOp", this.getOpId(), "NW"), this.lineNum, -1);
 		}
 		
 		return isValid; 
 	}
 
 	/** @see assemblernator.Instruction#assemble() */
 	@Override public int[] assemble() {
 		return formatSrcMem(this);
 	}
 
 	/** @see assemblernator.Instruction#execute(int, Machine) */
 	@Override public void execute(int instruction, Machine machine) {
 		OpcodeBreakdown brkdwn = Deformatter.breakDownSrcRange(instruction);
 		int addr = brkdwn.source;
 		long word = 0;
		//sum contents of memory from addr to addr + nw.
		for(int i = 0; i < brkdwn.numWords; ++i) {
			word += machine.getMemory(addr + i);
		}
		machine.hErr.reportError(makeError("runOverflow"), machine.getLC(), -1);
		if(word > Integer.MAX_VALUE || word < Integer.MIN_VALUE){
			machine.hErr.reportError(makeError("runOverflow"), machine.getLC(), -1);
			word = 0;
 		}
 		brkdwn.putToDest((int)word, machine);
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
 		return new USI_ISRG();
 	}
 
 	/**
 	 * Calls the Instance(String,int) constructor to track this instruction.
 	 * 
 	 * @param ignored
 	 *            Unused parameter; used to distinguish the constructor for the
 	 *            static instance.
 	 */
 	private USI_ISRG(boolean ignored) {
 		super(opId, opCode);
 	}
 
 	/** Default constructor; does nothing. */
 	private USI_ISRG() {}
 }
 
