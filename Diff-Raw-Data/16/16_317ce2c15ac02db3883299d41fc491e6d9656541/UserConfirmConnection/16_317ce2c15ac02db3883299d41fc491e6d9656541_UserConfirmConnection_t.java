 package States;
 
 import Communications.TCP;
 import Communications.UDPSender;
 import Messages.ClientAcceptMessage;
 import Messages.DeclineConnectMessage;
 import Messages.Message;
 
 public class UserConfirmConnection extends State{
 
 	public State process(String input, TCP tcp, UDPSender us, Message udpMessage, Message tcpMessage, long timeEnteredState,boolean firstCall) {
 		if(firstCall){
 			System.out.println("To connect to the caller type :y\nTo reject, type :n or :dc");
 		}
		if(!tcp.getActive()){
			try{
				tcp.close();
			}catch(Exception e){}
			System.out.println("The other side disconnected");
			return new Disconnected();
		}
		else if(input.startsWith(":y")){
 			tcp.send(new ClientAcceptMessage(5,Message.minSize,0,"",new byte[0]));
 			return new Chatting();
 		}
 		else if(input.startsWith(":n")){
 			tcp.send(new DeclineConnectMessage(10,Message.minSize,0,"",new byte[0]));
 			try{
 				tcp.close();
 			}
 			catch(Exception e){}
 			return new Disconnected();
 		}
 		else if(input.startsWith(":")){
 			System.out.println("Invalid command");
 			return this;
 		}
 		else if(!input.equals("")){
 			System.out.println("You cannot chat in this state");
 			return this;
 		}
 		return this;
 	}
 
 }
