 package ar.noxit.ehockey;
 
import ar.noxit.ehockey.web.pages.ExamplePage;
 import org.apache.wicket.util.tester.WicketTester;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 /**
  * Simple test using the WicketTester
  */
 public class TestHomePage {
 
     private WicketTester tester;
 
     @BeforeMethod
     public void setUp() {
         tester = new WicketTester();
     }
 
     @Test
     public void testRenderMyPage() {
         // start and render the test page
        tester.startPage(ExamplePage.class);
 
         // assert rendered page class
        tester.assertRenderedPage(ExamplePage.class);
     }
 }
