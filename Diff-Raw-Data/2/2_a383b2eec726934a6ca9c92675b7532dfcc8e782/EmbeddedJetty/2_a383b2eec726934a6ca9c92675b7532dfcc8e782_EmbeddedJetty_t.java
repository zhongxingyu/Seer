 package org.eluder.jetty.server;
 
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.util.Jetty;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Embedded Jetty server.
  */
 public class EmbeddedJetty {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedJetty.class);
     
     protected final ServerConfig serverConfig;
 
     /**
     * Initialize embedded Jetty with server configuration.
      * 
      * @param serverConfig the server configuration
      */
     public EmbeddedJetty(final ServerConfig serverConfig) {
         this.serverConfig = serverConfig;
     }
 
     /**
      * Create and start embedded Jetty server.
      * 
      * @return the started server
      * @throws Exception if server creation or startup fails
      */
     public final Server run() throws Exception {
         Server server = createServerFactory().create();
         LOGGER.info(">>> Starting embedded Jetty {}\n{}", Jetty.VERSION, serverConfig);
         start(server);
         return server;
     }
 
     /**
      * Creates a new server factory.
      * 
      * @return the server factory
      */
     protected ServerFactory createServerFactory() {
         return new ServerFactory(serverConfig);
     }
 
     /**
      * Start the embedded Jetty server.
      * 
      * @param server the server
      * @throws Exception if server startup fails
      */
     protected void start(final Server server) throws Exception {
         LOGGER.info(">>> Press any key to stop");
         server.start();
         System.in.read();
         LOGGER.info(">>> Stopping embedded Jetty");
         server.stop();
         server.join();
     }
 }
