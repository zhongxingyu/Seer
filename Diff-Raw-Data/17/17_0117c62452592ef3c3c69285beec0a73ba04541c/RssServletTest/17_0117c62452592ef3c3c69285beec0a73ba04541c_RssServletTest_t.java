 package tests;
 
 import junit.framework.TestCase;
 import rss.RssServlet;
 
 /**
  * Created by IntelliJ IDEA.
  * User: mkotsur
  * Date: Dec 12, 2009
  * Time: 11:08:00 PM
  * To change this template use File | Settings | File Templates.
  */
 public class RssServletTest extends TestCase {
 
     public void testSmoke() {
         RssServlet servlet = new RssServlet();
        assertEquals('1', '1');
     }
 
 }
 
