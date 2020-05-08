 package bbn.api.sms;
 
 import bbn.api.sms.domain.Result;
 import bbn.api.sms.domain.SMS;
 import bbn.api.sms.domain.SmsGroup;
 import java.io.IOException;
 import java.util.Arrays;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author Victor Igbokwe (vicsstar@yahoo.com)
  * @created Sep 23, 2012 at 5:20:59 AM
  */
 public class BbnApiTest {
 
   public BbnApiTest() {
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
 
   /**
    * Test of getInstance method, of class BbnApi.
    */
   @Test
   public void testGetInstance_0args() throws Exception {
     System.out.println("getInstance");
     BbnApi expResult = BbnApi.getInstance();
     assertNotNull(expResult);
   }
 
   /**
    * Test of defaultInstance method, of class BbnApi.
    */
   @Test
   public void testDefaultInstance() {
     System.out.println("defaultInstance");
     BbnApi result = BbnApi.defaultInstance();
     assertNotNull(result);
   }
 
   /**
    * Test of addConfig method, of class BbnApi.
    */
   @Test
   public void testAddConfig() throws IOException {
     System.out.println("addConfig");
     String key = "username";
     String value = "bbnapi_user";
     BbnApi api = BbnApi.getInstance();
     api.addConfig(key, value);
   }
 
   /**
    * Test of getConfig method, of class BbnApi.
    */
   @Test
   public void testGetConfig() throws IOException {
     System.out.println("getConfig");
     BbnApi api = BbnApi.getInstance();
     assertEquals("bbnapi_user", api.getConfig("username"));
   }
 
   /**
    * Test of getConfigAsBoolean method, of class BbnApi.
    */
   @Test
   public void testGetConfigBoolean() throws IOException {
     System.out.println("getConfigBoolean");
     String key = "bbn.api.secure";
     BbnApi instance = BbnApi.getInstance();
     Boolean expResult = false;
     Boolean result = instance.getConfigAsBoolean(key);
     assertEquals(expResult, result);
   }
 
   /**
    * Test of login method, of class BbnApi.
    */
   @Test
   public void testLogin() throws Exception {
     System.out.println("login");
     String username = "user123";
     String password = "password123";
     BbnApi api = BbnApi.getInstance();
     Result<Boolean> result = api.login(username, password);
     assertEquals(false, result.getValue());
 
     username = "blessingkalu@gmail.com";
     password = "finalmission20";
     result = api.login(username, password);
     System.out.println("Found: " + result.getMessage());
     System.out.println("\"" + result.getMessage() + "\"");
     assertEquals(true, result.getValue());
   }
 
   /**
    * Test of checkBalance method, of class BbnApi.
    */
   @Test
   public void testCheckBalance() throws Exception {
     System.out.println("checkBalance");
     BbnApi api = BbnApi.getInstance();
     String username = "blessingkalu@gmail.com";
     String password = "finalmission20";
     Result<Float> result = api.checkBalance(username, password);
     System.out.println(result.getValue());
    assertNull(result.getValue());
   }
 
   /**
    * Test of sendMessage method, of class BbnApi.
    */
   @Test
   public void testSendMessage_SMS() throws Exception {
     System.out.println("sendMessage");
     SMS sms = new SMS();
     sms.setMessage("This is a test message.");
     sms.setSender("victor");
     sms.setMobile("+2347031234567");
     BbnApi api = BbnApi.getInstance();
     api.setUsername("blessingkalu@gmail.com");
     api.setPassword("finalmission20");
     Result result = api.sendMessage(sms);
     System.out.println("Message sent: " + result.getValue());
    assertTrue(result.isError());
   }
 
   /**
    * Test of sendBatchMessage method, of class BbnApi.
    */
   @Test
   public void testSendBatchMessage_SmsGroup() throws Exception {
     System.out.println("sendBatchMessage");
     SmsGroup smsGroup = new SmsGroup();
     smsGroup.setMessage("This is a group message.");
     smsGroup.setSender("victor");
    smsGroup.setMobileList(Arrays.asList(new String[]{"+2347031234567"}));
     BbnApi api = BbnApi.getInstance();
    Result result = api.sendBatchMessage(smsGroup);
     assertTrue(result.isError());
   }
 }
