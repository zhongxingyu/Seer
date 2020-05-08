 package org.eclipse.dltk.dbgp;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.eclipse.dltk.dbgp.internal.DbgpDebugingEngine;
 import org.eclipse.dltk.dbgp.internal.DbgpSession;
 import org.eclipse.dltk.dbgp.internal.DbgpWorkingThread;
 
 public class DbgpServer extends DbgpWorkingThread implements IDbgpServer {
 	private int clientTimeout;
 	private int serverTimeout;
 
 	private Map acceptors;
 
 	private ServerSocket server;
 
 	protected void acceptNotAvailable() {
 		Iterator it = acceptors.values().iterator();
 		while (it.hasNext()) {
 			((IDbgpThreadAcceptor) it.next()).acceptDbgpThreadNotUnavailable();
 		}
 	}
 
 	protected void checkServerClosed() throws DbgpServerException {
 		if (server.isClosed()) {
 			throw new DbgpServerException("Server socket is already closed.");
 		}
 	}
 
 	protected void workingCycle() throws Exception, IOException {
 		try {
 			while (true) {
 				Socket client = server.accept();
				
				System.out.println("Client connected!");				
 
 				client.setSoTimeout(clientTimeout);
 
 				DbgpSession session = new DbgpSession(new DbgpDebugingEngine(
 						client));
 
 				String id = session.getInfo().getIdeKey();
 
 				IDbgpThreadAcceptor acceptor = (IDbgpThreadAcceptor) acceptors
 						.get(id);
 
 				if (acceptor != null) {
 					acceptor.acceptDbgpThread(session);
 				} else {
 					session.requestTermination();
 				}
 			}
 		} finally {
 			server.close();
 
 			acceptNotAvailable();
 		}
 	}
 
 	protected void tryStart(int port) throws IOException {
 		server = new ServerSocket(port);
 		server.setSoTimeout(serverTimeout);
 
 		super.start();
 	}
 
 	// Timeout
 	public int getClientTimeout() {
 		return clientTimeout;
 	}
 
 	public int getServerTimeout() {
 		return serverTimeout;
 	}
 
 	public DbgpServer(int clientTimeout, int serverTimeout) {
 		this.clientTimeout = clientTimeout;
 		this.serverTimeout = serverTimeout;
 
 		this.acceptors = Collections.synchronizedMap(new HashMap());
 	}
 
 	public void start(int port) throws DbgpServerException {
 		try {
 			tryStart(port);
 		} catch (IOException e) {
 			throw new DbgpServerException(e);
 		}
 	}
 
 	public void start(int startPort, int endPort) throws DbgpServerException {
 		if (startPort > endPort) {
 			throw new IllegalArgumentException(
 					"startPort should be less or equal than endPort");
 		}
 
 		int port = startPort;
 		while (port <= endPort) {
 			try {
 				tryStart(port);
 				return;
 			} catch (IOException e) {
 				port++;
 			}
 		}
 
 		throw new DbgpServerException("Can't find available port in rage "
 				+ startPort + " ... " + endPort);
 	}
 
 	// Stop
 	public void stop() throws DbgpServerException {
 		try {
 			server.close();
 		} catch (IOException e) {
 			throw new DbgpServerException(e);
 		}
 	}
 
 	// Port
 	public int getPort() throws DbgpServerException {
 		checkServerClosed();
 
 		return server.getLocalPort();
 	}
 
 	// Accpetors
 	public void registerAcceptor(String id, IDbgpThreadAcceptor acceptor)
 			throws DbgpServerException {
 		checkServerClosed();
 
 		acceptors.put(id, acceptor);
 	}
 
 	public IDbgpThreadAcceptor unregisterAcceptor(String id) {
 		return (IDbgpThreadAcceptor) acceptors.remove(id);
 	}
 }
