 package examples;
 
 import trains.*;
 import java.util.concurrent.Semaphore;
 
 public class Example {
 
 	static boolean sender;
 	static int nbMemberMin;
 	static int delayBetweenTwoUtoBroadcast;
 	static int nbRecMsgBeforeStop;
 	static boolean terminate;
 	static int nbRecMsg = 0;
 
 
 	static Semaphore semWaitEnoughMembers;
 	static Semaphore semWaitToDie;
 
 	public static class myCallbackCircuitChange implements CallbackCircuitChange{
 
 		public myCallbackCircuitChange(){
 			//Nothing to do
 		} 
 
 		@Override
 		public void run(CircuitView cv){
 			//Printing the circuit modification
 			System.out.println("!!! ******** callbackCircuitChange called with " +  cv.getMemb() 
 					+ " members (process ");
 
 			//Printing the new/departed participant
 			if(cv.getJoined() != 0){
 				System.out.println(Integer.toString(cv.getJoined()) + " has arrived.)");
 			} else {
 				System.out.println(Integer.toString(cv.getDeparted()) + " is gone.)");
 			}
 
 			if (cv.getMemb() >= nbMemberMin){
 				System.out.println("!!! ******** enough members to start utoBroadcasting\n");
 				semWaitEnoughMembers.release();
 				System.out.println("semWaitEnoughMemebers released");
 
 			}
 		}
 	}
 
 	public static class myCallbackUtoDeliver implements CallbackUtoDeliver{
 
 		public myCallbackUtoDeliver(){
 			//Nothing to do
 		} 
 
 		@Override
 		public void run(int sender, Message msg){
 
 			if (msg.getPayload().length() != Integer.SIZE){
 				System.out.println("Payload size is incorrect: it is " + msg.getPayload().length() + " when it should be" + Integer.SIZE);
 				return;
 			}
 
 			nbRecMsg++;
 			if (nbRecMsg >= nbRecMsgBeforeStop) {
 				terminate = true;
 				semWaitToDie.release();
 				System.out.println("semWaitToDie released in UtoDeliver");
 
 			}
 
 			System.out.println("!!! " + nbRecMsg + "-ieme message (recu de " + sender + " / contenu = " + msg.getPayload() + ")");
 		}
 	}
 
 	public static void main(String args[]) {	
 
 		//trInit parameters: values by default
 		int trainsNumber = 0;
 		int wagonLength = 0;
 		int waitNb = 0;
 		int waitTime = 0;
 
 		int payload = 0;
 		int rankMessage = 0;
 		Message msg = null;
 		int exitcode;
 		int i = 0;
 		int maxConcurrentRequests = 1;
 
 		// Test parameters
 		sender = true;
 		nbMemberMin = 2;
 		delayBetweenTwoUtoBroadcast = 1000;
 		nbRecMsgBeforeStop = 10;
 		terminate = false;
 
 		//Semaphores initialization
		semWaitEnoughMembers = new Semaphore(maxConcurrentRequests, true);
		semWaitToDie = new Semaphore(maxConcurrentRequests, true);
		
		try {
			semWaitEnoughMembers.acquire();
			System.out.println("semWaitEnoughMembers acquired in main");

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 		
 		System.out.println("** Load interface");
 		Interface trin = Interface.trainsInterface();
 
 		System.out.println("** trInit");
 		exitcode = trin.JtrInit(trainsNumber, wagonLength, waitNb, waitTime,
 				myCallbackCircuitChange.class.getName(), 
 				myCallbackUtoDeliver.class.getName());
 
 		if (exitcode < 0){
 			System.out.println("JtrInit failed.");
 			return;
 		}
 
 		try {
 			semWaitEnoughMembers.acquire();
 			System.out.println("semWaitEnoughMemebers acquired in main");
 
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		if (sender){
 			while (!terminate) {
 
 				//Filling the message
 				//System.out.println("** Filling a message");
 				payload = rankMessage;
 				msg = Message.messageFromPayload(String.valueOf(payload));
 
 				if (msg == null){
 					System.out.println("Creating a message failed.");
 					return;
 				}
 
 				//Needed to keep count of the messages
 				//System.out.println("** JnewMsg");
 				trin.JnewMsg(msg.getPayload().length());
 
 				rankMessage++;
 
 				//Sending the message
 			    //System.out.println("** JutoBroadcast");
 				exitcode = trin.JutoBroadcast(msg);
 				if (exitcode < 0){
 					System.out.println("JutoBroadcast failed.");
 					return;
 				}
 
 				try {
 					Thread.sleep(delayBetweenTwoUtoBroadcast);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 			}
 		} else {
 			try {
 				semWaitToDie.acquire();
 				System.out.println("semWaitToDie acquired in main");
 
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		System.out.println("System received enough messages? game over !");
 
 		System.out.println("** JtrTerminate");
 		exitcode = trin.JtrTerminate();
 		if (exitcode < 0){
 			System.out.println("JtrTerminate failed.");
 			return;
 		}
 
 		return;
 	}
 }
