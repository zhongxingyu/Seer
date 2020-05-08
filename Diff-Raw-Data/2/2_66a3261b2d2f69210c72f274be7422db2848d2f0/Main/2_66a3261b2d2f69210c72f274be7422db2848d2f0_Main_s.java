 package kvgameserver;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 public class Main {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		String sPort = Configuration.getInstance().get("port");
 		int port = Integer.parseInt(sPort);
 		System.out.println("Starting server at port " + port + ".");
 		try {
 			ServerSocket ss = new ServerSocket(port);
 			System.out.println("Started.");
 			while (true) {
 				Socket csocket = ss.accept();
 				Communicator comm = new Communicator(csocket);
 				Thread commThread = new Thread(comm);
 				commThread.start();
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
