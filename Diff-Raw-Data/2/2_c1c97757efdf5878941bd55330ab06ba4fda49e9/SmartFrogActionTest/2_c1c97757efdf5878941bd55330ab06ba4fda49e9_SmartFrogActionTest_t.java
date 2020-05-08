 package builder.smartfrog;
 
 import static org.junit.Assert.assertEquals;
 import org.junit.Test;
 
 public class SmartFrogActionTest {
     
     @Test
     public void encodeIPv6Hostname() {
         SmartFrogAction sfa = new SmartFrogAction(null, "::1");
        assertEquals("console-%3A%3A1", sfa.getUrlName());
     }
 
 }
