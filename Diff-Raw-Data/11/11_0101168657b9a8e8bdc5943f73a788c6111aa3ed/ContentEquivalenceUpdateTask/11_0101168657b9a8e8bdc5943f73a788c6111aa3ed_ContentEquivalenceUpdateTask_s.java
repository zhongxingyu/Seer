 package org.atlasapi.equiv.update.tasks;
 
 import static com.metabroadcast.common.persistence.mongo.MongoBuilders.where;
 import static com.metabroadcast.common.persistence.mongo.MongoConstants.ID;
 import static org.atlasapi.persistence.content.ContentTable.TOP_LEVEL_CONTAINERS;
 import static org.atlasapi.persistence.content.ContentTable.TOP_LEVEL_ITEMS;
 
 import org.atlasapi.equiv.update.ContentEquivalenceUpdater;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.persistence.content.ContentTable;
 import org.atlasapi.persistence.content.listing.ContentLister;
 import org.atlasapi.persistence.content.listing.ContentListingCriteria;
 import org.atlasapi.persistence.content.listing.ContentListingHandler;
 import org.atlasapi.persistence.content.listing.ContentListingProgress;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 
 import com.google.common.collect.ImmutableSet;
 import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
 import com.metabroadcast.common.persistence.mongo.MongoConstants;
 import com.metabroadcast.common.persistence.translator.TranslatorUtils;
 import com.metabroadcast.common.scheduling.ScheduledTask;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 
 public class ContentEquivalenceUpdateTask extends ScheduledTask {
 
     private final ContentLister contentStore;
     private final ContentEquivalenceUpdater<Content> rootUpdater;
     private final AdapterLog log;
     private final DBCollection scheduling;
     
     public ContentEquivalenceUpdateTask(ContentLister contentStore, ContentEquivalenceUpdater<Content> rootUpdater, AdapterLog log, DatabasedMongo db) {
         this.contentStore = contentStore;
         this.rootUpdater = rootUpdater;
         this.log = log;
         this.scheduling = db.collection("scheduling");
     }
     
     @Override
     protected void runTask() {
         ContentListingProgress currentProgress = getProgress();
         log.record(AdapterLogEntry.infoEntry().withSource(getClass()).withDescription(String.format("Start equivalence task from %s", startProgress(currentProgress.getUri()))));
         
         ContentListingCriteria criteria = ContentListingCriteria.defaultCriteria().startingAt(currentProgress).forPublisher(Publisher.PA);
         boolean finished = contentStore.listContent(ImmutableSet.of(TOP_LEVEL_CONTAINERS, TOP_LEVEL_ITEMS), criteria, new ContentListingHandler() {
 
             @Override
             public boolean handle(Content content, ContentListingProgress progress) {
                 try {
                     /*EquivalenceResult<Content> result = */rootUpdater.updateEquivalences(content);
                 } catch (Exception e) {
                     log.record(AdapterLogEntry.errorEntry().withCause(e).withSource(getClass()).withDescription("Exception updating equivalence for "+content.getCanonicalUri()));
                 } 
                 reportStatus(String.format("Processed %d / %d top-level content.", progress.count(), progress.total()));
                 if (shouldContinue()) {
                     if (progress.count() % 10 == 0) {
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
         log.record(AdapterLogEntry.infoEntry().withSource(getClass()).withDescription(String.format("Finished equivalence task")));
     }
 
     private String startProgress(String uri) {
         return uri == null ? "start" : uri;
     }
 
     private void updateProgress(ContentListingProgress progress) {
         DBObject update = new BasicDBObject();
         TranslatorUtils.from(update, "lastId", progress.getUri() == null ? "start" : progress.getUri());
         TranslatorUtils.from(update, "collection",  progress.getTable() == null ? null : progress.getTable().toString());
         TranslatorUtils.from(update, "total", progress.total());
         TranslatorUtils.from(update, "count", progress.count());
         
         scheduling.update(where().fieldEquals(ID, "equivalence").build(), new BasicDBObject(MongoConstants.SET, update), true, false);
     }
     
     private ContentListingProgress getProgress() {
         DBObject progress = scheduling.findOne("equivalence");
         if(progress == null || TranslatorUtils.toString(progress, "lastId").equals("start")) {
             return ContentListingProgress.START;
         }
         
         String lastId = TranslatorUtils.toString(progress, "lastId");
         String tableName = TranslatorUtils.toString(progress, "collection");
        ContentTable table = tableName == null ? null : ContentTable.valueOf(tableName);
         
         return new ContentListingProgress(lastId, table)
             .withCount(TranslatorUtils.toInteger(progress, "count"))
             .withTotal(TranslatorUtils.toInteger(progress, "total"));
     }
 
 }
