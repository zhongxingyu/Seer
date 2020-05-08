 package coed.collab.client;
 
 import java.net.InetSocketAddress;
 
 import org.apache.mina.common.ConnectFuture;
 import org.apache.mina.common.DefaultIoFilterChainBuilder;
 import org.apache.mina.common.IoHandlerAdapter;
 import org.apache.mina.common.IoSession;
 import org.apache.mina.filter.codec.ProtocolCodecFilter;
 import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
 import org.apache.mina.transport.socket.nio.SocketConnector;
 import org.apache.mina.transport.socket.nio.SocketConnectorConfig;
 
 import coed.collab.protocol.*;
 
 public class ServerConnection extends IoHandlerAdapter {
 	private String host;
 	private int port;
 	private IoSession io;
 	
 	private boolean connected;
 	ConnectionWorker connWorker;
 	private boolean shuttingDown;
 	
 	public synchronized boolean isConnected() {
 		return connected;
 	}
 	
 	private synchronized void setConnected(boolean connected) {
 		this.connected = connected;
		connWorker.notify();
 	}
 	
 	class ConnectionWorker extends Thread {
 		public synchronized void run() {
 			SocketConnector connector = new SocketConnector();
 			SocketConnectorConfig config = new SocketConnectorConfig();
 			
 			DefaultIoFilterChainBuilder chain = config.getFilterChain();
 	        chain.addLast("codec", new ProtocolCodecFilter(
 	                new ObjectSerializationCodecFactory()));
 		
 	        
 	        while(!shuttingDown) {
 	        	if(isConnected()) {
 					try {
 						// wait until shutting down or disconnected
 						wait();
 					} catch (InterruptedException e) {
 						continue;
 					}
 	        	}
 
 	        	ConnectFuture future = connector.connect
 	        		(new InetSocketAddress(host, port), ServerConnection.this, config);
 	        	future.join();
 	        	if (!future.isConnected()) {
 	        		System.out.println("failed to connect");
 	        		try {
 	        			// wait 1 second before trying a new connection, or until shutdown
 						wait(1000);
 					} catch (InterruptedException e) {
 						continue;
 					}
 	        	} else {
 	        		io = future.getSession();
 	        		setConnected(true);
 	        	}
 	        }
 		}
 	}
 	
 	public ServerConnection(String host, int port) {
 		this.host = host;
 		this.port = port;
 		connected = false;
 		shuttingDown = false;
 		connWorker = new ConnectionWorker();
 		connWorker.start();
 	}
 	
     public void exceptionCaught(IoSession session, Throwable cause) {
 
     }
 
     public void messageReceived(IoSession session, Object message) {
     
     }
     
     public void sessionClosed(IoSession session) {
     	System.out.println("session closed.");
     	setConnected(false);
     }
     
     public void send(CoedMessage msg) {
     	assert(io != null);
     	io.write(msg);
     }
     
     public void shutdown() {
     	shuttingDown = true;
    	connWorker.notify();
     }
 }
