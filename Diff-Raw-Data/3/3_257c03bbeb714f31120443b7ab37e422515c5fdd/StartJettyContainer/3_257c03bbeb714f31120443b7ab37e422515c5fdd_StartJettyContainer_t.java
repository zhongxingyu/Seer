 package com.vegaasen.htpasswd.run.container;
 
 import org.apache.log4j.Logger;
 import org.eclipse.jetty.server.Connector;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.nio.SelectChannelConnector;
 import org.eclipse.jetty.webapp.WebAppContext;
 
 import java.net.URL;
 import java.security.ProtectionDomain;
 
 /**
  * Simple Jetty-Starter-Container that is used when application is started using the
  * simple command:
  * <code>
  * java (-Dport=NNNNN) -jar {appname}.war
  * </code>
  *
  * @author vegaasen
  * @version 1.0-SNAPSHOT
  * @since HJW-005
  */
 public class StartJettyContainer {
 
     private static final int DEFAULT_MAX_IDLE_TIME = 5 * (1000 * 60);
     private static final String DEFAULT_HOST = "127.0.0.1";
     private static final String DEFAULT_PORT = "8000";
 
     private static final Logger LOGGER = Logger.getLogger(StartJettyContainer.class);
 
     public static void initiateAndStartServer() {
         LOGGER.debug("Initiating Jetty");
         Server server = new Server();
         WebAppContext webAppContext = new WebAppContext();
         SelectChannelConnector channelConnector = new SelectChannelConnector();
         ProtectionDomain domain = StartJettyContainer.class.getProtectionDomain();
 
         URL location = domain.getCodeSource().getLocation();
 
         LOGGER.debug("Configuring channel connector");
         channelConnector.setPort(Integer.parseInt(System.getProperty("port", DEFAULT_PORT)));
         channelConnector.setMaxIdleTime(DEFAULT_MAX_IDLE_TIME);
         channelConnector.setStatsOn(false);
         channelConnector.setHost(DEFAULT_HOST);
         server.setConnectors(new Connector[]{channelConnector});
 
         LOGGER.debug("Setting context");
         webAppContext.setServer(server);
         webAppContext.setContextPath("/");
         LOGGER.debug("Loading resources from " + location.toExternalForm());
         webAppContext.setDefaultsDescriptor("org/eclipse/jetty/webapp/webdefault.xml");
         webAppContext.setDescriptor(location.toExternalForm() + "WEB-INF/web.xml");
         webAppContext.setTempDirectory(null);
         webAppContext.setResourceBase(location.toExternalForm());
 
         LOGGER.debug("Setting handlers");
         server.setHandler(webAppContext);
 
         try {
             LOGGER.debug("Trying to start");
             server.start();
             LOGGER.debug("Started, trying to join on thread");
             LOGGER.debug(String.format("Joined - server started on port %s", channelConnector.getPort()));
             server.join();
         } catch (Exception e) {
             LOGGER.error("Something went wrong with the startup-phase. See error: \n" + e);
         }
     }
 
 }
