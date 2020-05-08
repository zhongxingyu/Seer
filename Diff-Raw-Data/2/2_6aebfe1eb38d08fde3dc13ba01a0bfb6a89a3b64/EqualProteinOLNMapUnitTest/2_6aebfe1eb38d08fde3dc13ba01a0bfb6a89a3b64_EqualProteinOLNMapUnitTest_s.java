 import au.org.intersect.samifier.domain.EqualProteinOLNMap;
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 public class EqualProteinOLNMapUnitTest
 {
     EqualProteinOLNMap map = null;
 
     @Before
     public void oneTimeSetup()
     {
         map = new EqualProteinOLNMap();
     }
 
     @Test
     public void testContainsProtein()
     {
         assertTrue(map.containsProtein("one"));
     }
 
     @Test
     public void testMapsCorrectlr()
     {
         assertEquals("one", map.getOLN("one"));
     }
 }
