 package com.abudko.scheduled.service.huuto;
 
import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 public class PublishManagerUtilsTest {
     
     private PublishManagerUtils utils = new PublishManagerUtils();
 
     @Test
     public void testGetId() {
         assertEquals("999", utils.getId("2-dspik  sxs2323 332323 [999]"));
     }
     
     @Test
     public void testGetIdNegative() {
         assertNull(utils.getId("2-dspik  sxs2323 332323 9t26723"));
     }
     
     @Test
    public void testGetIdNegative2() {
        assertNull(utils.getId("2-dspik  sxs2323 332323 [skdjhbk9]"));
     }
     
     @Test
    public void testGetIdNegative3() {
        assertNotNull(utils.getId("[82372082372038972309782902790273237]"));
     }
 }
