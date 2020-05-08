 package functions.turtle;
 
 import functions.TurtleFunction;
 import backEnd.Executable;
 import backEnd.Model;
 import backEnd.Turtle;
 import backEnd.Instruction;
 
 public class Towards extends TurtleFunction {
 
     private static final int DEFAULT_ARGS = 2;
     
     
     public Towards(Turtle turtle, Model model){
         super(turtle, model, DEFAULT_ARGS);
     }
 
     @Override
     public double execute(Instruction toExecute) {
     	double value1 = getReturnValue(toExecute);
     	double value2 = getReturnValue(toExecute);
         double turn = getTurtle().setTowards(value1, value2);
        return turn;
     }
 
 }
