 package botto.xmpp.service.bot;
 
 import botto.xmpp.service.AbstractBot;
 
 import java.util.HashMap;
 
 public class BotSessionManager {
 
     private final String host;
     private final int port;
 
     private final HashMap<String, BotSession> sessions = new HashMap<String, BotSession>();
 
     private final BotSessionPacketSender sender = new BotSessionPacketSender();
 
     public BotSessionManager(String host, int port) {
         this.host = host;
         this.port = port;
     }
 
    // TODO: each bot should be enclosed in its own thread
     // TODO: incoming packets should all go to the same queue
    // TODO: outgoing packets should be put in a queue
 
     public void createSession(final AbstractBot bot, final String node, String secret, String resource) {
         sessions.put(node, new BotSession(host, port, node, secret, resource, bot, sender));
     }
 
     public void start() {
         for(BotSession session : sessions.values()) {
             session.start();
         }
 
         sender.start();
     }
 
     public void shutdown() {
         sender.shutdown();
         for(BotSession session : sessions.values()) {
             session.shutdown();
         }
     }
 
 }
