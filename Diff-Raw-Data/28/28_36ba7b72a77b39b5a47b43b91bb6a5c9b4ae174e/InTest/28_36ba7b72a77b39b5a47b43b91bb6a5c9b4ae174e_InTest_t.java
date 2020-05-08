 package com.wiredmind.booleanengine.relations;
 
 import org.apache.commons.chain.Context;
 import org.apache.commons.chain.impl.ContextBase;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 /**
  * @author Craig Earley
  */
 public class InTest {
 
     public InTest() {
     }
 
     Context context = new ContextBase();
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     @Test
     @SuppressWarnings("unchecked")
     public void testCanCompareNumbers() throws Exception {
         double number = 123;
         context.put("property", number);
 
         In instance = new In("property", "100", "123.00", "1000");
         assertTrue(instance.isTruthValue(context));
 
         instance = new In("property", "1", "2", "122.999999");
         assertFalse(instance.isTruthValue(context));
     }
 
     @Test
     @SuppressWarnings("unchecked")
     public void testCanCompareDates() throws ParseException, Exception {
         DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
         Date date = df.parse("15/01/2009");
         context.put("property", date);
 
        In instance = new In("property", "01/01/2020", "15/01/2009", "01/01/2020");
         assertTrue(instance.isTruthValue(context));
 
         instance = new In("property", "01/01/2020", "02/01/2020", "03/01/2020");
         assertFalse(instance.isTruthValue(context));
     }
 
     @Test
     @SuppressWarnings("unchecked")
     public void testCanCompareCalendars() throws ParseException, Exception {
         Calendar calendar = Calendar.getInstance();
         calendar.set(2009, 0, 15); // month value is 0-based
         context.put("property", calendar);
 
        In instance = new In("property", "01/01/2020", "15/01/2009", "01/01/2020");
         assertTrue(instance.isTruthValue(context));
 
         instance = new In("property", "01/01/2020", "02/01/2020", "03/01/2020");
         assertFalse(instance.isTruthValue(context));
     }
 
     @Test
     @SuppressWarnings("unchecked")
     public void testCanCompareStrings() throws Exception {
         String s = "Apple";
         context.put("property", s);
 
        In instance = new In("property", "orange", "apple", "PC");
         assertTrue(instance.isTruthValue(context));
 
         instance = new In("property", "CRAY", "PC");
         assertFalse(instance.isTruthValue(context));
     }
 }
