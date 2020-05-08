 package net.dandielo.stats.core;
 
 import java.io.IOException;
import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
import java.net.SocketAddress;
 
 import net.dandielo.stats.bukkit.Stats;
 
 /**
  * Creates a small and simple server that reads valid connection requests. 
  * This allows to load and update data to plugins :) 
  * 
  * @author dandielo
  */
 public class Server extends Thread {
 	
 	//Server instance
 	public static Server instance;
 	
 	// Server socket, this sockets listens for connections on the given port
 	private ServerSocket server; 
 	
 	// Server port, that will be used for connections
 	public static int port = 4447;
 	
 	// Should we stop listening to new connections?
 	private boolean stop = false;
 	
 	/**
 	 * Creates a new socket that listens for connections
 	 * @throws IOException
 	 */
 	private Server() throws IOException
 	{
		server = new ServerSocket();
 		server.setReuseAddress(true);
		server.bind(InetSocketAddress.createUnresolved("0.0.0.0", port));
 	}
 	
 	public static void init()
 	{
 		try
 		{
 			instance = new Server();
 			instance.start();
 		}
 		catch( IOException e )
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Starts listening for connection requests and validate them
 	 */
 	private void __listen()
 	{
 		//an incoming connection
 		Socket incoming;
 		
 		//
 		Stats.info("Listening to port: " + port);
 		
 		while( !stop )
 		{
 			try
 			{
 				//accept the next incoming request
 				incoming = server.accept();
 				
 				//create a request
 				Request request = new Request(incoming);
 				
 				//run the request asynchronous 
 				Thread t = new Thread(request);
 				t.start();
 			}
 			catch( IOException e )
 			{
 				e.printStackTrace();
 			}
 		}
 		
 		Stats.info("Stopped the listener");
 		
 		//close the socket
 		try
 		{
 			server.close();
 		}
 		catch( IOException e )
 		{
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void run()
 	{
 		__listen();
 	}
 	
 	public void disconnect()
 	{
 		stop = true;
 	}
 }
