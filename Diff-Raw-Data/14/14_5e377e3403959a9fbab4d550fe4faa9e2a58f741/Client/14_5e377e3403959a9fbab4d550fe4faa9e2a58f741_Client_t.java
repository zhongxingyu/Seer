 package info.geared.corki.server;
 
 import info.geared.corki.net.Sender;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.util.concurrent.ExecutorService;
 
 public class Client
 {
 	public class ClientWorker implements Runnable
 	{
 		protected ClientListener listener;
 		protected Client client;
 		
 		public ClientWorker(ClientListener listener, Client client)
 		{
 			this.listener = listener;
 			this.client = client;
 		}
 			
 		public void run()
 		{
 			while (running == true)
 			{
 				String line;
 				try
 				{
					System.out.println("Listening for client");
 					line = in.readLine();
 					listener.receiveMessage(line, client);
 				}
 				catch (SocketTimeoutException e)
 				{
 					continue; // Check if running is still true, and try another read.
 				}
 				catch (IOException e)
 				{
 					e.printStackTrace();
 				}
 			}
 		}
 
 	}
 	
 	protected String name;
 	protected boolean running;
 	
 	protected Socket socket;
 	protected PrintStream out;
 	protected BufferedReader in;
 	
 	protected ClientWorker worker;
 	protected Sender sender;
 	
 	protected static final int CLIENT_SOCKET_TIMEOUT = 3000;
 	
 	public Client(Socket socket)
 	{
 		running = false;
 		this.socket = socket;
		name = "";
 	}
 	
 	public boolean start(ExecutorService executor, ClientListener listener)
 	{
 		if (running == true)
 			return false;
 		
 		/* Try to create the input and output streams for the socket. */
 		try
 		{
 			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 			out = new PrintStream(socket.getOutputStream());
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 			return false;
 		}
 		
 		/* Set the socket timeout so the worker thread will be able to finish when running is false. */
 		try
 		{
 			socket.setSoTimeout(CLIENT_SOCKET_TIMEOUT);
 		}
 		catch (SocketException e)
 		{
 			e.printStackTrace();
 			return false;
 		}
 		
 		/* Create and start the worker thread which will listen for messages from the client. */
 		worker = new ClientWorker(listener, this);
 		executor.execute(worker);
 		
 		return true;
 	}
 	
 	public void stop()
 	{
 		if (running == false)
 			return;
 		try
 		{
 			socket.close();
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 		running = false;
 	}
 	
 	public void send(String message, Sender sender)
 	{
 		sender.send(message, out);
 	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
 }
