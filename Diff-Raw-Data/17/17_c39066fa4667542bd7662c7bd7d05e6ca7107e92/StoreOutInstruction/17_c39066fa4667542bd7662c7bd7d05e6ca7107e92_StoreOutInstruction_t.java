 import java.util.*;
 
 public class StoreOutInstruction extends Instruction
 {
   public Register reg;
   public String immediate;
   public Register sparcRegister;
   
   public StoreOutInstruction(String immediate, Register reg)
   {
     super("storeoutargument");
     this.immediate = immediate;
     this.reg = reg;
     sparcRegister = new Register("%o" + immediate);
   }
   
   public String toString()
   {
     return new String(name + " " + reg + ", " + immediate);
   }
   
   public String toSparc()
   {
    return new String("mov " + this.reg.sparcName + ", %o" + immediate);
   }
   
   public ArrayList<Register> getSources(){
     ArrayList<Register> ret = new ArrayList<Register>();
     ret.add(reg);
     return ret;
   }
   
   public ArrayList<Register> getDests(){
     ArrayList<Register> ret = new ArrayList<Register>();
     ret.add(sparcRegister);
     return ret;
   }
 }
