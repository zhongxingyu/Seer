 import java.net.InetSocketAddress;
 import java.net.SocketAddress;
 import java.util.logging.Logger;
 
 import org.simpleframework.transport.connect.Connection;
 import org.simpleframework.transport.connect.SocketConnection;
 
 import jcube.core.configuration.ConfigContext;
 import jcube.core.exception.ConfigException;
 import jcube.core.exception.InitializeException;
 import jcube.core.server.dev.entry.Handler;
 import jcube.core.server.loaders.ConfigurationLoader;
 
 /**
  * The Class HelloWorld.
  */
 public class RunDevelop
 {
 	private static Logger log = Logger.getLogger(RunDevelop.class.getName());
 
 	/**
 	 * The main method.
 	 * 
 	 * @param list the arguments
 	 * @throws Exception the exception
 	 */
 	public static void main(String[] list)  throws Exception
 	{
 			
 		log.info("Trying to start application");
 		log.info("Reading config folder");
 		
 		try
 		{
			ConfigurationLoader.loadConfigurationInFolder("../config/");
 		} catch (InitializeException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ConfigException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 
 		Integer port = ConfigContext.get("jcube.develop.port").getInteger();
 		if (port == null)
 			throw new InitializeException("jcube.develop.port is missing!");
 
 		Connection connection = new SocketConnection(new Handler());
 		SocketAddress address = new InetSocketAddress(port);
 		connection.connect(address);
 	}
 }
