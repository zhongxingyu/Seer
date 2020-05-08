 package mobisocial.cocoon.server;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 
 import mobisocial.cocoon.model.Nearby;
 import mobisocial.cocoon.util.Database;
 import net.vz.mongodb.jackson.DBCursor;
 import net.vz.mongodb.jackson.JacksonDBCollection;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCollection;
import com.mongodb.DBObject;
 import com.sun.jersey.spi.resource.Singleton;
 
 @Singleton
 @Path("/nearbyapi/0/")
 public class NearbyService {
 	
 	public NearbyService() {
 	}
 	
 	@POST
     @Path("sharegroup")
     @Produces("application/json")
     public String share(Nearby n) throws IOException {
         DBCollection rawCol = Database.dbInstance().getCollection(Nearby.COLLECTION);
         JacksonDBCollection<Nearby, String> col = JacksonDBCollection.wrap(rawCol,
         		Nearby.class, String.class);
 
         if(n.buckets == null || n.buckets.size() == 0)
         	throw new RuntimeException("missing buckets");
 		
         if(n.data == null || n.data.length() == 0)
         	throw new RuntimeException("missing data");
         
         long now = new Date().getTime();
         if(n.expiration < now)
         	throw new RuntimeException("expiration in the past");
         
         col.insert(n);
 		col.remove(new BasicDBObject("expiration", new BasicDBObject("$lt", now)));
 		return "ok";
     }
 	
 	@POST
     @Path("findgroup")
     @Produces("application/json")
     public List<String> find(HashSet<String> buckets) throws IOException {
         DBCollection rawCol = Database.dbInstance().getCollection(Nearby.COLLECTION);
        //because of a bug in this version of jackson mongo mapper, we can't have strings larger than 64k
        com.mongodb.DBCursor c = rawCol.find(new BasicDBObject("buckets", new BasicDBObject("$in", buckets)));
         List<String> r = new LinkedList<String>();
         long now = new Date().getTime();
         boolean need_cleanup = false;
 		while(c.hasNext()) {
			DBObject n = c.next();
			if((Long)n.get("expiration") < now) {
 				need_cleanup = true;
 				continue;
 			}
			r.add((String)n.get("data"));
 		}
 		if(need_cleanup)
			rawCol.remove(new BasicDBObject("expiration", new BasicDBObject("$lt", now)));
 		return r;
     }
 	
 }
