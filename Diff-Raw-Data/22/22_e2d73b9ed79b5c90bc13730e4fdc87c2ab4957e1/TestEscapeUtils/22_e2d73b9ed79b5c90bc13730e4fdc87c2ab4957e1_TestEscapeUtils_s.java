 package com.atlassian.sal.core.pluginsettings;
 
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 
 public class TestEscapeUtils
 {
     @Test
     public void testNoEscape()
     {
         String value = "this is the value";
         assertEquals(value, EscapeUtils.escape(value));
     }
 
     @Test
     public void testEscape()
     {
         String value = "this is \fthe \nvalue";
         assertEquals("this is \\fthe \\nvalue", EscapeUtils.escape(value));
     }
 
     @Test
     public void testUnescape()
     {
         String value = "this \\fis \\nthe value";
         assertEquals("this \fis \nthe value", EscapeUtils.unescape(value));
     }
 
     @Test
     public void testUnescapeNonescape()
     {
         String value = "this \nis the \fvalue";
         assertEquals(value, EscapeUtils.unescape(value));
     }
 }
