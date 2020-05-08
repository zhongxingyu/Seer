 package DocumentMapper.RealTimeDocumentMapping;
 
 import DocumentMapper.iDocumentMapper;
 import Util.MongoDBUtil;
 import Util.Props;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 import org.bson.types.ObjectId;
 
 import java.util.List;
 
 import static java.lang.Thread.sleep;
 
 /**
  * Created with IntelliJ IDEA.
  * User: snowhyzhang
  * Date: 12-12-17
  * Time: 下午3:52
  * To change this template use File | Settings | File Templates.
  */
 public class RTDocumentMapping implements iDocumentMapper {
     private MongoDBUtil dbUtil = null;
     private final String mongoDBHost = Props.getProperty("MongoDBHost");
     private final String mongoDBPort = Props.getProperty("MongoDBPort");
     private final String mongoDBName = Props.getProperty("MongoDBName");
 
     private final int sleepTime = Integer.parseInt(Props.getProperty("MapperSleepTime"));
     private final int queueNumber = Integer.parseInt(Props.getProperty("QueueNumber"));
     private final String documentQueue = Props.getProperty("DocumentQueue");
 
     public void setDBConnection(String MongoDBHost, String MongoDBPort, String MongoDBName) {
         if (dbUtil == null){
             dbUtil = new MongoDBUtil(MongoDBHost, MongoDBPort, MongoDBName);
         } else {
             dbUtil.setMongoDBHost(MongoDBHost);
             dbUtil.setMongoDBPort(MongoDBPort);
             dbUtil.setMongoDBName(MongoDBName);
         }
     }
 
     @Override
     public int mapToDocumentProcessorQueue() {
         setDBConnection(mongoDBHost, mongoDBPort, mongoDBName);
         dbUtil.getConnection();
         boolean shutdownFlag = false;
         while (!shutdownFlag){
             List<DBObject> processingDoc = getProcessingDoc();
             if (processingDoc == null || processingDoc.isEmpty()){
                 System.out.println("No document to be processed.");
                 try {
                     sleep (sleepTime);
                 } catch (InterruptedException ex) {
                     ex.printStackTrace();
                 }
                 continue;
             }
             for (DBObject dbObject: processingDoc){
                 String documentName = (String)dbObject.get("DocumentName");
                 if (documentName.equalsIgnoreCase("SHUTDOWN")){
                     shutdownFlag = true;
                    processingShutMessage(dbObject);
                     System.out.println("Processing is SHUTDOWN");
                 } else {
                     ProcessMapping(dbObject);
                 }
             }
             System.out.println("Processed Document:" + processingDoc.size());
         }
 
         dbUtil.closeConnection();
 
         return 0;
     }
 
     private void ProcessMapping(DBObject dbObject) {
         String objId = dbObject.get("_id").toString();
         int mapperHashQueue = objId.hashCode() % queueNumber;
         String processingQueue = Props.getProperty("ProcessingQueue" + String.valueOf(mapperHashQueue));
 
         DBObject insertionQuery = new BasicDBObject();
         insertionQuery.put("ProcessingId", objId);
         insertionQuery.put("Processed", "N");
         dbUtil.insert(insertionQuery, processingQueue);
 
         DBObject queryUpdate = new BasicDBObject("_id", new ObjectId(objId));
 
         DBObject updateDocumentMapperStatus = new BasicDBObject();
         updateDocumentMapperStatus.put("Processed", "Y");
         updateDocumentMapperStatus = new BasicDBObject().append("$set", updateDocumentMapperStatus);
         dbUtil.update(queryUpdate, updateDocumentMapperStatus, documentQueue);
     }
 
    private void processingShutMessage(DBObject dbObject) {
         DBObject updateQuery = new BasicDBObject("Processed", "Y");
         updateQuery = new BasicDBObject().append("$set", updateQuery);
         dbUtil.update(dbObject, updateQuery, documentQueue);
 
         sendShutdownToReduceQueue();
     }
 
     private void sendShutdownToReduceQueue() {
         for (int i = 0; i < Integer.parseInt(Props.getProperty("QueueNumber")); ++i) {
             String processingQueue = Props.getProperty("ProcessingQueue" + String.valueOf(i));
             DBObject insertionQuery = new BasicDBObject();
             insertionQuery.put("ProcessingId", "SHUTDOWN");
             insertionQuery.put("Processed", "N");
             dbUtil.insert(insertionQuery, processingQueue);
         }
     }
 
     private List<DBObject> getProcessingDoc() {
         DBObject query = new BasicDBObject();
         query.put("Processed", "N");
         DBObject field = new BasicDBObject();
         field.put("DocumentContent", 0);
        return dbUtil.findAll(query, documentQueue);
     }
 }
