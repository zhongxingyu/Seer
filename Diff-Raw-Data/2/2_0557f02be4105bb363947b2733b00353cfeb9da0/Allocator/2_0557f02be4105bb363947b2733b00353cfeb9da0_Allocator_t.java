 /*
 SNARL/Allocator
 
 James Current
 4/28/12
  */
 public class Allocator {
     public final class Register {
         private String name;    //Printable name of this Register
         private Register next;  //Next Register in Registers
         private boolean used;   //Is register currently in use?
 
         //Dummy Constructor. Makes sure that only Allocator can create Registers.
 
         private Register () {}
 
         //Constructor. Returns a new Register with given name, next, and used slots.
         
         private Register (String name, Register next, boolean used) {
             this.name = name;
             this.next = next;
             this.used = used;
         }
 
         //IsUsed. Returns if the Register is used or not.
 
         public boolean isUsed(){
             return used;
         }
 
         //ToString. Returns the name of the Register.
         
         public String toString(){
             return name;
         }
     }
     
     public final Register fp = new Register("$fp", null, true);     //$fp register
     public final Register ra = new Register("$ra", null, true);     //$ra register
     public final Register sp = new Register("$sp", null, true);     //$sp register
     public final Register v0 = new Register("$v0", null, true);     //$v0 register
     public final Register zero = new Register("$zero", null, true); //$zero register
     
     protected Register registers;     //Linked stack of Registers to be allocated.
 
     //Constructor. Returns a new Allocator with a linked stack of Registers $s0 through $s7.
 
     public Allocator (){
         Register s0 = new Register("$s0", null, false);
         Register s1 = new Register("$s1", s0, false);
         Register s2 = new Register("$s2", s1, false);
         Register s3 = new Register("$s3", s2, false);
         Register s4 = new Register("$s4", s3, false);
         Register s5 = new Register("$s5", s4, false);
         Register s6 = new Register("$s6", s5, false);
         registers = new Register("$s7", s6, false);
     }
     
     //Request. Requests a register to be allocated, throws an exception if there are no registers available.
     
     public Register request(){
         if(registers == null){
            throw new SnarlCompilerException("Out of memory.");
         }
         
         Register register = registers;
         
         registers = register.next;
         
         register.used = true;
         
         return register;
     }
 
     //Release. Releases a register, throws an exception if the register has already been released.
     
     public void release(Register register){
         if(!register.isUsed()){
             throw new RuntimeException("Register has already been released.");
         }
         
         register.used = false;
         
         register.next = registers;
 
         registers = register;
     }
 }
