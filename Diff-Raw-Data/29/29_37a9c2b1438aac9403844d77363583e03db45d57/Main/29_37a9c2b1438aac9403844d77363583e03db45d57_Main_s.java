 package com.vaguehope.senkyou;
 
 import java.util.logging.Logger;
 
 import org.eclipse.jetty.server.Handler;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.HandlerList;
 import org.eclipse.jetty.server.handler.ResourceHandler;
 import org.eclipse.jetty.server.nio.SelectChannelConnector;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 
 import com.vaguehope.senkyou.servlets.ThreadServlet;
 import com.vaguehope.senkyou.servlets.TweetServlet;
 
 public class Main {
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 	
 	protected static final Logger logger = Logger.getLogger(Main.class.getName());
 	
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 	
 	private final Server server;
 	
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 	
	public Main () throws Exception {
 		// Servlet container.
 		ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
 		servletHandler.setContextPath("/");
 		
 		// Tweet servlet.
 		TweetServlet tweetServlet = new TweetServlet();
 		servletHandler.addServlet(new ServletHolder(tweetServlet), TweetServlet.CONTEXT);
 		
 		// Thread servlet.
 		ThreadServlet threadServlet = new ThreadServlet();
 		servletHandler.addServlet(new ServletHolder(threadServlet), ThreadServlet.CONTEXT);
 		
 		// Static files on classpath.
 		ResourceHandler resourceHandler = new ResourceHandler();
 		resourceHandler.setDirectoriesListed(true);
 		resourceHandler.setWelcomeFiles(new String[] { "index.html" });
 		String webrootClass = Main.class.getPackage().getName().replace('.', '/') + "/webroot";
 		String webrootResource = Main.class.getClassLoader().getResource(webrootClass).toExternalForm();
 		resourceHandler.setResourceBase(webrootResource);
 		
 		// Prepare final handler.
 		HandlerList handlers = new HandlerList();
 		handlers.setHandlers(new Handler[] { resourceHandler, servletHandler });
 		
 		// Listening connector.
 		String portString = System.getenv("PORT"); // Heroko pattern.
 		SelectChannelConnector connector = new SelectChannelConnector();
 		connector.setMaxIdleTime(30000); // 30 seconds.
 		connector.setAcceptors(2);
 		connector.setStatsOn(false);
 		connector.setLowResourcesConnections(100);
 		connector.setLowResourcesMaxIdleTime(5000); // 5 seconds.
 		connector.setPort(Integer.parseInt(portString));
 		
 		// Start server.
 		this.server = new Server();
 		this.server.setHandler(handlers);
 		this.server.addConnector(connector);
 		this.server.start();
 		logger.info("Server ready on port " + portString + ".");
 	}
 	
 	public void join () throws InterruptedException {
 		this.server.join();
 	}
 	
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 	
	public static void main (String[] args) throws Exception {
 		Main m = new Main();
 		m.join();
 	}
 	
 //	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 }
