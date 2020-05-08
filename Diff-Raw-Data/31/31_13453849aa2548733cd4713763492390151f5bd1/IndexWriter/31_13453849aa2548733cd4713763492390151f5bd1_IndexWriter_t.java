 package com.moviepilot.sheldon.compactor.handler;
 
 import com.moviepilot.sheldon.compactor.config.Config;
 import com.moviepilot.sheldon.compactor.event.IndexEntry;
 import com.moviepilot.sheldon.compactor.event.PropertyContainerEvent;
 import org.neo4j.unsafe.batchinsert.BatchInserterIndex;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Future;
 
 import static com.moviepilot.sheldon.compactor.event.PropertyContainerEvent.Action.DELETE;
 
 /**
  * Handler for writing into the index
  *
  * @author stefanp
  * @since 06.08.2012
  */
 public final class IndexWriter<E extends PropertyContainerEvent> extends AbstractPropertyContainerEventHandler<E> {
 
     private final Kind kind;
     private final int indexId;
     private final String tag;
     private final ExecutorService executor;
 
     private Future<?> flusher;
 
     public IndexWriter(final Config config, final Kind kind, final ExecutorService executorService, final int indexId) {
         super(config.getModMap());
         this.kind     = kind;
         this.indexId  = indexId;
         this.executor = executorService;
         this.tag      = "index_writer_" + indexId + "_flush";
     }
 
    @SuppressWarnings("ForLoopReplaceableByForEach")
     public void onEvent_(final E event, final long sequence, final boolean endOfBatch) throws Exception {
         if (event.isOk() && (event.action != DELETE)) {
            final IndexEntry[] indexEntries = event.indexEntries;
            for (int i = 0, indexEntriesLength = indexEntries.length; i < indexEntriesLength; i++) {
                IndexEntry entry = indexEntries[i];
                 if (entry.numIndex == indexId) {
                    waitFlush();
                    if (entry.write(event.id) && entry.flush)
                        submitNewFlush(entry.index);
                 }
            }
         }
     }
 
 
     private void submitNewFlush(final BatchInserterIndex index) {
         flusher = executor.submit(new Runnable() {
             public void run() {
                 index.flush();
             }
         });
     }
 
     public void waitFlush() {
         if (flusher != null) {
             while(! flusher.isDone()) {
                 Thread.yield();
             }
             flusher = null;
             getProgressor().tick(tag);
         }
     }
 
     public Kind getKind() {
         return kind;
     }
 }
