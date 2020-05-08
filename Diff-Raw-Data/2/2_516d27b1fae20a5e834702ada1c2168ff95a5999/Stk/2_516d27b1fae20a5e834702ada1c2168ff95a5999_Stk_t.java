 package robot.function;
 
 // Libraries
 import stackable.*;
 import exception.*;
 
 /**
 * Assembly functions - class Stk.
  * Provides the funcions for manipulating 
  * the main stack of the virtual machine.
  * 
  * @author Renato Cordeiro Ferreira
  * @see Function
  * @see RMV
  */
 public class Stk
 {
     final private Stack DATA;
 
     /**
      * Class constructor. 
      * Receives a handle to the main stack.
      * 
      * @param Stack Data
      */
     Stk(Stack DATA)
     {
         this.DATA = DATA;
     }
     
     /**
      * Assembly funcion PUSH. 
      * Puts an element in the top of the main stack.
      * @param Stackable element.
      * @see Stackable
      */
     void PUSH(Stackable st)
     {
         this.DATA.push(st);
     }
     
     /**
      * Assembly funcion POP. 
      * Takes out an element of the top of the main stack.
      * @see Stackable
      */
     Stackable POP() throws StackUnderflowException
     {
         return this.DATA.pop();
     }
     
     /**
      * Assembly funcion DUP. 
      * Duplicates the top of the main stack.
      * @see Stackable
      */
     void DUP() throws StackUnderflowException
     {
         Stackable st = this.DATA.pop(); 
         this.DATA.push(st);
         this.DATA.push(st);
     }
 }
