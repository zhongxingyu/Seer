 package client;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.net.Socket;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.TimeUnit;
 
import networking.packages.DataResponse;
 import networking.packages.Response;
 
 public class ResponseListener implements Runnable{
 	private Socket server;
 	private BlockingQueue<Response> responses;
 	
 	
 
 	public ResponseListener(Socket server, BlockingQueue<Response> responses) {
 		super();
 		this.server = server;
 		this.responses = responses;
 	}
 
 	@Override
 	public void run() {
 		ObjectInputStream ois = null;
 		
 		try {
 			while (true){	
 				ois = new ObjectInputStream(server.getInputStream());
 				Response response = (Response) ois.readObject();
 				System.out.println("response get: " + response);
 				while(!responses.offer(response, 200, TimeUnit.MILLISECONDS));
 			}
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 			// TODO Handle the server going away
 		} catch (ClassNotFoundException e) {
 			// TODO Go home and cry yourself to sleep :(
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	
 }
