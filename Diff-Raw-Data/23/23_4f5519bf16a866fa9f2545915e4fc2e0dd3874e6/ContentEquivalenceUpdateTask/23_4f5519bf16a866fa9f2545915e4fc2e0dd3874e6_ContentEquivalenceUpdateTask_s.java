 package org.atlasapi.equiv.update;
 
import static com.metabroadcast.common.persistence.mongo.MongoBuilders.update;
 import static com.metabroadcast.common.persistence.mongo.MongoBuilders.where;
 import static com.metabroadcast.common.persistence.mongo.MongoConstants.ID;
 import static org.atlasapi.persistence.content.ContentTable.TOP_LEVEL_CONTAINERS;
 import static org.atlasapi.persistence.content.ContentTable.TOP_LEVEL_ITEMS;
 
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.persistence.content.ContentLister;
 import org.atlasapi.persistence.content.ContentListingHandler;
 import org.atlasapi.persistence.content.ContentListingProgress;
 import org.atlasapi.persistence.content.ContentTable;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 
 import com.google.common.collect.ImmutableSet;
 import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
 import com.metabroadcast.common.persistence.translator.TranslatorUtils;
 import com.metabroadcast.common.scheduling.ScheduledTask;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 
 public class ContentEquivalenceUpdateTask extends ScheduledTask {
 
     private final ContentLister contentStore;
     private final ContentEquivalenceUpdater<Content> rootUpdater;
     private final AdapterLog log;
     private final DBCollection scheduling;
     
     private final AtomicInteger processed = new AtomicInteger();
 
     public ContentEquivalenceUpdateTask(ContentLister contentStore, ContentEquivalenceUpdater<Content> rootUpdater, AdapterLog log, DatabasedMongo db) {
         this.contentStore = contentStore;
         this.rootUpdater = rootUpdater;
         this.log = log;
         this.scheduling = db.collection("scheduling");
     }
     
     @Override
     protected void runTask() {
         ContentListingProgress currentProgress = getProgress();
         log.record(AdapterLogEntry.infoEntry().withSource(getClass()).withDescription(String.format("Start equivalence task from %s", currentProgress.getUri())));
         
         processed.set(0);
         
         boolean finished = contentStore.listContent(ImmutableSet.of(TOP_LEVEL_CONTAINERS, TOP_LEVEL_ITEMS), currentProgress, new ContentListingHandler() {
 
             @Override
             public boolean handle(Content content, ContentListingProgress progress) {
                 try {
                     /*EquivalenceResult<Content> result = */rootUpdater.updateEquivalences(content);
                     //TODO filter to avoid repetition
                 } catch (Exception e) {
                     log.record(AdapterLogEntry.errorEntry().withCause(e).withSource(getClass()).withDescription("Exception updating equivalence for "+content.getCanonicalUri()));
                 } 
                 reportStatus(String.format("Processed %d top-level content", processed.incrementAndGet()));
                 if (shouldContinue()) {
                     if (processed.get() % 10 == 0) {
                         updateProgress(progress);
                     }
                     return true;
                 } else {
                     updateProgress(progress);
                     return false;
                 }
             }
            
         });
         
         if(finished) {
             updateProgress(ContentListingProgress.START);
         }
         log.record(AdapterLogEntry.infoEntry().withSource(getClass()).withDescription(String.format("Finish equivalence task. %s top-level content processed", processed)));
     }
 
     private void updateProgress(ContentListingProgress progress) {
        DBObject update = update().setField("lastId", progress.getUri()).setField("collection", progress.getTable().toString()).build();
        scheduling.update(where().fieldEquals(ID, "equivalence").build(), update, true, false);
     }
     
     private ContentListingProgress getProgress() {
         DBObject progress = scheduling.findOne("equivalence");
        if(progress == null) {
             return ContentListingProgress.START;
         }
         
         String lastId = TranslatorUtils.toString(progress, "lastId");
        ContentTable table = ContentTable.valueOf(TranslatorUtils.toString(progress, "collection"));
         return new ContentListingProgress(lastId, table);
     }
 
 }
