 package com.atlassian.plugin.servlet.filter;
 
 import junit.framework.TestCase;
 import com.atlassian.plugin.PluginParseException;
 
 public class TestFilterLocation extends TestCase
 {
     public void testParse()
     {
         assertEquals(FilterLocation.AFTER_ENCODING, FilterLocation.parse("after-encoding"));
         assertEquals(FilterLocation.AFTER_ENCODING, FilterLocation.parse("after_encoding"));
         assertEquals(FilterLocation.AFTER_ENCODING, FilterLocation.parse("After-Encoding"));
         try
         {
             FilterLocation.parse(null);
             fail();
         } catch (IllegalArgumentException ex)
         {
             // test passed
         }
         try
         {
             FilterLocation.parse("asf");
             fail();
         } catch (IllegalArgumentException ex)
         {
             // test passed
         }
 
     }
 }
