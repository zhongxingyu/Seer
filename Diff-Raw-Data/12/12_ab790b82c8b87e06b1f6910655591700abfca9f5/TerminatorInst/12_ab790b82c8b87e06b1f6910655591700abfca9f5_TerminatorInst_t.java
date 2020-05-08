 package edu.scu.jvec.Instructions;
 
 import java.util.List;
 
 import cn.edu.sjtu.jllvm.VMCore.Constants.Constant;
 import cn.edu.sjtu.jllvm.VMCore.Operators.CompareCondition;
 import cn.edu.sjtu.jllvm.VMCore.Operators.InstType;
 import cn.edu.sjtu.jllvm.VMCore.Types.Type;
 
 /**
  * Terminator instructions
  * 
  * ret (void | t0 op0) 
 * br label (op0 | t0 op0, op1, op2)
  *
  */
 public class TerminatorInst extends
 		cn.edu.sjtu.jllvm.VMCore.Instructions.TerminatorInst {
 
 	public TerminatorInst(Constant dest, int opcode, List<Constant> operands,
 			List<Type> types) {
 		super(dest, opcode, operands, types);
 	}
 
     /**
      * Generates LLVM code for binary instructions.
      */
     @Override
     public String toString() {
         StringBuffer sb = new StringBuffer();
         
         sb.append(InstrName.op(opcode)); sb.append(' ');
         
         if (opcode == InstType.retInst) {
             if (types.size() == 1) {
                 sb.append(types.get(0));
                 sb.append(' ');
             }
             sb.append(operands.get(0));
         } else if (opcode == InstType.brInst) {
             if (types.size() == 0) {
                sb.append("label ");
                sb.append(operands.get(0));
             } else {
                 assert(operands.size() == 3);
                 sb.append(types.get(0));
                 sb.append(' ');
                 sb.append(operands.get(0));
                 sb.append(", ");
                sb.append("label ");
                sb.append(operands.get(1));                
                 sb.append(", ");
                sb.append("label ");
                 sb.append(operands.get(2));
             }
         } else
             return super.toString();
         
         return sb.toString();
     }
 }
