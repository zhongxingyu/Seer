 import java.util.*;
 import java.lang.*;
 
 /**
  * Generated automatically by generate_instructions.py.
  * Modified by Nat.
  */
 public class DivInstruction extends Instruction {
    public static Integer operandCount = 3;
    public DivInstruction() {
       super();
    }
 
    public String toString() { return this.toILOC(); }
 
    public ArrayList<Sparc> toSparc() {
       ArrayList<Sparc> instructions = new ArrayList<Sparc>();
       Sparc i;
 
       //mov  9, %o0   ; operand one
       i = new MovSparc();
       i.addSource(this.getSources().get(0));
       i.addDest(new Register("%o0"));
       instructions.add(i);
 
       //mov  7, %o1   ; operand two
       i = new MovSparc();
       i.addSource(this.getSources().get(1));
       i.addDest(new Register("%o1"));
       instructions.add(i);
 
       //call .div
       i = new CallSparc();
       i.addSource(new Label(".div"));
       instructions.add(i);
 
       //nop
       instructions.add(new NopSparc());
 
       //mov  %o0, %l0 ; result
       i = new MovSparc();
      i.addSource(new Register("%o0"));
       i.addDest(this.getOperands().get(2));
       instructions.add(i);
 
       return instructions;
    }
 
    public String toILOC() {
       String classPattern = new String("Register Register Register");
       String[] pattern = classPattern.split(" ");
       String ret = "div ";
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
