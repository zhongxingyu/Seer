 package server;
 
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.util.List;
 import java.util.concurrent.BlockingQueue;
 
 import networking.packages.Response;
 
 public class ResponseSender implements Runnable{
 	private BlockingQueue<PendingResponse> pendingResponses;
 	private List<Socket> clients;
 	
 	
 	
 	
 	public ResponseSender(BlockingQueue<PendingResponse> pendingResponses,
 			List<Socket> clients) {
 		super();
 		this.pendingResponses = pendingResponses;
 		this.clients = clients;
 		OutputController.output("ResponseSender initialized");
 	}
 
 	@Override
 	public void run() {
 		while(true){
 			try {
 				PendingResponse pendingResponse = pendingResponses.take();
 				if(pendingResponse.respondToAll){
					synchronized (clients) {
						for (Socket client : clients) {
							sendResponse(pendingResponse.getResponse(), client);
						}						
 					}
 				}
 				else{
 					sendResponse(pendingResponse.getResponse(), pendingResponse.getClientSoket());
 				}
 				
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 	}
 	
 	private void sendResponse(Response response, Socket clientSocket) {
 		ObjectOutputStream oos = null;
 
 		try {
 			oos = new ObjectOutputStream(clientSocket.getOutputStream());
 			oos.writeObject(response);
 			OutputController.output("Responded to "
 					+ clientSocket.getInetAddress() + " with " + response);
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 }
