 package StackTests;
 
 /**
  * This class tests the stack's add method
  *
  * @author Anna.
  *         Created 2.4.2013.
  */
 public class StackAddTest extends StackTest{
 	// IMPLEMENTED METHODS --------------------------------
 
 	@Override
 	public void prepareTest(){
		getStack().remove(-100);	
 	}
 
 	@Override
 	public void runMethod(){
 		getStack().add(-100);
 	}
 
 	@Override 
 	public String getTestName(){
 		return "Stack: Add";
 	}
 }
