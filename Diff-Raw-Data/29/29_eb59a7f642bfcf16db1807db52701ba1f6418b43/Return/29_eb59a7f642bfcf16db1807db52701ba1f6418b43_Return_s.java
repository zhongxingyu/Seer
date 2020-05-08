 package compiler.ir.instructions;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import compiler.back.regAloc.VirtualRegister;
 
 public class Return extends Instruction {
 
 	public Instruction returnValue;
 
 	public Return(Instruction returnValue) {
 		this.returnValue = returnValue;
 	}
 	
 	public Instruction getReturnValue() {
 		return returnValue;
 	}
 
 	public void setReturnValue(Instruction returnValue) {
 		this.returnValue = returnValue;
 	}
 	
 	public List<VirtualRegister> getInputOperands() {
 		// lazily fill the input operands list
 		if(inputOps == null) {
 			this.inputOps = new ArrayList<VirtualRegister>();
 			this.inputOps.add(Instruction.resolve(returnValue).outputOp);
 		}
 		return inputOps;
 	}
 
 	
 	public String toString() {
		return getInstrNumber()  + " : RET (" + Instruction.resolve(returnValue).getInstrLabel() + ")" + 
				 "\n [" + Instruction.resolve(returnValue).outputOp + "] ";
 	}
 }
