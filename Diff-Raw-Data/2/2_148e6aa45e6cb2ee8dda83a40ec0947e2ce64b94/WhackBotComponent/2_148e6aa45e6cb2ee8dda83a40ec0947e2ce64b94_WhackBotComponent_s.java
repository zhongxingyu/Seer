 package botto.xmpp.connectors.whack;
 
 import botto.xmpp.botto.xmpp.connector.BotConnectionInfo;
 import com.google.common.base.Objects;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xmpp.component.Component;
 import org.xmpp.component.ComponentException;
 import org.xmpp.component.ComponentManager;
 import org.xmpp.packet.JID;
 import org.xmpp.packet.Packet;
 
 import java.util.Map;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 
 public class WhackBotComponent implements Component {
 
     private static final Logger Log = LoggerFactory.getLogger(WhackBotComponent.class);
 
     private final WhackConnector connector;
     private final String subdomain;
     private JID jid;
     private ComponentManager componentManager;
     private Map<String, WhackBotConnection> connections = new ConcurrentHashMap<String, WhackBotConnection>();
     private final BotConnectionInfo connectionInfo = new BotConnectionInfo();
 
     public WhackBotComponent(WhackConnector connector, String subdomain) {
         this.connector = connector;
         this.subdomain = subdomain;
     }
 
     public void addConnection(WhackBotConnection connection) {
         connection.setConnectionInfo(connectionInfo);
         connections.put(connection.getSendAddress().toBareJID(), connection);
     }
 
     public void removeConnection(WhackBotConnection connection) {
         connections.remove(connection.getSendAddress().toBareJID());
     }
 
     @Override
     public String getName() {
         return "Bot Component Manager for subdomain " + subdomain;
     }
 
     @Override
     public String getDescription() {
         return getName();
     }
 
     @Override
     public void processPacket(Packet packet) {
         Log.debug("Received packet {}", packet);
         WhackBotConnection connection = connections.get(packet.getTo().toBareJID());
         if (connection == null) {
             Log.warn("Could not find a connection to route packet: {}", packet);
             return;
         }
         connector.receiveFromComponent(connection, packet);
     }
 
     @Override
     public void initialize(JID jid, ComponentManager componentManager) throws ComponentException {
         this.jid = jid;
         this.componentManager = componentManager;
     }
 
     @Override
     public void start() {
         // TODO: does this mean 'connected'?
         Log.info("Start");
     }
 
     @Override
     public void shutdown() {
         Log.info("Shutdown");
     }
 
     public synchronized void send(Packet packet) throws ComponentException {
 
         if (packet.getFrom() == null) {
             packet.setFrom(jid);
         }
 
         if (packet.getID() == null) {
             packet.setID(UUID.randomUUID().toString());
         }
 
        Log.debug("[{}] sending packet {}", packet);
         componentManager.sendPacket(this, packet);
     }
 
     public boolean isEmpty() {
         return connections.isEmpty();
     }
 
     public String getSubdomain() {
         return subdomain;
     }
 
     public void setConnected(boolean connected) {
         connectionInfo.setConnectionStatus(connected);
     }
 
     @Override
     public String toString() {
         return Objects.toStringHelper(this)
             .add("jid", jid)
             .toString();
     }
 }
