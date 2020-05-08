 package behavior;
 
 import slogo.Model;
 
 
 /**
  * Sets shape of turtle
  * 
  * @author Jerry
  * 
  */
 public class SetShape extends OneParameterCommand {
 
     /**
      * Number of parameters command takes
      */
     public static final int PARAMETER_NUMBER = 1;

     @Override
     public double move (Model model, int turtleNumber) {

         int index = (int) getMyValue();
        model.getController().getView().getTurtleArea().getMyTurtle
        (turtleNumber).setTurtleShapeSize(index);
         return index;
     }
 
 }
