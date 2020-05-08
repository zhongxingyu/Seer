 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pia;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import org.jivesoftware.smack.Connection;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smackx.packet.DiscoverInfo;
 import org.jivesoftware.smackx.packet.DiscoverInfo.Feature;
 import org.jivesoftware.smackx.pubsub.AccessModel;
 import org.jivesoftware.smackx.pubsub.ConfigureForm;
 import org.jivesoftware.smackx.pubsub.FormType;
 import org.jivesoftware.smackx.pubsub.Item;
 import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
 import org.jivesoftware.smackx.pubsub.LeafNode;
 import org.jivesoftware.smackx.pubsub.PubSubManager;
 import org.jivesoftware.smackx.pubsub.PublishModel;
 import org.jivesoftware.smackx.pubsub.Subscription;
 import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;
 
 /**
  *
  * @author kaisky89
  */
 public class SingletonSmack {
 
     private static SingletonSmack instance = new SingletonSmack();
 
     private SingletonSmack() {
     }
 
     public static SingletonSmack getInstance() {
         return instance;
     }
     
     
     private Connection connection;
    private PubSubManager mgr;
 
     public void connect() throws XMPPException {
         connection = new XMPPConnection(
                 SingletonDataStore.getInstance().getServerAdress());
         connection.connect();
     }
 
     public void login() throws XMPPException {
         UserData user = SingletonDataStore.getInstance().getUser();
         connection.login(user.getUsername(), user.getPassword());
        mgr = new PubSubManager(connection);
     }
 
     public List<String> getAvailableNodes() throws XMPPException {
         try {
             LeafNode node = mgr.getNode("availableNodes");
         } catch (XMPPException ex) {
             // Create the node
             LeafNode leaf = mgr.createNode("availableNodes");
             ConfigureForm form = new ConfigureForm(FormType.submit);
             form.setAccessModel(AccessModel.open);
             form.setDeliverPayloads(false);
             form.setNotifyRetract(true);
             form.setPersistentItems(true);
             form.setPublishModel(PublishModel.open);
             leaf.sendConfigurationForm(form);
         }
         
         LeafNode node = mgr.getNode("availableNodes");
         List list = node.getItems();
         List<String> returnList = new LinkedList<>();
         for (Object object : list) {
             returnList.add(((Item)object).toString());
         }
         return returnList;
     }
     
     public void setOnNodesActualization(ItemEventListener iel) throws XMPPException{
 
       // Get the node
       LeafNode node = mgr.getNode("availableNodes");
       
       node.addItemEventListener(iel);
       cleanSubscriptions(node);
      node.subscribe(SingletonDataStore.getInstance().getJID());
     }
     
     private void cleanSubscriptions(LeafNode node) throws XMPPException {
        node.unsubscribe(SingletonDataStore.getInstance().getJID());
     }
     
     public void addNode(String name) throws XMPPException{
 
       // Create the node
       ConfigureForm form = new ConfigureForm(FormType.submit);
       form.setAccessModel(AccessModel.open);
       form.setDeliverPayloads(true);
       form.setNotifyRetract(true);
       form.setPersistentItems(true);
       form.setPublishModel(PublishModel.open);
       mgr.createNode(name, form);
       
       addItem("availableNodes", name);
     }
     
     public void addItem(String nodeString, String id) throws XMPPException {
 
       // Get the node
       LeafNode leafnode = mgr.getNode(nodeString);
 
       // Publish an Item with the specified id
       Item item;
         item = new Item(id);
       leafnode.send(item);
     }
     
 
     // Tests //////////////////////////////////////////////////////////////////////////////
     public void discoverInfos() throws XMPPException {
 
         // Get the pubsub features that are supported
         DiscoverInfo supportedFeatures = mgr.getSupportedFeatures();
         Iterable<Feature> iterable = new SmartIterable(supportedFeatures.getFeatures());
         for (Feature feature : iterable) {
             System.out.println(feature.toXML());
         }
     }
 
     class SmartIterable<T> implements Iterable {
 
         private Iterator iterator;
 
         public SmartIterable(Iterator iterator) {
             this.iterator = iterator;
         }
 
         @Override
         public Iterator<T> iterator() {
             return iterator;
         }
     }
 }
