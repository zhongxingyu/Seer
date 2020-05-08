 import java.util.*;
 
 public class NewInstruction extends Instruction
 {
   public HashSet<String> fields;
   public Register reg;
   public String structname;
   public Register sparcRegister;
   
   public NewInstruction(String structname, HashSet<String> fields, Register reg)
   {
     super("new");
     this.structname = structname;
     this.fields = fields;
     this.reg = reg;
     sparcRegister = new Register("%o0");
   }
   
   public String toSparc(){
    return new String("mov " + fields.size()*4 + ", " + sparcRegister.sparcName + "\n  " + "call malloc\n  nop\n  mov " + sparcRegister.sparcName
        + ", " + reg.sparcName);
   }
   
   public String toString()
   {
     return new String(name + " " + structname + " " + fields + ", " + reg);
   }
   
   public ArrayList<Register> getSources(){
     ArrayList<Register> ret = new ArrayList<Register>();
     return ret;
   }
   
   public ArrayList<Register> getDests(){
     ArrayList<Register> ret = new ArrayList<Register>();
     ret.add(reg);
     ret.add(sparcRegister);//notsure
     return ret;
   }
 }
