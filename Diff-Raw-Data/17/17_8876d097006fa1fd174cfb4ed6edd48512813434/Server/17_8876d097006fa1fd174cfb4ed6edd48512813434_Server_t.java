 package server;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 
 public class Server extends Thread {
 	private int port;
 	private ServerSocket ss;
 	private ArrayList<ClientHandlerThread> clients;
 
 	public Server(int port) {
 		this.port = port;
 		try {
 			this.ss = new ServerSocket(port);
 			System.out.println("Listening for connection");
 		} catch (IOException e) {
 			System.err.println("Failed to open server socket.");
 		}
 	}
 
 	public void run() {
 		Socket socket;
 		ClientHandlerThread client;
 		while (true) {
			try {
				System.out.println("Waiting for client");
				socket = ss.accept();
				client = new ClientHandlerThread(socket);
				clients.add(client);
				System.out.println("Got client!");
				client.start();
			} catch (IOException e) {
				System.err.println("Failed to accept client:");
				e.printStackTrace();
			}
 		}
 	}
 
 	public static void main(String[] args) {
 		if (args.length != 1) {
 			System.err.println("Wrong number of parameters!");
 			System.err.println("Usage: java Server port_number");
 			System.exit(1);
 		}
 
 		int port = Integer.parseInt(args[0]);
 		Server server = new Server(port);
 	}
 }
