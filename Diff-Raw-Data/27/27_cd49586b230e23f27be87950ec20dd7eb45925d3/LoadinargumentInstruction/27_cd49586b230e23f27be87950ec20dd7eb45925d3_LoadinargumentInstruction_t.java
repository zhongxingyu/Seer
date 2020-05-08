 import java.util.*;
 import java.lang.*;
 
 /**
  * Generated automatically by generate_instructions.py.
  */
 public class LoadinargumentInstruction extends Instruction {
    public static Integer operandCount = 3;
    public LoadinargumentInstruction() {
       super();sparcs.add("mov");
    }
 
    public String toString() { return this.toILOC(); }
 
    public ArrayList<Sparc> toSparc() {
       ArrayList<Sparc> instructions = new ArrayList<Sparc>();
       Sparc i;
 
       Immediate argImmediate = (Immediate) this.getOperands().get(1);
      int argNumber = argImmediate.getValue();
 
       // TODO load from stack for parameters over 6.
       if (argNumber >= 6) {
         Evil.error("Parameter number " + argNumber + " not loaded.");
          return instructions; 
       }
 
       i = new MovSparc();
       i.addOp(new Register("%i" + argNumber));
       i.addOp(this.getDestinations().get(0));
 
       instructions.add(i);
       return instructions;
    }
 
    public String toILOC() {
       String classPattern = new String("ID Immediate Register");
       String[] pattern = classPattern.split(" ");
       String ret = "loadinargument ";
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
