 package functions;
 
 import backEnd.Instruction;
 import backEnd.Model;
 
 public class Repeat extends Function{
     
 	
     public Repeat(Model model) {
     	super(model);
     }
     
     @Override
     public double execute(Instruction toExecute) {
     	double reps = getReturnValue(toExecute);
     	Instruction blockToExecute = toExecute.block();
    	
     	for(double i = 0 ; i < reps; i ++){
     		executeBlock(blockToExecute);
     	}
     	
     	return reps;
     }
     
     
 
     
 
 }
