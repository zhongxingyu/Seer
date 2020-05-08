 package functions.block;
 
 import backEnd.Instruction;
 import backEnd.Model;
 import functions.Function;
 
 public class Tell extends Function {
 
 	
 	public Tell(Model model){
 		super(model);
 	}
 	
 	public double execute(Instruction toExecute){
 		Instruction turtlesToTell = toExecute.block();
 		getTurtleList().disactivateAll();
 		double toReturn = 0;
 		while(turtlesToTell.length() > 0){
 			toReturn = getReturnValue(turtlesToTell);
 			getTurtleList().activate((int)toReturn);
 		}
 		return toReturn;
 	}
	
	
 }
 
