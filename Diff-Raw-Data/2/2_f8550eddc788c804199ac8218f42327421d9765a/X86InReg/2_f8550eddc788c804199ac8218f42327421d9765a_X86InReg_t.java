 package minijava.ir.frame.x86;
 
 import minijava.ir.frame.Access;
 import minijava.ir.temp.Temp;
 import minijava.ir.tree.IRExp;
 
 public class X86InReg extends Access
 {
  private Temp temp;
   
   protected X86InReg(Temp temp)
   {
     this.temp = temp;
   }
   
   @Override
   public String toString()
   {
     // TODO Auto-generated method stub
     return null;
   }
   
   @Override
   public IRExp exp(IRExp fp)
   {
     // TODO Auto-generated method stub
     return null;
   }
   
 }
