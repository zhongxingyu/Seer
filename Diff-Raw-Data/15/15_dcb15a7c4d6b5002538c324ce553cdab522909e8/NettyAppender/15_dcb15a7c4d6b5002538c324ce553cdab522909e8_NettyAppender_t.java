 package championshang.wordpress.com.netty.client;
 
 import java.io.Serializable;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Executors;
 
 import org.jboss.netty.bootstrap.ClientBootstrap;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.channel.ChannelPipelineFactory;
 import org.jboss.netty.channel.Channels;
 import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
 import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
 import org.jboss.netty.util.HashedWheelTimer;
 import org.jboss.netty.util.Timer;
 
 import ch.qos.logback.classic.net.LoggingEventPreSerializationTransformer;
 import ch.qos.logback.classic.spi.ILoggingEvent;
 import ch.qos.logback.core.spi.PreSerializationTransformer;
 
 public class NettyAppender extends NetAppenderBase<ILoggingEvent> {
 	PreSerializationTransformer<ILoggingEvent> pst = new LoggingEventPreSerializationTransformer();
	protected ClientBootstrap bootstrap=null;
 	protected int channelSize = 10;
 
 	protected AppenderClientHandler appenderClientHandler;
 	protected static final Timer timer = new HashedWheelTimer();
 	protected final List<Channel> channelList = new ArrayList<Channel>();
 
 	int channelid = 0;
 
 	protected Channel getChannel() {
 		if (channelid >= channelSize)
 			channelid = 0;
 		return channelList.get(channelid++);
 	}
 
 	@Override
 	protected void append(ILoggingEvent eventObject) {
 		try {
 			if (isStarted()) {
				// First, close the previous connection if any.
				if(bootstrap==null)				
				connect(address, port);
 				eventObject.prepareForDeferredProcessing();
 				eventObject.getCallerData();
 				Serializable serEvent = getPST().transform(eventObject);
 				// if connect write to server
 				if (getChannel().isConnected())
 					getChannel().write(serEvent);
 				else
 					// else write to local
 					aai.appendLoopOnAppenders(eventObject);
 
 			}
 
 		} catch (Exception e) {
 			addError(e.getMessage());
 		}
 
 	}
 
 	@Override
 	public void cleanUp() {
 		try {
 			if (bootstrap != null) {
 				for (Channel channel : channelList) {
 					channel.close();
 				}
 				channelList.clear();
 				bootstrap.releaseExternalResources();
 				bootstrap.shutdown();
 			}
 		} catch (Exception e) {
 			addError(e.getMessage());
 		}
 
 	}
 
 	@Override
 	public void connect(InetAddress address, int port) {
 
 		bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
 		bootstrap.setOption("remoteAddress", new InetSocketAddress(address, port));
 		// Set up the pipeline factory.
 		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
 
 			public ChannelPipeline getPipeline() throws Exception {
 
 				return Channels.pipeline(new ObjectEncoder(), new AppenderClientHandler(NettyAppender.this, bootstrap, timer, reconnectionDelay));
 			}
 		});
 
 		for (int i = 0; i < channelSize; i++) {
 			ChannelFuture future = bootstrap.connect();
 			Channel channel = future.awaitUninterruptibly().getChannel();
 			channelList.add(channel);
 		}
 
 	}
 
 	@Override
 	protected PreSerializationTransformer<ILoggingEvent> getPST() {
 
 		return pst;
 	}
 
 }
