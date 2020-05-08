 package ch.cern.atlas.apvs.ptu.server;
 
 import io.netty.bootstrap.ServerBootstrap;
 import io.netty.buffer.BufType;
 import io.netty.buffer.Unpooled;
 import io.netty.channel.ChannelFuture;
 import io.netty.channel.ChannelInitializer;
 import io.netty.channel.socket.SocketChannel;
 import io.netty.channel.socket.nio.NioServerSocketChannel;
 import io.netty.handler.codec.DelimiterBasedFrameDecoder;
 import io.netty.handler.codec.string.StringDecoder;
 import io.netty.handler.codec.string.StringEncoder;
 import io.netty.handler.timeout.IdleStateHandler;
 import io.netty.util.CharsetUtil;
 
 import java.net.InetSocketAddress;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class PtuPullServer {
 
 	private Logger log = LoggerFactory.getLogger(getClass().getName());
 
 	private final int port;
 	private final int refresh;
 	private final String[] ids;
 
 	public PtuPullServer(int port, int refresh, String[] ids) {
 		this.refresh = refresh;
 		this.port = port;
 		this.ids = ids;
 	}
 
 	public void run() {
 		// Configure the server.
 		ServerBootstrap bootstrap = new ServerBootstrap();
 		try {
 			bootstrap.channel(NioServerSocketChannel.class);
 
 			// Set up the pipeline factory.
 			bootstrap.childHandler(new PtuChannelInitializer(new PtuServerHandler(refresh, ids)));
 
 			// Bind and start to accept incoming connections.
 			ChannelFuture f = bootstrap.bind(new InetSocketAddress(port))
 					.sync();
 			log.info("PTU Pull Server open at " + port);
 
 			f.channel().closeFuture().sync();
 		} catch (InterruptedException e) {
 			log.warn("Problem: "+e);
 		} finally {
 			// Shut down all event loops to terminate all threads.
 			bootstrap.shutdown();
 		}
 	}
 
 	public static void main(String[] args) {
 		new PtuPullServer(4005, 5000, null).run();
 	}
 }
