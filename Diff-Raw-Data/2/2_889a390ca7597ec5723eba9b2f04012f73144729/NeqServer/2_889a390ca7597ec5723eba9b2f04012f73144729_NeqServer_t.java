 package eu.neq.mais;
 
 import java.util.logging.Logger;
 
 import javax.net.ssl.SSLContext;
 
 import org.eclipse.jetty.http.ssl.SslContextFactory;
 import org.eclipse.jetty.server.Connector;
 import org.eclipse.jetty.server.Handler;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.ConnectHandler;
 import org.eclipse.jetty.server.handler.ContextHandlerCollection;
 import org.eclipse.jetty.server.nio.SelectChannelConnector;
 import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
 import org.eclipse.jetty.server.ssl.SslSocketConnector;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 //import org.eclipse.jetty.util.ssl.SslContextFactory;
 
 //import org.eclipse.jetty.util.ssl.SslContextFactory;
 
 import com.sun.jersey.spi.container.servlet.ServletContainer;
 
 import eu.neq.mais.connector.ConnectorFactory;
 import eu.neq.mais.request.comet.CometdServer;
 import eu.neq.mais.technicalservice.FileHandler;
 import eu.neq.mais.technicalservice.Monitor;
 import eu.neq.mais.technicalservice.SessionStore;
 import eu.neq.mais.technicalservice.Settings;
 
 /**
  * 
  * @author Jan Gansen
  *
  */
 public class NeqServer implements Runnable {
 	
 	private static NeqServer instance = null;
 	private static SessionStore sessionStore = new SessionStore();
 	
 	private Server server = null;
 	
 	public static NeqServer getInstance(){
 		if(instance == null){
 			instance = new NeqServer();
 		}
 		return instance;
 	}
 	
 	public NeqServer(){
 		
 	}
 	
 	public void run() {
 		this.stop();
 		
 		Logger logger = Logger.getLogger("eu.neq.mais.Main");
 		logger.addHandler(FileHandler.getLogFileHandler(Settings.LOG_FILE_MAIN));
 		
 		logger.info("Setting up Server - Port 8080");
 		server = new Server();
 		
 		SelectChannelConnector connector = new SelectChannelConnector();
         connector.setPort(8080);
         
         //default connector
         connector.setMaxIdleTime(30000);
         connector.setRequestHeaderSize(8192);
         
         //possible solution of error with to many patients
         connector.setRequestBufferSize(128000);
         connector.setResponseBufferSize(128000);
         
         
         //secure connector 
         SslSelectChannelConnector sec_connector = new  SslSelectChannelConnector();
         sec_connector.setMaxIdleTime(30000);
         sec_connector.setRequestHeaderSize(8192);
         sec_connector.setRequestBufferSize(128000);
         sec_connector.setResponseBufferSize(128000);
         sec_connector.setPort(8081);
         
 
         
         sec_connector.setKeystore(Settings.SSL_KEYFILE);
         
         //Only used for development purposes - Does not have to be removed
         sec_connector.setKeyPassword("qiQ6SLs6oIe2sDXoqPiE");
         sec_connector.setTrustPassword("qiQ6SLs6oIe2sDXoqPiE");
      
         
          
         
         server.setConnectors(new Connector[]{connector, sec_connector });
 		
 		//Connector secConnector = new Connector();
 		//server.addConnector(connector)
 		        
 		ServletHolder servletHolder = new ServletHolder(ServletContainer.class); 
 		
 		
 		servletHolder.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", 
 		        "com.sun.jersey.api.core.PackagesResourceConfig"); 
 		servletHolder.setInitParameter("com.sun.jersey.config.property.packages", "eu.neq.mais.request");
 		
 		
 		
 		ServletContextHandler context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS); 
 		context.addServlet(servletHolder, "/*"); 
 		
 		
 		//ContextHandlerCollection contexts = new ContextHandlerCollection();
         //contexts.setHandlers(new Handler[] { context });
 	
 		// ADDING COMETD
		//CometdServer.add(server);
 	
 		
 		logger.info("starting server");
 		try {
 			//Starts the NEQ MAIS
 			server.start();
 			
 			
 			//Starts the Monitoring Activities
 			Thread monitoringThread = new Thread( new Monitor() ); 
 			monitoringThread.start();
 		} catch (Exception e) {
 			e.printStackTrace();
 		} 
 		
 		logger.info("joining server");
 		try {
 			server.join();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	public void stop(){
 		if(server != null){
 			try {
 				server.stop();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public static void main(String[]  args){
 		NeqServer server = NeqServer.getInstance();
 		server.run();
 		
 	}
 
 	public static SessionStore getSessionStore() {
 		return sessionStore;
 	}
 
 	public static void setSessionStore(SessionStore sessionStore) {
 		NeqServer.sessionStore = sessionStore;
 	}
 	
 
 }
