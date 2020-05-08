 package com.convert.rice.client;
 
 import static org.jboss.netty.channel.Channels.pipeline;
 
 import java.net.InetSocketAddress;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.Executors;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.pool.PoolableObjectFactory;
 import org.apache.commons.pool.impl.GenericObjectPool;
 import org.jboss.netty.bootstrap.ClientBootstrap;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelEvent;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.channel.ChannelPipelineFactory;
 import org.jboss.netty.channel.ChannelStateEvent;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
 import org.jboss.netty.channel.group.ChannelGroup;
 import org.jboss.netty.channel.group.ChannelGroupFuture;
 import org.jboss.netty.channel.group.ChannelGroupFutureListener;
 import org.jboss.netty.channel.group.DefaultChannelGroup;
 import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
 import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
 import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;
 import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
 import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
 import org.jboss.netty.handler.logging.LoggingHandler;
 
 import com.convert.rice.protocol.Aggregation;
 import com.convert.rice.protocol.Request;
 import com.convert.rice.protocol.Request.Get;
 import com.convert.rice.protocol.Request.Increment;
 import com.convert.rice.protocol.Request.Increment.Builder;
 import com.convert.rice.protocol.Request.Increment.Metric;
 import com.convert.rice.protocol.Response;
 import com.convert.rice.protocol.Response.GetResult;
 import com.convert.rice.protocol.Response.IncResult;
 import com.google.common.util.concurrent.ListenableFuture;
 import com.google.common.util.concurrent.SettableFuture;
 
 public class RiceClient {
 
     private final GenericObjectPool<Channel> pool;
 
     private final ClientBootstrap bootstrap;
 
     private final ChannelGroup group = new DefaultChannelGroup();
 
     public RiceClient(final String host, final int port) {
         this(host, port, new GenericObjectPool.Config());
     }
 
     public RiceClient(final String host, final int port, GenericObjectPool.Config config) {
         bootstrap = new ClientBootstrap(
                 new NioClientSocketChannelFactory(
                         Executors.newCachedThreadPool(),
                         Executors.newCachedThreadPool()));
 
         // Configure the event pipeline factory.
         bootstrap.setPipelineFactory(new RiceClientPipelineFactory());
         pool = new GenericObjectPool<Channel>(new PoolableObjectFactory<Channel>() {
 
             @Override
             public Channel makeObject() throws Exception {
                 Channel ch = bootstrap.connect(new InetSocketAddress(host, port)).awaitUninterruptibly().getChannel();
                 group.add(ch);
                 return ch;
             }
 
             @Override
             public void destroyObject(Channel ch) throws Exception {
                 group.remove(ch);
                 ch.close().awaitUninterruptibly();
 
             }
 
             @Override
             public boolean validateObject(Channel ch) {
                 return ch.isConnected() && ch.isOpen();
             }
 
             @Override
             public void activateObject(Channel ch) throws Exception {
             }
 
             @Override
             public void passivateObject(Channel ch) throws Exception {
             }
         }, config);
     }
 
     public ListenableFuture<IncResult> inc(String type, String key, Map<String, Long> metrics, long timestamp)
             throws Exception {
         Channel ch = pool.borrowObject();
 
         RiceClientHandler handler = ch.getPipeline().get(RiceClientHandler.class);
         return handler.inc(type, key, timestamp, metrics);
 
     }
 
     public ListenableFuture<GetResult> get(String type, String key, long start, long end, Aggregation aggregation)
             throws Exception {
         Channel ch = pool.borrowObject();
         RiceClientHandler handler = ch.getPipeline().get(RiceClientHandler.class);
         return handler.get(type, key, start, end, aggregation);
 
     }
 
     public ListenableFuture<Void> close() throws Exception {
         pool.close();
         final SettableFuture<Void> future = SettableFuture.<Void> create();
         group.close().addListener(new ChannelGroupFutureListener() {
 
             @Override
             public void operationComplete(ChannelGroupFuture f) throws Exception {
                 future.set(null);
             }
         });
         return future;
 
     }
 
     private class RiceClientPipelineFactory implements ChannelPipelineFactory {
 
         @Override
         public ChannelPipeline getPipeline() throws Exception {
             ChannelPipeline p = pipeline();
             p.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
             p.addLast("protobufDecoder", new ProtobufDecoder(Response.getDefaultInstance()));
             p.addLast("log", new LoggingHandler());
 
             p.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
             p.addLast("protobufEncoder", new ProtobufEncoder());
 
             p.addLast("handler", new RiceClientHandler());
             return p;
         }
     }
 
     private class RiceClientHandler extends SimpleChannelUpstreamHandler {
 
         private final Logger logger = Logger.getLogger(
                 this.getClass().getName());
 
         // Stateful properties
         private volatile Channel channel;
 
         public SettableFuture<IncResult> inc(String type, String key, long timestamp, Map<String, Long> metrics) {
             Builder builder = Increment.newBuilder().setType(type)
                     .setKey(key)
                     .setTimestamp(timestamp);
             for (Entry<String, Long> entry : metrics.entrySet()) {
                 builder.addMetrics(Metric.newBuilder().setKey(entry.getKey()).setValue(entry.getValue()));
             }
             SettableFuture<IncResult> future = SettableFuture.<IncResult> create();
             channel.getPipeline().getContext(this).setAttachment(future);
             channel.write(Request.newBuilder().setInc(builder));
             return future;
 
         }
 
         public ListenableFuture<GetResult> get(String type, String key, long start, long end, Aggregation aggregation) {
             Get.Builder builder = Get.newBuilder()
                     .setKey(key)
                     .setType(type)
                     .setStart(start)
                     .setEnd(end)
                     .setAggregation(aggregation);
             SettableFuture<GetResult> future = SettableFuture.<GetResult> create();
             channel.getPipeline().getContext(this).setAttachment(future);
             channel.write(Request.newBuilder().setGet(builder));
             return future;
         }
 
         @Override
         public void handleUpstream(
                 ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
             if (e instanceof ChannelStateEvent) {
                 logger.fine(e.toString());
             }
             super.handleUpstream(ctx, e);
         }
 
         @Override
         public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
                 throws Exception {
             channel = e.getChannel();
             super.channelOpen(ctx, e);
         }
 
         @Override
         public void messageReceived(
                 ChannelHandlerContext ctx, final MessageEvent event) {
             try {
                 Response result = (Response) event.getMessage();
                 if (result.hasGetResult()) {
                     @SuppressWarnings("unchecked")
                     SettableFuture<GetResult> future = (SettableFuture<GetResult>) ctx.getAttachment();
                     future.set(result.getGetResult());
                 } else if (result.hasIncResult()) {
                     @SuppressWarnings("unchecked")
                     SettableFuture<IncResult> future = (SettableFuture<IncResult>) ctx.getAttachment();
                     future.set(result.getIncResult());
                 }
             } finally {
                 try {
                     pool.returnObject(event.getChannel());
                 } catch (Exception e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
             }
         }
 
         @Override
         public void exceptionCaught(
                 ChannelHandlerContext ctx, ExceptionEvent e) {
             logger.log(
                     Level.WARNING,
                     "Unexpected exception from downstream.",
                     e.getCause());
             e.getCause().printStackTrace();
             e.getChannel().close();
         }
     }
 }
