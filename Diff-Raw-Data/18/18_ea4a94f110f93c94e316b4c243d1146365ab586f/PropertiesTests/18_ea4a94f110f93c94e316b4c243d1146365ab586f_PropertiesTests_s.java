 /*
  * $Id$
  */
 package org.xins.tests.common.types.standard;
 
 import java.net.URLEncoder;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 import org.xins.types.TypeValueException;
 import org.xins.types.standard.Properties;
 import org.xins.util.collections.PropertyReader;
 
 /**
  * Tests for class <code>Properties</code>.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  */
 public class PropertiesTests extends TestCase {
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Returns a test suite with all test cases defined by this class.
     *
     * @return
     *    the test suite, never <code>null</code>.
     */
    public static Test suite() {
       return new TestSuite(PropertiesTests.class);
    }
 
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>PropertiesTests</code> test suite with
     * the specified name. The name will be passed to the superconstructor.
     *
     * @param name
     *    the name for this test suite.
     */
    public PropertiesTests(String name) {
       super(name);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Performs setup for the tests.
     */
    protected void setUp() {
       // empty
    }
 
    private void reset() {
       // empty
    }
 
    public void testFromString() throws Throwable {
 
       final Properties TYPE = Properties.SINGLETON;
 
       // Test the SISO principle (Shit In Shit Out)
       assertEquals(null, TYPE.fromString(null));
 
       // Test passing an empty string in
       PropertyReader pr = (PropertyReader) TYPE.fromString("");
       assertTrue(! pr.getNames().hasNext());
 
       // Test passing some values in
       String name1 = "a0@ %",          value1 = name1;
       String name2 = "Z&"                            ;
       String name3 = "-123^  \t\n",    value3 = " "  ;
 
       String string = URLEncoder.encode(name1) + '=' + URLEncoder.encode(value1)
              + '&' + URLEncoder.encode(name2)
               + '&' + URLEncoder.encode(name3) + '=' + URLEncoder.encode(value3);
 
       pr = (PropertyReader) TYPE.fromString(string);
 
       Iterator iterator = pr.getNames();
       int count = 0;
       Map m = new HashMap();
       while (iterator.hasNext()) {
          String name = (String) iterator.next();
          m.put(name, pr.get(name));
          count++;
       }
       assertEquals(2, count);
 
      assertEquals(name1, value1);
      assertEquals(name2, null);
      assertEquals(name3, value3);
 
      String string2 = string + "&&c=d";
       try {
          TYPE.fromString(string2);
          fail("Expected Properties.SINGLETON.fromString(\"" + string2 + "\") to throw a TypeValueException.");
       } catch (TypeValueException exception) { /* as expected */ }
    }
 }
