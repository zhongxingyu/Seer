 package examples;
 
 import trains.*;
 
 public class Example {
 	
 	private static final class myCallbackCircuitChange implements CallbackCircuitChange{
 				
 		public myCallbackCircuitChange(){
 			//Nothing to do
 		} 
 		
 		@Override
 		public void run(CircuitView cv){
 			//Printing the circuit modification
 			
 			//Printing the new/departed participant
 			if(cv.getJoined() != 0){
 				System.out.println(Integer.toString(cv.getJoined()) + "has arrived.");
 			} else {
 				System.out.println(Integer.toString(cv.getDeparted()) + "is gone.");
 			}
 			
 			//Printing the current number of members
 			System.out.println("Currently " + cv.getMemb() + " in the circuit.");
 		}
 	}
 	
 	private static final class myCallbackUtoDeliver implements CallbackUtoDeliver{
 		
 		public myCallbackUtoDeliver(){
 			//Nothing to do
 		} 
 		
 		@Override
 		public void run(int sender, Message msg){
 			
 			//Printing the message sender and the content upon receiving a message
 			System.out.println("I received a message from " + sender);
 			System.out.println("The content size is " + msg.getMessageHeader().getLen());
 			System.out.println("The content is " + msg.getPayload());
 		}
 	}
 	
	public static void main() {	
 		
 		//trInit parameters: values by default
 		int trainsNumber = 0;
 		int wagonLength = 0;
 		int waitNb = 0;
 		int waitTime = 0;
 		
 		myCallbackCircuitChange mycallbackCircuit = new myCallbackCircuitChange();
 		myCallbackUtoDeliver mycallbackUto = new myCallbackUtoDeliver();
 		
 		int payload = 0;
 		Message msg = null;
 		int exitcode;
 		
 		System.out.println("** Load interface");
 		Interface trin = Interface.trainsInterface();
 				
 		System.out.println("** trInit");
 		exitcode = trin.JtrInit(trainsNumber, wagonLength, waitNb, waitTime,
 					mycallbackCircuit.getClass().getName(), 
 					mycallbackUto.getClass().getName());
 
 		if (exitcode < 0){
 			System.out.println("JtrInit failed.");
 			return;
 		}
 		
 		payload = 3;
 		
 		//Filling the message
 		System.out.println("** Filling a message");
 		msg = Message.messageFromPayload(String.valueOf(payload));
 		
 		//Needed to keep count of the messages
 		System.out.println("** JnewMsg");
 		trin.JnewMsg(msg.getMessageHeader().getLen());
 		
 		//Sending the message
 		System.out.println("** JutoBroadcast");
 		exitcode = trin.JutoBroadcast(msg);
 		if (exitcode < 0){
 			System.out.println("JutoBroadcast failed.");
 			return;
 		}
 		
 		System.out.println("JtrTerminate");
 		exitcode = trin.JtrTerminate();
 		return;
 	}
 }
