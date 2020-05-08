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
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.owlplatform.worldmodel.Attribute;
 import com.owlplatform.worldmodel.client.listeners.ConnectionListener;
 import com.owlplatform.worldmodel.client.listeners.DataListener;
 import com.owlplatform.worldmodel.client.protocol.messages.AbstractRequestMessage;
 import com.owlplatform.worldmodel.client.protocol.messages.AttributeAliasMessage;
 import com.owlplatform.worldmodel.client.protocol.messages.DataResponseMessage;
 import com.owlplatform.worldmodel.client.protocol.messages.IdSearchResponseMessage;
 import com.owlplatform.worldmodel.client.protocol.messages.OriginAliasMessage;
 import com.owlplatform.worldmodel.client.protocol.messages.OriginPreferenceMessage;
 import com.owlplatform.worldmodel.client.protocol.messages.RangeRequestMessage;
 import com.owlplatform.worldmodel.client.protocol.messages.SnapshotRequestMessage;
 import com.owlplatform.worldmodel.client.protocol.messages.StreamRequestMessage;
 
 /**
  * A simple class for clients or solvers that need to request data from the
  * World Model.
  * 
  * @author Robert Moore
  * 
  */
 public class ClientWorldConnection {
 
   /**
    * Simple nested class to hide the listener methods for the World Model
    * interface.
    * 
    * @author Robert Moore
    * 
    */
   private static final class Handler implements ConnectionListener,
       DataListener {
 
     /**
      * The actual handler of events.
      */
     private final ClientWorldConnection client;
 
     /**
      * Creates a new Handler object to handle events for the client.
      * 
      * @param client
      *          the actual processor of events.
      */
     Handler(final ClientWorldConnection client) {
       this.client = client;
     }
 
     @Override
     public void requestCompleted(ClientWorldModelInterface worldModel,
         AbstractRequestMessage message) {
       this.client.requestCompleted(worldModel, message);
 
     }
 
     @Override
     public void dataResponseReceived(ClientWorldModelInterface worldModel,
         DataResponseMessage message) {
       this.client.dataResponseReceived(worldModel, message);
     }
 
     @Override
     public void idSearchResponseReceived(ClientWorldModelInterface worldModel,
         IdSearchResponseMessage message) {
       this.client.idSearchResponseReceived(worldModel, message);
     }
 
     @Override
     public void attributeAliasesReceived(ClientWorldModelInterface worldModel,
         AttributeAliasMessage message) {
       this.client.attributeAliasesReceived(worldModel, message);
     }
 
     @Override
     public void originAliasesReceived(ClientWorldModelInterface worldModel,
         OriginAliasMessage message) {
       this.client.originAliasesReceived(worldModel, message);
     }
 
     @Override
     public void originPreferenceSent(ClientWorldModelInterface worldModel,
         OriginPreferenceMessage message) {
       this.client.originPreferenceSent(worldModel, message);
     }
 
     @Override
     public void connectionInterrupted(ClientWorldModelInterface worldModel) {
       this.client.connectionInterrupted(worldModel);
     }
 
     @Override
     public void connectionEnded(ClientWorldModelInterface worldModel) {
       this.client.connectionEnded(worldModel);
     }
 
     @Override
     public void connectionEstablished(ClientWorldModelInterface worldModel) {
       this.client.connectionEstablished(worldModel);
     }
 
   }
 
   /**
    * Logging utility for this class.
    */
   private static final Logger log = LoggerFactory
       .getLogger(ClientWorldConnection.class);
 
   /**
    * A map of outstanding snapshot request responses. Used to set the World
    * State when a snapshot request completes. The key is the request ticket
    * number.
    */
   private final Map<Long, Response> outstandingSnapshots = new ConcurrentHashMap<Long, Response>();
 
   /**
    * A map of outstanding range and streaming request responses. Used to update
    * the World State when a range or streaming request message receives updated
    * values. The key is the request ticket number.
    */
   private final Map<Long, StepResponse> outstandingSteps = new ConcurrentHashMap<Long, StepResponse>();
 
   /**
    * A map of WorldState objects for Responses returned by snapshot requests.
    * The key is the request ticket number.
    */
   private final Map<Long, WorldState> outstandingStates = new ConcurrentHashMap<Long, WorldState>();
 
   /**
    * The interface to the world model.
    */
   private ClientWorldModelInterface wmi = new ClientWorldModelInterface();
 
   /**
    * Flag to indicate whether the connection is established or not.
    */
   private volatile boolean isConnected = false;
 
   /**
    * The private handler for events.
    */
   private final Handler handler = new Handler(this);
 
   /**
    * A queue of search responses to store for the client.
    */
   private final LinkedBlockingQueue<String[]> uriSearchResponses = new LinkedBlockingQueue<String[]>();
 
   /**
    * Whether or not the world model is connected.
    * 
    * @return {@code true} if the world model is connected, else {@code false}.
    */
   public boolean isConnected() {
     return this.isConnected;
   }
 
   /**
    * Creates a new Client world model connection with the following settings:
    * <ul>
    * <li>Disconnect on exceptions: {@code true}</li>
    * <li>Automatically reconnect: {@code true}</li>
    * <li>Connection retry delay: 1 second</li>
    * <li>Connection timeout: 1 second</li>
    * </ul>
    */
   public ClientWorldConnection() {
     super();
     this.wmi.setStayConnected(true);
     this.wmi.setConnectionRetryDelay(1000l);
     this.wmi.setDisconnectOnException(true);
     this.wmi.setConnectionTimeout(1000l);
     this.wmi.addConnectionListener(this.handler);
     this.wmi.addDataListener(this.handler);
   }
 
   /**
    * Connects to the world model at the configured host and port.
    * 
    * @param timeout
    *          the maximum time to attempt the connection or 0 for the configured timeout
    * 
    * @return {@code true} if the connection succeeds, else {@code false}.
    */
   public boolean connect(long timeout) {
     if (this.wmi.connect(timeout)) {
       this.wmi.setStayConnected(true);
       return true;
     }
     return false;
   }
 
   /**
    * Permanently disconnects from the world model.  
    */
   public void disconnect() {
     this.wmi.disconnect();
   }
 
   /**
    * Sets the world model host.
    * 
    * @param host
    *          the world model host.
    */
   public void setHost(final String host) {
     this.wmi.setHost(host);
   }
 
   /**
    * Sets the world model port.
    * 
    * @param port
    *          the world model port.
    */
   public void setPort(final int port) {
     this.wmi.setPort(port);
   }
 
   @Override
   public String toString() {
     return "World Model (C) @ " + this.wmi.getHost() + ":" + this.wmi.getPort();
   }
 
   /**
    * Sends a snapshot request to the world model for the specified Identifier
    * regular expression and Attribute regular expressions, between the start and
    * end timestamps.
    * 
    * @param idRegex
    *          regular expression for matching the identifier.
    * @param start
    *          the begin time for the snapshot.
    * @param end
    *          the ending time for the snapshot.
    * @param attributes
    *          the attribute regular expressions to request
    * @return a {@code Response} for the request.
    */
   public Response getSnapshot(final String idRegex, final long start,
       final long end, String... attributes) {
     SnapshotRequestMessage req = new SnapshotRequestMessage();
     req.setIdRegex(idRegex);
     req.setBeginTimestamp(start);
     req.setEndTimestamp(end);
     if (attributes != null) {
       req.setAttributeRegexes(attributes);
     }
     Response resp = new Response(this, 0);
     try {
       while (!this.isConnected) {
         log.debug("Trying to wait until connection is ready.");
         synchronized (this) {
           try {
             this.wait();
           } catch (InterruptedException ie) {
             // Ignored
           }
         }
       }
       long reqId = this.wmi.sendMessage(req);
       resp.setTicketNumber(reqId);
       this.outstandingSnapshots.put(Long.valueOf(reqId), resp);
       WorldState ws = new WorldState();
       this.outstandingStates.put(Long.valueOf(reqId), ws);
 
       return resp;
     } catch (Exception e) {
      log.error("Unable to send " + req + ".",e);
       resp.setError(e);
       return resp;
     }
   }
 
   /**
    * Sends a snapshot request to the world model for the current value of the
    * specified Identifier regular expression and Attribute regular expressions.
    * 
    * @param idRegex
    *          the regular expression to match the identifiers.
    * @param attributes
    *          regular expressions to match attributes.
    * @return a {@code Response} for the request.
    */
   public Response getCurrentSnapshot(final String idRegex, String... attributes) {
     return this.getSnapshot(idRegex, 0l, 0l, attributes);
   }
 
   /**
    * Sends a range request to the world model for the specified Identifier
    * regular expression, Attribute regular expressions, between the start and
    * end times.
    * 
    * @param idRegex
    *          regular expression for matching the identifier.
    * @param start
    *          the beginning of the range..
    * @param end
    *          the end of the range.
    * @param attributes
    *          the attribute regular expressions to request
    * @return a {@code StepResponse} for the request.
    */
   public StepResponse getRangeRequest(final String idRegex, final long start,
       final long end, String... attributes) {
     RangeRequestMessage req = new RangeRequestMessage();
     req.setIdRegex(idRegex);
     req.setBeginTimestamp(start);
     req.setEndTimestamp(end);
     if (attributes != null) {
       req.setAttributeRegexes(attributes);
     }
     StepResponse resp = new StepResponse(this, 0);
     try {
       while (!this.isConnected) {
         log.debug("Trying to wait until connection is ready.");
         synchronized (this) {
           try {
             this.wait();
           } catch (InterruptedException ie) {
             // Ignored
           }
         }
       }
       long reqId = this.wmi.sendMessage(req);
       resp.setTicketNumber(reqId);
       this.outstandingSteps.put(Long.valueOf(reqId), resp);
 
       return resp;
     } catch (Exception e) {
       resp.setError(e);
       return resp;
     }
   }
 
   /**
    * Sends a stream request to the world model for the specified identifier and
    * attribute regular expressions, beginning with data at time {@code start},
    * and updating no more frequently than every {@code interval} milliseconds.
    * 
    * @param idRegex
    *          the regular expression for matching identifiers
    * @param start
    *          the earliest data to stream.
    * @param interval
    *          the minimum time between attribute value updates.
    * @param attributes
    *          the attribute regular expressions to match.
    * @return a {@code StepResponse} for the request.
    */
   public StepResponse getStreamRequest(final String idRegex, final long start,
       final long interval, String... attributes) {
     StreamRequestMessage req = new StreamRequestMessage();
     req.setIdRegex(idRegex);
     req.setBeginTimestamp(start);
     req.setUpdateInterval(interval);
     if (attributes != null) {
       req.setAttributeRegexes(attributes);
     }
 
     StepResponse resp = new StepResponse(this, 0);
     try {
       while (!this.isConnected) {
         log.debug("Trying to wait until connection is ready.");
         synchronized (this) {
           try {
             this.wait();
           } catch (InterruptedException ie) {
             // Ignored
           }
         }
       }
       long reqId = this.wmi.sendMessage(req);
       resp.setTicketNumber(reqId);
       this.outstandingSteps.put(Long.valueOf(reqId), resp);
 
       return resp;
     } catch (Exception e) {
       resp.setError(e);
       return resp;
     }
   }
 
   /**
    * Searches for any Identifier values that match the provided regular
    * expression.
    * 
    * @param idRegex
    *          a regular expression to match against Identifiers in the world
    *          model.
    * @return all matching Identifiers.
    */
   public String[] searchId(final String idRegex) {
     synchronized (this.uriSearchResponses) {
       if (!this.wmi.searchIdRegex(idRegex)) {
         log.warn("Attempted to search for a null URI regex. Not sending.");
         return new String[] {};
       }
       while (this.uriSearchResponses.isEmpty()) {
         log.debug("Waiting for response.");
         try {
           return this.uriSearchResponses.take();
         } catch (InterruptedException ie) {
           // Ignored
         }
       }
       log.error("Unable to retrieve matching URI values for {}.", idRegex);
       return new String[] {};
     }
 
   }
 
   /**
    * Cancels a request based on the ticket number.
    * 
    * @param ticketNumber
    *          the ticket number of the request to cancel.
    */
   void cancelRequest(final long ticketNumber) {
     // Send a cancel request
     this.wmi.cancelRequest(ticketNumber);
   }
 
   /**
    * Completes any outstanding requests with errors.
    * 
    * @param worldModel
    *          the source of the connection interruption.
    */
   void connectionInterrupted(ClientWorldModelInterface worldModel) {
     this.isConnected = false;
     for (Iterator<Long> iter = this.outstandingSnapshots.keySet().iterator(); iter
         .hasNext();) {
       Long tix = iter.next();
       Response resp = this.outstandingSnapshots.get(tix);
       resp.setError(new RuntimeException("Connection to "
           + worldModel.toString() + " was closed."));
       iter.remove();
     }
 
     this.outstandingStates.clear();
 
     for (Iterator<Long> iter = this.outstandingSteps.keySet().iterator(); iter
         .hasNext();) {
       Long tix = iter.next();
       StepResponse resp = this.outstandingSteps.get(tix);
       resp.setError(new RuntimeException("Connection to "
           + worldModel.toString() + " was closed."));
       iter.remove();
     }
   }
 
   /**
    * Calls {@code connectionInterrupted(ClientWorldModelInterface)} to finish
    * any outstanding requests.
    * 
    * @param worldModel
    *          the source of the connection.
    */
   void connectionEnded(ClientWorldModelInterface worldModel) {
     this.connectionInterrupted(worldModel);
   }
 
   /**
    * Notifies any threads blocking on this, marks {@code isConnected} as
    * {@code true}.
    * 
    * @param worldModel
    *          the source of the connection.
    */
   void connectionEstablished(ClientWorldModelInterface worldModel) {
     this.isConnected = true;
     synchronized (this) {
       this.notifyAll();
     }
   }
 
   /**
    * Marks the appropriate {@code Response} or {@code StepResponse} as
    * completed.
    * 
    * @param worldModel
    *          the source of the message.
    * @param message
    *          the completed message.
    */
   void requestCompleted(ClientWorldModelInterface worldModel,
       AbstractRequestMessage message) {
     Long ticket = Long.valueOf(message.getTicketNumber());
     log.debug("Request {} completed.", ticket);
 
     Response resp = this.outstandingSnapshots.remove(ticket);
     // Snapshot request
     if (resp != null) {
       WorldState ws = this.outstandingStates.get(ticket);
       if (ws == null) {
         log.error("Unknown ticket number {} for request.", ticket, message);
         return;
       }
       resp.setState(ws);
       return;
     }
 
     StepResponse sResp = this.outstandingSteps.remove(ticket);
     // Range/Streaming request
     if (sResp != null) {
       sResp.setComplete();
       return;
     }
 
     log.error("Couldn't find response for ticket {}.", ticket);
   }
 
   /**
    * Stores the received response into the appropriate {@code Response} or
    * {@code StepResponse}
    * 
    * @param worldModel
    *          the source of the message.
    * @param message
    *          the received data response message.
    */
   void dataResponseReceived(ClientWorldModelInterface worldModel,
       DataResponseMessage message) {
     // Check for snapshot request
     WorldState ws = null;
     ws = this.outstandingStates.get(Long.valueOf(message.getTicketNumber()));
     if (ws != null) {
       log.debug("Updating data for ticket {}:\n{}",
           Long.valueOf(message.getTicketNumber()), message);
       List<Attribute> attribList = new ArrayList<Attribute>();
       for (Attribute a : message.getAttributes()) {
         attribList.add(a);
       }
       ws.addState(message.getId(), attribList);
       return;
     }
 
     StepResponse resp = this.outstandingSteps.get(Long.valueOf(message
         .getTicketNumber()));
     if (resp == null) {
       log.error("Unknown request ticket number {}",
           Long.valueOf(message.getTicketNumber()));
       return;
     }
     log.debug("Updating data for ticket {}:\n{}",
         Long.valueOf(message.getTicketNumber()), message);
     ws = new WorldState();
     List<Attribute> attribList = new ArrayList<Attribute>();
     if (message.getAttributes() != null) {
       for (Attribute a : message.getAttributes()) {
         attribList.add(a);
       }
     }
     ws.addState(message.getId(), attribList);
     resp.addState(ws);
     return;
   }
 
   /**
    * Stores the matching Identifiers so {@link #searchId(String)} returns.
    * 
    * @param worldModel
    * @param message
    */
   void idSearchResponseReceived(ClientWorldModelInterface worldModel,
       IdSearchResponseMessage message) {
     log.debug("Got a URI search response: {}", message);
     String[] matching = message.getMatchingIds();
     if (matching == null) {
       this.uriSearchResponses.add(new String[] {});
     } else {
       this.uriSearchResponses.add(matching);
     }
   }
 
   /**
    * Does nothing right now.
    * 
    * @param worldModel
    * @param message
    */
   void attributeAliasesReceived(ClientWorldModelInterface worldModel,
       AttributeAliasMessage message) {
     // Nothing to do
   }
 
   /**
    * Does nothing right now.
    * 
    * @param worldModel
    * @param message
    */
   void originAliasesReceived(ClientWorldModelInterface worldModel,
       OriginAliasMessage message) {
     // Nothing to do
 
   }
 
   /**
    * Does nothing for now
    * 
    * @param worldModel
    * @param message
    */
   void originPreferenceSent(ClientWorldModelInterface worldModel,
       OriginPreferenceMessage message) {
     // Nothing to do
   }
 }
