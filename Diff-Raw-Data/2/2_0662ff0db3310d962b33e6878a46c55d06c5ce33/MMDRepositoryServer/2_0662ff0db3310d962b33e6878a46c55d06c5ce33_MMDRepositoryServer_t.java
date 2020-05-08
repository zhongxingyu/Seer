 package ecologylab.semantics.metametadata.services;
 
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 
 public class MMDRepositoryServer
 {
 	    public static void main(String[] args) throws Exception
 	    {
	        Server server = new Server(8080);
 	        
 	        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
 	        handler.setContextPath("/");
 
 	        
 	        System.out.println("Starting server");
 	        handler.addServlet(new ServletHolder(new MMDJsonRepoServlet()),"/");
 	        server.setHandler(handler);
 	        server.start();
 	        server.join();
 	    }	
 
 }
