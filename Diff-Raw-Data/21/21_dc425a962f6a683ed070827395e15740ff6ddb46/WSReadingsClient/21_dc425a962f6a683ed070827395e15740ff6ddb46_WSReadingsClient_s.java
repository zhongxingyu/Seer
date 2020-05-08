 package eu.uberdust.communication.websocket.readings;
 
 import com.google.protobuf.InvalidProtocolBufferException;
 import eu.uberdust.communication.protobuf.Message;
 import eu.uberdust.communication.rest.UberdustRestClient;
 import eu.uberdust.communication.websocket.WSIdentifiers;
 import eu.uberdust.communication.websocket.readings.util.PingTask;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 import org.eclipse.jetty.websocket.WebSocket;
 import org.eclipse.jetty.websocket.WebSocketClient;
 import org.eclipse.jetty.websocket.WebSocketClientFactory;
 
 import java.io.IOException;
 import java.net.URI;
 import java.util.HashMap;
 import java.util.Observable;
 import java.util.Timer;
 
 /**
  * Created by IntelliJ IDEA.
  * User: akribopo
  * Date: 3/17/12
  * Time: 4:58 PM
  * To change this template use File | Settings | File Templates.
  */
 public class WSReadingsClient extends Observable {
 
     /**
      * Static Logger.
      */
     private static final Logger LOGGER = Logger.getLogger(WSReadingsClient.class);
 
     /**
      * static instance(ourInstance) initialized as null.
      */
     private static WSReadingsClient ourInstance = null;
 
     /**
      * Static WebSocket URI.
      */
     private URI WS_URI;
 
     /**
      * The WebSocketClientFactory.
      */
     private WebSocketClientFactory factory;
 
     /**
      * The HashMap which contains all the connections.
      */
     private HashMap<String, WebSocket.Connection> protocols;
 
     /**
      * The WebSocket implementation.
      */
     private final WebSocketIMPL webSocketIMPL = new WebSocketIMPL();
 
     /**
      * Server's Web Socket URL.
      */
     private String webSocketUrl;
 
     /**
      * WSocketClient is loaded on the first execution of WSocketClient.getInstance()
      * or the first access to WSocketClient.ourInstance, not before.
      *
      * @return ourInstance
      */
     public static WSReadingsClient getInstance() {
         synchronized (WSReadingsClient.class) {
             if (ourInstance == null) {
                 ourInstance = new WSReadingsClient();
             }
         }
         return ourInstance;
     }
 
     /**
      * Private constructor suppresses generation of a (public) default constructor.
      */
     private WSReadingsClient() {
         PropertyConfigurator.configure(this.getClass().getClassLoader().getResource("log4j.properties"));
         protocols = new HashMap<String, WebSocket.Connection>();
         startPingTask();
         LOGGER.info("WSocketClient initialized");
     }
 
     /**
      * Initialize Ping Task to keep alive web socket connections.
      */
     private void startPingTask() {
         final Timer timer = new Timer();
         timer.scheduleAtFixedRate(new PingTask(timer), PingTask.DELAY, PingTask.DELAY);
     }
 
 
     /**
      * Set Web Socket Server url.
      *
      * @param serverUrl the servers url.
      */
     public void setServerUrl(final String serverUrl) {
         webSocketUrl = serverUrl;
         createWebSocketFactory();
 
     }
 
     /**
      * Initiates the Web Socket Factory.
      */
     protected void createWebSocketFactory() {
         try {
             WS_URI = new URI(webSocketUrl);
             factory = new WebSocketClientFactory();
             factory.setBufferSize(4096);
             factory.start();
         } catch (final Exception e) {
             LOGGER.error("Unable to create WebSocket Factory ", e);
         }
     }
 
     /**
      * Creates a new Web Socket Connection using the specified protocol.
      *
      * @param protocol the protocol.
      */
     private void createNewConnection(final String protocol) {
         WebSocket.Connection connection = null;
         try {
             final WebSocketClient client = factory.newWebSocketClient();
             client.setMaxIdleTime(-1);
             client.setMaxBinaryMessageSize(1024);
             client.setProtocol(protocol);
             connection = client.open(WS_URI, webSocketIMPL).get();
             LOGGER.info("New Web Socket Connection. Protocol: " + client.getProtocol());
         } catch (final Exception e) {
             LOGGER.error("Unable to Create new WebSocket connection", e);
             if (e.getMessage().contains("ProtocolException")) {
                 LOGGER.fatal("Wrong Protocol Definition: " + protocol);
                 throw new RuntimeException("Wrong Protocol Definition: " + protocol);
             }
             try {
                 Thread.sleep(2000);
             } catch (final InterruptedException e1) {
                 LOGGER.error(e1);
             }
             createNewConnection(protocol);
         }
         if (connection != null) {
             protocols.put(protocol, connection);
         }
     }
 
     /**
      * Ping function to keep alive active connections.
      */
     public void ping() {
         for (WebSocket.Connection connection : protocols.values()) {
             if (connection.isOpen()) {
                 try {
                     connection.sendMessage("ping");
                 } catch (final IOException e) {
                     LOGGER.error("Unable to Ping ", e);
                 }
             }
         }
     }
 
     /**
      * This function is called to destroy web socket factory and to close open connections.
      */
     public void disconnect() {
 
         if (factory.isRunning()) {
             factory.destroy();
         }
 
         for (WebSocket.Connection connection : protocols.values()) {
             connection.disconnect();
         }
 
         try {
             factory.stop();
         } catch (final Exception e) {
             LOGGER.error("Unable to stop WebSocket Factory ", e);
         }
     }
 
     /**
      * Pings Rest interface.
      */
     protected void restPing() {
         UberdustRestClient.getInstance().callRestfulWebService(webSocketUrl.replace(WSIdentifiers.WS_PREFIX,
                 WSIdentifiers.HTTP_PREFIX));
     }
 
     /**
      * If this object has changed, as indicated by the
      * <code>hasChanged</code> method, then notify all of its observers
      * and then call the <code>clearChanged</code> method to indicate
      * that this object has no longer changed.
      * <p/>
      * Each observer has its <code>update</code> method called with two
      * arguments: this observable object and the <code>arg</code> argument.
      *
      * @param data a protocol buffer byte array.
      * @see java.util.Observable#clearChanged()
      * @see java.util.Observable#hasChanged()
      * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
      */
     protected void notifyObservers(final byte[] data) {
         System.out.println(data.length);
         try {
             final Message.Envelope envelope = Message.Envelope.newBuilder().mergeFrom(data).build();
             if (envelope.getType().equals(Message.Envelope.Type.LINK_READINGS)) {
                 this.setChanged();
                 this.notifyObservers(envelope.getLinkReadings());
             } else if (envelope.getType().equals(Message.Envelope.Type.NODE_READINGS)) {
                 this.setChanged();
                 this.notifyObservers(envelope.getNodeReadings());
             }
         } catch (final InvalidProtocolBufferException e) {
             LOGGER.error("Invalid Protocol Buffer Message", e);
         }
     }
 
 
     /**
      * Send Node Reading.
      *
      * @param nodeReadings a @Message.NodeReadings instance.
      * @throws java.io.IOException an IOException exception.
      */
     public void sendNodeReading(final Message.NodeReadings nodeReadings) throws IOException {
         final Message.Envelope envelope = Message.Envelope.newBuilder()
                 .setType(Message.Envelope.Type.NODE_READINGS)
                 .setNodeReadings(nodeReadings).build();
         sendEnvelope(envelope);
     }
 
     /**
      * Send Link Reading.
      *
      * @param linkReadings a NodeReading instance.
      * @throws java.io.IOException an IOException exception.
      */
     public void sendLinkReading(final Message.LinkReadings linkReadings) throws IOException {
         final Message.Envelope envelope = Message.Envelope.newBuilder()
                 .setType(Message.Envelope.Type.LINK_READINGS)
                 .setLinkReadings(linkReadings).build();
         sendEnvelope(envelope);
     }
 
     /**
      * Send Envelope to server.
      *
      * @param envelope a @Message.Envelope instance
      * @throws java.io.IOException an IOException exception.
      */
     private void sendEnvelope(final Message.Envelope envelope) throws IOException {
         if (!protocols.containsKey(WSIdentifiers.INSERT_PROTOCOL)) {
             createNewConnection(WSIdentifiers.INSERT_PROTOCOL);
         }
 
         final byte[] byteArray = envelope.toByteArray();
 
         protocols.get(WSIdentifiers.INSERT_PROTOCOL).sendMessage(byteArray, 0, byteArray.length);
 
     }
 
     /**
      * Subscribes a client for notification of the specific node and capability,
      *
      * @param nodeId       the node id.
      * @param capabilityId the capability id.
      */
     public void subscribe(final String nodeId, final String capabilityId) {
 
         final String protocol = new StringBuilder()
                 .append(WSIdentifiers.SUBSCRIBE_PROTOCOL_PREFIX)
                 .append(WSIdentifiers.DELIMITER)
                 .append(nodeId)
                 .append(WSIdentifiers.DELIMITER)
                 .append(capabilityId)
                 .toString();
 
         if (!protocols.containsKey(protocol)) {
             createNewConnection(protocol);
         }
     }
 }
 
