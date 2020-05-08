 package functions.turtle;
 
 import backEnd.Instruction;
 import backEnd.Model;
 import backEnd.Turtle;
 import backEnd.TurtleList;
 import functions.Function;
 
 public abstract class TurtleFunction extends Function {
     
    private int myInputs;
    
     public TurtleFunction (Model model, int values) {
        super(model);
        myInputs = values;
     }
     
     public abstract void process (Turtle turtle, double[] values);
     
     @Override
     public double execute (Instruction toExecute) {
         double[] values = getValues(toExecute);
         TurtleList turtles = getTurtleList();
         for (Integer id: turtles.getActiveIDs()) {
             process(turtles.get(id), values);
         }
         return getReturn(values);
     }
     
     private double getReturn (double[] values) {
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
