 package instructions;
 
 import static assemblernator.ErrorReporting.makeError;
 import assemblernator.AbstractInstruction;
 import assemblernator.ErrorReporting.ErrorHandler;
 import assemblernator.Instruction.Operand;
 import assemblernator.IOFormat;
 import assemblernator.Module;
 import assemblernator.OperandChecker;
 
 /**
  * @author Eric
  * @date Apr 14, 2012; 5:22:20 PM
  */
 public abstract class UIG_Arithmetic extends AbstractInstruction {
 
 	/**
 	 * The type of operand specifying the destination for this operation.
 	 */
 	String dest = "";
 	
 	/**
 	 *The type of operand specifying the source for this operation.
 	 */
 	String src = "";
 
 	/**
 	 * @author Eric
 	 * @date Apr 14, 2012; 5:52:36 PM
 	 */
 	@Override
 	public final boolean check(ErrorHandler hErr, Module module) {
 		boolean isValid = true;
 		// any size under two is invalid
 		if (this.operands.size() < 2) {
 			isValid = false;
 			hErr.reportError(makeError("instructionMissingOp", this.getOpId(), ""), this.lineNum, -1);
 			// checks all combinations for two operands if a combo is not found
 			// operands are invalid
 		} else if (this.operands.size() == 2) {
 			// checks combos associated with DM
 			if (this.hasOperand("DM")) {
 				dest = "DM";
 				//range checking
 				Operand o = getOperandData("DM");
 				int constantSize = module.evaluate(o.expression, true, hErr, this,
 						o.valueStartPosition);
 				this.getOperandData("DM").value = constantSize;
 				isValid = OperandChecker.isValidMem(constantSize);
 				if(!isValid) hErr.reportError(makeError("OORmemAddr", "DM", this.getOpId()), this.lineNum, -1);
 				if (this.hasOperand("FR")) {
 					src = "FR";
 					//range checking
 					Operand o1 = getOperandData("FR");
 					int constantSize1 = module.evaluate(o1.expression, false, hErr, this,
 							o1.valueStartPosition);
 					this.getOperandData("FR").value = constantSize1;
 					isValid = OperandChecker.isValidReg(constantSize1);
 					if(!isValid) hErr.reportError(makeError("OORarithReg", "FR", this.getOpId()), this.lineNum, -1);
 				} else if (this.hasOperand("FM")) {
 					src = "FM";
 					//range checking
 					Operand o1 = getOperandData("FM");
 					int constantSize1 = module.evaluate(o1.expression, true, hErr, this,
 							o1.valueStartPosition);
 					this.getOperandData("FM").value = constantSize1;
 					isValid = OperandChecker.isValidMem(constantSize1);
 					if(!isValid) hErr.reportError(makeError("OORmemAddr", "FM", this.getOpId()), this.lineNum, -1);
 				} else if (this.hasOperand("FL")) {
 					src = "FL";
 					//range checking
 					Operand o1 = getOperandData("FL");
 					int constantSize1 = module.evaluate(o1.expression, false, hErr, this,
 							o1.valueStartPosition);
 					this.getOperandData("FL").value = constantSize1;
 					isValid = OperandChecker.isValidLiteral(constantSize1, ConstantRange.RANGE_ADDR);
					if(!isValid) hErr.reportError(makeError("OOR13tc", "FL", this.getOpId()), this.lineNum, -1);
 				} else {
 					isValid = false;
 					hErr.reportError(makeError("operandInsNeedAdd", this.getOpId(), "FR,FM, or FL", "DM"), this.lineNum, -1);
 				}
 				// checks combos associated with DR
 			} else if (this.hasOperand("DR")) {
 				dest = "DR";
 				//range checking
 				Operand o = getOperandData("DR");
 				int constantSize = module.evaluate(o.expression, false, hErr, this,
 						o.valueStartPosition);
 				this.getOperandData("DR").value = constantSize;
 				isValid = OperandChecker.isValidReg(constantSize);
 				if(!isValid) hErr.reportError(makeError("OORarithReg", "DR", this.getOpId()), this.lineNum, -1);
 				if (this.hasOperand("FR")) {
 					src = "FR";
 					//range checking
 					Operand o1 = getOperandData("FR");
 					int constantSize1 = module.evaluate(o1.expression, false, hErr, this,
 							o1.valueStartPosition);
 					this.getOperandData("FR").value = constantSize1;
 					isValid = OperandChecker.isValidReg(constantSize1);
 					if(!isValid) hErr.reportError(makeError("OORarithReg", "FR", this.getOpId()), this.lineNum, -1);
 				} else if (this.hasOperand("FM")) {
 					src = "FM";
 					//range checking
 					Operand o1 = getOperandData("FM");
 					int constantSize1 = module.evaluate(o1.expression, true, hErr, this,
 							o1.valueStartPosition);
 					this.getOperandData("FM").value = constantSize1;
 					isValid = OperandChecker.isValidMem(constantSize1);
 					if(!isValid) hErr.reportError(makeError("OORmemAddr", "FM", this.getOpId()), this.lineNum, -1);
 				} else if (this.hasOperand("FL")) {
 					//range checking
 					Operand o1 = getOperandData("FL");
 					int constantSize1 = module.evaluate(o1.expression, false, hErr, this,
 							o1.valueStartPosition);
 					this.getOperandData("FL").value = constantSize1;
 					isValid = OperandChecker.isValidLiteral(constantSize1,ConstantRange.RANGE_16_TC);
 					if(!isValid) hErr.reportError(makeError("OOR13tc", "FL", this.getOpId()), this.lineNum, -1);
 					src = "FL";
 				} else if (this.hasOperand("FX")) {
 					//range checking
 					Operand o1 = getOperandData("FX");
 					int constantSize1 = module.evaluate(o1.expression, false, hErr, this,
 							o1.valueStartPosition);
 					this.getOperandData("FX").value = constantSize1;
 					isValid = OperandChecker.isValidIndex(constantSize1);
 					if(!isValid) hErr.reportError(makeError("OORidxReg", "FX", this.getOpId()), this.lineNum, -1);
 					src = "FX";
 				} else {
 					isValid = false;
 					hErr.reportError(makeError("operandInsNeedAdd", this.getOpId(), "FR,FM,FL, or FX", "DR"), this.lineNum, -1);
 				}
 				// checks combos associated with DX
 			} else if (this.hasOperand("DX")) {
 				dest = "DX";
 				//range checking
 				Operand o = getOperandData("DX");
 				int constantSize = module.evaluate(o.expression, false, hErr, this,
 						o.valueStartPosition);
 				this.getOperandData("DX").value = constantSize;
 				isValid = OperandChecker.isValidIndex(constantSize);
 				if(!isValid) hErr.reportError(makeError("OORidxReg", "DX", this.getOpId()), this.lineNum, -1);
 				if (this.hasOperand("FL")) {
 					src = "FL";
 					//range checking
 					Operand o1 = getOperandData("FL");
 					int constantSize1 = module.evaluate(o1.expression, false, hErr, this,
 							o1.valueStartPosition);
 					this.getOperandData("FL").value = constantSize1;
 					isValid = OperandChecker.isValidLiteral(constantSize1, ConstantRange.RANGE_16_TC);
 					if(!isValid) hErr.reportError(makeError("OOR13tc", "FL", this.getOpId()), this.lineNum, -1);
 				} else if (this.hasOperand("FX")) {
 					src = "FX";
 					//range checking
 					Operand o1 = getOperandData("FX");
 					int constantSize1 = module.evaluate(o1.expression, false, hErr, this,
 							o1.valueStartPosition);
 					this.getOperandData("FX").value = constantSize1;
 					isValid = OperandChecker.isValidIndex(constantSize1);
 					if(!isValid) hErr.reportError(makeError("OORidxReg", "FX", this.getOpId()), this.lineNum, -1);
 				}else if (this.hasOperand("FR")){
 					src = "FR";
 					//range checking
 					Operand o1 = getOperandData("FR");
 					int constantSize1 = module.evaluate(o1.expression, false, hErr, this,
 							o1.valueStartPosition);
 					this.getOperandData("FR").value = constantSize1;
 					isValid = OperandChecker.isValidReg(constantSize1);
 					if(!isValid) hErr.reportError(makeError("OORarithReg", "FR", this.getOpId()), this.lineNum, -1);
 				}else {
 					isValid = false;
 					hErr.reportError(makeError("operandInsNeedAdd", this.getOpId(), "FX or FL", "DX"), this.lineNum, -1);
 				}
 
 			} else {
 				isValid = false;
 				hErr.reportError(makeError("instructionMissingOp", this.getOpId(), "DX,DR or DM"), this.lineNum, -1);
 			}
 			// checks all combinations for three operands instructions
 		} else if (this.operands.size() == 3) {
 			// checks combos associated FR
 			if (this.hasOperand("FR")) {
 				src = "FR";
 				//range checking
 				Operand o = getOperandData("FR");
 				int constantSize = module.evaluate(o.expression, false, hErr, this,
 						o.valueStartPosition);
 				this.getOperandData("FR").value = constantSize;
 				isValid = OperandChecker.isValidReg(constantSize);
 				if(!isValid) hErr.reportError(makeError("OORarithReg", "FR", this.getOpId()), this.lineNum, -1);
 				if (this.hasOperand("DM") && this.hasOperand("DX")) {
 					dest = "DMDX";
 					//range checking
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
 				} else {
 					isValid = false;
 					hErr.reportError(makeError("operandInsNeedAdd", this.getOpId(), "DM and DX", "FR"), this.lineNum, -1);
 				}
 				// checks combos associated DR
 			} else if (this.hasOperand("DR")) {
 				dest = "DR";
 				//range checking
 				Operand o = getOperandData("DR");
 				int constantSize = module.evaluate(o.expression, false, hErr, this,
 						o.valueStartPosition);
 				this.getOperandData("DR").value = constantSize;
 				isValid = OperandChecker.isValidReg(constantSize);
 				if(!isValid) hErr.reportError(makeError("OORarithReg", "DR", this.getOpId()), this.lineNum, -1);
 				if (this.hasOperand("FX") && this.hasOperand("FM")) {
 					//range checking
 					Operand o1 = getOperandData("FX");
 					int constantSize1 = module.evaluate(o1.expression, false, hErr, this,
 							o1.valueStartPosition);
 					this.getOperandData("FX").value = constantSize1;
 					isValid = OperandChecker.isValidIndex(constantSize1);
 					if(!isValid) hErr.reportError(makeError("OORidxReg", "FX", this.getOpId()), this.lineNum, -1);
 					Operand o2 = getOperandData("FM");
 					int constantSize2 = module.evaluate(o2.expression, true, hErr, this,
 							o2.valueStartPosition);
 					this.getOperandData("FM").value = constantSize2;
 					isValid = OperandChecker.isValidMem(constantSize2);
 					if(!isValid) hErr.reportError(makeError("OORmemAddr", "FM", this.getOpId()), this.lineNum, -1);
 					src = "FXFM";
 				} else {
 					isValid = false;
 					hErr.reportError(makeError("operandInsNeedAdd", this.getOpId(), "FX and FM", "DR"), this.lineNum, -1);
 				}
 			} else {
 				isValid = false;
 				hErr.reportError(makeError("instructionMissingOp", this.getOpId(), "FR or DR"), this.lineNum, -1);
 			}
 			// more than three operands is invalid
 		} else {
 			isValid = false;
 			hErr.reportError(makeError("extraOperandsIns", this.getOpId()), this.lineNum, -1);
 		}
 		
 		return isValid;
 
 	}
 
 	/**
 	 * @author Eric
 	 * @date Apr 14, 2012; 5:52:36 PM
 	 */
 	@Override
 	public final int[] assemble() {
 		String code = IOFormat.formatBinInteger(this.getOpcode(), 6);
 		if (dest == "DR") {
 			if (src == "FM" || src == "FL" || src == "FXFM") {
 				// format 0
 			} else {
 				// format 1
 			}
 		} else if (dest == "DX") {
 			// and so on
 		}
 
 		return null;
 
 	}
 	
 	/**
 	 * Invokes parent's constructor.
 	 * @param opid
 	 * @param opcode
 	 */
 	UIG_Arithmetic(String opid, int opcode) {
 		super(opid, opcode);
 	}
 	
 	/**
 	 * default constructor does nothing.
 	 */
 	UIG_Arithmetic() {}
 }
