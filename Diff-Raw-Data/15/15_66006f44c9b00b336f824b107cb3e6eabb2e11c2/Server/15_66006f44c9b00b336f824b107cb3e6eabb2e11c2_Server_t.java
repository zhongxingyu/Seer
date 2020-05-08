 package Server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 
 /**
  * 
  * @author Andrea SMETKO
  * Eurecom DSMWare Project 2013 - Andrea SMETKO - Francois KY
  * Professor Y.ROUDIER
  *
  */
 
 public class Server {
 	ServerSocket server = null;
 	Socket client = null;
 	PrintWriter out = null;
 	String line = null;
 	BufferedReader in = null;
 	APIConnector apiconn;
 
 	public void listenSocket() throws IOException 
 	{
 		List<String> res = new ArrayList<String>();
 		try 
 		{
 			server = new ServerSocket(4444);
 			System.out.println("Listening on port 4444");
 
 		}
 		catch (IOException e) 
 		{
 			System.out.println("Could not listen on port 4444");
 			System.out.println(e.getMessage());
 			System.exit(-1);
 		}
 		try {
 			client = server.accept();
 		}
 		catch (IOException e) 
 		{
 			System.out.println ("Accept failed: 4444");
 			System.exit(-1);
 		}
 
 		try 
 		{
 			this.in = new BufferedReader (new InputStreamReader(client.getInputStream()));
 			out = new PrintWriter (client.getOutputStream(), true);
 		}
 		catch (IOException e) 
 		{
 			System.out.println ("Read failed");
 			System.exit(-1);
 		}
 		while (true)
 		{
 			try 
 			{
 					line = this.in.readLine();
 					apiconn = new GoogleShoppingAPIConnector();
 					res = apiconn.getItems(line);
 					apiconn = new BestBuyAPIConnector();
 					res.addAll(apiconn.getItems(line));
 					Collections.sort(res);
 					for (int i = 0; i < res.size(); i++) 
 					{
 						out.println(res.get(i));
 					}
 					out.println("\u0004");
 					out.flush();
 			}
 
 
 			catch (Exception e)
 			{
 				System.out.println("Closing the server ... ");
 				server.close();
				System.out.println("Restarting the server ... ");
 				listenSocket();
 			}
 		}
 	}
 
 }
