 package org.hackystat.projectbrowser;
 
 import org.mortbay.jetty.Connector;
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.bio.SocketConnector;
 import org.mortbay.jetty.webapp.WebAppContext;
 
 /**
  * Provides a mechanism for running Project Browser with Jetty.
  * @author Philip Johnson
  */
 public class Start {
 
   /**
    * Run the project browser with Jetty.
    * @param args Ignored. 
    * @throws Exception If problems occur. 
    */
 	public static void main(String[] args) throws Exception {
 	  ProjectBrowserProperties properties = new ProjectBrowserProperties();
 	  int port = properties.getPort();
 	  String contextPath = properties.getContextRoot();
 		Server server = new Server();
 		SocketConnector connector = new SocketConnector();
 		connector.setPort(port);
 		server.setConnectors(new Connector[] { connector });
 
 		WebAppContext bb = new WebAppContext();
 		bb.setServer(server);
 		bb.setContextPath("/" + contextPath);
 		bb.setWar("webapp");
 		server.addHandler(bb);
 
 		try {
 			server.start();
 			System.out.println(" ");
			System.out.println(properties.getHost() + " is now running. Press return to stop server.");
 			while (System.in.available() == 0) {
 				Thread.sleep(5000);
 			}
 			server.stop();
 			server.join();
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.exit(100);
 		}
 	}
 }
