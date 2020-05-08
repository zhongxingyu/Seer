 package com.moviepilot.sheldon.compactor.handler;
 
 import com.moviepilot.sheldon.compactor.config.Config;
 import com.moviepilot.sheldon.compactor.event.IndexEntry;
 import com.moviepilot.sheldon.compactor.event.PropertyContainerEvent;
 import gnu.trove.map.custom_hash.TObjectLongCustomHashMap;
 import gnu.trove.strategy.IdentityHashingStrategy;
 import org.neo4j.unsafe.batchinsert.BatchInserterIndex;
 
 import static com.moviepilot.sheldon.compactor.event.PropertyContainerEvent.Action.DELETE;
 
 /**
  * Handler for writing into the index
  *
  * @author stefanp
  * @since 06.08.2012
  */
 public final class IndexWriter<E extends PropertyContainerEvent> extends AbstractPropertyContainerEventHandler<E> {
 
     private final TObjectLongCustomHashMap<BatchInserterIndex> counts;
     private final Kind kind;
     private final Config config;
 
     public IndexWriter(final Config config, final Kind kind) {
         super(config.getModMap());
         this.config = config;
         this.kind   = kind;
         counts =
             new TObjectLongCustomHashMap<BatchInserterIndex>(
                     new IdentityHashingStrategy<BatchInserterIndex>(), config.getNumIndexEntries());
     }
 
     public void onEvent(E event, long sequence, boolean endOfBatch) throws Exception {
         if (event.isOk() && (event.action != DELETE)) {
             final IndexEntry[] indexEntries = event.indexEntries;
             for (final IndexEntry entry : indexEntries) {
                 if (entry.write(event.id))  {
                     flushIndex(entry.index, endOfBatch);
                    getProgressor().tick(getKind().lowercaseName() + "_write_index");
                 }
             }
         }
     }
 
     private void flushIndex(final BatchInserterIndex index, boolean endOfBatch) {
         final long count = counts.adjustOrPutValue(index, 1, 1);
 
         if (((count >= config.getIndexFlushMinInterval()) && endOfBatch)
           || (count >= config.getIndexFlushMaxInterval())) {
             index.flush();
             counts.put(index, 0);
         }
     }
 
     public Kind getKind() {
         return kind;
     }
 }
