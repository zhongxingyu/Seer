 import java.util.*;
 import java.lang.*;
 
 /**
  * Generated automatically by generate_instructions.py.
  */
 public class LoadiInstruction extends Instruction {
    public static Integer operandCount = 2;
    public LoadiInstruction() {
      super();sparcs.add("set");sparcs.add("neg");
   }

   public ArrayList<Sparc> toSparc() {
      ArrayList<Sparc> instructions = new ArrayList<Sparc>();
      Sparc i;

      Immediate num = (Immediate) this.getOperands().get(0);
      boolean isNegative = num.getValue() < 0;

      // Make positve.
      num.setValue(Math.abs(num.getValue()));

      i = new SetSparc();
      i.addOp(num);
      i.addDest(this.getDestinations().get(0));
      instructions.add(i);

      if (isNegative) {
         i = new SubSparc();
         i.addSource(new Register("g0")); // g0 is always 0.
         i.addSource(this.getDestinations().get(0));
         i.addDest(this.getDestinations().get(0));
         instructions.add(i);
      }

      return instructions;
    }
 
    public String toString() { return this.toILOC(); }
 
    public String toILOC() {
       String classPattern = new String("Immediate Register");
       String[] pattern = classPattern.split(" ");
       String ret = "loadi ";
       int operandCount = this.getOperands().size();
 
       if ((operandCount != 0) && (operandCount != pattern.length)) {
          Evil.error(ret + ": Found " + operandCount + " operands, ILOC expecting " + pattern.length);
       }
 
       for (Operand r : this.getOperands()) {
          if (!r.toString().equals(""))
             ret = ret + r.toILOC() + ", ";
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
