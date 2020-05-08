 package botto.xmpp;
 
 import botto.xmpp.annotations.PacketOutput;
 import botto.xmpp.botto.xmpp.connector.*;
 import botto.xmpp.service.dispatcher.ListenableConfirmation;
 import com.google.common.util.concurrent.*;
 import net.caprazzi.reusables.common.Managed;
 import net.caprazzi.reusables.threading.ExecutorUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xmpp.packet.JID;
 import org.xmpp.packet.Packet;
 
 import java.util.concurrent.Callable;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 public class BotManager implements Managed {
 
     private static final Logger Log = LoggerFactory.getLogger(BotManager.class);
 
     private final ChannelRegistry channels = new ChannelRegistry();
     private final ConnectorRegistry connectors = new ConnectorRegistry();
 
     // executes connector.openChannel, connector.closeChannel, connector.send, bot.receive
     private final ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
 
     private boolean started;
 
     private BotManager() { }
 
     public static BotManager create() {
         return new BotManager();
     }
 
     public boolean isStarted() {
         return started;
     }
 
     @Override
     public void start() {
         if (isStarted()) {
             throw new BottoRuntimeException("Could not start: already started");
         }
         started = true;
         for(Connector connector : connectors.list()) {
             startConnector(connector);
         }
     }
 
     @Override
     public void stop() {
         if (!isStarted()) {
             throw new BottoRuntimeException("Could not stop: not started");
         }
         started = false;
         try {
             ExecutorUtils.shutdown(Log, executor, 2, TimeUnit.SECONDS);
             for(Connector connector : connectors.list()) {
                 stopConnector(connector);
             }
         }
         catch(Exception ex) {
             Log.error("Error during shutdown - ignoring: {}", ex);
         }
     }
 
     public synchronized ConnectorId registerConnector(final Connector connector) throws BottoException {
         ConnectorId connectorId = connectors.addConnector(connector);
         connector.addChannelListener(new ConnectorChannelListener(connector, connectorId));
         Log.info("Registered connector {} with id {}", connector, connectorId);
         if (isStarted()) {
             startConnector(connector);
         }
         return connectorId;
     }
 
     public synchronized void removeConnector(ConnectorId connectorId) {
         Log.info("Removing connector {}", connectorId);
         Connector removed = connectors.removeConnector(connectorId);
         stopConnector(removed);
     }
 
     public ListenableConfirmation addBot(final ConnectorId connectorId, final JID address, final AbstractBot bot) {
         Log.info("Adding bot to {}::{}: {}", address, connectorId, bot);
         final ListenableConfirmation confirmation = new ListenableConfirmation();
         try {
             final Connector connector = connectors.getConnector(connectorId);
 
             // asynchronously open channel
             Log.debug("Opening channel for {}|{}", address, connector);
             ListenableFuture<Channel> openChannel = openChannel(connector, address);
             Futures.addCallback(openChannel, new FutureCallback<Channel>() {
                 @Override
                 public void onSuccess(final Channel channel) {
                     Log.debug("Channel {} opened for {}|{}", address, connector);
                     channels.addChannel(channel, bot);
                     bot.setPacketOutput(new PacketOutput() {
                         @Override
                         public void send(Packet packet) {
                             BotManager.this.send(connector, channel, packet);
                         }
                     });
                     confirmation.setSuccess();
                 }
 
                 @Override
                 public void onFailure(Throwable t) {
                     Log.error("Failed to open channel for {}::{}: {}", address, connector, t);
                    confirmation.setFailure(new BottoException(t, "Failed to open channel {}, {}, {}", connectorId, address, bot));
                 }
             });
         }
         catch(Exception ex) {
             confirmation.setFailure(ex);
         }
         return confirmation;
     }
 
     public ListenableConfirmation removeBot(final ConnectorId connectorId, final JID address, final AbstractBot bot) {
 
         final ListenableConfirmation confirmation = new ListenableConfirmation();
         try {
             final Connector connector = connectors.getConnector(connectorId);
             final Channel channel = channels.getChannel(address);
 
             // asynchronously close channel
             ListenableFuture<Channel> closeChannel = closeChannel(connector, channel);
             Futures.addCallback(closeChannel, new FutureCallback<Channel>() {
                 public void onSuccess(Channel channel) {
                     // TODO: should remove the channel from the registry even if closing fails?
                     channels.removeChannel(channel);
                     confirmation.setSuccess();
                 }
 
                 public void onFailure(Throwable t) {
                    confirmation.setFailure(new ConnectorException(t, "Failed to close channel {}, {}, {}", connectorId, address, bot));
                 }
             });
         }
         catch(Exception ex) {
             confirmation.setFailure(ex);
         }
         return confirmation;
     }
 
     private void send(final Connector connector, final Channel channel, final Packet packet) {
         Log.debug("Sending packet to {}::{}: {}", channel, connector, packet);
         ListenableFuture<?> send = executor.submit(new Runnable() {
             public void run() {
                 try {
                     // TODO: mind that not all connectors are thread safe
                     connector.send(channel, packet);
                 } catch (ConnectorException e) {
                    throw new BottoRuntimeException(e, "Exception while submitting to channel {}. packet {}", channel, packet);
                 }
             }
         });
 
         Futures.addCallback(send, new FutureCallback<Object>() {
             @Override
             public void onSuccess(Object result) {
                 Log.debug("Packet sent OK to {}::{}: {}", channel, connector, packet.getID());
             }
 
             @Override
             public void onFailure(Throwable t) {
                 Log.debug("Error sending packet to {}::{}: {}: {}", channel, connector, packet.getID(), t);
                 // TODO: count failure
                 // TODO: send error to bot
                 // channels.getBot(channel).receiveError(packet, t);
             }
         });
     }
 
     private void receive(final Connector connector, final Channel channel, final Packet packet, final Meters.ConnectorMetrics meter) {
        Log.debug("Received from {}::{}: {}", channel, connector, packet);

        final Channel destChannel = channels.getChannel(packet.getTo());
        final AbstractBot bot = channels.getBot(destChannel);
 
         ListenableFuture<Packet> execute = deliverToBot(packet, bot, meter);
         Futures.addCallback(execute, new FutureCallback<Packet>() {
             public void onSuccess(Packet response) {
                 Log.debug("Delivered packet to bot {}: {}, with response {}", bot, packet.getID(), response);
                 if (response != null) {
                     meter.countBotResponse();
                    send(connector, destChannel, response);
                 }
             }
 
             public void onFailure(Throwable t) {
                 Log.error("Error while delivering packet {} to {} on {}", packet, channel, connector);
                 meter.countDeliveryError();
             }
         });
     }
 
     private ListenableFuture<Packet> deliverToBot(final Packet packet, final AbstractBot bot, final Meters.ConnectorMetrics metrics) {
         return executor.submit(new Callable<Packet>() {
             public Packet call() throws Exception {
                 long start = metrics.startBotDelivery();
                 try {
                     return bot.receive(packet);
                 } catch (Exception ex) {
                    throw new BottoRuntimeException(ex, "Failed to deliver packet {} to bot {}", packet, bot);
                 } finally {
                     metrics.timeBotDelivery(start);
                 }
             }
         });
     }
 
     private ListenableFuture<Channel> openChannel(final Connector connector, final JID address) {
         return executor.submit(new Callable<Channel>() {
             @Override
             public Channel call() throws Exception {
                 Channel channel = connector.openChannel(address);
                 return channel;
             }
         });
     }
 
     private ListenableFuture<Channel> closeChannel(final Connector connector, final Channel channel) {
         return executor.submit(new Callable<Channel>() {
             public Channel call() throws Exception {
                 connector.closeChannel(channel);
                 return channel;
             }
         });
     }
 
     private void startConnector(Connector connector) {
         try {
             connector.start();
         } catch (ConnectorException e) {
            throw new BottoRuntimeException(e, "Could not start Connector {}");
         }
     }
 
     private void stopConnector(Connector connector) {
         try {
             connector.stop();
         } catch (ConnectorException e) {
            throw new BottoRuntimeException(e, "Error while stopping connector {}", connector);
         }
     }
 
     private class ConnectorChannelListener implements ChannelListener {
 
         private final Meters.ConnectorMetrics meter;
         private final Connector connector;
 
         public ConnectorChannelListener(Connector connector, ConnectorId connectorId) {
             this.connector = connector;
             this.meter = Meters.connectors.forConnector(connectorId);
         }
 
         @Override
         public void onChannelOpen(Channel channel) {
             meter.countOpenChannel();
         }
 
         @Override
         public void onChannelClose(Channel channel) {
             meter.countClosedChannel();
         }
 
         @Override
         public void onIncomingPacket(Channel channel, Packet packet) {
             meter.countIncoming(packet);
             receive(connector, channel, packet, this.meter);
         }
 
         @Override
         public void onOutgoingPacket(Channel channel, Packet packet) {
             meter.countOutgoing(packet);
         }
     }
 }
