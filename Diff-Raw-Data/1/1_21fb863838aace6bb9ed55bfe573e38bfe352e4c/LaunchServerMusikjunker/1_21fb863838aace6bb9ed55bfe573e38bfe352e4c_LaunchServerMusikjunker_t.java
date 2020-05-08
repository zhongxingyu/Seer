 package org.tekila.musikjunker;
 
 import java.io.File;
 
 import lombok.extern.slf4j.Slf4j;
 
 import org.eclipse.jetty.server.Connector;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.nio.SelectChannelConnector;
 import org.eclipse.jetty.webapp.WebAppContext;
 
 /**
  * Embedded launcher
  * @author lc
  *
  */
 @Slf4j
 public class LaunchServerMusikjunker {
 
 	/**
 	 * Test mode runner
 	 * @param args
 	 * @throws Exception
 	 */
 	public static void main(String[] args) throws Exception {
 		
 		int port = 30180;
 
 		// parse command line
 		for (int i = 0; i < args.length; i++) {
 			String arg = args[i].toLowerCase();
 			if (arg.startsWith("-h")) {
 				printHelp();
 				System.exit(-1);
 			}
 			if (arg.equalsIgnoreCase("-port")) {
 				if (i == (args.length-1)) {
 					System.err.println("Error: port not providen");
 					System.exit(-2);
 				}
 				try {
 					port = Integer.parseInt(args[++i]);
 				} catch (NumberFormatException nfe) {
 					System.err.println("Error: port not numeric");
 					System.exit(-3);
 				}
 			}
 		}
 		
 		log.info("Running server on port {}", port);
 		
 		Server server = new Server();
         Connector connector = new SelectChannelConnector();
         connector.setPort(port);
         server.addConnector(connector);
         
         WebAppContext wac = new WebAppContext();
         wac.setContextPath("/");
         // test local path
         File webAppFile = new File("musikunker.war");
         if (webAppFile.exists() && webAppFile.canRead()) {
         	log.info("Using packaged war");
         	wac.setWar(webAppFile.getName());
         } else {
         	// "eclipse" mode
         	log.info("Using exploded WAR from sources");
         	log.info("If you see class not found exceptions, maybe you're not running with the exploded war in classpath (your IDE usually does that)");
             wac.setWar("../musikjunker-app/src/main/webapp");
         }
         
         wac.setDefaultsDescriptor("jetty-webdefault.xml");
         server.setHandler(wac);
 
         server.setStopAtShutdown(true);
         server.start();
 		
 	}
 
 	private static void printHelp() {
 		System.out.println("Usage: java -jar musikjunker.jar [-port N]");
 		System.out.println("\tWhere:");
 		System.out.println("\t-port N : specify HTTP port to listen on");
 	}
 }
