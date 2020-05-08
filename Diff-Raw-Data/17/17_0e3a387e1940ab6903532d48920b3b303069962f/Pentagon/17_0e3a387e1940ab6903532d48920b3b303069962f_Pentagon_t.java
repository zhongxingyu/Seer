 import java.io.*;
 import java.util.*;
 import java.net.*;
 
 public class Pentagon {
 	private static ServerSocket serverSocket;
 	private static FileWriter fileStream;
 	private static Client clientList[] = new Client[3];
 	private static ClientThread clientThreadList[] = new ClientThread[3];
 
 	public static void main(String[] args) throws Exception {
 		Socket client;
 
 		serverSocket = new ServerSocket(5000);
 		for(int i=0; i<3; i++) {
 			client = serverSocket.accept();
 			clientList[i] = new Client(client);
 		}
 
 		for(int i=0; i<3; i++)
 			clientList[i].gatherClientDetails();
 		for(int i=0; i<3; i++)
 			clientThreadList[i] = new ClientThread(clientList[i]);
 		for(int i=0; i<3; i++)
 			clientThreadList[i].thread.join();
 		for(int i=0; i<3; i++)
 			clientList[i].endClients();
 
 		serverSocket.close();
 	}
 
 	private static void report(String msg) throws Exception {
 		fileStream = new FileWriter("report", true);
 		fileStream.write("Pentagon → " + msg + "\n");
 		fileStream.close();
 	}
 
 	private static class Client {
 		public Socket client;
 		public String port, host;
 		public ObjectInputStream in;
 		public ObjectOutputStream out;
 
 		public Client(Socket client) throws Exception {
 			this.client = client;
			host = client.getInetAddress().getHostAddress();
 			in = new ObjectInputStream(client.getInputStream());
 			out = new ObjectOutputStream(client.getOutputStream());
 		}
 
 		public void gatherClientDetails() throws Exception {
 			report((String)in.readObject());
 			port = (String)in.readObject();
 		}
 
 		public void endClients() throws Exception {
 			out.writeObject("END");
 			in.close();
 			out.close();
 		}
 	}
 
 	private static class ClientThread implements Runnable {
 		public Client client;
 		public Thread thread;
 
 		public ClientThread(Client client) throws Exception {
 			this.client = client;
 			thread = new Thread(this);
 			thread.start();
 		}
 
 		public void run() {
 			try {
 				for(int i=0; i<3; i++)
 					if(this!=clientThreadList[i])
 						client.out.writeObject(clientList[i].host + " " + clientList[i].port);
 				report((String)client.in.readObject());
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
