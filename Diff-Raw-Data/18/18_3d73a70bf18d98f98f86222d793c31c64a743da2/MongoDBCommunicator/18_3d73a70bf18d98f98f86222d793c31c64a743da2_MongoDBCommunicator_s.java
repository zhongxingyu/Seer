 package dribble.dataset;
 
 import com.mongodb.BasicDBList;
 import dribble.common.*;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.Mongo;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.MongoException;
 import com.mongodb.QueryBuilder;
 
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.logging.Logger;
 
 /**
  * @author Dribble
  */
 public class MongoDBCommunicator implements Dataset
 {
 
     static final Logger logger = Logger.getLogger("MongoDBCommunicator");
     private DBCollection dribTopics;
     DB db;
 
     // Initialise the database connection in constructor
     // Should look into making Singleton so it isn't created for each REST call
     public MongoDBCommunicator()
     {
         logger.info("Inside MongoDBCommunicator");
 
         // Connect to mongo database using default port 27017
         // If DB or collection doesn't exisit, it will be created
         try
         {
             Mongo m = new Mongo("localhost", 27017);
             db = m.getDB("DribbleDB");
 
             // Get collection of topics
             logger.info("Connected to DribbleDB");
             dribTopics = db.getCollection("DribTopics");
 
         }
         catch (UnknownHostException e)
         {
             logger.severe("Error constructing database: " + e.getMessage());
         }
     }
 
     @Override
     //Get all the DribSubjects available in a specific region
     public ArrayList<DribSubject> getDribSubjects(double latitude, double longitude, long radius)
     {
         logger.info("Getting all the DribTopics...");
         ArrayList<DribSubject> DribList = new ArrayList<DribSubject>();
 
         // Set criteria to get topics within certain radius from location
         // NB: needs 'loc' to be indexed before use
        double earthRadius = 6378; // km - needed to convert radius in 'near' command from radians to km
         QueryBuilder query = new QueryBuilder();
        DBObject criteria = query.start("loc").near(longitude, latitude, radius/earthRadius).get();
 
         // Fetch first topics matching criteria
         DBCursor cur = dribTopics.find(criteria);
 
         // Go through list of results and set only drib topics
         // TODO: look into a framework such as Morphia to persist POJO's
         // Embedded dribs are just discarded?  Should we save these and reuse them when topic is selected.
         while (cur.hasNext())
         {
             DBObject doc = cur.next();
 
             DribSubject topic = new DribSubject();
 
             BasicDBObject loc = (BasicDBObject) doc.get("loc");
            topic.setLatitude(loc.getInt("lat"));
            topic.setLongitude(loc.getInt("long"));
 
             topic.setSubjectID(Integer.valueOf(doc.get("_id").toString()));
             topic.setName(doc.get("name").toString());
             topic.setNumPosts(Integer.valueOf(doc.get("posts").toString()));
             topic.setNumViews(Integer.valueOf(doc.get("views").toString()));
             topic.setPopularity(Integer.valueOf(doc.get("popularity").toString()));
             topic.setTime(Long.valueOf(doc.get("currentTime").toString()));
 
             DribList.add(topic);
         }
 
         return DribList;
     }
 
     // Add drib either with new topic or add to existing topic
     @Override
     public boolean addDrib(Drib m)
     {
         logger.info("Adding a Drib to the database...");
 
         int subjectID = m.getSubject().getSubjectID();
 
         // Find if their is an existing topic (_id)
         DBObject existingTopic = dribTopics.findOne(new BasicDBObject("_id", Integer.valueOf(subjectID)));
         boolean isNewTopic = (existingTopic == null);
 
         DBObject topic;
 
         // Create new topic
         if (isNewTopic)
         {
             topic = new BasicDBObject();
             topic.put("_id", m.getSubject().getSubjectID());
             topic.put("name", m.getSubject().getName());
             topic.put("views", 0);
             topic.put("posts", 0);
             topic.put("popularity", 0);
             topic.put("currentTime", System.currentTimeMillis());
 
             // must insert long then lat
             BasicDBObject loc = new BasicDBObject();
             loc.put("long", m.getSubject().getLongitude());
             loc.put("lat", m.getSubject().getLatitude());
             topic.put("loc", loc);
 
             // Create index on topic geo key
             BasicDBObject index = new BasicDBObject();
             index.put("loc", "2d");
             dribTopics.ensureIndex(index);
         }
         else
         {
             topic = existingTopic;
         }
 
         BasicDBObject drib = new BasicDBObject();
 
         // must insert long then lat
         BasicDBObject loc = new BasicDBObject();
         loc.put("long", m.getLongitude());
         loc.put("lat", m.getLatitude());
         drib.put("loc", loc);
 
         // Create index on drib geo key
         BasicDBObject index = new BasicDBObject();
         index.put("dribs.loc", "2d");
         dribTopics.ensureIndex(index);
 
         drib.put("_id", m.getMessageID());
         drib.put("dribPopularity", m.getPopularity());
         drib.put("dribLike", m.getLikeCount());
         drib.put("currentTime", m.getTime());
         drib.put("drib", m.getText());
 
         // Add drib to array, and increase post count
         BasicDBObject addDrib = new BasicDBObject().append("$push",
                 new BasicDBObject().append("dribs", drib)).append("$inc",
                 new BasicDBObject().append("posts", 1));
         
         // Does upsert, ie. insert array if new topic, add to array if existing
         try
         {
             dribTopics.update(topic, addDrib, true, false);
         }
         catch (MongoException me)
         {
             logger.severe(String.format("Unable to add Drib with ID {0}:", m.getMessageID()) + me.getMessage());
         }
 
         return true;
     }
 
     // Get a list of dribs for a topic
     // TODO: not limiting by location or radius here
     @Override
     public ArrayList<Drib> getDribs(int subjectID, double latitude, double longitude, long radius)
     {
         logger.info("Getting Drib messages for a particular subject...");
         ArrayList<Drib> dribs = new ArrayList<Drib>();
 
         QueryBuilder query = new QueryBuilder();
         DBObject criteria = query.start("_id").is(Integer.valueOf(subjectID)).get();
 
         // Fetch first topic matching criteria
         DBObject topic = dribTopics.findOne(criteria);
 
         // Update topic views and current time
         BasicDBObject viewedTopic = new BasicDBObject().append("$inc",
                 new BasicDBObject().append("views", 1)).append("$set",
                 new BasicDBObject().append("currentTime", System.currentTimeMillis()));
         try
         {
             dribTopics.update(topic, viewedTopic);
         }
         catch (MongoException me)
         {
             logger.severe("Unable to get Dribs: " + me.getMessage());
         }
 
         // Temporary, might be better to use Morphia library to annotate Drib and DribSubject classes
         BasicDBList list = (BasicDBList) topic.get("dribs");
         int numDribs = list.size();
         for (int i = 0; i < numDribs; i++)
         {
             DBObject doc = (DBObject) list.get(i);
 
             Drib drib = new Drib();
 
             BasicDBObject loc = (BasicDBObject) doc.get("loc");
            drib.setLatitude(loc.getInt("lat"));
            drib.setLongitude(loc.getInt("long"));
 
             drib.setLikeCount(Integer.valueOf(doc.get("dribLike").toString()));
             drib.setMessageID(Integer.valueOf(doc.get("_id").toString()));
             drib.setText(doc.get("drib").toString());
             drib.setTime(Long.valueOf(doc.get("currentTime").toString()));
             drib.setPopularity(Integer.valueOf(doc.get("dribPopularity").toString()));
 
             // TODO get rid of eventually - each drib shouldn't contain whole subject
             //-------------------------------------------------------------------
             DribSubject subject = new DribSubject();
             subject.setSubjectID(Integer.valueOf(topic.get("_id").toString()));
             subject.setName(topic.get("name").toString());
             drib.setSubject(subject);
             //-------------------------------------------------------------------
 
             dribs.add(drib);
         }
 
         return dribs;
     }
 
     @Override
     public boolean deleteOldDribSubjects(long qualifyingTime)
     {
         logger.info("Deleting old DribSubjects...");
 
         BasicDBObject removeCriteria = new BasicDBObject();
         removeCriteria.put("currentTime", new BasicDBObject("$lt", qualifyingTime));
 
         try
         {
             dribTopics.remove(removeCriteria);
         }
         catch (MongoException me)
         {
             logger.severe("Error deleting old Drib Subjects: " + me.getMessage());
             return false;
         }
 
         return true;
     }
 
     @Override
     public boolean deleteOldDribs(long qualifyingTime)
     {
         logger.info("Deleting old Dribs...");
 
         BasicDBObject removeCriteria = new BasicDBObject();
         removeCriteria.put("currentTime",
                 new BasicDBObject("$lt", qualifyingTime));
 
         try
         {
             dribTopics.remove(removeCriteria);
         }
         catch (MongoException me)
         {
             logger.severe("Error deleting old Drib Subjects: " + me.getMessage());
             return false;
         }
 
         return true;
     }
 
     // Haven't tested, not used
     @Override
     public boolean deleteDrib(Drib m)
     {
         logger.info("Deleting Drib...");
 
         BasicDBObject query = new BasicDBObject();
 
         query.put("lat", m.getLatitude());
         query.put("long", m.getLongitude());
         query.put("_id", m.getMessageID());
         query.put("dribPopularity", m.getPopularity());
         query.put("dribLike", m.getLikeCount());
         query.put("currentTime", m.getTime());
         query.put("drib", m.getText());
 
         DBObject drib = dribTopics.findOne(query);
 
         dribTopics.remove(drib);
 
         logger.info("Drib deleted");
 
         return true;
     }
 
     @Override
     public boolean updateDrib(Drib m)
     {
         QueryBuilder query = new QueryBuilder();
         // Set criteria to get topics within certain radius from location
         DBObject topicCriteria = query.start("_id").is(Integer.valueOf(m.getSubject().getSubjectID())).get();
 
         // Fetch first topic matching criteria
         DBObject topic = dribTopics.findOne(topicCriteria);
 
         BasicDBList list = (BasicDBList) topic.get("dribs");
         int numDribs = list.size();
 
         for (int i = 0; i < numDribs; i++)
         {
             DBObject doc = (DBObject) list.get(i);
             if (Integer.valueOf(doc.get("_id").toString()) == m.getMessageID())
             {
                 doc.put("dribLike", m.getLikeCount());
                 dribTopics.update(topic, list);
                 dribTopics.save(topic);
             }
         }
 
         return true;
     }
 
     // Haven't tested, not used
     @Override
     public Drib getDrib(int subjectID, int DribID)
     {
         Drib drib = new Drib();
 
         QueryBuilder query = new QueryBuilder();
         // Set criteria to get topics within certain radius from location
         DBObject criteria = query.start("topic._id").is(Integer.valueOf(subjectID)).and("dribs._id").is(Integer.valueOf(DribID)).get();
 
         // Fetch first topic matching criteria
         DBObject doc = dribTopics.findOne(criteria);
 
         drib.setLatitude(Integer.valueOf(doc.get("lat").toString()));
         drib.setLongitude(Integer.valueOf(doc.get("long").toString()));
         drib.setLikeCount(Integer.valueOf(doc.get("dribLike").toString()));
         drib.setMessageID(Integer.valueOf(doc.get("_id").toString()));
         drib.setText(doc.get("drib").toString());
         drib.setTime(Long.valueOf(doc.get("currentTime").toString()));
         drib.setPopularity(Integer.valueOf(doc.get("dribPopularity").toString()));
 
         return drib;
     }
 }
