 package ch.x42.terye.persistence;
 
 import java.net.UnknownHostException;
 import java.util.Iterator;
 
 import javax.jcr.RepositoryException;
 
 import ch.x42.terye.persistence.ChangeLog.AddOperation;
 import ch.x42.terye.persistence.ChangeLog.Operation;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 import com.mongodb.Mongo;
 import com.mongodb.MongoException;
 
 public class PersistenceManager {
 
     private static PersistenceManager instance;
     private DBCollection collection;
 
     private PersistenceManager() throws RepositoryException {
         try {
             collection = new Mongo("localhost", 27018).getDB("test")
                     .getCollection("repo");
             // XXX: temporary
             // collection.drop();
         } catch (UnknownHostException e) {
             e.printStackTrace();
         } catch (MongoException e) {
             throw new RepositoryException("Unable to access MongoDB collection");
         }
     }
 
     public static PersistenceManager getInstance() throws RepositoryException {
         if (instance == null) {
             instance = new PersistenceManager();
         }
         return instance;
     }
 
     public NodeState load(String path) {
         System.out.println("load node: " + path);
         BasicDBObject query = new BasicDBObject();
         query.put("path", path);
         DBObject res = collection.findOne(query);
        if (res == null) {
            return null;
        }
         NodeState ns = new NodeState();
         ns.putAll(res.toMap());
         return ns;
     }
 
     private void store(NodeState ns) {
         System.out.println("store node: " + ns.getPath());
         BasicDBObject dbo = new BasicDBObject();
         dbo.put("path", ns.getPath());
         collection.update(dbo, ns, true, false);
     }
 
     public void persist(ChangeLog log) throws RepositoryException {
         Iterable<Operation> operations = log.getOperations();
         Iterator<Operation> iterator = operations.iterator();
         while (iterator.hasNext()) {
             Operation op = iterator.next();
             if (op instanceof AddOperation) {
                 store(op.getNode().getState());
             }
         }
     }
 
 }
