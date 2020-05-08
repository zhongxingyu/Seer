 package dk.kb.yggdrasil.json;
 
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.assertFalse;
 
 import java.util.UUID;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 
 @RunWith(JUnit4.class)
 public class PreservationRequestTest {
 
     @Test
     public void testIsInvalidMessage() {
         PreservationRequest pr = new PreservationRequest();
         pr.UUID = UUID.randomUUID().toString();
         pr.Preservation_profile = "simple";
         pr.Update_URI = "http://localhost/update";
         assertTrue(pr.isMessageValid());
         pr.Update_URI = null;
         assertFalse(pr.isMessageValid());
     }
 
 }
