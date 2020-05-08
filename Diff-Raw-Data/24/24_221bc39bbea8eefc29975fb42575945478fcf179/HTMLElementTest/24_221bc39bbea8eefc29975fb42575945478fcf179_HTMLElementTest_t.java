 package org.bails.element;
 
 import org.bails.stream.BailsStreamSTAX;
 import org.bails.stream.IBailsStream;
import org.bails.test.TestBailsTestUtil;
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
                TestBailsTestUtil.XML_DCOUMENT.getBytes(Charset.forName("UTF8"))));
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
            HTMLElement element = new HTMLElement(TestBailsTestUtil.TEST_BAILS_ID_ONE);
            element.add(new HTMLElement(TestBailsTestUtil.TEST_BAILS_ID_TWO, "Some new text."));

            add(element);
         }
     }
 }
