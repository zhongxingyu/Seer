 package backEnd;
 
 import controller.Workspace;
 
 
 public class Model {
 
     /**
      * Model object takes the user input and runs the associated command/function.
      * 
      * @author Francesco Agosti, Challen Herzberg-Brovold, Eunsu (Joe) Ryu
      */
 
     private Workspace myController;
     private Turtle myTurtle;
     private SmartMap myMap;
 
 
     public Model (Workspace controller) {
         myController = controller;
         myTurtle = new Turtle(myController);
         myMap = new SmartMap(myTurtle, this);
     }
 
     /**
      * The main purpose of this method is to clean up the input string
      * and convert it into an array, which is more convenient. It also 
      * checks for / substitutes variables. 
      * 
      * @param input String received from workspace
      * @return a tidy array of strings
      */
     public Instruction formatString(String input){
         String[] args = input.trim().toLowerCase().split("\\s+");
         return new Instruction(args);
     }
 
 
 
     /**
      * The model handles execute, processString can be called recursively to 
      * deal with situations such as repeat
      * 
      * @param s from workspace or recursive command
      * @return output that should be printed in the GUI
      * 
      */
     public double processInstruction(Instruction toExecute) {
         double output= 0;
         while(toExecute.length() != 0){
        	
             double value = process(toExecute);
             output = value;
         }
         return output;
 
     }
    
   
 
 	public double process(Instruction toExecute) {
 		Executable function = myMap.get(toExecute.get(0));
 		double value = function.execute(toExecute.progress());
 		return value;
 	}
 
     public SmartMap getMap () {
         return myMap;
     }
 }
