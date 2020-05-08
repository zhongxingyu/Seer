 import java.util.*;
 import java.lang.*;
 
 /**
  * Generated automatically by generate_instructions.py.
  * Modified by Nat.
  */
 public class ReadInstruction extends Instruction {
    public static Integer operandCount = 1;
    public ReadInstruction() {
       super();sparcs.add("sethi");sparcs.add("or");sparcs.add("mov");sparcs.add("call");sparcs.add("nop");
    }
 
    public String toString() { return this.toILOC(); }
 
    public ArrayList<Sparc> toSparc() {
       ArrayList<Sparc> instructions = new ArrayList<Sparc>();
       Sparc i;
 
       // set   .LLC2, %o0
       i = new SetSparc();
       i.addOp(new Label(".LLC2"));
       i.addDest(new Register("%o0"));
       instructions.add(i);
 
      // set .readspot %o1
      i = new SetSparc();
      i.addOp(new Label(".readspot"));
      i.addDest(new Register("%o1"));
      instructions.add(i);

       // call    scanf
       i = new CallSparc();
       i.addOp(new Label("scanf"));
       instructions.add(i);
 
       // nop
       instructions.add(new NopSparc());
 
      // set .readspot %o1
      i = new SetSparc();
      i.addOp(new Label(".readspot"));
      i.addDest(new Register("%o1"));
      instructions.add(i);

      // ldsw     [%o1], %dest
      i = new LdswSparc();
      i.addSource(new Register("%o1"));
       i.addDest(this.getDestinations().get(0));
       instructions.add(i);
 
       return instructions;
    }
 
    public String toILOC() {
       String classPattern = new String("Register");
       String[] pattern = classPattern.split(" ");
       String ret = "read ";
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
