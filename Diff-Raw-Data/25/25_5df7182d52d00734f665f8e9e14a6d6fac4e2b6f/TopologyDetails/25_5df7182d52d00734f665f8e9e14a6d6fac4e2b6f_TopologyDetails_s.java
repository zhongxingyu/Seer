 package backtype.storm.scheduler;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import backtype.storm.Config;
 import backtype.storm.generated.StormTopology;
 
 
 public class TopologyDetails {
     String topologyId;
     Map topologyConf;
     StormTopology topology;
     Map<ExecutorDetails, String> executorToComponent;
  
    public TopologyDetails(String topologyId, Map topologyConf, StormTopology topology) {
         this.topologyId = topologyId;
         this.topologyConf = topologyConf;
         this.topology = topology;
     }
     
    public TopologyDetails(String topologyId, Map topologyConf, StormTopology topology, Map<ExecutorDetails, String> executorToComponents) {
        this.topologyId = topologyId;
        this.topologyConf = topologyConf;
        this.topology = topology;
         this.executorToComponent = new HashMap<ExecutorDetails, String>(0);
         if (executorToComponents != null) {
             this.executorToComponent.putAll(executorToComponents);
         }
     }
     
     public String getId() {
         return topologyId;
     }
     
     public String getName() {
         return (String)this.topologyConf.get(Config.TOPOLOGY_NAME);
     }
     
     public Map getConf() {
         return topologyConf;
     }
     
     public StormTopology getTopology() {
         return topology;
     }
 
     public Map<ExecutorDetails, String> getExecutorToComponent() {
         return this.executorToComponent;
     }
 
     public Map<ExecutorDetails, String> selectExecutorToComponent(Collection<ExecutorDetails> executors) {
         Map<ExecutorDetails, String> ret = new HashMap<ExecutorDetails, String>(executors.size());
         for (ExecutorDetails executor : executors) {
             String compId = this.executorToComponent.get(executor);
             if (compId != null) {
                 ret.put(executor, compId);
             }
         }
         
         return ret;
     }
     
     public Collection<ExecutorDetails> getExecutors() {
         return this.executorToComponent.keySet();
     }
 }
