 package org.atlasapi.persistence.output;
 
 import static com.metabroadcast.common.persistence.mongo.MongoBuilders.select;
 import static com.metabroadcast.common.persistence.mongo.MongoBuilders.where;
 import static com.metabroadcast.common.persistence.translator.TranslatorUtils.toDBObjectList;
 import static com.metabroadcast.common.persistence.translator.TranslatorUtils.toDateTime;
 
 import org.atlasapi.media.entity.Container;
 import org.atlasapi.persistence.content.ContentCategory;
 import org.joda.time.DateTime;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicates;
 import com.google.common.collect.Iterables;
 import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
 import com.metabroadcast.common.persistence.mongo.MongoConstants;
 import com.metabroadcast.common.persistence.translator.TranslatorUtils;
 import com.metabroadcast.common.time.Clock;
 import com.metabroadcast.common.time.SystemClock;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 
 public class MongoUpcomingChildrenResolver implements UpcomingChildrenResolver {
     
     private final String versions = "versions";
     private final String broadcasts = "broadcasts";
     private final String transmissionEndTime = "transmissionEndTime";
     
    private final String transmissionEndTimeKey = String.format("%s%s%s", versions, broadcasts, transmissionEndTime);
     private final String containerKey = "container";
     
     private final DBObject fields = select().field(transmissionEndTimeKey).build();
 
     private DBCollection children;
     private final Clock clock;
     
     public MongoUpcomingChildrenResolver(DatabasedMongo db) {
         this(db, new SystemClock());
     }
     
     public MongoUpcomingChildrenResolver(DatabasedMongo db, Clock clock) {
         this.children = db.collection(ContentCategory.CHILD_ITEM.tableName());
         this.clock = clock;
     }
     
     @Override
     public Iterable<String> availableChildrenFor(Container container) {
         final DateTime now = clock.now();
         return Iterables.filter(Iterables.transform(availablityWindowsForChildrenOf(container, now), new Function<DBObject, String>() {
 
             @Override
             public String apply(DBObject input) {
                 for (DBObject version : toDBObjectList(input, versions)) {
                     for (DBObject broadcast : toDBObjectList(version, broadcasts)) {
                             if (after(toDateTime(broadcast, transmissionEndTime), now)) {
                                 return TranslatorUtils.toString(input, MongoConstants.ID);
                             }
                         }
                     }
                 return null;
             }
         }),Predicates.notNull());
     }
     
     private boolean after(DateTime dateTime, DateTime now) {
         return dateTime != null && dateTime.isAfter(now);
     }
 
     private Iterable<DBObject> availablityWindowsForChildrenOf(Container container, DateTime time) {
        return children.find(where().fieldEquals(containerKey, container.getCanonicalUri()).fieldAfter(transmissionEndTime, time).build(),fields);
     }
     
 }
