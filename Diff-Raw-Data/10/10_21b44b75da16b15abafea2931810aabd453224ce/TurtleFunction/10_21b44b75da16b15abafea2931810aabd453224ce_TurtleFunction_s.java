 package functions.turtle;
 
 import backEnd.Instruction;
 import backEnd.Model;
 import backEnd.Turtle;
 import backEnd.TurtleList;
 import functions.Function;
 
 
 /**
  * General class for turtle functions
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
      */
     @Override
     public double execute (Instruction toExecute) {
         double[] values = getValues(toExecute);
         TurtleList turtles = getTurtleList();
         for (Integer id: turtles.getActiveIDs()) {
             process(turtles.get(id), values);
         }
        return getReturn(values);
     }
     
    public double getReturn (double[] values) {
        return values[0];
    }
     
     private double[] getValues (Instruction toExecute) {
         double[] values = new double[myInputs];
         for (int i = 0; i < myInputs; i++) {
             values[i] = getReturnValue(toExecute);
         }
         return values;
     }
 }
