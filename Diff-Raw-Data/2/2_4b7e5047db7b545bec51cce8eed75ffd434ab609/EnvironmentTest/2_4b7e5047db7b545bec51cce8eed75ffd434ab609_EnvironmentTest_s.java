 import junit.framework.TestCase;
 import org.junit.Test;
 
 import static org.junit.Assert.assertTrue;
 
 public class EnvironmentTest{
 
     @Test
     public void ShouldAlwaysBeTrue() {
         assertTrue(1 == 1);
     }
     
     @Test
     public void ShouldAlwaysBeFalse() {
        assertTrue(1 == 2);
     }
 
 }
