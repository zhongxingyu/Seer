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
     Stack   <Stackable> DATA = new Stack  <Stackable> ();
     Vector  <Integer>   CTRL = new Vector <Integer>   ();
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
         this.PROG = PROG; 
     }
     
     /**
      * Setter method created to upload a new 'program' (vector
      * of object of the class Command) in the RVM.
      * 
      * @param PROG    Vector of objects of the class Command
      * @see   Command
      */
     public void upload(Vector <Command> PROG) { this.PROG = PROG; }
     
     /**
      * Function responsable for executing the 'program', step by 
      * step, using the parameters f the object.
      * 
      * @throws SegmentationFaultException
      * @throws InvalidOperationException
      * @throws NoLabelFoundException
      */ 
     public void ctrl() 
         throws SegmentationFaultException, 
                InvalidOperationException, 
                StackUnderflowException,
                NoLabelFoundException,
                OutOfBoundsException,
                WrongTypeException
     {
         int stack = 0;
         
         //##############################################################
         //##                     UPLOAD LABELS                        ##
         //##############################################################
         Command c = this.PROG.elementAt(0);
         for(int i = 0; c != null; i++)
         {
            // Upload labels to HashMap
             if(c.getLabel() != null) 
                 this.LABEL.put(c.getLabel(), i);
             c = this.PROG.elementAt(i+1);
         }
         
        /* for(String label : LABEL.keySet()) */
        /* { */
            /* String value = String.valueOf(LABEL.get(label)); */
            /* System.out.println("label: " + label + " value: " + value); */
        /* } */

         //##############################################################
         //##                     EXECUTE CODE                         ##
         //##############################################################
         
         // Carrega primeiro comando e funções
         Command com      = this.PROG.elementAt(0);
         String  function = com.getCommand();
         
         this.PC = 0; // Zera contador
        for(;;) // while(true)
         {
            Stackable arg = com.getAttribute(); // Upload argument
             int line = ++this.PC;
             
             // Call function
             if(function != null)
             {
                if(function.equals("END")) break;
                 try { Function.call(this, function, arg); }
                 catch (Exception e) {
                     System.out.print(e);
                 }
             }
             
             com      = this.PROG.elementAt(this.PC);
             function = com.getCommand();
        } //while
     }
 }
