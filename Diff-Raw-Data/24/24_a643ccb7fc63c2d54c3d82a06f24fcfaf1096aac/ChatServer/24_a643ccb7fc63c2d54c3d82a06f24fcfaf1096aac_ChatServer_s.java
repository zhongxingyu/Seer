 package info.geared.corki.server;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.SocketTimeoutException;
 import java.util.ArrayList;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 public class ChatServer implements ClientListener, Runnable
 {
 	protected ArrayList<Client> clients;
 	protected ExecutorService executor;
 	protected ServerSocket serverSocket;
 	protected int serverPort;
 	protected boolean running;
 	protected Thread connectionThread;
 	
 	protected static final int SERVER_SOCKET_TIMEOUT = 3000;
 	protected static final int DEFAULT_PORT = 37195;
 
 	public ChatServer()
 	{
 		running = false;
 		clients = new ArrayList<Client>();
 		serverPort = DEFAULT_PORT;
 	}
 	
 	public ChatServer(int serverPort)
 	{
 		running = false;
 		clients = new ArrayList<Client>();
 		this.serverPort = serverPort;
 		
 	}
 
 	public boolean start()
 	{		
 		/* Try to create the server socket. */
 		try
 		{
 			serverSocket = new ServerSocket(serverPort);
 			serverSocket.setSoTimeout(SERVER_SOCKET_TIMEOUT);
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 			return false;
 		}
 		
 		/* Create thread pool for clients. */
 		executor = Executors.newCachedThreadPool();
 		
 		/* Set running to true and start the connection receiver thread. */
 		running = true;
 		connectionThread = new Thread(this);
 		connectionThread.start();
 		
 		/* If there were not problems creating the server socket, then return true. */
 		return true;
 	}
 
 	public boolean stop()
 	{
 		/* Setting running to false will cause the connectionThread to end within SERVER_SOCKET_TIMEOUT. */
 		running = false;
 
 		/* Call disconnect on each client. While will cause their threads to end */
 		for (Client c : clients)
 		{
 			c.stop();
 		}
 
 		/* Shutdown the client thread pool. */
 		executor.shutdown();
 
 		/* Wait for each client thread to finish before returning. */
 		try
 		{
 			executor.awaitTermination(5, TimeUnit.SECONDS);
 		}
 		catch (InterruptedException e)
 		{
 			e.printStackTrace();
 			return false;
 		}
 		
 		/* Try to close the server socket. */
 		try
 		{
 			serverSocket.close();
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 			return false;
 		}
 		
 		return true;
 	}
 
 	public void run()
 	{
 		/* Indefinitely accept connections until running is no longer true.
 		 * The condition will be checked when a connection is accepted or when
 		 * the socket times out. So the maximum running time of the thread after
 		 * running is set to false is SERVER_SOCKET_TIMEOUT ms.
 		 */
 		while (running == true)
 		{
 			try
 			{
 				System.out.println("Waiting for a connection...");
				serverSocket.accept();
 				
 			}
 			catch(SocketTimeoutException e)
 			{
 				/* If the socket timed out, check that if running is still true, then accept again. */
 				continue;
 			}
 			catch(IOException e)
 			{
 				/* This is non-critical, since no Client has been created at this point. */
 				e.printStackTrace();
 			}
 		}
 	}
 
	public void receiveMessage(String message, Client name)
 	{
		// TODO Auto-generated method stub
 	}
 
 	public static void main(String args[]) throws InterruptedException
 	{
 		ChatServer server = new ChatServer();
 		try
 		{
 			if (!server.start())
 			{
 				System.out.println("Failed to start the server.");
 			}
 			else
 			{
 				System.out.println("The server has been started.");
 			}
 			while (server.running == true)
 			{
 				Thread.sleep(SERVER_SOCKET_TIMEOUT);
 			}
 		}
 		finally
 		{
 			if (server.stop())
 				System.out.println("The server has been stopped.");
 		}
 	}
 }
