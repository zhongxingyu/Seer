 package org.atlasapi.persistence.ids;
 
 import static com.metabroadcast.common.persistence.mongo.MongoBuilders.update;
 
 import java.math.BigInteger;
 
 import com.metabroadcast.common.ids.IdGenerator;
 import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;
 import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
 import com.metabroadcast.common.persistence.mongo.MongoConstants;
 import com.metabroadcast.common.persistence.mongo.MongoQueryBuilder;
 import com.metabroadcast.common.persistence.translator.TranslatorUtils;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 import com.mongodb.WriteConcern;
 
 public class MongoSequentialIdGenerator implements IdGenerator {
 
     private static final String VALUE_KEY = "nextId";
     
     private final DBCollection collection;
     private final String idGroup;
     private final SubstitutionTableNumberCodec codec;
     private final long initialId;
 
     public MongoSequentialIdGenerator(DatabasedMongo mongo, String idGroup) {
         this.collection = mongo.collection("id");
         this.idGroup = idGroup;
         this.codec = new SubstitutionTableNumberCodec();
        this.initialId = new Double(Math.pow(codec.getAlphabet().size(), 3)).longValue(); //minimum id length 3 chars.
         ensureFieldExists();
     }
 
     private void ensureFieldExists() {
         DBObject find = collection.findOne(new MongoQueryBuilder().idEquals(idGroup).build());
 
         if (find == null) {
             BasicDBObject basicDBObject = new BasicDBObject(MongoConstants.ID, idGroup);
             basicDBObject.put(VALUE_KEY, initialId);
             collection.insert(basicDBObject, WriteConcern.SAFE);
         }
     }
 
     @Override
     public String generate() {
         DBObject findAndModify = collection.findAndModify(new MongoQueryBuilder().idEquals(idGroup).build(), update().incField(VALUE_KEY, 1).build());
 
         return codec.encode(BigInteger.valueOf(TranslatorUtils.toLong(findAndModify, VALUE_KEY)));
     }
 
 }
