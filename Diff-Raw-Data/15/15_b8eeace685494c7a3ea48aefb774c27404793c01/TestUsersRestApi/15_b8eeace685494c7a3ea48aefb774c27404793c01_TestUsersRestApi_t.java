 package org.hackystat.sensorbase.resource.users;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import org.hackystat.sensorbase.resource.users.jaxb.Properties;
 import org.hackystat.sensorbase.resource.users.jaxb.Property;
 import org.hackystat.sensorbase.resource.users.jaxb.User;
 import org.hackystat.sensorbase.server.ServerProperties;
 import org.hackystat.sensorbase.test.SensorBaseRestApiHelper;
 
 import static org.hackystat.sensorbase.server.ServerProperties.TEST_DOMAIN_KEY;
 import org.junit.Test;
 import org.restlet.data.MediaType;
 import org.restlet.data.Method;
 import org.restlet.data.Response;
 import org.restlet.resource.DomRepresentation;
 import org.restlet.resource.Representation;
 import org.restlet.resource.XmlRepresentation;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 /**
  * Tests the SensorBase REST API for Users and User resources.
  * @author Philip M. Johnson
  */
 public class TestUsersRestApi extends SensorBaseRestApiHelper {
 
   /** The TestUser user email. */
   private String testUserEmail = "TestUser@hackystat.org";
   /** Here because PMD thinks this way is better. */
   private String usersUri = "users/";
   /** The URI string for the TestUser user. */
   private String testUserUri = usersUri + testUserEmail; 
 
   /**
    * Test that GET host/sensorbase/users returns an index containing TestUser.
    * @throws Exception If problems occur.
    */
   @Test public void getUsersIndex() throws Exception {
     Response response = makeAdminRequest(Method.GET, usersUri);
 
     // Test that the request was received and processed by the server OK. 
     assertTrue("Unsuccessful GET index", response.getStatus().isSuccess());
 
     // Ensure that we can find the SampleSdt definition.
     XmlRepresentation data = response.getEntityAsSax();
     Node node = data.getNode("//UserRef[@Email='" + testUserEmail + "']");
     assertNotNull("Failed to find " + testUserEmail, node);
   }
   
   /**
    * Test that GET host/sensorbase/users/TestUser returns the TestUser test user. 
    * @throws Exception If problems occur.
    */
   @Test public void getUser() throws Exception {
     Response response = makeRequest(Method.GET, testUserUri, testUserEmail);
 
     // Test that the request was received and processed by the server OK. 
     assertTrue("Unsuccessful GET TestUser", response.getStatus().isSuccess());
     DomRepresentation data = response.getEntityAsDom();
     assertEquals("Failed to find email", testUserEmail, data.getText("User/Email"));
     
     //Make it into a Java SDT and ensure the fields are there as expected. 
     User user = UserManager.unmarshallUser(data.getDocument());
     assertEquals("Bad email", testUserEmail, user.getEmail());
     assertEquals("Bad password", testUserEmail, user.getPassword());
   }
   
   /**
   * Test that HEAD host/sensorbase/users/TestUser provides a lightweight way
   * to check authentication credentials. 
   * @throws Exception If problems occur.
   */
  @Test public void getUserHead() throws Exception {
    Response response = makeRequest(Method.HEAD, testUserUri, testUserEmail);
    // Test that the request was received and processed by the server OK. 
    assertTrue("Unsuccessful HEAD TestUser", response.getStatus().isSuccess());
    // Now check for bad credentials.
    response = makeRequest(Method.HEAD, testUserUri, "unknownuser");
    // Test that the request was received and processed by the server OK. 
    assertTrue("Successful Bad HEAD TestUser", response.getStatus().isClientError());
  }
  
  /**
    * Tests the POST method that registers a new user. 
    * @throws Exception If problems occur.
    */
   @Test public void postUser() throws Exception {
     String testEmail = "TestPost@" + ServerProperties.get(TEST_DOMAIN_KEY);
     Response response = makeRequest(Method.POST, "users?email=" + testEmail, null);
 
     // Test that the request was received and processed by the server OK. 
     assertTrue("Unsuccessful POST of TestPost@hackystat.org", response.getStatus().isSuccess());
 
     // Test that we can now retrieve this user with a GET.
     response = makeRequest(Method.GET, usersUri + testEmail, testEmail);
     assertTrue("Unsuccessful GET TestPost", response.getStatus().isSuccess());
     DomRepresentation data = response.getEntityAsDom();
     assertEquals("Couldn't find TestPost user", testEmail, data.getText("User/Email"));
   }
   
   /**
    * Tests that a user can be deleted after creation. 
    * @throws Exception If problems occur. 
    */
   @Test public void deleteUser () throws Exception {
     String testEmail = "TestPost@" + ServerProperties.get(TEST_DOMAIN_KEY);
     String testEmailUri = usersUri + testEmail;
     Response response = makeRequest(Method.POST, "users?email=" + testEmail, testUserEmail);
 
     // Test that the request was received and processed by the server OK. 
     assertTrue("Unsuccessful POST of TestPost", response.getStatus().isSuccess());
     
     // Now try to delete
     response = makeRequest(Method.DELETE, testEmailUri, testEmail);
     
     // Test that it was processed OK.
     assertTrue("Unsuccessful DELETE of TestPost", response.getStatus().isSuccess());
     
     //Ensure that TestPost is no longer found as a user.
     response = makeRequest(Method.GET, testEmailUri, testEmail);
     assertFalse("Illegal GET of TestPost", response.getStatus().isSuccess());
     
     // Ensure that TestPost is not listed in the index.
     response = makeAdminRequest(Method.GET, "users");
     assertTrue("Unsuccessful GET index", response.getStatus().isSuccess());
 
     // Ensure that we can't find the TestPost definition.
     XmlRepresentation data = response.getEntityAsSax();
     Node node = data.getNode("//UserRef[@Email='" + testEmail + "']");
     assertNull("Incorrectly found TestPost user", node);
   }
   
   /**
    * Tests that a user can have their properties updated. 
    * @throws Exception If problems occur. 
    */
   @Test public void postUserProperties () throws Exception {
     // Create (or recreate) the TestPost user
     String testEmail = "TestPost@" + ServerProperties.get(TEST_DOMAIN_KEY);
     String testEmailUri = usersUri + testEmail;
     Response response = makeRequest(Method.POST, "users?email=" + testEmail, null);
 
     // Test that the request was received and processed by the server OK. 
     assertTrue("Unsuccessful POST of TestPost", response.getStatus().isSuccess());
     
     // Now create a properties object and post it.
     Properties properties = new Properties();
     Property property = new Property();
     property.setKey("testKey");
     property.setValue("testValue");
     properties.getProperty().add(property);
     Document doc = UserManager.marshallProperties(properties);
     Representation representation = new DomRepresentation(MediaType.TEXT_XML, doc);
 
     response = makeRequest(Method.POST, testEmailUri, testEmail, representation);
 
     // Test that the POST request was received and processed by the server OK. 
     assertTrue("Unsuccessful POST TestPost", response.getStatus().isSuccess());
     
     // Retrieve the User representation and see that this property is there. 
     response = makeRequest(Method.GET, testEmailUri, testEmail, representation);
     DomRepresentation data = response.getEntityAsDom();
     assertEquals("Bad key", "testKey", data.getText("//User/Properties/Property/Key"));
   }
 }
