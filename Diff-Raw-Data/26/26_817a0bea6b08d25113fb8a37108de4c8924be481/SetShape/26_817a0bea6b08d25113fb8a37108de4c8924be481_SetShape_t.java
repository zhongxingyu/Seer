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
    /**
     * index max value
     */
    public static final int INDEX_MAX = 5;
     @Override
     public double move (Model model, int turtleNumber) {
         int index = (int) getMyValue();
        if (index < 0 || index > INDEX_MAX) {
            model.showMessage("index must be between 1-5");
        }
        model.getController().getView().getTurtleArea().getMyTurtle(0).setTurtleShapeSize(index);
         return index;
     }
 
 }
