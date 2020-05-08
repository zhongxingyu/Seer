 package contrib.wicket.cms.example;
 
 
 import java.net.URL;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.mortbay.jetty.Server;
 
 /**
  * Seperate startup class for people that want to run the examples directly.
  */
 public class StartWicketCms
 {
 	/**
 	 * Used for logging.
 	 */
 	private static Log log = LogFactory.getLog(StartWicketCms.class);
 
 	/**
 	 * Construct.
 	 */
 	StartWicketCms()
 	{
 		super();
 	}
 
 	/**
 	 * Main function, starts the jetty server.
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
         Server jettyServer = null;
 		try
 		{
 			URL jettyConfig = new URL("file:src/main/resources/jetty-config.xml");
 			if (jettyConfig == null)
 			{
 				log.fatal("Unable to locate jetty-test-config.xml on the classpath");
 			}
 			jettyServer = new Server(jettyConfig);
 			jettyServer.start();
 		}
 		catch (Exception e)
 		{
			log.fatal("Could not start the Jetty server: " + e, e);
 			if (jettyServer != null)
 			{
 				try
 				{
 					jettyServer.stop();
 				}
 				catch (InterruptedException e1)
 				{
 					log.fatal("Unable to stop the jetty server: " + e1);
 				}
 			}
 		}
 	}
 }
