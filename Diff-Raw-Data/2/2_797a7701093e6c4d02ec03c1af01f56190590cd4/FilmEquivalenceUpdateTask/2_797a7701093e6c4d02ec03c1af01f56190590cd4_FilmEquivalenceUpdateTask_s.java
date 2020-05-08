 package org.atlasapi.equiv.update.tasks;
 
 import static com.metabroadcast.common.persistence.mongo.MongoBuilders.where;
 import static com.metabroadcast.common.persistence.mongo.MongoConstants.ID;
 import static org.atlasapi.persistence.content.ContentTable.TOP_LEVEL_ITEMS;
 import static org.atlasapi.persistence.content.listing.ContentListingCriteria.defaultCriteria;
 
 import org.atlasapi.equiv.update.ContentEquivalenceUpdater;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.Film;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.persistence.content.ContentTable;
 import org.atlasapi.persistence.content.listing.ContentLister;
 import org.atlasapi.persistence.content.listing.ContentListingCriteria;
 import org.atlasapi.persistence.content.listing.ContentListingHandler;
 import org.atlasapi.persistence.content.listing.ContentListingProgress;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
 import com.metabroadcast.common.persistence.mongo.MongoConstants;
 import com.metabroadcast.common.persistence.translator.TranslatorUtils;
 import com.metabroadcast.common.scheduling.ScheduledTask;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 
 
public class FilmEquivalenceUpdateTask  extends ScheduledTask {
 
     private final ContentLister contentLister;
     private final ContentEquivalenceUpdater<Film> rootUpdater;
     private final AdapterLog log;
     private final DBCollection scheduling;
     
     public FilmEquivalenceUpdateTask(ContentLister contentLister, ContentEquivalenceUpdater<Film> updater, AdapterLog log, DatabasedMongo db) {
         this.contentLister = contentLister;
         this.rootUpdater = updater;
         this.log = log;
         this.scheduling = db.collection("scheduling");
     }
     
     @Override
     protected void runTask() {
         ContentListingProgress currentProgress = getProgress();
         log.record(AdapterLogEntry.infoEntry().withSource(getClass()).withDescription(String.format("Start equivalence task from %s", startProgress(currentProgress.getUri()))));
         
         ContentListingCriteria criteria = defaultCriteria().forPublisher(Publisher.PA).startingAt(currentProgress);
         boolean finished = contentLister.listContent(ImmutableSet.of(TOP_LEVEL_ITEMS), criteria, new ContentListingHandler() {
 
             @Override
             public boolean handle(Iterable<? extends Content> contents, ContentListingProgress progress) {
                 for (Film film : Iterables.filter(contents, Film.class)) {
                     try {
                         /* EquivalenceResult<Content> result = */rootUpdater.updateEquivalences(film);
                     } catch (Exception e) {
                         log.record(AdapterLogEntry.errorEntry().withCause(e).withSource(getClass()).withDescription("Exception updating equivalence for "+film.getCanonicalUri()));
                     } 
                 }
                 reportStatus(String.format("Processed %d / %d top-level content.", progress.count(), progress.total()));
                 updateProgress(progress);
                 if (shouldContinue()) {
                     return true;
                 } else {
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
         
         scheduling.update(where().fieldEquals(ID, "filmEquivalence").build(), new BasicDBObject(MongoConstants.SET, update), true, false);
     }
     
     private ContentListingProgress getProgress() {
         DBObject progress = scheduling.findOne("filmEquivalence");
         if(progress == null || TranslatorUtils.toString(progress, "lastId").equals("start")) {
             return ContentListingProgress.START;
         }
         
         String lastId = TranslatorUtils.toString(progress, "lastId");
         String tableName = TranslatorUtils.toString(progress, "collection");
         ContentTable table = tableName == null ? null : ContentTable.fromString(tableName);
         
         return new ContentListingProgress(lastId, table)
             .withCount(TranslatorUtils.toInteger(progress, "count"))
             .withTotal(TranslatorUtils.toInteger(progress, "total"));
     }
 
 }
