 package jpfd;
 
 import org.apache.log4j.*;
 import java.io.*;
 import java.net.*;
 
 /**
  * TCPForwardServer is a simple TCP bridging software that
  * allows a TCP port on some host to be transparently forwarded
  * to some other TCP port on some other host. TCPForwardServer
  * continuously accepts client connections on the listening TCP
  * port (source port) and starts a thread (ClientThread) that
  * connects to the destination host and starts forwarding the
  * data between the client socket and destination socket.
  */
 public class TCPForwardServer implements Runnable
 {
 	private static Logger log = Logger.getLogger(TCPForwardServer.class);
	private ServerSocket serverSocket;
 
 	private String destHost = null;
 	private int destPort = 0;
 
 	public TCPForwardServer(final String listenHost, final int listenPort,
 				final String destHost, final int destPort)
 		throws Exception
 	{
 		this.destHost = destHost;
 		this.destPort = destPort;
 
 		if (listenHost.equals("*"))
 		{
 			serverSocket = new ServerSocket(listenPort);
 		}
 		else
 		{
 			serverSocket = new ServerSocket();
 			serverSocket.bind(new InetSocketAddress(listenHost, listenPort));
 		}
 
 		log.debug("successfully bound server socket to " + listenHost + ":" + listenPort);
 	}
 
 	public void run()
 	{
 		while (true)
 		{
 			try
 			{
 				Socket clientSocket = serverSocket.accept();
 				ClientThread clientThread = new ClientThread(destHost, destPort, clientSocket);
 				clientThread.start();
 			}
 			catch (IOException e)
 			{
 				log.info("caught io exception: " + e.getMessage());
 			}
 		}
 	}
 }
