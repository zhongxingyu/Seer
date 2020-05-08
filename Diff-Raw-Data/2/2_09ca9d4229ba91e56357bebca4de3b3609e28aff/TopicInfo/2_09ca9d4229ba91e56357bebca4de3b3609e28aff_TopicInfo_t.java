 package co.gridport.server.kafka;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.I0Itec.zkclient.IZkChildListener;
 import org.codehaus.jackson.annotate.JsonPropertyOrder;
 
 
 @JsonPropertyOrder({"name","activeConsumers","inactiveConsumers","partitions"})
 public class TopicInfo {
 
     private ClusterInfo cluster;
 
     private String name;
 
     private Map<String,PartitionInfo> partitions;
 
     private Map<String,Boolean> consumers;
 
     public TopicInfo(ClusterInfo cluster, String topicName) {
         this.cluster = cluster;
         this.name = topicName;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public List<String> getActiveConsumers() {
         List<String> result = new ArrayList<String>();
         getConsumers();
         for(Entry<String,Boolean> entry: consumers.entrySet()) {
             if (entry.getValue()!=null && entry.getValue()) result.add(entry.getKey());
         }
         return result;
     }
 
    public List<String> getInactiveConsumers() {
         List<String> result = new ArrayList<String>();
         getConsumers();
         for(Entry<String,Boolean> entry: consumers.entrySet()) {
             if (entry.getValue()!=null && !entry.getValue()) result.add(entry.getKey());
         }
         return result;
     }
 
 
     public Map<String,PartitionInfo> getPartitions() {
         if (partitions == null) {
             partitions = new HashMap<String,PartitionInfo>();
             try {
                 setPartitions(cluster.zk.getChildren("/brokers/topics/"+name));
                 //TODO set up watch /brokers/topics/name
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         synchronized(partitions) {
             return Collections.unmodifiableMap(partitions);
         }
     }
 
     private void setPartitions(List<String> brokerIds) {
         Map<String,BrokerInfo> brokers = cluster.getBrokers();
         Map<String,PartitionInfo> newPartitionList = new HashMap<String,PartitionInfo>();
         for(String brokerId: brokerIds) {
             Integer brokerPartitionCount = Integer.valueOf(cluster.zk.readData("/brokers/topics/"+name+"/"+brokerId).toString());
             for(int partition=0; partition<brokerPartitionCount; partition++) {
                 BrokerInfo broker = brokers.get(brokerId);
                 String pathId = brokerId+"-"+partition;
                 newPartitionList.put(pathId, new PartitionInfo(this,broker,partition,pathId));
             }
         }
         synchronized(partitions) {
             for(String id: newPartitionList.keySet()) {
                 if (!partitions.containsKey(id)) {
                     partitions.put(id, newPartitionList.get(id));
                 }
             }
             for(String id: partitions.keySet()) {
                 if (!newPartitionList.containsKey(id)) {
                     partitions.remove(id);
                 }
             }
         }
     }
 
     private Map<String,Boolean> getConsumers() {
         synchronized(this) {
             if (consumers == null){
                 consumers = new HashMap<String,Boolean>();
             }
             for(Entry<String,ConsumerInfo> entry: cluster.getConsumers().entrySet()) {
                 final String groupId = entry.getKey();
                 if (!consumers.containsKey(groupId)) {
                     //check active
                     if (!cluster.zk.exists("/consumers/"+groupId+"/owners")) {
                         flagConsumer(groupId, null);
                         cluster.zk.subscribeChildChanges("/consumers/"+groupId, new IZkChildListener() {
                             @Override
                             public void handleChildChange(String parentPath, List<String> change) throws Exception {
                                 if (change.contains("owners")) {
                                     consumers.remove(groupId);
                                 }
                             }
                         });
                     } else if (cluster.zk.getChildren("/consumers/"+groupId+"/owners").contains(name)) {
                         flagConsumer(groupId, true);
                         cluster.zk.subscribeChildChanges("/consumers/"+groupId+"/owners", new IZkChildListener() {
                             @Override
                             public void handleChildChange(String parentPath, List<String> topicList) throws Exception {
                                 if (topicList.contains(name)) {
                                     consumers.remove(groupId);
                                 }
                             }
                         });
                         continue;
                     } 
                     //check inactive
                     if (cluster.zk.exists("/consumers/"+groupId+"/offsets")
                         && cluster.zk.getChildren("/consumers/"+groupId+"/offsets").contains(name)) {
                         flagConsumer(groupId, false);
                     }
 
                 }
             }
             return consumers;
         }
     }
 
     private synchronized void flagConsumer(String groupId, Boolean isOwner) {
         consumers.put(groupId, isOwner);
     }
 
 
 }
