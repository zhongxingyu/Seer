 package org.codehaus.xsite.extractors;
 
 import junit.framework.TestCase;
 
 import org.codehaus.xsite.model.Page;
 
 /**
  * @author J&ouml;rg Schaible
  */
 public class SiteMeshPageExtractorTest extends TestCase {
 
 
     private SiteMeshPageExtractor pageExtractor;
 
     protected void setUp() throws Exception {
         super.setUp();
         pageExtractor = new SiteMeshPageExtractor();
     }
 
     public void testPageBodyStartingWithHeader() {
         final String html = "<html><head><title>JUnit</title></head><body><h1>Header</h1></body></html>";
         final Page page = pageExtractor.extractPage("JUnit.html", html);
         assertEquals("JUnit", page.getTitle());
        assertEquals("<h1 class=\"FirstChild\">Header</h1>", page.getBody());
    }

    public void testLinksWillNotBeSharedBetweenPages() {
        final String html1 = "<html><head><title>JUnit</title></head><body><a href=\"foo.html\">Header</a></body></html>";
        final String html2 = "<html><head><title>Test</title></head><body><p>Header</p></body></html>";
        final Page page1 = pageExtractor.extractPage("JUnit.html", html1);
        final Page page2 = pageExtractor.extractPage("Test.html", html2);
        assertEquals(1, page1.getLinks().size());
        assertEquals(0, page2.getLinks().size());
     }
 }
