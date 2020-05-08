 package eu.uberdust.communication.websocket.command;
 
 import com.google.protobuf.InvalidProtocolBufferException;
 import eu.uberdust.communication.protobuf.Message;
 import eu.uberdust.communication.rest.UberdustRestClient;
 import eu.uberdust.communication.websocket.WSIdentifiers;
 import eu.uberdust.communication.websocket.command.util.CommandPingTask;
 import org.apache.log4j.Logger;
 import org.eclipse.jetty.websocket.WebSocket;
 import org.eclipse.jetty.websocket.WebSocketClient;
 import org.eclipse.jetty.websocket.WebSocketClientFactory;
 
 import java.io.IOException;
 import java.net.URI;
 import java.util.Observable;
 import java.util.Timer;
 
 /**
  * Insert New Reading Web Socket Client.
  */
 public final class WSCommandClient extends Observable {
 
     /**
      * static instance(ourInstance) initialized as null.
      */
     private static WSCommandClient ourInstance = null;
 
     /**
      * Logger.
      */
     private static final Logger LOGGER = Logger.getLogger(WSCommandClient.class);
 
     /**
      * The testbed id to connect to.
      */
     private static int testbedId = 1;
 
     /**
      * Timer.
      */
     private Timer timer;
 
     /**
      * The WebSocketClient.
      */
     private WebSocketClient client;
 
     /**
      * The WebSocket Connection.
      */
     private WebSocket.Connection connection;
 
     /**
      * WebSocketClientFactory.
      */
     private WebSocketClientFactory factory;
 
     /**
      * Web Socket's URL.
      */
     private String webSocketUrl;
 
     /**
      * WSocketClient is loaded on the first execution of WSocketClient.getInstance()
      * or the first access to WSocketClient.ourInstance, not before.
      *
      * @return ourInstance
      */
     public static WSCommandClient getInstance() {
         synchronized (WSCommandClient.class) {
             if (ourInstance == null) {
                 ourInstance = new WSCommandClient();
             }
         }
         return ourInstance;
     }
 
     /**
      * Private constructor suppresses generation of a (public) default constructor.
      */
     private WSCommandClient() {
         factory = new WebSocketClientFactory();
         factory.setBufferSize(4096);
         try {
             factory.start();
             client = factory.newWebSocketClient();
             client.setMaxIdleTime(-1);
             client.setProtocol(WSIdentifiers.COMMAND_PROTOCOL + WSIdentifiers.DELIMITER + testbedId);
         } catch (Exception e) {
             LOGGER.error(e);
         }
     }
 
     /**
      * Set the testbed Id.
      *
      * @param testbedId the id.
      */
     public void setTestbedId(int testbedId) {
         WSCommandClient.testbedId = testbedId;
     }
 
     /**
      * Connects to the WebSocket.
      *
      * @throws Exception the Exception
      */
     public void connect() throws Exception {
         if (webSocketUrl != null) {
             connect(webSocketUrl);
         }
     }
 
     /**
      * Connects to the WebSocket.
      *
      * @param webSocketUrl WebSocket URL.
      */
     public void connect(final String webSocketUrl) {
         this.webSocketUrl = webSocketUrl;
         try {
             factory = new WebSocketClientFactory();
             factory.setBufferSize(4096);
             factory.start();
             client = factory.newWebSocketClient();
             client.setMaxIdleTime(-1);
             client.setMaxBinaryMessageSize(1024);
             client.setProtocol(WSIdentifiers.COMMAND_PROTOCOL + WSIdentifiers.DELIMITER + testbedId);
 
 
             LOGGER.info("Connecting to " + webSocketUrl);
             // open connection
             connection = client.open(new URI(webSocketUrl), new WSCommandReceiverIMPL()).get();
 
             try {
                 startPingingTask();
             } catch (Exception e) {
                 LOGGER.error(e);
             }
 
         } catch (final Exception e) {
 
             // in case of exception keep trying to make connection after 2 seconds
             LOGGER.error(e);
             e.printStackTrace();
 
             try {
                 Thread.sleep(2000);
             } catch (final InterruptedException e1) {
                 LOGGER.error(e1);
             }
             connect(webSocketUrl);
         }
     }
 
     /**
      * Start pinging task.
      */
     private void startPingingTask() {
         timer = new Timer();
         timer.scheduleAtFixedRate(new CommandPingTask(), CommandPingTask.DELAY, CommandPingTask.DELAY);
     }
 
     /**
      * Stop pinging task.
      */
     private void stopPingingTask() {
         timer.cancel();
     }
 
     /**
      * Send PING message as string data to keep connection alive.
      */
     public void ping() {
         // if connection is not opened do nothing
        if (!connection.isOpen()) {
            return;
        }
 
        try {
            connection.sendMessage("PING");
        } catch (final IOException e) {
            LOGGER.error(e);
         }
     }
 
     /**
      * Disconnect method.
      */
     public void disconnect() {
         try {
            connection.disconnect();
            if (factory.isRunning()) {
                factory.destroy();
            }
             factory.stop();
         } catch (final Exception e) {
             LOGGER.error(e);
         }
     }
 
     /**
      * Checks the Rest Interface to see if connection is available.
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
         try {
             final Message.Envelope envelope = Message.Envelope.parseFrom(data);
             if (envelope.getType().equals(Message.Envelope.Type.CONTROL)) {
                 LOGGER.info(envelope);
                 this.setChanged();
                 this.notifyObservers(envelope.getControl());
             }
         } catch (final InvalidProtocolBufferException e) {
             LOGGER.error("Invalid Protocol Buffer Message", e);
         }
     }
 
 }
