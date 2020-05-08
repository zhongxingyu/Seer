 package gov.usgs.cida.coastalhazards.rest.data;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author jordan
  */
 public class SLDResourceTest {
    
     /**
      * Test of getRedWhiteSLD method, of class SLDResource.
      */
     @Test
     public void testMakeSteps2() {
         float[] steps = SLDResource.makeSteps(0, 10, 2);
         assertEquals(0, steps[0], 0.01);
         assertEquals(10, steps[1], 0.01);
     }
     
     /**
      * Test of getRedWhiteSLD method, of class SLDResource.
      */
     @Test
     public void testMakeSteps4() {
         float[] steps = SLDResource.makeSteps(0, 10, 4);
         assertEquals(0, steps[0], 0.01);
         assertEquals(3.33, steps[1], 0.01);
         assertEquals(6.66, steps[2], 0.01);
         assertEquals(10, steps[3], 0.01);
     }
     
     @Test
     public void testMakeColors2() {
         String[] steps = SLDResource.makeColors("#ffffff", "#ff0000", 2);
         assertEquals("#FFFFFF", steps[0]);
         assertEquals("#FF0000", steps[1]);
     }
     
     @Test
     public void testMakeColors10() {
         String[] steps = SLDResource.makeColors("#ffffff", "#ff0000", 10);
         assertEquals("#FFFFFF", steps[0]);
         assertEquals("#FFE2E2", steps[1]);
         assertEquals("#FFC6C6", steps[2]);
         assertEquals("#FFAAAA", steps[3]);
         assertEquals("#FF8D8D", steps[4]);
         assertEquals("#FF7171", steps[5]);
         assertEquals("#FF5555", steps[6]);
         assertEquals("#FF3838", steps[7]);
         assertEquals("#FF1C1C", steps[8]);
         assertEquals("#FF0000", steps[9]);
     }
     
     
 }
