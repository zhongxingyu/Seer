 package pl.radical.open.gg;
 
 import pl.radical.open.gg.event.ConnectionListener;
 import pl.radical.open.gg.event.GGPacketListener;
 import pl.radical.open.gg.event.PingListener;
 import pl.radical.open.gg.packet.GGHeader;
 import pl.radical.open.gg.packet.GGIncomingPackage;
 import pl.radical.open.gg.packet.GGOutgoingPackage;
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
 	private final static Logger log = LoggerFactory.getLogger(DefaultConnectionService.class);
 
 	private final static String WINDOWS_ENCODING = "windows-1250";
 
 	private final EventListenerList m_listeners = new EventListenerList();
 
 	/** reference to session object */
 	private Session m_session = null;
 
 	private final ConcurrentLinkedQueue<GGOutgoingPackage> m_senderQueue = new ConcurrentLinkedQueue<GGOutgoingPackage>();
 
 	/** chain that handles packages */
 	private PacketChain m_packetChain = null;
 
 	/** thread that monitors connection */
 	private ConnectionThread m_connectionThread = null;
 
 	/** the thread that pings the connection to keep it alive */
 	private PingerThread m_connectionPinger = null;
 
 	private IServer m_server = null;
 
 	// friendly
 	DefaultConnectionService(final Session session) throws GGException {
 		if (session == null) {
 			throw new GGNullPointerException("session cannot be null");
 		}
 		m_session = session;
 		m_packetChain = new PacketChain();
 	}
 
 	/**
 	 * @see pl.radical.open.gg.IConnectionService#getServer(int) Example return:
 	 * 
 	 *      <pre>
 	 * 0 0 91.197.13.78:8074 91.197.13.78
 	 * </pre>
 	 */
 	public IServer[] lookupServer(final int uin) throws GGException {
 		if (log.isTraceEnabled()) {
 			log.trace("lookupServer() executed for user [" + uin + "]");
 		}
 		try {
 			final IGGConfiguration configuration = m_session.getGGConfiguration();
 
 			final URL url = new URL(configuration.getServerLookupURL() + "?fmnumber=" + String.valueOf(uin) + "&version=8.0.0.7669");
 			if (log.isDebugEnabled()) {
 				log.debug("GG HUB URL address: {}", url);
 			}
 
 			final HttpURLConnection con = (HttpURLConnection) url.openConnection();
 			con.setConnectTimeout(configuration.getSocketTimeoutInMiliseconds());
 			con.setReadTimeout(configuration.getSocketTimeoutInMiliseconds());
 
 			con.setDoInput(true);
 			con.connect();
 			final BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), WINDOWS_ENCODING));
 
 			final String line = reader.readLine();
 			reader.close();
 
 			if (log.isDebugEnabled()) {
 				log.debug("Dane zwrócone przez serwer: {}", line);
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
 	public void connect(final IServer[] server) throws GGException {
 		if (server == null) {
 			throw new GGException("Server cannot be null");
 		}
 		m_server = server[0];
 		checkConnectionState();
 		m_session.getSessionAccessor().setSessionState(SessionState.CONNECTING);
 		try {
 			m_connectionThread = new ConnectionThread();
 			m_connectionPinger = new PingerThread();
 			m_connectionThread.openConnection(server[0].getAddress(), server[0].getPort());
 			m_connectionPinger.startPinging();
 		} catch (final IOException ex) {
 			m_session.getSessionAccessor().setSessionState(SessionState.CONNECTION_ERROR);
			throw new GGException("Unable to connect to Gadu-Gadu server: " + server[0], ex);
 		}
 	}
 
 	/**
 	 * @see pl.radical.open.gg.IConnectionService#disconnect()
 	 */
 	public void disconnect() throws GGException {
 		checkDisconnectionState();
 		m_session.getSessionAccessor().setSessionState(SessionState.DISCONNECTING);
 		try {
 			if (m_connectionPinger != null) {
 				m_connectionPinger.stopPinging();
 				m_connectionPinger = null;
 			}
 			if (m_connectionThread != null) {
 				m_connectionThread.closeConnection();
 				m_connectionThread = null;
 			}
 			m_server = null;
 			m_session.getSessionAccessor().setSessionState(SessionState.DISCONNECTED);
 			notifyConnectionClosed();
 		} catch (final IOException ex) {
 			log.error("IOException occured while trying to disconnect", ex);
 			m_session.getSessionAccessor().setSessionState(SessionState.CONNECTION_ERROR);
 			throw new GGException("Unable to close connection to server", ex);
 		}
 	}
 
 	private void checkDisconnectionState() throws GGSessionException {
 		if (m_session.getSessionState() == SessionState.DISCONNECTED) {
 			throw new GGSessionException(SessionState.DISCONNECTED);
 		}
 	}
 
 	/**
 	 * @see pl.radical.open.gg.IConnectionService#isConnected()
 	 */
 	public boolean isConnected() {
 		final boolean authenticated = m_session.getSessionState() == SessionState.LOGGED_IN;
 		final boolean authenticationAwaiting = m_session.getSessionState() == SessionState.AUTHENTICATION_AWAITING;
 		final boolean connected = m_session.getSessionState() == SessionState.CONNECTED;
 
 		return authenticated || authenticationAwaiting || connected;
 	}
 
 	/**
 	 * @see pl.radical.open.gg.IConnectionService#getServer()
 	 */
 	public IServer getServer() {
 		return m_server;
 	}
 
 	/**
 	 * @see pl.radical.open.gg.IConnectionService#addConnectionListener(pl.radical.open.gg.event.ConnectionListener)
 	 */
 	public void addConnectionListener(final ConnectionListener connectionListener) {
 		if (connectionListener == null) {
 			throw new GGNullPointerException("connectionListener cannot be null");
 		}
 		m_listeners.add(ConnectionListener.class, connectionListener);
 	}
 
 	/**
 	 * @see pl.radical.open.gg.IConnectionService#removeConnectionListener(pl.radical.open.gg.event.ConnectionListener)
 	 */
 	public void removeConnectionListener(final ConnectionListener connectionListener) {
 		if (connectionListener == null) {
 			throw new GGNullPointerException("connectionListener cannot be null");
 		}
 		m_listeners.remove(ConnectionListener.class, connectionListener);
 	}
 
 	/**
 	 * @see pl.radical.open.gg.IConnectionService#addPacketListener(pl.radical.open.gg.event.GGPacketListener)
 	 */
 	public void addPacketListener(final GGPacketListener packetListener) {
 		if (packetListener == null) {
 			throw new GGNullPointerException("packetListener cannot be null");
 		}
 		m_listeners.add(GGPacketListener.class, packetListener);
 	}
 
 	/**
 	 * @see pl.radical.open.gg.IConnectionService#removePacketListener(pl.radical.open.gg.event.GGPacketListener)
 	 */
 	public void removePacketListener(final GGPacketListener packetListener) {
 		if (packetListener == null) {
 			throw new GGNullPointerException("packetListener cannot be null");
 		}
 		m_listeners.remove(GGPacketListener.class, packetListener);
 	}
 
 	/**
 	 * @see pl.radical.open.gg.IConnectionService#addPingListener(pl.radical.open.gg.event.PingListener)
 	 */
 	// FIXME IllegalArgumentException
 	public void addPingListener(final PingListener pingListener) {
 		if (pingListener == null) {
 			throw new IllegalArgumentException("pingListener cannot be null");
 		}
 		m_listeners.add(PingListener.class, pingListener);
 	}
 
 	/**
 	 * @see pl.radical.open.gg.IConnectionService#removePingListener(pl.radical.open.gg.event.PingListener)
 	 */
 	// FIXME IllegalArgumentException
 	public void removePingListener(final PingListener pingListener) {
 		if (pingListener == null) {
 			throw new IllegalArgumentException("pingListener cannot be null");
 		}
 		m_listeners.remove(PingListener.class, pingListener);
 	}
 
 	protected void notifyConnectionEstablished() throws GGException {
 		m_session.getSessionAccessor().setSessionState(SessionState.AUTHENTICATION_AWAITING);
 		final ConnectionListener[] connectionListeners = m_listeners.getListeners(ConnectionListener.class);
 		for (final ConnectionListener connectionListener : connectionListeners) {
 			connectionListener.connectionEstablished();
 		}
 		// this could be also realized as a ConnectionHandler in session class
 	}
 
 	protected void notifyConnectionClosed() throws GGException {
 		m_session.getSessionAccessor().setSessionState(SessionState.DISCONNECTED);
 		final ConnectionListener[] connectionListeners = m_listeners.getListeners(ConnectionListener.class);
 		for (final ConnectionListener connectionListener : connectionListeners) {
 			connectionListener.connectionClosed();
 		}
 	}
 
 	protected void notifyConnectionError(final Exception ex) throws GGException {
 		final ConnectionListener[] connectionListeners = m_listeners.getListeners(ConnectionListener.class);
 		for (final ConnectionListener connectionListener : connectionListeners) {
 			connectionListener.connectionError(ex);
 		}
 		m_session.getSessionAccessor().setSessionState(SessionState.CONNECTION_ERROR);
 	}
 
 	protected void notifyPingSent() {
 		final PingListener[] pingListeners = m_listeners.getListeners(PingListener.class);
 		for (final PingListener pingListener : pingListeners) {
 			pingListener.pingSent(m_server);
 		}
 	}
 
 	protected void notifyPongReceived() {
 		final PingListener[] pingListeners = m_listeners.getListeners(PingListener.class);
 		for (final PingListener pingListener : pingListeners) {
 			pingListener.pongReceived(m_server);
 		}
 	}
 
 	protected void notifyPacketReceived(final GGIncomingPackage incomingPackage) {
 		final GGPacketListener[] packetListeners = m_listeners.getListeners(GGPacketListener.class);
 		for (final GGPacketListener packetListener : packetListeners) {
 			packetListener.receivedPacket(incomingPackage);
 		}
 	}
 
 	protected void notifyPacketSent(final GGOutgoingPackage outgoingPackage) {
 		final GGPacketListener[] packetListeners = m_listeners.getListeners(GGPacketListener.class);
 		for (final GGPacketListener packetListener : packetListeners) {
 			packetListener.sentPacket(outgoingPackage);
 		}
 	}
 
 	protected void sendPackage(final GGOutgoingPackage outgoingPackage) throws IOException {
 		m_senderQueue.add(outgoingPackage);
 	}
 
 	private void checkConnectionState() throws GGSessionException {
 		if (m_session.getSessionState() == SessionState.CONNECTION_AWAITING) {
 			return;
 		}
 		if (m_session.getSessionState() == SessionState.DISCONNECTED) {
 			return;
 		}
 		if (m_session.getSessionState() == SessionState.CONNECTION_ERROR) {
 			return;
 		}
 		throw new GGSessionException(m_session.getSessionState());
 	}
 
 	/**
 	 * Parses the server's address.
 	 * 
 	 * @param line
 	 *            line to be parsed.
 	 * @return <code>Server</code> the server object.
 	 */
 	private static Server[] parseAddress(final String line) {
 		if (log.isTraceEnabled()) {
 			log.trace("Parsing token information from hub: [" + line + "]");
 		}
 		final Pattern p = Pattern.compile("\\d\\s\\d\\s((?:\\d{1,3}\\.?+){4})\\:(\\d{2,4})\\s((?:\\d{1,3}\\.?+){4})");
 		final Matcher m = p.matcher(line);
 
 		if (!m.matches()) {
 			throw new IllegalArgumentException("String returned by GG HUB is not what was expected");
 		} else {
 			final Server[] servers = new Server[2];
 
 			if (log.isTraceEnabled()) {
 				log.trace("Znaleziono prawidłowy string w danych przesłanych przez GG HUB:");
 				for (int i = 1; i <= m.groupCount(); i++) {
 					log.trace("--->  znaleziona grupa w adresie [{}]: {}", i, m.group(i));
 				}
 			}
 
 			servers[0] = new Server(m.group(1), Integer.parseInt(m.group(2)));
 			servers[1] = new Server(m.group(3), 443);
 			return servers;
 		}
 	}
 
 	private class ConnectionThread extends Thread {
 
 		private static final int HEADER_LENGTH = 8;
 
 		private Socket m_socket = null;
 		private BufferedInputStream m_dataInput = null;
 		private BufferedOutputStream m_dataOutput = null;
 		private boolean m_active = true;
 
 		@Override
 		public void run() {
 			try {
 				while (m_active) {
 					handleInput();
 					if (!m_senderQueue.isEmpty()) {
 						handleOutput();
 					}
 					final int sleepTime = m_session.getGGConfiguration().getConnectionThreadSleepTimeInMiliseconds();
 					Thread.sleep(sleepTime);
 				}
 				m_dataInput = null;
 				m_dataOutput = null;
 				m_socket.close();
 			} catch (final Exception ex) { // FIXME Czy ten catch jest potrzebny??
 				try {
 					m_active = false;
 					notifyConnectionError(ex);
 				} catch (final GGException ex2) {
 					log.warn("Unable to notify listeners", ex);
 				}
 			}
 		}
 
 		private void handleInput() throws IOException, GGException {
 			final byte[] headerData = new byte[HEADER_LENGTH];
 			if (m_dataInput.available() > 0) {
 				m_dataInput.read(headerData);
 				decodePacket(new GGHeader(headerData));
 			}
 		}
 
 		private void handleOutput() throws IOException {
 			while (!m_senderQueue.isEmpty() && !m_socket.isClosed() && m_dataOutput != null) {
 				final GGOutgoingPackage outgoingPackage = m_senderQueue.poll();
 				sendPackage(outgoingPackage);
 				notifyPacketSent(outgoingPackage);
 			}
 		}
 
 		private boolean isActive() {
 			return m_active;
 		}
 
 		private void openConnection(final String host, final int port) throws IOException {
 			// add runtime checking for port
 			m_socket = new Socket();
 			final SocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName(host), port);
 			final int socketTimeoutInMiliseconds = m_session.getGGConfiguration().getSocketTimeoutInMiliseconds();
 			m_socket.connect(socketAddress, socketTimeoutInMiliseconds);
 			m_socket.setKeepAlive(true);
 			m_socket.setSoTimeout(socketTimeoutInMiliseconds);
 			m_dataInput = new BufferedInputStream(m_socket.getInputStream());
 			m_dataOutput = new BufferedOutputStream(m_socket.getOutputStream());
 			start();
 		}
 
 		private void closeConnection() throws IOException {
 			if (log.isDebugEnabled()) {
 				log.debug("Closing connection...");
 			}
 			m_active = false;
 		}
 
 		private synchronized void sendPackage(final GGOutgoingPackage op) throws IOException {
 			if (log.isDebugEnabled()) {
 				log.debug("Sending packet: {}, packetPayLoad: {}", op.getPacketType(), GGUtils.prettyBytesToString(op.getContents()));
 			}
 
 			m_dataOutput.write(GGUtils.intToByte(op.getPacketType()));
 			m_dataOutput.write(GGUtils.intToByte(op.getContents().length));
 
 			if (op.getContents().length > 0) {
 				m_dataOutput.write(op.getContents());
 			}
 
 			m_dataOutput.flush();
 		}
 
 		private void decodePacket(final GGHeader header) throws IOException, GGException {
 			final byte[] keyBytes = new byte[header.getLength()];
 			m_dataInput.read(keyBytes);
 			final PacketContext context = new PacketContext(m_session, header, keyBytes);
 			m_packetChain.sendToChain(context);
 		}
 
 	}
 
 	private class PingerThread extends Thread {
 
 		private boolean m_active = false;
 
 		/**
 		 * @see java.lang.Thread#run()
 		 */
 		@Override
 		public void run() {
 			while (m_active && m_connectionThread.isActive()) {
 				try {
 					if (log.isDebugEnabled()) {
 						log.debug("Pinging...");
 					}
 					sendPackage(GGPing.getPing());
 					notifyPingSent();
 					final int pingInterval = m_session.getGGConfiguration().getPingIntervalInMiliseconds();
 					Thread.sleep(pingInterval);
 				} catch (final IOException ex) {
 					m_active = false;
 					// log.error("PingerThreadError: ", ex);
 					try {
 						notifyConnectionError(ex);
 					} catch (final GGException e) {
 						log.warn("Unable to notify connection error listeners", ex);
 					}
 				} catch (final InterruptedException ex) {
 					m_active = false;
 					if (log.isDebugEnabled()) {
 						log.debug("PingerThread was interruped", ex);
 					}
 				}
 			}
 		}
 
 		private void startPinging() {
 			if (log.isDebugEnabled()) {
 				log.debug("Starting pinging...");
 			}
 			m_active = true;
 			start();
 		}
 
 		private void stopPinging() {
 			if (log.isDebugEnabled()) {
 				log.debug("Stopping pinging...");
 			}
 			m_active = false;
 		}
 	}
 
 }
