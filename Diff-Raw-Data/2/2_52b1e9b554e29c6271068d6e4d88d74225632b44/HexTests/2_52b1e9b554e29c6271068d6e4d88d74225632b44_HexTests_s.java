 package org.xins.tests.common.types.standard;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 import org.apache.tools.ant.filters.HeadFilter;
 
 import org.xins.common.types.TypeValueException;
 import org.xins.common.types.standard.Base64;
 import org.xins.common.types.standard.Hex;
 
 
 /**
 * Tests for class <code>Base64</code>.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:anthony.goubard@orange-ft.com">Anthony Goubard</a>
  */
 public class HexTests extends TestCase {
 
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
       return new TestSuite(HexTests.class);
    }
 
 
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>HexTests</code> test suite with
     * the specified name. The name will be passed to the superconstructor.
     *
     * @param name
     *    the name for this test suite.
     */
    public HexTests(String name) {
       super(name);
    }
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Tests if fromStringForRequired() throws an IllegalArgumentException when the string is null
     */
    public void testFromStringForRequired1() throws TypeValueException {
       try {
          Hex.SINGLETON.fromStringForRequired(null);
          fail("Expected IllegalArgumentException when arg is null");
       } catch (IllegalArgumentException iae) {
          //ignore this because it means the test passed
       }
    }
 
    /**
     * Tests if fromStringForRequired() throws an TypeValueException when the string is not valid
     */
    public void testFromStringForRequired2() {
       try {
          String string = "1a2b3c4d5e6f7g";
          Hex.fromStringForRequired(string);
          fail("Expected TypeValueException when string is not valid");
       } catch (TypeValueException tve){
          //ignore this because it means the test passed
       }
    }
 
    /**
     * Tests if fromStringForOptional returns null when the parameter is null
     */
    public void testFromStringForOptional() throws TypeValueException {
       assertEquals(null, Hex.fromStringForOptional(null));
    }
 
 
    /**
     * Tests if the class functions of Hex do their job,passing a hex sting->byte[]->the same hex string
     */
    public void testToString1() throws TypeValueException {
 
       /*this test fails if the string has odd chars because the method that converts the byte
         to a hex string adds at the end of the string a 0.Actually this not a mistake,it is ok for but i just wanted
         u to see it. */
       String string = "1a2b3c4d5e6f74";
       byte[] result = Hex.fromStringForRequired(string);
       assertEquals("toString should return the same hex", string, Hex.toString(result));
    }
 
    /**
     * Tests if toString() returns null when the parameter is null.
     * Function toString() takes the null byte[] value  returned by fromStringOptional() and toString() returns also null
     */
    public void testToString2() throws TypeValueException {
       byte[]result = Hex.fromStringForOptional(null);
       assertEquals(null, Hex.toString(result));
    }
 
    /**
     * Tests if the method isValidValueImpl() returns false if the string is not hex
     */
    public void testIsValidValueImpl1() {
       String string = "12345adklm";
       assertTrue(!Hex.SINGLETON.isValidValue(string));
    }
 
    /**
     * Tests if the method isValidValueImpl() returns false if the byte is not at at the specified range
     */
    public void testIsValidValueImpl2() {
       Hex single = new ShortBinary();
       String string = "12345ad";
       assertTrue("hex is expected to be at the specified range", !single.isValidValue(string));
    }
 
    /**
     * Tests if the method isValidValueImpl() returns true for the correct hex string
     */
    public void testIsValidValueImpl3() {
       String string = "12345adcef";
       assertTrue("the string is expected to be of a hex type", Hex.SINGLETON.isValidValue(string));
    }
 
    /**
     * Tests if fromStringImpl() throws an TypeValueException when the string is not valid
     */
    public void testFromStringImpl()
    throws TypeValueException {
       try {
          String string = "1a2b3c4d5e6f7g";
          Object object = Hex.SINGLETON.fromString(string);
          fail("Expected TypeValueException when sting is not valid");
       } catch (TypeValueException tve) {
          //ignore this because it means the test passed
       }
    }
 
    /**
     * Tests if methods toString() and fromStringImpl() work properly
     */
    public void testToStringObject()
    throws TypeValueException {
 
       // the test fails if the string has odd chars
       String string = "1a2b3c4d5e6f72";
       Object object = Hex.SINGLETON.fromString(string);
       assertEquals("toString should return the same string", string, Hex.SINGLETON.toString(object));
    }
 
    /**
     * Tests if method toString() throws an exception when the parameter is null
     */
    public void testToStringWithNull()
    throws IllegalArgumentException,TypeValueException {
       try {
          Hex.SINGLETON.toString((Object) null);
          fail("Expected IllegalArgumentException when parameter-object is null");
       } catch (IllegalArgumentException iae) {
          //ignore this because it means the test passed
       }
    }
 
    class ShortBinary extends Hex {
 
       // constructor
       public ShortBinary() {
          super("ShortBinary", 0, 3);
       }
 
   }
 }
