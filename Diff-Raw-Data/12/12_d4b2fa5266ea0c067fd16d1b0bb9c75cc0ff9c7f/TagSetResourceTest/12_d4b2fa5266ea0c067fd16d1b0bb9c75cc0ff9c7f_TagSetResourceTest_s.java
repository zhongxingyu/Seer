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
 
 import com.github.seqware.queryengine.model.TagSet;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 
 public class TagSetResourceTest {
   public static final String WEBSERVICE_URL = "http://localhost:8889/seqware-queryengine-webservice/api/";
 
   public TagSetResourceTest() {
   }
   
   @BeforeClass
   public static void setUpClass() {
   }
   
   @AfterClass
   public static void tearDownClass() {
   }
   
   @Before
   public void setUp() {
   }
   
   @After
   public void tearDown() {
   }
 
   @Test
   public void testGetClassName() {
     TagSetResource instance = new TagSetResource();
     String expResult = "TagSet";
     String result = instance.getClassName();
     assertEquals(expResult, result);
   }
 
   /**
    * Test of getModelClass method, of class TagSetResource.
    */
   @Test
   public void testGetModelClass() {
     TagSetResource instance = new TagSetResource();
     Class expResult = TagSet.class;
     Class result = instance.getModelClass();
     assertEquals(expResult, result);
   }
 
   @Test
   public void testGetElements() {
     Client client = Client.create();
     WebResource webResource = client.resource(WEBSERVICE_URL + "tagset");
     ClientResponse response = webResource.type("application/json").get(ClientResponse.class);
     Assert.assertTrue("Request failed:" + response.getStatus(), response.getStatus() == 200);
     String output = response.getEntity(String.class);
     Assert.assertTrue("Request entity incorrect: " + output, output!=null);
   }
   
   @Test
   public void testAddSet() {
     Client client = Client.create();
     WebResource webResource = client.resource(WEBSERVICE_URL + "tagset");
     String tagset = "{\n"
             + "  \"name\": \"Funky TagSet\"\n"
             + "}";
     ClientResponse response = webResource.type("application/json").post(ClientResponse.class, tagset);
     Assert.assertTrue("Request failed: " + response.getStatus(), response.getStatus() == 200);
     String output = response.getEntity(String.class);
     System.out.println(output);
     String rowkey = extractRowKey(output);
     
    Assert.assertTrue("Returned entity incorrect" + output, output.contains("Funky name") && output.contains("Funky TagSet"));
     client.resource(WEBSERVICE_URL + "tagset").delete(rowkey);
     ClientResponse response2 = webResource.type("application/json").get(ClientResponse.class);
     Assert.assertTrue("Request failed: " + response2.getStatus(), response2.getStatus() == 200);
     String output2 = response2.getEntity(String.class);
     Assert.assertTrue("Could not delete entity:" + response.getStatus(), !output2.contains(rowkey));
     }
   
   @Test
   public void testGetVersion() {
     Client client = Client.create();
     WebResource webResource = client.resource(WEBSERVICE_URL + "tagset");
     String tagset = "{\n"
             + "  \"name\": \"TagSet Version Test\"\n"
             + "}";
     ClientResponse response = webResource.type("application/json").post(ClientResponse.class, tagset);
     Assert.assertTrue("Request failed: " + response.getStatus(), response.getStatus() == 200);
     String output = response.getEntity(String.class);
     System.out.println(output);
     String rowkey = extractRowKey(output);
     
     WebResource webResource2 = client.resource(WEBSERVICE_URL + "tagset/" + rowkey + "/version");
     ClientResponse response2 = webResource2.type("application/json").get(ClientResponse.class);
     Assert.assertTrue("Request failed: " + response2.getStatus(), response.getStatus() == 200);
    String version = extractVersion(response2.getEntity(String.class));
     System.out.println(version);
     Assert.assertTrue("Invalid Version returned: " + version, Integer.parseInt(version)==1);
     webResource.delete(rowkey);
   }
   
   
   protected String extractRowKey(String output) {
     // now create a Tag using the returned rowkey
     // grab rowkey via regular expression
     Pattern pattern = Pattern.compile("rowKey\":\"(.*?)\"");
     Matcher matcher = pattern.matcher(output);
     matcher.find();
     String rowkey = matcher.group(1);
     return rowkey;
   }
   
   protected String extractVersion(String output) {
     // grab version via regular expression
     Pattern pattern = Pattern.compile("version\":\"(.*?)\"");
     Matcher matcher = pattern.matcher(output);
     matcher.find();
    String rowkey = matcher.group(1);
    return rowkey;
   }
 }
