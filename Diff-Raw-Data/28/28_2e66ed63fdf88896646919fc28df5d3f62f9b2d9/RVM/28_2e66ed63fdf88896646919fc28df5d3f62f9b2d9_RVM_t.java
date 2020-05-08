 package robot;
 
 // Default libraries
 import java.util.Set;
 import java.util.Stack;
 import java.util.Vector;
 import java.util.HashMap;
 import java.util.Iterator;
 
 // Libraries
 import robot.*;
 import exception.*;
 import stackable.*;
 
 /**
  * Main class with the constructor of the robot and its data.
  * @author Renato Cordeiro Ferreira
  */
 public class RVM 
 {
     Vector  <Command>   PROG;
    Stack   <Integer>   CTRL = new Stack <Integer>   ();
    Stack   <Stackable> DATA = new Stack <Stackable> ();
     HashMap <String, Integer> LABEL = new HashMap <String, Integer>();
     HashMap <Integer, Stackable> RAM = new HashMap <Integer, Stackable>();
     int PC = 0;
     
     /**
      * Class constructor specifying a 'program' (vector of
      * objects of the class Command) to the RVM.
      * 
      * @param PROG    Vector of objects of the class Command
      * @see   Command 
      */
     public RVM(Vector <Command> PROG) 
     { 
         upload(PROG);
     }
     
     /**
      * Setter method created to upload a new 'program' (vector
      * of object of the class Command) in the RVM.
      * 
      * @param PROG    Vector of objects of the class Command
      * @see   Command
      */
     public void upload(Vector <Command> PROG) 
     { 
         this.PROG = PROG; upload_labels();
     }
     
     /**
      * Execute 1 assembly instruction.
      * 
      * @throws SegmentationFaultException
      * @throws InvalidOperationException
      * @throws StackUnderflowException
      * @throws NoLabelFoundException
      * @throws OutOfBoundsException
      * @throws WrongTypeException
      */ 
     public void exec() 
         throws SegmentationFaultException, 
                InvalidOperationException, 
                StackUnderflowException,
                NoLabelFoundException,
                OutOfBoundsException,
                WrongTypeException
     {
         Command   com      = this.PROG.elementAt(this.PC);
         String    function = com.getFunction  ();
         Stackable arg      = com.getAttribute ();
         
         // Call function
         if(function != null)
         {
            try { Ctrl.ctrl(this, function, arg); }
             catch (Exception e) { System.out.print(e); }
         }
     }
 
     /**
      * Function responsible for executing the 'program', step by 
      * step, untill it ends.
      * 
      * @throws SegmentationFaultException
      * @throws InvalidOperationException
      * @throws StackUnderflowException
      * @throws NoLabelFoundException
      * @throws OutOfBoundsException
      * @throws WrongTypeException
      */ 
     public void run() 
         throws SegmentationFaultException, 
                InvalidOperationException, 
                StackUnderflowException,
                NoLabelFoundException,
                OutOfBoundsException,
                WrongTypeException
     {
         for(this.PC = 0; this.PC != -1; this.PC++) exec();
     }
     
     /**
      * Function responsible for uploading the labels of PROG,
      * doint it if and only if the program is new.
      */ 
     private void upload_labels()
     {
         this.LABEL.clear();
         for(int i = 0 ;; i++)
         {
             Command c = this.PROG.elementAt(i);
             if(c == null) break;
             
             // Upload labels to HashMap.
            if(c.getLabel() != null) this.LABEL.put(c.getLabel(), i);
         }
     }
 }
