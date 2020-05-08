 package ch.cern.atlas.apvs.server;
 
 import io.netty.bootstrap.Bootstrap;
 import io.netty.channel.ChannelHandlerContext;
 import io.netty.channel.ChannelInitializer;
 import io.netty.channel.ChannelOption;
 import io.netty.channel.EventLoopGroup;
 import io.netty.channel.nio.NioEventLoopGroup;
 import io.netty.channel.socket.SocketChannel;
 import io.netty.channel.socket.nio.NioSocketChannel;
 import io.netty.handler.timeout.IdleStateHandler;
 
 import java.net.InetSocketAddress;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.cern.atlas.apvs.ptu.server.JsonMessageDecoder;
 import ch.cern.atlas.apvs.ptu.server.JsonMessageEncoder;
 import ch.cern.atlas.apvs.ptu.server.MessageEvent;
 import ch.cern.atlas.apvs.ptu.server.MessageToBus;
 import ch.cern.atlas.apvs.ptu.server.RemoveDelimiterDecoder;
 
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.event.shared.SimpleEventBus;
 
 public class ServerConnector {
 
 	private Logger log = LoggerFactory.getLogger(getClass().getName());
 	private String srcHost;
 	private int srcPort;
 	private String dstHost;
 	private int dstPort;
 
 	public ServerConnector(String srcHost, int srcPort, String dstHost, int dstPort) {
 		log.info("ServerConnector " + srcHost + ":" + srcPort + " " + dstHost
 				+ ":" + dstPort);
 		this.srcHost = srcHost;
 		this.srcPort = srcPort;
 		this.dstHost = dstHost;
 		this.dstPort = dstPort;
 	}
 		
 	public void run() {
 
 		final EventBus bus = new SimpleEventBus();
 
 		EventLoopGroup workerGroup = new NioEventLoopGroup();
 
 		Bootstrap bin = new Bootstrap();
 		bin.group(workerGroup);
 		bin.channel(NioSocketChannel.class);
 		bin.option(ChannelOption.SO_KEEPALIVE, true);
 		bin.handler(new ChannelInitializer<SocketChannel>() {
 			@Override
 			protected void initChannel(SocketChannel ch) throws Exception {
 				ch.pipeline().addLast(new IdleStateHandler(60, 30, 0) {
 					@Override
 					public void userEventTriggered(ChannelHandlerContext ctx,
 							Object evt) throws Exception {
 						// TODO Auto-generated method stub
 						super.userEventTriggered(ctx, evt);
 						System.err.println("*** "+evt);
 					}
 				});
 				ch.pipeline().addLast(new RemoveDelimiterDecoder());
				ch.pipeline().addLast(new JsonMessageDecoder());
 				ch.pipeline().addLast(new JsonMessageEncoder());
 				ch.pipeline().addLast(new MessageToBus("DOWN", bus));
 			}
 		});
 
 		Bootstrap bout = new Bootstrap();
 		bout.group(workerGroup);
 		bout.channel(NioSocketChannel.class);
 		bout.option(ChannelOption.SO_KEEPALIVE, true);
 		bout.handler(new ChannelInitializer<SocketChannel>() {
 			@Override
 			protected void initChannel(SocketChannel ch) throws Exception {
 				ch.pipeline().addLast(new IdleStateHandler(60, 30, 0));
 				ch.pipeline().addLast(new RemoveDelimiterDecoder());
				ch.pipeline().addLast(new JsonMessageDecoder());
 				ch.pipeline().addLast(new JsonMessageEncoder());
 				ch.pipeline().addLast(new MessageToBus("UP", bus));
 			}
 		});
 
 		if (dstHost != null) {
 			bout.connect(new InetSocketAddress(dstHost, dstPort));
 		}
 		bin.connect(new InetSocketAddress(srcHost, srcPort));
 
 		// debug the bus...
 		bus.addHandler(MessageEvent.TYPE, new MessageEvent.Handler() {
 
 			@Override
 			public void onMessageReceived(MessageEvent event) {
 				log.debug("" + event);
 			}
 		});
 	}
 
 	public static void main(String[] args) throws InterruptedException {
 		if ((args.length != 4) && (args.length != 2)) {
 			System.err
 					.println("Usage: ServerConnector source-host source-port [dest-host dest-port]");
 			System.exit(1);
 		}
 
 		new ServerConnector(args[0], Integer.parseInt(args[1]), args.length > 2 ? args[2] : null, args.length > 3 ? Integer.parseInt(args[3]) : 0).run();
 	}
 
 }
