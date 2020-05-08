 package Server;
 
 import java.io.IOException;
 
 import Communications.TCP;
 import Messages.ClientRequestInfoMessage;
 import Messages.ClientRequestUpdateMessage;
 import Messages.ErrorMessage;
 import Messages.LookupFailedMessage;
 import Messages.Message;
 import Messages.NameCollisionMessage;
 import Messages.ServerConfirmationUpdateMessage;
 import Messages.ServerSendsInfoMessage;
 
 public class ServerReady extends ServerState{
 	public ServerState process(TCP tcp, Message tcpMessage, long timeEnteredState) {
 		if(tcpMessage instanceof ClientRequestInfoMessage && tcpMessage.getCorrect()){
 			System.out.println("info message");
 			Message message = null;
 			String user=((ClientRequestInfoMessage)tcpMessage).targetUsername;
 			String ip=LookupTable.lookup(user);
 			if(ip==null){
 				message=new LookupFailedMessage(12,LookupFailedMessage.minSize+Message.minSize,0,"",user);
 			}
 			else{
 				message=new ServerSendsInfoMessage(9,ServerSendsInfoMessage.minSize+Message.minSize,0,"",user,ip);
 			}
 			tcp.send(message);
 			return this;
 		}
 		else if(tcpMessage instanceof ClientRequestUpdateMessage && tcpMessage.getCorrect()){
 			System.out.println("update message");
 			Message message = null;
 			String user=((ClientRequestUpdateMessage)tcpMessage).senderUsername;
 			String ip=((ClientRequestUpdateMessage)tcpMessage).senderIP;
 			System.out.println("past parsing");
 			if(LookupTable.lookup(user)!=null){
 				System.out.println("already found");
 				message=new NameCollisionMessage(14,NameCollisionMessage.minSize+Message.minSize,0,"",user);
 			}
 			else{
 				System.out.println("not found");
 				LookupTable.bind(user, ip);
 				message=new ServerConfirmationUpdateMessage(7,ServerConfirmationUpdateMessage.minSize+Message.minSize,0,"",user,ip);
 			}
 			System.out.println("sending");
 			tcp.send(message);
 			return this;
 		}
 		else if(tcpMessage!=null){
			System.out.println("Invalid message");
 			tcp.send(new ErrorMessage(13,Message.minSize,0,"",new byte[0]));
 			try {
 				tcp.close();
 			} catch (IOException e) {}
 			return new ServerDisconnected();
 		}
 		else if(System.currentTimeMillis()-timeEnteredState>300000){
 			System.out.println("timout");
 			try {
 				tcp.close();
 			} catch (IOException e) {}
 			return new ServerDisconnected();
 		}
 		return this;
 	}
 }
