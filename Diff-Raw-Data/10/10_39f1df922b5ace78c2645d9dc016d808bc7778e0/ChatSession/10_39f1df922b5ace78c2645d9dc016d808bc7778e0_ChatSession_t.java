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
 	public enum Status
 	{
 		DISCONNECTED, CONNECTED, UNKNOWN_HOST, IO_EXCEPTION, NO_MD5, NO_UTF8, NOT_INI, NAME_TAKEN
 	}
 
 	protected static final int DEFAULT_PORT = 37195;
 	protected static final int TIMEOUT = 2000;
 
 	protected String hostname;
 	protected String username;
 	protected int port;
 	protected byte[] hashedPassword;
 
 	protected ArrayList<ChatSessionListener> listeners;
 	protected Sender sender;
 	protected PrintStream outStream;
 	protected Socket socket;
 	protected Thread receivingThread;
 
 	protected Status status;
 	protected boolean isClosed;
 
 	public ChatSession(String host, String username, String password)
 	{
 		status = Status.NOT_INI;
 		isClosed = true;
 		receivingThread = new Thread(this);
 		listeners = new ArrayList<ChatSessionListener>();
 		
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
 
 		this.username = username;
 
 		try
 		{
 			MessageDigest md = MessageDigest.getInstance("MD5");
 			hashedPassword = md.digest(password.getBytes("UTF-8"));
 		}
 		catch (NoSuchAlgorithmException e)
 		{
 			e.printStackTrace();
 			status = Status.NO_MD5;
 			return; // Return before connected is set to true
 		}
 		catch (UnsupportedEncodingException e)
 		{
 			e.printStackTrace();
 			status = Status.NO_UTF8;
 			return; // Return before connected is set to true
 		}
 
 		try
 		{
 			socket = new Socket(hostname, port);
 			socket.setSoTimeout(TIMEOUT);
 		}
 		catch (UnknownHostException e)
 		{
 			//e.printStackTrace();
 			status = Status.UNKNOWN_HOST;
 			return;
 		}
 		catch (IOException e)
 		{
 			//e.printStackTrace();
 			status = Status.IO_EXCEPTION;
 			return;
 		}
 
 		if (isNameTaken())
 		{
 			status = Status.NAME_TAKEN;
 			return;
 		}
 
 		status = Status.DISCONNECTED;
 	}
 	
 	public boolean send(String message)
 	{
 		return sender.send("MSG:"+message, outStream);
 	}
 	
 	public boolean open()
 	{
 		/* Check if the session is already open. */
 		if (isClosed == false)
 			return false;
 		
 		/* If the session status is not DISCONNECTED, then the
 		 * session is not ready to be opened. */
 		if (status != Status.DISCONNECTED)
 			return false;
 		
 		/* Check if the main receiving thread is already running. */
 		if (receivingThread.isAlive())
 			return false;
 		else
 		{
 			isClosed = false;
 			receivingThread.start();
 		}
 		
 		/* Create our output stream for the socket. */
 		try
 		{
 			outStream = new PrintStream(socket.getOutputStream());
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 			isClosed = true;
 			return false;
 		}
 		
 		/* Start the sender service. */
 		sender = new Sender();
 		sender.start();
 		
 		/* Try to send connect command to server. */
 		if (!sender.send("CON:"+username, outStream))
 		{
 			isClosed = true;
 			return false;
 		}
 		
 		status = Status.CONNECTED;
 		return true;
 	}
 	
 	public void close()
 	{
 		/* First check if the Session is even open. */
 		if (status == Status.DISCONNECTED || isClosed == true)
 			return;
 		
 		sender.send("DIS:", outStream);
 		isClosed = true;
 		status = Status.DISCONNECTED;
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
	
	public boolean isOpen()
	{
		return !isClosed;
	}
 
 	public void run()
 	{
 		BufferedReader in;
 		try
 		{
 			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 			while (isClosed == false)
 			{
 				if (socket.isClosed())
 				{
 					isClosed = true;					
 					break;
 				}
 				String line;
 				try
 				{
 					/* Try to read a line from the socket. */
 					line = in.readLine();
 					
 					System.out.println("RECEIVED: "+line);
 					
 					/* When a line is read update the listeners. */
 					if (isClosed == false)
 					{
						//System.out.println(listeners.size() + " listeners");
 						for(ChatSessionListener listener : listeners)
 						{
 							/* If the listener no longer exists, then remove it. */
 							if (listener == null)
 							{
 								listeners.remove(listener);
 							}
 							listener.update(line);
 						}
 					}
 				}
 				/* The read timed out. Check if the session is still open, and try to read
 				 * again if it is.
 				 */
 				catch(SocketTimeoutException e)
 				{
 					continue;
 				}
 			}
 			socket.close();
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 			isClosed = true;
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
