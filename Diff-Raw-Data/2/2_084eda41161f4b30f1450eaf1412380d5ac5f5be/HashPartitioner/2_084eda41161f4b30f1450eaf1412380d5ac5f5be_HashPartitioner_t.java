 package org.andrewhitchcock.duwamish.util;
 
 import java.util.List;
 
 import org.andrewhitchcock.duwamish.Partition;
 import org.andrewhitchcock.duwamish.model.Partitioner;
 
 public class HashPartitioner<V, E, M> extends Partitioner<V, E, M> {
   private List<Partition<V, E, M>> partitions;
   
   public HashPartitioner(List<Partition<V, E, M>> partitions) { 
     this.partitions = partitions;
   }
   
   @Override
   public Partition<V, E, M> getPartitionByVertex(String vertexId) {
    return partitions.get(Math.abs(vertexId.hashCode()) % partitions.size());
   }
 }
