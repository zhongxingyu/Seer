 package com.convert.rice.server.protobuf;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import static org.jboss.netty.channel.Channels.pipeline;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.hadoop.hbase.TableNotFoundException;
 import org.jboss.netty.bootstrap.ServerBootstrap;
 import org.jboss.netty.channel.ChannelEvent;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelFutureListener;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.channel.ChannelPipelineFactory;
 import org.jboss.netty.channel.ChannelStateEvent;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
 import org.jboss.netty.channel.group.ChannelGroup;
 import org.jboss.netty.channel.group.DefaultChannelGroup;
 import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
 import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
 import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;
 import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
 import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
 import org.joda.time.Interval;
 
 import com.convert.rice.DataPoints;
 import com.convert.rice.TimeSeries;
 import com.convert.rice.protocol.Point;
 import com.convert.rice.protocol.Request;
 import com.convert.rice.protocol.Request.Create;
 import com.convert.rice.protocol.Request.Get;
 import com.convert.rice.protocol.Request.Increment;
 import com.convert.rice.protocol.Request.Increment.Metric;
 import com.convert.rice.protocol.Response;
 import com.convert.rice.protocol.Response.CreateResult;
 import com.convert.rice.protocol.Response.Error;
 import com.convert.rice.protocol.Response.Error.Builder;
 import com.convert.rice.protocol.Response.Error.Status;
 import com.convert.rice.protocol.Response.GetResult;
 import com.convert.rice.protocol.Response.IncResult;
 import com.google.common.base.Supplier;
 import com.google.common.util.concurrent.AbstractService;
 import com.yammer.metrics.Metrics;
 import com.yammer.metrics.core.Timer;
 import com.yammer.metrics.core.TimerContext;
 
 public class RiceProtoBufRpcServer extends AbstractService {
 
     private final Timer increments = Metrics.newTimer(RiceProtoBufRpcServer.class, "increments", TimeUnit.MILLISECONDS,
             TimeUnit.SECONDS);
 
     private final Timer gets = Metrics.newTimer(RiceProtoBufRpcServer.class, "gets", TimeUnit.MILLISECONDS,
             TimeUnit.SECONDS);
 
     private final Timer creates = Metrics.newTimer(RiceProtoBufRpcServer.class, "creates", TimeUnit.MILLISECONDS,
             TimeUnit.SECONDS);
 
     private final int port;
 
     private ServerBootstrap bootstrap;
 
     private final ChannelGroup channelGroup;
 
     public RiceProtoBufRpcServer(int port, Supplier<TimeSeries> tsSupplier) {
         checkNotNull(tsSupplier);
         this.port = port;
         this.channelGroup = new DefaultChannelGroup(this.getClass().getName());
         // Configure the server.
         this.bootstrap = new ServerBootstrap(
                 new NioServerSocketChannelFactory(
                         Executors.newCachedThreadPool(),
                         Executors.newCachedThreadPool()));
 
         // Set up the event pipeline factory.
         bootstrap.setPipelineFactory(new RiceServerPipelineFactory(tsSupplier));
         bootstrap.setOption("child.tcpNoDelay", true);
         bootstrap.setOption("child.keepAlive", true);
         bootstrap.setOption("reuseAddress", true);
     }
 
     public void run() {
         // Bind and start to accept incoming connections.
         channelGroup.add(bootstrap.bind(new InetSocketAddress(port)));
     }
 
     private class RiceServerPipelineFactory implements ChannelPipelineFactory {
 
         private Supplier<TimeSeries> tsSupplier;
 
         public RiceServerPipelineFactory(Supplier<TimeSeries> tsSupplier) {
             this.tsSupplier = tsSupplier;
         }
 
         @Override
         public ChannelPipeline getPipeline() throws Exception {
             ChannelPipeline p = pipeline();
             p.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
             p.addLast("protobufDecoder", new ProtobufDecoder(Request.getDefaultInstance()));
 
             p.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
             p.addLast("protobufEncoder", new ProtobufEncoder());
 
             p.addLast("handler", new RiceServerHandler(tsSupplier));
             return p;
         }
     }
 
     private class RiceServerHandler extends SimpleChannelUpstreamHandler {
 
         private final Logger logger = Logger.getLogger(this.getClass().getName());
 
         private Supplier<TimeSeries> tsSupplier;
 
         public RiceServerHandler(Supplier<TimeSeries> tsSupplier) {
             this.tsSupplier = tsSupplier;
         }
 
         @Override
         public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
                 throws Exception {
             channelGroup.add(ctx.getChannel());
             super.channelConnected(ctx, e);
         }
 
         @Override
         public void handleUpstream(
                 ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
             if (e instanceof ChannelStateEvent) {
                 logger.info(e.toString());
             }
             super.handleUpstream(ctx, e);
         }
 
         @Override
         public void messageReceived(
                 ChannelHandlerContext ctx, MessageEvent event) throws Exception {
             Request req = (Request) event.getMessage();
             TimeSeries timeSeries = tsSupplier.get();
             for (Increment inc : req.getIncList()) {
                 IncResult.Builder builder = IncResult.newBuilder();
                 final TimerContext timer = increments.time();
                 try {
                     long timestamp = inc.hasTimestamp() ? inc.getTimestamp() : System.currentTimeMillis();
                     Map<String, Long> metrics = new HashMap<String, Long>(inc.getMetricsCount());
                     for (Metric metric : inc.getMetricsList()) {
                         metrics.put(metric.getKey(), metric.getValue());
                     }
                     builder.setKey(inc.getKey()).setType(inc.getType()).setTimestamp(inc.getTimestamp()).build();
                     try {
                         timeSeries.inc(inc.getType(), inc.getKey(), timestamp, metrics);
                     } catch (IOException e) {
                         Builder errorBuilder = Error.newBuilder()
                                 .setMessage(e.getMessage())
                                 .setStatus(Status.IO_EXCEPTION);
                         builder.setError(errorBuilder);
                         logger.log(Level.SEVERE, e.getMessage(), e);
                     }
                 } finally {
                     event.getChannel().write(Response.newBuilder().addIncResult(builder))
                             .addListener(new ChannelFutureListener() {
 
                                 @Override
                                 public void operationComplete(ChannelFuture future) throws Exception {
                                     timer.stop();
                                 }
                             });
 
                 }
             }
             for (Get get : req.getGetList()) {
                 final TimerContext timer = gets.time();
                 GetResult.Builder builder = GetResult.newBuilder();
                 try {
                     builder.setKey(get.getKey());
                     builder.setType(get.getType());
                     try {
                         Map<String, DataPoints> dataPoints = timeSeries.get(get.getType(), get.getKey(),
                                 new Interval(get.getStart(), get.getEnd()));
                         for (DataPoints dps : dataPoints.values()) {
                             GetResult.Metric.Builder metricBuilder = GetResult.Metric.newBuilder().setName(
                                     dps.getMetricName());
                             for (Entry<Long, Long> dp : dps.aggregate(get.getAggregation()).entrySet()) {
                                 metricBuilder.addPoints(Point.newBuilder().setTimestamp(dp.getKey())
                                         .setValue(dp.getValue()));
                             }
                             builder.addMetrics(metricBuilder);
                         }
                     } catch (IOException e) {
                         Builder errorBuilder = Error.newBuilder()
                                 .setMessage(e.getMessage())
                                 .setStatus(Status.IO_EXCEPTION);
                         builder.setError(errorBuilder);
                         logger.log(Level.SEVERE, e.getMessage(), e);
                     }
 
                 } finally {
                     event.getChannel().write(Response.newBuilder().addGetResult(builder))
                             .addListener(new ChannelFutureListener() {
 
                                 @Override
                                 public void operationComplete(ChannelFuture future) throws Exception {
                                     timer.stop();
                                 }
                             });
 
                 }
             }
 
             for (Create create : req.getCreateList()) {
                 final TimerContext timer = creates.time();
                 CreateResult.Builder builder = CreateResult.newBuilder();
                 try {
                     builder.setType(create.getType());
                     timeSeries.create(create.getType());
                 } catch (TableNotFoundException e) {
                     Builder errorBuilder = Error.newBuilder()
                             .setMessage(e.getMessage())
                             .setStatus(Status.TABLE_NOT_FOUND);
                     builder.setError(errorBuilder);
                     logger.log(Level.SEVERE, e.getMessage(), e);
                 } catch (IOException e) {
                     Builder errorBuilder = Error.newBuilder()
                             .setMessage(e.getMessage())
                             .setStatus(Status.IO_EXCEPTION);
                     builder.setError(errorBuilder);
                     logger.log(Level.SEVERE, e.getMessage(), e);
                 } finally {
                     event.getChannel().write(Response.newBuilder().addCreateResult(builder))
                             .addListener(new ChannelFutureListener() {
 
                                 @Override
                                 public void operationComplete(ChannelFuture future) throws Exception {
                                     timer.stop();
                                 }
                             });
 
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
             e.getChannel().close();
         }
 
     }
 
     @Override
     protected void doStart() {
         try {
             this.run();
             this.notifyStarted();
         } catch (Throwable t) {
             this.notifyFailed(t);
         }
     }
 
     @Override
     protected void doStop() {
         channelGroup.close();
         bootstrap.releaseExternalResources();
         this.notifyStopped();
     }
 }
