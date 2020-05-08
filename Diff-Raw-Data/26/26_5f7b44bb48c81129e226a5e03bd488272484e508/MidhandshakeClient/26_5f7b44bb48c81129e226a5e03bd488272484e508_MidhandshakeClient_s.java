 package States;
 
 import java.io.IOException;
 
 import Communications.TCP;
 import Communications.UDPSender;
 import Messages.ClientAcceptMessage;
 import Messages.DeclineConnectMessage;
 import Messages.Message;
 import Messages.ServerAcceptMessage;
 
 
 public class MidhandshakeClient extends State{
 	public State process(String input, TCP tcp, UDPSender us,Message udpMessage,Message tcpMessage,long timeEnteredState,boolean firstCall){
 		if(firstCall){
 			System.out.println("Waiting for the client to accept your invitation to chat.\nTo cancel type :dc");
 		}
 		if(tcp.getActive()==false){
 			try {
 				tcp.close();
 			} catch (Exception e) {}
 			return new Disconnected();
 		}
 		else if(tcpMessage instanceof ClientAcceptMessage && tcpMessage.getCorrect()){
 			return new Chatting();
 		}
		else if((tcpMessage instanceof ServerAcceptMessage || tcpMessage instanceof DeclineConnectMessage) && tcpMessage.getCorrect()){
 			try {
 				tcp.close();
 			} catch (IOException e) {}
			System.out.println("Expected a client response but got a server response");
 			return new Disconnected();
 		}
 		else{
 			return this;
 		}
 	}
 }
