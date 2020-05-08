 package States;
 
 import Communications.*;
 import Messages.*;
 
 
 public class Waiting extends State{
 	public static final long timeout=60000;
 	public State process(String input, TCP tcp, UDPSender us,Message udpMessage,Message tcpMessage,long timeEnteredState,boolean firstCall){
 		if(firstCall){
 			System.out.println("Waiting for a response for at most 60 seconds. To cancel, type :dc");
 		}
 		if(tcp.getActive()==true){
 			System.out.println("Connection established");
 			if(tcp.initiator){
 				return new ConnectedInitiator();
 			}
 			else{
 				return new ConnectedReceiver();
 			}
 		}
 		else if(System.currentTimeMillis()-timeEnteredState>timeout){
 			System.out.println("No response was received within the time limit");
 			return new Disconnected();
 		}
 		else{
 			return this;
 		}
 	}
 }
