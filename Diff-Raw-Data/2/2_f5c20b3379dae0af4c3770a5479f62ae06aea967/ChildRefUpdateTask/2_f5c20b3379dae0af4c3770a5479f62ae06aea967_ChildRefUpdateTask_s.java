 package org.atlasapi.equiv;
 
 import static com.metabroadcast.common.persistence.mongo.MongoBuilders.where;
 import static com.metabroadcast.common.persistence.mongo.MongoConstants.ID;
 
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.persistence.content.ContentTable;
 import org.atlasapi.persistence.content.listing.ContentLister;
 import org.atlasapi.persistence.content.listing.ContentListingCriteria;
 import org.atlasapi.persistence.content.listing.ContentListingHandler;
 import org.atlasapi.persistence.content.listing.ContentListingProgress;
 import org.atlasapi.persistence.content.mongo.ChildRefWriter;
 
 import com.google.common.collect.ImmutableSet;
 import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
 import com.metabroadcast.common.persistence.mongo.MongoConstants;
 import com.metabroadcast.common.persistence.translator.TranslatorUtils;
 import com.metabroadcast.common.scheduling.ScheduledTask;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 
 public class ChildRefUpdateTask extends ScheduledTask {
 
     private final ContentLister contentLister;
     private ChildRefWriter childRefWriter;
     private DBCollection scheduling;
 
     public ChildRefUpdateTask(ContentLister contentLister, DatabasedMongo mongo) {
         this.contentLister = contentLister;
         this.childRefWriter = new ChildRefWriter(mongo);
         this.scheduling = mongo.collection("scheduling");
     }
 
     @Override
     protected void runTask() {
         
         ContentListingProgress progress = getProgress();
         
         contentLister.listContent(ImmutableSet.of(ContentTable.CHILD_ITEMS), ContentListingCriteria.defaultCriteria().startingAt(progress), new ContentListingHandler() {
             
             @Override
             public boolean handle(Content content, ContentListingProgress progress) {
                 if(content instanceof Episode) {
                     childRefWriter.includeEpisodeInSeriesAndBrand((Episode)content);
                 } else if(content instanceof Item) {
                     childRefWriter.includeItemInTopLevelContainer((Item)content);
                 }
                 reportStatus(progress.toString());
                 if(progress.count() % 100 == 0) {
                     updateProgress(progress);
                 }
                 return true;
             }
             
         });
         
     }
 
     private void updateProgress(ContentListingProgress progress) {
         DBObject update = new BasicDBObject();
         TranslatorUtils.from(update, "lastId", progress.getUri() == null ? "start" : progress.getUri());
         TranslatorUtils.from(update, "collection",  progress.getTable() == null ? null : progress.getTable().toString());
         TranslatorUtils.from(update, "total", progress.total());
         TranslatorUtils.from(update, "count", progress.count());
         
         scheduling.update(where().fieldEquals(ID, "childref").build(), new BasicDBObject(MongoConstants.SET, update), true, false);
     }
     
     private ContentListingProgress getProgress() {
         DBObject progress = scheduling.findOne("childref");
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
