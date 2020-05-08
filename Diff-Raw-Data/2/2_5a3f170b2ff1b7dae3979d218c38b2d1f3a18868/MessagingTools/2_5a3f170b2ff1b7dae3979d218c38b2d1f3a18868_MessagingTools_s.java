 
 package axirassa.util;
 
 import org.hornetq.api.core.TransportConfiguration;
 import org.hornetq.api.core.client.ClientSession;
 import org.hornetq.api.core.client.ClientSessionFactory;
 import org.hornetq.api.core.client.HornetQClient;
 import org.hornetq.api.core.client.ServerLocator;
 import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
 
 public class MessagingTools {
 	public static ClientSession getEmbeddedSession() throws Exception {
 		TransportConfiguration configuration = new TransportConfiguration(NettyConnectorFactory.class.getName());
		ServerLocator locator = HornetQClient.createServerLocatorWithHA(configuration);
 		ClientSessionFactory factory = locator.createSessionFactory();
 		ClientSession session = factory.createSession();
 		return session;
 	}
 }
