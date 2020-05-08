 package behavior;
 
 import slogo.Model;
 import slogo.Parser;
 import exceptions.ParameterException;
 import exceptions.SyntaxException;
 
 
 /**
  * Make a variable
  * 
  * @author Richard Yang
  * 
  */
 
 public class Make implements ICommand {
 
     public static final int PARAMETER_NUMBER = 2;
     
     private Parser myParser = new Parser();
     private String myVariableName;
     private String myValueString;
     private Double myValue;
 
     @Override
     public double move (Model model, int turtleNumber) {
         model.addVariable(myVariableName, myValueString);
         System.out.println("User variable map size: " + model.getUserVariables().size());
         System.out.print("Variable succesfully made, name: " + myVariableName + ", and value: " + myValueString);
         return myValue;
     }
 
     @Override
     public void initialize (String[] information, Model model) throws SyntaxException {
         if (information.length != 2) {
             System.out.println("this is the error");
             throw new SyntaxException("Syntax Error");
         } else if ( !myParser.getNumPattern().matcher(information[1]).matches()) {
             throw new ParameterException("Parameter Exception");
         }
        else if (information[0].charAt(0) != ':') { 
            throw new SyntaxException("Need semi colon"); 
        }
         
         myVariableName = information[0];
         myValueString = information[1];
         myValue = Double.parseDouble(information[1]);
         System.out.println("MAKE initialize successful");
     }
 
 }
