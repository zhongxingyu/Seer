 package com.moviepilot.sheldon.compactor.custom;
 
 import com.moviepilot.sheldon.compactor.config.Config;
 import com.moviepilot.sheldon.compactor.event.IndexEntry;
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
 
     @Override
     public void setup(final Config config) {
         super.setup(config);
         nodeIndex =
            config.getTargetIndexProvider().relationshipIndex("sheldon_node", MapUtil.stringMap("type", "exact"));
     }
 
     protected void setupEntry(final IndexEntry indexEntry) {
         if (indexEntry.index == null)
             indexEntry.index = nodeIndex;
         else {
             if (indexEntry.index != nodeIndex)
                 throw new IllegalStateException("Invalid index entry");
         }
     }
 
     public void flush() {
         nodeIndex.flush();
     }
 
     public final Kind getKind() {
         return Kind.NODE;
     }
 }
