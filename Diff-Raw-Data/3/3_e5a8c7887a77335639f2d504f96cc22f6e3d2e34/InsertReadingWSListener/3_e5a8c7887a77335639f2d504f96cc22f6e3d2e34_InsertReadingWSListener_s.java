 package eu.uberdust.websockets.readings;
 
 import com.caucho.websocket.AbstractWebSocketListener;
 import com.caucho.websocket.WebSocketContext;
 import eu.uberdust.communication.protobuf.Message;
 import eu.wisebed.wisedb.controller.LinkReadingController;
 import eu.wisebed.wisedb.controller.NodeReadingController;
 import eu.wisebed.wisedb.exception.UnknownTestbedException;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Reader;
 import java.util.Date;
 
 /**
  * Insert Reading Web Socket Listener.
  */
 @Controller
 public final class InsertReadingWSListener extends AbstractWebSocketListener {
 
     /**
      * Static Logger.
      */
     private static final Logger LOGGER = Logger.getLogger(InsertReadingWSListener.class);
 
     /**
      * Singleton instance.
      */
     private static InsertReadingWSListener ourInstance = null;
 
     /**
      * NodeReading persistence manager.
      */
     private transient NodeReadingController nodeReadingManager;
 
     /**
      * LinkReading persistence manager.
      */
     private transient LinkReadingController linkReadingManager;
 
 
     /**
      * Constructor.
      */
     private InsertReadingWSListener() {
         // empty constructor
     }
 
     /**
      * Returns singleton instance.
      *
      * @return singleton instance.
      */
     public static InsertReadingWSListener getInstance() {
         synchronized (InsertReadingWSListener.class) {
 
             if (ourInstance == null) {
                 ourInstance = new InsertReadingWSListener();
             }
             return ourInstance;
         }
     }
 
     /**
      * Sets node reading persistence manager.
      *
      * @param nodeReadingManager node reading persistence manager.
      */
     @Autowired
     public void setNodeReadingManager(final NodeReadingController nodeReadingManager) {
         this.nodeReadingManager = nodeReadingManager;
     }
 
     /**
      * Sets link reading persistence manager.
      *
      * @param linkReadingManager link reading manager.
      */
     @Autowired
     public void setLinkReadingManager(final LinkReadingController linkReadingManager) {
         this.linkReadingManager = linkReadingManager;
     }
 
     /**
      * On start of connection.
      *
      * @param context WebSocketContext instance.
      * @throws IOException IOException exception.
      */
     public void onStart(final WebSocketContext context) throws IOException {
         super.onStart(context);
         LOGGER.info("onStart()-" + (nodeReadingManager == null) + ":" + (linkReadingManager == null));
 
     }
 
     /**
      * On read binary.
      *
      * @param context     WebSocketContext instance.
      * @param inputStream InputStream instance.
      * @throws IOException IOException exception.
      */
     public void onReadBinary(final WebSocketContext context, final InputStream inputStream) throws IOException {
         LOGGER.debug("New Message Reveived");
 
         final Message.Envelope envelope = Message.Envelope.parseFrom(inputStream);
 
         if (envelope.getType().equals(Message.Envelope.Type.NODE_READINGS)) {
             LOGGER.debug("New NodeReading");
             final Message.NodeReadings nodeReadings = envelope.getNodeReadings();
             for (Message.NodeReadings.Reading reading : nodeReadings.getReadingList()) {
                 final String nodeId = reading.getNode();
                 final String capabilityId = reading.getCapability();
                 final long timestamp = reading.getTimestamp();
                 final double readingValue = reading.getDoubleReading();
                 final String stringReading = reading.getStringReading();
 
                 try {
                     nodeReadingManager.insertReading(nodeId, capabilityId, readingValue, stringReading,
                             new Date(timestamp));
                 } catch (final UnknownTestbedException e) {
                     LOGGER.error("Uknown Testebed to associate with node based on prefix: " + nodeId, e);
                 }
             }
 
         } else if (envelope.getType().equals(Message.Envelope.Type.LINK_READINGS)) {
             final Message.LinkReadings linkReadings = envelope.getLinkReadings();
             for (Message.LinkReadings.Reading reading : linkReadings.getReadingList()) {
                 final String sourceNodeId = reading.getSource();
                 final String targetNodeId = reading.getTarget();
                 final String capabilityId = reading.getCapability();
                 final long timestamp = reading.getTimestamp();
                 final double readingValue = reading.getDoubleReading();
                 final String stringReading = reading.getStringReading();
 
                 try {
                     linkReadingManager.insertReading(sourceNodeId, targetNodeId, capabilityId, readingValue,
                             stringReading, new Date(timestamp));
                 } catch (final UnknownTestbedException e) {
                     LOGGER.error("Uknown Testebed to associate with nodes based on prefix: "
                             + sourceNodeId + "," + targetNodeId, e);
                 }
             }
         } else {
             LOGGER.error("Received unsupported Envelope Type");
         }
 
 
         final long mBytes = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024;
         LOGGER.debug("Memory Usage: " + mBytes + " MB");
     }
 
     /**
      * On read text.
      *
      * @param context WebSocketContext instance.
      * @param reader  InputStream instance.
      * @throws IOException IOException exception.
      */
     public void onReadText(final WebSocketContext context, final Reader reader) throws IOException {
         final char[] arr = new char[1024]; // 1K at a time
         final StringBuffer buf = new StringBuffer();
         int numChars;
 
         while ((numChars = reader.read(arr, 0, arr.length)) > 0) {
             buf.append(arr, 0, numChars);
         }
         LOGGER.debug("onReadText() : " + buf.toString());
         super.onReadText(context, reader);
     }
 
     /**
      * On close.
      *
      * @param context WebSocketContext instance.
      * @throws IOException IOException exception.
      */
     public void onClose(final WebSocketContext context) throws IOException {
         LOGGER.info("onClose()");
     }
 
     /**
      * On disconnect.
      *
      * @param context WebSocketContext instance.
      * @throws IOException IOException exception.
      */
     public void onDisconnect(final WebSocketContext context) throws IOException {
         super.onDisconnect(context);
         LOGGER.info("onDisconnect()");
 
     }
 
     /**
      * On timeout.
      *
      * @param context WebSocketContext instance.
      * @throws IOException IOException exception.
      */
     public void onTimeout(final WebSocketContext context) throws IOException {
         LOGGER.debug("onTimeout()");
     }
 }
