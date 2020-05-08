 package ch.cern.atlas.apvs.server.jetty;
 
 import java.net.URL;
 import java.security.ProtectionDomain;
 
 import org.eclipse.jetty.server.Connector;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.nio.SelectChannelConnector;
 import org.eclipse.jetty.util.thread.QueuedThreadPool;
 import org.eclipse.jetty.webapp.WebAppContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class APVSServer {
 	private static Logger log = LoggerFactory.getLogger(APVSServer.class.getName());
 	
 	private static final int DEFAULT_PORT_NO = 8095;
 
 	public static void main(String[] args) {
 		int port = Integer.parseInt(System.getProperty("port",
 				"" + DEFAULT_PORT_NO));
 		Server server = new Server();
 		
 		// only access via localhost, hidden behind apache
 		Connector connector = new SelectChannelConnector();
		connector.setHost("localhost");
 		connector.setPort(port);
 		server.addConnector(connector);
 		
 		ProtectionDomain domain = APVSServer.class.getProtectionDomain();
 		URL location = domain.getCodeSource().getLocation();
 
 		// Create a handler for processing our GWT app
 		WebAppContext webapp = new WebAppContext();
 		webapp.setContextPath("/");
 //		webapp.setDescriptor(location.toExternalForm() + "/WEB-INF/web.xml");
 //		webapp.setServer(server);
 		webapp.setWar(location.toExternalForm());
 
 		// (Optional) Set the directory the war will extract to.
 		// If not set, java.io.tmpdir will be used, which can cause problems
 		// if the temp directory gets cleaned periodically.
 		// Your build scripts should remove this directory between deployments
 //		webapp.setTempDirectory(new File("/path/to/webapp-directory"));
 
 		// Add it to the server
 		server.setHandler(webapp);
 
 		// Other misc. options
 		server.setThreadPool(new QueuedThreadPool(20));
 
 		// And start it up
 		try {
 			server.start();
 			log.info("APVS started on http://localhost:"+port+"/index.html");
 
 			server.join();
 		} catch (Exception e) {
 			e.printStackTrace();
 			if (e.getCause() != null) {
 				log.warn("Caused by:",e.getCause());
 				e.getCause().printStackTrace();
 			}
 		}
 	}
 }
