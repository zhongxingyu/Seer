 package behavior;
 
 import slogo.Model;
 
 public class SetPalette implements ICommand {
     
     public static final int PARAMETER_NUMBER = 4;
     private int myColorIndex;
     private float myRedValue;
     private float myGreenValue;
     private float myBlueValue;
     
     public double move (Model model, int turtleNumber) {
         
         model.getController().getView().getTurtleArea().addToColorPalette(myColorIndex, myRedValue, myGreenValue, myBlueValue);
         return 0;
     }
     
     public void initialize (String[] information, Model model) {
         myColorIndex = Integer.parseInt(information[0]);
         myRedValue = Float.parseFloat(information[1]);
         myGreenValue = Float.parseFloat(information[2]);
         myBlueValue = Float.parseFloat(information[3]);
     }
 }
