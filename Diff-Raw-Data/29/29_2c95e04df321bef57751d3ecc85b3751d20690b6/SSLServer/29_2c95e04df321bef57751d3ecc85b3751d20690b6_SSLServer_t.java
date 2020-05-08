 package com.davecoss.android.genericserver;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 
 import javax.net.ssl.SSLServerSocketFactory;
 
 public class SSLServer extends GenericServer {
 
 	public SSLServer(ServerHandler handler) {
 		super(handler);
 		handler.info("SSLServer", "Creating SSLServer object");
 		
 		try {
 			this.stop_server();
 			this.start_server();
 		} catch (IOException ioe) {
 			handler.error("SSLServer", "IOException: " + ioe.getMessage());
 			handler.traceback(ioe);
 		}
 	}
 	
 	public SSLServer(InetAddress addr, ServerHandler handler) {
 		super(addr, handler);
 	}
 	
 	public SSLServer(InetAddress addr, int port, ServerHandler handler) {
 		super(addr, port, handler);
 	}
 		
 	
 	protected ServerSocket get_new_socket() throws IOException {
 		handler.debug("SSLServer.get_new_socket", "Creating new SSL Socket");
 		SSLServerSocketFactory sslserversocketfactory =
                 (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
 
 		return sslserversocketfactory.createServerSocket(port, 0, addr);
 	}
 
 }
