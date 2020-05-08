 package com.solidstategroup.radar.test.model;
 
 import com.solidstategroup.radar.model.Demographics;
 import org.junit.Test;
 
 import java.text.SimpleDateFormat;
 
 import static junit.framework.Assert.assertEquals;
 
 public class TestDemographics {
 
     @Test
     public void testGetAge() throws Exception {
         // Set up demographics
         Demographics demographics = new Demographics();
         demographics.setDateOfBirth(new SimpleDateFormat("dd/MM/yyyy").parse("15/04/1984"));
 
        // Assert that we get the correct age
        // Todo: This will break soon (on my birthday)
        assertEquals("Incorrect age", new Integer(27), demographics.getAge());
     }
 }
