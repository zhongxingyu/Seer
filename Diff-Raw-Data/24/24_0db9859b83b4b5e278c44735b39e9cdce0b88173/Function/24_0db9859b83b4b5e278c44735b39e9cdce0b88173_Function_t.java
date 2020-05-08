 package functions;
 
 import backEnd.Executable;
 import backEnd.Instruction;
 import backEnd.Model;
 import backEnd.TurtleList;
 
 /**
  * super class for all functions. Every function will extend this class
  * @author Francesco Agosti, Eunsu (Joe) Ryu, Challen Herzberg-Brovold
  *
  *
  */
 public abstract class Function implements Executable {
     
     private Model myModel;
     
     /**
      * 
      * @param model 
      */
     public Function(Model model) {
         myModel = model;
     }
     
     /**
      * Takes whatever action needed to carry out function
      *@param toExecute Instruction to be carried out
      */
     public abstract double execute (Instruction toExecute);
     
     protected Model getModel() {
         return myModel;
     }
     
     protected TurtleList getTurtleList() {
         return myModel.getTurtleList();
     }
     
     /**
      * gets next value to return
      * @param toExecute Instruction to get data from
      * @return 
      */
     public double getReturnValue(Instruction toExecute) {
    	return toExecute.execute(getModel().getMap());

     }
     
     /**
      * Executes an entire instruction, no matter the length
      * and returns the value corresponding to the last
      * returned value. 
      * @param toExecute 
      * @return
      */
     public double executeBlock(Instruction toExecute) {
         return toExecute.clone().process(getModel().getMap());
     }
 }
