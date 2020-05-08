 package org.apache.cxf.transport.xmpp.pubsub;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.cxf.Bus;
 import org.apache.cxf.common.logging.LogUtils;
 import org.apache.cxf.endpoint.Client;
 import org.apache.cxf.endpoint.Server;
 import org.apache.cxf.feature.AbstractFeature;
 import org.apache.cxf.transport.Conduit;
 import org.apache.cxf.transport.Destination;
 import org.apache.cxf.transport.xmpp.common.XMPPConnectionUser;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smackx.pubsub.LeafNode;
 import org.jivesoftware.smackx.pubsub.Node;
 import org.jivesoftware.smackx.pubsub.PubSubManager;
 import org.jivesoftware.smackx.pubsub.Subscription;
 import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;
 
 public class PubSubServiceNameNode extends AbstractFeature {
     
     private static final Logger LOGGER = LogUtils.getLogger(PubSubServiceNameNode.class);
     
     private boolean createIfMissing = true;
     
     public void setCreateIfMissing(boolean create) {
         createIfMissing = create;
     }
    
     @Override
     public void initialize(Bus bus) {
         // Doesn't work on a bus
         LOGGER.log(Level.WARNING, "This feature isn't for a bus");
     }
     
     @Override
     public void initialize(Server server, Bus bus) {
         Destination dest = server.getDestination();
         
         if (dest instanceof XMPPConnectionUser && dest instanceof ItemEventListener<?>) {
             //String serviceName = server.getEndpoint().getBinding().getBindingInfo().getName().toString();
             String serviceName = server.getEndpoint().getService().getName().toString();
             LOGGER.log(Level.INFO, "Node name for server destination: "+serviceName);
             XMPPConnectionUser connUser = (XMPPConnectionUser)dest;
             XMPPConnection connection = connUser.getXmppConnection();
             
             Node pubSubNode = findOrCreateNode(serviceName, connection);
             
             if (pubSubNode != null) {
                 findOrCreateSubscription((ItemEventListener<?>)dest, serviceName, connection, pubSubNode);
             }
         }
         else {
             LOGGER.log(Level.WARNING, "This feature is only for PubSubDestinations");
         }
     }
     
     
     @Override
     public void initialize(Client client, Bus bus) {
         Conduit conduit = client.getConduit();
         
         if (conduit instanceof XMPPConnectionUser && conduit instanceof PubSubClientConduit) {
             //String serviceName = client.getEndpoint().getEndpointInfo().getName().toString();
             String serviceName = client.getEndpoint().getService().getName().toString();
             int index = serviceName.lastIndexOf("Service");
             if (index > -1) {
                 serviceName = serviceName.substring(0, index);
             }
             LOGGER.log(Level.INFO, "Node name for client conduit: "+serviceName);
             XMPPConnectionUser connUser = (XMPPConnectionUser)conduit;
             XMPPConnection connection = connUser.getXmppConnection();
             
             Node pubSubNode = findOrCreateNode(serviceName, connection);
             
             if (pubSubNode instanceof LeafNode) {
                 ((PubSubClientConduit)conduit).setNode((LeafNode)pubSubNode);
             }
             else {
                 LOGGER.log(Level.SEVERE, "Node cannot be used to published items");
             }
         }
         else {
             LOGGER.log(Level.WARNING, "This feature is only for PubSubDestinations");
         }
     }
 
     private void findOrCreateSubscription(ItemEventListener<?> listener, String serviceName, XMPPConnection connection,
                                           Node pubSubNode) {
         try {
             List<Subscription> subscriptions = pubSubNode.getSubscriptions();
             boolean alreadySubscribed = false;
             for(Iterator<Subscription> i = subscriptions.iterator(); i.hasNext() && !alreadySubscribed;) {
                 Subscription sub = i.next();
                 alreadySubscribed = sub.getJid().equals(connection.getUser());
             }
             
            pubSubNode.addItemEventListener(listener);
            
             if (!alreadySubscribed) {
                 try {
                    LOGGER.log(Level.INFO, "Creating subscription for destination");
                     pubSubNode.subscribe(connection.getUser());
                 } catch (XMPPException failedToSub) {
                     LOGGER.log(Level.SEVERE, "JID: "+connection.getUser()+ " to node: "+serviceName);
                 }
             }
            else {
                LOGGER.log(Level.INFO, "Found existing subscription for destination");
            }
             
         } catch (XMPPException failedToGetSubscriptions) {
            LOGGER.log(Level.SEVERE, "Failed to find subscriptions for: "+serviceName);
         }
     }
 
     private Node findOrCreateNode(String serviceName, XMPPConnection connection) {
         PubSubManager mgr = new PubSubManager(connection);
         Node pubSubNode = null;
         try {
             pubSubNode = mgr.getNode(serviceName);
         } catch (XMPPException failedToFindNode) {
             if (createIfMissing) {
                 try {
                     pubSubNode = mgr.createNode(serviceName);
                 } catch (XMPPException failedToCreateNode) {
                     LOGGER.log(Level.SEVERE, "Failed to create node: "+ serviceName, failedToCreateNode);
                 }
             }
             else {
                 LOGGER.log(Level.WARNING, "Not creating node that wasn't found: "+serviceName);
             }
         }
         return pubSubNode;
     }
 
 }
