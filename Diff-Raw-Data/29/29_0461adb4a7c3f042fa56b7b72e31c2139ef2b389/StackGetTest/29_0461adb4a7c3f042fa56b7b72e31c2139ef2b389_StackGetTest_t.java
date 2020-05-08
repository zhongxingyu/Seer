 package StackTests;
 
 /**
  * This class tests the stack's method get
  *
  * @author Anna.
  *         Created 2.4.2013.
  */
 public class StackGetTest extends StackTest{
 	@Override
 	public void prepareTest(){
 		// Not needed.
 	}
 
 	@Override
 	public void runMethod(){
		getStack().get(0);
 	}
 
 	@Override
 	public String getTestName(){
 		return "Stack: Get";
 	}
 }
