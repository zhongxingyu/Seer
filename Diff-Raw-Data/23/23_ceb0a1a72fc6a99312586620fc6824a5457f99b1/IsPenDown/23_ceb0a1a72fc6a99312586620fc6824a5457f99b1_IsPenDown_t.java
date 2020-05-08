 package functions.turtle;
 
 import backEnd.Instruction;
 import backEnd.Model;
 import functions.Function;
 
 public class IsPenDown extends Function {
 
     public IsPenDown(Model model) {
         super(model);
     }
 
     @Override
     public double execute(Instruction toExecute) {
        if(getModel().getLastTurtle().isPenDown()) {
             return 1;
         }
         return 0;
     }
 }
