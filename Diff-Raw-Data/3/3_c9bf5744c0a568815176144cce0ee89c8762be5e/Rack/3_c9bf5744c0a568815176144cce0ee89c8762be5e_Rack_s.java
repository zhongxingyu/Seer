 package org.apache.cassandra.party.service;
 
 import static com.google.common.collect.Lists.newArrayList;
 import static java.util.UUID.randomUUID;
 
 import java.util.List;
 
 public class Rack {
     public String id = randomUUID().toString();;
     public String name;
     public RackData data = new RackData(this);
     public List<Host> children = newArrayList();
 
     public Rack(NodeInfo node) {
         this.name = node.rack;
     }
 
     public static class RackData {
         private final Rack parent;
         public int $area = 1;
         public String $color = "#D9EDF7";
 
         public RackData(Rack parent) {
             this.parent = parent;
         }
 
         public int getNbMachines() {
             return parent.children.size();
         }
     }
}
