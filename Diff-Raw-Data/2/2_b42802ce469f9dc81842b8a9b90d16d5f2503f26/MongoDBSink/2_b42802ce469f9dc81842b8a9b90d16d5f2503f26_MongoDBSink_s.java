 package mongodb;
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.cloudera.flume.conf.Context;
 import com.cloudera.flume.conf.SinkFactory.SinkBuilder;
 import com.cloudera.flume.core.Event;
 import com.cloudera.flume.core.EventSink;
 import com.cloudera.util.Pair;
 import com.google.common.base.Preconditions;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.Mongo;
 import com.mongodb.MongoException;
 
 public class MongoDBSink extends EventSink.Base {
   static final Logger LOG = LoggerFactory.getLogger(MongoDBSink.class);
   
   private String serverName;
   private int serverPort;
   private String dbName;
   private String collName;
   private Mongo mongo;
   private DB db;
   private DBCollection collection;
   
   public MongoDBSink(String server, String port, String dbName, String collName) {
     this.serverName = server;
     this.serverPort = Integer.parseInt(port);
     this.dbName = dbName;
     this.collName = collName;
   }
 
   @Override
   public synchronized void append(Event e) throws IOException {
     BasicDBObject entry = new BasicDBObject();
     entry.put("timestamp", new Date(e.getTimestamp()));
     entry.put("hostname", e.getHost());
     entry.put("priority", e.getPriority().name());
     entry.put("message", new String(e.getBody()));
     Map<String, byte[]> metadata = e.getAttrs();
     if (!metadata.isEmpty()) {
       BasicDBObject metadataEntry = new BasicDBObject();
       for (String key: metadata.keySet()) {
         metadataEntry.put(key, new String(metadata.get(key)));
       }
       entry.put("metadata", metadataEntry);
     }
     collection.insert(entry);
   }
 
   @Override
   public void close() throws IOException {
     mongo.close();
   }
 
   @Override
   public void open() throws IOException {
     try {
       mongo = new Mongo(serverName, serverPort);
       db = mongo.getDB(dbName);
       collection = db.getCollection(collName);
     } catch (UnknownHostException e) {
       LOG.error("Could not find specified server.", e);
     } catch (MongoException e) {
       LOG.error("Error connecting to server.", e);
     }
   }
   
   public static SinkBuilder builder() {
     return new SinkBuilder() {
       // construct a new parameterized sink
       @Override
       public EventSink build(Context context, String... argv) {
        Preconditions.checkArgument(argv.length == 3,
             "usage: mongoDBSink(\"server\",\"port\",\"db\",\"collection\")");
 
         return new MongoDBSink(argv[0], argv[1], argv[2], argv[3]);
       }
     };
   }
 
   /**
    * This is a special function used by the SourceFactory to pull in this class
    * as a plugin sink.
    */
   public static List<Pair<String, SinkBuilder>> getSinkBuilders() {
     List<Pair<String, SinkBuilder>> builders =
       new ArrayList<Pair<String, SinkBuilder>>();
     builders.add(new Pair<String, SinkBuilder>("mongoDBSink", builder()));
     return builders;
   }
 }
