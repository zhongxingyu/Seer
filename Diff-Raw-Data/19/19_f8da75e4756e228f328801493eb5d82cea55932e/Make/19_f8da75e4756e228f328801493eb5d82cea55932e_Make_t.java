 package behavior;
 
 import java.util.regex.Pattern;
 import slogo.Model;
 import exceptions.SyntaxException;
 
 
 /**
  * Make a variable
  * 
  * @author Richard Yang
  * 
  */
 
 public class Make implements ICommand {
     
     public static final int PARAMETER_NUMBER = 3;
     private Pattern myNumPattern = Pattern.compile("[0-9]*");
     private String myVariableName;
     private double myValue;
 
     @Override
     public double move (Model model, int turtleNumber) throws SyntaxException {
         model.addVariable(myVariableName, myValue);
         return myValue;
     }
 
     @Override
     public void initialize (String[] information, Model model) throws SyntaxException {
         if (information.length != 2) {
             throw new SyntaxException();
         }
         else if (myNumPattern.matcher(information[1]).matches()) { throw new SyntaxException(); }
        if (information[0].charAt(0) == ':') {
            myVariableName = information[0];
            myValue = Double.parseDouble(information[1]);
        }
        else {
            throw new SyntaxException("Need semi colon");
        }
     }
 
 }
