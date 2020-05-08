 package instructions;
 
 import static assemblernator.ErrorReporting.makeError;
 import static assemblernator.InstructionFormatter.formatOther;
 import static assemblernator.OperandChecker.isValidLiteral;
 import assemblernator.AbstractInstruction;
 import assemblernator.ErrorReporting.ErrorHandler;
 import assemblernator.Module;
 import assemblernator.Module.Value;
 import assemblernator.Module.Value.BitLocation;
 import assemblernator.OperandChecker;
 
 /**
  * @author Ratul Khosla
  * @date Apr 14, 2012; 5:25:24 PM
  */
 public abstract class UIG_ShiftManipulate extends AbstractInstruction {
 	
 	/**
 	 * A variable representing the from part of the instruction.
 	 */
 	String origin = "";
 	/**
 	 * A variable representing the destination/to part of the instruction. 
 	 */
 	String dest = "";
 		
 	/**
 	 * @author Ratul Khosla
 	 * @date Apr 14, 2012; 5:29:29 PM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @return  returns whether the given combinatin is valid or not.
 	 * @specRef N/A
 	 */
 	@Override
 	public final boolean check(ErrorHandler hErr, Module module){
 		boolean isValid = true;
 		Value value;
 		//The only possible combination has 2 operands
 		if(this.operands.size() > 2){
 			isValid = false;
 			hErr.reportError(makeError("extraOperandsIns", this.getOpId()), this.lineNum, -1);
 		} else if(this.operands.size() < 2) {
 			isValid = false;
 			hErr.reportError(makeError("tooFewOperandsIns", this.getOpId()), this.lineNum, -1);
 		} else if(!((this.hasOperand("FC") || (this.hasOperand("EX"))))){
 			isValid = false;
 			//isValid = OperandChecker.isValidConstant(value.value, ConstantRange.RANGE_SHIFT);
 			hErr.reportError(makeError("instructionMissingOp", this.getOpId(), "FC"), this.lineNum, -1);
 			//if(!isValid) hErr.reportError(makeError("OORconstant", "FC", this.getOpId()), this.lineNum, -1);
 			//now there are 2 operands, one of which is FC
 		} else if(this.hasOperand("FC")){
 			//check FC
 			value = module.evaluate(this.getOperand("FC"), false,BitLocation.Literal ,  hErr, this, this.getOperandData("FC").valueStartPosition); //value of FC
 			isValid = OperandChecker.isValidConstant(value.value, ConstantRange.RANGE_SHIFT); //check if value of FC is valid.
 			if(!isValid) hErr.reportError(makeError("OORconstant", "FC", 
 					this.getOpId(), Integer.toString(ConstantRange.RANGE_SHIFT.min), Integer.toString(ConstantRange.RANGE_SHIFT.max)), this.lineNum, -1);
 			this.getOperandData("FC").value = value;
 			if(this.hasOperand("DR")){
 				dest = "DR";
 				//range checking
 				value = module.evaluate(this.getOperand("DR"), false,BitLocation.Other, hErr, this, this.getOperandData("DR").valueStartPosition);
 				isValid = OperandChecker.isValidReg(value.value);
 				if(!isValid) hErr.reportError(makeError("OORidxReg", "DR", this.getOpId()), this.lineNum, -1);
 				this.getOperandData("DR").value = value;
 			} else if(this.hasOperand("DX")){
 				dest = "DX";
 				//range checking
 				value = module.evaluate(this.getOperand("DX"), false,BitLocation.Other, hErr, this, this.getOperandData("DX").valueStartPosition);
 				isValid = OperandChecker.isValidIndex(value.value);
 				if(!isValid) hErr.reportError(makeError("OORidxReg", "DX", this.getOpId()), this.lineNum, -1);
 				this.getOperandData("DX").value = value;
 			} else if(this.hasOperand("EX")){
 				isValid = false;
 				hErr.reportError(makeError("operandWrongWith", "EX", "FC"), this.lineNum, -1);		
			} 
 		} else if(this.hasOperand("EX")) {
 			//check EX
 			value = module.evaluate(this.getOperand("EX"), false,BitLocation.Literal, hErr, this, this.getOperandData("EX").valueStartPosition); 
 			isValid = isValidLiteral(value.value, ConstantRange.RANGE_ADDR);
 			if(!isValid) hErr.reportError(makeError("OORconstant", "EX", this.getOpId(), 
 					Integer.toString(ConstantRange.RANGE_ADDR.min), Integer.toString(ConstantRange.RANGE_ADDR.max)), this.lineNum, -1);
 			this.getOperandData("EX").value = value;
 			if(this.hasOperand("DR")){
 				dest = "DR";
 				//range checking
 				value = module.evaluate(this.getOperand("DR"), false,BitLocation.Other, hErr, this, this.getOperandData("DR").valueStartPosition);
 				isValid = OperandChecker.isValidReg(value.value);
 				if(!isValid) hErr.reportError(makeError("OORidxReg", "DR", this.getOpId()), this.lineNum, -1);
 				this.getOperandData("DR").value = value;
 			} else if(this.hasOperand("DX")){
 				dest = "DX";
 				//range checking
 				value = module.evaluate(this.getOperand("DX"), false,BitLocation.Other, hErr, this, this.getOperandData("DX").valueStartPosition);
 				isValid = OperandChecker.isValidIndex(value.value);
 				if(!isValid) hErr.reportError(makeError("OORidxReg", "DX", this.getOpId()), this.lineNum, -1);
 				this.getOperandData("DX").value = value;
 			} else if(this.hasOperand("FC")){
 					isValid = false;
 					hErr.reportError(makeError("operandWrongWith", "FC", "EX"), this.lineNum, -1);		
			} 	
 		} else {
 			isValid = false;
 			if(this.hasOperand("FR")){
 				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "FR"), this.lineNum, -1);
 			}  else if(this.hasOperand("DM")){
 				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "DM"), this.lineNum, -1);				
 			}  else if(this.hasOperand("FL")){
 				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "FL"), this.lineNum, -1);				
 			} else if(this.hasOperand("FX")){
 				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "FX"), this.lineNum, -1);				
 			} else if(this.hasOperand("FS")){
 				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "FS"), this.lineNum, -1);				
 			} else if(this.hasOperand("LR")){
 				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "LR"), this.lineNum, -1);				
 			} else if(this.hasOperand("FM")){
 				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "FM"), this.lineNum, -1);				
 			} else if(this.hasOperand("NW")){
 				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "NW"), this.lineNum, -1);				
 			} else if(this.hasOperand("ST")){
 				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "ST"), this.lineNum, -1);				
 			}
 		}
 		
 		//checks for invalid combo's between operands and opid's.
 		//only checks if is valid so far.
 		if(isValid) {
 			if(this.hasOperand("DM") && (this.getOpId().equals("ISHR") || this.getOpId().equals("ISHL")
 					|| this.getOpId().equals("ISRA") || this.getOpId().equals("ISLA") || this.getOpId().equals("ROR") 
 					|| this.getOpId().equals("ROL"))) {
 				isValid = false;
 				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "DM"), this.lineNum, -1);
 				
 			} else if (this.hasOperand("FM") && (this.getOpId().equals("ISHR") || this.getOpId().equals("ISHL")
 					|| this.getOpId().equals("ISRA") || this.getOpId().equals("ISLA") || this.getOpId().equals("ROR") 
 					|| this.getOpId().equals("ROL"))) {
 				isValid = false;
 				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "FM"), this.lineNum, -1);
 			} else if (this.hasOperand("FX") && (this.getOpId().equals("ISHR") || this.getOpId().equals("ISHL")
 					|| this.getOpId().equals("ISRA") || this.getOpId().equals("ISLA") || this.getOpId().equals("ROR") 
 					|| this.getOpId().equals("ROL"))) {
 				isValid = false;
 				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "FX"), this.lineNum, -1);
 			} else if (this.hasOperand("FR") && (this.getOpId().equals("ISHR") || this.getOpId().equals("ISHL")
 					|| this.getOpId().equals("ISRA") || this.getOpId().equals("ISLA") || this.getOpId().equals("ROR") 
 					|| this.getOpId().equals("ROL"))) {
 				isValid = false;
 				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "FR"), this.lineNum, -1);
 			} else if (this.hasOperand("LR") && (this.getOpId().equals("ISHR") || this.getOpId().equals("ISHL")
 					|| this.getOpId().equals("ISRA") || this.getOpId().equals("ISLA") || this.getOpId().equals("ROR") 
 					|| this.getOpId().equals("ROL"))) {
 				isValid = false;
 				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "LR"), this.lineNum, -1);
 			} else if (this.hasOperand("ST") && (this.getOpId().equals("ISHR") || this.getOpId().equals("ISHL")
 					|| this.getOpId().equals("ISRA") || this.getOpId().equals("ISLA") || this.getOpId().equals("ROR") 
 					|| this.getOpId().equals("ROL"))) {
 				isValid = false;
 				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "ST"), this.lineNum, -1);
 			} else if (this.hasOperand("NW") && (this.getOpId().equals("ISHR") || this.getOpId().equals("ISHL")
 					|| this.getOpId().equals("ISRA") || this.getOpId().equals("ISLA") || this.getOpId().equals("ROR") 
 					|| this.getOpId().equals("ROL"))) {
 				isValid = false;
 				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "NW"), this.lineNum, -1);
 			}
 		}
 		
 		
 		return isValid;
 	}
 
 	/**
 	 * @see assemblernator.Instruction#assemble()
 	 */
 	@Override
 	public final int[] assemble() {
 		
 		return formatOther(this);
 	}
 
 
 /**
  * Invokes parent's constructor.
  * @param opid	 the operation id
  * @param opcode 	the opcode
  */UIG_ShiftManipulate(String opid, int opcode) {
 	super(opid, opcode);
 }
 
 /**
  * default constructor does nothing.
  */
 UIG_ShiftManipulate() {}
 }
 
