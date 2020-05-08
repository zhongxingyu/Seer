 /**
  * 
  */
 package uk.ac.horizon.ubihelper.j2se;
 
 import java.io.IOException;
 import java.net.InetAddress;
import java.net.Inet4Address;
 import java.net.InetSocketAddress;
 import java.net.NetworkInterface;
 import java.net.ServerSocket;
 import java.nio.channels.ServerSocketChannel;
import java.util.Enumeration;
 import java.util.logging.Logger;
 
 import org.json.JSONObject;
 
 import uk.ac.horizon.ubihelper.dns.DnsClient;
 import uk.ac.horizon.ubihelper.dns.DnsProtocol;
 import uk.ac.horizon.ubihelper.dns.DnsServer;
 import uk.ac.horizon.ubihelper.dns.DnsUtils;
 import uk.ac.horizon.ubihelper.dns.DnsProtocol.SrvData;
 import uk.ac.horizon.ubihelper.net.PeerConnection;
 import uk.ac.horizon.ubihelper.net.PeerConnectionScheduler;
 import uk.ac.horizon.ubihelper.protocol.ClientInfo;
 import uk.ac.horizon.ubihelper.protocol.ProtocolManager;
 import uk.ac.horizon.ubihelper.protocol.ProtocolManager.ClientConnectionListener;
 
 /** Desktop server.
  * 
  * @author cmg
  *
  */
 public class Server {
 	static Logger logger = Logger.getLogger(Server.class.getName());
 
 	private ServerSocketChannel serverSocketChannel;
 	private PeerConnectionScheduler selector;
 	private boolean failed = false;
 	private int serverPort;
 	private DnsServer dns;
 	private MyProtocolManager protocol;
 	private ClientConnectionListener peerConnectionListener;
 	private String id;
 	
 	private static int DEFAULT_TTL = 600;
 	
 	public Server() {
 	}
	public static InetAddress getInetAddress(NetworkInterface ni) {
		// IPv4?
		Enumeration<InetAddress> as = ni.getInetAddresses();
		while (as.hasMoreElements()) {
			InetAddress a= as.nextElement();
			if (a instanceof Inet4Address)
				return a;
		}
		// any
		return ni.getInetAddresses().nextElement();
	}
 	public void init() {
 		protocol = new MyProtocolManager();
 		peerConnectionListener = new ProtocolManager.ClientConnectionListener(protocol);
 		// create channels
 		// create server socket
 		try {
 			serverSocketChannel = ServerSocketChannel.open();
 			ServerSocket ss = serverSocketChannel.socket();
 			ss.bind(new InetSocketAddress(InetAddress.getByName("0.0.0.0"),0));
 			serverPort = ss.getLocalPort();
 			logger.info("Open server socket on port "+serverPort);
 			serverSocketChannel.configureBlocking(false);
 		} catch (IOException e) {
 			logger.severe("Error opening ServerSocketChannel: "+e.getMessage());
 			failed = true;
 			return;
 		}
 		try {
 			selector = new PeerConnectionScheduler(serverSocketChannel);
 			selector.setListener(selectorListener);
 			selector.start();
 		} catch (IOException e) {
 			logger.severe("Error starting Selector: "+e.getMessage());
 			failed = true;
 			return;
 		}
 		// create and advertise with DnsServer
 		dns = new DnsServer();
 		NetworkInterface ni = DnsClient.getFirstActiveInterface();
 		dns.setNeworkInterface(ni);
		InetAddress ip = getInetAddress(ni);
 		logger.info("Binding for multicast to "+ip.getHostAddress());
 
 		id = ip.getHostAddress()+":"+serverPort;
 		
 		String servicename = DnsUtils.getServiceDiscoveryName();
 		String name = ip.getHostAddress();
 		SrvData srv = new SrvData(1, 1, serverPort, name);
 		logger.info("Discoverable "+name+" as "+servicename);
 		dns.add(new DnsProtocol.RR(servicename, DnsProtocol.TYPE_SRV, 
 				DnsProtocol.CLASS_IN, DEFAULT_TTL, DnsProtocol.srvToData(srv)));
 
 		String instancename = "Server on "+ip;
 		logger.info("Discoverable as "+instancename+" "+servicename);
 		DnsProtocol.RR ptrRR = new DnsProtocol.RR(servicename, DnsProtocol.TYPE_PTR, 
 				DnsProtocol.CLASS_IN, DEFAULT_TTL, DnsProtocol.ptrToData(instancename, servicename));
 		dns.add(ptrRR);
 
 		dns.start();
 	}
 	
 	private PeerConnectionScheduler.Listener selectorListener = new PeerConnectionScheduler.Listener() {
 		
 		public void onAccept(PeerConnectionScheduler pcs,
 				PeerConnection newPeerConnection) {
 			ClientInfo ci = new ClientInfo(newPeerConnection);
 			newPeerConnection.setOnPeerConnectionListener(peerConnectionListener);
 			newPeerConnection.attach(ci);
 			//clients.add(ci);
 			logger.info("Accepted new connection from "+newPeerConnection.getSocketChannel().socket().getInetAddress().getHostAddress()+":"+newPeerConnection.getSocketChannel().socket().getPort());			
 		}
 	};
 
 	private class MyProtocolManager extends ProtocolManager {
 
 		@Override
 		protected byte[] base64Decode(String str) {
 			return Base64.decode(str);
 		}
 
 		@Override
 		protected String base64Encode(byte[] bs) {
 			return Base64.encode(bs);
 		}
 
 		@Override
 		protected boolean clientHandlePeered(ClientInfo ci) {
 			// ...?
 			logger.info("New peer establised");
 			return false;
 		}
 
 		@Override
 		protected JSONObject getInfo() {
 			return new JSONObject();
 		}
 
 		@Override
 		protected String getName() {
 			return "Server "+id;
 		}
 
 		@Override
 		protected int getPort() {
 			return serverPort;
 		}
 
 		@Override
 		protected String getId() {
 			return id;
 		}
 		
 	}
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		Server server = new Server();
 		server.init();
 	}
 
 }
