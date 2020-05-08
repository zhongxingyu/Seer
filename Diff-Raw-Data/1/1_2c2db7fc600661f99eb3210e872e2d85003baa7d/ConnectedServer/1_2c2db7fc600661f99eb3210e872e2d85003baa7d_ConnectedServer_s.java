 package States;
 
 import Communications.*;
 import Messages.*;
 import Utilities.User;
 
 public class ConnectedServer extends State {
 	public State process(String input, TCP tcp, UDPSender us, Message udpMessage, Message tcpMessage, long timeEnteredState,boolean firstCall) {
 		if(firstCall){
 			System.out.println("You are connected to a server.\nType :update to try to bind your name to your ip.\nType :query <username> to ask the server for the ip of that username\nType :dc to disconnect");
 		}
 		if (tcp.getActive() == false) {
 			System.out.println("Server disconnected");
 			try{
 				tcp.close();
 			}
 			catch(Exception e){}
 			return new Disconnected();
 			
 		} else if (input.startsWith(":update")) {
 			Message message=new ClientRequestUpdateMessage(6,ClientRequestUpdateMessage.minSize+Message.minSize,0,"",User.userName,tcp.getIP());
 			tcp.send(message);
 			return new ServerUpdate();
 		} else if (input.startsWith(":query")) {
 			Message message=new ClientRequestInfoMessage(8,ClientRequestInfoMessage.minSize+Message.minSize,0,"",User.userName,input.substring(7).trim(),tcp.getIP());
 			System.out.println(tcp.send(message));
 			return new ServerQuery();
 		} else {
 			return this;
 		}
 	}
 }
