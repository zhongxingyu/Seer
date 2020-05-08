 package dbapi.plugins.mongodb;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.inject.Inject;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 
 import dbapi.api.DBQuery;
 import dbapi.kernel.annotation.AnnotatedEntity;
 import dbapi.plugins.QueryCommand;
 
 /**
  * 
  * @author alex
  * 
  *         Nov 5, 2012
  */
 public class MongoQuery extends MongoCommand implements QueryCommand
 {
     @Inject
     private MongoSerializer serializer;
 
 
     @Override
     public <T> List<T> query(final DBQuery<T> query, final AnnotatedEntity def)
     {
         final DBCollection coll = getDB().getCollection(def.getTable());
 
        final List<T> res = new ArrayList<T>();
 
         final Map<String, Object> crit = query.getCriteria();
 
        DBCursor cursor = null;

        if(crit.isEmpty())
         {
            cursor =  coll.find();
        }
        else
        {
            final DBObject filter = new BasicDBObject();
            for(final String key : crit.keySet())
            {
                filter.put(key, crit.get(key));
            }
            cursor =  coll.find(filter);
         }
 
        try
        {
             while(cursor.hasNext())
             {
                 final DBObject obj = cursor.next();
                 final T target = serializer.doc2obj(query.getType(), def, obj);
                 res.add(target);
             }
         }
         finally
         {
             cursor.close();
         }
 
         return res;
     }
 
 }
