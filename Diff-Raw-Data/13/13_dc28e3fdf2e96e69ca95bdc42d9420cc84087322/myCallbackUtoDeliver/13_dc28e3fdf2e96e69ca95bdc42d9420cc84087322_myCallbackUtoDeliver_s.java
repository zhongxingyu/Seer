 package tests;
 
 import trains.CallbackUtoDeliver;
 
 final class myCallbackUtoDeliver implements CallbackUtoDeliver{
 	@Override
	public void run(){
 		System.out.println("my CallbackCircuitChange");
 	}
 }
