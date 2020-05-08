 package functions;
 
 import backEnd.Executable;
 import backEnd.Instruction;
 import backEnd.Model;
 import backEnd.TurtleList;
 
 public abstract class Function implements Executable {
 
     private final static int DEFAULT_INPUTS = 1;
     
     private Model myModel;
     private int myInputs;
     
     public Function(Model model){
         myModel = model;
         myInputs = DEFAULT_INPUTS;
     }
     
     public Function(Model model, int values) {
         myInputs = values;
     }
 
     public abstract double execute (Instruction toExecute);
     
     protected Model getModel(){
         return myModel;
     }
     
     protected TurtleList getTurtleList(){
     	return myModel.getTurtleList();
     }
     
     public double getReturnValue(Instruction toExectue){
         return myModel.process(toExectue);
     }
 
     public double executeBlock(Instruction blockToExecute){
         return myModel.processInstruction(blockToExecute.clone());
     }
     
     public int getInputNum(){
         return myInputs;
     }
 }
