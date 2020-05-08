 package dk.statsbiblioteket.util.xml;
 
import dk.statsbiblioteket.util.Files;
 import dk.statsbiblioteket.util.Strings;
 import junit.framework.TestCase;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
import java.io.IOException;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 
 import static dk.statsbiblioteket.util.xml.DOM.*;
 
 /**
  * Test cases for the {@code DOM.select*} methods
  */
 public class DOMSelectTest extends TestCase {
 
     static final String SIMPLE_XML =
         DOM.XML_HEADER +
        "<body>"+
         "  <double>1.1234</double>"+
         "  <boolean>true</boolean>"+
         "  <string>foobar</string>"+
         "  <integer>27</integer>"+
         "</body>";
     static final String BIG_XML;
     static {
         BIG_XML = Strings.flushLocal(
                     ClassLoader.getSystemResourceAsStream("data/big.xml"));
     }
 
     Document dom;
 
     public void setUp() {
         clearXPathCache();
         dom = stringToDOM(SIMPLE_XML);
         assertNotNull(dom);
     }
 
      public void testSelectInteger() {
         Integer i = selectInteger(dom, "asdfg");
         assertEquals(null, i);
 
         i = selectInteger(dom, "asdfg", 1);
         assertEquals(1, i.intValue());
 
         i = selectInteger(dom, "/body/integer");
         assertEquals(27, i.intValue());
     }
 
     public void testSelectDouble() {
         Double d = selectDouble(dom, "asdfg");
         assertEquals(null, d);
         
         d = selectDouble(dom, "asdfg", 1.1);
         assertEquals(1.1, d);
 
         d = selectDouble(dom, "/body/double");
         assertEquals(1.1234, d);
     }
 
     public void testSelectBoolean() {
         Boolean b = selectBoolean(dom, "asdfg");
         assertEquals(Boolean.FALSE, b);
 
         b = selectBoolean(dom, "asdfg", false);
         assertEquals(Boolean.FALSE, b);
 
         b = selectBoolean(dom, "asdfg", null);
         assertEquals(null, b);
 
         b = selectBoolean(dom, "/body/boolean");
         assertEquals(Boolean.TRUE, b);
     }
 
     public void testSelectString() {
         String s = selectString(dom, "asdfg");
         assertEquals("", s);
 
         s = selectString(dom, "asdfg", "sbutil");
         assertEquals("sbutil", s);
 
         s = selectString(dom, "asdfg", null);
         assertEquals(null, s);
 
         s = selectString(dom, "/body/string");
         assertEquals("foobar", s);
 
         s = selectString(dom, "/body/string", "baz");
         assertEquals("foobar", s);
     }
 
     public void testSelectNode() {
         Node n = selectNode(dom, "asdfg");
         assertEquals(null, n);
 
         n = selectNode(dom, "/body");
         assertSame(dom.getFirstChild(), n);
     }
 
     public void testSelectNodeList() {
         NodeList l = selectNodeList(dom, "asdfg");
         assertEquals(0, l.getLength());
 
         // We use /body/node() because /body/* doesn't select the text nodes
        l = selectNodeList(dom, "/body/node()");
         NodeList expected = dom.getFirstChild().getChildNodes(); 
         assertSame(expected.getLength(), l.getLength());
        assertEquals(8, l.getLength());
     }
 
     public void threadTest(final boolean blowCache) throws Exception {
         Thread[] threads = new Thread[20];
         final List<Throwable> errors =
                 Collections.synchronizedList(new LinkedList<Throwable>());
         final Random random = new Random();
 
         for (int i = 0; i < threads.length; i++) {
             threads[i] = new Thread(new Runnable() {
                 @Override
                 public void run() {
                     for (int j = 0; j < 50; j++) {
                         testSelectBoolean();
                         testSelectDouble();
                         testSelectInteger();
                         testSelectNode();
                         testSelectNodeList();
                         testSelectString();
 
                         if (blowCache) {
                             for (int k = 0; k < 50; k++) {
                                 String bleh = selectString(
                                         dom, "/body/a" + random.nextInt(),
                                         "bleh, no such node");
                                 assertEquals("bleh, no such node", bleh);
                             }
                         }
                     }
                 }
             });
             threads[i].setUncaughtExceptionHandler(
                     new Thread.UncaughtExceptionHandler(){
                         @Override
                         public void uncaughtException(Thread thread,
                                                       Throwable throwable) {
                             errors.add(throwable);
                         }
                     });
         }
 
         for (Thread t : threads) t.start();
         for (Thread t : threads) t.join();
 
         if (errors.size() > 0) {
             for (Throwable t : errors) t.printStackTrace();
             fail("Uncaught exceptions in threads");
         }
     }
 
     public void testThreadsBlowCache() throws Exception {
         threadTest(true);
     }
 
     public void testThreadsWithCache() throws Exception {
         threadTest(false);
     }
 }
