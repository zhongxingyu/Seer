 package org.atlasapi.media.segment;
 
 import static com.metabroadcast.common.persistence.mongo.MongoConstants.ID;
 import static com.metabroadcast.common.persistence.mongo.MongoConstants.SINGLE;
 import static com.metabroadcast.common.persistence.mongo.MongoConstants.UPSERT;
 
 import com.metabroadcast.common.ids.NumberToShortStringCodec;
 import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
 import com.metabroadcast.common.persistence.mongo.MongoBuilders;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 
 public class MongoSegmentWriter implements SegmentWriter {
 
     private final DBCollection collection;
     private final SegmentTranslator translator;
 
     public MongoSegmentWriter(DatabasedMongo mongo, NumberToShortStringCodec idCodec) {
         this.collection = mongo.collection("segments");
         this.translator = new SegmentTranslator(idCodec);
     }
     
     @Override
     public Segment write(Segment segment) {
         write(translator.toDBObject(null, segment));
         return segment;
     }
 
     private void write(DBObject dbo) {
        collection.update(MongoBuilders.where().idEquals(String.valueOf(dbo.get(ID))).build(), dbo, UPSERT, SINGLE);
     }
 
 }
