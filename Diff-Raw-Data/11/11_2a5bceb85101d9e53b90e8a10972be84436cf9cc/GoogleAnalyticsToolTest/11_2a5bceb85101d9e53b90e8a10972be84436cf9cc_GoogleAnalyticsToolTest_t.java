 package org.otherobjects.cms.tools;
 
 import junit.framework.TestCase;
 
 public class GoogleAnalyticsToolTest extends TestCase
 {
     public void testGetPath()
     {
         GoogleAnalyticsTool gat = new GoogleAnalyticsTool();
 
         // External URLs
         assertEquals("/outgoing/www.google.com/", gat.getPath("http://www.google.com/"));
         assertEquals("/outgoing/www.google.com/news/", gat.getPath("http://www.google.com/news/"));
         assertEquals("/outgoing/www.google.com/news/test.html", gat.getPath("http://www.google.com/news/test.html"));
         assertEquals("/outgoing/www.google.com/news/test.html", gat.getPath("http://www.google.com/news/test.html?a=b"));
 
         // Mailto links
         assertEquals("/outgoing/mailto/othermedia.com/rich", gat.getPath("mailto:rich@othermedia.com"));
         assertEquals("/outgoing/mailto/othermedia.com/rich", gat.getPath("mailto:rich@othermedia.com?subject=not-logged"));
 
         // Internal links -- do not track
        assertEquals("", gat.getPath("/"));
        assertEquals("", gat.getPath("/home.html"));
     }
 }
