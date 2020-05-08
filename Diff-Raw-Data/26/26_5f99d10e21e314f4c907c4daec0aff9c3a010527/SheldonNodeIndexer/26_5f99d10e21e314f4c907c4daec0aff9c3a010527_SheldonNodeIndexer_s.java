 package com.moviepilot.sheldon.compactor.custom;
 
 import com.moviepilot.sheldon.compactor.config.Config;
 import com.moviepilot.sheldon.compactor.event.NodeEvent;
 import com.moviepilot.sheldon.compactor.handler.NodeIndexer;
 import org.neo4j.helpers.collection.MapUtil;
 import org.neo4j.unsafe.batchinsert.BatchInserterIndex;
 
 /**
  * @author stefanp
  * @since 09.08.12
  */
 @SuppressWarnings("UnusedDeclaration")
 public class SheldonNodeIndexer extends SheldonIndexer<NodeEvent> implements NodeIndexer {
     private BatchInserterIndex nodeIndex;
 
     public void setup(final Config config) {
        config.getTargetIndexProvider().relationshipIndex("sheldon_node", MapUtil.stringMap("action", "exact"));
     }
 
     public void flush() {
         nodeIndex.flush();
     }
 }
