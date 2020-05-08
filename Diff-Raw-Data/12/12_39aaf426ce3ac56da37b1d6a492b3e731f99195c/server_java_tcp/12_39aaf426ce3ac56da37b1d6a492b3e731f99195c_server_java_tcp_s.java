 import java.io.*;
 import java.math.*;
 import java.net.*;
 import java.security.*;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Fricken Hamster
  * Date: 10/3/13
  * Time: 11:31 AM
  */
 public class server_java_tcp implements Runnable
 {
 	
 	/*
 	Most of the code here was adapted from a server I made for a flash game in Java. I believe I followed this tutorial at the time
 	http://www.broculos.net/2008/03/how-to-make-multi-client-flash-java.html#.UloZhhA5kgk
 	but a lot of it I changed.
 	
 	Initially I thought the server would be able to handle more than one client, and wrote it .It should still work with multiple clients but I haven't tested it.
 	
 	Usually methods have a output line used for debugging that has been commented out. That can be used to discern what the code does if its ambiguous.
 	*/
 	
 	private int port;
 	
 	private Boolean listening;
 	
 	private ServerSocket serverSocket;
 	
 	private Vector<ChatConnection> connections;
 	
 	private Dictionary<String, String> usernameDict;
 	
 	public static String WELCOME;
 
 	public static final byte CONNECT = 0x01;
 	public static final byte ACCEPTED = 0x02;
 	public static final byte PRINTALL = 0x03;
 	public static final byte SEND = 0x04;
 	public static final byte CONTENT = 0x05;
 
 	public Vector<String> messages;
 	
 	public server_java_tcp(int port)
 	{
 		this.port = port;
 		if (port < 1024 || port > 49151)
 		{
 			System.err.println("Invalid port. Terminating.");
 			System.exit(1);
 		}
 		
 		listening = false;
 		// when created server doesn't listen
 		usernameDict = new Hashtable<String, String>(8);
 		connections = new Stack<ChatConnection>();
 		messages = new Stack<String>();
 	}
 
 	@Override
 	public void run()
 	{
 		//starts the actual server listening for tcp connections
 		listening = true;
 //		System.out.println("server started on port:" + port);
 		try
 		{
 			serverSocket = new ServerSocket(port);
 			while (listening)
 			{
 				Socket socket = serverSocket.accept();
 				//Aha tcp connection recieved
 				ChatConnection cc = new ChatConnection(socket, this);
 //				System.out.println("connection from:" + socket.getRemoteSocketAddress());
 				connections.add(cc);
 				cc.run();
 			}
 		} catch (IOException e)
 		{
 			System.err.println("Could not bind port. Terminating.");
 			System.exit(1);
 		}
 	}
 	
 	public void removeConnection(ChatConnection connection)
 	{
 		connections.remove(connection);
 	}
 	
 	public void newMessage(String identifier, String msg)
 	{
 		String username = usernameDict.get(identifier);
 		if (username == null)
 			return;
 		messages.add(username + ": " + msg + "\n");
 	}
 	
 	public String generateIdentifier(String username)
 	{
 		String id = new BigInteger(130, new SecureRandom()).toString();
 		usernameDict.put(id, username);
 		return id;
 	}
 	
 	public String getMessagesString()
 	{
 		StringBuilder stringBuilder = new StringBuilder();
 		for (String message : messages)
 		{
 			stringBuilder.append(message);
 		}
 		return stringBuilder.toString();
 	}
 	
 	public static void main(String[] args)
 	{
 		if (args.length != 1)
 		{
 			System.err.println("Invalid number of args. Terminating.");
 			System.exit(0);
 		}
 		Scanner scanner = new Scanner(System.in);
		WELCOME = scanner.nextLine();
 		server_java_tcp server = new server_java_tcp(Integer.parseInt(args[0]));
 		server.run();
 	}
 	
 	private class ChatConnection extends Thread
 	{//each of these classes handles a seperate connection
 		private Socket socket;
 		private server_java_tcp server;
 		private DataOutputStream outputStream;
 		private DataInputStream inputStream;
 		
 		private String username;
 		private String identifier;
 
 		public ChatConnection(Socket socket, server_java_tcp server)
 		{
 			this.socket = socket;
 			this.server = server;
 			
 		}
 		
 		public void sendMessage(String msg)
 		{
 			try
 			{
 				ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
 				DataOutputStream out = new DataOutputStream(byteOut);
 				out.writeByte(CONTENT);
 				out.writeUTF(msg);
 				sendByteArray(byteOut.toByteArray());
 //				System.out.println("sent msg:" + msg);
 			} catch (IOException e)
 			{
 				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
 			}
 		}
 		
 		public void sendWelcome()
 		{
 			sendMessage("Welcome message: " + WELCOME);
 		}
 
 		public void sendByteArray( byte[] ba )
 		{
 			try
 			{
 				ByteArrayOutputStream bout = new ByteArrayOutputStream();
 				DataOutputStream dd = new DataOutputStream( bout );
 				dd.writeShort( ba.length );
 				dd.write( ba );
 				byte[] bb = bout.toByteArray();
 				
 				outputStream.write(bb);
 				outputStream.flush();
 			}
 			catch( Exception e )
 			{
 				//there should be an exception thrown if trying to send packet when server is down, but it was not specified in the description so I just left it blank
 			}
 		}
 
 		@Override
 		public void run()
 		{//listening for any packets
 			try
 			{
 				inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
 				outputStream = new DataOutputStream( socket.getOutputStream( ));
 
 				while ( inputStream.available() != -1 )
 				{
 					int packetSize = inputStream.readShort() ;
 					byte packetBuffer[] = new byte[packetSize];
 					int byteTrans = 0;
 					while ( byteTrans < packetSize )
 					{
 						inputStream.read( packetBuffer , byteTrans , 1 );
 						byteTrans++;
 					}//waits for entire packet
 					ByteArrayInputStream bin = new ByteArrayInputStream( packetBuffer );
 					DataInputStream packetStream = new DataInputStream(bin);
 					int id = packetStream.readByte();
 					
 					switch (id)
 					{
 						case CONNECT:
 						{//recieved new username
 							username = packetStream.readUTF();
 //							System.out.println(username);
 							this.identifier = server.generateIdentifier(username);
 							ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
 							DataOutputStream out = new DataOutputStream(byteOut);
 							out.writeByte(ACCEPTED);
 							out.writeUTF(identifier);
 							sendByteArray(byteOut.toByteArray());
 							sendWelcome();
 							break;
 						}
 						
 						case SEND:
 						{//new mesasge
 							String uuid = packetStream.readUTF();
 							String msg = packetStream.readUTF();
 							server.newMessage(uuid, msg);
 							break;
 						}
 						
 						case PRINTALL:
 						{//wants printall
 							sendMessage(server.getMessagesString());
 						}
 					}
 				}
 			}
 			catch (IOException e)
 			{
 				//connection broken
 				closeConnection();
 			}
 			
 		}
 		public void closeConnection()
 		{
 			try
 			{
 				socket.close();
 				server.removeConnection(this);
 			} catch (IOException e)
 			{
 
 			}
 		}
 	}
 }
