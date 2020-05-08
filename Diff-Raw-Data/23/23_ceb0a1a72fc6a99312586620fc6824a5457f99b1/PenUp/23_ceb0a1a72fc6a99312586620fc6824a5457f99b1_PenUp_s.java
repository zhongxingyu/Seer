 package functions.turtle;
 
 import backEnd.Instruction;
 import functions.Function;
 import backEnd.Model;
 
 
 public class PenUp extends Function{
 
     public PenUp(Model model){
         super(model);
     }
 
     @Override
     public double execute(Instruction toExecute) {
        getModel().getActiveTurtles().setPenDown(false);
         return 0;
     }
 
 
 }
