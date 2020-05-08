 /**
    A class that wraps up the sine function as an implementer of
    the FunctionR1R1 interface.
    @author Ryan Halbrook
    @version CS56, Spring 2013, Project
  */
 public class SineFunction implements FunctionR1R1 {
     public double evaluate(double indVar) {
 	return Math.sin(indVar);
     }
 }
