 package com.arpnetworking.tsdaggregator.publishing;
 
 import com.arpnetworking.tsdaggregator.AggregatedData;
 import com.arpnetworking.tsdaggregator.aggserver.AggregatorConnection;
 import com.arpnetworking.tsdaggregator.aggserver.Messages;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.vertx.java.core.*;
 import org.vertx.java.core.net.NetClient;
 import org.vertx.java.core.net.NetSocket;
 import org.vertx.java.platform.Verticle;
 
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.atomic.AtomicBoolean;
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 
 /**
  * Publisher to send data to an upstream aggregation server.
  *
  * @author barp
  */
 public class AggServerPublisher extends Verticle implements AggregationPublisher {
     private static final Logger LOGGER = LoggerFactory.getLogger(AggServerPublisher.class);
     private final AtomicBoolean _connected = new AtomicBoolean(false);
     private final AtomicBoolean _connectionInProgress = new AtomicBoolean(false);
     private final String _aggServerHost;
     private final String _hostName;
     private final String _clusterName;
     private final int _aggServerPort;
     private final NetClient _client;
     private final ConcurrentLinkedQueue<AggregatedData[]> _pending = new ConcurrentLinkedQueue<AggregatedData[]>();
     @Nullable
     private NetSocket _socket = null;
     private final Vertx _vertx;
 
     public AggServerPublisher(@Nonnull String aggClusterLocation, String hostName, String clusterName) {
         _vertx = VertxFactory.newVertx();
         String[] split = aggClusterLocation.split(":");
         _aggServerHost = split[0];
         if (split.length > 1) {
             _aggServerPort = Integer.parseInt(split[1]);
         } else {
             _aggServerPort = 7065;
         }
 
         _hostName = hostName;
         _clusterName = clusterName;
 
         _client = _vertx.createNetClient();
 
         _client.setReconnectAttempts(1).setReconnectInterval(3000L).setConnectTimeout(5000).setTCPNoDelay(true)
                 .setTCPKeepAlive(true);
         connectToAggServer();
     }
 
     private void connectToAggServer() {
         _connectionInProgress.set(true);
         LOGGER.info("attempting to connect to agg server at " + _aggServerHost + ":" + _aggServerPort);
         _client.connect(
                 _aggServerPort, _aggServerHost, new AsyncResultHandler<NetSocket>() {
             @Override
             public void handle(@Nonnull final AsyncResult<NetSocket> event) {
                 if (event.succeeded()) {
                     LOGGER.info("connected to aggregation server at " + _aggServerHost + ":" + _aggServerPort);
                     _socket = event.result();
                     _socket.exceptionHandler(createSocketExceptionHandler(_socket));
                     _socket.closeHandler(createSocketCloseHandler(_socket));
                     _connected.set(true);
                     _connectionInProgress.set(false);
                     @Nonnull Messages.HostIdentification hostIdent =
                             Messages.HostIdentification.newBuilder().setHostName(_hostName).setClusterName(_clusterName)
                                     .build();
                     @Nonnull AggregatorConnection.Message m = AggregatorConnection.Message.create(hostIdent);
                     _socket.write(m.getBuffer());
                     flushPending();
                 } else if (event.failed()) {
                     LOGGER.warn(
                             "failed to connect to aggregation server at " + _aggServerHost + ":" + _aggServerPort);
                     _connectionInProgress.set(false);
                     _connected.set(false);
                     connectToAggServer();
                 }
             }
         }
         );
     }
 
     @Nullable
     private Handler<Void> createSocketCloseHandler(final NetSocket socket) {
         return new Handler<Void>() {
             @Override
             public void handle(final Void event) {
                 _socket = null;
                 _connected.set(false);
                 LOGGER.error("agg server publisher socket closed, attempting to reestablish connection", event);
                 connectToAggServer();
             }
         };
     }
 
     @Nullable
     private Handler<Throwable> createSocketExceptionHandler(final NetSocket socket) {
         return new Handler<Throwable>() {
             @Override
             public void handle(final Throwable event) {
                 try {
                     if (socket != null) {
                         socket.close();
                     }
                 } catch (Exception ignored) { }
                 _socket = null;
                 _connected.set(false);
                 LOGGER.error("error on agg server publisher socket, attempting to reestablish connection", event);
                 connectToAggServer();
             }
         };
     }
 
     @Override
     public void recordAggregation(final AggregatedData[] data) {
         _pending.offer(data);
         flushPending();
     }
 
     private void flushPending() {
         if (!_connected.get()) {
             LOGGER.warn("not connected, cannot flush pending data");
             return;
         }
         AggregatedData[] dataChunk;
         while (_connected.get() && (dataChunk = _pending.poll()) != null) {
             for (@Nonnull AggregatedData data : dataChunk) {
                 //Don't aggregate metrics < 1 minute
                 if (data.getPeriod().toStandardDuration().isShorterThan(org.joda.time.Duration.standardMinutes(1))) {
                     continue;
                 }
                 @Nonnull Messages.AggregationRecord record = Messages.AggregationRecord.newBuilder()
                         .setMetric(data.getMetric()).setPeriod(data.getPeriod().toString())
                         .setService(data.getService()).setStatistic(data.getStatistic().getName())
                         .setStatisticValue(data.getValue()).addAllStatisticSamples(data.getSamples())
                         .build();
                 @Nonnull AggregatorConnection.Message message = AggregatorConnection.Message.create(record);
                 _socket.write(message.getBuffer());
             }
         }
     }
 
     @Override
     public void close() {
         _socket.close();
     }
 }
