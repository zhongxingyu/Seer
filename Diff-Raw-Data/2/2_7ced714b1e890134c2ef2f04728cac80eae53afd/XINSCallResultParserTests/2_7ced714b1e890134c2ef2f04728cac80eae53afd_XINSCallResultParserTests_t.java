 /*
  * $Id$
  */
 package org.xins.tests.client;
 
 import java.util.Iterator;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.xins.common.collections.PropertyReader;
 
 import org.xins.common.text.ParseException;
 
 import org.xins.client.DataElement;
 import org.xins.client.XINSCallResultData;
 import org.xins.client.XINSCallResultParser;
 
 /**
  * Tests for class <code>XINSCallResultParser</code>.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  */
 public class XINSCallResultParserTests extends TestCase {
 
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
       return new TestSuite(XINSCallResultParserTests.class);
    }
 
    /**
     * Determines the number of elements returned by the specified
     * <code>Iterator</code>.
     *
     * @param iterator
     *    the {@link Iterator} to determine the number of elements in, or
     *    <code>null</code>.
     *
     * @return
     *    the number of elements in the {@link Iterator}, or <code>0</code> if
     *    <code>iterator == null</code>.
     */
    private static int iteratorSize(Iterator iterator) {
 
       // Short-circuit if argument is null
       if (iterator == null) {
          return 0;
       }
 
       // Loop through the elements
       int count = 0;
       while (iterator.hasNext()) {
          iterator.next();
          count++;
       }
 
       return count;
    }
 
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>XINSCallResultParserTests</code> test suite with
     * the specified name. The name will be passed to the superconstructor.
     *
     * @param name
     *    the name for this test suite.
     */
    public XINSCallResultParserTests(String name) {
       super(name);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Tests the behaviour of <code>XINSCallResultParser</code>, method
     * parse(byte[]) behaviour, in general and specifically also with regard to
     * parsing output parameters.
     *
     * @throws Exception
     *    if an unexpected exception is thrown.
     */
    public void testParseXINSCallResult1() throws Exception {
 
       XINSCallResultParser parser = new XINSCallResultParser();
 
       // Prepare the string to parse
       final String ENCODING = "UTF-8";
       String xml;
       XINSCallResultData result;
       PropertyReader params;
 
       // Passing null: Should fail
       try {
          parser.parse(null);
          fail("Passing <null> to XINSCallResultParser.parse(byte[]) should throw an IllegalArgumentException.");
       } catch (IllegalArgumentException ex) {
          // as expected
       }
 
       // Only a product element: Should fail
       xml = "<product/>";
       try {
          parser.parse(xml.getBytes(ENCODING));
          fail("Root element 'product' should cause XINSCallResultParser.parse(byte[]) to throw a ParseException.");
       } catch (ParseException ex) {
          // as expected
       }
 
       // Only a result element: Should succeed
       xml = "<result/>";
       parser.parse(xml.getBytes(ENCODING));
 
       // Empty keys, empty values, non-conflicting duplicates
       xml = "<result><param/><param name='a'/><param>b</param><param name='c'>z</param><param name='c'>z</param></result>";
       result = parser.parse(xml.getBytes(ENCODING));
       assertNotNull(result);
       assertEquals(null, result.getErrorCode());
       params = result.getParameters();
       assertNotNull(params);
       assertEquals(1, params.size());
       assertEquals(null, params.get("a"));
       assertEquals(null, params.get("b"));
       assertEquals("z", params.get("c"));
 
       // Conflicting duplicate should fail
       xml = "<result><param name='c'>1st value</param><param name='c'>2nd value</param></result>";
       try {
          parser.parse(xml.getBytes(ENCODING));
          fail("Conflicting values for parameter should cause XINSCallResultParser.parse(byte[]) to throw a ParseException.");
       } catch (ParseException ex) {
          // as expected
       }
 
       // Unknown element as child of result should be ignored
       xml = "<result><extra /><param name='a'>1</param></result>";
       result = parser.parse(xml.getBytes(ENCODING));
       assertNotNull(result);
       assertEquals(null, result.getErrorCode());
       params = result.getParameters();
       assertNotNull(params);
       assertEquals(1, params.size());
       assertEquals("1", params.get("a"));
 
    }
 
    /**
     * Tests the behaviour of <code>XINSCallResultParser</code>, method
     * parse(byte[]) behaviour with regard to parsing data sections.
     *
     * @throws Exception
     *    if an unexpected exception is thrown.
     */
    public void testParseXINSCallResult2() throws Exception {
 
       XINSCallResultParser parser = new XINSCallResultParser();
 
       // Prepare the string to parse
       final String ENCODING = "UTF-8";
       String xml;
       XINSCallResultData result;
       PropertyReader params;
 
       // The parser should not return null
       xml = "<?xml version=\"1.0\" encoding='" + ENCODING + "' ?>" +
             " <result><data>" +
             "<product available='false' name=\"FOO\" />" + 
             " <product available=\"true\"  name=\"BAR\" />" +
             "</data></result>";
       result = parser.parse(xml.getBytes(ENCODING));
       assertNotNull(result);
 
       // There should be no error code
       assertNull(result.getErrorCode());
 
       // There should be no parameters
       params = result.getParameters();
       assertTrue(params == null || params.size() == 0);
 
       // There should be a data section
       DataElement dataElement = result.getDataElement();
       assertNotNull(dataElement);
 
       // Root element should be <data/>
       assertEquals("data", dataElement.getName());
 
       // There should be no attributes in the root element
       Iterator dataElementAttributes = dataElement.getAttributes();
       assertEquals(0, iteratorSize(dataElementAttributes));
 
       // There should be 2 child elements
       Iterator children = dataElement.getChildren();
       assertEquals(2, iteratorSize(children));
 
       // Get both child elements
       children = dataElement.getChildren();
       DataElement childOne = (DataElement) children.next();
       DataElement childTwo = (DataElement) children.next();
 
       // First element should have 2 attributes
       assertEquals(2, iteratorSize(childOne.getAttributes()));
 
       // There should be an 'available' attribute and a 'name' attribute
       Iterator childOneAttributes = childOne.getAttributes();
       String attrOne1 = (String) childOneAttributes.next();
       String attrOne2 = (String) childOneAttributes.next();
       assertTrue(attrOne1.equals("available") || attrOne1.equals("name"));
       assertTrue(attrOne2.equals("available") || attrOne2.equals("name"));
       assertFalse(attrOne1.equals(attrOne2));
 
       // The 'name' attribute must be 'FOO'
       assertEquals("FOO", childOne.get("name"));
 
       // The 'available' attribute must be 'false'
       assertEquals("false", childOne.get("available"));
 
       // Second element should have 2 attributes
       assertEquals(2, iteratorSize(childTwo.getAttributes()));
 
       // There should be an 'available' attribute and a 'name' attribute
       Iterator childTwoAttributes = childTwo.getAttributes();
       String attrTwo1 = (String) childTwoAttributes.next();
       String attrTwo2 = (String) childTwoAttributes.next();
       assertTrue(attrTwo1.equals("available") || attrTwo1.equals("name"));
       assertTrue(attrTwo2.equals("available") || attrTwo2.equals("name"));
       assertFalse(attrTwo1.equals(attrTwo2));
 
       // The 'name' attribute must be 'BAR'
       assertEquals("BAR", childTwo.get("name"));
 
       // The 'available' attribute must be 'false'
       assertEquals("true", childTwo.get("available"));
   }
 
    /**
     * Tests the behaviour of <code>XINSCallResultParser</code>, method
     * parse(byte[]) behaviour with regard to error codes.
     *
     * @throws Exception
     *    if an unexpected exception is thrown.
     */
    public void testParseXINSCallResult3() throws Exception {
 
       XINSCallResultParser parser = new XINSCallResultParser();
 
       // Prepare the string to parse
       final String ENCODING = "UTF-8";
       String xml;
       XINSCallResultData result;
       PropertyReader params;
 
       // Result with error code defined in 'errorcode' attribute
       xml = "<result errorcode='SomeError'/>";
       result = parser.parse(xml.getBytes(ENCODING));
       assertNotNull(result);
       assertEquals("SomeError", result.getErrorCode());
 
       // Result with error code defined in 'code' attribute
       xml = "<result code='SomeError'/>";
       result = parser.parse(xml.getBytes(ENCODING));
       assertNotNull(result);
       assertEquals("SomeError", result.getErrorCode());
 
       // Error code both in 'error' and in 'code' attribute (non-conflicting)
       xml = "<result code='SomeError' errorcode='SomeError'/>";
       result = parser.parse(xml.getBytes(ENCODING));
       assertNotNull(result);
       assertEquals("SomeError", result.getErrorCode());
 
       // Error code both in 'error' and in 'code' attribute (conflicting)
      xml = "<result code='SomeError' errorcode='SomethingElse'/>";
       try {
          parser.parse(xml.getBytes(ENCODING));
          fail("Conflicting values for error code should cause XINSCallResultParser.parse(byte[]) to throw a ParseException.");
       } catch (ParseException ex) {
          // as expected
       }
    }
 }
