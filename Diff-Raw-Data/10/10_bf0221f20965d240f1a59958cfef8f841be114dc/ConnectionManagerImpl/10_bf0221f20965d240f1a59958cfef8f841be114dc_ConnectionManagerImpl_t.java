 package ar.edu.it.itba.pdc.proxy.implementations.proxy;
 
 import java.io.IOException;
 import java.net.ConnectException;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import ar.edu.it.itba.pdc.proxy.ProxyData;
 import ar.edu.it.itba.pdc.proxy.implementations.configurator.implementations.ConnectionStatus;
 import ar.edu.it.itba.pdc.proxy.implementations.monitor.implementations.Monitor;
 import ar.edu.it.itba.pdc.proxy.implementations.monitor.interfaces.DataStorage;
 import ar.edu.it.itba.pdc.proxy.interfaces.ConnectionManager;
 
 public class ConnectionManagerImpl implements ConnectionManager {
 	private Map<InetAddress, List<ConnectionStatus>> connections;
 	private Logger connectionLog = Logger
 			.getLogger(ConnectionManagerImpl.class);
 	private List<ConnectionStatus> clientConn;
 	private DataStorage dataStorage;
 	private ProxyData pd;
 	private static int STIMEOUT = 5000;
 	private static int CTIMEOUT = 1500;
 
 	public ConnectionManagerImpl(Monitor monitor, ProxyData pd) {
 		connections = new HashMap<InetAddress, List<ConnectionStatus>>();
 		connectionLog.setLevel(Level.INFO);
 		clientConn = new LinkedList<ConnectionStatus>();
 		this.dataStorage = monitor.getDataStorage();
 		this.pd = pd;
 	}
 
 	public void run() {
 		while (!Thread.interrupted()) {
 			try {
 				Thread.sleep(1500);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			// connectionLog.info("Cleaning closed sockets");
 			Set<InetAddress> keys = connections.keySet();
 			synchronized (connections) {
 				for (InetAddress addr : keys) {
 					List<ConnectionStatus> openConn = connections.get(addr);
 					Iterator<ConnectionStatus> iter = openConn.iterator();
 					while (iter.hasNext()) {
 						ConnectionStatus cs = iter.next();
 						Socket s = cs.getSocket();
 						if ((s.isClosed() || !s.isConnected())
 								&& !cs.isInUse()
 								&& System.currentTimeMillis() - cs.getCreated() >= STIMEOUT) {
 							iter.remove();
 						}
 					}
 				}
 			}
 
 		}
 	}
 
 	public Socket getConnection(String host) throws IOException,
 			UnknownHostException {
 		URL url = new URL("http://" + host);
 		connectionLog.info("Requested connection for " + url.toString());
 		InetAddress addr;
 		if (pd.extistsIntermediateProxy()) {
 			addr = pd.getIntermProxyAddr();
 		} else {
 			addr = InetAddress.getByName(url.getHost());
 		}
 		synchronized (connections) {
 
 			List<ConnectionStatus> connectionList = connections.get(addr);
 			if (connectionList == null) {
 				connectionList = new LinkedList<ConnectionStatus>();
 				connections.put(addr, connectionList);
 			}
 			for (ConnectionStatus connection : connectionList) {
 				Socket s = connection.getSocket();
 				if (!connection.isInUse() && !s.isClosed() && s.isConnected()
 						&& isReadable(s)) {
 					connectionLog
 							.info("Reused connection to " + url.toString());
 
 					connection.takeConnection();
 					return s;
 				}
 			}
 		}
 		connectionLog.info("Created new connection to " + url.toString());
 		int port;
 		if (pd.extistsIntermediateProxy()) {
 			port = pd.getIntermProxyPort();
 		} else {
 			port = (url.getPort() == -1) ? 80 : url.getPort();
 		}
 		Socket s = null;
 		try {
 			s = new Socket(addr, port);
 			dataStorage.addServerOpenConnection(1);
 		} catch (ConnectException e) {
 			return null;
 		}
 		synchronized (connections) {
 
 			connections.get(addr).add(
 					new ConnectionStatus(s, true, System.currentTimeMillis()));
 		}
 		return s;
 
 	}
 
 	public void releaseConnection(Socket socket, boolean keepAlive) {
 		List<ConnectionStatus> connectionList = connections.get(socket
 				.getInetAddress());
 		synchronized (connections) {
 			try {
 				for (ConnectionStatus connection : connectionList) {
 					Socket s = connection.getSocket();
 					if (equals(socket, s) && keepAlive) {
 						connection.releaseConnection();
 						return;
 					} else if (equals(socket, s)) {
 						s.close();
 					}
 				}
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void cleanAll(Socket socket) {
 		synchronized (connections) {
 			List<ConnectionStatus> connectionList = connections.get(socket
 					.getInetAddress());
 			connectionList.clear();
 		}
 	}
 
 	private boolean equals(Socket s1, Socket s2) {
 		return s1.getLocalPort() == s2.getLocalPort()
 				&& s1.getLocalSocketAddress()
 						.equals(s2.getLocalSocketAddress())
 				&& s1.getPort() == s2.getPort()
 				&& s1.getRemoteSocketAddress().equals(
 						s2.getRemoteSocketAddress());
 	}
 
 	private boolean isReadable(Socket s) {
		int timeout = -1;
 		try {
			timeout = s.getSoTimeout();
 			s.setSoTimeout(50);
 			int i = s.getInputStream().read();
 			if(i == -1) {
 				return false;
 			}
 		} catch (SocketTimeoutException e) {
			try {
				s.setSoTimeout(timeout);
			} catch (SocketException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
 			return true;
 		} catch (SocketException e) {
 			return false;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 }
