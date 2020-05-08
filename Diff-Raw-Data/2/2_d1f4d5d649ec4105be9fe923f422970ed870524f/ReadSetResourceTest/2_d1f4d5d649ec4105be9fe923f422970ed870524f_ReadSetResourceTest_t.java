 package com.github.seqware.queryengine.system.rest.resources;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.github.seqware.queryengine.model.ReadSet;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 
 public class ReadSetResourceTest {
   public static String setKey;
   public static String tagSetKey;
   
   public ReadSetResourceTest() {
   }
   
   @BeforeClass
   public static void setUpClass() {
     //Create a Test ReadSet
     Client client = Client.create();
     WebResource webResource = client.resource(QEWSResourceTestSuite.WEBSERVICE_URL + "readset" );
     String readSet = "{"
         + "\"readSetName\": \"ReadSetResourceTest\"}"
         + "}";
     ClientResponse response = webResource.type("application/json").post(ClientResponse.class, readSet);
     Assert.assertTrue("Request failed: " + response.getStatus(), response.getStatus() == 200);
     String output = response.getEntity(String.class);
     setKey = extractRowKey(output);
     client.destroy();
     
     //Create a TagSet for this test
    WebResource webResource2 = client.resource(QEWSResourceTestSuite.WEBSERVICE_URL + "tagset");
     String tagset = "{\n"
             + "  \"name\": \"TestReferenceSetTagSet\"\n"
             + "}";
     ClientResponse response2 = webResource2.type("application/json").post(ClientResponse.class, tagset);
     Assert.assertTrue("Request failed: " + response2.getStatus(), response2.getStatus() == 200);
     String output2 = response2.getEntity(String.class);
     tagSetKey = extractRowKey(output2);
   }
   
   @AfterClass
   public static void tearDownClass() {
     Client client = Client.create();
     WebResource webResource = client.resource(QEWSResourceTestSuite.WEBSERVICE_URL + "readset/" + setKey);
     webResource.delete();
     WebResource webResource2 = client.resource(QEWSResourceTestSuite.WEBSERVICE_URL + "tagset/" + tagSetKey);
     webResource2.delete();
     client.destroy();
   }
   
   @Before
   public void setUp() {
   }
   
   @After
   public void tearDown() {
   }
 
   @Test
   public void testGetClassName() {
     ReadSetResource instance = new ReadSetResource();
     String expResult = "ReadSet";
     String result = instance.getClassName();
     assertEquals(expResult, result);
   }
 
   /**
    * Test of getModelClass method, of class ReadSetResource.
    */
   @Test
   public void testGetModelClass() {
     ReadSetResource instance = new ReadSetResource();
     Class expResult = ReadSet.class;
     Class result = instance.getModelClass();
     assertEquals(expResult, result);
   }
   
   //GET readset
   @Test
   public void testGetReadSets() {
     Client client = Client.create();
     WebResource webResource = client.resource(QEWSResourceTestSuite.WEBSERVICE_URL + "readset");
     ClientResponse response = webResource.type("application/json").get(ClientResponse.class);
     Assert.assertTrue("Request failed: " + response.getStatus(), response.getStatus() == 200);
     client.destroy();
   }
   
   //GET readset/{sgid}
   @Test
   public void testGetReadSet() {
     Client client = Client.create();
     WebResource webResource = client.resource(QEWSResourceTestSuite.WEBSERVICE_URL + "readset/" + setKey);
     ClientResponse response = webResource.type("application/json").get(ClientResponse.class);
     Assert.assertTrue("Request failed: " + response.getStatus(), response.getStatus() == 200);
     client.destroy();
   }
   
   //PUT    readset/{sgid}
   //DELETE readset/{sgid}
   @Test
   public void testPutReadSet() {
     Client client = Client.create();
     String readset = "{\"readSetName\": \"TestPutReadSet\"}";
     WebResource webResource = client.resource(QEWSResourceTestSuite.WEBSERVICE_URL + "readset/");
     ClientResponse response = webResource.type("application/json").post(ClientResponse.class, readset);
     Assert.assertTrue("Request failed: " + response.getStatus(), response.getStatus() == 200);
     String rowKey = extractRowKey(response.getEntity(String.class));
     
     WebResource webResource2 = client.resource(QEWSResourceTestSuite.WEBSERVICE_URL + "readset/" + rowKey);
     String put = "{\"readSetName\": \"ChangedReadSet\"}";
     ClientResponse response2 = webResource2.type("application/json").put(ClientResponse.class, put);
     Assert.assertTrue("Request failed: " + response2.getStatus(), response2.getStatus() == 200);
     
     webResource2.delete();
     client.destroy();
   }
   
   //GET readset/{sgid}/tags
   @Test
   public void testGetTags() {
     Client client = Client.create();
     WebResource webResource = client.resource(QEWSResourceTestSuite.WEBSERVICE_URL + "readset/" + setKey + "/tags");
     ClientResponse response = webResource.type("application/json").get(ClientResponse.class);
     Assert.assertTrue("Request failed: " + response.getStatus(), response.getStatus() == 200);
     client.destroy();
   }
   
   //GET readset/{sgid}/version
   @Test
   public void testGetVersion() {
     Client client = Client.create();
     WebResource webResource = client.resource(QEWSResourceTestSuite.WEBSERVICE_URL + "readset/" + setKey + "/version");
     ClientResponse response = webResource.type("application/json").get(ClientResponse.class);
     Assert.assertTrue("Request failed: " + response.getStatus(), response.getStatus() == 200);
     client.destroy();
   }
   
   //GET readset/{sgid}/permissions
   @Test
   public void testGetPermissions() {
     Client client = Client.create();
     WebResource webResource = client.resource(QEWSResourceTestSuite.WEBSERVICE_URL + "readset/" + setKey + "/permissions");
     ClientResponse response = webResource.type("application/json").get(ClientResponse.class);
     Assert.assertTrue("Request failed: " + response.getStatus(), response.getStatus() == 200);
     client.destroy();
   }
   
   //PUT readset/{sgid}/tag
   //GET readset/tags
   @Test
   public void testPutTag() {
     Client client = Client.create();
     WebResource webResource = client.resource(QEWSResourceTestSuite.WEBSERVICE_URL + "readset/" + setKey + "/tag?tagset_id=" + tagSetKey + "&key=test");
     ClientResponse response = webResource.type("application/json").put(ClientResponse.class);
     Assert.assertTrue("Request failed: " + response.getStatus(), response.getStatus() == 200);
     
     WebResource webResource2 = client.resource(QEWSResourceTestSuite.WEBSERVICE_URL + "readset/tags?tagset_id=" + tagSetKey + "&key=test");
     ClientResponse response2 = webResource2.type("application/json").get(ClientResponse.class);
     Assert.assertTrue("Request failed: " + response2.getStatus(), response2.getStatus() == 200);
     client.destroy();
   }
  
   protected static String extractRowKey(String output) {
     Pattern pattern = Pattern.compile("rowKey\":\"(.*?)\"");
     Matcher matcher = pattern.matcher(output);
     matcher.find();
     String rowkey = matcher.group(1);
     return rowkey;
   }  
 }
