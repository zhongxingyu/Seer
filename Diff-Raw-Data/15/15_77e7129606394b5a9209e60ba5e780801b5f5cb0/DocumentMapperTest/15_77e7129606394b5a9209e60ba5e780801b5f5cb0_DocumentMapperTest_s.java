 package DocumentMapper;
 
 import DocumentMapper.RealTimeDocumentMapping.RTDocumentMapperProcessor;
 import DocumentMapper.RealTimeDocumentMapping.RTDocumentMapping;
 import Util.MongoDBUtil;
 import Util.Props;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 import junit.framework.TestCase;
 
 /**
  * Created with IntelliJ IDEA.
  * User: snowhyzhang
  * Date: 12-12-26
  * Time: 下午5:00
  * To change this template use File | Settings | File Templates.
  */
 public class DocumentMapperTest extends TestCase {
     private final String mongoDBHost = Props.getProperty("MongoDBHost");
     private final String mongoDBPort = Props.getProperty("MongoDBPort");
     private final String mongoDBName = Props.getProperty("MongoDBName");
     private final String documentQueue = Props.getProperty("DocumentQueue");
     private final String ProcessingQueue = Props.getProperty("ProcessingQueue0");
 
     public void testMapToDocumentProcessorQueue (){
         iDocumentMapper rdm = new RTDocumentMapping();
 
         initTestEnr();
         rdm.mapToDocumentProcessorQueue();
 
         MongoDBUtil dbUtil = new MongoDBUtil(mongoDBHost, mongoDBPort, mongoDBName);
         DBObject queryDocument = new BasicDBObject();
         queryDocument.put("DocumentName", "test");
         DBObject testProcessingDocument = dbUtil.findOne(queryDocument, documentQueue);
         assertEquals("Y", (String)testProcessingDocument.get("Processed"));
 
         DBObject queryShutdown = new BasicDBObject();
         queryShutdown.put("DocumentName", "SHUTDOWN");
         DBObject testShutdown = dbUtil.findOne(queryShutdown, documentQueue);
         assertEquals("Y", (String)testShutdown.get("Processed"));
 
         DBObject queryReduce = new BasicDBObject();
         queryReduce.put("ProcessingId", testProcessingDocument.get("_id").toString());
         DBObject testReduce = dbUtil.findOne(queryReduce, ProcessingQueue);
         assertEquals("N", (String)testReduce.get("Processed"));
 
         DBObject queryReduceShutdown = new BasicDBObject();
         queryReduceShutdown.put("ProcessingId", "SHUTDOWN");
        DBObject testReduceShutdown = dbUtil.findOne(queryShutdown, ProcessingQueue);
         assertEquals("N", (String)testReduceShutdown.get("Processed"));
     }
 
     private void initTestEnr(){
         MongoDBUtil dbUtil = new MongoDBUtil(mongoDBHost, mongoDBPort, mongoDBName);
 
         dbUtil.getConnection();
 
         dbUtil.drop(Props.getProperty("DocumentQueue"));
         dbUtil.drop(Props.getProperty("ProcessingQueue0"));
 
         DBObject insertionQuery = new BasicDBObject();
         insertionQuery.put("Processed", "N");
         insertionQuery.put("DocumentName", "test");
         insertionQuery.put("Content", "snow|real|time|snow|love");
         dbUtil.insert(insertionQuery, documentQueue);
 
         DBObject insertionShutdown = new BasicDBObject();
         insertionShutdown.put("DocumentName", "SHUTDOWN");
         insertionShutdown.put("Processed", "N");
         dbUtil.insert(insertionShutdown, documentQueue);
 
         dbUtil.closeConnection();
     }
 }
