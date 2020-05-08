 package cz.muni.fi.pb138.log4jconverter.configuration;
 
 import java.util.ArrayList;
import java.util.Collections;
 import java.util.HashMap;
 import org.junit.*;
 import org.omg.CORBA.ExceptionList;
 
 import cz.muni.fi.pb138.log4jconverter.configuration.Appender;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author Jonge
  */
 public class AppenderTest {
     
     @Test
     public void AppenderSetGetTest() {
         Appender appender1 = new Appender();
         Appender appender2 = new Appender();
         
         // Name test
         appender1.setAppenderName("Appender1");
         appender2.setAppenderName("Appender2");
         assertEquals(appender1.getAppenderName(), "Appender1");
         assertEquals(appender2.getAppenderName(), "Appender2");
         
         appender1.setAppenderName("");
         appender2.setAppenderName(null);
         assertEquals(appender1.getAppenderName(), "");
         assertNull(appender2.getAppenderName());
         
         // File size
         HashMap<String, String> params = new HashMap<String, String>();
         params.put("MaxFileSize", "20KB");
         appender1.setParams(params);
         assertEquals(appender1.getParams(), params);
         //appender2.setMaxFileSize("30KB");
         //assertEquals(appender1.getMaxFileSize(), "30KB");
         
         //appender1.setMaxFileSize("");
         //appender2.setMaxFileSize(null);
         //assertEquals(appender1.getMaxFileSize(), "");
         //assertNull(appender2.getMaxFileSize());
     }
     
     @Test
     public void testFilterOrdering1(){
     	Appender a = new Appender();
     	Filter f1 = new Filter();
     	Filter f2 = new Filter();
     	Filter f3 = new Filter();
     	Filter f4 = new Filter();
     	Filter f5 = new Filter();
 
     	a.addFilter(f1);
     	a.addFilter(f2);
     	a.addFilter(f3);
     	a.addFilter(f4);
     	a.addFilter(f5);
     	
     	ArrayList<Filter> filters = a.getFilters();
 
     	assertEquals(f1,filters.get(0));
     	assertEquals(f2,filters.get(1));
     	assertEquals(f3,filters.get(2));
     	assertEquals(f4,filters.get(3));
     	assertEquals(f5,filters.get(4));
     }
     
     @Test
     public void testFilterOrdering2(){
     	Appender a = new Appender();
     	Filter f1 = new Filter();
     	f1.setName("01");
     	Filter f2 = new Filter();
     	f2.setName("02");
     	Filter f3 = new Filter();
     	f3.setName("00");
     	Filter f4 = new Filter();
     	f4.setName("04");
     	Filter f5 = new Filter();
     	f5.setName("03");
 
     	a.addFilter(f1);
     	a.addFilter(f2);
     	a.addFilter(f3);
     	a.addFilter(f4);
     	a.addFilter(f5);
     	
     	ArrayList<Filter> filters = a.getFilters();
    	Collections.sort(filters);
    	
 
     	assertEquals(f1,filters.get(1));
     	assertEquals(f2,filters.get(2));
     	assertEquals(f3,filters.get(0));
     	assertEquals(f4,filters.get(4));
     	assertEquals(f5,filters.get(3));
     }
     
     @Test
     public void testFilterOrdering4(){
     	Appender a = new Appender();
     	Filter f1 = new Filter();
     	Filter f2 = new Filter();
    	f2.setName("00");

     	a.addFilter(f1);
     	a.addFilter(f2);
 
     	try{
    		Collections.sort(a.getFilters());
     	}catch (IllegalArgumentException e){
     		// OK
     	}
     }
 }
