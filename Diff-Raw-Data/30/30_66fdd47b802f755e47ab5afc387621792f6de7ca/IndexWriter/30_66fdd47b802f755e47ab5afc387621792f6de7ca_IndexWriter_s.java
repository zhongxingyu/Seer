 package com.moviepilot.sheldon.compactor.handler;
 
 import com.moviepilot.sheldon.compactor.config.Config;
 import com.moviepilot.sheldon.compactor.event.IndexEntry;
 import com.moviepilot.sheldon.compactor.event.PropertyContainerEvent;
 import org.neo4j.unsafe.batchinsert.BatchInserterIndex;
 
 import java.util.concurrent.ExecutionException;
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
 
     public IndexWriter(final Config config, final Kind kind, ExecutorService executorService, final int indexId) {
         super(config.getModMap());
         this.kind     = kind;
         this.indexId  = indexId;
         this.executor = executorService;
         this.tag      = "index_writer_" + indexId + "_flush";
     }
 
     public void onEvent_(final E event, final long sequence, final boolean endOfBatch) throws Exception {
         if (event.isOk() && (event.action != DELETE)) {
             for (final IndexEntry entry : event.indexEntries)
                 if (entry.numIndex == indexId) {
                     if (entry.write(event.id)) {
                         if (entry.flush) {
                             waitFlush();
                             submitNewFlush(entry.index);
                             getProgressor().tick(tag);
                         }
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
        try {
            while(true) {
                try {
                    flusher.get();
                    return;
                } catch (InterruptedException e) {
                    // intentionally
                 }
             }
        }
        catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        finally {
            flusher = null;
        }
     }
 
     public Kind getKind() {
         return kind;
     }
 }
