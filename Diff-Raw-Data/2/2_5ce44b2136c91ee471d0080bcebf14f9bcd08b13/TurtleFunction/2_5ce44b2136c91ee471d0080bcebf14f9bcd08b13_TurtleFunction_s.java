 package functions.turtle;
 
 import backEnd.Instruction;
 import backEnd.Model;
 import backEnd.Turtle;
 import backEnd.TurtleList;
 import functions.Function;
 
 
 /**
  * General class for turtle functions
  * 
  * @author Eunsu (Joe) Ryu, Challen Herzberg-Brovold, Francesco Agosti
  * 
  */
 public abstract class TurtleFunction extends Function {
 
     private int myInputs;
 
     public TurtleFunction (Model model, int values) {
         super(model);
         myInputs = values;
     }
 
     public abstract void process (Turtle turtle, double[] values);
 
     /**
      * execute() method runs over all turtles in the turtle list
      * it puts arguments in a values array and passes this on to the
      * subclass.
      */
     @Override
     public double execute (Instruction toExecute) {
         double[] values = getValues(toExecute);
         TurtleList turtles = getTurtleList();
         for (Integer id : turtles.getActiveIDs()) {
             process(turtles.get(id), values);
         }
         return getReturn(values);
 
     }
 
     /**
      * Method that knows what to return for a given class.
      * Typically overritten for functions that have unique returns.
      * 
      * @param values
      * @return
      */
     public double getReturn (double[] values) {
        if (values.length == 0)
            return 0;
         return values[0];
     }
 
     /**
      * Evaluates all parameters, even if these parameters are complex (ex: left 10),
      * and puts the returned values in an array.
      * 
      * @param toExecute
      * @return
      */
     private double[] getValues (Instruction toExecute) {
         double[] values = new double[myInputs];
         for (int i = 0; i < myInputs; i++) {
             values[i] = getReturnValue(toExecute);
         }
         return values;
     }
 }
