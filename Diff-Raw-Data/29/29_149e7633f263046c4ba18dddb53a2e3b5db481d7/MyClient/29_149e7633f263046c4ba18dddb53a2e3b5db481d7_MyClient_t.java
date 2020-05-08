 package info.chenliang.myclient;
 
 import java.net.InetSocketAddress;

 import java.util.concurrent.Executors;
 
 import org.jboss.netty.bootstrap.ClientBootstrap;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
 
 public class MyClient {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		// Set up.
		String host = "192.168.1.104";
 		//String host = "localhost";
 		int port = 8888;
 		
         ClientBootstrap bootstrap = new ClientBootstrap(
                 new NioClientSocketChannelFactory(
                         Executors.newCachedThreadPool(),
                         Executors.newCachedThreadPool()));
 
         // Configure the event pipeline factory.
         bootstrap.setPipelineFactory(new MyClientPipelineFactory());
 
         // Make a new connection.
         ChannelFuture connectFuture =
             bootstrap.connect(new InetSocketAddress(host, port));
 
         // Wait until the connection is made successfully.
         Channel channel = connectFuture.awaitUninterruptibly().getChannel();
 	}
 
 }
