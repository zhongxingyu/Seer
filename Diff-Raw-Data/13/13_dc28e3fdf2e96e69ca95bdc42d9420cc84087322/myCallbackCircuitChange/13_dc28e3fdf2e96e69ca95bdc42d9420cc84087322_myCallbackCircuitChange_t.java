 package tests;
 
 import trains.CallbackCircuitChange;
import trains.CircuitView;
 
 final class myCallbackCircuitChange implements CallbackCircuitChange{
 	@Override
	public void run(CircuitView cv){
 		System.out.println("my CallbackCircuitChange");
 	}
 }
