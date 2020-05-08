 package functions.turtle;
 
 import backEnd.Instruction;
 import backEnd.Model;
 import functions.Function;
 
 public class IsVisible extends Function {
 
     public IsVisible(Model model) {
         super(model);
     }
 
     @Override
     public double execute(Instruction toExecute) {
        if(getModel().getLastTurtle().isVisible()) {
             return 1;
         }
         return 0;
     }
 }
