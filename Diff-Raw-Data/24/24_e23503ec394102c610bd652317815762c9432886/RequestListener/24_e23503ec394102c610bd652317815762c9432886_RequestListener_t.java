 package server;
 
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.net.Socket;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 import networking.packages.NetworkRequest;
 
 public class RequestListener implements Runnable {
 	private Socket clientSocket;
 	private BlockingQueue<ReceivedRequest> queue;
 
 	public RequestListener(Socket clientSocket,
 			BlockingQueue<ReceivedRequest> queue) {
 		super();
 		this.clientSocket = clientSocket;
 		this.queue = queue;
 	}
 
 	@Override
 	public void run() {
 		ObjectInputStream in;
 		try {
 			in = new ObjectInputStream(clientSocket.getInputStream());
 		} catch (IOException e) {
 			return;
 		}
 		NetworkRequest request;
 
 		try {
 			while ((request = (NetworkRequest) in.readObject()) != null) {
 				OutputController.output("Received a new request ["
 						+ request.toString() + "]");
 				ReceivedRequest rRequest = new ReceivedRequest(request, clientSocket);
 				while (!queue.offer(rRequest, 200, TimeUnit.MILLISECONDS));
 				in = new ObjectInputStream(clientSocket.getInputStream());
 			}
 		} catch (IOException | InterruptedException | ClassNotFoundException e) {
 			OutputController.output("Client disconnected " + clientSocket.getInetAddress().toString());
			synchronized (Server.clients) {
				Server.clients.remove(clientSocket);				
			}
 			
 		}
 
 	}
 
 }
