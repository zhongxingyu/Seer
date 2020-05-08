 package pollingSystem.server;
 
 import java.util.Observable;
 import java.io.*;
 import java.net.*;
 
 public class AdminListener extends Observable implements Runnable {
 	
 	public static final int PORT=5050;
 	
 	private ServerSocket serverSocket;
 	private Socket clientSocket;
 	private BufferedReader in;
 	private boolean serverRunning;
 
 	   public AdminListener()
 	   {
 		   serverSocket = null;
 	       try {
 	           serverSocket = new ServerSocket(PORT);
 	           
 	       } catch (IOException e) {
 	           e.printStackTrace(System.err);
 	           System.exit(1);
 	       }
 	   }
 	   
 	   //recieves a message from the admin client
	   public String receive()
 	   {
		   String msg="";
 		   try {
 		       clientSocket = serverSocket.accept();
 		       in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
 
 		       //while (!clientSocket.isOutputShutdown())  // isClosed(), isConnected(),isInputShutdown do not work
 		       System.out.println("Waiting for Connection");
 			   msg = in.readLine(); 
 			   System.out.println("unblocked, message is: "+msg);
 			   in.close();  
 			   clientSocket.close();
 	      
 		   } catch (SocketException e2) { e2.printStackTrace(System.err); System.exit(0); }
 		   catch (IOException e) { e.printStackTrace(System.err); System.exit(1);  }
 		   
 		   return msg;
 	   }   
 	
 	
 	@Override
 	public void run() {
 		System.out.println("Admin Listener Started");
 		serverRunning = true;
 		while(serverRunning)
 		{
 			this.notifyObservers(receive());
 		}
 		try {
 			serverSocket.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
