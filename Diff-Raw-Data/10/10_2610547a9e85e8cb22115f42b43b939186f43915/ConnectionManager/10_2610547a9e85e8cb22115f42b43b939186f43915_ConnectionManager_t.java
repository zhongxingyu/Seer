 package com.outr.net.communicator.client.connection;
 
 import com.outr.net.communicator.client.GWTCommunicator;
 import com.outr.net.communicator.client.UUID;
 
 import java.io.IOException;
 
 /**
  * @author Matt Hicks <matt@outr.com>
  */
 public class ConnectionManager {
     public final GWTCommunicator communicator;
     public final String uuid;
     public final MessageQueue queue;
 
     private Connection connection;
     private boolean connected;
     private int lastReceiveId;
 
     private int reconnectInMilliseconds = -1;
     private int failures = 0;
 
     private boolean firstConnection = true;
 
     public ConnectionManager(GWTCommunicator communicator) {
         this.communicator = communicator;
         uuid = UUID.unique();
         queue = new MessageQueue();
     }
 
     public void init() {
     }
 
     public boolean hasConnection() {
         return connection != null;
     }
 
     public boolean isConnected() {
         return connection != null && connected;
     }
 
     public void connect() throws IOException {
         if (connection != null) {
             throw new IOException("Connection already established!");
         }
         if (firstConnection) {
             queue.enqueueHighPriority("create", communicator.createData());
             firstConnection = false;
         }
         // TODO: support multiple connection types based on settings
         connection = new AJAXConnection(this, communicator.getAJAXURL());
         reconnect();
     }
 
     public void update(int delta) {
         if (connection != null) {
             connection.update(delta);
         }
         if (reconnectInMilliseconds != -1) {        // Reconnect timer activated
             reconnectInMilliseconds -= delta;
             updateDisconnectMessage(false);
             if (reconnectInMilliseconds <= 0) {
                 reconnect();                        // Reconnect when the timer runs out
             }
         }
     }
 
     public void reconnect() {
         communicator.error.update("Reconnecting...", "Attempting to reconnect to the server.");
         reconnectInMilliseconds = -1;       // Remove reconnect timer
         if (connection == null) {
             throw new RuntimeException("Connection doesn't exist to reconnect with!");
         }
         queue.enqueueHighPriority("connect", communicator.connectData());
         connection.connect();
     }
 
     public void disconnected() {
         boolean wasConnected = connected;
         connected = false;
         if (reconnectInMilliseconds == -1) {
             failures++;
 //            log("Disconnected, attempting reconnect in " + communicator.reconnectDelay() + "milliseconds.");
             reconnectInMilliseconds = communicator.reconnectDelay();
             updateDisconnectMessage(wasConnected);
         }
     }
 
     private void updateDisconnectMessage(boolean show) {
         int seconds = (int)Math.round(reconnectInMilliseconds / 1000.0);
         String title;
         String text;
         if (failures > 1) {
             title = "Failed to Connect (" + (failures - 1) + ")";
             text = "Connection to the server failed. This may be caused by the server being updated or an internet connectivity issue. Will try again to connect in " + seconds + " seconds.";
         } else {
             title = "Disconnected from Server";
             text = "The connection to the server was lost. Will attempt to reconnect again in " + seconds + " seconds.";
         }
         if (show) {
             communicator.error.show(title, text);
         } else {
             communicator.error.update(title, text);
         }
     }
 
     public int getLastReceiveId() {
         return lastReceiveId;
     }
 
     /**
      * Reliably enqueues a message to be sent to the server.
      *
      * @param event the name of the message event you are sending to the server.
      * @param value the data to send in the message.
      */
     public void send(String event, Object value) {
         queue.enqueue(event, value);
         if (connection != null) {
             connection.messageReady();
         }
     }
 
     public void received(Message message) {
         int expectedId = lastReceiveId + 1;
         if (message.id != -1 && message.id != expectedId) {
            log("Receive id is incorrect (message from server)! Last Receive ID: " + lastReceiveId + ", Message Id: " + message.id + ", Expected: " + expectedId + ", Ignoring message and re-requesting!");
            // TODO: properly re-request
             return;
         }
         communicator.received.fire(message);
         if (message.id != -1) {     // Only increment if it's not a high priority message
             lastReceiveId = expectedId;
         } else if ("connected".equalsIgnoreCase(message.event)) {
             connected = true;
             failures = 0;
         }
     }
 
     public void handleError(int errorCode) {
         if (errorCode == MessageReceiveFailure.ConnectionNotFound) {
             communicator.error.show("Connection Not Found", "The server says the connection was not available to connect to. This may be caused by the connection timing out or a server restart. Reloading the page...");
             communicator.reload(false, true, 3000);
         } else if (errorCode == MessageReceiveFailure.ConnectionAlreadyExists) {
             communicator.error.show("Connection Already Exists", "The server says the referenced connection already exists. Reloading the page.");
             communicator.reload(false, true, 3000);
         } else if (errorCode == MessageReceiveFailure.InvalidMessageId) {
             communicator.error.show("Invalid Message ID", "The server says the referenced message id is incorrect. Reloading the page.");
             communicator.reload(false, true, 10000);
         } else {
             log("ConnectionManager.handleError: Unhandled error code: " + errorCode);
         }
     }
 
     private void log(String message) {
         GWTCommunicator.log(message);
     }
 }
