 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.MulticastSocket;
 
 public class Main {
 	public static void main(String[] args) throws IOException {
 		String multicastAddr = "230.0.0.1";
 		int port = 4446;
 		String nickname = "FROODY_SHOES";
 		
 		if(args.length == 3) {
 			multicastAddr = args[0];
 			port = Integer.parseInt(args[1]);
			nickname = args[2];
 		}
 		
 		MulticastSocket socket = new MulticastSocket(port);
 		socket.joinGroup(InetAddress.getByName(multicastAddr));
 
 		MembershipManager m = new MembershipManager(socket, multicastAddr, port, nickname);
 		Sender sender = new Sender(m, socket, nickname);
 		new Receiver(m, socket, sender);
 	}
 }
