 package org.got5.tapestry5.jquery.test;
 
 import org.apache.tapestry5.test.SeleniumTestCase;
 import org.testng.annotations.Test;
 
 public class SelectorTest extends SeleniumTestCase{
 	
 	@Test
     public void testSlider()
     {
 		open("/test/Selector");
 		
		assertEquals(getText("//div[@id='test']"),"$('#test')");		
         
     }
 
 }
