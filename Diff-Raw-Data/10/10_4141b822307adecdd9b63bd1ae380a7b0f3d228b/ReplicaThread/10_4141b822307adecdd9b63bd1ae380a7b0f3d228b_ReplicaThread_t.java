 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.Socket;
 import java.net.SocketException;
 
 /**
  * A thread that is created whenever a Server accepts a new Socket connection
  * 
  * @author jam4879
  *
  */
 
 public class ReplicaThread extends Thread {
 	
 	private Server server;
 	private Socket sock;
 	private BufferedReader in;
 	
 	public ReplicaThread(Server serv, Socket sock)
 	{
 		in = null;
 		server = serv;
 		this.sock = sock;
 		System.out.println("starting ReplicaThread for " + server + ", listening to " + sock.getPort());
 		start();
 	}
 	
 	public void run()
 	{
 		try
 		{
 			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
 			String line;
 			while((line = in.readLine()) != null)
 			{
 				boolean keepGoing = handleReplicaMessage(line);
 				if(!keepGoing)
 					return;
 			}
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 		finally
 		{
 			try {
 				in.close();
 			} catch (NullPointerException | IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	/*
 	 * Handles a message received by this replica and returns a String to the listening thread.
 	 * Returns false if disconnect message received.
 	 */
 	private boolean handleReplicaMessage(String msg)
 	{
 		try
 		{
 			System.out.println(server + " received: " + msg);
 			if(msg.startsWith("print"))
 				server.printForUser(Integer.parseInt(msg.split(" ")[1]));
 			else if(msg.startsWith("server disconnecting"))
 			{
				System.out.println("ReplicaThread closing connection with " + server);
 				server.closeConnectionTo(Integer.parseInt(msg.split(" ")[1]));
 				in.close();
 				return false;
 			}
 			else if(msg.startsWith("client disconnecting"))
 			{
				System.out.println("ReplicaThread closing connection with client on " + sock.getPort());
 				sock.close();
 				in.close();
 				return false;
 			}
 		}
 		catch(IOException e)
 		{
 			e.printStackTrace();
 		}
 		return true;
 	}
 }
