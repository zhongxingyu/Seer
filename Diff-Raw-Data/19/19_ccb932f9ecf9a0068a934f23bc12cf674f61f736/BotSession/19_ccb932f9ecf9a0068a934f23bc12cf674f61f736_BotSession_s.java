 package botto.xmpp.service.bot;
 
 import botto.xmpp.annotations.PacketOutput;
 import botto.xmpp.service.AbstractBot;
 import botto.xmpp.utils.PacketTypeConverter;
 import com.google.common.base.Preconditions;
 import org.jivesoftware.smack.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xmpp.packet.Packet;
 
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 class BotSession {
 
     private final Logger log;
 
     private final String node;
     private final String secret;
     private final String resource;
     private final XMPPConnection connection;
 
     private final ExecutorService executor = Executors.newSingleThreadExecutor();
 
     public BotSession(String host, int port, String node, String secret, String resource, final AbstractBot bot, final BotSessionPacketSender sender) {
         log = LoggerFactory.getLogger(BotSession.class.getName() + "." + node);
 
         this.node = node;
         this.secret = secret;
         this.resource = resource;
 
         ConnectionConfiguration configuration = new ConnectionConfiguration(host, port);
         configuration.setReconnectionAllowed(true);
         configuration.setSendPresence(true);
         configuration.setCompressionEnabled(true);
 
         Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.accept_all);
 
         connection = new XMPPConnection(configuration);
 
         final BotSession session = this;
 
         // deliver incoming packets to the bot and send out the response
         connection.addPacketListener(new PacketListener() {
             @Override
             public void processPacket(org.jivesoftware.smack.packet.Packet packet) {
                 try {
                    log.info("Received packet {}", packet.toXML());
                     bot.receive(PacketTypeConverter.converttoTinder(packet));
                 }
                 catch (Exception ex) {
                     log.error("Error while processing packet {}: {}", packet.getPacketID(), ex);
                 }
             }
         }, null);
 
         // set an the packet output to the bot
         bot.setPacketOutput(new PacketOutput() {
             @Override
             public void send(Packet packet) {
                 Preconditions.checkNotNull(packet, "Packet can't be null");
                 sender.send(session, PacketTypeConverter.convertFromTinder(packet, connection));
             }
         });
     }
 
     public synchronized void start() {
         final CountDownLatch latch = new CountDownLatch(1);
         executor.submit(new Runnable() {
             @Override
             public void run() {
                 log.info("Connecting...");
                 try {
                     connection.connect();
                     connection.login(node, secret, resource);
                 } catch (XMPPException e) {
                     e.printStackTrace();
                 }
                 latch.countDown();
             }
         });
 
         try {
             latch.await();
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
 
         log.info("Connected...");
 
     }
 
     public synchronized void shutdown() {
         connection.disconnect();
         executor.shutdown();
     }
 
     public synchronized boolean sendPacket(org.jivesoftware.smack.packet.Packet packet) {
         if (connection.isConnected() && connection.isAuthenticated()) {
             connection.sendPacket(packet);
             return true;
         }
         return false;
     }
 }
