 package instructions.turtle;
 
 import simulation.Model;
 
 
 /**
  * represents the forward instruction
  * 
  * i.e.
  * fd 50 moves the turtle 50 pixels in its current direction
  * 
  * @author Scott Valentine
  * 
  */
 public class Forward extends Translate {
 
     
     @Override
     public int getMagnitude(Model model) {
         return nextOperand().execute(model);
     }
     
 }
