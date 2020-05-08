 package org.apache.cassandra.party.service;
 
 import static com.google.common.collect.Lists.newArrayList;
 import static java.util.UUID.randomUUID;
 
 import java.util.Date;
 import java.util.List;
 
 public class Cluster {
     public List<DataCenter> children = newArrayList();
     public String id = randomUUID().toString();;
     public String name = "Cluster " + new Date();
     public ClusterData data = new ClusterData();
 
    public DataCenter getDc(String name) {
         for (DataCenter dc : children) {
            if (dc.name.equals(name)) {
                 return dc;
             }
         }
         return null;
     }
 
     public static class ClusterData {
         public int $area = 1;
         public String $color = "#2d6987";
     }
 
     public static Cluster buildCluster(List<NodeInfo> nodes) {
         Cluster cluster = new Cluster();
 
         for (NodeInfo node : nodes) {
             DataCenter dc = cluster.getDc(node.dc);
 
             if (dc == null) {
                 dc = new DataCenter(node);
                 cluster.children.add(dc);
             }
 
             Rack rack = dc.getRack(node.rack);
 
             if (rack == null) {
                 rack = new Rack(node);
                 dc.children.add(rack);
             }
             rack.children.add(new Host(node));
         }
 
         return cluster;
     }
 }
