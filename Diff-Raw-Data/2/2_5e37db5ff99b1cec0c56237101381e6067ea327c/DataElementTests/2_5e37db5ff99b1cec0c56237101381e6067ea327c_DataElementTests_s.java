 /*
  * $Id$
  *
  * Copyright 2004 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.tests.client;
 
 import java.util.Iterator;
 import java.util.List;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.xins.client.DataElement;
 
 /**
 * Tests for class <code>XINSCallResultParser</code>.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  */
 public class DataElementTests extends TestCase {
 
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
       return new TestSuite(DataElementTests.class);
    }
 
    /**
     * Determines the number of elements returned by the specified
     * <code>Iterator</code>.
     *
     * <p>TODO: Move this utility function to a utility class.
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
     * Constructs a new <code>DataElementTests</code> test suite with
     * the specified name. The name will be passed to the superconstructor.
     *
     * @param name
     *    the name for this test suite.
     */
    public DataElementTests(String name) {
       super(name);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Tests the behaviour of the <code>DataElement.QualifiedName</code> class.
     *
     * @throws Exception
     *    if an unexpected exception is thrown.
     */
    public void testDataElementQualifiedName() throws Exception {
 
       DataElement.QualifiedName qn1, qn2, qn3;
 
       String uri = "SomeURI";
       String localName = "SomeName";
 
       try {
          new DataElement.QualifiedName(null, null);
          fail("DataElement.QualifiedName(null, null) should throw an IllegalArgumentException.");
       } catch (IllegalArgumentException e) {
          // as expected
       }
 
       try {
          new DataElement.QualifiedName(uri, null);
          fail("DataElement.QualifiedName(\"" + uri + "\", null) should throw an IllegalArgumentException.");
       } catch (IllegalArgumentException e) {
          // as expected
       }
 
       qn1 = new DataElement.QualifiedName(null, localName);
       assertEquals(null,      qn1.getNamespaceURI());
       assertEquals(localName, qn1.getLocalName());
 
       qn2 = new DataElement.QualifiedName(null, localName);
       assertEquals(qn1, qn1);
       assertEquals(qn1, qn2);
       assertEquals(qn2, qn1);
       assertEquals(qn2, qn2);
 
       qn3 = new DataElement.QualifiedName("", localName);
       assertEquals(null,      qn1.getNamespaceURI());
       assertEquals(localName, qn1.getLocalName());
       assertEquals(qn1, qn2);
       assertEquals(qn1, qn3);
       assertEquals(qn2, qn1);
       assertEquals(qn2, qn3);
       assertEquals(qn3, qn1);
       assertEquals(qn3, qn2);
 
       qn1 = new DataElement.QualifiedName(uri, localName);
       assertEquals(uri,       qn1.getNamespaceURI());
       assertEquals(localName, qn1.getLocalName());
 
       qn2 = new DataElement.QualifiedName(uri, localName);
       assertEquals(qn1, qn2);
       assertEquals(qn2, qn1);
    }
 }
