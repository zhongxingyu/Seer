 package ch.nomoresecrets.mediastopf.server.networking;
 
 import java.io.IOException;
 import java.net.*;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 public class NetworkServer {
 
 	private ServerSocket mediastop_ServerSocket;
 	
 	public NetworkServer(int port, int thread_count) {
 		try {
 			mediastop_ServerSocket = new ServerSocket(port);
 		} catch (IOException e) {
 			System.err.println("Error: MediaStopf cannot bind to port: " + port);
 			System.err.println("Exiting...");
 			System.exit(-1);
 		}
 		
		ExecutorService exec = Executors.newFixedThreadPool(thread_count);
 		
 		while(mediastop_ServerSocket.isBound()) {	
 			try {
 				exec.execute(new NetworkServerThread(mediastop_ServerSocket.accept()));
 			} catch (IOException e) {
 				System.out.println("Error on accepting connection");
 				e.printStackTrace();
 			}
 		}
 		exec.shutdown();
 	}
 }
