 package org.mule.transport.mongodb;
 
 import com.mongodb.DB;
 import com.mongodb.Mongo;
 import org.mule.api.MuleMessage;
 import org.mule.module.client.MuleClient;
 import org.mule.tck.FunctionalTestCase;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class MongoDBFlowFunctionalTestCase extends FunctionalTestCase {
 
     @Override
     protected void doSetUp() throws Exception {
         super.doSetUp();
         logger.debug("Dropping database");
         Mongo m = new Mongo("localhost", 27017);
         DB db = m.getDB("mule-mongodb");
         db.dropDatabase();
         db.requestDone();
     }
 
 
     @Override
     protected String getConfigResources() {
         return "mongodb-global-endpoint-test-config.xml";
     }
 
     public void testUpsert() throws Exception {
         MuleClient client = new MuleClient(muleContext);
 
         Map<String, String> payload = new HashMap<String, String>();
         payload.put("name", "foo");
 
         client.send("vm://upsert.true", payload, null);
         client.request("vm://upsert.out.true",10000);

         MuleMessage result = client.request("mongodb://upserts", 15000);
         assertNotNull(result);
 
         List resultPayload = (List) result.getPayload();
         assertNotNull(resultPayload);
         assertEquals(1, resultPayload.size());
         Map upsertedObject = (Map) resultPayload.get(0);
         assertEquals("foo", upsertedObject.get("name"));
 
     }
 
     public void testNotUpsert() throws Exception {
         MuleClient client = new MuleClient(muleContext);
 
         Map<String, String> payload = new HashMap<String, String>();
         payload.put("name", "foo");
 
         client.send("vm://upsert.false", payload, null);
         client.request("vm://upsert.out.false",10000);
         MuleMessage result = client.request("mongodb://upserts", 15000);
         assertNotNull(result);
 
         List resultPayload = (List) result.getPayload();
         assertNotNull(resultPayload);
         assertEquals(0, resultPayload.size());
 
     }
 
     public void testGlobalEndpointOutbound() throws Exception {
         MuleClient client = new MuleClient(muleContext);
 
         Map<String, String> payload = new HashMap<String, String>();
         payload.put("name", "foo2");
 
         Map<String, String> properties = new HashMap<String, String>();
         properties.put(MongoDBConnector.MULE_MONGO_WRITE_CONCERN, MongoDBWriteConcern.NORMAL.toString());
 
         client.dispatch("mongodb://foo", payload, properties);
 
         MuleMessage result = client.request("vm://fooOutput", 15000);
         assertNotNull(result);
 
         List listResult = (List) result.getPayload();
         assertEquals(1, listResult.size());
 
         Map mapResult = (Map) listResult.get(0);
         assertEquals("foo2", mapResult.get("name"));
     }
 
 
     public void testGlobalEndpointInbound() throws Exception {
 
         MuleClient client = new MuleClient(muleContext);
 
         Map<String, String> payload = new HashMap<String, String>();
         payload.put("name", "foo");
 
         client.dispatch("vm://input", payload, null);
 
         MuleMessage result = client.request("vm://output", 15000);
 
         assertNotNull(result);
 
         Map mapResult = (Map) result.getPayload();
         assertEquals("foo", mapResult.get("name"));
     }
 
 
 }
