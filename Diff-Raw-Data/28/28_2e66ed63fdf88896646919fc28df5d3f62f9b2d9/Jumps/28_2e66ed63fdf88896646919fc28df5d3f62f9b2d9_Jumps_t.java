 package robot;
 
 // Default libraries
 import java.util.Vector;
 import java.util.HashMap;
 
 // Libraries
 import stackable.*;
 import exception.*;
 
 /**
  * Assembly functions - class Jumps.
  * Provides the funcions for conditional
  * (JMP) and unconditional (JIF/JIT) jumps
  * in the Program Counter (PC) of a giver
  * Virtual Machine.
  * 
  * @author Renato Cordeiro Ferreira
  * @see Function
  * @see RMV
  */
 final public class Jumps
 {
     /** 
      * Interface for compartison operations,
      * Dummy interface with the aim of being a 
      * model for all the comparison operations.
      */
     private interface Verify
     {
         boolean verify();
     }
     
     /**
      * General-model funcion JCMP.
      * Takes out the top of the main stack 
      * updates the counter accordingly to
      * the value on it. 
      * @param Virtual Machine
      * @param Stackable argument
      * @param Anonymous inner class of type Cmp
      */
     static final private void JCMP(RVM rvm, Stackable arg, Verify v)
         throws OutOfBoundsException
     {
         if(!v.verify()) return;
         
         if(arg instanceof Num)
         {
             Num num = (Num) arg; 
             int index = (int) num.getNumber();
             
             try {
                 if(rvm.PROG.elementAt(index) != null) 
                    rvm.PC = index-1;
                    // Sets the counter to the index -1, 
                    // to be able to increment in each 
                    // iteration of a for loop.
             }
             catch (Exception e) {
                 throw new OutOfBoundsException();
             }
         } 
         else if(arg instanceof Text) 
         {
             Text text = (Text) arg;
             String label = (String) text.getText();
             
             if(rvm.LABEL.containsKey(label))
                rvm.PC = rvm.LABEL.get(label)-1;
                // Sets the counter to the index -1, 
                // to be able to increment in each 
                // iteration of a for loop.
             else
                 throw new OutOfBoundsException();
         }
     }
     
     static final public void JMP(RVM rvm, Stackable arg) 
         throws OutOfBoundsException, WrongTypeException
     {
         // Always jump
         JCMP(rvm, arg, new Verify() {
             public boolean verify() { return true; } }
         );
     }
     
     static final public void JIT(RVM rvm, Stackable arg)
         throws OutOfBoundsException, WrongTypeException
     {
         Stackable top = rvm.DATA.pop();
         if(!(top instanceof Num)) throw new WrongTypeException("Num");
         
         Num num = (Num) top;
         final int bool = (int) num.getNumber();
         
         // Jump if top is a non-zero number
         JCMP(rvm, arg, new Verify() {
             public boolean verify()
             { return (bool != 0) ? true : false; } }
         );
     }
     
     static final public void JIF(RVM rvm, Stackable arg)
         throws OutOfBoundsException, WrongTypeException
     {
         Stackable top = rvm.DATA.pop();
         if(!(top instanceof Num)) throw new WrongTypeException("Num");
         
         Num num = (Num) top;
         final int bool = (int) num.getNumber();
         
         // Jump if top is zero
         JCMP(rvm, arg, new Verify() {
             public boolean verify()
             { return (bool == 0) ? true : false; } }
         );
     }
 }
