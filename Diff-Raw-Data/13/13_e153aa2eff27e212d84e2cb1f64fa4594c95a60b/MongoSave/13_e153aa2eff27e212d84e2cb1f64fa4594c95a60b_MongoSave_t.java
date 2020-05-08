 package dbapi.plugins.mongodb;
 
 import javax.inject.Inject;
 
 import org.bson.types.ObjectId;
 
 import com.google.common.base.Strings;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 import com.mongodb.WriteResult;
 
 import dbapi.api.KernelException;
 import dbapi.kernel.annotation.AnnotatedEntity;
 import dbapi.plugins.SaveCommand;
 
 /**
  * 
  * @author alex
  * 
  *         Nov 4, 2012
  */
 public class MongoSave extends MongoCommand implements SaveCommand
 {
     @Inject
     private MongoSerializer serializer;
 
     @Override
     public void execute(final Object entity, final AnnotatedEntity def)
     {
         final DBCollection coll = getDB().getCollection(def.getTable());
 
         final BasicDBObject doc = serializer.object2doc(entity, def);
 
         WriteResult res = null;
 
         if(doc.containsKey("_id"))
         {
             final DBObject query = new BasicDBObject("_id", doc.get("_id"));
            res = coll.update(query, doc);
         }
         else
         {
             res = coll.insert(doc);
         }
 
         final String error = res.getError();
 
         if (!Strings.isNullOrEmpty(error))
         {
             throw new KernelException("Failed to save entity " + def.getTable() + ". Reason: " + error);
         }
 
         final ObjectId id = ObjectId.class.cast(doc.get("_id"));
 
         def.setId(entity, id.toStringMongod());
 
     }
 
 
 
 }
