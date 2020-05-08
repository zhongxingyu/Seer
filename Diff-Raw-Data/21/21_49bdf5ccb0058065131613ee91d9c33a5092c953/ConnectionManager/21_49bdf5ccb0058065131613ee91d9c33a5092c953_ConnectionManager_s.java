 package jagex.runescape.net;
 
 import java.net.InetSocketAddress;
 import java.util.concurrent.Executor;
 import java.util.concurrent.Executors;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.jboss.netty.bootstrap.ServerBootstrap;
 import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
 
 /**
  * ConnectionManager.java
  * @author Ryley M. Kimmel <ryley.kimmel@live.com>
  * @version 1.0
  * Aug 14, 2012
  */
 public final class ConnectionManager {
 
     /**	This singleton logger instance can be used for logging various component messages to the console */
     private static final Logger logger = Logger.getLogger(ConnectionManager.class.getSimpleName());
 
     /** the instance for this class */
     private static ConnectionManager instance;
 
     /**
      * Creates the bootstrap and starts accepting connections on port 43594
      * 
      * @throws Throwable if some error occurs
      */
     public void init() throws Throwable {
 	Executor pool = Executors.newCachedThreadPool();
 	final NioServerSocketChannelFactory factory = new NioServerSocketChannelFactory(pool, pool);
 	final ServerBootstrap bootstrap = new ServerBootstrap(factory);
 	bootstrap.setPipelineFactory(PipelineFactory.getInstance());
 	bootstrap.setOption("tcpNoDelay", true);
 	bootstrap.setOption("keepAlive", true);
 	bootstrap.bind(new InetSocketAddress(43594));
 	logger.log(Level.INFO, "Listening on: " + 43594);
     }
 
     /**
      * Gets and creates the instance for this class
      */
     public static ConnectionManager getInstance() {
 	if (instance == null) {
 	    instance = new ConnectionManager();
 	}
 	return instance;
     }
 
     /**
      * Blank constructor to prevent creation of this class.
      */
     private ConnectionManager() { }
 
 }
