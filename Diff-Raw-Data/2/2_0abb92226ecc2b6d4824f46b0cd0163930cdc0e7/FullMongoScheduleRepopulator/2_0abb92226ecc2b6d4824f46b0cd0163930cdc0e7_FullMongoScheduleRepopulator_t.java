 package org.atlasapi.persistence.content.mongo;
 
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.atlasapi.media.entity.Container;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Film;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.persistence.content.schedule.mongo.ScheduleWriter;
 import org.atlasapi.persistence.media.entity.ContainerTranslator;
 import org.atlasapi.persistence.media.entity.ItemTranslator;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.metabroadcast.common.concurrency.BoundedExecutor;
 import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
 import com.metabroadcast.common.persistence.mongo.MongoBuilders;
 import com.metabroadcast.common.persistence.mongo.MongoConstants;
 import com.metabroadcast.common.persistence.mongo.MongoQueryBuilder;
 import com.metabroadcast.common.persistence.mongo.MongoSortBuilder;
 import com.metabroadcast.common.persistence.translator.TranslatorUtils;
 import com.metabroadcast.common.scheduling.ScheduledTask;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 
 public class FullMongoScheduleRepopulator extends ScheduledTask {
     
     private final ScheduleWriter scheduleStore;
     private final DBCollection contentCollection;
     private static final Log log = LogFactory.getLog(FullMongoScheduleRepopulator.class);
     private static final int BATCH_SIZE = 5;
     private final ContainerTranslator containerTranslator = new ContainerTranslator();
     private final ItemTranslator itemTranslator = new ItemTranslator(true);
     private final ExecutorService executor = Executors.newFixedThreadPool(10);
     private final BoundedExecutor boundedQueue = new BoundedExecutor(executor, 10);
     private final Iterable<Publisher> forPublishers;
 
     public FullMongoScheduleRepopulator(DatabasedMongo db, ScheduleWriter scheduleStore, Iterable<Publisher> forPublishers) {
         this.forPublishers = forPublishers;
         contentCollection = db.collection("content");
         this.scheduleStore = scheduleStore;
     }
     
     private MongoQueryBuilder where(Iterable<Publisher> forPublishers) {
         if (Iterables.isEmpty(forPublishers)) {
             return MongoBuilders.where();
         }
         return MongoBuilders.where().fieldIn("publisher", Iterables.transform(forPublishers, Publisher.TO_KEY));
     }
     
     @Override
     public void runTask() {
         String currentId = "0";
         long totalRows = contentCollection.count(where(forPublishers).build());
         long rowsSeen = 0;
         long errors = 0;
         while (shouldContinue()) {
         	reportStatus(rowsSeen + "/" + totalRows + ", " + errors + " errors");
         	
             List<DBObject> objects = ImmutableList.copyOf(where(forPublishers).fieldGreaterThan(MongoConstants.ID, currentId).find(contentCollection, new MongoSortBuilder().ascending(MongoConstants.ID), -BATCH_SIZE));
             if (objects.isEmpty()) {
                 break;
             }
             rowsSeen += objects.size();
             
             ImmutableList.Builder<Item> itemsBuilder = ImmutableList.builder();
             final String latestId = TranslatorUtils.toString(Iterables.getLast(objects), MongoConstants.ID);
             if (latestId == null || latestId.equals(currentId)) {
                 break;
             }
             currentId = latestId;
             
             for (DBObject dbObject: objects) {
                 try {
                     String type = (String) dbObject.get("type");
                     
                     if (Episode.class.getSimpleName().equals(type) || Film.class.getSimpleName().equals(type) || Item.class.getSimpleName().equals(type)) {
                         Item item = itemTranslator.fromDBObject(dbObject, null);
                         itemsBuilder.add(item);
                     } else {
                         Container<?> container = containerTranslator.fromDBObject(dbObject, null);
                         itemsBuilder.addAll(container.getContents());
                     }
                 } catch (Exception e) {
                     errors++;
                    log.error("Problem translating content from mongo: " + TranslatorUtils.toString(dbObject, MongoConstants.ID), e);
                 }
             }
     
             List<Item> items = itemsBuilder.build();
             try {
                 boundedQueue.submitTask(new UpdateItemScheduleJob(items));
             } catch (InterruptedException e) {
                 log.error("Problem submitting task to process queue for items: "+items, e);
             }
         }
     }
     
     class UpdateItemScheduleJob implements Runnable {
         
         private final Iterable<? extends Item> items;
 
         public UpdateItemScheduleJob(Iterable<? extends Item> items) {
             this.items = items;
         }
 
         @Override
         public void run() {
             scheduleStore.writeScheduleFor(items);
         }
     }
 }
