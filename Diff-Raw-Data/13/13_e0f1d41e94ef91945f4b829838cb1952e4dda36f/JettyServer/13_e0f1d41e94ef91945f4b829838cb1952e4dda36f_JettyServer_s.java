 package no.steria.swhrs;
 
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.webapp.WebAppContext;
 
 public class JettyServer {
 	public static void main(String[] args) throws Exception {
		Server server = new Server(22000);
 		server.setHandler(new WebAppContext("src/main/webapp", "/"));
 		server.start();
		System.out.println("Server started!!");
 		
 	}
 }
