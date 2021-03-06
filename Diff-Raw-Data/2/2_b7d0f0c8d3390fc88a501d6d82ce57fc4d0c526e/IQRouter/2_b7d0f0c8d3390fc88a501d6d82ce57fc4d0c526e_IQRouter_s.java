 /**
  * $RCSfile: IQRouter.java,v $
  * $Revision: 3135 $
  * $Date: 2005-12-01 02:03:04 -0300 (Thu, 01 Dec 2005) $
  *
  * Copyright (C) 2007 Jive Software. All rights reserved.
  *
  * This software is published under the terms of the GNU Public License (GPL),
  * a copy of which is included in this distribution.
  */
 
 package org.jivesoftware.openfire;
 
 import org.dom4j.Element;
 import org.jivesoftware.openfire.container.BasicModule;
 import org.jivesoftware.openfire.handler.IQHandler;
 import org.jivesoftware.openfire.interceptor.InterceptorManager;
 import org.jivesoftware.openfire.interceptor.PacketRejectedException;
 import org.jivesoftware.openfire.privacy.PrivacyList;
 import org.jivesoftware.openfire.privacy.PrivacyListManager;
 import org.jivesoftware.openfire.session.ClientSession;
 import org.jivesoftware.openfire.session.Session;
 import org.jivesoftware.openfire.user.UserManager;
 import org.jivesoftware.util.LocaleUtils;
 import org.jivesoftware.util.Log;
 import org.xmpp.packet.*;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 /**
  * Routes iq packets throughout the server. Routing is based on the recipient
  * and sender addresses. The typical packet will often be routed twice, once
  * from the sender to some internal server component for handling or processing,
  * and then back to the router to be delivered to it's final destination.
  *
  * @author Iain Shigeoka
  */
 public class IQRouter extends BasicModule {
 
     private RoutingTable routingTable;
     private MulticastRouter multicastRouter;
     private String serverName;
     private List<IQHandler> iqHandlers = new ArrayList<IQHandler>();
     private Map<String, IQHandler> namespace2Handlers = new ConcurrentHashMap<String, IQHandler>();
     private Map<String, IQResultListener> resultListeners =
             new ConcurrentHashMap<String, IQResultListener>();
     private SessionManager sessionManager;
     private UserManager userManager;
 
     /**
      * Creates a packet router.
      */
     public IQRouter() {
         super("XMPP IQ Router");
     }
 
     /**
      * <p>Performs the actual packet routing.</p>
      * <p>You routing is considered 'quick' and implementations may not take
      * excessive amounts of time to complete the routing. If routing will take
      * a long amount of time, the actual routing should be done in another thread
      * so this method returns quickly.</p>
      * <h2>Warning</h2>
      * <p>Be careful to enforce concurrency DbC of concurrent by synchronizing
      * any accesses to class resources.</p>
      *
      * @param packet The packet to route
      * @throws NullPointerException If the packet is null
      */
     public void route(IQ packet) {
         if (packet == null) {
             throw new NullPointerException();
         }
         JID sender = packet.getFrom();
         ClientSession session = sessionManager.getSession(sender);
         try {
             // Invoke the interceptors before we process the read packet
             InterceptorManager.getInstance().invokeInterceptors(packet, session, true, false);
             JID to = packet.getTo();
             if (session != null && to != null && session.getStatus() == Session.STATUS_CONNECTED &&
                     !serverName.equals(to.toString())) {
                 // User is requesting this server to authenticate for another server. Return
                 // a bad-request error
                 IQ reply = IQ.createResultIQ(packet);
                 reply.setChildElement(packet.getChildElement().createCopy());
                 reply.setError(PacketError.Condition.bad_request);
                 session.process(reply);
                 Log.warn("User tried to authenticate with this server using an unknown receipient: " +
                         packet);
             }
             else if (session == null || session.getStatus() == Session.STATUS_AUTHENTICATED || (
                     isLocalServer(to) && (
                             "jabber:iq:auth".equals(packet.getChildElement().getNamespaceURI()) ||
                                     "jabber:iq:register"
                                             .equals(packet.getChildElement().getNamespaceURI()) ||
                                     "urn:ietf:params:xml:ns:xmpp-bind"
                                             .equals(packet.getChildElement().getNamespaceURI())))) {
                 handle(packet);
             }
             else {
                 IQ reply = IQ.createResultIQ(packet);
                 reply.setChildElement(packet.getChildElement().createCopy());
                 reply.setError(PacketError.Condition.not_authorized);
                 session.process(reply);
             }
             // Invoke the interceptors after we have processed the read packet
             InterceptorManager.getInstance().invokeInterceptors(packet, session, true, true);
         }
         catch (PacketRejectedException e) {
             if (session != null) {
                 // An interceptor rejected this packet so answer a not_allowed error
                 IQ reply = new IQ();
                 reply.setChildElement(packet.getChildElement().createCopy());
                 reply.setID(packet.getID());
                 reply.setTo(session.getAddress());
                 reply.setFrom(packet.getTo());
                 reply.setError(PacketError.Condition.not_allowed);
                 session.process(reply);
                 // Check if a message notifying the rejection should be sent
                 if (e.getRejectionMessage() != null && e.getRejectionMessage().trim().length() > 0) {
                     // A message for the rejection will be sent to the sender of the rejected packet
                     Message notification = new Message();
                     notification.setTo(session.getAddress());
                     notification.setFrom(packet.getTo());
                     notification.setBody(e.getRejectionMessage());
                     session.process(notification);
                 }
             }
         }
     }
 
     /**
      * <p>Adds a new IQHandler to the list of registered handler. The new IQHandler will be
      * responsible for handling IQ packet whose namespace matches the namespace of the
      * IQHandler.</p>
      *
      * An IllegalArgumentException may be thrown if the IQHandler to register was already provided
      * by the server. The server provides a certain list of IQHandlers when the server is
      * started up.
      *
      * @param handler the IQHandler to add to the list of registered handler.
      */
     public void addHandler(IQHandler handler) {
         if (iqHandlers.contains(handler)) {
             throw new IllegalArgumentException("IQHandler already provided by the server");
         }
         // Ask the handler to be initialized
         handler.initialize(XMPPServer.getInstance());
         // Register the handler as the handler of the namespace
         namespace2Handlers.put(handler.getInfo().getNamespace(), handler);
     }
 
     /**
      * <p>Removes an IQHandler from the list of registered handler. The IQHandler to remove was
      * responsible for handling IQ packet whose namespace matches the namespace of the
      * IQHandler.</p>
      *
      * An IllegalArgumentException may be thrown if the IQHandler to remove was already provided
      * by the server. The server provides a certain list of IQHandlers when the server is
      * started up.
      *
      * @param handler the IQHandler to remove from the list of registered handler.
      */
     public void removeHandler(IQHandler handler) {
         if (iqHandlers.contains(handler)) {
             throw new IllegalArgumentException("Cannot remove an IQHandler provided by the server");
         }
         // Unregister the handler as the handler of the namespace
         namespace2Handlers.remove(handler.getInfo().getNamespace());
     }
 
     /**
      * Adds an {@link IQResultListener} that will be invoked when an IQ result is sent to the
      * server itself and is of type result or error. This is a nice way for the server to
      * send IQ packets to other XMPP entities and be waked up when a response is received back.<p>
      *
      * Once an IQ result was received, the listener will be invoked and removed from
      * the list of listeners.
      *
      * @param id the id of the IQ packet being sent from the server to an XMPP entity.
      * @param listener the IQResultListener that will be invoked when an answer is received
      */
     public void addIQResultListener(String id, IQResultListener listener) {
         // TODO Add a check that if no IQ reply was received for a while then an IQ error should
         // be generated by the server and simulate like the client sent it. This will let listeners
         // react and be removed from the collection
         resultListeners.put(id, listener);
     }
 
     public void initialize(XMPPServer server) {
         super.initialize(server);
         serverName = server.getServerInfo().getName();
         routingTable = server.getRoutingTable();
         multicastRouter = server.getMulticastRouter();
         iqHandlers.addAll(server.getIQHandlers());
         sessionManager = server.getSessionManager();
         userManager = server.getUserManager();
     }
 
     /**
      * A JID is considered local if:
      * 1) is null or
      * 2) has no domain or domain is empty or
      * 3) has no resource or resource is empty
      */
     private boolean isLocalServer(JID recipientJID) {
         return recipientJID == null || recipientJID.getDomain() == null
                 || "".equals(recipientJID.getDomain()) || recipientJID.getResource() == null
                 || "".equals(recipientJID.getResource());
     }
 
     private void handle(IQ packet) {
         JID recipientJID = packet.getTo();
         // Check if the packet was sent to the server hostname
         if (recipientJID != null && recipientJID.getNode() == null &&
                 recipientJID.getResource() == null && serverName.equals(recipientJID.getDomain())) {
             Element childElement = packet.getChildElement();
             if (childElement != null && childElement.element("addresses") != null) {
                 // Packet includes multicast processing instructions. Ask the multicastRouter
                 // to route this packet
                 multicastRouter.route(packet);
                 return;
             }
             else if (IQ.Type.result == packet.getType() || IQ.Type.error == packet.getType()) {
                 // The server got an answer to an IQ packet that was sent from the server
                 IQResultListener iqResultListener = resultListeners.remove(packet.getID());
                 if (iqResultListener != null) {
                     try {
                         iqResultListener.receivedAnswer(packet);
                     }
                     catch (Exception e) {
                         Log.error("Error processing answer of remote entity", e);
                     }
                     return;
                 }
             }
         }
         try {
             // Check for registered components, services or remote servers
             if (recipientJID != null) {
                 if (routingTable.hasComponentRoute(recipientJID) || routingTable.hasServerRoute(recipientJID)) {
                     // A component/service/remote server was found that can handle the Packet
                     routingTable.routePacket(recipientJID, packet);
                     return;
                 }
             }
             if (isLocalServer(recipientJID)) {
                 // Let the server handle the Packet
                 Element childElement = packet.getChildElement();
                 String namespace = null;
                 if (childElement != null) {
                     namespace = childElement.getNamespaceURI();
                 }
                 if (namespace == null) {
                    if (packet.getType() != IQ.Type.result) {
                         // Do nothing. We can't handle queries outside of a valid namespace
                         Log.warn("Unknown packet " + packet);
                     }
                 }
                 else {
                     // Check if communication to local users is allowed
                     if (recipientJID != null && userManager.isRegisteredUser(recipientJID.getNode())) {
                         PrivacyList list =
                                 PrivacyListManager.getInstance().getDefaultPrivacyList(recipientJID.getNode());
                         if (list != null && list.shouldBlockPacket(packet)) {
                             // Communication is blocked
                             if (IQ.Type.set == packet.getType() || IQ.Type.get == packet.getType()) {
                                 // Answer that the service is unavailable
                                 sendErrorPacket(packet, PacketError.Condition.service_unavailable);
                             }
                             return;
                         }
                     }
                     IQHandler handler = getHandler(namespace);
                     if (handler == null) {
                         if (recipientJID == null) {
                             // Answer an error since the server can't handle the requested namespace
                             sendErrorPacket(packet, PacketError.Condition.service_unavailable);
                         }
                         else if (recipientJID.getNode() == null ||
                                 "".equals(recipientJID.getNode())) {
                             // Answer an error if JID is of the form <domain>
                             sendErrorPacket(packet, PacketError.Condition.feature_not_implemented);
                         }
                         else {
                             // JID is of the form <node@domain>
                             // Answer an error since the server can't handle packets sent to a node
                             sendErrorPacket(packet, PacketError.Condition.service_unavailable);
                         }
                     }
                     else {
                         handler.process(packet);
                     }
                 }
             }
             else {
                 // JID is of the form <node@domain/resource>
                 routingTable.routePacket(recipientJID, packet);
             }
         }
         catch (Exception e) {
             Log.error(LocaleUtils.getLocalizedString("admin.error.routing"), e);
             Session session = sessionManager.getSession(packet.getFrom());
             if (session != null) {
                 session.close();
             }
         }
     }
 
     private void sendErrorPacket(IQ originalPacket, PacketError.Condition condition) {
         if (IQ.Type.error == originalPacket.getType()) {
             Log.error("Cannot reply an IQ error to another IQ error: " + originalPacket);
             return;
         }
         IQ reply = IQ.createResultIQ(originalPacket);
         reply.setChildElement(originalPacket.getChildElement().createCopy());
         reply.setError(condition);
         // Check if the server was the sender of the IQ
         if (serverName.equals(originalPacket.getFrom().toString())) {
             // Just let the IQ router process the IQ error reply
             handle(reply);
             return;
         }
         // Route the error packet to the original sender of the IQ.
         routingTable.routePacket(reply.getTo(), reply);
     }
 
     private IQHandler getHandler(String namespace) {
         IQHandler handler = namespace2Handlers.get(namespace);
         if (handler == null) {
             for (IQHandler handlerCandidate : iqHandlers) {
                 IQHandlerInfo handlerInfo = handlerCandidate.getInfo();
                 if (handlerInfo != null && namespace.equalsIgnoreCase(handlerInfo.getNamespace())) {
                     handler = handlerCandidate;
                     namespace2Handlers.put(namespace, handler);
                     break;
                 }
             }
         }
         return handler;
     }
 
     /**
      * Notification message indicating that a packet has failed to be routed to the receipient.
      *
      * @param receipient address of the entity that failed to receive the packet.
      * @param packet IQ packet that failed to be sent to the receipient.
      */
     public void routingFailed(JID receipient, Packet packet) {
         IQ iq = (IQ) packet;
         // If a route to the target address was not found then try to answer a
         // service_unavailable error code to the sender of the IQ packet
         if (IQ.Type.result != iq.getType() && IQ.Type.error != iq.getType()) {
             Log.info("Packet sent to unreachable address " + packet);
             sendErrorPacket(iq, PacketError.Condition.service_unavailable);
         }
         else {
             Log.warn("Error or result packet could not be delivered " + packet);
         }
     }
 }
