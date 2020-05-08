 package org.italiangrid.utils.https;
 
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 import org.eclipse.jetty.server.Connector;
 import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.BlockingChannelConnector;
 import org.eclipse.jetty.util.thread.ExecutorThreadPool;
 import org.italiangrid.utils.https.impl.canl.CANLSSLConnectorConfigurator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * A factory that creates Jetty server object with an HTTPS connector configured
  * according to the given options.
  * 
  * @author andreaceccanti
  *
  */
 public class ServerFactory {
 
 	/**
 	 * Maximum request queue size default value.
 	 */
 	public static final int MAX_REQUEST_QUEUE_SIZE = 50;
 	
 	/**
 	 * Default value for maximum number of concurrent connections. 
 	 */
 	public static final int MAX_CONNECTIONS = 50;
 	
 	/**
 	 * The slf4j logger.
 	 */
 	public static final Logger log = LoggerFactory.getLogger(ServerFactory.class);
 	
 	/**
 	 * The configurator used to configure the SSL connector.
 	 */
 	private static final JettySSLConnectorConfigurator configurator = new CANLSSLConnectorConfigurator();
 	
 	
 	/**
 	 * Thread pool server configuration 
 	 * @param s
 	 */
 	private static void configureThreadPool(Server s){
 		BlockingQueue<Runnable> requestQueue;
 
 		requestQueue = new ArrayBlockingQueue<Runnable>(
 				MAX_REQUEST_QUEUE_SIZE);
 
 		s.setThreadPool(new ExecutorThreadPool(5, MAX_CONNECTIONS, 60,
 				TimeUnit.SECONDS, requestQueue));
 		
 	}
 	
 	/**
 	 * Returns a new Jetty server configured to listen on the host:port passed as argument using
 	 * the SSL default options (see {@link SSLOptions}). 
 	 * 
 	 * @param host
 	 * @param port
 	 * @return a {@link Server} configured as requested
 	 */
 	public static Server newServer(String host, int port) {
 		
 		return newServer(host, port, SSLOptions.DEFAULT_OPTIONS);
 		
 	}
 	
 	/**
 	 *  
 	 *  Returns a new Jetty server configured to listen on the host:port passed as argument and 
 	 *  according to the SSL configuration options provided.
 	 * 
 	 * @param host
 	 * @param port
 	 * @param options
 	 * @return a {@link Server} configured as requested
 	 */
 	public static Server newServer(String host, int port, SSLOptions options){
 		
 		Server server = new Server();
 		
 		server.setSendServerVersion(false);
 		server.setSendDateHeader(false);
 		
 		configureThreadPool(server);
 		
 		Connector connector = configurator.configureConnector(host, port, options);
 		
 		if (connector == null)
 			throw new RuntimeException("Error creating SSL connector.");
 		
 		server.setConnectors(new Connector[] {connector});
 		return server;	
 	}
 }
