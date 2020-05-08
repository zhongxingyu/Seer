 import org.junit.Test;
 
 import static junit.framework.Assert.assertEquals;
 
 public class HelloTest {
     @Test
     public void testDaShit() {
        assertEquals("World", new Hello().greeting());
     }
 }
