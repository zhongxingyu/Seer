 package com.moviepilot.sheldon.compactor.custom;
 
 import com.moviepilot.sheldon.compactor.config.Config;
 import com.moviepilot.sheldon.compactor.event.EdgeEvent;
 import com.moviepilot.sheldon.compactor.event.IndexEntry;
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
 
     @Override
     public void setup(final Config config) {
         super.setup(config);
         edgeIndex =
             config.getTargetIndexProvider().relationshipIndex("sheldon_connection", MapUtil.stringMap("type", "exact"));
     }
 
     protected void setupEntry(final IndexEntry indexEntry) {
         if (indexEntry.index == null)
             indexEntry.index = edgeIndex;
     }
 
     public void flush() {
         edgeIndex.flush();
     }
 
     public final Kind getKind() {
         return Kind.EDGE;
     }
 
 }
