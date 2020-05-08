 package pl.radical.open.gg;
 
 import pl.radical.open.gg.event.ConnectionListener;
 import pl.radical.open.gg.event.GGPacketListener;
 import pl.radical.open.gg.event.PingListener;
 import pl.radical.open.gg.packet.GGHeader;
 import pl.radical.open.gg.packet.GGIncomingPackage;
 import pl.radical.open.gg.packet.GGOutgoingPackage;
 import pl.radical.open.gg.packet.dicts.SessionState;
 import pl.radical.open.gg.packet.handlers.PacketChain;
 import pl.radical.open.gg.packet.handlers.PacketContext;
 import pl.radical.open.gg.packet.out.GGPing;
 import pl.radical.open.gg.utils.GGUtils;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.SocketAddress;
 import java.net.URL;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.event.EventListenerList;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The default implementation of <code>IConnectionService</code>.
  * <p>
  * Created on 2004-11-27
  * 
  * @author <a href="mailto:mati@sz.home.pl">Mateusz Szczap</a>
  */
 public class DefaultConnectionService implements IConnectionService {
 	private static final Logger LOG = LoggerFactory.getLogger(DefaultConnectionService.class);
 
 	private static final String WINDOWS_ENCODING = "windows-1250";
 
 	private final EventListenerList eventListenerList = new EventListenerList();
 
 	/** reference to session object */
 	private Session session = null;
 
 	private final ConcurrentLinkedQueue<GGOutgoingPackage> senderQueue = new ConcurrentLinkedQueue<GGOutgoingPackage>();
 
 	/** chain that handles packages */
 	private PacketChain packetChain = null;
 
 	/** thread that monitors connection */
 	private ConnectionThread connectionThread = null;
 
 	/** the thread that pings the connection to keep it alive */
 	private PingerThread connectionPinger = null;
 
 	private IServer server = null;
 
 	// friendly
 	DefaultConnectionService(final Session session) throws GGException {
 		if (session == null) {
 			throw new IllegalArgumentException("session cannot be null");
 		}
 		this.session = session;
 		packetChain = new PacketChain();
 	}
 
 	/**
 	 * @see pl.radical.open.gg.IConnectionService#getServer(int) Example return:
 	 * 
 	 *      <pre>
 	 * 0 0 91.197.13.78:8074 91.197.13.78
 	 * </pre>
 	 */
 	public IServer[] lookupServer(final int uin) throws GGException {
 		if (LOG.isTraceEnabled()) {
 			LOG.trace("lookupServer() executed for user [" + uin + "]");
 		}
 		try {
 			final IGGConfiguration configuration = session.getGGConfiguration();
 
			final URL url = new URL(configuration.getServerLookupURL() + "?fmnumber=" + String.valueOf(uin) + "&version=8.0.0.7669");
 			if (LOG.isDebugEnabled()) {
 				LOG.debug("GG HUB URL address: {}", url);
 			}
 
 			final HttpURLConnection con = (HttpURLConnection) url.openConnection();
 			con.setConnectTimeout(configuration.getSocketTimeoutInMiliseconds());
 			con.setReadTimeout(configuration.getSocketTimeoutInMiliseconds());
 
 			con.setDoInput(true);
 			con.connect();
 			final BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), WINDOWS_ENCODING));
 
 			final String line = reader.readLine();
 			reader.close();
 
 			if (LOG.isDebugEnabled()) {
 				LOG.debug("Dane zwrócone przez serwer: {}", line);
 			}
 
 			if (line != null && line.length() > 22) {
 				return parseAddress(line);
 			} else {
 				throw new GGException("GG HUB didn't provided a valid IP address of GG server, aborting");
 			}
 		} catch (final IOException ex) {
 			throw new GGException("Unable to get default server for uin: " + uin, ex);
 		}
 	}
 
 	/**
 	 * @see pl.radical.open.gg.IConnectionService#connect()
 	 */
 	// TODO Add HTTPS support as a fallback
 	public void connect(final IServer[] server) throws GGException { // NOPMD by LRzanek on 04.05.10 02:28
 		if (server == null) {
 			throw new GGException("Server cannot be null");
 		}
 		this.server = server[0];
 		checkConnectionState();
 		session.getSessionAccessor().setSessionState(SessionState.CONNECTING);
 		try {
 			connectionThread = new ConnectionThread();
 			connectionPinger = new PingerThread();
 			connectionThread.openConnection(server[0].getAddress(), server[0].getPort());
 			connectionPinger.startPinging();
 		} catch (final IOException ex) {
 			session.getSessionAccessor().setSessionState(SessionState.CONNECTION_ERROR);
 			throw new GGException("Unable to connect to Gadu-Gadu server: " + server[0], ex);
 		}
 	}
 
 	/**
 	 * @see pl.radical.open.gg.IConnectionService#disconnect()
 	 */
 	public void disconnect() throws GGException {
 		checkDisconnectionState();
 		session.getSessionAccessor().setSessionState(SessionState.DISCONNECTING);
 		try {
 			if (connectionPinger != null) {
 				connectionPinger.stopPinging();
 				connectionPinger = null;
 			}
 			if (connectionThread != null) {
 				connectionThread.closeConnection();
 				connectionThread = null;
 			}
 			server = null;
 			session.getSessionAccessor().setSessionState(SessionState.DISCONNECTED);
 			notifyConnectionClosed();
 		} catch (final IOException ex) {
 			LOG.error("IOException occured while trying to disconnect", ex);
 			session.getSessionAccessor().setSessionState(SessionState.CONNECTION_ERROR);
 			throw new GGException("Unable to close connection to server", ex);
 		}
 	}
 
 	private void checkDisconnectionState() throws GGSessionException {
 		if (session.getSessionState() == SessionState.DISCONNECTED) {
 			throw new GGSessionException(SessionState.DISCONNECTED);
 		}
 	}
 
 	/**
 	 * @see pl.radical.open.gg.IConnectionService#isConnected()
 	 */
 	public boolean isConnected() {
 		final boolean authenticated = session.getSessionState() == SessionState.LOGGED_IN;
 		final boolean authenticationAwaiting = session.getSessionState() == SessionState.AUTHENTICATION_AWAITING;
 		final boolean connected = session.getSessionState() == SessionState.CONNECTED;
 
 		return authenticated || authenticationAwaiting || connected;
 	}
 
 	/**
 	 * @see pl.radical.open.gg.IConnectionService#getServer()
 	 */
 	public IServer getServer() {
 		return server;
 	}
 
 	/**
 	 * @see pl.radical.open.gg.IConnectionService#addConnectionListener(pl.radical.open.gg.event.ConnectionListener)
 	 */
 	public void addConnectionListener(final ConnectionListener connListener) {
 		if (connListener == null) {
 			throw new IllegalArgumentException("connectionListener cannot be null");
 		}
 		eventListenerList.add(ConnectionListener.class, connListener);
 	}
 
 	/**
 	 * @see pl.radical.open.gg.IConnectionService#removeConnectionListener(pl.radical.open.gg.event.ConnectionListener)
 	 */
 	public void removeConnectionListener(final ConnectionListener connListener) {
 		if (connListener == null) {
 			throw new IllegalArgumentException("connectionListener cannot be null");
 		}
 		eventListenerList.remove(ConnectionListener.class, connListener);
 	}
 
 	/**
 	 * @see pl.radical.open.gg.IConnectionService#addPacketListener(pl.radical.open.gg.event.GGPacketListener)
 	 */
 	public void addPacketListener(final GGPacketListener packetListener) {
 		if (packetListener == null) {
 			throw new IllegalArgumentException("packetListener cannot be null");
 		}
 		eventListenerList.add(GGPacketListener.class, packetListener);
 	}
 
 	/**
 	 * @see pl.radical.open.gg.IConnectionService#removePacketListener(pl.radical.open.gg.event.GGPacketListener)
 	 */
 	public void removePacketListener(final GGPacketListener packetListener) {
 		if (packetListener == null) {
 			throw new IllegalArgumentException("packetListener cannot be null");
 		}
 		eventListenerList.remove(GGPacketListener.class, packetListener);
 	}
 
 	/**
 	 * @see pl.radical.open.gg.IConnectionService#addPingListener(pl.radical.open.gg.event.PingListener)
 	 */
 	public void addPingListener(final PingListener pingListener) {
 		if (pingListener == null) {
 			throw new IllegalArgumentException("pingListener cannot be null");
 		}
 		eventListenerList.add(PingListener.class, pingListener);
 	}
 
 	/**
 	 * @see pl.radical.open.gg.IConnectionService#removePingListener(pl.radical.open.gg.event.PingListener)
 	 */
 	public void removePingListener(final PingListener pingListener) {
 		if (pingListener == null) {
 			throw new IllegalArgumentException("pingListener cannot be null");
 		}
 		eventListenerList.remove(PingListener.class, pingListener);
 	}
 
 	protected void notifyConnectionEstablished() throws GGException {
 		session.getSessionAccessor().setSessionState(SessionState.AUTHENTICATION_AWAITING);
 		final ConnectionListener[] connListeners = eventListenerList.getListeners(ConnectionListener.class);
 		for (final ConnectionListener connectionListener : connListeners) {
 			connectionListener.connectionEstablished();
 		}
 		// this could be also realized as a ConnectionHandler in session class
 	}
 
 	protected void notifyConnectionClosed() throws GGException {
 		session.getSessionAccessor().setSessionState(SessionState.DISCONNECTED);
 		final ConnectionListener[] connListeners = eventListenerList.getListeners(ConnectionListener.class);
 		for (final ConnectionListener connectionListener : connListeners) {
 			connectionListener.connectionClosed();
 		}
 	}
 
 	protected void notifyConnectionError(final Exception ex) throws GGException {
 		final ConnectionListener[] connListeners = eventListenerList.getListeners(ConnectionListener.class);
 		for (final ConnectionListener connectionListener : connListeners) {
 			connectionListener.connectionError(ex);
 		}
 		session.getSessionAccessor().setSessionState(SessionState.CONNECTION_ERROR);
 	}
 
 	protected void notifyPingSent() {
 		final PingListener[] pingListeners = eventListenerList.getListeners(PingListener.class);
 		for (final PingListener pingListener : pingListeners) {
 			pingListener.pingSent(server);
 		}
 	}
 
 	protected void notifyPongReceived() {
 		final PingListener[] pingListeners = eventListenerList.getListeners(PingListener.class);
 		for (final PingListener pingListener : pingListeners) {
 			pingListener.pongReceived(server);
 		}
 	}
 
 	protected void notifyPacketReceived(final GGIncomingPackage incomingPackage) {
 		final GGPacketListener[] packetListeners = eventListenerList.getListeners(GGPacketListener.class);
 		for (final GGPacketListener packetListener : packetListeners) {
 			packetListener.receivedPacket(incomingPackage);
 		}
 	}
 
 	protected void notifyPacketSent(final GGOutgoingPackage outgoingPackage) {
 		final GGPacketListener[] packetListeners = eventListenerList.getListeners(GGPacketListener.class);
 		for (final GGPacketListener packetListener : packetListeners) {
 			packetListener.sentPacket(outgoingPackage);
 		}
 	}
 
 	protected void sendPackage(final GGOutgoingPackage outgoingPackage) throws IOException {
 		senderQueue.add(outgoingPackage);
 	}
 
 	private void checkConnectionState() throws GGSessionException {
		if (session.getSessionState() == SessionState.CONNECTION_AWAITING) {
			return;
 		}
		if (session.getSessionState() == SessionState.DISCONNECTED) {
			return;
		}
		if (session.getSessionState() == SessionState.CONNECTION_ERROR) {
			return;
		}
		throw new GGSessionException(session.getSessionState());
 	}
 
 	/**
 	 * Parses the server's address.
 	 * 
 	 * @param line
 	 *            line to be parsed.
 	 * @return <code>Server</code> the server object.
 	 */
 	private static Server[] parseAddress(final String line) {
 		if (LOG.isTraceEnabled()) {
 			LOG.trace("Parsing token information from hub: [" + line + "]");
 		}
 		final Pattern p = Pattern.compile("\\d\\s\\d\\s((?:\\d{1,3}\\.?+){4})\\:(\\d{2,4})\\s((?:\\d{1,3}\\.?+){4})");
 		final Matcher m = p.matcher(line);
 
 		if (!m.matches()) {
 			throw new IllegalArgumentException("String returned by GG HUB is not what was expected");
 		} else {
 			final Server[] servers = new Server[2];
 
 			if (LOG.isTraceEnabled()) {
 				LOG.trace("Znaleziono prawidłowy string w danych przesłanych przez GG HUB:");
 				for (int i = 1; i <= m.groupCount(); i++) {
 					LOG.trace("--->  znaleziona grupa w adresie [{}]: {}", i, m.group(i));
 				}
 			}
 
 			servers[0] = new Server(m.group(1), Integer.parseInt(m.group(2)));
 			servers[1] = new Server(m.group(3), 443);
 			return servers;
 		}
 	}
 
 	private class ConnectionThread extends Thread {
 
 		private static final int HEADER_LENGTH = 8;
 
 		private Socket socket = null;
 		private BufferedInputStream dataInput = null;
 		private BufferedOutputStream dataOutput = null;
 		private boolean active = true;
 
 		@Override
 		public void run() {
 			try {
 				while (active) {
 					handleInput();
 					if (!senderQueue.isEmpty()) {
 						handleOutput();
 					}
 					final int sleepTime = session.getGGConfiguration().getConnectionThreadSleepTimeInMiliseconds();
 					Thread.sleep(sleepTime);
 				}
 				dataInput = null;
 				dataOutput = null;
 				socket.close();
 			} catch (final Exception ex) { // FIXME Czy ten catch jest potrzebny??
 				try {
 					active = false;
 					notifyConnectionError(ex);
 				} catch (final GGException ex2) {
 					LOG.warn("Unable to notify listeners", ex);
 				}
 			}
 		}
 
 		private void handleInput() throws IOException, GGException {
 			final byte[] headerData = new byte[HEADER_LENGTH];
 			if (dataInput.available() > 0) {
 				dataInput.read(headerData);
 				decodePacket(new GGHeader(headerData));
 			}
 		}
 
 		private void handleOutput() throws IOException {
 			while (!senderQueue.isEmpty() && !socket.isClosed() && dataOutput != null) {
 				final GGOutgoingPackage outgoingPackage = senderQueue.poll();
 				sendPackage(outgoingPackage);
 				notifyPacketSent(outgoingPackage);
 			}
 		}
 
 		private boolean isActive() {
 			return active;
 		}
 
 		private void openConnection(final String host, final int port) throws IOException {
 			// add runtime checking for port
 			socket = new Socket();
 			final SocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName(host), port);
 			final int socketTimeoutInMiliseconds = session.getGGConfiguration().getSocketTimeoutInMiliseconds();
 			socket.connect(socketAddress, socketTimeoutInMiliseconds);
 			socket.setKeepAlive(true);
 			socket.setSoTimeout(socketTimeoutInMiliseconds);
 			dataInput = new BufferedInputStream(socket.getInputStream());
 			dataOutput = new BufferedOutputStream(socket.getOutputStream());
 			start();
 		}
 
 		private void closeConnection() throws IOException {
 			if (LOG.isDebugEnabled()) {
 				LOG.debug("Closing connection...");
 			}
 			active = false;
 		}
 
 		private synchronized void sendPackage(final GGOutgoingPackage op) throws IOException {
 			if (LOG.isDebugEnabled()) {
 				LOG.debug("Sending packet: {}, packetPayLoad: {}", op.getPacketType(), GGUtils.prettyBytesToString(op.getContents()));
 			}
 
 			dataOutput.write(GGUtils.intToByte(op.getPacketType()));
 			dataOutput.write(GGUtils.intToByte(op.getContents().length));
 
 			if (op.getContents().length > 0) {
 				dataOutput.write(op.getContents());
 			}
 
 			dataOutput.flush();
 		}
 
 		private void decodePacket(final GGHeader header) throws IOException, GGException {
 			final byte[] keyBytes = new byte[header.getLength()];
 			dataInput.read(keyBytes);
 			final PacketContext context = new PacketContext(session, header, keyBytes);
 			packetChain.sendToChain(context);
 		}
 
 	}
 
 	private class PingerThread extends Thread {
 
 		private boolean active = false;
 
 		/**
 		 * @see java.lang.Thread#run()
 		 */
 		@Override
 		public void run() {
 			while (active && connectionThread.isActive()) {
 				try {
 					if (LOG.isDebugEnabled()) {
 						LOG.debug("Pinging...");
 					}
 					sendPackage(GGPing.getPing());
 					notifyPingSent();
 					final int pingInterval = session.getGGConfiguration().getPingIntervalInMiliseconds();
 					Thread.sleep(pingInterval);
 				} catch (final IOException ex) {
 					active = false;
 					// LOG.error("PingerThreadError: ", ex);
 					try {
 						notifyConnectionError(ex);
 					} catch (final GGException e) {
 						LOG.warn("Unable to notify connection error listeners", ex);
 					}
 				} catch (final InterruptedException ex) {
 					active = false;
 					if (LOG.isDebugEnabled()) {
 						LOG.debug("PingerThread was interruped", ex);
 					}
 				}
 			}
 		}
 
 		private void startPinging() {
 			if (LOG.isDebugEnabled()) {
 				LOG.debug("Starting pinging...");
 			}
 			active = true;
 			start();
 		}
 
 		private void stopPinging() {
 			if (LOG.isDebugEnabled()) {
 				LOG.debug("Stopping pinging...");
 			}
 			active = false;
 		}
 	}
 
 }
