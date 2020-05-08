 package org.kevoree.library.javase.basicGossiper.channel;
 
 import org.kevoree.Channel;
 import org.kevoree.ContainerRoot;
 import org.kevoree.KevoreeFactory;
 import org.kevoree.annotation.*;
 import org.kevoree.api.service.core.handler.ModelListener;
 import org.kevoree.framework.AbstractChannelFragment;
 import org.kevoree.framework.ChannelFragmentSender;
 import org.kevoree.framework.KevoreeChannelFragment;
 import org.kevoree.framework.NoopChannelFragmentSender;
 import org.kevoree.framework.message.Message;
 import org.kevoree.impl.DefaultKevoreeFactory;
 import org.kevoree.library.basicGossiper.protocol.gossip.Gossip;
 import org.kevoree.library.basicGossiper.protocol.version.Version;
 import org.kevoree.library.javase.basicGossiper.GossiperComponent;
 import org.kevoree.library.javase.basicGossiper.GossiperPeriodic;
 import org.kevoree.library.javase.basicGossiper.GossiperProcess;
 import org.kevoree.library.javase.basicGossiper.Serializer;
 import org.kevoree.log.Log;
 import scala.Tuple2;
 
 import java.net.InetSocketAddress;
 import java.util.*;
 import java.util.concurrent.atomic.AtomicReference;
 
 /**
  * @author Erwan Daubert
  *         TODO add a DictionaryAttribute to define the number of uuids sent by response when a VectorClockUUIDsRequest is sent
  */
 @Library(name = "JavaSE")
