 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.LinkedList;
 
 import javax.swing.JOptionPane;
 
 /**
  * The TCP server.
  * @author Phillip Cohen
  */
 public class Server extends NetworkDongle
 {
 	/**
 	 * The port we're running on.
 	 */
 	private int port;
 
 	/**
 	 * All of the clients connected to us.
 	 */
 	private final LinkedList<ClientInstance> clients = new LinkedList<ClientInstance>();
 
 	/**
 	 * The socket with which we listen for connections.
 	 */
 	private ServerSocket listeningSocket;
 
 	/**
 	 * The thread that listens for new connections.
 	 */
 	private final AcceptThread acceptThread = new AcceptThread();
 
 	/**
 	 * The thread that periodically updates clients' about the banner's position.
 	 */
 	private final PeriodicReflowThread reflowThread = new PeriodicReflowThread();
 
 	/**
 	 * Starts up the server on the given port.
 	 */
 	public Server( BannerController localController, int port )
 	{
 		super( localController );
 
 		// First set up the master instance of the banner (ours!).
 		reflowClients();
 		localController.start();
 
 		// Start listening for connections.
 		try
 		{
 			this.port = port;
 			listeningSocket = new ServerSocket( port );
 		}
 		catch ( final IOException e )
 		{
 			JOptionPane.showMessageDialog( null, "Could not listen on port: " + port + ".\nAnother server may already be running.", "Server error", JOptionPane.ERROR_MESSAGE );
 			return;
 		}
 
 		// We're connected!
 		connected = true;
 		System.out.println( "Server started at " + getServerIP() + "!" );
 
 		// Start the auxiliary threads.
 		acceptThread.start();
 		reflowThread.start();
 	}
 
 	/**
 	 * Returns this server's IP address and port.
 	 * @return "xxx.xxx.xxx.xxx:port"
 	 */
 	public String getServerIP()
 	{
 		try
 		{
 			final InetAddress localHost = InetAddress.getLocalHost();
 			final InetAddress[] all_IPs = InetAddress.getAllByName( localHost.getHostName() );
 			return ( all_IPs[0].toString().split( "/" ) )[1] + ":" + port;
 		}
 		catch ( final UnknownHostException e )
 		{
 			return "Unknown IP:" + port;
 		}
 	}
 
 	/**
 	 * Sends an update to all the clients to update their BannerControllers.
 	 */
 	public void reflowClients()
 	{
 		// Calculate the global width by summing all of the clients' screen widths.
 		int globalWidth = Util.getScreenWidth();
 		for ( final ClientInstance client : clients )
 			globalWidth += client.screenWidth;
 
 		// Start with our instance (we're always first on the left).
 		int currentLocation = 0;
 		localController.updateOffsetData( globalWidth, currentLocation );
 		currentLocation += Util.getScreenWidth();
 
 		// Update each of the clients.
 		for ( final ClientInstance client : clients )
		{
 			try
 			{
 				client.outputStream.writeByte( Protocol.ServerMessages.REFLOW.networkId );
 				client.outputStream.writeInt( localController.getX() ); // Global location of the banner.
 				client.outputStream.writeInt( globalWidth ); // Global size.
 				client.outputStream.writeInt( currentLocation ); // Their local offset.
 				client.outputStream.flush();
 				currentLocation += client.screenWidth;
 			}
 			catch ( final IOException e )
 			{
 			}
		}
 	}
 
 	/**
 	 * Shuts down the server.
 	 */
 	@SuppressWarnings( "deprecation" )
 	@Override
 	public void disconnect()
 	{
 		try
 		{
 			// Close all of the client sockets.
 			for ( final ClientInstance client : clients )
 				client.socket.close();
 
 			// Close the listening socket.
 			if ( listeningSocket != null )
 				listeningSocket.close();
 		}
 		catch ( final IOException e )
 		{
 			e.printStackTrace();
 		}
 
 		// Empty our client list, and stop the accept thread (the reflow thread stops automatically).
 		clients.clear();
 		if ( acceptThread != null )
 			acceptThread.stop();
 	}
 
 	@Override
 	public String toString()
 	{
 		if ( connected )
 			return "Server at " + getServerIP() + ". " + ( clients.size() > 0 ? clients.size() + " client(s)" : "" );
 		else
 			return "Disconnected server";
 	}
 
 	/**
 	 * Represents a client connected to us.
 	 */
 	private class ClientInstance
 	{
 		public Socket socket;
 
 		public DataOutputStream outputStream;
 
 		public DataInputStream inputStream;
 
 		public int screenWidth;
 
 		public ClientInstance( Socket socket ) throws IOException
 		{
 			this.socket = socket;
 			outputStream = new DataOutputStream( socket.getOutputStream() );
 			inputStream = new DataInputStream( socket.getInputStream() );
 			screenWidth = inputStream.readInt();
 		}
 	}
 
 	/**
 	 * Listens for new clients, infinitely.
 	 */
 	protected class AcceptThread extends Thread
 	{
 		@Override
 		public void run()
 		{
 			while ( connected )
 				try
 				{
 					// Wait for a new client.
 					final ClientInstance client = new ClientInstance( listeningSocket.accept() ); // Stop here until the client connects.
 
 					// Add it to the list, and update the banner's dimensions.
 					clients.addLast( client );
 					reflowClients();
 					System.out.println( "New client from " + client.socket.getInetAddress() + " connected." );
 				}
 				catch ( final IOException e )
 				{
 				}
 		}
 	}
 
 	/**
 	 * Periodically updates clients about the banner's location (for consistency).
 	 */
 	protected class PeriodicReflowThread extends Thread
 	{
 		@Override
 		public void run()
 		{
 			while ( connected )
 			{
 				reflowClients();
 				Util.safeSleep( 10 );
 			}
 		}
 	}
 }
