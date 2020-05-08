 // might not be a valid test
 
 template V {
     public class X {
         public tabstract String getString();
     }
 }
 
 template B {
     public class Y {
         public String getString() {
             return "Hello!";
         }
     }
 }
 
 package P {
     // should this require explicit renaming for the methods?
 
     inst V with X => Z;
     inst B with Y => Z;
 
     class Z adds {
         public static void main(String args[]) {
            System.out.println( new Z().getString() );
         }
     }
 }
