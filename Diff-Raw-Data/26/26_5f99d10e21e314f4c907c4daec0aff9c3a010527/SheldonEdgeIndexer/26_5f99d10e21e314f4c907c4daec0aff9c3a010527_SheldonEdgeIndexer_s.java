 package com.moviepilot.sheldon.compactor.custom;
 
 import com.moviepilot.sheldon.compactor.config.Config;
 import com.moviepilot.sheldon.compactor.event.EdgeEvent;
 import com.moviepilot.sheldon.compactor.handler.EdgeIndexer;
 import org.neo4j.helpers.collection.MapUtil;
 import org.neo4j.unsafe.batchinsert.BatchInserterIndex;
 
 /**
  * @author stefanp
  * @since 09.08.12
  */
 @SuppressWarnings("UnusedDeclaration")
 public class SheldonEdgeIndexer extends SheldonIndexer<EdgeEvent> implements EdgeIndexer {
 
     private  BatchInserterIndex edgeIndex;
 
     public void setup(final Config config) {
        config.getTargetIndexProvider().relationshipIndex("sheldon_connection", MapUtil.stringMap("type", "exact"));
     }
 
     public void flush() {
         edgeIndex.flush();
     }
 }
