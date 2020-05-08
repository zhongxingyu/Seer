 package com.rambi;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.naming.NamingException;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 
 public class DatastoreTest {
 
     private TestServer server = new TestServer();
 
     @Before
     public void init() throws NamingException {
         server.init();
     }
 
     @After
     public void tearDown() throws NamingException {
         server.end();
     }
 
     @Test
     public void testDatastore() throws Exception {
 
         // PUT
         assertPut();
 
         // POST
         long id = assertPost();
 
         // GET
         assertGet(id);
 
         // Queries
         createQueryMockEntities();
 
         // Query 1
         // LESS THAN
         assertLessThan(false);
         assertLessThan(true);
 
         // Query 2
         // LESS THAN OR EQUALS
         assertLessThanOrEquals(false);
         assertLessThanOrEquals(true);
 
         // Query 3
         // GREATER THAN
         assertGreatThan(false);
         assertGreatThan(true);
 
         // Query 4
         // GREATER THAN OR EQUALS
         assertGreaterOtEqualsThan(false);
         assertGreaterOtEqualsThan(true);
 
         // Query 5
         // EQUAL
         assertEqual(false);
         assertEqual(true);
 
         // Query 6
         // NOT EQUAL
         assertNotEqual(false);
         assertNotEqual(true);
 
         // Query 7
         // IN
         assertIn(false);
         assertIn(true);
 
         // Query 8
         // MULTIPLE OPERATOR
         assertMultiple(false);
         assertMultiple(true);
 
         // Query 9
         // MULTIPLE OPERATOR ON SAME FIELD
         assertRange(false);
         assertRange(true);
 
         // Query 10
         // NO FILTERS
         assertNoFilter(false);
         assertNoFilter(true);
 
         // Query 11
         // SORT
         createSortMockEntities();
         assertSort(false);
         assertSort(true);
 
         // Query 12
         // OFFSET and LIMIT
         assertLimitOffset(false);
         assertLimitOffset(true);
 
         assertDelete(id);
     }
 
     private void assertDelete(long id) {
         Response response = HttpUtils
                 .delete("http://localhost:8080/services/DatastoreTest.js?key=" + id + "&kind=Kind");
         assertEquals(200, response.getStatus());
 
         response = HttpUtils
                 .get("http://localhost:8080/services/DatastoreTest.js?key=" + id + "&kind=Kind");
         assertEquals(200, response.getStatus());
         assertEquals("null", response.getAsString());
 
     }
 
     private void assertLimitOffset(boolean b) {
         Response resp = null;
         if (b) {
             resp = HttpUtils.get("http://localhost:8080/services/DatastoreTest.js?query12=true");
         } else {
             resp = HttpUtils.get("http://localhost:8080/services/DatastoreTest.js?query12=true&builder=true");
         }
 
         JsonArray result = (JsonArray) new JsonParser().parse(resp.getAsString());
 
         assertEquals(1, result.size());
 
         JsonObject asJsonObject = result.get(0).getAsJsonObject();
         assertEquals(2, asJsonObject.get("number").getAsInt());
     }
 
     private void assertSort(boolean b) {
         Response resp = null;
         if (b) {
             resp = HttpUtils.get("http://localhost:8080/services/DatastoreTest.js?query11=true");
         } else {
             resp = HttpUtils.get("http://localhost:8080/services/DatastoreTest.js?query11=true&builder=true");
         }
 
         JsonArray result = (JsonArray) new JsonParser().parse(resp.getAsString());
 
         assertEquals(4, result.size());
 
         JsonObject asJsonObject = result.get(0).getAsJsonObject();
         assertEquals(3, asJsonObject.get("number").getAsInt());
         assertEquals(1, asJsonObject.get("number2").getAsInt());
 
         asJsonObject = result.get(1).getAsJsonObject();
         assertEquals(1, asJsonObject.get("number").getAsInt());
         assertEquals(1, asJsonObject.get("number2").getAsInt());
 
         asJsonObject = result.get(2).getAsJsonObject();
         assertEquals(0, asJsonObject.get("number").getAsInt());
         assertEquals(1, asJsonObject.get("number2").getAsInt());
 
         asJsonObject = result.get(3).getAsJsonObject();
         assertEquals(2, asJsonObject.get("number").getAsInt());
         assertEquals(2, asJsonObject.get("number2").getAsInt());
 
     }
 
     private long assertPost() {
         Response response = HttpUtils.post("http://localhost:8080/services/DatastoreTest.js", null);
 
         long id = Long.parseLong(response.getAsString());
 
         response = HttpUtils.get("http://localhost:8080/datastore?key=" + id + "&kind=Kind");
         JsonObject entity = (JsonObject) new JsonParser().parse(response.getAsString());
 
         assertEquals("POST - value", entity.get("value").getAsString());
         return id;
     }
 
     private void assertGet(long id) {
         Response response = HttpUtils
                 .get("http://localhost:8080/services/DatastoreTest.js?key=656565656565656565&kind=Kind");
         assertEquals("null", response.getAsString());
 
         response = HttpUtils.get("http://localhost:8080/services/DatastoreTest.js?key=" + id + "&kind=Kind");
         JsonObject resp = (JsonObject) new JsonParser().parse(response.getAsString());
         assertEquals("POST - value", resp.get("value").getAsString());
         assertEquals(1, resp.get("numberValue").getAsInt());
         assertEquals(0.1d, resp.get("decimalValue").getAsDouble(), 0);
 
         // embed id
         assertNotNull(resp.get("_id"));
         assertEquals(id, resp.get("_id").getAsLong());
 
         JsonArray asJsonArray = resp.get("values").getAsJsonArray();
         assertEquals(1, asJsonArray.get(0).getAsInt());
         assertEquals(2, asJsonArray.get(1).getAsInt());
         assertEquals(3, asJsonArray.get(2).getAsInt());
 
         assertTrue(resp.get("valid").getAsBoolean());
         assertTrue(resp.get("date").getAsString().startsWith("2013-01-09"));
 
         response = HttpUtils.get("http://localhost:8080/datastore?key=" + id + "&kind=Kind");
         JsonObject dsObj = (JsonObject) new JsonParser().parse(response.getAsString());
 
         assertNull(dsObj.get("_id"));
         assertTrue(dsObj.get("numberValue").isJsonPrimitive());
         assertTrue(dsObj.get("decimalValue").isJsonPrimitive());
         assertTrue(dsObj.get("valid").isJsonPrimitive());
         assertTrue(dsObj.get("values").isJsonArray());
         assertTrue(dsObj.get("date").isJsonPrimitive());
     }
 
     private void assertPut() throws UnsupportedEncodingException {
         Response response = HttpUtils.put("http://localhost:8080/services/DatastoreTest.js?key=5");
 
         JsonObject obj = (JsonObject) new JsonParser().parse(response.getAsString());
         assertEquals(5, obj.get("_id").getAsLong());
 
         response = HttpUtils.get("http://localhost:8080/datastore?key=5&kind=Kind");
         JsonObject dsObj = (JsonObject) new JsonParser().parse(response.getAsString());
         assertEquals("PUT - value", dsObj.get("value").getAsString());
 
         // Updated
         obj.addProperty("value", "PUT - value updated");
         obj.addProperty("numberValue", 3);
         response = HttpUtils.put("http://localhost:8080/services/DatastoreTest.js?data="
                 + URLEncoder.encode(obj.toString(), "UTF-8"));
 
         response = HttpUtils.get("http://localhost:8080/datastore?key=5&kind=Kind");
         obj = (JsonObject) new JsonParser().parse(response.getAsString());
         assertEquals("PUT - value updated", obj.get("value").getAsString());
         assertEquals(3, obj.get("numberValue").getAsInt());
 
         // From, put from db
         response = HttpUtils.put("http://localhost:8080/services/DatastoreTest.js?fromDB=true");
         assertEquals(200, response.getStatus());
 
         // Error
         response = HttpUtils.put("http://localhost:8080/services/DatastoreTest.js?error=true&data="
                 + URLEncoder.encode(obj.toString(), "UTF-8"));
         assertEquals(500, response.getStatus());
     };
 
     private void assertNoFilter(boolean b) {
         Response resp = null;
         if (b) {
             resp = HttpUtils.get("http://localhost:8080/services/DatastoreTest.js?query10=true");
         } else {
             resp = HttpUtils.get("http://localhost:8080/services/DatastoreTest.js?query10=true&builder=true");
         }
 
         JsonArray result = (JsonArray) new JsonParser().parse(resp.getAsString());
         assertEquals(3, result.size());
 
         JsonObject asJsonObject = result.get(0).getAsJsonObject();
         assertEquals(0, asJsonObject.get("number").getAsInt());
         assertEquals(2, asJsonObject.get("_id").getAsInt());
 
         asJsonObject = result.get(1).getAsJsonObject();
         assertEquals(1, asJsonObject.get("number").getAsInt());
         assertEquals(3, asJsonObject.get("_id").getAsInt());
 
         asJsonObject = result.get(2).getAsJsonObject();
         assertEquals(2, asJsonObject.get("number").getAsInt());
         assertEquals(4, asJsonObject.get("_id").getAsInt());
     }
 
     private void assertRange(boolean b) {
         Response resp = null;
         if (b) {
             resp = HttpUtils.get("http://localhost:8080/services/DatastoreTest.js?query9=true");
         } else {
             resp = HttpUtils.get("http://localhost:8080/services/DatastoreTest.js?query9=true&builder=true");
         }
 
         JsonArray result = (JsonArray) new JsonParser().parse(resp.getAsString());
 
         assertEquals(1, result.size());
 
         JsonObject asJsonObject = result.get(0).getAsJsonObject();
         assertEquals(1, asJsonObject.get("number").getAsInt());
     }
 
     private void assertMultiple(boolean b) {
         Response resp = null;
         if (b) {
             resp = HttpUtils.get("http://localhost:8080/services/DatastoreTest.js?query8=true");
         } else {
             resp = HttpUtils.get("http://localhost:8080/services/DatastoreTest.js?query8=true&builder=true");
         }
 
         JsonArray result = (JsonArray) new JsonParser().parse(resp.getAsString());
 
         assertEquals(1, result.size());
 
         JsonObject asJsonObject = result.get(0).getAsJsonObject();
         assertEquals(1, asJsonObject.get("number").getAsInt());
     }
 
     private void assertIn(boolean b) {
         Response resp = null;
         if (b) {
             resp = HttpUtils.get("http://localhost:8080/services/DatastoreTest.js?query7=true");
         } else {
             resp = HttpUtils.get("http://localhost:8080/services/DatastoreTest.js?query7=true&builder=true");
         }
 
         JsonArray result = (JsonArray) new JsonParser().parse(resp.getAsString());
 
         assertEquals(2, result.size());
 
         JsonObject asJsonObject = result.get(0).getAsJsonObject();
         assertEquals(0, asJsonObject.get("number").getAsInt());
         asJsonObject = result.get(1).getAsJsonObject();
         assertEquals(1, asJsonObject.get("number").getAsInt());
     }
 
     private void assertNotEqual(boolean b) {
         Response resp = null;
         if (b) {
             resp = HttpUtils.get("http://localhost:8080/services/DatastoreTest.js?query6=true");
         } else {
             resp = HttpUtils.get("http://localhost:8080/services/DatastoreTest.js?query6=true&builder=true");
         }
 
         JsonArray result = (JsonArray) new JsonParser().parse(resp.getAsString());
 
         assertEquals(2, result.size());
 
         JsonObject asJsonObject = result.get(0).getAsJsonObject();
         assertEquals(0, asJsonObject.get("number").getAsInt());
         asJsonObject = result.get(1).getAsJsonObject();
         assertEquals(2, asJsonObject.get("number").getAsInt());
     }
 
     private void assertEqual(boolean b) {
         Response resp = null;
         if (b) {
             resp = HttpUtils.get("http://localhost:8080/services/DatastoreTest.js?query5=true");
         } else {
             resp = HttpUtils.get("http://localhost:8080/services/DatastoreTest.js?query5=true&builder=true");
         }
 
         JsonArray result = (JsonArray) new JsonParser().parse(resp.getAsString());
 
         assertEquals(1, result.size());
 
         JsonObject asJsonObject = result.get(0).getAsJsonObject();
         assertEquals(0, asJsonObject.get("number").getAsInt());
     }
 
     private void assertGreaterOtEqualsThan(boolean b) {
         Response resp = null;
         if (b) {
             resp = HttpUtils.get("http://localhost:8080/services/DatastoreTest.js?query4=true");
         } else {
             resp = HttpUtils.get("http://localhost:8080/services/DatastoreTest.js?query4=true&builder=true");
         }
 
         JsonArray result = (JsonArray) new JsonParser().parse(resp.getAsString());
 
         assertEquals(3, result.size());
 
         JsonObject asJsonObject = result.get(0).getAsJsonObject();
         assertEquals(0, asJsonObject.get("number").getAsInt());
         asJsonObject = result.get(1).getAsJsonObject();
         assertEquals(1, asJsonObject.get("number").getAsInt());
         asJsonObject = result.get(2).getAsJsonObject();
         assertEquals(2, asJsonObject.get("number").getAsInt());
     }
 
     private void assertGreatThan(boolean b) {
         Response resp = null;
         if (b) {
             resp = HttpUtils.get("http://localhost:8080/services/DatastoreTest.js?query3=true");
         } else {
             resp = HttpUtils.get("http://localhost:8080/services/DatastoreTest.js?query3=true&builder=true");
         }
 
         JsonArray result = (JsonArray) new JsonParser().parse(resp.getAsString());
 
         assertEquals(2, result.size());
 
         JsonObject asJsonObject = result.get(0).getAsJsonObject();
         assertEquals(1, asJsonObject.get("number").getAsInt());
         asJsonObject = result.get(1).getAsJsonObject();
         assertEquals(2, asJsonObject.get("number").getAsInt());
     }
 
     private void assertLessThanOrEquals(boolean b) {
         Response resp = null;
         if (b) {
             resp = HttpUtils.get("http://localhost:8080/services/DatastoreTest.js?query2=true");
         } else {
             resp = HttpUtils.get("http://localhost:8080/services/DatastoreTest.js?query2=true&builder=true");
         }
 
         JsonArray result = (JsonArray) new JsonParser().parse(resp.getAsString());
 
         assertEquals(3, result.size());
 
         JsonObject asJsonObject = result.get(0).getAsJsonObject();
         assertEquals(0, asJsonObject.get("number").getAsInt());
         asJsonObject = result.get(1).getAsJsonObject();
         assertEquals(1, asJsonObject.get("number").getAsInt());
         asJsonObject = result.get(2).getAsJsonObject();
         assertEquals(2, asJsonObject.get("number").getAsInt());
     }
 
     private void assertLessThan(boolean b) {
         Response resp = null;
         if (b) {
             resp = HttpUtils.get("http://localhost:8080/services/DatastoreTest.js?query1=true");
         } else {
             resp = HttpUtils.get("http://localhost:8080/services/DatastoreTest.js?query1=true&builder=true");
         }
 
         JsonArray result = (JsonArray) new JsonParser().parse(resp.getAsString());
 
         assertEquals(2, result.size());
 
         JsonObject asJsonObject = result.get(0).getAsJsonObject();
         assertEquals(0, asJsonObject.get("number").getAsInt());
         asJsonObject = result.get(1).getAsJsonObject();
         assertEquals(1, asJsonObject.get("number").getAsInt());
     }
 
     private void createSortMockEntities() {
         JsonObject data = new JsonObject();
         data.addProperty("number", 0);
         data.addProperty("number2", 1);
 
         Map<String, String> params = new HashMap<String, String>();
         params.put("kind", "Mock2");
         params.put("data", data.toString());
         HttpUtils.post("http://localhost:8080/datastore", params);
 
         data = new JsonObject();
         data.addProperty("number", 1);
         data.addProperty("number2", 1);
 
         params = new HashMap<String, String>();
         params.put("kind", "Mock2");
         params.put("data", data.toString());
         HttpUtils.post("http://localhost:8080/datastore", params);
 
         data = new JsonObject();
         data.addProperty("number", 2);
         data.addProperty("number2", 2);
 
         params = new HashMap<String, String>();
         params.put("kind", "Mock2");
         params.put("data", data.toString());
         HttpUtils.post("http://localhost:8080/datastore", params);
 
         data = new JsonObject();
         data.addProperty("number", 3);
         data.addProperty("number2", 1);
 
         params = new HashMap<String, String>();
         params.put("kind", "Mock2");
         params.put("data", data.toString());
         HttpUtils.post("http://localhost:8080/datastore", params);
     }
 
     private void createQueryMockEntities() {
         for (int i = 0; i < 3; i++) {
             JsonObject data = new JsonObject();
             data.addProperty("number", i);
             data.addProperty("string", "test" + i);
 
             Map<String, String> params = new HashMap<String, String>();
             params.put("kind", "Mock");
             params.put("data", data.toString());
             HttpUtils.post("http://localhost:8080/datastore", params);
         }
 
     }
 }
