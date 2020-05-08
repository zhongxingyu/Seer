 package com.solidstategroup.radar.test.model;
 
 import com.solidstategroup.radar.model.Demographics;
import org.joda.time.DateTime;
import org.joda.time.Years;
 import org.junit.Test;
 
 import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
 
 import static junit.framework.Assert.assertEquals;
 
 public class TestDemographics {
 
     @Test
     public void testGetAge() throws Exception {
         // Set up demographics
         Demographics demographics = new Demographics();
         demographics.setDateOfBirth(new SimpleDateFormat("dd/MM/yyyy").parse("15/04/1984"));
 
         Integer age = Years.yearsBetween(new DateTime(new SimpleDateFormat("dd/MM/yyyy").parse("15/04/1984")),
                 new DateTime(new Date())).getYears();
         assertEquals("Incorrect age",age, demographics.getAge());
     }
 }
