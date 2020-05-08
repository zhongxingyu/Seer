 package States;
 import Communications.*;
 import Messages.*;
 import Utilities.User;
 
 
 
 
 public class Connected extends State {
 	public State process(String input, TCP tcp, UDPSender us,Message udpMessage,Message tcpMessage,long timeEnteredState,boolean firstCall){
 		if(firstCall){
 			System.out.println("A connection has been established. Type :client if you are connected to a client and :server if its a server");
 		}
 		if(tcp.getActive()==false){
 			System.out.println("The otherside disconnceted");
 			return new Disconnected();
 		}
 		else if(input.startsWith(":server")){
 			tcp.send(new ServerHandShakeMessage(2,ServerHandShakeMessage.minSize+Message.minSize,0,"",User.getUserName(),tcp.getIP()));
 			return new MidhandshakeServer();
 		}
 		else if(input.startsWith(":client")){
			System.out.println("Sending Client handshake message");
 			tcp.send(new ClientHandShakeMessage(3,ClientHandShakeMessage.minSize+Message.minSize,0,"",User.getUserName(),tcp.getIP()));
 			return new MidhandshakeClient();
 		}
 		else if(input.startsWith(":")){
 			System.out.println("Invalid command");
 			return this;
 		}
 		else if(!input.equals("")){
 			System.out.println("You cannot chat yet");
 			return this;
 		}
 		else if (tcpMessage instanceof ClientHandShakeMessage && tcpMessage.getCorrect()) {
 			ClientHandShakeMessage m=(ClientHandShakeMessage)tcpMessage;
 			System.out.println("Do you wish to talk to "+m.senderUsername+" at "+m.senderUsername);
 			return new UserConfirmConnection();
 		} 
 		else if (tcpMessage instanceof ServerHandShakeMessage && tcpMessage.getCorrect()) {
 			System.out.println("The person who connected to you thinks you're a server.\nDisconnecting");
 			try{
 				tcp.close();
 			}
 			catch(Exception e){}
 			return new Disconnected();
 		}
 		else if (tcpMessage != null) {
 			tcp.send(new ErrorMessage(13,Message.minSize,0,"",new byte[0]));
 			return this;
 		}
 		else{
 			return this;
 		}
 	}
 	
 }
