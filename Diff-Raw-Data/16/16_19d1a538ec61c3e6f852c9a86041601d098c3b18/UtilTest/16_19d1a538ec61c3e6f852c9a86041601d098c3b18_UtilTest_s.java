 package eionet.util;
 
 import junit.framework.TestCase;
 
 /**
  *
  * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
  *
  */
 public class UtilTest extends TestCase {
 
     /**
      *
      *
      */
     public void test_replaceTags() {
         assertEquals(Util.processForDisplay("http://cdr.eionet.europa.eu/search?y=1&z=2"),
         "<a href=\"http://cdr.eionet.europa.eu/search?y=1&amp;z=2\">http://cdr.eionet.europa.eu/search?y=1&amp;z=2</a>");
 
         // Test simple &
         assertEquals(Util.processForDisplay("Fruit & Vegetables"), "Fruit &amp; Vegetables");
 
         // Test newline
         assertEquals(Util.processForDisplay("Fruit\nVegetables"), "Fruit<br/>Vegetables");
 
         // Don't create anchors = true
         assertEquals(Util.processForDisplay("http://cdr.eionet.europa.eu/search?y=1&z=7", true),
         "http://cdr.eionet.europa.eu/search?y=1&amp;z=7");
 
         // Test Unicode char
        assertEquals(Util.processForDisplay("€"), "€");
 
         // Test HTML tags
         assertEquals(Util.processForDisplay("<div class='Apostrophs'>"), "&lt;div class=&#039;Apostrophs&#039;&gt;");
         assertEquals(Util.processForDisplay("<div class=\"Quotes\">"), "&lt;div class=&quot;Quotes&quot;&gt;");
     }
 
     /**
      *
      *
      */
     public void test_isURI() {
         assertTrue(Util.isURI("http://cdr.eionet.europa.eu/"));
         assertTrue(Util.isURI("ftp://ftp.eionet.europa.eu/"));
         assertTrue(Util.isURI("XXX"));
     }
 }
