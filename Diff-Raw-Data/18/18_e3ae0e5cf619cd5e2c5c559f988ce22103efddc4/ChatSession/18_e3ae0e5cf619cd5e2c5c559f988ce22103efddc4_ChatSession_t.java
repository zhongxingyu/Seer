 //argo
 package info.geared.corki.client;
 
 import info.geared.corki.net.Sender;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.io.UnsupportedEncodingException;
 import java.net.Socket;
 import java.net.SocketTimeoutException;
 import java.net.UnknownHostException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 
 public class ChatSession implements Runnable
 {
 
 	private class ConnectThread implements Runnable
 	{
 
 		public void run()
 		{
 			/* Create the socket. */
 			try
 			{
 				socket = new Socket(hostname, port);
 				socket.setSoTimeout(TIMEOUT);
 			}
 			catch (UnknownHostException e)
 			{
 				// e.printStackTrace();
 				status = Status.UNKNOWN_HOST;
 				connectionListener.update("");
 				return;
 			}
 			catch (IOException e)
 			{
 				// e.printStackTrace();
 				status = Status.IO_EXCEPTION;
 				connectionListener.update("");
 				return;
 			}
 
 			/* Create our output stream for the socket. */
 			try
 			{
 				outStream = new PrintStream(socket.getOutputStream());
 			}
 			catch (IOException e)
 			{
 				e.printStackTrace();
 				status = Status.IO_EXCEPTION;
 				connectionListener.update("");
 				return;
 			}
 
 			/* Start the sender service. */
 			sender = new Sender();
 			sender.start();
 			
 			/* Check if the main receiving thread is already running. */
 			if (!receivingThread.isAlive())
 			{
 				receivingThread.start();
 			}
 
 			/* Try to send connect command to server. */
 			sender.send("CON:" + username, outStream);
 		}
 	}
 
 	public enum Status
 	{
 		CONNECTED, DISCONNECTED, UNKNOWN_HOST, IO_EXCEPTION, NO_MD5, NO_UTF8, NOT_INI, NAME_TAKEN
 	}
 
 	protected static final int DEFAULT_PORT = 37195;
 	protected static final int TIMEOUT = 2000;
 
 	protected String hostname;
 	protected String username;
 	protected int port;
 	protected byte[] hashedPassword;
 
 	protected ArrayList<ChatSessionListener> listeners;
 	protected ChatSessionListener connectionListener;
 	protected Sender sender;
 	protected PrintStream outStream;
 	protected Socket socket;
 	protected Thread receivingThread;
 
 	protected Status status;
 
 	public ChatSession(String host, String username, String password)
 	{
 		status = Status.NOT_INI;
 		receivingThread = new Thread(this);
 		listeners = new ArrayList<ChatSessionListener>();
 		this.username = username;
 
 		if (host.contains(":"))
 		{
 			hostname = host.split(":")[0];
 			port = Integer.parseInt(host.split(":")[1]);
 		}
 		else
 		{
 			hostname = host;
 			port = DEFAULT_PORT;
 		}
 
 		try
 		{
 			MessageDigest md = MessageDigest.getInstance("MD5");
 			hashedPassword = md.digest(password.getBytes("UTF-8"));
 		}
 		catch (NoSuchAlgorithmException e)
 		{
 			e.printStackTrace();
 			status = Status.NO_MD5;
 			connectionListener.update("");
 			return; // Return before connected is set to true
 		}
 		catch (UnsupportedEncodingException e)
 		{
 			e.printStackTrace();
 			status = Status.NO_UTF8;
 			connectionListener.update("");
 			return; // Return before connected is set to true
 		}
 	}
 
 	public boolean send(String message)
 	{
 		return sender.send("MSG:" + message, outStream);
 	}
 
 	public void open(ChatSessionListener connectionListener)
 	{
 		/* Check if the session is already open. */
 		if (status == Status.CONNECTED)
 			return;
 
 		this.connectionListener = connectionListener;
 
 		new Thread(new ConnectThread()).start();
 	}
 
 	public void close()
 	{
 		/* First check if the Session is even open. */
 		
 		status = Status.DISCONNECTED;
		if (sender != null)
			sender.send("DIS:", outStream);
 		try
 		{
 			Thread.sleep(2000);
 		}
 		catch (InterruptedException e)
 		{
 			e.printStackTrace();
 		}
 		try
 		{
			if (socket != null)
				socket.close();
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
		if (sender != null)
			sender.stop();	
 	}
 
 	public void addChatSessionListener(ChatSessionListener csl)
 	{
 		listeners.add(csl);
 	}
 
 	public Status getStatus()
 	{
 		return status;
 	}
 
 	public void run()
 	{
 		BufferedReader in;
 		try
 		{
 			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 			while (status != Status.DISCONNECTED && !socket.isClosed())
 			{
 				String line;
 				try
 				{
 					/* Try to read a line from the socket. */
 					line = in.readLine();
 
 					if (line == null)
 					{
 						System.out.println("Read line was null.");
 						continue;
 					}
 						
 					/* Check if we received a no-connection-name message
 					 * If so, then we need to tell the connectionListener
 					 * that our name is taken and we should disconnect.
 					 */
 					if (line.startsWith("NCN:"))
 					{
 						status = Status.NAME_TAKEN;
 						connectionListener.update("");
 					}
 					
 					/* Check for confirmation that the client was able to connect. */
 					else if (line.startsWith("CON:"))
 					{
 						if (line.substring(4).equals(username))
 						{
 							status = Status.CONNECTED;
 							connectionListener.update("");
 						}
 					}
 					
 					System.out.println(line);
 					
 					/* When a line is read update the listeners. */
 					for (ChatSessionListener listener : listeners)
 					{
 						/* If the listener no longer exists, then remove it. */
 						if (listener == null)
 						{
 							listeners.remove(listener);
 						}
 						listener.update(line);
 					}
 				}
 				/*
 				 * The read timed out. Check if the session is still open, and
 				 * try to read again if it is.
 				 */
 				catch (SocketTimeoutException e)
 				{
 					continue;
 				}
 			}
 			
 			if (status == Status.CONNECTED)
 				status = Status.DISCONNECTED;
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 			close();
 		}
 	}
 
 	protected synchronized boolean isNameTaken()
 	{
 		return false;
 	}
 
 	protected synchronized boolean connect()
 	{
 		return false;
 	}
 
 	protected synchronized void disconnect()
 	{
 	}
 }
