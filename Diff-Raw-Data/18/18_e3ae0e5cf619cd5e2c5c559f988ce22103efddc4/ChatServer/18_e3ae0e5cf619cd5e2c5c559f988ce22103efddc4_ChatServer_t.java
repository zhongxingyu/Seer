 package info.geared.corki.server;
 
 import info.geared.corki.net.Sender;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.util.ArrayList;
 import java.util.Scanner;
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
 	protected Sender sender;
 
 	protected static final int SERVER_SOCKET_TIMEOUT = 3000;
 	protected static final int DEFAULT_PORT = 37195;
 
 	public ChatServer()
 	{
 		running = false;
 		clients = new ArrayList<Client>();
 		serverPort = DEFAULT_PORT;
 		sender = new Sender();
 	}
 
 	public ChatServer(int serverPort)
 	{
 		running = false;
 		clients = new ArrayList<Client>();
 		this.serverPort = serverPort;
 		sender = new Sender();
 
 	}
 
 	public boolean start()
 	{
 		if (running == true)
 			return false;
 
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
 
 		/* Start the sender. */
 		sender.start();
 
 		/* Create thread pool for clients. */
 		executor = Executors.newCachedThreadPool();
 
 		/* Set running to true and start the connection receiver thread. */
 		running = true;
 		connectionThread = new Thread(this);
 		connectionThread.start();
 
 		/*
 		 * If there were not problems creating the server socket, then return
 		 * true.
 		 */
 		return true;
 	}
 
 	public boolean stop()
 	{
 		if (running == false)
 		/*
 		 * Setting running to false will cause the connectionThread to end
 		 * within SERVER_SOCKET_TIMEOUT.
 		 */
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
 
 		sender.stop();
 
 		return true;
 	}
 
 	public void run()
 	{
 		/*
 		 * Indefinitely accept connections until running is no longer true. The
 		 * condition will be checked when a connection is accepted or when the
 		 * socket times out. So the maximum running time of the thread after
 		 * running is set to false is SERVER_SOCKET_TIMEOUT ms.
 		 */
 		while (running == true)
 		{
 			try
 			{
 				//System.out.println("Waiting for a connection : " + clients.size() + " clients connected.");
 				Socket soc = serverSocket.accept();
 
 				Client client = new Client(soc);
 				client.start(executor, this);
 				clients.add(client);
 
 				System.out.println("Connection received!");
 			}
 			catch (SocketTimeoutException e)
 			{
 				/*
 				 * If the socket timed out, check that if running is still true,
 				 * then accept again.
 				 */
 				continue;
 			}
 			catch (SocketException e)
 			{
 				return;
 			}
 			catch (IOException e)
 			{
 				/*
 				 * This is non-critical, since no Client has been created at
 				 * this point.
 				 */
 				e.printStackTrace();
 			}
 		}
 	}
 
 	protected void broadcast(String message)
 	{
 		if (running == false)
 			return;
 
 		for (Client c : clients)
 		{
 			if (c.isRunning() && c.getName().isEmpty() == false)
 				c.send(message, sender);
 		}
 	}
 
 	public void receiveMessage(String message, Client client)
 	{
 		if (running == false)
 			return;
 
 		if (message.startsWith("CON:"))
 		{
 			String name = message.substring(4);
 			for (Client c : clients)
 			{
 				if (c.getName().equals(name))
 				{
					client.send("NCN:", sender);
 					return;
 				}
 			}
 			client.setName(message.substring(4));
 			System.out.println(client.getName() + " connected.");
 			broadcast(message);
 		}
 		else if (message.startsWith("MSG:"))
 		{
 			System.out.println(client.getName() + ": " + message.substring(4));
 
 			/* Send the message to all of the clients. */
 			broadcast("MSG:" + client.getName() + ":" + message.substring(4));
 		}
 		else if (message.startsWith("DIS:"))
 		{
 			System.out.println(client.getName() + " disconnected.");
 			clients.remove(client);
 			client.stop();
 			broadcast(client.getName() + " disconnected.");
 		}
 	}
 
 	public static void main(String args[]) throws InterruptedException
 	{
 		ChatServer server = new ChatServer();
 		try
 		{
 			if (!server.start())
 			{
 				System.out.println("Failed to start the server. Press enter to continue");
 			}
 			else
 			{
 				System.out.println("The server has been started. Press enter to quit.");
 			}
 
 			Scanner keyboard = new Scanner(System.in);
 			keyboard.nextLine();
 			keyboard.close();
 		}
 		finally
 		{
 			if (server.stop())
 				System.out.println("The server has been stopped.");
 		}
 	}
 }
