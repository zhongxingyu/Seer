 package instructions;
 
 import static assemblernator.ErrorReporting.makeError;
 import static assemblernator.InstructionFormatter.formatDestRange;
 import static assemblernator.InstructionFormatter.formatSrcRange;
import static assemblernator.InstructionFormatter.formatOther;
 import static assemblernator.OperandChecker.isValidIndex;
 import static assemblernator.OperandChecker.isValidLiteral;
 import static assemblernator.OperandChecker.isValidMem;
 import static assemblernator.OperandChecker.isValidNumWords;
 import assemblernator.AbstractInstruction;
 import assemblernator.ErrorReporting.ErrorHandler;
 import assemblernator.Module;
 import assemblernator.Module.Value;
 
 /**
  * Parent class of input/output leaf instruction classes.
  * @author Noah
  * @date Apr 14, 2012; 5:47:13 PM
  */
 public abstract class UIG_IO extends AbstractInstruction{
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
 		DM(true, false, false, false),
 		/** operands = {NW, DM, DX} */
 		DMDX(true, true, false, false),
 		/** operands = {NW, FM} */
 		FM(false, false, false, false),
 		/** operands = {NW, FM, FX} */
 		FMFX(false, true, false, false),
 		/** operands = {NW, FL}*/
 		FL(false, false, true, false),
 		/** operands = {NW, EX}*/
 		EX(false, false, false, true);
 		
 		/** is the instruction input or output instruction */
 		private boolean input;
 		/** is the instruction using an index register */
 		private boolean index;
 		/** is the instruction using a literal.*/
 		private boolean literal;
 		/** is the instruction using a expression.*/
 		private boolean expression;
 		
 		/**
 		 * Constructs.
 		 * @param input is the instruction being used as input or output?
 		 * @param index is there an index register being used?
 		 * @param literal is there a literal being used?
 		 * @param exp is there an expression being used?
 		 */
 		OperandType(boolean input, boolean index, boolean literal, boolean exp) {
 			this.input = input;
 			this.index = index;
 			this.literal = literal;
 			this.expression = exp;
 		}
 	}
 
 	
 	/**
 	 * @see assemblernator.Instruction#check(ErrorHandler, Module)
 	 * @modified: 9:15:34PM; Added additional error message in case FL is found together with FX.
 	 */
 	@Override
 	public final boolean check(ErrorHandler hErr, Module module) {
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
 			} else if(this.hasOperand("EX")) {
 				this.operandType = OperandType.EX;
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
 				if (this.hasOperand("FL")) { 
 					hErr.reportError(makeError("extraOperandsIns", this.getOpId()), this.lineNum, -1);
 				} else if (this.hasOperand("DM")) {
 					hErr.reportError(makeError("operandWrongWith", "DM", "NW"), this.lineNum, -1);
 				} else if (this.hasOperand("FM")) {
 					hErr.reportError(makeError("operandWrongWith", "FM", "NW"), this.lineNum, -1);
 				} else {
 					hErr.reportError(makeError("operandInsNeedAdd", this.getOpId(), "MREF", "NW"), this.lineNum, -1);
 				}
 			}
 		} else {
 			isValid = false;
 			hErr.reportError(makeError("extraOperandsIns", this.getOpId()), this.lineNum, -1);
 		}
 		
 		//checks for invalid combo's between operands and opid's.
 		//only checks if is valid so far.
 		if(isValid) {
 			if(this.operandType.input && (this.getOpId().equals("IWSR") || this.getOpId().equals("CWSR"))) {
 				isValid = false;
 				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "DM"), this.lineNum, -1);
 				
 			} else if ((!this.operandType.input) && (this.getOpId().equals("IRKB") || this.getOpId().equals("CRKB"))) {
 				isValid = false;
 				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "FM"), this.lineNum, -1);
 			} else if (this.operandType.literal && (this.getOpId().equals("IRKB") || this.getOpId().equals("CRKB"))) {
 				isValid = false;
 				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "FL"), this.lineNum, -1);
 			}  else if (this.operandType.expression && (this.getOpId().equals("IRKB") || this.getOpId().equals("CRKB"))) {
 				isValid = false;
 				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "EX"), this.lineNum, -1);
 			}
 		}
 		
 		//checks for ranges of operand values and store values in Operand..
 		if(isValid) {
 			Value value = module.evaluate(this.getOperand("NW"), false, hErr, this, this.getOperandData("NW").keywordStartPosition);
 			isValid = isValidNumWords(value.value);
 			if(!isValid) hErr.reportError(makeError("OORnw", this.getOpId()), this.lineNum, -1);
 			this.getOperandData("NW").value = value;
 			
 			if(this.operandType.input) {
 				 //evaluate value of operand
 				value = module.evaluate(this.getOperand("DM"), true, hErr, this, this.getOperandData("DM").keywordStartPosition);
 				isValid = isValidMem(value.value); //check value of operand.
 				if(!isValid) hErr.reportError(makeError("OORmemAddr", "DM", this.getOpId()), this.lineNum, -1);
 				this.getOperandData("DM").value = value;
 				
 				if(this.operandType.index) {
 					//evaluate value of operand.
 					value = module.evaluate(this.getOperand("DX"), false, hErr, this, this.getOperandData("DX").keywordStartPosition); 
 					isValid = isValidIndex(value.value);
 					if(!isValid) hErr.reportError(makeError("OORidxReg", "DX", this.getOpId()), this.lineNum, -1);
 					this.getOperandData("DX").value = value;
 					
 				}
 			} else if(this.operandType.literal){
 				//evaluate value of operand.
 				value = module.evaluate(this.getOperand("FL"), false, hErr, this, this.getOperandData("FL").keywordStartPosition); 
 				isValid = isValidLiteral(value.value, ConstantRange.RANGE_ADDR);
 				if(!isValid) hErr.reportError(makeError("OORconstant", "FL", this.getOpId(), 
 						Integer.toString(ConstantRange.RANGE_ADDR.min), Integer.toString(ConstantRange.RANGE_ADDR.max)), this.lineNum, -1);
 				this.getOperandData("FL").value = value;
 			} else if(this.operandType.expression) {
 				//evaluate value of operand.
 				value = module.evaluate(this.getOperand("EX"), true, hErr, this, this.getOperandData("EX").keywordStartPosition); 
 				isValid = isValidLiteral(value.value, ConstantRange.RANGE_ADDR);
 				if(!isValid) hErr.reportError(makeError("OORconstant", "EX", this.getOpId(), 
 						Integer.toString(ConstantRange.RANGE_ADDR.min), Integer.toString(ConstantRange.RANGE_ADDR.max)), this.lineNum, -1);
 				this.getOperandData("EX").value = value;
 			} else {
 				//evaluate value of operand.
 				value = module.evaluate(this.getOperand("FM"), true, hErr, this, this.getOperandData("FM").keywordStartPosition); 
 				isValid = isValidMem(value.value);
 				if(!isValid) hErr.reportError(makeError("OORmemAddr", "FM", this.getOpId()), this.lineNum, -1);
 				this.getOperandData("FM").value = value;
 				
 				if(this.operandType.index) {
 					//evaluate value of operand.
 					value = module.evaluate(this.getOperand("FX"), false, hErr, this, this.getOperandData("FX").keywordStartPosition); 
 					isValid = isValidIndex(value.value);
 					if(!isValid) hErr.reportError(makeError("OORidxReg", "FX", this.getOpId()), this.lineNum, -1);
 					this.getOperandData("FX").value = value;
 					
 				}
 			}
 			
 		}
 		
 		
 		return isValid;
 	}
 	
 	/**
 	 * @see assemblernator.Instruction#assemble()
 	 * @modified 5:55:50PM; now uses cached value in Operands -Noah.
 	 */
 	@Override
 	public final int[] assemble() {
 		if(operandType.input) {
 			return formatDestRange(this);
 		} else {
 			return formatSrcRange(this);
 		}
 	}
 	
 	/**
 	 * Invokes parent's constructor.
 	 * @param opid The opId of child instructions.
 	 * @param opcode The distinguishing opcode of child instructions.
 	 */
 	UIG_IO(String opid, int opcode) {
 		super(opid, opcode);
 	}
 	
 	/**
 	 * default constructor does nothing.
 	 */
 	UIG_IO() {}
 }
