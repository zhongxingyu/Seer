 package server;
 
 import helper.NioOption;
 
 import java.net.InetSocketAddress;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.jboss.netty.bootstrap.ServerBootstrap;
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.channel.ChannelPipelineFactory;
 import org.jboss.netty.channel.ChannelStateEvent;
 import org.jboss.netty.channel.Channels;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
 import org.jboss.netty.channel.group.ChannelGroup;
 import org.jboss.netty.channel.group.ChannelGroupFuture;
 import org.jboss.netty.channel.group.ChannelGroupFutureListener;
 import org.jboss.netty.channel.group.DefaultChannelGroup;
 import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
 import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;
 import org.jboss.netty.util.DefaultObjectSizeEstimator;
 import org.jboss.netty.util.ObjectSizeEstimator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameter;
 import com.google.common.base.Stopwatch;
 import com.google.common.util.concurrent.Uninterruptibles;
 
 public class BroadcastServer
 {
 	private static final Logger log = LoggerFactory.getLogger(BroadcastServer.class);
 	
 	// handler
 	//
 	private final Recipients m_handler = new Recipients();
 	private final LengthFieldPrepender m_lenPrePender = new LengthFieldPrepender(4);
 	private final AtomicInteger m_count = new AtomicInteger(1);
 	
 	public void run(int nPort)
 	{
 		ServerBootstrap bootstrap = new ServerBootstrap();
 		bootstrap.setOption(NioOption.child_tcpNoDelay.toString(), true);
 		bootstrap.setFactory(new NioServerSocketChannelFactory());
 		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
 			public ChannelPipeline getPipeline() throws Exception
 			{
 				return Channels.pipeline(m_lenPrePender, m_handler);
 			}
 		});
 		
 		System.out.println(bootstrap);
 		Channel channel = bootstrap.bind(new InetSocketAddress(nPort));
 		log.info("start server port={} channel={}", nPort, channel);
 		log.info("default thread={}", Runtime.getRuntime().availableProcessors() * 2);
 	}
 	
 	public ChannelGroupFuture write(Object message)
     {
 	    final Stopwatch sw = new Stopwatch().start();
 	    ChannelGroupFuture f = m_handler.write(message);
 	    f.addListener(new ChannelGroupFutureListener()
         {
             @Override
             public void operationComplete(ChannelGroupFuture future) throws Exception
             {
                 long lMillis = sw.elapsedMillis();
                 log.debug("writeComplete {} ms, {}", lMillis, m_count.getAndIncrement());
             }
         });
 	    
 	    return f;
     }
 	
 	//------------------------------------------------------------------------
 	static class Recipients extends SimpleChannelUpstreamHandler
 	{
 		ChannelGroup m_recipients = new DefaultChannelGroup();
 		ObjectSizeEstimator estimator = new DefaultObjectSizeEstimator();
 		
 		@Override
 		public void channelInterestChanged(ChannelHandlerContext ctx,
 				ChannelStateEvent e) throws Exception {
 			
 			log.info("channel isWritable={} {}", ctx.getChannel().isWritable(), ctx.getChannel());
 		}
 		
 		@Override
 		public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
 		        throws Exception
 		{
 		    synchronized (m_recipients)
             {
     			m_recipients.add(e.getChannel());
             }
 		}
 		
 		@Override
 		public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
 		        throws Exception
 		{
 		    synchronized (m_recipients)
             {
     			m_recipients.remove(e.getChannel());
             }
 		    log.debug("[Close] {}", e.getChannel());
 		}
 		
 		@Override
 		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
 		        throws Exception
 		{
 		    log.warn("exception", e.getCause());
 		}
 		
 		public ChannelGroupFuture write(Object message)
         {
 //		    log.debug("send {} bytes to {} clients", estimator.estimateSize(message), 
 //		    		m_recipients.size());
 		    
 	        return m_recipients.write(message);
         }
 	}
 	
 	//------------------------------------------------------------------------
 	public static void main(String[] args)
     {
 	    //int nPort = Integer.parseInt(args[0]);
 	    final Param param = new Param();
 	    JCommander jcommander = new JCommander(param);
 	    jcommander.setProgramName("server");
 	    jcommander.usage();
 	    jcommander.parse(args);
 	    
 		final BroadcastServer server = new BroadcastServer();
 		server.run(param.port);
 		
		final byte[] bytes = new byte[100];
 		
 		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
 		service.scheduleAtFixedRate(new Runnable()
 		{
 			public void run()
 			{
 				ChannelBuffer buf = ChannelBuffers.buffer(8 + param.sendByteSize);
 				buf.writeLong(System.currentTimeMillis());
 				buf.writeBytes(bytes);
 				
 				server.write(buf);
 			}
 		}, param.sendPeriodMillis, param.sendPeriodMillis, TimeUnit.MILLISECONDS);
 		
 		Uninterruptibles.joinUninterruptibly(Thread.currentThread());
     }
 	
 	static class Param 
 	{
 	    @Parameter(names="-port")
 	    private int port = 9999;
 	    
 	    @Parameter(names="-sendPeriodMillis")
 	    private int sendPeriodMillis = 100;
 	    
 	    @Parameter(names="-sendByteSize")
 	    private int sendByteSize = 100;
 	    
 	    @Parameter(names="-sendBufSize")
 	    private int sendBufSize = 100;
 	    
 	}
 }
