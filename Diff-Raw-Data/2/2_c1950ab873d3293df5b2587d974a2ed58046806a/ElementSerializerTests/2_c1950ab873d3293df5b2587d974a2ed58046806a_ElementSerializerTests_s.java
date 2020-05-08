 /*
  * $Id$
  *
  * Copyright 2003-2007 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.tests.common.xml;
 
 import java.io.StringReader;
 import java.util.List;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 import org.xins.common.text.ParseException;
 import org.xins.common.xml.ElementBuilder;
 
 import org.xins.common.xml.Element;
 import org.xins.common.xml.ElementSerializer;
 
 /**
  * Tests for class <code>ElementSerializer</code>.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
  */
 public class ElementSerializerTests extends TestCase {
 
    /**
     * Constructs a new <code>ElementSerializerTests</code> test suite with
     * the specified name. The name will be passed to the superconstructor.
     *
     * @param name
     *    the name for this test suite.
     */
    public ElementSerializerTests(String name) {
       super(name);
    }
 
    /**
     * Returns a test suite with all test cases defined by this class.
     *
     * @return
     *    the test suite, never <code>null</code>.
     */
    public static Test suite() {
       return new TestSuite(ElementSerializerTests.class);
    }
 
    /**
     * Tests the <code>ElementSerializer</code> class.
     */
    public void testElementSerializer() throws Exception {
 
       // Build an element
       ElementBuilder builder = new ElementBuilder("b", "a");
       builder.setAttribute("c", "2");
       Element element = builder.createElement();
 
       ElementSerializer serializer = new ElementSerializer();
 
       // Serialize it
      String expected = "<ns0:a xmlns:ns0=\"b\" c=\"2\"/>";
       String actual   = serializer.serialize(element);
       assertEquals(expected, actual);
 
       // Repeat that
       actual = serializer.serialize(element);
       assertEquals(expected, actual);
    }
 }
