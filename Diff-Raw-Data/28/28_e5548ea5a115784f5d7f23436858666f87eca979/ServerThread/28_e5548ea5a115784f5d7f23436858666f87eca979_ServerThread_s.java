 package elfville.server;
 
 
 import java.net.*;
 
 import java.io.*;
 
 import elfville.protocol.*;
 
 public class ServerThread implements Runnable {
 	private Socket clientSocket;
 	private ObjectInputStream ois;
 	private ObjectOutputStream oos;
 	
 	public Integer currentUserId;	//TODO: make this better
 
 	public ServerThread(Socket client) {
 		clientSocket = client;
 		try {
 			ois = new ObjectInputStream(clientSocket.getInputStream());
 			oos = new ObjectOutputStream(clientSocket.getOutputStream());
 		} catch(Exception e1) {
 			try {
 				clientSocket.close();
 			}catch(Exception e) {
 				System.out.println(e.getMessage());
 			}
 			return;
 		}
 	}
 
 	@Override
 	public void run() {
 		while (true) {
 			try {
 				Request inMessage = (Request) ois.readObject();
 				Response outMessage = Routes.processRequest(inMessage, currentUserId);
 				oos.writeObject(outMessage);
 				oos.flush();
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				break;
 			}
 		}
 		
 		try {
 			// close connections
 			ois.close();
 			oos.close();
 			clientSocket.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
