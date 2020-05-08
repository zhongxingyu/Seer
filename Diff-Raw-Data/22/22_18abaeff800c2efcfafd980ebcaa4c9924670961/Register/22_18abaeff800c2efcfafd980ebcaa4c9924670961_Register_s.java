 public class Register extends Operand
 {
   public static int counter = 0;
   private int num;
   public String name;
   public boolean global;
   
   public Register()
   {
     counter++;
     num = counter;
     name = "r" + num;
     global = false;
   }
   
   public Register(String name)
   {
    name = "r" + name;
     global = false;
   }
   
   public String toString()
   {
     if(global){
       return "g" + name;
     }
     return name;
   }
 }
