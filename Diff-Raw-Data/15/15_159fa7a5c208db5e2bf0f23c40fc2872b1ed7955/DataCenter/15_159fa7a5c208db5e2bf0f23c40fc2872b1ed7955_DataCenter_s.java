 package org.apache.cassandra.party.service;
 
 import static com.google.common.collect.Lists.newArrayList;
 import static java.util.UUID.randomUUID;
 
 import java.util.List;
 
 public class DataCenter {
     public String id = randomUUID().toString();;
     public String name;
     public DcData data = new DcData(this);
     public List<Rack> children = newArrayList();
 
     public DataCenter(NodeInfo node) {
         this.name = node.dc; // todo city name
     }
 
    public Rack getRack(String id) {
         for (Rack rack : children) {
            if (rack.id.equals(id)) {
                 return rack;
             }
         }
         return null;
     }
 
     public class DcData {
         final DataCenter parent;
         public String $color = "#3A87AD";
         public int $area = 1;
 
         public DcData(DataCenter parent) {
             this.parent = parent;
         }
 
         public int getNbRacks() {
             return parent.children.size();
         }
     }
 }
