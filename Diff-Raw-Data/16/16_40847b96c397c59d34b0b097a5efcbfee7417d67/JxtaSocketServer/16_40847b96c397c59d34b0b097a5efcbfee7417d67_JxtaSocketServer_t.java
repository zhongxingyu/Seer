 package experiment.jxta;
 
 import java.io.DataInput;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 import java.text.MessageFormat;
 import java.util.logging.Level;
 
 import net.jxta.document.AdvertisementFactory;
 import net.jxta.exception.PeerGroupException;
 import net.jxta.id.IDFactory;
 import net.jxta.logging.Logging;
 import net.jxta.peergroup.PeerGroup;
 import net.jxta.peergroup.PeerGroupID;
 import net.jxta.pipe.PipeID;
 import net.jxta.pipe.PipeService;
 import net.jxta.platform.NetworkManager;
 import net.jxta.protocol.PipeAdvertisement;
 import net.jxta.socket.JxtaServerSocket;
 
 public class JxtaSocketServer {
 
 	private PeerGroup netPeerGroup = null;
 	public static final String serverName = "JxtaSocketServer";
 
 	public JxtaSocketServer() throws IOException, PeerGroupException {
		NetworkManager manager = new NetworkManager(NetworkManager.ConfigMode.ADHOC, serverName, new File(new File(".jxtacache"), serverName).toURI());
 		manager.startNetwork();
 		netPeerGroup = manager.getNetPeerGroup();
 	}
 
 	public static PipeAdvertisement createSocketAdvertisement() {
 		PipeID socketServerPipeID = (PipeID) IDFactory.newPipeID(PeerGroupID.defaultNetPeerGroupID, serverName.getBytes());
 		PipeAdvertisement advertisement = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());
 		advertisement.setPipeID(socketServerPipeID);
 		advertisement.setType(PipeService.UnicastType);
 		advertisement.setName("JxtaSocketServerPipe");
 		return advertisement;
 	}
 
 	public void run() {
 		System.out.println("### Starting JxtaSocketServer");
 		JxtaServerSocket serverSocket = null;
 
 		try {
 			serverSocket = new JxtaServerSocket(netPeerGroup, createSocketAdvertisement(), 10);
 			serverSocket.setSoTimeout(0);
 			System.out.println("### Server: " + serverSocket.getLocalSocketAddress());
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 
 		int i = 1;
 		while (true) {
 			try {
 				System.out.println("### Waiting for connections");				
 				Socket socket = serverSocket.accept();				
 				if (socket != null) {
 					System.out.println("### InetAddress: " + socket.getInetAddress());
 					System.out.println("### Port: " + socket.getLocalPort());
 					System.out.println("\n### Receiving #" + i++);
 					Thread thread = new Thread(new ConnectionHandler(socket), "ConnectionHandlerThread");
 					thread.start();
 				}else{
 					System.out.println("### Socket null!! \nStill waiting...");
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private class ConnectionHandler implements Runnable {
 		Socket socket = null;
 
 		ConnectionHandler(Socket socket) {
 			this.socket = socket;
 		}
 
 		private void receiveData(Socket socket) {
 			try {
 				long start = System.currentTimeMillis();
 
 				OutputStream out = socket.getOutputStream();
 				InputStream in = socket.getInputStream();
 				DataInput dis = new DataInputStream(in);
 
 				long iterations = dis.readLong();
 				int dataSize = dis.readInt();
 				long totalSize = iterations * dataSize;				
 
 				System.out.println("### Iterations: " + iterations);
 				System.out.println("### Size: " + dataSize);
 				
 				System.out.println(MessageFormat.format("### {0}# Sending/Receiving {1} bytes in {2} times.", 
 						Thread.currentThread().getId(), totalSize, iterations));
 
 				for (int i = 0; i < iterations; i++) {
 					byte[] buf = new byte[dataSize];
 					dis.readFully(buf);
 					out.write(buf);
 					out.flush();
 				}
 				
 				out.close();
 				in.close();
 				socket.close();
 				System.out.println("### "+ Thread.currentThread().getId() + "# Socket closed");
 				
 				long finish = System.currentTimeMillis();
 				long elapsed = finish - start;
 
 				System.out.println(MessageFormat.format("### {3}# Received {0} bytes in {1} ms. Throughput = {2} KB/sec.", 
 						totalSize, elapsed,	(totalSize / elapsed) * 1000 / 1024, Thread.currentThread().getId()));
 			} catch (Exception ie) {
 				ie.printStackTrace();
 			} 
 		}
 
 		public void run() {
 			receiveData(socket);
 		}
 	}
 
 	public static void main(String args[]) {
 		System.setProperty(Logging.JXTA_LOGGING_PROPERTY, Level.SEVERE.toString());
 		try {
 			Thread.currentThread().setName(JxtaSocketServer.class.getName() + ".main()");
 			JxtaSocketServer socEx = new JxtaSocketServer();
 			socEx.run();
 		} catch (Throwable e) {
 			e.printStackTrace();
 		}
 	}
 }
