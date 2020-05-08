 package commands;
 
 import exceptions.VariableNotFoundException;
 import model.Turtle;
 
 
 public class XCor extends AbstractZeroParameterTurtleCommand {
     
     public static final int NUM_ARGS = 0;
 
     public XCor (Turtle turtle) {
         super(turtle);
     }
 
     @Override    
     public int execute () throws VariableNotFoundException {
         Turtle turtle = getTurtle();
        int position = (int) turtle.getX();
         return position;
     }
     
     @Override
     public String toString () {
         return "xcor " + getCommands().get(0).toString();
     }
 
 }
