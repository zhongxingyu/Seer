 package de.flower.rmt.ui.app;
 
 import de.flower.rmt.test.AbstractRMTWicketIntegrationTests;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.util.ReflectionTestUtils;
 import org.testng.annotations.Test;
 
 import static org.testng.Assert.*;
 
 /**
  * @author flowerrrr
  */
 public class WicketLinkProviderTest extends AbstractRMTWicketIntegrationTests {
 
     @Autowired
     private WicketLinkProvider linkProvider;
 
     /**
      * Verify that bookmarkable urls are generated the way we expect it.
      */
     @Test
     public void testDeepLink() {
         Long eventId = 1L;
         CharSequence url = linkProvider.deepLinkEvent(eventId);
         assertEquals(url, "http://localhost/context/servlet/player/event/1");
         BookmarkablePageLink link = Links.eventLink("id", eventId);
         url = (CharSequence) ReflectionTestUtils.invokeGetterMethod(link, "getURL");
        assertEquals(url.toString(), "./player/event/1"); // ./ prefix required after upgrading to wicket 1.5.7
 
     }
 }
