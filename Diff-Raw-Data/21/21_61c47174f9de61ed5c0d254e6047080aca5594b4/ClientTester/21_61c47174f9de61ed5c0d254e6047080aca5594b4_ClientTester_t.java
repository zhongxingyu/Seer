 package FirstManda;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 public class ClientTester {
 
 	public static void main(String[] args) {
 		TCPClient TCPc = new TCPClient();
 		try {
 			System.out.println("Starting client.");
			TCPc.initialize("10.25.239.239", 4567);
 			System.out.println("Client started.");
 			TCPc.sendMessage("Hey it works? I think? Maybe? PLEASE?!?!");
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
