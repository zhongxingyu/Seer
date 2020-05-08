 package gov.usgs.cida.gdp.communication;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  *
  * @author isuftin
  */
 public class EmailHandlerTest {
 
     private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EmailHandlerTest.class);
 
     @BeforeClass
     public static void setUpBeforeClass() throws Exception {
         log.debug("Started testing class.");
     }
 
     @AfterClass
     public static void tearDownAfterClass() throws Exception {
         log.debug("Ended testing class.");
     }
 
     /**
      * Test of sendMessage method, of class EmailHandler.
      */
     @Test
     public void testSendMessageWithBcc() throws Exception {
         List<String> bcc = new ArrayList<String>();
         bcc.add("test@test.ing.gov");
         bcc.add("test@test.ing.gov");
         EmailMessage message = new EmailMessage("test@test.doesnt.exist.gov", "test@testing.purposes.on.ly.gov", bcc, bcc, "Test", "Test");
 
         try {
             EmailHandler.sendMessage(message);
         } catch (Exception e) {
             assert(false);
         }
 
         assert(true);
     }
 
     /**
      * Test of sendMessage method, of class EmailHandler.
      */
     @Test
     public void testSendMessageWithCc() throws Exception {
         List<String> bcc = new ArrayList<String>();
         bcc.add("test@test.ing.gov");
         bcc.add("test@test.ing.gov");
         EmailMessage message = new EmailMessage("test@test.doesnt.exist.gov", "test@testing.purposes.on.ly.gov", bcc, "Test", "Test");
 
         try {
             EmailHandler.sendMessage(message);
         } catch (Exception e) {
             assert(false);
         }
 
         assert(true);
     }
 }
