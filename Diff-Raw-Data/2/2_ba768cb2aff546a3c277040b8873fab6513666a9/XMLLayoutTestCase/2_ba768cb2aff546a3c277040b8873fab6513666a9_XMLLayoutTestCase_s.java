 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apache.log4j.xml;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.apache.log4j.FileAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.MDC;
 import org.apache.log4j.util.Compare;
 import org.apache.log4j.util.Filter;
 import org.apache.log4j.util.JunitTestRunnerFilter;
 import org.apache.log4j.util.LineNumberFilter;
 import org.apache.log4j.util.SunReflectFilter;
 import org.apache.log4j.util.Transformer;
 import org.apache.log4j.util.XMLLineAttributeFilter;
 import org.apache.log4j.util.XMLSequenceNumberFilter;
 import org.apache.log4j.util.XMLTimestampFilter;
 import org.apache.log4j.xml.XMLLayout;
 
 
 public class XMLLayoutTestCase extends TestCase {
   static String TEMP = "output/temp";
   static String FILTERED = "output/filtered";
   Logger root;
   Logger logger;
 
   public XMLLayoutTestCase(String name) {
     super(name);
   }
 
   public void setUp() {
     root = Logger.getRootLogger();
     logger = Logger.getLogger(XMLLayoutTestCase.class);
   }
 
   public void tearDown() {
     root.getLoggerRepository().resetConfiguration();
   }
 
   public void basic() throws Exception {
     XMLLayout xmlLayout = new XMLLayout();
     root.addAppender(new FileAppender(xmlLayout, TEMP, false));
     common();
     Transformer.transform(
       TEMP, FILTERED,
       new Filter[] {
         new LineNumberFilter(),
         new JunitTestRunnerFilter(),
         new XMLTimestampFilter(), 
         new XMLSequenceNumberFilter(),
         new SunReflectFilter()
       });
     assertTrue(Compare.compare(FILTERED, "witness/xml/xmlLayout.1"));
   }
 
   public void locationInfo() throws Exception {
     XMLLayout xmlLayout = new XMLLayout();
     xmlLayout.setLocationInfo(true);
     root.addAppender(new FileAppender(xmlLayout, TEMP, false));
     common();
     Transformer.transform(
       TEMP, FILTERED,
       new Filter[] {
         new LineNumberFilter(), 
         new JunitTestRunnerFilter(),
         new XMLTimestampFilter(),
         new XMLSequenceNumberFilter(),
         new XMLLineAttributeFilter(), new SunReflectFilter()
       });
     assertTrue(Compare.compare(FILTERED, "witness/xml/xmlLayout.2"));
   }
 
   public void testCDATA() throws Exception {
     XMLLayout xmlLayout = new XMLLayout();
     xmlLayout.setLocationInfo(true);
     root.addAppender(new FileAppender(xmlLayout, TEMP, false));
 
     logger.debug("Message with embedded <![CDATA[<hello>hi</hello>]]>.");
 
     Transformer.transform(
       TEMP, FILTERED,
       new Filter[] {
         new LineNumberFilter(), 
         new JunitTestRunnerFilter(),
         new XMLTimestampFilter(),
         new XMLSequenceNumberFilter(),
         new XMLLineAttributeFilter(), new SunReflectFilter()
       });
     assertTrue(Compare.compare(FILTERED, "witness/xml/xmlLayout.3"));
   }
 
   public void testNull() throws Exception {
     XMLLayout xmlLayout = new XMLLayout();
     root.addAppender(new FileAppender(xmlLayout, TEMP, false));
     logger.debug("hi");
     logger.debug(null);
 
     Exception e = new Exception((String) null);
     logger.debug("hi", e);
     Transformer.transform(
       TEMP, FILTERED,
       new Filter[] { new LineNumberFilter(), 
           new JunitTestRunnerFilter(),
           new SunReflectFilter(),
           new XMLTimestampFilter(),  
           new XMLSequenceNumberFilter() });
     assertTrue(Compare.compare(FILTERED, "witness/xml/xmlLayout.null"));
   }
 
   /**
    * Tests the format of the MDC portion of the layout to ensure
   * the KVP's we put in turn up in the output file.
    * @throws Exception
    */
   public void testMDC() throws Exception {
     XMLLayout xmlLayout = new XMLLayout();
     root.addAppender(new FileAppender(xmlLayout, TEMP, false));
 
     MDC.clear();
     MDC.put("key1", "val1");
     MDC.put("key2", "val2");
 
     logger.debug("Hello");
     Transformer.transform(
       TEMP, FILTERED,
       new Filter[] { new LineNumberFilter(), 
           new JunitTestRunnerFilter(),
           new XMLTimestampFilter(), 
           new XMLSequenceNumberFilter()});
     assertTrue(Compare.compare(FILTERED, "witness/xml/xmlLayout.mdc.1"));
   }
 
   public void holdTestMDCEscaped() throws Exception {
     XMLLayout xmlLayout = new XMLLayout();
     root.addAppender(new FileAppender(xmlLayout, TEMP, false));
 
     MDC.clear();
     MDC.put("blahAttribute", "<blah value=\"blah\">");
     MDC.put("<blahKey value=\"blah\"/>", "blahValue");
 
     logger.debug("Hello");
     Transformer.transform(
       TEMP, FILTERED,
       new Filter[] { new LineNumberFilter(), 
           new JunitTestRunnerFilter(),
           new XMLTimestampFilter(),
           new XMLSequenceNumberFilter() });
     assertTrue(Compare.compare(FILTERED, "witness/xml/xmlLayout.mdc.2"));
   }
 
   void common() {
     int i = -1;
 
     X x = new X();
 
     logger.debug("Message " + ++i);
     root.debug("Message " + i);
 
     logger.info("Message " + ++i);
     root.info("Message " + i);
 
     logger.warn("Message " + ++i);
     root.warn("Message " + i);
 
     logger.error("Message " + ++i);
     root.error("Message " + i);
 
     logger.log(Level.FATAL, "Message " + ++i);
     root.log(Level.FATAL, "Message " + i);
 
     Exception e = new Exception("Just testing");
     logger.debug("Message " + ++i, e);
     root.debug("Message " + i, e);
 
     logger.error("Message " + ++i, e);
     root.error("Message " + i, e);
   }
 
   public static Test suite() {
     TestSuite suite = new TestSuite();
     suite.addTest(new XMLLayoutTestCase("basic"));
     suite.addTest(new XMLLayoutTestCase("locationInfo"));
     suite.addTest(new XMLLayoutTestCase("testCDATA"));
     suite.addTest(new XMLLayoutTestCase("testNull"));
     suite.addTest(new XMLLayoutTestCase("testMDC"));
 
     return suite;
   }
 
   class X {
     Logger logger = Logger.getLogger(X.class);
 
     public X() {
       logger.info("in X() constructor");
     }
   }
 }
