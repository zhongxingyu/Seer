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
   public static final String WEBSERVICE_URL = "http://localhost:8889/seqware-queryengine-webservice/api/";
   public static String setKey;
   
   public ReadSetResourceTest() {
   }
   
   @BeforeClass
   public static void setUpClass() {
     //Create a Test ReadSet
    Client client = Client.create();
    /*WebResource webResource = client.resource(WEBSERVICE_URL + "readset" );
     String readSet = "{"
         + "\"description\": \"TestReadSet\""
         + "}";
     ClientResponse response = webResource.type("application/json").post(ClientResponse.class, readSet);
     Assert.assertTrue("Request failed: " + response.getStatus(), response.getStatus() == 200);
     String output = response.getEntity(String.class);
    setKey = extractRowKey(output); */
     client.destroy();
   }
   
   @AfterClass
   public static void tearDownClass() {
     Client client = Client.create();
     //WebResource webResource = client.resource(WEBSERVICE_URL + "readset/" + setKey);
     //webResource.delete();
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
     WebResource webResource = client.resource(WEBSERVICE_URL + "readset");
     ClientResponse response = webResource.type("application/json").get(ClientResponse.class);
     Assert.assertTrue("Request failed: " + response.getStatus(), response.getStatus() == 200);
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
