 package pl.edu.agh.two.mud.server;
 
 import java.io.*;
 import java.net.*;
 
 import org.apache.log4j.*;
 
 public class Service extends Thread {
 
 	private Socket clientSocket;
	private InetAddress clientAddress;
 	private Logger logger = Logger.getLogger(Service.class);
 	private ObjectOutputStream out;
 	private ObjectInputStream in;
 
 	public Service(Socket socket) throws IOException {
 		clientSocket = socket;
 		out = new ObjectOutputStream(socket.getOutputStream());
 		in = new ObjectInputStream(socket.getInputStream());
		clientAddress = clientSocket.getLocalAddress();
		logger.info("New client connected: " + socket.getLocalAddress());
 	}
 
 	@Override
 	public void run() {
 
 		try {
			out.writeObject("Connected to the server: " + clientSocket.getRemoteSocketAddress());
 			in.readObject();
 		} catch (Exception e) {
 			logger.error(clientAddress + " - " + e.getMessage());
 		}
 
 		try {
 			logger.info(clientAddress + " - shutting down client connection");
 			clientSocket.close();
 		} catch (IOException e) {
 			logger.error(clientAddress + " - closing client socket error: " + e.getMessage());
 		}
 
 	}
 }
