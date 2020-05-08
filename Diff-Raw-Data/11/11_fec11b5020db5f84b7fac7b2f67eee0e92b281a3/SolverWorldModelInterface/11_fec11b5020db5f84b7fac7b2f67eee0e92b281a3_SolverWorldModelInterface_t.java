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
 
 package com.owlplatform.worldmodel.solver;
 
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
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
 import com.owlplatform.worldmodel.solver.WorldModelIoHandler;
 import com.owlplatform.worldmodel.solver.listeners.ConnectionListener;
 import com.owlplatform.worldmodel.solver.listeners.DataListener;
 import com.owlplatform.worldmodel.solver.protocol.codec.WorldModelSolverProtocolCodecFactory;
 import com.owlplatform.worldmodel.solver.protocol.messages.CreateIdentifierMessage;
 import com.owlplatform.worldmodel.solver.protocol.messages.AttributeUpdateMessage;
 import com.owlplatform.worldmodel.solver.protocol.messages.DeleteAttributeMessage;
 import com.owlplatform.worldmodel.solver.protocol.messages.DeleteIdentifierMessage;
 import com.owlplatform.worldmodel.solver.protocol.messages.ExpireAttributeMessage;
 import com.owlplatform.worldmodel.solver.protocol.messages.ExpireIdentifierMessage;
 import com.owlplatform.worldmodel.solver.protocol.messages.HandshakeMessage;
 import com.owlplatform.worldmodel.solver.protocol.messages.KeepAliveMessage;
 import com.owlplatform.worldmodel.solver.protocol.messages.StartOnDemandMessage;
 import com.owlplatform.worldmodel.solver.protocol.messages.StopOnDemandMessage;
 import com.owlplatform.worldmodel.solver.protocol.messages.AttributeAnnounceMessage;
 import com.owlplatform.worldmodel.solver.protocol.messages.AttributeAnnounceMessage.AttributeSpecification;
 
 /**
  * Handles low-level network interaction with the World Model for solvers.
  * 
  * @author Robert Moore
  * 
  */
 public class SolverWorldModelInterface implements SolverIoAdapter {
 
   /**
    * Logging facility for this class.
    */
   private static final Logger log = LoggerFactory
       .getLogger(SolverWorldModelInterface.class);
 
   /**
    * Timeout value in seconds.
    */
   private static final int TIMEOUT_PERIOD = 60;
 
   /**
    * Host where the World Model is hosted.
    */
   private String host = null;
 
   /**
    * Port on which the World Model is listening for solver connections.
    */
   private int port = 7009;
 
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
    * Flag to indicate that the connection is ready for solvers to interact with
    * the World Model.
    */
   private boolean connectionReady = false;
 
   /**
    * Indicates whether or not this World Model Interface is ready for solvers to
    * interact with the World Model. Specifically, it will return true when the
    * local handshake has been sent and the remote (World Model) handshake has
    * been received.
    * 
    * @return {@code true} if the connection is ready for interaction, else
    *         {@code false}.
    */
   public boolean isReady() {
     return this.connectionReady;
   }
 
   /**
    * MINA connector for the socket.
    */
   private NioSocketConnector connector = null;
 
   /**
    * Private IOHandler to hide interface methods.
    */
   private WorldModelIoHandler ioHandler = new WorldModelIoHandler(this);
 
   /**
    * Thread pool filter for handling messages/events in non-IO threads.
    */
   private ExecutorFilter executors;
 
   /**
    * Attribute aliases for the current connection.
    */
   private final ConcurrentHashMap<String, Integer> attributeAliases = new ConcurrentHashMap<String, Integer>();
 
   /**
    * Queue of interfaces that are interested in connection status events.
    */
   private final ConcurrentLinkedQueue<ConnectionListener> connectionListeners = new ConcurrentLinkedQueue<ConnectionListener>();
 
   /**
    * Queue of interfaces that are interested in data-related events.
    */
   private final ConcurrentLinkedQueue<DataListener> dataListeners = new ConcurrentLinkedQueue<DataListener>();
 
   /**
    * List of Attribute types to be sent to the World Model after handshaking.
    */
   private final ConcurrentLinkedQueue<AttributeSpecification> attributes = new ConcurrentLinkedQueue<AttributeSpecification>();
 
   /**
    * Origin String for the solver, sent to the World Model.
    */
   private String originString = null;
 
   /**
    * Whether or not to create target Identifiers if they don't already exist in
    * the World Model.
    */
   private boolean createIds = true;
 
   /**
    * Gets whether or not to automatically create Identifiers in the world model
    * when Attribute updates are sent.
    * 
    * @return {@code true} if Identifiers are automatically created, else
    *         {@code false}.
    */
   public boolean isCreateIds() {
     return this.createIds;
   }
 
   /**
    * Sets whether to automatically create Identifiers in the world model when
    * Attribute values are updated.
    * 
    * @param createIds
    *          the new value: {@code true} to automatically create Identifiers,
    *          or {@code false} to ignore Attribute values for uncreated
    *          Identifiers.
    */
   public void setCreateIds(boolean createIds) {
     this.createIds = createIds;
   }
 
   /**
    * Flag to indicate whether attribute specifications have been sent.
    */
   private boolean sentAttrSpecifications = false;
 
   /**
    * Registers a connection listener for the world model connection.
    * 
    * @param listener
    *          the listener to add.
    */
   public void addConnectionListener(final ConnectionListener listener) {
     this.connectionListeners.add(listener);
   }
 
   /**
    * Unregisters a connection listener from the world model connection.
    * 
    * @param listener
    *          the listener to remove.
    */
   public void removeConnectionListener(final ConnectionListener listener) {
     this.connectionListeners.remove(listener);
   }
 
   /**
    * Registers a data listener for the world model connection.
    * 
    * @param listener
    *          the listener to add.
    */
   public void addDataListener(final DataListener listener) {
     this.dataListeners.add(listener);
   }
 
   /**
    * Unregisters a data listener from the world model connection.
    * 
    * @param listener
    *          the listener to remove.
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
    * Sets the MINA connector and establishes the connection to the world model.
    * Adds protocol filters.
    * 
    * @return {@code true} if connection is set-up correctly, else {@code false}.
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
         SolverWorldModelInterface.TIMEOUT_PERIOD / 2);
     if (!this.connector.getFilterChain().contains(
         WorldModelSolverProtocolCodecFactory.CODEC_NAME)) {
       this.connector.getFilterChain().addLast(
           WorldModelSolverProtocolCodecFactory.CODEC_NAME,
           new ProtocolCodecFilter(
               new WorldModelSolverProtocolCodecFactory(true)));
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
    } while (this.stayConnected && waitTime > 0);
 
     this._disconnect();
     this.finishConnection();
 
     return false;
   }
 
   /**
    * Takes care of any post-disconnect cleanup necessary for the connection.
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
    * Disconnects from the world model. Automatic reconnect is disabled for this
    * operation.
    */
   public void disconnect() {
     this.stayConnected = false;
     this._disconnect();
   }
 
   /**
    * Connects to the world model with the current parameter set.
    * 
    * @param timeout
    *          the connection timeout value in milliseconds.
    * 
    * @return {@code true} if successful, else {@code false}.
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
           "Could not create session to World Model (S) %s:%d.", this.host,
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
           "Closing connection to World Model (solver) at {} (waiting {}ms).",
           this.session.getRemoteAddress(), Long.valueOf(this.connectionTimeout));
       while(!this.session.close(false).awaitUninterruptibly(this.connectionTimeout)){
         log.error("Connection didn't close after {}ms.",Long.valueOf(this.connectionTimeout));
       }
       this.session = null;
       this.sentHandshake = null;
       this.receivedHandshake = null;
       this.sentAttrSpecifications = false;
       for (ConnectionListener listener : this.connectionListeners) {
         listener.connectionInterrupted(this);
       }
     }
   }
 
   @Override
   public void sessionIdle(IoSession session, IdleStatus status) {
     if (status.equals(IdleStatus.WRITER_IDLE)) {
       log.debug("Writing Keep-Alive message to World Model at {}",
           this.session.getRemoteAddress());
       this.session.write(KeepAliveMessage.MESSAGE);
     }
   }
 
   @Override
   public void connectionOpened(IoSession session) {
     if (this.session == null) {
       log.warn("Session was not correctly stored during connection set-up.");
       this.session = session;
     }
 
     log.info("Connected to World Model (solver) at {}.",
         session.getRemoteAddress());
 
     for (ConnectionListener listener : this.connectionListeners) {
       listener.connectionEstablished(this);
     }
 
     log.debug("Attempting to write handshake.");
     this.session.write(HandshakeMessage.getDefaultMessage());
   }
 
   @Override
   public void connectionClosed(IoSession session) {
     this._disconnect();
     while (this.stayConnected) {
       log.info("Reconnecting to World Model (S) {}:{}", this.host,
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
     }
   }
 
   /**
    * Verifies the sent and received handshakes.
    * 
    * @return {@code null} if one or both handshakes are not yet exchanged,
    *         {@code Boolean#TRUE} if the handshakes are valid, or
    *         {@code Boolean#FALSE} if one or both handshakes are invalid.
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
     this.connectionReady = true;
     this.announceAttributes();
     return Boolean.TRUE;
   }
 
   /**
    * Sends the Attribute aliases to the world model.
    */
   protected void announceAttributes() {
     if (this.originString == null) {
       log.error("Unable to announce solution types, no Origin String set.");
       return;
     }
     this.sentAttrSpecifications = true;
     if (this.attributes.size() > 0) {
       AttributeAnnounceMessage message = new AttributeAnnounceMessage();
 
       message.setOrigin(this.originString);
 
       ArrayList<AttributeSpecification> specificationList = new ArrayList<AttributeSpecification>();
       specificationList.addAll(this.attributes);
       AttributeSpecification[] specs = new AttributeSpecification[specificationList
           .size()];
       int specAlias = 0;
       for (AttributeSpecification spec : specificationList) {
         specs[specAlias] = spec;
         spec.setAlias(specAlias++);
         this.attributeAliases.put(spec.getAttributeName(),
             Integer.valueOf(spec.getAlias()));
       }
       message.setTypeSpecifications(specs);
       this.session.write(message);
       this.sentAttrSpecifications = true;
     }
   }
 
   @Override
   public void keepAliveReceived(IoSession session, KeepAliveMessage message) {
     log.debug("Received Keep-Alive message.");
   }
 
   @Override
   public void attributeAnnounceReceived(IoSession session,
       AttributeAnnounceMessage message) {
     log.error("World Model should not send type announce messages to the solver.");
     this._disconnect();
   }
 
   @Override
   public void startOnDemandReceived(IoSession session,
       StartOnDemandMessage message) {
     log.debug("Received Start Transient message from world model.");
     for (DataListener listener : this.dataListeners) {
       listener.startOnDemandReceived(this, message);
     }
   }
 
   @Override
   public void stopOnDemandReceived(IoSession session,
       StopOnDemandMessage message) {
     log.debug("Received Stop Transient message from world model.");
     for (DataListener listener : this.dataListeners) {
       listener.stopOnDemandReceived(this, message);
     }
   }
 
   @Override
   public void attributeUpdateReceived(IoSession session,
       AttributeUpdateMessage message) {
     log.error("World Model should not send Data Transfer messages to solvers.");
     this._disconnect();
   }
 
   @Override
   public void createIdReceived(IoSession session,
       CreateIdentifierMessage message) {
     log.error("World Model should not send Create Identifier messages to solvers.");
     this._disconnect();
   }
 
   @Override
   public void expireIdReceived(IoSession session,
       ExpireIdentifierMessage message) {
     log.error("World Model should not send Expire Identifier messages to solvers.");
     this._disconnect();
   }
 
   @Override
   public void deleteIdReceived(IoSession session,
       DeleteIdentifierMessage message) {
     log.error("World Model should not send Delete Identifier messages to solvers.");
     this._disconnect();
   }
 
   @Override
   public void expireAttributeReceived(IoSession session,
       ExpireAttributeMessage message) {
     log.error("World Model should not send Expire Attribute messages to solvers.");
     this._disconnect();
   }
 
   @Override
   public void deleteAttributeReceived(IoSession session,
       DeleteAttributeMessage message) {
     log.error("World Model should ot send Delete Attribute messages to solvers.");
     this._disconnect();
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
     }
   }
 
   @Override
   public void keepAliveSent(IoSession session, KeepAliveMessage message) {
     log.debug("Sending Keep-Alive message.");
   }
 
   @Override
   public void attributeAnnounceSent(IoSession session,
       AttributeAnnounceMessage message) {
     log.debug("Sent Type Announce message to {}: {}", this, message);
     for (DataListener listener : this.dataListeners) {
       listener.attributeSpecificationsSent(this, message);
     }
   }
 
   @Override
   public void startOnDemandSent(IoSession session, StartOnDemandMessage message) {
     log.error("Solver should not send Start Transient messages to the World Model.");
     this._disconnect();
   }
 
   @Override
   public void stopOnDemandSent(IoSession session, StopOnDemandMessage message) {
     log.error("Solver should not send Stop Transient messages to the World Model.");
     this._disconnect();
   }
 
   @Override
   public void attributeUpdateSent(IoSession session,
       AttributeUpdateMessage message) {
     log.debug("Sent Data Transfer to {}: {}", this, message);
   }
 
   @Override
   public void createIdSent(IoSession session, CreateIdentifierMessage message) {
     log.debug("Sent Create Identifier to {}: {}", this, message);
   }
 
   @Override
   public void expireIdSent(IoSession session, ExpireIdentifierMessage message) {
     log.debug("Sent Expire Identifier to {}: {}", this, message);
   }
 
   @Override
   public void deleteIdSent(IoSession session, DeleteIdentifierMessage message) {
     log.debug("Sent Delete Identifier to {}: {}", this, message);
   }
 
   @Override
   public void expireAttributeSent(IoSession session,
       ExpireAttributeMessage message) {
     log.debug("Sent Expire Attribute to {}: {}", this, message);
   }
 
   @Override
   public void deleteAttributeSent(IoSession session,
       DeleteAttributeMessage message) {
     log.debug("Sent Delete Attribute to {}: {}", this, message);
   }
 
   /**
    * Sends a single Attribute update message to the world model.
    * 
    * @param attribute
    *          the new Attribute value.
    * @return {@code true} if the message was sent successfully, else
    *         {@code false}.
    */
   public boolean updateAttribute(final Attribute attribute) {
     if (!this.sentAttrSpecifications) {
       log.error("Haven't sent type specifications yet, can't send solutions.");
       return false;
     }
 
     AttributeUpdateMessage message = new AttributeUpdateMessage();
 
     message.setCreateId(this.createIds);
     message.setAttributes(new Attribute[] { attribute });
 
     Integer attributeAlias = this.attributeAliases.get(attribute
         .getAttributeName());
     if (attributeAlias == null) {
       log.error("Cannot update attribute: Unregistered attribute type: {}",
           attribute.getAttributeName());
       return false;
     }
 
     attribute.setAttributeNameAlias(attributeAlias.intValue());
 
     this.session.write(message);
     log.debug("Sent {} to {}", message, this);
 
     return true;
   }
 
   /**
    * Sends a multiple Attribute update messages to the world model.
    * 
    * @param attributes
    *          the Attribute values to update
    * @return {@code true} if the messages were written, or {@code false} if one
    *         or more messages failed to send.
    */
   public boolean updateAttributes(final Collection<Attribute> attributes) {
     if (!this.sentAttrSpecifications) {
       log.error("Haven't sent type specifications yet, can't send solutions.");
       return false;
     }
     for (Iterator<Attribute> iter = attributes.iterator(); iter.hasNext();) {
       Attribute soln = iter.next();
       Integer solutionTypeAlias = this.attributeAliases.get(soln
           .getAttributeName());
       if (solutionTypeAlias == null) {
         log.error("Cannot send solution: Unregistered attribute type: {}",
             soln.getAttributeName());
         iter.remove();
         continue;
       }
       soln.setAttributeNameAlias(solutionTypeAlias.intValue());
     }
 
     AttributeUpdateMessage message = new AttributeUpdateMessage();
 
     message.setCreateId(this.createIds);
 
     message.setAttributes(this.attributes.toArray(new Attribute[] {}));
 
     this.session.write(message);
 
     return true;
   }
 
   /**
    * Adds an attribute specification to the world model session.
    * 
    * @param specification
    *          the Attribute that will be sent later.
    */
   public void addAttribute(AttributeSpecification specification) {
     synchronized (this.attributes) {
       if (!this.attributes.contains(specification)) {
         this.attributes.add(specification);
 
         if (this.sentAttrSpecifications) {
           this.announceAttributes();
         }
       }
     }
   }
 
   /**
    * Gets the currently-configured world model host. This may differ from the
    * current connection information if it was changed since the connection was
    * established.
    * 
    * @return the current world model host value.
    */
   public String getHost() {
     return this.host;
   }
 
   /**
    * Sets the world model host value. Setting this value has no effect until the
    * next attempt to connect to the world model.
    * 
    * @param host
    *          the new host value.
    */
   public void setHost(String host) {
     this.host = host;
   }
 
   /**
    * Gets the currently-configured world model port value. This may differ from
    * the current connection information if it was changed since the connection
    * was established.
    * 
    * @return the currently world model port value.
    */
   public int getPort() {
     return this.port;
   }
 
   /**
    * Sets the world model port value for Solver connections. Note that this
    * setting only takes effect on the next connection attempt.
    * 
    * @param port
    *          the new world model port value.
    */
   public void setPort(int port) {
     this.port = port;
   }
 
   /**
    * Gets the current connection timeout value.
    * 
    * @return the connection timeout value, in milliseconds.
    */
   public long getConnectionTimeout() {
     return this.connectionTimeout;
   }
 
   /**
    * Sets the timeout for making connections to the world model. Connections
    * wait at most this period of time before declaring the connection failed.
    * 
    * @param connectionTimeout
    *          the new connection timeout value, in milliseconds.
    */
   public void setConnectionTimeout(long connectionTimeout) {
     this.connectionTimeout = connectionTimeout;
   }
 
   /**
    * Get the current connection retry delay value. The amount of time to wait
    * between a disconnect and reconnect attempt.
    * 
    * @return the current connection retry delay value, in milliseconds.
    */
   public long getConnectionRetryDelay() {
     return this.connectionRetryDelay;
   }
 
   /**
    * Sets the connection retry delay value. This is the amount of time to wait
    * between a disconnect event and a reconnection attempt.
    * 
    * @param connectionRetryDelay
    *          the new reconnect delay value, in milliseconds.
    */
   public void setConnectionRetryDelay(long connectionRetryDelay) {
     this.connectionRetryDelay = connectionRetryDelay;
   }
 
   /**
    * If exceptions should cause a disconnect from the world model.
    * 
    * @return {@code true} if exceptions will cause a disconnect.
    */
   public boolean isDisconnectOnException() {
     return this.disconnectOnException;
   }
 
   /**
    * Set whether to disconnect from the world model when exceptions are thrown.
    * 
    * @param disconnectOnException
    *          {@code true} to disconnect when exceptions are thrown.
    */
   public void setDisconnectOnException(boolean disconnectOnException) {
     this.disconnectOnException = disconnectOnException;
   }
 
   /**
    * If an automatic reconnect should be attempted on connection failures.
    * 
    * @return {@code true} if automatic reconnection is enabled.
    */
   public boolean isStayConnected() {
     return this.stayConnected;
   }
 
   /**
    * Set whether to automatically attempt to reconnect to the world model if the
    * connection fails.
    * 
    * @param stayConnected
    *          {@code true} to automatically reconnect.
    */
   public void setStayConnected(boolean stayConnected) {
     this.stayConnected = stayConnected;
   }
 
   /**
    * Returns the currently-configured origin value for this connection.
    * 
    * @return the current origin value.
    */
   public String getOriginString() {
     return this.originString;
   }
 
   /**
    * Sets the origin value for this connection.
    * 
    * @param originString
    *          the new origin value.
    */
   public void setOriginString(String originString) {
     this.originString = originString;
   }
 
   @Override
   public String toString() {
     StringBuffer sb = new StringBuffer("Solver-World Model Interface");
     if (this.host != null) {
       sb.append(" (").append(this.host);
       if (this.port > 0) {
         sb.append(":").append(this.port);
       }
       sb.append(")");
     }
     return sb.toString();
 
   }
 
   /**
    * Expires all Attributes for an Identifier in the world model.
    * 
    * @param identifier
    *          the identifier to expire.
    * @param expirationTime
    *          the expiration timestamp.
    * @return {@code true} if the message is sent successfully, else
    *         {@code false}.
    */
   public boolean expireId(final String identifier, final long expirationTime) {
 
     if (identifier == null) {
       log.error("Unable to expire a null Identifier value.");
       return false;
     }
 
     if (this.originString == null) {
       log.error("Origin has not been set.  Cannot expire URIs without a valid origin.");
       return false;
     }
     ExpireIdentifierMessage message = new ExpireIdentifierMessage();
     message.setOrigin(this.originString);
     message.setId(identifier);
     message.setExpirationTime(expirationTime);
 
     this.session.write(message);
     log.debug("Sent {}", message);
 
     return true;
   }
 
   /**
    * Expire a particular Attribute for an Identifier in the world model.
    * 
    * @param identifier
    *          the Identifier to expire.
    * @param attribute
    *          the Attribute to expire.
    * @param expirationTime
    *          the expiration timestamp.
    * @return {@code true} if the message was sent successfully, else
    *         {@code false}.
    */
   public boolean expireAttribute(final String identifier,
       final String attribute, final long expirationTime) {
     if (identifier == null) {
       log.error("Unable to expire an attribute with a null Identifier value.");
       return false;
     }
 
     if (attribute == null) {
       log.error("Unable to expire a null attribute.");
       return false;
     }
 
     if (this.originString == null) {
       log.error("Origin has not been set.  Cannot expire attributes without a valid origin.");
       return false;
     }
 
     ExpireAttributeMessage message = new ExpireAttributeMessage();
 
     message.setId(identifier);
     message.setAttributeName(attribute);
     message.setExpirationTime(expirationTime);
     message.setOrigin(this.originString);
 
     this.session.write(message);
     log.debug("Sent {}", message);
 
     return true;
   }
 
   /**
    * Deletes an Identifier and all of its Attributes from the world model. All
    * data for the Identifier will be removed permanently. Callers may also wish
    * to consider expiring the Identifier instead.
    * 
    * @param identifier
    *          the Identifier to delete
    * @return {@code true} if the message was sent successfully
    */
   public boolean deleteId(final String identifier) {
     if (identifier == null) {
       log.error("Unable to delete a null Identifier value.");
       return false;
     }
 
     if (this.originString == null) {
       log.error("Origin has not been set.  Cannot delete Identifiers without a valid origin.");
       return false;
     }
 
     DeleteIdentifierMessage message = new DeleteIdentifierMessage();
     message.setOrigin(this.originString);
     message.setId(identifier);
 
     this.session.write(message);
     log.debug("Sent {}", message);
 
     return true;
   }
 
   /**
    * Deletes an Attribute and its entire history from the world model. Callers
    * may wish to expire the Attribute value instead.
    * 
    * @param identifier
    *          the Identifier of the Attribute to expire.
    * @param attribute
    *          the Attribute to expire.
    * @return {@code true} if the message was sent successfully.
    */
   public boolean deleteAttribute(final String identifier, final String attribute) {
     if (identifier == null) {
       log.error("Unable to delete an attribute with a null Identifier value.");
       return false;
     }
 
     if (attribute == null) {
       log.error("Unable to delete a null attribute.");
       return false;
     }
 
     if (this.originString == null) {
       log.error("Origin has not been set.  Cannot delete attributes without a valid origin.");
       return false;
     }
 
     DeleteAttributeMessage message = new DeleteAttributeMessage();
     message.setOrigin(this.originString);
     message.setId(identifier);
     message.setAttributeName(attribute);
 
     this.session.write(message);
     log.debug("Sent {}", message);
 
     return true;
   }
 
   /**
    * Creates a new Identifier in the world model.
    * 
    * @param identifier
    *          the Identifier to create
    * @return {@code true} if the message was sent successfully.
    */
   public boolean createId(final String identifier) {
 
     if (identifier == null) {
       log.error("Unable to create a null Identifier.");
       return false;
     }
 
     if (this.originString == null) {
       log.error("Origin has not been set.  Cannot create Identifiers without a valid origin.");
       return false;
     }
 
     CreateIdentifierMessage message = new CreateIdentifierMessage();
     message.setCreationTime(System.currentTimeMillis());
     message.setOrigin(this.originString);
     message.setId(identifier);
 
     this.session.write(message);
     log.debug("Sent {}", message);
 
     return true;
   }
 
   /**
    * The number of messages that have been written to the session but not yet
    * sent to the network.
    * 
    * @return the number of cached messages.
    */
   public int getCachedWrites() {
     return this.session.getScheduledWriteMessages();
   }
 }
