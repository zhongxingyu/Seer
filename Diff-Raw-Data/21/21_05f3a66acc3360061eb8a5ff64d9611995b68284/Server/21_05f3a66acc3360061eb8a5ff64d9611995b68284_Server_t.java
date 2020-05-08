 package server;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.sun.jersey.api.container.httpserver.HttpServerFactory;
 import com.sun.jersey.api.core.PackagesResourceConfig;
 import com.sun.jersey.api.core.ResourceConfig;
 import com.sun.net.httpserver.HttpServer;
 
 public class Server {
 
 	private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
 	private static int PORT = 39640;
 	public static final String URI = "http://localhost:" + PORT + "/";
 	
 	public static void main(String[] args) throws Exception {
 		if (args.length > 0) {
 			PORT = Integer.parseInt(args[0]);
 		}
 		
		Logger.getLogger("com.sun.jersey").setLevel(Level.OFF);
 		try {
 			LOGGER.info("Starting server on port " + PORT);
 			
 			ResourceConfig rc = new PackagesResourceConfig(
 				Server.class.getPackage().getName(),
 				database.DB.class.getPackage().getName()
 			); 
 			
 			rc.getResourceFilterFactories().add(AuthFilter.class.getName());
 			
 			HttpServer server = HttpServerFactory.create(URI, rc);
 			server.start();
 			
 			LOGGER.info("Server listening on port " + PORT);
 		} catch (java.net.SocketException e) {
 			LOGGER.log(Level.SEVERE, "Could not bind to port " + PORT + ": " + e.getMessage());
 		}
 	}
 
 }
