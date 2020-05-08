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
 
 import com.github.seqware.queryengine.model.FeatureSet;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 
 public class FeatureSetResourceTest {
   public static final String WEBSERVICE_URL = "http://localhost:8889/seqware-queryengine-webservice/api/";
   public static String setKey;
   
   public FeatureSetResourceTest() {
   }
   
   @BeforeClass
   public static void setUpClass() {
     //Create a Test FeatureSet
     Client client = Client.create();
     WebResource webResource = client.resource(WEBSERVICE_URL + "featureset" );
     String featureSet = "{"
         + "\"description\": \"TestFeatureSet\""
         + "}";
     ClientResponse response = webResource.type("application/json").post(ClientResponse.class, featureSet);
     Assert.assertTrue("Request failed: " + response.getStatus(), response.getStatus() == 200);
     String output = response.getEntity(String.class);
     setKey = extractRowKey(output);
     client.destroy();
   }
   
   @AfterClass
   public static void tearDownClass() {
     Client client = Client.create();
     WebResource webResource = client.resource(WEBSERVICE_URL + "featureset/" + setKey);
     webResource.delete();
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
     FeatureSetResource instance = new FeatureSetResource();
     String expResult = "FeatureSet";
     String result = instance.getClassName();
     assertEquals(expResult, result);
   }
 
   /**
    * Test of getModelClass method, of class FeatureSetResource.
    */
   @Test
   public void testGetModelClass() {
     FeatureSetResource instance = new FeatureSetResource();
     Class expResult = FeatureSet.class;
     Class result = instance.getModelClass();
     assertEquals(expResult, result);
   }
   
   // GET featureset
   @Test
   public void testGetFeatureSets() {
     Client client = Client.create();
     WebResource webResource = client.resource(WEBSERVICE_URL + "featureset");
     ClientResponse response = webResource.type("application/json").get(ClientResponse.class);
     Assert.assertTrue("Request failed: " + response.getStatus(), response.getStatus() == 200);
     client.destroy();
   }
   
   // GET featureset/{sgid}
   @Test
   public void testGetFeatureSet() {
     Client client = Client.create();
    System.out.println(WEBSERVICE_URL + "featureset/" + setKey);
     WebResource webResource = client.resource(WEBSERVICE_URL + "featureset/" + setKey);
     ClientResponse response = webResource.type("application/json").get(ClientResponse.class);
     Assert.assertTrue("Request failed: " + response.getStatus(), response.getStatus() == 200);
     client.destroy();
   }
   
   
   // POST   featureset
   // PUT    featureset/{sgid}
   // DELETE featureset/{sgid}
   @Test
   public void testPutFeatureSet() {
     //Create a new FeatureSet
     Client client = Client.create();
     WebResource webResource = client.resource(WEBSERVICE_URL + "featureset");
     String featureSet = "{\n"
             + "  \"description\": \"TestPutFeatureSet\"\n"
             + "}";
     ClientResponse response = webResource.type("application/json").post(ClientResponse.class, featureSet);
     Assert.assertTrue("Request failed: " + response.getStatus(), response.getStatus() == 200);
     String output = response.getEntity(String.class);
     String rowkey = extractRowKey(output);
     Assert.assertTrue("Returned entity incorrect" + output, output.contains(rowkey) && output.contains("TestPutFeatureSet"));
     
     //Update the FeatureSet
     WebResource webResource2 = client.resource(WEBSERVICE_URL + "featureset/" + rowkey);
     String put = "{\n"
             + "  \"description\": \"ChangedDescription\"\n"
             + "}"; 
     ClientResponse response2 = webResource2.type("application/json").put(ClientResponse.class, put);
     Assert.assertTrue("Put failed:" + response.getStatus(), response2.getStatus() == 200);
     String output2 =  response2.getEntity(String.class);
     Assert.assertTrue("Output does not contain the PUT update: " + output2, output2.contains(rowkey) && output2.contains("ChangedDescription"));
     
     //Delete the FeatureSet
     webResource2.delete();
     client.destroy();
   }
   
   // GET featureset/{sgid}/tags
   @Test
   public void testGetTags() {
     Client client = Client.create();
     WebResource webResource = client.resource(WEBSERVICE_URL + "featureset/" + setKey + "/tags");
     ClientResponse response = webResource.type("application/json").get(ClientResponse.class);
     Assert.assertTrue("Request failed: " + response.getStatus(), response.getStatus() == 200);
     client.destroy();
   }
   
   // GET featureset/{sgid}/permissions
   @Test
   public void testGetPermissions() {
     Client client = Client.create();
     WebResource webResource = client.resource(WEBSERVICE_URL + "featureset/" + setKey + "/permissions");
     ClientResponse response = webResource.type("application/json").get(ClientResponse.class);
     Assert.assertTrue("Request failed: " + response.getStatus(), response.getStatus() == 200);
     client.destroy();
   }
   
   //GET featureset/{sgid}/version
   @Test
   public void testGetVersion() {
     Client client = Client.create();
     WebResource webResource = client.resource(WEBSERVICE_URL + "featureset/" + setKey + "/version");
     ClientResponse response = webResource.type("application/json").get(ClientResponse.class);
     Assert.assertTrue("Request failed: " + response.getStatus(), response.getStatus() == 200);
     String output = response.getEntity(String.class);
     String version = extractVersion(output);
     Assert.assertTrue("Invalid Version returned: " + version, Integer.parseInt(version)==1);
     client.destroy();
   }
   
   protected static String extractRowKey(String output) {
     Pattern pattern = Pattern.compile("rowKey\":\"(.*?)\"");
     Matcher matcher = pattern.matcher(output);
     matcher.find();
     String rowkey = matcher.group(1);
     return rowkey;
   }
   
   protected static String extractVersion(String output) {
     Pattern pattern = Pattern.compile("version\":(.*?)}");
     Matcher matcher = pattern.matcher(output);
     matcher.find();
     String version = matcher.group(1);
     return version;
   }
 }
