 package tests;
 
 import trains.CallbackUtoDeliver;
import trains.Message;
 
 final class myCallbackUtoDeliver implements CallbackUtoDeliver{
 	@Override
	public void run(int sender, Message msg){
 		System.out.println("my CallbackCircuitChange");
 	}
 }
