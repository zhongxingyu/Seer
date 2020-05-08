 package server;
 
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 public class Server extends Thread {
 	/*
 	 * This is where the main method of the serverside program lives. This class
 	 * will spawn new listener-threads
 	 */
 
 	private BlockingQueue<Socket> newClientQueue;
 	private BlockingQueue<ReceivedRequest> requestQueue;
 	public static List<Socket> clients;
 	private BlockingQueue<PendingResponse> responseQueue;
 
 	public Server() {
 		responseQueue = new LinkedBlockingQueue<PendingResponse>();
 		newClientQueue = new LinkedBlockingQueue<Socket>();
 		requestQueue = new LinkedBlockingQueue<ReceivedRequest>();
 		clients = Collections.synchronizedList(new ArrayList<Socket>());
 		initializeServer();
 	}
 
 	private void initializeServer() {
 		Thread connectionListener = new Thread(new ConnectionListener(
 				newClientQueue));
 		connectionListener.start();
 
 		Thread requestHandler = new Thread(new ServerRequestHandler(
 				responseQueue, requestQueue, new DBController()));
 		
 		requestHandler.start();
 		
 		Thread sender = new Thread(new ResponseSender(responseQueue, clients));
 		sender.start();
 		OutputController.output("Server initialized");
 	}
 
 	public void run() {
 		while (true) {
 			try {
 				System.out.println(newClientQueue);
 				Socket nClient = newClientQueue.take();
 				clients.add(nClient);
 				OutputController.output("spawning new RequestListener thread");
 				(new Thread(new RequestListener(nClient, requestQueue)))
 						.start();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public static void main(String args[]) {
 		clients = Collections.synchronizedList(new ArrayList<Socket>());
 		(new Server()).start();
 	}
 
 }
