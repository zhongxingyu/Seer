 package ljas.functional;
 
 import java.io.IOException;
 
 import ljas.functional.application.TestApplicationImpl;
 import ljas.server.Server;
 import ljas.server.configuration.Property;
 
 public abstract class ServerManager {
 	private static Server server = null;
 
 	public static Server getServer() throws IOException {
 		if (server == null) {
 			server = createServer();
 		}
 		return server;
 	}
 
 	public static Server createServer() throws IOException {
 		return new Server(new TestApplicationImpl());
 	}
 
 	public static void startupServer() throws Exception {
 		Server server = getServer();
 		if (server.isOnline()) {
 			server.shutdown();
 		}
 
 		// Set properties
		server.getProperties().set(Property.LOG4J_PATH, "./log4j-tests.xml");
 		server.getProperties().set(Property.MAXIMUM_CLIENTS, 5);
 
 		server.startup();
 	}
 
 	public static void shutdownServer() throws IOException {
 		Server server = getServer();
 		if (server.isOnline()) {
 			server.shutdown();
 			server.getProperties().reset();
 		}
 	}
 }
