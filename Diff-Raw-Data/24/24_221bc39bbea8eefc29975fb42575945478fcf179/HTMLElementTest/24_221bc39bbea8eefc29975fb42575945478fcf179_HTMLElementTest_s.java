 package org.bails.element;
 
 import org.bails.stream.BailsStreamSTAX;
 import org.bails.stream.IBailsStream;
import org.bails.test.TestBailsStreamFactory;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.ByteArrayInputStream;
 import java.nio.charset.Charset;
 
 /**
  * @author Karl Bennett
  */
 public class HTMLElementTest {
 
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
 
         System.out.println(testPage.render());
     }
 
     private static class TestPage extends Page {
 
 
         public TestPage(IBailsStream stream) {
             super(stream);
         }
 
         @Override
         public void initialise() {
            add(new HTMLElement(TestBailsStreamFactory.TEST_BAILS_ID, "Some new text."));
         }
     }
 }
