 package org.bails.element;
 
 import org.bails.stream.BailsStreamSTAX;
 import org.bails.stream.IBailsStream;
import org.bails.test.TestBailsStreamFactory;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.ByteArrayInputStream;
 import java.nio.charset.Charset;
 
 import static junit.framework.Assert.*;
 
 /**
  * @author Karl Bennett
  */
 public class PageTest {
 
     private static IBailsStream stream;
 
     @Before
     public void initStream() {
         stream = new BailsStreamSTAX(new ByteArrayInputStream(
                TestBailsStreamFactory.XML_DCOUMENT.getBytes(Charset.forName("UTF8"))));
     }
 
     @After
     public void closeStream() {
         stream.close();
         stream = null;
     }
 
     @Test
     public void testRender() throws Exception {
         Page testPage = new TestPage(stream);
 
         assertEquals("page bails is correct", TestPage.class.getSimpleName(), testPage.getBailsId());
 
         Page otherTestPage = new TestPage();
 
         assertEquals("test pages equal", testPage, otherTestPage);
 
         Page stubPage = new StubPage();
 
         assertFalse("stub page not equal to test page", testPage.equals(stubPage));
 
         System.out.println(testPage.render());
     }
 
     private static class TestPage extends Page {
 
         public TestPage() {}
 
         public TestPage(IBailsStream stream) {
             super(stream);
         }
 
         @Override
         public void initialise() {
            add(new TestElement(TestBailsStreamFactory.TEST_BAILS_ID));
         }
     }
 
     private static class StubPage extends Page {
 
         @Override
         public void initialise() {
         }
     }
 
     private static class TestElement extends Element {
 
         public TestElement(String bailsId) {
             super(bailsId);
         }
 
         @Override
         public String render() {
             return getMarkupElement().toString();
         }
     }
 }
