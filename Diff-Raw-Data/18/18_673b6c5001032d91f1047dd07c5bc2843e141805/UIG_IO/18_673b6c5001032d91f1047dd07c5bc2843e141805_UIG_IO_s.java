 package instructions;
 
 import assemblernator.IOFormat;
 import assemblernator.Instruction;
 import assemblernator.ErrorReporting.ErrorHandler;
 import static assemblernator.ErrorReporting.makeError;
 import assemblernator.OperandChecker;
 
 
 /**
  * 
  * @author Noah
  * @date Apr 14, 2012; 5:47:13 PM
  */
 public abstract class UIG_IO extends Instruction{
 	/** keeps track of the type of operand is in this instruction.*/
 	OperandType operandType;
 
 	/**
 	 * Operand types.
 	 * for I/O instructions, valid combinations of operands = 
 	 * {{NW, DM}, {NW, DM, DX}, {NW, FM}, {NW, FM, FX}, {NW, FL}} 
 	 * @author Noah
 	 * @date Apr 14, 2012; 5:24:01 PM
 	 */
 	public enum OperandType {
 		/** operands = {NW, DM} */
 		DM(true, false, false),
 		/** operands = {NW, DM, DX} */
 		DMDX(true, true, false),
 		/** operands = {NW, FM} */
 		FM(false, false, false),
 		/** operands = {NW, FM, FX} */
 		FMFX(false, true, false),
 		/** operands = {NW, FL}*/
 		FL(false, false, true);
 		
 		/** is the instruction input or output instruction */
 		private boolean input;
 		/** is the instruction using an index register */
 		private boolean index;
 		/** is the instruction using a literal.*/
 		private boolean literal;
 		
 		/**
 		 * Constructs.
 		 * @param input is the instruction being used as input or output?
 		 * @param index is there an index register being used?
 		 * @param literal is there a literal being used?
 		 */
 		OperandType(boolean input, boolean index, boolean literal) {
 			this.input = input;
 			this.index = index;
 			this.literal = literal;
 		}
 	}
 
 	
 	/**
 	 * @see assemblernator.Instruction#check(ErrorHandler)
 	 */
 	@Override
 	public final boolean check(ErrorHandler hErr) {
 		boolean isValid = true;
 		
 		//checks for operand combos and assigns OperandType.
 		if(!this.hasOperand("NW")) {
 			isValid = false;
 			hErr.reportError(makeError("instructionMissingOp", this.getOpId(), "NW"), this.lineNum, -1);
 		} else if(this.operands.size() == 2) {
 			if(this.hasOperand("DM")) {
 				this.operandType = OperandType.DM;
 			} else if(this.hasOperand("FM")) {
 				this.operandType = OperandType.FM;
 			} else if(this.hasOperand("FL")){
 				this.operandType = OperandType.FL;
 			} else {
 				isValid = false;
 				hErr.reportError(makeError("operandInsNeedAdd", this.getOpId(), "MREF", "NW"), this.lineNum, -1);
 			}
 		} else if(this.operands.size() == 3) {
 			if(this.hasOperand("DM") && this.hasOperand("DX")) {
 				this.operandType = OperandType.DMDX;
 			} else if(this.hasOperand("FM")  && this.hasOperand("FX")) {
 				this.operandType = OperandType.FMFX;
 			} else {
 				isValid = false;
				hErr.reportError(makeError("operandInsNeedAdd", this.getOpId(), "MREF", "NW"), this.lineNum, -1);
 			}
 		} else {
 			isValid = false;
 			hErr.reportError(makeError("extraOperandsIns", this.getOpId()), this.lineNum, -1);
 		}
 		
 		//checks for invalid combo's between operands and opid's.
 		//only checks if is valid so far.
 		if(isValid) {
 			if(this.hasOperand("DM") && (this.getOpId().equals("IWSR") || this.getOpId().equals("CWSR"))) {
 				isValid = false;
 				hErr.reportError(makeError("operandInsWrong", "DM", this.getOpId()), this.lineNum, -1);
 				
 			} else if (this.hasOperand("FM") && (this.getOpId().equals("IRKB") || this.getOpId().equals("CRKB"))) {
 				isValid = false;
 				hErr.reportError(makeError("operandInsWrong", "FM", this.getOpId()), this.lineNum, -1);
 			} else if (this.hasOperand("FL") && (this.getOpId().equals("IRKB") || this.getOpId().equals("CRKB"))) {
 				isValid = false;
 				hErr.reportError(makeError("operandInsWrong", "FL", this.getOpId()), this.lineNum, -1);
 			}	
 		}
 		
 		//checks for ranges of operand values.
 		if(isValid) {
 			if(this.operandType.input) {
 				isValid = OperandChecker.isValidMem(this.getOperand("DM"));
 				if(!isValid) hErr.reportError(makeError("OORmemAddr", "DM", this.getOpId()), this.lineNum, -1);
 				if(this.operandType.index) {
 					isValid = OperandChecker.isValidIndex(this.getOperand("DX"));
 					if(!isValid) hErr.reportError(makeError("OORidxReg", "DX", this.getOpId()), this.lineNum, -1);
 				}
 			} else if(this.operandType.literal){
 				isValid = OperandChecker.isValidMem(this.getOperand("FL"));
 				if(!isValid) hErr.reportError(makeError("OOR13tc", "FL", this.getOpId()), this.lineNum, -1);
 			} else {
 				isValid = OperandChecker.isValidMem(this.getOperand("FM"));
 				if(this.operandType.index) {
 					isValid = OperandChecker.isValidIndex(this.getOperand("FX"));
 					if(!isValid) hErr.reportError(makeError("OORidxReg", "FX", this.getOpId()), this.lineNum, -1);
 				}
 			}
 		}
 		
 		
 		return isValid;
 	}
 	
 	/**
 	 * @see assemblernator.Instruction#assemble()
 	 */
 	@Override
 	public final int[] assemble() {
 		//all instructions start w/ 6 bit opcode.
 		String code = IOFormat.formatBinInteger(this.getOpcode(), 6); //011000
 		String index = "000"; //default value of index bits if index registers are not used. 
 		int mem; 
 		int[] assembled = new int[1];
 		
 		code = code + "0000"; //i/o instructions have a unused 4 bits following opcode. //011000 0000
 		
 		if(operandType.input) { //operands = {DM, NW} or {DM, NW, DX} 
 			mem = Integer.parseInt(this.getOperand("DM")); //parse into decimal integer.
 			if(operandType.index) { //operand = {DM, NW, DX}
 				index = this.getOperand("DX"); //get index register decimal.
 				index = IOFormat.formatBinInteger(Integer.parseInt(index), 3); //convert base-10 to binary.
 			}
 		} else {
 			if(operandType.literal) { //operands = {FL, NW}
 				mem = Integer.parseInt(this.getOperand("FL"));
 			} else {
 				mem = Integer.parseInt(this.getOperand("FM"));
 				if(operandType.index) {
 					index = this.getOperand("FX"); //get index register decimal.
 					index = IOFormat.formatBinInteger(Integer.parseInt(index), 3); //convert base-10 to binary.
 				}
 			}
 		}
 		
 		code = code + index;//add index register bits.
 		code = code + IOFormat.formatBinInteger(Integer.parseInt(this.getOperand("NW")), 3); //add number of word bits.
 		
 		if(operandType.input) { 
 			code = code + "00"; //unused two bits for input instruction format.
 		} else {
 			code = code + "0"; //1 unused bit for output instruction format.
 			//followed by literal bit.
 			if(operandType.literal) {
 				code = code + "1"; 
 			} else {
 				code = code + "0";
 			}
 		}
 		
 		code = code + IOFormat.formatBinInteger(mem, 13); //concat mem bits.
 		assembled[0] = Integer.parseInt(code, 2); //parse as a binary integer.
 		return assembled;
 	}
 	
 	/**
 	 * Invokes parent's constructor.
 	 * @param opid
 	 * @param opcode
 	 */
 	UIG_IO(String opid, int opcode) {
 		super(opid, opcode);
 	}
 	
 	/**
 	 * default constructor does nothing.
 	 */
 	UIG_IO() {}
 }
