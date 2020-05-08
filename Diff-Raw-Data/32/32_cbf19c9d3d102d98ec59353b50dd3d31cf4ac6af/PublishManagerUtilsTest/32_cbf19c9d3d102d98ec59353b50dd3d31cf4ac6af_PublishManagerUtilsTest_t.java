 package com.abudko.scheduled.service.huuto;
 
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
 
 import org.junit.Test;
 
 public class PublishManagerUtilsTest {
     
     private PublishManagerUtils utils = new PublishManagerUtils();
 
     @Test
     public void testGetId() {
         assertEquals("999", utils.getId("2-dspik  sxs2323 332323 [999]"));
     }
     
     @Test
    public void testGetIdDash() {
        assertEquals("522123-9990", utils.getId("2-dspik  sxs2323 332323 [522123-9990]"));
    }
    
    @Test
    public void testGetIdDashWord() {
        assertEquals("522123-RED", utils.getId("2-dspik  sxs2323 332323 [522123-RED]"));
    }
    
    @Test
     public void testGetIdNegative() {
         assertNull(utils.getId("2-dspik  sxs2323 332323 9t26723"));
     }
     
     @Test
    public void testGetIdWords() {
        assertEquals("skdjhbk9", utils.getId("2-dspik  sxs2323 332323 [skdjhbk9]"));
     }
     
     @Test
    public void testGetIdDigits() {
        final String descriptionId = "82372082372038972309782902790273237";
        assertEquals(descriptionId, utils.getId(String.format("[%s]", descriptionId)));
     }
 }
