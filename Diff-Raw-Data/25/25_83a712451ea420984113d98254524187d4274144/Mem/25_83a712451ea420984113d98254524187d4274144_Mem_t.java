 package robot;
 
 // Default libraries
 import java.util.Vector;
 
 // Libraries
 import stackable.*;
 import exception.*;
 
 /**
  * Assembly functions - class Mem.
  * Provides the funcions for using 
  * the auxiliar memory in the virtual 
  * machine.
  * 
  * @author Renato Cordeiro Ferreira
  * @see Function
  * @see RMV
  */
 final public class Mem
 {
     /**
      * Assembly funcion STO.
      * Takes out the top of the main stack
      * and puts it in a specific position 
      * of the secondary memory.
      * @param Stackable position (but 
      *        must be numeric)
      * @throws OutOfBoundsException
      * @throws StackUnderflowException
      */
     static void STO(RVM rvm, Stackable position)
        throws SegmentationFaultException,
                StackUnderflowException
     {
         int pos;
         if(position instanceof Num)
         {
             Num num = (Num) position;
             pos = (int) num.getNumber();
         }
         else throw new StackUnderflowException();
        rvm.RAM.put(pos, rvm.DATA.pop());
     }
     
     /**
      * Assembly funcion RCL.
      * Takes out the data in the secondary
      * memory (if avaiable) and puts it in 
      * the top of the main stack.
      * @param Stackable position (but 
      *        must be numeric)
      * @throws OutOfBoundsException
      * @throws StackUnderflowException
      */
     static void RCL(RVM rvm, Stackable position) 
        throws SegmentationFaultException,
                StackUnderflowException
     {
         int pos;
         if(position instanceof Num)
         {
             Num num = (Num) position;
             pos = (int) num.getNumber();
         }
         else throw new StackUnderflowException();
         rvm.DATA.push(rvm.RAM.remove(pos));
     }
 }
