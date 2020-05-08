 package net;
 
 /*
  * This is the connector object, it is instantiated once the game is run.
  * It is meant to serve as a tunnel between clients.
  * 
  * 
  */
 
 import java.io.IOException;
 import java.net.InetAddress;
import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
import java.net.SocketAddress;
 import java.net.UnknownHostException;
 
 public class HexConnector {
 	SocketAction MyConnection;
 	ServerSocket MyServerSocket;
 	Socket MySocket;
 	boolean isHosted;
 
 	public HexConnector() {
 		
 	}
 
 	public void setHosted(boolean isHosted){
 		this.isHosted = isHosted;
 	}
 	public boolean launch(String name, int portNumber) {
 		if (!isHosted) {
 			try {
 				System.out.println("HexConnector::(Client)Trying to create socket");
				MySocket = new Socket();
				MySocket.connect(new InetSocketAddress(InetAddress.getByName(name),portNumber));
 				System.out.println("HexConnector::(Client)Socket Created");
 				MyConnection = new SocketAction(MySocket);
 				MyConnection.start();
 				System.out.println("HexConnector::(Client)MyConnection Thread Started");
 			} catch (UnknownHostException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return false;
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return false;
 			}
 		} else {
 			try {
 				System.out.println("HexConnector::(Host)Trying to create socket");
 				MyServerSocket = new ServerSocket(portNumber);
 				MySocket = MyServerSocket.accept();
 				System.out.println("HexConnector::(Host)Socket Created");
 				MyConnection = new SocketAction(MySocket);
 				MyConnection.start();
 				System.out.println("HexConnector::(Host)MyConnection Thread Started");
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return false;
 			}
 
 		}
 		return true;
 	}
 
 	public void send(Object s) {
 		MyConnection.send(s);
 	}
 
 	public Object receive() throws IOException, ClassNotFoundException {
 		return MyConnection.receive();
 	}
 
 	public void closeConnections() {
 		MyConnection.closeConnections();
 	}
 
 	public boolean isConnected() {
 		return MyConnection.isConnected();
 	}
 
 	protected void finalize() {
 		MyConnection.finalize();
 	}
 
 }
