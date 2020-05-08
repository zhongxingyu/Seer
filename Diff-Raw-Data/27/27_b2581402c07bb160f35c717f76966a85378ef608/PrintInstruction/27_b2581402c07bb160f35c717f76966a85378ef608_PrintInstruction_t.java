 import java.util.*;
 import java.lang.*;
 
 /**
  * Generated automatically by generate_instructions.py.
  */
 public class PrintInstruction extends Instruction {
    public static Integer operandCount = 1;
    public PrintInstruction() {
       super();
    }
 
    public String toString() { return this.toILOC(); }
 
    public ArrayList<Sparc> toSparc() {
       ArrayList<Sparc> instructions = new ArrayList<Sparc>();
       Sparc i;
 
       // sethi   %hi(.LLC1), %i0
      i = new SetSparc();
      i.addSource(new Label(".LLC0"));
       i.addDest(new Register("%o0"));
       instructions.add(i);
 
       // mov     %i5, %o1
       i = new MovSparc();
       i.addSource(this.getSources().get(0));
       i.addSource(new Register("%o1"));
       instructions.add(i);
 
       // call    printf
       i = new CallSparc();
       i.addSource(new Label("printf"));
       instructions.add(i);
 
       //nop
       instructions.add(new NopSparc());
 
       return instructions;
    }
 
    public String toILOC() {
       String classPattern = new String("Register");
       String[] pattern = classPattern.split(" ");
       String ret = "print ";
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
