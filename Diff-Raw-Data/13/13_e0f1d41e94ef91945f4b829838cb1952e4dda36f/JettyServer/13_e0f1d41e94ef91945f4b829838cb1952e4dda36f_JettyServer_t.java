 package no.steria.swhrs;
 
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.webapp.WebAppContext;
 
 public class JettyServer {
 	public static void main(String[] args) throws Exception {
		int localPort = 8081;
		String envPort = System.getenv("PORT");
		if (envPort != null && !envPort.isEmpty()) {
			localPort = Integer.parseInt(envPort);
		}
		
		Server server = new Server(localPort);
 		server.setHandler(new WebAppContext("src/main/webapp", "/"));
 		server.start();
		System.out.println("Server started!! - port " + localPort);
		server.join();
		System.out.println("Dette gikk galt");
 		
 	}
 }
