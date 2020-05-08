 /*
  * Created on 8 Jan 2008
  */
 package uk.org.ponder.rsf.test.render;
 
 import uk.org.ponder.rsf.bare.RenderResponse;
 import uk.org.ponder.rsf.bare.junit.MultipleRSFTests;
 
 /** Test for ID NPE RSF-71, Verbatim RSF-85, UIMessage RSF-73, 
  * Relation target RSF-77, UISelect grouping RSF-108
  */
 
 public class TestRender extends MultipleRSFTests {
   
   public TestRender() {
     contributeRequestConfigLocation("classpath:uk/org/ponder/rsf/test/render/render-request-context.xml");
     contributeConfigLocation("classpath:uk/org/ponder/rsf/test/render/render-application-context.xml");
   }
    
   public void testRender() {
     RenderResponse response = getRequestLauncher().renderView();
     assertContains(response, "Test Message ID View");
     assertContains(response, "</div>");
     assertContains(response, "id=\"titleclosed\"");
     assertContains(response, "<i style=\"color");
     assertContains(response, "Referenced value");
     assertContains(response, "Remanent text");
    
     assertContains(response, "for=\"row::target\"");
     assertContains(response, "for=\"row:1:target\"");
    
     assertContains(response, "<optgroup label=\"A\">");
     assertContains(response, "<optgroup label=\"B\">");
   }
 }
