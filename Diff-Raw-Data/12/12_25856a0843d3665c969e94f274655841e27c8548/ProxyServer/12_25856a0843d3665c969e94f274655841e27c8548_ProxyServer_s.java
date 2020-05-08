 package com.butterfly;
 
 import static org.jboss.netty.channel.Channels.pipeline;
 
 import java.net.InetSocketAddress;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.apache.log4j.Logger;
 import org.jboss.netty.bootstrap.ServerBootstrap;
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.channel.ChannelPipelineFactory;
 import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
 
 /**
  * a simple reverse proxy server based on netty,
  * 
  * <a href=
  * "http://docs.jboss.org/netty/3.2/xref/org/jboss/netty/example/proxy/package-summary.html"
  * >netty sample code</a> and <a href=
  * "http://docs.jboss.org/netty/3.2/xref/org/jboss/netty/example/portunification/package-summary.html"
  * >this one</a>
  * 
  * <p>
  * port unification: http port(8080) and windows remote desktop port(3389). can
  * be configured by modify code
  * </p>
  * 
  * @author feng
  * @since 2010/11/27
  */
 public class ProxyServer {
 
 	private static Logger logger = Logger.getLogger(ProxyServer.class);
	public static final ExecutorService threadPoop = Executors
 			.newCachedThreadPool();
 
 	public static void main(String[] args) throws Exception {
 		// Configure the server.
 		ServerBootstrap bootstrap = new ServerBootstrap(
				new NioServerSocketChannelFactory(threadPoop, threadPoop));
 
 		// Set up the event pipeline factory.
 		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
 			public ChannelPipeline getPipeline() throws Exception {
 				ChannelPipeline pipeline = pipeline();
 				pipeline.addLast("proxydecoder", new ProxyDecoder());
 				return pipeline;
 			}
 		});
 
 		final int port = 8080;
 		// Bind and start to accept incoming connections.
 		bootstrap.bind(new InetSocketAddress(port));
 		logger.debug("listern on port " + port);
 
 	}
 }
