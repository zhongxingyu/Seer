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
     inst V with X => Z ( getString() -> getString );
     inst B with Y => Z ( getString () -> getString );
 
     class Z adds {
         public static void main(String args[]) {
            System.out.println( new Z().getString() );
         }
     }
 }
