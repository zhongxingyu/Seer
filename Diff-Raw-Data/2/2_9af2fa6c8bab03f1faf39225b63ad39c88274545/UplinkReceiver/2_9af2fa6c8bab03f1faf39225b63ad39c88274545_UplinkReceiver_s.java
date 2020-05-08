 package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.uplink;
 
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.RelayServers;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.RelayServer;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.expiry.ExpireNodesTimer;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.distributor.Distributor;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers.PacketHandler;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.logger.DatabaseLogger;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.logger.DatabaseLoggerTimer;
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.signals.StopHandlerConsumer;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Required;
 
 public class UplinkReceiver extends Thread implements StopHandlerConsumer {
 	private Logger logger = Logger.getLogger(this.getClass().getName());
 
	static public int BUFFERSIZE = 2 * 1024; /* 16KB */
 
 	/** the UDP port to listen on for uplink messages */
 	private int uplinkUdpPort = -1;
 
 	/**
 	 * @param uplinkUdpPort
 	 *          the uplinkUdpPort to set
 	 */
 	@Required
 	public final void setUplinkUdpPort(int uplinkUdpPort) {
 		this.uplinkUdpPort = uplinkUdpPort;
 	}
 
 	private PacketHandler packetHandler;
 
 	/**
 	 * @param packetHandler
 	 *          the packetHandler to set
 	 */
 	@Required
 	public final void setPacketHandler(PacketHandler packetHandler) {
 		this.packetHandler = packetHandler;
 	}
 
 	private Distributor distributor;
 
 	/**
 	 * @param distributor
 	 *          the distributor to set
 	 */
 	@Required
 	public final void setDistributor(Distributor distributor) {
 		this.distributor = distributor;
 	}
 
 	private ExpireNodesTimer expireNodesTimer;
 
 	/**
 	 * @param expireNodesTimer
 	 *          the expireNodesTimer to set
 	 */
 	@Required
 	public final void setExpireNodesTimer(ExpireNodesTimer expireNodesTimer) {
 		this.expireNodesTimer = expireNodesTimer;
 	}
 
 	private DatabaseLoggerTimer databaseLoggerTimer;
 
 	/**
 	 * @param databaseLoggerTimer
 	 *          the databaseLoggerTimer to set
 	 */
 	@Required
 	public final void setDatabaseLoggerTimer(DatabaseLoggerTimer databaseLoggerTimer) {
 		this.databaseLoggerTimer = databaseLoggerTimer;
 	}
 
 	private DatabaseLogger databaseLogger;
 
 	/**
 	 * @param databaseLogger
 	 *          the databaseLogger to set
 	 */
 	@Required
 	public final void setDatabaseLogger(DatabaseLogger databaseLogger) {
 		this.databaseLogger = databaseLogger;
 	}
 
 	private RelayServers relayServers;
 
 	/**
 	 * @param relayServers
 	 *          the relayServers to set
 	 */
 	@Required
 	public final void setRelayServers(RelayServers relayServers) {
 		this.relayServers = relayServers;
 	}
 
 	private Set<RelayServer> configuredRelayServers = new HashSet<RelayServer>();
 
 	private static final String ipMatcher = "(\\d{1,3}\\.){0,3}\\d{1,3}";
 	private static final String portMatcher = "\\d{1,5}";
 	private static final String entryMatcher = "\\s*" + ipMatcher + "(:" + portMatcher + ")?\\s*";
 	private static final String matcher = "^\\s*" + entryMatcher + "(," + entryMatcher + ")*\\s*$";
 
 	/**
 	 * @param relayServers
 	 *          the relayServers to set
 	 * @throws UnknownHostException
 	 *           upon error converting an IP address or host name to an INetAddress
 	 */
 	@Required
 	public final void setConfiguredRelayServers(String relayServers) throws UnknownHostException {
 		if ((relayServers == null) || relayServers.trim().isEmpty()) {
 			this.configuredRelayServers.clear();
 			return;
 		}
 
 		if (!relayServers.matches(matcher)) {
 			throw new IllegalArgumentException("Configured relayServers string does not comply to regular expression \""
 					+ matcher + "\"");
 		}
 
 		String[] splits = relayServers.split("\\s*,\\s*");
 		for (String split : splits) {
 			String[] fields = split.split(":", 2);
 
 			InetAddress ip = InetAddress.getByName(fields[0].trim());
 
 			RelayServer relayServer = new RelayServer();
 			relayServer.setIp(ip);
 
 			if (fields.length == 2) {
 				Integer port = Integer.valueOf(fields[1].trim());
 				if ((port.intValue() <= 0) || (port.intValue() > 65535)) {
 					throw new IllegalArgumentException("Configured port " + port.intValue() + " for IP address "
 							+ ip.getHostAddress() + " is outside valid range of [1, 65535]");
 				}
 				relayServer.setPort(port);
 			}
 
 			this.configuredRelayServers.add(relayServer);
 		}
 	}
 
 	private void initRelayServers() {
 		this.configuredRelayServers.add(this.relayServers.getMe());
 
 		/* save into database */
 		for (RelayServer relayServer : this.configuredRelayServers) {
 			this.relayServers.addRelayServer(relayServer);
 		}
 	}
 
 	/*
 	 * Main
 	 */
 
 	private DatagramSocket sock;
 	private AtomicBoolean run = new AtomicBoolean(true);
 
 	public void init() throws SocketException {
 		this.setName(this.getClass().getSimpleName());
 		this.sock = new DatagramSocket(null);
 		this.sock.setReuseAddress(true);
 		this.sock.bind(new InetSocketAddress(this.uplinkUdpPort));
 		this.start();
 	}
 
 	public void uninit() {
 		signalStop();
 	}
 
 	/**
 	 * Run the relay server.
 	 */
 	@Override
 	public void run() {
 		initRelayServers();
 
 		this.databaseLoggerTimer.init();
 		this.expireNodesTimer.init();
 
 		byte[] receiveBuffer = new byte[BUFFERSIZE];
 		DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);
 
 		while (this.run.get()) {
 			try {
 				this.sock.receive(packet);
 				try {
 					if (this.packetHandler.processPacket(this.relayServers.getMe(), packet)) {
 						this.databaseLogger.log(this.logger, Level.DEBUG);
 						this.distributor.signalUpdate();
 					}
 				} catch (Throwable e) {
 					this.logger.error(e);
 				}
 			} catch (Exception e) {
 				if (!SocketException.class.equals(e.getClass())) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		if (this.sock != null) {
 			this.sock.close();
 			this.sock = null;
 		}
 	}
 
 	/*
 	 * Signal Handling
 	 */
 
 	@Override
 	public void signalStop() {
 		this.run.set(false);
 		if (this.sock != null) {
 			/* this is crude but effective */
 			this.sock.close();
 			this.sock = null;
 		}
 	}
 }
