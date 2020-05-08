 package tests;
 
 import trains.CallbackCircuitChange;
 
 final class myCallbackCircuitChange implements CallbackCircuitChange{
 	@Override
	public void run(){
 		System.out.println("my CallbackCircuitChange");
 	}
 }
