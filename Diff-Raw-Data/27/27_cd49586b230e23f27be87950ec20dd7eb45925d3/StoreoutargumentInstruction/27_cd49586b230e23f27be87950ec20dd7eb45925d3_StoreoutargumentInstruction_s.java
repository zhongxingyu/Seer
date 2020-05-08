 import java.util.*;
 import java.lang.*;
 
 /**
  * Generated automatically by generate_instructions.py.
  */
 public class StoreoutargumentInstruction extends Instruction {
    public static Integer operandCount = 2;
    public StoreoutargumentInstruction() {
       super();sparcs.add("mov");
    }
 
    public String toString() { return this.toILOC(); }
 
    public ArrayList<Sparc> toSparc() {
       ArrayList<Sparc> instructions = new ArrayList<Sparc>();
       Sparc i;
 
       Immediate argImmediate = (Immediate) this.getOperands().get(1);
      int argNumber = argImmediate.getValue() + 1;
 
       // TODO store into stack for arguments over 6.
       if (argNumber >= 6) {
          Evil.warning("Argument number " + argNumber + " not stored.");
          return instructions; 
       }
 
       i = new MovSparc();
       i.addOp(this.getSources().get(0));
       i.addOp(new Register("%o" + argNumber));
 
       instructions.add(i);
       return instructions;
    }
 
    public String toILOC() {
       String classPattern = new String("Register Immediate");
       String[] pattern = classPattern.split(" ");
       String ret = "storeoutargument ";
       int operandCount = this.getOperands().size();
 
       if ((operandCount != 0) && (operandCount != pattern.length)) {
          Evil.error(ret + ": Found " + operandCount + " operands, ILOC expecting " + pattern.length);
       }
 
       for (Operand r : this.getOperands()) {
          if (!r.toString().equals(""))
             ret = ret + r + ", ";
       }
 
       ret = ret.trim();
       if (ret.lastIndexOf(",") == ret.length()-1)
          ret = ret.substring(0, ret.length()-1);
 
       for (int i = 0; i < this.getOperands().size(); i++) {
          Operand o = this.getOperands().get(i);
          String oper = "null";
 
          if (o != null) { oper = o.getClass().getName(); }
 
          if (!oper.equals(pattern[i])) {
             Evil.error(ret + ": ILOC expecting " + classPattern + ". Found " + oper);
          }
       }
 
       return ret;
    }
 }
