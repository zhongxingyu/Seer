 /*
  * Created On:  3-Dec-06 11:37:05 AM
  */
 package com.thinkparity.codebase.model.util.xstream;
 
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.thoughtworks.xstream.XStream;
 
 /**
  * <b>Title:</b><br>
  * <b>Description:</b><br>
  * @author raymond@thinkparity.com
  * @version 1.1.2.1
  */
 public final class XStreamMapTest extends XStreamTestCase {
 
     private static final String NAME = "XStream Map Test";
 
     private Fixture datum;
 
     /**
      * Create XStreamMapTest.
      *
      * @param name
      */
     public XStreamMapTest() {
         super(NAME);
     }
 
     public void testMapGeneration() {
         final Writer writer = new StringWriter();
         new XStream().toXML(datum.map, writer);
         logger.logInfo("writer.toString():{0}", writer.toString());
     }
 
     /**
      * @see com.thinkparity.codebase.model.util.xstream.XStreamTestCase#setUp()
      *
      */
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         final Map<String, Integer> map = new HashMap<String, Integer>(5, 1.0F);
         map.put("three", 3);
         map.put("one", 1);
         map.put("four", 4);
         map.put("one", 1);
         map.put("five", 5);
         map.put("nine", 9);
         map.put("two", 2);
        map.put("six", 6);
         map.put("five", 5);
         map.put("four", 4);
         datum = new Fixture(map);
     }
 
     /**
      * @see com.thinkparity.codebase.model.util.xstream.XStreamTestCase#tearDown()
      *
      */
     @Override
     protected void tearDown() throws Exception {
         super.tearDown();
     }
 
     private final class Fixture extends XStreamTestCase.Fixture {
         private final Map<String, Integer> map;
         private Fixture(final Map<String, Integer> map) {
             this.map = map;
         }
     }
 }