@DictionaryType({
        @DictionaryAttribute(name = "interval", defaultValue = "30000", optional = true)
})
 @ChannelType
 public class BasicGossiperChannel extends AbstractChannelFragment implements ModelListener, GossiperComponent {
 
     private DataManagerForChannel dataManager;
     private Serializer serializer;
     private ChannelScorePeerSelector selector;
     private GossiperPeriodic actor;
     private GossiperProcess processValue;
 
     @Start
     public void startGossiperChannel() {
         Long timeoutLong = Long.parseLong((String) this.getDictionary().get("interval"));
         serializer = new ChannelSerializer();
         dataManager = new DataManagerForChannel(this, this.getNodeName()/*, this.getModelService()*/);
         processValue = new GossiperProcess(this, dataManager, serializer, true);
         selector = new ChannelScorePeerSelector(timeoutLong, this.getModelService(), this.getNodeName());
         Log.debug(this.getName() + ": initialize GossiperActor");
         actor = new GossiperPeriodic(this, timeoutLong, selector, processValue);
         processValue.start();
         actor.start();
         this.getModelService().registerModelListener(this);
     }
 
     @Stop
     public void stopGossiperChannel() {
         this.getModelService().unregisterModelListener(this);
         if (actor != null) {
             actor.stop();
             actor = null;
         }
         if (selector != null) {
             selector = null;
         }
         if (processValue != null) {
             processValue.stop();
             processValue = null;
         }
         if (dataManager != null) {
             dataManager = null;
         }
     }
 
     @Update
     public void updateGossiperChannel() {
         // TODO use the garbage of the dataManager
         Map<UUID, Version.VectorClock> vectorClockUUIDs = dataManager.getUUIDVectorClocks();
         Map<UUID, Tuple2<Version.VectorClock, Object>> messages = new HashMap<UUID, Tuple2<Version.VectorClock, Object>>();
         for (UUID uuid : vectorClockUUIDs.keySet()) {
             messages.put(uuid, dataManager.getData(uuid));
         }
 
         stopGossiperChannel();
         startGossiperChannel();
 
         for (UUID uuid : messages.keySet()) {
             dataManager.setData(uuid, messages.get(uuid), "");
         }
     }
 
     @Override
     public Object dispatch(Message msg) {
         //Local delivery
         localNotification(msg);
 
         //CREATE NEW MESSAGE
         long timestamp = System.currentTimeMillis();
         UUID uuid = UUID.randomUUID();
         Tuple2<Version.VectorClock, Object> tuple = new Tuple2<Version.VectorClock, Object>(
                 Version.VectorClock.newBuilder().
                         addEnties(Version.ClockEntry.newBuilder().setNodeID(this.getNodeName())
                                 /*.setTimestamp(timestamp)*/.setVersion(2).build()).setTimestamp(timestamp).build(),
                 msg);
         dataManager.setData(uuid, tuple, "");
 
         notifyPeersInternal(peers.get());
         //SYNCHRONOUS NON IMPLEMENTED
         return null;
     }
 
 
     private void notifyPeersInternal(List<String> l) {
         org.kevoree.library.basicGossiper.protocol.message.KevoreeMessage.Message.Builder messageBuilder = org.kevoree.library.basicGossiper.protocol.message.KevoreeMessage.Message.newBuilder().setDestName(getName()).setDestNodeName(getNodeName());
         messageBuilder.setContentClass(Gossip.UpdatedValueNotification.class.getName()).setContent(Gossip.UpdatedValueNotification.newBuilder().build().toByteString());
         for (String peer : l) {
             if (!peer.equals(getNodeName())) {
                 int port = parsePortNumber(peer);
                 List<String> addresses = getAddresses(peer);
                 if (addresses.size() > 0) {
                     for (String address : addresses) {
                         // TODO do we need to check if the message is correctly sent to avoid useless dissemination with the others addresses ?
                         processValue.netSender().sendMessageUnreliable(messageBuilder.build(), new InetSocketAddress(address, port));
                     }
                 } else {
                     processValue.netSender().sendMessageUnreliable(messageBuilder.build(), new InetSocketAddress("127.0.0.1", port));
                 }
             }
         }
     }
 
     @Override
     public ChannelFragmentSender createSender(String remoteNodeName, String remoteChannelName) {
         return new NoopChannelFragmentSender();
     }
 
     @Override
     public void localNotification(Object o) {
         if (o instanceof Message) {
             for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
                 forward(p, (Message) o);
             }
         }
     }
 
     @Override
     public List<String> getAddresses(String remoteNodeName) {
         return org.kevoree.framework.KevoreePropertyHelper.instance$.getNetworkProperties(getModelService().getLastModel(), remoteNodeName, org.kevoree.framework.Constants.instance$.getKEVOREE_PLATFORM_REMOTE_NODE_IP());
     }
 
     @Override
     public int parsePortNumber(String nodeName) {
         Channel channelOption = currentCacheModel.get().findByPath("hubs[" + getName() + "]", Channel.class);
         int port = 8000;
         if (channelOption != null) {
             String portOption = org.kevoree.framework.KevoreePropertyHelper.instance$.getProperty(channelOption, "port", true, nodeName);
             if (portOption != null) {
                 try {
                     port = Integer.parseInt(portOption);
                 } catch (NumberFormatException e) {
                     Log.warn("Attribute \"port\" of {} is not an Integer, default value ({}) is used.", getName(), port+"");
                 }
             }
         } else {
             Log.warn("There is no channel named {}, default value ({}) is used.", getName(), port+"");
         }
         return port;
     }
 
     @Override
     public boolean preUpdate(ContainerRoot currentModel, ContainerRoot proposedModel) {
         return true;
     }
 
     @Override
     public boolean initUpdate(ContainerRoot currentModel, ContainerRoot proposedModel) {
         return true;
     }
 
     private KevoreeFactory factory = new DefaultKevoreeFactory();
     protected AtomicReference<ContainerRoot> currentCacheModel = new AtomicReference<ContainerRoot>(factory.createContainerRoot());
     protected AtomicReference<List<String>> peers = new AtomicReference<List<String>>();
 
     @Override
     public boolean afterLocalUpdate(ContainerRoot currentModel, ContainerRoot proposedModel) {
         currentCacheModel.set(proposedModel);
         List<String> LoopPeers = new ArrayList<String>();
         for (KevoreeChannelFragment fragment : getOtherFragments()) {
             LoopPeers.add(fragment.getNodeName());
         }
         peers.set(LoopPeers);
         return true;
     }
 
     @Override
     public void modelUpdated() {
     }
 
     @Override
     public void preRollback(ContainerRoot currentModel, ContainerRoot proposedModel) {
     }
 
     @Override
     public void postRollback(ContainerRoot currentModel, ContainerRoot proposedModel) {
     }
 }
