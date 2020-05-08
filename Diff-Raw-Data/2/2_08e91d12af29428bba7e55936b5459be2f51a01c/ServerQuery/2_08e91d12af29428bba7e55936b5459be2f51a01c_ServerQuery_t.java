 import java.io.IOException;
 
 
 public class ServerQuery extends State{
 	public State process(String input, TCP tcp, UDPSender us, Message udpMessage, Message tcpMessage, long timeEnteredState) {
 		if (tcp.getActive() == false) {
 			System.out.println("Server disconnected");
 			return new Disconnected();
 			
 		} else if (input.startsWith(":exit")) {
 			try {
 				tcp.close();
 			} catch (IOException e) {
 			}
 			System.out.println("Disconnecting");
 			return new Disconnected();
 		} else if (tcpMessage is instanceof LookupFailed) {
 			System.out.println("There is no binding on the server for that name");
 			return new ConnectedServer();
 		} else if (tcpMessage is instanceof ServerSendsInfo) {
			System.out.println("The IP address of that name is: "+((ServerSendsInfo)tcpMessage).ip);
 			return new ConnectedServer();
 		} else {
 			return this;
 		}
 	}
 }
