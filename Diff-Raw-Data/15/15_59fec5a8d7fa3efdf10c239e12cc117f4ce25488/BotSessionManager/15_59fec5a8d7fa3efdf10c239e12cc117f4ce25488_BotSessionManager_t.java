 package botto.xmpp.service.bot;
 
 
 import botto.xmpp.service.AbstractBot;
 import botto.xmpp.service.BotEnvironment;
 
 import net.caprazzi.reusables.common.Managed;
 
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 public class BotSessionManager implements Managed {
 
     private final String host;
     private final int port;
 
     private final Map<String, SmackBotSession> sessions = new ConcurrentHashMap<String, SmackBotSession>();
 
     public BotSessionManager(String host, int port) {
         this.host = host;
         this.port = port;
     }
 
     @Deprecated
     public void createSession(final AbstractBot bot, final String node, String secret, String resource) {
         sessions.put(node, new SmackBotSession(host, port, node, secret, resource, bot));
     }
 
     public PacketInputOutput createSession(BotEnvironment env) {
         final SmackBotSession session = new SmackBotSession(host, port, env.getNode(), env.getSecret(), env.getResource(), env.getBot());
         sessions.put(env.getNode(), session);
         return session;
     }
 
     public void destroySession(BotEnvironment env) {
         sessions.remove(env.getNode()).stop();
     }
 
     public void start() {
         for(SmackBotSession session : sessions.values()) {
             session.start();
         }
     }
 
     public void stop() {
         for(SmackBotSession session : sessions.values()) {
             session.stop();
         }
         sessions.clear();
     }
 
 }
