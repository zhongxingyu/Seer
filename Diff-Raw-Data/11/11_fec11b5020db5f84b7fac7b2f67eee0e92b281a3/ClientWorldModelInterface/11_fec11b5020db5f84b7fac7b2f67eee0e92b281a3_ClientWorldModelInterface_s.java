 /*
  * Owl Platform World Model Library for Java
  * Copyright (C) 2012 Robert Moore and the Owl Platform
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation; either version 2.1 of the License, or
  * (at your option) any later version.
  *  
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *  
  * You should have received a copy of the GNU Lesser General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 
 package com.owlplatform.worldmodel.client;
 
 import java.net.InetSocketAddress;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.apache.mina.core.RuntimeIoException;
 import org.apache.mina.core.future.ConnectFuture;
 import org.apache.mina.core.session.IdleStatus;
 import org.apache.mina.core.session.IoSession;
 import org.apache.mina.filter.codec.ProtocolCodecFilter;
 import org.apache.mina.filter.executor.ExecutorFilter;
 import org.apache.mina.transport.socket.nio.NioSocketConnector;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.owlplatform.worldmodel.Attribute;
 import com.owlplatform.worldmodel.client.listeners.ConnectionListener;
 import com.owlplatform.worldmodel.client.listeners.DataListener;
 import com.owlplatform.worldmodel.client.protocol.codec.WorldModelClientProtocolCodecFactory;
 import com.owlplatform.worldmodel.client.protocol.messages.AbstractRequestMessage;
 import com.owlplatform.worldmodel.client.protocol.messages.AttributeAliasMessage;
 import com.owlplatform.worldmodel.client.protocol.messages.AttributeAliasMessage.AttributeAlias;
 import com.owlplatform.worldmodel.client.protocol.messages.CancelRequestMessage;
 import com.owlplatform.worldmodel.client.protocol.messages.DataResponseMessage;
 import com.owlplatform.worldmodel.client.protocol.messages.HandshakeMessage;
 import com.owlplatform.worldmodel.client.protocol.messages.IdSearchMessage;
 import com.owlplatform.worldmodel.client.protocol.messages.IdSearchResponseMessage;
 import com.owlplatform.worldmodel.client.protocol.messages.KeepAliveMessage;
 import com.owlplatform.worldmodel.client.protocol.messages.OriginAliasMessage;
 import com.owlplatform.worldmodel.client.protocol.messages.OriginAliasMessage.OriginAlias;
 import com.owlplatform.worldmodel.client.protocol.messages.OriginPreferenceMessage;
 import com.owlplatform.worldmodel.client.protocol.messages.RangeRequestMessage;
 import com.owlplatform.worldmodel.client.protocol.messages.RequestCompleteMessage;
 import com.owlplatform.worldmodel.client.protocol.messages.SnapshotRequestMessage;
 import com.owlplatform.worldmodel.client.protocol.messages.StreamRequestMessage;
 
 /**
  * Handles low-level network interaction with the World Model for client
  * applications.
  * 
  * @author Robert Moore
  * 
  */
 public class ClientWorldModelInterface implements ClientIoAdapter {
 
   /**
    * Logging facility for this class.
    */
   private static final Logger log = LoggerFactory
       .getLogger(ClientWorldModelInterface.class);
 
   /**
    * Timeout value in seconds.
    */
   private static final int TIMEOUT_PERIOD = 60;
 
   /**
    * Host where the World Model is hosted.
    */
   private String host = "localhost";
 
   /**
    * Port on which the World Model is listening for client connections.
    */
   private int port = 7010;
 
   /**
    * The IoSession used to connect to the World Model
    */
   private IoSession session = null;
 
   /**
    * How long to wait (in milliseconds) on socket operations (open, close,
    * etc.).
    */
   private long connectionTimeout = 5000l;
 
   /**
    * How long to wait (in milliseconds) before reconnecting to the World Model.
    */
   private long connectionRetryDelay = 10000l;
 
   /**
    * Whether or not to disconnect from the World Model if an exception is
    * caught.
    */
   private boolean disconnectOnException = true;
 
   /**
    * Whether or not to reconnect to the World Model after the interface has
    * disconnected.
    */
   private boolean stayConnected = true;
 
   /**
    * The Handshake message sent to the World Model.
    */
   private HandshakeMessage sentHandshake = null;
 
   /**
    * The Handshake received from the World Model.
    */
   private HandshakeMessage receivedHandshake = null;
 
   /**
    * Session-based mapping of Attribute names to 32-bit unsigned integer values.
    */
   private final ConcurrentHashMap<Integer, String> attributeAliasValues = new ConcurrentHashMap<Integer, String>();
 
   /**
    * Session-based mapping of Origin values to 32-bit unsigned integer values.
    */
   private final ConcurrentHashMap<Integer, String> originAliasValues = new ConcurrentHashMap<Integer, String>();
 
   /**
    * Queue of interfaces that are interested in connection status events.
    */
   private final ConcurrentLinkedQueue<ConnectionListener> connectionListeners = new ConcurrentLinkedQueue<ConnectionListener>();
 
   /**
    * Queue of interfaces that are interested in data messages.
    */
   private final ConcurrentLinkedQueue<DataListener> dataListeners = new ConcurrentLinkedQueue<DataListener>();
 
   /**
    * MINA socket connector to connect to the world model.
    */
   private NioSocketConnector connector = null;
 
   /**
    * IOHandler for demultiplexing messages and events.
    */
   private WorldModelIoHandler ioHandler = new WorldModelIoHandler(this);
 
   /**
    * Worker threadpool for handling IO events.
    */
   private ExecutorFilter executors;
 
   /**
    * The next available ticket number for this World Model interface.
    */
   private volatile AtomicInteger nextTicketNumber = new AtomicInteger(1);
 
   /**
    * Requests sent to the World Model that have received Request Tickets but
    * have not yet completed. This would include all stream requests.
    */
   private final ConcurrentHashMap<Long, AbstractRequestMessage> outstandingRequests = new ConcurrentHashMap<Long, AbstractRequestMessage>();
 
   /**
    * Registers a listener to receive connection-related events from this
    * {@code ClientWorldModelInterface}.
    * 
    * @param listener
    *          the listener to register.
    */
   public void addConnectionListener(final ConnectionListener listener) {
     this.connectionListeners.add(listener);
   }
 
   /**
    * Unregisters a connection listener.
    * 
    * @param listener
    *          the listener to unregister.
    */
   public void removeConnectionListener(final ConnectionListener listener) {
     this.connectionListeners.remove(listener);
   }
 
   /**
    * Registers a listener to receive message-related events from this
    * {@code ClientWorldModelInterface}.
    * 
    * @param listener
    *          the listener to register
    */
   public void addDataListener(final DataListener listener) {
     this.dataListeners.add(listener);
   }
 
   /**
    * Unregisters a data listener.
    * 
    * @param listener
    *          the listener to unregister.
    */
   public void removeDataListener(final DataListener listener) {
     this.dataListeners.remove(listener);
   }
 
   @Override
   public void exceptionCaught(IoSession session, Throwable cause) {
     log.warn("Exception caught for {}: {}", this, cause);
     cause.printStackTrace();
     if (this.disconnectOnException) {
       this._disconnect();
     }
   }
 
   /**
    * Sets-up the connector for this MINA session.
    * 
    * @return {@code true} if the setup succeeds, else {@code false}.
    */
   protected boolean setConnector() {
     if (this.host == null) {
       log.error("No host value set, cannot set up socket connector.");
       return false;
     }
     if (this.port < 0 || this.port > 65535) {
       log.error("Port value is invalid {}.", Integer.valueOf(this.port));
       return false;
     }
     if (this.executors == null) {
       this.executors = new ExecutorFilter(1);
     }
 
     this.connector = new NioSocketConnector();
     this.connector.getSessionConfig().setIdleTime(IdleStatus.WRITER_IDLE,
         ClientWorldModelInterface.TIMEOUT_PERIOD / 2);
     this.connector.getSessionConfig().setIdleTime(IdleStatus.READER_IDLE,
         (int) (ClientWorldModelInterface.TIMEOUT_PERIOD * 1.1f));
     if (!this.connector.getFilterChain().contains(
         WorldModelClientProtocolCodecFactory.CODEC_NAME)) {
       this.connector.getFilterChain().addLast(
           WorldModelClientProtocolCodecFactory.CODEC_NAME,
           new ProtocolCodecFilter(
               new WorldModelClientProtocolCodecFactory(true)));
     }
     this.connector.getFilterChain().addLast("ExecutorPool", this.executors);
     this.connector.setHandler(this.ioHandler);
     log.debug("Connector set up successful.");
     return true;
   }
 
   /**
    * Initiates a connection to the World Model (if it is not yet connected).
    * 
    * @param maxWait
    *          how long to wait for the connection, in milliseconds
    * 
    * @return true if the connection is established.
    */
   public boolean connect(long maxWait) {
     long timeout = maxWait;
     if (timeout <= 0) {
       timeout = this.connectionTimeout;
     }
     if (this.connector == null) {
       if (!this.setConnector()) {
         log.error("Unable to set up connection to the World Model.");
         return false;
       }
     }
 
     if (this.session != null) {
       log.error("Already connected!");
       return false;
     }
 
     long waitTime = timeout;
     do {
       long startAttempt = System.currentTimeMillis();
       if (this._connect(waitTime)) {
         log.debug("Connection succeeded!");
         return true;
       }
 
       if (this.stayConnected) {
         long retryDelay = this.connectionRetryDelay;
         if (timeout < this.connectionRetryDelay * 2) {
           retryDelay = timeout / 2;
           if (retryDelay < 500) {
             retryDelay = 500;
           }
         }
         try {
           log.warn(String
               .format(
                   "Connection to World Model at %s:%d failed, waiting %dms before retrying.",
                   this.host, Integer.valueOf(this.port),
                   Long.valueOf(retryDelay)));
           Thread.sleep(retryDelay);
         } catch (InterruptedException ie) {
           // Ignored
         }
         waitTime = waitTime - (System.currentTimeMillis() - startAttempt);
       }
    } while (this.stayConnected);
 
     this._disconnect();
     this.finishConnection();
 
     return false;
   }
 
   /**
    * Cleans-up any resources after a connection has terminated. Should be called
    * when the connection is disconnected and reconnect is not desired.
    */
   void finishConnection() {
     this.connector.dispose();
     this.connector = null;
     for (ConnectionListener listener : this.connectionListeners) {
       listener.connectionEnded(this);
     }
     if (this.executors != null) {
       this.executors.destroy();
       this.executors = null;
     }
   }
 
   /**
    * Disconnects from the world model if it is connected.
    */
   public void disconnect() {
     // Make sure we don't automatically reconnect
     this.stayConnected = false;
     this._disconnect();
   }
 
   /**
    * Attempts a connection to the world model.
    * 
    * @param timeout
    *          the connection timeout value in milliseconds.
    * 
    * @return {@code true} if the attempt succeeds, else {@code false}.
    */
   protected boolean _connect(long timeout) {
     log.debug("Attempting connection...");
     ConnectFuture connFuture = this.connector.connect(new InetSocketAddress(
         this.host, this.port));
     if (!connFuture.awaitUninterruptibly(timeout)) {
       log.warn("Unable to connect to world model after {}ms.",
           Long.valueOf(this.connectionTimeout));
       return false;
     }
     if (!connFuture.isConnected()) {
       log.debug("Failed to connect.");
       return false;
     }
 
     try {
       log.debug("Attempting connection to {}:{}.", this.host,
           Integer.valueOf(this.port));
       this.session = connFuture.getSession();
     } catch (RuntimeIoException ioe) {
       log.error(String.format(
           "Could not create session to World Model (C) %s:%d.", this.host,
           Integer.valueOf(this.port)), ioe);
       return false;
     }
     return true;
   }
 
   /**
    * Disconnects from the world model.
    */
   protected void _disconnect() {
     log.info("Disconnect called.");
     if (this.session != null) {
       log.info(
           "Closing connection to World Model (client) at {} (waiting {}ms).",
           this.session.getRemoteAddress(), Long.valueOf(this.connectionTimeout));
       while (!this.session.close(false).awaitUninterruptibly(
           this.connectionTimeout)) {
         log.error("Connection didn't close after {}ms.",
             Long.valueOf(this.connectionTimeout));
       }
 
       this.session = null;
       this.sentHandshake = null;
       this.receivedHandshake = null;
       this.attributeAliasValues.clear();
       this.originAliasValues.clear();
       for (ConnectionListener listener : this.connectionListeners) {
         listener.connectionInterrupted(this);
       }
     }
   }
 
   @Override
   public void sessionIdle(IoSession session, IdleStatus status) {
     if (session == null) {
       return;
     }
     if (status.equals(IdleStatus.READER_IDLE)) {
       log.error("World Model timed-out. Disconnecting.");
       this._disconnect();
       return;
     }
     if (status.equals(IdleStatus.WRITER_IDLE)
         || status.equals(IdleStatus.BOTH_IDLE)) {
       log.debug("Writing Keep-Alive message to World Model at {}",
           session.getRemoteAddress());
       session.write(KeepAliveMessage.MESSAGE);
     }
   }
 
   @Override
   public void connectionOpened(IoSession session) {
     if (this.session == null) {
       log.warn("Session was not correctly stored during connection set-up.");
       this.session = session;
     }
 
     log.info("Connected to World Model (client) at {}.",
         session.getRemoteAddress());
 
     log.debug("Attempting to write handshake.");
     this.session.write(HandshakeMessage.getDefaultMessage());
   }
 
   @Override
   public void connectionClosed(IoSession session) {
     this._disconnect();
     while (this.stayConnected) {
       log.info("Reconnecting to World Model (C) {}:{}", this.host,
           Integer.valueOf(this.port));
 
       try {
         Thread.sleep(this.connectionRetryDelay);
       } catch (InterruptedException ie) {
         // Ignored
       }
 
       if (this.connect(this.connectionTimeout)) {
         return;
       }
     }
 
     this.finishConnection();
   }
 
   @Override
   public void handshakeReceived(IoSession session, HandshakeMessage message) {
     log.debug("Received {}", message);
     this.receivedHandshake = message;
     Boolean handshakeCheck = this.checkHandshake();
     if (handshakeCheck == null) {
       return;
     }
 
     if (Boolean.FALSE.equals(handshakeCheck)) {
       log.warn("Handshakes did not match.");
       this._disconnect();
     }
     if (Boolean.TRUE.equals(handshakeCheck)) {
       log.debug("Handshakes matched with {}.", this);
       for (ConnectionListener listener : this.connectionListeners) {
         listener.connectionEstablished(this);
       }
     }
   }
 
   /**
    * Checks the sent and received handshakes.
    * 
    * @return {@code Boolean.TRUE} if the handshakes are valid,
    *         {@code Boolean.FALSE} if the handshakes are invalid, or
    *         {@code null} if one of the handshakes is unavailable for checking.
    */
   protected Boolean checkHandshake() {
     if (this.sentHandshake == null) {
       log.debug("Sent handshake is null, not checking.");
       return null;
     }
     if (this.receivedHandshake == null) {
       log.debug("Received handshake is null, not checking.");
       return null;
     }
 
     if (!this.sentHandshake.equals(this.receivedHandshake)) {
       log.error(
           "Handshakes do not match.  Closing connection to world model at {}.",
           this.session.getRemoteAddress());
       boolean prevValue = this.stayConnected;
       this.stayConnected = false;
       this._disconnect();
       this.stayConnected = prevValue;
       return Boolean.FALSE;
     }
     return Boolean.TRUE;
   }
 
   @Override
   public void keepAliveReceived(IoSession session, KeepAliveMessage message) {
     log.debug("Received Keep-Alive message.");
   }
 
   @Override
   public void snapshotRequestReceived(IoSession session,
       SnapshotRequestMessage message) {
     log.error(
         "Client should not receive snapshot requests from the World Model: {}",
         message);
     this._disconnect();
 
   }
 
   @Override
   public void rangeRequestReceived(IoSession session,
       RangeRequestMessage message) {
     log.error(
         "Client should not receive snapshot requests from the World Model: {}",
         message);
     this._disconnect();
   }
 
   @Override
   public void streamRequestReceived(IoSession session,
       StreamRequestMessage message) {
     log.error(
         "Client should not received stream requests from the World Model: {}",
         message);
     this._disconnect();
   }
 
   @Override
   public void attributeAliasReceived(IoSession session,
       AttributeAliasMessage message) {
     log.debug("Received Attribute Aliases.");
     AttributeAlias[] aliases = message.getAliases();
     if (aliases == null) {
       log.warn("Attribute aliases were null!");
       return;
     }
     for (AttributeAlias alias : aliases) {
       this.attributeAliasValues.put(Integer.valueOf(alias.aliasNumber),
           alias.attributeName);
       log.debug("Attribute ({})->{}", alias.attributeName,
           Integer.valueOf(alias.aliasNumber));
     }
 
     for (DataListener listener : this.dataListeners) {
       listener.attributeAliasesReceived(this, message);
     }
   }
 
   @Override
   public void originAliasReceived(IoSession session, OriginAliasMessage message) {
     log.debug("Received Origin Aliases.");
     OriginAlias[] aliases = message.getAliases();
     if (aliases == null) {
       log.warn("Origin aliases were null!");
       return;
     }
     for (OriginAlias alias : aliases) {
       this.originAliasValues.put(Integer.valueOf(alias.aliasNumber),
           alias.origin);
       log.debug("Origin ({})->{}", alias.origin,
           Integer.valueOf(alias.aliasNumber));
     }
 
     for (DataListener listener : this.dataListeners) {
       listener.originAliasesReceived(this, message);
     }
   }
 
   @Override
   public void requestCompleteReceived(IoSession session,
       RequestCompleteMessage message) {
     Long ticketNumber = Long.valueOf(message.getTicketNumber());
     log.debug("Request {} has completed.", ticketNumber);
 
     AbstractRequestMessage request = this.outstandingRequests.get(ticketNumber);
     if (request == null) {
       log.error("Unable to retrieve request for ticket {}.", ticketNumber);
       this._disconnect();
       return;
     }
     for (DataListener listener : this.dataListeners) {
       listener.requestCompleted(this, request);
     }
 
   }
 
   @Override
   public void cancelRequestReceived(IoSession session,
       CancelRequestMessage message) {
     log.error("Client should not receive Cancel Request messages from the World Model.");
     this._disconnect();
   }
 
   @Override
   public void dataResponseReceived(IoSession session,
       DataResponseMessage message) {
     if (message.getAttributes() != null) {
       for (Attribute attr : message.getAttributes()) {
         String attributeName = this.attributeAliasValues.get(Integer
             .valueOf(attr.getAttributeNameAlias()));
         if (attributeName == null) {
           log.error("World Model sent unknown Attribute Alias {}.",
               Integer.valueOf(attr.getAttributeNameAlias()));
           this._disconnect();
           return;
         }
         attr.setAttributeName(attributeName);
 
         String originName = this.originAliasValues.get(Integer.valueOf(attr
             .getOriginNameAlias()));
         if (originName == null) {
           log.error("World Model sent unknown Origin Alias {}.",
               Integer.valueOf(attr.getOriginNameAlias()));
           this._disconnect();
           return;
         }
         attr.setOriginName(originName);
       }
     }
 
     log.debug("Received data response from {}: {}", this, message);
 
     for (DataListener listener : this.dataListeners) {
       listener.dataResponseReceived(this, message);
     }
   }
 
   /**
    * Sends a request message to the world model.
    * 
    * @param message
    *          the message to send.
    * @return the ticket number of the request.
    */
   public synchronized long sendMessage(AbstractRequestMessage message) {
     log.debug("Sending {} to {}", message, this);
     message.setTicketNumber(this.nextTicketNumber.getAndIncrement());
     this.outstandingRequests.put(Long.valueOf(message.getTicketNumber()),
         message);
     this.session.write(message);
     return message.getTicketNumber();
   }
 
   /**
    * Cancels the request with the specified ticket number. Does nothing if the
    * ticket is already complete or the ticket number doesn't match an existing
    * request.
    * 
    * @param ticketNumber
    *          the ticket number of the request to cancel.
    */
   public void cancelRequest(long ticketNumber) {
     if (this.session != null
         && this.outstandingRequests.containsKey(Long.valueOf(ticketNumber))) {
       CancelRequestMessage message = new CancelRequestMessage();
       message.setTicketNumber(ticketNumber);
       this.session.write(message);
     } else {
       log.warn("Tried to cancel unknown request for ticket number {}.",
           Long.valueOf(ticketNumber));
     }
   }
 
   @Override
   public void idSearchReceived(IoSession session, IdSearchMessage message) {
     log.error("Client should not receive URI search messages from the World Model.");
     this._disconnect();
   }
 
   @Override
   public void URISearchResponseReceived(IoSession session,
       IdSearchResponseMessage message) {
     log.debug("Received URI search response from {}: {}", this, message);
     for (DataListener listener : this.dataListeners) {
       listener.idSearchResponseReceived(this, message);
     }
 
   }
 
   @Override
   public void handshakeSent(IoSession session, HandshakeMessage message) {
     log.debug("Sent {}", message);
     this.sentHandshake = message;
     Boolean handshakeCheck = this.checkHandshake();
     if (handshakeCheck == null) {
       return;
     }
 
     if (Boolean.FALSE.equals(handshakeCheck)) {
       log.warn("Handshakes did not match.");
       this._disconnect();
     }
     if (Boolean.TRUE.equals(handshakeCheck)) {
       log.debug("Handshakes matched with {}.", this);
       for (ConnectionListener listener : this.connectionListeners) {
         listener.connectionEstablished(this);
       }
     }
   }
 
   @Override
   public void keepAliveSent(IoSession session, KeepAliveMessage message) {
     log.debug("Sending Keep-Alive message.");
   }
 
   @Override
   public void snapshotRequestSent(IoSession session,
       SnapshotRequestMessage message) {
     log.debug("Sent Snapshot request to {}: {}", this, message);
   }
 
   @Override
   public void rangeRequestSent(IoSession session, RangeRequestMessage message) {
     log.debug("Sent Range request to {}: {}", this, message);
   }
 
   @Override
   public void streamRequestSent(IoSession session, StreamRequestMessage message) {
     log.debug("Sent Stream request to {}: {}", this, message);
   }
 
   @Override
   public void attributeAliasSent(IoSession session,
       AttributeAliasMessage message) {
     log.error("Client should not send Attribute Alias messages to the World Model.");
     this._disconnect();
   }
 
   @Override
   public void originAliasSent(IoSession session, OriginAliasMessage message) {
     log.error("Client should not send Origin Alias messages to the World Model.");
     this._disconnect();
   }
 
   @Override
   public void requestCompleteSent(IoSession session,
       RequestCompleteMessage message) {
     log.error("Client should not send Request Complete messages to the World Model.");
     this._disconnect();
   }
 
   @Override
   public void cancelRequestSent(IoSession session, CancelRequestMessage message) {
     log.debug("Sent Cancel Request to {}: {}", this, message);
   }
 
   @Override
   public void dataResponseSent(IoSession session, DataResponseMessage message) {
     log.error("Client should not send Data Respones messages to the World Model.");
     this._disconnect();
   }
 
   @Override
   public void idSearchSent(IoSession session, IdSearchMessage message) {
     log.debug("Sent URI Search message to {}: {}", this, message);
   }
 
   @Override
   public void idSearchResponseSent(IoSession session,
       IdSearchResponseMessage message) {
     log.error("Client should not send URI Search Responses to the World Model.");
     this._disconnect();
   }
 
   /**
    * Search for an Identifier regular expression.
    * 
    * @param idRegex
    *          the regular expression to search.
    * @return {@code true} if the request was sent, else {@code false}.
    */
   public boolean searchIdRegex(final String idRegex) {
     if (idRegex == null) {
       log.error("Unable to search for a null Identifier regex.");
       return false;
     }
     IdSearchMessage message = new IdSearchMessage();
     message.setIdRegex(idRegex);
     this.session.write(message);
     log.debug("Sent {}", message);
 
     return true;
   }
 
   /**
    * Gets the world model host.
    * 
    * @return the host value.
    */
   public String getHost() {
     return this.host;
   }
 
   /**
    * Sets the world model host.
    * 
    * @param host
    *          the new host value
    */
   public void setHost(String host) {
     this.host = host;
   }
 
   /**
    * Gets the world model port.
    * 
    * @return the world model port.
    */
   public int getPort() {
     return this.port;
   }
 
   /**
    * Sets the world model port.
    * 
    * @param port
    *          the new port value.
    */
   public void setPort(int port) {
     this.port = port;
   }
 
   /**
    * Gets the connection timeout value in milliseconds. Connection attempts will
    * give-up after this timeout expires.
    * 
    * @return the connection timeout value in milliseconds.
    */
   public long getConnectionTimeout() {
     return this.connectionTimeout;
   }
 
   /**
    * Sets the connection timeout value in milliseconds. Connection attempts will
    * give-up after this timeout expires.
    * 
    * @param connectionTimeout
    *          the new connection timeout value in milliseconds.
    */
   public void setConnectionTimeout(long connectionTimeout) {
     this.connectionTimeout = connectionTimeout;
   }
 
   /**
    * Gets the connection retry delay, the interval between connection attempts.
    * 
    * @return the connection retry delay in milliseconds.
    */
   public long getConnectionRetryDelay() {
     return this.connectionRetryDelay;
   }
 
   /**
    * Sets the connection retry delay, the interval between connection attempts.
    * 
    * @param connectionRetryDelay
    *          the new connection retry delay in milliseconds.
    */
   public void setConnectionRetryDelay(long connectionRetryDelay) {
     this.connectionRetryDelay = connectionRetryDelay;
   }
 
   /**
    * Whether uncaught exceptions should cause a disconnect.
    * 
    * @return if uncaught exceptions should cause a disconnect.
    */
   public boolean isDisconnectOnException() {
     return this.disconnectOnException;
   }
 
   /**
    * Sets whether uncaught exceptions should cause a disconnect.
    * 
    * @param disconectOnException
    *          {@code true} to disconnect on exception, {@code false} to remain
    *          connected.
    */
   public void setDisconnectOnException(boolean disconectOnException) {
     this.disconnectOnException = disconectOnException;
   }
 
   /**
    * Whether or not to reconnect when a disconnect is caused by something other
    * than a call to {@code disconnect()}.
    * 
    * @return whether or not to automatically reconnect.
    */
   public boolean isStayConnected() {
     return this.stayConnected;
   }
 
   /**
    * Sets whether or not to reconnect when a disconect is caused by something
    * other than a call to {@code disconnect()}.
    * 
    * @param stayConnected
    *          {@code true}to reconnect, {@code false} to stay disconected.
    */
   public void setStayConnected(boolean stayConnected) {
     this.stayConnected = stayConnected;
   }
 
   @Override
   public String toString() {
     StringBuffer sb = new StringBuffer("Client-World Model Interface");
     if (this.host != null) {
       sb.append(" (").append(this.host);
       if (this.port > 0) {
         sb.append(":").append(this.port);
       }
       sb.append(")");
     }
     return sb.toString();
 
   }
 
   @Override
   public void originPreferenceReceived(IoSession session,
       OriginPreferenceMessage message) {
     log.error("Should not receive an origin preference message from the world model.");
     this._disconnect();
   }
 
   @Override
   public void OriginPreferenceSent(IoSession session,
       OriginPreferenceMessage message) {
     log.debug("Sent {}", message);
 
     for (DataListener listener : this.dataListeners) {
       listener.originPreferenceSent(this, message);
     }
 
   }
 }
