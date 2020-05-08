 package instructions;
 
 import static assemblernator.ErrorReporting.makeError;
 import static assemblernator.InstructionFormatter.formatOther;
 import assemblernator.AbstractInstruction;
 import assemblernator.ErrorReporting.ErrorHandler;
 import assemblernator.Module;
 import assemblernator.Module.Value;
import assemblernator.Module.Value.BitLocation;
 import assemblernator.OperandChecker;
 
 /**
  * @author Eric
  * @date Apr 14, 2012; 5:52:36 PM
  */
 public abstract class UIG_TransferCond extends AbstractInstruction {
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
 		//not enough operands
 		if(this.operands.size() < 2){
 			isValid=false;
 			hErr.reportError(makeError("instructionMissingOp", this.getOpId(), ""), this.lineNum, -1);
 			//checks combos for 2 operands
 		}else if(this.operands.size() == 2){
 			//combo check
 			if(this.hasOperand("FR")){
 				src="FR";
 				//range checking
 				Operand o = getOperandData("FR");
				Value constantSize = module.evaluate(o.expression, false,BitLocation.Other ,hErr, this,
 						o.valueStartPosition);
 				this.getOperandData("FR").value = constantSize;
 				isValid = OperandChecker.isValidReg(constantSize.value);
 				if(!isValid) hErr.reportError(makeError("OORarithReg", "FR", this.getOpId()), this.lineNum, -1);
 				if(this.hasOperand("DM")){
 					dest="DM";
 					//range checking
 					Operand o1 = getOperandData("DM");
					Value constantSize1 = module.evaluate(o1.expression, true,BitLocation.Address ,hErr, this,
 							o1.valueStartPosition);
 					this.getOperandData("DM").value = constantSize1;
 					isValid = OperandChecker.isValidMem(constantSize1.value);
 					if(!isValid) hErr.reportError(makeError("OORmemAddr", "DM", this.getOpId()), this.lineNum, -1);
 				}else{
 					isValid = false;
 					hErr.reportError(makeError("operandInsNeedAdd", this.getOpId(), "DM", "FR"), this.lineNum, -1);
 					
 				}
 			}else{
 				isValid = false;
 				hErr.reportError(makeError("instructionMissingOp","FR", this.getOpId()), this.lineNum, -1);
 				
 			}
 			//checks combos for 3 operands
 		}else if (this.operands.size() == 3){
 			if(this.hasOperand("FR")){
 			src="FR";
 			//range checking
 			Operand o = getOperandData("FR");
			Value constantSize = module.evaluate(o.expression, false,BitLocation.Other ,hErr, this,
 					o.valueStartPosition);
 			this.getOperandData("FR").value = constantSize;
 			isValid = OperandChecker.isValidReg(constantSize.value);
 			if(!isValid) hErr.reportError(makeError("OORarithReg", "FR", this.getOpId()), this.lineNum, -1);
 				if (this.hasOperand("DM") && this.hasOperand("DX")) {
 				dest = "DMDX";
 				//range checking
 				Operand o1 = getOperandData("DX");
				Value constantSize1 = module.evaluate(o1.expression, false,BitLocation.Other ,hErr, this,
 						o1.valueStartPosition);
 				this.getOperandData("DX").value = constantSize1;
 				isValid = OperandChecker.isValidIndex(constantSize1.value);
 				if(!isValid) hErr.reportError(makeError("OORidxReg", "DX", this.getOpId()), this.lineNum, -1);
 				Operand o2 = getOperandData("DM");
				Value constantSize2 = module.evaluate(o2.expression, true,BitLocation.Address ,hErr, this,
 						o2.valueStartPosition);
 				this.getOperandData("DM").value = constantSize2;
 				isValid = OperandChecker.isValidMem(constantSize2.value);
 				if(!isValid) hErr.reportError(makeError("OORmemAddr", "DM", this.getOpId()), this.lineNum, -1);
 				} else {
 				isValid = false;
 				hErr.reportError(makeError("operandInsNeedAdd", this.getOpId(), "DM and DX", "FR"), this.lineNum, -1);
 				}
 			
 			}else{
 				isValid = false;
 				hErr.reportError(makeError("instructionMissingOp","FR", this.getOpId()), this.lineNum, -1);
 			}
 			//to many operands
 		}else{
 			isValid =false;
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
 		return formatOther(this);
 	}
 	
 	
 	/**
 	 * Invokes parent's constructor.
 	 * @param opid The opId of child instructions.
 	 * @param opcode The distinguishing opcode of child instructions.
 	 */
 	UIG_TransferCond(String opid, int opcode) {
 		super(opid, opcode);
 	}
 	
 	/**
 	 * default constructor does nothing.
 	 */
 	UIG_TransferCond() {}
 	
 }
